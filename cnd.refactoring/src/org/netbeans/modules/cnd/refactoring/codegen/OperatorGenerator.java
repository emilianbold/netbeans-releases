/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.refactoring.codegen;

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmFunctionParameterList;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.services.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.ui.ElementNode;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.codegen.ui.OperatorsPanel;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.GeneratorUtils;
import org.netbeans.modules.cnd.utils.ui.UIGesturesSupport;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class OperatorGenerator implements CodeGenerator {

    public static final class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CsmContext path = context.lookup(CsmContext.class);
            if (component == null || path == null) {
                return ret;
            }
            CsmClass typeElement = path.getEnclosingClass();
            if (typeElement == null) {
                return ret;
            }
            CsmObject objectUnderOffset = path.getObjectUnderOffset();
            final Set<CsmField> shouldBeInitializedFields = new LinkedHashSet<>();
            final Set<CsmField> mayBeIninitializedFields = new LinkedHashSet<>();
            final Set<CsmField> cannotBeInitializedFields = new LinkedHashSet<>();
            final List<CsmConstructor> constructors = new ArrayList<>();
            final Map<CsmClass,List<CsmConstructor>> inheritedConstructors = new HashMap<>();
            // check base class
            for (CsmInheritance csmInheritance : typeElement.getBaseClasses()) {
                CsmClass baseClass = CsmInheritanceUtilities.getCsmClass(csmInheritance);
                if (baseClass != null) {
                    List<CsmConstructor> list = new ArrayList<>();
                    for (CsmMember member : baseClass.getMembers()) {
                        if (CsmKindUtilities.isConstructor(member) &&
                            CsmInheritanceUtilities.matchVisibility(member, csmInheritance.getVisibility()) &&
                            !isCopyConstructor(baseClass, (CsmConstructor)member)) {
                            list.add((CsmConstructor)member);
                        }
                    }
                    if (!list.isEmpty()) {
                        inheritedConstructors.put(baseClass, list);
                    }
                }
            }
            GeneratorUtils.scanForFieldsAndConstructors(typeElement, shouldBeInitializedFields, mayBeIninitializedFields, cannotBeInitializedFields, constructors);
            ElementNode.Description constructorDescription = null;
            if (!inheritedConstructors.isEmpty()) {
                List<ElementNode.Description> baseClassesDescriptions = new ArrayList<>();
                for (Map.Entry<CsmClass,List<CsmConstructor>> entry : inheritedConstructors.entrySet()) {
                    List<ElementNode.Description> constructorDescriptions = new ArrayList<>();
                    for(CsmConstructor c : entry.getValue()) {
                        constructorDescriptions.add(ElementNode.Description.create(c, null, true, false));
                    }
                    baseClassesDescriptions.add(ElementNode.Description.create(entry.getKey(), constructorDescriptions, false, false));
                }
                constructorDescription = ElementNode.Description.create(typeElement, baseClassesDescriptions, false, false);
            }
            ElementNode.Description fieldsDescription = null;
            if (!mayBeIninitializedFields.isEmpty() || !shouldBeInitializedFields.isEmpty() || !cannotBeInitializedFields.isEmpty()) {
                List<ElementNode.Description> fieldDescriptions = new ArrayList<>();
                for (CsmField variableElement : mayBeIninitializedFields) {
                    fieldDescriptions.add(ElementNode.Description.create(variableElement, null, true, variableElement.equals(objectUnderOffset)));
                }
                for (CsmField variableElement : shouldBeInitializedFields) {
                    fieldDescriptions.add(ElementNode.Description.create(variableElement, null, true, true));
                }
                for (CsmField variableElement : cannotBeInitializedFields) {
                    fieldDescriptions.add(ElementNode.Description.create(variableElement, null, false, false));
                }
                fieldsDescription = ElementNode.Description.create(typeElement, Collections.singletonList(ElementNode.Description.create(typeElement, fieldDescriptions, false, false)), false, false);
            }
            if (constructorDescription == null && fieldsDescription == null) {
                return ret;
            }
            List<ElementNode.Description> operators;
            ElementNode.Description operatorsDescription;
            
            operators = new ArrayList<>();
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.EQ), null, true, false));
            operatorsDescription = ElementNode.Description.create(typeElement, Collections.singletonList(ElementNode.Description.create(typeElement, operators, false, false)), false, false);
            ret.add(new OperatorGenerator(component, path, typeElement, operatorsDescription, "LBL_operatorAssignment")); //NOI18N
            
            operators = new ArrayList<>();
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.PLUS_EQ), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.PLUS), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.MINUS_EQ), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.MINUS), null, true, false));
            operatorsDescription = ElementNode.Description.create(typeElement, Collections.singletonList(ElementNode.Description.create(typeElement, operators, false, false)), false, false);
            ret.add(new OperatorGenerator(component, path, typeElement, operatorsDescription, "LBL_operatorBinaryArithmetic")); //NOI18N
            
            operators = new ArrayList<>();
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.EQ_EQ), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.NOT_EQ), null, true, false));
            operatorsDescription = ElementNode.Description.create(typeElement, Collections.singletonList(ElementNode.Description.create(typeElement, operators, false, false)), false, false);
            ret.add(new OperatorGenerator(component, path, typeElement, operatorsDescription, "LBL_operatorRelational")); //NOI18N
            
            operators = new ArrayList<>();
            operators.add(ElementNode.Description.create(new StubFriendImpl(typeElement, CsmFunction.OperatorKind.LEFT_SHIFT), null, true, false));
            operators.add(ElementNode.Description.create(new StubFriendImpl(typeElement, CsmFunction.OperatorKind.RIGHT_SHIFT), null, true, false));
            operatorsDescription = ElementNode.Description.create(typeElement, Collections.singletonList(ElementNode.Description.create(typeElement, operators, false, false)), false, false);
            ret.add(new OperatorGenerator(component, path, typeElement, operatorsDescription, "LBL_operatorFriendStream")); //NOI18N
            
            return ret;
        }
        
        private boolean isCopyConstructor(CsmClass cls, CsmConstructor constructor) {
            Collection<CsmParameter> parameters = constructor.getParameters();
            if (parameters.size() == 1) {
                CsmParameter p = parameters.iterator().next();
                CsmType paramType = p.getType();
                if (paramType.isReference()) {
                    if (cls.equals(paramType.getClassifier())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
    
    private final JTextComponent component;
    private final ElementNode.Description operators;
    private final CsmContext contextPath;
    private final CsmClass type;
    private final String id;

    /** Creates a new instance of ConstructorGenerator */
    private OperatorGenerator(JTextComponent component, CsmContext path, CsmClass type, ElementNode.Description operators, String id) {
        this.component = component;
        this.operators = operators;
        this.contextPath = path;
        this.type = type;
        this.id=id;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ConstructorGenerator.class, id);
    }

    @Override
    public void invoke() {
        UIGesturesSupport.submit(CsmRefactoringUtils.USG_CND_REFACTORING, CsmRefactoringUtils.GENERATE_TRACKING, "CONSTRUCTOR"); // NOI18N
        if (operators != null) {
            final OperatorsPanel panel = new OperatorsPanel(operators);
            DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel, NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_operator")); //NOI18N
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
            try {
                dialog.setVisible(true);
            } catch (Throwable th) {
                if (!(th.getCause() instanceof InterruptedException)) {
                    throw new RuntimeException(th);
                }
                dialogDescriptor.setValue(DialogDescriptor.CANCEL_OPTION);
            } finally {
                dialog.dispose();
            }
            if (dialogDescriptor.getValue() != dialogDescriptor.getDefaultValue()) {
                return;
            }
            GeneratorUtils.generateOperators(contextPath,  type, panel.getOperatorsToGenerate());
        }
    }

    private static abstract class StubFunctionImpl implements CsmFunction {
        protected final CsmClass parent;
        private final CsmFunction.OperatorKind kind;
        private String name;
        private String parameters;
        private String specifiers;
        private String returns;
        private String body;

        public StubFunctionImpl(CsmClass parent, CsmFunction.OperatorKind kind) {
            this.parent = parent;
            this.kind = kind;
            init();
        }

        private void init() {
            StringBuilder buf = new StringBuilder();
            switch (kind) {
                case PLUS_EQ:
                    name = "operator +="; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType()+"&"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "PLUS_EQ", getTemplateType()); // NOI18N
                    break;
                case PLUS:
                    name = "operator +"; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType();
                    body = NbBundle.getMessage(ConstructorGenerator.class, "PLUS", getTemplateType()); // NOI18N
                    break;
                case MINUS_EQ:
                    name = "operator -="; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType()+"&"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "MINUS_EQ", getTemplateType()); // NOI18N
                    break;
                case MINUS:
                    name = "operator -"; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType();
                    body = NbBundle.getMessage(ConstructorGenerator.class, "MINUS", getTemplateType()); // NOI18N
                    break;
                case EQ_EQ:
                    name = "operator =="; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = "bool"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "EQ_EQ", getTemplateType()); // NOI18N
                    break;
                case NOT_EQ:
                    name = "operator !="; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = "bool"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "NOT_EQ", getTemplateType()); // NOI18N
                    break;
                case EQ:
                    name = "operator ="; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType()+"&"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "ASSIGNMENT", getTemplateType()); // NOI18N
                    break;
                case LEFT_SHIFT:
                    specifiers=getTemplatePrefix("friend");// NOI18N
                    name = "operator <<"; // NOI18N
                    parameters = "std::ostream& os, const " + getTemplateType() + "& obj"; // NOI18N
                    returns = "std::ostream&"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "LEFT_SHIFT", getTemplateType()); // NOI18N
                    break;
                case RIGHT_SHIFT:
                    specifiers=getTemplatePrefix("friend");// NOI18N
                    name = "operator >>"; // NOI18N
                    parameters = "std::ostream& is, const " + getTemplateType() + "& obj"; // NOI18N
                    returns = "std::ostream&"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "RIGHT_SHIFT", getTemplateType()); // NOI18N
                    break;
            }
        }

        private String getTemplatePrefix(String prefix) {
            StringBuilder res = new StringBuilder();
            if (CsmKindUtilities.isTemplate(parent)) {
                final CsmTemplate template = (CsmTemplate)parent;
                List<CsmTemplateParameter> templateParameters = template.getTemplateParameters();
                if (templateParameters.size() > 0) {
                    res.append("template<");//NOI18N
                    boolean first = true;
                    for(CsmTemplateParameter param : templateParameters) {
                        if (!first) {
                            res.append(", "); //NOI18N
                        }
                        first = false;
                        res.append(param.getName());
                    }
                    res.append(">");//NOI18N
                    res.append('\n');//NOI18N
                }
            }
            res.append(prefix);
            return res.toString();
        }

        private String getTemplateType() {
            StringBuilder res = new StringBuilder();
            res.append(parent.getName());
            if (CsmKindUtilities.isTemplate(parent)) {
                final CsmTemplate template = (CsmTemplate)parent;
                List<CsmTemplateParameter> templateParameters = template.getTemplateParameters();
                if (templateParameters.size() > 0) {
                    res.append("<");//NOI18N
                    boolean first = true;
                    for(CsmTemplateParameter param : templateParameters) {
                        if (!first) {
                            res.append(", "); //NOI18N
                        }
                        first = false;
                        res.append(param.getName());
                    }
                    res.append(">");//NOI18N
                }
            }
            return res.toString();
        }
        
        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public CsmDeclaration.Kind getKind() {
            return CsmDeclaration.Kind.FUNCTION;
        }

        @Override
        public CharSequence getUniqueName() {
            return name;
        }

        @Override
        public CharSequence getQualifiedName() {
            return name;
        }

        @Override
        public CharSequence getName() {
            return name;
        }

        @Override
        public CsmScope getScope() {
            return parent;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public CsmFile getContainingFile() {
            return parent.getContainingFile();
        }

        @Override
        public int getStartOffset() {
            return -1;
        }

        @Override
        public int getEndOffset() {
            return -1;
        }

        @Override
        public CsmOffsetable.Position getStartPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CsmOffsetable.Position getEndPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getText() {
            StringBuilder buf = new StringBuilder();
            if (specifiers!=null) {
                buf.append(specifiers);
                buf.append(' '); // NOI18N
            }
            buf.append(returns);
            buf.append(' '); // NOI18N
            buf.append(name);
            buf.append('('); // NOI18N
            buf.append(parameters);
            buf.append(") {\n"); // NOI18N
            buf.append(body);
            buf.append("\n}"); // NOI18N
            return buf.toString();
        }

        @Override
        public CharSequence getDeclarationText() {
            return getText();
        }

        @Override
        public CsmFunctionDefinition getDefinition() {
            return null;
        }

        @Override
        public CsmFunction getDeclaration() {
            return this;
        }

        @Override
        public boolean isOperator() {
            return true;
        }

        @Override
        public CsmFunction.OperatorKind getOperatorKind() {
            return kind;
        }

        @Override
        public boolean isInline() {
            return true;
        }

        @Override
        public CsmFunctionParameterList getParameterList() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CsmType getReturnType() {
            return new CsmType() {

                @Override
                public CsmClassifier getClassifier() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public CharSequence getClassifierText() {
                    return returns;
                }

                @Override
                public boolean isInstantiation() {
                    return false;
                }

                @Override
                public List<CsmSpecializationParameter> getInstantiationParams() {
                    return Collections.emptyList();
                }

                @Override
                public int getArrayDepth() {
                    return 0;
                }

                @Override
                public boolean isPointer() {
                    return false;
                }

                @Override
                public int getPointerDepth() {
                    return 0;
                }

                @Override
                public boolean isReference() {
                    return false;
                }

                @Override
                public boolean isRValueReference() {
                    return false;
                }

                @Override
                public boolean isConst() {
                    return false;
                }

                @Override
                public boolean isBuiltInBased(boolean resolveTypeChain) {
                    return false;
                }

                @Override
                public boolean isTemplateBased() {
                    return false;
                }

                @Override
                public CharSequence getCanonicalText() {
                    return returns;
                }

                @Override
                public CsmFile getContainingFile() {
                    return parent.getContainingFile();
                }

                @Override
                public int getStartOffset() {
                    return -1;
                }

                @Override
                public int getEndOffset() {
                    return -1;
                }

                @Override
                public Position getStartPosition() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Position getEndPosition() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public CharSequence getText() {
                    return returns;
                }
            };
        }

        @Override
        public Collection<CsmParameter> getParameters() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getSignature() {
            StringBuilder buf = new StringBuilder();
            buf.append(name);
            buf.append('('); // NOI18N
            buf.append(parameters);
            buf.append(")"); // NOI18N
            return buf.toString();
        }

        @Override
        public Collection<CsmScopeElement> getScopeElements() {
            return Collections.emptyList();
        }
    }

    private static final class StubMethodImpl extends StubFunctionImpl implements CsmMethod {

        public StubMethodImpl(CsmClass parent, CsmFunction.OperatorKind kind) {
            super(parent, kind);
        }

        @Override
        public CsmDeclaration.Kind getKind() {
            return CsmDeclaration.Kind.FUNCTION_FRIEND;
        }

        @Override
        public boolean isAbstract() {
            return false;
        }

        @Override
        public boolean isVirtual() {
            return false;
        }

        @Override
        public boolean isExplicit() {
            return false;
        }

        @Override
        public boolean isConst() {
            return false;
        }

        @Override
        public CsmClass getContainingClass() {
            return parent;
        }

        @Override
        public CsmVisibility getVisibility() {
            return CsmVisibility.NONE;
        }

        @Override
        public boolean isStatic() {
            return false;
        }
    }
    private static final class StubFriendImpl extends StubFunctionImpl implements CsmFriendFunction {

        public StubFriendImpl(CsmClass parent, OperatorKind kind) {
            super(parent, kind);
        }

        @Override
        public CsmFunction getReferencedFunction() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CsmClass getContainingClass() {
            return parent;
        }
    }
}
