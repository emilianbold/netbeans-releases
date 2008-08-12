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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.mysql.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.modules.db.api.explorer.NodeProvider;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.DatabaseServerManager;
import org.netbeans.modules.db.mysql.nodes.ServerNode;
import org.netbeans.modules.db.mysql.util.DatabaseUtils;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.nodes.Node;

/**
 * Provides a node for working with a local MySQL Server instance
 * 
 * @author David Van Couvering
 */
public class ServerNodeProvider implements NodeProvider {

    private static volatile ServerNodeProvider DEFAULT;
    private static final MySQLOptions options = MySQLOptions.getDefault();
    private final ArrayList<Node> nodes = new ArrayList<Node>();
    private static final ArrayList<Node> emptyNodeList = new ArrayList<Node>();
    private final CopyOnWriteArrayList<ChangeListener> listeners = 
            new CopyOnWriteArrayList<ChangeListener>();
    
    public static synchronized ServerNodeProvider getDefault() {
        // Issue 134577 - getDefault() is called by Lookup. If we try to
        // get the DatabaseServer implementation here, we cause
        // deadlocks, as this lookup will wait until the lookup calling
        // getDefault() completes, which will never happen.
        //
        // So we lazily look up the DatabaseServer as part of the call
        // to getNodes(), which happens from application code and *not*
        // as part of lookup.
        if ( DEFAULT == null ) {
                DEFAULT = new ServerNodeProvider();
        }
        return DEFAULT;
    }
    
    private ServerNodeProvider() {
        findAndRegisterMySQL();
    }

    /**
     * Try to find MySQL on the local machine, and if it can be found,
     * register a connection and the MySQL server node in the Database
     * Explorer.
     */
    private void findAndRegisterMySQL() {
        if ( (DatabaseUtils.getJDBCDriver()) == null ) {
            // Driver not registered, that's OK, the user may
            // have deleted it, but nothing to do here.
            return;
        }

        registerConnectionListener();
        findAndRegisterInstallation();
    }

    private void registerConnectionListener() {
        // Register a listener that will auto-register the MySQL
        // server provider when a user adds a MySQL connection
        ConnectionManager.getDefault().addConnectionListener(
                new DbExplorerConnectionListener());
    }

    private void findAndRegisterInstallation() {
        Installation installation = InstallationManager.detectInstallation();
        if ( installation == null ) {
            return;
        }

        String[] command = installation.getAdminCommand();
        if ( Utils.isValidExecutable(command[0], true /*emptyOK*/) ||
             Utils.isValidURL(command[0], true /*emptyOK*/ )) {
            options.setAdminPath(command[0]);
            options.setAdminArgs(command[1]);
        }

        command = installation.getStartCommand();
        if ( Utils.isValidExecutable(command[0], true)) {
            options.setStartPath(command[0]);
            options.setStartArgs(command[1]);
        }

        command = installation.getStopCommand();
        if ( Utils.isValidExecutable(command[0], true)) {
            options.setStopPath(command[0]);
            options.setStopArgs(command[1]);
        }

        options.setPort(installation.getDefaultPort());

        setRegistered(true);
    }
    
    public List<Node> getNodes() {
        checkNodeArray();
        
        if ( options.isProviderRegistered() ) {
            return nodes;
        } else {
            DatabaseServerManager.getDatabaseServer().disconnect();
            return emptyNodeList;
        }
    }

    /**
     * Check to see if the nodes list has been initialized, and if not,
     * initialize it.
     */
    private void checkNodeArray() {
        if ( nodes.size() == 0 ) {
            Node node = ServerNode.create(DatabaseServerManager.getDatabaseServer());
            
            synchronized(this) {
                if ( nodes.size() == 0 ) {
                    nodes.add(node);
                }
            }
        }        
    }
    public void setRegistered(boolean registered) {
        boolean old = isRegistered();
        if ( registered != old ) {
            DatabaseServer instance = DatabaseServerManager.getDatabaseServer();
            options.setProviderRegistered(registered);
            
            if ( ! registered ) {
                instance.disconnect();
            } else {
                instance.reconnect(true, true ); // quiet, async
            }
            notifyChange();
        }
    }
    
    public synchronized boolean isRegistered() {
        return options.isProviderRegistered();
    }
    
    void notifyChange() {
        ChangeEvent evt = new ChangeEvent(this);
        for ( ChangeListener listener : listeners ) {
            listener.stateChanged(evt);
        }
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }
}
