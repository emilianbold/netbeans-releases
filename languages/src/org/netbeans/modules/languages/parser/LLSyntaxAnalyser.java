/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.Rule;


/**
 *
 * @author Jan Jancura
 */
public class LLSyntaxAnalyser {

    private Language        language;
    private List<Rule>      grammarRules;
    private First           first;
    private Set<Integer>    skipTokenTypes;
    private int             traceSteps = -1;
    private boolean         printFirst = false;
    
    
    private LLSyntaxAnalyser (
        Language language, 
        List<Rule> grammarRules,
        Set<Integer> skipTokenTypes
    ) {
        this.language = language;
        this.grammarRules = grammarRules;
        this.skipTokenTypes = skipTokenTypes;
    }
    
    
    // public methods ..........................................................

    public List<Rule> getRules () {
        return grammarRules;
    }
    
    public Set<Integer> getSkipTokenTypes () {
        return skipTokenTypes;
    }
    
    public static LLSyntaxAnalyser create (
        Language language, 
        List<Rule> grammarRules,
        Set<Integer> skipTokenTypes
    ) throws ParseException {
        LLSyntaxAnalyser a = new LLSyntaxAnalyser (language, grammarRules, skipTokenTypes);
        a.initTracing ();
        a.first = First.create (a.grammarRules, language);
//        boolean hasConflicts = AnalyserAnalyser.printConflicts (a.first, null);
//        if (hasConflicts)
//            AnalyserAnalyser.printRules (a.grammarRules, null);
        //if (a.printFirst)
//            AnalyserAnalyser.printF (a.first, null, language);
//        System.out.println(a.first);
//        AnalyserAnalyser.printUndefinedNTs (a.grammarRules, null);
        return a;
    }
    
    public static LLSyntaxAnalyser createEmpty (Language language) {
        LLSyntaxAnalyser a = new LLSyntaxAnalyser (
            language, 
            Collections.<Rule>emptyList (),
            Collections.<Integer>emptySet ()
        );
        try {
            a.first = First.create (Collections.<Rule>emptyList (), language);
        } catch (ParseException ex) {
            ex.printStackTrace ();
        }
        return a;
    }
    
    public ASTNode read (
        TokenInput input, 
        boolean skipErrors, 
        boolean[] cancel
    ) throws ParseException {
        Map<String,List<ASTItem>> embeddings = new HashMap<String, List<ASTItem>> ();
        ASTNode root;
        try {
            if (grammarRules.isEmpty () || input.eof ()) {
                root = readNoGrammar (input, skipErrors, embeddings, cancel);
            } else {
                root = read2 (input, skipErrors, embeddings, cancel);
            }
        } catch (CancelledException ex) {
            return null;
        }
        if (embeddings.isEmpty ()) {
            inspect (root);
            return root;
        }
        List<ASTItem> roots = new ArrayList<ASTItem> ();
        Iterator<String> it = embeddings.keySet ().iterator ();
        while (it.hasNext ()) {
            String mimeType =  it.next ();
            List<ASTItem> tokens = embeddings.get (mimeType);
            Language language = LanguagesManager.getDefault ().getLanguage (mimeType);
            TokenInput in = TokenInputUtils.create (tokens);
            ASTNode r = language.getAnalyser ().read (in, skipErrors, cancel);
            if(r == null) {
                continue;
            }
            Feature astProperties = language.getFeature ("AST");
            if (astProperties != null) {
                String process_embedded = (String)astProperties.getValue("process_embedded");
                if(process_embedded == null || Boolean.valueOf(process_embedded)) {
                    ASTNode newRoot = (ASTNode) astProperties.getValue (
                        "process", 
                        SyntaxContext.create (null, ASTPath.create (r))
                    );
                    if (newRoot != null)
                        r = newRoot;
                }
            }
            roots.add (r);
        }
        roots.add (root);
        ASTNode result = ASTNode.createCompoundASTNode (language, "Root", roots, 0);
        inspect (result);
        return result;
    }
    
