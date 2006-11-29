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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.openide.ErrorManager;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Tomas Stupka
 */
public class EntriesCache {


    private static final String SVN_THIS_DIR = "svn:this_dir"; // NOI18N
    private static final String EMPTY_STRING = "";       
    private static final String DELIMITER = "\f";
    
    private class EntryAttributes extends HashMap<String, Map<String, String>> { };
    
    /**
     * The order as it is defined should be the same as how the Subvesion entries handler
     * expects to read the entries file.  This ordering is based on Subversion 1.4.0
     */    
    static String[] entryFileAttributes = new String[] {
        "name",
        "kind",
        "revision",
        "url",
        "repos",
        "schedule",
        "text-time",
        "checksum",
        "committed-date",
        "committed-rev",
        "last-author",
        "has-props",
        "has-prop-mods",
        "cachable-props",
        "present-props",
        "prop-reject-file",
        "conflict-old",
        "conflict-new",
        "conflict-wrk",
        "copied",
        "copyfrom-url",
        "copyfrom-rev",
        "deleted",
        "absent",
        "incomplete",
        "uuid",
        "lock-token",
        "lock-owner",
        "lock-comment",
        "lock-creation-date",
    };
    
    private static final Set<String> BOOLEAN_ATTRIBUTES = new java.util.HashSet<String>();
    static {
        BOOLEAN_ATTRIBUTES.add("has-props");
        BOOLEAN_ATTRIBUTES.add("has-prop-mods");
        BOOLEAN_ATTRIBUTES.add("copied");
        BOOLEAN_ATTRIBUTES.add("deleted");
    }
    
    private class EntriesFile {        
        long ts;
        long size;
        EntryAttributes attributes;
        EntriesFile(EntryAttributes attributes, long ts, long size) {
            this.ts = ts;
            this.size = size;
            this.attributes = attributes;
        }        
    }
        
    private class Entries extends HashMap<String, EntriesFile> {};      
    
    private Entries entries;   
    private static EntriesCache instance;
            
    private EntriesCache() { }
    
    static EntriesCache getInstance() {
        if(instance == null) {
            instance = new EntriesCache(); 
        }
        return instance;
    }
    
    Map<String, String> getFileAttributes(File file) throws IOException, SAXException {        
        File entriesFile = SvnWcUtils.getEntriesFile(file);
        if(entriesFile==null) {
            return null;
        }
        return getFileAttributes(entriesFile, file);
    }   

    private synchronized Map<String, String> getFileAttributes(final File entriesFile, final File file) throws IOException, SAXException {
        EntriesFile ef = getEntries().get(entriesFile.getAbsolutePath());            
        long lastModified = entriesFile.lastModified();
        long fileLength = entriesFile.length();
        if(ef == null || ef.ts != lastModified || ef.size != fileLength) {                        
            EntryAttributes ea = getAttributesFromEntriesFile(entriesFile);
            ef = new EntriesFile(getMergedAttributes(ea), lastModified, fileLength);            
            getEntries().put(entriesFile.getAbsolutePath(), ef);
        } 
        if(ef.attributes.get(file.getName()) == null) {
            // file does not exist in the svn metadata and 
            // wasn't added to the entires cache yet
            Map<String, String> attributes  = mergeThisDirAttributes(file.isDirectory(), file.getName(), ef.attributes);        
        }
        
        return ef.attributes.get(file.isDirectory() ? SVN_THIS_DIR : file.getName());
    }

    private EntryAttributes getMergedAttributes(EntryAttributes ea) throws SAXException {        
        for(String fileName : ea.keySet()) {                                       
            boolean isDirectory = ea.get(fileName).get("kind").equals("dir");
            Map<String, String> attributes = mergeThisDirAttributes(isDirectory, fileName, ea);                    
            if(isDirectory) {
                attributes.put(WorkingCopyDetails.IS_HANDLED, "" + (ea.get(SVN_THIS_DIR).get("deleted") == null));  // NOI18N
            } else {
                if(ea.get(fileName) != null) {
                    for(Map.Entry<String, String> entry : ea.get(fileName).entrySet()) {
                        attributes.put(entry.getKey(), entry.getValue());                        
                    }            
                }           
                // it's realy a file
                attributes.put(WorkingCopyDetails.IS_HANDLED, "" + (ea.containsKey(fileName) && ea.get("deleted") == null));        // NOI18N
            }                                    
        }    
        return ea;
    }    

    private Map<String, String> mergeThisDirAttributes(final boolean isDirectory, final String fileName, final EntryAttributes ea) {
        Map<String, String> attributes = ea.get(fileName);         
        if(attributes == null) {
           attributes = new HashMap<String, String>();
           ea.put(fileName, attributes);
        }
        for(Map.Entry<String, String> entry : ea.get(SVN_THIS_DIR).entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();            
            if(isDirectory) {
                attributes.put(key, value);            
            } else {
                if(key.equals("url")) {
                    if( attributes.get(key) == null ) {
                        attributes.put(key, value + "/" + fileName);                
                    }    
                } else if( key.equals("uuid") || 
                           key.equals("repos") || 
                           key.equals("revision") || 
                           key.equals(WorkingCopyDetails.VERSION_ATTR_KEY)) {
                    if( attributes.get(key) == null ) {
                        attributes.put(key, value);
                    }
                } 
            }                            
        }        
        return attributes;
    }
    
