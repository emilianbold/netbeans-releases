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
package org.netbeans.modules.web.core.syntax;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.jsp.lexer.JspTokenId;
//import org.netbeans.api.languages.ASTEvaluator;
//import org.netbeans.api.languages.ASTItem;
//import org.netbeans.api.languages.ASTNode;
//import org.netbeans.api.languages.ASTPath;
//import org.netbeans.api.languages.ParserManager;
//import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
//import org.netbeans.modules.languages.Feature;

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
        
//        THIS NEEDS TO BE REIMPLEMETED TO USE THE SYNTAX TREE PARSER
//        ParserManager parser = ParserManager.get(doc);
//        //I need to add my own AST evaluator since the parser status shows OK even the source
//        //is apparently broken
//        JspASTEvaluator evaluator = new JspASTEvaluator(new JspASTEvaluatorListener() {
//            public void evaluated(ASTNode root, boolean error) {
//                //parser finished, AST evaluated
//                updateEmbeddings(root);
//            }
//        });
//        parser.addASTEvaluator(evaluator);
    }
    
//    private void updateEmbeddings(ASTNode root) {
//        //do not ask what the hell is that...
//        List<JSP.Pair> java_code_blocks = JSP.java_code_blocks;
//        for(JSP.Pair pair : java_code_blocks) {
//            createEmbedding("text/x-java", pair.a, pair.b, 0, 0);//NOI18N
//        }
//    }
//    
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
  
//    private class JspASTEvaluator extends ASTEvaluator {
//        
//        private boolean sourceOK;
//        
//        private JspASTEvaluator(JspASTEvaluatorListener listener) {
//            this.listener = listener;
//        }
//        
//        private JspASTEvaluatorListener listener = null;
//        private ASTNode root;
//        
//        public void beforeEvaluation(State state, ASTNode root) {
//            sourceOK = true;
//            this.root = root;
//        }
//        
//        public void afterEvaluation(State state, ASTNode root) {
//            if(listener != null) {
//                listener.evaluated(root, !sourceOK);
//            }
//        }
//        
//        public void evaluate(State state, List<ASTItem> path, Feature feature) {
//            ASTItem item = path.get (path.size () - 1);
//            if(item instanceof ASTNode) {
//                if(((ASTNode)item).getNT().equals("ERROR")) { //NOI18N
//                    //source contains errors
//                    sourceOK = false;
//                }
//            }
//        }
//
//        public String getFeatureName() {
//            return null;
//        }
//        
//    }
//    
//    private interface JspASTEvaluatorListener {
//     
//        public void evaluated(ASTNode root, boolean error);
//        
//    }
    
}
