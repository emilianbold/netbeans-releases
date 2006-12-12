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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import org.netbeans.modules.languages.Evaluator;
import org.netbeans.modules.languages.Evaluator;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.Evaluator;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

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
    private boolean     nbsnbs;
    
    
    private LLSyntaxAnalyser (List rules, Set skip, Map p, boolean nbsnbs, Language language) {
        this.rules = rules;
        this.nbsnbs = nbsnbs;
        initTracing (p);
        this.skip = skip;
        this.language = language;
    }
    
    
    // public methods ..........................................................
    
    public void cancel () {
        cancel = true;
    }

    public List getRules () {
        return rules;
    }
    
    public static LLSyntaxAnalyser create (Map trees, Set skipTokenTypes, Map p, Language language) {
        List rules = Petra.convert (trees, p);
        return new LLSyntaxAnalyser (rules, skipTokenTypes, p, false, language);
    }
    
    public static LLSyntaxAnalyser create (List rules, Set skipTokenTypes) {
        return new LLSyntaxAnalyser (rules, skipTokenTypes, Collections.EMPTY_MAP, true, null);
    }
    
    public ASTNode read (TokenInput input, boolean skipErrors) throws ParseException {
        cancel = false;
        return read (input, false, skipErrors);
    }
    
    private ASTNodeImpl read (TokenInput input, boolean embeded, boolean skipErrors) throws ParseException {
        if (rules.isEmpty () || input.eof ())
            return new ASTNodeImpl (null, "Root", -1, null, 0);
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
        ASTNodeImpl root = null, node = null;
        Iterator it = Collections.singleton ("S").iterator ();
        String mimeType = input.next (1).getMimeType ();
        List initialWhitespaces = new ArrayList ();
        while (!input.eof () && skip.contains (input.next (1).getType ()))
            initialWhitespaces.add (input.read ());
        do {
            if (!input.eof () && input.next (1).getMimeType () != mimeType) {
                if (embeded)
                    return root;
                ASTNode n = read (input, true, skipErrors);
                if (n != null) // embeded language without grammer definition
                    node.addNode ((ASTNodeImpl) n);
            }
            while (!it.hasNext ()) {
                if (stack.empty ()) break;
                node = (ASTNodeImpl) stack.pop ();
                it = (Iterator) stack.pop ();
            }
            if (!it.hasNext ()) break;
            int offset = input.eof () ? 
                (node == null ? 0 : node.getEndOffset ()) : 
                input.next (1).getOffset ();
            Object current = it.next ();
            if (current instanceof String) {
                String nt = (String) current;
                int newRule = getRule (mimeType, nt, input);
                if (newRule == -1) {
                    if (!skipErrors)
                        throw new ParseException ("No rule for " + input.next (1) + " in " + input, root);
                    if (input.eof ()) {
                        if (node == null)
                            root = node = new ASTNodeImpl (mimeType, "Root", -1, null, offset);
                        getErrorNode (node, offset);
                        //S ystem.out.println(input.getIndex () + ": unexpected eof " + nt);
                        return root;
                    }
                    //S ystem.out.println(input.getIndex () + ": no rule for " + nt + "&" + input.next (0));
                    getErrorNode (node, offset).addToken (input.read ());
                } else {
                    Rule rule = (Rule) rules.get (newRule);
                    Evaluator.Method evaluator = null;
                    if (language != null)
                        evaluator = (Evaluator.Method) language.getFeature (
                            language.PARSE, 
                            mimeType, 
                            rule.getNT ()
                        );
                    if (evaluator != null) {
                        stack.push (it);
                        stack.push (node);
                        it = Collections.EMPTY_LIST.iterator ();
                        node = (ASTNodeImpl) evaluator.evaluate (
                            new Object[] {input, stack, node}
                        );
                        //S ystem.out.println(input.getIndex () + ": >>Java " + nt + "&" + evaluator.getValue ());
                    } else {
                        if (node == null || it.hasNext () || !nt.equals (node.getNT ())) {
                            if (nt.indexOf ('$') > 0 || nt.indexOf ('#') > 0) {
                                stack.push (it);
                                stack.push (node);
                            } else {
                                ASTNodeImpl nnode = new ASTNodeImpl (mimeType, rule.getNT (), newRule, node, offset);
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
                        throw new ParseException ("Unexpected end of file in " + input, root);
                    getErrorNode (node, offset);
                    //S ystem.out.println(input.getIndex () + ": unexpected eof " + token);
                    return root;
                } else
                if (!token.isCompatible (input.next (1))) {
                    if (!skipErrors)
                        throw new ParseException ("Unexpected token " + input.next (1) + " in " + input + ". Ecpecting " + token, root);
                    getErrorNode (node, offset).addToken (input.read ());
                    //S ystem.out.println(input.getIndex () + ": unrecognized token " + token + "<>" + input.next (1));
                } else {
                    node.addToken (input.read ());
                    //S ystem.out.println(input.getIndex () + ": token readed " + input.next (1));
                }
                while (!input.eof () && skip.contains (input.next (1).getType ()))
                    node.addToken (input.read ());
            }
        } while (true);
        return root;
    }
    
    
    // helper methods ..........................................................
    
    private ASTNodeImpl getErrorNode (ASTNodeImpl node, int offset) {
        if (node != null) {
            List l = node.getChildren ();
            if (l.size () > 0) {
                Object possibleErrorNode = l.get (l.size () - 1);
                if (possibleErrorNode instanceof ASTNodeImpl)
                    if (((ASTNodeImpl) possibleErrorNode).getNT ().equals ("ERROR"))
                        return (ASTNodeImpl) possibleErrorNode;
            }
        }
        ASTNodeImpl errorNode = new ASTNodeImpl (
            node == null ? "?" : node.getMimeType (), 
            "ERROR", 
            -2, 
            node, 
            offset
        );
        if (node != null)
            node.addNode (errorNode);
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
    
    private void initTracing (Map p) {
        if (p != null && p.containsKey ("traceSteps")) {
            Evaluator e = (Evaluator) p.get ("traceSteps");
            try {
                traceSteps = Integer.parseInt ((String) e.evaluate ());
            } catch (NumberFormatException ex) {
                traceSteps = -2;
            }
        }
        if (p != null && p.containsKey ("printRules"))
            AnalyserAnalyser.printRules (rules, null);
        if (p != null && p.containsKey ("printFirst")) {
            printFirst = true;
        }
    }
    
    
    // innerclasses ............................................................
    
    static class ASTNodeImpl extends ASTNode {

        private String      mimeType;
        private String      nt;
        private int         rule;
        private List        children;
        private ASTNode     parent;
        private int         offset;

        ASTNodeImpl (
            String      mimeType, 
            String      nt, 
            int         rule, 
            ASTNode     parent, 
            int         offset
        ) {
            this.mimeType = mimeType;
            this.nt =       nt;
            this.rule =     rule;
            this.parent =   parent;
            this.offset =   offset;
            children =      new ArrayList ();
        }

        ASTNodeImpl (
            String      mimeType, 
            String      nt, 
            int         rule, 
            ASTNode     parent, 
            List        children,
            int         offset
        ) {
            this.mimeType = mimeType;
            this.nt =       nt;
            this.rule =     rule;
            this.parent =   parent;
            this.children = children;
            this.offset =   offset;
        }

        public String getMimeType () {
            return mimeType;
        }

        public String getNT () {
            return nt;
        }

        public int getRule () {
            return rule;
        }

        public ASTNode getParent () {
            return parent;
        }
        
        public int getOffset () {
            return offset;
        }
        
        public void addNode (ASTNode n) {
            if (n == null)
                throw new NullPointerException ();
            children.add (n);
        }
        
        public void addToken (SToken t) {
            if (t == null)
                throw new NullPointerException ();
            children.add (t);
        }

        public List getChildren () {
            return children;
        }
    }
    
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
}


