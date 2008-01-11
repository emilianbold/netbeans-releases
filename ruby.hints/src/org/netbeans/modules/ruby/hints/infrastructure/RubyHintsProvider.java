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
package org.netbeans.modules.ruby.hints.infrastructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.Error;
import org.netbeans.api.gsf.HintsProvider;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.hints.options.HintsSettings;
import org.netbeans.modules.ruby.hints.spi.AstRule;
import org.netbeans.modules.ruby.hints.spi.Description;
import org.netbeans.modules.ruby.hints.spi.ErrorRule;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import org.netbeans.modules.ruby.hints.spi.PreviewableFix;
import org.netbeans.modules.ruby.hints.spi.Rule;
import org.netbeans.modules.ruby.hints.spi.RuleContext;
import org.netbeans.modules.ruby.hints.spi.SelectionRule;
import org.netbeans.modules.ruby.hints.spi.UserConfigurableRule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.Exceptions;

/**
 * Class which acts on the rules and suggestions by iterating the 
 * AST and invoking applicable rules
 * 
 * 
 * @author Tor Norbye
 */
public class RubyHintsProvider implements HintsProvider {
    private boolean cancelled;
    private Map<Integer,List<AstRule>> testHints;
    private Map<Integer,List<AstRule>> testSuggestions;
    private List<SelectionRule> testSelectionHints;
    private Map<String,List<ErrorRule>> testErrors;
    
    public RubyHintsProvider() {
    }

    private boolean isTest() {
        return testHints != null || testSuggestions != null || testSelectionHints != null ||
                testErrors != null;
    }
    
