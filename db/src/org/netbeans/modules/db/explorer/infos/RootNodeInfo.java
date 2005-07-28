/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.infos;

import java.util.*;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.*;
import org.openide.nodes.*;

import org.netbeans.lib.ddl.*;
import org.netbeans.modules.db.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.infos.*;
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
            Vector cons = RootNode.getOption().getConnections();
            if (cons != null) {
                Enumeration en = cons.elements();
                while(en.hasMoreElements()) {
                    DBConnection cinfo = (DBConnection)en.nextElement();
                    ConnectionNodeInfo ninfo = createConnectionNodeInfo(cinfo);
                    children.add(ninfo);
                }
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

    private ConnectionNodeInfo createConnectionNodeInfo(DBConnection cinfo) throws DatabaseException {
        ConnectionNodeInfo ninfo = (ConnectionNodeInfo)createNodeInfo(this, DatabaseNode.CONNECTION);
        ninfo.setUser(cinfo.getUser());
        ninfo.setDatabase(cinfo.getDatabase());
        ninfo.setSchema(cinfo.getSchema());
        ninfo.setName(cinfo.getName());
        ninfo.setDatabaseConnection(cinfo);
        return ninfo;
    }

    public void refreshChildren() throws DatabaseException {
        // refresh action is empty
    }

    public void addDatabaseConnection(DatabaseConnection cinfo) {
        try {
            getChildren(); // force restore
            Vector cons = RootNode.getOption().getConnections();
            if (cons.contains(cinfo))
                return;

            DatabaseNode node = getNode();
            DatabaseNodeChildren children = (DatabaseNodeChildren) node.getChildren();
            ConnectionNodeInfo ninfo = createConnectionNodeInfo(cinfo);
            cons.add(cinfo);
            children.createSubnode(ninfo, true);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            //throw new DatabaseException(e.getMessage());
        }
    }
    
    public void addConnection(DBConnection cinfo) throws DatabaseException {
        getChildren(); // force restore
        Vector cons = RootNode.getOption().getConnections();

        if (cons.contains(cinfo))
            throw new DatabaseException(bundle().getString("EXC_ConnectionAlreadyExists"));

        DatabaseNode node = getNode();
        DatabaseNodeChildren children = (DatabaseNodeChildren) node.getChildren();
        ConnectionNodeInfo ninfo = (ConnectionNodeInfo) createNodeInfo(this, DatabaseNode.CONNECTION);
        ninfo.setName(cinfo.getName());
        ninfo.setUser(cinfo.getUser());
        ninfo.setDatabase(cinfo.getDatabase());
        ninfo.setSchema(cinfo.getSchema());
        ninfo.setDatabaseConnection(cinfo);
        cons.add(cinfo);
        DatabaseNode cnode = children.createSubnode(ninfo, true);
        if (((DatabaseConnection) cinfo).getConnection() == null)
            ((ConnectionNodeInfo) cnode.getInfo()).connect();
        else
            ((ConnectionNodeInfo) cnode.getInfo()).connect(cinfo);
    }

    public void addOrConnectConnection(DBConnection conn) throws DatabaseException {
        getChildren(); // force restore
        Vector cons = RootNode.getOption().getConnections();

        if (cons.contains(conn)) {
            ConnectionNode connNode = (ConnectionNode) getNode().getChildren().findChild( conn.getName() );
            if (connNode!=null)
                if (connNode.getInfo().getConnection()==null) {
                    connNode.getInfo().setDatabaseConnection(conn);
                    ((ConnectionNodeInfo) connNode.getInfo()).connect();
                }
        } else
            addConnection(conn);
    }

    public void addOrSetConnection(DBConnection conn) throws DatabaseException {
        getChildren(); // force restore
        Vector cons = RootNode.getOption().getConnections();

        if (cons.contains(conn)) {
            ConnectionNode connNode = (ConnectionNode) getNode().getChildren().findChild(conn.getName());
            if (connNode != null)
                if (connNode.getInfo().getConnection() == null) {
                    connNode.getInfo().setDatabaseConnection(conn);

                    // connection DON'T be connected!!! as designed
                    //((ConnectionNodeInfo)connNode.getInfo()).connect();
                }
        } else {
            DatabaseNode node = getNode();
            DatabaseNodeChildren children = (DatabaseNodeChildren) node.getChildren();
            ConnectionNodeInfo ninfo = (ConnectionNodeInfo) createNodeInfo(this, DatabaseNode.CONNECTION);

            // set the connection properties of ConnectionNodeInfo
            ninfo.setDatabaseConnection(conn);
            // set schema(schema is not required then is not set in setDatabaseConnection() method)
            ninfo.setSchema(conn.getSchema());

            cons.add(conn);
            DatabaseNode cnode = children.createSubnode(ninfo, true);
        }
    }
}