    private void inspect (ASTItem item) {
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
            inspect (child);
        }
    }
    
    
    // helper methods ..........................................................
    
    private ASTNode read (
        TokenInput input, 
        boolean skipErrors, 
        Map<String,List<ASTItem>> embeddings,
        boolean[] cancel
    ) throws ParseException, CancelledException {
        if (grammarRules.isEmpty () || input.eof ())
            return readNoGrammar (input, skipErrors, embeddings, cancel);
        return read2 (input, skipErrors, embeddings, cancel);
    }
    
    private ASTNode read2 (
        TokenInput input, 
        boolean skipErrors, 
        Map<String,List<ASTItem>> embeddings,
        boolean[] cancel
    ) throws ParseException, CancelledException {
        Stack<Object> stack = new Stack<Object> ();
        ASTNode root = null, node = null;
        Iterator it = Collections.singleton ("S").iterator ();
        boolean firstLine = true;
        do {
            if (cancel [0]) throw new CancelledException ();
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
                int newRule = first.getRule (language.getNTID (nt), input, skipTokenTypes);
                if (newRule < 0) {
                    if (!skipErrors) {
                        if (node == null)
                            root = node = ASTNode.create (language, "Root", whitespaces, offset);
                        throw new ParseException ("Syntax error (nt: " + nt + ", tokens: " + input.next (1) + " " + input.next (2) + ".", root);
                    }
                    if (input.eof ()) {
                        if (node == null)
                            root = node = ASTNode.create (language, "Root", whitespaces, offset);
                        createErrorNode (node, input.getOffset ());
                        //S ystem.out.println(input.getIndex () + ": unexpected eof " + nt);
                        return root;
                    }
                    //S ystem.out.println(input.getIndex () + ": no rule for " + nt + "&" + input.next (0));
                    createErrorNode (node, input.getOffset ()).addChildren (readEmbeddings (input.read (), skipErrors, embeddings, cancel));
                } else {
                    Rule rule = grammarRules.get (newRule);
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
                                if (rule.getRight ().isEmpty () && removeEmpty (language, rule.getNT ()))
                                    continue;
                                ASTNode nnode = ASTNode.create (
                                    language,
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
                    if(input.next(1).getTypeName ().equals(Language.GAP_TOKEN_TYPE_NAME)) {
//                        System.out.println("token " + token + " is not compatible with " + input.next(1));
                        input.read();
                    } else {
                        if (!skipErrors)
                            throw new ParseException ("Unexpected token " + input.next (1) + ". Expecting " + token, root);
                        createErrorNode (node, input.getOffset ()).addChildren (readEmbeddings (input.read (), skipErrors, embeddings, cancel));
                        //S ystem.out.println(input.getIndex () + ": unrecognized token " + token + "<>" + input.next (1));
                    }
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
                language,
                "Root", 
                readWhitespaces (node, input, skipErrors, embeddings, cancel), 
                input.getOffset ()
            );
        }
        return root;
    }
    
    private static boolean isCompatible (ASTToken t1, ASTToken t2) {
        if (t1.getTypeID () == -1) {
            return t1.getIdentifier ().equals (t2.getIdentifier ());
        } else {
            if (t1.getIdentifier () == null)
                return t1.getTypeID () == t2.getTypeID ();
            else
                return t1.getTypeID () == t2.getTypeID () && 
                       t1.getIdentifier ().equals (t2.getIdentifier ());
        }
    }
    
    private List<ASTItem> readWhitespaces (
        ASTNode node, 
        TokenInput input, 
        boolean skipErrors,
        Map<String,List<ASTItem>> embeddings,
        boolean[] cancel
    ) throws ParseException, CancelledException {
        List<ASTItem> result = null;
        while (
            !input.eof () &&
            skipTokenTypes.contains (input.next (1).getTypeID ())
        ) {
            if (cancel [0]) throw new CancelledException ();
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
    ) throws ParseException, CancelledException {
        List<ASTItem> children = token.getChildren ();
        if (children.isEmpty ())
            return token;

        TokenInput in = TokenInputUtils.create (children);
        String mimeType = children.get (0).getMimeType ();
        Language language = (Language) children.get (0).getLanguage ();
        if (language == null)
            return readNoGrammar (in, skipErrors, embeddings, cancel);

        //HACK should be deleted - inner language should not define its embedding to other languages...
        Feature astp = language.getFeature ("AST");
        if (astp != null) {
            String skip_embedded = (String)astp.getValue("skip_embedded");
            if(skip_embedded != null && Boolean.valueOf(skip_embedded)) {
                return skipEmbedding (token, embeddings, children, mimeType);
            }
        }
        //HACK END
        
        Language outerLanguage = (Language) token.getLanguage ();
        if (outerLanguage != null) {
            Feature f = outerLanguage.getPreprocessorImport ();
            if (f != null && 
                f.getValue ("mimeType").equals (mimeType) &&
                f.getBoolean ("continual", false)
            )
                return skipEmbedding (token, embeddings, children, mimeType);
            f = outerLanguage.getTokenImports ().get (token.getTypeName ());
            if (f != null && 
                f.getValue ("mimeType").equals (mimeType) &&
                f.getBoolean ("continual", false)
            )
                return skipEmbedding (token, embeddings, children, mimeType);
        }

        Feature astProperties = language.getFeature ("AST");
        ASTNode root = language.getAnalyser ().read (in, skipErrors, embeddings, cancel);
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
            outerLanguage,
            token.getTypeID (),
            token.getIdentifier (),
            token.getOffset (),
            token.getLength (),
            Collections.<ASTItem>singletonList (root)
        );
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
            l.addAll (children.subList (0, children.size ()));
            appendGap (l);
        } else {
            ASTToken token1 = (ASTToken) l.get (l.size () - 1);
            ASTToken token2 = (ASTToken) children.get (0);
            if (token1.getTypeID () == token2.getTypeID ()) {
                l.remove (l.size () - 1);
                ASTToken joinedToken = join (token1, token2);
                l.add (joinedToken);
                l.addAll (children.subList (1, children.size ()));
            } else
                l.addAll (children);
            appendGap (l);
        }
        return ASTToken.create (
            token.getLanguage (),
            token.getTypeID (),
            token.getIdentifier (),
            token.getOffset (),
            token.getLength (),
            Collections.<ASTItem>emptyList ()
        );
    }
    
    private static ASTToken join (ASTToken token1, ASTToken token2) {
        List<ASTItem> token1Children = token1.getChildren ();
        List<ASTItem> token2Children = token2.getChildren ();
        List<ASTItem> joinedChildren = new ArrayList<ASTItem> ();
        if (token1Children.size () > 1 && token2Children.size () > 0) {
            ASTToken t1 = (ASTToken) token1Children.get (token1Children.size () - 2);
            ASTToken t2 = (ASTToken) token2Children.get (0);
            if (
                    ("js_string".equals (t1.getTypeName ()) && "js_string".equals (t2.getTypeName ())) ||
                    ("css_string".equals (t1.getTypeName ()) && "css_string".equals (t2.getTypeName ()))
            ) {
                joinedChildren.addAll (token1Children.subList (0, token1Children.size () - 2));
                joinedChildren.add (ASTToken.create (
                        t1.getLanguage (),
                        t1.getTypeID (),
                        t1.getIdentifier () + t2.getIdentifier (),
                        t1.getOffset ()
                ));
                joinedChildren.addAll (token2Children.subList (1, token2Children.size ()));
            } else {
                joinedChildren.addAll (token1Children);
                joinedChildren.addAll (token2Children);
            }
        } else {
            joinedChildren.addAll (token1Children);
            joinedChildren.addAll (token2Children);
        }
        return ASTToken.create (
            token1.getLanguage (),
            token1.getTypeID (),
            "",
            token1.getOffset (),
            token2.getEndOffset () - token1.getOffset (),
            joinedChildren
        );
    }
    
    private static void appendGap (List<ASTItem> children) {
        ASTToken lastToken = (ASTToken) children.get (children.size () - 1);
        if (lastToken.getChildren ().isEmpty ()) return;
        List<ASTItem> lastTokenChildren = new ArrayList<ASTItem> (lastToken.getChildren ());
        lastTokenChildren.add (ASTToken.create (
            lastTokenChildren.get (0).getLanguage (),
            Language.GAP_TOKEN_TYPE_NAME,
            "",
            lastTokenChildren.get (lastTokenChildren.size () - 1).getEndOffset (),
            0,
            null
        ));
        children.remove (children.size () - 1);
        children.add (ASTToken.create (
            lastToken.getLanguage (),
            lastToken.getTypeID (),
            lastToken.getIdentifier (),
            lastToken.getOffset (),
            lastToken.getLength (),
            lastTokenChildren
        ));
    }
    
    private ASTNode readNoGrammar (
        TokenInput input,
        boolean skipErrors,
        Map<String,List<ASTItem>> embeddings,
        boolean[] cancel
    ) throws ParseException, CancelledException {
        ASTNode root = ASTNode.create (language, "S", input.getIndex ());
        while (!input.eof ()) {
            if (cancel [0]) throw new CancelledException ();
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
    ) throws ParseException, CancelledException {
        ASTNode root = ASTNode.create (language, "S", offset);
        for (Iterator iter = tokens.iterator(); iter.hasNext(); ) {
            if (cancel [0]) throw new CancelledException ();
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
            language,
            "ERROR", 
            null,
            offset
        );
        if (parentNode != null)
            parentNode.addChildren (errorNode);
        return errorNode;
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
            AnalyserAnalyser.printRules (grammarRules, null);
        printFirst = properties.getBoolean ("printFirst", false);
    }
    
    private boolean removeNode (ASTNode node) {
        List l = node.getChildren ();
        if (!l.isEmpty ()) return false;
        ASTFeatures astFeatures = ASTFeatures.get ((Language) node.getLanguage ());
        return astFeatures.removeEmpty || (astFeatures.removeEmptyN == astFeatures.empty.contains (node.getNT ()));
    }
    
    private ASTItem replaceNode (ASTNode node) {
        ASTFeatures astFeatures = ASTFeatures.get ((Language) node.getLanguage ());
        ASTItem result = null;
        do {
            List<ASTItem> l = node.getChildren ();
            if (l.size () != 1) return result;
            if (!astFeatures.removeSimple && (astFeatures.removeSimpleN != astFeatures.simple.contains (node.getNT ())))
                return result;
            result = l.get (0);
            if (!(result instanceof ASTNode)) return result;
            node = (ASTNode) result;
        } while (true);
    }
    
    private boolean removeEmpty (Language language, String nt) {
        ASTFeatures astFeatures = ASTFeatures.get (language);
        return astFeatures.removeEmpty || (astFeatures.removeEmptyN == astFeatures.empty.contains (nt));
    }
        
    
    // innerclasses ............................................................
    
    public static class T {
        int    type;
        String identifier;
        
        T (ASTToken t) {
            type = t.getTypeID ();
            identifier = t.getIdentifier ();
            if (type == -1 && identifier == null)
                System.out.println("null null!!!");
        }
        
        public boolean equals (Object o) {
            if (!(o instanceof T)) return false;
            return (((T) o).type == -1 || ((T) o).type == type) &&
                   (((T) o).identifier == null || ((T) o).identifier.equals (identifier));
        }
        
        public int hashCode () {
            return (type + 1) *
                   (identifier == null ? -1 : identifier.hashCode ());
        }
        
        public String toString () {
            if (type == -1)
                return "\"" + identifier + "\"";
            if (identifier == null)
                return "<" + type + ">";
            return "[" + type + "," + identifier + "]";
        }
        
        public String toString (Language language) {
            if (type == -1)
                return "\"" + identifier + "\"";
            String typeName = language.getTokenType (type);
            if (identifier == null)
                return "<" + typeName + ">";
            return "[" + typeName + "," + identifier + "]";
        }
    }
    
    private class CancelledException extends Exception {
        
    }
}


