/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.ruby;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jrubyparser.ast.ArrayNode;
import org.jrubyparser.ast.DotNode;
import org.jrubyparser.ast.ForNode;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.IfNode;
import org.jrubyparser.ast.ListNode;
import org.jrubyparser.ast.LocalAsgnNode;
import org.jrubyparser.ast.MultipleAsgnNode;
import org.jrubyparser.ast.NilImplicitNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.ast.ToAryNode;
import org.openide.filesystems.FileObject;

/**
 * Perform type analysis on a given AST tree, attempting to provide a type
 * associated with each variable, field etc.
 *
 * @todo Track boolean types for simple operators; e.g.
 *    cc_no_width = letter == '[' && !width
 *    etc.  The operators here let me conclude cc_no_width is of type boolean!
 * @todo Handle find* method in Rails to indicate object types
 * @todo A reference to "foo." in a method is an alias to "@foo" if the method
 *    has not been defined explicitly. Attributes are especially clear, but an
 *    index lookup from type analyzer may be too expensive.
 * @todo The structure analyzer already tracks field declarations for the current class;
 *    I should use that to track down the types
 * @todo Use some statistical results to improve this; .to_s => String, .to_f => float,
 *   etc.
 * @todo In     create_table :posts do |t|
 *   I need to realize the type of "t" is ActiveRecord::ConnectionAdapters::TableDefinition from schema_definitions.rb
 * @todo Methods whose names end with "?" probably return TrueClass or FalseClass
 *   so I can handle those expressions without actual return value lookup
 * @todo Possible conventions - http://www.alexandria.ucsb.edu/~gjanee/archive/2005/python-type-checking.html
 * @todo http://www.codecommit.com/blog/ruby/adding-type-checking-to-ruby
 *
 * @author Tor Norbye
 */
final class RubyTypeAnalyzer {

    private final ContextKnowledge knowledge;
    private boolean analyzed;
    private boolean targetReached;

    /**
     * The names of the methods that have been analyzed. Needed to keep track on
     * when types of instance/class vars should be overridden - we don't want to
     * override the types when an inst var is assigned in different methods, e.g.:
     * <pre>
     *  def foo
     *   &#64;baz = 1
     *  end
     *  def bar
     *   &#64;baz = "str"
     *  end
     *  def whats_my_return_type
     *   &#64;baz
     *  end
     * </pre>
     *
     * In the above <code>@baz</code> should be inferred both as <code>Fixnum</code>
     * and <code>String</code>.
     */
    private final Set<String> analyzedMethods = new HashSet<String>();

    private final RubyTypeInferencer typeInferencer;
    /**
     * Creates a new instance of RubyTypeAnalyzer for a given position. The
     * {@link #inferType} method will do the rest.
     */
    RubyTypeAnalyzer(final ContextKnowledge knowledge, RubyTypeInferencer typeInferencer) {
        this.knowledge = knowledge;
        this.typeInferencer = typeInferencer;
    }

    void analyze() {
        if (!analyzed) {
            knowledge.setAnalyzed(true);
            RubyTypeAnalyzer.initFileTypeVars(knowledge);
            RDocAnalyzer.collectTypeAssertions(knowledge);

            analyze(knowledge.getRoot(), knowledge.getTypesForSymbols(), true, null);
            analyzed = true;
        }
    }

    /**
     * Collects the variables initialized in the given <code>multipleAsgnNode</code>.
     * @param multipleAsgnNode
     * @param typeInferencer
     * 
     * @return a map containing the variable nodes and types of the variables in the given <code>multipleAsgnNode</code>.
     */
    static void collectMultipleAsgnVars(MultipleAsgnNode multipleAsgnNode, RubyTypeInferencer typeInferencer, Map<Node, RubyType> result) {
        ListNode head = multipleAsgnNode.getHeadNode();
        Node value = multipleAsgnNode.getValueNode();
        if (head == null || value == null) {
            return;
        }
        // special case
        if (value.getNodeType() == NodeType.TOARYNODE) {
            value = ((ToAryNode) value).getValue();
        }
        if (value.childNodes().size() != head.childNodes().size()) {
            return;
        }
        for (int i = 0; i < head.childNodes().size(); i++) {
            Node var = head.childNodes().get(i);
            collectTypes(var, value.childNodes().get(i), typeInferencer, result);
        }
    }

