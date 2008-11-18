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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.languages.php.lang.Keywords;
import org.netbeans.modules.languages.php.lang.Keywords;
import org.netbeans.modules.languages.php.lang.MagicConstants;
import org.netbeans.modules.php.editor.TokenUtils;
import org.netbeans.modules.php.model.SourceElement;

/**
 * Implementation of the <code>CompletionResultProvider</code> for the new 
 * instruction context.
 * <p><b>Note that this implementation is not synchronized.</b></p> 
 * 
 * @author Victor G. Vasilyev 
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.php.editor.completion.CompletionResultProvider.class)
public class NewInstructionContext extends ASTBasedProvider 
        implements CompletionResultProvider {
    
    protected final List<CompletionProposal> proposalList = 
            new ArrayList<CompletionProposal>();
    
    /**
     * Returns <code>true</code> iif the specified <code>context</code>
     * is applicable for inserting a new instruction.
     * E.g 
     * <p> 
     * <b>&lt;?php</b> 
     *     <span style="color: rgb(255, 0, 0);"><blink>|</blink></span>
     *     ... <b>?&gt;</b>
     * </p> 
     * or
     * <p>
     * <b>&lt;?php</b> <i>Instruction</i><b>;</b> 
     *    <span style="color: rgb(255, 0, 0);"><blink>|</blink></span>
     *     ... <b>?&gt;</b>
     * </p>
     * 
     * @param context
     * @return <code>true</code> iif the specified <code>context</code>
     * is applicable.
     */
    public boolean isApplicable(CodeCompletionContext context) {
        init(context);
        if(isMatchedCL1(CURR_TOKENS, PREV_TOKENS)) {
            return true;
        }
        SourceElement e = context.getSourceElement();
        if(e != null && ExpressionContext.isNewIncompletedStatement(e)) {
            return true;
        }
        return false;
    }

    public List<CompletionProposal> getProposals(final CodeCompletionContext context) {
        checkContext(context);
        addStaticProposals();
        ExpressionContext.addBuiltinFunctionProposals(proposalList, context);
        ExpressionContext.addUserDefinedFunctionProposals(proposalList, context);
        return proposalList;
    }
    
    protected void init(CodeCompletionContext context) {
        assert context != null;
        myContext = context;
        proposalList.clear();
    }
    
    /** Adds the proposals that are applicable to any context. */
    protected void addStaticProposals() {
        String prefix = myContext.getPrefix();
        int insertOffset = myContext.getInsertOffset();
        HtmlFormatter formater = myContext.getFormatter();
        for(Keywords k: KEYWORDS) {
            if(myContext.isEmptyPrefix() || k.isMatched(prefix)) {
                proposalList.add(new KeywordItem(k, insertOffset, formater));
            }
        }
        for(MagicConstants mc: MagicConstants.values()) {
            if(myContext.isEmptyPrefix() || mc.isMatched(prefix)) {
                proposalList.add(new MagicConstantItem(mc, insertOffset, formater));
            }            
        }
    }
    
    
    private static final Set<Keywords> KEYWORDS = new HashSet<Keywords>();
    static {
        KEYWORDS.add(Keywords.CLASS);
        KEYWORDS.add(Keywords.FUNCTION);
        KEYWORDS.add(Keywords.DECLARE);
        KEYWORDS.add(Keywords.ARRAY);
        KEYWORDS.add(Keywords.CLASS);
        KEYWORDS.add(Keywords.DIE);
        KEYWORDS.add(Keywords.DECLARE);
        KEYWORDS.add(Keywords.DO);
        KEYWORDS.add(Keywords.ECHO);
        KEYWORDS.add(Keywords.EMPTY);
        KEYWORDS.add(Keywords.EVAL);
        KEYWORDS.add(Keywords.EXIT);
        KEYWORDS.add(Keywords.FOR);
        KEYWORDS.add(Keywords.FOREACH);
        KEYWORDS.add(Keywords.FUNCTION);
        KEYWORDS.add(Keywords.IF);
        KEYWORDS.add(Keywords.INCLUDE);
        KEYWORDS.add(Keywords.INCLUDE_ONCE);
        KEYWORDS.add(Keywords.ISSET);
        KEYWORDS.add(Keywords.LIST);
        KEYWORDS.add(Keywords.NEW);
        KEYWORDS.add(Keywords.PRINT);
        KEYWORDS.add(Keywords.REQUIRE);
        KEYWORDS.add(Keywords.REQUIRE_ONCE);
        KEYWORDS.add(Keywords.RETURN);
        KEYWORDS.add(Keywords.SWITCH);
        KEYWORDS.add(Keywords.UNSET);
        KEYWORDS.add(Keywords.WHILE);
        KEYWORDS.add(Keywords.INTERFACE);
        KEYWORDS.add(Keywords.ABSTRACT);
        KEYWORDS.add(Keywords.TRY);
        KEYWORDS.add(Keywords.TROW);
    }
    
    private static final Set<ExpectedToken> PREV_TOKENS = new HashSet<ExpectedToken>();
    static {
        PREV_TOKENS.add(new ExpectedToken(TokenUtils.PHPTokenName.SEPARATOR.value(), null));
        PREV_TOKENS.add(new ExpectedToken(TokenUtils.PHPTokenName.BLOCK_COMMENT.value(), null));
        PREV_TOKENS.add(new ExpectedToken(TokenUtils.PHPTokenName.LINE_COMMENT.value(), null));
        PREV_TOKENS.add(new ExpectedToken(TokenUtils.PHPTokenName.WHITESPACE.value(), null));
    }   
    
    private static final Set<ExpectedToken> CURR_TOKENS = new HashSet<ExpectedToken>();
    static {
        CURR_TOKENS.add(new ExpectedToken(TokenUtils.PHPTokenName.WHITESPACE.value(), null));
    }
     
}
