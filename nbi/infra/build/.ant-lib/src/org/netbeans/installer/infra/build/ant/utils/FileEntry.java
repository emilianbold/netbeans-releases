/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */
package org.netbeans.installer.infra.build.ant.utils;

import java.io.File;
import java.io.IOException;

/**
 * This class represents the collection of meta data for a file.
 * 
 * @author Kirill Sorokin
 */
public class FileEntry {
    /**
     * The file's size.
     */
    private long size;
    
    /**
     * The file's MD5 checksum.
     */
    private String md5;
    
    /**
     * Whether the entry is a file or a directory.
     */
    private boolean directory;
    
    /**
     * Whether the directory is empty.
     */
    private boolean empty;
    
    /**
     * Whether the file is a jar file.
     */
    private boolean jarFile;
    
    /**
     * Whether the file is a packed jar file.
     */
    private boolean packedJarFile;
    
    /**
     * Whether the file is a signed jar file.
     */
    private boolean signedJarFile;
    
    /**
     * The file's modification date.
     */
    private long lastModified;
    
    /**
     * The file's permissions (e.g. 775).
     */
    private int permissions;
    
    /**
     * The entry's name.
     */
    private String name;
    
    /**
     * Constructs a new <code>FileEntry</code> instance from the given file.
     * 
     * @param file The file for which to calculate the metadata.
     * @param name The name of the entry.
     * @throws java.io.IOException if an I/O error occurs.
     */
    public FileEntry(final File file, final String name) throws IOException {
        this.directory = file.isDirectory();
        
        if (!directory) {
            this.size = file.length();
            this.md5  = Utils.getMd5(file);
            
            
            this.jarFile = Utils.isJarFile(file);
            if (jarFile) {
                this.packedJarFile = false; // we cannot determine this
                                this.signedJarFile = Utils.isSigned(file);
            }
        }  else {
            this.empty = Utils.isEmpty(file);
        }
        
        this.lastModified  = file.lastModified();
        this.name          = name;
    }
    
    /**
     * Getter of the 'size' property.
     * 
     * @return Value of the 'size' property.
     */
    public long getSize() {
        return size;
    }
    
    /**
     * Setter for the 'size' property.
     * 
     * @param size New value for the 'size' property.
     */
    public void setSize(final long size) {
        this.size = size;
    }
    
    /**
     * Getter of the 'md5' property.
     * 
     * @return Value of the 'md5' property.
     */
    public String getMd5() {
        return md5;
    }
    
    /**
     * Setter for the 'md5' property.
     * 
     * @param md5 New value for the 'md5' property.
     */
    public void setMd5(final String md5) {
        this.md5 = md5;
    }
    
    /**
     * Getter of the 'directory' property.
     * 
     * @return Value of the 'directory' property.
     */
    public boolean isDirectory() {
        return directory;
    }
    
    /**
     * Setter for the 'directory' property.
     * 
     * @param directory New value for the 'directory' property.
     */
    public void setDirectory(final boolean directory) {
        this.directory = directory;
    }
    
    /**
     * Getter of the 'empty' property.
     * 
     * @return Value of the 'empty' property.
     */
    public boolean isEmpty() {
        return empty;
    }
    
    /**
     * Setter for the 'empty' property.
     * 
     * @param empty New value for the 'empty' property.
     */
    public void setEmpty(final boolean empty) {
        this.empty = empty;
    }
    
    /**
     * Getter of the 'jarFile' property.
     * 
     * @return Value of the 'jarFile' property.
     */
    public boolean isJarFile() {
        return jarFile;
    }
    
    /**
     * Setter for the 'jarFile' property.
     * 
     * @param jarFile New value for the 'jarFile' property.
     */
    public void setJarFile(final boolean jarFile) {
        this.jarFile = jarFile;
    }
    
    /**
     * Getter of the 'packedJarFile' property.
     * 
     * @return Value of the 'packedJarFile' property.
     */
    public boolean isPackedJarFile() {
        return packedJarFile;
    }
    
    /**
     * Setter for the 'packedJarFile' property.
     * 
     * @param packedJarFile New value for the 'packedJarFile' property.
     */
    public void setPackedJarFile(final boolean packedJarFile) {
        this.packedJarFile = packedJarFile;
    }
    
    /**
     * Getter of the 'signedJarFile' property.
     * 
     * @return Value of the 'signedJarFile' property.
     */
    public boolean isSignedJarFile() {
        return signedJarFile;
    }
    
    /**
     * Setter for the 'signedJarFile' property.
     * 
     * @param signedJarFile New value for the 'signedJarFile' property.
     */
    public void setSignedJarFile(final boolean signedJarFile) {
        this.signedJarFile = signedJarFile;
    }
    
    /**
     * Getter of the 'lastModified' property.
     * 
     * @return Value of the 'lastModified' property.
     */
    public long getLastModified() {
        return lastModified;
    }
    
    /**
     * Setter for the 'lastModified' property.
     * 
     * @param lastModified New value for the 'lastModified' property.
     */
    public void setLastModified(final long lastModified) {
        this.lastModified = lastModified;
    }
    
    /**
     * Getter of the 'permissions' property.
     * 
     * @return Value of the 'permissions' property.
     */
    public int getPermissions() {
        return permissions;
    }
    
    /**
     * Setter for the 'permissions' property.
     * 
     * @param permissions New value for the 'permissions' property.
     */
    public void setPermissions(final int permissions) {
        this.permissions = permissions;
    }
    
    /**
     * Getter of the 'name' property.
     * 
     * @return Value of the 'name' property.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Setter for the 'name' property.
     * 
     * @param name New value for the 'name' property.
     */
    public void setName(final String name) {
        this.name = name;
    }
}
