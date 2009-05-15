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
package org.netbeans.modules.php.editor.parser;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.php.editor.parser.astnodes.*;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
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

    private boolean cancelled;
    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;

    public SemanticAnalysis() {
        semanticHighlights = null;
    }

    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
    }

    public void cancel() {
        cancelled = true;
    }

    public void run(Result r, SchedulerEvent event) {
        resume();

        if (isCancelled()) {
            return;
        }

        PHPParseResult result = (PHPParseResult) r;
        Map<OffsetRange, Set<ColoringAttributes>> highlights =
                new HashMap<OffsetRange, Set<ColoringAttributes>>(100);

        if (result.getProgram() != null) {
            result.getProgram().accept(new SemanticHighlightVisitor(highlights, result.getSnapshot()));

            if (highlights.size() > 0) {
                semanticHighlights = highlights;
            } else {
                semanticHighlights = null;
            }
        }
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    public @Override int getPriority() {
        return 0;
    }

    public @Override Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    private class SemanticHighlightVisitor extends DefaultVisitor {

        private class IdentifierColoring {
            public Identifier identifier;
            public Set<ColoringAttributes> coloring;

            public IdentifierColoring(Identifier identifier, Set<ColoringAttributes> coloring) {
                this.identifier = identifier;
                this.coloring = coloring;
            }
        }

        Map<OffsetRange, Set<ColoringAttributes>> highlights;        
        // for unused private fields: name, varible
        // if isused, then it's deleted from the list and marked as the field
        private final Map<String, IdentifierColoring> privateFieldsUsed;
        // for unsed private method: name, identifier
        private final Map<String, IdentifierColoring> privateMethod;
        // this is holder of blocks, which has to be scanned for usages in the class.
        private List<Block> needToScan = new ArrayList<Block>();

        private final Snapshot snapshot;

        public SemanticHighlightVisitor(Map<OffsetRange, Set<ColoringAttributes>> highlights, Snapshot snapshot) {
            this.highlights = highlights;
            privateFieldsUsed = new HashMap<String, IdentifierColoring>();
            privateMethod = new HashMap<String, IdentifierColoring>();
            this.snapshot = snapshot;
        }

        private void addOffsetRange(ASTNode node, Set<ColoringAttributes> coloring) {
            int start = snapshot.getOriginalOffset(node.getStartOffset());
            if (start > -1) {
                int end = start + node.getEndOffset() - node.getStartOffset();
                highlights.put(new OffsetRange(start, end), coloring);
            }
        }

        @Override
        public void visit(Program program) {
            scan(program.getStatements());
            for (Comment comment : program.getComments()) {
                if (comment.getCommentType() == Comment.Type.TYPE_VARTYPE) {
                    scan(comment);
                }
            }
        }

        @Override
        public void visit(ClassDeclaration cldec) {
            if (isCancelled()) {
                return;
            }
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
                for (IdentifierColoring item : privateFieldsUsed.values()) {
                    if (item.coloring.contains(ColoringAttributes.STATIC)) {
                        addOffsetRange(item.identifier, UNUSED_STATIC_FIELD_SET);
                    }
                    else {
                        addOffsetRange(item.identifier, UNUSED_FIELD_SET);
                    }

                }

                // are there unused private methods?
                for(IdentifierColoring item : privateMethod.values()) {
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
            boolean isPrivate = Modifier.isPrivate(md.getModifier());
            EnumSet<ColoringAttributes> coloring = ColoringAttributes.METHOD_SET;

            if (Modifier.isStatic(md.getModifier())) {
                coloring = STATIC_METHOD_SET;
            }

            Identifier identifier = md.getFunction().getFunctionName();
            String name = identifier.getName();
            // don't color private magic private method. methods which start __
            if (isPrivate && name != null && !name.startsWith("__")) {
                privateMethod.put(identifier.getName(), new IdentifierColoring(identifier, coloring));
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
                IdentifierColoring item = privateMethod.remove(identifier.getName());
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
            node.getBody().accept(this);
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
                        privateFieldsUsed.put(identifier.getName(), new IdentifierColoring(identifier, coloring));
                    }
                }
            }
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
                IdentifierColoring item = privateMethod.remove(name);
                if (item != null) {
                    addOffsetRange(item.identifier, item.coloring);
                    return;
                }
            }
            addOffsetRange(fnName, ColoringAttributes.STATIC_SET);
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

        }

        private class FieldAccessVisitor extends DefaultVisitor {
            private final Set<ColoringAttributes> coloring;

            public FieldAccessVisitor(Set<ColoringAttributes> coloring) {
                this.coloring = coloring;
            }

            @Override
            public void visit(Identifier identifier) {
                //remove the field, because is used
                IdentifierColoring removed = privateFieldsUsed.remove(identifier.getName());
                if (removed != null) {
                    // if it was removed, marked as normal field
                    addOffsetRange(removed.identifier, removed.coloring);
                }
                addOffsetRange(identifier, coloring);
            }
        }
    }
}
