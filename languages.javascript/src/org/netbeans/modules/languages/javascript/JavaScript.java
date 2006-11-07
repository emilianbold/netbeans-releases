/*
 * JavaScript.java
 *
 * Created on September 20, 2006, 1:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.languages.javascript;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.languages.fold.DatabaseManager;
import org.netbeans.modules.languages.parser.ASTNode;
import org.netbeans.modules.languages.parser.SToken;
import org.netbeans.modules.languages.parser.TokenInput;
import org.openide.ErrorManager;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;

/**
 *
 * @author Jan Jancura
 */
public class JavaScript {
    
    public static String bracketCompletion (TokenSequence ts) {
        String id = ts.token ().text ().toString ();
        //System.out.println("bracketCompletion " + ts.token ().id ().name ());
        if (id.equals ("("))
            return ")";
        if (id.equals ("\""))
            return "\"";
        if (id.equals ("\'"))
            return "\'";
        return null;
    }
    
    public static void onEnter (Document doc, Caret caret) {
        try {
            TokenSequence ts = getTokenSequence (doc, caret);
            int originalIndent = getIndent (ts);
            int i = 0;
            boolean ai = false, ai2 = true, lch = false;
            do {
                Token t = ts.token ();
                String id = t.text ().toString ();
                if (!t.id ().name ().equals ("js-whitespace"))
                    lch = false;
                if (id.equals ("{")) {
                    i++;
                    ai2 = false;
                    lch = true;
                } else
                if (id.equals ("("))
                    i++;
                else
                if (id.equals ("}") || id.equals (")"))
                    i--;
                else
                if (id.equals ("if") || id.equals ("while") || id.equals ("for"))
                    ai = true;
            } while (ts.moveNext () && ts.offset () < caret.getDot ());
            if (i == 0 && ai2 && ai)
                i++;
            int indent = originalIndent;
            if (i > 0) indent+=4;
            if (i < 0) indent-=4;
            indent (doc, caret, indent);
            int pos = caret.getDot ();
            if (lch) {
                doc.insertString (caret.getDot (), "\n", null);
                indent (doc, caret, originalIndent);
                doc.insertString (caret.getDot (), "}", null);
                caret.setDot (pos);
            }
        } catch (Exception ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
    
    public static ASTNode parseRegularExpression (
        TokenInput input, 
        Stack stack, 
        ASTNode parent
    ) {
        ASTNode nnode = ASTNode.create (
            input.next (1).getMimeType (), 
            "RegularExpression", 
            100, 
            parent,
            input.next (1).getOffset ()
        );
        parent.addNode (nnode);
        nnode.addToken (input.read ());
        while (!input.eof () && !input.next (1).getIdentifier ().equals ("/")) {
            if (input.next (1).getIdentifier ().equals ("\\"))
                nnode.addToken (input.read ());
            String s = input.next (1).getIdentifier ();
            if (s.equals ("\n") || s.equals ("\r")) return nnode;
            nnode.addToken (input.read ());
        }
        if (!input.eof () && input.next (1).getIdentifier ().equals ("/"))
            nnode.addToken (input.read ());
        if (input.next (1).getType ().equals ("js-identifier"))
            nnode.addToken (input.read ());
        return nnode;
    }

    public static Runnable hyperlink (SToken t) {
        String name = t.getIdentifier ();
        final Line.Part l = (Line.Part) DatabaseManager.get (name);
        if (l == null) return null;
        DataObject dataObject = (DataObject) l.getLine ().getLookup ().
            lookup (DataObject.class);
        EditorCookie ec = (EditorCookie) dataObject.getCookie (EditCookie.class);
        StyledDocument document = ec.getDocument ();
        int offset = NbDocument.findLineOffset (document, l.getLine ().getLineNumber ()) + l.getColumn ();
        if (offset == t.getOffset ()) return null;
        return new Runnable () {
            public void run () {
                l.getLine ().show (Line.SHOW_GOTO, l.getColumn ());
            }
        };
    }
    
    public static String functionName (ASTNode n) {
        String name = n.getTokenTypeIdentifier ("js-identifier");
        String parameters = "";
        ASTNode parametersNode = n.getNode ("FormalParameterList");
        if (parametersNode != null)
            parameters = parametersNode.getAsText ();
        if (name != null) return name + " (" + parameters + ")";
        ASTNode p = n.getParent ();
        while (p != null) {
            if (p.getNT ().equals ("AssignmentExpressionInitial") &&
                p.getNode ("AssignmentOperator") != null
            ) {
                return p.getNode ("ConditionalExpressionInitial").getAsText () + 
                    " (" + n.getNode ("FormalParameterList").getAsText () + ")";
            }
            if (p.getNT ().equals ("PropertyNameAndValue")) {
                return p.getNode ("PropertyName").getAsText () + 
                    " (" + n.getNode ("FormalParameterList").getAsText () + ")";
            }
            p = p.getParent ();
        }
        return "?";
    }

    public static String objectName (ASTNode n) {
        ASTNode p = n.getParent ();
        while (p != null) {
            if (p.getNT ().equals ("AssignmentExpressionInitial") &&
                p.getNode ("AssignmentOperator") != null
            ) {
                return p.getNode ("ConditionalExpressionInitial").getAsText ();
            }
            if (p.getNT ().equals ("PropertyNameAndValue")) {
                return p.getNode ("PropertyName").getAsText ();
            }
            p = p.getParent ();
        }
        return "?";
    }
    
    private static List completionItems;
    
    public static List completionItems (SToken t) {
        if (completionItems == null)
            completionItems = new ArrayList (XMLStorage.readDoc ());
        return completionItems;
    }

    public static List completionDescriptions (SToken t) {
        if (completionItems == null)
            completionItems = new ArrayList (XMLStorage.readDoc ());
        return completionItems;
    }
    
    
    // helper methods ..........................................................
    

    private static TokenSequence getTokenSequence (Document doc, Caret caret) {
        int ln = NbDocument.findLineNumber ((StyledDocument) doc, caret.getDot ()) - 1;
        int start = NbDocument.findLineOffset ((StyledDocument) doc, ln);
        TokenHierarchy th = TokenHierarchy.get (doc);
        TokenSequence ts = th.tokenSequence ();
        ts.move (start);
        return ts;
    }
    
    private static void indent (Document doc, Caret caret, int i) {
        StringBuilder sb = new StringBuilder ();
        while (i > 0) {
            sb.append (' ');i--;
        }
        try {
            doc.insertString (caret.getDot (), sb.toString (), null);
        } catch (BadLocationException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
    
    private static int getIndent (TokenSequence ts) {
        if (ts.token ().id ().name ().equals ("js-whitespace")) {
            String w = ts.token ().text ().toString ();
            int i = w.lastIndexOf ('\n');
            if (i >= 0)
                w = w.substring (i + 1);
            i = w.lastIndexOf ('\r');
            if (i >= 0)
                w = w.substring (i + 1);
            return w.length ();
        }
        return 0;
    }
}
