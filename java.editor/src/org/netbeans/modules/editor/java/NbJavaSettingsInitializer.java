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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.java;

import java.util.Map;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.ext.java.JavaSettingsDefaults;
import org.netbeans.editor.ext.java.JavaSettingsNames;

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
            
            settingsMap.put(SettingsNames.CODE_FOLDING_ENABLE, JavaSettingsDefaults.defaultCodeFoldingEnable);
            settingsMap.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_METHOD, JavaSettingsDefaults.defaultCodeFoldingCollapseMethod);
            settingsMap.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_INNERCLASS, JavaSettingsDefaults.defaultCodeFoldingCollapseInnerClass);
            settingsMap.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_IMPORT, JavaSettingsDefaults.defaultCodeFoldingCollapseImport);
            settingsMap.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_JAVADOC, JavaSettingsDefaults.defaultCodeFoldingCollapseJavadoc);
            settingsMap.put(JavaSettingsNames.CODE_FOLDING_COLLAPSE_INITIAL_COMMENT, JavaSettingsDefaults.defaultCodeFoldingCollapseInitialComment);
            
        }

    }
}
