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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.php.editor.completion;

import java.io.IOException;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.editor.TokenUtils;
import org.netbeans.modules.php.model.PhpModel;

/**
 *
 * @author Victor G. Vasilyev 
 */
public abstract class ASTBasedProvider {
    protected CodeCompletionContext myContext;
    
    @SuppressWarnings("unchecked")
    protected static final Set<ExpectedToken> ANY_TOKEN = Collections.EMPTY_SET;
    
    /**
     * A service that is used as the key to recognize a given project as the
     * PHP Project.
     */
//    private static final Class PHP_PROJECT_KEY_SERVICE = null;
        
    /**
     * Wrapps info about expected token.
     */
    protected static class ExpectedToken {
        private String type;
        private String text;
                        
        /**
         * Creates wrapper.
         * @param type expected type of the token (non-null value)
         * @param text expected text of the token, <code>null</code> means any 
         * text.
         */
        public ExpectedToken(String type, String text) {
            assert type != null;
            this.type = type;
            this.text = text;
        }

        public String getType() {
            return type;
        }

        public String getText() {
            return text;
        }
        
        public boolean isAnyText() {
            return text == null;
        }
    }
    
    /**
     *  
     * @param c the set of expected tokens in the current position.
     * @param l1 the set of expected tokens in the previous position.
     * @return <code>true</code> if expected tokens are matched.
     */
    protected boolean isMatchedCL1(Set<ExpectedToken> c, Set<ExpectedToken> l1) {
        try {
            TokenSequence ts = getTokenSequencePHP();
            Token t = TokenUtils.getEnteredToken(ts, getCaretOffset());
            if(c != ANY_TOKEN) {
                assertOneOf(t, c);
            }
            if(l1 != ANY_TOKEN) {
                boolean hasPreviousToken = ts.movePrevious();
                if(hasPreviousToken) {
                    t = ts.token();
                    assertOneOf(t, l1);
                }
            }
            return true;
        } catch (IOException ioe) {
            return false; // getDocument() fails
        } catch (ConcurrentModificationException cme) {
            // the token sequence is no longer valid because of an underlying 
            // mutable input source modification.
            return false;
        } catch (Exception e) {
            return false;
        }     
    }

    /**
     * Returns the <code>TokenSequence</code> of the PHP block pointed by 
     * the caret offset of the <code>CodeCompletionContext</code>.
     * @return the <code>TokenSequence</code> of the PHP block.
     * @throws java.io.IOException if {@link #getDocument()} fails
     * @throws java.lang.Exception if pointed code is not a PHP block or 
     * <code>TokenSequence</code> can't be returned for this code.
     */
    protected TokenSequence getTokenSequencePHP() throws IOException, Exception {
            assertPHPContext();
            Document doc = getDocument();
            int offset = getCaretOffset();
            TokenSequence ts = TokenUtils.getEmbeddedTokenSequence(doc, offset);
            if (ts == null) {
                throw new Exception();
            }
            return ts;
        
    }

    /**
     * Assert that a given <code>CodeCompletionContext</code> in the scope of a 
     * PHP project.
     * @throws java.lang.Exception if a given <code>CodeCompletionContext</code> 
     * is not in the scope of a PHP project.
     */
    protected void assertPHPProject() throws Exception {
//        FileObject fo = myContext.getCompilationInfo().getFileObject();
//        Project project = FileOwnerQuery.getOwner(fo);
//        if (project == null || 
//                project.getLookup().lookup(PHP_PROJECT_KEY_SERVICE) == null) {
            throw new Exception();
//        }
    }
    /**
     * Assert that a given <code>CodeCompletionContext</code> in the file that 
     * has a MIME type registered in the IDE for PHP processing.
     * @throws java.lang.Exception if a given <code>CodeCompletionContext</code> 
     * is not in the in the file that has a MIME type registered in the IDE for 
     * PHP processing.
     */
    protected void assertMIMETypePHP() throws Exception {
        String type = myContext.getCompilationInfo().getFileObject().getMIMEType();
        if (!type.equals("text/x-php5")) {
            throw new Exception();
        }
    }


    protected void assertPHPContext() throws IOException, Exception {
        Document doc = getDocument();
        int offset = getCaretOffset();
        if (!TokenUtils.checkPhp(doc, offset)) {
            throw new Exception();
        }
    }

    protected void assertWhitespace(Token t) throws Exception {
        if (!TokenUtils.PHPTokenName.WHITESPACE.value().equals(TokenUtils.getTokenType(t))) {
            throw new Exception();
        }
    }

    protected void assertOneOf(Token t, Set<ExpectedToken> tokenSet) throws Exception {
        String tType = TokenUtils.getTokenType(t);
        String tText = t.text().toString();
        for(ExpectedToken et : tokenSet) {
            if(et.getType().equals(tType)) {
                String etText = et.getText();
                if(et.isAnyText()) {
                    return;
                }
                if(etText.equals(tText)) {
                    return;
                }
            }
        }
        throw new Exception();
    }

    protected Document getDocument() throws IOException {
        return myContext.getCompilationInfo().getDocument();
    }

    protected int getCaretOffset() {
        return myContext.getCaretOffset();
    }
    
    protected HtmlFormatter getFormatter() {
       return myContext.getFormatter();
    }
    
    protected PhpModel getModel() {
        return myContext.getSourceElement().getModel();
    }
    
    protected void checkContext(CodeCompletionContext context) throws IllegalStateException{
        if (context != myContext) {
            throw new IllegalStateException("The isApplicable method MUST BE called before.");
        }
    }
    
}
