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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.jruby.nb.ast.Node;
import org.jruby.nb.ast.NodeType;
import org.jruby.nb.common.IRubyWarnings.ID;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.HintsProvider;
import org.netbeans.modules.gsf.api.HintsProvider.HintsManager;
import org.netbeans.modules.gsf.api.Rule;
import org.netbeans.modules.gsf.api.RuleContext;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyParser.RubyError;

/**
 * Class which acts on the rules and suggestions by iterating the 
 * AST and invoking applicable rules
 * 
 * 
 * @author Tor Norbye
 */
public class RubyHintsProvider implements HintsProvider {
    private boolean cancelled;
    
    public RubyHintsProvider() {
    }

    public RuleContext createRuleContext() {
        return new RubyRuleContext();
    }

    @SuppressWarnings("unchecked")
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
        Map<ID,List<RubyErrorRule>> hints = (Map)manager.getErrors();

        if (hints.isEmpty() || isCancelled()) {
            unhandled.addAll(errors);
            return;
        }
        
        for (Error error : errors) {
            if (error instanceof RubyError) {
                if (!applyRules((RubyError)error, context, hints, result)) {
                    unhandled.add(error);
                }
            }
        }
    }
    
    public List<Rule> getBuiltinRules() {
        return null;
    }

    @SuppressWarnings("unchecked")
    public void computeSelectionHints(HintsManager manager, RuleContext context, List<Hint> result, int start, int end) {
        cancelled = false;
        
        ParserResult parserResult = context.parserResult;
        if (parserResult == null) {
            return;
        }
        Node root = AstUtilities.getRoot(parserResult);

        if (root == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<RubySelectionRule> hints = (List<RubySelectionRule>)manager.getSelectionHints();

        if (hints.isEmpty()) {
            return;
        }
        
        if (isCancelled()) {
            return;
        }
        
        applyRules(manager, context, hints, result);
    }
    
    public void computeHints(HintsManager manager, RuleContext context, List<Hint> result) {
        cancelled = false;
        
        ParserResult parserResult = context.parserResult;
        if (parserResult == null) {
            return;
        }
        Node root = AstUtilities.getRoot(parserResult);

        if (root == null) {
            return;
        }
        
        @SuppressWarnings("unchecked")
        Map<NodeType,List<RubyAstRule>> hints = (Map)manager.getHints(false, context);

        if (hints.isEmpty()) {
            return;
        }
        
        if (isCancelled()) {
            return;
        }
        
        AstPath path = new AstPath();
        path.descend(root);
        
        applyRules(manager, context, NodeType.ROOTNODE, root, path, hints, result);
        
        scan(manager, context, root, path, hints, result);
        path.ascend();
    }
    
    @SuppressWarnings("unchecked")
    public void computeSuggestions(HintsManager manager, RuleContext context, List<Hint> result, int caretOffset) {
        cancelled = false;
        ParserResult parserResult = context.parserResult;
        if (parserResult == null) {
            return;
        }
        
        Node root = AstUtilities.getRoot(parserResult);

        if (root == null) {
            return;
        }

        Map<NodeType, List<RubyAstRule>> suggestions = new HashMap<NodeType, List<RubyAstRule>>();
   
        Map<NodeType,List<RubyAstRule>> hintsMap = (Map)manager.getHints(true, context);
        suggestions.putAll(hintsMap);

        Set<Entry<NodeType, List<RubyAstRule>>> suggestionsSet = (Set)manager.getSuggestions().entrySet();
        for (Entry<NodeType, List<RubyAstRule>> e : suggestionsSet) {
            List<RubyAstRule> rules = suggestions.get(e.getKey());

            if (rules != null) {
                List<RubyAstRule> res = new LinkedList<RubyAstRule>();

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
            applyRules(manager, context, node.nodeId, node, path, suggestions, result);
        }
        
        //applyRules(NodeType.ROOTNODE, path, info, suggestions, caretOffset, result);
    }

    private void applyRules(HintsManager manager, RuleContext context, NodeType nodeType, Node node, AstPath path, Map<NodeType,List<RubyAstRule>> hints,
            List<Hint> result) {
        List<RubyAstRule> rules = hints.get(nodeType);

        if (rules != null) {
            RubyRuleContext rubyContext = (RubyRuleContext)context;
            rubyContext.node = node;
            rubyContext.path = path;
            
            for (RubyAstRule rule : rules) {
                if (manager.isEnabled(rule)) {
                    rule.run(rubyContext, result);
                }
            }
        }
    }

    /** Apply error rules and return true iff somebody added an error description for it */
    private boolean applyRules(RubyError error, RuleContext context, Map<ID,List<RubyErrorRule>> hints,
            List<Hint> result) {
        ID code = error.getId();
        if (code != null) {
            List<RubyErrorRule> rules = hints.get(code);

            if (rules != null) {
                int countBefore = result.size();
                RubyRuleContext rubyContext = (RubyRuleContext)context;
                
                for (RubyErrorRule rule : rules) {
                    if (!rule.appliesTo(context)) {
                        continue;
                    }
                    rule.run(rubyContext, error, result);
                }
                
                return countBefore < result.size();
            }
        }
        
        return false;
    }

    private void applyRules(HintsManager manager, RuleContext context, List<RubySelectionRule> rules, List<Hint> result) {

        RubyRuleContext rubyContext = (RubyRuleContext)context;
        
        for (RubySelectionRule rule : rules) {
            if (!rule.appliesTo(context)) {
                continue;
            }
            
            //if (!manager.isEnabled(rule)) {
            //    continue;
            //}

            rule.run(rubyContext, result);
        }
    }
    
    private void scan(HintsManager manager, RuleContext context, Node node, AstPath path, Map<NodeType,List<RubyAstRule>> hints, 
            List<Hint> result) {
        applyRules(manager, context, node.nodeId, node, path, hints, result);
        
        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
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
}
