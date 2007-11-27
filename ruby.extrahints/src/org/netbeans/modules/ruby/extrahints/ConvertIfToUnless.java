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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.extrahints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.jruby.ast.IfNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.hints.spi.AstRule;
import org.netbeans.modules.ruby.hints.spi.Description;
import org.netbeans.modules.ruby.hints.spi.Fix;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Convert conditions of the form "if !foo" to "unless foo".
 * 
 * Inspired by the excellent blog entry
 *   http://langexplr.blogspot.com/2007/11/creating-netbeans-ruby-hints-with-scala_24.html
 * by Luis Diego Fallas.
 * 
 * @todo Check ?: nodes
 * 
 * @author Tor Norbye
 */
public class ConvertIfToUnless implements AstRule {

    public Set<Integer> getKinds() {
        return Collections.singleton(NodeTypes.IFNODE);
    }

    public void run(CompilationInfo info, Node node, AstPath path, int caretOffset,
                     List<Description> result) {
        IfNode ifNode = (IfNode) node;

        // Convert unless blocks?
        Node condition = ifNode.getCondition();
        if (condition.nodeId == NodeTypes.NOTNODE ||
                (condition.nodeId == NodeTypes.NEWLINENODE &&
                condition.childNodes().size() == 1 &&
                ((Node)condition.childNodes().get(0)).nodeId == NodeTypes.NOTNODE)) {
            try {
                BaseDocument doc = (BaseDocument) info.getDocument();
                int keywordOffset = findKeywordOffset(info, ifNode);
                if (keywordOffset == -1 || keywordOffset > doc.getLength() - 1) {
                    return;
                }

                OffsetRange range = AstUtilities.getRange(node);

                Fix fix = new ConvertToUnlessFix(info, ifNode);
                List<Fix> fixes = Collections.singletonList(fix);

                String displayName = NbBundle.getMessage(ConvertIfToUnless.class,
                        "ConvertIfToUnless");
                Description desc = new Description(this, displayName, info.getFileObject(), range,
                        fixes, 500);
                result.add(desc);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public String getId() {
        return "ConvertIfToUnless"; // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(ConvertIfToUnless.class, "ConvertIfToUnlessDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    public boolean appliesTo(CompilationInfo info) {
        // Skip for RHTML files for now - isn't implemented properly
        return info.getFileObject().getMIMEType().equals("text/x-ruby");
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ConvertIfToUnless.class, "ConvertIfToUnless");
    }

    public boolean showInTasklist() {
        return false;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.CURRENT_LINE_WARNING;
    }
    
    static int findKeywordOffset(CompilationInfo info, IfNode ifNode) throws IOException, BadLocationException {
        BaseDocument doc = (BaseDocument) info.getDocument();

        int astIfOffset = ifNode.getPosition().getStartOffset();
        int lexIfOffset = LexUtilities.getLexerOffset(info, astIfOffset);
        if (lexIfOffset == -1 || lexIfOffset > doc.getLength()) {
            return -1;
        }

        String statement = doc.getText(lexIfOffset, 2);
        if (statement.equals("if") || statement.equals("un")) {
            return lexIfOffset;
        } else {
            // Probably a statement modifier - gotta adjust the if offset
            int conditionStart = LexUtilities.getLexerOffset(info, AstUtilities.getRange(ifNode.getCondition()).getStart());
            int lineStart = Utilities.getRowFirstNonWhite(doc, conditionStart);
            if (lineStart != -1 && lineStart < conditionStart) {
                String line = doc.getText(lineStart, conditionStart-lineStart).trim();
                if (line.endsWith("if")) { // NOI18N
                    return lineStart + line.length() - 2;
                } else if (line.endsWith("unless")) { // NOI18N
                    return lineStart + line.length() - 6;
                }
            }
        }
        
        return -1;
    }
    
    private class ConvertToUnlessFix implements Fix {
        private CompilationInfo info;
        private IfNode ifNode;

        public ConvertToUnlessFix(CompilationInfo info, IfNode ifNode) {
            this.info = info;
            this.ifNode = ifNode;
        }

        public String getDescription() {
            String lif = "if"; // NOI18N
            String lunless = "unless"; // NOI18N
            String from;
            String to;
            if (ifNode.getThenBody() != null) {
                from = lif;
                to = lunless;
            } else {
                from = lunless;
                to = lif;
            }
            return NbBundle.getMessage(ConvertIfToUnless.class, "ConvertIfToUnlessFix", from, to);
        }

        public void implement() throws Exception {
            BaseDocument doc = (BaseDocument) info.getDocument();
            
            Node notNode = ifNode.getCondition();
            if (notNode.nodeId != NodeTypes.NOTNODE) {
                assert notNode.nodeId == NodeTypes.NEWLINENODE;
                Node firstChild = notNode.childNodes().size() == 1 ?
                    ((Node)notNode.childNodes().get(0)) : null;
                if (firstChild != null && firstChild.nodeId == NodeTypes.NOTNODE) {
                    notNode = firstChild;
                } else {
                    // Unexpected!
                    assert false : firstChild;
                    return;
                }

            }
            
            int astNotOffset = AstUtilities.getRange(notNode).getStart();
            int lexNotOffset = LexUtilities.getLexerOffset(info, astNotOffset);
            if (lexNotOffset == -1 || lexNotOffset > doc.getLength()-1) {
                return;
            }

            int astIfOffset = ifNode.getPosition().getStartOffset();
            int lexIfOffset = LexUtilities.getLexerOffset(info, astIfOffset);
            if (lexIfOffset == -1 || lexIfOffset > doc.getLength()) {
                return;
            }
            
            boolean isEqualComparison = false;
            char c = doc.getText(lexNotOffset, 1).charAt(0);
            if (c != '!') {
                // Probably something like "!=", where the not node range points to
                // a call node calling method "=="
                int lineEnd = Utilities.getRowEnd(doc, lexNotOffset);
                String line = doc.getText(lexNotOffset, lineEnd-lexNotOffset);
                int lineOffset = line.indexOf("!=");
                if (lineOffset == -1) {
                    assert false : line;
                    return;
                } else {
                    lexNotOffset += lineOffset;
                    isEqualComparison = true;
                }
            }
            
            int keywordOffset = findKeywordOffset(info, ifNode);
            if (keywordOffset == -1 || keywordOffset > doc.getLength()-1) {
                return;
            }

            assert keywordOffset < lexNotOffset;
            
            char k = doc.getText(keywordOffset, 1).charAt(0);
            boolean isIf = k == 'i';
            
            try {
                doc.atomicLock();
                if (isEqualComparison) {
                    // Convert != into ==
                    doc.replace(lexNotOffset, 1, "=", null);
                } else {
                    // Just remove ! from the expression
                    doc.remove(lexNotOffset, 1);
                }
                if (isIf) {
                    doc.replace(keywordOffset, 2, "unless", null);
                } else {
                    doc.replace(keywordOffset, 6, "if", null);
                }
            } finally {
                doc.atomicUnlock();
            }
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return false;
        }
    }
}
