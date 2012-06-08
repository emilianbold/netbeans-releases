/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.db.explorer.node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.api.db.explorer.node.NodeProviderFactory;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * A node provider that provides ConnectionNode instances.
 * 
 * @author Rob Englander
 */
public class ConnectionNodeProvider extends NodeProvider {
    
    // lazy initialization holder class idiom for static fields is used
    // for retrieving the factory
    public static NodeProviderFactory getFactory() {
        return FactoryHolder.FACTORY;
    }

    private static class FactoryHolder {
        static final NodeProviderFactory FACTORY = new NodeProviderFactory() {
            public ConnectionNodeProvider createInstance(Lookup lookup) {
                ConnectionNodeProvider provider = new ConnectionNodeProvider(lookup);
                provider.setup();
                return provider;
            }
        };
    }
    
    private final ConnectionList connectionList;
    
    private ConnectionNodeProvider(Lookup lookup) {
        super(lookup, new ConnectionComparator());
        connectionList = getLookup().lookup(ConnectionList.class);
    }
    
    private void setup() {
        connectionList.addConnectionListener(
            new ConnectionListener() {
                public void connectionsChanged() {
                    initialize();
                }
            }
        );
    }

    protected synchronized void initialize() {
        List<Node> newList = new ArrayList<Node>();
        DatabaseConnection newConnection = null;
        DatabaseConnection[] connections = connectionList.getConnections();
        for (DatabaseConnection connection : connections) {
            Collection<Node> matches = getNodes(connection);
            if (matches.size() > 0) {
                newList.addAll(matches);
            } else {
                NodeDataLookup lookup = new NodeDataLookup();
                lookup.add(connection);
                newConnection = connection;
                newList.add(ConnectionNode.create(lookup, this));
            }
        }

        setNodes(newList);
        // select added connection in explorer
        final DatabaseConnection newConnectionFinal = newConnection;
        if (newConnection != null) {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        newConnectionFinal.selectInExplorer(false);
                    }
                });
            } else {
                newConnectionFinal.selectInExplorer(false);
            }
        }
    }

    static class ConnectionComparator implements Comparator<Node> {

        public int compare(Node model1, Node model2) {
            return model1.getDisplayName().compareToIgnoreCase(model2.getDisplayName());
        }
        
    }
}
