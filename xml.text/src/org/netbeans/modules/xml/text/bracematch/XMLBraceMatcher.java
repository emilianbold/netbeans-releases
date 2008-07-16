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
package org.netbeans.modules.xml.text.bracematch;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 *
 * @author Samaresh
 */
public class XMLBraceMatcher implements BracesMatcher {

    private static final String CDATA_START         = "<![CDATA[";  //NOI18N
    private static final String CDATA_END           = "]]>";        //NOI18N
    private static final String COMMENT_START       = "<!--";       //NOI18N
    private static final String COMMENT_END         = "->";         //NOI18N
    private static final String DECLARATION_START   = "<!DOCTYPE";  //NOI18N
    private static final String DECLARATION_END     = ">";          //NOI18N
    
    private MatcherContext context;

    public XMLBraceMatcher(MatcherContext context) {
        this.context = context;
    }

    public int[] findOrigin() throws InterruptedException, BadLocationException {
        if (MatcherContext.isTaskCanceled()) {
            return null;
        }
        AbstractDocument doc = (AbstractDocument)context.getDocument();
        doc.readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(doc);
            TokenSequence ts = th.tokenSequence();
            ts.move(context.getSearchOffset());
            Token token = ts.token();
            if(token == null) {
                ts.moveNext();
                token = ts.token();
                if(token == null)
                    return null;
            }
            
            XMLTokenId id = (XMLTokenId)token.id();
            switch(id) {
                case PI_START:
                case PI_END:
                case TAG: {
                    int start = ts.offset();
                    int end = start+token.length();
                    return new int[] {start, end};
                }
                case BLOCK_COMMENT: {
                    return getGenericOrigin(ts.offset(), token, COMMENT_START, COMMENT_END);
                }
                case CDATA_SECTION: {
                    return getGenericOrigin(ts.offset(), token, CDATA_START, CDATA_END);
                }
                case DECLARATION: {
                    return getGenericOrigin(ts.offset(), token, DECLARATION_START, DECLARATION_END);
                }
            }
        } finally {
            doc.readUnlock();
        }
        return null;
    }
    
    /**
     * For CDATA and XML comments, there is no start and end tokens differentiator.
     * XML lexer just gives us one token and we have to find the origin in there.
     */
    private int[] getGenericOrigin(int offset, Token token, String startTag, String endTag) {
        int start = offset;
        int end = start+startTag.length();
        
        //for start
        if(start <= context.getSearchOffset() && end > context.getSearchOffset())
            return new int[] {start, end};
        //for end 
        start = offset + token.length()-endTag.length();
        end = start+COMMENT_END.length();
        if(start <= context.getSearchOffset() && end >= context.getSearchOffset())
            return new int[] {start, end};
        
        //if none worked return null
        return null;
    }

    public int[] findMatches() throws InterruptedException, BadLocationException {
        if (MatcherContext.isTaskCanceled()) {
            return null;
        }
        if(!(context.getDocument() instanceof BaseDocument))
            return null;
        
        //TODO: move to lexer for finding matches.
        //issue 
        BaseDocument doc = (BaseDocument)context.getDocument();
        XMLSyntaxSupport support = ((XMLSyntaxSupport)doc.getSyntaxSupport());
        return support.findMatch(context.getSearchOffset(), true);
    }
    
    
}
