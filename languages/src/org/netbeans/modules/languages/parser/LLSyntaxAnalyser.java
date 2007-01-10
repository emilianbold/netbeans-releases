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


package org.netbeans.modules.languages.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.SToken;
import org.netbeans.modules.languages.Evaluator;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.Evaluator;


/**
 *
 * @author Jan Jancura
 */
public class LLSyntaxAnalyser {

    private Language    language;
    private List        rules;
    private Map         first;
    private Set         skip;
    private int         traceSteps = -1;
    private boolean     cancel = false;
    private boolean     printFirst = false;
    
    
    private LLSyntaxAnalyser (Language language) {
        this.rules = language.getRules ();
        this.skip = language.getSkipTokenTypes ();
        this.language = language;
        initTracing ();
    }
    
    
    // public methods ..........................................................
    
    public void cancel () {
        cancel = true;
    }

    public List getRules () {
        return rules;
    }
    
    public static LLSyntaxAnalyser create (Language language) {
        return new LLSyntaxAnalyser (language);
    }
    
    public ASTNode read (TokenInput input, boolean skipErrors) throws ParseException {
        cancel = false;
        return read (input, false, skipErrors);
    }
    
    private ASTNode read (TokenInput input, boolean embeded, boolean skipErrors) throws ParseException {
        if (rules.isEmpty () || input.eof ())
            return ASTNode.create (null, "Root", -1, 0);
        if (first == null) {
            first = Petra.first2 (rules);
            boolean hasConflicts = AnalyserAnalyser.printConflicts (first, null);
            if (hasConflicts)
                AnalyserAnalyser.printRules (rules, null);
            if (printFirst)
                AnalyserAnalyser.printF (first, null);
        }
        AnalyserAnalyser.printUndefinedNTs (rules, null);
        
        Stack stack = new Stack ();
        Node root = null, node = null;
        Iterator it = Collections.singleton ("S").iterator ();
        String mimeType = input.next (1).getMimeType ();
        List initialWhitespaces = new ArrayList ();
        while (!input.eof () && skip.contains (input.next (1).getType ()))
            initialWhitespaces.add (input.read ());
        do {
            if (!input.eof () && input.next (1).getMimeType () != mimeType) {
                if (embeded)
                    return root.createASTNode ();
                ASTNode n = read (input, true, skipErrors);
                if (n != null) {// embeded language without grammer definition
                    if (node == null) {
                        root = node = new Node (mimeType, "Root", -1, 0);
                        Iterator it2 = initialWhitespaces.iterator ();
                        while (it2.hasNext ())
                            node.addToken ((SToken) it2.next ());
                    }
                    node.addNode (n);
                }
            }
            while (!it.hasNext ()) {
//                if (node.getChildren ().size () == 0)
//                    node.getParent ().
                if (stack.empty ()) break;
                node = (Node) stack.pop ();
                it = (Iterator) stack.pop ();
            }
            if (!it.hasNext ()) break;
            Object current = it.next ();
            if (current instanceof String) {
                String nt = (String) current;
                int newRule = getRule (mimeType, nt, input);
                if (newRule == -1) {
                    if (!skipErrors)
                        throw new ParseException ("No rule for " + input.next (1) + " in " + input, root.createASTNode ());
                    if (input.eof ()) {
                        if (node == null)
                            root = node = new Node (mimeType, "Root", -1, input.getOffset ());
                        createErrorNode (node, input.getOffset ());
                        //S ystem.out.println(input.getIndex () + ": unexpected eof " + nt);
                        return root.createASTNode ();
                    }
                    //S ystem.out.println(input.getIndex () + ": no rule for " + nt + "&" + input.next (0));
                    createErrorNode (node, input.getOffset ()).addToken (input.read ());
                } else {
                    Rule rule = (Rule) rules.get (newRule);
                    Evaluator.Method evaluator = null;
                    evaluator = (Evaluator.Method) language.getFeature (
                        language.PARSE, 
                        mimeType, 
                        rule.getNT ()
                    );
                    if (evaluator != null) {
                        stack.push (it);
                        stack.push (node);
                        it = Collections.EMPTY_LIST.iterator ();
                        ASTNode nast = (ASTNode) evaluator.evaluate (
                            new Object[] {input, stack}
                        );
                        node.addNode (nast);
                        //S ystem.out.println(input.getIndex () + ": >>Java " + nt + "&" + evaluator.getValue ());
                    } else {
                        if (node == null || it.hasNext () || !nt.equals (node.nt)) {
                            if (nt.indexOf ('$') > 0 || nt.indexOf ('#') > 0) {
                                stack.push (it);
                                stack.push (node);
                            } else {
                                Node nnode = new Node (mimeType, rule.getNT (), newRule, input.getOffset ());
                                if (node != null) {
                                    node.addNode (nnode);
                                    stack.push (it);
                                    stack.push (node);
                                } else {
                                    root = nnode;
                                    Iterator it2 = initialWhitespaces.iterator ();
                                    while (it2.hasNext ())
                                        nnode.addToken ((SToken) it2.next ());
                                }
                                node = nnode;
                            }
                        }
                        //S ystem.out.println(input.getIndex () + ": " + rule);
                        it = rule.getRight ().iterator ();
                    }
                }
            } else {
                SToken token = (SToken) current;
                if (input.eof ()) {
                    if (!skipErrors)
                        throw new ParseException ("Unexpected end of file in " + input, root.createASTNode ());
                    createErrorNode (node, input.getOffset ());
                    //S ystem.out.println(input.getIndex () + ": unexpected eof " + token);
                    return root.createASTNode ();
                } else
                if (!token.isCompatible (input.next (1))) {
                    if (!skipErrors)
                        throw new ParseException ("Unexpected token " + input.next (1) + " in " + input + ". Ecpecting " + token, root.createASTNode ());
                    createErrorNode (node, input.getOffset ()).addToken (input.read ());
                    //S ystem.out.println(input.getIndex () + ": unrecognized token " + token + "<>" + input.next (1));
                } else {
                    node.addToken (input.read ());
                    //S ystem.out.println(input.getIndex () + ": token readed " + input.next (1));
                }
                while (!input.eof () && skip.contains (input.next (1).getType ()))
                    node.addToken (input.read ());
            }
        } while (true);
        return root.createASTNode ();
    }
    
    
    // helper methods ..........................................................
    
