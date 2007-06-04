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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.css.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.ParserManagerListener;
import org.openide.ErrorManager;

/**
 * A domain object model representing CSS file backed up by 
 * instance of {@link javax.swing.text.Document}. For each 
 * document instance there is one instance of the model.
 * 
 * You can get the shared instance by using
 * <code>
 * CssModel.get(yourDocument);
 * </code>
 
 * The model also allows to listen on the changes of the model.
 *
 * @author Marek Fukala
 */
public final class CssModel {
    
    /** PropertyChangeEvent of this name is fired when the model 
     * has been successfully updated.
     */
    public static final String MODEL_UPDATED = "modelUpdated"; //NOI18N
    
    /** PropertyChangeEvent of this name is fired when the underlying 
     * document has changed and the model started to reparse it.
     */
    public static final String MODEL_PARSING = "modelParsing"; //NOI18N
    
    /** PropertyChangeEvent of this name is fired when the underlying 
     * document has changed and contains error which prevents 
     * the parser to successfully finish.
     */
    public static final String MODEL_INVALID = "modelInvalid"; //NOI18N
    
    /** Gets a shared instance of CssModel for the given document. 
     * 
     * @return instance of {@link CssModel}
     * @param doc source of the model
     */
    public static CssModel get(Document doc) {
        CssModel model = (CssModel)doc.getProperty(CssModel.class);
        if(model == null) {
            model = new CssModel(doc);
            doc.putProperty(CssModel.class, model);
        }
        return model;
    }
    
    private Document doc;
    
    private List<CssRule> rules = new ArrayList<CssRule>(10);
    
    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    private CssModel(Document doc) {
        this.doc = doc;
        
        final ParserManager parser = getCssParser();
        if(parser == null) {
            throw new IllegalStateException("Cannot get CSS parser for document " + doc);
        }
        
        //I need to add my own AST evaluator since the parser status shows OK even the source
        //is apparently broken
        CssModelASTEvaluator evaluator = new CssModelASTEvaluator(new CssModelASTEvaluatorListener() {
            public void evaluated(ASTNode root, boolean error) {
                //parser finished, AST evaluated
                if(error) {
                     support.firePropertyChange(MODEL_INVALID, null, null);
                } else {
                    updateModel(root);
                }
            }
        });
        parser.addASTEvaluator(evaluator);
        
        //listen on the parser and update the rules list
        parser.addListener(new ParserManagerListener() {
            public void parsed(State state, ASTNode root) {
                if(state == ParserManager.State.PARSING) {
                    support.firePropertyChange(MODEL_PARSING, null, null);
                }
            }
        });
        
        //update the list of rules if the file has already been parsed before
        try {
            if(parser.getAST() != null && parser.getState() == ParserManager.State.OK) {
                updateModel(parser.getAST());
            }
        } catch (ParseException pe) {
            //parser error in the document
        }
    }
    
    /** @return List of {@link CssRule}s or null if the document hasn't been parsed yet. */
    public List<CssRule> rules() {
        synchronized (rules) {
            return rules;
        }
    }
    
    /** Finds a rule on the given offset.
     * 
     * @return Instance of {@link CssRule} for the given offset or null if the 
     * offset falls into a space where there is no css rule.
     * @param offset within the model's document
     */
    public CssRule ruleForOffset(int offset) {
        synchronized (rules) {
            for(CssRule rule : rules()) {
                if(rule.getRuleNameOffset() <= offset
                        && rule.getRuleCloseBracketOffset() >= offset) {
                    return rule;
                }
            }
            return null;
        }
    }
    
    /** Adds a PropertyChangeListener instance.
     * The listener then receives {@link MODEL_UPDATED}, 
     * {@link MODEL_PARSING} and {@link MODEL_INVALID}.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    
    /** Removes a PropertyChangeListener instance. */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    
    private ParserManager getCssParser() {
        return ParserManager.get(doc);
    }
    
    private void updateModel(ASTNode root) {
        try {
            synchronized (rules) {
                List<CssRule> oldRules = rules;
                
                List<CssRule> newRules = new ArrayList<CssRule>(oldRules.size());
                //search the AST and find rules
                for(ASTItem ruleset : root.getChildren()) {
                    //resolve the rule
                    if(ruleset instanceof ASTNode) {
                        ASTNode rulesetNode = (ASTNode)ruleset;
                        ASTNode selectors = rulesetNode.getNode("selectors"); //NOI18N
                        ASTNode body = rulesetNode.getNode("body"); //NOI18N
                        if(selectors == null || body == null) {
                            return ; //error?
                        }
                        
                        List<CssRuleItem> items = new ArrayList<CssRuleItem>();
                        
                        ASTNode declarations = body.getNode("declarations"); //NOI18N
                        if(declarations != null) {
                            for(ASTItem item : declarations.getChildren()) {
                                if(item instanceof ASTNode) {
                                    ASTNode node = (ASTNode)item;
                                    if(node.getNT().equals("declaration")) { //NOI18N
                                        ASTNode key = node.getNode("key"); //NOI18N
                                        ASTNode value = node.getNode("expr"); //NOI18N
                                        CssRuleItem ruleItem = new CssRuleItem(key.getAsText(), key.getOffset(), value.getAsText(), value.getOffset());
                                        items.add(ruleItem);
                                    }
                                }
                            }
                            
                            ASTToken openBracket = null, closeBracket = null;
                            for(ASTItem item : body.getChildren()) {
                                if(item instanceof ASTToken) {
                                    ASTToken token = (ASTToken)item;
                                    if(token.getType().equals("css_operator")) {
                                        if(token.getIdentifier().equals("{")) {
                                            openBracket = token;
                                        } else if(token.getIdentifier().equals("}")) {
                                            closeBracket = token;
                                        }
                                    }
                                }
                            }
                            
                            CssRuleContent styleData = new CssRuleContent(items);
                            String ruleName = selectors.getAsText(); //do we really need a structural info about the selector???
                            CssRule rule = new CssRule(ruleName, selectors.getOffset(), openBracket.getOffset(), closeBracket.getOffset(), styleData);
                            newRules.add(rule);
                        }
                    }
                }
                rules = newRules;
                support.firePropertyChange(MODEL_UPDATED, oldRules, rules);
            }
        } catch (final Throwable t) {
            Logger.getAnonymousLogger().throwing("bla", "blebla", t);
            ErrorManager.getDefault().notify(t);
        }
    }
    
    private class CssModelASTEvaluator extends ASTEvaluator {
        
        private boolean sourceOK;
        
        private CssModelASTEvaluator(CssModelASTEvaluatorListener listener) {
            this.listener = listener;
        }
        
        private CssModelASTEvaluatorListener listener = null;
        private ASTNode root;
        
        public void beforeEvaluation(State state, ASTNode root) {
            sourceOK = true;
            this.root = root;
        }
        
        public void afterEvaluation(State state, ASTNode root) {
            if(listener != null) {
                listener.evaluated(root, !sourceOK);
            }
        }
        
        public void evaluate(State state, ASTPath path) {
            ASTItem item = path.getLeaf();
            if(item instanceof ASTNode) {
                if(((ASTNode)item).getNT().equals("ERROR")) { //NOI18N
                    //source contains errors
                    sourceOK = false;
                }
            }
        }
        
    }
    
    private interface CssModelASTEvaluatorListener {
     
        public void evaluated(ASTNode root, boolean error);
        
    }
    
}
