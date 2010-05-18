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
package org.netbeans.modules.etl.ui.view.wizardsloader;

import com.sun.etl.exception.DBSQLException;
import com.sun.etl.jdbc.SQLPart;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.java.hulp.i18n.Logger;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.dm.virtual.db.model.VirtualDatabaseModel;
import org.netbeans.modules.dm.virtual.db.ui.wizard.VirtualDBTableWizardIterator;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.ui.view.wizards.ETLCollaborationWizard;
import org.netbeans.modules.sql.framework.codegen.DB;
import org.netbeans.modules.sql.framework.codegen.DBFactory;
import org.netbeans.modules.sql.framework.codegen.axion.AxionDB;
import org.netbeans.modules.sql.framework.codegen.axion.AxionStatements;
import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.DBMetaDataFactory;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.impl.AbstractDBTable;
import org.netbeans.modules.sql.framework.model.impl.SourceTableImpl;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.openide.WizardDescriptor;

/**
 *
 * @author Manish Bharani
 */
public class BulkLoaderFileSelectionModeller {

    private WizardDescriptor wizdes = null;
    private String fld_delimiter = null;
    private String rec_delimiter = null;
    private String orgpropstr_prifix;
    DatabaseConnection dbconn;
    DBConnectionDefinition globalconndef;
    DBMetaDataFactory meta;
    List<File> selectedfiles;    //Logger
    private static transient final Logger mLogger = Logger.getLogger(BulkLoaderFileSelectionModeller.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public BulkLoaderFileSelectionModeller() {
    }

    public BulkLoaderFileSelectionModeller(WizardDescriptor wd) {
        this.wizdes = wd;
        dbconn = getFileDBConnection();
        meta = new DBMetaDataFactory();
        if (dbconn != null) {
            //As the tables enter into a single SQLDBModel, one connection def is only required
            createGlobalConnDefForSelectedFiles();
            createNewModelTempleteForBulkLoaderSrc(); //This has to be created for each mapped table
        }
        fld_delimiter = (String) wizdes.getProperty(ETLCollaborationWizard.BULK_LOADER_FIELD_DELIMITER);
        rec_delimiter = (String) wizdes.getProperty(ETLCollaborationWizard.BULK_LOADER_RECORD_DELIMITER);
        selectedfiles = (List) wizdes.getProperty(ETLCollaborationWizard.BULK_LOADER_SRC_DATA_FILEOBJ_LIST);
        this.orgpropstr_prifix = "LOADTYPE=\'DELIMITED\'  FIELDDELIMITER=\'" + fld_delimiter + "\' RECORDDELIMITER=\'" + rec_delimiter + "\' FILENAME=\'";
    }

    public List<SQLDBModel> generateSourceModel(SelectedTableMapperModeller mappermodel) {

        HashMap<String, List> trgtmodelmap = mappermodel.getMappedTargetModels();
        HashMap<String, List> srcmodelmap = mappermodel.getMappedSrcModels();
        HashMap<String, List> selectedsrcindex = mappermodel.getSelectedSrcTableIndexFromMapper();
        List<AbstractDBTable> trgttbls = mappermodel.getSortedTargetTables();
        List<String> srctbls = mappermodel.getTableNames(true);
        //XXX - This is a master container for models. This is to satisfy the contract of collab generation. To modify later
        List<SQLDBModel> temp_masterSrcdbModels = new ArrayList<SQLDBModel>();

        for (AbstractDBTable trgttbl : trgttbls) {
            String searchkey = mappermodel.getSearchKey(trgttbl);
            List selectionlist = ((List) selectedsrcindex.get(searchkey));
            
            String filepath = null;
            String selsrcfilename = null;
            if ((selectionlist != null) && (selectionlist.size() > 0)) {
                Integer selindex = (Integer) selectionlist.get(0);
                int sindex = selindex.intValue();
                selsrcfilename = srctbls.get(sindex); // Single Table is mapped against each target for Bulk Loader
                filepath = getAbsFilePath(selsrcfilename);
            }
            
            if (filepath != null) {
                String normalizedname = selsrcfilename.substring(0, selsrcfilename.lastIndexOf(".")).toUpperCase();
                String sqlquery = generateSourceCreationQuery(normalizedname, (SQLDBModel) ((List) trgtmodelmap.get(searchkey)).get(0), filepath);
                //Execute SQL
                Statement jdbcstmt;
                Connection conn;
                try {
                    conn = DBExplorerUtil.createConnection(dbconn);
                    jdbcstmt = conn.createStatement();
                    String query = sqlquery.toString();
                    mLogger.infoNoloc("Source Table Gen Query : \n" + query);
                    jdbcstmt.execute(query);
                } catch (SQLException ex) {
                    mLogger.errorNoloc("SQLException while creating external table  : " + sqlquery.toString(), ex);
                } catch (DBSQLException ex) {
                    mLogger.errorNoloc("DBSQLException while creating external table  : " + sqlquery.toString(), ex);
                }

                //Create a Model and add this table to model
                SQLDBModel sqldbmodel = createNewModelTempleteForBulkLoaderSrc();
                addSrcTableToModel(normalizedname, sqldbmodel);
                temp_masterSrcdbModels.add(sqldbmodel); //XXX - Remove this later. May not be needed

                //Add Src Model to master Structure
                ((ArrayList<SQLDBModel>) srcmodelmap.get(searchkey)).add(sqldbmodel);  //Single Model mapped to each target              
            }
        }
        cleanup();
        return temp_masterSrcdbModels;
    }
    
    private void cleanup(){
        meta.disconnectDB();
    }

    private void addSrcTableToModel(String srctablename, SQLDBModel sqldbmodel) {
        AbstractDBTable newTable = new SourceTableImpl();
        newTable.setName(srctablename);
        newTable.setDisplayName("(S1) " + srctablename);
        newTable.setSchema("");
        newTable.setEditable(true);
        newTable.setSelected(true);
        try {
            meta.populateColumns(newTable);
        } catch (Exception ex) {
            mLogger.errorNoloc("Unable to Populate columns for table [" + srctablename + "]", ex);
        }
        sqldbmodel.addTable(newTable);
            //Set File Table Organization Properties
              setFileOrgProperties(newTable);
    }
    
    private void setFileOrgProperties(SQLDBTable dbtable){
        dbtable.setAttribute("aliasName", "S1");
        dbtable.setAttribute("ORGPROP_FIELDDELIMITER",fld_delimiter);
        dbtable.setAttribute("ORGPROP_RECORDDELIMITER",rec_delimiter);
        dbtable.setAttribute("ORGPROP_LOADTYPE","DELIMITED");
        dbtable.setAttribute("ORGPROP_ISFIRSTLINEHEADER","false");
        dbtable.setAttribute("ORGPROP_CREATE_IF_NOT_EXIST","false");        
        dbtable.setAttribute("ORGPROP_VALIDATION","false");
        dbtable.setAttribute("temporaryTableName", SQLObjectUtil.generateTemporaryTableName(dbtable.getName()));
    }

    private String generateSourceCreationQuery(String srctblname, SQLDBModel trgtmodel, String filepath) {
        SQLDBTable trgtmodeldbtable = null;
        String modified_sqlquery = null;

        try {
            trgtmodeldbtable = (SQLDBTable) trgtmodel.getTables().get(0); //This Model is mapped to a single target table

            AxionDB db = (AxionDB) DBFactory.getInstance().getDatabase(DB.AXIONDB);
            AxionStatements stmts = (AxionStatements) db.getStatements();
            // Generate a "create external table" statement
            SQLPart sqlpart = stmts.getCreateFlatfileTableStatement(trgtmodeldbtable, this.orgpropstr_prifix + filepath + "\'", true);

            /* XXX - revisit this, somehow , table name fails to be reset. Implementing a work around */
            String sqlquery = sqlpart.toString();
            modified_sqlquery = sqlquery.replaceFirst(trgtmodeldbtable.getName(), srctblname);

        } catch (Exception ex) {
            mLogger.errorNoloc("Failed to Generate Axion Source Tbl from Target Tbl : " + srctblname, ex);
        }
        return modified_sqlquery;
    }

    private String getAbsFilePath(String filename) {
        for (File srcfile : selectedfiles) {
            if (srcfile.getName().equalsIgnoreCase(filename)) {
            return srcfile.getAbsolutePath();
            }
        }
        return null;
    }

    private void createGlobalConnDefForSelectedFiles() {
            try {
                meta.connectDB(DBExplorerUtil.createConnection(dbconn));
                globalconndef = SQLModelObjectFactory.getInstance().createDBConnectionDefinition("SourceConnection1", meta.getDBType(), dbconn.getDriverClass(), dbconn.getDatabaseURL(), dbconn.getUser(), dbconn.getPassword(), "Bulk Loader Source Model");
            } catch (Exception ex) {
                 mLogger.infoNoloc(ex.getMessage());
            }
    }

    private SQLDBModel createNewModelTempleteForBulkLoaderSrc() {
        SQLDBModel newmodel = null;
        newmodel = SQLModelObjectFactory.getInstance().createDBModel(SQLConstants.SOURCE_DBMODEL);
        newmodel.setModelName("SourceConnection1");
        newmodel.setConnectionDefinition(globalconndef);
        return newmodel;
    }

    private DatabaseConnection getFileDBConnection() {
        VirtualDatabaseModel ffmodel = (VirtualDatabaseModel) wizdes.getProperty(VirtualDBTableWizardIterator.PROP_VIRTUALDBMODEL);
        if (ffmodel != null) {
            String filedburl = ffmodel.toString();
            try {
                return DBExplorerUtil.createDatabaseConnection("org.axiondb.jdbc.AxionDriver", filedburl, "sa", "sa");
            } catch (DBSQLException ex) {
                mLogger.errorNoloc("Unable to connect to file database url : " + filedburl, ex);
            }
        }
        return null;
    }
}
