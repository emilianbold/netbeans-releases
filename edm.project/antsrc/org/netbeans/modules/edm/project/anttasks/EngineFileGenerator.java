/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.edm.project.anttasks;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.lang.Exception;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;


import org.w3c.dom.Element;

import org.netbeans.modules.edm.model.impl.MashupDefinitionImpl;
import org.netbeans.modules.edm.model.SQLDBModel;
import org.netbeans.modules.edm.model.SQLDefinition;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.netbeans.modules.edm.model.DBConnectionDefinition;
import org.netbeans.modules.edm.model.DBTable;
import org.netbeans.modules.edm.model.DatabaseModel;
import org.netbeans.modules.edm.codegen.DB;
import org.netbeans.modules.edm.codegen.DBFactory;
import org.netbeans.modules.edm.codegen.StatementContext;
import org.netbeans.modules.edm.codegen.AxionDB;
import org.netbeans.modules.edm.codegen.AxionPipelineStatements;
import org.netbeans.modules.edm.codegen.AxionStatements;
import org.netbeans.modules.edm.editor.utils.RuntimeAttribute;
import org.netbeans.modules.edm.editor.utils.SQLDBConnectionDefinition;
import org.netbeans.modules.edm.editor.utils.SQLPart;
import org.netbeans.modules.edm.editor.utils.StringUtil;
import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.model.RuntimeDatabaseModel;
import org.netbeans.modules.edm.model.RuntimeInput;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLJoinView;
import org.netbeans.modules.edm.model.SourceTable;


/**
 * @author
 *
 */
public class EngineFileGenerator {

    private FileOutputStream fos = null;
    protected AxionDB db;
    protected Map<DBConnectionDefinition, String> linkTableMap;
    protected AxionPipelineStatements pipelineStmts;
    protected AxionStatements stmts;
    private EDMProProcessDefinition edmProcessDef = null;
    private static final String EDM_FILE_EXT = ".edm";

