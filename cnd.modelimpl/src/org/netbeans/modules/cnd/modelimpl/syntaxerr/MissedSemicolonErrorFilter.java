/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.syntaxerr;

import antlr.NoViableAltException;
import antlr.RecognitionException;
import antlr.Token;
import antlr.collections.impl.BitSet;
import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.syntaxerr.spi.ReadOnlyTokenBuffer;
import org.openide.util.NbBundle;

/**
 * Filters a "Missed semicolon" error
 * @author Vladimir Kvashin
 */
public class MissedSemicolonErrorFilter extends BaseParserErrorFilter {

    @Override
    public void filter(Collection<RecognitionException> parserErrors, Collection<CsmErrorInfo> result, ReadOnlyTokenBuffer buffer, CsmFile file) {
        for (RecognitionException e : parserErrors) {
            BitSet expected = getExpected(e);
            if (expected.member(CPPTokenTypes.SEMICOLON)) {
                String text = NbBundle.getMessage(getClass(), "MSG_MISSED_SEMICOLON"); //NOI18N
                int line = e.getLine();
                int col = e.getColumn();
                Integer tokenIndex = findTokenIndex(buffer, line, col);
                if( tokenIndex != null ) {
                    if (tokenIndex.intValue()>0 && buffer.LA(tokenIndex.intValue() - 1) != Token.EOF_TYPE) {
                        Token t = buffer.LT(tokenIndex.intValue() - 1);
                        line = t.getLine();
                        col = t.getColumn();
                    }
                }
                result.add(toErrorInfo(text, line, col, file));
//                result.remove(e);
                break;
//                BitSet t = (BitSet) expected.clone();
//                t.remove(CPPTokenTypes.SEMICOLON);
//                t.remove(CPPTokenTypes.EOF);
//                if (t.nil()) {
//                    String text = NbBundle.getMessage(getClass(), "MSG_MISSED_SEMICOLON"); //NOI18N
//                    result.add(toErrorInfo(text, e.getLine(), e.getColumn(), doc)); //NOI18N
//                    result.remove(e);
//                    break;
//                }
            }
        }
        //parserErrors.clear();
    }
    
    private Integer findTokenIndex(ReadOnlyTokenBuffer buffer, int line, int column) {
        int index = 0;
        while(buffer.LA(index) == Token.EOF_TYPE && index > -9) {
            index--;
        }
        if (buffer.LA(index) != Token.EOF_TYPE) {
            Token t = buffer.LT(index); 
            if (compare(t, line, column) > 0)  {
                // we are below the specified location 
                while (compare(t, line, column) > 0) {
                    t = buffer.LT(--index);
                }
            } else if (compare(t, line, column) < 0)  {
                // we are above the specified location
                while (compare(t, line, column) < 0) {
                    t = buffer.LT(++index);
                }
            }
            if (compare(t, line, column) == 0)  {
                return index;
            }
        }
        return null;
    }

    private int compare(Token t, int line, int column) {
        if( t.getLine() > line ) {
            return 1;
        } else if( t.getLine() == line ) {
            return t.getColumn() - column;
        } else {
            return -1;
        }
    }
    
//    private boolean left(Token t, int line, int column) {
//        
//    }

    /**
     * Gets expected BitSet 
     * (in the case this exception is NoViableAltException)
     * @param e exception
     * @return expected BitSet - NEVER NULL!!!
     */
    protected BitSet getExpected(RecognitionException e) {
        BitSet result = null;
        if (e instanceof NoViableAltException) {
            result = ((NoViableAltException) e).getExpected();
        } 
        return (result == null) ? new BitSet() : result;
    }
    
    protected boolean isExpected(RecognitionException e, int tokenType) {
        return getExpected(e).member(tokenType);
    }
    
//    protected boolean areExpected(RecognitionException e, int tokenType1, int tokenType2) {
//        BitSet expected =  getExpected(e);
//        return expected.member(tokenType1) && expected.member(tokenType2);
//    }
}
