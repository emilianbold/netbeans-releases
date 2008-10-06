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
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 * Takes care of editing database.yml so that it is correctly
 * generated for PostrgreSQL Rails adapter.
 *
 * @author Erno Mononen
 */
class PostgreSQLAdapter implements RailsDatabaseConfiguration {

    PostgreSQLAdapter() {
    }

    public String railsGenerationParam() {
        return "postgresql";
    }

    public void editConfig(RailsProject project) {
        RubyPlatform platform = RubyPlatform.platformFor(project);
        if (platform.isJRuby()) {
            // only need extra config when using JRuby
            uncommentTcpIpConfig(project.getProjectDirectory());
        }
    }

    public String getDisplayName() {
        return railsGenerationParam();
    }

    /**
     * Uncomments host and port entries that rails generates
     * to database.yml but leaves commented out by default.
     */
    private static void uncommentTcpIpConfig(FileObject dir) {
        FileObject fo = dir.getFileObject("config/database.yml"); // NOI18N
        if (fo != null) {
            try {
                DataObject dobj = DataObject.find(fo);
                EditorCookie ec = dobj.getCookie(EditorCookie.class);
                if (ec != null) {
                    Document doc = ec.openDocument();
                    String text = doc.getText(0, doc.getLength());
                    int hostOffset = text.indexOf("#host:"); // NOI18N
                    if (hostOffset == -1) {
                        // nothing to uncomment
                        return;
                    }
                    doc.remove(hostOffset, 1);
                    text = doc.getText(0, doc.getLength());
                    int portOffset = text.indexOf("#port:"); // NOI18N
                    if (portOffset != -1) {
                        doc.remove(portOffset, 1);
                        text = doc.getText(0, doc.getLength());
                    }

                    // Determine indent
                    int indent = 0;
                    for (int i = hostOffset-1; i >= 0; i--) {
                        if (text.charAt(i) == '\n') {
                            break;
                        } else {
                            indent++;
                        }
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append("# (Automatically uncommented by the IDE - JRuby doesn't support socket)\n");//NOI18N
                    for (int i = 0; i < indent; i++) {
                        sb.append(" ");
                    }
                    doc.insertString(hostOffset, sb.toString(), null);
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
            }
        }
    }

    public JdbcInfo getJdbcInfo() {
        return new JdbcInfo() {

            public String getDriverClass() {
                return "org.postgresql.Driver"; //NOI18N
            }

            public String getURL(String host, String database) {
                return "jdbc:postgresql://" + host + "/" + database; //NOI18N
            }
        };
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
