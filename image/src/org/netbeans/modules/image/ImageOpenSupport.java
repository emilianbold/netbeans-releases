/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.image;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Enumeration;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.OpenSupport;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
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
 */

public class ImageOpenSupport extends OpenSupport implements OpenCookie {

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
        MessageFormat fmt = new MessageFormat(NbBundle.getBundle(ImageOpenSupport.class).getString("MSG_ExternalChange")); // NOI18N
        String msg = fmt.format(new Object[] { entry.getFile().getPackageNameExt('/', '.')});
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
        Object ret = TopManager.getDefault().notify(nd);

        if (NotifyDescriptor.YES_OPTION.equals(ret)) {
            // due to compiler 1.2 bug only
            final ImageDataObject imageObj = (ImageDataObject)entry.getDataObject();
            final CloneableTopComponent.Ref editors = allEditors;

            // Icon to reload.
            final NBImageIcon icon = new NBImageIcon(imageObj);

            Enumeration e = editors.getComponents();
            while(e.hasMoreElements()) {
                final Object pane = e.nextElement();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        ((ImageViewer)pane).reloadIcon(icon);
                    }
                });
            }
        }
    }
    
    /** Environment for image open support. */
    private static class Environment extends OpenSupport.Env {

        /** Constructor. */
        public Environment(DataObject dataObject) {
            super(dataObject);
        }
        
        
        /** Overrides superclass method. Gets from OpenCookie. */
        public CloneableOpenSupport findCloneableOpenSupport() {
            return (CloneableOpenSupport)getDataObject().getCookie(OpenCookie.class);
        }
    } // End of nested Environment class.
}

