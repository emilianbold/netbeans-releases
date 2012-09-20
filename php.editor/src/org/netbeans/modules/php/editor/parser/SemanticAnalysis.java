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

import java.util.*;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.parser.astnodes.*;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 *
 * @author Petr Pisl
 */
public class SemanticAnalysis extends SemanticAnalyzer {

    public static final EnumSet<ColoringAttributes> UNUSED_FIELD_SET = EnumSet.of(ColoringAttributes.UNUSED, ColoringAttributes.FIELD);
    public static final EnumSet<ColoringAttributes> UNUSED_STATIC_FIELD_SET = EnumSet.of(ColoringAttributes.UNUSED, ColoringAttributes.FIELD, ColoringAttributes.STATIC);
    public static final EnumSet<ColoringAttributes> UNUSED_METHOD_SET = EnumSet.of(ColoringAttributes.UNUSED, ColoringAttributes.METHOD);
    public static final EnumSet<ColoringAttributes> STATIC_METHOD_SET = EnumSet.of(ColoringAttributes.STATIC, ColoringAttributes.METHOD);
    public static final EnumSet<ColoringAttributes> UNUSED_STATIC_METHOD_SET = EnumSet.of(ColoringAttributes.STATIC, ColoringAttributes.METHOD, ColoringAttributes.UNUSED);
    public static final EnumSet<ColoringAttributes> UNUSED_USES_SET = EnumSet.of(ColoringAttributes.UNUSED);
    private static final String NAMESPACE_SEPARATOR = "\\"; //NOI18N

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
    public void cancel() {
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
            SemanticHighlightVisitor semanticHighlightVisitor = new SemanticHighlightVisitor(highlights, result.getSnapshot());
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
        // last visited class declaration
        private TypeDeclaration typeDeclaration;

        public SemanticHighlightVisitor(Map<OffsetRange, Set<ColoringAttributes>> highlights, Snapshot snapshot) {
            this.highlights = highlights;
            privateFieldsUnused = new HashMap<UnusedIdentifier, ASTNodeColoring>();
            unusedUses = new HashMap<String, ASTNodeColoring>();
            privateUnusedMethods = new HashMap<UnusedIdentifier, ASTNodeColoring>();
            unusedUsesOffsetRanges = new HashMap<String, UnusedOffsetRanges>();
            this.snapshot = snapshot;
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
            addOffsetRange(name, ColoringAttributes.CLASS_SET);
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
                        addOffsetRange(item.identifier, UNUSED_STATIC_FIELD_SET);
                    }
                    else {
                        addOffsetRange(item.identifier, UNUSED_FIELD_SET);
                    }

                }

                // are there unused private methods?
                for(ASTNodeColoring item : privateUnusedMethods.values()) {
                    if (item.coloring.contains(ColoringAttributes.STATIC)) {
                        addOffsetRange(item.identifier, UNUSED_STATIC_METHOD_SET);
                    }
                    else {
                        addOffsetRange(item.identifier, UNUSED_METHOD_SET);
                    }
                }
            }
        }

        @Override
        public void visit(MethodDeclaration md) {
            scan(md.getFunction().getFormalParameters());
            boolean isPrivate = Modifier.isPrivate(md.getModifier());
            EnumSet<ColoringAttributes> coloring = ColoringAttributes.METHOD_SET;

            if (Modifier.isStatic(md.getModifier())) {
                coloring = STATIC_METHOD_SET;
            }

            Identifier identifier = md.getFunction().getFunctionName();
            String name = identifier.getName();
            // don't color private magic private method. methods which start __
            if (isPrivate && name != null && !name.startsWith("__")) {
                privateUnusedMethods.put(new UnusedIdentifier(identifier.getName(), typeDeclaration), new ASTNodeColoring(identifier, coloring));
            }
            else {
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
                Variable variable = (Variable)node.getMethod().getFunctionName().getName();
                if (variable.getName() instanceof Identifier) {
                    identifier = (Identifier)variable.getName();
                }
            }
            else if (node.getMethod().getFunctionName().getName() instanceof Identifier) {
                identifier = (Identifier)node.getMethod().getFunctionName().getName();
            }

            if (identifier != null) {
                ASTNodeColoring item = privateUnusedMethods.remove(new UnusedIdentifier(identifier.getName(), typeDeclaration));
                if (item != null) {
                    addOffsetRange(item.identifier, item.coloring);
                }
            }
            super.visit(node);
        }

        @Override
        public void visit(InterfaceDeclaration node) {
            if (isCancelled()) {
                return;
            }
            Identifier name = node.getName();
            addOffsetRange(name, ColoringAttributes.CLASS_SET);
            super.visit(node);
        }

        @Override
        public void visit(TraitDeclaration node) {
            if (isCancelled()) {
                return;
            }
            Identifier name = node.getName();
            addOffsetRange(name, ColoringAttributes.CLASS_SET);
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
            EnumSet<ColoringAttributes> coloring = ColoringAttributes.FIELD_SET;

            if (Modifier.isStatic(node.getModifier())) {
                coloring = ColoringAttributes.STATIC_FIELD_SET;
            }

            Variable[] variables = node.getVariableNames();
            for (int i = 0; i < variables.length; i++) {
                Variable variable = variables[i];
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

        @Override
        public void visit(FieldAccess node) {
            if (isCancelled()) {
                return;
            }
            if (!node.getField().isDollared()) {
                Expression expr = node.getField().getName();
                (new FieldAccessVisitor(ColoringAttributes.FIELD_SET)).scan(expr);
            }
            scan(node.getField());
            super.scan(node.getDispatcher());
        }

        @Override
        public void visit(StaticMethodInvocation node) {
            if (isCancelled()) {
                return;
            }
            FunctionName fnName = node.getMethod().getFunctionName();
            if (fnName.getName() instanceof Identifier) {
                String name = ((Identifier) fnName.getName()).getName();
                ASTNodeColoring item = privateUnusedMethods.remove(new UnusedIdentifier(name, typeDeclaration));
                if (item != null) {
                    addOffsetRange(item.identifier, item.coloring);
                }
            }
            addOffsetRange(fnName, ColoringAttributes.STATIC_SET);
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
                ArrayAccess arrayAccess = (ArrayAccess)expr;
                expr = arrayAccess.getName();
            }
            (new FieldAccessVisitor(ColoringAttributes.STATIC_FIELD_SET)).scan(expr);
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
                        addOffsetRange(identifier, ColoringAttributes.STATIC_FIELD_SET);
                    }
                }
            }
            super.visit(node);
        }

        @Override
        public void visit(StaticConstantAccess node) {
            Identifier constant = node.getConstant();
            if (constant != null) {
                addOffsetRange(constant, ColoringAttributes.STATIC_FIELD_SET);
            }
            super.visit(node);
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
