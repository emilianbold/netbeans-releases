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
import org.netbeans.modules.masterfs.filebasedfs.naming.UNCName;

import javax.swing.filechooser.FileSystemView;
import java.io.File;

public final class FileInfo {
    private static final FileSystemView fsv = FileSystemView.getFileSystemView();

    private static final int TYPE_WINDOWS_FLOPPY = 0;
    private static final int TYPE_UNC_FOLDER = 1;
    private static final int TYPE_FILE = 2;
    private static final int TYPE_DIRECTORY = 3;
    private static final int TYPE_UNIX_SPECIAL_FILE = 4;

    private static final int TYPE_NOTEXISTING = 5;
    private static final int TYPE_UNKNOWN = 6;


    private int isFile = -1;
    private int isDirectory = -1;
    private int exists = -1;
    private int isComputeNode = -1;
    private int isFloppy = -1;
    private int isWindows = -1;

    private final File f;


    public FileInfo(final File f) {
        this.f = f;
    }

    public final FileInfo getRoot() {
        File rootFile = f;
        while (rootFile.getParentFile() != null) {
            rootFile = rootFile.getParentFile();
        }

        return new FileInfo(rootFile);
    }

    public final boolean isConvertibleToFileObject() {
        //TODO: lockfiles are hidden - rethink
        return (!isWindowsFloppy() && (getTypeCode() != FileInfo.TYPE_NOTEXISTING)) && !WriteLock.isActiveLockFile(f);
    }


    private boolean exists() {
        if (exists == -1) {
            assert isFloppy != -1 || !isWindows();
            exists = (f.exists()) ? 1 : 0;
        }

        return (exists == 1) ? true : false;
    }

    public final File getFile() {
        return f;
    }

    public final boolean isWindowsFloppy() {
        if (!org.openide.util.Utilities.isWindows ()) return false;
        return getTypeCode() == FileInfo.TYPE_WINDOWS_FLOPPY;
    }

    public final boolean isUNCFolder() {
        if (!org.openide.util.Utilities.isWindows ()) return false;
        return getTypeCode() == FileInfo.TYPE_UNC_FOLDER;
    }

    public final boolean isUnixSpecialFile() {
        return getTypeCode() == FileInfo.TYPE_UNIX_SPECIAL_FILE;
    }


    public final boolean isFile() {
        if (isFile == -1) {
            isFile = (f.isFile()) ? 1 : 0;
        }
        return (isFile == 1) ? true : false;
    }

    public final boolean isDirectory() {
        if (isDirectory == -1) {
            isDirectory = (f.isDirectory()) ? 1 : 0;
        }

        return (isDirectory == 1) ? true : false;
    }


    private int getTypeCode() {
        int retVal = -1;

        for (int i = 0; i <= FileInfo.TYPE_UNKNOWN && retVal == -1; i++) {
            switch (i) {
                case FileInfo.TYPE_WINDOWS_FLOPPY:
                    retVal = (isWindows() && isFloppy()) ? i : -1;
                    break;
                case FileInfo.TYPE_UNC_FOLDER:
                    retVal = (f instanceof UNCName.UNCFile) || ((isWindows() && !isFile() && !isDirectory() && !exists() && isComputeNode())) ? i : -1;
                    break;
                case FileInfo.TYPE_FILE:
                    retVal = (isFile()) ? i : -1;
                    break;
                case FileInfo.TYPE_DIRECTORY:
                    retVal = (isDirectory()) ? i : -1;
                    break;
                case FileInfo.TYPE_UNIX_SPECIAL_FILE:
                    retVal = (/*Utilities.isUnix() && */!isDirectory() && !isFile() && exists()) ? i : -1;
                    break;
                default:
                    retVal = FileInfo.TYPE_NOTEXISTING;
            }
        }
        return retVal;
    }


    private boolean isComputeNode() {
        if (isComputeNode == -1) {
            assert isWindows != -1;
            isComputeNode = (FileInfo.fsv.isComputerNode(f)) ? 1 : 0;
        }

        return (isComputeNode == 1) ? true : false;
    }

    private boolean isFloppy() {
        if (isFloppy == -1) {
            assert isWindows != -1;
            isFloppy = (FileInfo.fsv.isFloppyDrive(f)) ? 1 : 0;
        }

        return (isFloppy == 1) ? true : false;
    }

    private boolean isWindows() {
        if (isWindows == -1) {
            isWindows = (org.openide.util.Utilities.isWindows()) ? 1 : 0;
        }

        return (isWindows == 1) ? true : false;
    }
}