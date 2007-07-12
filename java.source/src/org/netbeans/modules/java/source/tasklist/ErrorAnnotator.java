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
package org.netbeans.modules.java.source.tasklist;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Lahoda
 */
public class ErrorAnnotator extends AnnotationProvider /*implements FileStatusListener*/ {
    
    public ErrorAnnotator() {
    }

    public String annotateName(String name, Set files) {
        return null;
    }

    @Override
    public Image annotateIcon(Image icon, int iconType, Set files) {
        if (!TasklistSettings.isTasklistEnabled() || !TasklistSettings.isBadgesEnabled())
            return null;
        
        boolean inError = false;
        
        if (files instanceof NonRecursiveFolder) {
            FileObject folder = ((NonRecursiveFolder) files).getFolder();
            inError = isInError(folder, false, true);
        } else {
            for (Object o : files) {
                if (o instanceof FileObject) {
                    FileObject f = (FileObject) o;
                    
                    if (f.isFolder()) {
                        if (isInError(f, true, !inError)) {
                            inError = true;
                            continue;
                        }
                        if (inError)
                            continue;
                        
                        Project p = FileOwnerQuery.getOwner(f);
                        
                        if (p != null) {
                            for (SourceGroup sg : ProjectUtils.getSources(p).getSourceGroups("java"/*JavaProjectConstants.SOURCES_TYPE_JAVA*/)) {
                                FileObject sgRoot = sg.getRootFolder();
                                
                                if ((FileUtil.isParentOf(f, sgRoot) || f == sgRoot) && isInError(sgRoot, true, true)) {
                                    inError = true;
                                    break;
                                }
                            }
                            
                            if (inError) {
                                break;
                            }
                        }
                    }else {
                        if (f.isData() && "java".equals(f.getExt())) {
                            if (isInError(f, true, !inError)) {
                                inError = true;
                            }
                        }
                    }
                }
            }
        }
        
        Logger.getLogger(ErrorAnnotator.class.getName()).log(Level.FINE, "files={0}, in error={1}", new Object[] {files, inError});
        
        if (inError) {
            //badge:
            Image i = Utilities.mergeImages(icon, Utilities.loadImage("org/netbeans/modules/java/source/resources/icons/error-badge.gif"), 0, 8);
            Iterator<? extends AnnotationProvider> it = Lookup.getDefault().lookupAll(AnnotationProvider.class).iterator();
            boolean found = false;
            
            while (it.hasNext()) {
                AnnotationProvider p = it.next();
                
                if (found) {
                    Image res = p.annotateIcon(i, iconType, files);
                    
                    if (res != null) {
                        return res;
                    }
                } else {
                    found = p == this;
                }
            }
            
            return i;
        }
        
        return null;
    }

    public String annotateNameHtml(String name, Set files) {
        return null;
    }

    public Action[] actions(Set files) {
        return null;
    }

    public InterceptionListener getInterceptionListener() {
        return null;
    }
    
    public void updateAllInError() {
        try {
            File[] roots = File.listRoots();
            for (File root : roots) {
                FileObject rootFO = FileUtil.toFileObject(root);
                
                if (rootFO != null) {
                    fireFileStatusChanged(new FileStatusEvent(rootFO.getFileSystem(), true, false));
                }
            }
        } catch (FileStateInvalidException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    public synchronized void updateInError(Set<URL> urls)  {
        Set<FileObject> toRefresh = new HashSet<FileObject>();
        for (Iterator<FileObject> it = knownFiles2Error.keySet().iterator(); it.hasNext(); ) {
            FileObject f = it.next();
            try {
                if (urls.contains(f.getURL())) {
                    toRefresh.add(f);
                    Integer i = knownFiles2Error.get(f);
                    
                    if (i != null) {
                        knownFiles2Error.put(f, i | INVALID);
                        
                        enqueue(f);
                    }
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }
    
    public void fireFileStatusChanged(Set<FileObject> fos) {
        if (fos.isEmpty())
            return ;
        try {
            fireFileStatusChanged(new FileStatusEvent(fos.iterator().next().getFileSystem(), fos, true, false));
        } catch (FileStateInvalidException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
    }
    
    public static ErrorAnnotator getAnnotator() {
        for (AnnotationProvider ap : Lookup.getDefault().lookupAll(AnnotationProvider.class)) {
            if (ap.getClass() == ErrorAnnotator.class) {
                return (ErrorAnnotator) ap;
            }
        }
        
        return null;
    }
    
    private static final int IN_ERROR_REC = 1;
    private static final int IN_ERROR_NONREC = 2;
    private static final int INVALID = 4;
    
    private Map<FileObject, Integer> knownFiles2Error = new WeakHashMap<FileObject, Integer>();
    
    private void enqueue(FileObject file) {
        if (toProcess == null) {
            toProcess = new LinkedList<FileObject>();
            WORKER.schedule(50);
        }
        
        toProcess.add(file);
    }
    
    private synchronized boolean isInError(FileObject file, boolean recursive, boolean forceValue) {
        boolean result = false;
        Integer i = knownFiles2Error.get(file);

        if (i != null) {
            result = (i & (recursive ? IN_ERROR_REC : IN_ERROR_NONREC)) != 0;
            
            if ((i & INVALID) == 0)
                return result;
        }
        
        if (!forceValue) {
            if (i == null) {
                knownFiles2Error.put(file, null);
            }
            return result;
        }

        enqueue(file);
        return result;
    }
    
    private Collection<FileObject> toProcess = null;
    private final RequestProcessor.Task WORKER = new RequestProcessor("ErrorAnnotator worker", 1).create(new Runnable() {
        public void run() {
            Collection<FileObject> toProcess;
            
            synchronized (ErrorAnnotator.this) {
                toProcess = ErrorAnnotator.this.toProcess;
                ErrorAnnotator.this.toProcess = null;
            }
            
            for (FileObject f : toProcess) {
                boolean recError;
                boolean nonRecError;
                if (f.isData()) {
                    recError = nonRecError = TaskCache.getDefault().isInError(f, true);
                } else {
                    recError = TaskCache.getDefault().isInError(f, true);
                    nonRecError = TaskCache.getDefault().isInError(f, false);
                }

                Integer value = (recError ? IN_ERROR_REC : 0) | (nonRecError ? IN_ERROR_NONREC : 0);

                synchronized (ErrorAnnotator.this) {
                    knownFiles2Error.put(f, value);
                }

                fireFileStatusChanged(Collections.singleton(f));
            }
        }
    });
}