    private EntryAttributes getAttributesFromEntriesFile(File entriesFile) throws IOException, SAXException {        
        //We need to check the first line of the File.
        //If it is a number, its the new format.
        //Otherwise, treat it as XML
        boolean isXml = false;
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(entriesFile), "UTF8"));
        try {
            String firstLine = fileReader.readLine();
            try {
                Integer.valueOf(firstLine);
                isXml = false;
            } catch (NumberFormatException ex) {
                isXml = true;
            }

            if (isXml) {
                return loadAttributesFromXml(entriesFile);
            } else {
                return loadAttributesFromPlainText(fileReader, entriesFile.getAbsolutePath());
            }
        } finally {
            fileReader.close();
        }
    }       
    
    private EntryAttributes loadAttributesFromXml(File entriesFile) throws IOException, SAXException {
        //Parse the entries file
        XMLReader saxReader = XMLUtil.createXMLReader();
        XmlEntriesHandler xmlEntriesHandler = new XmlEntriesHandler();
        saxReader.setContentHandler(xmlEntriesHandler);
        saxReader.setErrorHandler(xmlEntriesHandler);
        InputStream inputStream = new java.io.FileInputStream(entriesFile);

        try {            
            saxReader.parse(new InputSource(inputStream));
        } catch (SAXException ex) {            
            throw ex;                       
        } finally {
            inputStream.close();
        }
        return xmlEntriesHandler.getEntryAttributes();
    }

    //New entries file format, as of SVN 1.4.0
    private EntryAttributes loadAttributesFromPlainText(BufferedReader entriesReader, String entryFilePath) throws IOException {
        EntryAttributes returnValue = new EntryAttributes();
        
        int attrIndex = 0;
        
        String entryName = null;
        Map<String, String> attributes = new HashMap<String, String>();
        
        String nextLine = entriesReader.readLine();
        while (nextLine != null) {
            if (attrIndex == 0) {
                entryName = nextLine;
                if (entryName.equals(EMPTY_STRING)) {
                    entryName = SVN_THIS_DIR;
                }
            }
            
            if (!(EMPTY_STRING.equals(nextLine))) {
                if (isBooleanValue(entryFileAttributes[attrIndex])) {
                    nextLine  = "true";
                }
                attributes.put(entryFileAttributes[attrIndex], nextLine);
            }
            attrIndex++;
            nextLine = entriesReader.readLine();
 
            if(nextLine != null && attrIndex > entryFileAttributes.length - 1) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, "Skipping attribute from position " + attrIndex + " in entry file " + entryFilePath);  // NOI18N
                for( ; nextLine != null && !DELIMITER.equals(nextLine); nextLine = entriesReader.readLine());
            }
            
            if (DELIMITER.equals(nextLine)) {
                attributes.put(WorkingCopyDetails.VERSION_ATTR_KEY, WorkingCopyDetails.VERSION_14);
                returnValue.put(entryName, attributes);
                attributes = new HashMap<String, String>();
                attrIndex = 0;
                nextLine = entriesReader.readLine();
                continue;
            }
            
        }
        
        return returnValue;
    }   
    
    private static boolean isBooleanValue(String attribute) {
        return BOOLEAN_ATTRIBUTES.contains(attribute);
    }   
    
    private class XmlEntriesHandler extends DefaultHandler {
        
        private static final String ENTRY_ELEMENT_NAME = "entry";  // NOI18N
        private static final String NAME_ATTRIBUTE = "name";  // NOI18N
        private EntryAttributes entryAttributes;                               
        
        public void startElement(String uri, String localName, String qName, Attributes elementAttributes) throws SAXException {            
            if (ENTRY_ELEMENT_NAME.equals(qName)) {                                
                Map<String, String> attributes = new HashMap<String, String>();                    
                for (int i = 0; i < elementAttributes.getLength(); i++) {
                    String name = elementAttributes.getQName(i);
                    String value = elementAttributes.getValue(i);                        
                    attributes.put(name, value);
                }
                
                String nameValue = attributes.get(NAME_ATTRIBUTE);                             
                if (EMPTY_STRING.equals(nameValue)) {           
                    nameValue = SVN_THIS_DIR;
                }
                if(entryAttributes == null) {
                    entryAttributes = new EntryAttributes();
                }                
                attributes.put(WorkingCopyDetails.VERSION_ATTR_KEY, WorkingCopyDetails.VERSION_13);
                entryAttributes.put(nameValue, attributes);
            }
        }

        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }

        public EntryAttributes getEntryAttributes() {
            return entryAttributes;
        }
    }        

    private Entries getEntries() {
        if(entries == null) {
            entries = new Entries();
        }
        return entries;
    }

}
