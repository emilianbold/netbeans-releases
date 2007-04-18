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
package org.netbeans.modules.java.source.tasklist;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan Lahoda
 */
public class CompilerSettings {

    private CompilerSettings() {
    }
        
    public static final String ENABLE_LINT = "enable_lint";
    public static final String ENABLE_LINT_DEPRECATION = "enable_lint_deprecation";
    public static final String ENABLE_LINT_UNCHECKED = "enable_lint_unchecked";
    public static final String ENABLE_LINT_FALLTHROUGH = "enable_lint_fallthrough";
    public static final String ENABLE_LINT_SERIAL = "enable_lint_serial";
    public static final String ENABLE_LINT_FINALLY = "enable_lint_finally";
    public static final String ENABLE_LINT_CAST = "enable_lint_cast";
    public static final String ENABLE_LINT_DIVZERO = "enable_lint_dvizero";
    public static final String ENABLE_LINT_EMPTY = "enable_lint_empty";
    public static final String ENABLE_LINT_OVERRIDES = "enable_lint_overrides";
    
    private static final Map<String, Boolean> DEFAULTS;
    
    static {
        DEFAULTS = new HashMap<String, Boolean>();
        
        DEFAULTS.put(ENABLE_LINT, false);
        DEFAULTS.put(ENABLE_LINT_DEPRECATION, true);
        DEFAULTS.put(ENABLE_LINT_UNCHECKED, true);
        DEFAULTS.put(ENABLE_LINT_FALLTHROUGH, true);
        DEFAULTS.put(ENABLE_LINT_SERIAL, false);
        DEFAULTS.put(ENABLE_LINT_FINALLY, true);
        DEFAULTS.put(ENABLE_LINT_CAST, true);
        DEFAULTS.put(ENABLE_LINT_DIVZERO, true);
        DEFAULTS.put(ENABLE_LINT_EMPTY, true);
        DEFAULTS.put(ENABLE_LINT_OVERRIDES, true);
    }
    
    public static Preferences getNode() {
        return NbPreferences.forModule(CompilerSettings.class).node("compiler_settings");
    }
    
    public static String getCommandLine() {
        Preferences p = getNode();
        
        if (!get(p, ENABLE_LINT))
            return "";
        
        StringBuilder sb = new StringBuilder();
        
        if (get(p, ENABLE_LINT_DEPRECATION))
            sb.append("-Xlint:deprecation ");
        if (get(p, ENABLE_LINT_UNCHECKED))
            sb.append("-Xlint:unchecked ");
        if (get(p, ENABLE_LINT_FALLTHROUGH))
            sb.append("-Xlint:fallthrough ");
        if (get(p, ENABLE_LINT_SERIAL))
            sb.append("-Xlint:serial ");
        if (get(p, ENABLE_LINT_FINALLY))
            sb.append("-Xlint:finally ");
        if (get(p, ENABLE_LINT_CAST))
            sb.append("-Xlint:cast ");
        if (get(p, ENABLE_LINT_DIVZERO))
            sb.append("-Xlint:divzero ");
        if (get(p, ENABLE_LINT_EMPTY))
            sb.append("-Xlint:empty ");
        if (get(p, ENABLE_LINT_OVERRIDES))
            sb.append("-Xlint:overrides ");
        
        if (sb.charAt(sb.length() - 1) == ' ') {
            sb.deleteCharAt(sb.length() - 1);
        }
        
        return sb.toString();
    }
    
    public static boolean get(Preferences p, String key) {
        return p.getBoolean(key, DEFAULTS.get(key));
    }
    
}
