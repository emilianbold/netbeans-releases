/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.db.explorer.infos;

import java.util.*;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

import org.netbeans.lib.ddl.*;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DbActionLoaderSupport;
import org.netbeans.modules.db.explorer.DbNodeLoader;
import org.netbeans.modules.db.explorer.DbNodeLoaderSupport;
import org.netbeans.modules.db.explorer.nodes.*;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

public class RootNodeInfo extends DatabaseNodeInfo implements 
        ConnectionOwnerOperations, ChangeListener  {
    static final long serialVersionUID =-8079386805046070315L;
    
    static RootNodeInfo rootInfo = null;
    
    private Collection<DbNodeLoader> nodeLoaders;
    
    public static RootNodeInfo getInstance() throws DatabaseException {
        if (rootInfo == null) {
            rootInfo = (RootNodeInfo) DatabaseNodeInfo.createNodeInfo(null, "root"); //NOI18N
        }
        return rootInfo;
    }  
    public void initChildren(Vector children) throws DatabaseException {
        try {
            children.addAll(getRegisteredNodes());
            
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
            throw new DatabaseException(e);
        }
    }

    private List<Node> getRegisteredNodes() {
        boolean registerListener = false;
        if ( nodeLoaders == null ) {
            nodeLoaders = DbNodeLoaderSupport.getLoaders();
            registerListener = true;
        }
        
        ArrayList<Node> nodes = new ArrayList<Node>();
        
        for ( DbNodeLoader loader : nodeLoaders ) {
            if ( registerListener ) {
                loader.addChangeListener(this);
            }
            nodes.addAll(loader.getAllNodes());
        }    
        
        return nodes;
    }

    @Override
    @SuppressWarnings("checked")
    public Vector getActions() {
        Vector<Action> actions = super.getActions();
        
        List<Action> loadedActions = DbActionLoaderSupport.getAllActions();
        
        Vector<Action> allActions = new Vector<Action>();
        
        
        // TODO - it would be nice to enable ordering of actions, but this
        // is going to require some thought.  For now, put the actions in
        // just before the divider
        for ( Action action : actions ) {
            if ( action == null ) {
                allActions.addAll(loadedActions);
            }
            
            allActions.add(action);
        }
        
        return allActions;
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
    
    private void updateRegisteredNodes() throws DatabaseException {
        getChildren();
        
        final DatabaseNodeChildren children = 
                (DatabaseNodeChildren)getNode().getChildren();
        
        Node[] nodes = children.getNodes();
        
        final List<Node> newNodes = getRegisteredNodes();
        
        // Now add the non-registered nodes that currently exist
        for (Node node : nodes ) {
            if ( node instanceof DatabaseNode ) {
                newNodes.add(node);
            }
        }
        
        postUpdateChildren(children, newNodes.toArray(new Node[0]));

        fireRefresh();        
    }
    
    private void postUpdateChildren(final DatabaseNodeChildren children, 
            final Node[] newNodes) {                
        // Replace the node list with the new one
        Children.MUTEX.postWriteRequest(new Runnable() {
            public void run() {
                // remove current sub-tree
                children.remove(children.getNodes());

                // add built sub-tree
                children.add(newNodes);
            }
        });
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
    
    public void removeConnection(DatabaseConnection dbconn) throws DatabaseException {
        if ( dbconn == null ) {
            throw new NullPointerException();
        }
        
        ConnectionList.getDefault().remove(dbconn);
        
        DatabaseNode node = getNode();
        DatabaseNodeChildren children = (DatabaseNodeChildren)node.getChildren();
        Node[] nodes = children.getNodes();
        
        for ( Node childNode : nodes ) {
            if ( childNode instanceof ConnectionNode ) {
                ConnectionNodeInfo connInfo = (ConnectionNodeInfo)
                        ((ConnectionNode)childNode).getInfo();
                if ( ! connInfo.getDatabaseConnection().equals(dbconn)) {
                    children.removeSubNode(childNode);
                }
            }
        }
    }
    
    public void addConnection(DBConnection cinfo) throws DatabaseException {
        DatabaseConnection dbconn = (DatabaseConnection)cinfo;
        getChildren(); // force restore

        if (ConnectionList.getDefault().contains(dbconn)) {
            throw new DatabaseException(bundle().getString("EXC_ConnectionAlreadyExists"));
        }

        DatabaseNode node = getNode();
        DatabaseNodeChildren children = (DatabaseNodeChildren) node.getChildren();
        
        // the nodes have to be initialized too, otherwise the node created 
        // for the new connection will not be added and the connection
        // will be lost when the nodes are eventually initialized
        children.getNodes(true); 
        
        ConnectionNodeInfo ninfo = createConnectionNodeInfo(dbconn);
        ConnectionList.getDefault().add(dbconn);
        DatabaseNode cnode = children.createSubnode(ninfo, true);
        
        if (((DatabaseConnection) dbconn).getConnection() == null)
            ((ConnectionNodeInfo) cnode.getInfo()).connect();
        else
            ((ConnectionNodeInfo) cnode.getInfo()).connect(dbconn);
    }

    public void stateChanged(ChangeEvent evt) {
        // One of the node loader's underlying nodes have changed, so let's
        // do a refresh of our nodes
        try {
            updateRegisteredNodes();
        } catch ( DatabaseException dbe ) {
            Exceptions.printStackTrace(dbe);
        }
    }

}
