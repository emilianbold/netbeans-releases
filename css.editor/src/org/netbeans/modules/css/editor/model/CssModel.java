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
package org.netbeans.modules.css.editor.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.Severity;
import org.netbeans.modules.gsf.api.SourceModel;
import org.netbeans.modules.gsf.api.SourceModelFactory;
import org.netbeans.modules.css.gsf.CSSParserResult;
import org.netbeans.modules.css.parser.ASCII_CharStream;
import org.netbeans.modules.css.parser.CSSParser;
import org.netbeans.modules.css.parser.CSSParserConstants;
import org.netbeans.modules.css.parser.CSSParserTreeConstants;
import org.netbeans.modules.css.parser.NodeVisitor;
import org.netbeans.modules.css.parser.ParseException;
import org.netbeans.modules.css.parser.SimpleNode;
import org.netbeans.modules.css.parser.SimpleNodeUtil;
import org.netbeans.modules.css.parser.Token;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

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

    private static final Logger LOGGER = Logger.getLogger(CssModel.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    
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
            throw new NullPointerException("Passed null document!"); //NOI18N
        }
        CssModel model = (CssModel) doc.getProperty(CssModel.class);
        if (model == null) {
            model = new CssModel(doc);
            doc.putProperty(CssModel.class, model);
        }
        return model;
    }

     /** Gets a private instance of CssModel for the given source. 
     * 
     * @return instance of {@link CssModel}
     * @param source an instance of Reader for the source
     */
    public static CssModel get(Reader source) {
        try {
            CSSParser parser = new CSSParser();
            parser.ReInit(new ASCII_CharStream(source));
            SimpleNode node = parser.styleSheet();
            return new CssModel(node);
        } catch (ParseException pe) {
            Exceptions.printStackTrace(pe);
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

        DataObject od = NbEditorUtilities.getDataObject(doc);
        final FileObject fo = od.getPrimaryFile();

        //ensure the model is built
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                try {
                    final Collection<ParserResult> result = new HashSet<ParserResult>(1);
                    SourceModel model = SourceModelFactory.getInstance().getModel(fo);
                    model.runUserActionTask(new CancellableTask<CompilationInfo>() {

                        public void cancel() {
                        }

                        public void run(CompilationInfo ci) throws Exception {
                            ParserResult presult = ci.getEmbeddedResults("text/x-css").iterator().next(); //NOI18N
                            result.add(presult);
                        }
                    }, true);

                    ParserResult presult = result.iterator().next();
                    SimpleNode root = ((CSSParserResult) presult).root();
                    if (containsErrors(presult)) {
                        support.firePropertyChange(MODEL_INVALID, rules, null);
                        rules = Collections.EMPTY_LIST;
                        return;
                    }

                    updateModel(root);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

    }
    
    //called by CssEditorAwareSourceTask
    void parsed(CompilationInfo info) {
        //ignore if not our document
        //this is uglyyyyyyyyyyyy!!!!!!!!!! fix the whole support please once you have some time!
        if (info.getDocument() != CssModel.this.doc) {
            return;
        }

        Collection<? extends ParserResult> results = info.getEmbeddedResults("text/x-css"); //NOI18N
        if(results == null || results.isEmpty()) {
            return ;
        }
        
        ParserResult presult = results.iterator().next();
        SimpleNode root = ((CSSParserResult) presult).root();

        if (containsErrors(presult)) {
            support.firePropertyChange(MODEL_INVALID, rules, null);
            rules = Collections.EMPTY_LIST;
            return;
        }

        updateModel(root);
    }

    private boolean containsErrors(ParserResult result) {
        //check for errors
        for (Error error : result.getDiagnostics()) {
            if (error.getSeverity() == Severity.ERROR) {
                return true;
            }
        }
        return false;
    }

    private CssModel(SimpleNode root) {
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
            if (rules != null) {
                for (CssRule rule : rules()) {
                    if (rule.getRuleNameOffset() <= offset && rule.getRuleCloseBracketOffset() >= offset) {
                        return rule;
                    }
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

    private boolean isImmutable() {
        return immutable;
    }

    private synchronized void updateModel(SimpleNode root) {
        synchronized (rules) {
            List<CssRule> oldRules = rules;

            final List<CssRule> newRules = new ArrayList<CssRule>(oldRules.size());

            NodeVisitor styleRuleVisitor = new NodeVisitor() {

                public void visit(SimpleNode node) {
                    try {
                        if (node.kind() == CSSParserTreeConstants.JJTSTYLERULE) {
                            //find curly brackets
                            Token t = node.jjtGetFirstToken();
                            Token last = node.jjtGetLastToken();

                            int openCurlyBracketOffset = -1;
                            int closeCurlyBracketOffset = -1;
                            ArrayList<Integer> semicolons = new ArrayList<Integer>();
                            ArrayList<Integer> colons = new ArrayList<Integer>();
                            while (t != null && t.offset <= last.offset) { //also include the last token
                                if (t.kind == CSSParserConstants.LBRACE) {
                                    openCurlyBracketOffset = t.offset;
                                } else if (t.kind == CSSParserConstants.RBRACE) {
                                    closeCurlyBracketOffset = t.offset;
                                } else if (t.kind == CSSParserConstants.SEMICOLON) {
                                    semicolons.add(Integer.valueOf(t.offset));
                                } else if (t.kind == CSSParserConstants.COLON) {
                                    colons.add(Integer.valueOf(t.offset));
                                }
                                t = t.next;
                            }

                            //parse style rule
                            SimpleNode selectortList = SimpleNodeUtil.getChildByType(node, CSSParserTreeConstants.JJTSELECTORLIST);
                            SimpleNode[] declarations = SimpleNodeUtil.getChildrenByType(node, CSSParserTreeConstants.JJTDECLARATION);
                            List<CssRuleItem> ruleItems = new ArrayList<CssRuleItem>(declarations.length);
                            for (int i = 0; i < declarations.length; i++) {
                                SimpleNode declaration = declarations[i];
                                SimpleNode property = SimpleNodeUtil.getChildByType(declaration, CSSParserTreeConstants.JJTPROPERTY);
                                SimpleNode value = SimpleNodeUtil.getChildByType(declaration, CSSParserTreeConstants.JJTEXPR);

                                int semicolonOffset = i < semicolons.size() ? semicolons.get(i) : -1; //there may not be the semicolon after last declaration
                                int colonOffset = colons.get(i);

                                CssRuleItem ruleItem = new CssRuleItem(doc, property.image(), property.startOffset(), value.image(), value.startOffset(), colonOffset, semicolonOffset);

                                ruleItems.add(ruleItem);
                            }

                            CssRuleContent styleData = new CssRuleContent(ruleItems, isImmutable());
                            String ruleName = selectortList.image().trim();
                            CssRule rule = new CssRule(doc, ruleName, selectortList.startOffset(),
                                    openCurlyBracketOffset, closeCurlyBracketOffset, styleData);
                            newRules.add(rule);

                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            };

            SimpleNodeUtil.visitChildren(root, styleRuleVisitor);

            if (LOG) {
                LOGGER.fine("CssModel parse tree:"); //NOI18N
                root.dump("");
                LOGGER.fine("CssModel structure:"); //NOI18N
                for(CssRule rule : newRules) {
                    LOGGER.fine(rule.toString());
                }
            }
            
            rules = newRules;
            support.firePropertyChange(MODEL_UPDATED, oldRules, rules);
        }
    }
}
