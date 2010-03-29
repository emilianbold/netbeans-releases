/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.api.phpmodule;

import java.util.prefs.Preferences;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 * This class could be useful for extending a PHP project.
 * <p>
 * Note: For public API, this should likely be final class using accessor pattern.
 * @author Tomas Mysik
 */
public abstract class PhpModule {

    /**
     * See {@link org.netbeans.api.project.ProjectInformation#getName}.
     */
    public abstract String getName();

    /**
     * See {@link org.netbeans.api.project.ProjectInformation#getDisplayName}.
     */
    public abstract String getDisplayName();

    /**
     * Get the source directory for this PHP module.
     * @return the source directory, never <code>null</code>
     */
    public abstract FileObject getSourceDirectory();

    /**
     * Get the test directory for this PHP module.
     * @return the test directory, can be <code>null</code> if not set yet
     */
    public abstract FileObject getTestDirectory();

    /**
     * Get the current {@link PhpModuleProperties properties} of this PHP module.
     * Please note that caller should not hold this properties because they can
     * change very often (if user changes Run Configuration).
     * @return the current {@link PhpModuleProperties properties}
     * @since 1.19
     */
    public abstract PhpModuleProperties getProperties();

    /**
     * Get {@link Preferences} of this PHP module for the given PHP framework provider.
     * This method is suitable for storing (and reading) PHP module specific properties.
     * For more information, see {@link org.netbeans.api.project.ProjectUtils#getPreferences(org.netbeans.api.project.Project, Class, boolean)}.
     * @param clazz PHP framework provider class which defines the namespace of preferences
     * @param shared whether the returned settings should be shared
     * @return {@link Preferences} for this PHP module and the given PHP framework provider
     * @since 1.26
     * @see org.netbeans.api.project.ProjectUtils#getPreferences(org.netbeans.api.project.Project, Class, boolean)
     */
    public abstract <T extends PhpFrameworkProvider> Preferences getPreferences(Class<T> clazz, boolean shared);

    /**
     * Gets PHP module for the given {@link FileObject}.
     * @param fo {@link FileObject} to get PHP module for
     * @return PHP module or <code>null</code> if not found
     * @since 1.16
     */
    public static PhpModule forFileObject(FileObject fo) {
        return lookupPhpModule(FileOwnerQuery.getOwner(fo));
    }

    /**
     * Infers PHP module - from the currently selected top component, open projects etc.
     * @return PHP module or <code>null</code> if not found.
     */
    public static PhpModule inferPhpModule() {
        // try current context firstly
        Node[] activatedNodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        if (activatedNodes != null) {
            for (Node n : activatedNodes) {
                PhpModule result = lookupPhpModule(n.getLookup());
                if (result != null) {
                    return result;
                }
            }
        }

        Lookup globalContext = Utilities.actionsGlobalContext();
        PhpModule result = lookupPhpModule(globalContext);
        if (result != null) {
            return result;
        }
        FileObject fo = globalContext.lookup(FileObject.class);
        if (fo != null) {
            result = forFileObject(fo);
            if (result != null) {
                return result;
            }
        }

        // next try main project
        OpenProjects projects = OpenProjects.getDefault();
        result = lookupPhpModule(projects.getMainProject());
        if (result != null) {
            return result;
        }

        // next try other opened projects
        for (Project project : projects.getOpenProjects()) {
            result = lookupPhpModule(project);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private static PhpModule lookupPhpModule(Project project) {
        if (project != null) {
            return lookupPhpModule(project.getLookup());
        }
        return null;
    }

    private static PhpModule lookupPhpModule(Lookup lookup) {
        // try directly
        PhpModule result = lookup.lookup(PhpModule.class);
        if (result != null) {
            return result;
        }
        // try through Project instance
        Project project = lookup.lookup(Project.class);
        if (project != null) {
            result = project.getLookup().lookup(PhpModule.class);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * This class is used to notify about changes in the direction from frameworks to PHP module.
     * @see org.netbeans.modules.php.spi.phpmodule.PhpModuleCustomizerExtender#save(PhpModule)
     * @since 1.26
     */
    public enum Change {
        /**
         * Directory with source files changed.
         */
        SOURCES_CHANGE,
        /**
         * Directory with test files changed.
         */
        TESTS_CHANGE,
        /**
         * Directory with Selenium files changed.
         */
        SELENIUM_CHANGE,
        /**
         * Ignored files changed.
         * @see org.netbeans.modules.php.spi.phpmodule.PhpModuleIgnoredFilesExtender
         */
        IGNORED_FILES_CHANGE,
    }
}
