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

package org.netbeans.modules.groovy.editor.hints.infrastructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.AstUtilities;
import org.netbeans.modules.groovy.editor.GroovyCompilerErrorID;
import org.netbeans.modules.groovy.editor.NodeType;
import org.netbeans.modules.groovy.editor.hints.spi.AstRule;
import org.netbeans.modules.groovy.editor.hints.spi.Description;
import org.netbeans.modules.groovy.editor.hints.spi.ErrorRule;
import org.netbeans.modules.groovy.editor.hints.spi.HintSeverity;
import org.netbeans.modules.groovy.editor.hints.spi.PreviewableFix;
import org.netbeans.modules.groovy.editor.hints.spi.Rule;
import org.netbeans.modules.groovy.editor.hints.spi.RuleContext;
import org.netbeans.modules.groovy.editor.hints.spi.SelectionRule;
import org.netbeans.modules.groovy.editor.hints.spi.UserConfigurableRule;
import org.netbeans.modules.groovy.editor.parser.GroovyParserResult;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.HintsProvider;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.openide.util.Exceptions;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author schmidtm
 */
public class GroovyHintsProvider implements HintsProvider{
    
    private boolean cancelled;
    private List<SelectionRule> testSelectionHints;
    private Map<NodeType,List<AstRule>> testHints;
    private Map<NodeType,List<AstRule>> testSuggestions;
    private Map<GroovyCompilerErrorID,List<ErrorRule>> testErrors;
    
    public void computeHints(CompilationInfo info, List<ErrorDescription> hints) {
        return;
    }

    public void computeSuggestions(CompilationInfo info, List<ErrorDescription> suggestions, int caretOffset) {
        return;
    }

    public void computeSelectionHints(CompilationInfo info, List<ErrorDescription> result, int start, int end) {
        try {
            if (info.getDocument() == null) {
                return; // Document probably closed
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        cancelled = false;
        
        ASTNode root = AstUtilities.getRoot(info);

        if (root == null) {
            return;
        }
        List<SelectionRule> hints = testSelectionHints;
        if (hints == null) {
            hints = RulesManager.getInstance().getSelectionHints();
        }

        if (hints.isEmpty()) {
            return;
        }
        
        if (isCancelled()) {
            return;
        }
        
        List<Description> descriptions = new ArrayList<Description>();
        
        applyRules(info, hints, start, end, descriptions);
        
        if (descriptions.size() > 0) {
            for (Description desc : descriptions) {
                ErrorDescription errorDesc = createDescription(desc, info, -1);
                result.add(errorDesc);
            }
        }
    }

    public List<Error> computeErrors(CompilationInfo info, List<ErrorDescription> hints) {
        // Return all the errors we -haven't- added custom error hints for:
        GroovyParserResult rpr = AstUtilities.getParseResult(info);
        if (rpr != null) {
            return rpr.getDiagnostics();
        }
        
        return Collections.emptyList();
    }

    private boolean isTest() {
        return testHints != null || testSuggestions != null || testSelectionHints != null ||
                testErrors != null;
    }
    
    public void cancel() {
        return;
    }

    private boolean isCancelled() {
        return cancelled;
    }
    
    private void applyRules(CompilationInfo info, List<SelectionRule> rules, int start, int end, 
            List<Description> result) {

        RuleContext context = new RuleContext();
        context.compilationInfo = info;
        context.selectionStart = start;
        context.selectionEnd = end;
        try {
            context.doc = (BaseDocument) info.getDocument();
            if (context.doc == null) {
                // Document closed
                return;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        for (SelectionRule rule : rules) {
            if (!rule.appliesTo(info)) {
                continue;
            }

            rule.run(context, result);
        }
    }
    
private ErrorDescription createDescription(Description desc, CompilationInfo info, int caretPos) {
        Rule rule = desc.getRule();
        HintSeverity severity;
        if (rule instanceof UserConfigurableRule) {
            severity = RulesManager.getInstance().getSeverity((UserConfigurableRule)rule);
        } else {
            severity = rule.getDefaultSeverity();
        }
        OffsetRange range = desc.getRange();
        List<Fix> fixList;
        if (desc.getFixes() != null && desc.getFixes().size() > 0) {
            fixList = new ArrayList<Fix>(desc.getFixes().size());
            
            // TODO print out priority with left flushed 0's here
            // this is just a hack
            String sortText = Integer.toString(10000+desc.getPriority());
            
            for (org.netbeans.modules.groovy.editor.hints.spi.Fix fix : desc.getFixes()) {
                fixList.add(new FixWrapper(fix, sortText));
                
                if (fix instanceof PreviewableFix) {
                    PreviewableFix previewFix = (PreviewableFix)fix;
                    if (previewFix.canPreview() && !isTest()) {
                        fixList.add(new PreviewHintFix(info, previewFix, sortText));
                    }
                }
            }
            
            if (rule instanceof UserConfigurableRule && !isTest()) {
                // Add a hint for disabling this fix
                fixList.add(new DisableHintFix((UserConfigurableRule)rule, info, caretPos, sortText));
            }
        } else {
            fixList = Collections.emptyList();
        }
        return ErrorDescriptionFactory.createErrorDescription(
                severity.toEditorSeverity(), 
                desc.getDescription(), fixList, desc.getFile(), range.getStart(), range.getEnd());
        
    }
    
    private static class FixWrapper implements EnhancedFix {
        private org.netbeans.modules.groovy.editor.hints.spi.Fix fix;
        private String sortText;
        
        FixWrapper(org.netbeans.modules.groovy.editor.hints.spi.Fix fix, String sortText) {
            this.fix = fix;
            this.sortText = sortText;
        }

        public String getText() {
            return fix.getDescription();
        }

        public ChangeInfo implement() throws Exception {
            fix.implement();
            
            return null;
        }

        public CharSequence getSortText() {
            return sortText;
        }
    }

}
