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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.jruby.ast.FCallNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.elements.AstElement;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.modules.ruby.elements.MethodElement;
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
 * @author Tor Norbye
 */
public class RubyIndexerHelper {

    /** Set to true to include. Conditional compilation helps me keep
     * the bytecode size of this class tiny since no users will
     * need this.
     */
    public static final boolean AVAILABLE = false;

    /**
     * @todo Compute parameter delimiters (e.g. DSL methods which shouldn't have params should be noted here
     * @todo Compute hash arguments
     * @todo Figure out why block_given? doesn't work
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
    public static String getMethodSignature(AstElement child, Node root, Set<Map<String, String>> indexedList, 
            Set<Map<String, String>> notIndexedList, int flags, String signature, FileObject fo, BaseDocument doc) {
        if (AVAILABLE) {
            if (doc == null) { // tests?
                return signature;
            }
            
            try {
                String blockArgs = null;
                String returnTypes = "";
                String hashNames = "";
                int originalFlags = flags;

                // TODO: Check the comment to see if it implies having a block
                List<String> comments = AstUtilities.gatherDocumentation(null, doc, child.getNode());
                List<String> callseq = null;
                // Remove all but the callseq
                if (comments != null && fo.getParent() != null && fo.getParent().getParent() != null &&
                        "rubystubs".equals(fo.getParent().getParent().getName())) {
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
                }
                

                // See if the method takes blocks
                List<Node> yields = new ArrayList<Node>();
                AstUtilities.addNodesByType(child.getNode(), new int[]{NodeTypes.YIELDNODE}, yields);
                if (yields.size() > 0) {
                    // Yes, this method appears to have a yield... compute its args
                    // See if it's optional...
                    for (Node n : yields) {
                        // Store arity here
                        // TODO - compute block names here
                        // This is tricky though - take a look at optparse's
                        // summarize method - it has
                        //    yield(indent + l)
                        // and really, it's the "l" part here that's interesting
                        // (l=line) not say the indent string that it's prefixing 
                        // with.
                    }
                }

                // Is the block optional?
                List<Node> calls = new ArrayList<Node>();
                AstUtilities.addNodesByType(child.getNode(), new int[]{NodeTypes.FCALLNODE}, calls);
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
                if (callseq != null) {
                    boolean seenBlock = false;
                    boolean seenNonBlock = false;
                    for (String line : callseq) {
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
                int lineStart = Utilities.getRowStart(doc, child.getNode().getPosition().getStartOffset());
                int lineEnd = Utilities.getRowEnd(doc, lineStart);
                String line = doc.getText(lineStart, lineEnd - lineStart);
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
                        }
                    }
                }

                if (optionalBlock) {
                    flags |= IndexedMethod.BLOCK;
                    flags |= IndexedMethod.BLOCK_OPTIONAL;
                } else if (blockArgs != null) {
                    flags |= IndexedMethod.BLOCK;
                }
                
                // Compute return types
                if (name.equals("to_s")) {
                    returnTypes = "String";
                } // XXX what else?
                
                if (callseq != null) {
                    // TODO - handle methods like the "slice" method in
                    // String which has a number of competing callseqs
                    // but each one is replicated so actually only one of
                    // them applies, and I can look at the parameter name
                    // to determine which one I care about!
                    Set<String> rets = new HashSet<String>();
                    for (String l : callseq) {
                       if (endsWithIgnSpace(l, "=>str") ||
                           endsWithIgnSpace(l, "=>new_str") ||
                           endsWithIgnSpace(l, "=>strornil") ||
                           endsWithIgnSpace(l, "=>string") ||
                           endsWithIgnSpace(l, "=>aString") ||
                           endsWithIgnSpace(l, "=>stringornil")) {
                           rets.add("String");
                       } else 
                       if (endsWithIgnSpace(l, "=>strio")) {
                            rets.add("StringIO");
                       }
                       if (endsWithIgnSpace(l, "=>file")) {
                            rets.add("File");
                       }
                       if (endsWithIgnSpace(l, "=>thread") ||
                           endsWithIgnSpace(l, "=>thr")) {
                            rets.add("Thread");
                       }
                       if (endsWithIgnSpace(l, "=>trueorfalse") ||
                            endsWithIgnSpace(l, "=>true,false,ornil") ||
                            endsWithIgnSpace(l, "=>bool") ||
                            endsWithIgnSpace(l, "=>boolean")) {
                           rets.add("boolean");
                       } else
                       if (endsWithIgnSpace(l, "=>fixnumornil") ||
                           endsWithIgnSpace(l, "=>fixnum") ||
                            endsWithIgnSpace(l, "=>aFixnum")) {
                           rets.add("Fixnum");
                       }
                       if (endsWithIgnSpace(l, "=>integer") ||
                           endsWithIgnSpace(l, "=>int") ||
                            endsWithIgnSpace(l, "=>integerornil")) {
                           rets.add("Integer");
                       }
                       if (endsWithIgnSpace(l, "=>numornil") ||
                           endsWithIgnSpace(l, "=>num") ||
                           endsWithIgnSpace(l, "=>numeric")) {
                           rets.add("Numeric");
                       }
                       if (endsWithIgnSpace(l, "=>symbol") ||
                           endsWithIgnSpace(l, "=>aSymbol") ||
                           endsWithIgnSpace(l, "=>sym")) {
                           rets.add("Symbol");
                       }
                       if (endsWithIgnSpace(l, "=>float") ||
                           endsWithIgnSpace(l, "=>fl")) {
                           rets.add("Float");
                       }
                       if (endsWithIgnSpace(l, "=>array") ||
                           endsWithIgnSpace(l, "=>arrayornil") ||
                           endsWithIgnSpace(l, "=>anArray") ||
                           endsWithIgnSpace(l, "=>arrayornil") ||
                           endsWithIgnSpace(l, "=>an_arrayornil") ||
                           endsWithIgnSpace(l, "=>an_array")) {
                           rets.add("Array");
                       }
                       if (endsWithIgnSpace(l, "=>hash") ||
                           endsWithIgnSpace(l, "=>aHash") ||
                           endsWithIgnSpace(l, "=>hsh") ||
                           endsWithIgnSpace(l, "=>hshornil") ||
                           endsWithIgnSpace(l, "=>a_hash")) {
                           rets.add("Hash");
                       }
                       if (endsWithIgnSpace(l, "=>matchdata") ||
                           endsWithIgnSpace(l, "=>matchdataornil")) {
                           rets.add("MatchData");
                       }
                       if (endsWithIgnSpace(l, "=>regexp")) {
                           rets.add("Regexp");
                       }
                       if (endsWithIgnSpace(l, "=>class") ||
                           endsWithIgnSpace(l, "=>a_class")) {
                           rets.add("Class");
                       }
                       if (endsWithIgnSpace(l, "=>mod") ||
                           endsWithIgnSpace(l, "=>a_mod") ||
                           endsWithIgnSpace(l, "=>module")) {
                           rets.add("Module");
                       }
                       if (endsWithIgnSpace(l, "=>exception") ||
                           endsWithIgnSpace(l, "=>an_exceptionorexc")) {
                           rets.add("Exception");
                       }
                       if (endsWithIgnSpace(l, "=>range") ||
                           endsWithIgnSpace(l, "=>rng")) {
                           rets.add("Range");
                       }
                       if (endsWithIgnSpace(l, "=>stat")) {
                           rets.add("File::Stat");
                       }
                       if (endsWithIgnSpace(l, "=>time") ||
                           endsWithIgnSpace(l, "=>aTime") ||
                           endsWithIgnSpace(l, "=>anArray") ||
                           endsWithIgnSpace(l, "=>an_array")) {
                           rets.add("Time");
                       }
                       
                       if (rets.size() == 0 && (line.contains("=>") || line.contains("->"))) {
                           String returnExp = line.substring(Math.max(line.indexOf("=>"), line.indexOf("->")));
                           if (returnExp.indexOf("obj") == -1) { // Don't warn about obj
                               System.out.println("Warning: no return type found for " + returnExp);
                           }
                       }
                    }
                    
                    // I can't handle the case where there are multiple
                    // return types implied by the call seqs, since
                    // they could be referring to slightly different
                    // method signatures (rdoc which produced the stubs
                    // will produce separate ones but just include the
                    // same whole comment with all callseqs on each and
                    // every one
                    if (rets.size() == 1) {
                        returnTypes = rets.iterator().next();
                    }
                }
                
                // Methods ending with "?" are probably question methods
                // returning a boolean
                if (returnTypes.length() == 0 && name.endsWith("?")) {
                    returnTypes = "boolean";
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
                
                hashNames = getHashArgs(child, root, child.getNode(), doc);
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
                
                
                //     * Format:  methodname+";"+args+;+modifiers+;+blockargs+;+returntypes;hashnames
                if (blockArgs.length() > 0 || returnTypes.length() > 0 || hashNames.length() > 0) {
                    return signature + ";" + blockArgs + ";" + returnTypes + ";" + hashNames;
                } else {
                    return signature;
                }

            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            }
        } else {
            // Must be enabled when run
            assert false;
        }

        return signature;
    }
    
    public static final int DEFAULT_DOC = 0;
    public static final int NODOC = 1;
    public static final int DOC = 2;
    public static final int NODOC_ALL = 3;

    /** Should the class be removed? 0=no, 1=yes, and all children, 2=yes, but not children */
    public static int isNodocClass(AstElement child, FileObject fo, BaseDocument doc) {
        if (AVAILABLE) {
            if (doc == null) {
                // tests?
                return DEFAULT_DOC;
            }
            try {
                int start = child.getNode().getPosition().getStartOffset();
                if (start > doc.getLength()) {
                    return DEFAULT_DOC;
                }
                int lineStart = Utilities.getRowStart(doc, start);
                int lineEnd = Utilities.getRowEnd(doc, lineStart);
                String line = doc.getText(lineStart, lineEnd - lineStart);

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
        } else {
            // Must be enabled when run
            assert false;
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
        if (AVAILABLE) {
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
        } else {
            return false;
        }
    }

    public static final String HASH_KEY_BOOL = "bool";
    public static final String HASH_KEY_STRING = "string";
    public static final String HASH_KEY_INTEGER = "string";
    
    private static String getHashArgs(AstElement element, Node root, Node n, BaseDocument doc) throws BadLocationException {
        if (AVAILABLE) {
            // Builtins
            AstPath path = new AstPath(root, n);
            String clz = AstUtilities.getFqnName(path);
            String signature = AstUtilities.getDefSignature((MethodDefNode)n);
            String fqn = clz + "#" + signature;
            String TABLENAME = "-table";
            String COLUMNNAME = "-column";
            String MODELNAME = "-model";
            String VALIDATIONACTIVE = "validationactive"; // hash type
            String SUBMITMETHOD = "submitmethod"; // hash type
            
            String TABLE_OPTIONS = "(=>id:bool|primary_key:string|options:hash|temporary:bool|force:bool)";
            String TABLE_COLUMN_OPTIONS = "(=>limit|default:nil|null:bool|precision|scale)";
            String TABLE_COLUMN_TYPE = "(:primary_key|:string|:text|:integer|:float|:decimal|:datetime|:timestamp|:time|:date|:binary|:boolean)";
            String HTML_HASH_OPTIONS="class|id"; // XXX what else?

            // ActiveRecord
            if (clz.equals("ActiveRecord::ConnectionAdapters::SchemaStatements")) {
                if (signature.startsWith("create_table(")) {
                    return "options"+TABLE_OPTIONS;
                } else if (signature.startsWith("add_column(")) {
                    return "table_name(" + TABLENAME + ")," +
                           "column_name(" + COLUMNNAME + ")," +
                           "options"+TABLE_COLUMN_OPTIONS + "," +
                           "type" + TABLE_COLUMN_TYPE;
                } else if (signature.startsWith("change_column(")) {
                    return "table_name(" + TABLENAME + ")," +
                           "column_name(" + COLUMNNAME + ")," +                        
                           "options"+TABLE_COLUMN_OPTIONS + "," +
                           "type" + TABLE_COLUMN_TYPE;
                } else if (signature.startsWith("rename_table(")) {
                    return "name(" + TABLENAME + ")";
                } else if (signature.startsWith("rename_column(")) {
                    return "table_name(" + TABLENAME + ")," +
                           "column_name(" + COLUMNNAME + ")";
                } else if (signature.startsWith("change_column_default(")) {
                    return "table_name(" + TABLENAME + ")," +
                           "column_name(" + COLUMNNAME + ")";
                } else if (signature.startsWith("drop_table(")) {
                    return "name(" + TABLENAME + ")";
                } else if (signature.startsWith("add_index(")) {
                    return "table_name(" + TABLENAME + ")," +
                           "column_name(" + COLUMNNAME + ")";
                } else if (signature.startsWith("remove_index(")) {
                    return "table_name(" + TABLENAME + ")";
                } else if (signature.startsWith("remove_column(")) {
                    return "table_name(" + TABLENAME + ")," +
                           "column_name(" + COLUMNNAME + ")";
                }
            } else if (clz.equals("ActiveRecord::ConnectionAdapters::TableDefinition")) {
                if (signature.startsWith("column(")) {
                    return "type"+TABLE_COLUMN_TYPE + "," +
                        "options" + TABLE_COLUMN_OPTIONS;
                }
            } else if (clz.equals("ActiveRecord::Base")) {
                if (signature.startsWith("find(")) {
                    return "args(:first|:all),args(=>conditions|order|group|limit|offset|joins|readonly:bool|include|select|from|readonly:bool|lock:bool)";
                } // TODO others are missing - with_scope etc.
            }
            
            // What about the validates_presence_of stuff?
            if (clz.equals("ActiveRecord::Associations::ClassMethods")) {
                if (signature.startsWith("has_many(")) {
                    // NOTE - when you add this to a class, a number of new methods are added to it;
                    // I should teach code completion about that
                    // TODO - I can help with the class_name and foreign_key attributes here!
                    String hasManyOptions = "(=>class_name|conditions|order|group|foreign_key|dependent|exclusively_dependent|" +
                      "finder_sql|counter_sql|extend|include|limit|offset|select|as|through|source|source_type|uniq)";
                    return "options" + hasManyOptions + ",association_id(" + TABLENAME + ")";
                } else if (signature.startsWith("has_one(")) {
                    // NOTE - when you add this to a class, a number of new methods are added to it;
                    // I should teach code completion about that
                    // TODO - I can help with the class_name and foreign_key attributes here!
                    String hasOneOptions = "(=>class_name|conditions|order|dependent|foreign_key|include|as)";
                    return "options" + hasOneOptions + "),association_id(" + MODELNAME + ")";
                } else if (signature.startsWith("belongs_to(")) {
                    // NOTE - when you add this to a class, a number of new methods are added to it;
                    // I should teach code completion about that
                    // TODO - I can help with the class_name and foreign_key attributes here!
                    String belongsToOptions = "(=>class_name|conditions|foreign_key|counter_cache|include|polymorphic)";
                    return "options" + belongsToOptions + "),association_id(" + MODELNAME + ")";
                } else if (signature.startsWith("has_and_belongs_to_many(")) {
                    // NOTE - when you add this to a class, a number of new methods are added to it;
                    // I should teach code completion about that
                    // TODO - I can help with the class_name and foreign_key attributes here!
                    String belongsToOptions = "(=>class_name|join_table|foreign_key|association_foreign_key|conditions|order|uniq:bool|finder_sql|delete_sql|insert_sql|extend|include|group|limit|offset|select)";
                    return "options" + belongsToOptions + "),association_id(" + TABLENAME + ")";
                }
            } else if (clz.equals("ActiveRecord::Aggregations::ClassMethods")) {
                if (signature.startsWith("composed_of(")) {
                    return "options(=>class_name|mapping|allow_nil:bool)";
                }
            } else if (clz.equals("ActiveRecord::Acts::List::ClassMethods")) {
                if (signature.startsWith("acts_as_list(")) {
                    return "options(=>column|scope)";
                }
            } else if (clz.equals("ActiveRecord::Acts::Tree::ClassMethods")) {
                if (signature.startsWith("acts_as_tree(")) {
                    return "options(=>foreign_key|order|counter_cache)";
                }
            } else if (clz.equals("ActiveRecord::Acts::NestedSet::ClassMethods")) {
                if (signature.startsWith("acts_as_nested_set(")) {
                    return "options(=>parent_column|left_column|right_column|scope)";
                }
            } else if (clz.equals("ActiveRecord::Transactions::ClassMethods")) {
                // No methods with options or docs here
            } else if (clz.equals("ActiveRecord::Calculations::ClassMethods")) {
                String COUNT_OPTIONS = "(=>conditions|joins|include|order|group|select|distinct:bool)";
                String CALCULATE_OPTIONS = "(=>conditions|joins|order|group|select|distinct:bool)";
                if (signature.startsWith("calculate(")) {
                    return "options" + CALCULATE_OPTIONS + ",operation(:count|:avg|:min|:max|:sum),column_name" + COLUMNNAME + ")";
                } else if (signature.startsWith("count(")) {
                    // XXX will the "*" match work?
                    return "args" + COUNT_OPTIONS;
                } else if (signature.startsWith("minimum(") ||
                        signature.startsWith("average(") ||
                        signature.startsWith("sum(") ||
                        signature.startsWith("maximum(")) {
                    return "options" + CALCULATE_OPTIONS + ",column_name" + COLUMNNAME + ")";
                }
            } else if (clz.equals("ActiveRecord::Validations::ClassMethods")) {
                String COUNT_OPTIONS = "(=>conditions|joins|include|order|group|select|distinct:bool)";
                String CALCULATE_OPTIONS = "(=>conditions|joins|order|group|select|distinct:bool)";
                if (signature.startsWith("validates_each(")) {
                    return "attrs(=>on:" + VALIDATIONACTIVE + "|allow_nil:bool|if)";
                } else if (signature.startsWith("validates_confirmation_of(")) {
                    return "attr_names(=>on:" + VALIDATIONACTIVE + "|message|if)";
                } else if (signature.startsWith("validates_acceptance_of(")) {
                    return "attr_names(=>on:" + VALIDATIONACTIVE + "|message|if|accept)";
                } else if (signature.startsWith("validates_presence_of(")) {
                    return "attr_names(=>on:" + VALIDATIONACTIVE + "|message|if)";
                } else if (signature.startsWith("validates_length_of(")) {
                    return "attrs(=>minimum|maximum|is|within|in|allow_nil:bool|too_long|too_short|wrong_length|on:" + VALIDATIONACTIVE + "|message|if)";
                } else if (signature.startsWith("validates_uniqueness_of(")) {
                    return "attr_names(=>message|scope|case_sensitive:bool|allow_nil:bool|if)";
                } else if (signature.startsWith("validates_format_of(")) {
                    return "attr_names(=>on:" + VALIDATIONACTIVE + "|message|if|with)";
                } else if (signature.startsWith("validates_inclusion_of(")) {
                    return "attr_names(=>in|message|allow_nil:bool|if)";
                } else if (signature.startsWith("validates_exclusion_of(")) {
                    return "attr_names(=>in|message|allow_nil:bool|if)";
                } else if (signature.startsWith("validates_associated(")) {
                    return "attr_names(=>on:" + VALIDATIONACTIVE + "|if)";
                } else if (signature.startsWith("validates_numericality_of(")) {
                    return "attr_names(=>on:" + VALIDATIONACTIVE + "|message|if|only_integer:bool|allow_nil:bool)";
                }
            }
            // TimeStamp, AttributeMethods, XmlSerialization, Locking, Reflection, Observing, Callbacks - nothing
            
            
            // ActionController
            if (clz.equals("ActionController::Base")) {
                if (signature.startsWith("url_for(")) {
                    return "options(=>anchor|only_path:bool|controller:controller|action:action|trailing_slash:bool|host|protocol)";
                } else if (signature.startsWith("process_cgi(")) {
                    return "session_options(=>database_manager|session_key|session_id|new_session|session_expires|session_domain|session_secure|session_path)";
                } else if (signature.startsWith("redirect_to(")) {
                    return "options(=>anchor|only_path:bool|controller:controller|action:action|trailing_slash:bool|host|protocol),options(:back|\"http://)";
                } else if (signature.startsWith("render(") ||
                           signature.startsWith("render_to_string(")
                    ) {
                    // layout:bool applies if action is used
                    // locals: applies if action is used
                    // use_full_path: applies if file is used
                    // collection:collection: applies if partial is used
                    // spacer_template applies if partial is used
                    return "options(=>action:action|partial:partial|status|template|file:file|text:string|json|inline|nothing)";
                }
            } else if (clz.equals("ActionController::Layout::ClassMethods")) {
                // Nothing with smart args here
            } else if (clz.equals("ActionController::Streaming")) {
                if (signature.startsWith("send_file(")) {
                    return "options(=>filename|type|disposition|stream|buffer_size|status)";
                } else if (signature.startsWith("send_data(")) {
                    return "options(=>filename|type|disposition|status)";
                }
            } else if (clz.equals("ActionController::Pagination")) {
                if (signature.startsWith("paginate(")) {
                    return "options(=>singular_name|class_name|per_page|conditions|order|order_by|joins|join|include|selected|count)";
                }
                // I'm not sure what the options in count_collection_for_pagination are referring to
            } else if (clz.equals("ActionController::Verification::ClassMethods")) {
                if (signature.startsWith("verify(")) {
                    return "options(=>params|session|flash|method|post:" + SUBMITMETHOD + "|xhr:bool|add_flash:hash|add_headers:hash|redirect_to|render|only:bool|except:bool)";
                }
            } else if (clz.equals("ActionController::SessionManagement::ClassMethods")) {
                if (signature.startsWith("session_store=(")) {
                    return "store(:active_record_store|:drb_store|:mem_cache_store|:memory_store)";
                } else if (signature.startsWith("session(")) {
                    return "args(=>on:bool|off:bool|only|except|database_manager|session_key|session_id|new_session|session_expires|session_domain|session_secure|session_path)";
                }
            } else if (clz.equals("ActionController::MimeResponds::InstanceMethods")) {
                // Nothing for me to do here
                //if (signature.startsWith("respond_to(")) {
                //}
            } else if (clz.equals("ActionController::Scaffolding::ClassMethods")) {
                if (signature.startsWith("scaffold(")) {
                    return "model_id(" + MODELNAME + "),options(=>suffix:bool)";
                }
            } else if (clz.equals("ActionController::Filters::ClassMethods")) {
                // no relevant methods
                //if (signature.startsWith("scaffold(")) {
                //}
            }
            // Nothing for Layout, Dependencies, Benchmarking, Flash, Macros, AutoComplete, Caching, Cookies
            
            // ActionView
            if (clz.equals("ActionView::Helpers::FormHelper")) {
                if (signature.startsWith("form_for(")) {
                    return "object_name(" + MODELNAME + "),options(=>url:hash|html:hash|builder)";
                } else if (signature.startsWith("fields_for(")) {
                    return "object_name(" + MODELNAME + "),options(=>url:hash)";
                } else {
                    String[] mtds = {"text_field","password_field","hidden_field","file_field","text_area","check_box","radio_button" };
                    for (String mtd : mtds) {
                        if (signature.startsWith(mtd)) {
                            return "object_name(" + MODELNAME + ")";
                        }
                    }
                }
            } else if (clz.equals("ActionView::Helpers::PrototypeHelper")) {
                if (signature.startsWith("observe_field") ||
                        signature.startsWith("observe_form")) {
                    return "options(=>url:hash|function|frequency|update|with|on)";
                } else if (signature.startsWith("link_to_remote") ||
                        signature.startsWith("remote_function") ||
                        signature.startsWith("submit_to_remote") ||
                        signature.startsWith("form_remote_tag")) {
                    return "options(=>url:hash|update)";
                }
            } else if (clz.equals("ActionView::Helpers::FormTagHelper")) {
                if (signature.startsWith("form_tag(")) {
                    return "options(=>anchor|only_path:bool|controller:controller|action:action|trailing_slash:bool|host|protocol)";
                } else if (signature.startsWith("select_tag(")) {
                    return "options(=>multiple:bool)";
                } else if (signature.startsWith("text_area_tag(")) {
                    return "options(=>size)";
                } else if (signature.startsWith("text_field_tag(") ||
                           signature.startsWith("password_field_tag(") ||
                           signature.startsWith("hidden_field_tag(")) {
                    return "options(=>disabled:bool|size|maxlength)";
                }
            } else if (clz.equals("ActionView::Helpers::NumberHelper")) {
                
                if (signature.startsWith("number_to_phone(")) {
                    return "options(=>area_code:bool|delimiter|extension|country_code)";
                } else if (signature.startsWith("number_to_currency(")) {
                    return "options(=>precision|unit|separator|delimiter)";
                } else if (signature.startsWith("number_to_percentage(")) {
                    return "options(=>precision|separator)";
                } else if (signature.startsWith("number_with_delimiter(")) {
                    return "options(=>delimiter|separator)";
                }
            } else if (clz.equals("ActionView::Helpers::DateHelper")) {
                if (signature.startsWith("date_select(")) {
                    return "options(=>discard_year:bool|discard_month:bool|discard_day:bool|order|disabled:bool)";
                } else if (signature.startsWith("time_select(")) {
                    return "options(=>include_seconds:bool)";
                } // XXX Not sure about the rest here
            } else if (clz.equals("ActionView::Helpers::AssetTagHelper")) {
                if (signature.startsWith("auto_discovery_link_tag(")) {
                    return "type(:rss|:atom),tag_options(=>rel|type|title),url_options(=>anchor|only_path:bool|controller:controller|action:action|trailing_slash:bool|host|protocol)";
                } else if (signature.startsWith("image_tag(")) {
                    return "options(=>alt|size)";
                }
            } else if (clz.equals("ActionView::Helpers::UrlHelper")) {
                if (signature.startsWith("url_for(")) {
                    return "options(=>escape:bool|anchor|only_path:bool|controller:controller|action:action|trailing_slash:bool|host|protocol)";
                } else if (signature.startsWith("link_to(")) {
                    return "options(=>anchor|only_path:bool|controller:controller|action:action|trailing_slash:bool|host|protocol),html_options(=>confirm:string|popup:bool|method" + HTML_HASH_OPTIONS +")";
                } else if (signature.startsWith("button_to(")) {
                    return "options(=>anchor|only_path:bool|controller:controller|action:action|trailing_slash:bool|host|protocol),html_options(=>confirm:string|popup:bool|method|disabled:bool" + HTML_HASH_OPTIONS +")";
                } else if (signature.startsWith("mail_to(")) {
                    return "html_options(=>encode|replace_at|replace_dot|subject|body|cc|bcc" + HTML_HASH_OPTIONS +")";
                } // XXX missing some methods here - link_to_if etc.
            } else if (clz.equals("ActionView::Helpers::BenchmarkHelper")) {
                if (signature.startsWith("benchmark(")) {
                    return "level(:debug|:info|:warn|:error)";
                }
            } else if (clz.equals("ActionView::Helpers::PaginationHelper")) {
                if (signature.startsWith("pagination_links(")) {
                    return "options(name|window_size|always_show_anchors:bool|link_to_current_page:bool|params),html_options(=>confirm:string|popup:bool|method" + HTML_HASH_OPTIONS +")";
                }
            } else if (clz.equals("ActionView::Helpers::ActiveRecordHelper")) {
                if (signature.startsWith("form(")) {
                    return "options(action:action)";
                } else if (signature.startsWith("error_messages_for(")) {
                    // XXX note sig had "*params" instead of a hash - make
                    // sure my name comparison is okay with that
                    return "params(=>header_tag|id|class|object_name)";
                }
            }
            // TODO: TagHelper  -- if you generate a tag I can do the
            // attributes conditionally based on the tag you're building
            
            // TODO FormOptionsHelper -- not sure what to do there
        }
        return "";
    }
}
