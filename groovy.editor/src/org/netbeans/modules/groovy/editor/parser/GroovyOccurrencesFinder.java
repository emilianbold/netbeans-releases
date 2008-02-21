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
import javax.swing.text.Document;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.netbeans.editor.BaseDocument;
import org.netbeans.fpi.gsf.ColoringAttributes;
import org.netbeans.fpi.gsf.CompilationInfo;
import org.netbeans.fpi.gsf.OccurrencesFinder;
import org.netbeans.fpi.gsf.OffsetRange;
import org.netbeans.modules.groovy.editor.AstPath;
import org.netbeans.modules.groovy.editor.AstUtilities;
import org.netbeans.modules.groovy.editor.elements.AstRootElement;
import org.netbeans.modules.groovy.editor.lexer.LexUtilities;
import org.openide.util.Exceptions;

/**
 * Warning: this is very experimental!
 * 
 * @author Martin Adamek
 */
public class GroovyOccurrencesFinder implements OccurrencesFinder {
    
    private boolean cancelled;
    private int caretPosition;
    private Map<OffsetRange, ColoringAttributes> occurrences;

    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
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
        resume();

        if (isCancelled()) {
            return;
        }

        GroovyParserResult parseResult = (GroovyParserResult)info.getEmbeddedResult("text/x-groovy", 0);
        if (parseResult == null) {
            return;
        }
        
        ASTNode root = AstUtilities.getRoot(info);

        if (root == null) {
            return;
        }

        Map<OffsetRange, ColoringAttributes> highlights =
            new HashMap<OffsetRange, ColoringAttributes>(100);

        int astOffset = AstUtilities.getAstOffset(info, caretPosition);
        if (astOffset == -1) {
            return;
        }

        Document document = null;
        try {
            document = info.getDocument();
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        AstPath path = new AstPath(root, astOffset, (BaseDocument)document);
        ASTNode closest = path.leaf();
        
//        System.out.println("### closest: " + closest);
        
        if (closest != null) {
            
            if (closest instanceof Variable) {
                String name = ((Variable)closest).getName();
                AstRootElement astRootElement = parseResult.getRootElement();
                ModuleNode moduleNode = (ModuleNode)astRootElement.getNode();
                ASTNode scope = AstUtilities.findVariableScope((Variable)closest, path, moduleNode);
                
//                System.out.println("### block: " + scope);
                
                highlightVariable(moduleNode, closest, scope, name, highlights, (BaseDocument)document);
            }
        }

        if (isCancelled()) {
            return;
        }

        if (highlights.size() > 0) {
            if (parseResult.getTranslatedSource() != null) {
                Map<OffsetRange, ColoringAttributes> translated = new HashMap<OffsetRange,ColoringAttributes>(2*highlights.size());
                for (Map.Entry<OffsetRange,ColoringAttributes> entry : highlights.entrySet()) {
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

    public void setCaretPosition(int position) {
        this.caretPosition = position;
    }
    
    private void highlightVariable(ModuleNode moduleNode, ASTNode node, ASTNode scope, String name, 
            Map<OffsetRange, ColoringAttributes> highlights, BaseDocument doc) {
        
        if (scope instanceof Variable) {
            Variable variableExpression = (Variable) scope;
            if (name.equals(variableExpression.getName())) {
                OffsetRange range = AstUtilities.getRange(scope, doc);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } else if (scope instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) scope;
            // if selected node is from this method, we don't want to skip this method body
            AstPath astPath = new AstPath(scope, node);
            if (!astPath.iterator().hasNext()) {
                for (Parameter parameter : methodNode.getParameters()) {
                    if (name.equals(parameter.getName())) {
                        // we don't want go into method if one of its parameters has
                        // same name as our variable
                        return;
                    }
                }
            }
            AstUtilities.VariableScopeVisitor visitor = new AstUtilities.VariableScopeVisitor(moduleNode, name);
            visitor.visitMethod(methodNode);
            if (visitor.isDeclaring()) {
                // method shadows our variable
                return;
            }
        }
        // TODO: should use visitor instead?
        List<ASTNode> list = AstUtilities.children(scope);
        for (ASTNode child : list) {
            highlightVariable(moduleNode, node, child, name, highlights, doc);
        }
    }

}
