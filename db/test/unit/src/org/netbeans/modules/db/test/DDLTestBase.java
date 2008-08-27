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

package org.netbeans.modules.db.test;

import java.sql.Types;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.lib.ddl.impl.AddColumn;
import org.netbeans.lib.ddl.impl.CreateIndex;
import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.impl.CreateView;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.impl.SpecificationFactory;
import org.netbeans.lib.ddl.impl.TableColumn;
import org.netbeans.lib.ddl.DatabaseSpecification;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.infos.TableListNodeInfo;
import org.netbeans.modules.db.explorer.infos.TableNodeInfo;

/**
 *
 * @author David
 */
public class DDLTestBase extends DBTestBase {
    private static Logger LOGGER = Logger.getLogger(DDLTestBase.class.getName());

    protected static SpecificationFactory specfactory;
    protected Specification spec;
    protected DriverSpecification drvSpec;

    static {
        try {
            specfactory = new SpecificationFactory();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
            throw new RuntimeException(e);
        }
    }


    public DDLTestBase(String name) {
        super(name);
    }

    protected final DatabaseSpecification getSpecification() throws Exception {
        DatabaseConnection dbconn = getDatabaseConnection(true);
        
        ConnectionNodeInfo cinfo = org.netbeans.modules.db.explorer.DatabaseConnection.findConnectionNodeInfo(dbconn.getName());
        
        return cinfo.getSpecification();        
    }
    
    protected final DriverSpecification getDriverSpecification() throws Exception {
        DatabaseConnection dbconn = getDatabaseConnection(true);
        
        ConnectionNodeInfo cinfo = org.netbeans.modules.db.explorer.DatabaseConnection.findConnectionNodeInfo(dbconn.getName());
        
        return cinfo.getDriverSpecification();
    }
    
    protected final TableNodeInfo getTableNodeInfo(String tablename) throws Exception {
        DatabaseConnection dbconn = getDatabaseConnection(true);
        
        ConnectionNodeInfo cinfo = org.netbeans.modules.db.explorer.DatabaseConnection.findConnectionNodeInfo(dbconn.getName());

        TableListNodeInfo tableList = null;
        for (DatabaseNodeInfo child : cinfo.getChildren()) {
            if (child instanceof TableListNodeInfo) {
                tableList = (TableListNodeInfo)child;
            }
        }

        assertNotNull(tableList);

        for (Object child : tableList.getChildren()) {
            if (child instanceof TableNodeInfo) {
                TableNodeInfo tinfo = (TableNodeInfo)child;
                if (tinfo.getDisplayName().equals(tablename)) {
                    return tinfo;
                }
            }
        }

        return null;
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        spec = (Specification)getSpecification();
        drvSpec = getDriverSpecification();
    }
    
    protected void createBasicTable(String tablename, String pkeyName)
            throws Exception {
        dropTable(tablename);
        CreateTable cmd = spec.createCommandCreateTable(tablename);
        cmd.setObjectOwner(getSchema());

        // primary key
        TableColumn col = cmd.createPrimaryKeyColumn(pkeyName);
        col.setColumnType(Types.INTEGER);
        col.setNullAllowed(false);

        cmd.execute();
    }

    protected void createView(String viewName, String query) throws Exception {
        CreateView cmd = spec.createCommandCreateView(viewName);
        cmd.setQuery(query);
        cmd.setObjectOwner(getSchema());
        cmd.execute();

        assertFalse(cmd.wasException());
    }

    protected void createSimpleIndex(String tablename,
            String indexname, String colname) throws Exception {
        // Need to get identifier into correct case because we are
        // still quoting referred-to identifiers.
        tablename = fixIdentifier(tablename);
        CreateIndex xcmd = spec.createCommandCreateIndex(tablename);
        xcmd.setIndexName(indexname);

        // *not* unique
        xcmd.setIndexType(new String());

        xcmd.setObjectOwner(getSchema());
        xcmd.specifyColumn(fixIdentifier(colname));

        xcmd.execute();
    }

    /**
     * Adds a basic column.  Non-unique, allows nulls.
     */
    protected void addBasicColumn(String tablename, String colname,
            int type, int size) throws Exception {
        // Need to get identifier into correct case because we are
        // still quoting referred-to identifiers.
        tablename = fixIdentifier(tablename);
        AddColumn cmd = spec.createCommandAddColumn(tablename);
        cmd.setObjectOwner(getSchema());
        TableColumn col = (TableColumn)cmd.createColumn(colname);
        col.setColumnType(type);
        col.setColumnSize(size);
        col.setNullAllowed(true);

        cmd.execute();
        if ( cmd.wasException() ) {
            throw new Exception("Unable to add column");
        }
    }
}
