/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.text.syntax;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.Color;
import java.awt.Font;
import java.util.*;

import javax.swing.KeyStroke;

import org.openide.windows.TopComponent;

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

public class XMLSettingsInitializer extends Settings.AbstractInitializer {

    /** Name assigned to initializer */
    public static final String NAME = "xml-settings-initializer"; // NOI18N

    public XMLSettingsInitializer() {
        super(NAME);
    }

    public void updateSettingsMap (Class kitClass, Map settingsMap) {
        if (kitClass == BaseKit.class) {

            new XMLTokenColoringInitializer().updateSettingsMap(kitClass, settingsMap);

            new DTDTokenColoringInitializer().updateSettingsMap(kitClass, settingsMap);
        }


        List commonActionNames = new ArrayList (Arrays.asList
                                                (new String[] {
                                                    BaseKit.formatAction,
                                                    null,
                                                    TopComponent.class.getName(),
                                                    null,
                                                    BaseKit.cutAction,
                                                    BaseKit.copyAction,
                                                    BaseKit.pasteAction,
                                                    null,
                                                    BaseKit.removeSelectionAction,
                                                }));

        if (kitClass == DTDKit.class) { // ----- DTDKit Settings ---------------

            settingsMap.put (SettingsNames.ABBREV_MAP, getDTDAbbrevMap());

            SettingsUtil.updateListSetting (settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
                    new TokenContext[] { DTDTokenContext.context }
            );

            List dtdActionNames = new ArrayList (Arrays.asList
                                                 (new String[] {
                                                     "org.netbeans.modules.xml.tools.actions.CheckDTDAction", // NOI18N
                                                     null,
                                                 }));
            dtdActionNames.addAll (commonActionNames);
            settingsMap.put (ExtSettingsNames.POPUP_MENU_ACTION_NAME_LIST, dtdActionNames);
        }


        /** Add editor actions to XML kit. */
        if (kitClass == XMLKit.class) {

            settingsMap.put (SettingsNames.ABBREV_MAP, getXMLAbbrevMap());

            SettingsUtil.updateListSetting (settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
                    new TokenContext[] { XMLTokenContext.context }
            );

            List xmlActionNames = new ArrayList (Arrays.asList
                                                 (new String[] {
                                                     "org.netbeans.modules.xml.tools.actions.CheckAction", // NOI18N
                                                     "org.netbeans.modules.xml.tools.actions.ValidateAction", // NOI18N
//                                                       "org.netbeans.modules.xml.action.FormatAction", // NOI18N
                                                     null,
                                                 }));
            xmlActionNames.addAll (commonActionNames);
            settingsMap.put (ExtSettingsNames.POPUP_MENU_ACTION_NAME_LIST, xmlActionNames);
            
            settingsMap.put(SettingsNames.MACRO_MAP, getXMLMacroMap());
            
            SettingsUtil.updateListSetting(settingsMap, SettingsNames.KEY_BINDING_LIST,
                getXMLKeyBindings());

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

    Map getXMLAbbrevMap() {
        Map xmlAbbrevMap = getDTDAbbrevMap();
        xmlAbbrevMap.put ("?xm", "<?xml version=\"1.0\"?>"); // NOI18N
        xmlAbbrevMap.put ("!do", "<!DOCTYPE "); // NOI18N
        xmlAbbrevMap.put ("!cd", "<![CDATA["); // NOI18N
        return xmlAbbrevMap;
    }

    Map getDTDAbbrevMap() {
        Map dtdAbbrevMap = new TreeMap ();
        dtdAbbrevMap.put ("!el", "<!ELEMENT "); // NOI18N
        dtdAbbrevMap.put ("!en", "<!ENTITY "); // NOI18N
        dtdAbbrevMap.put ("!at", "<!ATTLIST "); // NOI18N
        dtdAbbrevMap.put ("!no", "<!NOTATION "); // NOI18N
        dtdAbbrevMap.put ("pu", "PUBLIC "); // NOI18N
        dtdAbbrevMap.put ("sy", "SYSTEM "); // NOI18N
        dtdAbbrevMap.put ("cd", "CDATA"); // NOI18N
        dtdAbbrevMap.put ("pc", "#PCDATA"); // NOI18N
        dtdAbbrevMap.put ("an", "ANY"); // NOI18N
        dtdAbbrevMap.put ("em", "EMPTY"); // NOI18N
        dtdAbbrevMap.put ("fi", "#FIXED"); // NOI18N
        dtdAbbrevMap.put ("im", "#IMPLIED"); // NOI18N
        dtdAbbrevMap.put ("re", "#REQUIRED"); // NOI18N
        dtdAbbrevMap.put ("nm", "NMTOKEN"); // NOI18N
        dtdAbbrevMap.put ("nms", "NMTOKENS"); // NOI18N
        dtdAbbrevMap.put ("rf", "IDREF"); // NOI18N
        dtdAbbrevMap.put ("rfs", "IDREFS"); // NOI18N
        dtdAbbrevMap.put ("en", "ENTITY"); // NOI18N
        dtdAbbrevMap.put ("ens", "ENTITIES"); // NOI18N
        dtdAbbrevMap.put ("nn", "NOTATION"); // NOI18N

        return dtdAbbrevMap;
    }


    /*
     * Editor is bundled with one usefull macro, bind it
     */
    MultiKeyBinding[] getXMLKeyBindings() {
        return new MultiKeyBinding[] {
                   new MultiKeyBinding(
                       new KeyStroke[] {
                           KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK),
                       },
                       "macro-put-end-tag" // NOI18N
                   ),

               };
    }

    
    Map getXMLMacroMap() {
        Map xmlMacroMap = new HashMap();
        
        // the macro uses trick with marking current position with "<"
        // then select it and instruct all subsequent finds to use it
        // it is very sensitive for exact find action sematics
        
        // it destroys users clipboard content and search selections
        
        xmlMacroMap.put( "put-end-tag", "\"<\" selection-backward find-selection find-previous find-previous caret-forward caret-forward\n" + // NOI18N
                         "select-identifier copy-to-clipboard caret-backward caret-forward\n" + // NOI18N
                         "find-next caret-backward caret-forward\n" + // NOI18N
                         "\"/\" paste-from-clipboard \">\"\n" + // NOI18N
                         "find-previous caret-forward caret-backward"); // NOI18N
        
        return xmlMacroMap;
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

        Font boldFont = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);
        Font italicFont = SettingsDefaults.defaultFont.deriveFont(Font.ITALIC);
        Settings.Evaluator boldSubst = new SettingsUtil.FontStylePrintColoringEvaluator(Font.BOLD);
        Settings.Evaluator italicSubst = new SettingsUtil.FontStylePrintColoringEvaluator(Font.ITALIC);
        Settings.Evaluator lightGraySubst = new SettingsUtil.ForeColorPrintColoringEvaluator(Color.lightGray);

        Coloring commentColoring = new Coloring(italicFont, Coloring.FONT_MODE_APPLY_STYLE,
                            Color.gray, null);

        Coloring numbersColoring = new Coloring(null, Color.red, null);

        public XMLTokenColoringInitializer() {
            super(XMLTokenContext.context);
        }

        public Object getTokenColoring(TokenContextPath tokenContextPath,
        TokenCategory tokenIDOrCategory, boolean printingSet) {
            if (!printingSet) {
                switch (tokenIDOrCategory.getNumericID()) {
                    case XMLTokenContext.TAG_ID:
                        return new Coloring (null, Coloring.FONT_MODE_APPLY_STYLE,
                                       Color.blue, null);

                    case XMLTokenContext.PLAIN_ID:
                        return new Coloring (null, Color.black, null);

                    case XMLTokenContext.STRING_ID:
                        return new Coloring (null, Color.magenta, null);

                    case XMLTokenContext.SYMBOL_ID:
                        return new Coloring (boldFont, Color.black, null);

                    case XMLTokenContext.TARGET_ID:
                        return new Coloring (boldFont, Coloring.FONT_MODE_APPLY_STYLE,
                                       Color.red.darker(), null);

                    case XMLTokenContext.KW_ID:
                        return new Coloring (boldFont, Color.blue.darker().darker(), null);

                    case XMLTokenContext.COMMENT_ID:
                        return new Coloring (italicFont, Coloring.FONT_MODE_APPLY_STYLE,
                                       Color.lightGray, null);

                    case XMLTokenContext.ATT_ID:
                        return new Coloring (boldFont, Coloring.FONT_MODE_APPLY_STYLE,
                                       Color.green.darker().darker(), null);

                    case XMLTokenContext.REF_ID:
                        return new Coloring (null, Color.blue.brighter().brighter(), null);

                    case XMLTokenContext.CDATA_ID:
                        return new Coloring (null, Color.yellow.darker(), null);

                    case XMLTokenContext.ERROR_ID:
                        return new Coloring (null, Color.red, null);
                        
                    case XMLTokenContext.CDATA_MARKUP_ID:
                        return new Coloring (null, Color.green.darker(), null);

                }

            } else { // printing set
                switch (tokenIDOrCategory.getNumericID()) {

                    default:
                         return SettingsUtil.defaultPrintColoringEvaluator;
                }

            }

            return null;

        }

    }

