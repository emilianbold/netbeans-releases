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

package org.netbeans.modules.html.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.CompletionItem;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.LibrarySupport;
import org.openide.ErrorManager;
import org.openide.text.NbDocument;


/**
 *
 * @author Jan Jancura
 */
public class HTML {
    
//    private static final String HTML40DOC = "modules/ext/html40.zip";
    private static final String HTML401 = "org/netbeans/modules/html/editor/resources/HTML401.xml";
    
    
    public static Object[] readJavaScript (CharInput input) {
        while (!input.eof ()) {
            while (input.next () != '<' && !input.eof ())
                input.read ();
            int start = input.getIndex ();
            input.read ();
            if (input.next () != '/') continue;
            input.read ();
            if (Character.toLowerCase (input.next ()) != 's') continue;
            input.read ();
            if (Character.toLowerCase (input.next ()) != 'c') continue;
            input.read ();
            if (Character.toLowerCase (input.next ()) != 'r') continue;
            input.read ();
            if (Character.toLowerCase (input.next ()) != 'i') continue;
            input.read ();
            if (Character.toLowerCase (input.next ()) != 'p') continue;
            input.read ();
            if (Character.toLowerCase (input.next ()) != 't') continue;
            input.read ();
            while (Character.isWhitespace (input.next ()))
                input.read ();
            if (input.next () != '>') continue;
            input.read ();
            input.setIndex (start);
            break;
        }
        return new Object[] {
            ASTToken.create ("text/html", "javascript", "", 0),
            "DEFAULT"
        };
    }
    
    public static Object[] readCSS (CharInput input) {
        while (!input.eof ()) {
            while (input.next () != '<' && !input.eof ())
                input.read ();
            int start = input.getIndex ();
            input.read ();
            if (input.next () != '/') continue;
            input.read ();
            if (Character.toLowerCase (input.next ()) != 's') continue;
            input.read ();
            if (Character.toLowerCase (input.next ()) != 't') continue;
            input.read ();
            if (Character.toLowerCase (input.next ()) != 'y') continue;
            input.read ();
            if (Character.toLowerCase (input.next ()) != 'l') continue;
            input.read ();
            if (Character.toLowerCase (input.next ()) != 'e') continue;
            input.read ();
            while (Character.isWhitespace (input.next ()))
                input.read ();
            if (input.next () != '>') continue;
            input.read ();
            input.setIndex (start);
            break;
        }
        return new Object[] {
            ASTToken.create ("text/html", "css", "", 0),
            "DEFAULT"
        };
    }

    
    // tag completion ..........................................................
    
    public static String complete (Context context) {
        TokenSequence ts = context.getTokenSequence ();
        ts.moveNext ();
        Token t = ts.token ();
        if (t == null) return null;
        String identifier = t.text ().toString ();
        if (!identifier.equals (">")) return null;
        do {
            if (!ts.movePrevious ()) return null;
        } while (!t.id ().name ().equals ("html_element_name"));
        String et = getLibrary ().getProperty ("TAG", identifier, "endTag");
        if (!ts.movePrevious ()) return null;
        if (!ts.token ().text ().toString ().equals ("<")) return null;
        return "</" + identifier + ">";
    }
    
    
    // indent ..................................................................
    
    public static void indent (Context context) {
        TokenSequence ts = context.getTokenSequence ();
        Document doc = context.getDocument ();
        int indent;
        Token t;
        do {
            ts.movePrevious ();
            t = ts.token ();
        } while (t.text ().toString ().trim ().length () == 0);
        String text = t.text ().toString ();
        String type = t.id ().name ();
        int ln = NbDocument.findLineNumber ((StyledDocument) doc, ts.offset ());
        int start = NbDocument.findLineOffset ((StyledDocument) doc, ln);
        if (text.equals (">") || text.equals ("/>")) {
            do {
                if (!ts.movePrevious ()) break;
                t = ts.token ();
            } while (!t.text ().toString ().equals ("<"));
            indent = getIndent (ts, doc);
        } else
        if (type.equals ("html_attribute_value") || 
            type.equals ("html_attribute_name") ||
            type.equals ("html_element_name") ||
            type.equals ("html_operator")
        ) {
            do {
                if (!ts.movePrevious ()) break;
                t = ts.token ();
            } while (
                !t.text ().toString ().equals ("<") &&
                ts.offset () > start
            );
            if (t.text ().toString ().equals ("<"))
                indent = getIndent (ts, doc) + 4;
            else {
                ts.moveNext ();
                indent = getIndent (ts, doc);
            }
        } else
        if (text.equals ("<")) {
            indent = getIndent (ts, doc) + 4;
        } else
            indent = getIndent (ts, doc);
        indent (doc, context.getJTextComponent ().getCaret ().getDot (), indent);
    }
    
