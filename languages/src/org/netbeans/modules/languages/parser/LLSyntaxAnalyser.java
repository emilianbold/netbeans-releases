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
import java.util.HashMap;
import java.util.HashSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;


/**
 *
 * @author Jan Jancura
 */
public class LLSyntaxAnalyser {

    private Language        language;
    private List<Rule>      rules;
    private Map<String,Map> first;
    private Set<String>     skip;
    private int             traceSteps = -1;
    private boolean         cancel = false;
    private boolean         printFirst = false;
    
    
    private LLSyntaxAnalyser (Language language) {
        this.language = language;
    }
    
    
    // public methods ..........................................................
    
    public void cancel () {
        cancel = true;
    }

    public List<Rule> getRules () {
        return rules;
    }
    
    public static LLSyntaxAnalyser create (Language language) throws ParseException {
        LLSyntaxAnalyser a = new LLSyntaxAnalyser (language);
        a.rules = language.getRules ();
        a.skip = new HashSet<String> (language.getSkipTokenTypes ());
        a.skip.add ("error");
        a.initTracing ();
        a.first = Petra.first2 (a.rules);
        boolean hasConflicts = AnalyserAnalyser.printConflicts (a.first, null);
        if (hasConflicts)
            AnalyserAnalyser.printRules (a.rules, null);
        if (a.printFirst)
            AnalyserAnalyser.printF (a.first, null);
        AnalyserAnalyser.printUndefinedNTs (a.rules, null);
        return a;
    }
    
    public static LLSyntaxAnalyser createEmpty (Language language) {
        LLSyntaxAnalyser a = new LLSyntaxAnalyser (language);
        a.rules = Collections.<Rule>emptyList ();
        a.skip = new HashSet<String> (language.getSkipTokenTypes ());
        a.first = Collections.<String,Map>emptyMap ();
        return a;
    }
    
    public ASTNode read (TokenInput input, boolean skipErrors) throws ParseException {
        cancel = false;
        Map<String,List<ASTItem>> embeddings = new HashMap<String, List<ASTItem>> ();
        ASTNode root;
        if (rules.isEmpty () || input.eof ())
            root = readNoGrammar (input, skipErrors, embeddings);
        root = read2 (input, skipErrors, embeddings);
        if (embeddings.isEmpty ())
            return root;
        List<ASTItem> roots = new ArrayList<ASTItem> ();
        Iterator<String> it = embeddings.keySet ().iterator ();
        while (it.hasNext ()) {
            String mimeType =  it.next ();
            List<ASTItem> tokens = embeddings.get (mimeType);
            Language language = LanguagesManager.getDefault ().getLanguage (mimeType);
            TokenInput in = TokenInputUtils.create (tokens);
            ASTNode r = language.getAnalyser ().read (in, skipErrors, null);
            Feature astProperties = language.getFeature ("AST");
            if (astProperties != null) {
                String process_embedded = (String)astProperties.getValue("process_embedded");
                if(process_embedded == null || Boolean.valueOf(process_embedded)) {
                    ASTNode newRoot = (ASTNode) astProperties.getValue (
                        "process", 
                        SyntaxContext.create (null, ASTPath.create (root))
                    );
                    if (newRoot != null)
                        r = newRoot;
                }
            }
            roots.add (r);
        }
        roots.add (root);
        return ASTNode.createCompoundASTNode (root.getMimeType (), "Root", roots, 0);
    }
    
    
    // helper methods ..........................................................
    
    private ASTNode read (TokenInput input, boolean skipErrors, Map<String,List<ASTItem>> embeddings) throws ParseException {
        cancel = false;
        if (rules.isEmpty () || input.eof ())
            return readNoGrammar (input, skipErrors, embeddings);
        return read2 (input, skipErrors, embeddings);
    }
    
