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

public class RootNodeInfo extends DatabaseNodeInfo
            implements ConnectionOwnerOperations
{
    static final long serialVersionUID =-8079386805046070315L;
    public void initChildren(Vector children)
    throws DatabaseException
    {
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
            FileObject databaseFileObject = rootFolder.getFileObject("Database");
            if (databaseFileObject != null) {
                FileObject adaptorsFolder = databaseFileObject.getFileObject("Adaptors");
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
        if (cons.contains(cinfo)) throw new DatabaseException("connection already exists");
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
            if (usr != null && usr.length() > 0 && pwd != null && pwd.length() > 0) {
                ((ConnectionNodeInfo)cnode.getInfo()).connect();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DatabaseException(e.getMessage());
        }
    }
}
/*
 * <<Log>>
 *  11   Gandalf   1.10        11/27/99 Patrik Knakal   
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         9/27/99  Slavek Psenicka Adding Database/Adaptors
 *       node under Databases
 *  8    Gandalf   1.7         9/13/99  Slavek Psenicka 
 *  7    Gandalf   1.6         7/21/99  Slavek Psenicka don't remember password
 *  6    Gandalf   1.5         6/15/99  Slavek Psenicka force restore 
 *       connections before first add, avoid duplicating connection being added
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         5/21/99  Slavek Psenicka new version
 *  3    Gandalf   1.2         5/14/99  Slavek Psenicka new version
 *  2    Gandalf   1.1         4/23/99  Slavek Psenicka Debug mode
 *  1    Gandalf   1.0         4/23/99  Slavek Psenicka 
 * $
 */
