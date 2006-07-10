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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
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

    private static final String ENTRIES = "entries";      // NOI18N    
    private static final String SVN_THIS_DIR = "svn:this_dir"; // NOI18N
    private static final String EMPTY_STRING = "";       
    
    private class EntryAttributes extends HashMap<String, Map<String, String>> { };
    
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
        File entriesFile = SvnWcUtils.getSvnFile(!file.isDirectory() ? file.getParentFile() : file, ENTRIES);        
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
            EntriesHandler handler = getEntriesHandler(entriesFile, file.getName());                                
            EntryAttributes ea = handler.getEntryAttributes();
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
                // XXX there was an issue that a not existing directory returned false for isDeleted!
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
                    attributes.put(key, value + "/" + fileName);                
                } else if( key.equals("uuid") || key.equals("repos") ) {
                    attributes.put(key, value);                                        
                }
            }                            
        }        
        return attributes;
    }
    
    private EntriesHandler getEntriesHandler(File entriesFile, String fileName) throws IOException, SAXException {        
        //Parse the entries file
        XMLReader saxReader = XMLUtil.createXMLReader();
        EntriesHandler entriesHandler = null;
        entriesHandler = new EntriesHandler();
        saxReader.setContentHandler(entriesHandler);
        saxReader.setErrorHandler(entriesHandler);
        InputStream inputStream = new java.io.FileInputStream(entriesFile);

        try {            
            saxReader.parse(new InputSource(inputStream));
        } catch (SAXException ex) {            
            throw ex;                       
        } finally {
            inputStream.close();
        }
        return entriesHandler;
    }    
    
    private class EntriesHandler extends DefaultHandler {
        
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