    private static int getIndent (TokenSequence ts, Document doc) {
        int ln = NbDocument.findLineNumber ((StyledDocument) doc, ts.offset ());
        int start = NbDocument.findLineOffset ((StyledDocument) doc, ln);
        ts.move (start);
        ts.moveNext ();
        if (ts.token ().text ().toString ().trim ().length () == 0)
            ts.moveNext ();
        return ts.offset () - start;
    }
    
    private static void indent (Document doc, int offset, int i) {
        StringBuilder sb = new StringBuilder ();
        while (i > 0) {
            sb.append (' ');i--;
        }
        try {
            doc.insertString (offset, sb.toString (), null);
        } catch (BadLocationException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
    
//    public static Runnable hyperlink (PTPath path) {
//        ASTToken t = (ASTToken) path.getLeaf ();
//        String s = t.getIdentifier ();
//        s = s.substring (1, s.length () - 1).trim ();
//        if (!s.endsWith (")")) return null;
//        s = s.substring (0, s.length () - 1).trim ();
//        if (!s.endsWith ("(")) return null;
//        s = s.substring (0, s.length () - 1).trim ();
//        final Line l = (Line) DatabaseManager.get (s);
//        if (l != null)
//            return new Runnable () {
//                public void run () {
//                    l.show (l.SHOW_SHOW);
//                }
//            };
//        return null;
//    }

    
    // completion ..............................................................
    
    public static List tags (Context context) {
        if (context instanceof SyntaxContext) return Collections.EMPTY_LIST;
        List tags = getLibrary ().getItems ("TAG");
        List items = new ArrayList (tags.size ());
        Iterator it = tags.iterator ();
        while (it.hasNext ()) {
            String tag = (String) it.next ();
            String description = getLibrary ().getProperty 
                ("TAG", tag, "description");
            items.add (CompletionItem.create (tag.toUpperCase ()));
        }
        return items;
    }

    public static List attributes (Context context) {
        if (context instanceof SyntaxContext) return Collections.EMPTY_LIST;
        String tagName = tagName (context.getTokenSequence ());
        if (tagName == null) return Collections.EMPTY_LIST;
        List r = getLibrary ().getItems (tagName);
        if (r == null) return Collections.EMPTY_LIST;
        //S ystem.out.println("attributes " + r);
        List items = new ArrayList (r.size ());
        Iterator it = r.iterator ();
        while (it.hasNext ()) {
            String tag = (String) it.next ();
            String description = getLibrary ().getProperty 
                (tagName, tag, "description");
            items.add (CompletionItem.create (tag.toUpperCase ()));
        }
        //S ystem.out.println("attributeDescriptions " + attributeDescriptions);
        return items;
    }
    

    // marks ...................................................................
    
    public static boolean isDeprecatedAttribute (Context context) {
        Token t = context.getTokenSequence ().token ();
        if (t == null) return false;
        String attribName = t.text ().toString ().toLowerCase ();
        String tagName = tagName (context.getTokenSequence ());
        if (tagName == null) return false;
        return "true".equals (getLibrary ().getProperty (tagName, attribName, "deprecated"));
    }

    public static boolean isDeprecatedTag (Context context) {
        Token t = context.getTokenSequence ().token ();
        if (t == null) return false;
        String tagName = t.text ().toString ().toLowerCase ();
        return "true".equals (getLibrary ().getProperty ("TAG", tagName, "deprecated"));
    }

    public static boolean isEndTagRequired (Context context) {
        Token t = context.getTokenSequence ().token ();
        if (t == null) return false;
        return isEndTagRequired (t.id ().name ().toLowerCase ());
    }

    static boolean isEndTagRequired (String tagName) {
        String v = getLibrary ().getProperty ("TAG", tagName, "endTag");
        return !"O".equals (v) && !"F".equals (v);
    }
    
    static boolean isSupportedTag(String tagName) {
        return getLibrary().getProperty("TAG", tagName, "key") != null;
    }
    
    public static ASTNode process (SyntaxContext context) {
        ASTNode n = (ASTNode) context.getASTPath ().getRoot ();
        List l = new ArrayList ();
        resolve (n, new Stack (), l, true);
        return ASTNode.create (n.getMimeType (), n.getNT (), l, n.getOffset ());
    }
    
    
    // private methods .........................................................

    private static String tagName (TokenSequence ts) {
        while (!ts.token ().id ().name ().equals ("html_element_name"))
            if (!ts.movePrevious ()) break;
        if (!ts.token ().id ().name ().equals ("html_element_name")) 
            return null;
        return ts.token ().text ().toString ().toLowerCase ();
    }
    
    private static LibrarySupport library;
    
    private static LibrarySupport getLibrary () {
        if (library == null)
            library = LibrarySupport.create (HTML401);
        return library;
    }
    
    private static ASTNode clone (String mimeType, String nt, int offset, List children) {
        Iterator it = children.iterator ();
        List l = new ArrayList ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof ASTToken)
                l.add (clone ((ASTToken) o));
            else
                l.add (clone ((ASTNode) o));
        }
        return ASTNode.create (mimeType, nt, l, offset);
    }
    
