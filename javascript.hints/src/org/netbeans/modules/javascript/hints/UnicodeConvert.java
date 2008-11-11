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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.modules.csl.api.CompilationInfo;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.PreviewableFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.javascript.editing.AstUtilities;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.netbeans.modules.javascript.hints.infrastructure.JsAstRule;
import org.netbeans.modules.javascript.hints.infrastructure.JsRuleContext;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * JavaScript can contain unicode, but not all browsers support it.
 * Identify these, and offer to convert!
 * 
 * @author Tor Norbye
 */
public class UnicodeConvert extends JsAstRule {
    public UnicodeConvert() {
    }

    public boolean appliesTo(RuleContext context) {
        return true;
    }

    public Set<Integer> getKinds() {
        return Collections.singleton(Token.STRING);
    }
    
    public void run(JsRuleContext context, List<Hint> result) {
        CompilationInfo info = context.compilationInfo;
        Node node = context.node;
        
        String s = node.getString();
        for (int i = 0, n = s.length(); i < n; i++) {
            char c = s.charAt(i);
            if ((int)c >= 256) {
                OffsetRange astRange = AstUtilities.getRange(info, node);
                int lexOffset = LexUtilities.getLexerOffset(info, astRange.getStart());
                if (lexOffset == -1) {
                    return;
                }
                try {
                    BaseDocument doc = context.doc;
                    lexOffset++; // Skip the "
                    lexOffset += i; // Jump to the bad character
                    if (lexOffset < doc.getLength()) {
                        char d = doc.getText(lexOffset, 1).charAt(0);
                        if (d != c) {
                            // Didn't find the actual unicode char there;
                            // it's probably already in \\u form
                            return;
                        }
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
                OffsetRange range = new OffsetRange(lexOffset, lexOffset+1);
                List<HintFix> fixList = new ArrayList<HintFix>();
                fixList.add(new ConvertFix(context, lexOffset, c));
                fixList.add(new MoreInfoFix("unicodeconvert")); // NOI18N
                String displayName = getDisplayName();
                Hint desc = new Hint(this, displayName, info.getFileObject(), range, fixList, 1500);
                result.add(desc);
            }
        }
    }

    public String getId() {
        return "UnicodeConvert"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(UnicodeConvert.class, "UnicodeConvert");
    }

    public String getDescription() {
        return NbBundle.getMessage(UnicodeConvert.class, "UnicodeConvertDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public boolean showInTasklist() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }
    
    private static class ConvertFix implements PreviewableFix {

        private final JsRuleContext context;
        private final int lexOffset;
        private final char c;

        ConvertFix(JsRuleContext context, int offset, char c) {
            this.context = context;
            this.lexOffset = offset;
            this.c = c;
        }
        
        private String getConverted() {
            StringBuilder sb = new StringBuilder();
            java.util.Formatter formatter = new java.util.Formatter(sb);
            formatter.format("\\u%04x", (int)c);
            return sb.toString();
        }

        public String getDescription() {
            String converted = getConverted();
            return NbBundle.getMessage(UnicodeConvert.class, "UnicodeConvertFix", c, converted);
        }
        
        public boolean canPreview() {
            return true;
        }

        public EditList getEditList() throws Exception {
            BaseDocument doc = context.doc;
            EditList edits = new EditList(doc);
            edits.replace(lexOffset, 1, getConverted(), false, 0);
            
            return edits;
        }
        
        public void implement() throws Exception {
            EditList edits = getEditList();
            edits.apply();
        }

        public boolean isSafe() {
            return false;
        }

        public boolean isInteractive() {
            return false;
        }
    }
}
