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
package org.netbeans.modules.php.editor.model.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.PhpKind;
import org.netbeans.modules.php.editor.model.PhpModifiers;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.nodes.PhpDocTypeTagInfo;
import org.netbeans.modules.php.editor.model.nodes.SingleFieldDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
class FieldElementImpl extends ScopeImpl implements FieldElement {
    String defaultType;
    private String className;

    FieldElementImpl(Scope inScope, String defaultType, SingleFieldDeclarationInfo nodeInfo) {
        super(inScope, nodeInfo, nodeInfo.getAccessModifiers(), null);
        this.defaultType = defaultType;
        assert inScope instanceof TypeScope;
        className = inScope.getName();
    }

    FieldElementImpl(Scope inScope, String defaultType, PhpDocTypeTagInfo nodeInfo) {
        super(inScope, nodeInfo, nodeInfo.getAccessModifiers(), null);
        this.defaultType = defaultType;
        assert inScope instanceof TypeScope;
        className = inScope.getName();
    }

    FieldElementImpl(Scope inScope, IndexedConstant indexedConstant) {
        super(inScope, indexedConstant, PhpKind.FIELD);
        String in = indexedConstant.getIn();
        if (in != null) {
            className = in;
        } else {
            className = inScope.getName();
        }
        this.defaultType = indexedConstant.getTypeName();
    }

    private FieldElementImpl(Scope inScope, String name,
            Union2<String/*url*/, FileObject> file, OffsetRange offsetRange,
            PhpModifiers modifiers, String defaultType) {
        super(inScope, name, file, offsetRange, PhpKind.FIELD, modifiers);
        this.defaultType = defaultType;
    }

    @Override
    void addElement(ModelElementImpl element) {
        //super.addElement(element);
    }


    static String toName(SingleFieldDeclaration node) {
        return VariableNameImpl.toName(node.getName());
    }

    static OffsetRange toOffsetRange(SingleFieldDeclaration node) {
        return VariableNameImpl.toOffsetRange(node.getName());
    }

    static PhpModifiers toAccessModifiers(FieldsDeclaration node) {
        return new PhpModifiers(node.getModifier());
    }

    public Collection<? extends TypeScope> getDefaultTypes() {
        return (defaultType != null && defaultType.length() > 0) ?
            CachingSupport.getTypes(defaultType.split("\\|")[0], this) :
            Collections.<TypeScopeImpl>emptyList();

    }
    @Override
    public String getNormalizedName() {
        return className+super.getNormalizedName();
    }

    private static Set<String> recursionDetection = new HashSet<String>();//#168868
    public Collection<? extends TypeScope> getTypes(int offset) {
        AssignmentImpl assignment = findAssignment(offset);
        Collection retval = (assignment != null) ? assignment.getTypes() : Collections.emptyList();
        if  (retval.isEmpty()) {
            retval = getDefaultTypes();
            if (retval.isEmpty()) {
                ClassScope classScope = (ClassScope) getInScope();
                for (VariableName variableName : classScope.getDeclaredVariables()) {
                    if (variableName.representsThis()) {
                        final String checkName = getNormalizedName();
                        boolean added = recursionDetection.add(checkName);
                        try {
                            if (added) {
                                return variableName.getFieldTypes(this, offset);
                            }
                        } finally {
                          recursionDetection.remove(checkName);
                        }
                    }
                }
            }
        }
        return retval;
    }

    public Collection<? extends FieldAssignmentImpl> getAssignments() {
        return filter(getElements(), new ElementFilter() {
            public boolean isAccepted(ModelElement element) {
                return true;
            }
        });
    }

    public AssignmentImpl findAssignment(int offset) {
        FieldAssignmentImpl retval = null;
        Collection<? extends FieldAssignmentImpl> assignments = getAssignments();
        if (assignments.size() == 1) {
            retval = assignments.iterator().next();
        } else {
            for (FieldAssignmentImpl assignmentImpl : assignments) {
                if (assignmentImpl.getBlockRange().containsInclusive(offset)) {
                    if (retval == null || retval.getOffset() <= assignmentImpl.getOffset()) {
                         if (assignmentImpl.getOffset() < offset) {
                            retval = assignmentImpl;
                         }
                    }
                }
            }
        }
        return retval;
    }

    @Override
    public String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().substring(1)).append(";");//NOI18N
        sb.append(getOffset()).append(";");//NOI18N
        sb.append(getPhpModifiers().toBitmask()).append(";");
        if (defaultType != null) {
            sb.append(defaultType);
        }
        sb.append(";");//NOI18N
        return sb.toString();
    }

    public Collection<? extends TypeScope> getFieldTypes(FieldElement element, int offset) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