    private ASTNode read2 (TokenInput input, boolean skipErrors, Map<String,List<ASTItem>> embeddings) throws ParseException {
        Stack<Object> stack = new Stack<Object> ();
        Node root = null, node = null;
        Iterator it = Collections.singleton ("S").iterator ();
        do {
            int offset = input.getOffset ();
            List whitespaces = readWhitespaces (node, input, skipErrors, embeddings);
            if (node != null)
                offset = input.getOffset ();
            while (!it.hasNext ()) {
                if (stack.empty ()) break;
                node = (Node) stack.pop ();
                it = (Iterator) stack.pop ();
            }
            if (!it.hasNext ()) break;
            Object current = it.next ();
            if (current instanceof String) {
                String nt = (String) current;
                int newRule = getRule (nt, input);
                if (newRule == -1) {
                    if (!skipErrors) {
                        if (node == null)
                            root = node = new Node ("Root", -1, offset, whitespaces);
                        throw new ParseException ("No rule for " + input.next (1) + " in " + nt + ".", root.createASTNode());
                    }
                    if (input.eof ()) {
                        if (node == null)
                            root = node = new Node ("Root", -1, offset, whitespaces);
                        createErrorNode (node, input.getOffset ());
                        //S ystem.out.println(input.getIndex () + ": unexpected eof " + nt);
                        return root.createASTNode ();
                    }
                    //S ystem.out.println(input.getIndex () + ": no rule for " + nt + "&" + input.next (0));
                    createErrorNode (node, input.getOffset ()).addItem (readEmbeddings (input.read (), skipErrors, embeddings));
                } else {
                    Rule rule = (Rule) rules.get (newRule);
                    Feature parse = language.getFeature ("PARSE", rule.getNT ());
                    if (parse != null) {
                        stack.push (it);
                        stack.push (node);
                        it = Collections.EMPTY_LIST.iterator ();
                        ASTNode nast = (ASTNode) parse.getValue (
                            new Object[] {input, stack}
                        );
                        if (nast != null)
                            node.addItem (nast);
                        //S ystem.out.println(input.getIndex () + ": >>Java " + nt + "&" + evaluator.getValue ());
                    } else {
                        if (node == null || it.hasNext () || !nt.equals (node.nt)) {
                            if (nt.indexOf ('$') > 0 || nt.indexOf ('#') > 0) {
                                stack.push (it);
                                stack.push (node);
                            } else {
                                Node nnode = new Node (
                                    rule.getNT (), 
                                    newRule, 
                                    offset, 
                                    whitespaces
                                );
                                if (node != null) {
                                    node.addNode (nnode);
                                    stack.push (it);
                                    stack.push (node);
                                } else {
                                    root = nnode;
                                }
                                node = nnode;
                            }
                        }
                        //S ystem.out.println(input.getIndex () + ": " + rule);
                        it = rule.getRight ().iterator ();
                    }
                }
            } else {
                ASTToken token = (ASTToken) current;
                if (input.eof ()) {
                    if (!skipErrors)
                        throw new ParseException ("Unexpected end of file.", root.createASTNode ());
                    createErrorNode (node, input.getOffset ());
                    //S ystem.out.println(input.getIndex () + ": unexpected eof " + token);
                    return root.createASTNode ();
                } else
                if (!isCompatible (token, input.next (1))) {
                    if (!skipErrors)
                        throw new ParseException ("Unexpected token " + input.next (1) + ". Expecting " + token, root.createASTNode ());
                    createErrorNode (node, input.getOffset ()).addItem (readEmbeddings (input.read (), skipErrors, embeddings));
                    //S ystem.out.println(input.getIndex () + ": unrecognized token " + token + "<>" + input.next (1));
                } else {
                    node.addItem (readEmbeddings (input.read (), skipErrors, embeddings));
                    //S ystem.out.println(input.getIndex () + ": token readed " + input.next (1));
                }
            }
        } while (true);
        if (!skipErrors && !input.eof ())
            throw new ParseException ("Unexpected token " + input.next (1) + ".", root.createASTNode ());
        while (
            !input.eof () //&& 
            //input.next (1).getMimeType () == mimeType
        )
            createErrorNode (node, input.getOffset ()).addItem (readEmbeddings (input.read (), skipErrors, embeddings));
        if (root == null) {
            root = new Node ("Root", -1, input.getOffset(), readWhitespaces (node, input, skipErrors, embeddings));
        }
        return root.createASTNode ();
    }
    
    private static boolean isCompatible (ASTToken t1, ASTToken t2) {
        if (t1.getType () == null) {
            return t1.getIdentifier ().equals (t2.getIdentifier ());
        } else {
            if (t1.getIdentifier () == null)
                return t1.getType ().equals (t2.getType ());
            else
                return t1.getType ().equals (t2.getType ()) && 
                       t1.getIdentifier ().equals (t2.getIdentifier ());
        }
    }
    
