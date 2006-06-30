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


package org.netbeans.modules.image;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Enumeration;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.OpenSupport;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableTopComponent;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;


/** 
 * OpenSupport flavored with some <code>CloneableEditorSupport</code> features like 
 * listening on changes of image file and renames on dataobject, 
 * so it can work appropriate in Editor window.
 *
 * @author Peter Zavadsky
 * @author Marian Petras
 */

public class ImageOpenSupport extends OpenSupport implements OpenCookie, CloseCookie {

    /** Saves last modified time. */
    private long lastSaveTime;

    /** Listens for changes on file. */
    private FileChangeListener fileChangeL; 

    /** Reloading task. */
    private Task reloadTask;
    

    /** Constructs ImageOpenSupportObject on given MultiDataObject.Entry. */
    public ImageOpenSupport(MultiDataObject.Entry entry) {
        super(entry, new Environment(entry.getDataObject())); // TEMP
    }

    
    /** Creates the CloenableTOPComponent viewer of image. */
    public CloneableTopComponent createCloneableTopComponent () {
        prepareViewer();
        return new ImageViewer((ImageDataObject)entry.getDataObject());
    }

    /** Set listener for changes on image file. */
    void prepareViewer() {
        // listen for changes on the image file
        if(fileChangeL == null) {
            fileChangeL = new FileChangeAdapter() {
                public void fileChanged(final FileEvent evt) {
                    if(allEditors.isEmpty()) {
                        return;
                    }
                    
                    if(evt.getFile().isVirtual()) {
                        entry.getFile().removeFileChangeListener(this);
                        // File doesn't exist on disk - simulate env
                        // invalidation.
                        ((Environment)ImageOpenSupport.this.env).fileRemoved();
                        entry.getFile().addFileChangeListener(this);
                        return;
                    }
                    
                    if (evt.getTime() > lastSaveTime) {
                        lastSaveTime = System.currentTimeMillis();
                        
                        // Post in new task.
                        if(reloadTask == null || reloadTask.isFinished()) {
                        
                            reloadTask = RequestProcessor.postRequest(
                                new Runnable() {
                                    public void run() {
                                        reload(evt);
                                    }
                                }
                            );
                        }
                    }
                }
            };
        }
        entry.getFile().addFileChangeListener(fileChangeL);
        lastSaveTime = System.currentTimeMillis();
    }

    /** Ask and reload/close image views. */
    private void reload(FileEvent evt) {
        // ask if reload?
        // XXX the following is a resource path in NB 3.x and a URL after build system
        // merge; better to produce something nicer (e.g. FileUtil.toFile):
        String msg = NbBundle.getMessage(ImageOpenSupport.class, "MSG_ExternalChange", entry.getFile() );
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
        Object ret = DialogDisplayer.getDefault().notify(nd);

        if (NotifyDescriptor.YES_OPTION.equals(ret)) {
            // due to compiler 1.2 bug only
            final ImageDataObject imageObj = (ImageDataObject)entry.getDataObject();
            final CloneableTopComponent.Ref editors = allEditors;

            Enumeration e = editors.getComponents();
            while(e.hasMoreElements()) {
                final Object pane = e.nextElement();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        ((ImageViewer)pane).updateView(imageObj);
                    }
                });
            }
        }
    }
    
    /** Environment for image open support. */
    private static class Environment extends OpenSupport.Env {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -1934890789745432254L;

        /** Constructor. */
        public Environment(DataObject dataObject) {
            super(dataObject);
        }
        
        
        /** Overrides superclass method. Gets from OpenCookie. */
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (CloneableOpenSupport)getDataObject().getCookie(OpenCookie.class);
        }
        
        /** Called from enclosing support.
         * The components are going to be closed anyway and in case of
         * modified document its asked before if to save the change. */
        private void fileRemoved() {
            try {
                fireVetoableChange(PROP_VALID, Boolean.TRUE, Boolean.FALSE);
            } catch(PropertyVetoException pve) {
                // Ignore.
            }
            
            firePropertyChange(PROP_VALID, Boolean.TRUE, Boolean.FALSE);
        }
    } // End of nested Environment class.
}

