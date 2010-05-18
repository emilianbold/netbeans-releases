/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.visualweb.dataconnectivity.model;

import org.netbeans.modules.visualweb.dataconnectivity.sql.DatabaseMetaDataHelper;
import org.netbeans.modules.visualweb.dataconnectivity.sql.DesignTimeDataSource;
import org.netbeans.modules.visualweb.dataconnectivity.sql.DesignTimeDataSourceAlias;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Wrapper class, helps pass around info related to a database metadata
 * and the name associated with the data source
 * @author Octavian Tanase, Winston Prakash
 */
// XXX - This class should just extend DesigntimeDatasource - WJP
public class DataSourceInfo {
    /** Data Source name */
    private String name;
    /** */
    private DesignTimeDataSource dataSource;
    /** A handle to the database metadata */
    private DatabaseMetaDataHelper dbmdh;

    String dataSourceId;

    Set listeners = new HashSet();

    boolean connected = false;
    boolean connectionTested = false;

    String connectionMsg = null;

    long connectionTimeout = 30000;

    public DataSourceInfo(String name, DesignTimeDataSource dataSource) {
        this.name = name;
        setDataSource(dataSource);
        dbmdh = null;
//        setId(DataSourceInfoManager.getInstance().getUniqueDataSourceId());

        // Used as tooltip in the Datasource node
        if ( isAlias() ) {
            connectionMsg = "Alias to " + getAlias() + "(" + connectionMsg + ")" ;
            connectionMsg = NbBundle.getMessage(DataSourceInfo.class, "CONNECTION_MSG_FOR_ALIAS", getAlias(), getUrl() ) ;
        }
        else {
            connectionMsg = getUrl();
        }
    }

    public DataSourceInfo(String name, String driverClassName, String url, String validationQuery,
    String username, String password) {
        this.name = name;
        setDataSource(new DesignTimeDataSource(null, false, driverClassName,url,
        validationQuery, username, password) );
        dbmdh = null;
//        setId(DataSourceInfoManager.getInstance().getUniqueDataSourceId());
    }

    public DataSourceInfo(String name, String aliasName) {
        this.name = name;
        setDataSource(new DesignTimeDataSourceAlias(aliasName) );
        dbmdh = null;
//        setId(DataSourceInfoManager.getInstance().getUniqueDataSourceId());
    }

    private void setDataSource( DesignTimeDataSource dtds ) {
        this.dataSource = dtds ;
        if ( isAlias()) {
            alias = ((DesignTimeDataSourceAlias)dtds).getAlias() ;
        }
    }
    private String alias = null ;
    private String previousAlias = null ;

    public String getAlias() {
        if (isAlias()) return ((DesignTimeDataSourceAlias)dataSource).getAlias() ;
        else return null ;
    }
    public boolean isAlias() {
        return this.dataSource instanceof DesignTimeDataSourceAlias ;
    }
    public void setAlias(String newAlias) {
        previousAlias = ((DesignTimeDataSourceAlias)dataSource).getAlias() ;
        ((DesignTimeDataSourceAlias)dataSource).setAlias(newAlias) ;
    }
    public void swapAlias() {
        ((DesignTimeDataSourceAlias)dataSource).setAlias(previousAlias) ;
    }

    /***
     * Compare two datasources for equal Info
     * Names can be different.
     * Useful for comparing an alias to a non-alias.
     * return true if the values for name, username, etc. are the same.
     */
    public boolean isInfoEqual( DataSourceInfo dsi ) {

        if ( this.getUsername() == null ) {
            if ( dsi.getUsername() != null ) return false ;
        } else {
            if ( ! this.getUsername().equals(dsi.getUsername())) return false ;
        }
        if ( this.getPassword() == null ) {
            if ( dsi.getPassword() != null ) return false ;
        } else {
            if ( ! this.getPassword().equals(dsi.getPassword())) return false ;
        }
        if ( this.getUrl() == null ) {
            if ( dsi.getUrl() != null ) return false ;
        } else {
            if ( ! this.getUrl().equals(dsi.getUrl())) return false ;
        }
        if ( this.getValidationTable() == null ) {
            if ( dsi.getValidationTable() != null ) return false ;
        } else {
            if ( ! this.getValidationTable().equals(dsi.getValidationTable())) return false ;
        }
        if ( this.getDriverClassName() == null ) {
            if ( dsi.getDriverClassName() != null ) return false ;
        } else {
            if ( ! this.getDriverClassName().equals(dsi.getDriverClassName())) return false ;
        }

        return true ;
    }
    
    public String toString() {
        String retval = "Name: " + this.name
            + "\nUrl: " + this.getUrl() 
            + "\nUser: " + this.getUsername() 
            ;
        return retval ;
    }
    public void addConnectionListener(DatasourceConnectionListener listener){
        listeners.add(listener);
    }
    
    public void removeConnectionListener(DatasourceConnectionListener listener){
        listeners.remove(listener);
    }
    
    public boolean containsSchema(String schemaName){
        boolean found = false;
        String[] schemas = getSchemas();
        if(schemas != null){
            for(int i=0; i< schemas.length; i++){
                if (schemas[i].equals(schemaName)){
                    found = true;
                }
            }
        }
        return found;
    }
    
    public void initSchemas() throws SQLException, NamingException{
        dataSource.initSchemas();
    }
    
