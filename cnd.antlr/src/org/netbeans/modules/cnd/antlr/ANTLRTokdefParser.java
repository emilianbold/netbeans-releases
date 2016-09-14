/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
// $ANTLR : "tokdef.g" -> "ANTLRTokdefParser.java"$
 package org.netbeans.modules.cnd.antlr;
import org.netbeans.modules.cnd.antlr.TokenBuffer;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import org.netbeans.modules.cnd.antlr.TokenStreamIOException;
import org.netbeans.modules.cnd.antlr.ANTLRException;
import org.netbeans.modules.cnd.antlr.LLkParser;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.RecognitionException;
import org.netbeans.modules.cnd.antlr.NoViableAltException;
import org.netbeans.modules.cnd.antlr.MismatchedTokenException;
import org.netbeans.modules.cnd.antlr.SemanticException;
import org.netbeans.modules.cnd.antlr.collections.impl.BitSet;

/** Simple lexer/parser for reading token definition files
  in support of the import/export vocab option for grammars.
 */
public class ANTLRTokdefParser extends org.netbeans.modules.cnd.antlr.LLkParser       implements ANTLRTokdefParserTokenTypes
 {

	// This chunk of error reporting code provided by Brian Smith

    private org.netbeans.modules.cnd.antlr.Tool antlrTool;

    /** In order to make it so existing subclasses don't break, we won't require
     * that the org.netbeans.modules.cnd.antlr.Tool instance be passed as a constructor element. Instead,
     * the org.netbeans.modules.cnd.antlr.Tool instance should register itself via {@link #initTool(org.netbeans.modules.cnd.antlr.Tool)}
     * @throws IllegalStateException if a tool has already been registered
     * @since 2.7.2
     */
    public void setTool(org.netbeans.modules.cnd.antlr.Tool tool) {
        if (antlrTool == null) {
            antlrTool = tool;
		}
        else {
            throw new IllegalStateException("org.netbeans.modules.cnd.antlr.Tool already registered");
		}
    }

    /** @since 2.7.2 */
    protected org.netbeans.modules.cnd.antlr.Tool getTool() {
        return antlrTool;
    }

    /** Delegates the error message to the tool if any was registered via
     *  {@link #initTool(org.netbeans.modules.cnd.antlr.Tool)}
     *  @since 2.7.2
     */
    public void reportError(String s) {
        if (getTool() != null) {
            getTool().error(s, getFilename(), -1, -1);
		}
        else {
            super.reportError(s);
		}
    }

    /** Delegates the error message to the tool if any was registered via
     *  {@link #initTool(org.netbeans.modules.cnd.antlr.Tool)}
     *  @since 2.7.2
     */
    public void reportError(RecognitionException e) {
        if (getTool() != null) {
            getTool().error(e.getMessage(), e.getFilename(), e.getLine(), e.getColumn());
		}
        else {
            super.reportError(e);
		}
    }

    /** Delegates the warning message to the tool if any was registered via
     *  {@link #initTool(org.netbeans.modules.cnd.antlr.Tool)}
     *  @since 2.7.2
     */
    public void reportWarning(String s) {
        if (getTool() != null) {
            getTool().warning(s, getFilename(), -1, -1);
		}
        else {
            super.reportWarning(s);
		}
    }

protected ANTLRTokdefParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public ANTLRTokdefParser(TokenBuffer tokenBuf) {
  this(tokenBuf,3);
}

protected ANTLRTokdefParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public ANTLRTokdefParser(TokenStream lexer) {
  this(lexer,3);
}

	public final void file(
		ImportVocabTokenManager tm
	) throws RecognitionException, TokenStreamException {
		
		Token  name = null;
		
		try {      // for error handling
			name = LT(1);
			match(ID);
			{
			_loop225:
			do {
				if ((LA(1)==ID||LA(1)==STRING)) {
					line(tm);
				}
				else {
					break _loop225;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_0);
		}
	}
	
	public final void line(
		ImportVocabTokenManager tm
	) throws RecognitionException, TokenStreamException {
		
		Token  s1 = null;
		Token  lab = null;
		Token  s2 = null;
		Token  id = null;
		Token  para = null;
		Token  id2 = null;
		Token  i = null;
		Token t=null; Token s=null;
		
		try {      // for error handling
			{
			if ((LA(1)==STRING)) {
				s1 = LT(1);
				match(STRING);
				s = s1;
			}
			else if ((LA(1)==ID) && (LA(2)==ASSIGN) && (LA(3)==STRING)) {
				lab = LT(1);
				match(ID);
				t = lab;
				match(ASSIGN);
				s2 = LT(1);
				match(STRING);
				s = s2;
			}
			else if ((LA(1)==ID) && (LA(2)==LPAREN)) {
				id = LT(1);
				match(ID);
				t=id;
				match(LPAREN);
				para = LT(1);
				match(STRING);
				match(RPAREN);
			}
			else if ((LA(1)==ID) && (LA(2)==ASSIGN) && (LA(3)==INT)) {
				id2 = LT(1);
				match(ID);
				t=id2;
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			match(ASSIGN);
			i = LT(1);
			match(INT);
			
					Integer value = Integer.valueOf(i.getText());
					// if literal found, define as a string literal
					if ( s!=null ) {
						tm.define(s.getText(), value.intValue());
						// if label, then label the string and map label to token symbol also
						if ( t!=null ) {
							StringLiteralSymbol sl =
								(StringLiteralSymbol) tm.getTokenSymbol(s.getText());
							sl.setLabel(t.getText());
							tm.mapToTokenSymbol(t.getText(), sl);
						}
					}
					// define token (not a literal)
					else if ( t!=null ) {
						tm.define(t.getText(), value.intValue());
						if ( para!=null ) {
							TokenSymbol ts = tm.getTokenSymbol(t.getText());
							ts.setParaphrase(
								para.getText()
							);
						}
					}
					
		}
		catch (RecognitionException ex) {
			reportError(ex);
			consume();
			consumeUntil(_tokenSet_1);
		}
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"ID",
		"STRING",
		"ASSIGN",
		"LPAREN",
		"RPAREN",
		"INT",
		"WS",
		"SL_COMMENT",
		"ML_COMMENT",
		"ESC",
		"DIGIT",
		"XDIGIT"
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 50L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	
	}
