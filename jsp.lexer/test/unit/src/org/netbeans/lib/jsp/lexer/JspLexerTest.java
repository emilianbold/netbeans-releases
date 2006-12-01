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

package org.netbeans.lib.jsp.lexer;

import java.io.File;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;


/**Jsp Lexer Test
 *
 * @author Marek.Fukala@Sun.COM
 */
public class JspLexerTest extends NbTestCase {
    
    public JspLexerTest() {
        super("JspLexerTest");
    }
    
    private CharSequence readFile(String fileName) throws IOException {
        File inputFile = new File(getDataDir(), fileName);
        return Utils.readFileContentToString(inputFile);
    }
    
    private static String getTokenInfo(Token token, TokenHierarchy tokenHierarchy) {
        return "TOKEN[text=\"" + token.text() + "\"; tokenId=" + token.id().name() + "; offset=" + token.offset(tokenHierarchy) + "]";
    }
    
    private void dumpTokens(CharSequence charSequence) {
        TokenHierarchy tokenHierarchy = TokenHierarchy.create(charSequence, JspTokenId.language());
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence();
        tokenSequence.moveFirst();
        do {
            getRef().println(getTokenInfo(tokenSequence.token(), tokenHierarchy));
        } while (tokenSequence.moveNext());
    }
    
    //test methods -----------
    
    public void testComplexJSP() throws BadLocationException, IOException {
        dumpTokens(readFile("input/JspLexerTest/testComplexJSP.jsp"));
        compareReferenceFiles();
    }
    
    
}
