/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.masterfs.filebasedfs.utils;

import org.netbeans.modules.masterfs.filebasedfs.fileobjects.WriteLockUtils;
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
    //public static final int FLAG_isWindowsFloppy = 4;
    public static final int FLAG_isUnixSpecialFile = 5;
    public static final int FLAG_isUNC = 6;    
    public static final int FLAG_isFloppy = 7;
    //public static final int FLAG_isWindows = 8;
    public static final int FLAG_isConvertibleToFileObject = 9;


    private int isFile = -1;
    private int isDirectory = -1;
    private int exists = -1;
    private int isComputeNode = -1;
    private int isUnixSpecialFile = -1;
    private int isUNC = -1;    
    private int isFloppy = -1;
    private int isConvertibleToFileObject = -1;

    private Integer id = null;        
    private FileInfo root = null;    
    private final File file;
    
    private FileInfo parent = null;
    private FileNaming fileNaming = null;
    private FileObject fObject = null;
    

    public FileInfo(final File file, int exists) {
        this.file = file;
        this.exists = exists; 
    }

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
            exists = (FileChangedManager.getInstance().exists(getFile())) ? 1 : 0;
        }
        return (exists == 0) ? false : true;
    }

    public boolean isComputeNode() {
        if (isComputeNode == -1) {
            isComputeNode = (FileInfo.FILESYSTEMVIEW.isComputerNode(getFile())) ? 1 : 0;
        }

        return (isComputeNode == 1) ? true : false;
    }


    // XXX this is identical to isFloppy, why is it here?
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
            isConvertibleToFileObject = (isSupportedFile() && exists()) ?  1 : 0;
        }
        
        return (isConvertibleToFileObject == 1) ? true : false;
    }

    public boolean isSupportedFile() {
        return (!getFile().getName().equals(".nbattrs") &&
                !WriteLockUtils.hasActiveLockFileSigns(getFile().getAbsolutePath()) && 
                (getFile().getParent() != null || !isWindowsFloppy())) ;
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
/*
             case FLAG_isWindows:
                 isWindows = (value) ? 1 : 0;                                  
                break;
             case FLAG_isWindowsFloppy:
                 isWindowsFloppy = (value) ? 1 : 0;                                  
                break;            
*/
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

    public String toString() {
    return getFile().toString();
    }

    public static final String composeName(String name, String ext) {
        return (ext != null && ext.length() > 0) ? (name + "." + ext) : name;//NOI18N
    }

    public static final String getName(String name) {
        int i = name.lastIndexOf('.');
        
        /** period at first position is not considered as extension-separator */
        return (i <= 0 || i == (name.length()-1)) ? name : name.substring(0, i);
    }
    
    public static final String getExt(String name) {
        int i = name.lastIndexOf('.') + 1;
        
        /** period at first position is not considered as extension-separator */
        return ((i <= 1) || (i == name.length())) ? "" : name.substring(i); // NOI18N
    }
}
