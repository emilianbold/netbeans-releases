/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.versioning;

import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.modules.masterfs.providers.AnnotationProvider;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.FlatFolder;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.openide.filesystems.*;

import javax.swing.*;
import java.util.*;
import java.awt.Image;
import java.io.File;

/**
 * Plugs into IDE filesystem and delegates annotation work to registered versioning systems.
 * 
 * @author Maros Sandor
 */
public class VersioningAnnotationProvider extends AnnotationProvider {
    
    static VersioningAnnotationProvider instance;

    public VersioningAnnotationProvider() {
        instance = this;
    }
    
    private VCSContext createContext(Set<FileObject> files) {
        Set<File> roots = new HashSet<File>(files.size());
        if (files instanceof NonRecursiveFolder) {
            FileObject folder = ((NonRecursiveFolder) files).getFolder();
            roots.add(new FlatFolder(FileUtil.toFile(folder).getAbsolutePath()));
        } else {
            for (FileObject fo : files) {
                roots.add(FileUtil.toFile(fo));
            }
        }
        return new VCSContext(roots);
    }

    public Image annotateIcon(Image icon, int iconType, Set files) {
        FileObject fo = (FileObject) files.iterator().next();
        VersioningSystem vs = VersioningManager.getInstance().getOwner(FileUtil.toFile(fo));
        
        if (vs == null) return null;
        VCSAnnotator an = vs.getVCSAnnotator();
        if (an == null) return null;

        VCSContext context = createContext(files);
        return an.annotateIcon(icon, context);
    }

    public String annotateNameHtml(String name, Set files) {
        FileObject fo = (FileObject) files.iterator().next();
        VersioningSystem vs = VersioningManager.getInstance().getOwner(FileUtil.toFile(fo));
        
        if (vs == null) return null;
        VCSAnnotator an = vs.getVCSAnnotator();
        if (an == null) return null;

        VCSContext context = createContext(files);
        return an.annotateName(name, context);
    }

    public Action[] actions(Set files) {
        FileObject fo = (FileObject) files.iterator().next();
        VersioningSystem vs = VersioningManager.getInstance().getOwner(FileUtil.toFile(fo));
        
        if (vs == null) return null;
        VCSAnnotator an = vs.getVCSAnnotator();
        if (an == null) return null;

        VCSContext context = createContext(files);
        return an.getActions(context);
    }

    public InterceptionListener getInterceptionListener() {
        return VersioningManager.getInstance().getInterceptionListener();
    }

    public String annotateName(String name, Set files) {
        return name;    // do not support 'plain' annotations
    }

    public void refreshAllAnnotations(boolean icon, boolean text) {
        Set<FileSystem> filesystems = new HashSet<FileSystem>(1);
        File[] allRoots = File.listRoots();
        for (int i = 0; i < allRoots.length; i++) {
            File root = allRoots[i];
            FileObject fo = FileUtil.toFileObject(root);
            if (fo != null) {
                try {
                    filesystems.add(fo.getFileSystem());
                } catch (FileStateInvalidException e) {
                    // ignore invalid filesystems
                }
            }
        }
        for (Iterator<FileSystem> i = filesystems.iterator(); i.hasNext();) {
            FileSystem fileSystem = i.next();
            fireFileStatusChanged(new FileStatusEvent(fileSystem, icon, text));                
        }
    }
    
    /**
     * Refreshes annotations for all given files and all parent folders of those files.
     * 
     * @param filesToRefresh files to refresh
     */
    void refreshAnnotations(Set<File> filesToRefresh) {
        if (filesToRefresh == null) {
            refreshAllAnnotations(true, true);
            return;
        }
        Map<FileSystem, Set<FileObject>> folders = new HashMap<FileSystem, Set<FileObject>>();
        for (File file : filesToRefresh) {
            for (File parent = file.getParentFile(); parent != null; parent = parent.getParentFile()) {
                try {
                    FileObject fo = FileUtil.toFileObject(parent);
                    if (fo != null) {
                        FileSystem fs = fo.getFileSystem();
                        Set<FileObject> fsFolders = folders.get(fs);
                        if (fsFolders == null) {
                            fsFolders = new HashSet<FileObject>();
                            folders.put(fs, fsFolders);
                        }
                        fsFolders.add(fo);
                    }
                } catch (FileStateInvalidException e) {
                    // ignore files in invalid filesystems
                }
            }
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                try {
                    fireFileStatusChanged(new FileStatusEvent(fo.getFileSystem(), fo, fo.isFolder(), true));
                } catch (FileStateInvalidException e) {
                    // ignore files in invalid filesystems
                }
            }
        }
        for (Iterator i = folders.keySet().iterator(); i.hasNext();) {
            FileSystem fs = (FileSystem) i.next();
            Set files = folders.get(fs);
            fireFileStatusChanged(new FileStatusEvent(fs, files, true, false));
        }
    }
}
