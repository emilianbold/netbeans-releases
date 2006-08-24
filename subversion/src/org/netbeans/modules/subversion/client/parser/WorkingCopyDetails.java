/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.client.parser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import org.netbeans.modules.subversion.client.*;

/**
 *
 * @author Ed Hillmann
 */
public class WorkingCopyDetails {
    static final String FILE_ATTRIBUTE_VALUE = "file";  // NOI18
    static final String IS_HANDLED = "handled";
    private static final char SLASH_N = '\n';
    private static final char SLASH_R = '\r';

    private File file;
    //These Map stores the values in the SVN entities file
    //for the file and its parent directory
    private Map<String, String> attributes;
    //private Properties parentProps;
    //These Properties store the working and base versions of the
    //SVN properties for the file
    private Properties workingSvnProperties = null;
    private Properties baseSvnProperties = null;

    private File propertiesFile = null;
    private File basePropertiesFile = null;
    private File textBaseFile = null;

    /** Creates a new instance of WorkingCopyDetails */
    public WorkingCopyDetails(File file, Map<String, String> attributes) {
        this.file = file;
        this.attributes  = attributes;
    }

    public String getValue(String propertyName, String defaultValue) {
        String returnValue = getValue(propertyName);
        return returnValue != null ? returnValue : defaultValue;
    }

    public String getValue(String key) {
        if(key==null) return null;
        return attributes != null ? attributes.get(key) : null;
    }

    public long getLongValue(String key) throws LocalSubversionException {
        try {
            String value = getValue(key);
            if(value==null) return -1;
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new LocalSubversionException(ex);
        }
    }

    public Date getDateValue(String key) throws LocalSubversionException {
        try {
            String value = getValue(key);
            if(value==null) return null;
            return SvnWcUtils.parseSvnDate(value);
        } catch (ParseException ex) {
            throw new LocalSubversionException(ex);
        }
    }

    public boolean getBooleanValue(String key) throws LocalSubversionException {
        String value = getValue(key);
        if(value==null) return false;
        return Boolean.valueOf(value).booleanValue();
    }

    public boolean isHandled() throws LocalSubversionException {
        return getBooleanValue(IS_HANDLED);
    }

    public boolean isFile() {
        return attributes !=null ? FILE_ATTRIBUTE_VALUE.equals(attributes.get("kind")) : false;
    }

    private File getPropertiesFile() throws IOException {
        if (propertiesFile == null) {
            propertiesFile = SvnWcUtils.getPropertiesFile(file, false);
        }
        return propertiesFile;
    }

    private File getBasePropertiesFile() throws IOException {
        if (basePropertiesFile == null) {
            basePropertiesFile = SvnWcUtils.getPropertiesFile(file, true);
        }
        return basePropertiesFile;
    }

    private File getTextBaseFile() throws IOException {
        if (textBaseFile == null) {
            textBaseFile = SvnWcUtils.getTextBaseFile(file);
        }
        return textBaseFile;
    }

    private Properties getWorkingSvnProperties() throws IOException {
        if (workingSvnProperties == null) {
                workingSvnProperties = loadProperties(getPropertiesFile());
        }
        return workingSvnProperties;
    }

    private Properties getBaseSvnProperties() throws IOException {
        if (baseSvnProperties == null) {
                baseSvnProperties = loadProperties(getBasePropertiesFile());
        }
        return baseSvnProperties;
    }

    public boolean propertiesExist() throws IOException {
        boolean returnValue = false;

        File propsFile = getPropertiesFile();
        returnValue = propsFile != null ? propsFile.exists() : false;
        if (returnValue) {
            //A size of 4 bytes is equivalent to empty properties
            InputStream inputStream = new java.io.FileInputStream(propsFile);
            try {
                int size = 0;
                int retval = inputStream.read();
                while ((retval != -1) && (size < 5)) {
                    size++;
                    retval = inputStream.read();
                }
                returnValue = (size > 4);
            } finally {
                inputStream.close();
            }
        }

        return returnValue;
    }

