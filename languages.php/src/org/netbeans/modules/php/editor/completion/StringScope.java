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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.editor.TokenUtils;

/**
 * Implementation of the <code>CompletionResultProvider</code> for the string 
 * scope.
 * @see http://www.php.net/manual/en/language.types.string.php#language.types.string.parsing
 * @author Victor G. Vasilyev
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.php.editor.completion.CompletionResultProvider.class)
public class StringScope extends ASTBasedProvider 
        implements CompletionResultProvider {
    
    private static final String SINGLE_QUOTE = "'";
    private static final String VARIABLE_PREFIX = "$";
    

    protected final List<CompletionProposal> proposalList = 
            new ArrayList<CompletionProposal>();

    public boolean isApplicable(CodeCompletionContext context) {
        init(context);
        if(!isMatchedCL1(CURR_TOKENS, ANY_TOKEN)) {
            return false;
        }
        String tokenText;
        try {
            TokenSequence ts = getTokenSequencePHP();
            Token t = TokenUtils.getEnteredToken(ts, getCaretOffset());
            tokenText = t.text().toString();
        } catch (IOException ex) {
            return false;
        } catch (Exception ex) {
            return false;
        }
        if(tokenText == null || tokenText.length() < 2) {
            return false;            
        }
        if(isSingleQuotedString(tokenText)) {
            // Note:  Unlike the two other syntaxes, variables and 
            // escape sequences for special characters will not be expanded 
            // when they occur in single quoted strings.
            return false;
        }
        // i.e. here we have either a Double quoted string or Heredoc string
        return true;
    }

    public List<CompletionProposal> getProposals(CodeCompletionContext context) {
        addVariableProposals(context);
        return proposalList;
    }
    
    private void addVariableProposals(CodeCompletionContext context) {
        String prefix = context.getPrefix();
        try {
            int indexOfVarPrefix = prefix.lastIndexOf(VARIABLE_PREFIX); // NPE !
            prefix = prefix.substring(indexOfVarPrefix); // IOBE !
            CodeCompletionContext newContext =
                    CodeCompletionContext.changePrefix(context, prefix);
            newContext.setCurrentSourceElement(newContext.getSourceElement());
            VariableProvider vp = new VariableProvider();
            List<CompletionProposal> pl = vp.getProposals(newContext);
            proposalList.addAll(pl);
        } catch (IndexOutOfBoundsException iobe) {
            // do nothing.
        } catch (NullPointerException npe) {
            // do nothing.
        }
    }

    protected void init(CodeCompletionContext context) {
        assert context != null;
        myContext = context;
        proposalList.clear();
    }
    
    private boolean isSingleQuotedString(String tokenText) {
        return tokenText != null && tokenText.startsWith(SINGLE_QUOTE) && 
                tokenText.endsWith(SINGLE_QUOTE);
    }

    private static final Set<ExpectedToken> CURR_TOKENS = new HashSet<ExpectedToken>();
    static {
        CURR_TOKENS.add(new ExpectedToken(TokenUtils.STRING, null));
        CURR_TOKENS.add(new ExpectedToken(TokenUtils.EOD_STRING, null));
    }
     
}
