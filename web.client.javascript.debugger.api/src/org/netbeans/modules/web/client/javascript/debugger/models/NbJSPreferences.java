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

package org.netbeans.modules.web.client.javascript.debugger.models;

import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author quynguyen
 */
public final class NbJSPreferences {
    private static NbJSPreferences INSTANCE = null;
    
    private static final boolean SHOW_FUNCTIONS_DEFAULT = true;
    private static final boolean SHOW_CONSTANTS_DEFAULT = true;
    private static final boolean BYPASS_CONSTRUCTORS_DEFAULT = false;
    private static final boolean ENABLE_STEP_FILTERS_DEFAULT = false;
    private static final boolean SUSPEND_ON_FIRST_LINE_DEFAULT = false;
    private static final boolean SUSPEND_ON_EXCEPTIONS_DEFAULT = false;
    private static final boolean SUSPEND_ON_ERRORS_DEFAULT = false;
    private static final boolean SUSPEND_ON_DEBUGGER_KEYWORD_DEFAULT = true;
    
    
    
    // property names
    public static final String PROP_SHOW_FUNCTIONS = "functionsShow"; // NOI18N
    public static final String PROP_SHOW_CONSTANTS = "constantsShow"; // NOI18N
    public static final String PROP_BYPASS_CONSTRUCTORS = "constructorsBypass"; // NOI18N
    public static final String PROP_ENABLE_STEP_FILTERS = "stepFiltersEnable"; // NOI18N
    public static final String PROP_SUSPEND_ON_FIRST_LINE = "firstLineSuspend"; // NOI18N
    public static final String PROP_SUSPEND_ON_EXCEPTIONS = "exceptionsSuspend"; // NOI18N
    public static final String PROP_SUSPEND_ON_ERRORS = "errorsSuspend"; // NOI18N
    public static final String PROP_SUSPEND_ON_DEBUGGER_KEYWORD = "debuggerKeywordSuspend"; // NOI18N
    
    public static NbJSPreferences getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NbJSPreferences();
        }
        
        return INSTANCE;
    }
    
    public boolean getShowFunctions() {
        return getPreferences().getBoolean(PROP_SHOW_FUNCTIONS, SHOW_FUNCTIONS_DEFAULT);
    }
    
    public void setShowFunctions(boolean b) {
        getPreferences().putBoolean(PROP_SHOW_FUNCTIONS, b);
    }
    
    public void setShowConstants(boolean b) {
        getPreferences().putBoolean(PROP_SHOW_CONSTANTS, b);
    }

    public boolean getShowConstants() {
        return getPreferences().getBoolean(PROP_SHOW_CONSTANTS, SHOW_CONSTANTS_DEFAULT);
    }
    
    public void setBypassConstructors(boolean b) {
        getPreferences().putBoolean(PROP_BYPASS_CONSTRUCTORS, b);
    }
    
    public boolean getBypassConstructors() {
        return getPreferences().getBoolean(PROP_BYPASS_CONSTRUCTORS, BYPASS_CONSTRUCTORS_DEFAULT);
    }
    
    public void setEnableStepFilters(boolean b) {
        getPreferences().putBoolean(PROP_ENABLE_STEP_FILTERS, b);
    }
    
    public boolean getEnableStepFilters() {
        return getPreferences().getBoolean(PROP_ENABLE_STEP_FILTERS, ENABLE_STEP_FILTERS_DEFAULT);
    }
    
    public void setSuspendOnFirstLine(boolean b) {
        getPreferences().putBoolean(PROP_SUSPEND_ON_FIRST_LINE, b);
    }
    
    public boolean getSuspendOnFirstLine() {
        return getPreferences().getBoolean(PROP_SUSPEND_ON_FIRST_LINE, SUSPEND_ON_FIRST_LINE_DEFAULT);
    }
    
    public void setSuspendOnExceptions(boolean b) {
        getPreferences().putBoolean(PROP_SUSPEND_ON_EXCEPTIONS, b);
    }
    
    public boolean getSuspendOnExceptions() {
        return getPreferences().getBoolean(PROP_SUSPEND_ON_EXCEPTIONS, SUSPEND_ON_EXCEPTIONS_DEFAULT);
    }
    
    public void setSuspendOnErrors(boolean b) {
        getPreferences().putBoolean(PROP_SUSPEND_ON_ERRORS, b);
    }
    
    public boolean getSuspendOnErrors() {
        return getPreferences().getBoolean(PROP_SUSPEND_ON_ERRORS, SUSPEND_ON_ERRORS_DEFAULT);
    }
    
    public void setSuspendOnDebuggerKeyword(boolean b) {
        getPreferences().putBoolean(PROP_SUSPEND_ON_DEBUGGER_KEYWORD, b);
    }
    
    public boolean getSuspendOnDebuggerKeyword() {
        return getPreferences().getBoolean(PROP_SUSPEND_ON_DEBUGGER_KEYWORD, SUSPEND_ON_DEBUGGER_KEYWORD_DEFAULT);
    }    
     
    public void addPreferencesChangeListener(PreferenceChangeListener listener) {
        getPreferences().addPreferenceChangeListener(listener);
    }
    
    public void removePreferencesChangeListener(PreferenceChangeListener listener) {
        getPreferences().removePreferenceChangeListener(listener);
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule(NbJSPreferences.class);
    }
    
}
