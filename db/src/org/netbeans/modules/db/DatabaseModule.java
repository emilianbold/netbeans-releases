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

package org.netbeans.modules.db;

import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.ModuleInstall;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

public class DatabaseModule extends ModuleInstall {
        
    public void close () {
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("UI/Runtime"); //NOI18N
        final DataFolder df;
        try {
            df = (DataFolder) DataObject.find(fo);
        } catch (DataObjectNotFoundException exc) {
            return;
        }
        //close all opened connection
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
