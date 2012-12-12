/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionName;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode;
import org.netbeans.modules.php.editor.parser.astnodes.PHPVarComment;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TraitMethodAliasDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 *
 * @author Petr Pisl
 */
public class SemanticAnalysis extends SemanticAnalyzer {

    public static final EnumSet<ColoringAttributes> UNUSED_FIELD_SET = EnumSet.of(ColoringAttributes.UNUSED, ColoringAttributes.FIELD);
    public static final EnumSet<ColoringAttributes> DEPRECATED_UNUSED_FIELD_SET = EnumSet.of(ColoringAttributes.DEPRECATED, ColoringAttributes.UNUSED, ColoringAttributes.FIELD);
    public static final EnumSet<ColoringAttributes> DEPRECATED_FIELD_SET = EnumSet.of(ColoringAttributes.DEPRECATED, ColoringAttributes.FIELD);
    public static final EnumSet<ColoringAttributes> UNUSED_STATIC_FIELD_SET = EnumSet.of(ColoringAttributes.UNUSED, ColoringAttributes.FIELD, ColoringAttributes.STATIC);
    public static final EnumSet<ColoringAttributes> DEPRECATED_UNUSED_STATIC_FIELD_SET = EnumSet.of(
            ColoringAttributes.DEPRECATED,
            ColoringAttributes.UNUSED,
            ColoringAttributes.FIELD,
            ColoringAttributes.STATIC);
    public static final EnumSet<ColoringAttributes> DEPRECATED_STATIC_FIELD_SET = EnumSet.of(ColoringAttributes.DEPRECATED, ColoringAttributes.FIELD, ColoringAttributes.STATIC);
    public static final EnumSet<ColoringAttributes> UNUSED_METHOD_SET = EnumSet.of(ColoringAttributes.UNUSED, ColoringAttributes.METHOD);
    public static final EnumSet<ColoringAttributes> DEPRECATED_UNUSED_METHOD_SET = EnumSet.of(ColoringAttributes.DEPRECATED, ColoringAttributes.UNUSED, ColoringAttributes.METHOD);
    public static final EnumSet<ColoringAttributes> STATIC_METHOD_SET = EnumSet.of(ColoringAttributes.STATIC, ColoringAttributes.METHOD);
    public static final EnumSet<ColoringAttributes> DEPRECATED_STATIC_METHOD_SET = EnumSet.of(ColoringAttributes.DEPRECATED, ColoringAttributes.STATIC, ColoringAttributes.METHOD);
    public static final EnumSet<ColoringAttributes> DEPRECATED_METHOD_SET = EnumSet.of(ColoringAttributes.DEPRECATED, ColoringAttributes.METHOD);
    public static final EnumSet<ColoringAttributes> UNUSED_STATIC_METHOD_SET = EnumSet.of(ColoringAttributes.STATIC, ColoringAttributes.METHOD, ColoringAttributes.UNUSED);
    public static final EnumSet<ColoringAttributes> DEPRECATED_UNUSED_STATIC_METHOD_SET = EnumSet.of(
            ColoringAttributes.DEPRECATED,
            ColoringAttributes.STATIC,
            ColoringAttributes.METHOD,
            ColoringAttributes.UNUSED);
    public static final EnumSet<ColoringAttributes> UNUSED_USES_SET = EnumSet.of(ColoringAttributes.UNUSED);
    public static final EnumSet<ColoringAttributes> DEPRECATED_CLASS_SET = EnumSet.of(ColoringAttributes.DEPRECATED, ColoringAttributes.CLASS);
    public static final EnumSet<ColoringAttributes> DEPRECATED_SET = EnumSet.of(ColoringAttributes.DEPRECATED);
    public static final EnumSet<ColoringAttributes> DEPRECATED_STATIC_SET = EnumSet.of(ColoringAttributes.DEPRECATED, ColoringAttributes.STATIC);
    private static final String NAMESPACE_SEPARATOR = "\\"; //NOI18N

    // @GuarderBy("this")
    private boolean cancelled;
    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;
    private Set<UnusedOffsetRanges> unusedUsesOffsetRanges;

    public static Set<UnusedOffsetRanges> computeUnusedUsesOffsetRanges(PHPParseResult r) {
        SemanticAnalysis semanticAnalysis = new SemanticAnalysis();
        semanticAnalysis.process(r);
        return semanticAnalysis.getUnusedUsesOffsetRanges();
    }

