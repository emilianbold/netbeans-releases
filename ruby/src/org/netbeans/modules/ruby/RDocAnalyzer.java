/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jrubyparser.ast.MethodDefNode;
import org.jrubyparser.ast.Node;
import org.netbeans.modules.parsing.api.Source;

/**
 * Currently serves (mainly) for analyzing RDoc of generated stubs of Ruby core
 * methods which are written in native language (Java, C).
 * 
 * <p>
 * Might be just temporary solution until we utilize different approach, like
 * e.g. <em>base_types.rb</em> described in
 * <a href="http://www.cs.umd.edu/~jfoster/ruby.pdf">Static Type Inference for Ruby</a>
 * paper.
 */
final class RDocAnalyzer {

    private static final Logger LOGGER = Logger.getLogger(RDocAnalyzer.class.getName());

    static final String PARAM_HINT_ARG = "#:arg:"; // NOI18N
    static final String PARAM_HINT_RETURN = "#:return:=>"; // NOI18N

    private static final List<TypeCommentAnalyzer> RAW_TYPE_COMMENT_ANALYZERS = initRawTypeCommentAnalyzers();
    private static final List<TypeCommentAnalyzer> TYPE_COMMENT_ANALYZERS = initTypeCommentAnalyzers();

    private final RubyType type;

    private RDocAnalyzer() {
        this.type = new RubyType();
    }

    private static List<TypeCommentAnalyzer> initRawTypeCommentAnalyzers() {
        List<TypeCommentAnalyzer> result = new ArrayList<TypeCommentAnalyzer>();
        result.add(new HashAnalyzer());
        result.add(new ArrayAnalyzer());
        return result;
    }

    private static List<TypeCommentAnalyzer> initTypeCommentAnalyzers() {
        List<TypeCommentAnalyzer> result = new ArrayList<TypeCommentAnalyzer>();
        result.add(new ClassNameAnalyzer());
        result.add(new CustomClassNameAnalyzer());
        result.add(new NumericAnalyzer());
        result.add(new TrueFalseAnalyzer());
        result.add(new StringAnalyzer());
        return result;
    }

    static RubyType collectTypesFromComment(final List<? extends String> comment) {
        RDocAnalyzer rda = new RDocAnalyzer();
        for (String line : comment) {
            line = line.trim();
            if (!line.startsWith("#  ")) { // NOI18N
                break; // ignore other then the header
            }
            rda.parseTypeFromLine(line);
        }
        return rda.type;
    }

    private void parseTypeFromLine(String line) {
        // try '#=>' first since e.g. rdocs for hash use that to
        // indicate return values
        int typeIndex = line.indexOf(" #=> "); // NOI18N
        if (typeIndex == -1) {
            typeIndex = line.indexOf(" -> "); // NOI18N
        }
        if (typeIndex == -1) {
            typeIndex = line.indexOf(" => "); // NOI18N
        }
        if (typeIndex == -1) {
            return;
        }
        String rawCommentTypes = line.substring(typeIndex + 4).trim();
        if (rawCommentTypes.length() == 0) {
            return;
        }
        boolean success = false;
        String[] rawCommentTypes2 = rawCommentTypes.split(" or "); // NOI18N
        for (String rawCommentType : rawCommentTypes2) {
            // first try whether we have an array or a hash
            for (TypeCommentAnalyzer analyzer : RAW_TYPE_COMMENT_ANALYZERS) {
                String realType = analyzer.getType(rawCommentType);
                if (realType != null) {
                    type.add(realType);
                    success = true;
                    // return, the type was already recognized as hash/array/etc (doesn't
                    // make sense to split these with ','
                    return;
                }
            }
            String[] commentTypes = rawCommentType.split(","); // NOI18N
            for (String commentType : commentTypes) {
                commentType = commentType.trim();
                if (commentType.length() > 0) {
                    success = addRealTypeForCommentType(commentType);
                }
            }
            if (!success && LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("Could not resolve type for " + line);
            }
        }
    }

