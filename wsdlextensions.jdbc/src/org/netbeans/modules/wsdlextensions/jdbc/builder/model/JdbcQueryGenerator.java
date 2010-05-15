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

/*
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.netbeans.modules.wsdlextensions.jdbc.builder.model;

import org.netbeans.modules.wsdlextensions.jdbc.builder.model.DBQueryModel;
import org.netbeans.modules.wsdlextensions.jdbc.builder.util.XMLCharUtil;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DBColumn;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DBTable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * @author Venkat P
 */
public class JdbcQueryGenerator implements DBQueryModel {

    private String mtabName = null;

    private List mcolumns = null;

    private List mInsertColumns = null;

    private List mUpdateColumns = null;

    private List mChooseColumns = null;

    private List mPollColumns = null;

    private String mSchemaName = null;

    private static final Logger mLog = Logger.getLogger(JdbcQueryGenerator.class.getName());

    private static final String INSERT_QUERY = "insertQuery";

    private static final String UPDATE_QUERY = "updateQuery";

    private JdbcQueryGenerator(){
    	
    }
    
    private static final JdbcQueryGenerator instance = new JdbcQueryGenerator();
    
    public static final JdbcQueryGenerator getInstance(){
    	if(instance != null)
    		return instance;
    	else
    		return new JdbcQueryGenerator();
    }
    
    /**
     * @param souTable
     */
    public void init(final DBTable souTable) {
        this.mtabName = souTable.getName();
        this.mcolumns = souTable.getColumnList();
        this.mInsertColumns = new ArrayList();
        this.mUpdateColumns = new ArrayList();
        this.mChooseColumns = new ArrayList();
        this.mPollColumns = new ArrayList();

        for (int cnt = 0; cnt < this.mcolumns.size(); cnt++) {
            final DBColumn temp = (DBColumn) this.mcolumns.get(cnt);
            if (temp.isInsertSelected()) {
                this.mInsertColumns.add(temp);
            }
            if (temp.isUpdateSelected()) {
                this.mUpdateColumns.add(temp);
            }
            if (temp.isChooseSelected()) {
                this.mChooseColumns.add(temp);
            }
            if (temp.isPollSelected()) {
                this.mPollColumns.add(temp);
            }
        }
        this.mSchemaName = souTable.getSchema();
    }

    /**
     * @return String
     * @throws Exception
     */
    public String createInsertQuery() throws Exception {
        final StringBuffer sb = new StringBuffer();
        /*sb.append("insert into");
        sb.append(" ");
        sb.append("\"" + this.mSchemaName + "\"");
        sb.append(".");
        sb.append("\"" + this.mtabName + "\"");
        sb.append(" ");
        sb.append("(");
        for (int i = 0; i < this.mInsertColumns.size(); i++) {
            sb.append("\"" + ((DBColumn) this.mInsertColumns.get(i)).getName() + "\"");
            if (i == this.mInsertColumns.size() - 1) {
                sb.append(")");
            } else {
                sb.append(",");
            }
        }
        sb.append(" ");
        sb.append("values (");
        for (int i = 0; i < this.mInsertColumns.size(); i++) {
            sb.append("?");
            if (i == this.mInsertColumns.size() - 1) {
                sb.append(")");
            } else {
                sb.append(",");
            }
        }
        JdbcQueryGenerator.mLog.log(Level.INFO, "Generated Insert Query " + sb.toString());
        return sb.toString();*/
        sb.append("insert into");
        sb.append(" ");
        sb.append(this.mtabName);
        sb.append(" ");
        sb.append("(");
        for (int i = 0; i < this.mInsertColumns.size(); i++) {
            sb.append(((DBColumn) this.mInsertColumns.get(i)).getName());
            if (i == this.mInsertColumns.size() - 1) {
                sb.append(")");
            } else {
                sb.append(",");
            }
        }
        sb.append(" ");
        sb.append("values (");
        for (int i = 0; i < this.mInsertColumns.size(); i++) {
            sb.append("?");
            if (i == this.mInsertColumns.size() - 1) {
                sb.append(")");
            } else {
                sb.append(",");
            }
        }
        JdbcQueryGenerator.mLog.log(Level.INFO, "Generated Insert Query " + sb.toString());
        return sb.toString();
    }

    /**
     * @return String
     * @throws Exception
     */
    public String createUpdateQuery(String where) throws Exception {
        final StringBuffer sb = new StringBuffer();
        /*sb.append("update");
        sb.append(" ");
        sb.append("\"" + this.mSchemaName + "\"");
        sb.append(".");
        sb.append("\"" + this.mtabName + "\"");
        sb.append(" ");
        sb.append("set ");
        for (int i = 0; i < this.mUpdateColumns.size(); i++) {
            sb.append("\"" + this.mtabName + "\"");
            sb.append(".");
            sb.append("\"" + ((DBColumn) this.mUpdateColumns.get(i)).getName() + "\"");
            sb.append("  ");
            sb.append("=");
            sb.append(" ?");
            if (i != this.mUpdateColumns.size() - 1) {
                sb.append(",");
            }
        }
        JdbcQueryGenerator.mLog.log(Level.INFO, "Generated Update Query " + sb.toString());
        return sb.toString();*/
        sb.append("update");
        sb.append(" ");
        sb.append(this.mtabName);
        sb.append(" ");
        sb.append("set ");
        for (int i = 0; i < this.mUpdateColumns.size(); i++) {
            sb.append(this.mtabName);
            sb.append(".");
            sb.append(((DBColumn) this.mUpdateColumns.get(i)).getName());
            sb.append("  ");
            sb.append("=");
            sb.append(" ?");
            if (i != this.mUpdateColumns.size() - 1) {
                sb.append(",");
            }
        }
        if(where != null && !(where.equals(""))){
	        sb.append(" where ");
	        sb.append(where);
        }
        JdbcQueryGenerator.mLog.log(Level.INFO, "Generated Update Query " + sb.toString());
        return sb.toString();
    }

