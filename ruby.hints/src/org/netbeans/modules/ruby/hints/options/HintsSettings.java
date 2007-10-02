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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.ruby.hints.options;

import java.util.prefs.Preferences;
import org.netbeans.modules.ruby.hints.infrastructure.RulesManager;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import org.netbeans.modules.ruby.hints.spi.Rule;
import org.netbeans.modules.ruby.hints.spi.UserConfigurableRule;

/**
 *
 * @author Petr Hrebejk
 * @author Jan Lahoda
 */
public class HintsSettings {

    // Only used for categories (disabled state in options dialog)
    static final HintSeverity SEVERITY_DEFAUT = HintSeverity.WARNING;
    static final boolean IN_TASK_LIST_DEFAULT = true;
    
    static final String ENABLED_KEY = "enabled";         // NOI18N
    static final String SEVERITY_KEY = "severity";       // NOI18N
    static final String IN_TASK_LIST_KEY = "inTaskList"; // NOI18N
    
    private static final String DEFAULT_PROFILE = "default"; // NOI18N
    
    private HintsSettings() {
    }
 
    public static String getCurrentProfileId() {
        return DEFAULT_PROFILE;
    }
    
    /** For current profile
     */ 
    public static boolean isEnabled(UserConfigurableRule hint ) {
        Preferences p = RulesManager.getInstance().getPreferences(hint, HintsSettings.getCurrentProfileId());
        return isEnabled(hint, p);
    }
    
    /** For current profile
     */ 
    public static boolean isShowInTaskList( UserConfigurableRule hint ) {
        Preferences p = RulesManager.getInstance().getPreferences(hint, HintsSettings.getCurrentProfileId());
        return isShowInTaskList(hint, p);
    }
    
      
    public static boolean isEnabled(UserConfigurableRule hint, Preferences preferences ) {        
        return preferences.getBoolean(ENABLED_KEY, hint.getDefaultEnabled());
    }
    
    public static void setEnabled( Preferences p, boolean value ) {
        p.putBoolean(ENABLED_KEY, value);
    }
      
    public static boolean isShowInTaskList(UserConfigurableRule hint, Preferences preferences ) {
        return preferences.getBoolean(IN_TASK_LIST_KEY, hint.showInTasklist());
    }
    
    public static void setShowInTaskList( Preferences p, boolean value ) {
        p.putBoolean(IN_TASK_LIST_KEY, value);
    }
      
    public static HintSeverity getSeverity(UserConfigurableRule hint, Preferences preferences ) {
        String s = preferences.get(SEVERITY_KEY, null );
        return s == null ? hint.getDefaultSeverity() : HintSeverity.valueOf(s);
    }
    
    public static void setSeverity( Preferences p, HintSeverity severity ) {
        p.put(SEVERITY_KEY, severity.name());
    }
}
