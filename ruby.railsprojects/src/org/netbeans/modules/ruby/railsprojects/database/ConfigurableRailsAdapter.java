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
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;

/**
 * Wraps a <code>RailsDatabaseConfiguration</code> and modifies database.yml
 * so that the specified extra parameters are put into it.
 *
 * @author Erno Mononen
 */
public class ConfigurableRailsAdapter implements RailsDatabaseConfiguration {

    private final RailsDatabaseConfiguration delegate;
    private final String userName;
    private final String password;
    private final String database;
    private final boolean jdbc;

    /**
     * Creates a new instance of ConfigurableRailsAdapter.
     *
     * @param delegate the configuration representing the original configuration.
     * @param userName the user name to be put into the generated configuration,
     * i.e. value for the <code>username:</code> attribute.
     * @param password the user name to be put into the generated configuration,
     * i.e. value for the <code>password:</code> attribute.
     * @param database the name of the database to be put into the generated configuration,
     * i.e. value for the <code>database:</code> attribute.
     * @param jdbc specifies whether the generated configuration should use JDBC
     * to access the database.
     */
    public ConfigurableRailsAdapter(RailsDatabaseConfiguration delegate,
            String userName, String password, String database, boolean jdbc) {
        this.delegate = delegate;
        this.userName = userName;
        this.password = password;
        this.database = database;
        this.jdbc = jdbc;
    }

    public String railsGenerationParam() {
        return delegate.railsGenerationParam();
    }

    public void editConfig(RailsProject project) {
        delegate.editConfig(project);
        edit(project.getProjectDirectory());
        JdbcInfo jdbcInfo = getJdbcInfo();
        if (jdbc && jdbcInfo != null) {
            // try to bundle a driver
            RailsJdbcConnection.bundleDrivers(project, jdbcInfo.getDriverClass());
        }
    }

    public String getDisplayName() {
        return delegate.getDisplayName();
    }


    /**
     * Replaces the value of the given attribute with the given value. If the specified
     * attribute was not found and <code>addAfter</code> is specified, adds the attribute.
     * @param doc the doc representing a yaml file
     * @param attributeName the name of the attribute to replace
     * @param attributeValue the value for the attribute
     * @param addAfter the name of the attribute after which the specified <code>attributeName</code>
     * should be created. May be <code>null</code> in which case the attribute is not created
     * if it does not exist already. A non-null <code>addAfter</code> has no effect if
     * the attribute already exists.
     * @throws javax.swing.text.BadLocationException
     */
    private void changeAttribute(Document doc, String attributeName, String attributeValue, String addAfter) throws BadLocationException {
        Parameters.notWhitespace("attributeName", attributeName); //NOI18N
        if (attributeValue == null || "".equals(attributeValue.trim())) {
            // assume the default generated values are preferred
            return;
        }
        String text = doc.getText(0, doc.getLength());
        int attributeNameIndex = text.indexOf(attributeName);
        if (attributeNameIndex == -1) {
            if (addAfter != null) {
                RailsAdapters.addProperty(doc, attributeName, attributeValue, addAfter);
            } else {
                // can't do much
                return;
            }
        }
        int attributeNameEndIndex = attributeNameIndex + attributeName.length();
        int attributeValuelength = 0;
        for (int i = attributeNameEndIndex; i < text.length(); i++) {
            if ((text.charAt(i)) == '\n') { //NOI18N
                break;
            } else {
                attributeValuelength++;
            }
        }
        doc.remove(attributeNameEndIndex, attributeValuelength);
        doc.insertString(attributeNameEndIndex, attributeValue != null ? " " + attributeValue : "", null);

    }

    private void edit(FileObject dir) {
        FileObject fo = dir.getFileObject("config/database.yml"); // NOI18N
        if (fo != null) {
            try {
                DataObject dobj = DataObject.find(fo);
                EditorCookie ec = dobj.getCookie(EditorCookie.class);
                if (ec != null) {
                    Document doc = ec.openDocument();
                    setDatabase(doc);
                    // see #132383 - need to force the creation of these attributes
                    // for the javadb adapter
                    changeAttribute(doc, "username:", userName, "url:"); //NOI18N
                    changeAttribute(doc, "password:", password, "username:"); //NOI18N
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

    private void setDatabase(Document databaseYml) throws BadLocationException {

        JdbcInfo jdbcInfo = getJdbcInfo();
        if (!jdbc || jdbcInfo == null) {
            // not using jdbc, so just set the database
            changeAttribute(databaseYml, "database:", database, null); //NOI18N
            return;
        }
        // use the default database name if none was specified
        String dbName = !isEmpty(database) ? database : RailsAdapters.getPropertyValue(databaseYml, "database:");

        // change the adapter
        changeAttribute(databaseYml, "adapter:", "jdbc", null); //NOI18N
        // add url and driver
        RailsAdapters.addProperty(databaseYml, "url:", jdbcInfo.getURL("localhost", dbName), "adapter:"); //NOI18N
        RailsAdapters.addProperty(databaseYml, "driver:", jdbcInfo.getDriverClass(), "adapter:"); //NOI18N

        // remove database, since we now have url and driver
        RailsAdapters.removeProperty(databaseYml, "database:"); //NOI18N

    }

    private boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }

    public JdbcInfo getJdbcInfo() {
        return delegate.getJdbcInfo();
    }

    public String getDatabaseName(String projectName) {
        return delegate.getDatabaseName(projectName);
    }
}
