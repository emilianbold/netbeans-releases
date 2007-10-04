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

package org.netbeans.modules.web.core.xmlsyntax;

import java.util.*;

import org.netbeans.editor.Settings;

/** Initializer class for settings of JSP editor with XML content. */

@Deprecated()
public class JspXMLSettingsInitializer extends Settings.AbstractInitializer {

    /** Name assigned to initializer */
    public static final String NAME = "jsp-xml-settings-initializer";    // NOI18N

    public JspXMLSettingsInitializer() {
        super(NAME);
    }

    public void updateSettingsMap (Class kitClass, Map settingsMap) {
        // Jsp Colorings
//        if (kitClass == BaseKit.class) {
//            new JspXMLTokenColoringInitializer().updateSettingsMap(kitClass, settingsMap);
//        }
//
//        // Jsp Settings
//        if (kitClass == JSPKit.class) {
//            
//            SettingsUtil.updateListSetting(settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
//                new TokenContext[] { JspXMLTokenContext.context });
//        }
    }

//    static class JspXMLTokenColoringInitializer
//    extends SettingsUtil.TokenColoringInitializer {
//
//        private Coloring errorColoring = new Coloring(null, Color.red, null);
//
//        private Font italicFont = SettingsDefaults.defaultFont.deriveFont (Font.ITALIC);
//
//        public JspXMLTokenColoringInitializer() {
//            super(JspXMLTokenContext.context);
//        }
//
//        public Object getTokenColoring(TokenContextPath tokenContextPath,
//        TokenCategory tokenIDOrCategory, boolean printingSet) {
//
//            if (tokenContextPath == JspXMLTokenContext.contextPath) {
//                 if (!printingSet) {
//                      switch (tokenIDOrCategory.getNumericID()) {
//                           case JspXMLTokenContext.ERROR_ID:
//                               return errorColoring;
//                      }
//
//                } else { // printing set
//                     return SettingsUtil.defaultPrintColoringEvaluator;
//                }
//
//            } else if (tokenContextPath == JspXMLTokenContext.xmlContextPath) {
//                // XML token colorings
//                if (!printingSet) {
//                    return new SettingsUtil.TokenColoringEvaluator(
//                        tokenContextPath.getParent().getFullTokenName(tokenIDOrCategory),
//                        null, printingSet);
//
//                } else { // printing set
//                     return SettingsUtil.defaultPrintColoringEvaluator;
//                }
//            }
//
//            return null;
//
//        }
//
//    }

}

