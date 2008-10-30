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
package org.netbeans.modules.javascript.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.mozilla.nb.javascript.Node;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.CompilationInfo;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.PreviewableFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.javascript.editing.AstUtilities;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.netbeans.modules.javascript.hints.infrastructure.JsErrorRule;
import org.netbeans.modules.javascript.hints.infrastructure.JsRuleContext;
import org.openide.util.NbBundle;

/**
 *  Test for equality (==) mistyped as assignment (=)?
 *
 * @author Tor Norbye
 */
public class AccidentalAssignment extends JsErrorRule {

    public Set<String> getCodes() {
        return Collections.singleton("msg.equal.as.assign"); // NOI18N
    }

    public void run(JsRuleContext context, Error error, List<Hint> result) {
        CompilationInfo info = context.compilationInfo;
        BaseDocument doc = context.doc;

        Node node = (Node) error.getParameters()[0];
        int astOffset = error.getStartPosition();
        OffsetRange astRange = AstUtilities.getRange(node);
        OffsetRange range = LexUtilities.getLexerOffsets(info, astRange);
        if (range != OffsetRange.NONE) {
            range = StrictWarning.limitErrorToLine(doc, range, astOffset);

            List<HintFix> fixList = new ArrayList<HintFix>(2);
            fixList.add(new ConvertAssignmentFix(context, node, error, false));
            fixList.add(new ConvertAssignmentFix(context, node, error, true));
            Hint desc = new Hint(this, getDisplayName(), info.getFileObject(), range, fixList, 500);
            result.add(desc);
        }
    }

    public boolean appliesTo(RuleContext context) {
        return true;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(AccidentalAssignment.class, "AccidentalAssignment");
    }

    public boolean showInTasklist() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    public String getId() {
        return "AccidentalAssignment"; // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(AccidentalAssignment.class, "AccidentalAssignmentDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    private static class ConvertAssignmentFix implements PreviewableFix {

        private final JsRuleContext context;
        private final Node assignment;
        private final Error error;
        private final boolean ignore;

        public ConvertAssignmentFix(JsRuleContext context, Node assignment, Error error, boolean ignore) {
            this.context = context;
            this.assignment = assignment;
            this.error = error;
            this.ignore = ignore;
        }

        public String getDescription() {
            return ignore ? NbBundle.getMessage(AccidentalAssignment.class, "AccidentalAssignmentIgnore") : 
                NbBundle.getMessage(AccidentalAssignment.class, "AccidentalAssignmentFix");
        }

        public void implement() throws Exception {
            EditList edits = getEditList();
            if (edits != null) {
                edits.apply();
            }
        }

        public EditList getEditList() throws Exception {
            BaseDocument doc = context.doc;
            CompilationInfo info = context.compilationInfo;
            EditList list = new EditList(doc);
            OffsetRange astRange = AstUtilities.getNameRange(assignment);
            OffsetRange lexRange = LexUtilities.getLexerOffsets(info, astRange);
            if (lexRange == OffsetRange.NONE) {
                return list;
            }

            if (ignore) {
                list.replace(lexRange.getStart(), 0, "(", false, 0);
                list.replace(lexRange.getEnd(), 0, ")", false, 0);
            } else {
                int startOffset = lexRange.getStart();
                int lineEnd = Utilities.getRowEnd(doc, startOffset);
                String line = doc.getText(startOffset, lineEnd - startOffset);
                int dashIndex = line.indexOf('=');
                if (dashIndex == -1) {
                    int astOffset = error.getStartPosition();
                    int lexOffset = LexUtilities.getLexerOffset(info, astOffset);
                    if (lexOffset == -1) {
                        return list;
                    }
                    startOffset = Utilities.getRowStart(doc, lexOffset);
                    lineEnd = Utilities.getRowEnd(doc, startOffset);
                    line = doc.getText(startOffset, lineEnd - startOffset);
                    dashIndex = line.indexOf('=');
                }
                if (dashIndex != -1) {
                    list.replace(startOffset + dashIndex, 0, "=", false, 0);
                }
            }

            return list;
        }

        public boolean isSafe() {
            return false;
        }

        public boolean isInteractive() {
            return false;
        }

        public boolean canPreview() {
            return true;
        }
    }
}
