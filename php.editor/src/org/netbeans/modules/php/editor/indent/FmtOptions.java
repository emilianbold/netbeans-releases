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
package org.netbeans.modules.php.editor.indent;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.editor.settings.SimpleValueNames;

//import static org.netbeans.modules.javascript.editing.options.CodeStyle.*;

/**
 *
 * @author phrebejk
 * 
 * @todo Add an RHTML options category, such that I can see the effects of
 *   switching the RHTML toggles?
 */
public class FmtOptions {

    public static final String expandTabToSpaces = SimpleValueNames.EXPAND_TABS;
    public static final String tabSize = SimpleValueNames.TAB_SIZE;
    public static final String spacesPerTab = SimpleValueNames.SPACES_PER_TAB;
    public static final String indentSize = SimpleValueNames.INDENT_SHIFT_WIDTH;
    public static final String continuationIndentSize = "continuationIndentSize"; //NOI18N
    public static final String reformatComments = "reformatComments"; //NOI18N
    public static final String indentHtml = "indentHtml"; //NOI18N
    public static final String rightMargin = SimpleValueNames.TEXT_LIMIT_WIDTH;
    
    private FmtOptions() {}

    public static int getDefaultAsInt(String key) {
        return Integer.parseInt(defaults.get(key));
    }
    
    public static boolean getDefaultAsBoolean(String key) {
        return Boolean.parseBoolean(defaults.get(key));
    }
        
    public static String getDefaultAsString(String key) {
        return defaults.get(key);
    }
    
    // Private section ---------------------------------------------------------
    
    private static final String TRUE = "true";      // NOI18N
    private static final String FALSE = "false";    // NOI18N
    
    private static Map<String,String> defaults;
    
    static {
        createDefaults();
    }
    
    private static void createDefaults() {
        String defaultValues[][] = {
            { expandTabToSpaces, TRUE}, //NOI18N
            { tabSize, "8"}, //NOI18N
            { indentSize, "4"}, //NOI18N
            { continuationIndentSize, "4"}, //NOI18N
            { reformatComments, FALSE }, //NOI18N
            { indentHtml, TRUE }, //NOI18N
            { rightMargin, "80"}, //NOI18N
        };
        
        defaults = new HashMap<String,String>();
        
        for (java.lang.String[] strings : defaultValues) {
            defaults.put(strings[0], strings[1]);
        }

    }
    
}
