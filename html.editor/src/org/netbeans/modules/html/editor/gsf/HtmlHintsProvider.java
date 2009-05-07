/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html.editor.gsf;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.Rule.ErrorRule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.api.Severity;

/**
 *
 * @author marekfukala
 */
public class HtmlHintsProvider implements HintsProvider {

    private boolean cancelled = false;

    /**
     * Compute hints applicable to the given compilation info and add to the given result list.
     */
    @Override
    public void computeHints(HintsManager manager, RuleContext context,List<Hint> hints) {
    }

    /**
     * Compute any suggestions applicable to the given caret offset, and add to
     * the given suggestion list.
     */
    @Override
    public void computeSuggestions(HintsManager manager, RuleContext context, List<Hint> suggestions, int caretOffset) {
    }

    /**
     * Compute any suggestions applicable to the given caret offset, and add to
     * the given suggestion list.
     */
    @Override
    public void computeSelectionHints(HintsManager manager, RuleContext context, List<Hint> suggestions, int start, int end) {
    }

    /**
     * Process the errors for the given compilation info, and add errors and
     * warning descriptions into the provided hint list. Return any errors
     * that were not added as error descriptions (e.g. had no applicable error rule)
     */
    @Override
    public void computeErrors(HintsManager manager, RuleContext context, List<Hint> hints, List<Error> unhandled) {
        for(Error e : context.parserResult.getDiagnostics()) {

            assert e.getDescription() != null;

            Hint h = new Hint(getRule(e.getSeverity()),
                    e.getDescription(),
                    e.getFile(),
                    new OffsetRange(e.getStartPosition(), e.getEndPosition()),
                    Collections.EMPTY_LIST,
                    20);
            
            hints.add(h);
        }
    }

    /**
     * Cancel in-progress processing of hints.
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * <p>Optional builtin Rules. Typically you don't use this; you register your rules in your filesystem
     * layer in the gsf-hints/mimetype1/mimetype2 folder, for example gsf-hints/text/x-ruby/.
     * Error hints should go in the "errors" folder, selection hints should go in the "selection" folder,
     * and all other hints should go in the "hints" folder (but note that you can create localized folders
     * and organize them under hints; these categories are shown in the hints options panel.
     * Hints returned from this method will be placed in the "general" folder.
     * </p>
     * <p>
     * This method is primarily intended for rules that should be added dynamically, for example for
     * Rules that have a many different flavors yet a single implementation class (such as
     * JavaScript's StrictWarning rule which wraps a number of builtin parser warnings.)
     *
     * @return A list of rules that are builtin, or null or an empty list when there are no builtins
     */

    @Override
    public List<Rule> getBuiltinRules() {
        return null;
    }

    /**
     * Create a RuleContext object specific to this HintsProvider. This lets implementations of
     * this interface created subclasses of the RuleContext that can be passed around to all
     * the executed rules.
     * @return A new instance of a RuleContext object
     */
    @Override
    public RuleContext createRuleContext() {
        return new RuleContext();
    }

    private static final HtmlRule ERROR_RULE = new HtmlRule(HintSeverity.ERROR);
    private static final HtmlRule WARNING_RULE = new HtmlRule(HintSeverity.WARNING);

    private static HtmlRule getRule(Severity s) {
        switch(s) {
            case WARNING:
                return WARNING_RULE;
            case ERROR:
                return ERROR_RULE;
            default:
                throw new AssertionError("Unexpected severity level"); //NOI18N
        }
    }

    private static final class HtmlRule implements ErrorRule {

        private HintSeverity severity;

        private HtmlRule(HintSeverity severity) {
            this.severity = severity;
        }

        public Set<?> getCodes() {
            return Collections.emptySet();
        }

        public boolean appliesTo(RuleContext context) {
            return true;
        }

        public String getDisplayName() {
            return "html"; //NOI18N //does this show up anywhere????
        }

        public boolean showInTasklist() {
            return true;
        }

        public HintSeverity getDefaultSeverity() {
            return severity;
        }

    }

}
