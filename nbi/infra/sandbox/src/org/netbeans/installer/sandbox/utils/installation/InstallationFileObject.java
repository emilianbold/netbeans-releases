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
 * $Id$
 */

package org.netbeans.installer.sandbox.utils.installation;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.sandbox.utils.installation.conditions.FileCondition;

/**
 *
 * @author Dmitry Lipin
 */
public class InstallationFileObject {
    private HashMap <String, Object> fileData;
    
    public static final String PATH_KEY  = "path"; //NOI18N
    public static final String CRC32_KEY = "crc32";//NOI18N
    public static final String MD5_KEY   = "md5";  //NOI18N
    public static final String SHA1_KEY  = "sha1";//NOI18N
    public static final String SIZE_KEY  = "size";//NOI18N
    public static final String TIME_KEY  = "time";//NOI18N
    public static final String COND_KEY  = "cond";//NOI18N
    
    private FileCondition condition;
    public InstallationFileObject() {
    }
    public InstallationFileObject(File file) {
        setFile(file);
    }
    public InstallationFileObject(HashMap <String, Object> data) {
        setFileData(data);        
    }
    public InstallationFileObject(File file,HashMap <String, Object> data) {
        this(data);
        setFile(file);
    }
    public InstallationFileObject(File file, HashMap <String, Object> data, FileCondition cond) {
        this(file,data);
        setFileCondition(cond);        
    }
    public InstallationFileObject(File file, FileCondition conds) {
        this(file, new HashMap <String, Object> (),conds);
    }
    public void initDataFromFile() {
        setCRC32(getFile());
        setLastModified(getFile());
        setSize(getFile());
        setMD5(getFile());
        setSHA1(getFile());
    }
    
    public FileCondition getFileCondition() {
        return condition;
    }
    
    public void setFileCondition(FileCondition condition) {
        this.condition = condition;
    }
    
    public HashMap <String, Object> getFileData() {
        if(fileData==null) {
            fileData = new HashMap <String, Object> ();
        }
        return fileData;
    }
    
    public void setFileData(HashMap <String, Object> fileData) {
        this.fileData = fileData;
    }
    
    public File getFile() {
        return new File(getStringValue(PATH_KEY));
    }
    
    public void setFile(File file) {
        setData(PATH_KEY,file.getPath());
    }
    
    public long getSize() {
        return getLongValue(SIZE_KEY);
    }
    public long getLongValue(String key) {
        return ((Long)getData(key)).longValue();
    }
    public void setLongValue(String key, String value) {
        setData(key,Long.parseLong(value));
    }
    public void setSize(long size) {
        setData(SIZE_KEY,size);
    }
    public void setSize(String size) {
        setLongValue(SIZE_KEY,size);
    }
    public void setSize(File file) {
        if(file!=null && file.exists()&& file.isFile()) {
            setSize(FileUtils.getFileSize(file));
        }
    }
    public long getLastModified() {
        return getLongValue(TIME_KEY);
    }
    public String getStringValue(String key) {
        return (String)getData(key);
    }
    public void setLastModified(long tm) {
        setData(TIME_KEY,tm);
    }
    public void setLastModified(String tm) {
        setLongValue(TIME_KEY,tm);
    }
    public void setLastModified(File file) {
        if(file!=null && file.exists()&& file.isFile()) {
            setLastModified(FileUtils.getLastModified(file).getTime());
        }
    }
    public String getCRC32() {
        return getStringValue(CRC32_KEY);
    }
    
    public void setCRC32(String value) {
        setData(CRC32_KEY,value);
    }
    
    public void setCRC32(File file) {
        try {
            if(file!=null && file.exists()&& file.isFile()) {
                setCRC32(FileUtils.getCrc32String(file));
            }
        } catch (IOException ex) {
            LogManager.log(ErrorLevel.WARNING,ex);
        }
    }
    
    public String getMD5() {
        return getStringValue(MD5_KEY);
    }
    
    public void setMD5(String value) {
        setData(MD5_KEY,value);
    }
    public void setMD5(File file) {
        try {
            if(file!=null && file.exists()&& file.isFile()) {
                setMD5(FileUtils.getMd5String(file));
            }
        } catch (IOException ex) {
            LogManager.log(ErrorLevel.WARNING,ex);
        } catch (NoSuchAlgorithmException ex) {
            LogManager.log(ErrorLevel.WARNING,ex);
        }
    }
    public String getSHA1() {
        return getStringValue(SHA1_KEY);
    }
    public String getCondition() {
        return getStringValue(COND_KEY);
    }
    
    
    public void setSHA1(String value) {
        setData(SHA1_KEY,value);
    }
    public void setSHA1(File file) {
        try {
            if(file!=null && file.exists()&& file.isFile()) {
                setSHA1(FileUtils.getSha1String(file));
            }
        } catch (IOException ex) {
            LogManager.log(ErrorLevel.WARNING,ex);
        } catch (NoSuchAlgorithmException ex) {
            LogManager.log(ErrorLevel.WARNING,ex);
        }
    }
    public void setData(String key, Object value) {
        getFileData().put(key,value);
    }
    public Object getData(String key) {
        return getFileData().get(key);
    }
    
    public boolean accept() {
        return (condition==null) ? false : condition.accept(this);
    }
}

