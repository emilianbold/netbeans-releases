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
package org.netbeans.modules.ruby.railsprojects.database;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.ruby.railsprojects.ui.customizer.RailsProjectProperties;
import org.netbeans.modules.ruby.rubyproject.ProjectPropertyExtender;
import org.netbeans.modules.ruby.rubyproject.ProjectPropertyExtender.Item;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;

/**
 * Represents a JDBC database configuration for a Rails project. Encapsulates
 * the connections for all three environments.
 *
 * @author Erno Mononen
 */
public class RailsJdbcConnection extends RailsDatabaseConfiguration {

    private final DatabaseConnection development;
    private final DatabaseConnection test;
    private final DatabaseConnection production;

    /**
     * Creates a new JBDC database configuration.
     * 
     * @param development the connection for the developement environment; may be <code>null</code>.
     * @param test the connection for the test environment; may be <code>null</code>.
     * @param production the connection for the production environment; may be <code>null</code>.
     */
    public RailsJdbcConnection(DatabaseConnection development,
            DatabaseConnection test,
            DatabaseConnection production) {

        this.development = development;
        this.test = test;
        this.production = production;
    }

    public String railsGenerationParam() {
        return null;
    }

    public void editConfig(RailsProject project) {
        insertActiveJdbcHook(project.getProjectDirectory());
        editDatabaseYml(project.getProjectDirectory());
        bundleDrivers(project, 
                getDriverClass(development), getDriverClass(test), getDriverClass(production));
    }

    public String getDisplayName() {
        return null;
    }
    
    private String getDriverClass(DatabaseConnection connection) {
        return connection != null ? connection.getDriverClass() : null;
    }
    
