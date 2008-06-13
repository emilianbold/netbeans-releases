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
import org.netbeans.api.ruby.platform.RubyPlatform;
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
 *
 * @author Erno Mononen
 */
class JavaDBAdapter implements RailsDatabaseConfiguration {

    JavaDBAdapter() {
    }

    public String railsGenerationParam() {
        return null;
    }
    
    public String getDisplayName() {
        return "javadb";
    }

    public void editConfig(RailsProject project) {
        FileObject fo = project.getProjectDirectory().getFileObject("config/database.yml"); // NOI18N
        RubyPlatform platform = RubyPlatform.platformFor(project);
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
                    String insert =
                            "# JavaDB Setup\n" +
                            "#\n" +
                            "# You may need to copy derby.jar into\n" +
                            "#  TODO: location " +  platform.getLibDir() + "\n" +
                            "# With Java SE 6 and later this is not necessary.\n" +
                            "development:\n" + // NOI18N
                            "  adapter: derby\n" + // NOI18N
                            "  database: db/development.db\n" + // NOI18N
                            "\n" + // NOI18N
                            "# Warning: The database defined as 'test' will be erased and\n" +
                            "# re-generated from your development database when you run 'rake'.\n" +
                            "# Do not set this db to the same as development or production.\n" +
                            "test:\n" + // NOI18N
                            "  adapter: derby\n" + // NOI18N
                            "  database: db/test.db\n" + // NOI18N
                            "\n" + // NOI18N
                            "production:\n" + // NOI18N
                            "  adapter: derby\n" + // NOI18N
                            "  database: db/production.db\n"; // NOI18N
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

    public JdbcInfo getJdbcInfo() {
        return new JdbcInfo() {

            public String getDriverClass() {
                return "org.apache.derby.jdbc.ClientDriver"; //NOI18N
            }

            public String getURL(String host, String database) {
                return "jdbc:derby://" + host + ":1527/" + database;
            }
        };
    }

}