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
package org.netbeans.modules.localhistory.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 * Local History specific utilities.
 *
 * @author Tomas Stupka
 */
public class Utils {
    
    public static void revert(final Node[] nodes) {
        for(Node node : nodes) {
            StoreEntry se =  node.getLookup().lookup(StoreEntry.class);
            if(se != null) {
                Utils.revert(se);
            }
        }
    }
    
    public static void revert(StoreEntry se) {
        InputStream is = null;
        OutputStream os = null;
        try {
            FileObject fo = FileUtil.toFileObject(se.getFile());
            if(se.getStoreFile() != null) { // XXX change this semantic to isDeleted() or something similar
                if(fo == null) {
                    fo = FileUtil.createData(se.getFile());
                }
                os = getOutputStream(fo);
                is = se.getStoreFileInputStream();
                FileUtil.copy(is, os);
            } else {
                fo.delete();
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
        } finally {
            try {
                if(os != null) { os.close(); }
                if(is != null) { is.close(); }
            } catch (IOException e) {}
        }
    }
    
    private static OutputStream getOutputStream(FileObject fo) throws FileAlreadyLockedException, IOException, InterruptedException {
        int retry = 0;
        while (true) {
            try {
                return fo.getOutputStream();
            } catch (IOException ioe) {
                retry++;
                if (retry > 7) {
                    throw ioe;
                }
                Thread.sleep(retry * 30);
            }
        }
    }
    
}
