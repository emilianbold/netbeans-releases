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

package org.netbeans.modules.derby;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;

/**
 *
 * @author Andrei Badea
 */
public class Util {

    private Util() {
    }

    public static boolean hasInstallLocation() {
        return getCheckedLocation() != null;
    }

    private static File getCheckedLocation() {
        File location = new File(DerbyOptions.getDefault().getLocation());
        if (location.isAbsolute() && location.isDirectory() && location.exists()) {
            return location;
        }
        return null;
    }

    public static File getDerbyFile(String relPath) {
        File location = getCheckedLocation();
        if (location != null) {
            return new File(location, relPath);
        }
        return null;
    }

    public static void showInformation(final String msg){
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        });
    }

    public static void extractZip(File source, FileObject target) throws IOException {
        FileInputStream is = new FileInputStream(source);
        try {
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry ze;

            while ((ze = zis.getNextEntry()) != null) {
                String name = ze.getName();

                // if directory, create
                if (ze.isDirectory()) {
                    FileUtil.createFolder(target, name);
                    continue;
                }

                // if file, copy
                FileObject fd = FileUtil.createData(target, name);
                FileLock lock = fd.lock();
                try {
                    OutputStream os = fd.getOutputStream(lock);
                    try {
                        FileUtil.copy(zis, os);
                    } finally {
                        os.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        } finally {
            is.close();
        }
    }
}
