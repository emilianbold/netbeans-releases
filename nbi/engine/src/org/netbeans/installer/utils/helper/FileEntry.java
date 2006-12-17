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
    private long size = 0;
    
    private String md5 = null;
    private String sha1 = null;
    private String crc32 = null;
    
    private boolean directory = false;
    private boolean empty = false;
    
    private boolean jarFile       = false;
    private boolean packedJarFile = false;
    private boolean signedJarFile = false;
    
    private long lastModified = 0;
    
    private int permissions = 0;
    
    private String name = null;
    
    public FileEntry(Element element) {
        String type = element.getAttribute("type");
        
        if (type.equals("directory")) {
            setDirectory(true);
            setEmpty(Boolean.parseBoolean(element.getAttribute("is-empty")));
        } else {
            setDirectory(false);
            
            setSize(new Long(element.getAttribute("size")));
            setLastModified(new Long(element.getAttribute("modified")));
            setMd5(element.getAttribute("md5"));
            setCrc32(element.getAttribute("crc32"));
            setSha1(element.getAttribute("sha1"));
            
            setJarFile(new Boolean(element.getAttribute("jar")));
            setPackedJarFile(new Boolean(element.getAttribute("packed-jar")));
            setSignedJarFile(new Boolean(element.getAttribute("signed-jar")));
        }
        
        setName(element.getTextContent());
    }
    
    public FileEntry(File file, String name) throws IOException, NoSuchAlgorithmException {
        this.directory = file.isDirectory();
        
        if (!directory) {
            this.size  = file.length();
            
            this.md5   = FileUtils.getMd5String(file);
            this.sha1  = FileUtils.getSha1String(file);
            this.crc32 = FileUtils.getCrc32String(file);
            
            this.jarFile = FileUtils.isJarFile(file);
            if (jarFile) {
                this.packedJarFile = false; // we cannot determine this
                this.signedJarFile = FileUtils.isSigned(file);
            }
        }  else {
            this.empty = FileUtils.isEmpty(file);
        }
        
        this.lastModified  = file.lastModified();
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
    
    public String getSha1() {
        return sha1;
    }
    
    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }
    
    public String getCrc32() {
        return crc32;
    }
    
    public void setCrc32(String crc32) {
        this.crc32 = crc32;
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
            element.setAttribute("empty", "" + isEmpty());
        } else {
            element.setAttribute("type", "file");
            
            element.setAttribute("size", ""+ getSize());
            element.setAttribute("modified", ""+ getLastModified());
            element.setAttribute("md5", ""+ getMd5());
            element.setAttribute("crc32", ""+ getCrc32());
            element.setAttribute("sha1", ""+ getSha1());
            element.setAttribute("jar", ""+ isJarFile());
            element.setAttribute("packed-jar", ""+ isPackedJarFile());
            element.setAttribute("signed-jar", ""+ isSignedJarFile());
        }
        
        element.setTextContent(getName());
        
        return element;
    }
}