    private static void collectTypes(Node head, Node value, RubyTypeInferencer typeInferencer, Map<Node, RubyType> result) {
        if (head == null || value == null) {
            return;
        }
        if (value.getNodeType() == NodeType.TOARYNODE) {
            value = ((ToAryNode) value).getValue();
        }
        // nested multiple asgn
        // if we have a multiple asgn of form (a,(b,c))=[1,[2,3]] the nested multipleAsgnNode don't
        // contain the correct value node, we need to get the value from the "parent" multipleAsgnNode
        if (head.getNodeType() == NodeType.MULTIPLEASGNNODE) {
            MultipleAsgnNode multipleAsgnNode = (MultipleAsgnNode) head;
            ListNode headNode = multipleAsgnNode.getHeadNode();
            if (headNode != null && headNode.childNodes().size() == value.childNodes().size()) {
                for (int i = 0; i < multipleAsgnNode.getHeadNode().childNodes().size(); i++) {
                    Node var = multipleAsgnNode.getHeadNode().childNodes().get(i);
                    collectTypes(var, value.childNodes().get(i), typeInferencer, result);
                }
            }
        } else if (head.getNodeType() == NodeType.ARRAYNODE && value.getNodeType() == NodeType.ARRAYNODE) {
            ArrayNode headArray = (ArrayNode) head;
            ArrayNode valueArray = (ArrayNode) value;
            if (headArray.size() == valueArray.size()) {
                for (int i = 0; i < headArray.size(); i++) {
                    collectTypes(headArray.get(i), valueArray.get(i), typeInferencer, result);
                }
            }
        } else {
            result.put(head, typeInferencer.inferType(value));
        }
    }

