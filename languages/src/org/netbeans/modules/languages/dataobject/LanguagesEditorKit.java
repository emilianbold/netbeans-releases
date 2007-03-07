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

package org.netbeans.modules.languages.dataobject;

import java.util.Map;
import org.netbeans.modules.languages.*;
import javax.swing.Action;
import org.netbeans.api.languages.LanguagesManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JLabel;
import org.netbeans.api.languages.DatabaseManager;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.ext.ToolTipSupport;
import javax.swing.text.Document;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.UIManager;
import javax.swing.text.TextAction;
import org.netbeans.editor.PopupManager;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.ext.plain.PlainSyntax;
import org.netbeans.modules.editor.NbEditorUI;
import org.netbeans.modules.languages.features.BraceCompletionDeleteAction;
import org.netbeans.modules.languages.features.BraceCompletionInsertAction;
import org.netbeans.modules.languages.features.BraceHighlighting;
import org.netbeans.modules.languages.features.CollapseFoldTypeAction;
import org.netbeans.modules.languages.features.DatabaseManagerImpl;
import org.netbeans.modules.languages.LanguagesManagerImpl;
import org.netbeans.modules.languages.features.AnnotationManager;
import org.netbeans.modules.languages.features.ExpandFoldTypeAction;
import org.netbeans.modules.languages.features.HyperlinkListener;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.languages.features.IndentAction;
import org.netbeans.modules.languages.features.LanguagesGenerateFoldPopupAction;
import org.netbeans.modules.languages.features.MyFirstDrawLayer;
import org.netbeans.modules.languages.features.MySecondDrawLayer;


/**
 *
 * @author Jan Jancura
 */
public class LanguagesEditorKit extends NbEditorKit {

    private String mimeType;
    
    /** 
     * Creates a new instance of LanguagesEditorKit 
     */
    public LanguagesEditorKit (final String mimeType) { 
        this.mimeType = mimeType;
        //Settings.setValue (LanguagesEditorKit.class, SettingsNames.CODE_FOLDING_ENABLE, Boolean.TRUE);
        Settings.addInitializer (new Settings.Initializer () {
            public String getName() {
                return mimeType;
            }

            public void updateSettingsMap (Class kitClass, Map settingsMap) {
                if (kitClass != null && kitClass.equals (LanguagesEditorKit.class)) {
                    settingsMap.put (SettingsNames.CODE_FOLDING_ENABLE, Boolean.TRUE);
                }
            }
        });
    }
    
    private JLabel label;
    
    private JLabel createToolTipComponent () {
        if (label == null) {
            label = new JLabel () {
                public void setSize(int width, int height) {
                    if (getText () == null) {
                        super.setSize (width, height);
                        return;
                    }
                    int docLen = getText ().length ();
                    if (docLen > 0) { // nonzero length
                        Dimension prefSize = getPreferredSize();
                        if (width > prefSize.width) { // given width unnecessarily big
                            width = prefSize.width; // shrink the width to preferred
                            if (height >= prefSize.height) {
                                height = prefSize.height;
                            } else { // height not big enough
                                height = -1;
                            }

                        } else { // available width not enough - wrap lines
                            super.setSize(width, 100000);
//                            try {
                                //Rectangle r = modelToView(docLen - 1);
                                int prefHeight = getPreferredSize ().height;//r.y + r.height;
                                if (prefHeight < height) {
                                    height = prefHeight;

                                } else { // the given height is too small
                                    height = -1;
                                }
//                            } catch (BadLocationException e) {
//                            }
                        }
                    }

                    if (height >= 0) { // only for valid height
                        super.setSize(width, height);
                    } else { // signal that the height is too small to display tooltip
                        putClientProperty(PopupManager.Placement.class, null);
                    }
                }
            };

            // bugfix of #43174
            label.setActionMap (new ActionMap ());
            label.setInputMap (JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);

            Font font = UIManager.getFont ("ToolTip.font"); // NOI18N
            Color backColor = UIManager.getColor("ToolTip.background"); // NOI18N
            Color foreColor = UIManager.getColor("ToolTip.foreground"); // NOI18N

            if (font != null) {
                label.setFont(font);
            }
            if (foreColor != null) {
                label.setForeground(foreColor);
            }
            if (backColor != null) {
                label.setBackground(backColor);
            }

            label.setOpaque(true);
            label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(label.getForeground()),
                BorderFactory.createEmptyBorder(0, 3, 0, 3)
            ));
        }
        return label;
    }

    protected Action[] createActions() {
        Action[] myActions = new Action[] {
            new BraceCompletionInsertAction (),
            new BraceCompletionDeleteAction (),
            new IndentAction (),
            new LanguagesGenerateFoldPopupAction (),
        };
        return TextAction.augmentList (
            super.createActions (), 
            myActions
        );
    }
    
    public Action getActionByName(String name) {
        if (name.startsWith("Expand"))
            return new ExpandFoldTypeAction (name);
        if (name.startsWith("Collapse"))
            return new CollapseFoldTypeAction (name);
        return super.getActionByName (name);
    }
    
    protected EditorUI createEditorUI () {
        return new NbEditorUI () {
            private ToolTipSupport toolTipSupport;
            public ToolTipSupport getToolTipSupport() {
                if (toolTipSupport == null) {
                    toolTipSupport = new ToolTipSupport (this) {
                        public void setToolTipText (String text) {
                            if (text == null) return;
                            JLabel l = createToolTipComponent ();
                            l.setText (text);
                            setToolTip (l);
                        }
                    };
                }
                return toolTipSupport;
            }
        };
    }
    
    public Document createDefaultDocument() {
        Document doc = super.createDefaultDocument ();
        doc.putProperty("mimeType", mimeType); //NOI18N
        ((BaseDocument) doc).addLayer (
            new MyFirstDrawLayer (mimeType), 
            3000
        );
        ((BaseDocument) doc).addLayer (
            new MySecondDrawLayer (mimeType), 
            1500
        );
        new AnnotationManager (doc);
        ((DatabaseManagerImpl) DatabaseManager.getDefault ()).new Listener (doc);
        return doc;
    }
    
//    public Syntax createSyntax (Document doc) {
//        LanguagesSyntax syntax = (LanguagesSyntax) documentToSyntax.get (doc);
//        if (syntax == null) {
//            syntax = new LanguagesSyntax (doc);
//            documentToSyntax.put (doc, syntax);
//            syntax.init ();
//        }
//        return syntax;
//    }

    public Syntax createSyntax(Document doc) {
        return new PlainSyntax();
    }

    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new BraceHighlighting (doc);
    }
    
    public void install (JEditorPane c) {
        super.install (c);
        try {
            Language l = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).getLanguage (mimeType);
            //if (!l.supportsHyperlinks ()) return;
            HyperlinkListener hl = new HyperlinkListener (l);
            c.addMouseMotionListener (hl);
            c.addMouseListener (hl);
        } catch (ParseException ex) {
        }
    }
    
    public String getContentType() {
        return mimeType;
    }
    
    public Object clone () {
        return new LanguagesEditorKit (mimeType);
    }
}