    private static ASTNode clone (ASTNode n) {
        return clone (n.getMimeType (), n.getNT (), n.getOffset (), n.getChildren ());
    }
    
    private static ASTToken clone (ASTToken token) {
        List<ASTItem> children = new ArrayList ();
        Iterator<ASTItem> it = token.getChildren ().iterator ();
        while (it.hasNext ()) {
            ASTItem item = it.next ();
            if (item instanceof ASTNode)
                children.add (clone ((ASTNode) item));
            else
                children.add (clone ((ASTToken) item));
        }
        return ASTToken.create (
            token.getMimeType (),
            token.getType (),
            token.getIdentifier (),
            token.getOffset (),
            token.getLength (),
            children
        );
    }
    
    private static ASTNode clone (ASTNode n, String nt) {
        return clone (n.getMimeType (), nt, n.getOffset (), n.getChildren ());
    }
    
    public static void resolve (ASTNode n, Stack s, List l, boolean findUnpairedTags) {
        Iterator<ASTItem> it = n.getChildren ().iterator ();
        while (it.hasNext ()) {
            ASTItem item = it.next ();
            if (item instanceof ASTToken) {
                l.add (clone ((ASTToken) item));
                continue;
            }
            ASTNode node = (ASTNode) item;
            if (node.getNT ().equals ("startTag")) {
                ASTToken tagCloseSymbolToken = node.getTokenType ("TAG_CLOSE_SYMBOL");
                if (tagCloseSymbolToken != null && "/>".equals(tagCloseSymbolToken.getIdentifier())) {
                    l.add (clone (node, "simpleTag"));
                } else {
                    String name = node.getTokenTypeIdentifier ("TAG_OPEN");
                    if (name == null) 
                        name = "";
                    else
                        name = name.toLowerCase ();
                    s.add (name);
                    s.add (new Integer (l.size ()));
                    
                    /*if(!isSupportedTag(name)) {
                        l.add (clone (node, "unsupportedTag"));
                    } else */if (findUnpairedTags && isEndTagRequired (name))
                        l.add (clone (node, "unpairedStartTag"));
                    else
                        l.add (clone (node, "simpleTag"));
                }
                continue;
            } else
            if (node.getNT ().equals ("endTag")) {
                String name = node.getTokenTypeIdentifier ("TAG_CLOSE");
                if (name == null) 
                    name = "";
                else
                    name = name.toLowerCase ();
                int indexS = s.lastIndexOf (name);
                if (indexS >= 0) {
                    int indexL = ((Integer) s.get (indexS + 1)).intValue ();
                    List ll = l.subList (indexL, l.size ());
                    ll.set (0, clone ((ASTNode) ll.get (0), "startTag"));
                    List ll1 = new ArrayList (ll);
                    ll1.add (node);
                    ASTNode tag = clone (
                        node.getMimeType (),
                        "tag",
                        ((ASTNode) ll1.get (0)).getOffset (),
                        ll1
                    );
                    ll.clear ();
                    s.subList (indexS, s.size ()).clear ();
                    l.add (tag);
                } else
                    l.add (clone (node, "unpairedEndTag"));
                continue;
            } else
            if (node.getNT ().equals ("tags")) {
                resolve (node, s, l, findUnpairedTags);
                continue;
            }
            if (node.getNT ().equals ("tag")) {
                resolve (node, s, l, findUnpairedTags);
                continue;
            }
            l.add (clone (node));
        }
    }
}

