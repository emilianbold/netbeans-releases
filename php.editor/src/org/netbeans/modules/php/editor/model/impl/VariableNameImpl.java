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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.PredefinedSymbols;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.elements.VariableElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 * @author Radek Matous
 */
class VariableNameImpl extends ScopeImpl implements VariableName {
    List<LazyFieldAssignment> assignmentDatas = new ArrayList<LazyFieldAssignment>();

    private Collection<TypeScope> getMergedTypes() {
        Collection<TypeScope> types = new HashSet<TypeScope>();
        List<? extends VarAssignmentImpl> varAssignments = getVarAssignments();
        for (VarAssignmentImpl vAssignment : varAssignments) {
            types.addAll(vAssignment.getTypes());
        }
        return types;
    }
    enum TypeResolutionKind {
        LAST_ASSIGNMENT,
        MERGE_ASSIGNMENTS
    };
    private TypeResolutionKind typeResolutionKind = TypeResolutionKind.LAST_ASSIGNMENT;
    private boolean globallyVisible;
    VariableNameImpl(IndexScope inScope, VariableElement indexedVariable) {
        this(inScope, indexedVariable.getName(),
                Union2.<String/*url*/, FileObject>createFirst(indexedVariable.getFilenameUrl()),
                new OffsetRange(indexedVariable.getOffset(),indexedVariable.getOffset()+indexedVariable.getName().length()), true);
    }
    VarAssignmentImpl createAssignment(Scope scope, boolean conditionalBlock,OffsetRange blockRange, OffsetRange nameRange, Assignment assignment, Map<String, AssignmentImpl> allAssignments) {
        VarAssignmentImpl retval = new VarAssignmentImpl(this, scope, conditionalBlock, blockRange, nameRange,assignment, allAssignments);
        return retval;
    }

    VariableNameImpl(Scope inScope, Variable variable, boolean globallyVisible) {
        this(inScope,
                toName(variable), inScope.getFile(), toOffsetRange(variable), globallyVisible);
    }
    VariableNameImpl(Scope inScope, String name, Union2<String/*url*/, FileObject> file, OffsetRange offsetRange, boolean globallyVisible) {
        super(inScope, name, file, offsetRange, PhpElementKind.VARIABLE);
        this.globallyVisible = globallyVisible;
    }

