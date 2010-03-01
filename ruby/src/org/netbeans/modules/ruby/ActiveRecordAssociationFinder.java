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

import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;
import org.jrubyparser.ast.ArrayNode;
import org.jrubyparser.ast.HashNode;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.StrNode;
import org.jrubyparser.ast.SymbolNode;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.ruby.elements.IndexedClass;

/**
 *
 * @author Erno Mononen
 */
final class ActiveRecordAssociationFinder {

    private static final String HAS_MANY = "has_many";
    private static final String HAS_MANY_POLYMORPHS = "has_many_polymorphs";
    private static final String HAS_AND_BELONGS_TO_MANY = "has_and_belongs_to_many";

    static final String[] AR_ASSOCIATIONS = {"belongs_to", "has_one", HAS_MANY, HAS_MANY_POLYMORPHS, HAS_AND_BELONGS_TO_MANY};

    private final RubyIndex index;
    private final SymbolNode closest;
    private final Node root;
    private final AstPath path;

    public ActiveRecordAssociationFinder(RubyIndex index, SymbolNode closest, Node root, AstPath path) {
        this.index = index;
        this.closest = closest;
        this.root = root;
        this.path = path;
    }

    private Node findAssociationNode() {
        // first argument
        for (Node child : closest.childNodes()) {
            if (AstUtilities.isActiveRecordAssociation(child)) {
                return child;
            }
        }
        // others
        ListIterator<Node> leafToRoot = path.leafToRoot();
        while(leafToRoot.hasNext()) {
            Node next = leafToRoot.next();
            if (AstUtilities.isActiveRecordAssociation(next)) {
                return next;
            }
        }

        return null;
    }

    private static String getExplicitySpecifiedClassName(Node associationNode) {
        if (associationNode.childNodes().isEmpty()
                || HAS_MANY_POLYMORPHS.equals(AstUtilities.getName(associationNode))) {
            return null;
        }
        ArrayNode parameters = (ArrayNode) associationNode.childNodes().get(0);
        for (Node param : parameters.childNodes()) {
            if (!(param instanceof HashNode)) {
                continue;
            }
            HashNode hash = (HashNode) param;
            if (hash.childNodes().isEmpty()) {
                continue;
            }
            Node hashParams = param.childNodes().get(0);
            if (hashParams.childNodes().size() < 2) {
                continue;
            }
            for (int i = 0; i < hashParams.childNodes().size(); i++) {
                Node each = hashParams.childNodes().get(i);
                if (!(each instanceof INameNode)) {
                    continue;
                }
                if ("class_name".equals(AstUtilities.getName(each)) &&  hashParams.childNodes().size() > i + 1) {
                    Node value = hashParams.childNodes().get(i + 1);
                    if (value instanceof StrNode) {
                        return ((StrNode) value).getValue();
                    } else {
                        return AstUtilities.safeGetName(value);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the class name specified in the given <code>associationNode</code>, e.g. 
     * returns <code>Project</code> for <code>has_many :projects</code> or
     * <code>User</code>belongs_to :owner, class_name => "User"</code>.
     * 
     * @param associationNode the call node, e.g. the node for <code>has_many</code>.
     * @param closest the closest symbol node for the call node.
     * 
     * @return the class name.
     */
    static String getClassNameFor(Node associationNode, SymbolNode closest) {
        // first check whether class_name is explicitly specified,
        // e.g. has_many :details, class_name => "UserDetail"
        String className = getExplicitySpecifiedClassName(associationNode);
        if (className == null) {
            className = AstUtilities.getName(closest);
            if (className.length() == 0) {
                return className;
            }
            className = RubyUtils.underlinedNameToCamel(className);
        }
        String associationName = AstUtilities.getName(associationNode);
        if ((HAS_MANY.equals(associationName)
                || HAS_MANY_POLYMORPHS.equals(associationName)
                || HAS_AND_BELONGS_TO_MANY.equals(associationName))) { //NOI18N
            return Inflector.getDefault().singularize(className);
        }
        return className;
    }

    DeclarationLocation findAssociationLocation() {
        Node associationNode = findAssociationNode();
        if (associationNode == null) {
            return DeclarationLocation.NONE;
        }

        String className = getClassNameFor(associationNode, closest);
        if (className.length() == 0) {
            return DeclarationLocation.NONE;
        }
        Set<IndexedClass> modelClasses = index.getSubClasses(RubyIndex.ACTIVE_RECORD_BASE, null, className, false);
        if (modelClasses.isEmpty()) {
            return DeclarationLocation.NONE;
        }
        Set<IndexedClass> result = new HashSet<IndexedClass>();
        for (IndexedClass model : modelClasses) {
            if (model.getName().equals(className)) {
                result.add(model);
            }
        }

        return RubyDeclarationFinder.getLocation(result);
    }
}
