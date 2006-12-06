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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.LibrarySupport;
import org.netbeans.modules.languages.fold.DatabaseManager;
import org.netbeans.modules.languages.parser.ASTNode;
import org.netbeans.modules.languages.parser.PTPath;
import org.netbeans.modules.languages.parser.SToken;
import org.netbeans.modules.languages.parser.TokenInput;
import org.openide.ErrorManager;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;


/**
 *
 * @author Jan Jancura
 */
public class JavaScript {

    private static final String DOC = "org/netbeans/modules/languages/javascript/Documentation.xml";
    
    
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

    public static Runnable hyperlink (PTPath path) {
        SToken t = (SToken) path.getLeaf ();
        ASTNode n = path.size () > 1 ? 
            (ASTNode) path.get (path.size () - 2) :
            null;
        String name = t.getIdentifier ();
        List list = DatabaseManager.get (n, name, false);
        if (list.isEmpty ()) 
            list = DatabaseManager.get (DatabaseManager.FOLDER, name);
        if (list.isEmpty ()) return null;
        final Line.Part l = (Line.Part) list.get (0);
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
    
    public static String functionName (PTPath path) {
        ASTNode n = (ASTNode) path.getLeaf ();
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

    public static String objectName (PTPath path) {
        ASTNode n = (ASTNode) path.getLeaf ();
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
    
    public static List completionItems (PTPath path) {
        if (completionItems == null) {
            completionItems = new ArrayList ();
            completionItems.addAll (getLibrary ().getItems ("keyword"));
            completionItems.addAll (getLibrary ().getItems ("root"));
        }
        ArrayList l = new ArrayList ();
        Collection c = DatabaseManager.getIds ((ASTNode) path.get (path.size () - 2), true);
        l.addAll (c);
        c = DatabaseManager.getIds (DatabaseManager.FOLDER);
        l.addAll (c);
        l.addAll (completionItems);
        return l;
    }

    private static List completionDescriptions;

    public static List completionDescriptions (PTPath path) {
        if (completionDescriptions == null) {
            List tags = completionItems (path);
            tags = completionItems;
            completionDescriptions = new ArrayList (tags.size ());
            Iterator it = tags.iterator ();
            while (it.hasNext ()) {
                String tag = (String) it.next ();
                String description = getLibrary ().getProperty 
                    ("keyword", tag, "description");
                if (description != null) {
                    completionDescriptions.add (
                        "<html><b><font color=blue>" + tag + 
                        ": </font></b><font color=black> " + 
                        description + "</font></html>"
                    );
                } else {
                    description = getLibrary ().getProperty 
                        ("root", tag, "description");
                    if (description == null) 
                        completionDescriptions.add (
                            "<html><b><font color=black>" + tag + 
                            "</font></b></html>"
                        );
                    else
                        completionDescriptions.add (
                            "<html><b><font color=black>" + tag + 
                            ": </font></b><font color=black> " + 
                            description + "</font></html>"
                        );
                }
            }
        }
        ArrayList l = new ArrayList ();
        Collection c = DatabaseManager.getIds ((ASTNode) path.get (path.size () - 2), true);
        l.addAll (c);
        c = DatabaseManager.getIds (DatabaseManager.FOLDER);
        l.addAll (c);
        l.addAll (completionDescriptions);
        return l;
    }
    
    
    public static void performDeleteCurrentMethod (ASTNode node) {
        Lookup lookup = TopComponent.getRegistry().getActivated().getLookup();
        EditorCookie ec = (EditorCookie) lookup.lookup(EditorCookie.class);
        NbEditorDocument doc = (NbEditorDocument)ec.getDocument();
        JEditorPane[] panes = ec.getOpenedPanes();
        if (panes == null || panes.length == 0) {
            return;
        }
        int position = panes[0].getCaretPosition();
        PTPath path = node.findPath(position);
        ASTNode methodNode = null;
        for (Iterator iter = path.iterator(); iter.hasNext(); ) {
            Object obj = iter.next();
            if (!(obj instanceof ASTNode))
                break;
            ASTNode n = (ASTNode) obj;
            if ("FunctionDeclaration".equals(n.getNT())) { // NOI18N
                methodNode = n;
            } // if
        } // for
        if (methodNode != null) {
            try {
                doc.remove(methodNode.getOffset(), methodNode.getLength());
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
     
    public static boolean enabledDeleteCurrentMethod (ASTNode node) {
        Lookup lookup = TopComponent.getRegistry().getActivated().getLookup();
        EditorCookie ec = (EditorCookie) lookup.lookup(EditorCookie.class);
        NbEditorDocument doc = (NbEditorDocument)ec.getDocument();
        JEditorPane[] panes = ec.getOpenedPanes();
        if (panes == null || panes.length == 0) {
            return false;
        }
        int position = panes[0].getCaretPosition();
        PTPath path = node.findPath(position);
        for (Iterator iter = path.iterator(); iter.hasNext(); ) {
            Object obj = iter.next();
            if (!(obj instanceof ASTNode))
                return false;
            ASTNode n = (ASTNode) obj;
            if ("FunctionDeclaration".equals(n.getNT())) { // NOI18N
                return true;
            } // if
        } // for
        return false;
    }
    
    // helper methods ..........................................................
    
    private static LibrarySupport library;
    
    private static LibrarySupport getLibrary () {
        if (library == null)
            library = LibrarySupport.create (DOC);
        return library;
    }

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
