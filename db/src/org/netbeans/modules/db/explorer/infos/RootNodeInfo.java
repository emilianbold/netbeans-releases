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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

import org.netbeans.lib.ddl.*;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.lib.ddl.impl.SpecificationFactory;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DatabaseOption;
import org.netbeans.modules.db.explorer.DbActionLoaderSupport;
import org.netbeans.modules.db.explorer.DbNodeLoader;
import org.netbeans.modules.db.explorer.DbNodeLoaderSupport;
import org.netbeans.modules.db.explorer.nodes.*;
import org.openide.nodes.Node;
import org.openide.options.SystemOption;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class RootNodeInfo extends DatabaseNodeInfo implements 
        ConnectionOwnerOperations, ChangeListener  {
    static final long serialVersionUID =-8079386805046070315L;
    
    static RootNodeInfo rootInfo = null;
    
    private static DatabaseOption option = null;
    
    private Collection<DbNodeLoader> nodeLoaders;
    
    private static Logger LOGGER = 
            Logger.getLogger(RootNodeInfo.class.getName());
    
    public static RootNodeInfo getInstance() throws DatabaseException {
        if (rootInfo == null) {
            rootInfo = (RootNodeInfo) DatabaseNodeInfo.createNodeInfo(null, "root"); //NOI18N
        }
        return rootInfo;
    }  
    
    public RootNodeInfo() {  
        try {
            SpecificationFactory sfactory = new SpecificationFactory();
            if ( sfactory == null ) {
                throw new Exception(
                        bundle().getString("EXC_NoSpecificationFactory"));
            }
            
            setSpecificationFactory(new SpecificationFactory());
            
            ConnectionList.getDefault().addConnectionListener(new ConnectionListener() {
                public void connectionsChanged() {
                    stateChanged(new ChangeEvent(this));
                }
            });
            
            //initialization listener for debug mode
            initDebugListening();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }

    }
    
        /**
     * Connects the debug property in sfactory and debugMode property in DBExplorer module's option.
     */
    private void initDebugListening() {
        final DatabaseSpecificationFactory sfactory = getSpecificationFactory();
        
        if ( option == null || sfactory == null ) {
            return;
        }
        
        option.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName() == null) {
                    sfactory.setDebugMode(option.getDebugMode());
                    return;
                }
                if (e.getPropertyName().equals(DatabaseOption.PROP_DEBUG_MODE))
                    sfactory.setDebugMode(((Boolean) e.getNewValue()).booleanValue());
            }
        });
        sfactory.setDebugMode(option.getDebugMode());
    }
    
    public static synchronized DatabaseOption getOption() {
        if (option == null)
            option = (DatabaseOption)SystemOption.findObject(DatabaseOption.class, true);

        return option;
    }



    public void initChildren(Vector children) throws DatabaseException {
        try {
            children.addAll(getRegisteredNodeInfos());
            
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

    private List<RegisteredNodeInfo> getRegisteredNodeInfos() {
        boolean registerListener = false;
        if ( nodeLoaders == null ) {
            nodeLoaders = DbNodeLoaderSupport.getLoaders();
            registerListener = true;
        }
        
        ArrayList<RegisteredNodeInfo> infos = new ArrayList<RegisteredNodeInfo>();
                
        for ( DbNodeLoader loader : nodeLoaders ) {
            if ( registerListener ) {
                loader.addChangeListener(this);
            }
            for ( Node node: loader.getAllNodes() ) {
                infos.add(new RegisteredNodeInfo(this, node));
            }
        }    
        
        return infos;
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
        if (dbconn.getConnection() != null) {
            ninfo.connect(dbconn);
        }
        return ninfo;
    }
        
    public void addConnection(DBConnection con) throws DatabaseException {
        DatabaseConnection dbconn = (DatabaseConnection)con;
        getChildren(); // force restore

        if (ConnectionList.getDefault().contains(dbconn)) {
            throw new DatabaseException(bundle().getString("EXC_ConnectionAlreadyExists"));
        }
        
        ConnectionList.getDefault().add(dbconn);
        refreshChildren();
    }
        
    public void removeConnection(DatabaseConnection dbconn) throws DatabaseException {
        if ( dbconn == null ) {
            throw new NullPointerException();
        }
        
        Vector<DatabaseNodeInfo> children = getChildren();
        DatabaseNodeInfo toRemove = null;
        
        for ( DatabaseNodeInfo child : children ) {
            if ( child instanceof ConnectionNodeInfo ) {
                ConnectionNodeInfo ninfo = (ConnectionNodeInfo)child;
                if ( ninfo.getDatabaseConnection().equals(dbconn)) {
                    toRemove = ninfo;
                }
                
                dbconn.disconnect();
            }
        }
        
        if ( toRemove != null ) {
            removeChild(toRemove, false);
        }
        
        ConnectionList.getDefault().remove(dbconn);
        
        notifyChange();
    }
    
    public void stateChanged(ChangeEvent evt) {
        // One of the node loader's underlying nodes have changed, so let's
        // do a refresh of our nodes
        try {
            refreshChildren();
        } catch ( DatabaseException dbe ) {
            Exceptions.printStackTrace(dbe);
        }
    } 
    
    @Override
    public void refreshChildren() throws DatabaseException {
        super.refreshChildren();
        
        // Now re-add the driver node
        addChild(createNodeInfo(this, DatabaseNode.DRIVER_LIST));
    }

    
    @Override
    public String getDisplayName() {
         return bundle().getString("NDN_Databases"); //NOI18N
    }
    
    @Override
    public String getShortDescription() {
        return bundle().getString("ND_Root"); //NOI18N
    }


}
