/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.clientproject.api.jslibs;

import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Support class for JavaScript libraries.
 * @since 1.31
 */
public final class JavaScriptLibraries {

    /**
     * Category name in Project Customizer.
     * @see #createCustomizer(org.netbeans.modules.web.clientproject.api.jslibs.JavaScriptLibraries.CustomizerSupport)
     */
    public static final String CUSTOMIZER_IDENT = "JavaScriptFiles"; // NOI18N

    private static final String JS_LIBS_FOLDER = "js.libs.folder"; // NOI18N


    private JavaScriptLibraries() {
    }


    /**
     * Create project customizer for JavaScript files and libraries.
     * <p>
     * Category name is {@link #CUSTOMIZER_IDENT} ({@value #CUSTOMIZER_IDENT}).
     * <p>
     * Instance of this class can be registered for any project in its project customizer SFS folder.
     * @see ProjectCustomizer.CompositeCategoryProvider.Registration
     * @since
     */
    public static ProjectCustomizer.CompositeCategoryProvider createCustomizer(@NonNull CustomizerSupport customizerSupport) {
        Parameters.notNull("customizerSupport", customizerSupport); // NOI18N
        return new JavaScriptLibraryCustomizer(customizerSupport);
    }

    /**
     * Get stored JS libraries folder path.
     * @param project project to get JS libraries folder path for
     * @return stored JS libraries folder path, can be {@code null} if not set
     * @since 1.36
     */
    @CheckForNull
    public static String getJsLibFolder(@NonNull Project project) {
        Parameters.notNull("project", project);
        return getProjectPreferences(project).get(JS_LIBS_FOLDER, null);
    }

    /**
     * Set stored JS libraries folder path.
     * @param project project to set JS libraries folder path for
     * @since 1.36
     */
    public static void setJsLibFolder(@NonNull Project project, @NonNull String jsLibFolder) {
        Parameters.notNull("project", project);
        Parameters.notNull("jsLibFolder", jsLibFolder);
        getProjectPreferences(project) .put(JS_LIBS_FOLDER, jsLibFolder);
    }

    private static Preferences getProjectPreferences(Project project) {
        return ProjectUtils.getPreferences(project, JavaScriptLibraries.class, true);
    }

    //~ Inner classes

    /**
     * Support for this customizer panel.
     * <p>
     * Implementations must be thread-safe.
     */
    public interface CustomizerSupport {

        /**
         * Get web root that is used as a parent folder of {@link JavaScriptLibrarySelectionPanel#getLibrariesFolder() libraries folder}
         * as well as for searching existing JavaScript files. Can be {@code null} if the current web root or project is broken - in such case,
         * an error is displayed and JS libraries cannot be added.
         * @return web root, can be {@code null}
         */
        @CheckForNull
        File getWebRoot(@NonNull Lookup context);

        /**
         * Set {@link JavaScriptLibrarySelectionPanel#getLibrariesFolder() libraries folder}.
         * <p>
         * This method is called whenever the libraries folder changes and is valid.
         * @param librariesFolder libraries folder
         */
        void setLibrariesFolder(@NonNull Lookup context, @NonNull String librariesFolder);

        /**
         * Set {@link JavaScriptLibrarySelectionPanel#getSelectedLibraries() selected JS libraries}.
         * <p>
         * This method is called whenever the selected JS libraries change and are valid.
         * @param selectedLibraries selected JS libraries
         */
        void setSelectedLibraries(@NonNull Lookup context, @NonNull List<JavaScriptLibrarySelectionPanel.SelectedLibrary> selectedLibraries);

    }

}
