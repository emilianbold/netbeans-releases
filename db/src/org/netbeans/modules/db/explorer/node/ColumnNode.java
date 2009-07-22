/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.explorer.node;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.lib.ddl.impl.RemoveColumn;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.explorer.DatabaseMetaDataTransferAccessor;
import org.netbeans.modules.db.explorer.action.RefreshAction;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Column;
import org.netbeans.modules.db.metadata.model.api.Index;
import org.netbeans.modules.db.metadata.model.api.IndexColumn;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.api.Nullable;
import org.netbeans.modules.db.metadata.model.api.Table;
import org.netbeans.modules.db.metadata.model.api.Tuple;
import org.netbeans.modules.db.metadata.model.api.PrimaryKey;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author Rob Englander
 */
public class ColumnNode extends BaseNode implements SchemaNameProvider, ColumnNameProvider {
    private static final String COLUMN = "org/netbeans/modules/db/resources/column.gif";
    private static final String PRIMARY = "org/netbeans/modules/db/resources/columnPrimary.gif";
    private static final String INDEX = "org/netbeans/modules/db/resources/columnIndex.gif";
    private static final String FOLDER = "Column"; //NOI18N
    private static final String TOOLTIP_COLUMN = NbBundle.getMessage(ColumnNode.class, "ND_Column");
    private static final String TOOLTIP_PRIMARY = NbBundle.getMessage(ColumnNode.class, "ND_PrimaryKey");
    private static final String TOOLTIP_INDEX = NbBundle.getMessage(ColumnNode.class, "ND_Index");

    /**
     * Create an instance of ColumnNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the ColumnNode instance
     */
    public static ColumnNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        ColumnNode node = new ColumnNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private String name = ""; // NOI18N
    private String icon;
    /** Description used for tooltip. */
    private String description = TOOLTIP_COLUMN;
    private final MetadataElementHandle<Column> columnHandle;
    private final DatabaseConnection connection;
    private boolean isTableColumn = true;

    @SuppressWarnings("unchecked")
    private ColumnNode(NodeDataLookup lookup, NodeProvider provider) {
        super(lookup, FOLDER, provider);
        columnHandle = getLookup().lookup(MetadataElementHandle.class);
        connection = getLookup().lookup(DatabaseConnection.class);
    }

    @Override
    public synchronized void refresh() {
        setupNames();
        super.refresh();
    }

    protected void initialize() {
        setupNames();
    }

