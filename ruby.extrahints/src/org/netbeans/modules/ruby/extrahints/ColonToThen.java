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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.extrahints;


import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.ast.WhenNode;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.hints.spi.AstRule;
import org.netbeans.modules.ruby.hints.spi.Description;
import org.netbeans.modules.ruby.hints.spi.EditList;
import org.netbeans.modules.ruby.hints.spi.Fix;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import org.netbeans.modules.ruby.hints.spi.PreviewableFix;
import org.netbeans.modules.ruby.hints.spi.RuleContext;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Warn Ruby 1.9 change which disallows retry outside of the rescue-portion
 * 
 * @author Tor Norbye
 */
public class ColonToThen implements AstRule {

    public Set<Integer> getKinds() {
        return Collections.singleton(NodeTypes.WHENNODE);
    }

    public void run(RuleContext context, List<Description> result) {
        Node node = context.node;
        CompilationInfo info = context.compilationInfo;

        WhenNode when = (WhenNode)node;
        Node body = when.getBodyNode();
        if (body == null) {
            return;
        }
        
        // See if the child contains
        BaseDocument doc;
        try {
            // (1) make sure the body is on the same line as the when, and
            // (2) the separator is ":", not "then" or something else
            doc = (BaseDocument) info.getDocument();
            int whenStart = when.getPosition().getStartOffset();
            int bodyStart = body.getPosition().getStartOffset();
            if (Utilities.getRowEnd(doc, bodyStart) !=
                    Utilities.getRowEnd(doc, whenStart)) {
                return;
            }

            int offset = -1;
            try {
                // Check tokens - look for ":" as opposed to then
                doc.readLock();
                TokenSequence<? extends RubyTokenId> ts = LexUtilities.getRubyTokenSequence(doc,
                        bodyStart);
                if (ts == null) {
                    return;
                }
                ts.move(bodyStart);
                while (ts.movePrevious()) {
                    Token<? extends RubyTokenId> token = ts.token();
                    TokenId id = token.id();
                    if (id == RubyTokenId.WHITESPACE) {
                        continue;
                    } else if (id == RubyTokenId.NONUNARY_OP) {
                        String s = token.text().toString();
                        if (":".equals(s)) {
                            offset = ts.offset();
                            break;
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
            } finally {
                doc.readUnlock();
            }
            
            OffsetRange range = new OffsetRange(offset, offset+1);
            String displayName = NbBundle.getMessage(ColonToThen.class, "ColonToThenGutter");
            ColonFix fix = new ColonFix(doc, offset);
            Description desc = new Description(this, displayName, info.getFileObject(), range, 
                    Collections.<Fix>singletonList(fix), 150);
            result.add(desc);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public String getId() {
        return "ColonToThen"; // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(ColonToThen.class, "ColonToThenDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    public boolean appliesTo(CompilationInfo info) {
        return true;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ColonToThen.class, "ColonToThen");
    }

    public boolean showInTasklist() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }
    
    private static class ColonFix implements PreviewableFix {
        private BaseDocument doc;
        private int offset;

        public ColonFix(BaseDocument doc, int offset) {
            this.doc = doc;
            this.offset = offset;
        }

        public String getDescription() {
            return NbBundle.getMessage(Deprecations.class, "ColonToThenFix");
        }

        public void implement() throws Exception {
            getEditList().apply();
        }

        public EditList getEditList() throws Exception {
            EditList list = new EditList(doc);
            // TODO - determine if we need whitespace around it
            String s = doc.getText(offset, 3);
            String insert;
            StringBuilder sb = new StringBuilder();
            if (!Character.isWhitespace(doc.getText(offset-1, 1).charAt(0))) {
                sb.append(' ');
            }
            sb.append("then");
            if (offset < doc.getLength()-2) {
                if (!Character.isWhitespace(doc.getText(offset+1, 1).charAt(0))) {
                    sb.append(' ');
                }
            }
            list.replace(offset, 1, sb.toString(), false, 0);
            
            return list;
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return false;
        }

        public boolean canPreview() {
            return true;
        }
    }
}
