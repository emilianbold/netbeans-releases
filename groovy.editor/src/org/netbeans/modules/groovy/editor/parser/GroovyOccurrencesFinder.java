/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.groovy.editor.parser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OccurrencesFinder;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.groovy.editor.AstPath;
import org.netbeans.modules.groovy.editor.AstUtilities;
import org.netbeans.modules.groovy.editor.elements.AstRootElement;
import org.netbeans.modules.groovy.editor.lexer.LexUtilities;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.netbeans.modules.groovy.editor.lexer.GroovyTokenId;

/**
 * The (call-)proctocol for OccurrencesFinder is always:
 * 
 * 1.) setCaretPosition() = <number>
 * 2.) run()
 * 3.) getOccurrences()
 * 
 * @author Martin Adamek
 * @author Matthias Schmidt
 */
public class GroovyOccurrencesFinder implements OccurrencesFinder {

    private boolean cancelled;
    private int caretPosition;
    private Map<OffsetRange, ColoringAttributes> occurrences;
    private final Logger LOG = Logger.getLogger(GroovyOccurrencesFinder.class.getName());
    BaseDocument document;
    ModuleNode moduleNode = null;

    public GroovyOccurrencesFinder() {
        super();
        // LOG.setLevel(Level.FINEST);
    }

    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
        LOG.log(Level.FINEST, "getOccurrences()\n"); //NOI18N
        return occurrences;
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    public final synchronized void cancel() {
        cancelled = true;
    }

    public void run(CompilationInfo info) {
        LOG.log(Level.FINEST, "run()"); //NOI18N
        
        resume();

        if (isCancelled()) {
            return;
        }

        GroovyParserResult parseResult = (GroovyParserResult) info.getEmbeddedResult(GroovyTokenId.GROOVY_MIME_TYPE, 0);
        if (parseResult == null) {
            return;
        }

        ASTNode rootNode = AstUtilities.getRoot(info);

        if (rootNode == null) {
            return;
        }

        int astOffset = AstUtilities.getAstOffset(info, caretPosition);
        if (astOffset == -1) {
            return;
        }

        try {
            document = (BaseDocument) info.getDocument();
        } catch (IOException ioe) {
            LOG.log(Level.FINEST, "Could not get BaseDocument: {0}", ioe); //NOI18N
            return;
        }

        if (document == null) {
            LOG.log(Level.FINEST, "Could not get BaseDocument. It's null"); //NOI18N
            return;
        }

        final AstPath path = new AstPath(rootNode, astOffset, document);
        final ASTNode closest = path.leaf();

        LOG.log(Level.FINEST, "path = {0}", path); //NOI18N
        LOG.log(Level.FINEST, "closest: {0}", closest); //NOI18N

        if (closest == null) {
            return;
        }

        AstRootElement astRootElement = parseResult.getRootElement();

        if (astRootElement == null) {
            LOG.log(Level.FINEST, "astRootElement == null"); //NOI18N
            return;
        }

        moduleNode = (ModuleNode) astRootElement.getNode();

        if (moduleNode == null) {
            LOG.log(Level.FINEST, "moduleNode == null"); //NOI18N
            return;
        }

        Variable variable = null;
        ASTNode scope = null;

        if (closest instanceof VariableExpression) {
            LOG.log(Level.FINEST, "found: VariableExpression"); //NOI18N
            variable = ((VariableExpression) closest).getAccessedVariable();

        } else if (closest instanceof Variable) {
            LOG.log(Level.FINEST, "found: Variable"); //NOI18N
            variable = (Variable) closest;
        } else if (closest instanceof ConstantExpression) {

            LOG.log(Level.FINEST, "found: ConstantExpression"); //NOI18N
            Object o = ((ConstantExpression) closest).getValue();
            LOG.log(Level.FINEST, "ConstantExpression.getValue() : {0}", o); //NOI18N
            LOG.log(Level.FINEST, "ConstantExpression.getClas() : {0}", o.getClass()); //NOI18N

            if (o.getClass() == String.class) {
                ASTNode node = path.leafParent();

                if (node instanceof MethodCallExpression) {
                    MethodCallExpression callExpr = (MethodCallExpression) node;
                    LOG.log(Level.FINEST, "Method call found : {0}", callExpr.getText()); //NOI18N
                }
            }

            ClassNode cn = ((ConstantExpression) closest).getType();
            LOG.log(Level.FINEST, "ClassNode : {0}", cn); //NOI18N
        } else {
            return;
        }

        // not yet finished typing artifacts are likely getting created
        // as DynamicVariable's which implement Variable but are *no* ASTNode

        if (variable == null || !(variable instanceof ASTNode)) {
            LOG.log(Level.FINEST, "variable == null || !(variable instanceof ASTNode)");
            return;
        }

        String name = ((Variable) variable).getName();
        LOG.log(Level.FINEST, "variable.getName() = {0}", name); //NOI18N

        scope = AstUtilities.findVariableScope((Variable) variable, path, moduleNode);

        LOG.log(Level.FINEST, "scope: {0}", scope); //NOI18N
        
        Map<OffsetRange, ColoringAttributes> highlights = new HashMap<OffsetRange, ColoringAttributes>(100);

        highlightVariable((ASTNode) variable, scope, name, highlights);

        if (isCancelled()) {
            return;
        }

        if (highlights.size() > 0) {
            if (parseResult.getTranslatedSource() != null) {
                Map<OffsetRange, ColoringAttributes> translated = new HashMap<OffsetRange, ColoringAttributes>(2 * highlights.size());
                for (Map.Entry<OffsetRange, ColoringAttributes> entry : highlights.entrySet()) {
                    OffsetRange range = LexUtilities.getLexerOffsets(info, entry.getKey());
                    if (range != OffsetRange.NONE) {
                        translated.put(range, entry.getValue());
                    }
                }

                highlights = translated;
            }

            this.occurrences = highlights;
        } else {
            this.occurrences = null;
        }

    }

