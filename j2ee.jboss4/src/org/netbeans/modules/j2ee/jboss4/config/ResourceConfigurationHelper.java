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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.jboss4.config;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author Libor Kotouc
 */
public class ResourceConfigurationHelper {
    
    private ResourceConfigurationHelper() {
    }
    
    public static void writeFile(final File file, final BaseBean bean) throws ConfigurationException {
        try {
            FileObject cfolder = FileUtil.toFileObject(file.getParentFile());
            if (cfolder == null) {
                File parentFile = file.getParentFile();
                try {
                    cfolder = FileUtil.toFileObject(parentFile.getParentFile()).createFolder(parentFile.getName());
                } catch (IOException ioe) {
                    throw new ConfigurationException(NbBundle.getMessage(ResourceConfigurationHelper.class, "MSG_FailedToCreateConfigFolder", parentFile.getAbsolutePath()));
                }
            }
            final FileObject folder = cfolder;
            FileSystem fs = folder.getFileSystem();
            fs.runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        String name = file.getName();
                        FileObject configFO = folder.getFileObject(name);
                        if (configFO == null) {
                            configFO = folder.createData(name);
                        }
                        lock = configFO.lock();
                        os = new BufferedOutputStream (configFO.getOutputStream(lock), 4086);
                        // TODO notification needed
                        if (bean != null) {
                            bean.write(os);
                        }
                    } finally {
                        if (os != null) {
                            try { os.close(); } catch(IOException ioe) {}
                        }
                        if (lock != null) 
                            lock.releaseLock();
                    }
                }
            });
        } catch (IOException e) {
            throw new ConfigurationException (e.getLocalizedMessage ());
        }
    }
    
    /**
     * Replace the content of the document by the graph.
     */
    public static void replaceDocument(final StyledDocument doc, BaseBean graph) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            graph.write(out);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        NbDocument.runAtomic(doc, new Runnable() {
            public void run() {
                try {
                    doc.remove(0, doc.getLength());
                    doc.insertString(0, out.toString(), null);
                } catch (BadLocationException ble) {
                    ErrorManager.getDefault().notify(ble);
                }
            }
        });
    }
    
}
