/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is the CVSROOT Selector (RFE #65366).
 * The Initial Developer of the Original Software is Michael Nascimento Santos.
 * Portions created by Michael Nascimento Santos are Copyright (C) 2005.
 * All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.versioning.system.cvss.ui.actions;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.awt.StatusDisplayer;

/**
 * Rewrites CVS/Root files with new content. Acts on one root folder recursively. 
 * 
 * @author Michael Nascimento Santos
 */
public class CvsRootRewriter {
    private static final String CVS_FOLDER = "CVS";  // NOI18N
    private static final String ROOT_FILE = "Root";  // NOI18N

    private final File file;
    private final String newRoot;
    private ProgressHandle handle;
    
    public CvsRootRewriter(File file, String newRoot) {
        this.file = file;
        this.newRoot = newRoot;
    }
    
    public void rewrite() {
        StatusDisplayer.getDefault().setStatusText("");  // NOI18N
        
        try {
            final Collection toRewrite = scanForFiles();
            rewriteFolders(toRewrite);
            
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(CvsRootRewriter.class, "MSG_CVSRootRewriter_Success"));  // NOI18N
        } catch (IOException ex) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(CvsRootRewriter.class, "MSG_CVSRootRewriter_Failure", ex.getMessage()));  // NOI18N
        };
    }
    
    private Collection scanForFiles() throws IOException {
        Collection<File> toRewrite = new HashSet<File>();
        Collection<File> toScan = new HashSet<File>();
        toScan.add(file);
        
        handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(CvsRootRewriter.class, "MSG_CVSRootRewriter_Progress"));  // NOI18N
        
        int i = 0;
        int totalSize = 1;
        
        handle.start(100);
        int lastValue = 0;
        
        do {
            Collection<File> toAdd = new HashSet<File>();
            
            for (Iterator it = toScan.iterator(); it.hasNext(); i++) {
                File folder = (File)it.next();
                it.remove();
                
                File rootFile = getCvsRootFile(folder);
                
                if (rootFile == null) {
                    continue;
                }
                
                handle.progress(NbBundle.getMessage(CvsRootRewriter.class, "MSG_CVSRootRewriter_Scanning", folder.getPath()));  // NOI18N
                
                toRewrite.add(rootFile);
                toAdd.addAll(Arrays.asList(folder.listFiles(new FileFilter() {
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }
                })));
            }
            
            toScan.addAll(toAdd);
            totalSize += toScan.size();
            
            lastValue = Math.max(lastValue, (int)((i / (double)totalSize) * 50));
            handle.progress(lastValue);
        } while (!toScan.isEmpty());
        
        return toRewrite;
    }
    
    private void rewriteFolders(final Collection toRewrite) {
        int i = 0;
        
        for (Iterator it = toRewrite.iterator(); it.hasNext();) {
            File rootFile = (File)it.next();
            
            if (rootFile.exists() && rootFile.canWrite()) {
                PrintStream ps = null;
                
                try {
                    ps = new PrintStream(new BufferedOutputStream(
                            new FileOutputStream(rootFile)));
                    ps.println(newRoot);
                } catch (IOException ioe) {
                    notify(ioe);
                } finally {
                    if (ps != null) {
                        ps.close();
                    }
                }
                
            }
            
            handle.progress(NbBundle.getMessage(CvsRootRewriter.class, "MSG_CVSRootRewriter_Rewriting", rootFile.getPath()), 
                            ((int)((++i / (double)toRewrite.size()) * 50)) + 50);
        }
        
        handle.finish();
    }
    
    static File getCvsRootFile(File folder) throws IOException {
        if (folder == null || !folder.isDirectory()) {
            return null;
        }
        
        File rootFile = new File(folder, CVS_FOLDER + File.separator + ROOT_FILE);
        
        // TODO In light of issue #68881 the code should be improved to recalculate
        // CVS/Repository files (using absolute path format) too.
        
        File repositoryFile = new File(folder, CVS_FOLDER + File.separator + "Repository");
        
        if  (repositoryFile.canRead()) {
            InputStream in = null;
            try {
                in = new FileInputStream(repositoryFile);
                if (in.read() == '/') {
                    throw new IOException("#68881 Absolute CVS/Repository paths are unsupported.");
                }
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException alreadyClosed) {
                    }
                }
            }
        }
        
        return (rootFile.exists() && !rootFile.isDirectory()) ? rootFile : null;
    }
    
    static String getCvsRoot(File folder) throws IOException {
        File rootFile = getCvsRootFile(folder);
        
        if (rootFile == null) {
            return null;
        }
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(rootFile));
            
            try {
                try {
                    return reader.readLine();
                } catch (IOException ex) {
                    notify(ex);
                    return null;
                }
            } finally {
                try {
                    reader.close();
                } catch (IOException ex) {
                    notify(ex);
                }
            }
        } catch (FileNotFoundException ex) {
            notify(ex);
            
            return null;
        }
    }
    
    private static void notify(Throwable t) {
        ErrorManager.getDefault().notify(ErrorManager.ERROR, t);
    }
}