    private void setupNames() {
        boolean connected = !connection.getConnector().isDisconnected();
        MetadataModel metaDataModel = connection.getMetadataModel();
        if (connected && metaDataModel != null) {
            try {
                metaDataModel.runReadAction(
                    new Action<Metadata>() {
                        public void run(Metadata metaData) {
                            Column column = columnHandle.resolve(metaData);
                            if (column != null) {
                                name = column.getName();
                                icon = COLUMN;

                                updateProperties(column);

                                Tuple tuple = column.getParent();
                                if (tuple instanceof Table) {
                                    Table table = (Table)tuple;
                                    PrimaryKey pkey = table.getPrimaryKey();

                                    boolean found = false;
                                    if (pkey != null) {
                                        Collection<Column> columns = pkey.getColumns();
                                        for (Column c : columns) {
                                            if (c.getName().equals(column.getName())) {
                                                found = true;
                                                icon = PRIMARY;
                                                description = TOOLTIP_PRIMARY;
                                                break;
                                            }
                                        }
                                    }

                                    if (!found) {
                                        Collection<Index> indexes = table.getIndexes();
                                        for (Index index : indexes) {
                                            Collection<IndexColumn> columns = index.getColumns();
                                            for (IndexColumn c : columns) {
                                                if (c.getName().equals(column.getName())) {
                                                    found = true;
                                                    icon = INDEX;
                                                    description = TOOLTIP_INDEX;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    isTableColumn = false;
                                }
                            }
                        }
                    }
                );
            } catch (MetadataModelException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    private void updateProperties(Column column) {
        PropertySupport ps = new PropertySupport.Name(this);
        addProperty(ps);

        assert column != null : "Column " + this + " cannot be null.";
        if (column == null) {
            return ;
        }

        try {
            addProperty(NULL, NULLDESC, Boolean.class, false, column.getNullable() == Nullable.NULLABLE);
            if (column.getType() != null) {
                addProperty(DATATYPE, DATATYPEDESC, String.class, false, column.getType().toString());
            }

            int len = column.getLength();
            if (len == 0) {
                len = column.getPrecision();
            }
            addProperty(COLUMNSIZE, COLUMNSIZEDESC, Integer.class, false, len);
            addProperty(DIGITS, DIGITSDESC, Short.class, false, column.getScale());
            addProperty(POSITION, POSITIONDESC, Integer.class, false, column.getPosition());
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    public String getColumnName() {
        return getColumnName(connection, columnHandle);
    }

    public String getSchemaName() {
        return getSchemaName(connection, columnHandle);
    }

    public String getCatalogName() {
        return getCatalogName(connection, columnHandle);
    }

    public String getParentName() {
        return getParentName(connection, columnHandle);
    }

    public int getPosition() {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final int[] array = new int[1];

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {
                    public void run(Metadata metaData) {
                        Column column = columnHandle.resolve(metaData);
                        array[0] = column.getPosition();
                    }
                }
            );
        } catch (MetadataModelException e) {
            Exceptions.printStackTrace(e);
        }

        return array[0];
    }

    @Override
    public void destroy() {
        DatabaseConnector connector = connection.getConnector();
        Specification spec = connector.getDatabaseSpecification();

        try {
            RemoveColumn command = spec.createCommandRemoveColumn(getParentName());

            String schema = getSchemaName();
            if (schema == null) {
                schema = getCatalogName();
            }

            command.setObjectOwner(schema);
            command.removeColumn(getName());
            command.execute();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }

        SystemAction.get(RefreshAction.class).performAction(new Node[] { getParentNode() });
    }

    @Override
    public boolean canDestroy() {
        if (isTableColumn) {
            DatabaseConnector connector = connection.getConnector();
            return connector.supportsCommand(Specification.REMOVE_COLUMN);
        } else {
            return false;
        }
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
        return icon;
    }

    @Override
    public boolean canCopy() {
        return true;
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        ExTransferable result = ExTransferable.create(super.clipboardCopy());
        result.put(new ExTransferable.Single(DatabaseMetaDataTransfer.COLUMN_FLAVOR) {
            protected Object getData() {
                return DatabaseMetaDataTransferAccessor.DEFAULT.createColumnData(connection.getDatabaseConnection(),
                        connection.findJDBCDriver(), getParentName(), getName());
            }
        });
        return result;
    }

    @Override
    public String getShortDescription() {
        return description;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ColumnNode.class);
    }

    public static String getColumnName(DatabaseConnection connection, final MetadataElementHandle<Column> handle) {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final String[] array = { null };
        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {
                    public void run(Metadata metaData) {
                        Column column = handle.resolve(metaData);
                        if (column != null) {
                            array[0] = column.getName();
                        }
                    }
                }
            );
        } catch (MetadataModelException e) {
            Exceptions.printStackTrace(e);
        }

        return array[0];
    }

    public static String getParentName(DatabaseConnection connection, final MetadataElementHandle<Column> handle) {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final String[] array = { null };

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {
                    public void run(Metadata metaData) {
                        Column column = handle.resolve(metaData);
                        if (column != null) {
                            array[0] = column.getParent().getName();
                        }
                    }
                }
            );
        } catch (MetadataModelException e) {
            Exceptions.printStackTrace(e);
        }

        return array[0];
    }

    public static String getSchemaName(DatabaseConnection connection, final MetadataElementHandle<Column> handle) {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final String[] array = new String[1];

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {
                    public void run(Metadata metaData) {
                        Column column = handle.resolve(metaData);
                        if (column != null) {
                            array[0] = column.getParent().getParent().getName();
                        }
                    }
                }
            );
        } catch (MetadataModelException e) {
            Exceptions.printStackTrace(e);
        }

        return array[0];
    }

    public static String getCatalogName(DatabaseConnection connection, final MetadataElementHandle<Column> handle) {
        MetadataModel metaDataModel = connection.getMetadataModel();
        final String[] array = new String[1];

        try {
            metaDataModel.runReadAction(
                new Action<Metadata>() {
                    public void run(Metadata metaData) {
                        Column column = handle.resolve(metaData);
                        if (column != null) {
                            array[0] = column.getParent().getParent().getParent().getName();
                        }
                    }
                }
            );
        } catch (MetadataModelException e) {
            Exceptions.printStackTrace(e);
        }

        return array[0];
    }

}