    public String[] getSchemas() {
        return dataSource.getSchemas();
    }
    
    public void setSchemas(String[] schemas) {
        dataSource.setSchemas(Arrays.asList(schemas));
    }

    public void clearSchemas() {
        dataSource.clearSchemas();
    }

    public void setSchemasInitialized(boolean schemasInitialized) {
        dataSource.setSchemasInitialized(schemasInitialized);
    }
    
    public void addSchema(String schema) {
        dataSource.addSchema(schema);
    }
    
    public void removeSchema(String schema) {
        dataSource.removeSchema(schema);
    }
    
    public String getId(){
        return dataSourceId;
    }
    
    public void setId(String id){
        dataSourceId = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDriverClassName() {
        return dataSource.getDriverClassName();
    }
    
    public void setDriverClassName(String driverClassName) {
        dataSource.setDriverClassName(driverClassName);
    }
    
    public String getUrl() {
        return dataSource.getUrl();
    }
    
    public void setUrl(String url) {
        dataSource.setUrl(url);
    }
    
    public String getUsername() {
        return dataSource.getUsername();
    }
    
    public void setUsername(String username) {
        dataSource.setUsername(username);
    }
    
    public String getPassword() {
        return dataSource.getPassword();
    }
    
    public void setPassword(String password) {
        dataSource.setPassword(password);
    }
    
    public String getValidationQuery() {
        return dataSource.getValidationQuery();
    }
    
    public void setValidationQuery(String valQuery) {
        dataSource.setValidationQuery(valQuery);
    }
    public void setValidationTable(String valTable) {
        dataSource.setValidationTable(valTable) ;
    }
    public String getValidationTable() {
        return dataSource.getValidationTable() ;
    }
    
    public DatabaseMetaDataHelper getDatabaseMetaDataHelper() {
        if (dbmdh == null) {
            try {
                dbmdh = new DatabaseMetaDataHelper(dataSource);
            } catch (SQLException e) {
            }
        }
        return dbmdh;
    }
    
    public DesignTimeDataSource getDataSource() {
        return dataSource;
    }
    
    
    public boolean testConnection(){
        return testConnection(connectionTimeout);
    }
    
    public boolean testConnection(long timeOut){
        TestConnectionResults retVal = testConnectionAndValidation(timeOut) ;
        return retVal.connected ;
    }
    
    private TestConnectionResults lastTestConnectionResults = null ;
    public TestConnectionResults getLastTestResults() {
        if (lastTestConnectionResults == null ) return null ;
        return lastTestConnectionResults.cloneResults() ;
    }
    public TestConnectionResults testConnectionAndValidation(){
        return testConnectionAndValidation(connectionTimeout);
    }
    public TestConnectionResults testConnectionAndValidation(long timeOut){
        lastTestConnectionResults = null ;
        connectionTested = true;
        Thread connectionTestThread = new Thread(new Runnable() {
            public void run() {
                connected = dataSource.test();
            }
        });
        connectionTestThread.start();
        
        TestConnectionResults retVal = new TestConnectionResults() ;
        
        try{
            connectionTestThread.join(timeOut);
            if (connectionTestThread.isAlive()){
                retVal.connected = false ;
                retVal.sqlException =  NbBundle.getMessage(DataSourceInfo.class, "CONNECTION_TIMEDOUT_MSG", getUrl());
                retVal.rows = DesignTimeDataSource.SQL_NOT_RUN ;
                return retVal;
            }
        }catch(InterruptedException exc){
            retVal.connected = false ;
            retVal.sqlException = NbBundle.getMessage(DataSourceInfo.class, "CONNECTION_INTERRUPTED", getUrl());
            retVal.rows = DesignTimeDataSource.SQL_NOT_RUN ;
            return retVal ;
        }
        retVal.connected = dataSource.getTestConnectionSucceeded() ;
        retVal.sqlException = dataSource.getTestException() == null ? null : dataSource.getTestException().getMessage() ;
        retVal.rows = dataSource.getTestRowsReturned() ;
        
        Iterator iter = listeners.iterator();
        while(iter.hasNext()) {
            ((DatasourceConnectionListener)iter.next()).dataSourceConnectionModified();
        }
        lastTestConnectionResults = retVal ;
        return retVal.cloneResults() ;
    }
    
    public static class TestConnectionResults {
        public boolean connected = false ;
        public String sqlException = null ;
        public SQLException sqlExceptionRootCause = null ;
        public int rows = DesignTimeDataSource.SQL_NOT_RUN ;
        
        public TestConnectionResults cloneResults() {
            TestConnectionResults x = new TestConnectionResults() ;
            x.connected = this.connected ;
            x.rows = this.rows ;
            x.sqlException = this.sqlException ;
            x.sqlExceptionRootCause = this.sqlExceptionRootCause ;
            return x ;
        }
    }
        
    public void reset() {
        getDatabaseMetaDataHelper().refresh() ;
        testConnectionAndValidation() ;
    } 
    public boolean isConnected(){
        return connected;
    }
    
    public boolean isConnectionTested(){
        return connectionTested;
    }
    
    public String getConnectionMsg(){
        return connectionMsg;
    }
    
    public void setDbmdh(DatabaseMetaDataHelper dbmdh) {
        this.dbmdh = dbmdh;
    }
}
