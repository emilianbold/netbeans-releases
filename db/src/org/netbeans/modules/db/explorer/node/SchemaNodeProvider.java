/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.api.db.explorer.node.NodeProviderFactory;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.metadata.MetadataModelManager;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Rob Englander
 */
public class SchemaNodeProvider extends NodeProvider {

    // lazy initialization holder class idiom for static fields is used
    // for retrieving the factory
    public static NodeProviderFactory getFactory() {
        return FactoryHolder.FACTORY;
    }

    private static class FactoryHolder {
        static final NodeProviderFactory FACTORY = new NodeProviderFactory() {
            public SchemaNodeProvider createInstance(Lookup lookup) {
                SchemaNodeProvider provider = new SchemaNodeProvider(lookup);
                provider.setup();
                return provider;
            }
        };
    }

    private final DatabaseConnection connection;
    private final Catalog catalog;

    private SchemaNodeProvider(Lookup lookup) {
        super(lookup, new SchemaComparator());
        connection = getLookup().lookup(DatabaseConnection.class);
        catalog = getLookup().lookup(Catalog.class);
    }

    private void setup() {
        // listen for change events
        connection.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    RequestProcessor.getDefault().post(
                        new Runnable() {
                            public void run() {
                                // just ask the node to update itself
                                update();
                            }
                        }
                    );
                }
            }
        );

        update();
    }

    private synchronized void update() {
        Connection conn = connection.getConnection();
        boolean connected = false;

        if (conn != null) {
            try {
                connected = !conn.isClosed();
            } catch (SQLException e) {

            }
        }

        if (connected) {
            //MetadataModel model = MetadataModels.createModel(connection.getConnection(), null);
            MetadataModel model = MetadataModelManager.get(connection.getDatabaseConnection());

            try {
                model.runReadAction(
                    new Action<Metadata>() {
                        public void run(Metadata parameter) {
                            List<Node> newList = new ArrayList<Node>();

                            Catalog cat = parameter.getDefaultCatalog();
                            Schema syntheticSchema = cat.getSyntheticSchema();

                            if (syntheticSchema != null) {
                                updateNode(newList, syntheticSchema, parameter);
                            } else {
                                Collection<Schema> schemas = cat.getSchemas();
                                for (Schema schema : schemas) {
                                    updateNode(newList, schema, parameter);
                                }
                            }

                            setNodes(newList);
                        }

                        private void updateNode(List<Node> newList, Schema schema, Metadata metadata) {
                            Collection<Node> matches = SchemaNodeProvider.this.getNodes(schema);
                            if (matches.size() > 0) {
                                newList.addAll(matches);
                            } else {
                                NodeDataLookup lookup = new NodeDataLookup();
                                lookup.add(connection);

                                MetadataElementHandle<Schema> schemaHandle = MetadataElementHandle.create(schema);
                                lookup.add(schemaHandle);
                                lookup.add(metadata);

                                newList.add(SchemaNode.create(lookup, SchemaNodeProvider.this));
                            }
                        }
                    }
                );
            } catch (MetadataModelException e) {
            }
        } else {
            removeAllNodes();
        }
    }

    static class SchemaComparator implements Comparator<Node> {

        public int compare(Node node1, Node node2) {
            return node1.getDisplayName().compareToIgnoreCase(node2.getDisplayName());
        }

    }
}
