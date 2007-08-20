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
    private boolean         printFirst = false;
    
    
    private LLSyntaxAnalyser (Language language) {
        this.language = language;
    }
    
    
    // public methods ..........................................................

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
    
    public ASTNode read (
        TokenInput input, 
        boolean skipErrors, 
        boolean[] cancel
    ) throws ParseException {
        _t = 0;
        Map<String,List<ASTItem>> embeddings = new HashMap<String, List<ASTItem>> ();
        ASTNode root;
        if (rules.isEmpty () || input.eof ()) {
            root = readNoGrammar (input, skipErrors, embeddings, cancel);
        } else {
            root = read2 (input, skipErrors, embeddings, cancel);
        }
        if (cancel [0]) return null;
        if (embeddings.isEmpty ()) {
            int[] ntw = new int [3];
            inspect (root, ntw);
            //S ystem.out.println("inspect node:" + ntw [0] + " token: " + ntw [1] + " whitespace: " + ntw [2]);
            //S ystem.out.println("T = " + _t);
            return root;
        }
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
        ASTNode result = ASTNode.createCompoundASTNode (root.getMimeType (), "Root", roots, 0);
        int[] ntw = new int [3];
        inspect (result, ntw);
        //S ystem.out.println("inspect node:" + ntw [0] + " token: " + ntw [1] + " whitespace: " + ntw [2]);
        //S ystem.out.println("T = " + _t);
        return result;
    }
    
    private void inspect (ASTItem item, int[] ntw) {
        if (item instanceof ASTNode)
            ntw [0]++;
        else {
            ASTToken t = (ASTToken) item;
            ntw [1]++;
            if (t.getType ().indexOf ("whitespace") >= 0)
                ntw [2]++;
        }
        
        Iterator<ASTItem> it = new ArrayList<ASTItem> (item.getChildren ()).iterator ();
        int i = 0;
        while (it.hasNext ()) {
            ASTItem child = it.next ();
            if (child instanceof ASTNode &&
                item instanceof ASTNode
            ) {
                ASTNode n = (ASTNode) child;
                if (removeNode (n)) {
                    ((ASTNode) item).removeChildren (n);
                    continue;
                }
                ASTItem r = replaceNode (n);
                if (r != null) {
                    ((ASTNode) item).setChildren (i, r);
                    child = r;
                }
            }
            i++;
            inspect (child, ntw);
        }
    }
    
    
    // helper methods ..........................................................
    
    private ASTNode read (
        TokenInput input, 
        boolean skipErrors, 
        Map<String,List<ASTItem>> embeddings,
        boolean[] cancel
    ) throws ParseException {
        if (rules.isEmpty () || input.eof ())
            return readNoGrammar (input, skipErrors, embeddings, cancel);
        return read2 (input, skipErrors, embeddings, cancel);
    }
    
    private ASTNode read2 (
        TokenInput input, 
        boolean skipErrors, 
        Map<String,List<ASTItem>> embeddings,
        boolean[] cancel
    ) throws ParseException {
        Stack<Object> stack = new Stack<Object> ();
        ASTNode root = null, node = null;
        Iterator it = Collections.singleton ("S").iterator ();
        boolean firstLine = true;
        do {
            if (cancel [0]) return null;
            int offset = input.getOffset ();
            List<ASTItem> whitespaces = readWhitespaces (node, input, skipErrors, embeddings, cancel);
            if (firstLine && input.eof() && whitespaces != null) {
                return readNoGrammar (whitespaces, offset, skipErrors, embeddings, cancel);
            }
            if (node != null)
                offset = input.getOffset ();
            while (!it.hasNext ()) {
                if (stack.empty ()) break;
                node = (ASTNode) stack.pop ();
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
                            root = node = ASTNode.create (language.getMimeType(), "Root", whitespaces, offset);
                        throw new ParseException ("No rule for " + input.next (1) + " in " + nt + ".", root);
                    }
                    if (input.eof ()) {
                        if (node == null)
                            root = node = ASTNode.create (language.getMimeType(), "Root", whitespaces, offset);
                        createErrorNode (node, input.getOffset ());
                        //S ystem.out.println(input.getIndex () + ": unexpected eof " + nt);
                        return root;
                    }
                    //S ystem.out.println(input.getIndex () + ": no rule for " + nt + "&" + input.next (0));
                    createErrorNode (node, input.getOffset ()).addChildren (readEmbeddings (input.read (), skipErrors, embeddings, cancel));
                } else {
                    Rule rule = rules.get (newRule);
                    Feature parse = language.getFeature ("PARSE", rule.getNT ());
                    if (parse != null) {
                        stack.push (it);
                        stack.push (node);
                        it = Collections.EMPTY_LIST.iterator ();
                        ASTNode nast = (ASTNode) parse.getValue (
                            new Object[] {input, stack}
                        );
                        if (nast != null)
                            node.addChildren (nast);
                        //S ystem.out.println(input.getIndex () + ": >>Java " + nt + "&" + evaluator.getValue ());
                    } else {
                        if (node == null || it.hasNext () || !nt.equals (node.getNT ())) {
                            if (nt.indexOf ('$') > 0 || nt.indexOf ('#') > 0) {
                                stack.push (it);
                                stack.push (node);
                            } else {
                                if (rule.getRight ().isEmpty () && removeEmpty (rule.getNT ()))
                                    continue;
                                ASTNode nnode = ASTNode.create (
                                    language.getMimeType (),
                                    rule.getNT (), 
                                    whitespaces,
                                    offset
                                );
                                if (node != null) {
                                    node.addChildren (nnode);
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
                        throw new ParseException ("Unexpected end of file.", root);
                    createErrorNode (node, input.getOffset ());
                    //S ystem.out.println(input.getIndex () + ": unexpected eof " + token);
                    return root;
                } else
                if (!isCompatible (token, input.next (1))) {
                    if (!skipErrors)
                        throw new ParseException ("Unexpected token " + input.next (1) + ". Expecting " + token, root);
                    createErrorNode (node, input.getOffset ()).addChildren (readEmbeddings (input.read (), skipErrors, embeddings, cancel));
                    //S ystem.out.println(input.getIndex () + ": unrecognized token " + token + "<>" + input.next (1));
                } else {
                    node.addChildren (readEmbeddings (input.read (), skipErrors, embeddings, cancel));
                    //S ystem.out.println(input.getIndex () + ": token readed " + input.next (1));
                }
            }
        } while (true);
        if (!skipErrors && !input.eof ())
            throw new ParseException ("Unexpected token " + input.next (1) + ".", root);
        while (
            !input.eof () //&& 
            //input.next (1).getMimeType () == mimeType
        )
            createErrorNode (node, input.getOffset ()).addChildren (readEmbeddings (input.read (), skipErrors, embeddings, cancel));
        if (root == null) {
            root = ASTNode.create (
                language.getMimeType (),  
                "Root", 
                readWhitespaces (node, input, skipErrors, embeddings, cancel), 
                input.getOffset ()
            );
        }
        return root;
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
        ASTNode node, 
        TokenInput input, 
        boolean skipErrors,
        Map<String,List<ASTItem>> embeddings,
        boolean[] cancel
    ) throws ParseException {
        List<ASTItem> result = null;
        while (
            !input.eof () &&
            skip.contains (input.next (1).getType ())
        ) {
            ASTToken token = input.read ();
            if (node != null)
                node.addChildren (readEmbeddings (token, skipErrors, embeddings, cancel));
            else {
                if (result == null)
                    result = new ArrayList<ASTItem> ();
                result.add (readEmbeddings (token, skipErrors, embeddings, cancel));
            }
        }
        return result;
    }
    
    private ASTItem readEmbeddings (
        ASTToken token, 
        boolean skipErrors,
        Map<String,List<ASTItem>> embeddings,
        boolean[] cancel
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
            ASTNode root = language.getAnalyser ().read (in, skipErrors, embeddings, cancel);
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
            return readNoGrammar (in, skipErrors, embeddings, cancel);
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
        Map<String,List<ASTItem>> embeddings,
        boolean[] cancel
    ) throws ParseException {
        ASTNode root = ASTNode.create (language.getMimeType(), "S", input.getIndex ());
        while (!input.eof ()) {
            ASTToken token = input.read ();
            root.addChildren (readEmbeddings (token, skipErrors, embeddings, cancel));
        }
        return root;
    }
    
    private ASTNode readNoGrammar (
        List tokens,
        int offset,
        boolean skipErrors,
        Map<String,List<ASTItem>> embeddings,
        boolean[] cancel
    ) throws ParseException {
        ASTNode root = ASTNode.create (language.getMimeType(), "S", offset);
        for (Iterator iter = tokens.iterator(); iter.hasNext(); ) {
            ASTToken token = (ASTToken) iter.next();
            root.addChildren (readEmbeddings (token, skipErrors, embeddings, cancel));
        }
        return root;
    }
    
    private ASTNode createErrorNode (ASTNode parentNode, int offset) {
        if (parentNode != null) {
            List<ASTItem> l = parentNode.getChildren ();
            if (l != null && l.size () > 0) {
                ASTItem possibleErrorNode = l.get (l.size () - 1);
                if (possibleErrorNode instanceof ASTNode)
                    if (((ASTNode) possibleErrorNode).getNT ().equals ("ERROR"))
                        return (ASTNode) possibleErrorNode;
            }
        }
        ASTNode errorNode = ASTNode.create (
            parentNode.getMimeType (),
            "ERROR", 
            null,
            offset
        );
        if (parentNode != null)
            parentNode.addChildren (errorNode);
        return errorNode;
    }
    
    private int getRule (String nt, TokenInput input) {
        Map m = first.get (nt);
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
    
    private boolean removeEmpty = false;
    private boolean removeSimple = false;
    private boolean removeEmptyN = true;
    private boolean removeSimpleN = true;
    private Set<String> empty;
    private Set<String> simple;
    
    private void initASTFeatures () {
        if (empty != null) return;
        empty = new HashSet<String> ();
        simple = new HashSet<String> ();
        Feature optimiseProperty = language.getFeature ("AST");
        if (optimiseProperty == null) return;
        
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
    
    private boolean removeNode (ASTNode node) {
        List l = node.getChildren ();
        if (!l.isEmpty ()) return false;
        initASTFeatures ();
        return removeEmpty || (removeEmptyN == empty.contains (node.getNT ()));
    }
    
    private ASTItem replaceNode (ASTNode node) {
        initASTFeatures ();
        ASTItem result = null;
        do {
            List<ASTItem> l = node.getChildren ();
            if (l.size () != 1) return result;
            if (!removeSimple && (removeSimpleN != simple.contains (node.getNT ())))
                return result;
            result = l.get (0);
            if (!(result instanceof ASTNode)) return result;
            node = (ASTNode) result;
        } while (true);
    }
    
    private boolean removeEmpty (String nt) {
        initASTFeatures ();
        return removeEmpty || (removeEmptyN == empty.contains (nt));
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
        
        @Override
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

    static int _t;
    
    public static class T {
        String type;
        String identifier;
        
        T (ASTToken t) {
            type = t.getType ();
            identifier = t.getIdentifier ();
            if (type == null && identifier == null)
                System.out.println("null null!!!");
            _t++;
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


