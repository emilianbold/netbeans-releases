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

package org.netbeans.modules.ruby.hints;


import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.jruby.nb.ast.HashNode;
import org.jruby.nb.ast.ListNode;
import org.jruby.nb.ast.Node;
import org.jruby.nb.ast.NodeType;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.EditList;
import org.netbeans.modules.gsf.api.HintFix;
import org.netbeans.modules.gsf.api.HintSeverity;
import org.netbeans.modules.gsf.api.PreviewableFix;
import org.netbeans.modules.gsf.api.RuleContext;
import org.netbeans.modules.ruby.hints.infrastructure.RubyAstRule;
import org.netbeans.modules.ruby.hints.infrastructure.RubyRuleContext;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.util.NbBundle;

/**
 * Convert {a,b,c,d,...} to {a=>b,c=>d,...} as required by Ruby 1.9.
 * 
 * @author Tor Norbye
 */
public class HashListConvert extends RubyAstRule {

    public Set<NodeType> getKinds() {
        return Collections.singleton(NodeType.HASHNODE);
    }

    public void run(RubyRuleContext context, List<Hint> result) {
        Node node = context.node;
        CompilationInfo info = context.compilationInfo;

        HashNode hash = (HashNode)node;
        ListNode listNode = hash.getListNode();
        if (listNode == null) {
            return;
        }
        
        if (listNode.size() < 2) {
            return;
        }

        int commaOffset = getCommaOffset(context, listNode, 0);
        if (commaOffset == -1) {
            return;
        }

        OffsetRange range = new OffsetRange(commaOffset, commaOffset+1);
        String displayName = NbBundle.getMessage(HashListConvert.class, "HashListConvertGutter");
        List<HintFix> fixes = Collections.<HintFix>singletonList(new HashFix(context, listNode));
        Hint desc = new Hint(this, displayName, info.getFileObject(), range, 
                fixes, 140);
        result.add(desc);
    }
    
    private static int getCommaOffset(RubyRuleContext context, ListNode listNode, int pair) {
        int prevEnd = listNode.get(2*pair).getPosition().getEndOffset();
        int nextStart = listNode.get(2*pair+1).getPosition().getStartOffset();
        OffsetRange lexRange = LexUtilities.getLexerOffsets(context.compilationInfo, 
                new OffsetRange(prevEnd, nextStart));
        if (lexRange == OffsetRange.NONE) {
            return -1;
        }
        
        try {
            String s = context.doc.getText(lexRange.getStart(), lexRange.getLength());
            int index = s.indexOf(',');
            
            // TODO - look out for comments here! Use lexical tokens rather than just document text
            // (and watch out for RHTML discontiguous sections

            if (index == -1 || s.indexOf("=>") != -1) {
                return -1;
            }
            return lexRange.getStart() + index;
        } catch (BadLocationException ex) {
            return -1;
        }
    }

    public String getId() {
        return "HashListConvert"; // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(HashListConvert.class, "HashListConvertDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    public boolean appliesTo(RuleContext context) {
        return true;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(HashListConvert.class, "HashListConvert");
    }

    public boolean showInTasklist() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }
    
    private static class HashFix implements PreviewableFix {
        private RubyRuleContext context;
        private ListNode listNode;

        public HashFix(RubyRuleContext context, ListNode listNode) {
            this.context = context;
            this.listNode = listNode;
        }

        public String getDescription() {
            return NbBundle.getMessage(Deprecations.class, "HashListConvertFix");
        }

        public void implement() throws Exception {
            getEditList().apply();
        }

        public EditList getEditList() throws Exception {
            BaseDocument doc = context.doc;
            EditList list = new EditList(doc);
            for (int i = 0, n = listNode.size() / 2; i < n; i++) {
                int offset = getCommaOffset(context, listNode, i);
                if (offset == -1) {
                    continue;
                }
                String s = doc.getText(offset, 3);
                StringBuilder sb = new StringBuilder();
                if (!Character.isWhitespace(doc.getText(offset-1, 1).charAt(0))) {
                    sb.append(' ');
                }
                sb.append("=>");
                if (offset < doc.getLength()-2) {
                    if (!Character.isWhitespace(doc.getText(offset+1, 1).charAt(0))) {
                        sb.append(' ');
                    }
                }
                list.replace(offset, 1, sb.toString(), false, 0);
            }
            
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