    public EngineFileGenerator() {
        try {
            edmProcessDef = new EDMProProcessDefinition();
            db = (AxionDB) DBFactory.getInstance().getDatabase(DB.AXIONDB);
            stmts = (AxionStatements) db.getStatements();
            pipelineStmts = db.getAxionPipelineStatements();
            linkTableMap = new HashMap<DBConnectionDefinition, String>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void buildInitializationStatements(SQLDBTable table, Map connDefToLinkName, List<SQLPart> stmts) throws EDMException {
        DBConnectionDefinition connDef = this.getConnectionDefinition(table);

        // Generate a unique name for the DB link, ensuring that the link name is a
        // legal SQL identifier, then generate SQL statement(s).
        String linkName = StringUtil.createSQLIdentifier(connDef.getName());

        if (!connDefToLinkName.containsValue(linkName)) {
            SQLPart dbLinkSqlPart = getCreateDBLinkSQL(connDef, linkName);
            dbLinkSqlPart.setConnectionPoolName(linkName);
            dbLinkSqlPart.setTableName(table.getQualifiedName());
            stmts.add(dbLinkSqlPart);
            connDefToLinkName.put(connDef, linkName);
        }

        StatementContext context = new StatementContext();
        context.setUsingFullyQualifiedTablePrefix(false);
        context.setUsingUniqueTableName(table, true);
        String localName = db.getUnescapedName(db.getGeneratorFactory().generate(table, context));
        SQLPart createSqlPart = getCreateRemoteTableSQL(table, localName, linkName);
        createSqlPart.setConnectionPoolName(linkName);
        createSqlPart.setTableName(table.getQualifiedName());
        SQLPart dropSqlPart = this.getDropExternalTableSQL(table, localName, true, context);
        dropSqlPart.setConnectionPoolName(linkName);
        dropSqlPart.setTableName(table.getQualifiedName());

        stmts.add(dropSqlPart);
        stmts.add(createSqlPart);
    }

    /**
     *
     * @param edmFile
     * @param buildDir
     * @throws java.lang.Exception
     */
    public void generateEngine(File edmFile, File targetDir) throws Exception {
        String edmFileName = edmFile.getName().substring(0, edmFile.getName().indexOf(EDM_FILE_EXT));
        String projectName = targetDir.getParentFile().getName();
        String engineFile = targetDir + File.separator + projectName + "_" + edmFileName + "_engine.xml";

        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        Element root = f.newDocumentBuilder().parse(edmFile).getDocumentElement();

        MashupDefinitionImpl def = new MashupDefinitionImpl();
        def.parseXML(root);

        SQLDefinition sqlDefinition = def.getSQLDefinition();
        RuntimeDatabaseModel rtModel = sqlDefinition.getRuntimeDbModel();
        Map rtInputMap = new HashMap<String, RuntimeAttribute>();
        Map attributeMap = new HashMap();
        if (rtModel != null) {
            RuntimeInput rtInput = rtModel.getRuntimeInput();
            if (rtInput != null) {
                rtInputMap = rtInput.getRuntimeAttributeMap();
            }
        }
        Iterator attribIter = rtInputMap.keySet().iterator();

        while (attribIter.hasNext()) {
            Object key = attribIter.next();
            RuntimeAttribute ra = (RuntimeAttribute) rtInputMap.get(key);
            //attributeMap.put(ra.getAttributeName(),ra.getAttributeValue());
            attributeMap.put(ra.getAttributeName(), ra);
        }

        //DBConnectionDefinition tgtConnDef = ((SQLDBModel) sqlDefinition.getSourceDatabaseModels().get(0)).getConnectionDefinition();

        StatementContext joinContext = new StatementContext();
        linkTableMap.clear();
        List sources = sqlDefinition.getSourceTables();
        Iterator sourceIter = sources.iterator();
        List<SQLPart> initSQLs = new LinkedList<SQLPart>();
        while (sourceIter.hasNext()) {
            SourceTable st = (SourceTable) sourceIter.next();
            DBConnectionDefinition connDef = getConnectionDefinition(st);
            if (st.getAliasName() == null || st.getAliasName().length() == 0) {
                st.setAliasName("EXT");
            }
            this.buildInitializationStatements((SQLDBTable) st, linkTableMap, initSQLs);
            joinContext.setUsingUniqueTableName((SQLDBTable) st, true);
        }

        this.edmProcessDef.setInitStatements(initSQLs);

        Collection joins = sqlDefinition.getObjectsOfType(SQLConstants.JOIN_VIEW);
        Iterator joinIter = joins.iterator();
        if (joinIter.hasNext()) {
            SQLJoinView view = (joins.size() > 0) ? (SQLJoinView) joinIter.next() : null;
            if (view != null) {
                joinContext.setUsingFullyQualifiedTablePrefix(false);
                joinContext.setUsingUniqueTableName(true);
                SQLPart queryPart = stmts.getSelectStatement(view, joinContext);
                queryPart.setConnectionPoolName(view.getDisplayName());
                queryPart.setTableName(view.getDisplayName());
                this.edmProcessDef.setDataMashupQuery(queryPart);
            }
        } else if (joins.size() == 0 && sources.size() > 1) {
            throw new EDMException("Invalid Validation.Having two tables with no join is not valid.");
        } else {
            SourceTable st = (SourceTable) sources.listIterator().next();
            if (st.getAliasName() == null || st.getAliasName().length() == 0) {
                st.setAliasName("EXT");
            }
            joinContext.setUsingFullyQualifiedTablePrefix(false);
            joinContext.setUsingUniqueTableName(true);
            SQLPart queryPart = stmts.getSelectStatement(st, joinContext);
            queryPart.setConnectionPoolName(getConnectionDefinition(st).getConnectionURL());
            queryPart.setTableName(st.getQualifiedName());
            this.edmProcessDef.setDataMashupQuery(queryPart);
        }
        this.edmProcessDef.setMashupResponse(sqlDefinition.getResponseType());
        this.edmProcessDef.setAttributeMap(attributeMap);
        String engineContent = this.edmProcessDef.generateEngineFile();
        fos = new FileOutputStream(engineFile);
        fos.write(engineContent.getBytes("UTF-8"));
        fos.flush();
        fos.close();
    }

    private void populateConnectionDefinitions(SQLDefinition def) {
        List trgDbmodels = def.getSourceDatabaseModels();
        Iterator iterator = trgDbmodels.iterator();
        while (iterator.hasNext()) {
            SQLDBModel element = (SQLDBModel) iterator.next();
            SQLDBConnectionDefinition originalConndef = (SQLDBConnectionDefinition) element.getConnectionDefinition();
            this.edmProcessDef.setDBConnectionParameters(originalConndef);
        }
    }

    private boolean requiresInit(SQLDefinition sqlDefn) {
        return true;
    }

    /**
     * Generates drop external statement for the given SQLDBTable if appropriate.
     *
     * @param table SQLDBTable for which to generate a drop external statement
     * @param localName local name of table as used in the Axion database; may be
     *        different from the table name contained in <code>table</code>
     * @param ifExists true if statement should include an "IF EXISTS" qualifier
     * @param context StatementContext to use in generating statement
     * @return SQL statement representing drop external statement for SQLDBTable.
     * @throws EDMException if error occurs during statement generation
     */
    protected SQLPart getDropExternalTableSQL(SQLDBTable table, String localName,
            boolean ifExists, StatementContext context) throws EDMException {
        return stmts.getDropExternalTableStatement(table, localName, ifExists, context);
    }

    private DBConnectionDefinition getConnectionDefinition(DBTable table) throws EDMException {
        DatabaseModel dbModel = table.getParent();
        DBConnectionDefinition conDef = dbModel.getConnectionDefinition();
        return conDef;
    }

    protected SQLPart getCreateDBLinkSQL(DBConnectionDefinition connDef, String linkName) throws EDMException {
        return stmts.getCreateDBLinkStatement(connDef, linkName);
    }

    private SQLPart getCreateRemoteTableSQL(SQLDBTable table, String localName, String linkName) throws EDMException {
        String prefix = "ORGPROP_";
        StringBuffer stmtBuf = new StringBuffer(50);
        SQLPart createSQLPart = null;
        if (StringUtil.isNullString(localName)) {
            localName = table.getName();
        }
        String flatfileLocation = table.getFlatFileLocationRuntimeInputName();
        if (flatfileLocation != null) {
            table.setAttribute(prefix + "FILENAME", "{" + flatfileLocation + "}");
        }
        StringBuffer sb = new StringBuffer();
        //sb.append("  ORGANIZATION( ");

        Collection<String> attrNames = (Collection<String>) table.getAttributeNames();
        for (String attrName : attrNames) {
            if (attrName.startsWith(prefix)) {
                String keyName = attrName.substring(attrName.indexOf(prefix) + prefix.length());
                sb.append(keyName + "='");
                String propValue = (String) table.getAttribute(attrName).getAttributeValue();
                sb.append(StringUtil.escapeControlChars(propValue) + "' ");
            }
        }
        //sb.append(")");
        // Generate a "create external table" statement that references its DB link

        if (flatfileLocation != null) {
            createSQLPart = stmts.getCreateFlatfileTableStatement(table, sb.toString(), true);
            String flatfileSQL = createSQLPart.getSQL();
            flatfileSQL = StringUtil.replaceFirst(flatfileSQL, ((table.getAliasName().length() == 0 || table.getAliasName() == null) ? "EXT" : table.getAliasName()) + "_" + table.getName(), "\\$tableName");

            flatfileSQL = StringUtil.replaceFirst(flatfileSQL, sb.toString(), "\\$\\{orgPropertiesList\\}");
            stmtBuf.append(flatfileSQL);
            createSQLPart.setSQL(flatfileSQL);
        } else {
            createSQLPart = stmts.getCreateRemoteTableStatement(table, localName, linkName);
            stmtBuf.append(createSQLPart.getSQL());
        }
        return createSQLPart;
    }
}