    public boolean propertiesModified() throws IOException {
        File basePropsFile = getPropertiesFile();
        File propsFile = getBasePropertiesFile();

        if ((basePropsFile == null) && (propsFile != null)) {
            return true;
        }

        if ((basePropsFile != null) && (propsFile == null)) {
            return true;
        }

        if ((basePropsFile == null) && (propsFile == null)) {
            return false;
        }

        Properties baseProps = getBaseSvnProperties();
        Properties props = getWorkingSvnProperties();

        return !(baseProps.equals(props));
    }

    private Properties loadProperties(File propsFile) throws IOException {
        if(propsFile == null || !propsFile.exists()) {
            return null;
        }
        Properties returnValue = new Properties();
        BufferedReader fileReader = new BufferedReader(new java.io.FileReader(propsFile));
        try {
            String currentLine = fileReader.readLine();
            String propKey = null;
            String propValue = "";
            boolean headerLine = true;
            while (currentLine != null) {
                if (!(headerLine)) {
                    if (propKey == null) {
                        propKey = currentLine;
                    } else {
                        propValue = currentLine;
                        returnValue.setProperty(propKey, propValue);
                        propKey = null;
                        propValue = "";
                    }
                }
                headerLine = !(headerLine);
                currentLine = fileReader.readLine();
            }
        } finally {
            fileReader.close();
        }

        return returnValue;
    }

    public boolean textModified() throws IOException {
        boolean returnValue = false;

        if (file.exists()) {
            File baseFile = getTextBaseFile();
            if ((file == null) && (baseFile != null)) {
                return true;
            }

            if ((file != null) && (baseFile == null)) {
                return true;
            }

            if ((file == null) && (baseFile == null)) {
                return false;
            }

            Properties workingSvnProps = getWorkingSvnProperties();
            String value = "";
            if(workingSvnProps!=null) {
                value = workingSvnProps.getProperty("svn:special", "none");
            }
            if (value.equals("*")) {
		if (isSymbolicLink()) {
        	    returnValue = false;
		}
            } else {
	        String rawKeywords = workingSvnProps != null ? workingSvnProps.getProperty("svn:keywords") : null;
	        if (rawKeywords != null) {
                    returnValue = isModifiedByLine(rawKeywords.trim());
	        } else {
	            returnValue = isModifiedByByte();
	        }
            }
        }

        return returnValue;
    }

