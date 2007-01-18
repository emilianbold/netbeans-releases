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
 *
 * $Id$
 */
package org.netbeans.installer.utils.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Kirill Sorokin
 */
public class FilesList implements Iterable<FileEntry> {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private File            listFile    = null;
    private File            tempFile    = null;
    
    private List<FileEntry> entries     = null;
    
    private int             size        = 0;
    
    // constructors /////////////////////////////////////////////////////////////////
    public FilesList() {
        entries = new ArrayList<FileEntry>(CACHE_SIZE);
    }
    
    public FilesList(final File xml) throws IOException, XMLException {
        this();
        
        loadXml(xml);
    }
    
    // add/remove ///////////////////////////////////////////////////////////////////
    public void add(
            final File file) throws IOException {
        add(new FileEntry(file));
    }
    
    public void add(
            final FileEntry entry) throws IOException {
        final String name = entry.getName();
        
        int index = 0;
        while (index < entries.size()) {
            final String current = entries.get(index).getName();
            
            if (current.length() < name.length()) {
                break;
            } else if (current.equals(name)) {
                return;
            }
            
            index++;
        }
        
        entries.add(index, entry);
        size++;
        
        if (entries.size() == CACHE_SIZE) {
            save();
        }
    }
    
    public void add(
            final List<File> list) throws IOException {
        for (File file: list) {
            add(file);
        }
    }
    
    public void add(
            final FilesList list) throws IOException {
        for (FileEntry entry: list) {
            add(entry);
        }
    }
    
    public void clear() throws IOException {
        if (listFile != null) {
            FileUtils.deleteFiles(listFile, tempFile);
        }
        entries.clear();
        size = 0;
    }
    
    // getters //////////////////////////////////////////////////////////////////////
    public int getSize() {
        return size;
    }
    
    // list <-> xml /////////////////////////////////////////////////////////////////
    public FilesList loadXml(final File xml) throws XMLException {
        return loadXml(xml, null);
    }
    
    public FilesList loadXml(final File xml, final File root) throws XMLException {
        try {
            InputStream in = new FileInputStream(xml);
            
            loadXml(in, root);
            in.close();
            
            return this;
        } catch (IOException e) {
            throw new XMLException("Cannot parse xml file", e);
        }
    }
    
    public FilesList loadXmlGz(final File xml) throws XMLException {
        return loadXmlGz(xml, null);
    }
    
    public FilesList loadXmlGz(final File xml, final File root) throws XMLException {
        try {
            InputStream in = new GZIPInputStream(new FileInputStream(xml));
            
            loadXml(in, root);
            in.close();
            
            return this;
        } catch (IOException e) {
            throw new XMLException("Cannot parse xml file", e);
        }
    }
    
    public void saveXml(final File xml) throws XMLException {
        try {
            OutputStream out = new FileOutputStream(xml);
            
            saveXml(out);
            
            out.close();
        } catch (UnsupportedEncodingException e) {
            throw new XMLException("Cannot save XML", e);
        } catch (IOException e) {
            throw new XMLException("Cannot save XML", e);
        }
    }
    
    public void saveXmlGz(final File xml) throws XMLException {
        try {
            OutputStream out = new GZIPOutputStream(new FileOutputStream(xml));
            
            saveXml(out);
            
            out.close();
        } catch (UnsupportedEncodingException e) {
            throw new XMLException("Cannot save XML", e);
        } catch (IOException e) {
            throw new XMLException("Cannot save XML", e);
        }
    }
    
    // list <-> list :) /////////////////////////////////////////////////////////////
    public List<File> toList() {
        List<File> files = new ArrayList<File>(size);
        
        for (FileEntry entry: this) {
            files.add(entry.getFile());
        }
        
        return files;
    }
    
