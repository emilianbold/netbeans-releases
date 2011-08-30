/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.editor.module.spi;

import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;

/**
 *
 * @author marekfukala
 */
public class CompletionContext extends EditorFeatureContext {

    private final QueryType queryType;
    private final int anchorOffset, embeddedCaretOffset, embeddedAnchorOffset, activeTokenDiff;
    private final String prefix;
    private final Node activeNode;
    private final TokenSequence<CssTokenId> tokenSequence;
    private final Node activeTokenNode;

    public CompletionContext(Node activeNode, Node activeTokeNode, CssParserResult result, 
            TokenSequence<CssTokenId> tokenSequence, int activeTokenDiff, 
            QueryType queryType, int caretOffset, int anchorOffset, int embeddedCaretOffset, 
            int embeddedAnchorOffset, String prefix) {
        super(result, caretOffset);
        this.tokenSequence = tokenSequence;
        this.activeNode = activeNode;
        this.activeTokenNode = activeTokeNode;
        this.queryType = queryType;
        this.anchorOffset = anchorOffset;
        this.embeddedCaretOffset = embeddedCaretOffset;
        this.embeddedAnchorOffset = embeddedAnchorOffset;
        this.prefix = prefix;
        this.activeTokenDiff = activeTokenDiff;
    }

    /**
     * 
     * @return a TokenSequence of Css tokens created on top of the *virtual* css source.
     * The TokenSequence is positioned on a token laying at the getAnchorOffset() offset.
     */
    @Override
    public TokenSequence<CssTokenId> getTokenSequence() {
        return tokenSequence;
    }
    
    public Node getActiveNode() {
        return activeNode;
    }

    public Node getActiveTokenNode() {
        return activeTokenNode;
    }
    
    /**
     * anchor offset = caret offset - prefix length.
     * Relative to the edited document.
     * 
     */
    public int getAnchorOffset() {
        return anchorOffset;
    }

    /**
     * Same as getCaretOffset() but relative to the embedded css code.
     */
    public int getEmbeddedCaretOffset() {
        return embeddedCaretOffset;
    }

    /**
     * Same as getAnchorOffset() but relative to the embedded css code.
     */
    public int getEmbeddedAnchorOffset() {
        return embeddedAnchorOffset;
    }

    public String getPrefix() {
        return prefix;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public int getActiveTokenDiff() {
        return activeTokenDiff;
    }
    
}
