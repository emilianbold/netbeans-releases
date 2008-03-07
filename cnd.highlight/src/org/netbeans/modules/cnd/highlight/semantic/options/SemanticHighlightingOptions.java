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

package org.netbeans.modules.cnd.highlight.semantic.options;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Sergey Grinev
 */
public class SemanticHighlightingOptions {

    private SemanticHighlightingOptions() {}

    private static final Preferences preferences = NbPreferences.forModule(SemanticHighlightingOptions.class);
    private static final String ENABLE_MARK_OCCURENCES = "EnableMarkOccurrences"; // NOI18N
    private static final String KEEP_MARKS = "KeepMarks"; // NOI18N
    private static final String ENABLE_MACROS = "Macros"; // NOI18N
    private static final String DIFFER_SYSTEM_MACROS = "SysMacros"; // NOI18N
    private static final String ENABLE_CLASS_FIELDS = "ClassFields"; // NOI18N
    private static final String ENABLE_FUNCTION_NAMES = "FunctionNames"; // NOI18N
    public static final boolean SEMANTIC_ADVANCED = Boolean.getBoolean("cnd.semantic.advanced"); // NOI18N
    
    private static boolean getOption(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    private static void setOption(String key, boolean value) {
        preferences.putBoolean(key, value);
    }
    
    // 6.1 options
    
    public static boolean getEnableMarkOccurences() {
        return getOption(ENABLE_MARK_OCCURENCES, true);
    }

    public static void setEnableMarkOccurences(boolean value) {
        setOption(ENABLE_MARK_OCCURENCES, value);
    }

    public static boolean getKeepMarks() {
        return getOption(KEEP_MARKS, true);
    }

    public static void setKeepMarks(boolean value) {
        setOption(KEEP_MARKS, value);
    }

    public static boolean getEnableMacros() {
        return getOption(ENABLE_MACROS, true);
    }

    public static void setEnableMacros(boolean value) {
        setOption(ENABLE_MACROS, value);
    }

    public static boolean getDifferSystemMacros() {
        return getOption(DIFFER_SYSTEM_MACROS, true);
    }

    public static void setDifferSystemMacros(boolean value) {
        setOption(DIFFER_SYSTEM_MACROS, value);
    }

    // prototype options
    
    public static boolean getEnableClassFields() {
        return SEMANTIC_ADVANCED && getOption(ENABLE_CLASS_FIELDS, true);
    }

    public static void setEnableClassFields(boolean value) {
        setOption(ENABLE_CLASS_FIELDS, value);
    }

    public static boolean getEnableFunctionNames() {
        return SEMANTIC_ADVANCED && getOption(ENABLE_FUNCTION_NAMES, true);
    }

    public static void setEnableFunctionNames(boolean value) {
        setOption(ENABLE_FUNCTION_NAMES, value);
    }
}
