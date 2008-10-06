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
    
    public enum PROPERTIES {
        PROP_SUSPEND_ON_FIRST_LINE( "firstLineSuspend", false),             // NOI18N
        PROP_SUSPEND_ON_EXCEPTIONS("exceptionsSuspend", false),             // NOI18N
        PROP_SUSPEND_ON_ERRORS("errorsSuspend",false),                      // NOI18N
        PROP_SUSPEND_ON_DEBUGGER_KEYWORD("debuggerKeywordSuspend", true),   // NOI18N
        PROP_SHOW_FUNCTIONS("functionsShow", false),                        // NOI18N
        PROP_SHOW_CONSTANTS("constantsShow", true),                         // NOI18N
        PROP_HTTP_MONITOR_ENABLED("PROP_HTTP_MONITOR_ENABLED", true),       // NOI18N
        PROP_HTTP_MONITOR_OPENED("http_monitor_opened", false),             // NOI18N
        PROP_IGNORE_QUERY_STRINGS("ignoreQueryStrings", true);              // NOI18N
        PROPERTIES( String string, boolean b_default){
            this.string = string;
            this.b_default = b_default;
        }
        private boolean getBooleanDefault() {
            return b_default;
        }

        public boolean getBooleanPreference() {
            return getPreferences().getBoolean(this.toString(), this.getBooleanDefault());
        }

        public void setBooleanPreferences(boolean b) {
            getPreferences().putBoolean(this.toString(), b);
        }
        private String string;
        private boolean b_default;
        public boolean equals(String str) {
            return this.toString().equals(str);
        }

    }

    public static synchronized NbJSPreferences getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NbJSPreferences();
        }

        return INSTANCE;
    }

    /**
     * @return  This preference is really used similar to HttpMonitorEnabled to be
     * a mechanism in which the extension determines if it should be recording
     * events.  In the future, we should consider monitor Showing/Hiding and
     * Queue up the events.
     */
    public boolean getHttpMonitorOpened() {
        return PROPERTIES.PROP_HTTP_MONITOR_OPENED.getBooleanPreference();
    }

    /**
     * @param This preference is really used similar to HttpMonitorEnabled to be
     * a mechanism in which the extension determines if it should be recording
     * events.  In the future, we should consider monitor Showing/Hiding and
     * Queue up the events.
     */
    public void setHttpMonitorOpened(boolean b) {
        PROPERTIES.PROP_HTTP_MONITOR_OPENED.setBooleanPreferences(b);
    }

    public boolean getShowFunctions() {
        return PROPERTIES.PROP_SHOW_FUNCTIONS.getBooleanPreference();
    }

    public void setShowFunctions(boolean b) {
        PROPERTIES.PROP_SHOW_FUNCTIONS.setBooleanPreferences(b);
    }

    public void setShowConstants(boolean b) {
        PROPERTIES.PROP_SHOW_CONSTANTS.setBooleanPreferences(b);
    }

    public boolean getShowConstants() {
        return PROPERTIES.PROP_SHOW_CONSTANTS.getBooleanPreference();
    }

    public void setSuspendOnFirstLine(boolean b) {
        PROPERTIES.PROP_SUSPEND_ON_FIRST_LINE.setBooleanPreferences(b);
    }

    public boolean getSuspendOnFirstLine() {
        return PROPERTIES.PROP_SUSPEND_ON_FIRST_LINE.getBooleanPreference();
    }

    public void setSuspendOnExceptions(boolean b) {
        PROPERTIES.PROP_SUSPEND_ON_EXCEPTIONS.setBooleanPreferences(b);
    }

    public boolean getSuspendOnExceptions() {
        return PROPERTIES.PROP_SUSPEND_ON_EXCEPTIONS.getBooleanPreference();
    }

    public void setHttpMonitorEnabled(boolean b) {
        PROPERTIES.PROP_HTTP_MONITOR_ENABLED.setBooleanPreferences(b);
    }

    public void setSuspendOnErrors(boolean b) {
        PROPERTIES.PROP_SUSPEND_ON_ERRORS.setBooleanPreferences(b);
    }

    public boolean getSuspendOnErrors() {
        return PROPERTIES.PROP_SUSPEND_ON_ERRORS.getBooleanPreference();
    }

    public void setSuspendOnDebuggerKeyword(boolean b) {
        PROPERTIES.PROP_SUSPEND_ON_DEBUGGER_KEYWORD.setBooleanPreferences(b);
    }

    public boolean getSuspendOnDebuggerKeyword() {
        return PROPERTIES.PROP_SUSPEND_ON_DEBUGGER_KEYWORD.getBooleanPreference();
    }

    public boolean getHttpMonitorEnabled() {
        return PROPERTIES.PROP_HTTP_MONITOR_ENABLED.getBooleanPreference();
    }
    public void seHttpMonitorEnabled(boolean b) {
        PROPERTIES.PROP_HTTP_MONITOR_ENABLED.setBooleanPreferences(b);
    }

    public void setIgnoreQueryStrings(boolean b) {
        PROPERTIES.PROP_IGNORE_QUERY_STRINGS.setBooleanPreferences(b);
    }

    public boolean getIgnoreQueryStrings() {
        return PROPERTIES.PROP_IGNORE_QUERY_STRINGS.getBooleanPreference();
    }

    public void addPreferenceChangeListener(PreferenceChangeListener listener) {
        getPreferences().addPreferenceChangeListener(listener);
    }

    public void removePreferenceChangeListener(PreferenceChangeListener listener) {
        getPreferences().removePreferenceChangeListener(listener);
    }

    private static final Preferences getPreferences() {
        return NbPreferences.forModule(NbJSPreferences.class);
    }


}
