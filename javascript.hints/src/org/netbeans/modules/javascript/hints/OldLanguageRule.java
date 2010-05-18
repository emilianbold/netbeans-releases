/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import javax.swing.text.BadLocationException;
import org.mozilla.nb.javascript.Context;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.javascript.editing.AstUtilities;
import org.netbeans.modules.javascript.editing.JsParseResult;
import org.netbeans.modules.javascript.editing.SupportedBrowsers;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.netbeans.modules.javascript.hints.infrastructure.JsErrorRule;
import org.netbeans.modules.javascript.hints.infrastructure.JsRuleContext;
import org.netbeans.spi.lexer.MutableTextInput;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Try to recognize errors where you're using a language construct
 * that is not supported by the older language you are targeting.
 * Provide some information, and more importantly, a quickfix for changing
 * the language version targeted to the new language.
 *
 * @author Tor Norbye
 */
public class OldLanguageRule extends JsErrorRule {

    private static final String NEW_LANGUAGE_DESC = "1.7"; // NOI18N
    private static final int NEW_LANGUAGE_VERSION = Context.VERSION_1_7;

    @Override
    public Set<String> getCodes() {
        return Collections.singleton("msg.no.semi.stmt"); // NOI18N
    }

    @Override
    public void run(JsRuleContext context, Error error, List<Hint> result) {
        int astOffset = error.getStartPosition();
        JsParseResult info = AstUtilities.getParseResult(context.parserResult);
        int offset = LexUtilities.getLexerOffset(info, astOffset);
        if (offset == -1) {
            return;
        }

        BaseDocument doc = context.doc;
        if (offset <= doc.getLength()) {
            try {
                int rowStart = Utilities.getRowStart(doc, offset);
                int rowEnd = Utilities.getRowEnd(doc, offset);
                String line = doc.getText(rowStart, rowEnd - rowStart);
                boolean isYield = line.matches(".*\\byield\\s\\b.*"); // NOI18N
                boolean isLet = line.matches(".*\\blet\\s\\b.*"); // NOI18N
                if (isYield || isLet) {
                    // Yes, looks like a language issue

                    // Add a regular (nonfixable) error for this problem
                    // TBD - use the error's original range?
                    OffsetRange lexRange = LexUtilities.getLexerOffsets(info, new OffsetRange(error.getStartPosition(), error.getEndPosition()));
                    if (lexRange == OffsetRange.NONE) {
                        lexRange = new OffsetRange(rowStart, rowEnd);
                    }
                    Hint desc = new Hint(this, error.getDisplayName(), info.getSnapshot().getSource().getFileObject(), lexRange, Collections.<HintFix>emptyList(), 500);
                    result.add(desc);

                    OffsetRange range = new OffsetRange(rowStart, rowEnd);
                    List<HintFix> fixList = new ArrayList<HintFix>(2);
                    fixList.add(new ChangeLanguageFix(context));
                    fixList.add(new ChangeTargetFix());
                    //SupportedBrowsers browsers = SupportedBrowsers.getInstance();
                    //String oldVer = SupportedBrowsers.getLanguageVersionString(browsers.getLanguageVersion());
                    String keyword = isYield ? "yield" : "let"; // NOI18N
                    String msg = NbBundle.getMessage(OldLanguageRule.class, "OldLanguageRuleMsg", NEW_LANGUAGE_DESC, keyword);
                    desc = new Hint(this, msg, info.getSnapshot().getSource().getFileObject(), range, fixList, 500);
                    result.add(desc);

                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public boolean appliesTo(RuleContext context) {
        return SupportedBrowsers.getInstance().getLanguageVersion() < NEW_LANGUAGE_VERSION;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(OldLanguageRule.class, "OldLanguageRule");
    }

    public boolean showInTasklist() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }

    public String getId() {
        return "OldLanguageRule";
    }

    public String getDescription() {
        return NbBundle.getMessage(OldLanguageRule.class, "OldLanguageRuleDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    private static class ChangeLanguageFix implements HintFix {

        private JsRuleContext context;

        public ChangeLanguageFix(JsRuleContext context) {
            this.context = context;
        }

        public String getDescription() {
            return NbBundle.getMessage(OldLanguageRule.class, "OldLanguageRuleFix", NEW_LANGUAGE_DESC);
        }

        public void implement() throws Exception {
            SupportedBrowsers.getInstance().setLanguageVersion(NEW_LANGUAGE_VERSION);
            // Trigger reparse as well!
            // Just cause an edit on the document (using an edit list to add and remove
            // as a single undoable edit)
            EditList editList = new EditList(context.doc);
            editList.replace(0, 0, " ", false, 0); // NOI18N
            editList.replace(0, 1, null, false, 1);
            editList.apply();

            // Also need to redo the lexer
            MutableTextInput mti = (MutableTextInput) context.doc.getProperty(MutableTextInput.class);
            if (mti != null) {
                mti.tokenHierarchyControl().rebuild();
            }

        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return true;
        }
    }

    private static class ChangeTargetFix implements HintFix {
        ChangeTargetFix() {
        }

        public String getDescription() {
            return NbBundle.getMessage(OldLanguageRule.class, "ChangeJsLanguages");
        }

        public void implement() throws Exception {
            OptionsDisplayer.getDefault().open("Advanced/JsOptions"); // NOI18N
        }

        public boolean isSafe() {
            return false;
        }

        public boolean isInteractive() {
            return true;
        }
    }
}
