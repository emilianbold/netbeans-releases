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

package org.netbeans.modules.editor.settings.storage;

import java.io.IOException;
import java.io.OutputStream;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

/**
 *
 * @author Vita Stejskal
 */
public final class TestUtilities {
    
    /** Creates a new instance of TestUtilities */
    private TestUtilities() {
    }
    
    public static FileObject createFile(String path) throws IOException {
        return createFO(path, false, null);
    }
    
    public static FileObject createFile(String path, String contents) throws IOException {
        return createFO(path, false, contents);
    }
    
    public static FileObject createFolder(String path) throws IOException {
        return createFO(path, true, null);
    }
    
    private static FileObject createFO(final String path, final boolean folder, final String contents) throws IOException {
        Repository rp = Repository.getDefault();
        final FileSystem sfs = rp == null ? null : rp.getDefaultFileSystem();
        
        if (sfs == null) {
            throw new IOException("No system FS.");
        }

        final FileObject [] createdFo = new FileObject[1];
        sfs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject fo = sfs.getRoot();
                String [] pathElements = path.split("/", -1);
                for (int i = 0; i < pathElements.length; i++ ) {
                    String elementName = pathElements[i];

                    if (elementName.length() == 0) {
                        continue;
                    }

                    FileObject f = fo.getFileObject(elementName);
                    if (f != null && f.isValid()) {
                        fo = f;
                    } else {
                        if (i + 1 < pathElements.length || folder) {
                            fo = fo.createFolder(elementName);
                        } else {
                            // The last element in the path should be a file
                            fo = fo.createData(elementName);
                            if (contents != null) {
                                OutputStream os = fo.getOutputStream();
                                try {
                                    os.write(contents.getBytes());
                                } finally {
                                    os.close();
                                }
                            }
                        }
                    }
                }
                createdFo[0] = fo;
            }
        });
        
        return createdFo[0];
    }

    public static void delete(String path) throws IOException {
        Repository rp = Repository.getDefault();
        FileSystem sfs = rp == null ? null : rp.getDefaultFileSystem();
        
        if (sfs == null) {
            throw new IOException("No system FS.");
        }

        FileObject fo = sfs.findResource(path);
        if (fo != null) {
            fo.delete();
        }
    }
    
}
