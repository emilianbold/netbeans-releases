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

import java.util.Set;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.SymbolNode;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.ruby.RubyDeclarationFinderHelper.RubyAltLocation;
import org.netbeans.modules.ruby.elements.IndexedClass;

/**
 *
 * @author Erno Mononen
 */
final class ActiveRecordAssociationFinder {

    private static final String HAS_MANY = "has_many";
    private static final String HAS_AND_BELONGS_TO_MANY = "has_and_belongs_to_many";

    static final String[] AR_ASSOCIATIONS = {"belongs_to", "has_one", HAS_MANY, HAS_AND_BELONGS_TO_MANY};

    private final ParserResult info;
    private final SymbolNode closest;
    private final Node root;
    private final AstPath path;

    public ActiveRecordAssociationFinder(ParserResult info, SymbolNode closest, Node root, AstPath path) {
        this.info = info;
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
        if (AstUtilities.isActiveRecordAssociation(path.leafParent())){
            return path.leafParent();
        }

        if (AstUtilities.isActiveRecordAssociation(path.leafGrandParent())) {
            return path.leafGrandParent();
        }

        return null;
    }

    private String getClassNameFor(Node associationNode) {
        String className = AstUtilities.getName(closest);
        if (className.length() == 0) {
            return className;
        }
        className = Character.toUpperCase(className.charAt(0)) + className.substring(1);
        String associationName = AstUtilities.getName(associationNode);
        if ((HAS_MANY.equals(associationName) || HAS_AND_BELONGS_TO_MANY.equals(associationName))
                && className.length() > 1
                && className.endsWith("s")) { //NOI18N
            return className.substring(0, className.length() -1);
        }
        return className;
    }

    DeclarationLocation findAssociationLocation() {
        Node associationNode = findAssociationNode();
        if (associationNode == null) {
            return DeclarationLocation.NONE;
        }

        String className = getClassNameFor(associationNode);
        if (className.length() == 0) {
            return DeclarationLocation.NONE;
        }
        Set<IndexedClass> modelClasses = RubyIndex.get(info).getSubClasses(RubyIndex.ACTIVE_RECORD_BASE, null, className, false);
        if (modelClasses.isEmpty()) {
            return DeclarationLocation.NONE;
        }

        DeclarationLocation result = DeclarationLocation.NONE;
        for (IndexedClass clazz : modelClasses) {
            if (!clazz.getName().equals(className)) {
                continue;
            }
            Node foreign = AstUtilities.getForeignNode(clazz);
            if (result == DeclarationLocation.NONE) {
                result = new DeclarationLocation(clazz.getFileObject(), AstUtilities.getRange(foreign).getStart(), clazz);
                result.addAlternative(new RubyAltLocation(clazz, clazz.getName().equalsIgnoreCase(clazz.getFileObject().getName())));
            } else {
                result.addAlternative(new RubyAltLocation(clazz, clazz.getName().equalsIgnoreCase(clazz.getFileObject().getName())));
            }
        }
        return result;
    }
}
