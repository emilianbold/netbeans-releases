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

package org.netbeans.modules.db.explorer.infos;

import java.util.*;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

import org.netbeans.lib.ddl.*;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.nodes.*;

public class RootNodeInfo extends DatabaseNodeInfo implements ConnectionOwnerOperations {
    static final long serialVersionUID =-8079386805046070315L;
    
    static RootNodeInfo rootInfo = null;
    public static RootNodeInfo getInstance() throws DatabaseException {
        if (rootInfo == null) {
            rootInfo = (RootNodeInfo) DatabaseNodeInfo.createNodeInfo(null, "root"); //NOI18N
        }
        return rootInfo;
    }
    public void initChildren(Vector children) throws DatabaseException {
        try {
            DatabaseConnection[] cinfos = ConnectionList.getDefault().getConnections();
            for (int i = 0; i < cinfos.length; i++) {
                DatabaseConnection cinfo = cinfos[i];
                ConnectionNodeInfo ninfo = createConnectionNodeInfo(cinfo);
                children.add(ninfo);
            }

            Repository r = Repository.getDefault();
            FileSystem rfs = r.getDefaultFileSystem();
            FileObject rootFolder = rfs.getRoot();
            FileObject databaseFileObject = rootFolder.getFileObject("Database"); //NOI18N
            if (databaseFileObject != null) {
                FileObject adaptorsFolder = databaseFileObject.getFileObject("Adaptors"); //NOI18N
                DataObject dbdo = DataFolder.findFolder(adaptorsFolder);
                if (dbdo != null)
                    children.add(dbdo.getNodeDelegate());
            }

        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    private ConnectionNodeInfo createConnectionNodeInfo(DatabaseConnection dbconn) throws DatabaseException {
        ConnectionNodeInfo ninfo = (ConnectionNodeInfo)createNodeInfo(this, DatabaseNode.CONNECTION);
        ninfo.setUser(dbconn.getUser());
        ninfo.setDatabase(dbconn.getDatabase());
        ninfo.setSchema(dbconn.getSchema());
        ninfo.setName(dbconn.getName());
        ninfo.setDatabaseConnection(dbconn);
        return ninfo;
    }

    public void refreshChildren() throws DatabaseException {
        // refresh action is empty
    }

    public void addConnectionNoConnect(DatabaseConnection dbconn) throws DatabaseException {
        getChildren(); // force restore
        
        if (ConnectionList.getDefault().contains(dbconn)) {
            return;
        }

        DatabaseNode node = getNode();
        DatabaseNodeChildren children = (DatabaseNodeChildren) node.getChildren();
        ConnectionNodeInfo ninfo = createConnectionNodeInfo(dbconn);
        ConnectionList.getDefault().add(dbconn);
        children.createSubnode(ninfo, true);
    }
    
    public void addConnection(DBConnection cinfo) throws DatabaseException {
        DatabaseConnection dbconn = (DatabaseConnection)cinfo;
        getChildren(); // force restore

        if (ConnectionList.getDefault().contains(dbconn)) {
            throw new DatabaseException(bundle().getString("EXC_ConnectionAlreadyExists"));
        }

        DatabaseNode node = getNode();
        DatabaseNodeChildren children = (DatabaseNodeChildren) node.getChildren();
        ConnectionNodeInfo ninfo = createConnectionNodeInfo(dbconn);
        ConnectionList.getDefault().add(dbconn);
        DatabaseNode cnode = children.createSubnode(ninfo, true);
        
        if (((DatabaseConnection) dbconn).getConnection() == null)
            ((ConnectionNodeInfo) cnode.getInfo()).connect();
        else
            ((ConnectionNodeInfo) cnode.getInfo()).connect(dbconn);
    }
}