    public List<Error> computeErrors(CompilationInfo info, List<ErrorDescription> result) {
        try {
            if (info.getDocument() == null) {
                // Document probably closed
                return Collections.emptyList();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        List<Error> errors = info.getDiagnostics();
        if (errors == null || errors.size() == 0) {
            return Collections.emptyList();
        }

        cancelled = false;
        
        Map<String,List<ErrorRule>> hints = testErrors;
        if (hints == null) {
            hints = RulesManager.getInstance().getErrors();
        }

        if (hints.isEmpty() || isCancelled()) {
            return errors;
        }
        
        List<Description> descriptions = new ArrayList<Description>();
        
        List<Error> unhandled = new ArrayList<Error>();
        
        for (Error error : errors) {
            if (!applyRules(error, info, hints, descriptions)) {
                unhandled.add(error);
            }
        }
        
        if (descriptions.size() > 0) {
            for (Description desc : descriptions) {
                ErrorDescription errorDesc = createDescription(desc, info, -1);
                result.add(errorDesc);
            }
        }
        
        return unhandled;
    }

    public void computeSelectionHints(CompilationInfo info, List<ErrorDescription> result, int start, int end) {
        try {
            if (info.getDocument() == null) {
                // Document probably closed
                return;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        cancelled = false;
        
        Node root = AstUtilities.getRoot(info);

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
    
    public void computeHints(CompilationInfo info, List<ErrorDescription> result) {
        try {
            if (info.getDocument() == null) {
                // Document probably closed
                return;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        cancelled = false;
        
        Node root = AstUtilities.getRoot(info);

        if (root == null) {
            return;
        }
        Map<Integer,List<AstRule>> hints = testHints;
        if (hints == null) {
            hints = RulesManager.getInstance().getHints(false, info);
        }

        if (hints.isEmpty()) {
            return;
        }
        
        if (isCancelled()) {
            return;
        }
        
        List<Description> descriptions = new ArrayList<Description>();
        
        AstPath path = new AstPath();
        path.descend(root);
        
        applyRules(NodeTypes.ROOTNODE, root, path, info, hints, -1, descriptions);
        
        scan(root, path, info, hints, -1, descriptions);
        path.ascend();
        
        if (descriptions.size() > 0) {
            for (Description desc : descriptions) {
                ErrorDescription errorDesc = createDescription(desc, info, -1);
                result.add(errorDesc);
            }
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
            
            for (org.netbeans.modules.ruby.hints.spi.Fix fix : desc.getFixes()) {
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

    public void computeSuggestions(CompilationInfo info, List<ErrorDescription> result, int caretOffset) {
        try {
            if (info.getDocument() == null) {
                // Document probably closed
                return;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        cancelled = false;
        
        Node root = AstUtilities.getRoot(info);

        if (root == null) {
            return;
        }

        Map<Integer, List<AstRule>> suggestions = testSuggestions;
        if (suggestions == null) {
            suggestions = new HashMap<Integer, List<AstRule>>();
   
            suggestions.putAll(RulesManager.getInstance().getHints(true, info));

            for (Entry<Integer, List<AstRule>> e : RulesManager.getInstance().getSuggestions().entrySet()) {
                List<AstRule> rules = suggestions.get(e.getKey());

                if (rules != null) {
                    List<AstRule> res = new LinkedList<AstRule>();

                    res.addAll(rules);
                    res.addAll(e.getValue());

                    suggestions.put(e.getKey(), res);
                } else {
                    suggestions.put(e.getKey(), e.getValue());
                }
            }
        }

        if (suggestions.isEmpty()) {
            return;
        }
        

        if (isCancelled()) {
            return;
        }
        
        int astOffset = AstUtilities.getAstOffset(info, caretOffset);

        AstPath path = new AstPath(root, astOffset);
        List<Description> descriptions = new ArrayList<Description>();
        
        Iterator<Node> it = path.leafToRoot();
        while (it.hasNext()) {
            if (isCancelled()) {
                return;
            }

            Node node = it.next();
            applyRules(node.nodeId, node, path, info, suggestions, caretOffset, descriptions);
        }
        
        //applyRules(NodeTypes.ROOTNODE, path, info, suggestions, caretOffset, result);

        if (descriptions.size() > 0) {
            for (Description desc : descriptions) {
                ErrorDescription errorDesc = createDescription(desc, info, caretOffset);
                result.add(errorDesc);
            }
        }
    }

    private void applyRules(int nodeType, Node node, AstPath path, CompilationInfo info, Map<Integer,List<AstRule>> hints,
            int caretOffset, List<Description> result) {
        List<AstRule> rules = hints.get(nodeType);

        if (rules != null) {
            RuleContext context = new RuleContext();
            context.compilationInfo = info;
            try {
                context.doc = (BaseDocument) info.getDocument();
                if (context.doc == null) {
                    // Document closed
                    return;
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            context.node = node;
            context.path = path;
            context.caretOffset = caretOffset;
            
            for (AstRule rule : rules) {
                if (HintsSettings.isEnabled(rule)) {
                    rule.run(context, result);
                }
            }
        }
    }

    /** Apply error rules and return true iff somebody added an error description for it */
    private boolean applyRules(Error error, CompilationInfo info, Map<String,List<ErrorRule>> hints,
            List<Description> result) {
        String code = error.getKey();
        if (code != null) {
            List<ErrorRule> rules = hints.get(code);

            if (rules != null) {
                int countBefore = result.size();
                RuleContext context = new RuleContext();
                context.compilationInfo = info;
                try {
                    context.doc = (BaseDocument) info.getDocument();
                    if (context.doc == null) {
                        // Document closed
                        return false;
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
                for (ErrorRule rule : rules) {
                    if (!rule.appliesTo(info)) {
                        continue;
                    }
                    rule.run(context, error, result);
                }
                
                return countBefore < result.size();
            }
        }
        
        return false;
    }

    private void applyRules(CompilationInfo info, List<SelectionRule> rules, int start, int end, 
            List<Description> result) {

        RuleContext context = new RuleContext();
        context.compilationInfo = info;
        //context.node = node;
        //context.path = path;
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
            
            //if (!HintsSettings.isEnabled(rule)) {
            //    continue;
            //}

            rule.run(context, result);
        }
    }
    
    private void scan(Node node, AstPath path, CompilationInfo info, Map<Integer,List<AstRule>> hints, int caretOffset, 
            List<Description> result) {
        applyRules(node.nodeId, node, path, info, hints, caretOffset, result);
        
        @SuppressWarnings(value = "unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (isCancelled()) {
                return;
            }

            path.descend(child);
            scan(child, path, info, hints, caretOffset, result);
            path.ascend();
        }        
    }

    public void cancel() {
        cancelled = true;
    }

    private boolean isCancelled() {
        return cancelled;
    }
    
    /** For testing purposes only! */
    public void setTestingHints(Map<Integer,List<AstRule>> testHints, Map<Integer,List<AstRule>> testSuggestions, Map<String,List<ErrorRule>> testErrors,
            List<SelectionRule> testSelectionHints) {
        this.testHints = testHints;
        this.testSuggestions = testSuggestions;
        this.testErrors = testErrors;
        this.testSelectionHints = testSelectionHints;
    }
    
    private static class FixWrapper implements EnhancedFix {
        private org.netbeans.modules.ruby.hints.spi.Fix fix;
        private String sortText;
        
        FixWrapper(org.netbeans.modules.ruby.hints.spi.Fix fix, String sortText) {
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
