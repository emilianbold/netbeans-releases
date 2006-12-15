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

package org.netbeans.modules.web.core.syntax.settings;

import org.netbeans.modules.web.core.syntax.settings.JspSettings;
import org.netbeans.modules.web.core.syntax.*;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import javax.swing.KeyStroke;
import java.util.*;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.ext.html.HTMLSettingsInitializer;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.ext.java.JavaSettingsDefaults;
import org.netbeans.editor.ext.java.JavaSettingsNames;
import org.netbeans.modules.editor.java.JavaKit;

public class JspMultiSettingsInitializer extends Settings.AbstractInitializer {

    /** Name assigned to initializer */
    public static final String NAME = "jsp-multi-settings-initializer"; // NOI18N

    private static final int ALT_MASK = System.getProperty("mrj.version") != null ?
        InputEvent.CTRL_MASK : InputEvent.ALT_MASK;
    
    private static boolean isMac = System.getProperty("mrj.version") != null;
    
    public JspMultiSettingsInitializer() {
        super(NAME);
    }

    public void updateSettingsMap (Class kitClass, Map settingsMap) {
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	 
	if (kitClass == JavaKit.class) {
            settingsMap.put(JavaSettingsNames.INDENT_HOT_CHARS_ACCEPTOR,
                    JavaSettingsDefaults.defaultIndentHotCharsAcceptor);
        }
        
        // Jsp Settings
        if (kitClass == JSPKit.class) {
            SettingsUtil.updateListSetting(settingsMap, SettingsNames.KEY_BINDING_LIST,
                    new MultiKeyBinding[] {
                        new MultiKeyBinding(
                           KeyStroke.getKeyStroke(KeyEvent.VK_O, ALT_MASK),
                            JavaKit.gotoSourceAction
                        ),
                        new MultiKeyBinding(
                            KeyStroke.getKeyStroke(KeyEvent.VK_G, ALT_MASK | (isMac ? InputEvent.SHIFT_MASK : 0)),
                            org.netbeans.editor.ext.ExtKit.gotoDeclarationAction
                        ),
                        new MultiKeyBinding(
                            KeyStroke.getKeyStroke(KeyEvent.VK_B,
                            mask),
                            JavaKit.gotoSuperImplementationAction
                        )
            }
            );
            
            settingsMap.put(JavaSettingsNames.PAIR_CHARACTERS_COMPLETION,
                        JavaSettingsDefaults.defaultPairCharactersCompletion);

            //enable code folding
            settingsMap.put(SettingsNames.CODE_FOLDING_ENABLE, JavaSettingsDefaults.defaultCodeFoldingEnable);
            settingsMap.put(JspSettings.CODE_FOLDING_UPDATE_TIMEOUT, JspSettings.defaultCodeFoldingUpdateInterval);
            settingsMap.put(JspSettings.CARET_SIMPLE_MATCH_BRACE, JspSettings.defaultCaretSimpleMatchBrace);
            
//            settingsMap.put (org.netbeans.editor.SettingsNames.ABBREV_MAP, getJSPAbbrevMap());
            
            settingsMap.put(SettingsNames.IDENTIFIER_ACCEPTOR,
                            HTMLSettingsInitializer.HTML_IDENTIFIER_ACCEPTOR);

        }
    }

    Map getJSPAbbrevMap() {
        Map jspAbbrevMap = new TreeMap ();
        // <jsp:something tags
        jspAbbrevMap.put ("jspu", "<jsp:useBean id=\"|\" type=\"\"/>");        // NOI18N
        jspAbbrevMap.put ("jspg", "<jsp:getProperty name=\"|\" property=\"\"/>");  // NOI18N
        jspAbbrevMap.put ("jsps", "<jsp:setProperty name=\"|\" property=\"\"/>");  // NOI18N
        jspAbbrevMap.put ("jspi", "<jsp:include page=\"|\"/>");      // NOI18N
        jspAbbrevMap.put ("jspf", "<jsp:forward page=\"|\"/>");      // NOI18N
        jspAbbrevMap.put ("jspp", "<jsp:plugin type=\"|\" code=\"\" codebase=\"\">\n</jsp:plugin>");       // NOI18N
        // taglib
        jspAbbrevMap.put ("tglb", "<%@taglib uri=\"|\"%>");         // NOI18N
        // <%@ page tags
        jspAbbrevMap.put ("pg", "<%@page |%>");                   // NOI18N
        jspAbbrevMap.put ("pgl", "<%@page language=\"java\"%>");       // NOI18N
        jspAbbrevMap.put ("pgex", "<%@page extends=\"|\"%>");       // NOI18N
        jspAbbrevMap.put ("pgim", "<%@page import=\"|\"%>");        // NOI18N
        jspAbbrevMap.put ("pgs", "<%@page session=\"false\"%>");        // NOI18N
        jspAbbrevMap.put ("pgb", "<%@page buffer=\"|kb\"%>");         // NOI18N
        jspAbbrevMap.put ("pga", "<%@page autoFlush=\"false\"%>");      // NOI18N
        jspAbbrevMap.put ("pgin", "<%@page info=\"|\"%>");          // NOI18N
        jspAbbrevMap.put ("pgit", "<%@page isThreadSafe=\"false\"%>");  // NOI18N
        jspAbbrevMap.put ("pgerr", "<%@page errorPage=\"|\"%>");    // NOI18N
        jspAbbrevMap.put ("pgc", "<%@page contentType=\"|\"%>");    // NOI18N
        jspAbbrevMap.put ("pgie", "<%@page isErrorPage=\"true\"%>");   // NOI18N
        // common java abbrevs
        jspAbbrevMap.put ("rg", "request.getParameter(\"|\")");     // NOI18N
        jspAbbrevMap.put ("sg", "session.getAttribute(\"|\")");         // NOI18N
        jspAbbrevMap.put ("sp", "session.setAttribute(\"|\", )");         // NOI18N
        jspAbbrevMap.put ("sr", "session.removeAttribute(\"|\")");      // NOI18N
        jspAbbrevMap.put ("pcg", "pageContext.getAttribute(\"|\")");// NOI18N
        jspAbbrevMap.put ("pcgn", "pageContext.getAttributeNamesInScope(|)");// NOI18N
        jspAbbrevMap.put ("pcgs", "pageContext.getAttributesScope(\"|\")");// NOI18N
        jspAbbrevMap.put ("pcr", "pageContext.removeAttribute(\"|\")");// NOI18N
        jspAbbrevMap.put ("pcs", "pageContext.setAttribute(\"|\", )");// NOI18N
        jspAbbrevMap.put ("ag", "application.getAttribute(\"|\")");     // NOI18N
        jspAbbrevMap.put ("ap", "application.putAttribute(\"|\", )");     // NOI18N
        jspAbbrevMap.put ("ar", "application.removeAttribute(\"|\")");  // NOI18N
        jspAbbrevMap.put ("oup", "out.print(\"|\")");               // NOI18N
        jspAbbrevMap.put ("oupl", "out.println(\"|\")");            // NOI18N
        jspAbbrevMap.put ("cfgi", "config.getInitParameter(\"|\")");// NOI18N

        return jspAbbrevMap;
    }


}

