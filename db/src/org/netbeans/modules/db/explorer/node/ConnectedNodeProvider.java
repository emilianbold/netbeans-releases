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

package org.netbeans.modules.db.explorer.node;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.SQLException;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.openide.util.Lookup;

/**
 * ConnectedNodeprovider serves as a base class for all node providers
 * that work with a database connection.
 *
 * @author Rob Englander
 */
public abstract class ConnectedNodeProvider  extends NodeProvider {

    private final DatabaseConnection connection;

    protected ConnectedNodeProvider(Lookup lookup) {
        super(lookup);
        connection = getLookup().lookup(DatabaseConnection.class);
    }

    /**
     * Create a BaseNode instance.
     *
     * @param lookup the lookup to use to create the node
     * @return the created baseNode
     */
    protected abstract BaseNode createNode(NodeDataLookup lookup);
    
    protected void setup() {
        connection.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals("connected") ||
                            evt.getPropertyName().equals("failed")) {
                        updateState();
                    }
                }
            }
        );

        updateState();
    }
    
    private void updateState() {
        Connection conn = connection.getConnection();
        boolean disconnected = true;

        if (conn != null) {
            try {
                disconnected = conn.isClosed();
            } catch (SQLException e) {

            }
        }

        if (disconnected) {
            removeAllNodes();
        } else {
            removeAllNodes();
            NodeDataLookup lookup = new NodeDataLookup();
            lookup.add(connection);

            Metadata metaData = getLookup().lookup(Metadata.class);
            if (metaData != null) {
                lookup.add(metaData);
            }

            MetadataElementHandle<Schema> schemaHandle = getLookup().lookup(MetadataElementHandle.class);
            if (schemaHandle != null) {
                lookup.add(schemaHandle);
            }
            
            addNode(createNode(lookup));
        }
    }
}
