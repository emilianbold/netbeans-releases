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

import java.util.List;
import java.util.Set;
import org.jrubyparser.ast.CallNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.INameNode;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.modules.ruby.options.TypeInferenceSettings;

final class RubyMethodTypeInferencer {

    static RubyType inferTypeFor(final ContextKnowledge knowledge) {
        return inferTypeFor((CallNode) knowledge.getTarget(), knowledge);
    }

    static RubyType inferTypeFor(final Node nodeToInfer, final ContextKnowledge knowledge) {
        return new RubyMethodTypeInferencer(nodeToInfer, knowledge).inferType();
    }

    private Node callNodeToInfer;
    private ContextKnowledge knowledge;

    private RubyMethodTypeInferencer(final Node nodeToInfer, final ContextKnowledge knowledge) {
        assert AstUtilities.isCall(nodeToInfer) : "Must be a call node";
        this.callNodeToInfer = nodeToInfer;
        this.knowledge = knowledge;
    }

    private boolean enabled() {
        return TypeInferenceSettings.getDefault().getMethodTypeInference();
    }

    RubyIndex getIndex() {
        return knowledge == null ? null : knowledge.getIndex();
    }

    private RubyType inferType() {
        String name = AstUtilities.getName(callNodeToInfer);
        Node receiver = null;
        switch (callNodeToInfer.getNodeType()) {
            case CALLNODE:
                if (RubyTypeAnalyzer.isTrueFalseCall(name)) {
                    return RubyType.BOOLEAN;
                }
                receiver = ((CallNode) callNodeToInfer).getReceiverNode();
                break;
            case FCALLNODE:
                // TODO: receiver is self;
                break;
            case VCALLNODE:
                receiver = null;
                break;
            default:
                throw new IllegalArgumentException("Illegal node passed: " + callNodeToInfer);
        }
        if (receiver == null) {
            return RubyType.createUnknown();
        }
        RubyType receiverType = getReceiverType(receiver);
        // If you call Foo.new I'm going to assume the type of the expression if "Foo"
        if ("new".equals(name)) { // NOI18N
            return receiverType;
        } else if (FindersHelper.isFinderMethod(name)) {
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

        // this can be very time consuming, return if TI is not enabled
        if (!enabled()) {
            return RubyType.createUnknown();
        }

        RubyType resultType = new RubyType();
        if (getIndex() != null) {
            Set<IndexedMethod> methods = getIndex().getInheritedMethods(receiverType, name, QuerySupport.Kind.EXACT);
            for (IndexedMethod indexedMethod : methods) {
                RubyType type = indexedMethod.getType();
                // no point in searching rdoc for dynamic methods
                if (!type.isKnown() && indexedMethod.getMethodType() != IndexedMethod.MethodType.DYNAMIC_FINDER) {
                    // fallback to the RDoc comment
                    IndexedElement match = RubyCodeCompleter.findDocumentationEntry(null, indexedMethod);
                    if (match != null) {
                        List<String> comment = RubyCodeCompleter.getComments(null, match);
                        if (comment != null) {
                            type = RDocAnalyzer.collectTypesFromComment(comment);
                        }
                    }
                }
                resultType.append(type);
            }
        }
        return resultType;
    }

    private RubyType getReceiverType(final Node receiver) {
        RubyType type = new RubyTypeInferencer(knowledge).inferType(receiver);
        if (!type.isKnown() && receiver instanceof INameNode) {
            // TODO - compute fqn (packages etc.)
            type = RubyType.create(((INameNode) receiver).getName());
        }
        return type;
    }

}
