/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.subversion.remote.client.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.SVNConflictDescriptor;
import org.netbeans.modules.subversion.remote.config.KVFile;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
public class WorkingCopyDetails {
    static final String FILE_ATTRIBUTE_VALUE = "file"; //NOI18N
    static final String IS_HANDLED = "handled"; //NOI18N
    private static final char SLASH_N = '\n'; //NOI18N
    private static final char SLASH_R = '\r'; //NOI18N

    static final String VERSION_ATTR_KEY = "wc-version"; //NOI18N
    static final String VERSION_UNKNOWN = ""; //NOI18N
    static final String VERSION_13 = "1.3"; //NOI18N
    static final String VERSION_14 = "1.4"; //NOI18N
    static final String ATTR_TREE_CONFLICT_DESCRIPTOR = "is-in-conflict"; //NOI18N

    private final VCSFileProxy file;
    //These Map stores the values in the SVN entities file
    //for the file and its parent directory
    private final Map<String, String> attributes;
    //private Properties parentProps;
    //These Properties store the working and base versions of the
    //SVN properties for the file
    private Map<String, byte[]> workingSvnProperties = null;
    private Map<String, byte[]> baseSvnProperties = null;

    protected VCSFileProxy propertiesFile = null;
    private VCSFileProxy basePropertiesFile = null;
    private VCSFileProxy textBaseFile = null;
    private final SVNConflictDescriptor conflictDescriptor;

    /** Creates a new instance of WorkingCopyDetails */
    private WorkingCopyDetails(VCSFileProxy file, Map<String, String> attributes, SVNConflictDescriptor conflictDescriptor) {
        this.file = file;
        this.attributes  = attributes;
        this.conflictDescriptor = conflictDescriptor;
    }

    public static WorkingCopyDetails createWorkingCopy(VCSFileProxy file, Map<String, String> attributes) {
        SVNConflictDescriptor conflictDescriptor = null;
        if (attributes != null) {
            conflictDescriptor = EntriesCache.getInstance().getConflictDescriptor(file.getName(), attributes.get(WorkingCopyDetails.ATTR_TREE_CONFLICT_DESCRIPTOR));
        }
        String version = attributes != null ? attributes.get(VERSION_ATTR_KEY) : VERSION_UNKNOWN;
        if(version != null) {
            if(version.equals(VERSION_13)) {

                return new WorkingCopyDetails(file, attributes, conflictDescriptor);
                
            } else if(version.equals(VERSION_14)) {
                
                return new WorkingCopyDetails(file, attributes, conflictDescriptor) {
                    @Override
                    public boolean propertiesExist() throws IOException {
                        return getAttributes().containsKey("has-props");        // NOI18N
                    }  
                    @Override
                    public boolean propertiesModified() throws IOException {
                        return getAttributes().containsKey("has-prop-mods");    // NOI18N
                    }            
                    @Override
                    VCSFileProxy getPropertiesFile() throws IOException {
                        if (propertiesFile == null) {
                            // unchanged properties have only the base file
                            boolean modified = getBooleanValue("has-prop-mods");                                // NOI18N
                            propertiesFile = SvnWcUtils.getPropertiesFile(getFile(), modified ? false : true);
                        }
                        return propertiesFile;
                    }                    
                };

            } else if(version.equals(VERSION_UNKNOWN)) {

                WorkingCopyDetails wcd = new WorkingCopyDetails(file, attributes, conflictDescriptor);
                if(!wcd.isHandled()) {
                    return wcd;
                } 
                // how is this possible?
                throw new UnsupportedOperationException("Unknown SVN working copy version: " + version);    // NOI18N                

            } else {

                throw new UnsupportedOperationException("Unknown SVN working copy version: " + version);    // NOI18N

            }   
        } else {
            Subversion.LOG.warning("Could not determine the SVN working copy version for " + file + ". Falling back on 1.3");  // NOI18N
            return new WorkingCopyDetails(file, attributes, conflictDescriptor);
        }
    }

    protected Map<String, String> getAttributes() { 
        return attributes;
    }
    
    protected VCSFileProxy getFile() {
        return file;
    }
    
    public String getValue(String propertyName, String defaultValue) {
        String returnValue = getValue(propertyName);
        return returnValue != null ? returnValue : defaultValue;
    }

    public String getValue(String key) {
        if(key==null) return null;
        return getAttributes() != null ? getAttributes().get(key) : null;
    }

    public long getLongValue(String key) throws LocalSubversionException {
        try {
            String value = getValue(key);
            if(value==null) {
                return -1;
            }
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new LocalSubversionException(ex);
        }
    }

    public Date getDateValue(String key) throws LocalSubversionException {
        try {
            String value = getValue(key);
            if(value==null || value.equals("")) {
                return null;
            }
            return SvnWcUtils.parseSvnDate(value);
        } catch (ParseException ex) {
            throw new LocalSubversionException(ex);
        }
    }

    public boolean getBooleanValue(String key) {
        String value = getValue(key);
        if(value==null) {
            return false;
        }
        return Boolean.valueOf(value).booleanValue();
    }

