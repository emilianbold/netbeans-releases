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

package org.netbeans.modules.masterfs.filebasedfs.naming;


import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Radek Matous
 */
public class FolderName extends FileName {
    private static Map fileCache = new WeakHashMap();


    FolderName(final FileNaming parent, final File file) {
        super(parent, file);
        synchronized (FolderName.class) {
            FolderName.fileCache.put(this, file);
        }
    }


    public File getFile() {
        File retValue;
        synchronized (FolderName.class) {
            retValue = (File) FolderName.fileCache.get(this);

            if (retValue == null) {
                retValue = super.getFile();
                FolderName.fileCache.put(this, retValue);
            }
        }

        assert retValue != null;
        return retValue;
    }

    static void freeCaches() {
        synchronized (FolderName.class) {
            FolderName.fileCache = new WeakHashMap();
        }

    }

    public boolean isFile() {
        return false;
    }

}