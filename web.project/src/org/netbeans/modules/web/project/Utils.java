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

package org.netbeans.modules.web.project;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.io.File;
import java.io.IOException;

public class Utils {

    public static File getRoot(File f) {
        File rootF = f;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        return rootF;
    }

    public static FileObject getValidDir(File dir) throws IOException {
        final File f = dir.getCanonicalFile();
        f.mkdirs();
        if (!f.exists()) {
            throw new IOException("No such dir on disk: " + f);
        }
        if (!f.isDirectory()) {
            throw new IOException("Not really a dir" + ": " + f);
        }
        return FileUtil.toFileObject(f);
    }

    public static FileObject getValidEmptyDir(File dir) throws IOException {
        final FileObject fo = getValidDir(dir);
        if (fo.getChildren().length != 0) {
            throw new IOException("Dir has to be empty: " + dir);
        }
        return fo;
    }
}