/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.text.syntax;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.Color;
import java.awt.Font;
import java.util.*;

import javax.swing.KeyStroke;

import org.openide.actions.ToolsAction;
import org.openide.windows.TopComponent;
import org.openide.util.HelpCtx;
import org.openide.actions.OpenAction;
import org.openide.actions.ViewAction;
import org.openide.util.actions.SystemAction;

import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.TokenCategory;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.ext.ExtSettingsNames;

import org.netbeans.modules.xml.core.actions.CollectSystemAction;

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

        if (kitClass == BaseKit.class) {

            new XMLTokenColoringInitializer().updateSettingsMap(kitClass, settingsMap);

            new DTDTokenColoringInitializer().updateSettingsMap(kitClass, settingsMap);
        }



        /** Add editor actions to DTD Kit. */
        if (kitClass == DTDKit.class) {

            // layer based default does not work!
            settingsMap.put (SettingsNames.ABBREV_MAP, getDTDAbbrevMap());

            SettingsUtil.updateListSetting (settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
                    new TokenContext[] { DTDTokenContext.context }
            );

        }


        /** Add editor actions to XML Kit. */
        if (kitClass == XMLKit.class) {

            // layer based default does not work!
            settingsMap.put (SettingsNames.ABBREV_MAP, getXMLAbbrevMap());

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
    
    // This must be synchronized with org/netbeans/modules/xml/text/resources/XMLEditor-abbreviations.xml!!!
    Map getXMLAbbrevMap() {
        Map xmlAbbrevMap = new TreeMap();

        xmlAbbrevMap.put ("?xm", "<?xml version=\"1.0\" encoding=\"UTF-8\">"); // NOI18N
        xmlAbbrevMap.put ("!do", "<!DOCTYPE "); // NOI18N
        xmlAbbrevMap.put ("!cd", "<![CDATA[|]]>"); // NOI18N
        xmlAbbrevMap.put ("!at", "<!ATTLIST |>"); // NOI18N
        xmlAbbrevMap.put ("!el", "<!ELEMENT |>"); // NOI18N
        xmlAbbrevMap.put ("!en", "<!ENTITY |>"); // NOI18N
        xmlAbbrevMap.put ("pu",  "PUBLIC \"|\""); // NOI18N
        xmlAbbrevMap.put ("sy",  "SYSTEM \"|\""); // NOI18N

        return xmlAbbrevMap;
    }

    // This must be synchronized with org/netbeans/modules/xml/text/resources/DTDEditor-abbreviations.xml!!!
    Map getDTDAbbrevMap() {
        Map dtdAbbrevMap = new TreeMap();

        dtdAbbrevMap.put ("!at", "<!ATTLIST |>"); // NOI18N
        dtdAbbrevMap.put ("!el", "<!ELEMENT |>"); // NOI18N
        dtdAbbrevMap.put ("!en", "<!ENTITY |>"); // NOI18N
        dtdAbbrevMap.put ("!no", "<!NOTATION |>"); // NOI18N
        dtdAbbrevMap.put ("cd",  "CDATA"); // NOI18N
        dtdAbbrevMap.put ("em",  "EMPTY"); // NOI18N
        dtdAbbrevMap.put ("en",  "ENTITY"); // NOI18N
        dtdAbbrevMap.put ("ens", "ENTITIES"); // NOI18N
        dtdAbbrevMap.put ("fi",  "#FIXED"); // NOI18N
        dtdAbbrevMap.put ("im",  "#IMPLIED"); // NOI18N
        dtdAbbrevMap.put ("nm",  "NMTOKEN"); // NOI18N
        dtdAbbrevMap.put ("nms", "NMTOKENS"); // NOI18N
        dtdAbbrevMap.put ("nn",  "NOTATION"); // NOI18N
        dtdAbbrevMap.put ("pc",  "#PCDATA"); // NOI18N
        dtdAbbrevMap.put ("pu",  "PUBLIC \"|\""); // NOI18N
        dtdAbbrevMap.put ("re",  "#REQUIRED"); // NOI18N
        dtdAbbrevMap.put ("rf",  "IDREF"); // NOI18N
        dtdAbbrevMap.put ("rfs", "IDREFS"); // NOI18N
        dtdAbbrevMap.put ("sy",  "SYSTEM \"|\""); // NOI18N

        return dtdAbbrevMap;
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

    
    /** XML colorings */
    static class XMLTokenColoringInitializer
    extends SettingsUtil.TokenColoringInitializer {

        public XMLTokenColoringInitializer() {
            super(XMLDefaultTokenContext.context);
        }

        public Object getTokenColoring(TokenContextPath tokenContextPath, TokenCategory tokenIDOrCategory, boolean printingSet) {
            // see XML_fontsColors.xml for actual values
            return new Coloring (null, Color.BLACK, null);
        }
    }

    /** DTD colorings */
    static class DTDTokenColoringInitializer
        extends SettingsUtil.TokenColoringInitializer {

        public DTDTokenColoringInitializer() {
            super(DTDTokenContext.context);
        }

        public Object getTokenColoring(TokenContextPath tokenContextPath, TokenCategory tokenIDOrCategory, boolean printingSet) {
            // see DTD_fontsColors.xml for actual values
            return new Coloring (null, Color.BLACK, null);
        }
    }

}
