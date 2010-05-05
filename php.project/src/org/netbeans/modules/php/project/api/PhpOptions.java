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
import org.netbeans.spi.project.support.ant.PropertyUtils;
import static org.netbeans.modules.php.project.ui.options.PhpOptions.PHP_INTERPRETER;
import static org.netbeans.modules.php.project.ui.options.PhpOptions.PHP_DEBUGGER_PORT;
import static org.netbeans.modules.php.project.ui.options.PhpOptions.PHP_DEBUGGER_SESSION_ID;
import static org.netbeans.modules.php.project.ui.options.PhpOptions.PHP_DEBUGGER_STOP_AT_FIRST_LINE;
import static org.netbeans.modules.php.project.ui.options.PhpOptions.PHP_DEBUGGER_WATCHES_AND_EVAL;
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
    public static final String PROP_PHP_INTERPRETER = "propPhpInterpreter"; // NOI18N
    public static final String PROP_PHP_DEBUGGER_PORT = "propPhpDebuggerPort"; // NOI18N
    public static final String PROP_PHP_DEBUGGER_SESSION_ID = "propPhpDebuggerSessionId"; // NOI18N
    public static final String PROP_PHP_DEBUGGER_STOP_AT_FIRST_LINE = "propPhpDebuggerStopAtFirstLine"; // NOI18N
    public static final String PROP_PHP_DEBUGGER_WATCHES_AND_EVAL = "propPhpDebuggerWatchesAndEval"; // NOI18N
    public static final String PROP_PHP_GLOBAL_INCLUDE_PATH = "propPhpGlobalIncludePath"; // NOI18N

    private static final PhpOptions INSTANCE = new PhpOptions();

    private final PropertyChangeSupport propertyChangeSupport;

    private PhpOptions() {
        propertyChangeSupport = new PropertyChangeSupport(this);
        getPhpOptions().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                String key = evt.getKey();
                String newValue = evt.getNewValue();
                if (PHP_INTERPRETER.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_INTERPRETER, null, newValue);
                } else if (PHP_DEBUGGER_PORT.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_DEBUGGER_PORT, null, Integer.valueOf(newValue));
                } else if (PHP_DEBUGGER_SESSION_ID.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_DEBUGGER_SESSION_ID, null, newValue);
                } else if (PHP_DEBUGGER_STOP_AT_FIRST_LINE.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_DEBUGGER_STOP_AT_FIRST_LINE, null, Boolean.valueOf(newValue));
                } else if (PHP_DEBUGGER_WATCHES_AND_EVAL.equals(key)) {
                    propertyChangeSupport.firePropertyChange(PROP_PHP_DEBUGGER_WATCHES_AND_EVAL, null, Boolean.valueOf(newValue));
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
     * Get the session ID for PHP debugger, the default is
     * <code>{@value org.netbeans.modules.php.project.ui.options.PhpOptions#DEFAULT_DEBUGGER_SESSION_ID}</code>.
     * @return the session ID for PHP debugger.
     */
    public String getDebuggerSessionId() {
        return getPhpOptions().getDebuggerSessionId();
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
     * Check whether debugger allows to use watches and balloon evaluation. The default value is
     * <code>{@value org.netbeans.modules.php.project.ui.options.PhpOptions#DEFAULT_DEBUGGER_WATCHES_AND_EVAL}</code>.
     * @return <code>true</code> if the debugger allows to use watches and balloon evaluation, <code>false</code> otherwise.
     * @since 2.25
     */
    public boolean isDebuggerWatchesAndEval() {
        return getPhpOptions().isDebuggerWatchesAndEval();
    }

    /**
     * Get the global PHP include path.
     * @return the global PHP include path or an empty String if no folders are defined.
     * @see #getPhpGlobalIncludePathAsArray()
     */
    public String getPhpGlobalIncludePath() {
        return getPhpOptions().getPhpGlobalIncludePath();
    }

    /**
     * Get the global PHP include path as array of strings.
     * @return the global PHP include path as array or an empty array of strings if no folders are defined.
     * @see #getPhpGlobalIncludePath()
     * @since 2.7
     */
    public String[] getPhpGlobalIncludePathAsArray() {
        return PropertyUtils.tokenizePath(getPhpGlobalIncludePath());
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
}
