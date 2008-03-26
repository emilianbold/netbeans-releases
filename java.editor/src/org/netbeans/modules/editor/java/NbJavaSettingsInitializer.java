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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.java;

import java.util.Map;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.ext.java.JavaSettingsDefaults;
import org.netbeans.editor.ext.java.JavaSettingsNames;
import org.netbeans.modules.java.editor.options.JavaOptions;

/**
* Nb settings for Java.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbJavaSettingsInitializer extends Settings.AbstractInitializer {

    public static final String NAME = "nb-java-settings-initializer"; // NOI18N

    public NbJavaSettingsInitializer() {
        super(NAME);
    }

    /** Update map filled with the settings.
    * @param kitClass kit class for which the settings are being updated.
    *   It is always non-null value.
    * @param settingsMap map holding [setting-name, setting-value] pairs.
    *   The map can be empty if this is the first initializer
    *   that updates it or if no previous initializers updated it.
    */
    public void updateSettingsMap(Class kitClass, Map settingsMap) {

        if (kitClass == JavaKit.class) {
//            settingsMap.put(ExtSettingsNames.UPDATE_PD_AFTER_MOUNTING,
  //                          ExtSettingsDefaults.defaultUpdatePDAfterMounting);

            settingsMap.put(ExtSettingsNames.SHOW_DEPRECATED_MEMBERS,
                            ExtSettingsDefaults.defaultShowDeprecatedMembers);
            settingsMap.put(JavaOptions.GUESS_METHOD_ARGUMENTS_PROP,
                            Boolean.TRUE);
            
            settingsMap.put(SettingsNames.CODE_FOLDING_ENABLE, JavaSettingsDefaults.defaultCodeFoldingEnable);
            settingsMap.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_METHOD, JavaSettingsDefaults.defaultCodeFoldingCollapseMethod);
            settingsMap.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_INNERCLASS, JavaSettingsDefaults.defaultCodeFoldingCollapseInnerClass);
            settingsMap.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_IMPORT, JavaSettingsDefaults.defaultCodeFoldingCollapseImport);
            settingsMap.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_JAVADOC, JavaSettingsDefaults.defaultCodeFoldingCollapseJavadoc);
            settingsMap.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_INITIAL_COMMENT, JavaSettingsDefaults.defaultCodeFoldingCollapseInitialComment);
            
        }

    }
}
