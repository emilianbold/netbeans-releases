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

package org.netbeans.modules.hibernate.util;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.Enumeration;

import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataObject;
import org.netbeans.modules.hibernate.service.TableColumn;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;

/**
 * This class provides utility methods using Hibernate API to query database
 * based on the configurations setup in the project.
 *
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class HibernateUtil {

    /**
     * This methods gets all database tables from the supplied Hibernate Configurations.
     * Note : This class uses a deprecated method, that will be replaced in future.
     *
     * @param configurations hibernate configrations used to connect to the DB.
     * @return arraylist of strings of table names.
     * @throws java.sql.SQLException
     */
    public static ArrayList<String> getAllDatabaseTables(HibernateConfiguration... configurations)
    throws java.sql.SQLException{
        ArrayList<String> allTables = new ArrayList<String>();
        for(HibernateConfiguration configuration : configurations) {
            try {
                DatabaseConnection dbConnection = getDBConnection(configuration);
                java.sql.Connection jdbcConnection = dbConnection.getJDBCConnection();
                java.sql.DatabaseMetaData dbMetadata = jdbcConnection.getMetaData();
                java.sql.ResultSet rsSchema = dbMetadata.getSchemas();
                if (rsSchema.next()) {
                    do {
                        java.sql.ResultSet rs = dbMetadata.getTables(null, rsSchema.getString("TABLE_SCHEM"), null, new String[]{"TABLE"}); //NOI18N
                        while (rs.next()) {
                            allTables.add(rs.getString("TABLE_NAME")); 
                        }
                    } while (rsSchema.next());
                } else {
                    // Getting tables from default schema.
                    java.sql.ResultSet rs = dbMetadata.getTables(null, dbConnection.getSchema(), null, new String[]{"TABLE"}); //NOI18N
                    while (rs.next()) {
                        allTables.add(rs.getString("TABLE_NAME")); //NOI18N
                    }
                }
            } catch (DatabaseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return allTables;
    }
    
    /**
     * Constructs HibernateConfiguration (schema2beans) objects for each of the cfg 
     * file under this project. 
     * 
     * @param project the project for which HibernateConfigurations need to be constructed.
     * @return list of HibernateConfiguration objects or an empty list of none found.
     */
    public static ArrayList<HibernateConfiguration> getAllHibernateConfigurations(Project project) {
        ArrayList<HibernateConfiguration> configFiles = new ArrayList<HibernateConfiguration>();
        for(FileObject fo : getAllHibernateConfigFileObjects(project)) {
            try {
                configFiles.add(((HibernateCfgDataObject) DataObject.find(fo)).getHibernateConfiguration());
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return configFiles;
    }

    /**
     * Seaches cfg FileObjects under the given projects and returns them.
     * 
     * @param project the project for which HIbernate configuration files need to be searched.
     * @return list of HibernateConfiguration FileObjects or an empty list of none found.
     */
    public static ArrayList<FileObject> getAllHibernateConfigFileObjects(Project project) {
        ArrayList<FileObject> configFiles = new ArrayList<FileObject>();
        Sources projectSources = ProjectUtils.getSources(project);
        SourceGroup[] javaSourceGroup = projectSources.getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA
                );
        
        for(SourceGroup sourceGroup : javaSourceGroup) {
            FileObject root = sourceGroup.getRootFolder();
            Enumeration<? extends FileObject> enumeration = root.getChildren(false);
            while(enumeration.hasMoreElements()) {
                FileObject fo = enumeration.nextElement();
                if(fo.getNameExt() != null && fo.getNameExt().endsWith("cfg.xml")) { //NOI18N
                        configFiles.add(fo);
                }
            }
        }
        return configFiles;
    }
    
    /**
     * Seaches mapping files under the given project and returns the list of 
     * FileObjects if found.
     * 
     * @param project the project for whcih the mapping files are to be found.
     * @return list of FileObjects of actual mapping files.
     */
    public static ArrayList<FileObject> getAllHibernateMappingFileObjects(Project project) {
        ArrayList<FileObject> mappingFiles = new ArrayList<FileObject>();
        Sources projectSources = ProjectUtils.getSources(project);
        SourceGroup[] javaSourceGroup = projectSources.getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA
                );
        
        for(SourceGroup sourceGroup : javaSourceGroup) {
            FileObject root = sourceGroup.getRootFolder();
            Enumeration<? extends FileObject> enumeration = root.getChildren(true);
            while(enumeration.hasMoreElements()) {
                FileObject fo = enumeration.nextElement();
                if(fo.getNameExt() != null && fo.getNameExt().endsWith("hbm.xml")) { //NOI18N
                        mappingFiles.add(fo);
                }
            }
        }
        return mappingFiles;
    }

    /**
     * Seaches mapping files under the given project and returns the list of 
     * mapping files relative to the source path. This method is intendeed to be 
     * used in code completion of mapping files in config files.
     * 
     * @param project the project for whcih the mapping files are to be found.
     * @return list of relative paths of actual mapping files.
     */
    public static ArrayList<String> getAllHibernateMappingsRelativeToSourcePath(Project project) {
        ArrayList<String> mappingFiles = new ArrayList<String>();
        Sources projectSources = ProjectUtils.getSources(project);
        SourceGroup[] javaSourceGroup = projectSources.getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA
                );
        
        for(SourceGroup sourceGroup : javaSourceGroup) {
            FileObject root = sourceGroup.getRootFolder();
            Enumeration<? extends FileObject> enumeration = root.getChildren(true);
            while(enumeration.hasMoreElements()) {
                FileObject fo = enumeration.nextElement();
                if(fo.getNameExt() != null && fo.getNameExt().endsWith("hbm.xml")) { //NOI18N
                        mappingFiles.add(
                                getRelativeSourcePath(fo, root)
                                );
                }
            }
        }
        return mappingFiles;
    }
    
    /**
     * Returns Column information for the given table defined under the given 
     * configuration.
     * 
     * @param tableName the tablename.
     * @param hibernateConfiguration the database configuration to be used.
     * @return
     */
    public static ArrayList<TableColumn> getColumnsForTable(String tableName, HibernateConfiguration hibernateConfiguration) {
        ArrayList<TableColumn> columnNames = new ArrayList<TableColumn>();
        
        try {
            java.sql.Connection connection = getJDBCConnection(hibernateConfiguration);
            java.sql.Statement stmt = connection.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName); //NOI18N
            java.sql.ResultSetMetaData rsMetadata = rs.getMetaData();
            java.sql.DatabaseMetaData dbMetadata = connection.getMetaData();
            java.sql.ResultSet rsDBMetadata = dbMetadata.getPrimaryKeys(null, null, tableName);
            ArrayList<String> primaryColumns = new ArrayList<String>();
            while(rsDBMetadata.next()) {
               primaryColumns.add(rsDBMetadata.getString("COLUMN_NAME")); //NOI18N
            }
            for (int i = 1; i <= rsMetadata.getColumnCount(); i++) {
                TableColumn tableColumn = new TableColumn();
                tableColumn.setColumnName(rsMetadata.getColumnName(i));
                if(primaryColumns.contains(tableColumn.getColumnName())) {
                    tableColumn.setPrimaryKey(true);
                }
                columnNames.add(tableColumn);
            }

        } catch (DatabaseException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SQLException sQLException) {
            Exceptions.printStackTrace(sQLException);
        }

        return columnNames;
    }
    
    
    
    private static String getDbConnectionDetails(HibernateConfiguration configuration, String property) {
        SessionFactory fact = configuration.getSessionFactory();
        int count = 0;
        for (String val : fact.getProperty2()) { 
            @SuppressWarnings("static-access") //NOI18N
            String propName = fact.getAttributeValue(fact.PROPERTY2, count++, "name");  //NOI18N
            if(propName.equals(property)) {
                return val;
            }
        }

        return ""; //NOI18N
    }

    private static DatabaseConnection getDBConnection(HibernateConfiguration configuration)
        throws DatabaseException {
        try {

            String driverClassName = getDbConnectionDetails(configuration, "hibernate.connection.driver_class"); //NOI18N
            String driverURL = getDbConnectionDetails(configuration, "hibernate.connection.url"); //NOI18N
            String username = getDbConnectionDetails(configuration, "hibernate.connection.username"); //NOI18N
            String password = getDbConnectionDetails(configuration, "hibernate.connection.password"); //NOI18N
            //Hibernate allows abbrivated properties
            if (driverClassName == null) {
                driverClassName = getDbConnectionDetails(configuration, "connection.driver_class"); //NOI18N
            }
            if (driverURL == null) {
                driverURL = getDbConnectionDetails(configuration, "connection.url"); //NOI18N
            }
            if (username == null) {
                username = getDbConnectionDetails(configuration, "connection.username"); //NOI18N
            }
            if (password == null) {
                password = getDbConnectionDetails(configuration, "connection.password"); //NOI18N
            }

            JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers(driverClassName);
            //TODO check the driver here... it might not be loaded and driver[0] might result in AIOOB exception
//            while(drivers.length == 0) {
//                // Unable to load the driver. 
//                Mutex.EVENT.readAccess(new Mutex.Action<Object>() {
//
//                    public Object run() {
//                        JDBCDriverManager.getDefault().showAddDriverDialog();
//                        return new Object();
//                    }
//                    
//                });
//                drivers = JDBCDriverManager.getDefault().getDrivers(driverClassName);
//            }
            final DatabaseConnection dbConnection = DatabaseConnection.create(drivers[0], driverURL, username, null, password, true);
            ConnectionManager.getDefault().addConnection(dbConnection);
            if(dbConnection.getJDBCConnection() == null ) {
                return Mutex.EVENT.readAccess(new Mutex.Action<DatabaseConnection>() {

                    public DatabaseConnection run() {
                        ConnectionManager.getDefault().showConnectionDialog(dbConnection);
                        return dbConnection;
                    }
                });
                
            }
            return dbConnection;
        } catch (DatabaseException ex) {
            Exceptions.printStackTrace(ex);
            throw ex;
        }
    }
     
    public static String getRelativeSourcePath(FileObject file, FileObject sourceRoot) {
        String relativePath = "";
        try{
            String absolutePath = file.getPath();
            String sourceRootPath = sourceRoot.getPath();
            int index = absolutePath.indexOf(sourceRootPath);
            relativePath = absolutePath.substring(index + sourceRootPath.length() + 1);
        } catch(Exception e) {
          System.out.println("exception while parsing relative path " + e);  
        }
        return relativePath;
    }

    private static Connection getJDBCConnection(HibernateConfiguration hibernateConfiguration) throws DatabaseException {
        return getDBConnection(hibernateConfiguration).getJDBCConnection();
    }

}