    private boolean addRealTypeForCommentType(final String commentType) {
        boolean result = false;
        for (TypeCommentAnalyzer analyzer : TYPE_COMMENT_ANALYZERS) {
            String realType = analyzer.getType(commentType);
            if (realType != null) {
                type.add(realType);
                result = true;
            }
        }
        return result;

        /* TODO: uncomment else block and run the RDocAnalyzerTest, try to
         handle the unknown types like literals:
           * 1, -1, ...
           * "str"
           * [1, 2, 3]
         as well */
//         else {
//            System.err.println("Unknown real Ruby type for comment type: " + commentType);
//        }
    }

    /** Look at type assertions in the document and initialize name context. */
    static void collectTypeAssertions(final ContextKnowledge knowledge) {
        if (!knowledge.hasDocument()) {
            return;
        }
        Node root = knowledge.getRoot();
        if (root instanceof MethodDefNode) {
            // Look for parameter hints
            Source source = Source.create(knowledge.getDocument());
            List<String> rdoc = AstUtilities.gatherDocumentation(source.createSnapshot(), root);

            if ((rdoc != null) && (rdoc.size() > 0)) {
                for (String line : rdoc) {
                    if (line.startsWith(PARAM_HINT_ARG)) {
                        StringBuilder sb = new StringBuilder();
                        String name = null;
                        int max = line.length();
                        int i = PARAM_HINT_ARG.length();

                        for (; i < max; i++) {
                            char c = line.charAt(i);

                            if (c == ' ') {
                                continue;
                            } else if (c == '=') {
                                break;
                            } else {
                                sb.append(c);
                            }
                        }

                        if ((i == max) || (line.charAt(i) != '=')) {
                            continue;
                        }

                        i++;

                        if (sb.length() > 0) {
                            name = sb.toString();
                            sb.setLength(0);
                        } else {
                            continue;
                        }

                        if ((i == max) || (line.charAt(i) != '>')) {
                            continue;
                        }

                        i++;

                        for (; i < max; i++) {
                            char c = line.charAt(i);

                            if (c == ' ') {
                                continue;
                            }

                            if (!Character.isJavaIdentifierPart(c)) {
                                break;
                            } else {
                                sb.append(c);
                            }
                        }

                        if (sb.length() > 0) {
                            String type = sb.toString();
                            knowledge.maybePutTypeForSymbol(name, type, true);
                        }
                    }

                    //if (line.startsWith(":return:=>")) {
                    //    // I don't really need the return type yet
                    //}
                }
            }
        }
    }
    
    private static abstract class TypeCommentAnalyzer {
        
        final String getType(String comment) {
            String result = doGetType(comment);
            if (result != null && LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("Resolved type [" + result + "] for comment: [" + comment + "]. " +
                        "Analyzer: [" + getClass().getSimpleName() + "].");
            } 
            return result;
        }

        protected abstract String doGetType(String comment);
    }

    private static final class ClassNameAnalyzer extends TypeCommentAnalyzer {

        private static final Map<String, String> COMMENT_TYPE_TO_REAL_TYPE = new HashMap<String, String>();

