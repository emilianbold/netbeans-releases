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

package org.netbeans.modules.masterfs.filebasedfs;

import org.netbeans.modules.masterfs.filebasedfs.fileobjects.BaseFileObj;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

import java.io.File;
import java.util.*;


/**
 * @author Radek Matous
 */
public final class FileBasedFileSystem extends FileSystem {
    private static Map allInstances = new HashMap();
    private final FileObjectFactory factory;
    private static final RefreshAction REFRESH_ACTION = (RefreshAction) SharedClassObject.findObject (RefreshAction.class, true);

    //only for tests purposes
    public static void reinitForTests() {
        //FileBasedFileSystem.allInstances = new HashMap();
    }

    public static FileBasedFileSystem getInstance(final File file) {
        FileBasedFileSystem retVal;
        final FileInfo fInfo = new FileInfo(file);
        final FileInfo rootInfo = fInfo.getRoot();

        synchronized (FileBasedFileSystem.allInstances) {
            final File rootFile = rootInfo.getFile();
            retVal = (FileBasedFileSystem) FileBasedFileSystem.allInstances.get(rootFile);
            if (retVal == null) {
                if (rootInfo.isConvertibleToFileObject()) {
                    retVal = new FileBasedFileSystem(rootFile);
                    FileBasedFileSystem.allInstances.put(rootFile, retVal);
                }
            }
        }
        return retVal;
    }

    private FileBasedFileSystem(final File rootFile) {
        this.factory = FileObjectFactory.getInstance(new FileInfo(rootFile));
    }

    public final org.openide.filesystems.FileObject findResource(final String name) {
        File f = new File(name);
        assert f.getAbsolutePath().replace('\\', '/').equals(name.replace('\\', '/')) : name + " versus " + f.getAbsolutePath();
        return findFileObject(f);
    }

    public final FileObject findFileObject(final File f) {
        return findFileObject(new FileInfo (f));
    }
    
    public final FileObject findFileObject(final FileInfo fInfo) {
        File f = fInfo.getFile();
        boolean issue45485 = fInfo.isWindows() && f.getName().endsWith(".");//NOI18N        
        if (issue45485) {
            File f2 = FileUtil.normalizeFile(f);
            issue45485 = !f2.getName().endsWith(".");
            if (issue45485) return null;
        }
        final BaseFileObj retVal = (BaseFileObj)(getFactory().findFileObject(fInfo));
        return (retVal != null && retVal.isValid()) ? retVal : null;
    }

    public final org.openide.filesystems.FileObject getRoot() {
        return getFactory().getRoot();
    }

    public final String getDisplayName() {
        return getFactory().getRoot().getRealRoot().getPath();
    }

    public final org.openide.util.actions.SystemAction[] getActions() {
        return new SystemAction[]{};
    }

    public final SystemAction[] getActions(final Set foSet) {
        return new SystemAction[]{FileBasedFileSystem.REFRESH_ACTION};

    }

    public final void refresh(final boolean expected) {
        getFactory().refreshAll(expected);
    }

    public final boolean isReadOnly() {
        return false;
    }

    public final String toString() {
        return getDisplayName();
    }

    public final FileObjectFactory getFactory() {
        return factory;
    }
}
