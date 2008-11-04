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
package org.netbeans.modules.groovy.editor.api.parser;

import java.util.HashMap;
import java.util.Map;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OccurrencesFinder;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.AstUtilities;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.groovy.editor.api.AstUtilities.FakeASTNode;
import org.netbeans.modules.groovy.editor.api.VariableScopeVisitor;
import org.openide.filesystems.FileObject;

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
    private FileObject file;
    private final Logger LOG = Logger.getLogger(GroovyOccurrencesFinder.class.getName());

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

        FileObject currentFile = info.getFileObject();
        if (currentFile != file) {
            // Ensure that we don't reuse results from a different file
            occurrences = null;
            file = currentFile;
        }

        ModuleNode rootNode = AstUtilities.getRoot(info);
        if (rootNode == null) {
            return;
        }
        int astOffset = AstUtilities.getAstOffset(info, caretPosition);
        if (astOffset == -1) {
            return;
        }
        BaseDocument document = (BaseDocument) info.getDocument();
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

        Map<OffsetRange, ColoringAttributes> highlights = new HashMap<OffsetRange, ColoringAttributes>(100);
        highlight(path, highlights, document, caretPosition);

        if (isCancelled()) {
            return;
        }

        if (highlights.size() > 0) {
            Map<OffsetRange, ColoringAttributes> translated = new HashMap<OffsetRange, ColoringAttributes>(2 * highlights.size());
            for (Map.Entry<OffsetRange, ColoringAttributes> entry : highlights.entrySet()) {
                OffsetRange range = LexUtilities.getLexerOffsets(info, entry.getKey());
                if (range != OffsetRange.NONE) {
                    translated.put(range, entry.getValue());
                }
            }
            highlights = translated;
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

    private static void highlight(AstPath path, Map<OffsetRange, ColoringAttributes> highlights, BaseDocument document, int cursorOffset) {
        ASTNode root = path.root();
        assert root instanceof ModuleNode;
        ModuleNode moduleNode = (ModuleNode) root;
        VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(moduleNode.getContext(), path, document, cursorOffset);
        scopeVisitor.collect();
        for (ASTNode astNode : scopeVisitor.getOccurrences()) {
            OffsetRange range;
            if (astNode instanceof FakeASTNode) {
                String text = astNode.getText();
                ASTNode orig = ((FakeASTNode) astNode).getOriginalNode();
                int line = orig.getLineNumber();
                int column = orig.getColumnNumber();
                if (line > 0 && column > 0) {
                    int start = AstUtilities.getOffset(document, line, column);
                    range = AstUtilities.getNextIdentifierByName(document, text, start);
                } else {
                    range = OffsetRange.NONE;
                }
            } else {
                range = AstUtilities.getRange(astNode, document);
            }
            if (range != OffsetRange.NONE) {
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        }
    }

}
