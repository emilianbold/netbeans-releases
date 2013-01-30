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

package org.netbeans.modules.php.editor.verification;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.Rule.AstRule;
import org.netbeans.modules.csl.api.Rule.ErrorRule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.parser.PHPParseResult;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPHintsProvider implements HintsProvider {
    public static final String DEFAULT_HINTS = "default.hints"; //NOI18N
    public static final String DEFAULT_SUGGESTIONS = "default.suggestions"; //NOI18N

    enum ErrorType {
        UNHANDLED_ERRORS,
        HINT_ERRORS
    }

    @Override
    public void computeHints(HintsManager mgr, RuleContext context, List<Hint> hints) {
        ParserResult info = context.parserResult;
        Map<?, List<? extends Rule.AstRule>> allHints = mgr.getHints(false, context);
        List<? extends AstRule> modelHints = allHints.get(DEFAULT_HINTS);
        if (modelHints != null) {
            assert (context instanceof PHPRuleContext);
            PHPRuleContext ruleContext = (PHPRuleContext) context;
            PHPParseResult result = (PHPParseResult) info;
            final Model model = result.getModel();
            FileScope modelScope = model.getFileScope();
            ruleContext.fileScope = modelScope;
            for (AstRule astRule : modelHints) {
                if (mgr.isEnabled(astRule)) {
                    if (astRule instanceof PHPRuleWithPreferences) {
                        PHPRuleWithPreferences icm = (PHPRuleWithPreferences) astRule;
                        icm.setPreferences(mgr.getPreferences(astRule));
                    }
                    if (astRule instanceof AbstractHint) {
                        AbstractHint icm = (AbstractHint) astRule;
                        icm.compute(ruleContext, hints);
                    }
                }
            }
        }
    }

    @Override
    public void computeSuggestions(HintsManager mgr, RuleContext context, List<Hint> suggestions, int caretOffset) {
        Map<?, List<? extends Rule.AstRule>> allHints = mgr.getSuggestions();
        List<? extends AstRule> modelHints = allHints.get(DEFAULT_SUGGESTIONS);
        if (modelHints != null) {
            assert (context instanceof PHPRuleContext);
            PHPRuleContext ruleContext = (PHPRuleContext) context;
            ParserResult info = context.parserResult;
            if (!(info instanceof PHPParseResult)) {
                return;
            }
            PHPParseResult result = (PHPParseResult) info;
            final Model model = result.getModel();
            FileScope modelScope = model.getFileScope();
            ruleContext.fileScope = modelScope;
            for (AstRule astRule : modelHints) {
                if (mgr.isEnabled(astRule)) {
                    if (astRule instanceof AbstractSuggestion) {
                        AbstractSuggestion suggestion = (AbstractSuggestion) astRule;
                        try {
                            suggestion.compute(ruleContext, suggestions, caretOffset);
                        } catch (BadLocationException ex) {
                            return; // #172881
                        }
                    }
                }
            }
        }
    }

    @Override
    public void computeSelectionHints(HintsManager manager, RuleContext context, List<Hint> selections, int start, int end) {
    }

    @Override
    public void computeErrors(HintsManager manager, RuleContext context, List<Hint> hints, List<Error> unhandled) {
        ParserResult parserResult = context.parserResult;
        if (parserResult != null) {
            List<? extends Error> errors = parserResult.getDiagnostics();
            unhandled.addAll(errors);
        }
        Map<?, List<? extends ErrorRule>> allErrors = manager.getErrors();
        if (allErrors != null) {
            List<? extends ErrorRule> unhandledErrors = allErrors.get(ErrorType.UNHANDLED_ERRORS);
            if (unhandledErrors != null) {
                PHPRuleContext phpRuleContext = initializeContext(context);
                for (ErrorRule errorRule : unhandledErrors) {
                    if (errorRule instanceof AbstractUnhandledError) {
                        AbstractUnhandledError abstractUnhandledError = (AbstractUnhandledError) errorRule;
                        abstractUnhandledError.compute(phpRuleContext, unhandled);
                    }
                }
            }
            List<? extends ErrorRule> hintErrors = allErrors.get(ErrorType.HINT_ERRORS);
            if (hintErrors != null) {
                PHPRuleContext phpRuleContext = initializeContext(context);
                for (ErrorRule errorRule : hintErrors) {
                    if (errorRule instanceof AbstractHintError) {
                        AbstractHintError abstractHintError = (AbstractHintError) errorRule;
                        abstractHintError.compute(phpRuleContext, hints);
                    }
                }
            }
        }
    }

    private PHPRuleContext initializeContext(RuleContext context) {
        PHPRuleContext phpRuleContext = (PHPRuleContext) context;
        ParserResult info = context.parserResult;
        PHPParseResult result = (PHPParseResult) info;
        final Model model = result.getModel();
        FileScope modelScope = model.getFileScope();
        phpRuleContext.fileScope = modelScope;
        return phpRuleContext;
    }

    @Override
    public void cancel() {
    }

    @Override
    public List<Rule> getBuiltinRules() {
        return Collections.<Rule>emptyList();
    }

    @Override
    public RuleContext createRuleContext() {
        return new PHPRuleContext();
    }

}
