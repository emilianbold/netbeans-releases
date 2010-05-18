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
package org.netbeans.modules.hibernate.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.project.uiapi.ProjectOpenedTrampoline;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataLoader;

/**
 * This class provides utility methods for test cases.
 *
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class Util {

    public static boolean startDB(String dbHome) {
        try {
            String java = System.getProperty("java.home") + File.separator +
                    "bin" + File.separator + "java";
            NbProcessDescriptor desc = new NbProcessDescriptor(
                    java,
                    "-Dderby.system.home=\"" + dbHome + "\" " +
                    "-classpath \"" + getNetworkServerClasspath(dbHome) + "\"" +
                    " org.apache.derby.drda.NetworkServerControl start");

            Process process = desc.exec(null, new String[]{"DERBY_INSTALL=" + dbHome}, true,
                    new File(dbHome));
            Thread.sleep(4000);
            return true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    static void clearDB() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            System.out.println("Loaded the appropriate driver.");

            Connection conn = null;
            Properties props = new Properties();
            props.put("user", "user1");
            props.put("password", "user1");

            conn = DriverManager.getConnection("jdbc:derby://localhost:1527/" + "derbyDB", props);

            System.out.println("Connected to and cleared derbyDB");

            Statement s = conn.createStatement();

            s.execute("drop table derbyDB1");
            s.execute("drop table derbyDB2");
            s.execute("drop table derbyDB3");
            s.execute("drop table derbyDB4");
            s.execute("drop table derbyDB5");
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    static ArrayList<String> getAllDatabaseTables() {

        try {
            ArrayList<String> tables = new ArrayList<String>();
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            System.out.println("Loaded the appropriate driver.");

            Connection conn = null;
            Properties props = new Properties();
            props.put("user", "user1");
            props.put("password", "user1");

            conn = DriverManager.getConnection("jdbc:derby://localhost:1527/" + "derbyDB", props);

            java.sql.DatabaseMetaData dbMetadata = conn.getMetaData();
            java.sql.ResultSet rs = dbMetadata.getTables("", "derbyDB", null, new String[]{"TABLE"}); //NOI18N
            while (rs.next()) {
                tables.add(rs.getString(3)); // COLUMN 3 stores the table names.
            }

            System.out.println("Loaded tables from derbyDB");


            return tables;
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    static void prepareDB() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            System.out.println("Loaded the appropriate driver.");

            Connection conn = null;
            Properties props = new Properties();
            props.put("user", "user1");
            props.put("password", "user1");

            /*
            The connection specifies create=true to cause
            the database to be created. To remove the database,
            remove the directory derbyDB and its contents.
            The directory derbyDB will be created under
            the directory that the system property
            derby.system.home points to, or the current
            directory if derby.system.home is not set.
             */
            conn = DriverManager.getConnection("jdbc:derby://localhost:1527/" + "derbyDB;create=true", props);

            System.out.println("Connected to and created database derbyDB");

            Statement s = conn.createStatement();

            s.execute("create table derbyDB1(num int, addr varchar(40))");
            s.execute("create table derbyDB2(num int, addr varchar(40))");
            s.execute("create table derbyDB3(num int, addr varchar(40))");
            s.execute("create table derbyDB4(num int, addr varchar(40))");
            s.execute("create table derbyDB5(num int, addr varchar(40))");

            System.out.println("Created tables derbyDB[1-5]");
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static String getNetworkServerClasspath(String dbHome) {
        return dbHome + File.separator + "lib/derby.jar" + File.pathSeparator +
                dbHome + File.separator + "lib/derbytools.jar" + File.pathSeparator +
                dbHome + File.separator + "lib/derbynet.jar"; // NOI18N
    }

    public static boolean stopDB(String dbHome) {
        try {
            String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            NbProcessDescriptor desc = new NbProcessDescriptor(java, "-Dderby.system.home=\"" + dbHome + "\" " + "-classpath \"" + getNetworkServerClasspath(dbHome) + "\"" + " org.apache.derby.drda.NetworkServerControl shutdown");

            Process process = desc.exec(null, new String[]{"DERBY_INSTALL=" + dbHome}, true, new File(dbHome));
            Thread.sleep(4000);
            return true;
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    public static Project getProject(File projFolder) {
        try {
            FileObject pfo = FileUtil.toFileObject(projFolder);

            Project project = ProjectManager.getDefault().findProject(pfo);
            //open the project here !!
            openProject(project);

            return project;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static ArrayList<FileObject> getConfigFiles(Project project) {
        ArrayList<FileObject> files = new ArrayList<FileObject>();
        Sources projectSources = ProjectUtils.getSources(project);
        SourceGroup[] javaSourceGroup = projectSources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
        if (javaSourceGroup == null || javaSourceGroup.length == 0) {
            javaSourceGroup = projectSources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        }
        for (SourceGroup sourceGroup : javaSourceGroup) {
            FileObject root = sourceGroup.getRootFolder();
            Enumeration<? extends FileObject> enumeration = root.getChildren(true);
            while (enumeration.hasMoreElements()) {
                FileObject fo = enumeration.nextElement();
                if(fo != null && fo.getMIMEType().equals(HibernateCfgDataLoader.REQUIRED_MIME)) {
                    files.add(fo);
                }
            }
        }
        return files;
    }

    private static void openProject(Project project) {
        ProjectOpenedHook hook = project.getLookup().lookup(ProjectOpenedHook.class);
        if (hook == null) {
            System.out.println(" project open hook is null ");
        }
        ProjectOpenedTrampoline.DEFAULT.projectOpened(hook);
    }
    
    public static URL[] getDBDriverFiles(String driverLocation) {
        File clientDriverFile = new File(driverLocation + 
                java.io.File.separator + 
                "lib" + 
                java.io.File.separator + 
                "derbyClient.jar");
        ArrayList<URL> urls = new ArrayList<URL>();
        try {
        
        urls.add(clientDriverFile.toURL());
        }catch(MalformedURLException e) {
            e.printStackTrace();
        }
        return urls.toArray(new URL[]{}); 
    }
}
