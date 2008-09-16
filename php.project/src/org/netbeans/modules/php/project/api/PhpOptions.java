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

package org.netbeans.modules.php.project.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;
import static org.netbeans.modules.php.project.ui.options.PhpOptions.PHP_INTERPRETER;
import static org.netbeans.modules.php.project.ui.options.PhpOptions.PHP_DEBUGGER_PORT;
import static org.netbeans.modules.php.project.ui.options.PhpOptions.PHP_DEBUGGER_STOP_AT_FIRST_LINE;
import static org.netbeans.modules.php.project.ui.options.PhpOptions.PHP_GLOBAL_INCLUDE_PATH;

/**
 * Helper class to get actual PHP properties like debugger port etc.
 * Use {@link #getInstance()} to get class instance.
 * <p>
 * Since 1.4 it is possible to listen to changes in particular PHP options.
 * @author Tomas Mysik
 * @since 1.2
 */
public final class PhpOptions {
    /**
     * The default value for short tags (&lt?) (<code>{@value #SHORT_TAGS_ENABLED}</code>).
     * @since 2.2
     */
    public static final boolean SHORT_TAGS_ENABLED = true;
    /**
     * The default value for ASP tags (&lt% and %&gt;) (<code>{@value #ASP_TAGS_ENABLED}</code>).
     * @since 2.2
     */
    public static final boolean ASP_TAGS_ENABLED = false;

    public static final String PROP_PHP_INTERPRETER = "propPhpInterpreter"; // NOI18N
    public static final String PROP_PHP_DEBUGGER_PORT = "proPhpDebuggerPort"; // NOI18N
    public static final String PROP_PHP_DEBUGGER_STOP_AT_FIRST_LINE = "propPhpDebuggerStopAtFirstLine"; // NOI18N
    public static final String PROP_PHP_GLOBAL_INCLUDE_PATH = "propPhpGlobalIncludePath"; // NOI18N

    private static final PhpOptions INSTANCE = new PhpOptions();

    private final PropertyChangeSupport propertyChangeSupport;

    private PhpOptions() {
        propertyChangeSupport = new PropertyChangeSupport(this);
        getPhpOptions().addPreferenceChangeListener(new PreferenceChangeListener() {
            public void preferenceChange(PreferenceChangeEvent evt) {
                String key = evt.getKey();
                String newValue = evt.getNewValue();
                if (PHP_INTERPRETER.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_INTERPRETER, null, newValue);
                } else if (PHP_DEBUGGER_PORT.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_DEBUGGER_PORT, null, Integer.valueOf(newValue));
                } else if (PHP_DEBUGGER_STOP_AT_FIRST_LINE.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_DEBUGGER_STOP_AT_FIRST_LINE, null, Boolean.valueOf(newValue));
                } else if (PHP_GLOBAL_INCLUDE_PATH.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_GLOBAL_INCLUDE_PATH, null, newValue);
                }
            }
        });
    }

    public static PhpOptions getInstance() {
        return INSTANCE;
    }

    private org.netbeans.modules.php.project.ui.options.PhpOptions getPhpOptions() {
        return org.netbeans.modules.php.project.ui.options.PhpOptions.getInstance();
    }

    /**
     * Get the PHP interpreter file path.
     * @return the PHP interpreter file path or <code>null</code> if none is found.
     */
    public String getPhpInterpreter() {
        return getPhpOptions().getPhpInterpreter();
    }

    /**
     * Get the port for PHP debugger, the default is
     * <code>{@value org.netbeans.modules.php.project.ui.options.PhpOptions#DEFAULT_DEBUGGER_PORT}</code>.
     * @return the port for PHP debugger.
     */
    public int getDebuggerPort() {
        return getPhpOptions().getDebuggerPort();
    }

    /**
     * Check whether debugger stops at the first line of a PHP script. The default value is
     * <code>{@value org.netbeans.modules.php.project.ui.options.PhpOptions#DEFAULT_DEBUGGER_STOP_AT_FIRST_LINE}</code>.
     * @return <code>true</code> if the debugger stops at the first line of a PHP script, <code>false</code> otherwise.
     */
    public boolean isDebuggerStoppedAtTheFirstLine() {
        return getPhpOptions().isDebuggerStoppedAtTheFirstLine();
    }

    /**
     * Get the global PHP include path.
     * @return the global PHP include path or an empty String if no folders are defined.
     */
    public String getPhpGlobalIncludePath() {
        return getPhpOptions().getPhpGlobalIncludePath();
    }

    /**
     * Find out whether short tags (&lt;?) are supported or not. This option is project specific.
     * If no project is found for the file, then {@link #SHORT_TAGS_ENABLED the default value} is returned.
     * @param file a file which could belong to a project (if not, {@link #SHORT_TAGS_ENABLED the default value} is returned).
     * @return <code>true</code> if short tags are supported, <code>false</code> otherwise.
     * @see #SHORT_TAGS_ENABLED
     * @since 2.2
     */
    public boolean areShortTagsEnabled(FileObject file) {
        Parameters.notNull("file", file);
        PhpProject phpProject = getPhpProject(file);
        if (phpProject == null) {
            return SHORT_TAGS_ENABLED;
        }
        return ProjectPropertiesSupport.areShortTagsEnabled(phpProject);
    }

    /**
     * Find out whether ASP tags (&lt% and %&gt;) are supported or not. This option is project specific.
     * If no project is found for the file, then {@link #ASP_TAGS_ENABLED the default value} is returned.
     * @param file a file which could belong to a project (if not, {@link #ASP_TAGS_ENABLED the default value} is returned).
     * @return <code>true</code> if ASP tags are supported, <code>false</code> otherwise.
     * @see #ASP_TAGS_ENABLED
     * @since 2.2
     */
    public boolean areAspTagsEnabled(FileObject file) {
        Parameters.notNull("file", file);
        PhpProject phpProject = getPhpProject(file);
        if (phpProject == null) {
            return ASP_TAGS_ENABLED;
        }
        return ProjectPropertiesSupport.areAspTagsEnabled(phpProject);
    }

    /**
     * @since 1.4
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * @since 1.4
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    private PhpProject getPhpProject(FileObject file) {
        Project project = FileOwnerQuery.getOwner(file);
        if (project instanceof PhpProject) {
            return (PhpProject) project;
        }
        return null;
    }
}
