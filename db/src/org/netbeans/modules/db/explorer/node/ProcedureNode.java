/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.explorer.node;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.ChildNodeFactory;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.impl.AbstractCommand;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.api.Procedure;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Rob Englander
 */
public class ProcedureNode extends BaseNode {
    private static final String ICONBASE_P = "org/netbeans/modules/db/resources/procedure.png";
    private static final String ICONBASE_F = "org/netbeans/modules/db/resources/function.png";
    private static final String ICONBASE_T = "org/netbeans/modules/db/resources/trigger.png";
    private static final String FOLDER = "Procedure"; //NOI18N

    /**
     * Create an instance of ProcedureNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the ProcedureNode instance
     */
    public static ProcedureNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        DatabaseConnection conn = dataLookup.lookup(DatabaseConnection.class);
        ProcedureNode node;
        if (conn != null && "MySQL".equalsIgnoreCase(conn.getDriverName())) { // NOI18N
            node = new MySQL(dataLookup, provider);
        } else {
            node = new ProcedureNode(dataLookup, provider);
        }
        node.setup();
        return node;
    }

    private String name = ""; // NOI18N
    private final MetadataElementHandle<Procedure> procedureHandle;
    private final DatabaseConnection connection;
    private Type type;

    @SuppressWarnings("unchecked")
    private ProcedureNode(NodeDataLookup lookup, NodeProvider provider) {
        super(new ChildNodeFactory(lookup), lookup, FOLDER, provider);
        connection = getLookup().lookup(DatabaseConnection.class);
        procedureHandle = getLookup().lookup(MetadataElementHandle.class);
    }

    @Override
    protected void initialize() {
        boolean connected = !connection.getConnector().isDisconnected();
        MetadataModel metaDataModel = connection.getMetadataModel();
        if (connected && metaDataModel != null) {
            try {
                metaDataModel.runReadAction(
                    new Action<Metadata>() {
                        @Override
                        public void run(Metadata metaData) {
                            Procedure proc = procedureHandle.resolve(metaData);
                            name = proc.getName();
                            type = proc.getReturnValue() == null ? Type.Procedure : Type.Function;

                            updateProperties(proc);
                        }
                    }
                );
            } catch (MetadataModelException e) {
                NodeRegistry.handleMetadataModelException(this.getClass(), connection, e, true);
            }
        }
    }

    private void updateProperties(Procedure proc) {
        PropertySupport.Name ps = new PropertySupport.Name(this);
        addProperty(ps);

        if (proc.getReturnValue() == null) {
            addProperty(TYPE, TYPEDESC, String.class, false, NbBundle.getMessage (ProcedureNode.class, "StoredProcedure")); // NOI18N
        } else {
            addProperty(TYPE, TYPEDESC, String.class, false, NbBundle.getMessage (ProcedureNode.class, "StoredFunction")); // NOI18N
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
    
    public Type getType() {
        return this.type;
    }

    @Override
    public String getIconBase() {
        switch (type) {
            case Function:
                return ICONBASE_F;
            case Procedure:
                return ICONBASE_P;
            case Trigger:
                return ICONBASE_T;
            default:
                return null;
        }
    }

    @Override
    public String getShortDescription() {
        switch (type) {
            case Function:
                return NbBundle.getMessage (ProcedureNode.class, "ND_Function"); //NOI18N
            case Procedure:
                return NbBundle.getMessage (ProcedureNode.class, "ND_Procedure"); //NOI18N
            case Trigger:
                return NbBundle.getMessage (ProcedureNode.class, "ND_Trigger"); //NOI18N;
            default:
                return null;
        }
    }

    @Override
    public boolean canDestroy() {
        DatabaseConnector connector = connection.getConnector();
        return connector.supportsCommand(Specification.DROP_PROCEDURE);
    }

    @Override
    public void destroy() {
        DatabaseConnector connector = connection.getConnector();
        Specification spec = connector.getDatabaseSpecification();

        try {
            AbstractCommand command = spec.createCommandDropProcedure(getName());
            command.execute();
            remove();
        } catch (DDLException e) {
            Logger.getLogger(ProcedureNode.class.getName()).log(Level.INFO, e + " while deleting procedure " + getName());
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
        } catch (Exception e) {
            Logger.getLogger(ProcedureNode.class.getName()).log(Level.INFO, e + " while deleting procedure " + getName());
        }
    }
    
    public boolean isViewSourceSupported() {
        return false;
    }
    
    public boolean isEditSourceSupported() {
        return false;
    }
    
    public String getSource() {
        return "";
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ProcedureNode.class);
    }

    public enum Type {
        Procedure,
        Function,
        Trigger
    }
    
    public static class MySQL extends ProcedureNode implements SchemaNameProvider {
        private final MetadataElementHandle<Procedure> handle;
        private final DatabaseConnection connection;
        
        @SuppressWarnings("unchecked")
        private MySQL(NodeDataLookup lookup, NodeProvider provider) {
            super(lookup, provider);
            connection = getLookup().lookup(DatabaseConnection.class);
            handle = getLookup().lookup(MetadataElementHandle.class);
        }

        @Override
        public boolean isViewSourceSupported() {
            return true;
        }

        @Override
        public String getSource() {
            String source = "";
            try {
                Statement stat = connection.getConnection().createStatement();
                ResultSet rs = stat.executeQuery("SELECT * FROM mysql.proc WHERE name = '" + getName() + "';"); // NOI18N
                while(rs.next()) {
                    //String params = rs.getString("param_list");
                    source = rs.getString("body"); // NOI18N
                    //System.out.println(create + "(" + params + ")" + '\n' + code);
                }
            } catch (SQLException ex) {
                Logger.getLogger(ProcedureNode.class.getName()).log(Level.INFO, ex + " while get source of procedure " + getName());
            }
            return source;
        }

        @Override
        public String getSchemaName() {
            MetadataModel metaDataModel = connection.getMetadataModel();
            final String[] array = new String[1];

            try {
                metaDataModel.runReadAction(
                    new Action<Metadata>() {
                    @Override
                        public void run(Metadata metaData) {
                            Procedure view = handle.resolve(metaData);
                            if (view != null) {
                                array[0] = view.getParent().getName();
                            }
                        }
                    }
                );
            } catch (MetadataModelException e) {
                NodeRegistry.handleMetadataModelException(ProcedureNode.class, connection, e, true);
            }

            return array[0];
        }

        @Override
        public String getCatalogName() {
            MetadataModel metaDataModel = connection.getMetadataModel();
            final String[] array = new String[1];

            try {
                metaDataModel.runReadAction(
                    new Action<Metadata>() {
                    @Override
                        public void run(Metadata metaData) {
                            Procedure view = handle.resolve(metaData);
                            if (view != null) {
                                array[0] = view.getParent().getParent().getName();
                            }
                        }
                    }
                );
            } catch (MetadataModelException e) {
                NodeRegistry.handleMetadataModelException(ProcedureNode.class, connection, e, true);
            }

            return array[0];
        }
        
    }
    
}
