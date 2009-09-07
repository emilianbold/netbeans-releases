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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.IScopingNode;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.modules.ruby.options.TypeInferenceSettings;

final class RubyMethodTypeInferencer {


    private static final String[] COMPARISON_OPERATORS = {"==", "===", "!=", }; //NOI18N
    /**
     * Names of the methods that whose return type is (typically) the same as the receiver.
     */
    private static final String[] RECEIVER_METHODS = {"new", "clone", "dup", "freeze", "+", "-"};

    /**
     * Method names whose return type we know.
     */
    private static final Map<String, RubyType> METHOD_TYPES = new HashMap<String, RubyType>(16);

    static {
        // some implementations of the comparison method below may return nil,
        // such as in Module or Class
        METHOD_TYPES.put("<=>", new RubyType(RubyType.FIXNUM, RubyType.NIL_CLASS));
        METHOD_TYPES.put("<", new RubyType(RubyType.BOOLEAN, RubyType.NIL_CLASS));
        METHOD_TYPES.put(">", new RubyType(RubyType.BOOLEAN, RubyType.NIL_CLASS));
        METHOD_TYPES.put("<=", new RubyType(RubyType.BOOLEAN, RubyType.NIL_CLASS));
        METHOD_TYPES.put("=>", new RubyType(RubyType.BOOLEAN, RubyType.NIL_CLASS));

        METHOD_TYPES.put("to_s", RubyType.STRING);
        METHOD_TYPES.put("to_str", RubyType.STRING);
        METHOD_TYPES.put("to_string", RubyType.STRING);
        METHOD_TYPES.put("to_sym", RubyType.SYMBOL);
        METHOD_TYPES.put("to_symbol", RubyType.SYMBOL);
        METHOD_TYPES.put("to_a", RubyType.ARRAY);
        METHOD_TYPES.put("to_ary", RubyType.ARRAY);
        METHOD_TYPES.put("to_array", RubyType.ARRAY);
        METHOD_TYPES.put("to_i", RubyType.INTEGER);
        METHOD_TYPES.put("to_int", RubyType.INTEGER);
        METHOD_TYPES.put("to_f", RubyType.FLOAT);
        METHOD_TYPES.put("to_float", RubyType.FLOAT);
    }


    private final Node callNodeToInfer;
    private final ContextKnowledge knowledge;
    private final boolean fast;


    private RubyMethodTypeInferencer(final Node nodeToInfer, final ContextKnowledge knowledge, boolean fast) {
        assert AstUtilities.isCall(nodeToInfer) : "Must be a call node";
        this.callNodeToInfer = nodeToInfer;
        this.knowledge = knowledge;
        this.fast = fast;
    }

    static RubyType inferTypeFor(final Node nodeToInfer, final ContextKnowledge knowledge, boolean fast) {
        return new RubyMethodTypeInferencer(nodeToInfer, knowledge, fast).inferType();
    }

    /**
     * Attempts to resolve the return type of the given method 
     * based on its name (which is very fast).
     * @param methodName
     * @return the return type or <code>null</code>.
     */
    static RubyType fastCheckType(String methodName) {
        // assume all methods ending with '?' return boolean
        if (methodName.endsWith("?") || isTrueFalseCall(methodName)) {
            return RubyType.BOOLEAN;
        }
        return METHOD_TYPES.get(methodName);
    }

    private static boolean isTrueFalseCall(String methodName) {
        for (String each : COMPARISON_OPERATORS) {
            if (each.equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean returnsReceiver(String methodName) {
        // If you call Foo.new or I'm going to assume the type of the expression is "Foo"
        for (String each : RECEIVER_METHODS) {
            if (each.equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    RubyIndex getIndex() {
        return knowledge == null ? null : knowledge.getIndex();
    }

    private RubyType inferType() {
        String name = AstUtilities.getName(callNodeToInfer);
        Node receiver = null;
        RubyType receiverType = null;
        switch (callNodeToInfer.getNodeType()) {
            case CALLNODE:
                receiver = ((CallNode) callNodeToInfer).getReceiverNode();
                break;
            case FCALLNODE:
            case VCALLNODE:
                Node root = knowledge.getRoot();
                AstPath path = new AstPath(root, callNodeToInfer);
                IScopingNode clazz = AstUtilities.findClassOrModule(path);
                if (clazz == null) {
                    break;
                }
                receiverType = RubyType.create(AstUtilities.getClassOrModuleName(clazz));
                break;
            default:
                throw new IllegalArgumentException("Illegal node passed: " + callNodeToInfer);
        }
        // first try whether we can figure out the return type 
        // without resolving the receiver type (which can take some time)
        RubyType fastResult = fastCheckType(name);
        if (fastResult != null) {
            return fastResult;
        }

        if (receiverType == null && receiver != null) {
            receiverType = getReceiverType(receiver);
        }

        if (receiverType == null) {
            return RubyType.createUnknown();
        }

        if (returnsReceiver(name)) {
            return receiverType;
        }

        if (FindersHelper.isFinderMethod(name)) {
            // -Possibly- ActiveRecord finders, very important
            if (receiverType.isSingleton() && getIndex() != null) {
                IndexedClass superClass = getIndex().getSuperclass(receiverType.first());
                if (superClass != null && RubyIndex.ACTIVE_RECORD_BASE.equals(superClass.getFqn())) { // NOI18N
                    // Looks like a find method on active record The big
                    // question is whether this is going to return the type
                    // itself (receivedName) or an array of it; that depends on
                    // the args (for find(:all) it's asn array, find(:first)
                    // it's an item, and for find(1,2,3) it's an array etc.
                    // There are other find signatures which define other
                    // semantics
                    return FindersHelper.pickFinderType((CallNode) callNodeToInfer, name, receiverType);
                }
            }
        }

        // this can be very time consuming, return if TI is not enabled and
        // we're operating in the fast mode
        if (fast && !TypeInferenceSettings.getDefault().getMethodTypeInference()) {
            return RubyType.createUnknown();
        }

        RubyType resultType = new RubyType();
        RubyIndex index = getIndex();
        if (index == null) {
            return resultType;
        }

        Set<IndexedMethod> methods = new HashSet<IndexedMethod>();
        // first methods from the class itself
        for (String type : receiverType.getRealTypes()) {
            methods = index.getMethods(name, type, QuerySupport.Kind.EXACT);
        }
        if (methods.isEmpty()) {
            // inherited methods
            // TODO: should consider only the return type of the first inherited method in the hiearchy
            methods = index.getInheritedMethods(receiverType, name, QuerySupport.Kind.EXACT);
        }
        for (IndexedMethod indexedMethod : methods) {
            RubyType type = indexedMethod.getType();
            resultType.append(type);
        }
        index.logMostTimeConsuming();
        return resultType;
    }

    private RubyType getReceiverType(final Node receiver) {
        RubyType type = RubyTypeInferencer.create(knowledge).inferType(receiver);
        if (!type.isKnown() && receiver instanceof INameNode) {
            String name = ((INameNode) receiver).getName();
            // create a type for classes only -- no point in creating a type
            // for a variable or method whose type we couldn't infer
            if (RubyUtils.isValidConstantName(name)) {
            // TODO - compute fqn (packages etc.)
                type = RubyType.create(((INameNode) receiver).getName());
            }
        }
        return type;
    }
}
