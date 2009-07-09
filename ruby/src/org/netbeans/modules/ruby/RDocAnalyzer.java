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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private static final Pattern PARAM_HINT_ARG_PATTERN = Pattern.compile(PARAM_HINT_ARG + "\\s*(\\S+)\\s*=>\\s*(.+)\\s*");

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
            if (!inspect(line)) { // NOI18N
                break; // ignore other then the header
            }
            rda.parseTypeFromLine(line);
        }
        return rda.type;
    }

    private static boolean inspect(String line) {
       // ignore other then the header and type assertions
       return line.startsWith("#  ") //NOI18N
               || line.startsWith(PARAM_HINT_ARG) 
               || line.startsWith(PARAM_HINT_RETURN);
    }

    private void parseTypeFromLine(String line) {
        // type assertions first
        if (addTypes(returnTypeFromTypeAssertion(line))) {
            return;
        }
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
        addTypes(analyzeRawCommentType(rawCommentTypes));
        if (type.isKnown()) {
            LOGGER.log(Level.FINE, "Could not resolve type for {0}", line);
        }
    }

    private boolean addTypes(List<String> types) {
        if (types.isEmpty()) {
            return false;
        }
        for (String each : types) {
            type.add(each);
        }
        return true;
    }

    private static List<String> analyzeRawCommentType(String rawCommentTypes) {
        List<String> result = new ArrayList<String>();
        String[] rawCommentTypes2 = rawCommentTypes.split(" or "); // NOI18N
        for (String rawCommentType : rawCommentTypes2) {
            // first try whether we have an array or a hash
            for (TypeCommentAnalyzer analyzer : RAW_TYPE_COMMENT_ANALYZERS) {
                String realType = analyzer.getType(rawCommentType);
                if (realType != null) {
                    // return, the type was already recognized as hash/array/etc (doesn't
                    // make sense to split these with ','
                    result.add(realType);
                    return result;
                }
            }
            String[] commentTypes = rawCommentType.split(","); // NOI18N
            for (String commentType : commentTypes) {
                commentType = commentType.trim();
                if (commentType.length() > 0) {
                    String type = addRealTypeForCommentType(commentType);
                    if (type != null) {
                        result.add(type);
                    }
                }
            }
        }
        return result;
    }

    private static String addRealTypeForCommentType(final String commentType) {
        for (TypeCommentAnalyzer analyzer : TYPE_COMMENT_ANALYZERS) {
            String realType = analyzer.getType(commentType);
            if (realType != null) {
                return realType;
            }
        }
        return null;
    }

    static List<String> returnTypeFromTypeAssertion(String line) {
        int start = line.indexOf(PARAM_HINT_RETURN);
        if (start != -1) {
            String rawCommentTypes = line.substring(start + PARAM_HINT_RETURN.length()).trim();
            if (rawCommentTypes.length() == 0) {
                return Collections.emptyList();
            }
            return analyzeRawCommentType(rawCommentTypes);
        }
        return Collections.emptyList();
    }

    static TypeForSymbol paramTypesFromTypeAssertion(String line) {
        Matcher matcher = PARAM_HINT_ARG_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return null;
        }
        return new TypeForSymbol(matcher.group(1), analyzeRawCommentType(matcher.group(2).trim()));
    }

    /** Look at type assertions in the document and initialize name context. */
    static void collectTypeAssertions(final ContextKnowledge knowledge) {
        Node root = knowledge.getRoot();
        if (root instanceof MethodDefNode) {
            // Look for parameter hints
            List<String> rdoc = AstUtilities.gatherDocumentation(knowledge.getParserResult().getSnapshot(), root);

            if ((rdoc != null) && (rdoc.size() > 0)) {
                for (String line : rdoc) {
                    List<String> returnTypes = returnTypeFromTypeAssertion(line);
                    if (!returnTypes.isEmpty()) {
                        knowledge.setType(root, new RubyType(returnTypes));
                    } else {
                        TypeForSymbol tfs = paramTypesFromTypeAssertion(line);
                        if (tfs != null) {
                            knowledge.maybePutTypeForSymbol(tfs.getName(), new RubyType(tfs.getTypes()), true);
                        }
                    }
                }
            }
        }
    }
    
    // package private for unit tests
    static List<String> getStandardNameVariants(String baseName) {
        List<String> result = new ArrayList<String>(9);
        result.add(baseName);
        // ideally should analyze baseName and add just the appropriate article...
        result.add("a_" + baseName);
        result.add("an_" + baseName);
        String underlined = RubyUtils.camelToUnderlinedName(baseName);
        result.add(underlined);
        result.add("a_" + underlined);
        result.add("an_" + underlined);
        String camelCase = RubyUtils.underlinedNameToCamel(baseName);
        result.add(camelCase);
        result.add("a" + camelCase);
        result.add("an" + camelCase);
        return result;
    }

    private static String validName(String type) {
        if (RubyUtils.isValidConstantName(type)) {
            return type;
        }
        return null;
    }

    static String resolveType(String typeInComment) {
        if ("".equals(typeInComment.trim()) || !Character.isLetter(typeInComment.charAt(0))) {
            return null;
        }
        if (typeInComment.startsWith("an_")) {
            return validName(RubyUtils.underlinedNameToCamel(typeInComment.substring(3)));
        }
        if (typeInComment.startsWith("a_")) {
            return validName(RubyUtils.underlinedNameToCamel(typeInComment.substring(2)));
        }
        if (typeInComment.startsWith("an") 
                && typeInComment.length() > 2 
                && Character.isUpperCase(typeInComment.charAt(2))) {
            return validName(typeInComment.substring(2));
        }
        if (typeInComment.startsWith("a") 
                && typeInComment.length() > 1 
                && Character.isUpperCase(typeInComment.charAt(1))) {
            return validName(typeInComment.substring(1));
        }
        if (Character.isUpperCase(typeInComment.charAt(0))) {
            return validName(typeInComment);
        }
        return validName(RubyUtils.underlinedNameToCamel(typeInComment));
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
            putType("abs_file_name", "String"); // NOI18N
            putType("class", "Class"); // NOI18N
            putType("super_class", "Class"); // NOI18N
            putType("klass", "Class"); // NOI18N
            putType("dir", "Dir"); // NOI18N
            putType("dir_name", "String"); // NOI18N
            putType("fixnum", "Fixnum"); // NOI18N
            putType("hash", "Hash"); // NOI18N
            putType("hsh", "Hash"); // NOI18N
            putType("array", "Array"); // NOI18N
            putType("sub_array", "Array"); // NOI18N
            putType("ary", "Array"); // NOI18N
            putType("object", "Object"); // NOI18N
            putType("enum", "Enumeration"); // NOI18N
            putType("enumerat", "Enumeration"); // NOI18N
            putType("enumerator", "Enumeration"); // NOI18N
            putType("enumeration", "Enumeration"); // NOI18N
            putType("io", "IO"); // NOI18N
            putType("ios", "IO"); // NOI18N
            putType("proc", "Proc"); // NOI18N
            putType("str", "String"); // NOI18N
            putType("base_name", "String"); // NOI18N
            putType("big", "Bignum"); // NOI18N
            putType("bignum", "Bignum"); // NOI18N
            putType("boolean", "TrueClass"); // NOI18N
            putType("bool", "TrueClass"); // NOI18N
            putType("buffer", "String"); // NOI18N
            putType("binding", "Binding"); // NOI18N
            putType("exception", "Exception");
            putType("no_method_error", "NoMethodError");
            COMMENT_TYPE_TO_REAL_TYPE.put("false", "FalseClass"); // NOI18N
            putType("file", "File");
            putType("fixnum", "Fixnum");
            putType("float", "Float");
            putType("flt", "Float");
            COMMENT_TYPE_TO_REAL_TYPE.put(":foo", "Symbol"); // NOI18N
            putType("integer", "Integer");
            putType("int", "Integer");
            putType("matchdata", "MatchData");
            putType("method", "Method");
            putType("mod", "Module");
            putType("name_error", "NameError"); // NOI18N
            putType("new_method", "UnboundMethod"); // NOI18N
            putType("new_regexp", "Regexp"); // NOI18N
            putType("new_str", "String"); // NOI18N
            putType("new_time", "Time"); // NOI18N
            putType("nil", "NilClass"); // NOI18N
            putType("number", "Numeric"); // NOI18N
            putType("numeric", "Numeric"); // NOI18N
            putType("numeric_result", "Numeric"); // NOI18N
            putType("num", "Numeric"); // NOI18N
            putType("obj", "Object"); // NOI18N
            putType("other_big", "Bignum"); // NOI18N
            putType("outbuf", "String"); // NOI18N
            putType("prc", "Proc"); // NOI18N
            putType("range", "Range"); // NOI18N
            putType("regexp", "Regexp"); // NOI18N
            putType("rng", "Range"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("stat", "File::Stat"); // NOI18N
            putType("string", "String"); // NOI18N
            putType("str", "String"); // NOI18N
            putType("system_exit", "SystemExit"); // NOI18N
            putType("struct", "Struct"); // NOI18N
            putType("struct_tms", "Struct::Tms"); // NOI18N
            putType("symbol", "Symbol"); // NOI18N
            putType("sym", "Symbol"); // NOI18N
            putType("thgrp", "ThreadGroup"); // NOI18N
            putType("thread", "Thread"); // NOI18N
            putType("thr", "Thread"); // NOI18N
            putType("time", "Time"); // NOI18N
            // TODO: should return both Class and Module
            COMMENT_TYPE_TO_REAL_TYPE.put("class_or_module", "Class"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("e", "Enumeration"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("old_seed", "Numeric"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("old_seed", "Numeric"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("true", "TrueClass"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("false", "FalseClass"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("path", "String"); // NOI18N
            COMMENT_TYPE_TO_REAL_TYPE.put("$_", "String"); // NOI18N
            putType("unbound_method", "UnboundMethod"); // NOI18N
        }

        private static void putType(String baseName, String type) {
            for (String each : getStandardNameVariants(baseName)) {
                COMMENT_TYPE_TO_REAL_TYPE.put(each, type);
            }
        }

        @Override
        protected String doGetType(String comment) {
            return COMMENT_TYPE_TO_REAL_TYPE.get(comment);
        }
        
    }
    
    private static final class CustomClassNameAnalyzer extends TypeCommentAnalyzer {

        /**
         * Exceptions for which we don't want to create a type.
         */
        // TODO: create an own type for self that the method TI infrastructure could 
        // use and return the receiver in these cases.
        private static final String[] EXCEPTIONS = {"Self", "Key", "Value", "Detail", "Result"};

        protected String doGetType(String typeInComment) {
            String result = resolveType(typeInComment);
            for (String each : EXCEPTIONS) {
                if (each.equals(result)) {
                    return null;
                }
            }
            return result;
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

    static class TypeForSymbol {
        private final String name;
        private final List<String> types;

        public TypeForSymbol(String name, List<String> types) {
            this.name = name;
            this.types = types;
        }

        public String getName() {
            return name;
        }

        public List<String> getTypes() {
            return types;
        }
    }

}
