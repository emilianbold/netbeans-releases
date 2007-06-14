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
package org.netbeans.modules.xml.text.syntax;

import java.util.*;

import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.TokenContext;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;

/**
 * Editor settings defaults.
 * It shoudl be replaced by layer based "Defaults" to simplify
 * {@link TextEditorModuleInstall}.
 */
public class XMLSettingsInitializer extends Settings.AbstractInitializer {

    /** Name assigned to initializer */
    public static final String NAME = "xml-settings-initializer"; // NOI18N

    public XMLSettingsInitializer() {
        super(NAME);
    }

    public void updateSettingsMap (Class kitClass, Map settingsMap) {
        // editor breaks the contact, handle it somehow
        if (kitClass == null) return;

        /** Add editor actions to DTD Kit. */
        if (kitClass == DTDKit.class) {
            SettingsUtil.updateListSetting (settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
                    new TokenContext[] { DTDTokenContext.context }
            );
        }


        /** Add editor actions to XML Kit. */
        if (kitClass == XMLKit.class) {
            settingsMap.put(SettingsNames.CODE_FOLDING_ENABLE, Boolean.TRUE);
            
            SettingsUtil.updateListSetting (settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
                    new TokenContext[] { XMLDefaultTokenContext.context }
            );
            
            settingsMap.put(SettingsNames.IDENTIFIER_ACCEPTOR, getXMLIdentifierAcceptor());            
        }


        /* Allow '?' and '!' in abbrevirations. */
        if (kitClass == XMLKit.class || kitClass == DTDKit.class) {
            settingsMap.put(SettingsNames.ABBREV_RESET_ACCEPTOR,
                            new Acceptor() {
                                public boolean accept(char ch) {
                                    return AcceptorFactory.NON_JAVA_IDENTIFIER.accept(ch) && ch != '!' && ch != '?';
                                }
                            }
                           );
        }

    }
    
    /*
     * Identifiers accept all NameChar [4].
     */
    Acceptor getXMLIdentifierAcceptor() {
        
        return  new Acceptor() {
            public boolean accept(char ch) {
                switch (ch) {
                    case ' ': case '\t': case '\n': case '\r':          // WS
                    case '>': case '<': case '&': case '\'': case '"': case '/':
                    case '\\': // markup
                        return false;
                }

                return true;
            }
        };
    }
}