    private Node createErrorNode (Node parentNode, int offset) {
        if (parentNode != null) {
            List l = parentNode.children;
            if (l != null && l.size () > 0) {
                Object possibleErrorNode = l.get (l.size () - 1);
                if (possibleErrorNode instanceof Node)
                    if (((Node) possibleErrorNode).nt.equals ("ERROR"))
                        return (Node) possibleErrorNode;
            }
        }
        Node errorNode = new Node (
            parentNode == null ? "?" : parentNode.mimeType, 
            "ERROR", 
            -2, 
            offset
        );
        if (parentNode != null)
            parentNode.addNode (errorNode);
        return errorNode;
    }
    
    private int getRule (String mimeType, String nt, TokenInput input) {
        Map m = (Map) first.get (mimeType);
        if (m == null) return -1;
        m = (Map) m.get (nt);
        if (m == null) return -1;
        int i = 1;
        while (true) {
            SToken token = input.next (i);
            if (token == null || !mimeType.equals (token.getMimeType ())) {
                Set result = (Set) (Set) m.get ("#");
                if (result == null || result.size () > 1) return -1;
                return ((Integer) result.iterator ().next ()).intValue ();
            }
            T t = new T (token);
            Map r = (Map) m.get (t);
            if (r == null) {
                t.type = null;
                r = (Map) m.get (t);
            }
            if (r == null) {
                t.type = token.getType ();
                t.identifier = null;
                r = (Map) m.get (t);
            }
            if (r == null) {
                Set s = (Set) m.get ("#");
                if (s == null)
                    s = (Set) m.get ("&");
                if (s == null) {
                    System.out.println("No way! " + nt + " : " + input.next (1) + " " + input.next (2));
                    return -1;
                }
                if (s.size () > 1) {
                    System.out.println("Too many choices! " + nt + " : " + input.next (1) + " " + input.next (2) + ":" + input);
                    return -1;
                }
                return ((Integer) s.iterator ().next ()).intValue ();
            }
            m = r;
        }
    }
    
    private void initTracing () {
        Evaluator e = (Evaluator) language.getProperty (language.getMimeType (), "traceSteps");
        if (e != null)
            try {
                traceSteps = Integer.parseInt ((String) e.evaluate ());
            } catch (NumberFormatException ex) {
                traceSteps = -2;
            }
        e = (Evaluator) language.getProperty (language.getMimeType (), "printRules");
        if (e != null && "true".equals (e.evaluate ()))
            AnalyserAnalyser.printRules (rules, null);
        e = (Evaluator) language.getProperty (language.getMimeType (), "printFirst");
        if (e != null && "true".equals (e.evaluate ()))
            printFirst = true;
    }
    
    private Map optimiseProperty;
    private boolean removeEmpty = false;
    private boolean removeSimple = false;
    private boolean removeEmptyN = true;
    private boolean removeSimpleN = true;
    private Set empty = new HashSet ();
    private Set simple = new HashSet ();
    