    /**
     * 
     * @param position
     */
    public void setCaretPosition(int position) {
        this.caretPosition = position;
        LOG.log(Level.FINEST, "\n\nsetCaretPosition() = {0}\n", position); //NOI18N
    }

    /**
     * 
     * @param node
     * @param scope
     * @param name
     */
    private void highlightVariable(ASTNode node, ASTNode scope, String name, Map<OffsetRange, ColoringAttributes> highlights) {

        LOG.log(Level.FINEST, "-----------------------------------------------"); //NOI18N
        LOG.log(Level.FINEST, "   node  = {0}", node); //NOI18N
        LOG.log(Level.FINEST, "   scope = {0}", scope); //NOI18N
        LOG.log(Level.FINEST, "   name  = {0}", name); //NOI18N

        if (scope instanceof Variable) {
            Variable variableExpression = (Variable) scope;
            if (name.equals(variableExpression.getName())) {
                OffsetRange range = AstUtilities.getRange(scope, document);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } 
        
//        else if (scope instanceof MethodNode) {
//            MethodNode methodNode = (MethodNode) scope;
//            // if selected node is from this method, we don't want to skip this method body
//            AstPath astPath = new AstPath(scope, node);
//            if (!astPath.iterator().hasNext()) {
//                for (Parameter parameter : methodNode.getParameters()) {
//                    if (name.equals(parameter.getName())) {
//                        // we don't want go into method if one of its parameters has
//                        // same name as our variable
//                        return;
//                    }
//                }
//            }
//            AstUtilities.VariableScopeVisitor visitor = new AstUtilities.VariableScopeVisitor(moduleNode, name);
//            visitor.visitMethod(methodNode);
//            if (visitor.isDeclaring()) {
//                // method shadows our variable
//                return;
//            }
//        }
        
        
        // TODO: should use visitor instead?
        List<ASTNode> list = AstUtilities.children(scope);
        for (ASTNode child : list) {
            highlightVariable(node, child, name, highlights);
        }
    }
}
