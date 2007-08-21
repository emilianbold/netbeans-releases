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

package org.netbeans.modules.languages.features;

import java.awt.event.InputEvent;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.text.StyledDocument;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.languages.ASTItem;

import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.Highlighting;
import org.netbeans.api.languages.Highlighting.Highlight;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.Highlighting;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.ParserManagerImpl;

import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;


/**
 *
 * @author Administrator
 */
public class HyperlinkListener  implements MouseMotionListener,
MouseListener {

    private Highlight   highlight;
    private Runnable    runnable = null;

    public void mouseMoved (MouseEvent e) {
        JEditorPane c = (JEditorPane) e.getComponent ();
        final NbEditorDocument doc = (NbEditorDocument) c.getDocument ();
        if (highlight != null) highlight.remove ();
        highlight = null;
        if (((e.getModifiers() | e.getModifiersEx()) & InputEvent.CTRL_DOWN_MASK) != InputEvent.CTRL_DOWN_MASK) {
            return;
        }

        int offset = c.viewToModel (e.getPoint ());
        highlight (doc, offset);
        c.repaint ();
    }
    
    public void mouseReleased (MouseEvent e) {
        if (runnable != null) {
            runnable.run ();
            runnable = null;
        }
    }

    public void mouseClicked (MouseEvent e) {}
    public void mousePressed (MouseEvent e) {}
    public void mouseExited (MouseEvent e) {}
    public void mouseEntered (MouseEvent e) {}
    public void mouseDragged (MouseEvent e) {}
    
    private void highlight (
        final NbEditorDocument  doc,
        int                     offset
    ) {
        try {
            ASTNode ast = null;
            try {
                ast = ParserManagerImpl.get (doc).getAST ();
            } catch (ParseException ex) {
                ast = ex.getASTNode ();
            }
            if (ast == null) {
                String mimeType = (String) doc.getProperty ("mimeType");
                TokenHierarchy tokenHierarchy = TokenHierarchy.get (doc);
                if (doc instanceof NbEditorDocument)
                    ((NbEditorDocument) doc).readLock ();
                try {
                    TokenSequence tokenSequence = tokenHierarchy.tokenSequence ();
                    tokenSequence.move (offset);
                    tokenSequence.moveNext ();
                    Language language = LanguagesManager.getDefault ().getLanguage (mimeType);
                    Token token = tokenSequence.token ();
                    Feature hyperlinkFeature = language.getFeature 
                        ("HYPERLINK", token.id ().name ());
                    if (hyperlinkFeature == null) return;
                    ASTToken stoken = ASTToken.create (
                        tokenSequence.language ().mimeType (),
                        token.id ().name (),
                        token.text ().toString (),
                        tokenSequence.offset ()
                    );
                    highlight = Highlighting.getHighlighting (doc).highlight (
                        stoken,
                        getHyperlinkAS ()
                    );
                    runnable = (Runnable) hyperlinkFeature.getValue (Context.create (doc, tokenSequence));
                } finally {
                    if (doc instanceof NbEditorDocument)
                        ((NbEditorDocument) doc).readUnlock ();
                }
                return;
            }
            ASTPath path = ast.findPath (offset);
            if (path == null) return;
            int i, k = path.size ();
            for (i = 0; i < k; i++) {
                ASTPath p = path.subPath (i);
                Language language = LanguagesManager.getDefault ().getLanguage (p.getLeaf ().getMimeType ());
                Feature hyperlinkFeature = language.getFeature ("HYPERLINK", p);
                if (hyperlinkFeature == null) continue;
                highlight = Highlighting.getHighlighting (doc).highlight (
                    p.getLeaf (),
                    getHyperlinkAS ()
                );
                runnable = (Runnable) hyperlinkFeature.getValue (SyntaxContext.create (doc, p));
            }
            DatabaseContext root = DatabaseManager.getRoot (ast);
            if (root != null) {
                final DatabaseItem item = root.getDatabaseItem (offset);
                if (item != null && item instanceof DatabaseUsage) {
                    highlight = Highlighting.getHighlighting (doc).highlight (
                        path.getLeaf (),
                        getHyperlinkAS ()
                    );
                    runnable = new Runnable () {
                        public void run () {
                            DatabaseDefinition definition = ((DatabaseUsage) item).getDefinition ();
                            int definitionOffset = definition.getOffset ();
                            DataObject dobj = NbEditorUtilities.getDataObject (doc);
                            LineCookie lc = (LineCookie) dobj.getCookie (LineCookie.class);
                            Line.Set lineSet = lc.getLineSet ();
                            Line line = lineSet.getCurrent (NbDocument.findLineNumber (doc, definitionOffset));
                            int column = NbDocument.findLineColumn (doc, definitionOffset);
                            line.show (Line.SHOW_GOTO, column);
                        }
                    };
                }
                if (item == null) {
                    FileObject fileObject = NbEditorUtilities.getFileObject (doc);
                    ASTItem leaf = path.getLeaf ();
                    if (!(leaf instanceof ASTToken)) return;
                    String name = ((ASTToken) leaf).getIdentifier ();
                    try {
                        Map<FileObject,List<DatabaseDefinition>> map = Index.getGlobalItem (fileObject, name, false);
                        if (!map.isEmpty ()) {
                            final FileObject fo = map.keySet ().iterator ().next ();
                            final DatabaseDefinition definition = map.get (fo).iterator ().next ();
                            highlight = Highlighting.getHighlighting (doc).highlight (
                                path.getLeaf (),
                                getHyperlinkAS ()
                            );
                            runnable = new Runnable () {
                                public void run () {
                                    int definitionOffset = definition.getOffset ();
                                    try {
                                        DataObject dobj = DataObject.find (fo);
                                        EditorCookie ec = (EditorCookie) dobj.getCookie (EditorCookie.class);
                                        StyledDocument doc2 = ec.openDocument ();
                                        LineCookie lc = (LineCookie) dobj.getCookie (LineCookie.class);
                                        Line.Set lineSet = lc.getLineSet ();
                                        Line line = lineSet.getCurrent (NbDocument.findLineNumber (doc2, definitionOffset));
                                        int column = NbDocument.findLineColumn (doc2, definitionOffset);
                                        line.show (Line.SHOW_GOTO, column);
                                    } catch (IOException ex) {
                                        ex.printStackTrace ();
                                    }
                                }
                            };
                        }
                    } catch (FileNotParsedException ex) {
                    }
                }
            }
        } catch (ParseException ex) {
        }
        return;
    }
    
    private static AttributeSet hyperlinkAS = null;
    
    private static AttributeSet getHyperlinkAS () {
        if (hyperlinkAS == null) {
            SimpleAttributeSet as = new SimpleAttributeSet ();
            as.addAttribute (StyleConstants.Foreground, Color.blue);
            as.addAttribute (StyleConstants.Underline, Color.blue);
            hyperlinkAS = as;
        }
        return hyperlinkAS;
    }
    
    private static AttributeSet hyperlinkPressedAS = null;
    
    private static AttributeSet getHyperlinkPressedAS () {
        if (hyperlinkPressedAS == null) {
            SimpleAttributeSet as = new SimpleAttributeSet ();
            as.addAttribute (StyleConstants.Foreground, Color.red);
            as.addAttribute (StyleConstants.Underline, Color.red);
            hyperlinkPressedAS = as;
        }
        return hyperlinkPressedAS;
    }
}