    private boolean isSymbolicLink() throws IOException {
        boolean returnValue = false;

	File baseFile = getTextBaseFile();
	if (baseFile != null) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new java.io.FileReader(baseFile));
	        String firstLine = reader.readLine();
	        returnValue = firstLine.startsWith("link");
            } finally {
		if (reader != null) {
                    reader.close();
		}
            }
        }   


        return returnValue;
    }

    /**
     * Assumes that textBaseFile exists
     */
    private boolean isModifiedByByte() throws IOException {
	boolean returnValue = false;

        InputStream baseStream = null;
        InputStream fileStream = null;
        try {
            baseStream = new BufferedInputStream(new java.io.FileInputStream(textBaseFile));
            fileStream = new BufferedInputStream(new java.io.FileInputStream(file));

            int baseRetVal = baseStream.read();
            int fileRetVal =  fileStream.read();

            while (baseRetVal != -1) {
                if (baseRetVal != fileRetVal) {
                    //Check for line endings... ignore if need be
                    boolean isLineEnding = false;
                    if ((SLASH_N == ((char) baseRetVal)) && SLASH_R == ((char) fileRetVal)) {
                        fileRetVal = fileStream.read();
                        isLineEnding = (SLASH_N == ((char) fileRetVal));
                    } else if ((SLASH_N == ((char) fileRetVal)) && SLASH_R == ((char) baseRetVal)) {
                        baseRetVal = baseStream.read();
                        isLineEnding = (SLASH_N == ((char) baseRetVal));
                    }

                    if (!(isLineEnding)) {
                        return true;
                    }
                }
                baseRetVal = baseStream.read();
                fileRetVal = fileStream.read();
            }

            //If we're here, then baseRetVal is -1.  So, returnValue
            //should be true is fileRetVal != -1
            returnValue = (fileRetVal != -1);
        } finally {
            if(fileStream != null) {
                fileStream.close();
            }
            if(baseStream != null) {
                baseStream.close();
            }
        }

        return returnValue;
    }

    /**
     * Assumes that textBaseFile exists
     */
    private boolean isModifiedByLine(String rawKeywords) throws IOException {
        boolean returnValue = false;

        String[] keywords = rawKeywords.split(" ");

        BufferedReader baseReader = null;
        BufferedReader fileReader = null;

        try {
            baseReader = new BufferedReader(new java.io.FileReader(textBaseFile));
            fileReader = new BufferedReader(new java.io.FileReader(file));

            String baseLine = baseReader.readLine();
            String fileLine = fileReader.readLine();
            while (baseLine != null) {
                //StringBuilder modifiedFileLine = new StringBuilder(fileLine);
                
                if (!fileLine.equals(baseLine)) {
                    boolean equal = false;
                    for (int i = 0; i < keywords.length; i++) {
                        String headerPattern = "$" + keywords[i];
                        if(fileLine.indexOf(headerPattern) > -1) {
                            equal = compareKeywordLines(fileLine, baseLine, keywords);
                            break;
                        }                    
                    }
                    if(!equal) {
                        return true;
                    }
                }
                    
                baseLine = baseReader.readLine();
                fileLine = fileReader.readLine();
            }

            returnValue = (fileLine != null);
        } finally {
            if (fileReader != null) {
                fileReader.close();
            }

            if (baseReader != null) {
                baseReader.close();
            }
        }

        return returnValue;
    }

    private boolean compareKeywordLines(String modifiedLine, String baseLine, String[] keywords) {
        
        int modifiedIdx = 0;
        for (int fileIdx = 0; fileIdx < baseLine.length(); fileIdx++) {
            
            if(baseLine.charAt(fileIdx) == '$') {
                // 1. could be a keyword ...
                for (int keywordsIdx = 0; keywordsIdx < keywords.length; keywordsIdx++) {
                    
                    String keyword = keywords[keywordsIdx] + "$";
                    
                    boolean gotHeader = false;
                    for (int keyIdx = 0; keyIdx < keyword.length(); keyIdx++) {
                        if(fileIdx + keyIdx + 1 > baseLine.length() - 1 ||                           // we are already at the end of the baseline
                           keyword.charAt(keyIdx) != baseLine.charAt(fileIdx + keyIdx + 1))          // the chars are not equal
                        {    
                            gotHeader = false;
                            break; // 2. it's not a keyword -> try the next one
                        } 
                        gotHeader = true;
                    }
                    if(gotHeader) {
                        // 3. it was a keyword -> skip the chars until the next '$'
                        
                        // for the base file
                        fileIdx += keyword.length(); 
                   
                        // for the modified file - '$Id: '
                        modifiedIdx += keyword.length() + 1;       //                  
                        while(++modifiedIdx < modifiedLine.length() && modifiedLine.charAt(modifiedIdx) != '$');
                                          
                        if(modifiedIdx >= modifiedLine.length()) {
                            // modified line is done but we found a kyeword -> wrong
                            return false; 
                        } 
                        break;
                    }
                }
            }            
            if(modifiedLine.charAt(modifiedIdx) != baseLine.charAt(fileIdx)) {
                return false; 
            }
            modifiedIdx++;
            if(modifiedIdx >= modifiedLine.length()) {
                // if the modified line is done then must be also the base line done
                return fileIdx == baseLine.length() - 1;
            }
        }
        return modifiedIdx == modifiedLine.length() - 2;      
    }
}
