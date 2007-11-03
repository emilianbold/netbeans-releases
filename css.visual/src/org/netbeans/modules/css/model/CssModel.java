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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.css.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.Language;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.ParserManagerListener;
import org.netbeans.modules.css.editor.CssEditorKit;
import org.netbeans.modules.languages.Feature;
import org.openide.util.Exceptions;

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
        if (doc == null) {
            throw new NullPointerException("Passed null document!");
        }
        CssModel model = (CssModel)doc.getProperty(CssModel.class);
        if(model == null) {
            model = new CssModel(doc);
            doc.putProperty(CssModel.class, model);
        }
        return model;
    }
    
    /** Winston's workaround. */
    public static CssModel get(InputStream source) {
        try {
            Language lang = LanguagesManager.get().getLanguage(CssEditorKit.CSS_MIME_TYPE);
            return new CssModel(lang.parse(source));
        } catch (LanguageDefinitionNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParseException pe) {
            Exceptions.printStackTrace(pe);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        
        return null;
    }
    
    private Document doc;
    
    private List<CssRule> rules = new ArrayList<CssRule>(10);
    
    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    private boolean immutable;
    
    private CssModel(Document doc) {
        this.doc = doc;
        
        //all domain model objects provided by this model are immutable
        //changes are done by propagating the modifications to the underlying document
        //and subsequent regeneration of the model
        this.immutable = true; 
        
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
    
    private CssModel(ASTNode root) {
        //the domain model object may mutate during their livecycle 
        //based on user modifications since the underlying source of this model
        //is immutable
        this.immutable = false;
        updateModel(root);
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
    
    private boolean isImmutable() {
        return immutable;
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
                            continue;
                        }
                        
                        List<CssRuleItem> items = new ArrayList<CssRuleItem>();
                        
                        //recursively find declarations - the grammar now produces strange
                        //parse tree - the declaration nodes are recursively nested instead
                        //of being on the same tree-path level.
                        //this is largely a workaround for the invalud css grammar which
                        //is probably risky to change now before release.
                        //the change should be reverted to the previous version after
                        //the grammar gets fixed
                        ASTNode declarations = body.getNode("declarations"); //NOI18N
                        if (declarations != null) {
                            findRuleItems(declarations, items);
                        }
                        
                        //find curly barckets offsets
                        ASTToken openBracket = null, closeBracket = null;
                        for (ASTItem item : body.getChildren()) {
                            if (item instanceof ASTToken) {
                                ASTToken token = (ASTToken) item;
                                if (token.getTypeName().equals("css_operator")) {
                                    if (token.getIdentifier().equals("{")) {
                                        openBracket = token;
                                    } else if (token.getIdentifier().equals("}")) {
                                        closeBracket = token;
                                    }
                                }
                            }
                        }

                        if (openBracket == null || closeBracket == null) {
                            continue;
                        }
                        
                        CssRuleContent styleData = new CssRuleContent(items, isImmutable());
                        String ruleName = selectors.getAsText().trim(); //do we really need a structural info about the selector???
                        CssRule rule = new CssRule(doc, ruleName, selectors.getOffset(), openBracket.getOffset(), closeBracket.getOffset(), styleData);
                        newRules.add(rule);
                        
                    }
                }
                rules = newRules;
                support.firePropertyChange(MODEL_UPDATED, oldRules, rules);
            }
        } catch (final Throwable t) {
            Exceptions.printStackTrace(t);
        }
    }

    private void findRuleItems(ASTNode base, List<CssRuleItem> items) throws BadLocationException {
        ASTNode declaration = base.getNode("declaration");
        ASTNode declarations1 = base.getNode("declarations1");
                    
        if(declaration == null || declarations1 == null) {
            return ;
        }
        
        //find ':' token
        int colonOffset = -1;
        ASTToken colon = declaration.getTokenType("css_operator");
        if (colon != null && colon.getIdentifier().equals(":")) {
            colonOffset = colon.getOffset();
        }

        //try to find ending semicolon
        int semicolonOffset = -1;
        if(!declarations1.getChildren().isEmpty()) {
            ASTItem item = declarations1.getChildren().get(0);
            if (item instanceof ASTToken) {
                ASTToken token = (ASTToken) item;
                if (token.getTypeName().equals("css_operator") && token.getIdentifier().equals(";")) {
                    semicolonOffset = token.getOffset();
                }
            }
        }
        
        ASTNode key = declaration.getNode("key"); //NOI18N
        ASTNode value = declaration.getNode("expr"); //NOI18N
        CssRuleItem ruleItem = new CssRuleItem(doc, key.getAsText().trim(), key.getOffset(), value.getAsText().trim(), value.getOffset(), colonOffset, semicolonOffset);
        items.add(ruleItem);

        //go deeper to another declaration
        ASTNode declarations2 = declarations1.getNode("declarations2");
        if(declarations2 != null) {
            findRuleItems(declarations2, items);
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
        
        public void evaluate(State state, List<ASTItem> path, Feature feature) {
            ASTItem item = path.get (path.size () - 1);
            if(item instanceof ASTNode) {
                if(((ASTNode)item).getNT().equals("ERROR")) { //NOI18N
                    //source contains errors
                    sourceOK = false;
                }
            }
        }

        public String getFeatureName() {
            return null;
        }
        
    }
    
    private interface CssModelASTEvaluatorListener {
     
        public void evaluated(ASTNode root, boolean error);
        
    }
    
}