    public boolean isHandled() {
        return getBooleanValue(IS_HANDLED);
    }

    public boolean isFile() {
        return getAttributes() !=null ? FILE_ATTRIBUTE_VALUE.equals(getAttributes().get("kind")) : false; // NOI18N
    }

    VCSFileProxy getPropertiesFile() throws IOException {
        if (propertiesFile == null) {
            propertiesFile = SvnWcUtils.getPropertiesFile(file, false);
        }
        return propertiesFile;
    }

    VCSFileProxy getBasePropertiesFile() throws IOException {
        if (basePropertiesFile == null) {
            basePropertiesFile = SvnWcUtils.getPropertiesFile(file, true);
        }
        return basePropertiesFile;
    }

    private VCSFileProxy getTextBaseFile() throws IOException {
        if (textBaseFile == null) {
            textBaseFile = SvnWcUtils.getTextBaseFile(file);
        }
        return textBaseFile;
    }

    private Map<String, byte[]> getWorkingSvnProperties() throws IOException {
        if (workingSvnProperties == null) {
            workingSvnProperties = loadProperties(getPropertiesFile());
        }
        return workingSvnProperties;
    }

    private Map<String, byte[]> getBaseSvnProperties() throws IOException {
        if (baseSvnProperties == null) {
            baseSvnProperties = loadProperties(getBasePropertiesFile());
        }
        return baseSvnProperties;
    }

