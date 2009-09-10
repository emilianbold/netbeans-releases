/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.php.editor.verification;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.Rule.AstRule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.editor.model.ModelFactory;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPHintsProvider implements HintsProvider {
    public static final String FIRST_PASS_HINTS = "1st pass"; //NOI18N
    public static final String SECOND_PASS_HINTS = "2nd pass"; //NOI18N
    public static final String DEFAULT_LINE_HINTS = "default.line.hints"; //NOI18N
    private static final Logger LOGGER = Logger.getLogger(PHPHintsProvider.class.getName());
    enum Kind {HINT, SUGGESTION, SELECTION, ERROR};

    public void computeHints(HintsManager mgr, RuleContext context, List<Hint> hints) {
        long startTime = (LOGGER.isLoggable(Level.FINE)) ? System.currentTimeMillis() : 0;
        ParserResult info = context.parserResult;

        Map<?, List<? extends Rule.AstRule>> allHints = mgr.getHints(false, context);
        List<? extends AstRule> modelHints = allHints.get(DEFAULT_LINE_HINTS);
        if (modelHints != null) {
            PHPRuleContext ruleContext = (PHPRuleContext) context;
            PHPParseResult result = (PHPParseResult) info;
            final Model model = result.getModel();
            FileScope modelScope = model.getFileScope();
            ruleContext.fileScope = modelScope;
            for (AstRule astRule : modelHints) {
                if (mgr.isEnabled(astRule)) {
                    if (astRule instanceof AbstractRule) {
                        AbstractRule icm = (AbstractRule) astRule;
                        icm.computeHintsImpl(ruleContext, hints, PHPHintsProvider.Kind.HINT);
                    }
                }
            }
        }

        computeExperimentalHints(context, allHints, mgr, hints);

        if (LOGGER.isLoggable(Level.FINE)) {
            long execTime = Calendar.getInstance().getTimeInMillis() - startTime;
            FileObject fobj = info.getSnapshot().getSource().getFileObject();
            LOGGER.fine(String.format("Computing PHP hints for %s.%s took %d ms", fobj.getName(), fobj.getExt(), execTime));
        }
    }

    public void computeSuggestions(HintsManager mgr, RuleContext context, List<Hint> suggestions, int caretOffset) {
        Map<?, List<? extends Rule.AstRule>> allHints = mgr.getHints(true, context);
        List<? extends AstRule> modelHints = allHints.get(DEFAULT_LINE_HINTS);
        if (modelHints != null) {
            PHPRuleContext ruleContext = (PHPRuleContext) context;
            ParserResult info = context.parserResult;
            if (!(info instanceof PHPParseResult)) return;
            PHPParseResult result = (PHPParseResult) info;
            final Model model = result.getModel();
            FileScope modelScope = model.getFileScope();
            ruleContext.fileScope = modelScope;
            for (AstRule astRule : modelHints) {
                if (mgr.isEnabled(astRule)) {
                    if (astRule instanceof AbstractRule) {
                        AbstractRule icm = (AbstractRule) astRule;
                        icm.computeHintsImpl(ruleContext, suggestions, PHPHintsProvider.Kind.SUGGESTION);
                    }
                }
            }
        }
    }

    public void computeSelectionHints(HintsManager manager, RuleContext context, List<Hint> suggestions, int start, int end) {
        
    }

    public void computeErrors(HintsManager manager, RuleContext context, List<Hint> hints, List<org.netbeans.modules.csl.api.Error> unhandled) {
        ParserResult parserResult = context.parserResult;
        if (parserResult != null) {
            List<? extends org.netbeans.modules.csl.api.Error> errors = parserResult.getDiagnostics();
            unhandled.addAll(errors);
        }

        FileObject fobj = NbEditorUtilities.getFileObject(context.doc);

        if (fobj != null && CheckPHPVersionVisitor.appliesTo(fobj)) {
            PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
            if (phpParseResult.getProgram() != null) {

                CheckPHPVersionVisitor visitor = new CheckPHPVersionVisitor(fobj);
                phpParseResult.getProgram().accept(visitor);
                unhandled.addAll(visitor.getErrors());
            }
        }
    }

    public void cancel() {
        
    }

    public List<Rule> getBuiltinRules() {
        return Collections.<Rule>emptyList();
    }

    public RuleContext createRuleContext() {
        return new PHPRuleContext();
    }

    private void computeExperimentalHints(RuleContext context, Map<?, List<? extends AstRule>> allHints, HintsManager mgr, List<Hint> hints) {
        Collection<PHPRule> firstPassHints = new ArrayList<PHPRule>();
        for (Object obj : allHints.get(FIRST_PASS_HINTS)) {
            if (obj instanceof PHPRule) {
                PHPRule rule = (PHPRule) obj;
                if (mgr.isEnabled(rule)) {
                    firstPassHints.add(rule);
                    if (rule instanceof PHPRuleWithPreferences) {
                        PHPRuleWithPreferences ruleWithPrefs = (PHPRuleWithPreferences) rule;
                        ruleWithPrefs.setPreferences(mgr.getPreferences(rule));
                    }
                }
            }
        }
        // A temp workaround for performance problems with hints accessing the VarStack.
        boolean maintainVarStack = false;
        for (List<? extends Rule.AstRule> list : allHints.values()) {
            for (Rule.AstRule obj : list) {
                if (obj instanceof VarStackReadingRule) {
                    VarStackReadingRule rule = (VarStackReadingRule) obj;
                    if (mgr.isEnabled(rule)) {
                        maintainVarStack = true;
                        LOGGER.fine(rule.getClass().getName() + " is enabled, turning on the VarStack");
                        break;
                    }
                }
            }
        }
        // end of the workaround
        PHPVerificationVisitor visitor = new PHPVerificationVisitor((PHPRuleContext) context, firstPassHints, maintainVarStack);
        @SuppressWarnings(value = "unchecked")
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() != null) {
            phpParseResult.getProgram().accept(visitor);
        }
        hints.addAll(visitor.getResult());
        List<? extends Rule.AstRule> secondPass = allHints.get(SECOND_PASS_HINTS);
        if (secondPass != null && secondPass.size() > 0) {
            assert secondPass.size() == 1;
            UnusedVariableRule unusedVariableRule = (UnusedVariableRule) secondPass.get(0);
            if (mgr.isEnabled(unusedVariableRule)) {
                unusedVariableRule.check((PHPRuleContext) context, hints);
            }
        }
    }
}
