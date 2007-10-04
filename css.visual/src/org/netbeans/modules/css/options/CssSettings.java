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
/*
 * CssSettings.java
 *
 * Created on December 10, 2004, 10:47 AM
 */

package org.netbeans.modules.css.options;

import java.util.Map;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.TokenContext;

/**
 * CSS editor setting initializer
 * @author Winston Prakash
 * @version 1.0
 */
public class CssSettings extends Settings.AbstractInitializer {

    /** Name of the editor setting initializer */
    private static final String SETTINGS_NAME = "css-editor-settings-initializer"; // NOI18N

    /** Creates a new instance of CssSettings */
    public CssSettings() {
        super(SETTINGS_NAME);
    }

    /** Update the settings map */
    public void updateSettingsMap(Class kitClass, Map settingsMap) {
        if (kitClass == null) return;

//        // Add the coloring information to the base kit
//        if (kitClass == BaseKit.class) {
//            new CssTokenColoringInitializer().updateSettingsMap(kitClass, settingsMap);
//        }
//        if ("org.netbeans.modules.css.editor.CssEditorKit".equals(kitClass.getName())) { // NOI18N
//            SettingsUtil.updateListSetting(settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
//                    new TokenContext[] { CssTokenContext.context });
//        }
//
//        settingsMap.put(SettingsNames.CODE_FOLDING_ENABLE, CssSettingsDefaults.defaultCodeFoldingEnable);
    }

}
