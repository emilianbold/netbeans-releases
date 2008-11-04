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
package org.netbeans.modules.javascript.hints.infrastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.mozilla.nb.javascript.Node;
import org.netbeans.modules.csl.api.CompilationInfo;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.HintsProvider;
import org.netbeans.modules.csl.api.HintsProvider.HintsManager;
import org.netbeans.modules.csl.api.ParserResult;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.javascript.editing.AstPath;
import org.netbeans.modules.javascript.editing.AstUtilities;
import org.netbeans.modules.javascript.hints.StrictWarning;

/**
 * Class which acts on the rules and suggestions by iterating the 
 * AST and invoking applicable rules
 * 
 * @author Tor Norbye
 */
public class JsHintsProvider implements HintsProvider {
    private boolean cancelled;
 
    public JsHintsProvider() {
    }

    public void computeErrors(HintsManager manager, RuleContext context, List<Hint> result, List<Error> unhandled) {
        ParserResult parserResult = context.parserResult;
        if (parserResult == null) {
            return;
        }

        List<Error> errors = parserResult.getDiagnostics();
        if (errors == null || errors.size() == 0) {
            return;
        }

        cancelled = false;
        
        @SuppressWarnings("unchecked")
        Map<String,List<JsErrorRule>> hints = (Map)manager.getErrors();

        if (hints.isEmpty() || isCancelled()) {
            unhandled.addAll(errors);
            return;
        }
        
        for (Error error : errors) {
            if (!applyErrorRules(manager, context, error, hints, result)) {
                unhandled.add(error);
            }
        }
    }

    public void computeSelectionHints(HintsManager manager, RuleContext context, List<Hint> result, int start, int end) {
        cancelled = false;
        
        if (context.parserResult == null) {
            return;
        }
        Node root = AstUtilities.getRoot(context.parserResult);

        if (root == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<JsSelectionRule> hints = (List<JsSelectionRule>) manager.getSelectionHints();

        if (hints.isEmpty()) {
            return;
        }
        
        if (isCancelled()) {
            return;
        }
        
        applySelectionRules(manager, context, hints, result);
    }
    
    public void computeHints(HintsManager manager, RuleContext context, List<Hint> result) {
        cancelled = false;
        
        if (context.parserResult == null) {
            return;
        }
        Node root = AstUtilities.getRoot(context.parserResult);

        if (root == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        Map<Integer,List<JsAstRule>> hints = (Map)manager.getHints(false, context);

        if (hints.isEmpty()) {
            return;
        }
        
        if (isCancelled()) {
            return;
        }
        
        AstPath path = new AstPath();
        path.descend(root);
        
        //applyRules(manager, NodeTypes.ROOTNODE, root, path, info, hints, descriptions);
        applyHints(manager, context, -1, root, path, hints, result);
        
        scan(manager, context, root, path, hints, result);
        path.ascend();
    }
    
    @SuppressWarnings("unchecked")
    public void computeSuggestions(HintsManager manager, RuleContext context, List<Hint> result, int caretOffset) {
        cancelled = false;
        if (context.parserResult == null) {
            return;
        }
        
        Node root = AstUtilities.getRoot(context.parserResult);

        if (root == null) {
            return;
        }

        Map<Integer, List<JsAstRule>> suggestions = new HashMap<Integer, List<JsAstRule>>();
   
        suggestions.putAll((Map)manager.getHints(true, context));

        Set<Entry<Integer, List<JsAstRule>>> entrySet = (Set)manager.getSuggestions().entrySet();
        for (Entry<Integer, List<JsAstRule>> e : entrySet) {
            List<JsAstRule> rules = suggestions.get(e.getKey());

            if (rules != null) {
                List<JsAstRule> res = new LinkedList<JsAstRule>();

                res.addAll(rules);
                res.addAll(e.getValue());

                suggestions.put(e.getKey(), res);
            } else {
                suggestions.put(e.getKey(), e.getValue());
            }
        }

        if (suggestions.isEmpty()) {
            return;
        }
        

        if (isCancelled()) {
            return;
        }
        
        CompilationInfo info = context.compilationInfo;
        int astOffset = AstUtilities.getAstOffset(info, caretOffset);

        AstPath path = new AstPath(root, astOffset);
        Iterator<Node> it = path.leafToRoot();
        while (it.hasNext()) {
            if (isCancelled()) {
                return;
            }

            Node node = it.next();
            applyHints(manager, context, node.getType(), node, path, suggestions, result);
        }
        
        //applyRules(NodeTypes.ROOTNODE, path, info, suggestions, caretOffset, result);
    }

    private void applyHints(HintsManager manager, RuleContext context, int nodeType, Node node, AstPath path, Map<Integer,List<JsAstRule>> hints,
            List<Hint> result) {
        List<JsAstRule> rules = hints.get(nodeType);

        if (rules != null) {
            JsRuleContext jsContext = (JsRuleContext)context;
            jsContext.node = node;
            jsContext.path = path;
            
            for (JsAstRule rule : rules) {
                if (manager.isEnabled(rule)) {
                    rule.run(jsContext, result);
                }
            }
        }
    }

    /** Apply error rules and return true iff somebody added an error description for it */
    private boolean applyErrorRules(HintsManager manager, RuleContext context, Error error, Map<String,List<JsErrorRule>> hints,
            List<Hint> result) {
        String code = error.getKey();
        if (code != null) {
            List<JsErrorRule> rules = hints.get(code);

            if (rules != null) {
                int countBefore = result.size();
                JsRuleContext jsContext = (JsRuleContext)context;
                
                boolean disabled = false;
                for (JsErrorRule rule : rules) {
                    if (!manager.isEnabled(rule)) {
                        disabled = true;
                    } else if (rule.appliesTo(context)) {
                        rule.run(jsContext, error, result);
                    }
                }
                
                return disabled || countBefore < result.size() || jsContext.remove;
            }
        }
        
        return false;
    }

    private void applySelectionRules(HintsManager manager, RuleContext context, List<JsSelectionRule> rules,
            List<Hint> result) {
        
        for (JsSelectionRule rule : rules) {
            if (!rule.appliesTo(context)) {
                continue;
            }
            
            //if (!manager.isEnabled(rule)) {
            //    continue;
            //}

            rule.run(context, result);
        }
    }
    
    private void scan(HintsManager manager, RuleContext context, Node node, AstPath path, Map<Integer,List<JsAstRule>> hints,
            List<Hint> result) {
        applyHints(manager, context, node.getType(), node, path, hints, result);
        
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            if (isCancelled()) {
                return;
            }

            path.descend(child);
            scan(manager, context, child, path, hints, result);
            path.ascend();
        }        
    }

    public void cancel() {
        cancelled = true;
    }

    private boolean isCancelled() {
        return cancelled;
    }

    public RuleContext createRuleContext() {
        return new JsRuleContext();
    }
    
    public List<Rule> getBuiltinRules() {
        List<Rule> rules = new ArrayList<Rule>(StrictWarning.KNOWN_STRICT_ERROR_KEYS.length);

        // Add builtin wrappers for strict warnings
        for (String key : StrictWarning.KNOWN_STRICT_ERROR_KEYS) {
            StrictWarning rule = new StrictWarning(key);
            if (StrictWarning.RESERVED_KEYWORD.equals(key) || StrictWarning.TRAILING_COMMA.equals(key)) { // NOI18N
                rule.setDefaultSeverity(HintSeverity.ERROR);
            }
            rules.add(rule);
        }

        return rules;
    }
}
