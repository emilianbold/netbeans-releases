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
import java.io.FileNotFoundException;
import java.io.IOException;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.StringUtils;

public class FileEntry {
    private File    file          = null;
    private String  name          = null;
    
    private boolean metaDataReady = false;
    
    private boolean directory     = false;
    private boolean empty         = false;
    
    private long    size          = 0;
    private String  md5           = null;
    
    private boolean jar           = false;
    private boolean packed        = false;
    private boolean signed        = false;
    
    private long    modified  = 0;
    
    private int     permissions   = 0;
    
    // constructors /////////////////////////////////////////////////////////////////
    public FileEntry(
            final File file) {
        this.file = file;
        this.name = file.
                getAbsolutePath().
                replace(FileUtils.BACKSLASH, FileUtils.SLASH);

        this.metaDataReady = false;
    }
    
    public FileEntry(
            final File file,
            final boolean empty,
            final long modified,
            final int permissions) {
        this(file);
        
        this.directory   = true;
        this.empty       = empty;
        this.modified    = modified;
        this.permissions = permissions;
        
        this.metaDataReady = true;
    }
    
    public FileEntry(
            final File file,
            final long size,
            final String md5,
            final boolean jar,
            final boolean packed,
            final boolean signed,
            final long modified,
            final int permissions) {
        this(file);
        
        this.directory   = false;
        this.size        = size;
        this.md5         = md5;
        this.jar         = jar;
        this.packed      = packed;
        this.signed      = signed;
        this.modified    = modified;
        this.permissions = permissions;
        
        this.metaDataReady = true;
    }
    
    // getters/setters //////////////////////////////////////////////////////////////
    public String getName() {
        return name;
    }
    
    public File getFile() {
        return file;
    }
    
    public boolean isMetaDataReady() {
        return metaDataReady;
    }
    
    public boolean isDirectory() {
        return directory;
    }
    
    public boolean isEmpty() {
        return empty;
    }
    
    public long getSize() {
        return size;
    }
    
    public String getMd5() {
        return md5;
    }
    
    public boolean isJarFile() {
        return jar;
    }
    
    public boolean isPackedJarFile() {
        return packed;
    }
    
    public boolean isSignedJarFile() {
        return signed;
    }
    
    public long getLastModified() {
        return modified;
    }
    
    public int getPermissions() {
        return permissions;
    }
    
    // object -> string /////////////////////////////////////////////////////////////
    public String toString() {
        if (directory) {
            return
                    name + StringUtils.LF +
                    directory + StringUtils.LF +
                    empty + StringUtils.LF +
                    modified + StringUtils.LF +
                    permissions + StringUtils.LF;
        } else {
            return
                    name + StringUtils.LF +
                    directory + StringUtils.LF +
                    size + StringUtils.LF +
                    md5 + StringUtils.LF +
                    jar + StringUtils.LF +
                    packed + StringUtils.LF +
                    signed + StringUtils.LF +
                    modified + StringUtils.LF +
                    permissions + StringUtils.LF;
        }
    }
    
    public String toXml() {
        if (directory) {
            return "<entry " +
                    "type=\"directory\" " +
                    "empty=\"" + empty + "\" " +
                    "modified=\"" + modified + "\" " +
                    "permissions=\"" + permissions + "\">" + name + "</entry>";
        } else {
            return "<entry " +
                    "type=\"file\" " +
                    "size=\"" + size + "\" " +
                    "md5=\"" + md5 + "\" " +
                    "jar=\"" + jar + "\" " +
                    "packed=\"" + packed + "\" " +
                    "signed=\"" + signed + "\" " +
                    "modified=\"" + modified + "\" " +
                    "permissions=\"" + permissions + "\">" + name + "</entry>";
        }
    }
    
    // miscellanea //////////////////////////////////////////////////////////////////
    public void calculateMetaData() throws IOException {
        if (file.exists()) {
            directory = file.isDirectory();
            
            if (!directory) {
                size = file.length();
                md5  = FileUtils.getMd5(file);
                jar  = FileUtils.isJarFile(file);
                
                if (jar) {
                    packed = false; // we cannot determine this
                    signed = FileUtils.isSigned(file);
                }
            } else {
                empty = FileUtils.isEmpty(file);
            }
            
            modified = file.lastModified();
            
            metaDataReady = true;
        } else {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
    }
}