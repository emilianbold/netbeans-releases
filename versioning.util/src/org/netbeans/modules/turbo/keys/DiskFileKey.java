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

package org.netbeans.modules.turbo.keys;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.ErrorManager;

import java.io.File;

/**
 * Key for FileObject with identity given by disk files.
 * It means that keys can be equal for non-equal FileObjects.
 *
 * @author Petr Kuzel
 */
public final class DiskFileKey {
    private final FileObject fileObject;
    private final int hashCode;
    private String absolutePath;


    public static DiskFileKey createKey(FileObject fo) {
        return new DiskFileKey(fo);
    }

    private DiskFileKey(FileObject fo) {

        // PERFORMANCE optimalization, it saves memory because elimintes nedd for creating absolute paths.
        // XXX unwrap from MasterFileSystem, hidden dependency on "VCS-Native-FileObject" attribute knowledge
        // Unfortunately MasterFileSystem API does not support generic unwrapping.
        FileObject nativeFileObject = (FileObject) fo.getAttribute("VCS-Native-FileObject");  // NOI18N
        if (nativeFileObject == null) nativeFileObject = fo;


        fileObject = fo;
        hashCode = fo.getNameExt().hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof DiskFileKey) {

            DiskFileKey key = (DiskFileKey) o;

            if (hashCode != key.hashCode) return false;
            FileObject fo2 = key.fileObject;
            FileObject fo = fileObject;

            if (fo == fo2) return true;

            try {
                FileSystem fs = fo.getFileSystem();
                FileSystem fs2 = fo2.getFileSystem();
                if (fs.equals(fs2)) {
                    return fo.equals(fo2);
                } else {
                    // fallback use absolute paths (cache them)
                    if (absolutePath == null) {
                        File f = FileUtil.toFile(fo);
                        absolutePath = f.getAbsolutePath();
                    }
                    if (key.absolutePath == null) {
                        File f2 = FileUtil.toFile(fo2);
                        key.absolutePath = f2.getAbsolutePath();
                    }
                    return absolutePath.equals(key.absolutePath);
                }
            } catch (FileStateInvalidException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.notify(e);
            }
        }
        return false;
    }

    public int hashCode() {
        return hashCode;
    }

    public String toString() {
        if (absolutePath != null) {
            return absolutePath;
        }
        return fileObject.toString();
    }
}
