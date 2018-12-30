/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesjoiner;

import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author ibodia
 */
public class FilesJoinerLogic {

    ArrayList<ExtendedFile> files;
    MainFrameGUI parent;
    ArrayList<String[]> resultList;
    String outputPath = null;
    HashMap<String, Integer> headers = new HashMap<String, Integer>();
    ExecutorService executorService;
    File propertiesFile;
    public Future<?> future;

    public FilesJoinerLogic(MainFrameGUI parent) {
        this.parent = parent;
        initHeaders();
    }

    public void initFilesList(ArrayList<ExtendedFile> inputFiles) {
        if (inputFiles != null) {
            files = inputFiles;
        }
    }

    public void StartRun() {
        if (files == null) {
            return;
        }
        LogicSingleton.setCountToZero();
            
            files.forEach(file -> {
                try {
                    file.initFile();
                } catch (IOException ex) {
                    Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            normalizeHeaders();
            scrapeDataFromCsvFiles();
            if (parent.getCbRemoveDuplicates().isSelected()) {
                removeDuplicates();
            }
            countItems();
            ArrayList<Integer> columns = getSelectableColumns();
            String data = writeDataToString();
            removeEmptyColumns(data, columns);
            saveDataToFile();
        executorService = Executors.newSingleThreadExecutor();
        future = executorService.submit(() -> {
//            LogicSingleton.setCountToZero();
//            
//            files.forEach(file -> {
//                try {
//                    file.initFile();
//                } catch (IOException ex) {
//                    Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            });
//            normalizeHeaders();
//            scrapeDataFromCsvFiles();
//            if (parent.getCbRemoveDuplicates().isSelected()) {
//                removeDuplicates();
//            }
//            countItems();
//            ArrayList<Integer> columns = getSelectableColumns();
//            String data = writeDataToString();
//            removeEmptyColumns(data, columns);
//            saveDataToFile();
        });

        Thread seeker = new Thread() {
            public void run() {
                parent.getBtnProcessFiles().setEnabled(false);
                parent.getlblUrlsCountData().setText("Processing...");
                while (true) {
                    if (future.isDone()) {
                        break;
                    }
                }
                parent.getBtnProcessFiles().setEnabled(true);
            }
        };
        seeker.start();
    }

    private void countItems() {
        ArrayList<String> urls = new ArrayList<String>();
        for (String[] row : resultList) {
            if (!StringUtils.isEmpty(row[0])) {
                urls.add(row[0]);
            }
        }
        parent.getlblUrlsCountData().setText(Integer.toString(urls.size()));
    }

    public static <String, Integer> Entry<String, Integer> getKeysByValue(Map<String, Integer> map, Integer value) {
        Entry<String, Integer> resEntry = null;
        for (Entry<String, Integer> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                resEntry = entry;
            }
        }
        return resEntry;
    }
    
    private void removeEmptyColumns(String data, ArrayList<Integer> indexesToSelect) {
        CsvParserSettings settings = new CsvParserSettings();
        RowListProcessor rowProcessor = new RowListProcessor();
        settings.setProcessor(rowProcessor);
        settings.setNullValue("");
        settings.setEmptyValue("");
        settings.setHeaderExtractionEnabled(true);
        settings.setAutoConfigurationEnabled(true);
        Integer[] stockArr = new Integer[indexesToSelect.size()];
        stockArr = indexesToSelect.toArray(stockArr);
        Arrays.sort(stockArr);
        settings.selectIndexes(stockArr);
        
        CsvParser parser = new CsvParser(settings);
        this.resultList = (ArrayList<String[]>) parser.parseAll(new ByteArrayInputStream(data.getBytes()));
        if (ExtendedFile.isFileHasHeaders(rowProcessor.getHeaders())) {
            this.headers = new HashMap<String, Integer>();
            String[] newHeaders = rowProcessor.getHeaders();
            for (int i = 0; i < newHeaders.length; i++) {
                if (indexesToSelect.contains(i)) {
                    this.headers.put(newHeaders[i], i);
                }
            }
        }
        
    }
    
    public String writeDataToString() {
        StringBuilder sb = new StringBuilder();
        HashMap<String, Integer> sortedHeaders = sortHashMapByValues(headers);
        int counter = sortedHeaders.entrySet().size();
        for (Map.Entry<String, Integer> entry : sortedHeaders.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\"");
            counter--;
            if (counter != 0) {
                sb.append(",");
            }
        }
        sb.append("\n");
        for (String[] row : resultList) {
            for (int i = 0; i < row.length; i++) {
                String content = StringUtils.isEmpty(row[i]) ? "" : row[i];
                sb.append("\"").append(content).append("\"");
                if (i != (row.length - 1)) {
                    sb.append(",");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private void saveDataToFile() {
        try {
            DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date date = new Date();
            String names = " " + sdf.format(date) + "_";
            switch (files.size()) {
                case 1:
                    names += files.get(0).getCutterFilename();
                    break;
                case 2:
                    names += files.get(0).getCutterFilename() + "...+" + files.get(1).getCutterFilename();
                    break;
                default:
                    for (int i = 0; i < 3; i++) {
                        if (i < files.size() && files.get(i) != null) {
                            names += files.get(i).getCutterFilename();
                            if (i < 2 && i < files.size()) {
                                names += "...+";
                            }
                        }
                    }   break;
            }
            if ((files.size() - 3) > 0) {
                names += "...+";
                names += (files.size() - 3) + "_more";
            }
            String pathToSave = outputPath + File.separator + names + ".csv";
            Files.write(Paths.get(pathToSave), writeDataToString().toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException ex) {
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setOutputPath(String path) {
        outputPath = path;
    }

    private LinkedHashMap<String, Integer> sortHashMapByValues(
            HashMap<String, Integer> passedMap) {
        ArrayList<String> mapKeys = new ArrayList<String>(passedMap.keySet());
        ArrayList<Integer> mapValues = new ArrayList<Integer>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap<String, Integer> sortedMap
                = new LinkedHashMap<>();

        Iterator<Integer> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Integer val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                Integer comp1 = passedMap.get(key);
                Integer comp2 = val;

                if (comp1.equals(comp2)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }
    
    private ArrayList<Integer> getSelectableColumns() {
        ArrayList<Integer> columnIndexes = new ArrayList<Integer>();
        for (int j = 0; j < headers.size(); j++) {
            boolean isColumnEmpty = true;
            for (int i = 0; i < resultList.size(); i++) {
                if (!StringUtils.isBlank(resultList.get(i)[j])) {
                    isColumnEmpty = false;
                    break;
                }
            }
            if (isColumnEmpty) {
                columnIndexes.add(j);
            }
        }
        
        ArrayList<Integer> columnIndexesToSelect = new ArrayList<Integer>();
        HashMap<String, Integer> headersCopy = new HashMap<String, Integer>(this.headers); 
        for (Integer index : columnIndexes) {
            if (headersCopy.containsValue(index)) {
                Entry<String, Integer> entry = getKeysByValue(headersCopy, index);
                headersCopy.remove(entry.getKey());
            }
        }
        for (Entry<String, Integer> entry : headersCopy.entrySet()) {
            columnIndexesToSelect.add(entry.getValue());
        }

        return columnIndexesToSelect;
    }

    private void scrapeDataFromCsvFiles() {
        Map.Entry<String, Integer> maxEntry = null;
        for (Map.Entry<String, Integer> entry : headers.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
        resultList = new ArrayList<String[]>();
        for (ExtendedFile file : files) {
            for (String[] record : file.getLines()) {

                String[] row = new String[maxEntry.getValue() + 1];
                for (int i = 0; i < headers.size(); i++) {
                    Entry<String, Integer> itemFrom = getKeysByValue(file.headersPositionsFrom, i);
                    if (itemFrom != null) {
                        Integer index = 0;
                        try {
                            index = file.headersPositionsTo.get(itemFrom.getKey());
                            if (record.length > itemFrom.getValue()) {
                                row[index] = record[itemFrom.getValue()];
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                String[] newRow = Arrays.copyOf(row, row.length + 1);
                newRow[row.length] = file.getAbsolutePath();
                resultList.add(newRow);
            }
        }
        headers.put("File path", headers.size());
    }

    private void normalizeHeaders() {
        for (ExtendedFile file : files) {
            for (Map.Entry<String, Integer> fileHeaderEntry : file.headersPositionsFrom.entrySet()) {
                Object value = null;
                for (Map.Entry<String, Integer> entry : headers.entrySet()) {
                    if (fileHeaderEntry.getKey().replace(" ", "").toLowerCase().contains(entry.getKey().toLowerCase()) || 
                            entry.getKey().replace(" ", "").toLowerCase().contains(fileHeaderEntry.getKey().toLowerCase())) {
                        value = entry;
                        file.headersPositionsTo.put(fileHeaderEntry.getKey(), entry.getValue());
                        break;
                    }
                }

                if (value == null) {
                    Map.Entry<String, Integer> maxEntry = null;
                    for (Map.Entry<String, Integer> entry : headers.entrySet()) {
                        if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                            maxEntry = entry;
                        }
                    }
                    file.headersPositionsTo.put(fileHeaderEntry.getKey(), maxEntry.getValue() + 1);
                    headers.put(fileHeaderEntry.getKey(), maxEntry.getValue() + 1);
                }
            }
        }
    }

    private void initHeaders() {
        headers = new HashMap<String, Integer>();
        headers.put("Website", 0);
        headers.put("Email", 1);
        headers.put("First Name", 2);
        headers.put("Last Name", 3);
        headers.put("Full Name", 4);
        headers.put("Position", 5);
        headers.put("Telephone", 6);
        headers.put("Address", 7);
        headers.put("City", 8);
        headers.put("Country", 9);
        headers.put("Company Name", 10);
        headers.put("Industry", 11);
        headers.put("Yearly Revenue", 12);
        headers.put("Notes", 13);
        headers.put("Instagram", 14);
        headers.put("LinkedIn", 15);
        headers.put("VerifyStatus", 16);
        headers.put("Company Size", 17);
    }

    private void removeDuplicates() {
        ArrayList<String[]> result = new ArrayList<String[]>();
        for (int i = 0; i < resultList.size(); i++) {
            if (!isContainsSameURL(resultList.get(i), result)) {
                result.add(resultList.get(i));
            }
        }
        resultList = result;
    }

    private boolean isContainsSameURL(String[] from, ArrayList<String[]> result) {
        boolean flag = false;
        try {
            if (StringUtils.isEmpty(from[0])) {
                return true;
            }
            for (String[] strings : result) {
                for (String string : strings) {
                    if (string == null) {
                        continue;
                    }
                    if (string.equalsIgnoreCase(from[0])) {
                        flag = true;
                        return flag;
                    }
                }
            }
        } catch (NullPointerException ex) {
            System.out.print(ex);
        }
        return flag;
    }
}
