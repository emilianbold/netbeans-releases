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
package org.netbeans.modules.ruby.railsprojects.database;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 * This class converts JDBC connections to Rails adapter configurations.
 * 
 * @author Erno Mononen
 */
public class RailsJdbcAsAdapterConnection implements RailsDatabaseConfiguration {

    /**
     * The pattern used for capturing the adapter name, host name, port and database name.
     */
    private static final Pattern PATTERN = Pattern.compile("jdbc:(\\w+)://(.+):(\\d+)/(\\w+)"); //NOI18N
    
    private final DatabaseConnection development;
    private final DatabaseConnection test;
    private final DatabaseConnection production;
    
    public RailsJdbcAsAdapterConnection(DatabaseConnection development, 
            DatabaseConnection test, DatabaseConnection production) {
        
        this.development = development;
        this.test = test;
        this.production = production;
    }

    public String railsGenerationParam() {
        return null;
    }

    public void editConfig(RailsProject project) {
        editDatabaseYml(project.getProjectDirectory());
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
                    String projectName = projectDir.getName();
                    insert =
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
        
        AdapterParameters adapterParameters = connection != null 
                ? resolveAdapterParams(connection.getDatabaseURL()) 
                : AdapterParameters.DEFAULT;
        
        String hostAndAdapter = 
                "      host: " + adapterParameters.getHostName() + "\n" + // NOI18N
                "      adapter: " + adapterParameters.getAdapterName() + "\n"; // NOI18N

        if (connection == null) {
            return hostAndAdapter;
        }
        // to avoid 'null' in config if no password is specified
        String password = connection.getPassword() == null ? "" : connection.getPassword();
        return hostAndAdapter +
                "      database: " + adapterParameters.getDatabase() + "\n" + // NOI18N
                "      port: " + adapterParameters.getPort() + "\n" + // NOI18N
                "      username: " + connection.getUser() + "\n" + // NOI18N
                "      password: " + password + "\n"; // NOI18N
    }

    
    // not private because of tests
    static AdapterParameters resolveAdapterParams(String databaseURL) {
        Matcher m = PATTERN.matcher(databaseURL);
        if (!m.find() || m.groupCount() < 4) {
            return AdapterParameters.DEFAULT;
        }
        return new AdapterParameters(m.group(1), m.group(2), m.group(3), m.group(4));

    }

    /**
     * Encapsulates connection info extracted from a database URL.
     */
    static class AdapterParameters {

        private final String adapterName;
        private final String hostName;
        private final String port;
        private final String database;
        
        static final AdapterParameters DEFAULT = new AdapterParameters("mysql", "localhost", "3306", ""); //NOI18N

        AdapterParameters(String adapterName, String hostName, String port, String database) {
            this.adapterName = adapterName;
            this.hostName = hostName;
            this.port = port;
            this.database = database;
        }

        public String getAdapterName() {
            return adapterName;
        }

        public String getHostName() {
            return hostName;
        }

        public String getPort() {
            return port;
        }

        public String getDatabase() {
            return database;
        }
        
    }
}
