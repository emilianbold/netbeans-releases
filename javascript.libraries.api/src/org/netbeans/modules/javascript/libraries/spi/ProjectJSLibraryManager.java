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

package org.netbeans.modules.javascript.libraries.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.javascript.libraries.api.JavaScriptLibraryManager;
import org.netbeans.modules.javascript.libraries.spi.JavaScriptLibraryChangeSupport;

/**
 *
 * Facility to store and retrieve javascript library references from project 
 * metadata.  Appears in this module to allow projects to warn users that
 * javascript libraries are present when the js library manager is not installed.
 * 
 * Also used for broken reference management by firing change events when the property
 * is changed.
 * 
 * @author Quy Nguyen <quynguyen@netbeans.org>
 */
public final class ProjectJSLibraryManager {
    private static final String LIBRARY_LIST_PROP = "javascript-libraries"; // NOI18N

    public static void modifyJSLibraries(final Project project, final boolean remove, final Collection<String> libraries) {
        final Set<String> libNames = getJSLibraryNames(project);

        ProjectManager.mutex().writeAccess(
                new Runnable() {

                    public void run() {
                        Preferences prefs = ProjectUtils.getPreferences(project, JavaScriptLibraryManager.class, true);
                        assert prefs != null;

                        boolean modified = false;
                        for (String name : libraries) {

                            if (remove && libNames.contains(name)) {
                                modified = true;
                                libNames.remove(name);
                            } else if (!remove && !libNames.contains(name)) {
                                modified = true;
                                libNames.add(name);
                            }
                        }

                        if (modified) {
                            StringBuffer propValue = new StringBuffer();
                            for (String name : libNames) {
                                if (propValue.length() == 0) {
                                    propValue.append(name);
                                } else {
                                    propValue.append(";");
                                    propValue.append(name);
                                }
                            }

                            prefs.put(LIBRARY_LIST_PROP, propValue.toString());
                            try {
                                prefs.flush();
                            } catch (BackingStoreException ex) {
                                Log.getLogger().log(Level.SEVERE, "Could not write to project preferences", ex);
                            }
                            
                            if (remove) {
                                JavaScriptLibraryManager.getDefault().fireLibraryMetadataChanged(project);
                            }
                        }

                    }
                });
    }

    public static Set<String> getJSLibraryNames(Project project) {
        Preferences prefs = ProjectUtils.getPreferences(project, JavaScriptLibraryManager.class, true);
        assert prefs != null;

        String libraries = prefs.get(LIBRARY_LIST_PROP, "");
        String[] tokens = removeEmptyStrings(libraries.split(";"));

        Set<String> librarySet = new LinkedHashSet<String>();
        for (String token : tokens) {
            librarySet.add(token);
        }

        return librarySet;
    }

    private static String[] removeEmptyStrings(String[] arg) {
        ArrayList<String> strings = new ArrayList<String>();
        for (String component : arg) {
            if (component != null && component.length() > 0) {
                strings.add(component);
            }
        }

        return strings.toArray(new String[strings.size()]);
    }
}
