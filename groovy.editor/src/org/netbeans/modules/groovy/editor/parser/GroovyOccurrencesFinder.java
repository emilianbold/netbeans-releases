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
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.netbeans.api.gsf.ColoringAttributes;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OccurrencesFinder;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.AstPath;
import org.netbeans.modules.groovy.editor.AstUtilities;
import org.netbeans.modules.groovy.editor.lexer.LexUtilities;
import org.openide.util.Exceptions;

/**
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

        ASTNode root = AstUtilities.getRoot(info);

        if (root == null) {
            return;
        }

        Map<OffsetRange, ColoringAttributes> highlights =
            new HashMap<OffsetRange, ColoringAttributes>(100);

        GroovyParserResult rpr = (GroovyParserResult)info.getParserResult();

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
            if (closest instanceof VariableExpression) {
                String name = ((VariableExpression)closest).getName();
                ASTNode block = AstUtilities.findLocalScope(closest, path);
                try {
                    highlightLocal(block, name, highlights, document.getText(0, document.getLength() - 1));
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            }
        }

        if (isCancelled()) {
            return;
        }

        if (highlights.size() > 0) {
            if (info.getPositionManager().isTranslatingSource()) {
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
    
    private void highlightLocal(ASTNode node, String name, Map<OffsetRange, ColoringAttributes> highlights, String text) {
        if (node instanceof VariableExpression) {
            VariableExpression variableExpression = (VariableExpression) node;
            if (name.equals(variableExpression.getName())) {
                OffsetRange range = AstUtilities.getRange(node, text);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        }
        // TODO: should use visitor instead?
        List<ASTNode> list = AstUtilities.children(node);
        for (ASTNode child : list) {
            highlightLocal(child, name, highlights, text);
        }
    }

}
