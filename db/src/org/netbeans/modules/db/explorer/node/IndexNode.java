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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.ChildNodeFactory;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.explorer.action.RefreshAction;
import org.netbeans.modules.db.explorer.infos.DDLHelper;
import org.netbeans.modules.db.explorer.metadata.MetadataReader;
import org.netbeans.modules.db.explorer.metadata.MetadataReader.DataWrapper;
import org.netbeans.modules.db.explorer.metadata.MetadataReader.MetadataReadListener;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Index;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.Schema;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Rob Englander
 */
public class IndexNode extends BaseNode {
    private static final Logger LOGGER = Logger.getLogger(IndexNode.class.getName());
    private static final String ICONBASE = "org/netbeans/modules/db/resources/index.gif";
    private static final String FOLDER = "Index"; //NOI18N

    /**
     * Create an instance of IndexNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the IndexNode instance
     */
    public static IndexNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        IndexNode node = new IndexNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private String name = ""; // NOI18N
    private MetadataElementHandle<Index> indexHandle;
    private final DatabaseConnection connection;

    private IndexNode(NodeDataLookup lookup, NodeProvider provider) {
        super(new ChildNodeFactory(lookup), lookup, FOLDER, provider);
        connection = getLookup().lookup(DatabaseConnection.class);
    }

    protected void initialize() {
        indexHandle = getLookup().lookup(MetadataElementHandle.class);

        boolean connected = !connection.getConnector().isDisconnected();
        MetadataModel metaDataModel = connection.getMetadataModel();
        if (connected && metaDataModel != null) {
            Index index = getIndex();
            name = index.getName();
        }
    }

    public Index getIndex() {
        MetadataModel metaDataModel = connection.getMetadataModel();
        DataWrapper<Index> wrapper = new DataWrapper<Index>();
        MetadataReader.readModel(metaDataModel, wrapper,
            new MetadataReadListener() {
                public void run(Metadata metaData, DataWrapper wrapper) {
                    Index index = indexHandle.resolve(metaData);
                    wrapper.setObject(index);
                }
            }
        );

        return wrapper.getObject();
    }

    public Schema getSchema() {
        Index index = getIndex();
        return (Schema)index.getParent().getParent();
    }

    public Table getTable() {
        Index index = getIndex();
        return (Table)index.getParent();
    }

    @Override
    public void destroy() {
        DatabaseConnector connector = connection.getConnector();
        Table table = getTable();
        final String tablename = table.getName();

        Schema schema = table.getParent();
        Catalog catalog = schema.getParent();

        String schemaName = schema.getName();
        String catalogName = catalog.getName();

        if (schemaName == null) {
            schemaName = catalog.getName();
        } else if (catalogName == null) {
            catalogName = schemaName;
        }

        try {
            Specification spec = connector.getDatabaseSpecification();
            DDLHelper.deleteIndex(spec, schemaName, tablename, getName());
            
            // go up as many as 2 nodes to find a parent to refresh
            Node refreshNode = getParentNode();
            if (refreshNode == null) {
                refreshNode = this;
            } else {
                Node parent = refreshNode.getParentNode();
                if (parent != null) {
                    refreshNode = parent;
                }
            }

            SystemAction.get(RefreshAction.class).performAction(new Node[] { refreshNode } );
            
        } catch (DDLException e) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, null, e);
        }
    }

    @Override
    public boolean canDestroy() {
        DatabaseConnector connector = connection.getConnector();
        return connector.supportsCommand(Specification.DROP_INDEX);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public String getIconBase() {
        return ICONBASE;
    }
}