    public SemanticAnalysis() {
        semanticHighlights = null;
    }

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
    }

    private Set<UnusedOffsetRanges> getUnusedUsesOffsetRanges() {
        return unusedUsesOffsetRanges;
    }

    @Override
    public synchronized void cancel() {
        cancelled = true;
    }

    @Override
    public void run(Result r, SchedulerEvent event) {
        resume();

        if (isCancelled()) {
            return;
        }
        process(r);
    }

    void process(Result r) {
        PHPParseResult result = (PHPParseResult) r;
        Map<OffsetRange, Set<ColoringAttributes>> highlights = new HashMap<OffsetRange, Set<ColoringAttributes>>(100);
        if (result.getProgram() != null) {
            SemanticHighlightVisitor semanticHighlightVisitor = new SemanticHighlightVisitor(highlights, result.getSnapshot(), result.getModel());
            result.getProgram().accept(semanticHighlightVisitor);
            if (highlights.size() > 0) {
                semanticHighlights = highlights;
            } else {
                semanticHighlights = null;
            }
            unusedUsesOffsetRanges = semanticHighlightVisitor.getUnusedUsesOffsetRanges();
        }
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    private class SemanticHighlightVisitor extends DefaultTreePathVisitor {

        private class ASTNodeColoring {
            public ASTNode identifier;
            public Set<ColoringAttributes> coloring;

            public ASTNodeColoring(ASTNode identifier, Set<ColoringAttributes> coloring) {
                this.identifier = identifier;
                this.coloring = coloring;
            }
        }

        Map<OffsetRange, Set<ColoringAttributes>> highlights;
        // for unused private fields: name, varible
        // if isused, then it's deleted from the list and marked as the field
        private final Map<UnusedIdentifier, ASTNodeColoring> privateFieldsUnused;
        // for unsed private method: name, identifier
        private final Map<UnusedIdentifier, ASTNodeColoring> privateUnusedMethods;
        // this is holder of blocks, which has to be scanned for usages in the class.
        private List<Block> needToScan = new ArrayList<Block>();

        private final Map<String, ASTNodeColoring> unusedUses;

        private final Snapshot snapshot;

        private final Map<String, UnusedOffsetRanges> unusedUsesOffsetRanges;

        private final Model model;

        private final Set<TypeElement> deprecatedTypes;

        private final Set<MethodElement> deprecatedMethods;

        private final Set<FieldElement> deprecatedFields;

        private final Set<TypeConstantElement> deprecatedConstants;

        // last visited type declaration
        private TypeDeclaration typeDeclaration;

        public SemanticHighlightVisitor(Map<OffsetRange, Set<ColoringAttributes>> highlights, Snapshot snapshot, Model model) {
            this.highlights = highlights;
            privateFieldsUnused = new HashMap<UnusedIdentifier, ASTNodeColoring>();
            unusedUses = new HashMap<String, ASTNodeColoring>();
            privateUnusedMethods = new HashMap<UnusedIdentifier, ASTNodeColoring>();
            unusedUsesOffsetRanges = new HashMap<String, UnusedOffsetRanges>();
            this.snapshot = snapshot;
            this.model = model;
            deprecatedTypes = ElementFilter.forDeprecated(true).filter(model.getIndexScope().getIndex().getTypes(NameKind.empty()));
            deprecatedMethods = ElementFilter.forDeprecated(true).filter(model.getIndexScope().getIndex().getMethods(NameKind.empty()));
            deprecatedFields = ElementFilter.forDeprecated(true).filter(model.getIndexScope().getIndex().getFields(NameKind.empty()));
            deprecatedConstants = ElementFilter.forDeprecated(true).filter(model.getIndexScope().getIndex().getTypeConstants(NameKind.empty()));
        }

        public Set<UnusedOffsetRanges> getUnusedUsesOffsetRanges() {
            HashSet<UnusedOffsetRanges> result = new HashSet<UnusedOffsetRanges>();
            for (UnusedOffsetRanges unusedOffsetRanges : unusedUsesOffsetRanges.values()) {
                result.add(unusedOffsetRanges);
            }
            return result;
        }

        private void addOffsetRange(ASTNode node, Set<ColoringAttributes> coloring) {
            int start = snapshot.getOriginalOffset(node.getStartOffset());
            if (start > -1) {
                int end = start + node.getEndOffset() - node.getStartOffset();
                assert coloring != null : snapshot.getText().toString();
                highlights.put(new OffsetRange(start, end), coloring);
            }
        }

        @Override
        public void visit(Program program) {
            scan(program.getStatements());
            scan(program.getComments());
            for (ASTNodeColoring item : unusedUses.values()) {
                addOffsetRange(item.identifier, item.coloring);
            }
        }

        @Override
        public void visit(ClassDeclaration cldec) {
            if (isCancelled()) {
                return;
            }
            addToPath(cldec);
            this.typeDeclaration = cldec;
            scan(cldec.getSuperClass());
            scan(cldec.getInterfaes());
            Identifier name = cldec.getName();
            addOffsetRange(name, createTypeNameColoring(name));
            needToScan = new ArrayList<Block>();
            if (cldec.getBody() != null) {
                cldec.getBody().accept(this);

                // find all usages in the method bodies
                for (Block block : needToScan) {
                    block.accept(this);
                }
                // are there unused private fields?
                for (ASTNodeColoring item : privateFieldsUnused.values()) {
                    if (item.coloring.contains(ColoringAttributes.STATIC)) {
                        if (item.coloring.contains(ColoringAttributes.DEPRECATED)) {
                            addOffsetRange(item.identifier, DEPRECATED_UNUSED_STATIC_FIELD_SET);
                        } else {
                            addOffsetRange(item.identifier, UNUSED_STATIC_FIELD_SET);
                        }
                    } else {
                        if (item.coloring.contains(ColoringAttributes.DEPRECATED)) {
                            addOffsetRange(item.identifier, DEPRECATED_UNUSED_FIELD_SET);
                        } else {
                            addOffsetRange(item.identifier, UNUSED_FIELD_SET);
                        }
                    }

                }

                // are there unused private methods?
                for (ASTNodeColoring item : privateUnusedMethods.values()) {
                    if (item.coloring.contains(ColoringAttributes.STATIC)) {
                        if (item.coloring.contains(ColoringAttributes.DEPRECATED)) {
                            addOffsetRange(item.identifier, DEPRECATED_UNUSED_STATIC_METHOD_SET);
                        } else {
                            addOffsetRange(item.identifier, UNUSED_STATIC_METHOD_SET);
                        }
                    } else {
                        if (item.coloring.contains(ColoringAttributes.DEPRECATED)) {
                            addOffsetRange(item.identifier, DEPRECATED_UNUSED_METHOD_SET);
                        } else {
                            addOffsetRange(item.identifier, UNUSED_METHOD_SET);
                        }
                    }
                }
            }
        }

        private Set<ColoringAttributes> createTypeNameColoring(Identifier typeName) {
            Set<ColoringAttributes> result;
            if (isDeprecatedTypeDeclaration(typeName)) {
                result = DEPRECATED_CLASS_SET;
            } else {
                result = ColoringAttributes.CLASS_SET;
            }
            return result;
        }

        private boolean isDeprecatedTypeDeclaration(Identifier typeName) {
            boolean isDeprecated = false;
            VariableScope variableScope = model.getVariableScope(typeName.getStartOffset());
            QualifiedName fullyQualifiedName = VariousUtils.getFullyQualifiedName(QualifiedName.create(typeName), typeName.getStartOffset(), variableScope);
            for (TypeElement typeElement : deprecatedTypes) {
                if (typeElement.getFullyQualifiedName().equals(fullyQualifiedName)) {
                    isDeprecated = true;
                    break;
                }
            }
            return isDeprecated;
        }

        @Override
        public void visit(MethodDeclaration md) {
            scan(md.getFunction().getFormalParameters());
            boolean isPrivate = Modifier.isPrivate(md.getModifier());
            Identifier identifier = md.getFunction().getFunctionName();
            String name = identifier.getName();
            Set<ColoringAttributes> coloring = createMethodDeclarationColoring(md);
            // don't color private magic private method. methods which start __
            if (isPrivate && name != null && !name.startsWith("__")) {
                privateUnusedMethods.put(new UnusedIdentifier(identifier.getName(), typeDeclaration), new ASTNodeColoring(identifier, coloring));
            } else {
                // color now only non private method
                addOffsetRange(identifier, coloring);
            }
            if (!Modifier.isAbstract(md.getModifier())) {
                // don't scan the body now. It should be scanned after all declarations
                // are known
                Block body = md.getFunction().getBody();
                if (body != null) {
                    needToScan.add(body);
                }
            }
        }

        private Set<ColoringAttributes> createMethodDeclarationColoring(MethodDeclaration methodDeclaration) {
            boolean isDeprecated = isDeprecatedMethodDeclaration(methodDeclaration.getFunction().getFunctionName());
            Set<ColoringAttributes> coloring = isDeprecated ? DEPRECATED_METHOD_SET : ColoringAttributes.METHOD_SET;
            if (Modifier.isStatic(methodDeclaration.getModifier())) {
                coloring = isDeprecated ? DEPRECATED_STATIC_METHOD_SET : STATIC_METHOD_SET;
            }
            return coloring;
        }

        private boolean isDeprecatedMethodDeclaration(Identifier methodName) {
            boolean isDeprecated = false;
            VariableScope variableScope = model.getVariableScope(methodName.getStartOffset());
            QualifiedName typeFullyQualifiedName = VariousUtils.getFullyQualifiedName(QualifiedName.create(typeDeclaration.getName()), methodName.getStartOffset(), variableScope);
            for (MethodElement methodElement : deprecatedMethods) {
                if (methodElement.getName().equals(methodName.getName()) && methodElement.getType().getFullyQualifiedName().equals(typeFullyQualifiedName)) {
                    isDeprecated = true;
                    break;
                }
            }
            return isDeprecated;
        }

        @Override
        public void visit(TraitMethodAliasDeclaration node) {
            if (node.getNewMethodName() != null) {
                addOffsetRange(node.getNewMethodName(), ColoringAttributes.METHOD_SET);
            }
        }

        @Override
        public void visit(MethodInvocation node) {
            Identifier identifier = null;
            if (node.getMethod().getFunctionName().getName() instanceof Variable) {
                Variable variable = (Variable) node.getMethod().getFunctionName().getName();
                if (variable.getName() instanceof Identifier) {
                    identifier = (Identifier) variable.getName();
                }
            } else if (node.getMethod().getFunctionName().getName() instanceof Identifier) {
                identifier = (Identifier) node.getMethod().getFunctionName().getName();
            }
            if (identifier != null) {
                if (isDeprecatedMethodInvocation(ModelUtils.resolveType(model, node), identifier)) {
                    addOffsetRange(identifier, DEPRECATED_SET);
                }
                ASTNodeColoring item = privateUnusedMethods.remove(new UnusedIdentifier(identifier.getName(), typeDeclaration));
                if (item != null) {
                    addOffsetRange(item.identifier, item.coloring);
                }
            }
            super.visit(node);
        }

        private boolean isDeprecatedMethodInvocation(Collection<? extends TypeScope> typeScopes, Identifier identifier) {
            boolean isDeprecated = false;
            for (TypeScope typeScope : typeScopes) {
                for (MethodScope methodScope : typeScope.getMethods()) {
                    if (methodScope.getName().equals(identifier.getName()) && methodScope.isDeprecated()) {
                        isDeprecated = true;
                        break;
                    }
                }
            }
            return isDeprecated;
        }

        @Override
        public void visit(InterfaceDeclaration node) {
            if (isCancelled()) {
                return;
            }
            typeDeclaration = node;
            Identifier name = node.getName();
            addOffsetRange(name, createTypeNameColoring(name));
            super.visit(node);
        }

        @Override
        public void visit(TraitDeclaration node) {
            if (isCancelled()) {
                return;
            }
            typeDeclaration = node;
            Identifier name = node.getName();
            addOffsetRange(name, createTypeNameColoring(name));
            needToScan = new ArrayList<Block>();
            if (node.getBody() != null) {
                node.getBody().accept(this);
                for (Block block : needToScan) {
                    block.accept(this);
                }
            }
            super.visit(node);
        }

        @Override
        public void visit(FieldsDeclaration node) {
            if (isCancelled()) {
                return;
            }
            boolean isPrivate = Modifier.isPrivate(node.getModifier());
            boolean isStatic = Modifier.isStatic(node.getModifier());
            Variable[] variables = node.getVariableNames();
            for (int i = 0; i < variables.length; i++) {
                Variable variable = variables[i];
                Set<ColoringAttributes> coloring = createFieldDeclarationColoring(variable, isStatic);
                if (!isPrivate) {
                    addOffsetRange(variable.getName(), coloring);
                } else {
                    if (variable.getName() instanceof Identifier) {
                        Identifier identifier =  (Identifier) variable.getName();
                        privateFieldsUnused.put(new UnusedIdentifier(identifier.getName(), typeDeclaration), new ASTNodeColoring(identifier, coloring));
                    }
                }
            }
            super.visit(node);
        }

        private Set<ColoringAttributes> createFieldDeclarationColoring(Variable variable, boolean isStatic) {
            boolean isDeprecated = isDeprecatedFieldDeclaration(variable);
            Set<ColoringAttributes> coloring = isDeprecated ? DEPRECATED_FIELD_SET : ColoringAttributes.FIELD_SET;
            if (isStatic) {
                coloring = isDeprecated ? DEPRECATED_STATIC_FIELD_SET : ColoringAttributes.STATIC_FIELD_SET;
            }
            return coloring;
        }

        private boolean isDeprecatedFieldDeclaration(Variable variable) {
            boolean isDeprecated = false;
            String variableName = CodeUtils.extractVariableName(variable);
            VariableScope variableScope = model.getVariableScope(variable.getStartOffset());
            QualifiedName typeFullyQualifiedName = VariousUtils.getFullyQualifiedName(
                    QualifiedName.create(typeDeclaration.getName()),
                    variable.getStartOffset(),
                    variableScope);
            for (FieldElement fieldElement : deprecatedFields) {
                if (fieldElement.getName().equals(variableName) && fieldElement.getType().getFullyQualifiedName().equals(typeFullyQualifiedName)) {
                    isDeprecated = true;
                    break;
                }
            }
            return isDeprecated;
        }

        @Override
        public void visit(FieldAccess node) {
            if (isCancelled()) {
                return;
            }
            if (!node.getField().isDollared()) {
                Expression expr = node.getField().getName();
                Collection<? extends TypeScope> resolvedTypes = ModelUtils.resolveType(model, node);
                String variableName = CodeUtils.extractVariableName(node.getField());
                boolean isDeprecated = isDeprecatedFieldAccess(resolvedTypes, "$" + variableName); //NOI18N
                (new FieldAccessVisitor(isDeprecated ? DEPRECATED_FIELD_SET : ColoringAttributes.FIELD_SET)).scan(expr);
            }
            scan(node.getField());
            super.scan(node.getDispatcher());
        }

        private boolean isDeprecatedFieldAccess(Collection<? extends TypeScope> typeScopes, String variableName) {
            boolean isDeprecated = false;
            for (TypeScope typeScope : typeScopes) {
                Collection<? extends org.netbeans.modules.php.editor.model.FieldElement> declaredFields = null;
                if (typeScope instanceof ClassScope) {
                    ClassScope classScope = (ClassScope) typeScope;
                    declaredFields = classScope.getDeclaredFields();
                } else if (typeScope instanceof TraitScope) {
                    TraitScope traitScope = (TraitScope) typeScope;
                    declaredFields = traitScope.getDeclaredFields();
                }
                if (declaredFields != null) {
                    for (org.netbeans.modules.php.editor.model.FieldElement fieldElement : declaredFields) {
                        if (fieldElement.getName().equals(variableName) && fieldElement.isDeprecated()) {
                            isDeprecated = true;
                            break;
                        }
                    }
                }
            }
            return isDeprecated;
        }

        @Override
        public void visit(StaticMethodInvocation node) {
            if (isCancelled()) {
                return;
            }
            boolean isDeprecated = false;
            FunctionName fnName = node.getMethod().getFunctionName();
            if (fnName.getName() instanceof Identifier) {
                Identifier identifier = (Identifier) fnName.getName();
                isDeprecated = isDeprecatedMethodInvocation(ModelUtils.resolveType(model, node), identifier);
                String name = identifier.getName();
                ASTNodeColoring item = privateUnusedMethods.remove(new UnusedIdentifier(name, typeDeclaration));
                if (item != null) {
                    addOffsetRange(item.identifier, item.coloring);
                }
            }
            addOffsetRange(fnName, isDeprecated ? DEPRECATED_STATIC_SET : ColoringAttributes.STATIC_SET);
            super.visit(node);
        }

        @Override
        public void visit(PHPVarComment node) {
            int start = node.getVariable().getStartOffset();
            int end = start + 4;
            int startTranslated = snapshot.getOriginalOffset(start);
            if (startTranslated > -1) {
                int endTranslated = startTranslated + end - start;
                highlights.put(new OffsetRange(startTranslated, endTranslated), ColoringAttributes.CUSTOM1_SET);
            }
        }

        @Override
        public void visit(StaticFieldAccess node) {
            Expression expr = node.getField().getName();
            if (expr instanceof ArrayAccess) {
                ArrayAccess arrayAccess = (ArrayAccess) expr;
                expr = arrayAccess.getName();
            }
            Collection<? extends TypeScope> resolvedTypes = ModelUtils.resolveType(model, node);
            String variableName = CodeUtils.extractVariableName(node.getField());
            boolean isDeprecated = isDeprecatedFieldAccess(resolvedTypes, variableName);
            (new FieldAccessVisitor(isDeprecated ? DEPRECATED_STATIC_FIELD_SET : ColoringAttributes.STATIC_FIELD_SET)).scan(expr);
            super.visit(node);

        }

        @Override
        public void visit(ConstantDeclaration node) {
            ASTNode parentNode = null;
            List<ASTNode> path = getPath();
            if (path != null && path.size() > 1) {
                parentNode = path.get(1);
            }
            if (parentNode instanceof ClassDeclaration || parentNode instanceof InterfaceDeclaration
                    || parentNode instanceof TraitDeclaration) {
                List<Identifier> names = node.getNames();
                if (!names.isEmpty()) {
                    for (Identifier identifier : names) {
                        addOffsetRange(identifier, createConstantDeclarationColoring(identifier));
                    }
                }
            }
            super.visit(node);
        }

        private Set<ColoringAttributes> createConstantDeclarationColoring(Identifier constantName) {
            return isDeprecatedConstantDeclaration(constantName) ? DEPRECATED_STATIC_FIELD_SET : ColoringAttributes.STATIC_FIELD_SET;
        }

        private boolean isDeprecatedConstantDeclaration(Identifier constantName) {
            boolean isDeprecated = false;
            VariableScope variableScope = model.getVariableScope(constantName.getStartOffset());
            QualifiedName typeFullyQualifiedName = VariousUtils.getFullyQualifiedName(
                    QualifiedName.create(typeDeclaration.getName()),
                    constantName.getStartOffset(),
                    variableScope);
            for (TypeConstantElement constantElement : deprecatedConstants) {
                if (constantElement.getName().equals(constantName.getName()) && constantElement.getType().getFullyQualifiedName().equals(typeFullyQualifiedName)) {
                    isDeprecated = true;
                    break;
                }
            }
            return isDeprecated;
        }

        @Override
        public void visit(StaticConstantAccess node) {
            Identifier constant = node.getConstant();
            if (constant != null) {
                Collection<? extends TypeScope> resolvedTypes = ModelUtils.resolveType(model, node);
                boolean isDeprecated = isDeprecatedConstantAccess(resolvedTypes, constant.getName());
                addOffsetRange(constant, isDeprecated ? DEPRECATED_STATIC_FIELD_SET : ColoringAttributes.STATIC_FIELD_SET);
            }
            super.visit(node);
        }

        private boolean isDeprecatedConstantAccess(Collection<? extends TypeScope> typeScopes, String constantName) {
            boolean isDeprecated = false;
            for (TypeScope typeScope : typeScopes) {
                for (ClassConstantElement constantElement : typeScope.getDeclaredConstants()) {
                    if (constantElement.getName().equals(constantName) && constantElement.isDeprecated()) {
                        isDeprecated = true;
                        break;
                    }
                }
            }
            return isDeprecated;
        }

        @Override
        public void visit(PHPDocTypeNode node) {
            if (isCancelled()) {
                return;
            }
            QualifiedName typeName = QualifiedName.create(node.getValue());
            if (unusedUses.size() > 0 && !typeName.getKind().isFullyQualified()) {
                String firstSegmentName = typeName.getSegments().getFirst();
                processFirstSegmentName(firstSegmentName);
            }
        }

        @Override
        public void visit(NamespaceName node) {
            if (isCancelled()) {
                return;
            }
            if (unusedUses.size() > 0 && !node.isGlobal()) {
                Identifier firstSegment = node.getSegments().get(0);
                String firstSegmentName = firstSegment.getName();
                processFirstSegmentName(firstSegmentName);
            }
        }

        private void processFirstSegmentName(final String firstSegmentName) {
            Set<String> namesToRemove = new HashSet<String>();
            for (String name : unusedUses.keySet()) {
                QualifiedName qualifiedUseName = QualifiedName.create(name);
                if (qualifiedUseName.getSegments().getLast().equals(firstSegmentName)) {
                    namesToRemove.add(name);
                }
            }
            for (String nameToRemove : namesToRemove) {
                unusedUses.remove(nameToRemove);
                unusedUsesOffsetRanges.remove(nameToRemove);
            }
        }

        @Override
        public void visit(UseStatement node) {
            if (isCancelled()) {
                return;
            }
            List<UseStatementPart> parts = node.getParts();
            if (parts.size() == 1) {
                UseStatementPart useStatementPart = parts.get(0);
                String correctName = getCorrectName(useStatementPart);
                unusedUses.put(correctName, new ASTNodeColoring(node, UNUSED_USES_SET));
                OffsetRange offsetRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
                unusedUsesOffsetRanges.put(correctName, new UnusedOffsetRanges(offsetRange, offsetRange));
            } else {
                processUseStatementsParts(parts);
            }
        }

        private void processUseStatementsParts(final List<UseStatementPart> parts) {
            int lastStartOffset = 0;
            for (int i = 0; i < parts.size(); i++) {
                UseStatementPart useStatementPart = parts.get(i);
                int endOffset = useStatementPart.getEndOffset();
                if (i == 0) {
                    lastStartOffset = useStatementPart.getStartOffset();
                    assert i + 1 < parts.size();
                    UseStatementPart nextPart = parts.get(i + 1);
                    endOffset = nextPart.getStartOffset();
                }
                String correctName = getCorrectName(useStatementPart);
                unusedUses.put(correctName, new ASTNodeColoring(useStatementPart, UNUSED_USES_SET));
                OffsetRange rangeToVisualise = new OffsetRange(useStatementPart.getStartOffset(), useStatementPart.getEndOffset());
                OffsetRange rangeToReplace = new OffsetRange(lastStartOffset, endOffset);
                unusedUsesOffsetRanges.put(correctName, new UnusedOffsetRanges(rangeToVisualise, rangeToReplace));
                lastStartOffset = useStatementPart.getEndOffset();
            }
        }

        private String getCorrectName(UseStatementPart useStatementPart) {
            Identifier alias = useStatementPart.getAlias();
            String identifierName;
            if (alias != null) {
                identifierName = alias.getName();
            } else {
                NamespaceName name = useStatementPart.getName();
                identifierName = CodeUtils.extractQualifiedName(name);
                if (name.isGlobal()) {
                    identifierName = NAMESPACE_SEPARATOR + identifierName;
                }
            }
            return identifierName;
        }

        private class FieldAccessVisitor extends DefaultVisitor {
            private final Set<ColoringAttributes> coloring;

            public FieldAccessVisitor(Set<ColoringAttributes> coloring) {
                this.coloring = coloring;
            }

            @Override
            public void visit(ArrayAccess node) {
                scan(node.getName());
                // don't scan(scan(node.getDimension()); issue #194535
            }

            @Override
            public void visit(Identifier identifier) {
                //remove the field, because is used
                ASTNodeColoring removed = privateFieldsUnused.remove(new UnusedIdentifier(identifier.getName(), typeDeclaration));
                if (removed != null) {
                    // if it was removed, marked as normal field
                    addOffsetRange(removed.identifier, removed.coloring);
                }
                addOffsetRange(identifier, coloring);
            }
        }

        private class UnusedIdentifier {
            private final String name;
            private final TypeDeclaration typeDeclaration;

            public UnusedIdentifier(final String name, final TypeDeclaration classDeclaration) {
                this.name = name;
                this.typeDeclaration = classDeclaration;
            }

            @Override
            public int hashCode() {
                int hash = 5;
                hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
                hash = 29 * hash + (this.typeDeclaration != null ? this.typeDeclaration.hashCode() : 0);
                return hash;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final UnusedIdentifier other = (UnusedIdentifier) obj;
                if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                    return false;
                }
                if (this.typeDeclaration != other.typeDeclaration && (this.typeDeclaration == null || !this.typeDeclaration.equals(other.typeDeclaration))) {
                    return false;
                }
                return true;
            }

        }
    }
}
