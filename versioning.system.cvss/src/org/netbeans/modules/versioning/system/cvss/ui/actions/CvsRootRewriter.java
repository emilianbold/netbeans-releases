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
 * The Original Software is the CVSROOT Selector (RFE #65366).
 * The Initial Developer of the Original Software is Michael Nascimento Santos.
 * Portions created by Michael Nascimento Santos are Copyright (C) 2005.
 * All Rights Reserved.
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
    private static final String CVS_FOLDER = "CVS";
    private static final String ROOT_FILE = "Root";

    private final File file;
    private final String newRoot;
    private ProgressHandle handle;
    
    public CvsRootRewriter(File file, String newRoot) {
        this.file = file;
        this.newRoot = newRoot;
    }
    
    public void rewrite() {
        StatusDisplayer.getDefault().setStatusText("");
        
        try {
            final Collection toRewrite = scanForFiles();
            rewriteFolders(toRewrite);
            
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(CvsRootRewriter.class, "MSG_CVSRootRewriter_Success"));
        } catch (IOException ex) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(CvsRootRewriter.class, "MSG_CVSRootRewriter_Failure", ex.getMessage()));
        };
    }
    
    private Collection scanForFiles() throws IOException {
        Collection<File> toRewrite = new HashSet<File>();
        Collection<File> toScan = new HashSet<File>();
        toScan.add(file);
        
        handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(CvsRootRewriter.class, "MSG_CVSRootRewriter_Progress"));
        
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
                
                handle.progress(NbBundle.getMessage(CvsRootRewriter.class, "MSG_CVSRootRewriter_Scanning", folder.getPath()));
                
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