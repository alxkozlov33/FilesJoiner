/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesjoiner;

import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author Ihor
 */
public class ExtendedFile extends File {

    static ExtendedFile DEFAULT;
    private CSVReader csvReader = null;

    public ExtendedFile(String pathname) {
        super(pathname);
        headersPositionsTo = new HashMap<String, Integer>();
        headersPositionsFrom = new HashMap<String, Integer>();
        hasHeader = true;
    }
    
    public ExtendedFile(ExtendedFile obj) {
        super(obj.getAbsolutePath());
        headersPositionsTo = obj.headersPositionsTo;
        headersPositionsFrom = obj.headersPositionsFrom;
        hasHeader = obj.hasHeader;
        headers = obj.headers;
        isTempFile = obj.isTempFile;
    }
    
    public Map<String, Integer> headersPositionsTo;
    public Map<String, Integer> headersPositionsFrom;
    public boolean hasHeader;
    public boolean isTempFile;
    public String txtSeparator = ",";
    public char csvSeparator = ',';
    public String[] headers;
    
    public ExtendedFile markFileAsTemp(){
        this.isTempFile = true;
        return this;
    }

    public CSVReader getCsvReader() {
        Reader reader;
        try {
            if (FilenameUtils.getExtension(this.getAbsolutePath()).equalsIgnoreCase("csv")) {
                reader = Files.newBufferedReader(Paths.get(this.getAbsolutePath()));
                csvReader = new CSVReader(reader, this.csvSeparator);
            } else if (FilenameUtils.getExtension(this.getAbsolutePath()).equalsIgnoreCase("xlsx")
                    || FilenameUtils.getExtension(this.getAbsolutePath()).equalsIgnoreCase("xls")) {

                InputStream inp = new FileInputStream(this);
                Workbook wb = WorkbookFactory.create(inp);
                csvReader = new CSVReader(new StringReader(echoAsCSV(wb.getSheetAt(0))));
                inp.close();

            } else if (FilenameUtils.getExtension(this.getAbsolutePath()).equalsIgnoreCase("txt")) {
                InputStream inp = new FileInputStream(this);
                reader = Files.newBufferedReader(Paths.get(this.getAbsolutePath()));
                csvReader = new CSVReader(reader);
                inp.close();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FilesJoinerLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return csvReader;
    }

    private String echoAsCSV(Sheet sheet) {
        String result = "";
        Row row = null;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            row = sheet.getRow(i);
            for (int j = 0; j < row.getLastCellNum(); j++) {
                result += "\"" + row.getCell(j) + "\",";
            }
            result += "\n";
        }
        return result;
    }
}
