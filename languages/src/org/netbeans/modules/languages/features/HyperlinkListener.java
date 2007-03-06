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

import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.Highlighting;
import org.netbeans.api.languages.LanguagesManager;
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
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManagerImpl;
import org.netbeans.modules.languages.ParserManagerImpl;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JEditorPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;


/**
 *
 * @author Administrator
 */
public class HyperlinkListener  implements MouseMotionListener,
MouseListener {

    private Context      context = null;
    private Language    language;
    private Runnable    runnable = null;

    public HyperlinkListener (Language l) {
        language = l;
    }

    public void mouseMoved (MouseEvent e) {
        JEditorPane c = (JEditorPane) e.getComponent ();
        NbEditorDocument doc = (NbEditorDocument) c.getDocument ();
        if (!e.isControlDown ()) {
            if (context != null)
                removeHighlihgt (doc);
            return;
        }

        Object[] r = findEvaluator (
            doc,
            c.viewToModel (e.getPoint ())
        );

        if (context != null && (r == null || context != r [0])) {
            removeHighlihgt (doc);
        }
        if (r != null) {
            Feature hyperlink = (Feature) r [1];
            runnable = (Runnable) hyperlink.getValue ((Context) r [0]);
            if (runnable != null) {
                context = (Context) r [0];
                highlight (doc);
            }
        }
        c.repaint ();
    }

    public void mouseClicked (MouseEvent e) {
    }

    public void mouseReleased (MouseEvent e) {
        if (context == null) return;
        if (runnable != null)
            runnable.run ();
        JEditorPane c = (JEditorPane) e.getComponent ();
        NbEditorDocument doc = (NbEditorDocument) c.getDocument ();
        runnable = null;
        removeHighlihgt (doc);
        c.repaint ();
    }
    
    public void mousePressed (MouseEvent e) {
        if (context == null) return;
        JEditorPane c = (JEditorPane) e.getComponent ();
        NbEditorDocument doc = (NbEditorDocument) c.getDocument ();
        highlight (doc);
        c.repaint ();
    }
    
    public void mouseExited (MouseEvent e) {}
    public void mouseEntered (MouseEvent e) {}
    public void mouseDragged (MouseEvent e) {}

    private void highlight (NbEditorDocument doc) {
        if (context instanceof SyntaxContext) {
            Object o = null;
            o = ((SyntaxContext) context).getASTPath ().getLeaf ();
            if (o instanceof ASTToken)
                Highlighting.getHighlighting (doc).highlight (
                    (ASTToken) o,
                    getHyperlinkPressedAS ()
                );
            else
                Highlighting.getHighlighting (doc).highlight (
                    (ASTNode) o,
                    getHyperlinkPressedAS ()
                );
        } else {
            TokenSequence ts = context.getTokenSequence ();
            Token t = ts.token ();
            ASTToken stoken = ASTToken.create (
                ts.language ().mimeType (),
                t.id ().name (),
                t.text ().toString (),
                ts.offset ()
            );
            Highlighting.getHighlighting (doc).highlight (
                stoken,
                getHyperlinkPressedAS ()
            );
        }
    }

    private void removeHighlihgt (NbEditorDocument doc) {
        if (context instanceof SyntaxContext) {
            Object o = null;
            o = ((SyntaxContext) context).getASTPath ().getLeaf ();
            if (o instanceof ASTToken)
                Highlighting.getHighlighting (doc).removeHighlight (
                    (ASTToken) o
                );
            else
                Highlighting.getHighlighting (doc).removeHighlight (
                    (ASTNode) o
                );
        } else {
            TokenSequence ts = context.getTokenSequence ();
            Token t = ts.token ();
            ASTToken stoken = ASTToken.create (
                ts.language ().mimeType (),
                t.id ().name (),
                t.text ().toString (),
                ts.offset ()
            );
            Highlighting.getHighlighting (doc).highlight (
                stoken,
                getHyperlinkPressedAS ()
            );
        }
        context = null;
    }
    
    private Object[] findEvaluator (
        NbEditorDocument    doc,
        int                 offset
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
                TokenSequence tokenSequence = tokenHierarchy.tokenSequence ();
                tokenSequence.move (offset);
                tokenSequence.moveNext ();
                Language l = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).getLanguage (mimeType);
                Token token = tokenSequence.token ();
                Feature hyperlink = language.getFeature 
                    (Language.HYPERLINK, token.id ().name ());
                if (hyperlink != null) return new Object[] {Context.create (doc, tokenSequence), hyperlink};
                return null;
            }
            ASTPath path = ast.findPath (offset);
            if (path == null) return null;
            int i, k = path.size ();
            for (i = 0; i < k; i++) {
                ASTPath p = path.subPath (i);
                Feature hyperlink = language.getFeature (Language.HYPERLINK, p);
                if (hyperlink != null) 
                    return new Object[] {SyntaxContext.create (doc, p), hyperlink};
            }
        } catch (ParseException ex) {
        }
        return null;
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

