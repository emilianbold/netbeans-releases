/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints.infrastructure;

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
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.hints.options.HintsSettings;
import org.netbeans.modules.ruby.hints.spi.AstRule;
import org.netbeans.modules.ruby.hints.spi.Description;
import org.netbeans.modules.ruby.hints.spi.ErrorRule;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import org.netbeans.modules.ruby.hints.spi.Rule;
import org.netbeans.modules.ruby.hints.spi.UserConfigurableRule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;

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
    private Map<String,List<ErrorRule>> testErrors;
    
    public RubyHintsProvider() {
    }

    public List<Error> computeErrors(CompilationInfo info, List<ErrorDescription> result) {
        List<Error> errors = info.getDiagnostics();
        if (errors == null || errors.size() == 0) {
            return Collections.emptyList();
        }

        cancelled = false;
        
        Map<String,List<ErrorRule>> hints = testErrors;
        if (testErrors == null) {
            hints = RulesManager.getInstance().getErrors();
        }

        if (hints.isEmpty() || isCancelled()) {
            return Collections.emptyList();
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
    
    public void computeHints(CompilationInfo info, List<ErrorDescription> result) {
        cancelled = false;
        
        Node root = AstUtilities.getRoot(info);

        if (root == null) {
            return;
        }
        Map<Integer,List<AstRule>> hints = testHints;
        if (testHints == null) {
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
            for (org.netbeans.modules.ruby.hints.spi.Fix fix : desc.getFixes()) {
                fixList.add(new FixWrapper(fix));
            }
            
            if (rule instanceof UserConfigurableRule) {
                // Add a hint for disabling this fix
                fixList.add(new DisableHintFix((UserConfigurableRule)rule, info, caretPos));
            }
        } else {
            fixList = Collections.emptyList();
        }
        return ErrorDescriptionFactory.createErrorDescription(
                severity.toEditorSeverity(), 
                desc.getDescription(), fixList, desc.getFile(), range.getStart(), range.getEnd());
        
    }

    public void computeSuggestions(CompilationInfo info, List<ErrorDescription> result, int caretOffset) {
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

        AstPath path = new AstPath(root, caretOffset);
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
            for (AstRule rule : rules) {
                if (HintsSettings.isEnabled(rule)) {
                    rule.run(info, node, path, caretOffset, result);
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
                for (ErrorRule rule : rules) {
                    if (!rule.appliesTo(info)) {
                        continue;
                    }
                    rule.run(info, error, result);
                }
                
                return countBefore < result.size();
            }
        }
        
        return false;
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
    public void setTestingHints(Map<Integer,List<AstRule>> testHints, Map<Integer,List<AstRule>> testSuggestions, Map<String,List<ErrorRule>> testErrors) {
        this.testHints = testHints;
        this.testSuggestions = testSuggestions;
        this.testErrors = testErrors;
    }
    
    private static class FixWrapper implements Fix {
        private org.netbeans.modules.ruby.hints.spi.Fix fix;
        
        FixWrapper(org.netbeans.modules.ruby.hints.spi.Fix fix) {
            this.fix = fix;
        }

        public String getText() {
            return fix.getDescription();
        }

        public ChangeInfo implement() throws Exception {
            fix.implement();
            
            return null;
        }
    }
}
