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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.highlight.semantic.options;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.openide.util.SharedClassObject;

/**
 *
 * @author Sergey Grinev
 */
public class SemanticHighlightingOptions {

    private SemanticHighlightingOptions() {}

    private static final Preferences preferences = NbPreferences.forModule(SemanticHighlightingOptions.class);
    private static final String ENABLE_MARK_OCCURENCES = "EnableMarkOccurrences";
    private static final String KEEP_MARKS = "KeepMarks";
    
    private static boolean getOption(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    private static void setOption(String key, boolean value) {
        preferences.putBoolean(key, value);
    }
    
    public static boolean getEnableMarkOccurences() {
        return getOption(ENABLE_MARK_OCCURENCES, false);
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
}