    // iterable /////////////////////////////////////////////////////////////////////
    public Iterator<FileEntry> iterator() {
        return new FilesListIterator();
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private void save() throws IOException {
        if (entries.size() > 0) {
            if (listFile == null) {
                listFile = FileUtils.createTempFile();
                tempFile = FileUtils.createTempFile();
            }
            
            final BufferedReader reader;
            if (listFile.length() > 0) {
                reader =
                        new BufferedReader(
                        new InputStreamReader(
                        new GZIPInputStream(
                        new FileInputStream(listFile))));
            } else {
                reader =
                        new BufferedReader(
                        new FileReader(listFile));
            }
            final BufferedWriter writer =
                    new BufferedWriter(
                    new OutputStreamWriter(
                    new GZIPOutputStream(
                    new FileOutputStream(tempFile))));
            
            int       index = 0;
            FileEntry saved = readEntry(reader);
            
            while ((index < entries.size()) && (saved != null)) {
                final String unsavedName = entries.get(index).getName();
                final String savedName   = saved.getName();
                
                if (savedName.equals(unsavedName)) {
                    if ((index < entries.size() - 1) &&
                            entries.get(index + 1).getName().equals(unsavedName)) {
                        index++;
                    } else {
                        saved = readEntry(reader);
                    }
                    size--;
                } else {
                    if (unsavedName.length() < savedName.length()) {
                        writeEntry(saved, writer);
                        saved = readEntry(reader);
                    } else {
                        writeEntry(entries.get(index), writer);
                        index++;
                    }
                }
            }
            
            while (index < entries.size()) {
                writeEntry(entries.get(index), writer);
                index++;
            }
            
            while (saved != null) {
                writeEntry(saved, writer);
                saved = readEntry(reader);
            }
            
            reader.close();
            
            writer.flush();
            writer.close();
            
            FileUtils.copyFile(tempFile, listFile);
            
            entries.clear();
            System.gc(); // just in case
        }
    }
    
    private FileEntry readEntry(
            final BufferedReader reader) throws IOException {
        final String name = reader.readLine();
        
        if (name != null) {
            final File    file      = new File(name);
            final boolean directory = Boolean.parseBoolean(reader.readLine());
            
            if (directory) {
                final boolean empty       = Boolean.parseBoolean(reader.readLine());
                final long    modified    = Long.parseLong(reader.readLine());
                final int     permissions = Integer.parseInt(reader.readLine());
                
                return new FileEntry(
                        file,
                        empty,
                        modified,
                        permissions);
            } else {
                final long    size        = Long.parseLong(reader.readLine());
                final String  md5         = reader.readLine();
                final boolean jarFile     = Boolean.parseBoolean(reader.readLine());
                final boolean packed      = Boolean.parseBoolean(reader.readLine());
                final boolean signed      = Boolean.parseBoolean(reader.readLine());
                final long    modified    = Long.parseLong(reader.readLine());
                final int     permissions = Integer.parseInt(reader.readLine());
                
                return new FileEntry(
                        file,
                        size,
                        md5,
                        jarFile,
                        packed,
                        signed,
                        modified,
                        permissions);
            }
        }
        
        return null;
    }
    
    private void writeEntry(
            final FileEntry entry,
            final Writer writer) throws IOException {
        if (entry.getFile().exists()) {
            if (!entry.isMetaDataReady()) {
                entry.calculateMetaData();
            }
            
            writer.write(entry.toString());
        }
    }
    
    private void saveXml(
            final OutputStream out) throws IOException {
        final PrintWriter writer =
                new PrintWriter(new OutputStreamWriter(out, ENCODING));
        
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<files-list>");
        
        for (FileEntry entry: this) {
            if (entry.getFile().exists()) {
                if (!entry.isMetaDataReady()) {
                    entry.calculateMetaData();
                }
                
                writer.println("    " + entry.toXml());
            }
        }
        
        writer.println("</files-list>");
        
        writer.flush();
    }
    
    private void loadXml(
            final InputStream in,
            final File root) throws IOException, XMLException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser;
        try {
            parser = factory.newSAXParser();
            
            parser.parse(new InputSource(in), new FilesListHandler(root));
        } catch (SAXException e) {
            throw new XMLException("Cannot load files list from xml", e);
        } catch (ParserConfigurationException e) {
            throw new XMLException("Cannot load files list from xml", e);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private class FilesListHandler extends DefaultHandler {
        private boolean entryElement = false;
        
        private File    root         = null;
        
        private String  name         = null;
        
        private boolean directory    = false;
        private boolean empty        = false;
        
        private long    size         = 0;
        private String  md5          = null;
        private boolean jarFile      = false;
        private boolean packed       = false;
        private boolean signed       = false;
        private long    modified     = 0;
        private int     permissions  = 0;
        
        public FilesListHandler(File root) {
            this.root = root;
        }
        
        public void startElement(
                final String uri,
                final String localName,
                final String qName,
                final Attributes attributes) throws SAXException {
            if (qName.equals("entry")) {
                entryElement = true;
                
                String type = attributes.getValue("type");
                if (type.equals("file")) {
                    directory = false;
                    
                    size = Long.parseLong(attributes.getValue("size"));
                    md5 = attributes.getValue("md5");
                    jarFile = Boolean.parseBoolean(attributes.getValue("jar"));
                    
                    if (jarFile) {
                        packed = Boolean.parseBoolean(attributes.getValue("packed"));
                        signed = Boolean.parseBoolean(attributes.getValue("signed"));
                    } else {
                        packed  = false;
                        signed  = false;
                    }
                    
                    modified = Long.parseLong(attributes.getValue("modified"));
                    permissions = 0;
                } else {
                    directory = true;
                    empty = Boolean.parseBoolean(attributes.getValue("empty"));
                    modified = 0;
                    permissions = 0;
                }
            } else {
                entryElement = false;
            }
        }
        
        public void characters(
                final char[] characters,
                final int start,
                final int length) throws SAXException {
            if (entryElement) {
                final String value = new String(characters, start, length);
                
                if (name == null) {
                    name = value;
                } else {
                    name += value;
                }
            }
        }
        
        public void endElement(
                final String uri,
                final String localName,
                final String qName) throws SAXException {
            if (entryElement) {
                final File file;
                if (root == null) {
                    file = new File(name);
                } else {
                    file = new File(root, name);
                }
                
                name = null;
                
                FileEntry entry;
                
                if (directory) {
                    entry = new FileEntry(
                            file,
                            empty,
                            modified,
                            permissions);
                } else {
                    entry = new FileEntry(
                            file,
                            size,
                            md5,
                            jarFile,
                            packed,
                            signed,
                            modified,
                            permissions);
                }
                
                entryElement = false;
                
                try {
                    FilesList.this.add(entry);
                } catch (IOException e) {
                    throw new SAXException("Could not add an entry", e);
                }
            }
        }
    }
    
    private class FilesListIterator implements Iterator<FileEntry> {
        private int            sizeAtConstruction = 0;
        private boolean        listInMemory       = false;
        
        private int            index              = 0;
        private BufferedReader reader             = null;
        
        private FileEntry      next               = null;
        
        public FilesListIterator() {
            // if the list size is already bigger than can be reposited in memory -
            // make sure that all entries are present in the cache file; and set the
            // iteration mode (over memory or over cache file)
            if (FilesList.this.listFile != null) {
                try {
                    FilesList.this.save();
                } catch (IOException e) {
                    ErrorManager.notifyError("Cannot save list", e);
                }
                
                listInMemory = false;
                try {
                    reader = new BufferedReader(
                            new InputStreamReader(
                            new GZIPInputStream(
                            new FileInputStream(FilesList.this.listFile))));
                    
                } catch (IOException e) {
                    ErrorManager.notifyError("Cannot open reader to the list file", e);
                }
            } else {
                listInMemory = true;
                index        = 0;
            }
            
            sizeAtConstruction = FilesList.this.size;
        }
        
        public boolean hasNext() {
            if (sizeAtConstruction != FilesList.this.size) {
                throw new ConcurrentModificationException("The list was changed, while iterating");
            }
            
            if (next == null) {
                next = next();
            }
            
            return next != null;
        }
        
        public FileEntry next() {
            if (next != null) {
                final FileEntry temp = next;
                next = null;
                
                return temp;
            } else {
                FileEntry entry = null;
                
                if (listInMemory) {
                    if (index < FilesList.this.entries.size()) {
                        entry = FilesList.this.entries.get(index++);
                    }
                } else {
                    try {
                        entry = FilesList.this.readEntry(reader);
                        
                        if (entry == null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        ErrorManager.notifyError("Cannot read next entry", e);
                    }
                }
                
                return entry;
            }
        }
        
        public void remove() {
            throw new UnsupportedOperationException("Remove is not supported for files list");
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final int CACHE_SIZE =
            2500;
    
    public static final String ENCODING =
            "UTF-8"; // NOI18N
}