    private static void insertActiveJdbcHook(FileObject dir) {
        FileObject fo = dir.getFileObject("config/environment.rb"); // NOI18N
        if (fo != null) {
            try {
                DataObject dobj = DataObject.find(fo);
                EditorCookie ec = dobj.getCookie(EditorCookie.class);
                if (ec != null) {
                    javax.swing.text.Document doc = ec.openDocument();
                    String text = doc.getText(0, doc.getLength());
                    int offset = text.indexOf("jdbc"); // NOI18N
                    if (offset != -1) {
                        // This rails version already handles JDBC somehow
                        return;
                    }
                    offset = text.indexOf("Rails::Initializer.run do |config|"); // NOI18N
                    if (offset != -1) {
                        String insert =
                                "# Inserted by NetBeans Ruby support to support JRuby\n" +
                                "if defined?(JRUBY_VERSION)\n" + // NOI18N
                                "  require 'rubygems'\n" + // NOI18N
                                "  gem 'activerecord-jdbc-adapter'\n" + // NOI18N
                                "  require 'jdbc_adapter'\n" + // NOI18N
                                "end\n\n"; // NOI18N
                        doc.insertString(offset, insert, null);
                        SaveCookie sc = dobj.getCookie(SaveCookie.class);
                        if (sc != null) {
                            sc.save();
                        } else {
                            LifecycleManager.getDefault().saveAll();
                        }
                    }
                }
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            } catch (DataObjectNotFoundException dnfe) {
                Exceptions.printStackTrace(dnfe);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }

    private void editDatabaseYml(FileObject projectDir) {

        FileObject fo = projectDir.getFileObject("config/database.yml"); // NOI18N

        if (fo != null) {
            BaseDocument bdoc = null;
            try {
                DataObject dobj = DataObject.find(fo);
                EditorCookie ec = dobj.getCookie(EditorCookie.class);
                if (ec != null) {
                    javax.swing.text.Document doc = ec.openDocument();
                    // Replace contents wholesale
                    if (doc instanceof BaseDocument) {
                        bdoc = (BaseDocument) doc;
                        bdoc.atomicLock();
                    }

                    doc.remove(0, doc.getLength());
                    String insert = null;
                    insert =
                            "# JDBC Setup\n" +
                            "# Adjust JDBC driver URLs as necessary.\n" +
                            "development:\n" + // NOI18N
                            buildConnectionConf(development) +
                            "\n" + // NOI18N
                            "# Warning: The database defined as 'test' will be erased and\n" +
                            "# re-generated from your development database when you run 'rake'.\n" +
                            "# Do not set this db to the same as development or production.\n" +
                            "test:\n" + // NOI18N
                            buildConnectionConf(test) +
                            "\n" + // NOI18N
                            "production:\n" + // NOI18N
                            buildConnectionConf(production);
                    doc.insertString(0, insert, null);
                    SaveCookie sc = dobj.getCookie(SaveCookie.class);
                    if (sc != null) {
                        sc.save();
                    } else {
                        LifecycleManager.getDefault().saveAll();
                    }
                }
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            } catch (DataObjectNotFoundException dnfe) {
                Exceptions.printStackTrace(dnfe);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            } finally {
                if (bdoc != null) {
                    bdoc.atomicUnlock();
                }
            }
        }
    }

    private String buildConnectionConf(DatabaseConnection connection) {
        if (connection == null) {
            return "      host: localhost\n" + // NOI18N
                    "      adapter: jdbc\n"; // NOI18N
        }
        // to avoid 'null' in config if no password is specified
        String password = connection.getPassword() == null ? "" : connection.getPassword();
        return "      host: localhost\n" + // NOI18N
                "      adapter: jdbc\n" + // NOI18N
                "      driver: " + connection.getDriverClass() + "\n" + // NOI18N
                "      url: " + connection.getDatabaseURL() + "\n" + // NOI18N
                "      username: " + connection.getUser() + "\n" + // NOI18N
                "      password: " + password + "\n"; // NOI18N

    }

    
    /**
     * Tries to bundle the JDBC drivers identified by the given <code>driverClasses</code>
     * to the given <code>project</code>.
     * @param project the project to which to bundle the drivers.
     * @param driverClasses the driver classes of JDBC drivers to bundle,
     * may contain <code>null</code>s.
     */
    static void bundleDrivers(RailsProject project, String... driverClasses) {

        ProjectPropertyExtender ppe = new ProjectPropertyExtender(project.evaluator(), project.getReferenceHelper(), project.getRakeProjectHelper(),
                RailsProjectProperties.WELL_KNOWN_PATHS, RailsProjectProperties.LIBRARY_PREFIX, RailsProjectProperties.LIBRARY_SUFFIX, RailsProjectProperties.ANT_ARTIFACT_PREFIX);

        Set<Item> items = new  HashSet<Item>();
        for (String driverClass : driverClasses) {
            if (driverClass != null) {
                items.addAll(getDriverItems(driverClass));
            }
        }

        String[] fileRefs = ppe.encodeToStrings(items.iterator());
        EditableProperties projectProperties = project.getRakeProjectHelper().getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH);
        projectProperties.setProperty(RailsProjectProperties.JAVAC_CLASSPATH, fileRefs);
        project.getRakeProjectHelper().putProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
    }
    
    private static Set<Item> getDriverItems(String driverClass) {
        Set<Item> result = new HashSet<Item>();
        for (URL url : getDriverURLs(driverClass)) {
            FileObject fo = URLMapper.findFileObject(url);
            result.add(Item.create(FileUtil.toFile(fo), null));
        }
        return result;
        
    }

    private static List<URL> getDriverURLs(String driverClass) {
        JDBCDriver[] drivers = JDBCDriverManager.getDefault().getDrivers(driverClass);
        List<URL> result = new ArrayList<URL>();
        for (JDBCDriver driver : drivers) {
            for (URL url : driver.getURLs()) {
                result.add(url);
            }
        }
        return result;
    }

    public JdbcInfo getJdbcInfo() {
        return null;
    }

    public String getDatabaseName(String projectName) {
        return projectName + RailsAdapters.DEVELOPMENT_DB_SUFFIX;
    }

    public String getTestDatabaseName(String developmentDbName) {
        return RailsAdapters.getTestDatabaseName(developmentDbName);
    }

    public String getProductionDatabaseName(String developmentDbName) {
        return RailsAdapters.getProductionDatabaseName(developmentDbName);
    }
}
