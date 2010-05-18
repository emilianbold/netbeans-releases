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
package org.netbeans.modules.visualweb.dataconnectivity.sql;
import javax.naming.NamingException;

/**
 * This class represents an alias for a DataSource.  It just points to another
 * datasource.  Calls to getter methods delegate to the referenced data source.
 * Calls to setter metnods throw an UnsupportedOperationException.
 * Other methods such as test() and getSchemas(), also delegate to the
 * referenced data source.
 *
 * @author jfbrown
 */
public class DesignTimeDataSourceAlias extends DesignTimeDataSource {

    private DesignTimeDataSourceAlias() {
    }
    public DesignTimeDataSourceAlias( String newAlias ) {
        try {
            dshelper = new DesignTimeDataSourceHelper() ;
        }
        catch (NamingException ne) {
            // should never be here - bad stuff will happen.
            throw new RuntimeException("DesignTimeDataSourceAlias:  InitalConxtext coding error.") ; //NOI18N
        }
        if (newAlias != null && !"".equals(newAlias)) { //NOI18N
            alias = newAlias ;
        }
    }
    DesignTimeDataSourceHelper dshelper ;

    private String alias = null ;

    public void setAlias(String newAlias) {
        if ( newAlias != null && newAlias.equals(alias ) ){
            return ;
        }
        this.alias = newAlias ;
        try {
            save() ;
        }
        catch (NamingException ne) {
            // should never be here.
            RuntimeException ree = new RuntimeException(ne.getLocalizedMessage()) ;
            ree.fillInStackTrace() ;
            throw ree ;
        }
    }
    public String getAlias() {
        return alias ;
    }

    public boolean isValidAlias() {
        // TODO
        return ( getReferencedDataSource() != null ) ;
    }

    /**
     * lookup the referenced datasource in the naming context.
     * If not found, return null.  Callers to this method
     * will have to handle a "null" return value as appropriate.
     */
    public DesignTimeDataSource getReferencedDataSource() {

        DesignTimeDataSource dts = null ;
        try {
            dts = dshelper.getDataSource(alias) ;
        }
        catch ( NamingException ne ) {
            // TODO:  maybe log something, but this isn't necessarily
            // an error if the user deleted the referenced data
            // source.
        }
        return dts ;
    }

    /**
     * utility method for constructing the RuntimeException when setter methods
     * are called.
     */
    private RuntimeException unsupportedMethodCall(String methodName) {
        String retVal = "Unsupported method " + methodName + " on Data Source Alias." ;  //NOI18N
        return new UnsupportedOperationException(retVal) ;
    }

    public void setLogWriter(java.io.PrintWriter out) throws java.sql.SQLException {
        throw unsupportedMethodCall("setLogWriter()") ; //NOI18N
    }

    public void setSchemasInitialized(boolean schemasInitialized) {

        throw unsupportedMethodCall("setSchemasInitialized()") ; //NOI18N
    }

    public void setValidationQuery(String validationQuery) {

        throw unsupportedMethodCall("setValidationQuery()") ; //NOI18N
    }

    public void setValidationTable(String validationTable) {

        throw unsupportedMethodCall("setValidationTable()") ; //NOI18N
    }

    public void setUsername(String username) {

        throw unsupportedMethodCall("setUsername()") ; //NOI18N
    }

    public void setUrl(String url) {

        throw unsupportedMethodCall("setUrl()") ; //NOI18N
    }

    public void addSchema(String schema) {

        throw unsupportedMethodCall("addSchema()") ; //NOI18N
    }

    public void removeSchema(String schema) {

        throw unsupportedMethodCall("removeSchema()") ; //NOI18N
    }

    public void setDriverClassName(String driverClassName) {

        throw unsupportedMethodCall("setDriverClassName()") ; //NOI18N
    }

    public void setPassword(String password) {

        throw unsupportedMethodCall("setPassword()") ; //NOI18N
    }

    public void setLoginTimeout(int seconds) throws java.sql.SQLException {

        throw unsupportedMethodCall("setLoginTimeout()") ; //NOI18N
    }

    public void setSchemas(java.util.Collection schemas) {

        throw unsupportedMethodCall("setSchemas()") ; //NOI18N
    }

    /**
     * used for persisting this instance.
     */
    public String getTag(String key, int level, int tabWidth) {

        return getSpaces(level, tabWidth)
            + "<object name=\"" + escapeXML(key) + "\" class=\"" + getClass().getName() + "\">\n" //NOI18N
        
            + getSpaces(level + 1, tabWidth)
            + "<arg class=\"java.lang.String\"" // NOI18N
            + ((getAlias() == null)? "": " value=\"" + escapeXML(getAlias()) + "\"") // NOI18N
            + "/>\n" // NOI18N

            + getSpaces(level, tabWidth)
            + "</object>\n"; // NOI18N
    }

