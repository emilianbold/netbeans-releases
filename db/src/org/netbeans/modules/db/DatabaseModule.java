/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.modules.*;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.netbeans.modules.db.explorer.nodes.ConnectionNode;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;

import org.openide.nodes.*;

//import org.openide.util.Mutex;

public class DatabaseModule extends ModuleInstall {
    private ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N

    static final long serialVersionUID =5426465356344170725L;
    
    public void installed() {
        TopManager tm = TopManager.getDefault();

        try {
            FileSystem rfs = tm.getRepository().getDefaultFileSystem();
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
            if (tm != null)
                tm.notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
        } catch (Exception ex) {
            String msg = MessageFormat.format(bundle.getString("FMT_EXCEPTIONINSTALL"), new String[] {ex.getMessage()}); //NOI18N
            if (tm != null)
                tm.notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
    
    public void uninstalled() {
        TopManager tm = TopManager.getDefault();

        try {
            FileSystem rfs = tm.getRepository().getDefaultFileSystem();
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
    
    public void close () {
        // closing all open connection
        Children.MUTEX.writeAccess (new Runnable () {
            public void run () {
                try {
                    Node n[] = TopManager.getDefault().getPlaces().nodes().environment().getChildren().findChild("Databases").getChildren().getNodes(); //NOI18N
                    for (int i = 0; i < n.length; i++)
                        if (n[i] instanceof ConnectionNode)
                            ((ConnectionNodeInfo)((ConnectionNode)n[i]).getInfo()).disconnect();
                } catch (Exception exc) {
                    //connection not closed
                }
            }
        });
    }

}
