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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jrubyparser.ast.IScopingNode;
import org.jrubyparser.ast.MethodDefNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.ast.ReturnNode;
import org.jrubyparser.ast.SelfNode;
import org.netbeans.modules.ruby.options.TypeInferenceSettings;

public final class RubyTypeInferencer {

    private final ContextKnowledge knowledge;
    private RubyTypeAnalyzer analyzer;

    public static RubyTypeInferencer create(ContextKnowledge knowledge) {
        return new RubyTypeInferencer(knowledge);
    }

    private RubyTypeInferencer(final ContextKnowledge knowledge) {
        this.knowledge = knowledge;
    }

    private void initializeAnalyzer() {
        if (analyzer == null) {
            assert knowledge != null : "need ContextKnowledge for RubyTypeAnalyzer";
            analyzer = new RubyTypeAnalyzer(knowledge);
        }
    }

    /**
     * Tries to infer the type(s) of the given <code>symbol</code> within the
     * context, that is by walking and analyzing {@link #RubyTypeAnalyzer the
     * given block}.
     *
     * @param symbol symbol for which to infer the type
     * @return inferred {@link RubyType types}, never <code>null</code>;
     */
    public RubyType inferType(final String symbol) {
        initializeAnalyzer();
        analyzer.analyze();

        RubyType type = knowledge.getType(symbol);
        if (type == null) {
            type = RubyType.createUnknown();
        }

        // Special cases
        if (!type.isKnown()) {
            // Handle migrations. This needs better flow analysis of block
            // variables but do quickfix for 6.0 which will work in most
            // migrations files.
            if ("t".equals(symbol) && knowledge.getRoot().getNodeType() == NodeType.DEFSNODE) { // NOI18N
                String n = AstUtilities.getName(knowledge.getRoot());
                if ("up".equals(n) || ("down".equals(n))) { // NOI18N
                    return RubyType.create("ActiveRecord::ConnectionAdapters::TableDefinition"); // NOI18N
                }
            }
        }

        // We keep track of the types contained within Arrays
        // internally (and probably hashes as well, TODO)
        // such that we can do the right thing when you operate
        // on an Array. However, clients should only see the "raw" (and real)
        // type.
        if (type.isKnown()) {
            for (String realType : type.getRealTypes()) {
                if (realType.startsWith("Array<")) { // NOI18N
                    return RubyType.ARRAY;
                }
            }
        }

        return type;
    }

    /** Called on AsgnNodes to compute RHS. */
    RubyType inferTypesOfRHS(final Node node) {
        List<Node> children = node.childNodes();
        if (children.size() != 1) {
            return RubyType.createUnknown();
        }
        return inferType(children.get(0));
    }

    RubyType inferType(final Node node) {
        RubyType type = knowledge.getType(node);
        if (type != null) {
            return type;
        }
        if (!knowledge.wasAnalyzed()) {
            new RubyTypeAnalyzer(knowledge).analyze();
        }
        switch (node.getNodeType()) {
            case LOCALVARNODE:
            case DVARNODE:
            case INSTVARNODE:
            case GLOBALVARNODE:
            case CLASSVARNODE:
            case COLON2NODE:
                type = knowledge.getType(AstUtilities.getName(node));
                break;
            case RETURNNODE:
                ReturnNode retNode = (ReturnNode) node;
                type = inferType(retNode.getValueNode());
                break;
            case DEFNNODE:
            case DEFSNODE:
                MethodDefNode methodDefNode = (MethodDefNode) node;
                type = inferMethodNode(methodDefNode);
                break;
            case SELFNODE:
                SelfNode selfNode = (SelfNode) node;
                type = inferSelfNode(selfNode);
                break;
        }
        if (type == null && AstUtilities.isCall(node)) {
            type = RubyMethodTypeInferencer.inferTypeFor(node, knowledge);
        }
        if (type == null) {
            type = getTypeForLiteral(node);
        }
        // null element in types set means that we are not able to infer
        // the expression
        knowledge.setType(node, type);
        return type;
    }

    private RubyType inferSelfNode(SelfNode selfNode) {
        Node root = knowledge.getRoot();
        AstPath path = new AstPath(root, selfNode);
        IScopingNode clazz = AstUtilities.findClassOrModule(path);
        if (clazz == null) {
            return null;
        }
        return RubyType.create(AstUtilities.getClassOrModuleName(clazz));
    }

    private RubyType inferMethodNode(MethodDefNode methodDefNode) {
        String name = methodDefNode.getName();
        RubyType fastType = RubyMethodTypeInferencer.fastCheckType(name);
        if (fastType != null) {
            return fastType;
        }
        if (TypeInferenceSettings.getDefault().getRdocTypeInference()) {
            List<String> rdocs = AstUtilities.gatherDocumentation(knowledge.getParserResult().getSnapshot(), methodDefNode);
            if (rdocs != null) {
                RubyType type = RDocAnalyzer.collectTypesFromComment(rdocs);
                if (type != null && type.isKnown()) {
                    return type;
                }
            }
        }
        // this can be very time consuming, return if TI is not enabled
        RubyType result = new RubyType();
        Set<Node> exits = new LinkedHashSet<Node>();
        AstUtilities.findExitPoints(methodDefNode, exits);
        for (Node exit : exits) {
            result.append(inferType(exit));
        }
        return result;
    }
    /**
     * Returns type for Ruby built-in literal, like String, Array, Hash, Regexp,
     * etc.
     *
     * @param node node representing Ruby build-int literal, i.e. having {@link
     *   Node#nodeId} e.g. {@link NodeType#STRNODE},  {@link
     *   NodeType#ARRAYNODE}, {@link NodeType#HASHNODE},  {@link
     *   NodeType#REGEXPNODE}, ...
     * @return Ruby type as used in Ruby code, e.g. <code>String</code>,
     *   <code>Array</code>,  <code>Hash</code>,  <code>Regexp</code>, ...
     */
    static RubyType getTypeForLiteral(final Node node) {
        switch (node.getNodeType()) {
            case ARRAYNODE:
            case ZARRAYNODE:
                return RubyType.ARRAY; // NOI18N
            case STRNODE:
            case DSTRNODE:
            case XSTRNODE:
            case DXSTRNODE:
                return RubyType.STRING; // NOI18N
            case FIXNUMNODE:
                return RubyType.FIXNUM; // NOI18N
            case BIGNUMNODE:
                return RubyType.BIGNUM; // NOI18N
            case HASHNODE:
                return RubyType.HASH; // NOI18N
            case REGEXPNODE:
            case DREGEXPNODE:
                return RubyType.REGEXP; // NOI18N
            case SYMBOLNODE:
            case DSYMBOLNODE:
                return RubyType.SYMBOL; // NOI18N
            case FLOATNODE:
                return RubyType.FLOAT; // NOI18N
            case NILNODE:
                // NilImplicitNode - don't use it, the type is really unknown!
                if (!node.isInvisible()) {
                    return RubyType.NIL_CLASS; // NOI18N
                }
                break;
            case TRUENODE:
                return RubyType.TRUE_CLASS; // NOI18N
            case FALSENODE:
                return RubyType.FALSE_CLASS; // NOI18N
            //} else if (child instanceof RangeNode) {
            //    return RubyType.RANGE; // NOI18N
        }
        return RubyType.createUnknown();
    }

    @Override
    public String toString() {
        return "RubyTypeAnalyzer[knowledge:" + knowledge + ']'; // NOI18N
    }

}

