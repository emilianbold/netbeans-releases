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

import java.awt.Image;
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
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Rob Englander, Jiri Rechtacek
 */
public class ProcedureNode extends BaseNode {
    private static final String ICONBASE_P = "org/netbeans/modules/db/resources/procedure.png";
    private static final String ICONBASE_F = "org/netbeans/modules/db/resources/function.png";
    private static final String ICONBASE_T = "org/netbeans/modules/db/resources/trigger.png";
    private static final Image ERROR_BADGE = ImageUtilities.loadImage("org/netbeans/modules/db/resources/error-badge.gif");
    private static final String FOLDER = "Procedure"; //NOI18N
    
    private static final String DELIMITER = "@@"; // NOI18N
    private static final String SPACE = " "; // NOI18N
    private static final String NEW_LINE = "\n"; // NOI18N

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
        } else if (conn != null && conn.getDriverName() != null && conn.getDriverName().startsWith("Oracle")) { // NOI18N
            node = new Oracle(dataLookup, provider);
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
    
    protected Type getType() {
        return this.type;
    }

    @Override
    public String getIconBase() {
        switch (getType()) {
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
        switch (getType()) {
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
    
    public String getParams() {
        return "";
    }
    
    public String getBody() {
        return "";
    }

    public String getSource() {
        return "";
    }
    
    public String getDDL() {
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
    
    public static class MySQL extends ProcedureNode {
        private final DatabaseConnection connection;
        
        @SuppressWarnings("unchecked")
        private MySQL(NodeDataLookup lookup, NodeProvider provider) {
            super(lookup, provider);
            connection = getLookup().lookup(DatabaseConnection.class);
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
                    String params = rs.getString("param_list"); // NOI18N
                    String body = rs.getString("body"); // NOI18N
                    source = "PROCEDURE " + getName() + '\n' + // NOI18N
                            '(' + params + ")" + '\n' + // NOI18N
                            body;
                }
            } catch (SQLException ex) {
                Logger.getLogger(ProcedureNode.class.getName()).log(Level.INFO, ex + " while get source of procedure " + getName());
            }
            return source;
        }

        @Override
        public String getParams() {
            String params = "";
            try {
                Statement stat = connection.getConnection().createStatement();
                ResultSet rs = stat.executeQuery("SELECT * FROM mysql.proc WHERE name = '" + getName() + "';"); // NOI18N
                while(rs.next()) {
                    params = rs.getString("param_list"); // NOI18N
                }
            } catch (SQLException ex) {
                Logger.getLogger(ProcedureNode.class.getName()).log(Level.INFO, ex + " while get params of procedure " + getName());
            }
            return params;
        }

        @Override
        public String getBody() {
            String body = "";
            try {
                Statement stat = connection.getConnection().createStatement();
                ResultSet rs = stat.executeQuery("SELECT * FROM mysql.proc WHERE name = '" + getName() + "';"); // NOI18N
                while(rs.next()) {
                    body = rs.getString("body"); // NOI18N
                }
            } catch (SQLException ex) {
                Logger.getLogger(ProcedureNode.class.getName()).log(Level.INFO, ex + " while get source of procedure " + getName());
            }
            return body;
        }
        
        

        @Override
        public boolean isEditSourceSupported() {
            return true;
        }

        @Override
        public String getDDL() {
            StringBuilder expression = new StringBuilder();
            // set delimiter
            expression.append("DELIMITER ").append(DELIMITER).append(NEW_LINE); // NOI18N
            // DDL
            expression.append("DROP PROCEDURE ").append(getName()).append(SPACE).append(DELIMITER).append(NEW_LINE);
            expression.append("CREATE ").append(getSource());
            expression.append(SPACE).append(DELIMITER).append(SPACE).append(NEW_LINE); // NOI18N
            // unset delimiter
            expression.append("DELIMITER ; ").append(NEW_LINE); // NOI18N
            return expression.toString();
        }

    }
    
    public static class Oracle extends ProcedureNode {
        private final DatabaseConnection connection;
        private final MetadataElementHandle<Procedure> procedureHandle;
        private String procedureName = null;
        private Type procedureType = null;
        private boolean valid = false;
        
        @SuppressWarnings("unchecked")
        private Oracle(NodeDataLookup lookup, NodeProvider provider) {
            super(lookup, provider);
            connection = getLookup().lookup(DatabaseConnection.class);
            procedureHandle = getLookup().lookup(MetadataElementHandle.class);
        }

        @Override
        protected void initialize() {
            super.initialize();
            boolean connected = !connection.getConnector().isDisconnected();
            MetadataModel metaDataModel = connection.getMetadataModel();
            if (connected && metaDataModel != null) {
                try {
                    metaDataModel.runReadAction(
                        new Action<Metadata>() {
                            @Override
                            public void run(Metadata metaData) {
                                Procedure proc = procedureHandle.resolve(metaData);
                                procedureName = proc.getName();
                                
                                Statement stmt;
                                try {
                                    stmt = connection.getConnection().createStatement();
                                    ResultSet rs = stmt.executeQuery("SELECT OBJECT_TYPE, STATUS FROM SYS.ALL_OBJECTS WHERE OWNER='" + connection.getSchema().toUpperCase() + "'" // NOI18N
                                            + " AND OBJECT_NAME = '" + procedureName + "'" // NOI18N
                                            + " AND ( OBJECT_TYPE = 'PROCEDURE' OR OBJECT_TYPE = 'TRIGGER' OR OBJECT_TYPE = 'FUNCTION' )"); // NOI18N
                                    
                                    while(rs.next()) {
                                        // type of procedure
                                        String objectType = rs.getString("OBJECT_TYPE"); // NOI18N
                                        if ("PROCEDURE".equals(objectType)) { // NOI18N
                                            procedureType = Type.Procedure;
                                        } else if ("FUNCTION".equals(objectType)) { // NOI18N
                                            procedureType = Type.Function;
                                        } else if ("TRIGGER".equals(objectType)) { // NOI18N
                                            procedureType = Type.Trigger;
                                        } else {
                                            assert false : "Unknown type " + objectType;
                                        }

                                        // valid or invalid
                                        String status = rs.getString("STATUS"); // NOI18N
                                        valid = "VALID".equals(status); // NOI18N
                                    }
                                } catch (SQLException ex) {
                                    Logger.getLogger(ProcedureNode.class.getName()).log(Level.INFO, ex + " while initialize procedure " + proc);
                                }
                                
                                updateProcedureProperties();
                            }
                        }
                    );
                } catch (MetadataModelException e) {
                    NodeRegistry.handleMetadataModelException(this.getClass(), connection, e, true);
                }
            }
        }
        
        private void updateProcedureProperties() {
            PropertySupport.Name ps = new PropertySupport.Name(this);
            addProperty(ps);
            
            switch (procedureType) {
                case Function:
                    addProperty(TYPE, TYPEDESC, String.class, false, NbBundle.getMessage (ProcedureNode.class, "StoredFunction")); // NOI18N
                    break;
                case Procedure:
                    addProperty(TYPE, TYPEDESC, String.class, false, NbBundle.getMessage (ProcedureNode.class, "StoredProcedure")); // NOI18N
                    break;
                case Trigger:
                    addProperty(TYPE, TYPEDESC, String.class, false, NbBundle.getMessage (ProcedureNode.class, "StoredTrigger")); // NOI18N
                    break;
                default:
                    assert false : "Unknown type " + procedureType;
            }
        }

        @Override
        public String getName() {
            return procedureName;
        }

        @Override
        public Type getType() {
            return procedureType;
        }

        @Override
        public String getShortDescription() {
            switch (procedureType) {
                case Function:
                    return valid ? NbBundle.getMessage (ProcedureNode.class, "ND_Function") : NbBundle.getMessage (ProcedureNode.class, "ND_Function_Invalid"); //NOI18N
                case Procedure:
                    return valid ? NbBundle.getMessage (ProcedureNode.class, "ND_Procedure") : NbBundle.getMessage (ProcedureNode.class, "ND_Procedure_Invalid"); //NOI18N
                case Trigger:
                    return valid ? NbBundle.getMessage (ProcedureNode.class, "ND_Trigger") : NbBundle.getMessage (ProcedureNode.class, "ND_Trigger_Invalid"); //NOI18N;
                default:
                    return null;
            }
        }

        @Override
        public Image getIcon(int type) {
            Image base = super.getIcon(type);
            if (valid) {
                return base;
            } else {
                return ImageUtilities.mergeImages(base, ERROR_BADGE, 6, 6);
            }
        }

        @Override
        public boolean isViewSourceSupported() {
            return true;
        }

        @Override
        public String getBody() {
            String source = getSource();
            String body = "";
            int beginIdx = source.indexOf("BEGIN"); // NOI18N
            if (beginIdx != -1) {
                body = source.substring(beginIdx);
            }
            return body;
        }

        @Override
        public String getParams() {
            String source = getSource();
            String params = "";
            int beginIdx = source.indexOf("BEGIN"); // NOI18N
            int lIdx = source.indexOf('('); // NOI18N
            int rIdx = source.indexOf(')'); // NOI18N
            if (lIdx != -1 && rIdx != -1 && lIdx < beginIdx) {
                params = source.substring(lIdx, rIdx + 1);
            }
            return params;
        }
        
        @Override
        public String getSource() {
            StringBuilder sb = new StringBuilder();
            String owner = "";
            try {
                Statement stat = connection.getConnection().createStatement();
                // select text from sys.dba_source where name = ??? and owner = upper('???') order by dba_source.line;
                String q = "SELECT TEXT, OWNER FROM SYS.ALL_SOURCE WHERE NAME = '" + getName() + "'" // NOI18N
                        + " ORDER BY LINE"; // NOI18N
                ResultSet rs = stat.executeQuery(q);
                while(rs.next()) {
                    sb.append(rs.getString("text")); // NOI18N
                    owner = rs.getString("owner"); // NOI18N
                }
            } catch (SQLException ex) {
                Logger.getLogger(ProcedureNode.class.getName()).log(Level.INFO, ex + " while get source of procedure " + getName());
            }
            if (connection.getSchema().equalsIgnoreCase(owner)) {
                return sb.toString();
            } else {
                return fqn(sb.toString(), owner);
            }
        }

        @Override
        public boolean isEditSourceSupported() {
            return true;
        }

        @Override
        public String getDDL() {
            StringBuilder expression = new StringBuilder();
            // set delimiter
            expression.append("DELIMITER ").append(DELIMITER).append(NEW_LINE); // NOI18N
            // DDL
            expression.append("CREATE OR REPLACE ").append(getSource());
            expression.append(SPACE).append(DELIMITER).append(NEW_LINE); // NOI18N
            // unset delimiter
            expression.append("DELIMITER ; ").append(NEW_LINE); // NOI18N
            return expression.toString();
        }

        private String fqn(String source, String owner) {
            String fqSource = source;
            String toFind = "PROCEDURE "; // NOI18N
            int nameIdx = source.indexOf(toFind);
            if (nameIdx != -1) {
                fqSource = source.substring(0, nameIdx + toFind.length()) +
                        owner +
                        '.' + // NOI18N
                        source.substring(nameIdx + toFind.length()).trim();
            }
            return fqSource;
        }

    }
}
