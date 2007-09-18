/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
