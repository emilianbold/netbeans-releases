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

package org.netbeans.modules.web.core.xmlsyntax;

import java.awt.Color;
import java.awt.Font;
import java.util.*;

import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenCategory;
import org.netbeans.modules.web.core.syntax.JSPKit;

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

