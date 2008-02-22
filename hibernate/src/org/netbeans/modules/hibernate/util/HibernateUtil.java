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


import java.util.ArrayList;

import java.util.Enumeration;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;

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
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataObject;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

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
    throws java.sql.SQLException, HibernateException{
        ArrayList<String> allTables = new ArrayList<String>();
        for(HibernateConfiguration configuration : configurations) {
         //   Configuration hibConfiguration = getHibConfiguration(configuration);
         //   ensureDbDriverLoaded(hibConfiguration);
         //   org.hibernate.SessionFactory hibSessionFactory = hibConfiguration.buildSessionFactory();
         //   org.hibernate.Session hibSession = hibSessionFactory.openSession();
            java.sql.Connection jdbcConnection = getJDBCConnection(configuration); //hibSession.connection();
            java.sql.DatabaseMetaData dbMetadata = jdbcConnection.getMetaData();
            java.sql.ResultSet rs = dbMetadata.getTables(null,
                    getDatabaseSchema(configuration),
                    null, new String[]{"TABLE"}); //NOI18N
            while(rs.next()) {
                allTables.add(rs.getString(3)); // COLUMN 3 stores the table names.
            }
        }
        return allTables;
    }
    
    public static ArrayList<HibernateConfiguration> getAllHibernateConfigurations(Project project) {
        ArrayList<HibernateConfiguration> configFiles = new ArrayList<HibernateConfiguration>();
        Sources projectSources = ProjectUtils.getSources(project);
        SourceGroup[] javaSourceGroup = projectSources.getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA
                );
        
        for(SourceGroup sourceGroup : javaSourceGroup) {
            FileObject root = sourceGroup.getRootFolder();
            Enumeration<? extends FileObject> enumeration = root.getChildren(true);
            while(enumeration.hasMoreElements()) {
                FileObject fo = enumeration.nextElement();
                if(fo.getNameExt() != null && fo.getNameExt().endsWith("cfg.xml")) {
                    try {
                        configFiles.add(
                                (
                                (HibernateCfgDataObject) DataObject.find(fo)
                                ).getHibernateConfiguration()
                                );
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        return configFiles;
    }

    public static ArrayList<FileObject> getAllHibernateConfigFileObjects(Project project) {
        ArrayList<FileObject> configFiles = new ArrayList<FileObject>();
        Sources projectSources = ProjectUtils.getSources(project);
        SourceGroup[] javaSourceGroup = projectSources.getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA
                );
        
        for(SourceGroup sourceGroup : javaSourceGroup) {
            FileObject root = sourceGroup.getRootFolder();
            Enumeration<? extends FileObject> enumeration = root.getChildren(true);
            while(enumeration.hasMoreElements()) {
                FileObject fo = enumeration.nextElement();
                if(fo.getNameExt() != null && fo.getNameExt().endsWith("cfg.xml")) {
                        configFiles.add(fo);
                }
            }
        }
        return configFiles;
    }
    
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
                if(fo.getNameExt() != null && fo.getNameExt().endsWith("hbm.xml")) {
                        mappingFiles.add(fo);
                }
            }
        }
        return mappingFiles;
    }
    
    
    private static String getDbConnectionDetails(HibernateConfiguration configuration, String property) {
       // try {
        //configuration.getClass().getClassLoader().getParent().loadClass("org.apache.commons.logging.Log");
          //  Configuration hibConfiguration = /* null  ;*/new Configuration();
           // Class clazz = Class.forName("org.hibernate.cfg.Configuration", 
            //        true, configuration.getClass().getClassLoader()); //ClassLoader.getSystemClassLoader());
           // hibConfiguration = (Configuration)clazz.newInstance();
            SessionFactory fact = configuration.getSessionFactory();
            int count = 0;
            for (String val : fact.getProperty2()) {
                String propName = fact.getAttributeValue(fact.PROPERTY2, count++, "name"); 
                if(propName.equals(property)) {
                    return val;
                }
            }

            return "";
       // } catch (ClassNotFoundException ex) {
      //      Exceptions.printStackTrace(ex);
      //  } 
      //  return null;
    }

    private static String getDatabaseSchema(HibernateConfiguration configuration) {
        String dbSchema = null;

        SessionFactory fact = configuration.getSessionFactory();
        int count = 0;
        for(String val : fact.getProperty2()) {
            String propName = fact.getAttributeValue(fact.PROPERTY2,
                    count++, "name"); //NOI18N
            if(propName.equals("hibernate.connection.url") || //NOI18N
                    propName.equals("connection.url")) { //NOI18N
                dbSchema = val.substring(
                        val.lastIndexOf("/") + 1
                        );
            }
        }
        return dbSchema;
    }
    
     private static java.sql.Connection getJDBCConnection(HibernateConfiguration configuration) {
        try {
            for (Library lib : LibraryManager.getDefault().getLibraries()) {
                System.out.println("lib name : " + lib.getName());
                System.out.println("libdesce : " + lib.getDescription());
                System.out.println("lib type : " + lib.getType());
            }
            String driverClassName = getDbConnectionDetails(configuration, "hibernate.connection.driver_class");
            String driverURL = getDbConnectionDetails(configuration, "hibernate.connection.url");
            String username = getDbConnectionDetails(configuration, "hibernate.connection.username");
            String password = getDbConnectionDetails(configuration, "hibernate.connection.password");
            //Hibernate allows abbrivated properties
            if (driverClassName == null) {
                driverClassName = getDbConnectionDetails(configuration, "connection.driver_class");
            }
            if (driverURL == null) {
                driverURL = getDbConnectionDetails(configuration, "connection.url");
            }
            if (username == null) {
                username = getDbConnectionDetails(configuration, "connection.username");
            }
            if (password == null) {
                password = getDbConnectionDetails(configuration, "connection.password");
            }

            JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers(driverClassName);
//        for(JDBCDriver driver : drivers) {
//            try {
//                System.out.println("driverClassName ; " + driverClassName);
//                System.out.println("driver.getClassname() : " + driver.getClassName());
//                Class.forName(driver.getClassName());
//            }catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
            DatabaseConnection dbConnection = DatabaseConnection.create(drivers[0], driverURL, username, null, password, true);
            ConnectionManager.getDefault().addConnection(dbConnection);
            if(dbConnection.getJDBCConnection() == null ) {
                ConnectionManager.getDefault().showConnectionDialog(dbConnection);
                Thread.sleep(4000);
            }
            return dbConnection.getJDBCConnection();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (DatabaseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

}