    void setTypeResolutionKind(TypeResolutionKind typeResolutionKind) {
        this.typeResolutionKind = typeResolutionKind;
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

    public List<? extends VarAssignmentImpl> getVarAssignments() {
        Collection<? extends VarAssignmentImpl> values = filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElement element) {
                return element instanceof VarAssignmentImpl;
            }
        });
        return new ArrayList<VarAssignmentImpl>(values);
    }
    private List<? extends FieldAssignmentImpl> getFieldAssignments() {
        Collection<? extends FieldAssignmentImpl> values = filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElement element) {
                return element instanceof FieldAssignmentImpl;
            }
        });
        return new ArrayList<FieldAssignmentImpl>(values);
    }
    AssignmentImpl findVarAssignment(int offset) {
        return findAssignment(offset, true, null);
    }
    AssignmentImpl findFieldAssignment(int offset, FieldElement expectedField) {
        return findAssignment(offset, false, expectedField);
    }

    String findFieldType(int offset, String fldName) {
        String retval = null;
        int retvalOffset = -1;
        if (assignmentDatas.isEmpty() && (isGloballyVisible() || representsThis())) {
            Scope inScope = getInScope();
            if (inScope != null) {
                inScope = inScope.getInScope();
            }
            if (inScope instanceof VariableScope) {
                VariableScope varScope = (VariableScope) inScope;
                List<? extends VariableName> variables = ModelUtils.filter(varScope.getDeclaredVariables(), getName());
                if (!variables.isEmpty()) {
                    VariableName varName = ModelUtils.getFirst(variables);
                    if (varName instanceof VariableNameImpl) {
                        return ((VariableNameImpl) varName).findFieldType(offset, fldName);
                    }
                }
            }
        }
        for (LazyFieldAssignment assign : assignmentDatas) {
            if (assign.scope.getBlockRange().containsInclusive(offset)) {
                if (retval == null || retvalOffset <= assign.startOffset) {
                    if (assign.startOffset < offset) {
                        if (fldName.equals(assign.fldName)) {
                            retval = assign.typeName;
                        } else if (assign.fldName.length() > 0 && fldName.equals(assign.fldName.substring(1))) {
                            retval = assign.typeName;
                        }
                    }
                }
            }
        }

        return retval;
    }

    AssignmentImpl findAssignment(int offset, boolean varAssignment,FieldElement expectedField) {
        AssignmentImpl retval = null;
        Collection<? extends AssignmentImpl> assignments = varAssignment ? 
            getVarAssignments() : getFieldAssignments();
        if (assignments.size() == 1) {
            AssignmentImpl assign = assignments.iterator().next();
            if (expectedField == null || expectedField.equals(assign.getContainer())) {
                retval = assign;
            }

        }
        if (retval == null) {
            if (assignments.isEmpty() && (isGloballyVisible() || representsThis())) {
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
                            return ((VariableNameImpl)varName).findAssignment(offset, true, null);
                        }
                    }
                }
            }            
            if (!assignments.isEmpty()) {
                for (AssignmentImpl assign : assignments) {
                    if (retval == null || retval.getOffset() <= assign.getOffset()) {
                        if (assign.getOffset() < offset) {
                            if (expectedField == null || expectedField.equals(assign.getContainer())) {
                                retval = assign;
                            }
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

    public Collection<? extends String> getTypeNames(int offset) {
        return getTypeNamesImpl(offset, false);
    }
    public Collection<? extends String> getArrayAccessTypeNames(int offset) {
        return getTypeNamesImpl(offset, true);
    }

    public Collection<? extends TypeScope> getArrayAccessTypes(int offset) {
        return getTypesImpl(offset, true);
    }

    public Collection<? extends TypeScope> getTypes(int offset) {
        return getTypesImpl(offset, false);
    }
    private Collection<? extends String> getTypeNamesImpl(int offset, boolean arrayAccess) {
        if (representsThis()) {
            ClassScope classScope = (ClassScope) getInScope();
            return Collections.singletonList(classScope.getName());
        }
        Collection<String> retval = new ArrayList<String>();
        TypeResolutionKind useTypeResolutionKind = arrayAccess ?
            TypeResolutionKind.MERGE_ASSIGNMENTS : typeResolutionKind;
        if (useTypeResolutionKind.equals(TypeResolutionKind.LAST_ASSIGNMENT)) {
            AssignmentImpl assignment = findVarAssignment(offset);
            while (assignment != null) {
                if (assignment.isConditionalBlock()) {
                    if (!assignment.getBlockRange().containsInclusive(offset)) {
                        return getMergedTypeNames();
                    }
                }
                Collection<String> typeNames = assignment.getTypeNames();
                if (typeNames.isEmpty() || assignment.isArrayAccess()) {
                    if (assignment.isArrayAccess()) {
                        retval = Collections.singleton("array"); //NOI18N
                    }
                    AssignmentImpl nextAssignment = findVarAssignment(assignment.getOffset() - 1);
                    if (nextAssignment != null && !nextAssignment.equals(assignment)) {
                        assignment = nextAssignment;
                        continue;
                    }
                    break;
                }
                return typeNames;
            }
            return retval;
        } else {
            return getMergedTypeNames();
        }
    }

    private Collection<? extends String> getMergedTypeNames() {
        Collection<String> types = new HashSet<String>();
        List<? extends VarAssignmentImpl> varAssignments = getVarAssignments();
        for (VarAssignmentImpl vAssignment : varAssignments) {
            types.addAll(vAssignment.getTypeNames());
        }
        return types;
    }

    private Collection<? extends TypeScope> getTypesImpl(int offset, boolean arrayAccess) {
        if (representsThis()) {
            ClassScope classScope = (ClassScope) getInScope();
            return Collections.singletonList(classScope);
        }
        TypeResolutionKind useTypeResolutionKind = arrayAccess ?
            TypeResolutionKind.MERGE_ASSIGNMENTS : typeResolutionKind;
        if (useTypeResolutionKind.equals(TypeResolutionKind.LAST_ASSIGNMENT)) {
            AssignmentImpl assignment = findVarAssignment(offset);
            while (assignment != null) {
                if (assignment.isConditionalBlock()) {
                    if (!assignment.getBlockRange().containsInclusive(offset)) {
                        return getMergedTypes();
                    }
                }
                Collection<TypeScope> types = assignment.getTypes();
                if (types.isEmpty() || assignment.isArrayAccess()) {
                    AssignmentImpl nextAssignment = findVarAssignment(assignment.getOffset() - 1);
                    if (nextAssignment != null && !nextAssignment.equals(assignment)) {
                        assignment = nextAssignment;
                        continue;
                    }
                    break;
                }
                return types;
            }
        } else {
            return getMergedTypes();
        }
        return Collections.emptyList();
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
        if (inScope instanceof ClassScope && getName().equals("$this")) {//NOI18N
            return true;
        }
        return false;
    }

    @Override
    public String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        final String varName = getName();
        sb.append(varName.toLowerCase()).append(";");//NOI18N
        sb.append(varName).append(";");//NOI18N
        //makes little sense because the variable with the same name can exists in huge number of files
        /*
        Set<String> typeNames = new HashSet<String>(getTypeNames(getNameRange().getEnd()+1));
        if (typeNames.size() == 1) {
            for (String typeName : typeNames) {
                if (!typeName.contains("@")) {
                    sb.append(typeName);
                    break;
                }
            }
        }*/
        sb.append(";");//NOI18N
        sb.append(getOffset()).append(";");//NOI18N
        return sb.toString();
    }

    void createLazyFieldAssignment(FieldAccess fieldAccess, Assignment node, Scope scope) {
        String fldName = CodeUtils.extractVariableName(fieldAccess.getField());
        if (fldName != null) {
            if (!fldName.startsWith("$")) {
                fldName = "$" + fldName; //NOI18N
            }
        }
        String typeName = VariousUtils.extractVariableTypeFromAssignment(node, Collections.<String, AssignmentImpl>emptyMap());
        ASTNodeInfo<FieldAccess> fieldInfo = ASTNodeInfo.create(fieldAccess);
        final OffsetRange range = fieldInfo.getRange();
        final int startOffset = fieldAccess.getStartOffset();
        assignmentDatas.add(new LazyFieldAssignment(typeName, fldName, range, startOffset, scope));
    }

    public Collection<? extends TypeScope> getFieldTypes(FieldElement element, int offset) {
        processFieldAssignments();
        AssignmentImpl assignment = findFieldAssignment(offset, element);
        return (assignment != null) ? assignment.getTypes() : element.getTypes(offset);
    }

    void processFieldAssignments() {
        if (!assignmentDatas.isEmpty()) {
            for (LazyFieldAssignment fieldAssignmentData : assignmentDatas) {
                fieldAssignmentData.process();
            }
            assignmentDatas = Collections.emptyList();
        }
    }

    private class LazyFieldAssignment {
        private final String typeName;
        private final String fldName;
        private final OffsetRange range;
        private final int startOffset;
        private final Scope scope;

        private LazyFieldAssignment(String typeName, String fldName, OffsetRange range, int startOffset, Scope scope) {
            this.typeName = typeName;
            this.fldName = fldName;
            this.range = range;
            this.startOffset = startOffset;
            this.scope = scope;
        }

        void process() {
            Collection<? extends TypeScope> types = getTypes(startOffset);
            FieldElementImpl field = null;
            TypeScope type = ModelUtils.getFirst(types);
            if (type instanceof ClassScope) {
                ClassScope cls = (ClassScope) type;
                field = (FieldElementImpl) ModelUtils.getFirst(cls.getDeclaredFields(), fldName);
                if (field != null) {
                    FieldAssignmentImpl fa = new FieldAssignmentImpl(VariableNameImpl.this, (FieldElementImpl) field, scope, scope.getBlockRange(), range, typeName);
                    addElement(fa);
                }
            }
        }
    }
}

