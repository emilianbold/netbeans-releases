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
package org.netbeans.modules.web.core.syntax;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;

/** 
 * This class listens on the Schlieman parser and once the AST processing finishes
 * after the document was modified, it dynamicaly creates java code embeddings
 * inside <jsp:scriptlet>, <jsp:directive> and <jsp:expression> tags.
 */
public class JspColoringUpdater {
    
    private static final Logger LOGGER = Logger.getLogger(JspColoringUpdater.class.getName());

    /** 
     * @return instance of {@link JspColoringUpdater}
     * @param doc source of the model
     */
    public static JspColoringUpdater init(Document doc) {
        JspColoringUpdater model = (JspColoringUpdater)doc.getProperty(JspColoringUpdater.class);
        if(model == null) {
            model = new JspColoringUpdater(doc);
            doc.putProperty(JspColoringUpdater.class, model);
        }
        return model;
    }
    
    private Document doc;
    
    private JspColoringUpdater(Document doc) {
        this.doc = doc;
        ParserManager parser = ParserManager.get(doc);
        //I need to add my own AST evaluator since the parser status shows OK even the source
        //is apparently broken
        JspASTEvaluator evaluator = new JspASTEvaluator(new JspASTEvaluatorListener() {
            public void evaluated(ASTNode root, boolean error) {
                //parser finished, AST evaluated
                updateEmbeddings(root);
            }
        });
        parser.addASTEvaluator(evaluator);
    }
    
    private void updateEmbeddings(ASTNode root) {
        //do not ask what the hell is that...
        List<JSP.Pair> java_code_blocks = JSP.java_code_blocks;
        for(JSP.Pair pair : java_code_blocks) {
            createEmbedding("text/x-java", pair.a, pair.b, 0, 0);//NOI18N
        }
    }
    
 private void createEmbedding(String mimeType, int startOffset, int endOffset, int startSkipLength, int endSkipLength ) {
        if(startOffset >= endOffset) {
            LOGGER.log(Level.WARNING, "startOffset >= endOffset: "+ startOffset + " >= " + endOffset);
            return ;
        }
        
        Language lang = Language.find(mimeType);
        if(lang == null) {
            LOGGER.log(Level.WARNING, "No " + mimeType + " language found! (" + startOffset + " - " + endOffset + ")");
            return ; //no language found
        }
        
        ((BaseDocument)doc).extWriteLock(); //writeLock is required since we create embedding what is kind of document change
        try {
            TokenHierarchy th = TokenHierarchy.get(doc);
            TokenSequence ts = th.tokenSequence(JspTokenId.language()); //JSP is always top level language

            if(ts == null) {
                LOGGER.log(Level.WARNING, "Trying to create embedding for " + mimeType + " [" + startOffset + " - " + endOffset + "]: cannot get HTML token sequence!");
                return ;
            }
            
            ts.move(startOffset);
            if(!ts.moveNext() && !ts.movePrevious()) {
                return ; //no token
            }
            
            //huh, use the startSkipLength and endSkipLength only on the first and last token
            //in the sequence of tokens we create the embedding.
            boolean iAmFirstToken = true;
            boolean iAmLastToken = false;
            do {
                Token item = ts.token();
                //test if we are last token
                boolean hasNextToken = ts.moveNext();
                iAmLastToken = !(hasNextToken && ts.offset() < endOffset);
                if(hasNextToken) {
                    //rewind the tokenSequence back so the
                    //embedding is created on a proper token
                    ts.movePrevious();
                }
                if(ts.embedded(lang) == null) {
                    //the embedding doesn't exist, try to create
                    if(!ts.createEmbedding(lang, iAmFirstToken ? startSkipLength : 0, iAmLastToken ? endSkipLength : 0)) {
                        LOGGER.log(Level.WARNING, "Cannot create embedding for " + mimeType + " [" + startOffset + " - "  + endOffset + "] (" + item.text().toString() + ")");
                    } else {
                        LOGGER.log(Level.INFO, "Embedding for " + mimeType + " created [" + startOffset + " - "  + endOffset + "] (" + printEmbeddedText(item, iAmFirstToken ? startSkipLength : 0, iAmLastToken ? endSkipLength : 0) + ")");
                    }
                }
                iAmFirstToken = false;
                if(!iAmLastToken) {
                    ts.moveNext();
                }
            } while(!iAmLastToken);
        }finally {
            ((BaseDocument)doc).extWriteUnlock();
        }
    }
    
    private CharSequence printEmbeddedText(Token item, int startSkipLength, int endSkipLength) {
        StringBuffer sb = new StringBuffer(item.text());
        if(startSkipLength > 0) {
            sb.insert(startSkipLength, '[');
        }
        if(endSkipLength > 0) {
            sb.insert(sb.length() - endSkipLength, ']');
        }
        return sb;
    }
  
    private class JspASTEvaluator extends ASTEvaluator {
        
        private boolean sourceOK;
        
        private JspASTEvaluator(JspASTEvaluatorListener listener) {
            this.listener = listener;
        }
        
        private JspASTEvaluatorListener listener = null;
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
    
    private interface JspASTEvaluatorListener {
     
        public void evaluated(ASTNode root, boolean error);
        
    }
    
}