    private List<ASTItem> readWhitespaces (
        Node node, 
        TokenInput input, 
        boolean skipErrors,
        Map<String,List<ASTItem>> embeddings
    ) throws ParseException {
        List<ASTItem> result = null;
        while (
            !input.eof () &&
            skip.contains (input.next (1).getType ())
        ) {
            ASTToken token = input.read ();
            if (node != null)
                node.addItem (readEmbeddings (token, skipErrors, embeddings));
            else {
                if (result == null)
                    result = new ArrayList<ASTItem> ();
                result.add (readEmbeddings (token, skipErrors, embeddings));
            }
        }
        return result;
    }
    
    private ASTItem readEmbeddings (
        ASTToken token, 
        boolean skipErrors,
        Map<String,List<ASTItem>> embeddings
    ) throws ParseException {
        List<ASTItem> children = token.getChildren ();
        if (children.isEmpty ())
            return token;

        TokenInput in = TokenInputUtils.create (children);
        try {
            String mimeType = children.get (0).getMimeType ();
            Language oLanguage = LanguagesManager.getDefault ().
                getLanguage (token.getMimeType ());
            Feature f = oLanguage.getPreprocessorImport ();
            if (f != null && 
                mimeType.equals (f.getValue ("mimeType")) &&
                f.getBoolean ("continual", false)
            )
                return skipEmbedding (token, embeddings, children, mimeType);
            f = oLanguage.getTokenImports ().get (token.getType ());
            if (f != null && 
                mimeType.equals (f.getValue ("mimeType")) &&
                f.getBoolean ("continual", false)
            )
                return skipEmbedding (token, embeddings, children, mimeType);

            Language language = LanguagesManager.getDefault ().
                getLanguage (children.get (0).getMimeType ());
            ASTNode root = language.getAnalyser ().read (in, skipErrors, embeddings);
            Feature astProperties = language.getFeature ("AST");
            if (astProperties != null) {
                String process_embedded = (String)astProperties.getValue("process_embedded");
                if(process_embedded == null || Boolean.valueOf(process_embedded)) {
                    ASTNode newRoot = (ASTNode) astProperties.getValue (
                        "process", 
                        SyntaxContext.create (null, ASTPath.create (root))
                    );
                    if (newRoot != null)
                        root = newRoot;
                }
            }
            return ASTToken.create (
                token.getMimeType (),
                token.getType (),
                token.getIdentifier (),
                token.getOffset (),
                token.getLength (),
                Collections.<ASTItem>singletonList (root)
            );
        } catch (LanguageDefinitionNotFoundException ex) {
            return readNoGrammar (in, skipErrors, embeddings);
        }
    }
    
    private ASTToken skipEmbedding (
        ASTToken                    token, 
        Map<String,List<ASTItem>>   embeddings,
        List<ASTItem>               children,
        String                      mimeType
    ) {
        List<ASTItem> l = embeddings.get (mimeType);
        if (l == null) {
            l = new ArrayList<ASTItem> ();
            embeddings.put (mimeType, l);
        }
        l.addAll (children);
        return ASTToken.create (
            token.getMimeType (),
            token.getType (),
            token.getIdentifier (),
            token.getOffset (),
            token.getLength (),
            Collections.<ASTItem>emptyList ()
        );
    }
    