    /** DTD colorings */
    static class DTDTokenColoringInitializer
    extends SettingsUtil.TokenColoringInitializer {

        Font boldFont = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);
        Font italicFont = SettingsDefaults.defaultFont.deriveFont(Font.ITALIC);
        Settings.Evaluator boldSubst = new SettingsUtil.FontStylePrintColoringEvaluator(Font.BOLD);
        Settings.Evaluator italicSubst = new SettingsUtil.FontStylePrintColoringEvaluator(Font.ITALIC);
        Settings.Evaluator lightGraySubst = new SettingsUtil.ForeColorPrintColoringEvaluator(Color.lightGray);

        Coloring commentColoring = new Coloring(italicFont, Coloring.FONT_MODE_APPLY_STYLE,
                            Color.gray, null);

        Coloring numbersColoring = new Coloring(null, Color.red, null);

        public DTDTokenColoringInitializer() {
            super(DTDTokenContext.context);
        }

        public Object getTokenColoring(TokenContextPath tokenContextPath,
        TokenCategory tokenIDOrCategory, boolean printingSet) {
            if (!printingSet) {
                switch (tokenIDOrCategory.getNumericID()) {
                    case DTDTokenContext.REF_ID:
                        return new Coloring(null, Color.blue.brighter(), null);

                    case DTDTokenContext.SYMBOL_ID:
                        return new Coloring(boldFont, Coloring.FONT_MODE_APPLY_STYLE,
                                                  Color.black, null);

                    case DTDTokenContext.TARGET_ID:
                        return new Coloring(boldFont, Coloring.FONT_MODE_APPLY_STYLE,
                                                  Color.red.darker(), null);

                    case DTDTokenContext.PLAIN_ID:
                        return new Coloring(null, Color.black, null);

                    case DTDTokenContext.COMMENT_ID:
                        return new Coloring(italicFont, Coloring.FONT_MODE_APPLY_STYLE,
                                                  Color.lightGray, null);

                    case DTDTokenContext.KW_ID:
                        return new Coloring(boldFont, Color.blue.darker().darker(), null);

                    case DTDTokenContext.ERROR_ID:
                        return new Coloring(null, Color.red, null);

                    case DTDTokenContext.STRING_ID:
                        return new Coloring (null, Color.magenta, null);

                }

            } else { // printing set
                switch (tokenIDOrCategory.getNumericID()) {

                    default:
                         return SettingsUtil.defaultPrintColoringEvaluator;
                }

            }

            return null;

        }

    }

} // end of inner class XMLSettingsInitializer
