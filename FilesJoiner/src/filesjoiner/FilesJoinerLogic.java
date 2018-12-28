/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesjoiner;

import com.opencsv.CSVReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
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

        executorService = Executors.newSingleThreadExecutor();
        future = executorService.submit(new Runnable() {
            public void run() {
                LogicSingleton.setCountToZero();
                headers = new HashMap<String, Integer>();
                initHeaders();
                for (int i = 0; i < files.size(); i++) {
                    ExtendedFile file = detectHeaders(files.get(i));
                    if (file != null) {
                        files.set(i, file);
                    }
                }
                normalizeHeaders();
                scrapeDataFromCsvFiles();
                if (parent.getCbRemoveDuplicates().isSelected()) {
                    removeDuplicates();
                }
                countItems();
                saveDataToFile();
                for (ExtendedFile file : files) {
                    file.separator = ",";
                }
                removeTempFiles();
            }
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

        Thread producerThread = new Thread() {
            @Override
            public void run() {
               
            }
        };
        producerThread.start();
    }

    private void countItems() {
        parent.getlblUrlsCountData().setText(String.valueOf(resultList.size()));
    }
    
    private void removeTempFiles() {
        for (int i = 0; i < files.size(); i++) {
            ExtendedFile file = files.get(i);
            if (file.isTempFile) {
                file.delete();
            }
        }
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

    private void saveDataToFile() {
        StringBuilder sb = new StringBuilder();
        HashMap<String, Integer> sortedHeaders = sortHashMapByValues(headers);
        int counter = sortedHeaders.entrySet().size();
        for (Map.Entry<String, Integer> entry : sortedHeaders.entrySet()) {
            sb.append("\"" + entry.getKey() + "\"");
            counter--;
            if (counter != 0) {
                sb.append(",");
            }
        }
        sb.append("\n");
        for (String[] row : resultList) {
            for (String string : row) {
                String content = StringUtils.isEmpty(string) ? "" : string;
                sb.append("\"" + content + "\"");
                sb.append(",");
            }
            sb.append("\n");
        }
        try {
            DateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            Date date = new Date();
            String pathToSave = outputPath.replace(".", "") +File.separator+ "Merged data_"+sdf.format(date)+".csv";
            Files.write(Paths.get(pathToSave), sb.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException ex) {
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setOutputPath(String path) {
        if (outputPath == null) {
              outputPath = path;
        }
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

    private void scrapeDataFromCsvFiles() {
        Map.Entry<String, Integer> maxEntry = null;
        for (Map.Entry<String, Integer> entry : headers.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
        resultList = new ArrayList<String[]>();
        for (ExtendedFile file : files) {
            try {
                String[] nextRecord;
                int counter = 0;
                CSVReader csvReader = file.getCsvReader();
                while ((nextRecord = csvReader.readNext()) != null) {
                    if (counter == 0 && file.hasHeader) {
                        counter = -1;
                        continue;
                    }
                    String[] row = new String[maxEntry.getValue() + 1];
                    for (int i = 0; i < headers.size(); i++) {
                        Entry<String, Integer> itemFrom = getKeysByValue(file.headersPositionsFrom, i);
                        if (itemFrom != null) {
                            Integer index = 0;
                            try {
                                index = file.headersPositionsTo.get(itemFrom.getKey());
                                if (nextRecord.length > itemFrom.getValue()) {
                                    row[index] = nextRecord[itemFrom.getValue()];
                                }
                            } catch (Exception ex) {
                                Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                    resultList.add(row);
                }
            } catch (IOException ex) {
                Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private boolean isFileHasHeaders(String[] row, String separator) {
        for (String string : row) {
            for (String cell : string.split(separator)) {
                if (headers.containsKey(cell)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static String[] validateHeaders(String[] row, String separator) {
        ArrayList<String> headers = new ArrayList<String>();
        for (String string : row) {
            int counter = 0;
            for (String cell : string.split(separator)) {
                if (DataHelper.validateURLs(cell)) {
                    headers.add("Website");
                }
                else if (DataHelper.validateEmails(cell)) {
                    headers.add("Email");
                }
                else {
                    headers.add("UnknownHeader" + (counter + 1));
                }
                counter++;
            }
        }
        return headers.toArray(new String[headers.size()]);
    }
    
    private ExtendedFile createTempCsvFile(ExtendedFile file) {
        StringBuilder sb = new StringBuilder();
        int counter = file.headers.length;
        String pathToSave = null;
        for (String entry : file.headers) {
            sb.append("\"" + entry + "\"");
            counter--;
            if (counter != 0) {
                sb.append(",");
            }
        }
        try {
            sb.append("\n");
            CSVReader csvReader = file.getCsvReader();
            List<String[]> rows = csvReader.readAll();
            for (String[] row : rows) {
                for (String string : row) {
                    int commasCounter = file.headers.length;
                    for (String cell : string.split(file.separator)) {
                        String content = StringUtils.isEmpty(cell) ? "" : cell;
                        sb.append("\"" + content + "\"");
                        commasCounter--;
                        if (commasCounter != 0) {
                            sb.append(",");
                        }
                    }
                }
                sb.append("\n");
            }
            pathToSave = outputPath.replace(".", "") + File.separator + "tmp_" + FilenameUtils.getName(file.getAbsolutePath()) + ".csv";
            Files.write(Paths.get(pathToSave), sb.toString().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException ex) {
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ExtendedFile(pathToSave).markFileAsTemp();
    }

    private ExtendedFile detectHeaders(ExtendedFile inFile) {
        ExtendedFile file = null;
        try {
            if (FilenameUtils.getExtension(inFile.getAbsolutePath()).equalsIgnoreCase("txt")) {
                file = inFile;
            } else {
                CSVReader processedCsvReader = inFile.getCsvReader();
                String[] processedFirstRow = processedCsvReader.readAll().get(0);
                if (processedFirstRow.length > 1) {
                    inFile.headers = processedFirstRow;
                    inFile.hasHeader = true;
                    return inFile;
                }
            }
            CSVReader csvReader = file.getCsvReader();
            boolean isFirstRow = true;
            List<String[]> data = csvReader.readAll();
            if (data.size() == 0) {
                return null;
            }
            String[] firstRow = data.get(0);
            if (firstRow.length == 1) {
                String row = firstRow[0];
                if (row.split("\\t{1,5}").length > 1) {
                    file.separator = "\\t{1,5}";
                }
                if (row.split(" {2,5}").length > 1) {
                    file.separator = " {2,5}";
                }
                if (row.split(",").length > 1) {
                    file.separator = ",";
                }
                if (isFirstRow) {
                    file.hasHeader = isFileHasHeaders(firstRow, file.separator);
                    if (!file.hasHeader) {
                        file.headers = validateHeaders(firstRow, file.separator);
                    }
                    isFirstRow = false;
                    file = new ExtendedFile(createTempCsvFile(file));
                    return detectHeaders(file);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void normalizeHeaders() {
        for (ExtendedFile file : files) {
            int counter = 0;
            if (file.headers == null) {
                return;
            }
            for (String fileHeader : file.headers) {
                if (fileHeader.equalsIgnoreCase("")) {
                    continue;
                }
                file.headersPositionsFrom.put(fileHeader, counter);
                counter++;
                Object value = null;
                for (Map.Entry<String, Integer> entry : headers.entrySet()) {
                    if (fileHeader.replace(" ", "").toLowerCase().contains(entry.getKey().toLowerCase())
                            || entry.getKey().replace(" ", "").toLowerCase().contains(fileHeader.toLowerCase())) {
                        value = entry;
                        file.headersPositionsTo.put(fileHeader, entry.getValue());
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
                    file.headersPositionsTo.put(fileHeader, maxEntry.getValue() + 1);
                    headers.put(fileHeader, maxEntry.getValue() + 1);
                }
            }
        }
    }

    private void initHeaders() {
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
    
    private boolean isContainsSameURL(String[] from, ArrayList<String[]> result){
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