    /**
     * @return String
     * @throws Exception
     */
    public String createDeleteQuery(String where) throws Exception {
        final StringBuffer sb = new StringBuffer();
        /*sb.append("delete");
        sb.append(" ");
        sb.append("from");
        sb.append(" ");
        sb.append("\"" + this.mSchemaName + "\"");
        sb.append(".");
        sb.append("\"" + this.mtabName + "\"");
        JdbcQueryGenerator.mLog.log(Level.INFO, "Generated Delete Query " + sb.toString());
        return sb.toString();*/
        sb.append("delete");
        sb.append(" ");
        sb.append("from");
        sb.append(" ");
        sb.append(this.mtabName);
        if(where != null && !(where.equals(""))){
	        sb.append(" where ");
	        sb.append(where);
        }
        JdbcQueryGenerator.mLog.log(Level.INFO, "Generated Delete Query " + sb.toString());
        return sb.toString();
    }

    /**
     * @return String
     * @throws Exception
     */
    public String createFindQuery(String where) throws Exception {
        final StringBuffer sb = new StringBuffer();
        /*sb.append("select");
        sb.append(" ");
        for (int i = 0; i < this.mChooseColumns.size(); i++) {
            sb.append("\"" + ((DBColumn) this.mChooseColumns.get(i)).getName() + "\"");
            if (i == this.mChooseColumns.size() - 1) {
                sb.append(" ");
            } else {
                sb.append(",");
            }
        }
        // selected columns

        sb.append("from");
        sb.append(" ");
        sb.append("\"" + this.mSchemaName + "\"");
        sb.append(".");
        sb.append("\"" + this.mtabName + "\"");

        JdbcQueryGenerator.mLog.log(Level.INFO, "Generated Find Query " + sb.toString());
        return sb.toString();*/
        sb.append("select");
        sb.append(" ");
        for (int i = 0; i < this.mChooseColumns.size(); i++) {
            sb.append(((DBColumn) this.mChooseColumns.get(i)).getName());
            if (i == this.mChooseColumns.size() - 1) {
                sb.append(" ");
            } else {
                sb.append(",");
            }
        }
        // selected columns

        sb.append("from");
        sb.append(" ");
        sb.append(this.mtabName);
        if(where != null && !(where.equals(""))){
	        sb.append(" where ");
	        sb.append(where);
        }
        JdbcQueryGenerator.mLog.log(Level.INFO, "Generated Find Query " + sb.toString());
        return sb.toString();
    }

    /**
     * @return String
     * @throws Exception
     */
    public String createPoolQuery(String where) throws Exception {
        final StringBuffer sb = new StringBuffer();
        /*sb.append("select");
        sb.append(" ");
        for (int i = 0; i < this.mPollColumns.size(); i++) {
            sb.append("\"" + ((DBColumn) this.mPollColumns.get(i)).getName() + "\"");
            if (i == this.mPollColumns.size() - 1) {
                sb.append(" ");
            } else {
                sb.append(",");
            }
        }
        // selected columns
        sb.append("from");
        sb.append(" ");
        sb.append("\"" + this.mSchemaName + "\"");
        sb.append(".");
        sb.append("\"" + this.mtabName + "\"");
        JdbcQueryGenerator.mLog.log(Level.INFO, "Generated Pool Query " + sb.toString());
        return sb.toString();*/
        sb.append("select");
        sb.append(" ");
        for (int i = 0; i < this.mPollColumns.size(); i++) {
            sb.append(((DBColumn) this.mPollColumns.get(i)).getName());
            if (i == this.mPollColumns.size() - 1) {
                sb.append(" ");
            } else {
                sb.append(",");
            }
        }
        // selected columns
        sb.append("from");
        sb.append(" ");
        sb.append(this.mtabName);
        if(where != null && !(where.equals(""))){
	        sb.append(" where ");
	        sb.append(where);
        }
        JdbcQueryGenerator.mLog.log(Level.INFO, "Generated Pool Query " + sb.toString());
        return sb.toString();
    }

    /**
     * @return String
     * @throws Exception
     */
    public String getParamOrder(final String queryType) throws Exception {
        final StringBuffer sb = new StringBuffer();
        List tempList = null;
        if (JdbcQueryGenerator.INSERT_QUERY.equals(queryType)) {
            tempList = this.mInsertColumns;
        }
        if (JdbcQueryGenerator.UPDATE_QUERY.equals(queryType)) {
            tempList = this.mUpdateColumns;
        }
        for (int i = 0; i < tempList.size(); i++) {
        	String ncName = XMLCharUtil.makeValidNCName(((DBColumn) tempList.get(i)).getName());
        	sb.append(ncName);
            if (i == tempList.size() - 1) {
                sb.append("");
            } else {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * @return String
     * @throws Exception
     */
    public String getPrimaryKey() throws Exception {
        String pKey = null;
        final java.util.Iterator pkIter = this.mcolumns.iterator();
        while (pkIter.hasNext()) {
            final DBColumn tc = (DBColumn) pkIter.next();
            if (tc.isPrimaryKey()) {
                pKey = tc.getName();
                break;
            }
        }
        return pKey;
    }
}
