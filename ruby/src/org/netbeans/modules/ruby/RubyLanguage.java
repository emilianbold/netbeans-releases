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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.gsf.GsfLanguage;
import org.netbeans.api.gsf.GsfTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.ruby.lexer.RubyCommentTokenId;
import org.netbeans.modules.ruby.lexer.RubyStringTokenId;
import org.netbeans.modules.ruby.lexer.RubyTokenId;


/*
 * Language/lexing configuration for Ruby
 *
 * @author Tor Norbye
 */
public class RubyLanguage implements GsfLanguage {
    public RubyLanguage() {
    }

    public List<?extends TokenId> getRelevantTokenTypes() {
        List<TokenId> list = new ArrayList<TokenId>(30);
        list.addAll(RubyTokenId.getUsedTokens());
        list.add(RubyStringTokenId.STRING_TEXT);
        list.add(RubyStringTokenId.STRING_ESCAPE);
        list.add(RubyStringTokenId.STRING_INVALID);
        list.add(RubyCommentTokenId.COMMENT_TEXT);
        list.add(RubyCommentTokenId.COMMENT_TODO);
        list.add(RubyCommentTokenId.COMMENT_RDOC);
        list.add(RubyCommentTokenId.COMMENT_HTMLTAG);
        list.add(RubyCommentTokenId.COMMENT_LINK);

        return list;
    }

    public String getLineCommentPrefix() {
        return RubyUtils.getLineCommentPrefix();
    }

    public boolean isIdentifierChar(char c) {
        return RubyUtils.isIdentifierChar(c);
    }

    public Language getLexerLanguage() {
        return RubyTokenId.language();
    }
}