    private boolean removeNT (Node n) {
        if (optimiseProperty == null) {
            optimiseProperty = (Map) language.getProperty 
                (language.getMimeType (), Language.AST);
            if (optimiseProperty != null) {
                Evaluator e = (Evaluator) optimiseProperty.get ("removeEmpty");
                if (e != null) {
                    String s = (String) e.evaluate ();
                    if (s.startsWith ("!")) {
                        removeEmptyN = false;
                        s = s.substring (1);
                    }
                    removeEmpty = "true".equals (s);
                    if (!"false".equals (e)) {
                        StringTokenizer st = new StringTokenizer (s, ",");
                        while (st.hasMoreTokens ())
                            empty.add (st.nextToken ());
                    }
                }
                e = (Evaluator) optimiseProperty.get ("removeSimple");
                if (e != null) {
                    String s = (String) e.evaluate ();
                    if (s.startsWith ("!")) {
                        removeSimpleN = false;
                        s = s.substring (1);
                    }
                    removeSimple = "true".equals (s);
                    if (!"false".equals (s)) {
                        StringTokenizer st = new StringTokenizer (s, ",");
                        while (st.hasMoreTokens ())
                            simple.add (st.nextToken ());
                    }
                }
            }
        }
        if (n.children == null)
            return removeEmpty || (removeEmptyN == empty.contains (n.nt));
        else
            return removeSimple || (removeSimpleN == simple.contains (n.nt));
    } 
    
    
    // innerclasses ............................................................
    
    
    public static class Rule {
        
        private String  mimeType;
        private String  nt;
        private List    right;
        private int     save;
        private int     load;
        
        private Rule () {}
        
        public static Rule create (
            String      mimeType, 
            String      nt, 
            List        right,
            int         save,
            int         load
        ) {
            Rule r = new Rule ();
            r.mimeType = mimeType;
            r.nt = nt;
            r.right = right;
            r.save = save;
            r.load = load;
            return r;
        }
        
        public static Rule create (
            String      mimeType, 
            String      nt, 
            List        right
        ) {
            return create (
                mimeType, nt, right, -1, -1
            );
        }


        public String getMimeType () {
            return mimeType;
        }

        public String getNT () {
            return nt;
        }
        
        public List getRight () {
            return right;
        }

        public int getSave () {
            return save;
        }

        public int getLoad () {
            return load;
        }
        
        private String toString = null;
        
        public String toString () {
            if (toString == null) {
                StringBuilder sb = new StringBuilder ();
                sb.append ("Rule ").append (nt).append (" = ");
                int i = 0, k = right.size ();
                if (i < k) 
                    if (save == i)
                        sb.append ('$').append (right.get (i++));
                    else 
                    if (load == i)
                        sb.append ('^').append (right.get (i++));
                    else 
                        sb.append (right.get (i++));
                while (i < k)
                    if (save == i)
                        sb.append (' ').append ('$').append (right.get (i++));
                    else 
                    if (load == i)
                        sb.append (' ').append ('^').append (right.get (i++));
                    else 
                        sb.append (' ').append (right.get (i++));
                toString = sb.toString ();
            }
            return toString;
        }
    }

    public static class T {
        String type;
        String identifier;
        
//        T (String type, String identifier) {
//            this.type = type;
//            this.identifier = identifier;
//        }
        
        T (SToken t) {
            type = t.getType ();
            identifier = t.getIdentifier ();
            if (type == null && identifier == null)
                System.out.println("null null!!!");
        }
        
        public boolean equals (Object o) {
            if (!(o instanceof T)) return false;
            return (((T) o).type == null || ((T) o).type.equals (type)) &&
                   (((T) o).identifier == null || ((T) o).identifier.equals (identifier));
        }
        
        public int hashCode () {
            return type == null ? -1 : type.hashCode () *
                   (identifier == null ? -1 : identifier.hashCode ());
        }
        
        public String toString () {
            if (type == null)
                return "\"" + identifier + "\"";
            if (identifier == null)
                return "<" + type + ">";
            return "[" + type + "," + identifier + "]";
        }
    }
    
    private class Node {
        
        String      mimeType;
        String      nt;
        int         rule;
        int         offset;
        List        children;
        
        Node (
            String mimeType,
            String nt,
            int rule,
            int offset
        ) {
            this.mimeType = mimeType;
            this.nt = nt;
            this.rule = rule;
            this.offset = offset;
        }
        
        void addNode (Object n) {
            if (children == null) children = new ArrayList ();
            children.add (n);
        }
        
        void addToken (SToken t) {
            if (children == null) children = new ArrayList ();
            children.add (t);
        }
        
        void replace (ASTNode n1, Node n2) {
            int i, k = children.size ();
            for (i = 0; i < k; i++) {
                Object o = children.get (i);
                if (n2.equals (o)) {
                    children.set (i, n1);
                    return;
                }
            }
            throw new IllegalStateException ();
        }

        ASTNode createASTNode () {
            List l = new ArrayList ();
            if (children == null) {
                if (removeNT (this)) return null;
            } else {
                if (children.size () == 1 && 
                    children.get (0) instanceof Node &&
                    removeNT (this)
                ) return ((Node) children.get (0)).createASTNode ();
                Iterator it = children.iterator ();
                while (it.hasNext ()) {
                    Object o = it.next ();
                    if (o instanceof Node) {
                        ASTNode nn = ((Node) o).createASTNode ();
                        if (nn != null) l.add (nn);
                    } else
                        l.add (o);
                }
            }
            return ASTNode.create (
                mimeType,
                nt,
                rule,
                l,
                offset
            );
        }
    }
}


