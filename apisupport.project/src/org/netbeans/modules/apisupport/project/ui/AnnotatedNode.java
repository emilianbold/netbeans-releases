/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui;

import java.awt.Image;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

class AnnotatedNode extends AbstractNode implements Runnable, FileStatusListener {
    
    private Set files;
    private Set fileSystemListeners;
    private RequestProcessor.Task task;
    private volatile boolean iconChange;
    private volatile boolean nameChange;
    private boolean forceAnnotation;
    
    protected AnnotatedNode(Children children) {
        super(children, null);
    }
    
    protected AnnotatedNode(Children children, Lookup lookup) {
        super(children, lookup);
    }
    
    protected final void setFiles(final Set files) {
        fileSystemListeners = new HashSet();
        this.files = files;
        if (files == null) {
            return;
        }
        Iterator it = files.iterator();
        Set hookedFileSystems = new HashSet();
        while (it.hasNext()) {
            FileObject fo = (FileObject) it.next();
            try {
                FileSystem fs = fo.getFileSystem();
                if (hookedFileSystems.contains(fs)) {
                    continue;
                }
                hookedFileSystems.add(fs);
                FileStatusListener fsl = FileUtil.weakFileStatusListener(this, fs);
                fs.addFileStatusListener(fsl);
                fileSystemListeners.add(fsl);
            } catch (FileStateInvalidException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.annotate(e, "Cannot get " + fo + " filesystem, ignoring...");  // NOI18N
                err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }
    
    protected final Set/*<FileObject>*/ getFiles() {
        return files;
    }
    
    protected void setForceAnnotation(boolean forceAnnotation) {
        this.forceAnnotation = forceAnnotation;
    }
    
    protected final Image annotateIcon(final Image img, final int type) {
        Image annotatedImg = img;
        if (files != null && files.iterator().hasNext()) {
            try {
                FileObject fo = (FileObject) files.iterator().next();
                annotatedImg = fo.getFileSystem().getStatus().annotateIcon(img, type, files);
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return annotatedImg;
    }
    
    protected final String annotateName(final String name) {
        String annotatedName = name;
        if (files != null && files.iterator().hasNext()) {
            try {
                FileObject fo = (FileObject) files.iterator().next();
                annotatedName = fo.getFileSystem().getStatus().annotateName(name, files);
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return annotatedName;
    }
    
    public final void annotationChanged(FileStatusEvent event) {
        if (task == null) {
            task = RequestProcessor.getDefault().create(this);
        }
        
        boolean changed = false;
        if (forceAnnotation || ((iconChange == false && event.isIconChange())  || (nameChange == false && event.isNameChange()))) {
            Iterator it = files.iterator();
            while (it.hasNext()) {
                FileObject fo = (FileObject) it.next();
                if (event.hasChanged(fo)) {
                    iconChange |= event.isIconChange();
                    nameChange |= event.isNameChange();
                    changed = true;
                }
            }
        }
        
        if (changed) {
            task.schedule(50); // batch by 50 ms
        }
    }
    
    public final void run() {
        if (forceAnnotation || iconChange) {
            fireIconChange();
            fireOpenedIconChange();
            iconChange = false;
        }
        if (forceAnnotation || nameChange) {
            fireDisplayNameChange(null, null);
            nameChange = false;
        }
    }
    
}