    private ASTNode readNoGrammar (
        TokenInput input,
        boolean skipErrors,
        Map<String,List<ASTItem>> embeddings
    ) throws ParseException {
        Node root = new Node ("S", -1, input.getIndex (), null);
        while (!input.eof ()) {
            ASTToken token = input.read ();
            root.addItem (readEmbeddings (token, skipErrors, embeddings));
        }
        return root.createASTNode ();
    }
    
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
            "ERROR", 
            -2, 
            offset,
            null
        );
        if (parentNode != null)
            parentNode.addNode (errorNode);
        return errorNode;
    }
    
    private int getRule (String nt, TokenInput input) {
        Map m = (Map) first.get (nt);
        if (m == null) return -1;
        int i = 1;
        while (true) {
            ASTToken token = input.next (i);
            Map r = null;
            if (token != null) {
                T t = new T (token);
                r = (Map) m.get (t);
                if (r == null) {
                    t.type = null;
                    r = (Map) m.get (t);
                }
                if (r == null) {
                    t.type = token.getType ();
                    t.identifier = null;
                    r = (Map) m.get (t);
                }
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
            i++;
        }
    }
    
    private void initTracing () {
        Feature properties = language.getFeature ("PROPERTIES");
        if (properties == null) return;
        try {
            traceSteps = Integer.parseInt ((String) properties.getValue ("traceSteps"));
        } catch (NumberFormatException ex) {
            traceSteps = -2;
        }
        if (properties.getBoolean ("printRules", false))
            AnalyserAnalyser.printRules (rules, null);
        printFirst = properties.getBoolean ("printFirst", false);
    }
    
    private Feature optimiseProperty;
    private boolean removeEmpty = false;
    private boolean removeSimple = false;
    private boolean removeEmptyN = true;
    private boolean removeSimpleN = true;
    private Set<String> empty = new HashSet<String> ();
    private Set<String> simple = new HashSet<String> ();
    
    private boolean removeNT (Node n) {
        if (optimiseProperty == null) {
            Feature properties = language.getFeature ("PROPERTIES");
            optimiseProperty = language.getFeature ("AST");
            if (optimiseProperty != null) {
                String s = (String) optimiseProperty.getValue ("removeEmpty");
                if (s != null) {
                    if (s.startsWith ("!")) {
                        removeEmptyN = false;
                        s = s.substring (1);
                    }
                    removeEmpty = "true".equals (s);
                    if (!"false".equals (s)) {
                        StringTokenizer st = new StringTokenizer (s, ",");
                        while (st.hasMoreTokens ())
                            empty.add (st.nextToken ());
                    }
                }
                s = (String) optimiseProperty.getValue ("removeSimple");
                if (s != null) {
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
        
        private String  nt;
        private List    right;
        
        private Rule () {}
        
        public static Rule create (
            String      nt, 
            List        right
        ) {
            Rule r = new Rule ();
            r.nt = nt;
            r.right = right;
            return r;
        }

        public String getNT () {
            return nt;
        }
        
        public List getRight () {
            return right;
        }
        
        private String toString = null;
        
        public String toString () {
            if (toString == null) {
                StringBuilder sb = new StringBuilder ();
                sb.append ("Rule ").append (nt).append (" = ");
                int i = 0, k = right.size ();
                if (i < k) 
                    sb.append (right.get (i++));
                while (i < k)
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
        
        T (ASTToken t) {
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
        
        String      nt;
        int         rule;
        int         offset;
        List<Object> children;
        
        Node (
            String nt,
            int rule,
            int offset,
            List children
        ) {
            this.nt = nt;
            this.rule = rule;
            this.offset = offset;
            if (children != null) {
                Iterator it = children.iterator ();
                while (it.hasNext ()) {
                    Object o = it.next ();
                    if (o instanceof ASTItem)
                        addItem ((ASTItem) o);
                    else
                        addNode ((Node) o);
                }
            }
        }
        
        void addNode (Node n) {
            if (n == null) throw new NullPointerException ();
            if (children == null) children = new ArrayList<Object> ();
//            if (((Node) n).offset != getEndOffset ())
//                throw new IllegalStateException ();
            children.add (n);
        }
        
        void addItem (ASTItem item) {
            if (item == null) throw new NullPointerException ();
            if (children == null) children = new ArrayList<Object> ();
//            if (item.getOffset () != getEndOffset ())
//                throw new IllegalStateException ();
            children.add (item);
        }
        
        void replace (ASTNode n1, Node n2) {
            if (n1 == null) throw new NullPointerException ();
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
        
        private int getEndOffset () {
            if (children == null) return offset;
            if (children.isEmpty ()) return offset;
            Object l = children.get (children.size () - 1);
            if (l instanceof ASTToken)
                return ((ASTToken) l).getOffset () + ((ASTToken) l).getLength ();
            if (l instanceof Node) {
                return ((Node) l).getEndOffset ();
            }
            return ((ASTNode) l).getEndOffset ();
        }

        ASTNode createASTNode () {
            List<ASTItem> l = new ArrayList<ASTItem> ();
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
                        l.add ((ASTItem) o);
                }
            }
            return ASTNode.create (
                language.getMimeType (),
                nt,
                l,
                offset
            );
        }
        
        public String toString () {
            return "LLSyntaxAnalyser$Node " + nt;
        }
    }
}


