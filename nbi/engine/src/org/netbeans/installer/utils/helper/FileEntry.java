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
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.w3c.dom.Element;

public class FileEntry {
    private long    size          = 0;
    private String  md5           = "";
    
    private boolean directory     = false;
    private boolean empty         = false;
    
    private boolean jarFile       = false;
    private boolean packedJarFile = false;
    private boolean signedJarFile = false;
    
    private long    lastModified  = 0;
    
    private int     permissions   = 0;
    
    private String  name          = null;
    
    public FileEntry(Element element) {
        String type = element.getAttribute("type");
        
        if (type.equals("directory")) {
            directory = true;
            empty = new Boolean(element.getAttribute("is-empty"));
        } else {
            directory = false;
            
            size  = new Long(element.getAttribute("size"));
            md5   = element.getAttribute("md5");
            
            lastModified = new Long(element.getAttribute("modified"));
            
            jarFile = new Boolean(element.getAttribute("jar"));
            if (jarFile) {
                packedJarFile = new Boolean(element.getAttribute("packed-jar"));
                signedJarFile = new Boolean(element.getAttribute("signed-jar"));
            }
        }
        
        setName(element.getTextContent());
    }
    
    public FileEntry(File file, String name) throws IOException, NoSuchAlgorithmException {
        if (file.exists()) {
            directory = file.isDirectory();
            
            if (!directory) {
                size    = file.length();
                md5     = FileUtils.getMd5(file);
                jarFile = FileUtils.isJarFile(file);
                
                if (jarFile) {
                    packedJarFile = false; // we cannot determine this
                    signedJarFile = FileUtils.isSigned(file);
                }
            }  else {
                empty = FileUtils.isEmpty(file);
            }
            
            lastModified = file.lastModified();
        }
        
        this.name = name;
    }
    
    public long getSize() {
        return size;
    }
    
    public void setSize(long size) {
        this.size = size;
    }
    
    public String getMd5() {
        return md5;
    }
    
    public void setMd5(String md5) {
        this.md5 = md5;
    }
    
    public boolean isDirectory() {
        return directory;
    }
    
    public void setDirectory(boolean directory) {
        this.directory = directory;
    }
    
    public boolean isEmpty() {
        return empty;
    }
    
    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
    
    public boolean isJarFile() {
        return jarFile;
    }
    
    public void setJarFile(boolean jarFile) {
        this.jarFile = jarFile;
    }
    
    public boolean isPackedJarFile() {
        return packedJarFile;
    }
    
    public void setPackedJarFile(boolean packedJarFile) {
        this.packedJarFile = packedJarFile;
    }
    
    public boolean isSignedJarFile() {
        return signedJarFile;
    }
    
    public void setSignedJarFile(boolean signedJarFile) {
        this.signedJarFile = signedJarFile;
    }
    
    public long getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
    
    public int getPermissions() {
        return permissions;
    }
    
    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Element saveToDom(Element element) {
        if (isDirectory()) {
            element.setAttribute("type", "directory");
            element.setAttribute("empty", "" + empty);
        } else {
            element.setAttribute("type", "file");
            
            element.setAttribute("size",       "" + size);
            element.setAttribute("modified",   "" + lastModified);
            element.setAttribute("md5",        "" + md5);
            element.setAttribute("jar",        "" + jarFile);
            element.setAttribute("packed-jar", "" + packedJarFile);
            element.setAttribute("signed-jar", "" + signedJarFile);
        }
        
        element.setTextContent(name);
        
        return element;
    }
}