    public String toString() {
        return "Alias for "+getAlias()+"" ;  //NOI18N
    }

    public boolean test() {
        DesignTimeDataSource dts = getReferencedDataSource() ;
        if ( dts == null ) {
            return false ;
        }
        return dts.test();
    }

    public java.sql.SQLException getTestException() {
        DesignTimeDataSource dts = getReferencedDataSource() ;
        if ( dts == null ) {
            return getReferenceNotFoundException() ;
        } 

        return dts.getTestException();
    }

    public int getTestRowsReturned() {
        DesignTimeDataSource dts = getReferencedDataSource() ;
        if ( dts == null ) {
            return DesignTimeDataSource.SQL_NOT_RUN ;
        }

        return dts.getTestRowsReturned();
    }
    public boolean getTestConnectionSucceeded() {
        DesignTimeDataSource dts = getReferencedDataSource() ;
        if ( dts == null ) {
            return false ;
        }
        return dts.getTestConnectionSucceeded() ;
    }
    
    public boolean getSchemasInitialized() {
        
        DesignTimeDataSource dts = getReferencedDataSource() ;
        if ( dts == null ) {
            return false ;
        }
        return dts.getSchemasInitialized() ;
    }

    /**
     * gets schemas selected for this datasource, an empty set means all schemas
     */
    public String[] getSchemas() {

        DesignTimeDataSource dts = getReferencedDataSource() ;
        if ( dts == null ) {
            return null  ;
        }
        
        return dts.getSchemas() ;
    }

    public String getPassword() {

        DesignTimeDataSource dts = getReferencedDataSource() ;
        if ( dts == null ) {
            return null  ;
        }
        
        return dts.getPassword() ;
    }

    public int getLoginTimeout() throws java.sql.SQLException {

        DesignTimeDataSource dts = getReferencedDataSource() ;
        if ( dts == null ) {
            return 0  ;
        }
        
        return dts.getLoginTimeout() ;
    }

    public java.io.PrintWriter getLogWriter() throws java.sql.SQLException {

        DesignTimeDataSource dts = getReferencedDataSource() ;
        if ( dts == null ) {
            return null  ;
        }
        
        return dts.getLogWriter() ;
    }

    public String getDriverClassName() {

        DesignTimeDataSource dts = getReferencedDataSource() ;
        if ( dts == null ) {
            return null  ;
        }
        
        return dts.getDriverClassName() ;
    }

    public java.sql.Connection getConnection(String username, String password) throws java.sql.SQLException {

        DesignTimeDataSource dts = getReferencedDataSource() ;
        if ( dts == null ) {
            throw getReferenceNotFoundException() ;
        }
        return dts.getConnection(username, password) ;
    }

    public java.sql.Connection getConnection() throws java.sql.SQLException {

        DesignTimeDataSource dts = getReferencedDataSource() ;
        if ( dts == null ) {
            throw getReferenceNotFoundException() ;
        }
        return dts.getConnection() ;
    }

    public void clearSchemas() {
        DesignTimeDataSource dts = getReferencedDataSource() ;
        if ( dts == null ) {
            return ;
        }
        dts.clearSchemas() ;
    }

    public String getUrl() {

        DesignTimeDataSource dts = getReferencedDataSource() ;
        if ( dts == null ) {
            return null  ;
        }
        
        return dts.getUrl() ;
    }

    public String getUsername() {

        DesignTimeDataSource dts = getReferencedDataSource() ;
        if ( dts == null ) {
            return null  ;
        }
        
        return dts.getUsername() ;
    }

    public String getValidationQuery() {

        DesignTimeDataSource dts = getReferencedDataSource() ;
        if ( dts == null ) {
            return null  ;
        }
        
        return dts.getValidationQuery() ;
    }
    
    public String getValidationTable() {

        DesignTimeDataSource dts = getReferencedDataSource() ;
        if ( dts == null ) {
            return null  ;
        }
        
        return dts.getValidationTable() ;
    }
    
    public void initSchemas() throws java.sql.SQLException, javax.naming.NamingException {

        DesignTimeDataSource dts = getReferencedDataSource() ;
        if ( dts == null ) {
            throw getReferenceNotFoundException() ;
        }
        
        dts.initSchemas() ;
    }
    
    /***
     * generate an SQLException that the referenced data source
     * was not found.
     */
    private java.sql.SQLException getReferenceNotFoundException() {
        String msg =  java.text.MessageFormat.format(rb.getString("REFERENCE_NOT_FOUND"), 
                new Object[] {getAlias()} ) ;
        return new java.sql.SQLException( msg ) ;

    }
    
}
