/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filesjoiner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Ihor
 */
public class DataHelper {
    
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX
            = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    
    public static final Pattern VALID_WEBSITE_ADDRESS_REGEX
            = Pattern.compile("^(http:\\/\\/www\\.|https:\\/\\/www\\.|http:\\/\\/|https:\\/\\/)?[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(\\/.*)?$", Pattern.CASE_INSENSITIVE);
    

    public static boolean validateEmails(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }
    
    public static boolean validateURLs(String url) {
        Matcher matcher = VALID_WEBSITE_ADDRESS_REGEX.matcher(url);
        return matcher.find();
    }
    
    public static String removeMultipleTabs(String str) {
        return str.replaceAll("\t{2,}", "\t").trim();
    }
    
    public static String removeMultipleSpaces(String str) {
        return str.replaceAll("\\s{2,}", "\\s+").trim();
    }
    
    public static String removeMultipleCommas(String str) {
        return str.replaceAll("\\,{2,}", "\\,+").trim();
    }
}
