/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs.filebasedfs.utils;

import org.netbeans.modules.masterfs.filebasedfs.fileobjects.WriteLock;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.netbeans.modules.masterfs.filebasedfs.naming.UNCName;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.openide.filesystems.FileObject;

public final class FileInfo {
    private static final FileSystemView FILESYSTEMVIEW = FileSystemView.getFileSystemView();
    private static boolean IS_WINDOWS = org.openide.util.Utilities.isWindows();

    public static final int FLAG_isFile = 0;
    public static final int FLAG_isDirectory = 1;
    public static final int FLAG_exists = 2;
    public static final int FLAG_isComputeNode = 3;
    public static final int FLAG_isWindowsFloppy = 4;
    public static final int FLAG_isUnixSpecialFile = 5;
    public static final int FLAG_isUNC = 6;    
    public static final int FLAG_isFloppy = 7;
    public static final int FLAG_isWindows = 8;
    public static final int FLAG_isConvertibleToFileObject = 9;


    private int isFile = -1;
    private int isDirectory = -1;
    private int exists = -1;
    private int isComputeNode = -1;
    private int isWindowsFloppy = -1;
    private int isUnixSpecialFile = -1;
    private int isUNC = -1;    
    private int isFloppy = -1;
    private int isWindows = -1;
    private int isConvertibleToFileObject = -1;

    private Integer id = null;        
    private FileInfo root = null;    
    private final File file;
    
    private FileInfo parent = null;
    private FileNaming fileNaming = null;
    private FileObject fObject = null;
    


    public FileInfo(final File file) {
        this.file = file;
    }

    public FileInfo(final FileInfo parent, final File file) {
        this (file);
        this.parent = parent;
    }
    
    public boolean isFile() {
        if (isFile == -1) {
            isFile = (getFile().isFile()) ? 1 : 0;
        }
        return (isFile == 0) ? false : true;
    }


    public boolean isDirectory() {
        if (isDirectory == -1) {
            isDirectory = (getFile().isDirectory()) ? 1 : 0;
        }
        return (isDirectory == 0) ? false : true;
    }


    public boolean  exists() {
        if (exists == -1) {
            exists = (getFile().exists()) ? 1 : 0;
        }
        return (exists == 0) ? false : true;
    }

    public boolean isComputeNode() {
        if (isComputeNode == -1) {
            isComputeNode = (FileInfo.FILESYSTEMVIEW.isComputerNode(getFile())) ? 1 : 0;
        }

        return (isComputeNode == 1) ? true : false;
    }


    public boolean isWindowsFloppy() {
        if (isFloppy == -1) {
            isFloppy = (FileInfo.FILESYSTEMVIEW.isFloppyDrive(getFile())) ? 1 : 0;
        }
        return (isFloppy == 1) ? true : false;
    }


    public boolean isUnixSpecialFile() {
        if (isUnixSpecialFile == -1) {
            isUnixSpecialFile = (!IS_WINDOWS && !isDirectory() && !isFile() && exists()) ? 1 : 0;
        }        
        return (isUnixSpecialFile == 1) ? true : false;
    }


    public boolean isUNCFolder() {
        if (isUNC == -1) {
            isUNC = ((getFile() instanceof UNCName.UNCFile) || ((isWindows() && !isFile() && !isDirectory() && !exists() && isComputeNode()))) ? 1 : 0;
        }                
        return (isUNC == 1) ? true : false;
    }


    public boolean isWindows() {
        return FileInfo.IS_WINDOWS;
    }
    
    public boolean isFloppy() {
        if (isFloppy == -1) {
            isFloppy = (FileInfo.FILESYSTEMVIEW.isFloppyDrive(getFile())) ? 1 : 0;
        }

        return (isFloppy == 1) ? true : false;
    }




    public boolean isConvertibleToFileObject() {
        if (isConvertibleToFileObject == -1) {
            isConvertibleToFileObject = (exists() && !WriteLock.isActiveLockFile(getFile()) && (getFile().getParent() != null || !isWindowsFloppy())) ?  1 : 0;
        }
        
        return (isConvertibleToFileObject == 1) ? true : false;
    }


    public FileInfo getRoot() {
        if (root == null) {
            File tmp = getFile();
            File retVal = tmp;
            while (tmp != null) {
                retVal = tmp;
                tmp = tmp.getParentFile();
            }
            
            root = new FileInfo (retVal);
        }
        
        return root;
    }


    public File getFile() {
        return file;
    }

    public Integer getID() {
        if (id == null) {
            id = NamingFactory.createID(getFile());
        }        
        return id;
    }

    public FileInfo getParent() {
        return parent;
    }
    
    public void setValueForFlag (int flag, boolean value) {
        switch (flag) {
            case FLAG_exists:
                 exists = (value) ? 1 : 0;                
                break;
             case FLAG_isComputeNode:
                 isComputeNode = (value) ? 1 : 0;
                break;
             case FLAG_isConvertibleToFileObject:
                 isConvertibleToFileObject = (value) ? 1 : 0;                 
                break;
             case FLAG_isDirectory:
                 isDirectory = (value) ? 1 : 0;                                  
                break;
             case FLAG_isFile:
                 isFile = (value) ? 1 : 0;                                  
                break;
             case FLAG_isFloppy:
                 isFloppy = (value) ? 1 : 0;                                  
                break;                
             case FLAG_isUNC:
                 isUNC = (value) ? 1 : 0;                                  
                break;
             case FLAG_isUnixSpecialFile:
                 isUnixSpecialFile = (value) ? 1 : 0;                                  
                break;
             case FLAG_isWindows:
                 isWindows = (value) ? 1 : 0;                                  
                break;
             case FLAG_isWindowsFloppy:
                 isWindowsFloppy = (value) ? 1 : 0;                                  
                break;            
        }
    }

    public FileNaming getFileNaming() {
        return fileNaming;
    }

    public void setFileNaming(FileNaming fileNaming) {
        this.fileNaming = fileNaming;
    }

    public FileObject getFObject() {
        return fObject;
    }

    public void setFObject(FileObject fObject) {
        this.fObject = fObject;
    }
}