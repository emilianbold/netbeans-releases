/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db;

import java.util.ResourceBundle;
import java.text.MessageFormat;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.InstanceDataObject;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;

import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;

import org.openide.nodes.*;

public class DatabaseModule extends ModuleInstall {
    private ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N

    static final long serialVersionUID =5426465356344170725L;
    
    public void installed() {
        DialogDisplayer dd = DialogDisplayer.getDefault();

        try {
            FileSystem rfs = Repository.getDefault().getDefaultFileSystem();
            FileObject rootFolder = rfs.getRoot();
            FileObject databaseFileObject = rootFolder.getFileObject("Database"); //NOI18N
            if (databaseFileObject == null)
                databaseFileObject = rootFolder.createFolder("Database"); //NOI18N
            FileObject adaptorsFileObject = databaseFileObject.getFileObject("Adaptors"); //NOI18N
            if (adaptorsFileObject == null) {
                adaptorsFileObject = databaseFileObject.createFolder("Adaptors"); //NOI18N
                InstanceDataObject.create(DataFolder.findFolder(adaptorsFileObject), "DefaultAdaptor", org.netbeans.lib.ddl.adaptors.DefaultAdaptor.class); //NOI18N
            }
        } catch (LinkageError ex) {
            String msg = MessageFormat.format(bundle.getString("FMT_CLASSNOTFOUND"), new String[] {ex.getMessage()}); //NOI18N
            if (dd != null)
                dd.notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
        } catch (Exception ex) {
            String msg = MessageFormat.format(bundle.getString("FMT_EXCEPTIONINSTALL"), new String[] {ex.getMessage()}); //NOI18N
            if (dd != null)
                dd.notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
    
    public void uninstalled() {
        try {
            FileSystem rfs = Repository.getDefault().getDefaultFileSystem();
            FileObject rootFolder = rfs.getRoot();
            FileObject databaseFileObject = rootFolder.getFileObject("Database"); //NOI18N
            if (databaseFileObject != null) {
                FileObject adaptorsFileObject = databaseFileObject.getFileObject("Adaptors"); //NOI18N
                FileLock l = adaptorsFileObject.lock();
                try {
                    adaptorsFileObject.delete(l);
                } catch (Exception e) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) //NOI18N
                        System.out.println("DBExplorer: Uninstalled: "+e.getMessage()); //NOI18N
                } finally {
                    l.releaseLock();
                }
            }
        } catch (Exception ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) //NOI18N
                System.out.println("DBExplorer: Uninstalled: "+ex.getMessage()); //NOI18N
        }
    }
    
    public boolean closing() {
        org.netbeans.modules.db.explorer.nodes.RootNode.getOption().save();
        return true;
    }
    
    public void close () {
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("UI/Runtime"); //NOI18N
        final DataFolder df;
        try {
            df = (DataFolder) DataObject.find(fo);
        } catch (DataObjectNotFoundException exc) {
            return;
        }
        // closing all open connection
        Children.MUTEX.writeAccess (new Runnable () {
            public void run () {
                try {
                    Node environment = df.getNodeDelegate();
                    Node[] n = environment.getChildren().findChild("Databases").getChildren().getNodes(); //NOI18N
                    ConnectionNodeInfo cni;
                    for (int i = 0; i < n.length; i++) {
                        cni = (ConnectionNodeInfo) n[i].getCookie(ConnectionNodeInfo.class);
                        if (cni != null)
                            cni.disconnect();
                    }
                } catch (Exception exc) {
                    //connection not closed
                }
            }
        });
    }

}