        static {
            COMMENT_TYPE_TO_REAL_TYPE.put("!obj", "FalseClass"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("a_class", "Class"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("aDir", "Dir"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("aFixnum", "Fixnum"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("a_hash", "Hash"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("aHash", "Hash"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("a_klass", "Class"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("an_array", "Array"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("anArray", "Array"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("anEnumerat", "Enumeration"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("anEnumerator", "Enumeration"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("anIO", "IO"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("a_proc", "Proc"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("array", "Array"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("aStructTms", "Struct::Tms"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("a_str", "String"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("big", "Bignum"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("bignum", "Bignum"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("binding", "Binding"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("class", "Class"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("dir", "Dir"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("exception", "Exception"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("false", "FalseClass"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("file", "File"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("fixnum", "Fixnum"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("float", "Float"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("flt", "Float"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put(":foo", "Symbol"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("hash", "Hash"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("hsh", "Hash"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("integer", "Integer"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("int", "Integer"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("io", "IO"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("ios", "IO"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("matchdata", "MatchData"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("method", "Method"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("mod", "Module"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("name_error", "NameError"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("new_method", "UnboundMethod"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("new_regexp", "Regexp"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("new_str", "String"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("new_time", "Time"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("nil", "NilClass"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("number", "Numeric"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("numeric", "Numeric"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("numeric_result", "Numeric"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("num", "Numeric"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("obj", "Object"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("object", "Object"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("other_big", "Bignum"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("prc", "Proc"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("proc", "Proc"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("range", "Range"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("regexp", "Regexp"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("rng", "Range"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("stat", "File::Stat"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("string", "String"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("str", "String"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("struct", "Struct"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("struct_tms", "Struct::Tms"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("symbol", "Symbol"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("sym", "Symbol"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("thgrp", "ThreadGroup"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("thread", "Thread"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("thr", "Thread"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("time", "Time"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("true", "TrueClass"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("unbound_method", "UnboundMethod"); // NOI18N
        }

        @Override
        protected String doGetType(String comment) {
            return COMMENT_TYPE_TO_REAL_TYPE.get(comment);
        }
        
    }
    
    private static final class CustomClassNameAnalyzer extends TypeCommentAnalyzer {

        protected String doGetType(String typeInComment) {
            return typeInComment.length() > 0 && Character.isUpperCase(typeInComment.charAt(0))
                    ? typeInComment
                    : null;
        }
    }


    private static final class NumericAnalyzer extends TypeCommentAnalyzer {

        protected String doGetType(String typeInComment) {
            if (isFixnum(typeInComment)) {
                return "Fixnum";
            }
            if (isFloat(typeInComment)) {
                return "Float";
            }
            return null;
        }
        
        private boolean isFixnum(String str) {
            try {
                Integer.parseInt(str);
                return true;
            } catch (NumberFormatException nfe) {
                // check for +, otherwise e.g. +5 is not recognized correctly
                if (str.startsWith("+") && str.length() > 1) {
                    return isFixnum(str.substring(1));
                }
                return false;
            }
        }

        private boolean isFloat(String str) {
            try {
                Float.parseFloat(str);
                return true;
            } catch (NumberFormatException nfe) {
                return false;
            }
        }
    }

    private static final class StringAnalyzer extends TypeCommentAnalyzer {

        protected String doGetType(String typeInComment) {
            return typeInComment.startsWith("\"") && typeInComment.endsWith("\"")
                    ? "String"
                    : null;
        }
    }

    private static final class TrueFalseAnalyzer extends TypeCommentAnalyzer {

        private static final String[] TRUE_TYPES = {"true"}; //NOI18N
        private static final String[] FALSE_TYPES = {"false"}; //NOI18N

        protected String doGetType(String typeInComment) {
            for (String type : TRUE_TYPES) {
                if (type.equalsIgnoreCase(typeInComment)) {
                    return "TrueClass"; //NOI18N
                }
            }
            for (String type : FALSE_TYPES) {
                if (type.equalsIgnoreCase(typeInComment)) {
                    return "FalseClass"; //NOI18N
                }
            }
            return null;
        }
    }

    private static final class ArrayAnalyzer extends TypeCommentAnalyzer {

        protected String doGetType(String typeInComment) {
            String trimmed = typeInComment.trim();
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                return "Array";
            }
            return null;

        }
    }

    private static final class HashAnalyzer extends TypeCommentAnalyzer {

        protected String doGetType(String typeInComment) {
            String trimmed = typeInComment.trim();
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                return "Hash";
            }
            return null;

        }
    }

}
