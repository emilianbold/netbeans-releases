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

package org.netbeans.modules.db.explorer.infos;

import java.util.*;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceDataObject;
import org.openide.modules.*;
import org.openide.nodes.*;
import org.openide.options.SystemOption;
import org.openide.TopManager;

import org.netbeans.lib.ddl.*;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.netbeans.modules.db.explorer.actions.DatabaseAction;

public class RootNodeInfo extends DatabaseNodeInfo implements ConnectionOwnerOperations {
    static final long serialVersionUID =-8079386805046070315L;
    
    public void initChildren(Vector children) throws DatabaseException {
        try {
            Vector cons = RootNode.getOption().getConnections();
            if (cons != null) {
                Enumeration en = cons.elements();
                while(en.hasMoreElements()) {
                    DBConnection cinfo = (DBConnection)en.nextElement();
                    ConnectionNodeInfo ninfo = (ConnectionNodeInfo)createNodeInfo(this, DatabaseNode.CONNECTION);
                    ninfo.setUser(cinfo.getUser());
                    ninfo.setDatabase(cinfo.getDatabase());
                    ninfo.setDatabaseConnection(cinfo);
                    children.add(ninfo);
                }
            }

            TopManager tm = TopManager.getDefault();
            FileSystem rfs = tm.getRepository().getDefaultFileSystem();
            FileObject rootFolder = rfs.getRoot();
            FileObject databaseFileObject = rootFolder.getFileObject("Database"); //NOI18N
            if (databaseFileObject != null) {
                FileObject adaptorsFolder = databaseFileObject.getFileObject("Adaptors"); //NOI18N
                DataObject dbdo = DataFolder.findFolder(adaptorsFolder);
                if (dbdo != null) children.add(dbdo.getNodeDelegate());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException(e.getMessage());
        }
    }

    public void addConnection(DBConnection cinfo)
    throws DatabaseException
    {
        getChildren(); // force restore
        Vector cons = RootNode.getOption().getConnections();
        String usr = cinfo.getUser();
        String pwd = cinfo.getPassword();
        
        if (cons.contains(cinfo))
            throw new DatabaseException(bundle.getString("EXC_ConnectionAlreadyExists"));
        
        try {
            DatabaseNode node = getNode();
            DatabaseNodeChildren children = (DatabaseNodeChildren)node.getChildren();
            ConnectionNodeInfo ninfo = (ConnectionNodeInfo)createNodeInfo(this, DatabaseNode.CONNECTION);
            ninfo.setName(cinfo.getDatabase());
            ninfo.setUser(cinfo.getUser());
            ninfo.setDatabase(cinfo.getDatabase());
            ninfo.setDatabaseConnection(cinfo);
            cons.add(cinfo);
            DatabaseNode cnode = children.createSubnode(ninfo, true);
            ((ConnectionNodeInfo)cnode.getInfo()).connect();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException(e.getMessage());
        }
    }
}
