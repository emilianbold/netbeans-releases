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
package org.netbeans.modules.web.jsf.editor.hints;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.text.Document;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.syntax.completion.api.ELExpression;
import org.netbeans.modules.web.jsf.editor.el.JsfElExpression;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
public class ElChecker extends HintsProvider {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.editor.hints.HintsProvider#compute(org.netbeans.modules.web.jsf.editor.hints.RuleContext)
     */
    @Override
    public List<Hint> compute( RuleContext context ) {
        final Document doc = context.doc;
        final FileObject fileObject = context.parserResult.getSnapshot().getSource().
            getFileObject();
        final WebModule webModule = WebModule.getWebModule( fileObject);
        if ( webModule == null ){
            return Collections.emptyList();
        }
        final List<Hint> result = new LinkedList<Hint>();
        ((BaseDocument)doc).runAtomic(new Runnable(){
            public void run() {
                TokenHierarchy<?> th = TokenHierarchy.get(doc); 
                TokenSequence<?> topLevel = th.tokenSequence();
                topLevel.moveStart();
                while(topLevel.moveNext()) {
                    TokenSequence<ELTokenId> elTokenSequence = 
                        topLevel.embedded(ELTokenId.language());
                    if(elTokenSequence != null) {
                        elTokenSequence.moveEnd();
                        if ( !elTokenSequence.moveNext() ){
                            elTokenSequence.movePrevious();
                        }
                        Token<ELTokenId> token = elTokenSequence.token();
                        int offset = elTokenSequence.offset() + token.length();
                        checkEl( webModule , doc , offset , elTokenSequence , 
                                fileObject, result );
                    }
                }
            }
        });

        return result;
    }

    protected void checkEl( WebModule webModule, Document doc , int offset , 
            TokenSequence<ELTokenId> tokenSequence, FileObject fileObject,
            List<Hint> hints ) 
    {
        JsfElExpression elExpr = new JsfElExpression (webModule, doc);
        int parseType = elExpr.parse(offset);
        int startOffset = elExpr.getStartOffset();
        if ( startOffset == -1){
            tokenSequence.move(offset);
            if ( !tokenSequence.movePrevious() ){
                return;
            }
            checkEl(webModule, doc, tokenSequence.offset(), tokenSequence, 
                    fileObject, hints);
        }
        else {
            checkElContext( parseType, elExpr , hints , doc , fileObject );
            checkEl(webModule, doc, startOffset, tokenSequence, fileObject, hints);
        }
    }

    private void checkElContext( int parseType,  JsfElExpression expression, 
            List<Hint> hints , Document document, FileObject fileObject) 
    {
        ElContextChecker checker = CHECKERS.get( parseType);
        if ( checker != null ){
             checker.check(expression, document, fileObject, hints);
        }
    }
    
    private static final Map<Integer, ElContextChecker> CHECKERS = new HashMap<Integer, 
        ElContextChecker>();

    static {
        CHECKERS.put( ELExpression.EL_START,  new ElStartContextChecker());
        CHECKERS.put( ELExpression.EL_BEAN, new ElBeanContextChecker());
        CHECKERS.put( ELExpression.EL_IMPLICIT,  CHECKERS.get( ELExpression.EL_BEAN));
        
        CHECKERS.put( JsfElExpression.EL_JSF_BEAN, new JsfElBeanContextChecker());
        CHECKERS.put( JsfElExpression.EL_JSF_BEAN_REFERENCE, 
                CHECKERS.get(JsfElExpression.EL_JSF_BEAN));
        CHECKERS.put( JsfElExpression.EL_JSF_RESOURCE_BUNDLE, 
                new JsfElResourceBundleContextChecker());
    }
}
