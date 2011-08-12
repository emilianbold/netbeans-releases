/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.java.source.tasklist;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.api.java.source.JavaSource;
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
    public static final String ENABLE_LINT_RAWTYPES = "enable_lint_rawtypes";
    
    private static final Map<String, Boolean> DEFAULTS;
    
    static {
        DEFAULTS = new HashMap<String, Boolean>();
        
        DEFAULTS.put(ENABLE_LINT, false);
        DEFAULTS.put(ENABLE_LINT_DEPRECATION, false);
        DEFAULTS.put(ENABLE_LINT_UNCHECKED, false);
        DEFAULTS.put(ENABLE_LINT_FALLTHROUGH, false);
        DEFAULTS.put(ENABLE_LINT_SERIAL, false);
        DEFAULTS.put(ENABLE_LINT_FINALLY, false);
        DEFAULTS.put(ENABLE_LINT_CAST, false);
        DEFAULTS.put(ENABLE_LINT_DIVZERO, false);
        DEFAULTS.put(ENABLE_LINT_EMPTY, false);
        DEFAULTS.put(ENABLE_LINT_OVERRIDES, false);
        DEFAULTS.put(ENABLE_LINT_RAWTYPES, false);
    }
    
    public static Preferences getNode() {
        return NbPreferences.forModule(JavaSource.class).node("compiler_settings");
    }
    
    public static String getCommandLine() {
        Preferences p = getNode();
        
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
        if (get(p, ENABLE_LINT_RAWTYPES))
            sb.append("-Xlint:rawtypes ");

        sb.append("-XDfindDiamond ");
        
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ') {
            sb.deleteCharAt(sb.length() - 1);
        }
        
        return sb.toString();
    }
    
    public static boolean get(Preferences p, String key) {
        return p.getBoolean(key, DEFAULTS.get(key));
    }
    
}
