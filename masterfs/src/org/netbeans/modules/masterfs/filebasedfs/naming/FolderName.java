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