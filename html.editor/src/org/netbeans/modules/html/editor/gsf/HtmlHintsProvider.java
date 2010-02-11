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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.SyntaxTree;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.Rule.ErrorRule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.spi.lexer.MutableTextInput;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public class HtmlHintsProvider implements HintsProvider {

    /**
     * Compute hints applicable to the given compilation info and add to the given result list.
     */
    @Override
    public void computeHints(HintsManager manager, RuleContext context, List<Hint> hints) {
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
        //html extensions
        for(HtmlExtension ext : HtmlExtension.getRegisteredExtensions(context.parserResult.getSnapshot().getSource().getMimeType())) {
            ext.computeSelectionHints(manager, context, suggestions, start, end);
        }
    }

    /**
     * Process the errors for the given compilation info, and add errors and
     * warning descriptions into the provided hint list. Return any errors
     * that were not added as error descriptions (e.g. had no applicable error rule)
     */
    @Override
    public void computeErrors(HintsManager manager, RuleContext context, List<Hint> hints, List<Error> unhandled) {
        Snapshot snapshot = context.parserResult.getSnapshot();
        FileObject fo = snapshot.getSource().getFileObject();
        if (isErrorCheckingEnabled(fo)) {
            for (Error e : context.parserResult.getDiagnostics()) {
                    assert e.getDescription() != null;
                    HintFix disableChecksFix = new DisableErrorChecksFix(snapshot);

                    //tweak the error position if close to embedding boundary
                    int from = e.getStartPosition();
                    int to = e.getEndPosition();

                    if (from == -1 && to == -1) {
                        //completely unknown position, give up
                        continue;
                    } else if (from == -1 && to != -1) {
                        from = to;
                    } else if (from != -1 && to == -1) {
                        to = from;
                    }

                    List<HintFix> fixes = new ArrayList<HintFix>(3);
                    //add custom hint fixes
                    fixes.addAll(getCustomHintFixesForError(context, e));
                    
                    //add default disable hints fix
                    fixes.add(disableChecksFix);

                    Hint h = new Hint(getRule(e.getSeverity()),
                            e.getDescription(),
                            e.getFile(),
                            new OffsetRange(from, to),
                            fixes,
                            20);

                    hints.add(h);
            }
        } else {
            //add a special hint for reenabling disabled error checks
            HintFix fix = new EnableErrorChecksFix(snapshot);
            Hint h = new Hint(new HtmlRule(HintSeverity.WARNING, false),
                    NbBundle.getMessage(HtmlHintsProvider.class, "MSG_HINT_ENABLE_ERROR_CHECKS_FILE_DESCR"), //NOI18N
                    fo,
                    new OffsetRange(0, 0),
                    Collections.singletonList(fix),
                    50);

            hints.add(h);
        }

        //html extensions
        for(HtmlExtension ext : HtmlExtension.getRegisteredExtensions(context.parserResult.getSnapshot().getSource().getMimeType())) {
            ext.computeErrors(manager, context, hints, unhandled);
        }

    }

    private static Collection<HintFix> getCustomHintFixesForError(final RuleContext context, final Error e) {
        List<HintFix> fixes = new ArrayList<HintFix>();
        if(e.getKey().equals(SyntaxTree.MISSING_REQUIRED_ATTRIBUTES)) {
            fixes.add(new HintFix() {
                
                @Override
                public String getDescription() {
                    return NbBundle.getMessage(HtmlHintsProvider.class, "MSG_HINT_GENERATE_REQUIRED_ATTRIBUTES"); //NOI18N
                }

                @Override
                public void implement() throws Exception {
                    AstNode node = HtmlParserResult.getBoundAstNode(e);
                    Collection<String> missingAttrs = (Collection<String>)node.getProperty(SyntaxTree.MISSING_REQUIRED_ATTRIBUTES);
                    assert missingAttrs != null;
                    int astOffset = node.startOffset() + 1 + node.name().length();
                    int insertOffset = context.parserResult.getSnapshot().getOriginalOffset(astOffset);
                    if(insertOffset == -1) {
                        return ;
                    }
                    StringBuffer templateText = new StringBuffer();
                    templateText.append(' ');

                    for(String attr : missingAttrs) {
                        templateText.append(attr);
                        templateText.append('=');
                        templateText.append('"');
                        templateText.append("${");
                        templateText.append(attr);
                        templateText.append(" default=\"\"}"); //NOI18N
                        templateText.append('"');
                        templateText.append(' ');
                    }
                    templateText.append("${cursor}"); //NOI18N

                    CodeTemplate ct = CodeTemplateManager.get(context.doc).createTemporary(templateText.toString());
                    JTextComponent pane = EditorRegistry.focusedComponent();
                    if(pane != null) {
                        pane.setCaretPosition(insertOffset);
                        ct.insert(pane);
                    }

                    //reformat the line?

                }

                @Override
                public boolean isSafe() {
                    return true;
                }

                @Override
                public boolean isInteractive() {
                    return false;
                }
                
            });

        } else {
            fixes = Collections.emptyList();
        }

        return fixes;
    }

    /**
     * Cancel in-progress processing of hints.
     */
    @Override
    public void cancel() {
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
    private static final HtmlRule ERROR_RULE = new HtmlRule(HintSeverity.ERROR, true);
    private static final HtmlRule WARNING_RULE = new HtmlRule(HintSeverity.WARNING, true);

    private static HtmlRule getRule(Severity s) {
        switch (s) {
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
        private boolean showInTasklist;

        private HtmlRule(HintSeverity severity, boolean showInTaskList) {
            this.severity = severity;
            this.showInTasklist = showInTaskList;
        }

        @Override
        public Set<?> getCodes() {
            return Collections.emptySet();
        }

        @Override
        public boolean appliesTo(RuleContext context) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "html"; //NOI18N //does this show up anywhere????
        }

        @Override
        public boolean showInTasklist() {
            return showInTasklist;
        }

        @Override
        public HintSeverity getDefaultSeverity() {
            return severity;
        }
    }
    static final String DISABLE_ERROR_CHECKS_KEY = "disable_error_checking"; //NOI18N

    public static boolean isErrorCheckingEnabled(FileObject fo) {
        return fo.getAttribute(DISABLE_ERROR_CHECKS_KEY) == null;
    }

    private static final class DisableErrorChecksFix implements HintFix {

        private static String VALUE = "true"; //NOI18N
        private Snapshot snapshot;

        public DisableErrorChecksFix(Snapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(HtmlHintsProvider.class, "MSG_HINT_DISABLE_ERROR_CHECKS_FILE"); //NOI18N
        }

        @Override
        public void implement() throws Exception {
            FileObject fo = snapshot.getSource().getFileObject();
            if (fo != null) {
                fo.setAttribute(DISABLE_ERROR_CHECKS_KEY, VALUE);
            }

            //force reparse => hints update
            Document doc = snapshot.getSource().getDocument(false);
            if (doc != null) {
                forceReparse(doc);
            }
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
    }

    private static final class EnableErrorChecksFix implements HintFix {

        private Snapshot snapshot;

        public EnableErrorChecksFix(Snapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(HtmlHintsProvider.class, "MSG_HINT_ENABLE_ERROR_CHECKS_FILE"); //NOI18N
        }

        @Override
        public void implement() throws Exception {
            FileObject fo = snapshot.getSource().getFileObject();
            if (fo != null) {
                fo.setAttribute(DISABLE_ERROR_CHECKS_KEY, null);
            }

            //force reparse => hints update
            Document doc = snapshot.getSource().getDocument(false);
            if (doc != null) {
                forceReparse(doc);
            }
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }
    }

    private static void forceReparse(final Document doc) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                NbEditorDocument nbdoc = (NbEditorDocument) doc;
                nbdoc.runAtomic(new Runnable() {

                    @Override
                    public void run() {
                        MutableTextInput mti = (MutableTextInput) doc.getProperty(MutableTextInput.class);
                        if (mti != null) {
                            mti.tokenHierarchyControl().rebuild();
                        }
                    }
                });
            }
        });
    }
}