    private static String getCurrentMethod(Node node, String currentMethod) {

        if (node.getNodeType() == NodeType.DEFNNODE || node.getNodeType() == NodeType.DEFSNODE) {
            return AstUtilities.getName(node);
        }

        if (node.getNodeType() == NodeType.MODULENODE
                || node.getNodeType() == NodeType.CLASSNODE
                || node.getNodeType() == NodeType.SCLASSNODE) {
            return  "";
        }
        return currentMethod;
    }
    /**
     * Analyze the given code block down to the given offset. The {@link
     * #inferType} method can then be used to read out the symbol type if any at
     * that point.
     *
     * @param currentMethod the method we're currently analyzing (may be null).
     */
    private void analyze(
            final Node node,
            final Map<String, RubyType> typesForSymbols,
            final boolean override, String currentMethod) {

        // the method in which we are currently; helps in optimizing performance
        currentMethod = getCurrentMethod(node, currentMethod);

        // Avoid including definitions appearing later in the context than the
        // caret. (This only works for local variable analysis; for fields it
        // could be complicated by code earlier than the caret calling code
        // later than the caret which initializes the field...
        if (node == knowledge.getTarget()) {
            targetReached = true;
        }

        if (targetReached && node.getPosition().getStartOffset() > knowledge.getAstOffset()) {
            return;
        }

        // Algorithm: walk AST and look for assignments and such.
        // Attempt to compute the type of each expression and
        switch (node.getNodeType()) {
            case MULTIPLEASGNNODE: {
                MultipleAsgnNode multipleAsgnNode = (MultipleAsgnNode) node;
                Map<Node, RubyType> vars = new HashMap<Node, RubyType>();
                collectMultipleAsgnVars(multipleAsgnNode, typeInferencer, vars);
                for (Node each : vars.keySet()) {
                    if (each instanceof INameNode) {
                        String name = AstUtilities.getName(each);
                        maybePutTypeForSymbol(typesForSymbols, name, vars.get(each), override, currentMethod);
                    }
                }
                return;
            }
            case LOCALASGNNODE: {
                String symbol = RubyTypeInferencer.getLocalVarPath(knowledge.getRoot(), node, currentMethod);
                LocalAsgnNode localAsgnNode = (LocalAsgnNode) node;
                // see if it is a loop var
                if (localAsgnNode.getValueNode() instanceof NilImplicitNode) {
                    AstPath path = new AstPath(knowledge.getRoot(), node);
                    Node leafParent = path.leafParent();
                    if (leafParent instanceof ForNode) {
                        ForNode forNode = (ForNode) leafParent;
                        Node iterNode = forNode.getIterNode();
                        if (iterNode instanceof DotNode) {
                            DotNode dotNode = (DotNode) iterNode;
                            RubyType type = typeInferencer.inferType(dotNode.getBeginNode());
                            maybePutTypeForSymbol(typesForSymbols, symbol, type, override, currentMethod);
                            break;
                        }
                    }
                }
                RubyType type = typeInferencer.inferTypesOfRHS(node, currentMethod);
                maybePutTypeForSymbol(typesForSymbols, symbol, type, override, currentMethod);
                break;
            }
            case CONSTDECLNODE: {
                RubyType type = typeInferencer.inferTypesOfRHS(node, currentMethod);
                String fqn  = AstUtilities.getFqnName(knowledge.getRoot(), node);
                maybePutTypeForSymbol(typesForSymbols, fqn, type, override, currentMethod);
                break;
            }
            case INSTASGNNODE:
            case GLOBALASGNNODE:
            case CLASSVARASGNNODE:
            case CLASSVARDECLNODE:
            case DASGNNODE: {
                RubyType type = typeInferencer.inferTypesOfRHS(node, currentMethod);

                // null element in types set means that we are not able to infer
                // the expression
                String symbol = AstUtilities.getName(node);
                maybePutTypeForSymbol(typesForSymbols, symbol, type, override, currentMethod);
                break;
            }
//        case ITERNODE: {
//            // A block. See if I know the LHS expression types, and if so
//            // I can propagate the type into the block variables.
//        }
//        case CALLNODE: {
//            // Look for known calls whose return types we can guess
//            String name = AstUtilities.getName(node);
//            if (name.startsWith("find")) {
//            }
//        }
        }

        if (node.getNodeType() == NodeType.IFNODE) {
            analyzeIfNode((IfNode) node, typesForSymbols, currentMethod);
        } else {
            for (Node child : node.childNodes()) {
                if (child.isInvisible()) {
                    continue;
                }
                analyze(child, typesForSymbols, override, currentMethod);
            }
        }
    }

    private void analyzeIfNode(final IfNode ifNode, final Map<String, RubyType> typesForSymbols, String currentMethod) {
        Node thenBody = ifNode.getThenBody();
        Map<String, RubyType> ifTypesAccu = new HashMap<String, RubyType>();
        if (thenBody != null) { // might happen with e.g. 'unless'
            analyze(thenBody, ifTypesAccu, true, currentMethod);
        }

        Node elseBody = ifNode.getElseBody();
        Map<String, RubyType> elseTypesAccu = new HashMap<String, RubyType>();
        if (elseBody != null) {
            analyze(elseBody, elseTypesAccu, true, currentMethod);
        }

        Map<String, RubyType> allTypesForSymbols = new HashMap<String, RubyType>();

        // accumulate 'then' and 'else' bodies into one collection so they will
        // not override each other
        for (Map.Entry<String, RubyType> entry : elseTypesAccu.entrySet()) {
            maybePutTypeForSymbol(allTypesForSymbols, entry.getKey(), entry.getValue(), false, currentMethod);
        }
        for (Map.Entry<String, RubyType> entry : ifTypesAccu.entrySet()) {
            maybePutTypeForSymbol(allTypesForSymbols, entry.getKey(), entry.getValue(), false, currentMethod);
        }

        // if there is no 'then' or 'else' body do not override assignment in
        // parent scope(s)
        for (Map.Entry<String, RubyType> entry : allTypesForSymbols.entrySet()) {
            String var = entry.getKey();
            boolean override = ifTypesAccu.containsKey(var) && elseTypesAccu.containsKey(var);
            maybePutTypeForSymbol(typesForSymbols, var, entry.getValue(), override, currentMethod);
        }
    }

