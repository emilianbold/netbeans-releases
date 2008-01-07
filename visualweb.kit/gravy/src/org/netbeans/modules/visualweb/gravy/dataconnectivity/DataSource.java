/*
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
*
* Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.visualweb.gravy.dataconnectivity;

/**
 * This class implements an object DataSource, which is used for storing
 *  of various parameters of a datasource.
 */
public class DataSource {
    private String dbType="", dbUrl="", dbHost="", dbName="", hostName="", 
                   user="", password="", port="", validationTable=null;

    /** 
     * Returns a type of database.
     * @return string with a type of database. 
     */
    public String getDbType() {
        return dbType;
    }

    /** 
     * Returns a database URL.
     * @return string with a database URL. 
     */
    public String getDbUrl() {
        return dbUrl;
    }

    /** 
     * Returns a database host.
     * @return string with a database host. 
     */
    public String getDbHost() {
        return dbHost;
    }

    /** 
     * Returns a database name.
     * @return string with a database name. 
     */
    public String getDbName() {
        return dbName;
    }

    /** 
     * Returns a host name.
     * @return string with a host name. 
     */
    public String getHostName() {
        return hostName;
    }

    /** 
     * Returns an user name.
     * @return string with an user name. 
     */
    public String getUser() {
        return user;
    }

    /** 
     * Returns a password.
     * @return string with a password. 
     */
    public String getPassword() {
        return password;
    }

    /** 
     * Returns a database port number.
     * @return string with a port number. 
     */
    public String getPort() {
        return port;
    }

    /** 
     * Returns a database host.
     * @return string with a database host. 
     */
    public String getValidationTable() {
        return validationTable;
    }

    /**
     * Creates an instance of this class.
     * @param p_dbHost string with a database host
     * @param p_dbName string with a database name
     * @param p_user string with an user name
     * @param p_password string with an user password
     * @param p_port string with a database port
     */
    public DataSource(String p_dbHost, String p_dbName, String p_user, String p_password, String p_port){
        this("",p_dbHost,p_dbName,p_user,p_password,p_port);
    }

    /**
     * Creates an instance of this class.
     * @param p_dbType string with a database type
     * @param p_dbHost string with a database host
     * @param p_dbName string with a database name
     * @param p_user string with an user name
     * @param p_password string with an user password
     * @param p_port string with a database port
     */
    public DataSource(String p_dbType, String p_dbHost, String p_dbName, String p_user, String p_password, String p_port){
        this(p_dbType,p_dbHost,p_dbName,p_user,p_password,p_port,null);
    }

    /**
     * Creates an instance of this class.
     * @param p_dbType string with a database type
     * @param p_dbHost string with a database host
     * @param p_dbName string with a database name
     * @param p_user string with an user name
     * @param p_password string with an user password
     * @param p_port string with a database port
     * @param p_validationTable
     */
    public DataSource(String p_dbType, String p_dbHost, String p_dbName, String p_user, String p_password, String p_port, String p_validationTable){
        setDbType(p_dbType);
        setDbHost(p_dbHost);
        setDbName(p_dbName);
        setUser(p_user);
        setPassword(p_password);
        setPort(p_port);
        setValidationTable(p_validationTable);
        setDbUrl("");
        //@todo parse dbUrl
    }

    /**
     * Creates an instance of this class.
     * @param p_ds an another DataSource object (a prototype of new datasource)
     */
    public DataSource(DataSource p_ds){
        setDbType(p_ds.getDbType());
        setDbHost(p_ds.getDbHost());
        setDbName(p_ds.getDbName());
        setUser(p_ds.getUser());
        setPassword(p_ds.getPassword());
        setPort(p_ds.getPort());
        setDbUrl(p_ds.getDbUrl());
        setValidationTable(p_ds.getValidationTable());
    }

    /**
     * Changes a database type for a datasource.
     * @param dbType string with a database type
     */
    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    /**
     * Changes a database URL for a datasource.
     * @param dbUrl string with a database URL
     */
    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    /**
     * Changes a database host for a datasource.
     * @param dbHost string with a database host
     */
    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    /**
     * Changes a database name for a datasource.
     * @param dbName string with a database name
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * Changes a host name for a datasource.
     * @param hostName string with a host name
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Changes an user name for a datasource.
     * @param user an user name
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Changes a password for a datasource.
     * @param password an user password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Changes a database port number for a datasource.
     * @param port a database port number
     */
    public void setPort(String port) {
        this.port = port;
    }

    public void setValidationTable(String validationTable) {
        this.validationTable = validationTable;
    }
}
