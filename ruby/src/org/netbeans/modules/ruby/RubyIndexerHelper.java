/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.jrubyparser.ast.FCallNode;
import org.jrubyparser.ast.MethodDefNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.ruby.elements.AstElement;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.modules.ruby.elements.MethodElement;
import org.netbeans.modules.ruby.options.TypeInferenceSettings;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * This class contains a lot of logic used to build the code indices
 * for Ruby.
 *
 * This level of indexing is not performed on the user's sources, only
 * on preindexed libraries such as the Rails gems and Ruby's own library.
 *
 * We need various extra information that relies on various heuristics
 * to determine:
 *
 * <ul>
 *  <li> Does a method take a block? If so, how many arguments should
 *     the block take and what names should the IDE suggest?
 *  <li> Should a particular method be used with parentheses or not?
 *     For example, when calling
 *      File.exists?("foo")
 *     we normally use parens around the arguments, but when calling
 *     DSL-style methods like validates_presence_of it's common not to.
 *  <li> Are one or more of the arguments expected to to be hashes?
 *     If so, what are the available hash key names? What are their
 *    "types"? (e.g. ":controller" is obviously a Rails controller,
 *    so for code completion I can offer the various controller names,
 *    in other cases we may expect a rails model or database table
 *    name and I should record these well known types somehow.
 * </ul>
 *
 *
 * @todo Rename PlatformFileIndexer, and move activesupport stuff in here
 * @todo Dynamically "patch" index entries for things I don't actually have.
 *
 * @author Tor Norbye
 */
public final class RubyIndexerHelper {
    
    private RubyIndexerHelper() {
        // Private utility class, not instantiatable
    }
    /**
     * @todo Compute parameter delimiters (e.g. DSL methods which shouldn't have params should be noted here
     * @todo Compute hash arguments
     * @todo Gotta store the WHOLE argument string, but avoid "indexOf" looks at the 
     *   attribute list from getting too perky
    // TODO - store deprecated status?
     * 
     * @param child
     * @param indexedList
     * @param notIndexedList
     * @param topLevel
     * @param signature
     * @param fo
     * @param doc
     * @return An updated signature, or null if this method should be removed from index
     */
    public static String getMethodSignature(AstElement child, int flags,
            String signature, FileObject fo, ContextKnowledge knowledge) {
        
        if (knowledge.getParserResult().getSnapshot() == null) { // tests?
            return signature;
        }

        ParserResult parserResult = knowledge.getParserResult();
        Node root = knowledge.getRoot();

        try {
            String hashNames = "";
            int originalFlags = flags;

            List<String> rdocs = null;
            if (isRubyStubs(fo)) {
                rdocs = getCallSeq(child, parserResult.getSnapshot());
            } else if (TypeInferenceSettings.getDefault().getRdocTypeInference()) {
                rdocs = AstUtilities.gatherDocumentation(parserResult.getSnapshot(), root);
            }

            // See if the method takes blocks
            //List<Node> yields = new ArrayList<Node>();
            //AstUtilities.addNodesByType(child.getNode(), new int[]{NodeType.YIELDNODE}, yields);
            //if (yields.size() > 0) {
            //    // Yes, this method appears to have a yield... compute its args
            //    // See if it's optional...
            //    for (Node n : yields) {
            //        // Store arity here
            //        // TODO - compute block names here
            //        // This is tricky though - take a look at optparse's
            //        // summarize method - it has
            //        //    yield(indent + l)
            //        // and really, it's the "l" part here that's interesting
            //        // (l=line) not say the indent string that it's prefixing 
            //        // with.
            //    }
            //}

            // Is the block optional?
            List<Node> calls = new ArrayList<Node>();
            AstUtilities.addNodesByType(child.getNode(), new NodeType[]{NodeType.FCALLNODE}, calls);
            boolean optionalBlock = false;
            for (Node call : calls) {
                if ("block_given?".equals(((FCallNode)call).getName())) { // NOI18N
                    optionalBlock = true;
                    break;
                }
            }

            // Look at callseq to see if the block appears to be available
            // or even mandatory - do some or all of the descriptions
            // include a block?
            String blockArgs = null;
            if (rdocs != null) {
                boolean seenBlock = false;
                boolean seenNonBlock = false;
                for (String line : rdocs) {
                    // Does this line imply a block?
                    // Look for methods(args) { ...
                    // or  method { ...
                    boolean seenLeftParen = false;
                    boolean seenRightParen = false;
                    boolean hasBlock = false;
                    for (int i = 0; i < line.length(); i++) {
                        char c = line.charAt(i);
                        if (c == '(') {
                            seenLeftParen = true;
                        } else if (c == ')') {
                            seenRightParen = true;
                        } else if (c == '{') {
                            if ((!seenLeftParen && !seenRightParen) ||
                                    (seenLeftParen && seenRightParen)) {
                                hasBlock = true;

                                if (blockArgs == null) {
                                    // See if I can find the yield name
                                    int blockBegin = line.indexOf('|', i+1);
                                    if (blockBegin != -1) {
                                        int blockEnd = line.indexOf('|', blockBegin+1);
                                        if (blockEnd != -1) {
                                            StringBuilder sb = new StringBuilder();
                                            for (int j = blockBegin; j < blockEnd; j++) {
                                                char d = line.charAt(j);
                                                if (Character.isLetter(d) || d == ',') {
                                                    sb.append(d);
                                                }
                                            }
                                            if (sb.length() > 0) {
                                                blockArgs = sb.toString();
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                    if (hasBlock) {
                        seenBlock = true;
                    } else {
                        seenNonBlock = true;
                    }
                }

                if (seenBlock) {
                    if (blockArgs == null) {
                        blockArgs = "";
                    }
                    if (seenNonBlock) {
                        optionalBlock = true;
                    }
                }
            }

            // How do I name this yield parameter?
            //    yield self if block_given?
            // RDoc :yield: clauses should be specificed on the line of the starting
            // element - the def
            Snapshot snapshot = parserResult.getSnapshot();
            int lineStart = GsfUtilities.getRowStart(snapshot.getText(), child.getNode().getPosition().getStartOffset());
            int lineEnd = GsfUtilities.getRowEnd(snapshot.getText(), lineStart);
            String line = snapshot.getText().subSequence(lineStart, lineEnd).toString();
            int rdocYieldIdx = line.indexOf(":yield:");
            if (rdocYieldIdx == -1) {
                rdocYieldIdx = line.indexOf(":yields:");
                if (rdocYieldIdx != -1) {
                    rdocYieldIdx += ":yields:".length();
                }
            } else {
                rdocYieldIdx += ":yield:".length();
            }
            if (rdocYieldIdx != -1) {
                // TODO : strip off "+"'s around it
                StringBuilder sb = new StringBuilder();
                for (int i = rdocYieldIdx; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (c == '+' || Character.isWhitespace(c)) {
                        continue;
                    }
                    sb.append(c);
                }

                blockArgs = sb.toString();
            }

            // Is this element one we don't want to  document? If so,
            // we probably don't want to see it either! Exclude from
            // code completion.
            int nodocIndex = line.indexOf(":nodoc:");
            if (nodocIndex != -1) {
                flags |= IndexedElement.NODOC;
            }
            int docIndex = line.indexOf(":doc:");
            if (docIndex != -1) {
                flags &= ~IndexedElement.NODOC;
            }

            String name = child.getName();

            // A few more things can imply that we have a block: an "&" in the
            // method argument list, as well as methods named "each" or "each_".
            if (blockArgs == null) {
                if ("each".equals(name)) { // NOI18N
                    blockArgs = ""; // unknown name
                } else if (name.startsWith("each_")) { // NOI18N
                    blockArgs = name.substring("each_".length()); // NOI18N
                } else if (child instanceof MethodElement) {
                    MethodElement me = (MethodElement)child;
                    // Check arg list
                    List<String> params = me.getParameters();
                    int paramCount = params != null ? params.size() : 0;
                    if (paramCount > 0 && params.get(paramCount-1).startsWith("&")) { // NOI18N
                        blockArgs = ""; // block name unknown
                        // However, make sure that the block variable is actually
                        // used -- Test::Unit::assert(context=nil,&condition) for example
                        // wasn't!
                        // Also, I really need to know whether to use do/end or { }
                    }
                }
            }

            // TODO: Add known block methods: RSpec describe, it, ...

            if (optionalBlock) {
                flags |= IndexedMethod.BLOCK;
                flags |= IndexedMethod.BLOCK_OPTIONAL;
            } else if (blockArgs != null) {
                flags |= IndexedMethod.BLOCK;
            }

            // Replace attributes
            int attributeIndex = signature.indexOf(';');
            if (flags != originalFlags || attributeIndex == -1) {
                char first = IndexedElement.flagToFirstChar(flags);
                char second = IndexedElement.flagToSecondChar(flags);
                if (attributeIndex == -1) {
                    signature = ((signature+ ";") + first) + second;
                } else {
                    signature = (signature.substring(0, attributeIndex+1) + first) + second + signature.substring(attributeIndex+3);
                }
            }

            if (blockArgs == null) {
                blockArgs = "";
            }

            MethodDefNode method = (MethodDefNode)child.getNode();
            //hashNames = getAttribute(file, fo, root, method);
            hashNames = getAttribute(fo, root, method);
            if (hashNames == null) {
                hashNames = "";
            } else {
                // Parse to make sure we don't have problems later...
                int offset = 0;
                while (true) {
                    int paren = hashNames.indexOf('(', offset);
                    if (paren != -1) {
                        paren = hashNames.indexOf(')',paren);
                        assert paren != -1;
                        offset = paren;
                    } else {
                        break;
                    }
                }
            }

            RubyType returnType = child.getType().isKnown()
                    ? child.getType()
                    : getReturnTypes(child, rdocs, knowledge);

            if (returnType.isKnown()) {
                child.setType(returnType);
            }

            // See RubyIndexer for a description of the signature format
            if (blockArgs.length() > 0 || returnType.isKnown() || hashNames.length() > 0) {
                return signature + ";" + blockArgs + ";" + returnType.asIndexedString() + ";" + hashNames; // NOI18N
            } else {
                return signature;
            }

        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        return signature;
    }
    
    public static final int DEFAULT_DOC = 0;
    public static final int NODOC = 1;
    public static final int DOC = 2;
    public static final int NODOC_ALL = 3;

    /** Should the class be removed? Return one of the _DOC constants above */
    public static int isNodocClass(AstElement child, Snapshot snapshot) {
        if (snapshot == null) {
            // tests?
            return DEFAULT_DOC;
        }
        try {
            int start = child.getNode().getPosition().getStartOffset();
            if (start > snapshot.getText().length()) {
                return DEFAULT_DOC;
            }
            int lineStart = GsfUtilities.getRowStart(snapshot.getText(), start);
            int lineEnd = GsfUtilities.getRowEnd(snapshot.getText(), lineStart);
            String line = snapshot.getText().subSequence(lineStart, lineEnd).toString();

            // Is this element one we don't want to  document? If so,
            // we probably don't want to see it either! Exclude from
            // code completion.
            int nodocIndex = line.indexOf(":nodoc:"); // NOI18N
            if (nodocIndex != -1) {
                if (line.indexOf("all", nodocIndex) != -1) { // NOI18N
                    return NODOC_ALL;
                } else {
                    return NODOC;
                }
            } else {
                int docIndex = line.indexOf(":doc:"); // NOI18N
                if (docIndex != -1) {
                    return DOC;
                }
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        return DEFAULT_DOC;
    }
    
    /** See if the given string ends with the given ending, IGNORING ALL
     * SPACES IN THE INPUT STRING. E.g.  " foo b ar " is considered ending
     * with "foobar".  "=" and "-" are considered equivalent
     * (since this method is used for callseqs - => and -> are used
     * inconsistently.
     * 
     * @param s The string to be checked
     * @param ending The ending we're looking for
     * @return True iff s ends with "ending" regardless of spaces
     */
    private static boolean endsWithIgnSpace(String s, String ending) {
        int si = s.length()-1;
        for (int ei = ending.length()-1; ei >= 0; ei--, si--) {
            while (si >= 0 && Character.isWhitespace(s.charAt(si))) {
                si--;
            }
            if (si < 0) {
                return false;
            }

            char a = Character.toLowerCase(s.charAt(si));
            char b = Character.toLowerCase(ending.charAt(ei));
            if (a != b) {
                // Consider = and - to be equivalent
                if ((a == '=' ) || (a == '-') && (b == '=' || (b == '-'))) {
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    private static List<String> getCallSeq(AstElement child, Snapshot snapshot) {
        List<String> callseq = null;
        List<String> comments = AstUtilities.gatherDocumentation(snapshot, child.getNode());
        if (comments == null) {
            return null;
        }
        for (int i = 0; i < comments.size(); i++) {
            String line = comments.get(i);
            if (!line.startsWith("#   ") || line.substring(1).trim().length() == 0) {
                // This is not a callseq
                if (i > 0) {
                    callseq = new ArrayList<String>(i);
                    for (int j = 0; j < i; j++) {
                        callseq.add(comments.get(j));
                    }
                }
                break;
            }
        }
        return callseq;
    }

    private static boolean isRubyStubs(FileObject fo) {
        return fo.getParent() != null && fo.getParent().getParent() != null &&
                RubyPlatform.RUBYSTUBS.equals(fo.getParent().getParent().getName());

    }

    static String replaceAttributes(String signature, int flags) {
        int attributeIndex = signature.indexOf(';');
        if (attributeIndex == -1) {
            char first = IndexedElement.flagToFirstChar(flags);
            char second = IndexedElement.flagToSecondChar(flags);
            if (attributeIndex == -1) {
                signature = ((signature + ";") + first) + second;
            } else {
                signature = (signature.substring(0, attributeIndex + 1) + first) + second + signature.substring(attributeIndex + 3);
            }
        }
        return signature;

    }
    static String getMethodSignatureForUserSources( AstElement methodElement,
            String signature, int flags, ContextKnowledge knowledge) {

        RubyType type = methodElement.getType();
        if (!type.isKnown()) {
            List<String> rdocs = null;
            if (TypeInferenceSettings.getDefault().getRdocTypeInference()) {
                rdocs = AstUtilities.gatherDocumentation(knowledge.getParserResult().getSnapshot(), methodElement.getNode());
            }
            type = getReturnTypes(methodElement, rdocs, knowledge);
        }
        if (type.isKnown()) {
            methodElement.setType(type);
            return signature + ";;" + methodElement.getType().asIndexedString() + ";"; // NOI18N
        }
        return signature;
    }

    private static RubyType getReturnTypes(AstElement methodElement, List<String> callseq, ContextKnowledge knowledge) {

        if (callseq != null) {
            RubyType types = RDocAnalyzer.collectTypesFromComment(callseq);
            if (types.isKnown()) {
                return types;
            }
        }
        
        if (TypeInferenceSettings.getDefault().getMethodTypeInference()) {
            AstPath path = new AstPath();
            path.descend(knowledge.getRoot());
            RubyTypeInferencer typeInferencer = RubyTypeInferencer.create(knowledge);
            return typeInferencer.inferType(methodElement.getNode());
        }

        return RubyType.createUnknown();

    }
    
    // BEGIN AUTOMATICALLY GENERATED CODE. SEE THE http://hg.netbeans.org/main/misc/ruby/indexhelper PROJECT FOR DETAILS.
    public static final String HASH_KEY_BOOL = "bool"; // NOI18N
    public static final String HASH_KEY_STRING = "string"; // NOI18N
    public static final String HASH_KEY_INTEGER = "string"; // NOI18N
    
    private static String clz(Node root, Node n) {
        AstPath path = new AstPath(root, n);
        String clz = AstUtilities.getFqnName(path);
        return clz;
    }
    
    private static String sig(MethodDefNode method) {
        return AstUtilities.getDefSignature(method);
    }
    
    private static String getAttribute(FileObject file, Node root, MethodDefNode method) {
        String n = file.getName();
        if (n.length() < 2) {
            return null;
        }
        char c = n.charAt(0);
        switch (c) {
        case 'a':
            if ("active_record_helper".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActionView::Helpers::ActiveRecordHelper".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("form(")) { // NOI18N
                         return "options(action:action)"; // NOI18N
                     }
                     if (sig.startsWith("error_messages_for(")) { // NOI18N
                         return "params(=>header_tag|id|class|object_name)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            if ("aggregations".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActiveRecord::Aggregations::ClassMethods".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("composed_of(")) { // NOI18N
                         return "options(=>class_name|mapping|allow_nil:bool)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            if ("asset_tag_helper".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActionView::Helpers::AssetTagHelper".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("auto_discovery_link_tag(")) { // NOI18N
                         return "type(:rss|:atom),tag_options(=>rel|type|title),url_options(=>anchor|only_path:bool|controller:controller|action:action|trailing_slash:bool|host|protocol)"; // NOI18N
                     }
                     if (sig.startsWith("image_tag(")) { // NOI18N
                         return "options(=>alt|size)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            if ("associations".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActiveRecord::Associations::ClassMethods".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("has_many(")) { // NOI18N
                         return "options(=>class_name|conditions|order|group|foreign_key|dependent|exclusively_dependent|finder_sql|counter_sql|extend|include|limit|offset|select|as|through|source|source_type|uniq),association_id(-table)"; // NOI18N
                     }
                     if (sig.startsWith("has_one(")) { // NOI18N
                         return "options(=>class_name|conditions|order|dependent|foreign_key|include|as)),association_id(-model)"; // NOI18N
                     }
                     if (sig.startsWith("belongs_to(")) { // NOI18N
                         return "options(=>class_name|conditions|foreign_key|counter_cache|include|polymorphic)),association_id(-model)"; // NOI18N
                     }
                     if (sig.startsWith("has_and_belongs_to_many(")) { // NOI18N
                         return "options(=>class_name|join_table|foreign_key|association_foreign_key|conditions|order|uniq:bool|finder_sql|delete_sql|insert_sql|extend|include|group|limit|offset|select)),association_id(-table)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            break;
        case 'b':
            if ("base".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if (RubyIndex.ACTIVE_RECORD_BASE.equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("find(")) { // NOI18N
                         return "args(:first|:all),args(=>conditions|order|group|limit|offset|joins|readonly:bool|include|select|from|readonly:bool|lock:bool)"; // NOI18N
                     }
                     return null;
                }
                if ("ActionController::Base".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("url_for(")) { // NOI18N
                         return "options(=>anchor|only_path:bool|controller:controller|action:action|trailing_slash:bool|host|protocol)"; // NOI18N
                     }
                     if (sig.startsWith("redirect_to(")) { // NOI18N
                         return "options(=>anchor|only_path:bool|controller:controller|action:action|trailing_slash:bool|host|protocol),options(:back|\"http://)"; // NOI18N
                     }
                     if (sig.startsWith("render(")) { // NOI18N
                         return "options(=>action:action|partial:partial|status|template|file:file|text:string|json|inline|nothing)"; // NOI18N
                     }
                     if (sig.startsWith("render_to_string(")) { // NOI18N
                         return "options(=>action:action|partial:partial|status|template|file:file|text:string|json|inline|nothing)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            if ("benchmark_helper".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActionView::Helpers::BenchmarkHelper".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("benchmark(")) { // NOI18N
                         return "level(:debug|:info|:warn|:error)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            break;
        case 'c':
            if ("calculations".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActiveRecord::Calculations::ClassMethods".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("calculate(")) { // NOI18N
                         return "options(=>conditions|joins|order|group|select|distinct:bool),operation(:count|:avg|:min|:max|:sum),column_name(-column)"; // NOI18N
                     }
                     if (sig.startsWith("count(")) { // NOI18N
                         return "args(=>conditions|joins|include|order|group|select|distinct:bool)"; // NOI18N
                     }
                     if (sig.startsWith("minimum(")) { // NOI18N
                         return "options(=>conditions|joins|order|group|select|distinct:bool),column_name(-column)"; // NOI18N
                     }
                     if (sig.startsWith("average(")) { // NOI18N
                         return "options(=>conditions|joins|order|group|select|distinct:bool),column_name(-column)"; // NOI18N
                     }
                     if (sig.startsWith("sum(")) { // NOI18N
                         return "options(=>conditions|joins|order|group|select|distinct:bool),column_name(-column)"; // NOI18N
                     }
                     if (sig.startsWith("maximum(")) { // NOI18N
                         return "options(=>conditions|joins|order|group|select|distinct:bool),column_name(-column)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            if ("cgi_process".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActionController::Base".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("process_cgi(")) { // NOI18N
                         return "session_options(=>database_manager|session_key|session_id|new_session|session_expires|session_domain|session_secure|session_path)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            break;
        case 'd':
            if ("date_helper".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActionView::Helpers::DateHelper".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("date_select(")) { // NOI18N
                         return "options(=>discard_year:bool|discard_month:bool|discard_day:bool|order|disabled:bool)"; // NOI18N
                     }
                     if (sig.startsWith("time_select(")) { // NOI18N
                         return "options(=>include_seconds:bool)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            break;
        case 'f':
            if ("form_helper".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActionView::Helpers::FormHelper".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("form_for(")) { // NOI18N
                             String path = file.getPath();
                             if (path.indexOf("-2") != -1 || path.indexOf("-1") == -1) { // NOI18N
                                 return "record_or_name_or_array(-model),(rgs=>url:hash|html:hash|builder)"; // NOI18N // NOI18N
                             } else {
                                 return "object_name(-model),args(=>url:hash|html:hash|builder)"; // NOI18N // NOI18N
                             }
                     }
                     if (sig.startsWith("fields_for(")) { // NOI18N
                             String path = file.getPath();
                             if (path.indexOf("-2") != -1 || path.indexOf("-1") == -1) { // NOI18N
                                 return "record_or_name_or_array(-model),args(=>url:hash)"; // NOI18N // NOI18N
                             } else {
                                 return "object_name(-model),args(=>url:hash)"; // NOI18N // NOI18N
                             }
                     }
                     if (sig.startsWith("text_field(")) { // NOI18N
                             String path = file.getPath();
                             if (path.indexOf("-2") != -1 || path.indexOf("-1") == -1) { // NOI18N
                                 return "record_or_name_or_array(-model)"; // NOI18N // NOI18N
                             } else {
                                 return "object_name(-model)"; // NOI18N // NOI18N
                             }
                     }
                     if (sig.startsWith("password_field(")) { // NOI18N
                             String path = file.getPath();
                             if (path.indexOf("-2") != -1 || path.indexOf("-1") == -1) { // NOI18N
                                 return "record_or_name_or_array(-model)"; // NOI18N // NOI18N
                             } else {
                                 return "object_name(-model)"; // NOI18N // NOI18N
                             }
                     }
                     if (sig.startsWith("hidden_field(")) { // NOI18N
                             String path = file.getPath();
                             if (path.indexOf("-2") != -1 || path.indexOf("-1") == -1) { // NOI18N
                                 return "record_or_name_or_array(-model)"; // NOI18N // NOI18N
                             } else {
                                 return "object_name(-model)"; // NOI18N // NOI18N
                             }
                     }
                     if (sig.startsWith("file_field(")) { // NOI18N
                             String path = file.getPath();
                             if (path.indexOf("-2") != -1 || path.indexOf("-1") == -1) { // NOI18N
                                 return "record_or_name_or_array(-model)"; // NOI18N // NOI18N
                             } else {
                                 return "object_name(-model)"; // NOI18N // NOI18N
                             }
                     }
                     if (sig.startsWith("text_area(")) { // NOI18N
                             String path = file.getPath();
                             if (path.indexOf("-2") != -1 || path.indexOf("-1") == -1) { // NOI18N
                                 return "record_or_name_or_array(-model)"; // NOI18N // NOI18N
                             } else {
                                 return "object_name(-model)"; // NOI18N // NOI18N
                             }
                     }
                     if (sig.startsWith("check_box(")) { // NOI18N
                             String path = file.getPath();
                             if (path.indexOf("-2") != -1 || path.indexOf("-1") == -1) { // NOI18N
                                 return "record_or_name_or_array(-model)"; // NOI18N // NOI18N
                             } else {
                                 return "object_name(-model)"; // NOI18N // NOI18N
                             }
                     }
                     if (sig.startsWith("radio_button(")) { // NOI18N
                             String path = file.getPath();
                             if (path.indexOf("-2") != -1 || path.indexOf("-1") == -1) { // NOI18N
                                 return "record_or_name_or_array(-model)"; // NOI18N // NOI18N
                             } else {
                                 return "object_name(-model)"; // NOI18N // NOI18N
                             }
                     }
                     return null;
                }
                return null;
            }
            if ("form_tag_helper".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActionView::Helpers::FormTagHelper".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("form_tag(")) { // NOI18N
                         return "options(=>anchor|only_path:bool|controller:controller|action:action|trailing_slash:bool|host|protocol)"; // NOI18N
                     }
                     if (sig.startsWith("select_tag(")) { // NOI18N
                         return "options(=>multiple:bool)"; // NOI18N
                     }
                     if (sig.startsWith("text_area_tag(")) { // NOI18N
                         return "options(=>size)"; // NOI18N
                     }
                     if (sig.startsWith("text_field_tag(")) { // NOI18N
                         return "options(=>disabled:bool|size|maxlength)"; // NOI18N
                     }
                     if (sig.startsWith("password_field_tag(")) { // NOI18N
                         return "options(=>disabled:bool|size|maxlength)"; // NOI18N
                     }
                     if (sig.startsWith("hidden_field_tag(")) { // NOI18N
                         return "options(=>disabled:bool|size|maxlength)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            break;
        case 'k':
            if ("kernel".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("Kernel".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("describe(")) { // NOI18N
                         return "args(=>behaviour_type|shared:bool)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            break;
        case 'l':
            if ("list".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActiveRecord::Acts::List::ClassMethods".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("acts_as_list(")) { // NOI18N
                         return "options(=>column|scope)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            break;
        case 'n':
            if ("nested_set".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActiveRecord::Acts::NestedSet::ClassMethods".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("acts_as_nested_set(")) { // NOI18N
                         return "options(=>parent_column|left_column|right_column|scope)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            if ("number_helper".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActionView::Helpers::NumberHelper".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("number_to_phone(")) { // NOI18N
                         return "options(=>area_code:bool|delimiter|extension|country_code)"; // NOI18N
                     }
                     if (sig.startsWith("number_to_currency(")) { // NOI18N
                         return "options(=>precision|unit|separator|delimiter)"; // NOI18N
                     }
                     if (sig.startsWith("number_to_percentage(")) { // NOI18N
                         return "options(=>precision|separator)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            break;
        case 'p':
            if ("pagination".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActionController::Pagination".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("paginate(")) { // NOI18N
                         return "options(=>singular_name|class_name|per_page|conditions|order|order_by|joins|join|include|selected|count)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            if ("pagination_helper".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActionView::Helpers::PaginationHelper".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("pagination_links(")) { // NOI18N
                         return "options(name|window_size|always_show_anchors:bool|link_to_current_page:bool|params),html_options(=>confirm:string|popup:bool|methodclass|id)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            if ("prototype_helper".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActionView::Helpers::PrototypeHelper".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("observe_field")) { // NOI18N
                         return "options(=>url:hash|function|frequency|update|with|on)"; // NOI18N
                     }
                     if (sig.startsWith("observe_form")) { // NOI18N
                         return "options(=>url:hash|function|frequency|update|with|on)"; // NOI18N
                     }
                     if (sig.startsWith("link_to_remote(")) { // NOI18N
                         return "options(=>url:hash|update)"; // NOI18N
                     }
                     if (sig.startsWith("remote_function(")) { // NOI18N
                         return "options(=>url:hash|update)"; // NOI18N
                     }
                     if (sig.startsWith("submit_to_remote(")) { // NOI18N
                         return "options(=>url:hash|update)"; // NOI18N
                     }
                     if (sig.startsWith("form_remote_tag(")) { // NOI18N
                         return "options(=>url:hash|update)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            break;
        case 's':
            if ("scaffolding".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActionController::Scaffolding::ClassMethods".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("scaffold(")) { // NOI18N
                         return "model_id(-model),options(=>suffix:bool)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            if ("schema_definitions".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActiveRecord::ConnectionAdapters::TableDefinition".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("column(")) { // NOI18N
                         return "type(:primary_key|:string|:text|:integer|:float|:decimal|:datetime|:timestamp|:time|:date|:binary|:boolean),options(=>limit|default:nil|null:bool|precision|scale)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            if ("schema_statements".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActiveRecord::ConnectionAdapters::SchemaStatements".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("create_table(")) { // NOI18N
                         return "options(=>id:bool|primary_key:string|options:hash|temporary:bool|force:bool)"; // NOI18N
                     }
                     if (sig.startsWith("add_column(")) { // NOI18N
                         return "table_name(-table),column_name(-column),options(=>limit|default:nil|null:bool|precision|scale),type(:primary_key|:string|:text|:integer|:float|:decimal|:datetime|:timestamp|:time|:date|:binary|:boolean)"; // NOI18N
                     }
                     if (sig.startsWith("change_column(")) { // NOI18N
                         return "table_name(-table),column_name(-column),options(=>limit|default:nil|null:bool|precision|scale),type(:primary_key|:string|:text|:integer|:float|:decimal|:datetime|:timestamp|:time|:date|:binary|:boolean)"; // NOI18N
                     }
                     if (sig.startsWith("rename_table(")) { // NOI18N
                             String path = file.getPath();
                             if (path.indexOf("-2") != -1 || path.indexOf("-1") == -1) { // NOI18N
                                 return "table_name(-table)"; // NOI18N // NOI18N
                             } else {
                                 return "name(-table)"; // NOI18N // NOI18N
                             }
                     }
                     if (sig.startsWith("rename_column(")) { // NOI18N
                         return "table_name(-table),column_name(-column)"; // NOI18N
                     }
                     if (sig.startsWith("change_column_default(")) { // NOI18N
                         return "table_name(-table),column_name(-column)"; // NOI18N
                     }
                     if (sig.startsWith("drop_table(")) { // NOI18N
                             String path = file.getPath();
                             if (path.indexOf("-2") != -1 || path.indexOf("-1") == -1) { // NOI18N
                                 return "table_name(-table)"; // NOI18N // NOI18N
                             } else {
                                 return "name(-table)"; // NOI18N // NOI18N
                             }
                     }
                     if (sig.startsWith("add_index(")) { // NOI18N
                         return "table_name(-table),column_name(-column)"; // NOI18N
                     }
                     if (sig.startsWith("remove_index(")) { // NOI18N
                         return "table_name(-table)"; // NOI18N
                     }
                     if (sig.startsWith("remove_column(")) { // NOI18N
                         return "table_name(-table),column_name(-column)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            if ("session_management".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActionController::SessionManagement::ClassMethods".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("session_store=(")) { // NOI18N
                         return "store(:active_record_store|:drb_store|:mem_cache_store|:memory_store)"; // NOI18N
                     }
                     if (sig.startsWith("session(")) { // NOI18N
                         return "args(=>on:bool|off:bool|only|except|database_manager|session_key|session_id|new_session|session_expires|session_domain|session_secure|session_path)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            if ("streaming".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActionController::Streaming".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("send_file(")) { // NOI18N
                         return "options(=>filename|type|disposition|stream|buffer_size|status)"; // NOI18N
                     }
                     if (sig.startsWith("send_data(")) { // NOI18N
                         return "options(=>filename|type|disposition|status)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            break;
        case 't':
            if ("tree".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActiveRecord::Acts::Tree::ClassMethods".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("acts_as_tree(")) { // NOI18N
                         return "options(=>foreign_key|order|counter_cache)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            break;
        case 'u':
            if ("url_helper".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActionView::Helpers::UrlHelper".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("url_for(")) { // NOI18N
                         return "options(=>escape:bool|anchor|only_path:bool|controller:controller|action:action|trailing_slash:bool|host|protocol)"; // NOI18N
                     }
                     if (sig.startsWith("link_to(")) { // NOI18N
                         return "options(=>anchor|only_path:bool|controller:controller|action:action|trailing_slash:bool|host|protocol),html_options(=>confirm:string|popup:bool|methodclass|id)"; // NOI18N
                     }
                     if (sig.startsWith("button_to(")) { // NOI18N
                         return "options(=>anchor|only_path:bool|controller:controller|action:action|trailing_slash:bool|host|protocol),html_options(=>confirm:string|popup:bool|method|disabled:boolclass|id)"; // NOI18N
                     }
                     if (sig.startsWith("mail_to(")) { // NOI18N
                         return "html_options(=>encode|replace_at|replace_dot|subject|body|cc|bccclass|id)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            break;
        case 'v':
            if ("validations".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActiveRecord::Validations::ClassMethods".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("validates_each(")) { // NOI18N
                         return "attrs(=>on:validationactive|allow_nil:bool|if)"; // NOI18N
                     }
                     if (sig.startsWith("validates_confirmation_of(")) { // NOI18N
                         return "attr_names(=>on:validationactive|message|if)"; // NOI18N
                     }
                     if (sig.startsWith("validates_acceptance_of(")) { // NOI18N
                         return "attr_names(=>on:validationactive|message|if|accept)"; // NOI18N
                     }
                     if (sig.startsWith("validates_presence_of(")) { // NOI18N
                         return "attr_names(=>on:validationactive|message|if)"; // NOI18N
                     }
                     if (sig.startsWith("validates_length_of(")) { // NOI18N
                         return "attrs(=>minimum|maximum|is|within|in|allow_nil:bool|too_long|too_short|wrong_length|on:validationactive|message|if)"; // NOI18N
                     }
                     if (sig.startsWith("validates_uniqueness_of(")) { // NOI18N
                         return "attr_names(=>message|scope|case_sensitive:bool|allow_nil:bool|if)"; // NOI18N
                     }
                     if (sig.startsWith("validates_format_of(")) { // NOI18N
                         return "attr_names(=>on:validationactive|message|if|with)"; // NOI18N
                     }
                     if (sig.startsWith("validates_inclusion_of(")) { // NOI18N
                         return "attr_names(=>in|message|allow_nil:bool|if)"; // NOI18N
                     }
                     if (sig.startsWith("validates_exclusion_of(")) { // NOI18N
                         return "attr_names(=>in|message|allow_nil:bool|if)"; // NOI18N
                     }
                     if (sig.startsWith("validates_associated(")) { // NOI18N
                         return "attr_names(=>on:validationactive|if)"; // NOI18N
                     }
                     if (sig.startsWith("validates_numericality_of(")) { // NOI18N
                         return "attr_names(=>on:validationactive|message|if|only_integer:bool|allow_nil:bool)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            if ("verification".equals(n)) { // NOI18N
                String clz = clz(root,method);
                if ("ActionController::Verification::ClassMethods".equals(clz)) { // NOI18N
                     String sig = sig(method);
                     if (sig.startsWith("verify(")) { // NOI18N
                         return "options(=>params|session|flash|method|post:submitmethod|xhr:bool|add_flash:hash|add_headers:hash|redirect_to|render|only:bool|except:bool)"; // NOI18N
                     }
                     return null;
                }
                return null;
            }
            break;
        }
        return null;
    }
    // END AUTOMATICALLY GENERATED CODE
}
