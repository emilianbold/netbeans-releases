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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.WeakSet;

/**
 *
 * @author Jan Lahoda
 */
public class ErrorAnnotator extends AnnotationProvider /*implements FileStatusListener*/ {
    
    private Set<FileObject> knownFiles = new WeakSet<FileObject>();
    
    public ErrorAnnotator() {
    }

    public String annotateName(String name, Set files) {
        return null;
    }

    @Override
    public synchronized Image annotateIcon(Image icon, int iconType, Set files) {
        if (!TasklistSettings.isTasklistEnabled() || !TasklistSettings.isBadgesEnabled())
            return null;
        
        boolean inError = false;
        
        if (files instanceof NonRecursiveFolder) {
            FileObject folder = ((NonRecursiveFolder) files).getFolder();
            inError = TaskCache.getDefault().isInError(folder, false);
            knownFiles.add(folder);
        } else {
            for (Object o : files) {
                if (o instanceof FileObject) {
                    FileObject f = (FileObject) o;
                    
                    if (f.isFolder()) {
                        knownFiles.add(f);
                        if (inError)
                            continue;
                        if (TaskCache.getDefault().isInError(f, true)) {
                            inError = true;
                            continue;
                        }
                        
                        Project p = FileOwnerQuery.getOwner(f);
                        
                        if (p != null) {
                            for (SourceGroup sg : ProjectUtils.getSources(p).getSourceGroups("java"/*JavaProjectConstants.SOURCES_TYPE_JAVA*/)) {
                                FileObject sgRoot = sg.getRootFolder();
                                
                                if ((FileUtil.isParentOf(f, sgRoot) || f == sgRoot) && TaskCache.getDefault().isInError(sgRoot, true)) {
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
                            knownFiles.add(f);
                            if (inError)
                                continue;
                            if (TaskCache.getDefault().isInError(f, true)) {
                                inError = true;
                                continue;
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
        for (FileObject f : knownFiles) {
            try {
                if (urls.contains(f.getURL())) {
                    toRefresh.add(f);
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        fireFileStatusChanged(toRefresh);
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
    
}
