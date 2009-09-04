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

package org.netbeans.modules.php.editor.model.nodes;

import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.QualifiedName;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
public class ElementInfo  {
    private Scope scope;
    private Union2<ASTNodeInfo, ModelElement> element;

    public ElementInfo(ModelElement element) {
        this.element = Union2.createSecond(element);
        if (element instanceof Scope) {
            this.scope  = (Scope) element;
        } else {
            this.scope  = element.getInScope();
        }
    }


    public ElementInfo(ASTNodeInfo nodeInfo, ModelElement element) {
        this.element = Union2.createFirst(nodeInfo);
        if (element instanceof Scope) {
            this.scope  = (Scope) element;
        } else {
            this.scope  = element.getInScope();
        }
    }
    /**
     * @return the scope
     */
    public Scope getScope() {
        return scope;
    }

    public FileScope getFileScope() {
        return ModelUtils.getFileScope(scope);
    }
    public NamespaceScope getNamespaceScope() {
        return ModelUtils.getNamespaceScope(scope);
    }
    public QualifiedName getQualifiedName() {
        ASTNodeInfo nodeInfo = getNodeInfo();
        QualifiedName qualifiedName = null;
        if (nodeInfo != null) {
            qualifiedName = nodeInfo.getQualifiedName();
        } else {
            ModelElement modelElemnt = getModelElemnt();
            final QualifiedName namespaceName = modelElemnt.getNamespaceName();
            qualifiedName = namespaceName.append(modelElemnt.getName());
        }
        return qualifiedName;
    }
    public String getName() {
        ASTNodeInfo nodeInfo = getNodeInfo();
        if (nodeInfo != null) {
            return nodeInfo.getName();
        }
        return getModelElemnt().getName();
    }
    public ASTNodeInfo.Kind getKind() {
        ASTNodeInfo nodeInfo = getNodeInfo();
        if (nodeInfo != null) {
            return nodeInfo.getKind();
        }
        ASTNodeInfo.Kind kind = null;
        ModelElement modelElemnt = getModelElemnt();
        switch (modelElemnt.getPhpKind()) {
            case CLASS:
                kind = Kind.CLASS;
                break;
            case CLASS_CONSTANT:
                kind = Kind.CLASS_CONSTANT;
                break;
            case CONSTANT:
                kind = Kind.CONSTANT;
                break;
            case FIELD:
                kind = Kind.FIELD;
                break;
            case FUNCTION:
                kind = Kind.FUNCTION;
                break;
            case IFACE:
                kind = Kind.IFACE;
                break;
            case INCLUDE:
                kind = Kind.INCLUDE;
                break;
            case METHOD:
                boolean isStatic = modelElemnt.getPhpModifiers().isStatic();
                kind = isStatic ? Kind.STATIC_METHOD : Kind.METHOD;
                break;
            case VARIABLE:
                kind = Kind.VARIABLE;
                break;
        }
        assert kind != null;
        return kind;
    }
    public OffsetRange getRange() {
        ASTNodeInfo nodeInfo = getNodeInfo();
        if (nodeInfo != null) {
            return nodeInfo.getRange();
        }
        return getModelElemnt().getNameRange();
    }

    public Union2<ASTNodeInfo, ModelElement> getRawElement() {
        return element;
    }

    private ASTNodeInfo getNodeInfo() {
        return element.hasFirst() ? element.first() : null;
    }
    private ModelElement getModelElemnt() {
        return element.hasSecond() ? element.second() : null;
    }
}