    public boolean propertiesExist() throws IOException {
        boolean returnValue = false;
        VCSFileProxy propsFile = getPropertiesFile();
        returnValue = propsFile != null ? propsFile.exists() : false;
        if (returnValue) {
            //A size of 4 bytes is equivalent to empty properties
            InputStream inputStream = propsFile.getInputStream(false);
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
        Map<String, byte[]> baseProps = getBaseSvnProperties();
        Map<String, byte[]> props = getWorkingSvnProperties();
        if ((baseProps == null) && (props != null)) {
            return true;
        }
        if ((baseProps != null) && (props == null)) {
            return true;
        }
        if ((baseProps == null) && (props == null)) {
            return false;
        }        
        if(baseProps.size() != props.size()) {
            return true;
        }
        for(Map.Entry<String, byte[]> baseEntry : baseProps.entrySet()) {
            byte[] propsValue = props.get(baseEntry.getKey());            
            if(propsValue == null || !Arrays.equals(propsValue, baseEntry.getValue())) {
                return true;
            }                        
        }
        return false;
    }

    private Map<String, byte[]> loadProperties(VCSFileProxy propsFile) throws IOException {
        if(propsFile == null || !propsFile.exists()) {
            return null;
        }        
        KVFile kv = new KVFile(propsFile);
        return kv.getNormalizedMap();                
    }

    public boolean textModified() throws IOException {
        if (file.exists()) {
            VCSFileProxy baseFile = getTextBaseFile();
            if (baseFile == null) {
                return true;
            }
            Map<String, byte[]> workingSvnProps = getWorkingSvnProperties();
            if(workingSvnProps != null) {                
                String svnSpecial = getPropertyValue(workingSvnProps, "svn:special");           // NOI18N
                if (svnSpecial != null && svnSpecial.equals("*")) {                             // NOI18N
                    if (isSymbolicLink()) {
                        return false;
                    }
                }                   
                String svnKeywords = getPropertyValue(workingSvnProps, "svn:keywords");         // NOI18N          
                if (svnKeywords != null) {
                    return isModifiedByLine(svnKeywords.trim());
                } 
            }
            return isModifiedByByte();            
        }
        return false;
    }

    SVNConflictDescriptor getConflictDescriptor() {
        return conflictDescriptor;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("Dm")
    private String getPropertyValue(Map<String, byte[]> props, String key) {
        byte[] byteValue = props.get(key);
        if(byteValue != null && byteValue.length > 0) {
            try {
                return new String(byteValue, "UTF-8"); //NOI18N
            } catch (UnsupportedEncodingException ex) {
                return new String(byteValue);
            }
        }        
        return  null;
    }
    
    private boolean isSymbolicLink() throws IOException {
        boolean returnValue = false;

	VCSFileProxy baseFile = getTextBaseFile();
	if (baseFile != null) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(baseFile.getInputStream(false), "UTF-8")); //NOI18N
	        String firstLine = reader.readLine();
	        returnValue = firstLine.startsWith("link");     // NOI18N
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
            baseStream = textBaseFile.getInputStream(false);
            fileStream = file.getInputStream(false);

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
        if(rawKeywords == null || rawKeywords.equals("")) { //NOI18N
            return false;
        }
        boolean returnValue = false;

        List<String> keywordsList = new ArrayList<>();
        
        rawKeywords = rawKeywords.replaceAll("\n", " "); //NOI18N
        rawKeywords = rawKeywords.replaceAll("\t", " "); //NOI18N   
        keywordsList.addAll(normalizeKeywords(rawKeywords.split(" ")));             // NOI18N          

        String[] keywords = keywordsList.toArray(new String[keywordsList.size()]);
        
        BufferedReader baseReader = null;
        BufferedReader fileReader = null;

        try {
            baseReader = new BufferedReader(new InputStreamReader(textBaseFile.getInputStream(false), "UTF-8"));
            fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(false), "UTF-8"));

            String baseLine = baseReader.readLine();
            String fileLine = fileReader.readLine();

            while (baseLine != null && fileLine != null) {                
                            
                if (!fileLine.equals(baseLine)) {
                    boolean equal = false;
                    for (int i = 0; i < keywords.length; i++) {
                        String headerPattern = "$" + keywords[i];                           // NOI18N
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

            returnValue = baseLine != null || fileLine != null;
            
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

    private List<String> normalizeKeywords(String[] keywords) {
        List<String> keywordsList = new ArrayList<>();
        for (int i = 0; i < keywords.length; i++) {
            String kw = keywords[i].toLowerCase(Locale.ENGLISH).trim();
            if(kw.equals("date") || kw.equals("lastchangeddate")) {                                         // NOI18N
                keywordsList.add("LastChangedDate");                                                        // NOI18N
                keywordsList.add("Date");                                                                   // NOI18N
            } else if(kw.equals("revision") || kw.equals("rev") || kw.equals("lastchangedrevision")) {      // NOI18N
                keywordsList.add("LastChangedRevision");                                                    // NOI18N
                keywordsList.add("Revision");                                                               // NOI18N
                keywordsList.add("Rev");                                                                    // NOI18N
            } else if(kw.equals("author") || kw.equals("lastchangedby")) {                                  // NOI18N
                keywordsList.add("LastChangedBy");                                                          // NOI18N
                keywordsList.add("Author");                                                                 // NOI18N
            } else if(kw.equals("url") || kw.equals("headurl")) {                                           // NOI18N
                keywordsList.add("HeadURL");                                                                // NOI18N
                keywordsList.add("URL");                                                                    // NOI18N
            } else if(kw.equals("id")) {                                                                    // NOI18N
                keywordsList.add("Id");                                                                     // NOI18N                
            } else if(!kw.equals("")){
                keywordsList.add(keywords[i]);                
            }            
        } 
        return keywordsList;
    }
    
    private boolean compareKeywordLines(String modifiedLine, String baseLine, String[] keywords) {

        int modifiedIdx = 0;
        for (int fileIdx = 0; fileIdx < baseLine.length(); fileIdx++) {

            if(baseLine.charAt(fileIdx) == '$') {
                // 1. could be a keyword ...
                for (int keywordsIdx = 0; keywordsIdx < keywords.length; keywordsIdx++) {

                    String keyword = keywords[keywordsIdx];                                          // NOI18N

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
                        
                        // base file idx
                        fileIdx += keyword.length(); 
                        boolean isKeyword = true;
                        // 3. now check if there is somthing like "$", ":$" after the keyword                                
                        if(checkFollowingString(baseLine, fileIdx + 1, "$")) { //NOI18N
                            fileIdx += 1;
                        } else if(checkFollowingString(baseLine, fileIdx + 1, ":$")) { //NOI18N
                            fileIdx += 2;    
                        } else if(checkFollowingString(baseLine, fileIdx + 1, "::")) { //NOI18N
                            int spaces = getSpacesCount(baseLine, fileIdx + 3);
                            if(spaces <= 0) {
                                return false;
                            }
                            fileIdx += spaces + 3;                                
                        } else if(checkFollowingString(baseLine, fileIdx + 1, ":")) { //NOI18N
                            int spaces = getSpacesCount(baseLine, fileIdx + 2);
                            if(spaces > 0) {
                                fileIdx += spaces + 2;
                            } else {
                                isKeyword = false;
                            }
                        } else {
                            isKeyword = false;
                        }
                        if(!isKeyword) {
                            // it's not a keyword -> rollback the index and keep comparing
                            fileIdx -= keyword.length();
                            break;
                        }
                        // 4. it was a correctly closed keyword -> skip the chars until the next '$'
                        // for the modified file - '$Id: '
                        modifiedIdx += keyword.length() + 1;       //                  
                        while(++modifiedIdx < modifiedLine.length() && modifiedLine.charAt(modifiedIdx) != '$'); //NOI18N

                        if(modifiedIdx >= modifiedLine.length()) {
                            // modified line is done but we found a keyword -> wrong
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
    
    private boolean checkFollowingString(String baseLine, int offset, String str) {
        if(baseLine.length() < offset + str.length()) {
            return false;
        }
        for (int idx = 0; idx < str.length(); idx++) {
            if(baseLine.charAt(offset + idx) != str.charAt(idx)) {
                return false;
            }            
        }
        return true;    
    }
    
    private int getSpacesCount(String baseLine, int offset) {
        if(baseLine.length() <= offset) {
            return -1;
        }
        int maxPos = baseLine.length() - offset;
        for (int idx = 0; idx < maxPos; idx++) {
            char c = baseLine.charAt(offset + idx);
            if(c == ' ') {
                continue;
            } else if(c == '$') {
                return idx;
            }            
            return -1;
        }
        return -1;
    }
    
}