    /**
     * This is a bit of a trick. I really know the types of the builtin fields
     * here - @action_name, @assigns, @cookies,. However, this usage is
     * deprecated; people should be using the corresponding accessors methods.
     * Since I don't yet correctly do type analysis of attribute to method
     * mappings (because that would require consulting the index to make sure
     * the given method has not been overridden), I'll just simulate this by
     * pretending that there are -local- variables of the given name
     * corresponding to the return value of these methods.
     */
    private static final String[] RAILS_CONTROLLER_VARS = new String[]{
        "action_name", "String", // NOI18N
        "assigns", "Hash", // NOI18N
        "cookies", "ActionController::CookieJar", // NOI18N
        "flash", "ActionController::Flash::FlashHash", // NOI18N
        "headers", "Hash", // NOI18N
        "params", "Hash", // NOI18N
        "request", "ActionController::CgiRequest", // NOI18N
        "session", "CGI::Session", // NOI18N
        "url", "ActionController::UrlRewriter", // NOI18N
    };

    /**
     * Look at the file name and file extension and see if we know about some
     * known variables. Does not perform real type analysis. Should be
     * deprecated once we have real type analysis for this kind. See also {@link
     * #RAILS_CONTROLLER_VARS}.
     */
    private static void initFileTypeVars(final ContextKnowledge knowledge) {
        FileObject fo = RubyUtils.getFileObject(knowledge.getParserResult());
        if (fo == null) {
            return;
        }

        String ext = fo.getExt();
        if (ext.equals("rb")) {
            String name = fo.getName();
            if (name.endsWith("_controller")) { // NOI18N
                // request, params, etc.
                for (int i = 0; i < RAILS_CONTROLLER_VARS.length; i += 2) {
                    String var = RAILS_CONTROLLER_VARS[i];
                    String type = RAILS_CONTROLLER_VARS[i+1];
                    knowledge.maybePutTypeForSymbol(var, type, true);
                }
            }
            // test files
            //if (name.endsWith("_controller_test")) {
            // For test files in Rails, get testing context (#105043). In particular, actionpack's
            // ActionController::Assertions needs to be pulled in. This happens in action_controller/assertions.rb.
        } else if (ext.equals("rhtml") || ext.equals("erb")) { // NOI18N
            //Insert fields etc. as documented in actionpack's lib/action_view/base.rb (#105095)
            
            // Insert request, params, etc.
            for (int i = 0; i < RAILS_CONTROLLER_VARS.length; i += 2) {
                String var = RAILS_CONTROLLER_VARS[i];
                String type = RAILS_CONTROLLER_VARS[i+1];
                knowledge.maybePutTypeForSymbol(var, type, true);
            }
        } else if (ext.equals("rjs")) { // #105088
            knowledge.maybePutTypeForSymbol("page", "ActionView::Helpers::PrototypeHelper::JavaScriptGenerator::GeneratorMethods", true); // NOI18N
        } else if (ext.equals("builder") || ext.equals("rxml")) { // NOI18N
            knowledge.maybePutTypeForSymbol("xml", "Builder::XmlMarkup", true); // NOI18N
        }
    }

    private void maybePutTypeForSymbol(
            final Map<String, RubyType> typesForSymbols,
            final String symbol,
            final RubyType newType,
            boolean override,
            final String currentMethod) {
        
        RubyType mapType = typesForSymbols.get(symbol);

        if (symbol.startsWith("@")
                && currentMethod != null 
                && !analyzedMethods.contains(currentMethod)) {
            
            analyzedMethods.add(currentMethod);
            override = false;
        }

        if (mapType == null || override) {
            mapType = new RubyType();
            typesForSymbols.put(symbol, mapType);
        }
        mapType.append(newType);
    }

}
