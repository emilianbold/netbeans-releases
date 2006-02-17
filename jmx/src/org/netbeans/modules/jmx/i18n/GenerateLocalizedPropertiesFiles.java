/*
 * GenerateLocalizedPropertiesFiles.java
 *
 * Created on January 9, 2006, 9:53 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.jmx.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 *
 * @author jfdenise
 */
public class GenerateLocalizedPropertiesFiles {
    
    /** Creates a new instance of Main */
    public GenerateLocalizedPropertiesFiles() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
       
        String dirs = System.getProperty("jmx.i18n.properties.dirs");
        StringTokenizer tokenizer = new StringTokenizer(dirs, ",");
        String prefix = System.getProperty("jmx.i18n.prefix");
        String locale = System.getProperty("jmx.i18n.locale");
      while(tokenizer.hasMoreElements()) {
            generateFile(locale, prefix, tokenizer.nextToken());
      }
    }
    
    private static void generateFile(String locale,
            String prefix, String dir) throws Exception {
        File file = new File("." + File.separator + "src" + 
                File.separator + dir + File.separator + "Bundle.properties");
        System.out.println("Reading file : " + file.getAbsolutePath());
        String newDir = "." + File.separator + "build" + 
                File.separator + "locale" + File.separator + dir;
        String newFileName = newDir + File.separator + "Bundle_"+ locale + 
                ".properties";
        File dirs = new File(newDir);
        dirs.mkdirs();
        File f = new File(newFileName);
        f.createNewFile();
        FileOutputStream output = 
                new FileOutputStream(f);
        
        //Load properties file
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));
        Properties newProperties = new Properties();
        Set<Entry<Object, Object>> entries = properties.entrySet();
        for(Entry entry : entries) {
            String key = (String)entry.getKey();
            
            String value = (String) entry.getValue();
            if(!key.endsWith("Template")) {
                //We are not translating Numbers
                try {
                    Integer.valueOf(value);
                }catch(Exception e) {
                    if(!("localhost".equals(value)))
                        value = parseEscapes(prefix) + entry.getValue();
                }
            }
            newProperties.setProperty(key, value);
        }
        System.out.println("Localized file : " + newFileName);
        newProperties.store(output, "Localized in " + locale);
        output.close();
    }
    
    public static String parseEscapes(CharSequence text) throws ParseException
    {
        // For each character...
        int          length = text.length();
        StringBuffer sb     = new StringBuffer(length);
        for (int iChar = 0; iChar < length;) {
            char ch = text.charAt(iChar++);
 
            // Handle escapes.
            if (ch == '\\') {
                if (iChar >= length) {
                    throw new ParseException("Orphaned escape character.",iChar);
                }
                ch = text.charAt(iChar++);
                if (ch == 'u') {
                    // A Unicode escape sequence.
                    if (iChar > length - 4) {
                        throw new ParseException("Malformed Unicode escape.",iChar);
                    }
                    int value = 0;
                    for (int iDigit = 4; iDigit > 0; iDigit--) {
                        int digitValue = Character.digit(text.charAt(iChar++), 16);
                        if (digitValue < 0) {
                            throw new ParseException("Malformed Unicode escape.",iChar);
                        }
                        value = (value << 4) + digitValue;
                    }
                    sb.append((char)value);
                } else {
                    // A standard escape sequence.
                    switch (ch) {
                        case 't' : ch = '\t'; break;
                        case 'r' : ch = '\r'; break;
                        case 'n' : ch = '\n'; break;
                        case 'f' : ch = '\f'; break;
                    }
                    sb.append(ch);
                }
                
                continue;
            }
 
            // An unescaped character.
            sb.append(ch);
        }
        
        return sb.toString();
    }
}
