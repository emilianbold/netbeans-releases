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
import java.util.List;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.PredefinedSymbols;
import org.netbeans.modules.php.editor.index.IndexedVariable;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.PhpKind;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 * @author Radek Matous
 */
class VariableNameImpl extends ScopeImpl implements VariableName {
    private boolean globallyVisible;
    VariableNameImpl(IndexScope inScope, IndexedVariable indexedVariable) {
        this(inScope, indexedVariable.getName(),
                Union2.<String/*url*/, FileObject>createFirst(indexedVariable.getFilenameUrl()),
                new OffsetRange(indexedVariable.getOffset(),indexedVariable.getOffset()+indexedVariable.getName().length()), true);
    }
    VarAssignmentImpl createElement(Scope scope, OffsetRange blockRange, OffsetRange nameRange, Assignment assignment, Map<String, AssignmentImpl> allAssignments) {
        VarAssignmentImpl retval = new VarAssignmentImpl(this, scope, blockRange, nameRange,assignment, allAssignments);
        addElement(retval);
        return retval;
    }

    VariableNameImpl(Scope inScope, Variable variable, boolean globallyVisible) {
        this(inScope, toName(variable), inScope.getFile(), toOffsetRange(variable), globallyVisible);
    }
    VariableNameImpl(Scope inScope, String name, Union2<String/*url*/, FileObject> file, OffsetRange offsetRange, boolean globallyVisible) {
        super(inScope, name, file, offsetRange, PhpKind.VARIABLE);
        this.globallyVisible = globallyVisible;
    }

    static String toName(Variable node) {
        return CodeUtils.extractVariableName(node);
    }

    static OffsetRange toOffsetRange(Variable node) {
        Expression name = node.getName();
        //TODO: dangerous never ending loop
        while ((name instanceof Variable)) {
            while (name instanceof ArrayAccess) {
                ArrayAccess access = (ArrayAccess) name;
                name = access.getName();
            }
            if (name instanceof Variable) {
                Variable var = (Variable) name;
                name = var.getName();
            }
        }
        return new OffsetRange(name.getStartOffset(), name.getEndOffset());
    }

    public List<? extends VarAssignmentImpl> getAssignments() {
        return (List<? extends VarAssignmentImpl>) getElements();
    }

    public AssignmentImpl findAssignment(int offset) {
        VarAssignmentImpl retval = null;
        Collection<? extends VarAssignmentImpl> assignments = getAssignments();
        if (assignments.size() == 1) {
            retval = assignments.iterator().next();
        } else {
            if (assignments.isEmpty() && isGloballyVisible()) {
                Scope inScope = getInScope();
                if (inScope != null) {
                    inScope = inScope.getInScope();
                }
                if (inScope instanceof VariableScope) {
                    VariableScope varScope = (VariableScope)inScope;
                    List<? extends VariableName> variables = ModelUtils.filter(varScope.getDeclaredVariables(), getName());
                    if (!variables.isEmpty()) {
                        VariableName varName = ModelUtils.getFirst(variables);
                        if (varName instanceof VariableNameImpl) {
                            return ((VariableNameImpl)varName).findAssignment(offset);
                        }
                    }
                }
            }
            for (VarAssignmentImpl varAssignmentImpl : assignments) {
                if (varAssignmentImpl.getBlockRange().containsInclusive(offset)) {
                    if (retval == null || retval.getOffset() <= varAssignmentImpl.getOffset()) {
                         if (varAssignmentImpl.getOffset() < offset) {
                            retval = varAssignmentImpl;
                         }
                    }
                }
            }
        }
        return retval;
    }

    @Override
    public String getNormalizedName() {
        Scope inScope = getInScope();
        if (inScope instanceof MethodScope ) {
            String methodName = representsThis() ? "" : inScope.getName();//NOI18N
            inScope = inScope.getInScope();
            return (inScope != null && !isGloballyVisible()) ? inScope.getName()+methodName+getName() : getName();
        }
        return (inScope != null && !isGloballyVisible()) ? inScope.getName()+getName() : getName();
    }

    public Collection<? extends TypeScope> getTypes(int offset) {
        List<? extends TypeScope> empty = Collections.emptyList();
        if (representsThis()) {
            MethodScope methodScope = (MethodScope) getInScope();
            return Collections.singletonList(methodScope.getTypeScope());
        }
        AssignmentImpl assignment = findAssignment(offset);
        return (assignment != null) ? assignment.getTypes() : empty;
    }

    public boolean isGloballyVisible() {
        String name = getName();
        if (name.startsWith("$")) {
            name = name.substring(1);
        }
        return globallyVisible || PredefinedSymbols.SUPERGLOBALS.contains(name);
    }

    /**
     * @param globallyVisible the globallyVisible to set
     */
    void setGloballyVisible(boolean globallyVisible) {
        this.globallyVisible = globallyVisible;
    }

    public boolean representsThis() {
        Scope inScope = getInScope();
        if (inScope instanceof MethodScope && getName().equals("$this")) {//NOI18N
            return true;
        }
        return false;
    }

    @Override
    public String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        final String varName = getName();
        String varNameNoDollar = varName.startsWith("$") ? varName.substring(1) : varName;
        if (!PredefinedSymbols.isSuperGlobalName(varNameNoDollar)) {
            sb.append(varName.toLowerCase()).append(";");//NOI18N
            sb.append(varName).append(";");//NOI18N
            sb.append(";");//NOI18N
            sb.append(getOffset()).append(";");//NOI18N
            return sb.toString();
        }
        return null;
    }
}
