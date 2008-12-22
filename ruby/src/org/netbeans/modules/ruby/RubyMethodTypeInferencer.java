/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jruby.nb.ast.CallNode;
import org.jruby.nb.ast.Colon2Node;
import org.jruby.nb.ast.Node;
import org.jruby.nb.ast.NodeType;
import org.jruby.nb.ast.SymbolNode;
import org.jruby.nb.ast.types.INameNode;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;

final class RubyMethodTypeInferencer {

    private final CallNode callNode;
    private final RubyIndex index;

    static RubyType inferTypeFor(final CallNode callNode, final RubyIndex index) {
        return new RubyMethodTypeInferencer(callNode, index).inferType();
    }

    private RubyMethodTypeInferencer(final CallNode callNode, final RubyIndex index) {
        this.callNode = callNode;
        this.index = index;
    }

    private RubyType inferType() {
        String name = callNode.getName();
        Node receiver = callNode.getReceiverNode();
        RubyType receiverType = getReceiverType(receiver);
        // If you call Foo.new I'm going to assume the type of the expression if "Foo"
        if ("new".equals(name)) { // NOI18N
            return receiverType;
        } else if (name.startsWith("find")) {
            // -Possibly- ActiveRecord finders, very important
            if (receiverType.isSingleton() && index != null) {
                IndexedClass superClass = index.getSuperclass(receiverType.first());
                if (superClass != null && "ActiveRecord::Base".equals(superClass.getFqn())) { // NOI18N
                    // Looks like a find method on active record The big
                    // question is whether this is going to return the type
                    // itself (receivedName) or an array of it; that depends on
                    // the args (for find(:all) it's asn array, find(:first)
                    // it's an item, and for find(1,2,3) it's an array etc.
                    // There are other find signatures which define other
                    // semantics
                    return pickFinderType(callNode, name, receiverType);
                }
            }
        }

        RubyType resultType = new RubyType();
        if (index != null) {
            Set<IndexedMethod> methods = index.getInheritedMethods(receiverType, name, NameKind.EXACT_NAME);
            for (IndexedMethod indexedMethod : methods) {
                RubyType type = indexedMethod.getType();
                if (!type.isKnown()) {
                    // fallback to the RDoc comment
                    IndexedElement match = RubyCodeCompleter.findDocumentationEntry(null, indexedMethod);
                    if (match != null) {
                        List<? extends String> comment = RubyCodeCompleter.getComments(null, match);
                        type = RDocAnalyzer.collectTypesFromComment(comment);
                    }
                }
                resultType.append(type);
            }
        }
        return resultType;
    }

    static RubyType inferTypeFor(final List<String> comment) {
        return RDocAnalyzer.collectTypesFromComment(comment);
    }

    /**
     * Look up the right return type for the given finder call.
     */
    private static RubyType pickFinderType(final CallNode call, final String method, final RubyType model) {
        // Dynamic finders
        boolean multiple;
        if (method.startsWith("find_all")) { // NOI18N
            multiple = true;
        } else if (method.startsWith("find_by_") || method.equals("find_first")) { // NOI18N
            multiple = false;
        } else if (method.equals("find")) { // NOI18N
            // Finder method that does both - gotta inspect it
            List<Node> nodes = new ArrayList<Node>();
            AstUtilities.addNodesByType(call, new NodeType[]{NodeType.SYMBOLNODE}, nodes);
            boolean foundAll = false;
            for (Node n : nodes) {
                SymbolNode symbol = (SymbolNode) n;
                if ("all".equals(symbol.getName())) { // NOI18N
                    foundAll = true;
                    break;
                }
            }
            multiple = foundAll;
        } else {
            // Not sure - probably some other locally defined finder method;
            // just default to the model name
            multiple = false;
        }

        if (multiple) {
            return RubyType.create("Array<" + model.first() + ">"); // NOI18N
        } else {
            return model;
        }
    }

    private RubyType getReceiverType(final Node receiver) {
        switch (receiver.nodeId) {
            case COLON2NODE:
                return RubyType.create(AstUtilities.getFqn((Colon2Node) receiver));
            case CALLNODE:
                return RubyMethodTypeInferencer.inferTypeFor((CallNode) receiver, index);
        }
        if (receiver instanceof INameNode) {
            // TODO - compute fqn (packages etc.)
            return RubyType.create(((INameNode) receiver).getName());
        } else {
            return RubyTypeAnalyzer.getTypeForLiteral(receiver);
        }
    }
}
