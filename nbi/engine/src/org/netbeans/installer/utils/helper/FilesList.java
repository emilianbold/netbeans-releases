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

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Kirill Sorokin
 */
public class FilesList {
    private final List<FileEntry> entries = new LinkedList<FileEntry>();
    
    private File directory = null;
    
    public FilesList(final File directory) {
        this.directory = directory.getAbsoluteFile();
    }
    
    public FilesList(final File directory, final File xml) throws XMLException {
        this(directory);
        
        final Document document = XMLUtils.loadXMLDocument(xml);
        
        for (Element element: XMLUtils.getChildren(document.getDocumentElement())) {
            entries.add(new FileEntry(element));
        }
    }
    
    public FilesList(final File directory, final List<File> files) throws IOException, NoSuchAlgorithmException {
        this(directory);
        
        for (File file: files) {
            add(file);
        }
    }
    
    public List<FileEntry> getEntries() {
        return entries;
    }
    
    public void add(final File file) throws IOException, NoSuchAlgorithmException {
        add(new FileEntry(file, getName(file)));
    }
    
    public void add(final FileEntry entry) {
        int index; 
        
        for (index = 0; index < entries.size(); index++) {
            if (entries.get(index).getName().equals(entry.getName())) {
                entries.remove(index);
                
                return;
            }
        }
        
        entries.add(index, entry);
    }
    
    public void add(final FilesList list) {
        for (FileEntry entry: list.getEntries()) {
            add(entry);
        }
    }

    public void add(final List<File> list) throws IOException, NoSuchAlgorithmException {
        for (File file: list) {
            add(file);
        }
    }
    
    public File getFile(FileEntry entry) {
        return new File(directory, entry.getName());
    }
    
    public boolean contains(final File file) {
        final String name = getName(file);
        
        for (FileEntry entry: entries) {
            if (entry.getName().equals(name)) {
                return true;
            }
        }
        
        return false;
    }
    
    public void remove(final File file) {
        final String name = getName(file);
        
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getName().equals(name)) {
                entries.remove(i);
                i--;
            }
        }
    }
    
    public void saveTo(final File file) throws XMLException {
        final Document document = XMLUtils.getEmptyDocument("files-list");
        final Element  element  = document.getDocumentElement();
        
        for (FileEntry entry: entries) {
            element.appendChild(entry.saveToDom(document.createElement("entry")));
        }
        
        XMLUtils.saveXMLDocument(document, file);
    }
    
    public String getName(final File file) {
        final String path = file.getAbsolutePath().replace('\\', '/');
        final String name;
        
        if (directory.getPath().length() < path.length()) {
            name = path.substring(directory.getPath().length() + 1);
        } else {
            name = ".";
        }
        
        if (file.isDirectory()) {
            return name + "/";
        } else {
            return name;
        }
    }
    
    public void normalize() {
        
    }
}
