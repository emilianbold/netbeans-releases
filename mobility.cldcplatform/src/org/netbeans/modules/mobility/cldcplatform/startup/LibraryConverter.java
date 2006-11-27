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

package org.netbeans.modules.mobility.cldcplatform.startup;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author Adam Sotona
 */
public class LibraryConverter extends FileChangeAdapter {
    
    /** Creates a new instance of LibraryConverter */
    public LibraryConverter() {
        FileSystem storageFS = Repository.getDefault().getDefaultFileSystem();
        try {
            FileObject rep = FileUtil.createFolder(storageFS.getRoot(), "org-netbeans-api-project-libraries/Libraries");  //NOI18N
            rep.addFileChangeListener(this);
            FileObject fo[] = rep.getChildren();
            for (int i=0; i < fo.length; i++) {
                convertLibrary(fo[i]);
            }
        } catch (IOException e) {
        }
    }
    
    public void fileDataCreated(FileEvent fe) {
        convertLibrary(fe.getFile());
    }
    
    public void convertLibrary(FileObject fo) {
        if (fo == null || !fo.isData() || !"xml".equals(fo.getExt())) return; //NOI18N
        int size = (int)fo.getSize();
        if (size <= 0) return;
        final byte buffer[] = new byte[size];
        DataInputStream in = null;
        try {
            in = new DataInputStream(fo.getInputStream());
            in.readFully(buffer);
            in.close();
            if (replace(buffer)) {
                final String name = fo.getNameExt();
                final FileObject parent = fo.getParent();
                fo.delete();
                parent.getFileSystem().runAtomicAction(new AtomicAction() {
                    public void run() throws IOException {
                        OutputStream out = null;
                        try {
                            out = parent.createData(name).getOutputStream();
                            out.write(buffer);
                        } finally {
                            if (out != null) try {out.close();} catch (IOException e) {}
                        }
                    }
                });
            }
        } catch (IOException ioe) {
            if (in != null) try {in.close();} catch (IOException e) {}
        }
    }

    private static final byte target[] = "<type>j2me</type>".getBytes(); //NOI18N
    
    private boolean replace(byte[] source) {
        if (source.length < target.length) return false;
        byte first  = target[0];
        int max = source.length - target.length;
        for (int i = 0; i <= max; i++) {
            if (source[i] != first) {
                while (++i <= max && source[i] != first);
            }
            if (i <= max) {
                int j = i + 1;
                int end = j + target.length - 1;
                for (int k = 1; j < end && source[j] == target[k]; j++, k++);
                if (j == end) {
                    /* replace j2me -> j2se */
                    source[i+8] = 's';
                    return true;
                }
            }
        }
        return false;
    }
    
    
}
