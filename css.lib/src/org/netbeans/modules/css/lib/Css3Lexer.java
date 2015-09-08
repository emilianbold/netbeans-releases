// $ANTLR 3.5.2 /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2015-09-08 12:04:03

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
package org.netbeans.modules.css.lib;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@SuppressWarnings("all")
public class Css3Lexer extends Lexer {
	public static final int EOF=-1;
	public static final int A=4;
	public static final int ANGLE=5;
	public static final int AT_IDENT=6;
	public static final int AT_SIGN=7;
	public static final int B=8;
	public static final int BEGINS=9;
	public static final int BOTTOMCENTER_SYM=10;
	public static final int BOTTOMLEFTCORNER_SYM=11;
	public static final int BOTTOMLEFT_SYM=12;
	public static final int BOTTOMRIGHTCORNER_SYM=13;
	public static final int BOTTOMRIGHT_SYM=14;
	public static final int C=15;
	public static final int CDC=16;
	public static final int CDO=17;
	public static final int CHARSET_SYM=18;
	public static final int COLON=19;
	public static final int COMMA=20;
	public static final int COMMENT=21;
	public static final int CONTAINS=22;
	public static final int COUNTER_STYLE_SYM=23;
	public static final int CP_DOTS=24;
	public static final int CP_EQ=25;
	public static final int CP_NOT_EQ=26;
	public static final int D=27;
	public static final int DASHMATCH=28;
	public static final int DCOLON=29;
	public static final int DIMENSION=30;
	public static final int DOT=31;
	public static final int E=32;
	public static final int EMS=33;
	public static final int ENDS=34;
	public static final int ESCAPE=35;
	public static final int EXCLAMATION_MARK=36;
	public static final int EXS=37;
	public static final int F=38;
	public static final int FONT_FACE_SYM=39;
	public static final int FREQ=40;
	public static final int G=41;
	public static final int GEN=42;
	public static final int GREATER=43;
	public static final int GREATER_OR_EQ=44;
	public static final int H=45;
	public static final int HASH=46;
	public static final int HASH_SYMBOL=47;
	public static final int HEXCHAR=48;
	public static final int I=49;
	public static final int IDENT=50;
	public static final int IMPORTANT_SYM=51;
	public static final int IMPORT_SYM=52;
	public static final int INCLUDES=53;
	public static final int INVALID=54;
	public static final int J=55;
	public static final int K=56;
	public static final int L=57;
	public static final int LBRACE=58;
	public static final int LBRACKET=59;
	public static final int LEFTBOTTOM_SYM=60;
	public static final int LEFTMIDDLE_SYM=61;
	public static final int LEFTTOP_SYM=62;
	public static final int LENGTH=63;
	public static final int LESS=64;
	public static final int LESS_AND=65;
	public static final int LESS_JS_STRING=66;
	public static final int LESS_OR_EQ=67;
	public static final int LESS_REST=68;
	public static final int LINE_COMMENT=69;
	public static final int LPAREN=70;
	public static final int M=71;
	public static final int MEDIA_SYM=72;
	public static final int MINUS=73;
	public static final int MOZ_DOCUMENT_SYM=74;
	public static final int MOZ_DOMAIN=75;
	public static final int MOZ_REGEXP=76;
	public static final int MOZ_URL_PREFIX=77;
	public static final int N=78;
	public static final int NAME=79;
	public static final int NAMESPACE_SYM=80;
	public static final int NL=81;
	public static final int NMCHAR=82;
	public static final int NMSTART=83;
	public static final int NONASCII=84;
	public static final int NOT=85;
	public static final int NUMBER=86;
	public static final int O=87;
	public static final int OPEQ=88;
	public static final int P=89;
	public static final int PAGE_SYM=90;
	public static final int PERCENTAGE=91;
	public static final int PERCENTAGE_SYMBOL=92;
	public static final int PIPE=93;
	public static final int PLUS=94;
	public static final int Q=95;
	public static final int R=96;
	public static final int RBRACE=97;
	public static final int RBRACKET=98;
	public static final int REM=99;
	public static final int RESOLUTION=100;
	public static final int RIGHTBOTTOM_SYM=101;
	public static final int RIGHTMIDDLE_SYM=102;
	public static final int RIGHTTOP_SYM=103;
	public static final int RPAREN=104;
	public static final int S=105;
	public static final int SASS_AT_ROOT=106;
	public static final int SASS_CONTENT=107;
	public static final int SASS_DEBUG=108;
	public static final int SASS_DEFAULT=109;
	public static final int SASS_EACH=110;
	public static final int SASS_ELSE=111;
	public static final int SASS_ELSEIF=112;
	public static final int SASS_ERROR=113;
	public static final int SASS_EXTEND=114;
	public static final int SASS_EXTEND_ONLY_SELECTOR=115;
	public static final int SASS_FOR=116;
	public static final int SASS_FUNCTION=117;
	public static final int SASS_GLOBAL=118;
	public static final int SASS_IF=119;
	public static final int SASS_INCLUDE=120;
	public static final int SASS_MIXIN=121;
	public static final int SASS_OPTIONAL=122;
	public static final int SASS_RETURN=123;
	public static final int SASS_VAR=124;
	public static final int SASS_WARN=125;
	public static final int SASS_WHILE=126;
	public static final int SEMI=127;
	public static final int SOLIDUS=128;
	public static final int STAR=129;
	public static final int STRING=130;
	public static final int T=131;
	public static final int TILDE=132;
	public static final int TIME=133;
	public static final int TOPCENTER_SYM=134;
	public static final int TOPLEFTCORNER_SYM=135;
	public static final int TOPLEFT_SYM=136;
	public static final int TOPRIGHTCORNER_SYM=137;
	public static final int TOPRIGHT_SYM=138;
	public static final int U=139;
	public static final int UNICODE=140;
	public static final int URI=141;
	public static final int URL=142;
	public static final int V=143;
	public static final int W=144;
	public static final int WEBKIT_KEYFRAMES_SYM=145;
	public static final int WS=146;
	public static final int X=147;
	public static final int Y=148;
	public static final int Z=149;

	    protected boolean isLessSource() {
	        return false;
	    }

	    protected boolean isScssSource() {
	        return false;
	    }

	    private boolean isCssPreprocessorSource() {
	        return isLessSource() || isScssSource();
	    }


	// delegates
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public Css3Lexer() {} 
	public Css3Lexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public Css3Lexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "/home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g"; }

	// $ANTLR start "GEN"
	public final void mGEN() throws RecognitionException {
		try {
			int _type = GEN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1429:25: ( '@@@' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1429:27: '@@@'
			{
			match("@@@"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "GEN"

	// $ANTLR start "HEXCHAR"
	public final void mHEXCHAR() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1431:25: ( ( 'a' .. 'f' | 'A' .. 'F' | '0' .. '9' ) )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
			{
			if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
				input.consume();
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "HEXCHAR"

	// $ANTLR start "NONASCII"
	public final void mNONASCII() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1433:25: ( '\\u0080' .. '\\uFFFF' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
			{
			if ( (input.LA(1) >= '\u0080' && input.LA(1) <= '\uFFFF') ) {
				input.consume();
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				recover(mse);
				throw mse;
			}
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NONASCII"

	// $ANTLR start "UNICODE"
	public final void mUNICODE() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1435:25: ( '\\\\' HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )? ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1435:27: '\\\\' HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )? ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
			{
			match('\\'); if (state.failed) return;
			mHEXCHAR(); if (state.failed) return;

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1436:33: ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )?
			int alt5=2;
			int LA5_0 = input.LA(1);
			if ( ((LA5_0 >= '0' && LA5_0 <= '9')||(LA5_0 >= 'A' && LA5_0 <= 'F')||(LA5_0 >= 'a' && LA5_0 <= 'f')) ) {
				alt5=1;
			}
			switch (alt5) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1436:34: HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )?
					{
					mHEXCHAR(); if (state.failed) return;

					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1437:37: ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )?
					int alt4=2;
					int LA4_0 = input.LA(1);
					if ( ((LA4_0 >= '0' && LA4_0 <= '9')||(LA4_0 >= 'A' && LA4_0 <= 'F')||(LA4_0 >= 'a' && LA4_0 <= 'f')) ) {
						alt4=1;
					}
					switch (alt4) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1437:38: HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )?
							{
							mHEXCHAR(); if (state.failed) return;

							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:41: ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )?
							int alt3=2;
							int LA3_0 = input.LA(1);
							if ( ((LA3_0 >= '0' && LA3_0 <= '9')||(LA3_0 >= 'A' && LA3_0 <= 'F')||(LA3_0 >= 'a' && LA3_0 <= 'f')) ) {
								alt3=1;
							}
							switch (alt3) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:42: HEXCHAR ( HEXCHAR ( HEXCHAR )? )?
									{
									mHEXCHAR(); if (state.failed) return;

									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1439:45: ( HEXCHAR ( HEXCHAR )? )?
									int alt2=2;
									int LA2_0 = input.LA(1);
									if ( ((LA2_0 >= '0' && LA2_0 <= '9')||(LA2_0 >= 'A' && LA2_0 <= 'F')||(LA2_0 >= 'a' && LA2_0 <= 'f')) ) {
										alt2=1;
									}
									switch (alt2) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1439:46: HEXCHAR ( HEXCHAR )?
											{
											mHEXCHAR(); if (state.failed) return;

											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1439:54: ( HEXCHAR )?
											int alt1=2;
											int LA1_0 = input.LA(1);
											if ( ((LA1_0 >= '0' && LA1_0 <= '9')||(LA1_0 >= 'A' && LA1_0 <= 'F')||(LA1_0 >= 'a' && LA1_0 <= 'f')) ) {
												alt1=1;
											}
											switch (alt1) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
													{
													if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'F')||(input.LA(1) >= 'a' && input.LA(1) <= 'f') ) {
														input.consume();
														state.failed=false;
													}
													else {
														if (state.backtracking>0) {state.failed=true; return;}
														MismatchedSetException mse = new MismatchedSetException(null,input);
														recover(mse);
														throw mse;
													}
													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							}
							break;

					}

					}
					break;

			}

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1443:33: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
			loop6:
			while (true) {
				int alt6=2;
				int LA6_0 = input.LA(1);
				if ( ((LA6_0 >= '\t' && LA6_0 <= '\n')||(LA6_0 >= '\f' && LA6_0 <= '\r')||LA6_0==' ') ) {
					alt6=1;
				}

				switch (alt6) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
					{
					if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||(input.LA(1) >= '\f' && input.LA(1) <= '\r')||input.LA(1)==' ' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop6;
				}
			}

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "UNICODE"

	// $ANTLR start "ESCAPE"
	public final void mESCAPE() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1445:25: ( UNICODE | '\\\\' ~ ( '\\r' | '\\n' | '\\f' | HEXCHAR ) )
			int alt7=2;
			int LA7_0 = input.LA(1);
			if ( (LA7_0=='\\') ) {
				int LA7_1 = input.LA(2);
				if ( ((LA7_1 >= '\u0000' && LA7_1 <= '\t')||LA7_1=='\u000B'||(LA7_1 >= '\u000E' && LA7_1 <= '/')||(LA7_1 >= ':' && LA7_1 <= '@')||(LA7_1 >= 'G' && LA7_1 <= '`')||(LA7_1 >= 'g' && LA7_1 <= '\uFFFF')) ) {
					alt7=2;
				}
				else if ( ((LA7_1 >= '0' && LA7_1 <= '9')||(LA7_1 >= 'A' && LA7_1 <= 'F')||(LA7_1 >= 'a' && LA7_1 <= 'f')) ) {
					alt7=1;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 7, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 7, 0, input);
				throw nvae;
			}

			switch (alt7) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1445:27: UNICODE
					{
					mUNICODE(); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1445:37: '\\\\' ~ ( '\\r' | '\\n' | '\\f' | HEXCHAR )
					{
					match('\\'); if (state.failed) return;
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||input.LA(1)=='\u000B'||(input.LA(1) >= '\u000E' && input.LA(1) <= '/')||(input.LA(1) >= ':' && input.LA(1) <= '@')||(input.LA(1) >= 'G' && input.LA(1) <= '`')||(input.LA(1) >= 'g' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ESCAPE"

	// $ANTLR start "NMSTART"
	public final void mNMSTART() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1447:25: ( '_' | 'a' .. 'z' | 'A' .. 'Z' | NONASCII | ESCAPE )
			int alt8=5;
			int LA8_0 = input.LA(1);
			if ( (LA8_0=='_') ) {
				alt8=1;
			}
			else if ( ((LA8_0 >= 'a' && LA8_0 <= 'z')) ) {
				alt8=2;
			}
			else if ( ((LA8_0 >= 'A' && LA8_0 <= 'Z')) ) {
				alt8=3;
			}
			else if ( ((LA8_0 >= '\u0080' && LA8_0 <= '\uFFFF')) ) {
				alt8=4;
			}
			else if ( (LA8_0=='\\') ) {
				alt8=5;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 8, 0, input);
				throw nvae;
			}

			switch (alt8) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1447:27: '_'
					{
					match('_'); if (state.failed) return;
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1448:27: 'a' .. 'z'
					{
					matchRange('a','z'); if (state.failed) return;
					}
					break;
				case 3 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1449:27: 'A' .. 'Z'
					{
					matchRange('A','Z'); if (state.failed) return;
					}
					break;
				case 4 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1450:27: NONASCII
					{
					mNONASCII(); if (state.failed) return;

					}
					break;
				case 5 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1451:27: ESCAPE
					{
					mESCAPE(); if (state.failed) return;

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NMSTART"

	// $ANTLR start "NMCHAR"
	public final void mNMCHAR() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:25: ( '_' | 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '-' | NONASCII | ESCAPE )
			int alt9=7;
			int LA9_0 = input.LA(1);
			if ( (LA9_0=='_') ) {
				alt9=1;
			}
			else if ( ((LA9_0 >= 'a' && LA9_0 <= 'z')) ) {
				alt9=2;
			}
			else if ( ((LA9_0 >= 'A' && LA9_0 <= 'Z')) ) {
				alt9=3;
			}
			else if ( ((LA9_0 >= '0' && LA9_0 <= '9')) ) {
				alt9=4;
			}
			else if ( (LA9_0=='-') ) {
				alt9=5;
			}
			else if ( ((LA9_0 >= '\u0080' && LA9_0 <= '\uFFFF')) ) {
				alt9=6;
			}
			else if ( (LA9_0=='\\') ) {
				alt9=7;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 9, 0, input);
				throw nvae;
			}

			switch (alt9) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:27: '_'
					{
					match('_'); if (state.failed) return;
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1455:27: 'a' .. 'z'
					{
					matchRange('a','z'); if (state.failed) return;
					}
					break;
				case 3 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1456:27: 'A' .. 'Z'
					{
					matchRange('A','Z'); if (state.failed) return;
					}
					break;
				case 4 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1457:27: '0' .. '9'
					{
					matchRange('0','9'); if (state.failed) return;
					}
					break;
				case 5 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1458:27: '-'
					{
					match('-'); if (state.failed) return;
					}
					break;
				case 6 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1459:27: NONASCII
					{
					mNONASCII(); if (state.failed) return;

					}
					break;
				case 7 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1460:27: ESCAPE
					{
					mESCAPE(); if (state.failed) return;

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NMCHAR"

	// $ANTLR start "NAME"
	public final void mNAME() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1463:25: ( ( NMCHAR )+ )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1463:27: ( NMCHAR )+
			{
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1463:27: ( NMCHAR )+
			int cnt10=0;
			loop10:
			while (true) {
				int alt10=2;
				int LA10_0 = input.LA(1);
				if ( (LA10_0=='-'||(LA10_0 >= '0' && LA10_0 <= '9')||(LA10_0 >= 'A' && LA10_0 <= 'Z')||LA10_0=='\\'||LA10_0=='_'||(LA10_0 >= 'a' && LA10_0 <= 'z')||(LA10_0 >= '\u0080' && LA10_0 <= '\uFFFF')) ) {
					alt10=1;
				}

				switch (alt10) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1463:27: NMCHAR
					{
					mNMCHAR(); if (state.failed) return;

					}
					break;

				default :
					if ( cnt10 >= 1 ) break loop10;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(10, input);
					throw eee;
				}
				cnt10++;
			}

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NAME"

	// $ANTLR start "URL"
	public final void mURL() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1465:25: ( ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | '@' | '|' | NMCHAR )* )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1465:27: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | '@' | '|' | NMCHAR )*
			{
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1465:27: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | '@' | '|' | NMCHAR )*
			loop11:
			while (true) {
				int alt11=20;
				int LA11_0 = input.LA(1);
				if ( (LA11_0=='[') ) {
					alt11=1;
				}
				else if ( (LA11_0=='!') ) {
					alt11=2;
				}
				else if ( (LA11_0=='#') ) {
					alt11=3;
				}
				else if ( (LA11_0=='$') ) {
					alt11=4;
				}
				else if ( (LA11_0=='%') ) {
					alt11=5;
				}
				else if ( (LA11_0=='&') ) {
					alt11=6;
				}
				else if ( (LA11_0=='*') ) {
					alt11=7;
				}
				else if ( (LA11_0=='~') ) {
					alt11=8;
				}
				else if ( (LA11_0=='.') ) {
					alt11=9;
				}
				else if ( (LA11_0==':') ) {
					alt11=10;
				}
				else if ( (LA11_0=='/') ) {
					alt11=11;
				}
				else if ( (LA11_0=='?') ) {
					alt11=12;
				}
				else if ( (LA11_0=='=') ) {
					alt11=13;
				}
				else if ( (LA11_0==';') ) {
					alt11=14;
				}
				else if ( (LA11_0==',') ) {
					alt11=15;
				}
				else if ( (LA11_0=='+') ) {
					alt11=16;
				}
				else if ( (LA11_0=='@') ) {
					alt11=17;
				}
				else if ( (LA11_0=='|') ) {
					alt11=18;
				}
				else if ( (LA11_0=='-'||(LA11_0 >= '0' && LA11_0 <= '9')||(LA11_0 >= 'A' && LA11_0 <= 'Z')||LA11_0=='\\'||LA11_0=='_'||(LA11_0 >= 'a' && LA11_0 <= 'z')||(LA11_0 >= '\u0080' && LA11_0 <= '\uFFFF')) ) {
					alt11=19;
				}

				switch (alt11) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:31: '['
					{
					match('['); if (state.failed) return;
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:35: '!'
					{
					match('!'); if (state.failed) return;
					}
					break;
				case 3 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:39: '#'
					{
					match('#'); if (state.failed) return;
					}
					break;
				case 4 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:43: '$'
					{
					match('$'); if (state.failed) return;
					}
					break;
				case 5 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:47: '%'
					{
					match('%'); if (state.failed) return;
					}
					break;
				case 6 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:51: '&'
					{
					match('&'); if (state.failed) return;
					}
					break;
				case 7 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:55: '*'
					{
					match('*'); if (state.failed) return;
					}
					break;
				case 8 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:59: '~'
					{
					match('~'); if (state.failed) return;
					}
					break;
				case 9 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:63: '.'
					{
					match('.'); if (state.failed) return;
					}
					break;
				case 10 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:67: ':'
					{
					match(':'); if (state.failed) return;
					}
					break;
				case 11 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:71: '/'
					{
					match('/'); if (state.failed) return;
					}
					break;
				case 12 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:75: '?'
					{
					match('?'); if (state.failed) return;
					}
					break;
				case 13 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:79: '='
					{
					match('='); if (state.failed) return;
					}
					break;
				case 14 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:83: ';'
					{
					match(';'); if (state.failed) return;
					}
					break;
				case 15 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:87: ','
					{
					match(','); if (state.failed) return;
					}
					break;
				case 16 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:91: '+'
					{
					match('+'); if (state.failed) return;
					}
					break;
				case 17 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:95: '@'
					{
					match('@'); if (state.failed) return;
					}
					break;
				case 18 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:99: '|'
					{
					match('|'); if (state.failed) return;
					}
					break;
				case 19 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1467:31: NMCHAR
					{
					mNMCHAR(); if (state.failed) return;

					}
					break;

				default :
					break loop11;
				}
			}

			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "URL"

	// $ANTLR start "A"
	public final void mA() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1474:17: ( ( 'a' | 'A' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '1' )
			int alt16=2;
			int LA16_0 = input.LA(1);
			if ( (LA16_0=='A'||LA16_0=='a') ) {
				alt16=1;
			}
			else if ( (LA16_0=='\\') ) {
				alt16=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 16, 0, input);
				throw nvae;
			}

			switch (alt16) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1474:21: ( 'a' | 'A' )
					{
					if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1475:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '1'
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1475:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
					int alt15=2;
					int LA15_0 = input.LA(1);
					if ( (LA15_0=='0') ) {
						alt15=1;
					}
					switch (alt15) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1475:27: '0' ( '0' ( '0' ( '0' )? )? )?
							{
							match('0'); if (state.failed) return;
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1475:31: ( '0' ( '0' ( '0' )? )? )?
							int alt14=2;
							int LA14_0 = input.LA(1);
							if ( (LA14_0=='0') ) {
								alt14=1;
							}
							switch (alt14) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1475:32: '0' ( '0' ( '0' )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1475:36: ( '0' ( '0' )? )?
									int alt13=2;
									int LA13_0 = input.LA(1);
									if ( (LA13_0=='0') ) {
										alt13=1;
									}
									switch (alt13) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1475:37: '0' ( '0' )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1475:41: ( '0' )?
											int alt12=2;
											int LA12_0 = input.LA(1);
											if ( (LA12_0=='0') ) {
												alt12=1;
											}
											switch (alt12) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1475:41: '0'
													{
													match('0'); if (state.failed) return;
													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							}
							break;

					}

					if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					match('1'); if (state.failed) return;
					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "A"

	// $ANTLR start "B"
	public final void mB() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1477:17: ( ( 'b' | 'B' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '2' )
			int alt21=2;
			int LA21_0 = input.LA(1);
			if ( (LA21_0=='B'||LA21_0=='b') ) {
				alt21=1;
			}
			else if ( (LA21_0=='\\') ) {
				alt21=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 21, 0, input);
				throw nvae;
			}

			switch (alt21) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1477:21: ( 'b' | 'B' )
					{
					if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '2'
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
					int alt20=2;
					int LA20_0 = input.LA(1);
					if ( (LA20_0=='0') ) {
						alt20=1;
					}
					switch (alt20) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:27: '0' ( '0' ( '0' ( '0' )? )? )?
							{
							match('0'); if (state.failed) return;
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:31: ( '0' ( '0' ( '0' )? )? )?
							int alt19=2;
							int LA19_0 = input.LA(1);
							if ( (LA19_0=='0') ) {
								alt19=1;
							}
							switch (alt19) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:32: '0' ( '0' ( '0' )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:36: ( '0' ( '0' )? )?
									int alt18=2;
									int LA18_0 = input.LA(1);
									if ( (LA18_0=='0') ) {
										alt18=1;
									}
									switch (alt18) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:37: '0' ( '0' )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:41: ( '0' )?
											int alt17=2;
											int LA17_0 = input.LA(1);
											if ( (LA17_0=='0') ) {
												alt17=1;
											}
											switch (alt17) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:41: '0'
													{
													match('0'); if (state.failed) return;
													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							}
							break;

					}

					if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					match('2'); if (state.failed) return;
					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "B"

	// $ANTLR start "C"
	public final void mC() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1480:17: ( ( 'c' | 'C' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '3' )
			int alt26=2;
			int LA26_0 = input.LA(1);
			if ( (LA26_0=='C'||LA26_0=='c') ) {
				alt26=1;
			}
			else if ( (LA26_0=='\\') ) {
				alt26=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 26, 0, input);
				throw nvae;
			}

			switch (alt26) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1480:21: ( 'c' | 'C' )
					{
					if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1481:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '3'
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1481:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
					int alt25=2;
					int LA25_0 = input.LA(1);
					if ( (LA25_0=='0') ) {
						alt25=1;
					}
					switch (alt25) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1481:27: '0' ( '0' ( '0' ( '0' )? )? )?
							{
							match('0'); if (state.failed) return;
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1481:31: ( '0' ( '0' ( '0' )? )? )?
							int alt24=2;
							int LA24_0 = input.LA(1);
							if ( (LA24_0=='0') ) {
								alt24=1;
							}
							switch (alt24) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1481:32: '0' ( '0' ( '0' )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1481:36: ( '0' ( '0' )? )?
									int alt23=2;
									int LA23_0 = input.LA(1);
									if ( (LA23_0=='0') ) {
										alt23=1;
									}
									switch (alt23) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1481:37: '0' ( '0' )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1481:41: ( '0' )?
											int alt22=2;
											int LA22_0 = input.LA(1);
											if ( (LA22_0=='0') ) {
												alt22=1;
											}
											switch (alt22) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1481:41: '0'
													{
													match('0'); if (state.failed) return;
													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							}
							break;

					}

					if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					match('3'); if (state.failed) return;
					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "C"

	// $ANTLR start "D"
	public final void mD() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1483:17: ( ( 'd' | 'D' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '4' )
			int alt31=2;
			int LA31_0 = input.LA(1);
			if ( (LA31_0=='D'||LA31_0=='d') ) {
				alt31=1;
			}
			else if ( (LA31_0=='\\') ) {
				alt31=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 31, 0, input);
				throw nvae;
			}

			switch (alt31) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1483:21: ( 'd' | 'D' )
					{
					if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1484:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '4'
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1484:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
					int alt30=2;
					int LA30_0 = input.LA(1);
					if ( (LA30_0=='0') ) {
						alt30=1;
					}
					switch (alt30) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1484:27: '0' ( '0' ( '0' ( '0' )? )? )?
							{
							match('0'); if (state.failed) return;
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1484:31: ( '0' ( '0' ( '0' )? )? )?
							int alt29=2;
							int LA29_0 = input.LA(1);
							if ( (LA29_0=='0') ) {
								alt29=1;
							}
							switch (alt29) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1484:32: '0' ( '0' ( '0' )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1484:36: ( '0' ( '0' )? )?
									int alt28=2;
									int LA28_0 = input.LA(1);
									if ( (LA28_0=='0') ) {
										alt28=1;
									}
									switch (alt28) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1484:37: '0' ( '0' )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1484:41: ( '0' )?
											int alt27=2;
											int LA27_0 = input.LA(1);
											if ( (LA27_0=='0') ) {
												alt27=1;
											}
											switch (alt27) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1484:41: '0'
													{
													match('0'); if (state.failed) return;
													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							}
							break;

					}

					if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					match('4'); if (state.failed) return;
					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "D"

	// $ANTLR start "E"
	public final void mE() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:17: ( ( 'e' | 'E' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '5' )
			int alt36=2;
			int LA36_0 = input.LA(1);
			if ( (LA36_0=='E'||LA36_0=='e') ) {
				alt36=1;
			}
			else if ( (LA36_0=='\\') ) {
				alt36=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 36, 0, input);
				throw nvae;
			}

			switch (alt36) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:21: ( 'e' | 'E' )
					{
					if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1487:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '5'
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1487:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
					int alt35=2;
					int LA35_0 = input.LA(1);
					if ( (LA35_0=='0') ) {
						alt35=1;
					}
					switch (alt35) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1487:27: '0' ( '0' ( '0' ( '0' )? )? )?
							{
							match('0'); if (state.failed) return;
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1487:31: ( '0' ( '0' ( '0' )? )? )?
							int alt34=2;
							int LA34_0 = input.LA(1);
							if ( (LA34_0=='0') ) {
								alt34=1;
							}
							switch (alt34) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1487:32: '0' ( '0' ( '0' )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1487:36: ( '0' ( '0' )? )?
									int alt33=2;
									int LA33_0 = input.LA(1);
									if ( (LA33_0=='0') ) {
										alt33=1;
									}
									switch (alt33) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1487:37: '0' ( '0' )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1487:41: ( '0' )?
											int alt32=2;
											int LA32_0 = input.LA(1);
											if ( (LA32_0=='0') ) {
												alt32=1;
											}
											switch (alt32) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1487:41: '0'
													{
													match('0'); if (state.failed) return;
													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							}
							break;

					}

					if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					match('5'); if (state.failed) return;
					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "E"

	// $ANTLR start "F"
	public final void mF() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1489:17: ( ( 'f' | 'F' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '6' )
			int alt41=2;
			int LA41_0 = input.LA(1);
			if ( (LA41_0=='F'||LA41_0=='f') ) {
				alt41=1;
			}
			else if ( (LA41_0=='\\') ) {
				alt41=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 41, 0, input);
				throw nvae;
			}

			switch (alt41) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1489:21: ( 'f' | 'F' )
					{
					if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1490:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '6'
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1490:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
					int alt40=2;
					int LA40_0 = input.LA(1);
					if ( (LA40_0=='0') ) {
						alt40=1;
					}
					switch (alt40) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1490:27: '0' ( '0' ( '0' ( '0' )? )? )?
							{
							match('0'); if (state.failed) return;
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1490:31: ( '0' ( '0' ( '0' )? )? )?
							int alt39=2;
							int LA39_0 = input.LA(1);
							if ( (LA39_0=='0') ) {
								alt39=1;
							}
							switch (alt39) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1490:32: '0' ( '0' ( '0' )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1490:36: ( '0' ( '0' )? )?
									int alt38=2;
									int LA38_0 = input.LA(1);
									if ( (LA38_0=='0') ) {
										alt38=1;
									}
									switch (alt38) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1490:37: '0' ( '0' )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1490:41: ( '0' )?
											int alt37=2;
											int LA37_0 = input.LA(1);
											if ( (LA37_0=='0') ) {
												alt37=1;
											}
											switch (alt37) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1490:41: '0'
													{
													match('0'); if (state.failed) return;
													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							}
							break;

					}

					if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					match('6'); if (state.failed) return;
					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "F"

	// $ANTLR start "G"
	public final void mG() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1492:17: ( ( 'g' | 'G' ) | '\\\\' ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' ) )
			int alt47=2;
			int LA47_0 = input.LA(1);
			if ( (LA47_0=='G'||LA47_0=='g') ) {
				alt47=1;
			}
			else if ( (LA47_0=='\\') ) {
				alt47=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 47, 0, input);
				throw nvae;
			}

			switch (alt47) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1492:21: ( 'g' | 'G' )
					{
					if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1493:21: '\\\\' ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:25: ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' )
					int alt46=3;
					switch ( input.LA(1) ) {
					case 'g':
						{
						alt46=1;
						}
						break;
					case 'G':
						{
						alt46=2;
						}
						break;
					case '0':
					case '4':
					case '6':
						{
						alt46=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 46, 0, input);
						throw nvae;
					}
					switch (alt46) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1495:31: 'g'
							{
							match('g'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1496:31: 'G'
							{
							match('G'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1497:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7'
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1497:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt45=2;
							int LA45_0 = input.LA(1);
							if ( (LA45_0=='0') ) {
								alt45=1;
							}
							switch (alt45) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1497:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1497:36: ( '0' ( '0' ( '0' )? )? )?
									int alt44=2;
									int LA44_0 = input.LA(1);
									if ( (LA44_0=='0') ) {
										alt44=1;
									}
									switch (alt44) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1497:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1497:41: ( '0' ( '0' )? )?
											int alt43=2;
											int LA43_0 = input.LA(1);
											if ( (LA43_0=='0') ) {
												alt43=1;
											}
											switch (alt43) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1497:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1497:46: ( '0' )?
													int alt42=2;
													int LA42_0 = input.LA(1);
													if ( (LA42_0=='0') ) {
														alt42=1;
													}
													switch (alt42) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1497:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							match('7'); if (state.failed) return;
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "G"

	// $ANTLR start "H"
	public final void mH() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1500:17: ( ( 'h' | 'H' ) | '\\\\' ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' ) )
			int alt53=2;
			int LA53_0 = input.LA(1);
			if ( (LA53_0=='H'||LA53_0=='h') ) {
				alt53=1;
			}
			else if ( (LA53_0=='\\') ) {
				alt53=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 53, 0, input);
				throw nvae;
			}

			switch (alt53) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1500:21: ( 'h' | 'H' )
					{
					if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1501:19: '\\\\' ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:25: ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' )
					int alt52=3;
					switch ( input.LA(1) ) {
					case 'h':
						{
						alt52=1;
						}
						break;
					case 'H':
						{
						alt52=2;
						}
						break;
					case '0':
					case '4':
					case '6':
						{
						alt52=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 52, 0, input);
						throw nvae;
					}
					switch (alt52) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1503:31: 'h'
							{
							match('h'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1504:31: 'H'
							{
							match('H'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1505:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8'
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1505:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt51=2;
							int LA51_0 = input.LA(1);
							if ( (LA51_0=='0') ) {
								alt51=1;
							}
							switch (alt51) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1505:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1505:36: ( '0' ( '0' ( '0' )? )? )?
									int alt50=2;
									int LA50_0 = input.LA(1);
									if ( (LA50_0=='0') ) {
										alt50=1;
									}
									switch (alt50) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1505:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1505:41: ( '0' ( '0' )? )?
											int alt49=2;
											int LA49_0 = input.LA(1);
											if ( (LA49_0=='0') ) {
												alt49=1;
											}
											switch (alt49) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1505:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1505:46: ( '0' )?
													int alt48=2;
													int LA48_0 = input.LA(1);
													if ( (LA48_0=='0') ) {
														alt48=1;
													}
													switch (alt48) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1505:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							match('8'); if (state.failed) return;
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "H"

	// $ANTLR start "I"
	public final void mI() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1508:17: ( ( 'i' | 'I' ) | '\\\\' ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' ) )
			int alt59=2;
			int LA59_0 = input.LA(1);
			if ( (LA59_0=='I'||LA59_0=='i') ) {
				alt59=1;
			}
			else if ( (LA59_0=='\\') ) {
				alt59=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 59, 0, input);
				throw nvae;
			}

			switch (alt59) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1508:21: ( 'i' | 'I' )
					{
					if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1509:19: '\\\\' ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:25: ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' )
					int alt58=3;
					switch ( input.LA(1) ) {
					case 'i':
						{
						alt58=1;
						}
						break;
					case 'I':
						{
						alt58=2;
						}
						break;
					case '0':
					case '4':
					case '6':
						{
						alt58=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 58, 0, input);
						throw nvae;
					}
					switch (alt58) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1511:31: 'i'
							{
							match('i'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1512:31: 'I'
							{
							match('I'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1513:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9'
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1513:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt57=2;
							int LA57_0 = input.LA(1);
							if ( (LA57_0=='0') ) {
								alt57=1;
							}
							switch (alt57) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1513:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1513:36: ( '0' ( '0' ( '0' )? )? )?
									int alt56=2;
									int LA56_0 = input.LA(1);
									if ( (LA56_0=='0') ) {
										alt56=1;
									}
									switch (alt56) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1513:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1513:41: ( '0' ( '0' )? )?
											int alt55=2;
											int LA55_0 = input.LA(1);
											if ( (LA55_0=='0') ) {
												alt55=1;
											}
											switch (alt55) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1513:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1513:46: ( '0' )?
													int alt54=2;
													int LA54_0 = input.LA(1);
													if ( (LA54_0=='0') ) {
														alt54=1;
													}
													switch (alt54) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1513:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							match('9'); if (state.failed) return;
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "I"

	// $ANTLR start "J"
	public final void mJ() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1516:17: ( ( 'j' | 'J' ) | '\\\\' ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) ) )
			int alt65=2;
			int LA65_0 = input.LA(1);
			if ( (LA65_0=='J'||LA65_0=='j') ) {
				alt65=1;
			}
			else if ( (LA65_0=='\\') ) {
				alt65=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 65, 0, input);
				throw nvae;
			}

			switch (alt65) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1516:21: ( 'j' | 'J' )
					{
					if ( input.LA(1)=='J'||input.LA(1)=='j' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1517:19: '\\\\' ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:25: ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) )
					int alt64=3;
					switch ( input.LA(1) ) {
					case 'j':
						{
						alt64=1;
						}
						break;
					case 'J':
						{
						alt64=2;
						}
						break;
					case '0':
					case '4':
					case '6':
						{
						alt64=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 64, 0, input);
						throw nvae;
					}
					switch (alt64) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1519:31: 'j'
							{
							match('j'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1520:31: 'J'
							{
							match('J'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1521:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' )
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1521:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt63=2;
							int LA63_0 = input.LA(1);
							if ( (LA63_0=='0') ) {
								alt63=1;
							}
							switch (alt63) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1521:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1521:36: ( '0' ( '0' ( '0' )? )? )?
									int alt62=2;
									int LA62_0 = input.LA(1);
									if ( (LA62_0=='0') ) {
										alt62=1;
									}
									switch (alt62) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1521:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1521:41: ( '0' ( '0' )? )?
											int alt61=2;
											int LA61_0 = input.LA(1);
											if ( (LA61_0=='0') ) {
												alt61=1;
											}
											switch (alt61) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1521:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1521:46: ( '0' )?
													int alt60=2;
													int LA60_0 = input.LA(1);
													if ( (LA60_0=='0') ) {
														alt60=1;
													}
													switch (alt60) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1521:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "J"

	// $ANTLR start "K"
	public final void mK() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1524:17: ( ( 'k' | 'K' ) | '\\\\' ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) ) )
			int alt71=2;
			int LA71_0 = input.LA(1);
			if ( (LA71_0=='K'||LA71_0=='k') ) {
				alt71=1;
			}
			else if ( (LA71_0=='\\') ) {
				alt71=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 71, 0, input);
				throw nvae;
			}

			switch (alt71) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1524:21: ( 'k' | 'K' )
					{
					if ( input.LA(1)=='K'||input.LA(1)=='k' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1525:19: '\\\\' ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:25: ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) )
					int alt70=3;
					switch ( input.LA(1) ) {
					case 'k':
						{
						alt70=1;
						}
						break;
					case 'K':
						{
						alt70=2;
						}
						break;
					case '0':
					case '4':
					case '6':
						{
						alt70=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 70, 0, input);
						throw nvae;
					}
					switch (alt70) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1527:31: 'k'
							{
							match('k'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1528:31: 'K'
							{
							match('K'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1529:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' )
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1529:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt69=2;
							int LA69_0 = input.LA(1);
							if ( (LA69_0=='0') ) {
								alt69=1;
							}
							switch (alt69) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1529:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1529:36: ( '0' ( '0' ( '0' )? )? )?
									int alt68=2;
									int LA68_0 = input.LA(1);
									if ( (LA68_0=='0') ) {
										alt68=1;
									}
									switch (alt68) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1529:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1529:41: ( '0' ( '0' )? )?
											int alt67=2;
											int LA67_0 = input.LA(1);
											if ( (LA67_0=='0') ) {
												alt67=1;
											}
											switch (alt67) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1529:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1529:46: ( '0' )?
													int alt66=2;
													int LA66_0 = input.LA(1);
													if ( (LA66_0=='0') ) {
														alt66=1;
													}
													switch (alt66) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1529:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "K"

	// $ANTLR start "L"
	public final void mL() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1532:17: ( ( 'l' | 'L' ) | '\\\\' ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) ) )
			int alt77=2;
			int LA77_0 = input.LA(1);
			if ( (LA77_0=='L'||LA77_0=='l') ) {
				alt77=1;
			}
			else if ( (LA77_0=='\\') ) {
				alt77=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 77, 0, input);
				throw nvae;
			}

			switch (alt77) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1532:21: ( 'l' | 'L' )
					{
					if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1533:19: '\\\\' ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1534:25: ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) )
					int alt76=3;
					switch ( input.LA(1) ) {
					case 'l':
						{
						alt76=1;
						}
						break;
					case 'L':
						{
						alt76=2;
						}
						break;
					case '0':
					case '4':
					case '6':
						{
						alt76=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 76, 0, input);
						throw nvae;
					}
					switch (alt76) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1535:31: 'l'
							{
							match('l'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1536:31: 'L'
							{
							match('L'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1537:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' )
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1537:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt75=2;
							int LA75_0 = input.LA(1);
							if ( (LA75_0=='0') ) {
								alt75=1;
							}
							switch (alt75) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1537:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1537:36: ( '0' ( '0' ( '0' )? )? )?
									int alt74=2;
									int LA74_0 = input.LA(1);
									if ( (LA74_0=='0') ) {
										alt74=1;
									}
									switch (alt74) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1537:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1537:41: ( '0' ( '0' )? )?
											int alt73=2;
											int LA73_0 = input.LA(1);
											if ( (LA73_0=='0') ) {
												alt73=1;
											}
											switch (alt73) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1537:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1537:46: ( '0' )?
													int alt72=2;
													int LA72_0 = input.LA(1);
													if ( (LA72_0=='0') ) {
														alt72=1;
													}
													switch (alt72) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1537:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "L"

	// $ANTLR start "M"
	public final void mM() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1540:17: ( ( 'm' | 'M' ) | '\\\\' ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) ) )
			int alt83=2;
			int LA83_0 = input.LA(1);
			if ( (LA83_0=='M'||LA83_0=='m') ) {
				alt83=1;
			}
			else if ( (LA83_0=='\\') ) {
				alt83=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 83, 0, input);
				throw nvae;
			}

			switch (alt83) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1540:21: ( 'm' | 'M' )
					{
					if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1541:19: '\\\\' ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1542:25: ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) )
					int alt82=3;
					switch ( input.LA(1) ) {
					case 'm':
						{
						alt82=1;
						}
						break;
					case 'M':
						{
						alt82=2;
						}
						break;
					case '0':
					case '4':
					case '6':
						{
						alt82=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 82, 0, input);
						throw nvae;
					}
					switch (alt82) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1543:31: 'm'
							{
							match('m'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1544:31: 'M'
							{
							match('M'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1545:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' )
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1545:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt81=2;
							int LA81_0 = input.LA(1);
							if ( (LA81_0=='0') ) {
								alt81=1;
							}
							switch (alt81) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1545:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1545:36: ( '0' ( '0' ( '0' )? )? )?
									int alt80=2;
									int LA80_0 = input.LA(1);
									if ( (LA80_0=='0') ) {
										alt80=1;
									}
									switch (alt80) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1545:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1545:41: ( '0' ( '0' )? )?
											int alt79=2;
											int LA79_0 = input.LA(1);
											if ( (LA79_0=='0') ) {
												alt79=1;
											}
											switch (alt79) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1545:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1545:46: ( '0' )?
													int alt78=2;
													int LA78_0 = input.LA(1);
													if ( (LA78_0=='0') ) {
														alt78=1;
													}
													switch (alt78) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1545:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "M"

	// $ANTLR start "N"
	public final void mN() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1548:17: ( ( 'n' | 'N' ) | '\\\\' ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) ) )
			int alt89=2;
			int LA89_0 = input.LA(1);
			if ( (LA89_0=='N'||LA89_0=='n') ) {
				alt89=1;
			}
			else if ( (LA89_0=='\\') ) {
				alt89=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 89, 0, input);
				throw nvae;
			}

			switch (alt89) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1548:21: ( 'n' | 'N' )
					{
					if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:19: '\\\\' ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1550:25: ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) )
					int alt88=3;
					switch ( input.LA(1) ) {
					case 'n':
						{
						alt88=1;
						}
						break;
					case 'N':
						{
						alt88=2;
						}
						break;
					case '0':
					case '4':
					case '6':
						{
						alt88=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 88, 0, input);
						throw nvae;
					}
					switch (alt88) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1551:31: 'n'
							{
							match('n'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1552:31: 'N'
							{
							match('N'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1553:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' )
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1553:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt87=2;
							int LA87_0 = input.LA(1);
							if ( (LA87_0=='0') ) {
								alt87=1;
							}
							switch (alt87) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1553:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1553:36: ( '0' ( '0' ( '0' )? )? )?
									int alt86=2;
									int LA86_0 = input.LA(1);
									if ( (LA86_0=='0') ) {
										alt86=1;
									}
									switch (alt86) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1553:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1553:41: ( '0' ( '0' )? )?
											int alt85=2;
											int LA85_0 = input.LA(1);
											if ( (LA85_0=='0') ) {
												alt85=1;
											}
											switch (alt85) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1553:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1553:46: ( '0' )?
													int alt84=2;
													int LA84_0 = input.LA(1);
													if ( (LA84_0=='0') ) {
														alt84=1;
													}
													switch (alt84) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1553:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "N"

	// $ANTLR start "O"
	public final void mO() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1556:17: ( ( 'o' | 'O' ) | '\\\\' ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) ) )
			int alt95=2;
			int LA95_0 = input.LA(1);
			if ( (LA95_0=='O'||LA95_0=='o') ) {
				alt95=1;
			}
			else if ( (LA95_0=='\\') ) {
				alt95=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 95, 0, input);
				throw nvae;
			}

			switch (alt95) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1556:21: ( 'o' | 'O' )
					{
					if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:19: '\\\\' ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1558:25: ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) )
					int alt94=3;
					switch ( input.LA(1) ) {
					case 'o':
						{
						alt94=1;
						}
						break;
					case 'O':
						{
						alt94=2;
						}
						break;
					case '0':
					case '4':
					case '6':
						{
						alt94=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 94, 0, input);
						throw nvae;
					}
					switch (alt94) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1559:31: 'o'
							{
							match('o'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1560:31: 'O'
							{
							match('O'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1561:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' )
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1561:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt93=2;
							int LA93_0 = input.LA(1);
							if ( (LA93_0=='0') ) {
								alt93=1;
							}
							switch (alt93) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1561:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1561:36: ( '0' ( '0' ( '0' )? )? )?
									int alt92=2;
									int LA92_0 = input.LA(1);
									if ( (LA92_0=='0') ) {
										alt92=1;
									}
									switch (alt92) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1561:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1561:41: ( '0' ( '0' )? )?
											int alt91=2;
											int LA91_0 = input.LA(1);
											if ( (LA91_0=='0') ) {
												alt91=1;
											}
											switch (alt91) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1561:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1561:46: ( '0' )?
													int alt90=2;
													int LA90_0 = input.LA(1);
													if ( (LA90_0=='0') ) {
														alt90=1;
													}
													switch (alt90) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1561:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='4'||input.LA(1)=='6' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "O"

	// $ANTLR start "P"
	public final void mP() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1564:17: ( ( 'p' | 'P' ) | '\\\\' ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) ) )
			int alt101=2;
			int LA101_0 = input.LA(1);
			if ( (LA101_0=='P'||LA101_0=='p') ) {
				alt101=1;
			}
			else if ( (LA101_0=='\\') ) {
				alt101=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 101, 0, input);
				throw nvae;
			}

			switch (alt101) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1564:21: ( 'p' | 'P' )
					{
					if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:19: '\\\\' ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1566:25: ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) )
					int alt100=3;
					switch ( input.LA(1) ) {
					case 'p':
						{
						alt100=1;
						}
						break;
					case 'P':
						{
						alt100=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt100=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 100, 0, input);
						throw nvae;
					}
					switch (alt100) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1567:31: 'p'
							{
							match('p'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1568:31: 'P'
							{
							match('P'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1569:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' )
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1569:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt99=2;
							int LA99_0 = input.LA(1);
							if ( (LA99_0=='0') ) {
								alt99=1;
							}
							switch (alt99) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1569:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1569:36: ( '0' ( '0' ( '0' )? )? )?
									int alt98=2;
									int LA98_0 = input.LA(1);
									if ( (LA98_0=='0') ) {
										alt98=1;
									}
									switch (alt98) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1569:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1569:41: ( '0' ( '0' )? )?
											int alt97=2;
											int LA97_0 = input.LA(1);
											if ( (LA97_0=='0') ) {
												alt97=1;
											}
											switch (alt97) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1569:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1569:46: ( '0' )?
													int alt96=2;
													int LA96_0 = input.LA(1);
													if ( (LA96_0=='0') ) {
														alt96=1;
													}
													switch (alt96) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1569:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1569:66: ( '0' )
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1569:67: '0'
							{
							match('0'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "P"

	// $ANTLR start "Q"
	public final void mQ() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1572:17: ( ( 'q' | 'Q' ) | '\\\\' ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) ) )
			int alt107=2;
			int LA107_0 = input.LA(1);
			if ( (LA107_0=='Q'||LA107_0=='q') ) {
				alt107=1;
			}
			else if ( (LA107_0=='\\') ) {
				alt107=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 107, 0, input);
				throw nvae;
			}

			switch (alt107) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1572:21: ( 'q' | 'Q' )
					{
					if ( input.LA(1)=='Q'||input.LA(1)=='q' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1573:19: '\\\\' ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1574:25: ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) )
					int alt106=3;
					switch ( input.LA(1) ) {
					case 'q':
						{
						alt106=1;
						}
						break;
					case 'Q':
						{
						alt106=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt106=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 106, 0, input);
						throw nvae;
					}
					switch (alt106) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1575:31: 'q'
							{
							match('q'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1576:31: 'Q'
							{
							match('Q'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1577:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' )
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1577:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt105=2;
							int LA105_0 = input.LA(1);
							if ( (LA105_0=='0') ) {
								alt105=1;
							}
							switch (alt105) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1577:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1577:36: ( '0' ( '0' ( '0' )? )? )?
									int alt104=2;
									int LA104_0 = input.LA(1);
									if ( (LA104_0=='0') ) {
										alt104=1;
									}
									switch (alt104) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1577:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1577:41: ( '0' ( '0' )? )?
											int alt103=2;
											int LA103_0 = input.LA(1);
											if ( (LA103_0=='0') ) {
												alt103=1;
											}
											switch (alt103) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1577:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1577:46: ( '0' )?
													int alt102=2;
													int LA102_0 = input.LA(1);
													if ( (LA102_0=='0') ) {
														alt102=1;
													}
													switch (alt102) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1577:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1577:66: ( '1' )
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1577:67: '1'
							{
							match('1'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Q"

	// $ANTLR start "R"
	public final void mR() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1580:17: ( ( 'r' | 'R' ) | '\\\\' ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) ) )
			int alt113=2;
			int LA113_0 = input.LA(1);
			if ( (LA113_0=='R'||LA113_0=='r') ) {
				alt113=1;
			}
			else if ( (LA113_0=='\\') ) {
				alt113=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 113, 0, input);
				throw nvae;
			}

			switch (alt113) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1580:21: ( 'r' | 'R' )
					{
					if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1581:19: '\\\\' ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1582:25: ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) )
					int alt112=3;
					switch ( input.LA(1) ) {
					case 'r':
						{
						alt112=1;
						}
						break;
					case 'R':
						{
						alt112=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt112=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 112, 0, input);
						throw nvae;
					}
					switch (alt112) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1583:31: 'r'
							{
							match('r'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1584:31: 'R'
							{
							match('R'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1585:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' )
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1585:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt111=2;
							int LA111_0 = input.LA(1);
							if ( (LA111_0=='0') ) {
								alt111=1;
							}
							switch (alt111) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1585:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1585:36: ( '0' ( '0' ( '0' )? )? )?
									int alt110=2;
									int LA110_0 = input.LA(1);
									if ( (LA110_0=='0') ) {
										alt110=1;
									}
									switch (alt110) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1585:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1585:41: ( '0' ( '0' )? )?
											int alt109=2;
											int LA109_0 = input.LA(1);
											if ( (LA109_0=='0') ) {
												alt109=1;
											}
											switch (alt109) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1585:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1585:46: ( '0' )?
													int alt108=2;
													int LA108_0 = input.LA(1);
													if ( (LA108_0=='0') ) {
														alt108=1;
													}
													switch (alt108) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1585:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1585:66: ( '2' )
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1585:67: '2'
							{
							match('2'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "R"

	// $ANTLR start "S"
	public final void mS() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1588:17: ( ( 's' | 'S' ) | '\\\\' ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) ) )
			int alt119=2;
			int LA119_0 = input.LA(1);
			if ( (LA119_0=='S'||LA119_0=='s') ) {
				alt119=1;
			}
			else if ( (LA119_0=='\\') ) {
				alt119=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 119, 0, input);
				throw nvae;
			}

			switch (alt119) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1588:21: ( 's' | 'S' )
					{
					if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1589:19: '\\\\' ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1590:25: ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) )
					int alt118=3;
					switch ( input.LA(1) ) {
					case 's':
						{
						alt118=1;
						}
						break;
					case 'S':
						{
						alt118=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt118=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 118, 0, input);
						throw nvae;
					}
					switch (alt118) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1591:31: 's'
							{
							match('s'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1592:31: 'S'
							{
							match('S'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1593:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' )
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1593:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt117=2;
							int LA117_0 = input.LA(1);
							if ( (LA117_0=='0') ) {
								alt117=1;
							}
							switch (alt117) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1593:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1593:36: ( '0' ( '0' ( '0' )? )? )?
									int alt116=2;
									int LA116_0 = input.LA(1);
									if ( (LA116_0=='0') ) {
										alt116=1;
									}
									switch (alt116) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1593:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1593:41: ( '0' ( '0' )? )?
											int alt115=2;
											int LA115_0 = input.LA(1);
											if ( (LA115_0=='0') ) {
												alt115=1;
											}
											switch (alt115) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1593:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1593:46: ( '0' )?
													int alt114=2;
													int LA114_0 = input.LA(1);
													if ( (LA114_0=='0') ) {
														alt114=1;
													}
													switch (alt114) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1593:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1593:66: ( '3' )
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1593:67: '3'
							{
							match('3'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "S"

	// $ANTLR start "T"
	public final void mT() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1596:17: ( ( 't' | 'T' ) | '\\\\' ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) ) )
			int alt125=2;
			int LA125_0 = input.LA(1);
			if ( (LA125_0=='T'||LA125_0=='t') ) {
				alt125=1;
			}
			else if ( (LA125_0=='\\') ) {
				alt125=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 125, 0, input);
				throw nvae;
			}

			switch (alt125) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1596:21: ( 't' | 'T' )
					{
					if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1597:19: '\\\\' ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1598:25: ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) )
					int alt124=3;
					switch ( input.LA(1) ) {
					case 't':
						{
						alt124=1;
						}
						break;
					case 'T':
						{
						alt124=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt124=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 124, 0, input);
						throw nvae;
					}
					switch (alt124) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1599:31: 't'
							{
							match('t'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1600:31: 'T'
							{
							match('T'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1601:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' )
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1601:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt123=2;
							int LA123_0 = input.LA(1);
							if ( (LA123_0=='0') ) {
								alt123=1;
							}
							switch (alt123) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1601:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1601:36: ( '0' ( '0' ( '0' )? )? )?
									int alt122=2;
									int LA122_0 = input.LA(1);
									if ( (LA122_0=='0') ) {
										alt122=1;
									}
									switch (alt122) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1601:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1601:41: ( '0' ( '0' )? )?
											int alt121=2;
											int LA121_0 = input.LA(1);
											if ( (LA121_0=='0') ) {
												alt121=1;
											}
											switch (alt121) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1601:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1601:46: ( '0' )?
													int alt120=2;
													int LA120_0 = input.LA(1);
													if ( (LA120_0=='0') ) {
														alt120=1;
													}
													switch (alt120) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1601:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1601:66: ( '4' )
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1601:67: '4'
							{
							match('4'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T"

	// $ANTLR start "U"
	public final void mU() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1604:17: ( ( 'u' | 'U' ) | '\\\\' ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) ) )
			int alt131=2;
			int LA131_0 = input.LA(1);
			if ( (LA131_0=='U'||LA131_0=='u') ) {
				alt131=1;
			}
			else if ( (LA131_0=='\\') ) {
				alt131=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 131, 0, input);
				throw nvae;
			}

			switch (alt131) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1604:21: ( 'u' | 'U' )
					{
					if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1605:19: '\\\\' ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1606:25: ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) )
					int alt130=3;
					switch ( input.LA(1) ) {
					case 'u':
						{
						alt130=1;
						}
						break;
					case 'U':
						{
						alt130=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt130=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 130, 0, input);
						throw nvae;
					}
					switch (alt130) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1607:31: 'u'
							{
							match('u'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1608:31: 'U'
							{
							match('U'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1609:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' )
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1609:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt129=2;
							int LA129_0 = input.LA(1);
							if ( (LA129_0=='0') ) {
								alt129=1;
							}
							switch (alt129) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1609:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1609:36: ( '0' ( '0' ( '0' )? )? )?
									int alt128=2;
									int LA128_0 = input.LA(1);
									if ( (LA128_0=='0') ) {
										alt128=1;
									}
									switch (alt128) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1609:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1609:41: ( '0' ( '0' )? )?
											int alt127=2;
											int LA127_0 = input.LA(1);
											if ( (LA127_0=='0') ) {
												alt127=1;
											}
											switch (alt127) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1609:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1609:46: ( '0' )?
													int alt126=2;
													int LA126_0 = input.LA(1);
													if ( (LA126_0=='0') ) {
														alt126=1;
													}
													switch (alt126) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1609:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1609:66: ( '5' )
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1609:67: '5'
							{
							match('5'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "U"

	// $ANTLR start "V"
	public final void mV() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1612:17: ( ( 'v' | 'V' ) | '\\\\' ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) ) )
			int alt137=2;
			int LA137_0 = input.LA(1);
			if ( (LA137_0=='V'||LA137_0=='v') ) {
				alt137=1;
			}
			else if ( (LA137_0=='\\') ) {
				alt137=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 137, 0, input);
				throw nvae;
			}

			switch (alt137) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1612:21: ( 'v' | 'V' )
					{
					if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1613:19: '\\\\' ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1614:25: ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) )
					int alt136=3;
					switch ( input.LA(1) ) {
					case 'v':
						{
						alt136=1;
						}
						break;
					case 'V':
						{
						alt136=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt136=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 136, 0, input);
						throw nvae;
					}
					switch (alt136) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1614:31: 'v'
							{
							match('v'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1615:31: 'V'
							{
							match('V'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1616:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' )
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1616:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt135=2;
							int LA135_0 = input.LA(1);
							if ( (LA135_0=='0') ) {
								alt135=1;
							}
							switch (alt135) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1616:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1616:36: ( '0' ( '0' ( '0' )? )? )?
									int alt134=2;
									int LA134_0 = input.LA(1);
									if ( (LA134_0=='0') ) {
										alt134=1;
									}
									switch (alt134) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1616:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1616:41: ( '0' ( '0' )? )?
											int alt133=2;
											int LA133_0 = input.LA(1);
											if ( (LA133_0=='0') ) {
												alt133=1;
											}
											switch (alt133) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1616:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1616:46: ( '0' )?
													int alt132=2;
													int LA132_0 = input.LA(1);
													if ( (LA132_0=='0') ) {
														alt132=1;
													}
													switch (alt132) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1616:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1616:66: ( '6' )
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1616:67: '6'
							{
							match('6'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "V"

	// $ANTLR start "W"
	public final void mW() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1619:17: ( ( 'w' | 'W' ) | '\\\\' ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) ) )
			int alt143=2;
			int LA143_0 = input.LA(1);
			if ( (LA143_0=='W'||LA143_0=='w') ) {
				alt143=1;
			}
			else if ( (LA143_0=='\\') ) {
				alt143=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 143, 0, input);
				throw nvae;
			}

			switch (alt143) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1619:21: ( 'w' | 'W' )
					{
					if ( input.LA(1)=='W'||input.LA(1)=='w' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1620:19: '\\\\' ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1621:25: ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) )
					int alt142=3;
					switch ( input.LA(1) ) {
					case 'w':
						{
						alt142=1;
						}
						break;
					case 'W':
						{
						alt142=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt142=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 142, 0, input);
						throw nvae;
					}
					switch (alt142) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1622:31: 'w'
							{
							match('w'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1623:31: 'W'
							{
							match('W'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1624:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' )
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1624:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt141=2;
							int LA141_0 = input.LA(1);
							if ( (LA141_0=='0') ) {
								alt141=1;
							}
							switch (alt141) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1624:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1624:36: ( '0' ( '0' ( '0' )? )? )?
									int alt140=2;
									int LA140_0 = input.LA(1);
									if ( (LA140_0=='0') ) {
										alt140=1;
									}
									switch (alt140) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1624:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1624:41: ( '0' ( '0' )? )?
											int alt139=2;
											int LA139_0 = input.LA(1);
											if ( (LA139_0=='0') ) {
												alt139=1;
											}
											switch (alt139) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1624:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1624:46: ( '0' )?
													int alt138=2;
													int LA138_0 = input.LA(1);
													if ( (LA138_0=='0') ) {
														alt138=1;
													}
													switch (alt138) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1624:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1624:66: ( '7' )
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1624:67: '7'
							{
							match('7'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "W"

	// $ANTLR start "X"
	public final void mX() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1627:17: ( ( 'x' | 'X' ) | '\\\\' ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) ) )
			int alt149=2;
			int LA149_0 = input.LA(1);
			if ( (LA149_0=='X'||LA149_0=='x') ) {
				alt149=1;
			}
			else if ( (LA149_0=='\\') ) {
				alt149=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 149, 0, input);
				throw nvae;
			}

			switch (alt149) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1627:21: ( 'x' | 'X' )
					{
					if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1628:19: '\\\\' ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1629:25: ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) )
					int alt148=3;
					switch ( input.LA(1) ) {
					case 'x':
						{
						alt148=1;
						}
						break;
					case 'X':
						{
						alt148=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt148=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 148, 0, input);
						throw nvae;
					}
					switch (alt148) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1630:31: 'x'
							{
							match('x'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1631:31: 'X'
							{
							match('X'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1632:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' )
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1632:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt147=2;
							int LA147_0 = input.LA(1);
							if ( (LA147_0=='0') ) {
								alt147=1;
							}
							switch (alt147) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1632:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1632:36: ( '0' ( '0' ( '0' )? )? )?
									int alt146=2;
									int LA146_0 = input.LA(1);
									if ( (LA146_0=='0') ) {
										alt146=1;
									}
									switch (alt146) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1632:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1632:41: ( '0' ( '0' )? )?
											int alt145=2;
											int LA145_0 = input.LA(1);
											if ( (LA145_0=='0') ) {
												alt145=1;
											}
											switch (alt145) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1632:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1632:46: ( '0' )?
													int alt144=2;
													int LA144_0 = input.LA(1);
													if ( (LA144_0=='0') ) {
														alt144=1;
													}
													switch (alt144) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1632:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1632:66: ( '8' )
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1632:67: '8'
							{
							match('8'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "X"

	// $ANTLR start "Y"
	public final void mY() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1635:17: ( ( 'y' | 'Y' ) | '\\\\' ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) ) )
			int alt155=2;
			int LA155_0 = input.LA(1);
			if ( (LA155_0=='Y'||LA155_0=='y') ) {
				alt155=1;
			}
			else if ( (LA155_0=='\\') ) {
				alt155=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 155, 0, input);
				throw nvae;
			}

			switch (alt155) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1635:21: ( 'y' | 'Y' )
					{
					if ( input.LA(1)=='Y'||input.LA(1)=='y' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1636:19: '\\\\' ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1637:25: ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) )
					int alt154=3;
					switch ( input.LA(1) ) {
					case 'y':
						{
						alt154=1;
						}
						break;
					case 'Y':
						{
						alt154=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt154=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 154, 0, input);
						throw nvae;
					}
					switch (alt154) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1638:31: 'y'
							{
							match('y'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1639:31: 'Y'
							{
							match('Y'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1640:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' )
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1640:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt153=2;
							int LA153_0 = input.LA(1);
							if ( (LA153_0=='0') ) {
								alt153=1;
							}
							switch (alt153) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1640:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1640:36: ( '0' ( '0' ( '0' )? )? )?
									int alt152=2;
									int LA152_0 = input.LA(1);
									if ( (LA152_0=='0') ) {
										alt152=1;
									}
									switch (alt152) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1640:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1640:41: ( '0' ( '0' )? )?
											int alt151=2;
											int LA151_0 = input.LA(1);
											if ( (LA151_0=='0') ) {
												alt151=1;
											}
											switch (alt151) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1640:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1640:46: ( '0' )?
													int alt150=2;
													int LA150_0 = input.LA(1);
													if ( (LA150_0=='0') ) {
														alt150=1;
													}
													switch (alt150) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1640:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1640:66: ( '9' )
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1640:67: '9'
							{
							match('9'); if (state.failed) return;
							}

							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Y"

	// $ANTLR start "Z"
	public final void mZ() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1643:17: ( ( 'z' | 'Z' ) | '\\\\' ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) ) )
			int alt161=2;
			int LA161_0 = input.LA(1);
			if ( (LA161_0=='Z'||LA161_0=='z') ) {
				alt161=1;
			}
			else if ( (LA161_0=='\\') ) {
				alt161=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 161, 0, input);
				throw nvae;
			}

			switch (alt161) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1643:21: ( 'z' | 'Z' )
					{
					if ( input.LA(1)=='Z'||input.LA(1)=='z' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1644:19: '\\\\' ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1645:25: ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) )
					int alt160=3;
					switch ( input.LA(1) ) {
					case 'z':
						{
						alt160=1;
						}
						break;
					case 'Z':
						{
						alt160=2;
						}
						break;
					case '0':
					case '5':
					case '7':
						{
						alt160=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 160, 0, input);
						throw nvae;
					}
					switch (alt160) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1646:31: 'z'
							{
							match('z'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1647:31: 'Z'
							{
							match('Z'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1648:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' )
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1648:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt159=2;
							int LA159_0 = input.LA(1);
							if ( (LA159_0=='0') ) {
								alt159=1;
							}
							switch (alt159) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1648:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1648:36: ( '0' ( '0' ( '0' )? )? )?
									int alt158=2;
									int LA158_0 = input.LA(1);
									if ( (LA158_0=='0') ) {
										alt158=1;
									}
									switch (alt158) {
										case 1 :
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1648:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1648:41: ( '0' ( '0' )? )?
											int alt157=2;
											int LA157_0 = input.LA(1);
											if ( (LA157_0=='0') ) {
												alt157=1;
											}
											switch (alt157) {
												case 1 :
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1648:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1648:46: ( '0' )?
													int alt156=2;
													int LA156_0 = input.LA(1);
													if ( (LA156_0=='0') ) {
														alt156=1;
													}
													switch (alt156) {
														case 1 :
															// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1648:46: '0'
															{
															match('0'); if (state.failed) return;
															}
															break;

													}

													}
													break;

											}

											}
											break;

									}

									}
									break;

							}

							if ( input.LA(1)=='5'||input.LA(1)=='7' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

					}

					}
					break;

			}
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "Z"

	// $ANTLR start "CDO"
	public final void mCDO() throws RecognitionException {
		try {
			int _type = CDO;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1660:17: ( '<!--' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1660:19: '<!--'
			{
			match("<!--"); if (state.failed) return;

			if ( state.backtracking==0 ) {
			                        _channel = 3;   // CDO on channel 3 in case we want it later
			                    }
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CDO"

	// $ANTLR start "CDC"
	public final void mCDC() throws RecognitionException {
		try {
			int _type = CDC;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1673:17: ( '-->' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1673:19: '-->'
			{
			match("-->"); if (state.failed) return;

			if ( state.backtracking==0 ) {
			                        _channel = 4;   // CDC on channel 4 in case we want it later
			                    }
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CDC"

	// $ANTLR start "INCLUDES"
	public final void mINCLUDES() throws RecognitionException {
		try {
			int _type = INCLUDES;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1680:17: ( '~=' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1680:19: '~='
			{
			match("~="); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INCLUDES"

	// $ANTLR start "DASHMATCH"
	public final void mDASHMATCH() throws RecognitionException {
		try {
			int _type = DASHMATCH;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1681:17: ( '|=' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1681:19: '|='
			{
			match("|="); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DASHMATCH"

	// $ANTLR start "BEGINS"
	public final void mBEGINS() throws RecognitionException {
		try {
			int _type = BEGINS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1682:17: ( '^=' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1682:19: '^='
			{
			match("^="); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BEGINS"

	// $ANTLR start "ENDS"
	public final void mENDS() throws RecognitionException {
		try {
			int _type = ENDS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1683:17: ( '$=' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1683:19: '$='
			{
			match("$="); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ENDS"

	// $ANTLR start "CONTAINS"
	public final void mCONTAINS() throws RecognitionException {
		try {
			int _type = CONTAINS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1684:17: ( '*=' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1684:19: '*='
			{
			match("*="); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CONTAINS"

	// $ANTLR start "GREATER"
	public final void mGREATER() throws RecognitionException {
		try {
			int _type = GREATER;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1686:17: ( '>' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1686:19: '>'
			{
			match('>'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "GREATER"

	// $ANTLR start "LBRACE"
	public final void mLBRACE() throws RecognitionException {
		try {
			int _type = LBRACE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1687:17: ( '{' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1687:19: '{'
			{
			match('{'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LBRACE"

	// $ANTLR start "RBRACE"
	public final void mRBRACE() throws RecognitionException {
		try {
			int _type = RBRACE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1688:17: ( '}' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1688:19: '}'
			{
			match('}'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RBRACE"

	// $ANTLR start "LBRACKET"
	public final void mLBRACKET() throws RecognitionException {
		try {
			int _type = LBRACKET;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1689:17: ( '[' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1689:19: '['
			{
			match('['); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LBRACKET"

	// $ANTLR start "RBRACKET"
	public final void mRBRACKET() throws RecognitionException {
		try {
			int _type = RBRACKET;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1690:17: ( ']' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1690:19: ']'
			{
			match(']'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RBRACKET"

	// $ANTLR start "OPEQ"
	public final void mOPEQ() throws RecognitionException {
		try {
			int _type = OPEQ;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1691:17: ( '=' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1691:19: '='
			{
			match('='); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "OPEQ"

	// $ANTLR start "SEMI"
	public final void mSEMI() throws RecognitionException {
		try {
			int _type = SEMI;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:17: ( ';' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1692:19: ';'
			{
			match(';'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SEMI"

	// $ANTLR start "COLON"
	public final void mCOLON() throws RecognitionException {
		try {
			int _type = COLON;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1693:17: ( ':' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1693:19: ':'
			{
			match(':'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COLON"

	// $ANTLR start "DCOLON"
	public final void mDCOLON() throws RecognitionException {
		try {
			int _type = DCOLON;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1694:17: ( '::' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1694:19: '::'
			{
			match("::"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DCOLON"

	// $ANTLR start "SOLIDUS"
	public final void mSOLIDUS() throws RecognitionException {
		try {
			int _type = SOLIDUS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1695:17: ( '/' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1695:19: '/'
			{
			match('/'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SOLIDUS"

	// $ANTLR start "MINUS"
	public final void mMINUS() throws RecognitionException {
		try {
			int _type = MINUS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:17: ( '-' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:19: '-'
			{
			match('-'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MINUS"

	// $ANTLR start "PLUS"
	public final void mPLUS() throws RecognitionException {
		try {
			int _type = PLUS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1697:17: ( '+' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1697:19: '+'
			{
			match('+'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "PLUS"

	// $ANTLR start "STAR"
	public final void mSTAR() throws RecognitionException {
		try {
			int _type = STAR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1698:17: ( '*' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1698:19: '*'
			{
			match('*'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STAR"

	// $ANTLR start "LPAREN"
	public final void mLPAREN() throws RecognitionException {
		try {
			int _type = LPAREN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1699:17: ( '(' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1699:19: '('
			{
			match('('); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LPAREN"

	// $ANTLR start "RPAREN"
	public final void mRPAREN() throws RecognitionException {
		try {
			int _type = RPAREN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1700:17: ( ')' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1700:19: ')'
			{
			match(')'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RPAREN"

	// $ANTLR start "COMMA"
	public final void mCOMMA() throws RecognitionException {
		try {
			int _type = COMMA;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1701:17: ( ',' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1701:19: ','
			{
			match(','); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COMMA"

	// $ANTLR start "DOT"
	public final void mDOT() throws RecognitionException {
		try {
			int _type = DOT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1702:17: ( '.' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1702:19: '.'
			{
			match('.'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DOT"

	// $ANTLR start "TILDE"
	public final void mTILDE() throws RecognitionException {
		try {
			int _type = TILDE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1703:8: ( '~' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1703:10: '~'
			{
			match('~'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TILDE"

	// $ANTLR start "PIPE"
	public final void mPIPE() throws RecognitionException {
		try {
			int _type = PIPE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1704:17: ( '|' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1704:19: '|'
			{
			match('|'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "PIPE"

	// $ANTLR start "PERCENTAGE_SYMBOL"
	public final void mPERCENTAGE_SYMBOL() throws RecognitionException {
		try {
			int _type = PERCENTAGE_SYMBOL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1706:17: ( '%' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1706:19: '%'
			{
			match('%'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "PERCENTAGE_SYMBOL"

	// $ANTLR start "EXCLAMATION_MARK"
	public final void mEXCLAMATION_MARK() throws RecognitionException {
		try {
			int _type = EXCLAMATION_MARK;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1707:17: ( '!' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1707:19: '!'
			{
			match('!'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "EXCLAMATION_MARK"

	// $ANTLR start "CP_EQ"
	public final void mCP_EQ() throws RecognitionException {
		try {
			int _type = CP_EQ;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1709:17: ( '==' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1709:19: '=='
			{
			match("=="); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CP_EQ"

	// $ANTLR start "CP_NOT_EQ"
	public final void mCP_NOT_EQ() throws RecognitionException {
		try {
			int _type = CP_NOT_EQ;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1710:17: ( '!=' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1710:19: '!='
			{
			match("!="); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CP_NOT_EQ"

	// $ANTLR start "LESS"
	public final void mLESS() throws RecognitionException {
		try {
			int _type = LESS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1711:17: ( '<' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1711:19: '<'
			{
			match('<'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LESS"

	// $ANTLR start "GREATER_OR_EQ"
	public final void mGREATER_OR_EQ() throws RecognitionException {
		try {
			int _type = GREATER_OR_EQ;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1712:17: ( '>=' | '=>' )
			int alt162=2;
			int LA162_0 = input.LA(1);
			if ( (LA162_0=='>') ) {
				alt162=1;
			}
			else if ( (LA162_0=='=') ) {
				alt162=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 162, 0, input);
				throw nvae;
			}

			switch (alt162) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1712:19: '>='
					{
					match(">="); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1712:26: '=>'
					{
					match("=>"); if (state.failed) return;

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "GREATER_OR_EQ"

	// $ANTLR start "LESS_OR_EQ"
	public final void mLESS_OR_EQ() throws RecognitionException {
		try {
			int _type = LESS_OR_EQ;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1713:17: ( '=<' | '<=' )
			int alt163=2;
			int LA163_0 = input.LA(1);
			if ( (LA163_0=='=') ) {
				alt163=1;
			}
			else if ( (LA163_0=='<') ) {
				alt163=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 163, 0, input);
				throw nvae;
			}

			switch (alt163) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1713:19: '=<'
					{
					match("=<"); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1713:26: '<='
					{
					match("<="); if (state.failed) return;

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LESS_OR_EQ"

	// $ANTLR start "LESS_AND"
	public final void mLESS_AND() throws RecognitionException {
		try {
			int _type = LESS_AND;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:17: ( '&' ( '-' )* )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:19: '&' ( '-' )*
			{
			match('&'); if (state.failed) return;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:23: ( '-' )*
			loop164:
			while (true) {
				int alt164=2;
				int LA164_0 = input.LA(1);
				if ( (LA164_0=='-') ) {
					alt164=1;
				}

				switch (alt164) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:23: '-'
					{
					match('-'); if (state.failed) return;
					}
					break;

				default :
					break loop164;
				}
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LESS_AND"

	// $ANTLR start "CP_DOTS"
	public final void mCP_DOTS() throws RecognitionException {
		try {
			int _type = CP_DOTS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1715:17: ( '...' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1715:19: '...'
			{
			match("..."); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CP_DOTS"

	// $ANTLR start "LESS_REST"
	public final void mLESS_REST() throws RecognitionException {
		try {
			int _type = LESS_REST;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1716:17: ( '@rest...' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1716:19: '@rest...'
			{
			match("@rest..."); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LESS_REST"

	// $ANTLR start "INVALID"
	public final void mINVALID() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1721:21: ()
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1721:22: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "INVALID"

	// $ANTLR start "STRING"
	public final void mSTRING() throws RecognitionException {
		try {
			int _type = STRING;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1722:17: ( '\\'' (~ ( '\\r' | '\\f' | '\\'' ) )* ( '\\'' |) | '\"' ( ( ( '\\\\' '\\\"' )=> '\\\\' '\\\"' ) |~ ( '\\r' | '\\f' | '\"' ) )* ( '\"' |) )
			int alt169=2;
			int LA169_0 = input.LA(1);
			if ( (LA169_0=='\'') ) {
				alt169=1;
			}
			else if ( (LA169_0=='\"') ) {
				alt169=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 169, 0, input);
				throw nvae;
			}

			switch (alt169) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1722:19: '\\'' (~ ( '\\r' | '\\f' | '\\'' ) )* ( '\\'' |)
					{
					match('\''); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1722:24: (~ ( '\\r' | '\\f' | '\\'' ) )*
					loop165:
					while (true) {
						int alt165=2;
						int LA165_0 = input.LA(1);
						if ( ((LA165_0 >= '\u0000' && LA165_0 <= '\u000B')||(LA165_0 >= '\u000E' && LA165_0 <= '&')||(LA165_0 >= '(' && LA165_0 <= '\uFFFF')) ) {
							alt165=1;
						}

						switch (alt165) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
							{
							if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\u000B')||(input.LA(1) >= '\u000E' && input.LA(1) <= '&')||(input.LA(1) >= '(' && input.LA(1) <= '\uFFFF') ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

						default :
							break loop165;
						}
					}

					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1723:21: ( '\\'' |)
					int alt166=2;
					int LA166_0 = input.LA(1);
					if ( (LA166_0=='\'') ) {
						alt166=1;
					}

					else {
						alt166=2;
					}

					switch (alt166) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1724:27: '\\''
							{
							match('\''); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1725:27: 
							{
							if ( state.backtracking==0 ) { _type = INVALID; }
							}
							break;

					}

					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1728:19: '\"' ( ( ( '\\\\' '\\\"' )=> '\\\\' '\\\"' ) |~ ( '\\r' | '\\f' | '\"' ) )* ( '\"' |)
					{
					match('\"'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1728:23: ( ( ( '\\\\' '\\\"' )=> '\\\\' '\\\"' ) |~ ( '\\r' | '\\f' | '\"' ) )*
					loop167:
					while (true) {
						int alt167=3;
						int LA167_0 = input.LA(1);
						if ( (LA167_0=='\\') ) {
							int LA167_2 = input.LA(2);
							if ( (LA167_2=='\"') ) {
								int LA167_4 = input.LA(3);
								if ( (synpred1_Css3()) ) {
									alt167=1;
								}
								else if ( (true) ) {
									alt167=2;
								}

							}
							else {
								alt167=2;
							}

						}
						else if ( ((LA167_0 >= '\u0000' && LA167_0 <= '\u000B')||(LA167_0 >= '\u000E' && LA167_0 <= '!')||(LA167_0 >= '#' && LA167_0 <= '[')||(LA167_0 >= ']' && LA167_0 <= '\uFFFF')) ) {
							alt167=2;
						}

						switch (alt167) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1728:25: ( ( '\\\\' '\\\"' )=> '\\\\' '\\\"' )
							{
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1728:25: ( ( '\\\\' '\\\"' )=> '\\\\' '\\\"' )
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1728:26: ( '\\\\' '\\\"' )=> '\\\\' '\\\"'
							{
							match('\\'); if (state.failed) return;
							match('\"'); if (state.failed) return;
							}

							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1728:54: ~ ( '\\r' | '\\f' | '\"' )
							{
							if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\u000B')||(input.LA(1) >= '\u000E' && input.LA(1) <= '!')||(input.LA(1) >= '#' && input.LA(1) <= '\uFFFF') ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

						default :
							break loop167;
						}
					}

					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1729:21: ( '\"' |)
					int alt168=2;
					int LA168_0 = input.LA(1);
					if ( (LA168_0=='\"') ) {
						alt168=1;
					}

					else {
						alt168=2;
					}

					switch (alt168) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1730:27: '\"'
							{
							match('\"'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1731:27: 
							{
							if ( state.backtracking==0 ) { _type = INVALID; }
							}
							break;

					}

					}
					break;

			}
			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRING"

	// $ANTLR start "LESS_JS_STRING"
	public final void mLESS_JS_STRING() throws RecognitionException {
		try {
			int _type = LESS_JS_STRING;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1735:17: ( '`' (~ ( '\\r' | '\\f' | '`' ) )* ( '`' |) )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1735:19: '`' (~ ( '\\r' | '\\f' | '`' ) )* ( '`' |)
			{
			match('`'); if (state.failed) return;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1735:23: (~ ( '\\r' | '\\f' | '`' ) )*
			loop170:
			while (true) {
				int alt170=2;
				int LA170_0 = input.LA(1);
				if ( ((LA170_0 >= '\u0000' && LA170_0 <= '\u000B')||(LA170_0 >= '\u000E' && LA170_0 <= '_')||(LA170_0 >= 'a' && LA170_0 <= '\uFFFF')) ) {
					alt170=1;
				}

				switch (alt170) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\u000B')||(input.LA(1) >= '\u000E' && input.LA(1) <= '_')||(input.LA(1) >= 'a' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop170;
				}
			}

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1736:21: ( '`' |)
			int alt171=2;
			int LA171_0 = input.LA(1);
			if ( (LA171_0=='`') ) {
				alt171=1;
			}

			else {
				alt171=2;
			}

			switch (alt171) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1737:27: '`'
					{
					match('`'); if (state.failed) return;
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1738:27: 
					{
					if ( state.backtracking==0 ) { _type = INVALID; }
					}
					break;

			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LESS_JS_STRING"

	// $ANTLR start "NOT"
	public final void mNOT() throws RecognitionException {
		try {
			int _type = NOT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1742:6: ( 'NOT' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1742:8: 'NOT'
			{
			match("NOT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NOT"

	// $ANTLR start "IDENT"
	public final void mIDENT() throws RecognitionException {
		try {
			int _type = IDENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1747:17: ( ( '-' )? NMSTART ( NMCHAR )* )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1747:19: ( '-' )? NMSTART ( NMCHAR )*
			{
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1747:19: ( '-' )?
			int alt172=2;
			int LA172_0 = input.LA(1);
			if ( (LA172_0=='-') ) {
				alt172=1;
			}
			switch (alt172) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1747:19: '-'
					{
					match('-'); if (state.failed) return;
					}
					break;

			}

			mNMSTART(); if (state.failed) return;

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1747:32: ( NMCHAR )*
			loop173:
			while (true) {
				int alt173=2;
				int LA173_0 = input.LA(1);
				if ( (LA173_0=='-'||(LA173_0 >= '0' && LA173_0 <= '9')||(LA173_0 >= 'A' && LA173_0 <= 'Z')||LA173_0=='\\'||LA173_0=='_'||(LA173_0 >= 'a' && LA173_0 <= 'z')||(LA173_0 >= '\u0080' && LA173_0 <= '\uFFFF')) ) {
					alt173=1;
				}

				switch (alt173) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1747:32: NMCHAR
					{
					mNMCHAR(); if (state.failed) return;

					}
					break;

				default :
					break loop173;
				}
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IDENT"

	// $ANTLR start "HASH_SYMBOL"
	public final void mHASH_SYMBOL() throws RecognitionException {
		try {
			int _type = HASH_SYMBOL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1752:17: ( '#' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1752:19: '#'
			{
			match('#'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "HASH_SYMBOL"

	// $ANTLR start "HASH"
	public final void mHASH() throws RecognitionException {
		try {
			int _type = HASH;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1753:17: ( HASH_SYMBOL NAME )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1753:19: HASH_SYMBOL NAME
			{
			mHASH_SYMBOL(); if (state.failed) return;

			mNAME(); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "HASH"

	// $ANTLR start "IMPORTANT_SYM"
	public final void mIMPORTANT_SYM() throws RecognitionException {
		try {
			int _type = IMPORTANT_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1755:17: ( EXCLAMATION_MARK ( WS | COMMENT )* 'IMPORTANT' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1755:19: EXCLAMATION_MARK ( WS | COMMENT )* 'IMPORTANT'
			{
			mEXCLAMATION_MARK(); if (state.failed) return;

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1755:36: ( WS | COMMENT )*
			loop174:
			while (true) {
				int alt174=3;
				int LA174_0 = input.LA(1);
				if ( (LA174_0=='\t'||LA174_0==' ') ) {
					alt174=1;
				}
				else if ( (LA174_0=='/') ) {
					alt174=2;
				}

				switch (alt174) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1755:37: WS
					{
					mWS(); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1755:40: COMMENT
					{
					mCOMMENT(); if (state.failed) return;

					}
					break;

				default :
					break loop174;
				}
			}

			match("IMPORTANT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IMPORTANT_SYM"

	// $ANTLR start "IMPORT_SYM"
	public final void mIMPORT_SYM() throws RecognitionException {
		try {
			int _type = IMPORT_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1757:21: ( '@IMPORT' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1757:23: '@IMPORT'
			{
			match("@IMPORT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "IMPORT_SYM"

	// $ANTLR start "PAGE_SYM"
	public final void mPAGE_SYM() throws RecognitionException {
		try {
			int _type = PAGE_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1758:21: ( '@PAGE' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1758:23: '@PAGE'
			{
			match("@PAGE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "PAGE_SYM"

	// $ANTLR start "MEDIA_SYM"
	public final void mMEDIA_SYM() throws RecognitionException {
		try {
			int _type = MEDIA_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1759:21: ( '@MEDIA' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1759:23: '@MEDIA'
			{
			match("@MEDIA"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MEDIA_SYM"

	// $ANTLR start "NAMESPACE_SYM"
	public final void mNAMESPACE_SYM() throws RecognitionException {
		try {
			int _type = NAMESPACE_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1760:21: ( '@NAMESPACE' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1760:23: '@NAMESPACE'
			{
			match("@NAMESPACE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NAMESPACE_SYM"

	// $ANTLR start "CHARSET_SYM"
	public final void mCHARSET_SYM() throws RecognitionException {
		try {
			int _type = CHARSET_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1761:21: ( '@CHARSET' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1761:23: '@CHARSET'
			{
			match("@CHARSET"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "CHARSET_SYM"

	// $ANTLR start "COUNTER_STYLE_SYM"
	public final void mCOUNTER_STYLE_SYM() throws RecognitionException {
		try {
			int _type = COUNTER_STYLE_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1762:21: ( '@COUNTER-STYLE' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1762:23: '@COUNTER-STYLE'
			{
			match("@COUNTER-STYLE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COUNTER_STYLE_SYM"

	// $ANTLR start "FONT_FACE_SYM"
	public final void mFONT_FACE_SYM() throws RecognitionException {
		try {
			int _type = FONT_FACE_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1763:21: ( '@FONT-FACE' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1763:23: '@FONT-FACE'
			{
			match("@FONT-FACE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FONT_FACE_SYM"

	// $ANTLR start "TOPLEFTCORNER_SYM"
	public final void mTOPLEFTCORNER_SYM() throws RecognitionException {
		try {
			int _type = TOPLEFTCORNER_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1765:23: ( '@TOP-LEFT-CORNER' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1765:24: '@TOP-LEFT-CORNER'
			{
			match("@TOP-LEFT-CORNER"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TOPLEFTCORNER_SYM"

	// $ANTLR start "TOPLEFT_SYM"
	public final void mTOPLEFT_SYM() throws RecognitionException {
		try {
			int _type = TOPLEFT_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1766:23: ( '@TOP-LEFT' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1766:24: '@TOP-LEFT'
			{
			match("@TOP-LEFT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TOPLEFT_SYM"

	// $ANTLR start "TOPCENTER_SYM"
	public final void mTOPCENTER_SYM() throws RecognitionException {
		try {
			int _type = TOPCENTER_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1767:23: ( '@TOP-CENTER' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1767:24: '@TOP-CENTER'
			{
			match("@TOP-CENTER"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TOPCENTER_SYM"

	// $ANTLR start "TOPRIGHT_SYM"
	public final void mTOPRIGHT_SYM() throws RecognitionException {
		try {
			int _type = TOPRIGHT_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1768:23: ( '@TOP-RIGHT' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1768:24: '@TOP-RIGHT'
			{
			match("@TOP-RIGHT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TOPRIGHT_SYM"

	// $ANTLR start "TOPRIGHTCORNER_SYM"
	public final void mTOPRIGHTCORNER_SYM() throws RecognitionException {
		try {
			int _type = TOPRIGHTCORNER_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1769:23: ( '@TOP-RIGHT-CORNER' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1769:24: '@TOP-RIGHT-CORNER'
			{
			match("@TOP-RIGHT-CORNER"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TOPRIGHTCORNER_SYM"

	// $ANTLR start "BOTTOMLEFTCORNER_SYM"
	public final void mBOTTOMLEFTCORNER_SYM() throws RecognitionException {
		try {
			int _type = BOTTOMLEFTCORNER_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1770:23: ( '@BOTTOM-LEFT-CORNER' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1770:24: '@BOTTOM-LEFT-CORNER'
			{
			match("@BOTTOM-LEFT-CORNER"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BOTTOMLEFTCORNER_SYM"

	// $ANTLR start "BOTTOMLEFT_SYM"
	public final void mBOTTOMLEFT_SYM() throws RecognitionException {
		try {
			int _type = BOTTOMLEFT_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1771:23: ( '@BOTTOM-LEFT' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1771:24: '@BOTTOM-LEFT'
			{
			match("@BOTTOM-LEFT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BOTTOMLEFT_SYM"

	// $ANTLR start "BOTTOMCENTER_SYM"
	public final void mBOTTOMCENTER_SYM() throws RecognitionException {
		try {
			int _type = BOTTOMCENTER_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1772:23: ( '@BOTTOM-CENTER' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1772:24: '@BOTTOM-CENTER'
			{
			match("@BOTTOM-CENTER"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BOTTOMCENTER_SYM"

	// $ANTLR start "BOTTOMRIGHT_SYM"
	public final void mBOTTOMRIGHT_SYM() throws RecognitionException {
		try {
			int _type = BOTTOMRIGHT_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1773:23: ( '@BOTTOM-RIGHT' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1773:24: '@BOTTOM-RIGHT'
			{
			match("@BOTTOM-RIGHT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BOTTOMRIGHT_SYM"

	// $ANTLR start "BOTTOMRIGHTCORNER_SYM"
	public final void mBOTTOMRIGHTCORNER_SYM() throws RecognitionException {
		try {
			int _type = BOTTOMRIGHTCORNER_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1774:23: ( '@BOTTOM-RIGHT-CORNER' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1774:24: '@BOTTOM-RIGHT-CORNER'
			{
			match("@BOTTOM-RIGHT-CORNER"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "BOTTOMRIGHTCORNER_SYM"

	// $ANTLR start "LEFTTOP_SYM"
	public final void mLEFTTOP_SYM() throws RecognitionException {
		try {
			int _type = LEFTTOP_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1775:23: ( '@LEFT-TOP' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1775:24: '@LEFT-TOP'
			{
			match("@LEFT-TOP"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LEFTTOP_SYM"

	// $ANTLR start "LEFTMIDDLE_SYM"
	public final void mLEFTMIDDLE_SYM() throws RecognitionException {
		try {
			int _type = LEFTMIDDLE_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1776:23: ( '@LEFT-MIDDLE' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1776:24: '@LEFT-MIDDLE'
			{
			match("@LEFT-MIDDLE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LEFTMIDDLE_SYM"

	// $ANTLR start "LEFTBOTTOM_SYM"
	public final void mLEFTBOTTOM_SYM() throws RecognitionException {
		try {
			int _type = LEFTBOTTOM_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1777:23: ( '@LEFT-BOTTOM' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1777:24: '@LEFT-BOTTOM'
			{
			match("@LEFT-BOTTOM"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LEFTBOTTOM_SYM"

	// $ANTLR start "RIGHTTOP_SYM"
	public final void mRIGHTTOP_SYM() throws RecognitionException {
		try {
			int _type = RIGHTTOP_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1778:23: ( '@RIGHT-TOP' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1778:24: '@RIGHT-TOP'
			{
			match("@RIGHT-TOP"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RIGHTTOP_SYM"

	// $ANTLR start "RIGHTMIDDLE_SYM"
	public final void mRIGHTMIDDLE_SYM() throws RecognitionException {
		try {
			int _type = RIGHTMIDDLE_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1779:23: ( '@RIGHT-MIDDLE' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1779:24: '@RIGHT-MIDDLE'
			{
			match("@RIGHT-MIDDLE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RIGHTMIDDLE_SYM"

	// $ANTLR start "RIGHTBOTTOM_SYM"
	public final void mRIGHTBOTTOM_SYM() throws RecognitionException {
		try {
			int _type = RIGHTBOTTOM_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1780:23: ( '@RIGHT-BOTTOM' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1780:24: '@RIGHT-BOTTOM'
			{
			match("@RIGHT-BOTTOM"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RIGHTBOTTOM_SYM"

	// $ANTLR start "MOZ_DOCUMENT_SYM"
	public final void mMOZ_DOCUMENT_SYM() throws RecognitionException {
		try {
			int _type = MOZ_DOCUMENT_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1782:23: ( '@-MOZ-DOCUMENT' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1782:25: '@-MOZ-DOCUMENT'
			{
			match("@-MOZ-DOCUMENT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MOZ_DOCUMENT_SYM"

	// $ANTLR start "WEBKIT_KEYFRAMES_SYM"
	public final void mWEBKIT_KEYFRAMES_SYM() throws RecognitionException {
		try {
			int _type = WEBKIT_KEYFRAMES_SYM;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1783:23: ( '@-WEBKIT-KEYFRAMES' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1783:25: '@-WEBKIT-KEYFRAMES'
			{
			match("@-WEBKIT-KEYFRAMES"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WEBKIT_KEYFRAMES_SYM"

	// $ANTLR start "SASS_CONTENT"
	public final void mSASS_CONTENT() throws RecognitionException {
		try {
			int _type = SASS_CONTENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1786:21: ( '@CONTENT' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1786:23: '@CONTENT'
			{
			match("@CONTENT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_CONTENT"

	// $ANTLR start "SASS_MIXIN"
	public final void mSASS_MIXIN() throws RecognitionException {
		try {
			int _type = SASS_MIXIN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1787:21: ( '@MIXIN' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1787:23: '@MIXIN'
			{
			match("@MIXIN"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_MIXIN"

	// $ANTLR start "SASS_INCLUDE"
	public final void mSASS_INCLUDE() throws RecognitionException {
		try {
			int _type = SASS_INCLUDE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1788:21: ( '@INCLUDE' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1788:23: '@INCLUDE'
			{
			match("@INCLUDE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_INCLUDE"

	// $ANTLR start "SASS_EXTEND"
	public final void mSASS_EXTEND() throws RecognitionException {
		try {
			int _type = SASS_EXTEND;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1789:21: ( '@EXTEND' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1789:23: '@EXTEND'
			{
			match("@EXTEND"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_EXTEND"

	// $ANTLR start "SASS_DEBUG"
	public final void mSASS_DEBUG() throws RecognitionException {
		try {
			int _type = SASS_DEBUG;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1790:21: ( '@DEBUG' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1790:23: '@DEBUG'
			{
			match("@DEBUG"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_DEBUG"

	// $ANTLR start "SASS_ERROR"
	public final void mSASS_ERROR() throws RecognitionException {
		try {
			int _type = SASS_ERROR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1791:21: ( '@ERROR' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1791:23: '@ERROR'
			{
			match("@ERROR"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_ERROR"

	// $ANTLR start "SASS_WARN"
	public final void mSASS_WARN() throws RecognitionException {
		try {
			int _type = SASS_WARN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1792:21: ( '@WARN' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1792:23: '@WARN'
			{
			match("@WARN"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_WARN"

	// $ANTLR start "SASS_IF"
	public final void mSASS_IF() throws RecognitionException {
		try {
			int _type = SASS_IF;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1793:21: ( '@IF' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1793:23: '@IF'
			{
			match("@IF"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_IF"

	// $ANTLR start "SASS_ELSE"
	public final void mSASS_ELSE() throws RecognitionException {
		try {
			int _type = SASS_ELSE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1794:21: ( '@ELSE' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1794:23: '@ELSE'
			{
			match("@ELSE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_ELSE"

	// $ANTLR start "SASS_ELSEIF"
	public final void mSASS_ELSEIF() throws RecognitionException {
		try {
			int _type = SASS_ELSEIF;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1795:21: ( '@ELSEIF' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1795:23: '@ELSEIF'
			{
			match("@ELSEIF"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_ELSEIF"

	// $ANTLR start "SASS_FOR"
	public final void mSASS_FOR() throws RecognitionException {
		try {
			int _type = SASS_FOR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1796:21: ( '@FOR' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1796:23: '@FOR'
			{
			match("@FOR"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_FOR"

	// $ANTLR start "SASS_FUNCTION"
	public final void mSASS_FUNCTION() throws RecognitionException {
		try {
			int _type = SASS_FUNCTION;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1797:21: ( '@FUNCTION' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1797:23: '@FUNCTION'
			{
			match("@FUNCTION"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_FUNCTION"

	// $ANTLR start "SASS_RETURN"
	public final void mSASS_RETURN() throws RecognitionException {
		try {
			int _type = SASS_RETURN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1798:21: ( '@RETURN' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1798:23: '@RETURN'
			{
			match("@RETURN"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_RETURN"

	// $ANTLR start "SASS_EACH"
	public final void mSASS_EACH() throws RecognitionException {
		try {
			int _type = SASS_EACH;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1800:21: ( '@EACH' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1800:23: '@EACH'
			{
			match("@EACH"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_EACH"

	// $ANTLR start "SASS_WHILE"
	public final void mSASS_WHILE() throws RecognitionException {
		try {
			int _type = SASS_WHILE;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1801:21: ( '@WHILE' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1801:23: '@WHILE'
			{
			match("@WHILE"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_WHILE"

	// $ANTLR start "SASS_AT_ROOT"
	public final void mSASS_AT_ROOT() throws RecognitionException {
		try {
			int _type = SASS_AT_ROOT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1802:21: ( '@AT-ROOT' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1802:23: '@AT-ROOT'
			{
			match("@AT-ROOT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_AT_ROOT"

	// $ANTLR start "AT_SIGN"
	public final void mAT_SIGN() throws RecognitionException {
		try {
			int _type = AT_SIGN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1804:21: ( '@' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1804:23: '@'
			{
			match('@'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "AT_SIGN"

	// $ANTLR start "AT_IDENT"
	public final void mAT_IDENT() throws RecognitionException {
		try {
			int _type = AT_IDENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1805:14: ( AT_SIGN ( NMCHAR )+ )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1805:16: AT_SIGN ( NMCHAR )+
			{
			mAT_SIGN(); if (state.failed) return;

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1805:24: ( NMCHAR )+
			int cnt175=0;
			loop175:
			while (true) {
				int alt175=2;
				int LA175_0 = input.LA(1);
				if ( (LA175_0=='-'||(LA175_0 >= '0' && LA175_0 <= '9')||(LA175_0 >= 'A' && LA175_0 <= 'Z')||LA175_0=='\\'||LA175_0=='_'||(LA175_0 >= 'a' && LA175_0 <= 'z')||(LA175_0 >= '\u0080' && LA175_0 <= '\uFFFF')) ) {
					alt175=1;
				}

				switch (alt175) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1805:24: NMCHAR
					{
					mNMCHAR(); if (state.failed) return;

					}
					break;

				default :
					if ( cnt175 >= 1 ) break loop175;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(175, input);
					throw eee;
				}
				cnt175++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "AT_IDENT"

	// $ANTLR start "SASS_VAR"
	public final void mSASS_VAR() throws RecognitionException {
		try {
			int _type = SASS_VAR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1807:21: ( '$' ( NMCHAR )+ )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1807:23: '$' ( NMCHAR )+
			{
			match('$'); if (state.failed) return;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1807:27: ( NMCHAR )+
			int cnt176=0;
			loop176:
			while (true) {
				int alt176=2;
				int LA176_0 = input.LA(1);
				if ( (LA176_0=='-'||(LA176_0 >= '0' && LA176_0 <= '9')||(LA176_0 >= 'A' && LA176_0 <= 'Z')||LA176_0=='\\'||LA176_0=='_'||(LA176_0 >= 'a' && LA176_0 <= 'z')||(LA176_0 >= '\u0080' && LA176_0 <= '\uFFFF')) ) {
					alt176=1;
				}

				switch (alt176) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1807:27: NMCHAR
					{
					mNMCHAR(); if (state.failed) return;

					}
					break;

				default :
					if ( cnt176 >= 1 ) break loop176;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(176, input);
					throw eee;
				}
				cnt176++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_VAR"

	// $ANTLR start "SASS_DEFAULT"
	public final void mSASS_DEFAULT() throws RecognitionException {
		try {
			int _type = SASS_DEFAULT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1808:21: ( '!DEFAULT' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1808:23: '!DEFAULT'
			{
			match("!DEFAULT"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_DEFAULT"

	// $ANTLR start "SASS_OPTIONAL"
	public final void mSASS_OPTIONAL() throws RecognitionException {
		try {
			int _type = SASS_OPTIONAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1809:21: ( '!OPTIONAL' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1809:23: '!OPTIONAL'
			{
			match("!OPTIONAL"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_OPTIONAL"

	// $ANTLR start "SASS_GLOBAL"
	public final void mSASS_GLOBAL() throws RecognitionException {
		try {
			int _type = SASS_GLOBAL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1810:21: ( '!GLOBAL' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1810:23: '!GLOBAL'
			{
			match("!GLOBAL"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_GLOBAL"

	// $ANTLR start "SASS_EXTEND_ONLY_SELECTOR"
	public final void mSASS_EXTEND_ONLY_SELECTOR() throws RecognitionException {
		try {
			int _type = SASS_EXTEND_ONLY_SELECTOR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1813:21: ( PERCENTAGE_SYMBOL ( NMCHAR )+ )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1813:23: PERCENTAGE_SYMBOL ( NMCHAR )+
			{
			mPERCENTAGE_SYMBOL(); if (state.failed) return;

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1813:41: ( NMCHAR )+
			int cnt177=0;
			loop177:
			while (true) {
				int alt177=2;
				int LA177_0 = input.LA(1);
				if ( (LA177_0=='-'||(LA177_0 >= '0' && LA177_0 <= '9')||(LA177_0 >= 'A' && LA177_0 <= 'Z')||LA177_0=='\\'||LA177_0=='_'||(LA177_0 >= 'a' && LA177_0 <= 'z')||(LA177_0 >= '\u0080' && LA177_0 <= '\uFFFF')) ) {
					alt177=1;
				}

				switch (alt177) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1813:41: NMCHAR
					{
					mNMCHAR(); if (state.failed) return;

					}
					break;

				default :
					if ( cnt177 >= 1 ) break loop177;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(177, input);
					throw eee;
				}
				cnt177++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "SASS_EXTEND_ONLY_SELECTOR"

	// $ANTLR start "EMS"
	public final void mEMS() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1825:25: ()
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1825:26: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "EMS"

	// $ANTLR start "EXS"
	public final void mEXS() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1826:25: ()
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1826:26: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "EXS"

	// $ANTLR start "LENGTH"
	public final void mLENGTH() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1827:25: ()
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1827:26: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LENGTH"

	// $ANTLR start "REM"
	public final void mREM() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1828:18: ()
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1828:19: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "REM"

	// $ANTLR start "ANGLE"
	public final void mANGLE() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1829:25: ()
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1829:26: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ANGLE"

	// $ANTLR start "TIME"
	public final void mTIME() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1830:25: ()
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1830:26: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "TIME"

	// $ANTLR start "FREQ"
	public final void mFREQ() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1831:25: ()
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1831:26: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "FREQ"

	// $ANTLR start "DIMENSION"
	public final void mDIMENSION() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1832:25: ()
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1832:26: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "DIMENSION"

	// $ANTLR start "PERCENTAGE"
	public final void mPERCENTAGE() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1833:25: ()
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1833:26: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "PERCENTAGE"

	// $ANTLR start "RESOLUTION"
	public final void mRESOLUTION() throws RecognitionException {
		try {
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1834:25: ()
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1834:26: 
			{
			}

		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "RESOLUTION"

	// $ANTLR start "NUMBER"
	public final void mNUMBER() throws RecognitionException {
		try {
			int _type = NUMBER;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1837:5: ( ( ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ ) ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R ( A | E ) )=> R ( A D | E M ) | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | PERCENTAGE_SYMBOL |) )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1837:9: ( ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ ) ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R ( A | E ) )=> R ( A D | E M ) | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | PERCENTAGE_SYMBOL |)
			{
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1837:9: ( ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ )
			int alt182=2;
			int LA182_0 = input.LA(1);
			if ( ((LA182_0 >= '0' && LA182_0 <= '9')) ) {
				alt182=1;
			}
			else if ( (LA182_0=='.') ) {
				alt182=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 182, 0, input);
				throw nvae;
			}

			switch (alt182) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1838:15: ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )?
					{
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1838:15: ( '0' .. '9' )+
					int cnt178=0;
					loop178:
					while (true) {
						int alt178=2;
						int LA178_0 = input.LA(1);
						if ( ((LA178_0 >= '0' && LA178_0 <= '9')) ) {
							alt178=1;
						}

						switch (alt178) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
							{
							if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

						default :
							if ( cnt178 >= 1 ) break loop178;
							if (state.backtracking>0) {state.failed=true; return;}
							EarlyExitException eee = new EarlyExitException(178, input);
							throw eee;
						}
						cnt178++;
					}

					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1838:25: ( '.' ( '0' .. '9' )+ )?
					int alt180=2;
					int LA180_0 = input.LA(1);
					if ( (LA180_0=='.') ) {
						alt180=1;
					}
					switch (alt180) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1838:26: '.' ( '0' .. '9' )+
							{
							match('.'); if (state.failed) return;
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1838:30: ( '0' .. '9' )+
							int cnt179=0;
							loop179:
							while (true) {
								int alt179=2;
								int LA179_0 = input.LA(1);
								if ( ((LA179_0 >= '0' && LA179_0 <= '9')) ) {
									alt179=1;
								}

								switch (alt179) {
								case 1 :
									// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
									{
									if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
										input.consume();
										state.failed=false;
									}
									else {
										if (state.backtracking>0) {state.failed=true; return;}
										MismatchedSetException mse = new MismatchedSetException(null,input);
										recover(mse);
										throw mse;
									}
									}
									break;

								default :
									if ( cnt179 >= 1 ) break loop179;
									if (state.backtracking>0) {state.failed=true; return;}
									EarlyExitException eee = new EarlyExitException(179, input);
									throw eee;
								}
								cnt179++;
							}

							}
							break;

					}

					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1839:15: '.' ( '0' .. '9' )+
					{
					match('.'); if (state.failed) return;
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1839:19: ( '0' .. '9' )+
					int cnt181=0;
					loop181:
					while (true) {
						int alt181=2;
						int LA181_0 = input.LA(1);
						if ( ((LA181_0 >= '0' && LA181_0 <= '9')) ) {
							alt181=1;
						}

						switch (alt181) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
							{
							if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
								input.consume();
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								recover(mse);
								throw mse;
							}
							}
							break;

						default :
							if ( cnt181 >= 1 ) break loop181;
							if (state.backtracking>0) {state.failed=true; return;}
							EarlyExitException eee = new EarlyExitException(181, input);
							throw eee;
						}
						cnt181++;
					}

					}
					break;

			}

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1841:9: ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R ( A | E ) )=> R ( A D | E M ) | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | PERCENTAGE_SYMBOL |)
			int alt189=13;
			alt189 = dfa189.predict(input);
			switch (alt189) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1842:15: ( D P ( I | C ) )=> D P ( I | C M )
					{
					mD(); if (state.failed) return;

					mP(); if (state.failed) return;

					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1844:17: ( I | C M )
					int alt183=2;
					switch ( input.LA(1) ) {
					case 'I':
					case 'i':
						{
						alt183=1;
						}
						break;
					case '\\':
						{
						switch ( input.LA(2) ) {
						case 'I':
						case 'i':
							{
							alt183=1;
							}
							break;
						case '0':
							{
							int LA183_4 = input.LA(3);
							if ( (LA183_4=='0') ) {
								int LA183_6 = input.LA(4);
								if ( (LA183_6=='0') ) {
									int LA183_7 = input.LA(5);
									if ( (LA183_7=='0') ) {
										int LA183_8 = input.LA(6);
										if ( (LA183_8=='4'||LA183_8=='6') ) {
											int LA183_5 = input.LA(7);
											if ( (LA183_5=='9') ) {
												alt183=1;
											}
											else if ( (LA183_5=='3') ) {
												alt183=2;
											}

											else {
												if (state.backtracking>0) {state.failed=true; return;}
												int nvaeMark = input.mark();
												try {
													for (int nvaeConsume = 0; nvaeConsume < 7 - 1; nvaeConsume++) {
														input.consume();
													}
													NoViableAltException nvae =
														new NoViableAltException("", 183, 5, input);
													throw nvae;
												} finally {
													input.rewind(nvaeMark);
												}
											}

										}

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 183, 8, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

									}
									else if ( (LA183_7=='4'||LA183_7=='6') ) {
										int LA183_5 = input.LA(6);
										if ( (LA183_5=='9') ) {
											alt183=1;
										}
										else if ( (LA183_5=='3') ) {
											alt183=2;
										}

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 183, 5, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

									}

									else {
										if (state.backtracking>0) {state.failed=true; return;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 183, 7, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}

								}
								else if ( (LA183_6=='4'||LA183_6=='6') ) {
									int LA183_5 = input.LA(5);
									if ( (LA183_5=='9') ) {
										alt183=1;
									}
									else if ( (LA183_5=='3') ) {
										alt183=2;
									}

									else {
										if (state.backtracking>0) {state.failed=true; return;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 183, 5, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}

								}

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 183, 6, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}
							else if ( (LA183_4=='4'||LA183_4=='6') ) {
								int LA183_5 = input.LA(4);
								if ( (LA183_5=='9') ) {
									alt183=1;
								}
								else if ( (LA183_5=='3') ) {
									alt183=2;
								}

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 183, 5, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 183, 4, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						case '4':
						case '6':
							{
							int LA183_5 = input.LA(3);
							if ( (LA183_5=='9') ) {
								alt183=1;
							}
							else if ( (LA183_5=='3') ) {
								alt183=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 183, 5, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						default:
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								input.consume();
								NoViableAltException nvae =
									new NoViableAltException("", 183, 2, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}
						}
						break;
					case 'C':
					case 'c':
						{
						alt183=2;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 183, 0, input);
						throw nvae;
					}
					switch (alt183) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1845:22: I
							{
							mI(); if (state.failed) return;

							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1845:26: C M
							{
							mC(); if (state.failed) return;

							mM(); if (state.failed) return;

							}
							break;

					}

					if ( state.backtracking==0 ) { _type = RESOLUTION; }
					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1849:15: ( E ( M | X ) )=> E ( M | X )
					{
					mE(); if (state.failed) return;

					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1851:17: ( M | X )
					int alt184=2;
					switch ( input.LA(1) ) {
					case 'M':
					case 'm':
						{
						alt184=1;
						}
						break;
					case '\\':
						{
						switch ( input.LA(2) ) {
						case '4':
						case '6':
						case 'M':
						case 'm':
							{
							alt184=1;
							}
							break;
						case '0':
							{
							switch ( input.LA(3) ) {
							case '0':
								{
								switch ( input.LA(4) ) {
								case '0':
									{
									switch ( input.LA(5) ) {
									case '0':
										{
										int LA184_7 = input.LA(6);
										if ( (LA184_7=='4'||LA184_7=='6') ) {
											alt184=1;
										}
										else if ( (LA184_7=='5'||LA184_7=='7') ) {
											alt184=2;
										}

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 184, 7, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

										}
										break;
									case '4':
									case '6':
										{
										alt184=1;
										}
										break;
									case '5':
									case '7':
										{
										alt184=2;
										}
										break;
									default:
										if (state.backtracking>0) {state.failed=true; return;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 184, 6, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}
									}
									break;
								case '4':
								case '6':
									{
									alt184=1;
									}
									break;
								case '5':
								case '7':
									{
									alt184=2;
									}
									break;
								default:
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 184, 5, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}
								}
								break;
							case '4':
							case '6':
								{
								alt184=1;
								}
								break;
							case '5':
							case '7':
								{
								alt184=2;
								}
								break;
							default:
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 184, 4, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}
							}
							break;
						case '5':
						case '7':
						case 'X':
						case 'x':
							{
							alt184=2;
							}
							break;
						default:
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								input.consume();
								NoViableAltException nvae =
									new NoViableAltException("", 184, 2, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}
						}
						break;
					case 'X':
					case 'x':
						{
						alt184=2;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 184, 0, input);
						throw nvae;
					}
					switch (alt184) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1852:23: M
							{
							mM(); if (state.failed) return;

							if ( state.backtracking==0 ) { _type = EMS;          }
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1853:23: X
							{
							mX(); if (state.failed) return;

							if ( state.backtracking==0 ) { _type = EXS;          }
							}
							break;

					}

					}
					break;
				case 3 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:15: ( P ( X | T | C ) )=> P ( X | T | C )
					{
					mP(); if (state.failed) return;

					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1857:17: ( X | T | C )
					int alt185=3;
					switch ( input.LA(1) ) {
					case 'X':
					case 'x':
						{
						alt185=1;
						}
						break;
					case '\\':
						{
						switch ( input.LA(2) ) {
						case 'X':
						case 'x':
							{
							alt185=1;
							}
							break;
						case '0':
							{
							switch ( input.LA(3) ) {
							case '0':
								{
								switch ( input.LA(4) ) {
								case '0':
									{
									switch ( input.LA(5) ) {
									case '0':
										{
										int LA185_9 = input.LA(6);
										if ( (LA185_9=='5'||LA185_9=='7') ) {
											int LA185_6 = input.LA(7);
											if ( (LA185_6=='8') ) {
												alt185=1;
											}
											else if ( (LA185_6=='4') ) {
												alt185=2;
											}

											else {
												if (state.backtracking>0) {state.failed=true; return;}
												int nvaeMark = input.mark();
												try {
													for (int nvaeConsume = 0; nvaeConsume < 7 - 1; nvaeConsume++) {
														input.consume();
													}
													NoViableAltException nvae =
														new NoViableAltException("", 185, 6, input);
													throw nvae;
												} finally {
													input.rewind(nvaeMark);
												}
											}

										}
										else if ( (LA185_9=='4'||LA185_9=='6') ) {
											alt185=3;
										}

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 185, 9, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

										}
										break;
									case '5':
									case '7':
										{
										int LA185_6 = input.LA(6);
										if ( (LA185_6=='8') ) {
											alt185=1;
										}
										else if ( (LA185_6=='4') ) {
											alt185=2;
										}

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 185, 6, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

										}
										break;
									case '4':
									case '6':
										{
										alt185=3;
										}
										break;
									default:
										if (state.backtracking>0) {state.failed=true; return;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 185, 8, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}
									}
									break;
								case '5':
								case '7':
									{
									int LA185_6 = input.LA(5);
									if ( (LA185_6=='8') ) {
										alt185=1;
									}
									else if ( (LA185_6=='4') ) {
										alt185=2;
									}

									else {
										if (state.backtracking>0) {state.failed=true; return;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 185, 6, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}

									}
									break;
								case '4':
								case '6':
									{
									alt185=3;
									}
									break;
								default:
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 185, 7, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}
								}
								break;
							case '5':
							case '7':
								{
								int LA185_6 = input.LA(4);
								if ( (LA185_6=='8') ) {
									alt185=1;
								}
								else if ( (LA185_6=='4') ) {
									alt185=2;
								}

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 185, 6, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

								}
								break;
							case '4':
							case '6':
								{
								alt185=3;
								}
								break;
							default:
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 185, 5, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}
							}
							break;
						case '5':
						case '7':
							{
							int LA185_6 = input.LA(3);
							if ( (LA185_6=='8') ) {
								alt185=1;
							}
							else if ( (LA185_6=='4') ) {
								alt185=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 185, 6, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						case 'T':
						case 't':
							{
							alt185=2;
							}
							break;
						case '4':
						case '6':
							{
							alt185=3;
							}
							break;
						default:
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								input.consume();
								NoViableAltException nvae =
									new NoViableAltException("", 185, 2, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}
						}
						break;
					case 'T':
					case 't':
						{
						alt185=2;
						}
						break;
					case 'C':
					case 'c':
						{
						alt185=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 185, 0, input);
						throw nvae;
					}
					switch (alt185) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1858:23: X
							{
							mX(); if (state.failed) return;

							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1859:23: T
							{
							mT(); if (state.failed) return;

							}
							break;
						case 3 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1860:23: C
							{
							mC(); if (state.failed) return;

							}
							break;

					}

					if ( state.backtracking==0 ) { _type = LENGTH;       }
					}
					break;
				case 4 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1863:15: ( C M )=> C M
					{
					mC(); if (state.failed) return;

					mM(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = LENGTH;       }
					}
					break;
				case 5 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1865:15: ( M ( M | S ) )=> M ( M | S )
					{
					mM(); if (state.failed) return;

					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1867:17: ( M | S )
					int alt186=2;
					switch ( input.LA(1) ) {
					case 'M':
					case 'm':
						{
						alt186=1;
						}
						break;
					case '\\':
						{
						switch ( input.LA(2) ) {
						case '4':
						case '6':
						case 'M':
						case 'm':
							{
							alt186=1;
							}
							break;
						case '0':
							{
							switch ( input.LA(3) ) {
							case '0':
								{
								switch ( input.LA(4) ) {
								case '0':
									{
									switch ( input.LA(5) ) {
									case '0':
										{
										int LA186_7 = input.LA(6);
										if ( (LA186_7=='4'||LA186_7=='6') ) {
											alt186=1;
										}
										else if ( (LA186_7=='5'||LA186_7=='7') ) {
											alt186=2;
										}

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 186, 7, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

										}
										break;
									case '4':
									case '6':
										{
										alt186=1;
										}
										break;
									case '5':
									case '7':
										{
										alt186=2;
										}
										break;
									default:
										if (state.backtracking>0) {state.failed=true; return;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 186, 6, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}
									}
									break;
								case '4':
								case '6':
									{
									alt186=1;
									}
									break;
								case '5':
								case '7':
									{
									alt186=2;
									}
									break;
								default:
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 186, 5, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}
								}
								break;
							case '4':
							case '6':
								{
								alt186=1;
								}
								break;
							case '5':
							case '7':
								{
								alt186=2;
								}
								break;
							default:
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 186, 4, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}
							}
							break;
						case '5':
						case '7':
						case 'S':
						case 's':
							{
							alt186=2;
							}
							break;
						default:
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								input.consume();
								NoViableAltException nvae =
									new NoViableAltException("", 186, 2, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}
						}
						break;
					case 'S':
					case 's':
						{
						alt186=2;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 186, 0, input);
						throw nvae;
					}
					switch (alt186) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1868:23: M
							{
							mM(); if (state.failed) return;

							if ( state.backtracking==0 ) { _type = LENGTH;       }
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1870:23: S
							{
							mS(); if (state.failed) return;

							if ( state.backtracking==0 ) { _type = TIME;         }
							}
							break;

					}

					}
					break;
				case 6 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1872:15: ( I N )=> I N
					{
					mI(); if (state.failed) return;

					mN(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = LENGTH;       }
					}
					break;
				case 7 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1875:15: ( D E G )=> D E G
					{
					mD(); if (state.failed) return;

					mE(); if (state.failed) return;

					mG(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = ANGLE;        }
					}
					break;
				case 8 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1880:15: ( R ( A | E ) )=> R ( A D | E M )
					{
					mR(); if (state.failed) return;

					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1882:17: ( A D | E M )
					int alt187=2;
					switch ( input.LA(1) ) {
					case 'A':
					case 'a':
						{
						alt187=1;
						}
						break;
					case '\\':
						{
						int LA187_2 = input.LA(2);
						if ( (LA187_2=='0') ) {
							int LA187_4 = input.LA(3);
							if ( (LA187_4=='0') ) {
								int LA187_6 = input.LA(4);
								if ( (LA187_6=='0') ) {
									int LA187_7 = input.LA(5);
									if ( (LA187_7=='0') ) {
										int LA187_8 = input.LA(6);
										if ( (LA187_8=='4'||LA187_8=='6') ) {
											int LA187_5 = input.LA(7);
											if ( (LA187_5=='1') ) {
												alt187=1;
											}
											else if ( (LA187_5=='5') ) {
												alt187=2;
											}

											else {
												if (state.backtracking>0) {state.failed=true; return;}
												int nvaeMark = input.mark();
												try {
													for (int nvaeConsume = 0; nvaeConsume < 7 - 1; nvaeConsume++) {
														input.consume();
													}
													NoViableAltException nvae =
														new NoViableAltException("", 187, 5, input);
													throw nvae;
												} finally {
													input.rewind(nvaeMark);
												}
											}

										}

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 187, 8, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

									}
									else if ( (LA187_7=='4'||LA187_7=='6') ) {
										int LA187_5 = input.LA(6);
										if ( (LA187_5=='1') ) {
											alt187=1;
										}
										else if ( (LA187_5=='5') ) {
											alt187=2;
										}

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 187, 5, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

									}

									else {
										if (state.backtracking>0) {state.failed=true; return;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 187, 7, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}

								}
								else if ( (LA187_6=='4'||LA187_6=='6') ) {
									int LA187_5 = input.LA(5);
									if ( (LA187_5=='1') ) {
										alt187=1;
									}
									else if ( (LA187_5=='5') ) {
										alt187=2;
									}

									else {
										if (state.backtracking>0) {state.failed=true; return;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 187, 5, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}

								}

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 187, 6, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}
							else if ( (LA187_4=='4'||LA187_4=='6') ) {
								int LA187_5 = input.LA(4);
								if ( (LA187_5=='1') ) {
									alt187=1;
								}
								else if ( (LA187_5=='5') ) {
									alt187=2;
								}

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 187, 5, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 187, 4, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}
						else if ( (LA187_2=='4'||LA187_2=='6') ) {
							int LA187_5 = input.LA(3);
							if ( (LA187_5=='1') ) {
								alt187=1;
							}
							else if ( (LA187_5=='5') ) {
								alt187=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 187, 5, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}

						else {
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								input.consume();
								NoViableAltException nvae =
									new NoViableAltException("", 187, 2, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

						}
						break;
					case 'E':
					case 'e':
						{
						alt187=2;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 187, 0, input);
						throw nvae;
					}
					switch (alt187) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1883:20: A D
							{
							mA(); if (state.failed) return;

							mD(); if (state.failed) return;

							if ( state.backtracking==0 ) {_type = ANGLE;         }
							}
							break;
						case 2 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1884:20: E M
							{
							mE(); if (state.failed) return;

							mM(); if (state.failed) return;

							if ( state.backtracking==0 ) {_type = REM;           }
							}
							break;

					}

					}
					break;
				case 9 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1887:15: ( S )=> S
					{
					mS(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = TIME;         }
					}
					break;
				case 10 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1889:15: ( ( K )? H Z )=> ( K )? H Z
					{
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1890:17: ( K )?
					int alt188=2;
					int LA188_0 = input.LA(1);
					if ( (LA188_0=='K'||LA188_0=='k') ) {
						alt188=1;
					}
					else if ( (LA188_0=='\\') ) {
						switch ( input.LA(2) ) {
							case 'K':
							case 'k':
								{
								alt188=1;
								}
								break;
							case '0':
								{
								int LA188_4 = input.LA(3);
								if ( (LA188_4=='0') ) {
									int LA188_6 = input.LA(4);
									if ( (LA188_6=='0') ) {
										int LA188_7 = input.LA(5);
										if ( (LA188_7=='0') ) {
											int LA188_8 = input.LA(6);
											if ( (LA188_8=='4'||LA188_8=='6') ) {
												int LA188_5 = input.LA(7);
												if ( (LA188_5=='B'||LA188_5=='b') ) {
													alt188=1;
												}
											}
										}
										else if ( (LA188_7=='4'||LA188_7=='6') ) {
											int LA188_5 = input.LA(6);
											if ( (LA188_5=='B'||LA188_5=='b') ) {
												alt188=1;
											}
										}
									}
									else if ( (LA188_6=='4'||LA188_6=='6') ) {
										int LA188_5 = input.LA(5);
										if ( (LA188_5=='B'||LA188_5=='b') ) {
											alt188=1;
										}
									}
								}
								else if ( (LA188_4=='4'||LA188_4=='6') ) {
									int LA188_5 = input.LA(4);
									if ( (LA188_5=='B'||LA188_5=='b') ) {
										alt188=1;
									}
								}
								}
								break;
							case '4':
							case '6':
								{
								int LA188_5 = input.LA(3);
								if ( (LA188_5=='B'||LA188_5=='b') ) {
									alt188=1;
								}
								}
								break;
						}
					}
					switch (alt188) {
						case 1 :
							// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1890:17: K
							{
							mK(); if (state.failed) return;

							}
							break;

					}

					mH(); if (state.failed) return;

					mZ(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = FREQ;         }
					}
					break;
				case 11 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1892:15: IDENT
					{
					mIDENT(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = DIMENSION;    }
					}
					break;
				case 12 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1894:15: PERCENTAGE_SYMBOL
					{
					mPERCENTAGE_SYMBOL(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = PERCENTAGE;   }
					}
					break;
				case 13 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1897:9: 
					{
					}
					break;

			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NUMBER"

	// $ANTLR start "URI"
	public final void mURI() throws RecognitionException {
		try {
			int _type = URI;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1903:5: ( U R L '(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1903:9: U R L '(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')'
			{
			mU(); if (state.failed) return;

			mR(); if (state.failed) return;

			mL(); if (state.failed) return;

			match('('); if (state.failed) return;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1905:13: ( ( WS )=> WS )?
			int alt190=2;
			int LA190_0 = input.LA(1);
			if ( (LA190_0=='\t'||LA190_0==' ') ) {
				int LA190_1 = input.LA(2);
				if ( (synpred12_Css3()) ) {
					alt190=1;
				}
			}
			switch (alt190) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1905:14: ( WS )=> WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1905:25: ( URL | STRING )
			int alt191=2;
			int LA191_0 = input.LA(1);
			if ( (LA191_0=='\t'||(LA191_0 >= ' ' && LA191_0 <= '!')||(LA191_0 >= '#' && LA191_0 <= '&')||(LA191_0 >= ')' && LA191_0 <= ';')||LA191_0=='='||(LA191_0 >= '?' && LA191_0 <= '\\')||LA191_0=='_'||(LA191_0 >= 'a' && LA191_0 <= 'z')||LA191_0=='|'||LA191_0=='~'||(LA191_0 >= '\u0080' && LA191_0 <= '\uFFFF')) ) {
				alt191=1;
			}
			else if ( (LA191_0=='\"'||LA191_0=='\'') ) {
				alt191=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 191, 0, input);
				throw nvae;
			}

			switch (alt191) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1905:26: URL
					{
					mURL(); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1905:30: STRING
					{
					mSTRING(); if (state.failed) return;

					}
					break;

			}

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1905:38: ( WS )?
			int alt192=2;
			int LA192_0 = input.LA(1);
			if ( (LA192_0=='\t'||LA192_0==' ') ) {
				alt192=1;
			}
			switch (alt192) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1905:38: WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			match(')'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "URI"

	// $ANTLR start "MOZ_URL_PREFIX"
	public final void mMOZ_URL_PREFIX() throws RecognitionException {
		try {
			int _type = MOZ_URL_PREFIX;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1910:2: ( 'URL-PREFIX(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1911:2: 'URL-PREFIX(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')'
			{
			match("URL-PREFIX("); if (state.failed) return;

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1912:13: ( ( WS )=> WS )?
			int alt193=2;
			int LA193_0 = input.LA(1);
			if ( (LA193_0=='\t'||LA193_0==' ') ) {
				int LA193_1 = input.LA(2);
				if ( (synpred13_Css3()) ) {
					alt193=1;
				}
			}
			switch (alt193) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1912:14: ( WS )=> WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1912:25: ( URL | STRING )
			int alt194=2;
			int LA194_0 = input.LA(1);
			if ( (LA194_0=='\t'||(LA194_0 >= ' ' && LA194_0 <= '!')||(LA194_0 >= '#' && LA194_0 <= '&')||(LA194_0 >= ')' && LA194_0 <= ';')||LA194_0=='='||(LA194_0 >= '?' && LA194_0 <= '\\')||LA194_0=='_'||(LA194_0 >= 'a' && LA194_0 <= 'z')||LA194_0=='|'||LA194_0=='~'||(LA194_0 >= '\u0080' && LA194_0 <= '\uFFFF')) ) {
				alt194=1;
			}
			else if ( (LA194_0=='\"'||LA194_0=='\'') ) {
				alt194=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 194, 0, input);
				throw nvae;
			}

			switch (alt194) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1912:26: URL
					{
					mURL(); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1912:30: STRING
					{
					mSTRING(); if (state.failed) return;

					}
					break;

			}

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1912:38: ( WS )?
			int alt195=2;
			int LA195_0 = input.LA(1);
			if ( (LA195_0=='\t'||LA195_0==' ') ) {
				alt195=1;
			}
			switch (alt195) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1912:38: WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			match(')'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MOZ_URL_PREFIX"

	// $ANTLR start "MOZ_DOMAIN"
	public final void mMOZ_DOMAIN() throws RecognitionException {
		try {
			int _type = MOZ_DOMAIN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1918:2: ( 'DOMAIN(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1919:2: 'DOMAIN(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')'
			{
			match("DOMAIN("); if (state.failed) return;

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1920:13: ( ( WS )=> WS )?
			int alt196=2;
			int LA196_0 = input.LA(1);
			if ( (LA196_0=='\t'||LA196_0==' ') ) {
				int LA196_1 = input.LA(2);
				if ( (synpred14_Css3()) ) {
					alt196=1;
				}
			}
			switch (alt196) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1920:14: ( WS )=> WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1920:25: ( URL | STRING )
			int alt197=2;
			int LA197_0 = input.LA(1);
			if ( (LA197_0=='\t'||(LA197_0 >= ' ' && LA197_0 <= '!')||(LA197_0 >= '#' && LA197_0 <= '&')||(LA197_0 >= ')' && LA197_0 <= ';')||LA197_0=='='||(LA197_0 >= '?' && LA197_0 <= '\\')||LA197_0=='_'||(LA197_0 >= 'a' && LA197_0 <= 'z')||LA197_0=='|'||LA197_0=='~'||(LA197_0 >= '\u0080' && LA197_0 <= '\uFFFF')) ) {
				alt197=1;
			}
			else if ( (LA197_0=='\"'||LA197_0=='\'') ) {
				alt197=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 197, 0, input);
				throw nvae;
			}

			switch (alt197) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1920:26: URL
					{
					mURL(); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1920:30: STRING
					{
					mSTRING(); if (state.failed) return;

					}
					break;

			}

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1920:38: ( WS )?
			int alt198=2;
			int LA198_0 = input.LA(1);
			if ( (LA198_0=='\t'||LA198_0==' ') ) {
				alt198=1;
			}
			switch (alt198) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1920:38: WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			match(')'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MOZ_DOMAIN"

	// $ANTLR start "MOZ_REGEXP"
	public final void mMOZ_REGEXP() throws RecognitionException {
		try {
			int _type = MOZ_REGEXP;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1926:2: ( 'REGEXP(' ( ( WS )=> WS )? STRING ( WS )? ')' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1927:2: 'REGEXP(' ( ( WS )=> WS )? STRING ( WS )? ')'
			{
			match("REGEXP("); if (state.failed) return;

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1928:13: ( ( WS )=> WS )?
			int alt199=2;
			int LA199_0 = input.LA(1);
			if ( (LA199_0=='\t'||LA199_0==' ') && (synpred15_Css3())) {
				alt199=1;
			}
			switch (alt199) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1928:14: ( WS )=> WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			mSTRING(); if (state.failed) return;

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1928:32: ( WS )?
			int alt200=2;
			int LA200_0 = input.LA(1);
			if ( (LA200_0=='\t'||LA200_0==' ') ) {
				alt200=1;
			}
			switch (alt200) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1928:32: WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			match(')'); if (state.failed) return;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "MOZ_REGEXP"

	// $ANTLR start "WS"
	public final void mWS() throws RecognitionException {
		try {
			int _type = WS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1939:5: ( ( ' ' | '\\t' )+ )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1940:5: ( ' ' | '\\t' )+
			{
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1940:5: ( ' ' | '\\t' )+
			int cnt201=0;
			loop201:
			while (true) {
				int alt201=2;
				int LA201_0 = input.LA(1);
				if ( (LA201_0=='\t'||LA201_0==' ') ) {
					alt201=1;
				}

				switch (alt201) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
					{
					if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt201 >= 1 ) break loop201;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(201, input);
					throw eee;
				}
				cnt201++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WS"

	// $ANTLR start "NL"
	public final void mNL() throws RecognitionException {
		try {
			int _type = NL;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1944:5: ( ( '\\r' | '\\n' )+ )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1946:5: ( '\\r' | '\\n' )+
			{
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1946:5: ( '\\r' | '\\n' )+
			int cnt202=0;
			loop202:
			while (true) {
				int alt202=2;
				int LA202_0 = input.LA(1);
				if ( (LA202_0=='\n'||LA202_0=='\r') ) {
					alt202=1;
				}

				switch (alt202) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
					{
					if ( input.LA(1)=='\n'||input.LA(1)=='\r' ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt202 >= 1 ) break loop202;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(202, input);
					throw eee;
				}
				cnt202++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "NL"

	// $ANTLR start "COMMENT"
	public final void mCOMMENT() throws RecognitionException {
		try {
			int _type = COMMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1952:5: ( '/*' ( options {greedy=false; } : ( . )* ) '*/' )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1953:5: '/*' ( options {greedy=false; } : ( . )* ) '*/'
			{
			match("/*"); if (state.failed) return;

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1953:10: ( options {greedy=false; } : ( . )* )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1953:40: ( . )*
			{
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1953:40: ( . )*
			loop203:
			while (true) {
				int alt203=2;
				int LA203_0 = input.LA(1);
				if ( (LA203_0=='*') ) {
					int LA203_1 = input.LA(2);
					if ( (LA203_1=='/') ) {
						alt203=2;
					}
					else if ( ((LA203_1 >= '\u0000' && LA203_1 <= '.')||(LA203_1 >= '0' && LA203_1 <= '\uFFFF')) ) {
						alt203=1;
					}

				}
				else if ( ((LA203_0 >= '\u0000' && LA203_0 <= ')')||(LA203_0 >= '+' && LA203_0 <= '\uFFFF')) ) {
					alt203=1;
				}

				switch (alt203) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1953:40: .
					{
					matchAny(); if (state.failed) return;
					}
					break;

				default :
					break loop203;
				}
			}

			}

			match("*/"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "COMMENT"

	// $ANTLR start "LINE_COMMENT"
	public final void mLINE_COMMENT() throws RecognitionException {
		try {
			int _type = LINE_COMMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1957:5: ( '//' ( options {greedy=false; } : (~ ( '\\r' | '\\n' ) )* ) )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1958:5: '//' ( options {greedy=false; } : (~ ( '\\r' | '\\n' ) )* )
			{
			match("//"); if (state.failed) return;

			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1958:9: ( options {greedy=false; } : (~ ( '\\r' | '\\n' ) )* )
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1958:39: (~ ( '\\r' | '\\n' ) )*
			{
			// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1958:39: (~ ( '\\r' | '\\n' ) )*
			loop204:
			while (true) {
				int alt204=2;
				int LA204_0 = input.LA(1);
				if ( ((LA204_0 >= '\u0000' && LA204_0 <= '\t')||(LA204_0 >= '\u000B' && LA204_0 <= '\f')||(LA204_0 >= '\u000E' && LA204_0 <= '\uFFFF')) ) {
					alt204=1;
				}

				switch (alt204) {
				case 1 :
					// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
					{
					if ( (input.LA(1) >= '\u0000' && input.LA(1) <= '\t')||(input.LA(1) >= '\u000B' && input.LA(1) <= '\f')||(input.LA(1) >= '\u000E' && input.LA(1) <= '\uFFFF') ) {
						input.consume();
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					break loop204;
				}
			}

			}

			if ( state.backtracking==0 ) {
				if (isCssPreprocessorSource()) {_channel = HIDDEN;}
			    }
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LINE_COMMENT"

	@Override
	public void mTokens() throws RecognitionException {
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:8: ( GEN | CDO | CDC | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS | GREATER | LBRACE | RBRACE | LBRACKET | RBRACKET | OPEQ | SEMI | COLON | DCOLON | SOLIDUS | MINUS | PLUS | STAR | LPAREN | RPAREN | COMMA | DOT | TILDE | PIPE | PERCENTAGE_SYMBOL | EXCLAMATION_MARK | CP_EQ | CP_NOT_EQ | LESS | GREATER_OR_EQ | LESS_OR_EQ | LESS_AND | CP_DOTS | LESS_REST | STRING | LESS_JS_STRING | NOT | IDENT | HASH_SYMBOL | HASH | IMPORTANT_SYM | IMPORT_SYM | PAGE_SYM | MEDIA_SYM | NAMESPACE_SYM | CHARSET_SYM | COUNTER_STYLE_SYM | FONT_FACE_SYM | TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM | MOZ_DOCUMENT_SYM | WEBKIT_KEYFRAMES_SYM | SASS_CONTENT | SASS_MIXIN | SASS_INCLUDE | SASS_EXTEND | SASS_DEBUG | SASS_ERROR | SASS_WARN | SASS_IF | SASS_ELSE | SASS_ELSEIF | SASS_FOR | SASS_FUNCTION | SASS_RETURN | SASS_EACH | SASS_WHILE | SASS_AT_ROOT | AT_SIGN | AT_IDENT | SASS_VAR | SASS_DEFAULT | SASS_OPTIONAL | SASS_GLOBAL | SASS_EXTEND_ONLY_SELECTOR | NUMBER | URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP | WS | NL | COMMENT | LINE_COMMENT )
		int alt205=101;
		alt205 = dfa205.predict(input);
		switch (alt205) {
			case 1 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:10: GEN
				{
				mGEN(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:14: CDO
				{
				mCDO(); if (state.failed) return;

				}
				break;
			case 3 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:18: CDC
				{
				mCDC(); if (state.failed) return;

				}
				break;
			case 4 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:22: INCLUDES
				{
				mINCLUDES(); if (state.failed) return;

				}
				break;
			case 5 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:31: DASHMATCH
				{
				mDASHMATCH(); if (state.failed) return;

				}
				break;
			case 6 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:41: BEGINS
				{
				mBEGINS(); if (state.failed) return;

				}
				break;
			case 7 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:48: ENDS
				{
				mENDS(); if (state.failed) return;

				}
				break;
			case 8 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:53: CONTAINS
				{
				mCONTAINS(); if (state.failed) return;

				}
				break;
			case 9 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:62: GREATER
				{
				mGREATER(); if (state.failed) return;

				}
				break;
			case 10 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:70: LBRACE
				{
				mLBRACE(); if (state.failed) return;

				}
				break;
			case 11 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:77: RBRACE
				{
				mRBRACE(); if (state.failed) return;

				}
				break;
			case 12 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:84: LBRACKET
				{
				mLBRACKET(); if (state.failed) return;

				}
				break;
			case 13 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:93: RBRACKET
				{
				mRBRACKET(); if (state.failed) return;

				}
				break;
			case 14 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:102: OPEQ
				{
				mOPEQ(); if (state.failed) return;

				}
				break;
			case 15 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:107: SEMI
				{
				mSEMI(); if (state.failed) return;

				}
				break;
			case 16 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:112: COLON
				{
				mCOLON(); if (state.failed) return;

				}
				break;
			case 17 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:118: DCOLON
				{
				mDCOLON(); if (state.failed) return;

				}
				break;
			case 18 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:125: SOLIDUS
				{
				mSOLIDUS(); if (state.failed) return;

				}
				break;
			case 19 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:133: MINUS
				{
				mMINUS(); if (state.failed) return;

				}
				break;
			case 20 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:139: PLUS
				{
				mPLUS(); if (state.failed) return;

				}
				break;
			case 21 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:144: STAR
				{
				mSTAR(); if (state.failed) return;

				}
				break;
			case 22 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:149: LPAREN
				{
				mLPAREN(); if (state.failed) return;

				}
				break;
			case 23 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:156: RPAREN
				{
				mRPAREN(); if (state.failed) return;

				}
				break;
			case 24 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:163: COMMA
				{
				mCOMMA(); if (state.failed) return;

				}
				break;
			case 25 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:169: DOT
				{
				mDOT(); if (state.failed) return;

				}
				break;
			case 26 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:173: TILDE
				{
				mTILDE(); if (state.failed) return;

				}
				break;
			case 27 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:179: PIPE
				{
				mPIPE(); if (state.failed) return;

				}
				break;
			case 28 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:184: PERCENTAGE_SYMBOL
				{
				mPERCENTAGE_SYMBOL(); if (state.failed) return;

				}
				break;
			case 29 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:202: EXCLAMATION_MARK
				{
				mEXCLAMATION_MARK(); if (state.failed) return;

				}
				break;
			case 30 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:219: CP_EQ
				{
				mCP_EQ(); if (state.failed) return;

				}
				break;
			case 31 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:225: CP_NOT_EQ
				{
				mCP_NOT_EQ(); if (state.failed) return;

				}
				break;
			case 32 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:235: LESS
				{
				mLESS(); if (state.failed) return;

				}
				break;
			case 33 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:240: GREATER_OR_EQ
				{
				mGREATER_OR_EQ(); if (state.failed) return;

				}
				break;
			case 34 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:254: LESS_OR_EQ
				{
				mLESS_OR_EQ(); if (state.failed) return;

				}
				break;
			case 35 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:265: LESS_AND
				{
				mLESS_AND(); if (state.failed) return;

				}
				break;
			case 36 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:274: CP_DOTS
				{
				mCP_DOTS(); if (state.failed) return;

				}
				break;
			case 37 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:282: LESS_REST
				{
				mLESS_REST(); if (state.failed) return;

				}
				break;
			case 38 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:292: STRING
				{
				mSTRING(); if (state.failed) return;

				}
				break;
			case 39 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:299: LESS_JS_STRING
				{
				mLESS_JS_STRING(); if (state.failed) return;

				}
				break;
			case 40 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:314: NOT
				{
				mNOT(); if (state.failed) return;

				}
				break;
			case 41 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:318: IDENT
				{
				mIDENT(); if (state.failed) return;

				}
				break;
			case 42 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:324: HASH_SYMBOL
				{
				mHASH_SYMBOL(); if (state.failed) return;

				}
				break;
			case 43 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:336: HASH
				{
				mHASH(); if (state.failed) return;

				}
				break;
			case 44 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:341: IMPORTANT_SYM
				{
				mIMPORTANT_SYM(); if (state.failed) return;

				}
				break;
			case 45 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:355: IMPORT_SYM
				{
				mIMPORT_SYM(); if (state.failed) return;

				}
				break;
			case 46 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:366: PAGE_SYM
				{
				mPAGE_SYM(); if (state.failed) return;

				}
				break;
			case 47 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:375: MEDIA_SYM
				{
				mMEDIA_SYM(); if (state.failed) return;

				}
				break;
			case 48 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:385: NAMESPACE_SYM
				{
				mNAMESPACE_SYM(); if (state.failed) return;

				}
				break;
			case 49 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:399: CHARSET_SYM
				{
				mCHARSET_SYM(); if (state.failed) return;

				}
				break;
			case 50 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:411: COUNTER_STYLE_SYM
				{
				mCOUNTER_STYLE_SYM(); if (state.failed) return;

				}
				break;
			case 51 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:429: FONT_FACE_SYM
				{
				mFONT_FACE_SYM(); if (state.failed) return;

				}
				break;
			case 52 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:443: TOPLEFTCORNER_SYM
				{
				mTOPLEFTCORNER_SYM(); if (state.failed) return;

				}
				break;
			case 53 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:461: TOPLEFT_SYM
				{
				mTOPLEFT_SYM(); if (state.failed) return;

				}
				break;
			case 54 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:473: TOPCENTER_SYM
				{
				mTOPCENTER_SYM(); if (state.failed) return;

				}
				break;
			case 55 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:487: TOPRIGHT_SYM
				{
				mTOPRIGHT_SYM(); if (state.failed) return;

				}
				break;
			case 56 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:500: TOPRIGHTCORNER_SYM
				{
				mTOPRIGHTCORNER_SYM(); if (state.failed) return;

				}
				break;
			case 57 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:519: BOTTOMLEFTCORNER_SYM
				{
				mBOTTOMLEFTCORNER_SYM(); if (state.failed) return;

				}
				break;
			case 58 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:540: BOTTOMLEFT_SYM
				{
				mBOTTOMLEFT_SYM(); if (state.failed) return;

				}
				break;
			case 59 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:555: BOTTOMCENTER_SYM
				{
				mBOTTOMCENTER_SYM(); if (state.failed) return;

				}
				break;
			case 60 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:572: BOTTOMRIGHT_SYM
				{
				mBOTTOMRIGHT_SYM(); if (state.failed) return;

				}
				break;
			case 61 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:588: BOTTOMRIGHTCORNER_SYM
				{
				mBOTTOMRIGHTCORNER_SYM(); if (state.failed) return;

				}
				break;
			case 62 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:610: LEFTTOP_SYM
				{
				mLEFTTOP_SYM(); if (state.failed) return;

				}
				break;
			case 63 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:622: LEFTMIDDLE_SYM
				{
				mLEFTMIDDLE_SYM(); if (state.failed) return;

				}
				break;
			case 64 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:637: LEFTBOTTOM_SYM
				{
				mLEFTBOTTOM_SYM(); if (state.failed) return;

				}
				break;
			case 65 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:652: RIGHTTOP_SYM
				{
				mRIGHTTOP_SYM(); if (state.failed) return;

				}
				break;
			case 66 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:665: RIGHTMIDDLE_SYM
				{
				mRIGHTMIDDLE_SYM(); if (state.failed) return;

				}
				break;
			case 67 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:681: RIGHTBOTTOM_SYM
				{
				mRIGHTBOTTOM_SYM(); if (state.failed) return;

				}
				break;
			case 68 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:697: MOZ_DOCUMENT_SYM
				{
				mMOZ_DOCUMENT_SYM(); if (state.failed) return;

				}
				break;
			case 69 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:714: WEBKIT_KEYFRAMES_SYM
				{
				mWEBKIT_KEYFRAMES_SYM(); if (state.failed) return;

				}
				break;
			case 70 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:735: SASS_CONTENT
				{
				mSASS_CONTENT(); if (state.failed) return;

				}
				break;
			case 71 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:748: SASS_MIXIN
				{
				mSASS_MIXIN(); if (state.failed) return;

				}
				break;
			case 72 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:759: SASS_INCLUDE
				{
				mSASS_INCLUDE(); if (state.failed) return;

				}
				break;
			case 73 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:772: SASS_EXTEND
				{
				mSASS_EXTEND(); if (state.failed) return;

				}
				break;
			case 74 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:784: SASS_DEBUG
				{
				mSASS_DEBUG(); if (state.failed) return;

				}
				break;
			case 75 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:795: SASS_ERROR
				{
				mSASS_ERROR(); if (state.failed) return;

				}
				break;
			case 76 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:806: SASS_WARN
				{
				mSASS_WARN(); if (state.failed) return;

				}
				break;
			case 77 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:816: SASS_IF
				{
				mSASS_IF(); if (state.failed) return;

				}
				break;
			case 78 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:824: SASS_ELSE
				{
				mSASS_ELSE(); if (state.failed) return;

				}
				break;
			case 79 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:834: SASS_ELSEIF
				{
				mSASS_ELSEIF(); if (state.failed) return;

				}
				break;
			case 80 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:846: SASS_FOR
				{
				mSASS_FOR(); if (state.failed) return;

				}
				break;
			case 81 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:855: SASS_FUNCTION
				{
				mSASS_FUNCTION(); if (state.failed) return;

				}
				break;
			case 82 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:869: SASS_RETURN
				{
				mSASS_RETURN(); if (state.failed) return;

				}
				break;
			case 83 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:881: SASS_EACH
				{
				mSASS_EACH(); if (state.failed) return;

				}
				break;
			case 84 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:891: SASS_WHILE
				{
				mSASS_WHILE(); if (state.failed) return;

				}
				break;
			case 85 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:902: SASS_AT_ROOT
				{
				mSASS_AT_ROOT(); if (state.failed) return;

				}
				break;
			case 86 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:915: AT_SIGN
				{
				mAT_SIGN(); if (state.failed) return;

				}
				break;
			case 87 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:923: AT_IDENT
				{
				mAT_IDENT(); if (state.failed) return;

				}
				break;
			case 88 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:932: SASS_VAR
				{
				mSASS_VAR(); if (state.failed) return;

				}
				break;
			case 89 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:941: SASS_DEFAULT
				{
				mSASS_DEFAULT(); if (state.failed) return;

				}
				break;
			case 90 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:954: SASS_OPTIONAL
				{
				mSASS_OPTIONAL(); if (state.failed) return;

				}
				break;
			case 91 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:968: SASS_GLOBAL
				{
				mSASS_GLOBAL(); if (state.failed) return;

				}
				break;
			case 92 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:980: SASS_EXTEND_ONLY_SELECTOR
				{
				mSASS_EXTEND_ONLY_SELECTOR(); if (state.failed) return;

				}
				break;
			case 93 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1006: NUMBER
				{
				mNUMBER(); if (state.failed) return;

				}
				break;
			case 94 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1013: URI
				{
				mURI(); if (state.failed) return;

				}
				break;
			case 95 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1017: MOZ_URL_PREFIX
				{
				mMOZ_URL_PREFIX(); if (state.failed) return;

				}
				break;
			case 96 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1032: MOZ_DOMAIN
				{
				mMOZ_DOMAIN(); if (state.failed) return;

				}
				break;
			case 97 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1043: MOZ_REGEXP
				{
				mMOZ_REGEXP(); if (state.failed) return;

				}
				break;
			case 98 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1054: WS
				{
				mWS(); if (state.failed) return;

				}
				break;
			case 99 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1057: NL
				{
				mNL(); if (state.failed) return;

				}
				break;
			case 100 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1060: COMMENT
				{
				mCOMMENT(); if (state.failed) return;

				}
				break;
			case 101 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1068: LINE_COMMENT
				{
				mLINE_COMMENT(); if (state.failed) return;

				}
				break;

		}
	}

	// $ANTLR start synpred1_Css3
	public final void synpred1_Css3_fragment() throws RecognitionException {
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1728:26: ( '\\\\' '\\\"' )
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1728:28: '\\\\' '\\\"'
		{
		match('\\'); if (state.failed) return;
		match('\"'); if (state.failed) return;
		}

	}
	// $ANTLR end synpred1_Css3

	// $ANTLR start synpred2_Css3
	public final void synpred2_Css3_fragment() throws RecognitionException {
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1842:15: ( D P ( I | C ) )
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1842:16: D P ( I | C )
		{
		mD(); if (state.failed) return;

		mP(); if (state.failed) return;

		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1842:20: ( I | C )
		int alt206=2;
		switch ( input.LA(1) ) {
		case 'I':
		case 'i':
			{
			alt206=1;
			}
			break;
		case '\\':
			{
			switch ( input.LA(2) ) {
			case 'I':
			case 'i':
				{
				alt206=1;
				}
				break;
			case '0':
				{
				int LA206_4 = input.LA(3);
				if ( (LA206_4=='0') ) {
					int LA206_6 = input.LA(4);
					if ( (LA206_6=='0') ) {
						int LA206_7 = input.LA(5);
						if ( (LA206_7=='0') ) {
							int LA206_8 = input.LA(6);
							if ( (LA206_8=='4'||LA206_8=='6') ) {
								int LA206_5 = input.LA(7);
								if ( (LA206_5=='9') ) {
									alt206=1;
								}
								else if ( (LA206_5=='3') ) {
									alt206=2;
								}

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 7 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 206, 5, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 206, 8, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}
						else if ( (LA206_7=='4'||LA206_7=='6') ) {
							int LA206_5 = input.LA(6);
							if ( (LA206_5=='9') ) {
								alt206=1;
							}
							else if ( (LA206_5=='3') ) {
								alt206=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 206, 5, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}

						else {
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 206, 7, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}
					else if ( (LA206_6=='4'||LA206_6=='6') ) {
						int LA206_5 = input.LA(5);
						if ( (LA206_5=='9') ) {
							alt206=1;
						}
						else if ( (LA206_5=='3') ) {
							alt206=2;
						}

						else {
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 206, 5, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 206, 6, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA206_4=='4'||LA206_4=='6') ) {
					int LA206_5 = input.LA(4);
					if ( (LA206_5=='9') ) {
						alt206=1;
					}
					else if ( (LA206_5=='3') ) {
						alt206=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 206, 5, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
							input.consume();
						}
						NoViableAltException nvae =
							new NoViableAltException("", 206, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case '4':
			case '6':
				{
				int LA206_5 = input.LA(3);
				if ( (LA206_5=='9') ) {
					alt206=1;
				}
				else if ( (LA206_5=='3') ) {
					alt206=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
							input.consume();
						}
						NoViableAltException nvae =
							new NoViableAltException("", 206, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				int nvaeMark = input.mark();
				try {
					input.consume();
					NoViableAltException nvae =
						new NoViableAltException("", 206, 2, input);
					throw nvae;
				} finally {
					input.rewind(nvaeMark);
				}
			}
			}
			break;
		case 'C':
		case 'c':
			{
			alt206=2;
			}
			break;
		default:
			if (state.backtracking>0) {state.failed=true; return;}
			NoViableAltException nvae =
				new NoViableAltException("", 206, 0, input);
			throw nvae;
		}
		switch (alt206) {
			case 1 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1842:21: I
				{
				mI(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1842:23: C
				{
				mC(); if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred2_Css3

	// $ANTLR start synpred3_Css3
	public final void synpred3_Css3_fragment() throws RecognitionException {
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1849:15: ( E ( M | X ) )
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1849:16: E ( M | X )
		{
		mE(); if (state.failed) return;

		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1849:18: ( M | X )
		int alt207=2;
		switch ( input.LA(1) ) {
		case 'M':
		case 'm':
			{
			alt207=1;
			}
			break;
		case '\\':
			{
			switch ( input.LA(2) ) {
			case '4':
			case '6':
			case 'M':
			case 'm':
				{
				alt207=1;
				}
				break;
			case '0':
				{
				switch ( input.LA(3) ) {
				case '0':
					{
					switch ( input.LA(4) ) {
					case '0':
						{
						switch ( input.LA(5) ) {
						case '0':
							{
							int LA207_7 = input.LA(6);
							if ( (LA207_7=='4'||LA207_7=='6') ) {
								alt207=1;
							}
							else if ( (LA207_7=='5'||LA207_7=='7') ) {
								alt207=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 207, 7, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						case '4':
						case '6':
							{
							alt207=1;
							}
							break;
						case '5':
						case '7':
							{
							alt207=2;
							}
							break;
						default:
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 207, 6, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}
						}
						break;
					case '4':
					case '6':
						{
						alt207=1;
						}
						break;
					case '5':
					case '7':
						{
						alt207=2;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 207, 5, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}
					}
					break;
				case '4':
				case '6':
					{
					alt207=1;
					}
					break;
				case '5':
				case '7':
					{
					alt207=2;
					}
					break;
				default:
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
							input.consume();
						}
						NoViableAltException nvae =
							new NoViableAltException("", 207, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
				}
				break;
			case '5':
			case '7':
			case 'X':
			case 'x':
				{
				alt207=2;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				int nvaeMark = input.mark();
				try {
					input.consume();
					NoViableAltException nvae =
						new NoViableAltException("", 207, 2, input);
					throw nvae;
				} finally {
					input.rewind(nvaeMark);
				}
			}
			}
			break;
		case 'X':
		case 'x':
			{
			alt207=2;
			}
			break;
		default:
			if (state.backtracking>0) {state.failed=true; return;}
			NoViableAltException nvae =
				new NoViableAltException("", 207, 0, input);
			throw nvae;
		}
		switch (alt207) {
			case 1 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1849:19: M
				{
				mM(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1849:21: X
				{
				mX(); if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred3_Css3

	// $ANTLR start synpred4_Css3
	public final void synpred4_Css3_fragment() throws RecognitionException {
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:15: ( P ( X | T | C ) )
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:16: P ( X | T | C )
		{
		mP(); if (state.failed) return;

		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:17: ( X | T | C )
		int alt208=3;
		switch ( input.LA(1) ) {
		case 'X':
		case 'x':
			{
			alt208=1;
			}
			break;
		case '\\':
			{
			switch ( input.LA(2) ) {
			case 'X':
			case 'x':
				{
				alt208=1;
				}
				break;
			case '0':
				{
				switch ( input.LA(3) ) {
				case '0':
					{
					switch ( input.LA(4) ) {
					case '0':
						{
						switch ( input.LA(5) ) {
						case '0':
							{
							int LA208_9 = input.LA(6);
							if ( (LA208_9=='5'||LA208_9=='7') ) {
								int LA208_6 = input.LA(7);
								if ( (LA208_6=='8') ) {
									alt208=1;
								}
								else if ( (LA208_6=='4') ) {
									alt208=2;
								}

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 7 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 208, 6, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}
							else if ( (LA208_9=='4'||LA208_9=='6') ) {
								alt208=3;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 208, 9, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						case '5':
						case '7':
							{
							int LA208_6 = input.LA(6);
							if ( (LA208_6=='8') ) {
								alt208=1;
							}
							else if ( (LA208_6=='4') ) {
								alt208=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 208, 6, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						case '4':
						case '6':
							{
							alt208=3;
							}
							break;
						default:
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 208, 8, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}
						}
						break;
					case '5':
					case '7':
						{
						int LA208_6 = input.LA(5);
						if ( (LA208_6=='8') ) {
							alt208=1;
						}
						else if ( (LA208_6=='4') ) {
							alt208=2;
						}

						else {
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 208, 6, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

						}
						break;
					case '4':
					case '6':
						{
						alt208=3;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 208, 7, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}
					}
					break;
				case '5':
				case '7':
					{
					int LA208_6 = input.LA(4);
					if ( (LA208_6=='8') ) {
						alt208=1;
					}
					else if ( (LA208_6=='4') ) {
						alt208=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 208, 6, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

					}
					break;
				case '4':
				case '6':
					{
					alt208=3;
					}
					break;
				default:
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
							input.consume();
						}
						NoViableAltException nvae =
							new NoViableAltException("", 208, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
				}
				break;
			case '5':
			case '7':
				{
				int LA208_6 = input.LA(3);
				if ( (LA208_6=='8') ) {
					alt208=1;
				}
				else if ( (LA208_6=='4') ) {
					alt208=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
							input.consume();
						}
						NoViableAltException nvae =
							new NoViableAltException("", 208, 6, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case 'T':
			case 't':
				{
				alt208=2;
				}
				break;
			case '4':
			case '6':
				{
				alt208=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				int nvaeMark = input.mark();
				try {
					input.consume();
					NoViableAltException nvae =
						new NoViableAltException("", 208, 2, input);
					throw nvae;
				} finally {
					input.rewind(nvaeMark);
				}
			}
			}
			break;
		case 'T':
		case 't':
			{
			alt208=2;
			}
			break;
		case 'C':
		case 'c':
			{
			alt208=3;
			}
			break;
		default:
			if (state.backtracking>0) {state.failed=true; return;}
			NoViableAltException nvae =
				new NoViableAltException("", 208, 0, input);
			throw nvae;
		}
		switch (alt208) {
			case 1 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:18: X
				{
				mX(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:20: T
				{
				mT(); if (state.failed) return;

				}
				break;
			case 3 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:22: C
				{
				mC(); if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred4_Css3

	// $ANTLR start synpred5_Css3
	public final void synpred5_Css3_fragment() throws RecognitionException {
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1863:15: ( C M )
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1863:16: C M
		{
		mC(); if (state.failed) return;

		mM(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred5_Css3

	// $ANTLR start synpred6_Css3
	public final void synpred6_Css3_fragment() throws RecognitionException {
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1865:15: ( M ( M | S ) )
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1865:16: M ( M | S )
		{
		mM(); if (state.failed) return;

		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1865:18: ( M | S )
		int alt209=2;
		switch ( input.LA(1) ) {
		case 'M':
		case 'm':
			{
			alt209=1;
			}
			break;
		case '\\':
			{
			switch ( input.LA(2) ) {
			case '4':
			case '6':
			case 'M':
			case 'm':
				{
				alt209=1;
				}
				break;
			case '0':
				{
				switch ( input.LA(3) ) {
				case '0':
					{
					switch ( input.LA(4) ) {
					case '0':
						{
						switch ( input.LA(5) ) {
						case '0':
							{
							int LA209_7 = input.LA(6);
							if ( (LA209_7=='4'||LA209_7=='6') ) {
								alt209=1;
							}
							else if ( (LA209_7=='5'||LA209_7=='7') ) {
								alt209=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 209, 7, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

							}
							break;
						case '4':
						case '6':
							{
							alt209=1;
							}
							break;
						case '5':
						case '7':
							{
							alt209=2;
							}
							break;
						default:
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 209, 6, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}
						}
						break;
					case '4':
					case '6':
						{
						alt209=1;
						}
						break;
					case '5':
					case '7':
						{
						alt209=2;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 209, 5, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}
					}
					break;
				case '4':
				case '6':
					{
					alt209=1;
					}
					break;
				case '5':
				case '7':
					{
					alt209=2;
					}
					break;
				default:
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
							input.consume();
						}
						NoViableAltException nvae =
							new NoViableAltException("", 209, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
				}
				break;
			case '5':
			case '7':
			case 'S':
			case 's':
				{
				alt209=2;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				int nvaeMark = input.mark();
				try {
					input.consume();
					NoViableAltException nvae =
						new NoViableAltException("", 209, 2, input);
					throw nvae;
				} finally {
					input.rewind(nvaeMark);
				}
			}
			}
			break;
		case 'S':
		case 's':
			{
			alt209=2;
			}
			break;
		default:
			if (state.backtracking>0) {state.failed=true; return;}
			NoViableAltException nvae =
				new NoViableAltException("", 209, 0, input);
			throw nvae;
		}
		switch (alt209) {
			case 1 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1865:19: M
				{
				mM(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1865:21: S
				{
				mS(); if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred6_Css3

	// $ANTLR start synpred7_Css3
	public final void synpred7_Css3_fragment() throws RecognitionException {
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1872:15: ( I N )
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1872:16: I N
		{
		mI(); if (state.failed) return;

		mN(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred7_Css3

	// $ANTLR start synpred8_Css3
	public final void synpred8_Css3_fragment() throws RecognitionException {
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1875:15: ( D E G )
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1875:16: D E G
		{
		mD(); if (state.failed) return;

		mE(); if (state.failed) return;

		mG(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred8_Css3

	// $ANTLR start synpred9_Css3
	public final void synpred9_Css3_fragment() throws RecognitionException {
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1880:15: ( R ( A | E ) )
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1880:16: R ( A | E )
		{
		mR(); if (state.failed) return;

		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1880:18: ( A | E )
		int alt210=2;
		switch ( input.LA(1) ) {
		case 'A':
		case 'a':
			{
			alt210=1;
			}
			break;
		case '\\':
			{
			int LA210_2 = input.LA(2);
			if ( (LA210_2=='0') ) {
				int LA210_4 = input.LA(3);
				if ( (LA210_4=='0') ) {
					int LA210_6 = input.LA(4);
					if ( (LA210_6=='0') ) {
						int LA210_7 = input.LA(5);
						if ( (LA210_7=='0') ) {
							int LA210_8 = input.LA(6);
							if ( (LA210_8=='4'||LA210_8=='6') ) {
								int LA210_5 = input.LA(7);
								if ( (LA210_5=='1') ) {
									alt210=1;
								}
								else if ( (LA210_5=='5') ) {
									alt210=2;
								}

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 7 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 210, 5, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 210, 8, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}
						else if ( (LA210_7=='4'||LA210_7=='6') ) {
							int LA210_5 = input.LA(6);
							if ( (LA210_5=='1') ) {
								alt210=1;
							}
							else if ( (LA210_5=='5') ) {
								alt210=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 210, 5, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}

						else {
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 210, 7, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}
					else if ( (LA210_6=='4'||LA210_6=='6') ) {
						int LA210_5 = input.LA(5);
						if ( (LA210_5=='1') ) {
							alt210=1;
						}
						else if ( (LA210_5=='5') ) {
							alt210=2;
						}

						else {
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 210, 5, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 210, 6, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA210_4=='4'||LA210_4=='6') ) {
					int LA210_5 = input.LA(4);
					if ( (LA210_5=='1') ) {
						alt210=1;
					}
					else if ( (LA210_5=='5') ) {
						alt210=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 210, 5, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
							input.consume();
						}
						NoViableAltException nvae =
							new NoViableAltException("", 210, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA210_2=='4'||LA210_2=='6') ) {
				int LA210_5 = input.LA(3);
				if ( (LA210_5=='1') ) {
					alt210=1;
				}
				else if ( (LA210_5=='5') ) {
					alt210=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
							input.consume();
						}
						NoViableAltException nvae =
							new NoViableAltException("", 210, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				int nvaeMark = input.mark();
				try {
					input.consume();
					NoViableAltException nvae =
						new NoViableAltException("", 210, 2, input);
					throw nvae;
				} finally {
					input.rewind(nvaeMark);
				}
			}

			}
			break;
		case 'E':
		case 'e':
			{
			alt210=2;
			}
			break;
		default:
			if (state.backtracking>0) {state.failed=true; return;}
			NoViableAltException nvae =
				new NoViableAltException("", 210, 0, input);
			throw nvae;
		}
		switch (alt210) {
			case 1 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1880:19: A
				{
				mA(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1880:21: E
				{
				mE(); if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred9_Css3

	// $ANTLR start synpred10_Css3
	public final void synpred10_Css3_fragment() throws RecognitionException {
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1887:15: ( S )
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1887:16: S
		{
		mS(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred10_Css3

	// $ANTLR start synpred11_Css3
	public final void synpred11_Css3_fragment() throws RecognitionException {
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1889:15: ( ( K )? H Z )
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1889:16: ( K )? H Z
		{
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1889:16: ( K )?
		int alt211=2;
		int LA211_0 = input.LA(1);
		if ( (LA211_0=='K'||LA211_0=='k') ) {
			alt211=1;
		}
		else if ( (LA211_0=='\\') ) {
			switch ( input.LA(2) ) {
				case 'K':
				case 'k':
					{
					alt211=1;
					}
					break;
				case '0':
					{
					int LA211_4 = input.LA(3);
					if ( (LA211_4=='0') ) {
						int LA211_6 = input.LA(4);
						if ( (LA211_6=='0') ) {
							int LA211_7 = input.LA(5);
							if ( (LA211_7=='0') ) {
								int LA211_8 = input.LA(6);
								if ( (LA211_8=='4'||LA211_8=='6') ) {
									int LA211_5 = input.LA(7);
									if ( (LA211_5=='B'||LA211_5=='b') ) {
										alt211=1;
									}
								}
							}
							else if ( (LA211_7=='4'||LA211_7=='6') ) {
								int LA211_5 = input.LA(6);
								if ( (LA211_5=='B'||LA211_5=='b') ) {
									alt211=1;
								}
							}
						}
						else if ( (LA211_6=='4'||LA211_6=='6') ) {
							int LA211_5 = input.LA(5);
							if ( (LA211_5=='B'||LA211_5=='b') ) {
								alt211=1;
							}
						}
					}
					else if ( (LA211_4=='4'||LA211_4=='6') ) {
						int LA211_5 = input.LA(4);
						if ( (LA211_5=='B'||LA211_5=='b') ) {
							alt211=1;
						}
					}
					}
					break;
				case '4':
				case '6':
					{
					int LA211_5 = input.LA(3);
					if ( (LA211_5=='B'||LA211_5=='b') ) {
						alt211=1;
					}
					}
					break;
			}
		}
		switch (alt211) {
			case 1 :
				// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1889:16: K
				{
				mK(); if (state.failed) return;

				}
				break;

		}

		mH(); if (state.failed) return;

		mZ(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred11_Css3

	// $ANTLR start synpred12_Css3
	public final void synpred12_Css3_fragment() throws RecognitionException {
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1905:14: ( WS )
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1905:15: WS
		{
		mWS(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred12_Css3

	// $ANTLR start synpred13_Css3
	public final void synpred13_Css3_fragment() throws RecognitionException {
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1912:14: ( WS )
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1912:15: WS
		{
		mWS(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred13_Css3

	// $ANTLR start synpred14_Css3
	public final void synpred14_Css3_fragment() throws RecognitionException {
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1920:14: ( WS )
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1920:15: WS
		{
		mWS(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred14_Css3

	// $ANTLR start synpred15_Css3
	public final void synpred15_Css3_fragment() throws RecognitionException {
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1928:14: ( WS )
		// /home/mito/nb/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1928:15: WS
		{
		mWS(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred15_Css3

	public final boolean synpred11_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred11_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred8_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred8_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred2_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred2_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred5_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred5_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred14_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred14_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred9_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred9_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred4_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred4_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred7_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred7_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred13_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred13_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred1_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred1_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred10_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred10_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred6_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred6_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred15_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred15_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred12_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred12_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred3_Css3() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred3_Css3_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}


	protected DFA189 dfa189 = new DFA189(this);
	protected DFA205 dfa205 = new DFA205(this);
	static final String DFA189_eotS =
		"\1\30\1\14\1\uffff\6\14\1\uffff\2\14\1\uffff\7\14\1\uffff\2\14\2\uffff"+
		"\1\14\1\uffff\16\14\2\uffff\4\14\27\uffff\1\14\1\uffff\3\14\1\uffff\1"+
		"\14\1\uffff\1\14\31\uffff\1\14\1\uffff\6\14\15\uffff\14\14\12\uffff\2"+
		"\14\21\uffff\2\14\1\uffff\1\14\4\uffff\2\14\1\uffff\1\14\3\uffff\2\14"+
		"\4\uffff\2\14\1\uffff\1\14\3\uffff\2\14\3\uffff\2\14\11\uffff\4\14\5\uffff"+
		"\2\14\3\uffff\2\14\11\uffff\5\14\3\uffff\20\14\1\uffff\2\14\30\uffff\7"+
		"\14\3\uffff\3\14\3\uffff\2\14\2\uffff\3\14\3\uffff\2\14\3\uffff\6\14\2"+
		"\uffff\2\14\4\uffff\5\14\2\uffff\2\14\1\uffff\1\14\2\uffff\7\14\1\uffff"+
		"\1\14\1\uffff\1\14\2\uffff\2\14\1\uffff\16\14\1\uffff\2\14\30\uffff\4"+
		"\14\14\uffff\3\14\3\uffff\3\14\3\uffff\2\14\2\uffff\3\14\3\uffff\2\14"+
		"\2\uffff\2\14\1\uffff\4\14\4\uffff\2\14\2\uffff\3\14\2\uffff\2\14\2\uffff"+
		"\2\14\1\uffff\1\14\2\uffff\2\14\2\uffff\5\14\1\uffff\1\14\1\uffff\1\14"+
		"\6\uffff\2\14\1\uffff\15\14\1\uffff\2\14\30\uffff\4\14\14\uffff\3\14\3"+
		"\uffff\3\14\3\uffff\2\14\2\uffff\3\14\3\uffff\2\14\2\uffff\2\14\1\uffff"+
		"\4\14\4\uffff\2\14\2\uffff\3\14\2\uffff\2\14\2\uffff\2\14\1\uffff\1\14"+
		"\2\uffff\2\14\2\uffff\4\14\1\uffff\1\14\1\uffff\1\14\6\uffff\2\14\1\uffff"+
		"\13\14\1\uffff\2\14\30\uffff\4\14\14\uffff\2\14\3\uffff\2\14\3\uffff\1"+
		"\14\2\uffff\2\14\3\uffff\1\14\2\uffff\2\14\1\uffff\3\14\4\uffff\2\14\2"+
		"\uffff\2\14\2\uffff\1\14\2\uffff\1\14\1\uffff\1\14\2\uffff\2\14\2\uffff"+
		"\2\14\1\uffff\1\14\1\uffff\1\14\6\uffff\1\14\62\uffff\1\14\1\uffff\2\14"+
		"\4\uffff\1\14\2\uffff\1\14\5\uffff\1\14\2\uffff\1\14\30\uffff";
	static final String DFA189_eofS =
		"\u0349\uffff";
	static final String DFA189_minS =
		"\1\45\1\105\1\0\1\115\1\103\2\115\1\116\1\101\1\0\1\110\1\132\1\uffff"+
		"\1\105\1\115\1\103\2\115\1\116\1\101\1\0\1\110\1\132\2\uffff\1\103\1\0"+
		"\1\107\1\103\1\107\1\103\1\60\1\63\1\103\1\115\1\60\1\115\2\116\2\101"+
		"\2\0\2\110\2\132\27\0\1\104\1\0\1\115\1\104\1\115\1\uffff\1\132\1\0\1"+
		"\132\31\0\1\115\1\0\1\115\2\103\2\60\1\65\15\0\1\60\1\63\1\60\1\105\3"+
		"\115\1\116\1\110\1\132\1\115\1\110\12\0\1\103\1\101\15\0\1\uffff\3\0\1"+
		"\60\1\104\1\0\1\70\1\uffff\3\0\1\60\1\64\1\0\1\63\1\uffff\2\0\1\60\1\104"+
		"\1\uffff\3\0\1\60\1\104\1\0\1\63\1\uffff\2\0\1\60\1\105\3\0\1\60\1\61"+
		"\11\0\2\132\1\60\1\70\2\0\1\uffff\2\0\1\60\1\101\1\uffff\2\0\1\60\1\63"+
		"\11\0\2\60\1\65\1\103\1\107\1\uffff\2\0\1\60\1\67\1\60\1\63\1\60\1\105"+
		"\3\115\1\116\1\110\1\132\1\115\1\110\1\103\1\101\1\0\2\107\30\0\1\104"+
		"\1\115\1\104\1\115\1\60\1\104\1\70\3\0\1\60\1\64\1\63\3\0\1\60\1\104\2"+
		"\0\1\60\1\104\1\63\3\0\1\60\1\105\2\0\1\uffff\1\60\1\64\1\60\1\61\1\104"+
		"\1\115\2\0\1\60\1\104\4\0\1\60\1\70\1\132\1\60\1\101\2\0\1\60\1\63\1\0"+
		"\1\115\2\0\1\60\1\104\2\60\1\65\1\103\1\107\1\0\1\115\1\0\1\115\2\0\1"+
		"\60\1\67\1\0\1\64\1\63\1\60\1\105\3\115\1\116\1\110\1\132\1\115\1\110"+
		"\1\103\1\101\1\0\2\107\30\0\1\104\1\115\1\104\1\115\14\0\1\60\1\104\1"+
		"\70\3\0\1\60\1\64\1\63\3\0\1\60\1\104\2\0\1\60\1\104\1\63\3\0\1\60\1\105"+
		"\2\0\1\60\1\64\1\0\1\60\1\61\1\104\1\115\4\0\1\60\1\104\2\0\1\60\1\70"+
		"\1\132\2\0\1\60\1\101\2\0\1\60\1\63\1\0\1\115\2\0\1\60\1\104\2\0\1\64"+
		"\1\60\1\65\1\103\1\107\1\0\1\115\1\0\1\115\6\0\1\60\1\67\1\0\1\63\1\60"+
		"\1\105\3\115\1\116\1\110\1\132\1\115\1\110\1\103\1\101\1\0\2\107\30\0"+
		"\1\104\1\115\1\104\1\115\14\0\1\64\1\104\1\70\3\0\2\64\1\63\3\0\1\64\1"+
		"\104\2\0\1\64\1\104\1\63\3\0\1\64\1\105\2\0\1\60\1\64\1\0\1\64\1\61\1"+
		"\104\1\115\4\0\1\60\1\104\2\0\1\64\1\70\1\132\2\0\1\65\1\101\2\0\1\64"+
		"\1\63\1\0\1\115\2\0\1\60\1\104\2\0\1\60\1\65\1\103\1\107\1\0\1\115\1\0"+
		"\1\115\6\0\1\64\1\67\1\0\1\105\3\115\1\116\1\110\1\132\1\115\1\110\1\103"+
		"\1\101\1\0\2\107\30\0\1\104\1\115\1\104\1\115\14\0\1\104\1\70\3\0\1\64"+
		"\1\63\3\0\1\104\2\0\1\104\1\63\3\0\1\105\2\0\2\64\1\0\1\61\1\104\1\115"+
		"\4\0\1\64\1\104\2\0\1\70\1\132\2\0\1\101\2\0\1\63\1\0\1\115\2\0\1\64\1"+
		"\104\2\0\1\103\1\107\1\0\1\115\1\0\1\115\6\0\1\67\62\0\1\64\1\0\1\104"+
		"\1\115\4\0\1\104\2\0\1\132\5\0\1\115\2\0\1\104\30\0";
	static final String DFA189_maxS =
		"\1\uffff\1\160\1\uffff\2\170\1\155\1\163\1\156\1\145\1\0\1\150\1\172\1"+
		"\uffff\1\160\2\170\1\155\1\163\1\156\1\145\1\0\1\150\1\172\2\uffff\1\151"+
		"\1\uffff\1\147\1\151\1\147\1\170\1\67\1\144\1\170\1\163\1\63\1\163\2\156"+
		"\2\145\2\0\2\150\2\172\1\0\1\uffff\4\0\1\uffff\6\0\1\uffff\2\0\1\uffff"+
		"\4\0\1\uffff\1\0\1\144\1\uffff\1\155\1\144\1\155\1\uffff\1\172\1\uffff"+
		"\1\172\1\0\1\uffff\26\0\1\uffff\1\155\1\0\1\155\2\151\1\67\1\60\1\65\1"+
		"\0\1\uffff\13\0\1\67\1\144\1\63\1\160\1\170\1\155\1\163\1\156\1\150\1"+
		"\172\1\163\1\150\12\0\1\170\1\145\15\0\1\uffff\3\0\1\67\1\144\1\0\1\70"+
		"\1\uffff\3\0\1\67\1\70\1\0\1\63\1\uffff\2\0\1\66\1\144\1\uffff\3\0\1\67"+
		"\1\144\1\0\1\63\1\uffff\2\0\1\66\1\145\1\0\1\uffff\1\0\1\66\1\65\1\0\1"+
		"\uffff\7\0\2\172\1\66\1\70\2\0\1\uffff\2\0\1\67\1\141\1\uffff\2\0\1\66"+
		"\1\71\1\0\1\uffff\7\0\1\67\1\60\1\65\1\151\1\147\1\uffff\2\0\1\66\2\67"+
		"\1\144\1\63\1\160\1\170\1\155\1\163\1\156\1\150\1\172\1\163\1\150\1\170"+
		"\1\145\1\0\2\147\30\0\1\144\1\155\1\144\1\155\1\67\1\144\1\70\3\0\1\67"+
		"\1\70\1\63\3\0\1\66\1\144\2\0\1\67\1\144\1\63\3\0\1\66\1\145\2\0\1\uffff"+
		"\1\66\1\64\1\66\1\65\1\144\1\155\2\0\1\66\1\144\4\0\1\66\1\70\1\172\1"+
		"\67\1\141\2\0\1\66\1\71\1\0\1\155\2\0\1\66\1\144\1\67\1\60\1\65\1\151"+
		"\1\147\1\0\1\155\1\0\1\155\2\0\1\66\1\67\1\0\1\67\1\144\1\63\1\160\1\170"+
		"\1\155\1\163\1\156\1\150\1\172\1\163\1\150\1\170\1\145\1\0\2\147\30\0"+
		"\1\144\1\155\1\144\1\155\14\0\1\67\1\144\1\70\3\0\1\67\1\70\1\63\3\0\1"+
		"\66\1\144\2\0\1\67\1\144\1\63\3\0\1\66\1\145\2\0\1\66\1\64\1\0\1\66\1"+
		"\65\1\144\1\155\4\0\1\66\1\144\2\0\1\66\1\70\1\172\2\0\1\67\1\141\2\0"+
		"\1\66\1\71\1\0\1\155\2\0\1\66\1\144\2\0\1\67\1\60\1\65\1\151\1\147\1\0"+
		"\1\155\1\0\1\155\6\0\1\66\1\67\1\0\1\144\1\63\1\160\1\170\1\155\1\163"+
		"\1\156\1\150\1\172\1\163\1\150\1\170\1\145\1\0\2\147\30\0\1\144\1\155"+
		"\1\144\1\155\14\0\1\67\1\144\1\70\3\0\1\67\1\70\1\63\3\0\1\66\1\144\2"+
		"\0\1\67\1\144\1\63\3\0\1\66\1\145\2\0\1\66\1\64\1\0\1\66\1\65\1\144\1"+
		"\155\4\0\1\66\1\144\2\0\1\66\1\70\1\172\2\0\1\67\1\141\2\0\1\66\1\71\1"+
		"\0\1\155\2\0\1\66\1\144\2\0\1\60\1\65\1\151\1\147\1\0\1\155\1\0\1\155"+
		"\6\0\1\66\1\67\1\0\1\160\1\170\1\155\1\163\1\156\1\150\1\172\1\163\1\150"+
		"\1\170\1\145\1\0\2\147\30\0\1\144\1\155\1\144\1\155\14\0\1\144\1\70\3"+
		"\0\1\70\1\63\3\0\1\144\2\0\1\144\1\63\3\0\1\145\2\0\1\66\1\64\1\0\1\65"+
		"\1\144\1\155\4\0\1\66\1\144\2\0\1\70\1\172\2\0\1\141\2\0\1\71\1\0\1\155"+
		"\2\0\1\66\1\144\2\0\1\151\1\147\1\0\1\155\1\0\1\155\6\0\1\67\62\0\1\64"+
		"\1\0\1\144\1\155\4\0\1\144\2\0\1\172\5\0\1\155\2\0\1\144\30\0";
	static final String DFA189_acceptS =
		"\14\uffff\1\13\12\uffff\1\14\1\15\62\uffff\1\11\126\uffff\1\2\7\uffff"+
		"\1\3\7\uffff\1\4\4\uffff\1\5\7\uffff\1\6\30\uffff\1\12\4\uffff\1\1\22"+
		"\uffff\1\7\113\uffff\1\10\u020c\uffff";
	static final String DFA189_specialS =
		"\2\uffff\1\u00ad\6\uffff\1\u0129\12\uffff\1\u012a\5\uffff\1\u00b2\16\uffff"+
		"\1\u011e\1\u011f\4\uffff\1\u0188\1\u01f6\1\111\1\u018f\1\120\1\134\1\u0163"+
		"\1\15\1\u00b6\1\145\1\26\1\u00bb\1\u0139\1\u0097\1\u013f\1\u01a6\1\4\1"+
		"\41\1\u01b3\1\61\1\u014d\1\u00e7\1\u0157\1\uffff\1\u00ef\5\uffff\1\u00a6"+
		"\1\uffff\1\160\1\u01a3\1\u0082\1\u0189\1\112\1\u0190\1\121\1\135\1\17"+
		"\1\u00b7\1\146\1\25\1\u00bc\1\u013a\1\u0140\1\u01a9\1\40\1\u01b2\1\60"+
		"\1\u014e\1\u0158\1\161\1\u0081\1\u010c\1\u01ca\1\uffff\1\u0116\6\uffff"+
		"\1\u00c5\1\102\1\u00d5\1\u010d\1\u0117\1\u00c6\1\u00d4\1\136\1\147\1\14"+
		"\1\27\1\u00b8\1\u00bd\14\uffff\1\137\1\150\1\20\1\30\1\u00b9\1\u00be\1"+
		"\u01a5\1\u01b5\1\44\1\64\2\uffff\1\u00b4\1\u01a4\1\u01b6\1\45\1\65\1\u014f"+
		"\1\u0159\1\u0150\1\u015a\1\157\1\u0085\1\162\1\u0086\1\uffff\1\u0132\1"+
		"\u0133\1\u0098\2\uffff\1\u0099\2\uffff\1\u009f\1\u00a0\1\36\2\uffff\1"+
		"\46\2\uffff\1\u00ed\1\u00ee\3\uffff\1\u0147\1\u0149\1\7\2\uffff\1\10\2"+
		"\uffff\1\u0121\1\u0122\2\uffff\1\u00fe\1\105\1\u0103\2\uffff\1\u01d0\1"+
		"\u009a\1\u01de\1\u00ff\1\u0104\1\u01d1\1\u01df\1\163\1\u0087\4\uffff\1"+
		"\167\1\u0088\1\uffff\1\u00ae\1\u00af\3\uffff\1\u00ab\1\u00ac\2\uffff\1"+
		"\u0166\1\u0093\1\u0176\1\u0167\1\u0177\1\u010e\1\u0115\1\u010f\1\u0118"+
		"\6\uffff\1\130\1\131\20\uffff\1\u010b\2\uffff\1\u018a\1\113\1\u0191\1"+
		"\122\1\u013b\1\u0141\1\u01aa\1\50\1\u01b7\1\66\1\u014c\1\u015b\1\170\1"+
		"\u008a\1\u01ab\1\53\1\u01ba\1\70\1\140\1\21\1\u00e5\1\144\1\31\1\u00e6"+
		"\7\uffff\1\u00b0\1\u00b1\1\6\3\uffff\1\11\1\u01a2\1\u010a\2\uffff\1\u0095"+
		"\1\u0096\3\uffff\1\u00c2\1\u00c3\1\u019f\2\uffff\1\u00a7\1\u00a8\7\uffff"+
		"\1\u0197\1\u019b\2\uffff\1\171\1\u008b\1\177\1\u008e\5\uffff\1\76\1\100"+
		"\2\uffff\1\77\1\uffff\1\u0123\1\u0124\7\uffff\1\u0110\1\uffff\1\u0119"+
		"\1\uffff\1\u00c7\1\u00d6\2\uffff\1\u01f5\16\uffff\1\u0187\2\uffff\1\u018b"+
		"\1\114\1\u0192\1\123\1\u013c\1\u0142\1\u01ad\1\56\1\u01bb\1\72\1\u0151"+
		"\1\u015c\1\166\1\u0090\1\u01ae\1\57\1\u01bc\1\73\1\141\1\22\1\u0134\1"+
		"\151\1\32\1\u0135\4\uffff\1\u00c8\1\u00d7\1\u00cb\1\u00d8\1\u0198\1\u019c"+
		"\1\u01d2\1\u01e2\1\u0199\1\u019d\1\u01d3\1\u01e5\3\uffff\1\u00fc\1\u00fd"+
		"\1\132\3\uffff\1\156\1\0\1\u0186\2\uffff\1\u00c0\1\u00c1\3\uffff\1\u0130"+
		"\1\u0131\1\u01f7\2\uffff\1\u00f3\1\u00f4\2\uffff\1\u0155\4\uffff\1\u01c8"+
		"\1\u01c9\1\u01cf\1\u01e6\2\uffff\1\u00f0\1\u00f1\3\uffff\1\175\1\u0092"+
		"\2\uffff\1\u009c\1\u009e\2\uffff\1\u009d\1\uffff\1\u0168\1\u0175\2\uffff"+
		"\1\u00a9\1\u00aa\5\uffff\1\u0111\1\uffff\1\u011a\1\uffff\1\u00cc\1\u00d9"+
		"\1\u0169\1\u017a\1\u016d\1\u017b\2\uffff\1\101\15\uffff\1\u0120\2\uffff"+
		"\1\u018c\1\115\1\u0193\1\124\1\u013d\1\u0143\1\u01af\1\51\1\u01bd\1\74"+
		"\1\u0153\1\u015d\1\164\1\u0083\1\u01b1\1\52\1\u01b9\1\75\1\133\1\23\1"+
		"\u00e8\1\153\1\33\1\u00e9\4\uffff\1\u00cf\1\u00db\1\u00d1\1\u00de\1\u0125"+
		"\1\u0127\1\u01d9\1\u01e8\1\u0126\1\u0128\1\u01da\1\u01e9\3\uffff\1\u0164"+
		"\1\u0165\1\u00b3\3\uffff\1\u00b5\1\106\1\u01f4\2\uffff\1\u012e\1\u012f"+
		"\3\uffff\1\u01a0\1\u01a1\1\104\2\uffff\1\u014a\1\u014b\2\uffff\1\u01cb"+
		"\4\uffff\1\12\1\13\1\u01dc\1\u01ec\2\uffff\1\u0146\1\u0148\3\uffff\1\176"+
		"\1\u008d\2\uffff\1\u00ea\1\u00ec\2\uffff\1\u00eb\1\uffff\1\u0171\1\u017c"+
		"\2\uffff\1\u00f5\1\u00f6\4\uffff\1\u0113\1\uffff\1\u011b\1\uffff\1\u00d2"+
		"\1\u00e1\1\u0172\1\u0180\1\u0173\1\u0182\2\uffff\1\u00a2\13\uffff\1\u01cc"+
		"\2\uffff\1\u018d\1\116\1\u0194\1\125\1\u0138\1\u0144\1\u01ac\1\42\1\u01be"+
		"\1\62\1\u0154\1\u015f\1\u0080\1\u0091\1\u01b0\1\43\1\u01bf\1\63\1\142"+
		"\1\24\1\u0196\1\154\1\34\1\u019a\4\uffff\1\u00c9\1\u00da\1\u00cd\1\u00e0"+
		"\1\u01e0\1\u01ef\1\u01db\1\u01e7\1\u01e1\1\u01f0\1\u01d6\1\u01ed\2\uffff"+
		"\1\u0108\1\u0109\1\155\2\uffff\1\u0094\1\5\1\u019e\1\uffff\1\u00e3\1\u00e4"+
		"\2\uffff\1\u0136\1\u0137\1\1\1\uffff\1\u00f7\1\u00f8\2\uffff\1\47\3\uffff"+
		"\1\u01cd\1\u01ce\1\u01d4\1\u01e3\2\uffff\1\u01c6\1\u01c7\2\uffff\1\173"+
		"\1\u008f\1\uffff\1\u00a3\1\u00a5\1\uffff\1\u00a4\1\uffff\1\u0174\1\u0183"+
		"\2\uffff\1\u0156\1\u0160\2\uffff\1\u0114\1\uffff\1\u011c\1\uffff\1\u00ce"+
		"\1\u00dc\1\u0170\1\u017e\1\u016a\1\u0178\1\uffff\1\103\1\u018e\1\117\1"+
		"\u0195\1\126\1\u013e\1\u0145\1\u01a7\1\54\1\u01b8\1\71\1\u0152\1\u015e"+
		"\1\172\1\u0084\1\u01a8\1\55\1\u01b4\1\67\1\143\1\16\1\u00ba\1\152\1\35"+
		"\1\u00bf\1\u00ca\1\u00dd\1\u00d3\1\u00e2\1\u0100\1\u0105\1\u01d5\1\u01ea"+
		"\1\u0101\1\u0106\1\u01d7\1\u01ee\1\u01c4\1\u01c5\1\u00f2\1\u00f9\1\u00a1"+
		"\1\37\1\u0184\1\u0185\1\u01f2\1\u01f3\1\u009b\1\u01c0\1\u01c1\1\uffff"+
		"\1\u01f1\2\uffff\1\107\1\110\1\u01d8\1\u01e4\1\uffff\1\u0161\1\u0162\1"+
		"\uffff\1\165\1\u008c\1\u012b\1\u012d\1\u012c\1\uffff\1\u016f\1\u0179\1"+
		"\uffff\1\u00fa\1\u00fb\1\u0112\1\u011d\1\u00d0\1\u00df\1\u016c\1\u0181"+
		"\1\u016b\1\u017f\1\u00c4\1\127\1\u0102\1\u0107\1\u01dd\1\u01eb\1\2\1\3"+
		"\1\174\1\u0089\1\u016e\1\u017d\1\u01c2\1\u01c3}>";
	static final String[] DFA189_transitionS = {
			"\1\27\7\uffff\1\14\23\uffff\2\14\1\20\1\15\1\16\2\14\1\26\1\22\1\14\1"+
			"\25\1\14\1\21\2\14\1\17\1\14\1\23\1\24\7\14\1\uffff\1\2\2\uffff\1\14"+
			"\1\uffff\2\14\1\5\1\1\1\3\2\14\1\13\1\7\1\14\1\12\1\14\1\6\2\14\1\4\1"+
			"\14\1\10\1\11\7\14\5\uffff\uff80\14",
			"\1\35\12\uffff\1\34\13\uffff\1\32\10\uffff\1\33\12\uffff\1\31",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\37\3\14\1\40\1\43\1\40\1\43\20\14"+
			"\1\56\1\46\1\14\1\54\1\14\1\44\2\14\1\41\1\14\1\50\1\52\24\14\1\55\1"+
			"\45\1\14\1\53\1\14\1\42\2\14\1\36\1\14\1\47\1\51\uff8c\14",
			"\1\62\12\uffff\1\63\3\uffff\1\60\20\uffff\1\57\12\uffff\1\61",
			"\1\72\20\uffff\1\71\3\uffff\1\70\3\uffff\1\65\6\uffff\1\67\20\uffff"+
			"\1\66\3\uffff\1\64",
			"\1\75\16\uffff\1\74\20\uffff\1\73",
			"\1\101\5\uffff\1\102\10\uffff\1\77\20\uffff\1\76\5\uffff\1\100",
			"\1\105\15\uffff\1\104\21\uffff\1\103",
			"\1\111\3\uffff\1\112\26\uffff\1\107\4\uffff\1\106\3\uffff\1\110",
			"\1\uffff",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\121\1\uffff\1\120\35\uffff\1\117",
			"",
			"\1\35\12\uffff\1\34\13\uffff\1\32\10\uffff\1\33\12\uffff\1\31",
			"\1\124\12\uffff\1\125\3\uffff\1\60\20\uffff\1\122\12\uffff\1\123",
			"\1\133\20\uffff\1\132\3\uffff\1\131\3\uffff\1\65\6\uffff\1\130\20\uffff"+
			"\1\127\3\uffff\1\126",
			"\1\135\16\uffff\1\74\20\uffff\1\134",
			"\1\140\5\uffff\1\141\10\uffff\1\77\20\uffff\1\136\5\uffff\1\137",
			"\1\143\15\uffff\1\104\21\uffff\1\142",
			"\1\111\3\uffff\1\112\26\uffff\1\107\4\uffff\1\106\3\uffff\1\110",
			"\1\uffff",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\145\1\uffff\1\120\35\uffff\1\144",
			"",
			"",
			"\1\152\5\uffff\1\151\22\uffff\1\147\6\uffff\1\150\5\uffff\1\146",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\155\3\14\1\157\1\156\1\157\1\156"+
			"\30\14\1\154\37\14\1\153\uff8f\14",
			"\1\162\24\uffff\1\161\12\uffff\1\160",
			"\1\152\5\uffff\1\164\22\uffff\1\147\6\uffff\1\150\5\uffff\1\163",
			"\1\166\24\uffff\1\161\12\uffff\1\165",
			"\1\174\20\uffff\1\172\3\uffff\1\170\3\uffff\1\65\6\uffff\1\173\20\uffff"+
			"\1\171\3\uffff\1\167",
			"\1\175\3\uffff\1\176\1\177\1\176\1\177",
			"\1\u0082\1\u0080\1\u0081\2\uffff\1\u0086\1\u0084\10\uffff\1\u0088\1"+
			"\uffff\1\u0087\35\uffff\1\u0085\1\uffff\1\u0083",
			"\1\u008e\20\uffff\1\u008c\3\uffff\1\u008a\3\uffff\1\65\6\uffff\1\u008d"+
			"\20\uffff\1\u008b\3\uffff\1\u0089",
			"\1\u0090\5\uffff\1\u0092\10\uffff\1\77\20\uffff\1\u008f\5\uffff\1\u0091",
			"\1\u0093\1\uffff\1\u0094\1\u0095",
			"\1\u0097\5\uffff\1\u0099\10\uffff\1\77\20\uffff\1\u0096\5\uffff\1\u0098",
			"\1\u009b\15\uffff\1\104\21\uffff\1\u009a",
			"\1\u009d\15\uffff\1\104\21\uffff\1\u009c",
			"\1\111\3\uffff\1\112\26\uffff\1\107\4\uffff\1\106\3\uffff\1\110",
			"\1\111\3\uffff\1\112\26\uffff\1\107\4\uffff\1\106\3\uffff\1\110",
			"\1\uffff",
			"\1\uffff",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u009f\1\uffff\1\120\35\uffff\1\u009e",
			"\1\u00a1\1\uffff\1\120\35\uffff\1\u00a0",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00a6\3\14\1\u00a7\1\u00a9\1\u00a7"+
			"\1\u00a9\25\14\1\u00a4\12\14\1\u00a8\24\14\1\u00a3\12\14\1\u00a5\uff87"+
			"\14",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00ae\3\14\1\u00b1\1\u00af\1\u00b1"+
			"\1\u00af\34\14\1\u00b0\3\14\1\u00ac\33\14\1\u00ad\3\14\1\u00ab\uff87"+
			"\14",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00b5\3\14\1\u00b6\1\14\1\u00b6"+
			"\26\14\1\u00b4\37\14\1\u00b3\uff92\14",
			"\1\uffff",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00bb\3\14\1\u00bc\1\u00be\1\u00bc"+
			"\1\u00be\25\14\1\u00b9\5\14\1\u00bd\31\14\1\u00b8\5\14\1\u00ba\uff8c"+
			"\14",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00c2\3\14\1\u00c3\1\14\1\u00c3"+
			"\27\14\1\u00c1\37\14\1\u00c0\uff91\14",
			"\1\uffff",
			"\1\u00c6\27\uffff\1\u00c5\7\uffff\1\u00c4",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00c7\3\14\1\u00c8\1\14\1\u00c8"+
			"\uffc9\14",
			"\1\u00cb\16\uffff\1\u00ca\20\uffff\1\u00c9",
			"\1\u00cd\27\uffff\1\u00c5\7\uffff\1\u00cc",
			"\1\u00cf\16\uffff\1\u00ca\20\uffff\1\u00ce",
			"",
			"\1\u00d1\1\uffff\1\120\35\uffff\1\u00d0",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00d4\3\14\1\u00d5\1\14\1\u00d5"+
			"\21\14\1\u00d3\37\14\1\u00d2\uff97\14",
			"\1\u00d7\1\uffff\1\120\35\uffff\1\u00d6",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00db\4\14\1\u00dc\1\14\1\u00dc"+
			"\42\14\1\u00da\37\14\1\u00d9\uff85\14",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00e0\3\14\1\u00e1\1\14\1\u00e1"+
			"\22\14\1\u00df\37\14\1\u00de\uff96\14",
			"\1\u00e4\16\uffff\1\u00e3\20\uffff\1\u00e2",
			"\1\uffff",
			"\1\u00e6\16\uffff\1\u00e3\20\uffff\1\u00e5",
			"\1\152\5\uffff\1\u00e8\22\uffff\1\147\6\uffff\1\150\5\uffff\1\u00e7",
			"\1\152\5\uffff\1\u00ea\22\uffff\1\147\6\uffff\1\150\5\uffff\1\u00e9",
			"\1\u00eb\3\uffff\1\u00ed\1\u00ec\1\u00ed\1\u00ec",
			"\1\u00ee",
			"\1\u00ef",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u00f3\3\14\1\u00f4\1\14\1\u00f4"+
			"\20\14\1\u00f2\37\14\1\u00f1\uff98\14",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u00f5\3\uffff\1\u00f6\1\u00f7\1\u00f6\1\u00f7",
			"\1\u00fa\1\u00f8\1\u00f9\2\uffff\1\u00fe\1\u00fc\10\uffff\1\u0100\1"+
			"\uffff\1\u00ff\35\uffff\1\u00fd\1\uffff\1\u00fb",
			"\1\u0101\1\uffff\1\u0102\1\u0103",
			"\1\u0105\12\uffff\1\34\13\uffff\1\32\10\uffff\1\u0104\12\uffff\1\31",
			"\1\u0108\12\uffff\1\u0109\3\uffff\1\60\20\uffff\1\u0106\12\uffff\1\u0107",
			"\1\u010b\16\uffff\1\74\20\uffff\1\u010a",
			"\1\u010e\5\uffff\1\u010f\10\uffff\1\77\20\uffff\1\u010c\5\uffff\1\u010d",
			"\1\u0111\15\uffff\1\104\21\uffff\1\u0110",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u0113\1\uffff\1\120\35\uffff\1\u0112",
			"\1\u0116\5\uffff\1\u0117\10\uffff\1\77\20\uffff\1\u0114\5\uffff\1\u0115",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u011d\20\uffff\1\u011c\3\uffff\1\u011b\3\uffff\1\65\6\uffff\1\u011a"+
			"\20\uffff\1\u0119\3\uffff\1\u0118",
			"\1\u0120\3\uffff\1\u0121\26\uffff\1\107\4\uffff\1\u011e\3\uffff\1\u011f",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0122\3\uffff\1\u0123\1\u0124\1\u0123\1\u0124",
			"\1\u0126\37\uffff\1\u0125",
			"\1\uffff",
			"\1\u0127",
			"",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0128\3\uffff\1\u012a\1\u0129\1\u012a\1\u0129",
			"\1\u012c\3\uffff\1\u012b",
			"\1\uffff",
			"\1\u012d",
			"",
			"\1\uffff",
			"\1\uffff",
			"\1\u012e\3\uffff\1\u012f\1\uffff\1\u012f",
			"\1\u0131\37\uffff\1\u0130",
			"",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0132\3\uffff\1\u0133\1\u0134\1\u0133\1\u0134",
			"\1\u0136\37\uffff\1\u0135",
			"\1\uffff",
			"\1\u0137",
			"",
			"\1\uffff",
			"\1\uffff",
			"\1\u0138\3\uffff\1\u0139\1\uffff\1\u0139",
			"\1\u013b\37\uffff\1\u013a",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u013d\3\14\1\u013e\1\14\1\u013e"+
			"\uffc9\14",
			"\1\uffff",
			"\1\u013f\3\uffff\1\u0140\1\uffff\1\u0140",
			"\1\u0141\3\uffff\1\u0142",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u0145\3\14\1\u0146\1\14\1\u0146"+
			"\26\14\1\u0144\37\14\1\u0143\uff92\14",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0148\1\uffff\1\120\35\uffff\1\u0147",
			"\1\u014a\1\uffff\1\120\35\uffff\1\u0149",
			"\1\u014b\3\uffff\1\u014c\1\uffff\1\u014c",
			"\1\u014d",
			"\1\uffff",
			"\1\uffff",
			"",
			"\1\uffff",
			"\1\uffff",
			"\1\u014e\4\uffff\1\u014f\1\uffff\1\u014f",
			"\1\u0151\37\uffff\1\u0150",
			"",
			"\1\uffff",
			"\1\uffff",
			"\1\u0152\3\uffff\1\u0153\1\uffff\1\u0153",
			"\1\u0155\5\uffff\1\u0154",
			"\1\uffff",
			"\12\14\1\uffff\1\14\2\uffff\42\14\1\u0158\3\14\1\u0159\1\14\1\u0159"+
			"\26\14\1\u0157\37\14\1\u0156\uff92\14",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u015a\3\uffff\1\u015c\1\u015b\1\u015c\1\u015b",
			"\1\u015d",
			"\1\u015e",
			"\1\u0162\5\uffff\1\u0161\22\uffff\1\147\6\uffff\1\u0160\5\uffff\1\u015f",
			"\1\u0164\24\uffff\1\161\12\uffff\1\u0163",
			"",
			"\1\uffff",
			"\1\uffff",
			"\1\u0165\3\uffff\1\u0166\1\uffff\1\u0166",
			"\1\u0167",
			"\1\u0168\3\uffff\1\u0169\1\u016a\1\u0169\1\u016a",
			"\1\u016d\1\u016b\1\u016c\2\uffff\1\u0171\1\u016f\10\uffff\1\u0173\1"+
			"\uffff\1\u0172\35\uffff\1\u0170\1\uffff\1\u016e",
			"\1\u0174\1\uffff\1\u0175\1\u0176",
			"\1\u0178\12\uffff\1\34\13\uffff\1\32\10\uffff\1\u0177\12\uffff\1\31",
			"\1\u017b\12\uffff\1\u017c\3\uffff\1\60\20\uffff\1\u0179\12\uffff\1\u017a",
			"\1\u017e\16\uffff\1\74\20\uffff\1\u017d",
			"\1\u0181\5\uffff\1\u0182\10\uffff\1\77\20\uffff\1\u017f\5\uffff\1\u0180",
			"\1\u0184\15\uffff\1\104\21\uffff\1\u0183",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u0186\1\uffff\1\120\35\uffff\1\u0185",
			"\1\u0189\5\uffff\1\u018a\10\uffff\1\77\20\uffff\1\u0187\5\uffff\1\u0188",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u0190\20\uffff\1\u018f\3\uffff\1\u018e\3\uffff\1\65\6\uffff\1\u018d"+
			"\20\uffff\1\u018c\3\uffff\1\u018b",
			"\1\u0193\3\uffff\1\u0194\26\uffff\1\107\4\uffff\1\u0191\3\uffff\1\u0192",
			"\1\uffff",
			"\1\u0196\24\uffff\1\161\12\uffff\1\u0195",
			"\1\u0198\24\uffff\1\161\12\uffff\1\u0197",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u019a\27\uffff\1\u00c5\7\uffff\1\u0199",
			"\1\u019c\16\uffff\1\u00ca\20\uffff\1\u019b",
			"\1\u019e\27\uffff\1\u00c5\7\uffff\1\u019d",
			"\1\u01a0\16\uffff\1\u00ca\20\uffff\1\u019f",
			"\1\u01a1\3\uffff\1\u01a2\1\u01a3\1\u01a2\1\u01a3",
			"\1\u01a5\37\uffff\1\u01a4",
			"\1\u01a6",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u01a7\3\uffff\1\u01a9\1\u01a8\1\u01a9\1\u01a8",
			"\1\u01ab\3\uffff\1\u01aa",
			"\1\u01ac",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u01ad\3\uffff\1\u01ae\1\uffff\1\u01ae",
			"\1\u01b0\37\uffff\1\u01af",
			"\1\uffff",
			"\1\uffff",
			"\1\u01b1\3\uffff\1\u01b2\1\u01b3\1\u01b2\1\u01b3",
			"\1\u01b5\37\uffff\1\u01b4",
			"\1\u01b6",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u01b7\3\uffff\1\u01b8\1\uffff\1\u01b8",
			"\1\u01ba\37\uffff\1\u01b9",
			"\1\uffff",
			"\1\uffff",
			"",
			"\1\u01bb\3\uffff\1\u01bc\1\uffff\1\u01bc",
			"\1\u01bd",
			"\1\u01be\3\uffff\1\u01bf\1\uffff\1\u01bf",
			"\1\u01c0\3\uffff\1\u01c1",
			"\1\u01c3\27\uffff\1\u00c5\7\uffff\1\u01c2",
			"\1\u01c5\16\uffff\1\u00ca\20\uffff\1\u01c4",
			"\1\uffff",
			"\1\uffff",
			"\1\u01c6\3\uffff\1\u01c7\1\uffff\1\u01c7",
			"\1\u01c9\37\uffff\1\u01c8",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u01ca\3\uffff\1\u01cb\1\uffff\1\u01cb",
			"\1\u01cc",
			"\1\u01ce\1\uffff\1\120\35\uffff\1\u01cd",
			"\1\u01cf\4\uffff\1\u01d0\1\uffff\1\u01d0",
			"\1\u01d2\37\uffff\1\u01d1",
			"\1\uffff",
			"\1\uffff",
			"\1\u01d3\3\uffff\1\u01d4\1\uffff\1\u01d4",
			"\1\u01d6\5\uffff\1\u01d5",
			"\1\uffff",
			"\1\u01d8\16\uffff\1\u00e3\20\uffff\1\u01d7",
			"\1\uffff",
			"\1\uffff",
			"\1\u01d9\3\uffff\1\u01da\1\uffff\1\u01da",
			"\1\u01dc\37\uffff\1\u01db",
			"\1\u01dd\3\uffff\1\u01df\1\u01de\1\u01df\1\u01de",
			"\1\u01e0",
			"\1\u01e1",
			"\1\u01e5\5\uffff\1\u01e4\22\uffff\1\147\6\uffff\1\u01e3\5\uffff\1\u01e2",
			"\1\u01e7\24\uffff\1\161\12\uffff\1\u01e6",
			"\1\uffff",
			"\1\u01e9\16\uffff\1\u00e3\20\uffff\1\u01e8",
			"\1\uffff",
			"\1\u01eb\16\uffff\1\u00e3\20\uffff\1\u01ea",
			"\1\uffff",
			"\1\uffff",
			"\1\u01ec\3\uffff\1\u01ed\1\uffff\1\u01ed",
			"\1\u01ee",
			"\1\uffff",
			"\1\u01ef\1\u01f0\1\u01ef\1\u01f0",
			"\1\u01f3\1\u01f1\1\u01f2\2\uffff\1\u01f7\1\u01f5\10\uffff\1\u01f9\1"+
			"\uffff\1\u01f8\35\uffff\1\u01f6\1\uffff\1\u01f4",
			"\1\u01fa\1\uffff\1\u01fb\1\u01fc",
			"\1\u01fe\12\uffff\1\34\13\uffff\1\32\10\uffff\1\u01fd\12\uffff\1\31",
			"\1\u0201\12\uffff\1\u0202\3\uffff\1\60\20\uffff\1\u01ff\12\uffff\1\u0200",
			"\1\u0204\16\uffff\1\74\20\uffff\1\u0203",
			"\1\u0207\5\uffff\1\u0208\10\uffff\1\77\20\uffff\1\u0205\5\uffff\1\u0206",
			"\1\u020a\15\uffff\1\104\21\uffff\1\u0209",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u020c\1\uffff\1\120\35\uffff\1\u020b",
			"\1\u020f\5\uffff\1\u0210\10\uffff\1\77\20\uffff\1\u020d\5\uffff\1\u020e",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u0216\20\uffff\1\u0215\3\uffff\1\u0214\3\uffff\1\65\6\uffff\1\u0213"+
			"\20\uffff\1\u0212\3\uffff\1\u0211",
			"\1\u0219\3\uffff\1\u021a\26\uffff\1\107\4\uffff\1\u0217\3\uffff\1\u0218",
			"\1\uffff",
			"\1\u021c\24\uffff\1\161\12\uffff\1\u021b",
			"\1\u021e\24\uffff\1\161\12\uffff\1\u021d",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0220\27\uffff\1\u00c5\7\uffff\1\u021f",
			"\1\u0222\16\uffff\1\u00ca\20\uffff\1\u0221",
			"\1\u0224\27\uffff\1\u00c5\7\uffff\1\u0223",
			"\1\u0226\16\uffff\1\u00ca\20\uffff\1\u0225",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0227\3\uffff\1\u0228\1\u0229\1\u0228\1\u0229",
			"\1\u022b\37\uffff\1\u022a",
			"\1\u022c",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u022d\3\uffff\1\u022f\1\u022e\1\u022f\1\u022e",
			"\1\u0231\3\uffff\1\u0230",
			"\1\u0232",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0233\3\uffff\1\u0234\1\uffff\1\u0234",
			"\1\u0236\37\uffff\1\u0235",
			"\1\uffff",
			"\1\uffff",
			"\1\u0237\3\uffff\1\u0238\1\u0239\1\u0238\1\u0239",
			"\1\u023b\37\uffff\1\u023a",
			"\1\u023c",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u023d\3\uffff\1\u023e\1\uffff\1\u023e",
			"\1\u0240\37\uffff\1\u023f",
			"\1\uffff",
			"\1\uffff",
			"\1\u0241\3\uffff\1\u0242\1\uffff\1\u0242",
			"\1\u0243",
			"\1\uffff",
			"\1\u0244\3\uffff\1\u0245\1\uffff\1\u0245",
			"\1\u0246\3\uffff\1\u0247",
			"\1\u0249\27\uffff\1\u00c5\7\uffff\1\u0248",
			"\1\u024b\16\uffff\1\u00ca\20\uffff\1\u024a",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u024c\3\uffff\1\u024d\1\uffff\1\u024d",
			"\1\u024f\37\uffff\1\u024e",
			"\1\uffff",
			"\1\uffff",
			"\1\u0250\3\uffff\1\u0251\1\uffff\1\u0251",
			"\1\u0252",
			"\1\u0254\1\uffff\1\120\35\uffff\1\u0253",
			"\1\uffff",
			"\1\uffff",
			"\1\u0255\4\uffff\1\u0256\1\uffff\1\u0256",
			"\1\u0258\37\uffff\1\u0257",
			"\1\uffff",
			"\1\uffff",
			"\1\u0259\3\uffff\1\u025a\1\uffff\1\u025a",
			"\1\u025c\5\uffff\1\u025b",
			"\1\uffff",
			"\1\u025e\16\uffff\1\u00e3\20\uffff\1\u025d",
			"\1\uffff",
			"\1\uffff",
			"\1\u025f\3\uffff\1\u0260\1\uffff\1\u0260",
			"\1\u0262\37\uffff\1\u0261",
			"\1\uffff",
			"\1\uffff",
			"\1\u0264\1\u0263\1\u0264\1\u0263",
			"\1\u0265",
			"\1\u0266",
			"\1\u026a\5\uffff\1\u0269\22\uffff\1\147\6\uffff\1\u0268\5\uffff\1\u0267",
			"\1\u026c\24\uffff\1\161\12\uffff\1\u026b",
			"\1\uffff",
			"\1\u026e\16\uffff\1\u00e3\20\uffff\1\u026d",
			"\1\uffff",
			"\1\u0270\16\uffff\1\u00e3\20\uffff\1\u026f",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0271\3\uffff\1\u0272\1\uffff\1\u0272",
			"\1\u0273",
			"\1\uffff",
			"\1\u0276\1\u0274\1\u0275\2\uffff\1\u027a\1\u0278\10\uffff\1\u027c\1"+
			"\uffff\1\u027b\35\uffff\1\u0279\1\uffff\1\u0277",
			"\1\u027d\1\uffff\1\u027e\1\u027f",
			"\1\u0281\12\uffff\1\34\13\uffff\1\32\10\uffff\1\u0280\12\uffff\1\31",
			"\1\u0284\12\uffff\1\u0285\3\uffff\1\60\20\uffff\1\u0282\12\uffff\1\u0283",
			"\1\u0287\16\uffff\1\74\20\uffff\1\u0286",
			"\1\u028a\5\uffff\1\u028b\10\uffff\1\77\20\uffff\1\u0288\5\uffff\1\u0289",
			"\1\u028d\15\uffff\1\104\21\uffff\1\u028c",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u028f\1\uffff\1\120\35\uffff\1\u028e",
			"\1\u0292\5\uffff\1\u0293\10\uffff\1\77\20\uffff\1\u0290\5\uffff\1\u0291",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u0299\20\uffff\1\u0298\3\uffff\1\u0297\3\uffff\1\65\6\uffff\1\u0296"+
			"\20\uffff\1\u0295\3\uffff\1\u0294",
			"\1\u029c\3\uffff\1\u029d\26\uffff\1\107\4\uffff\1\u029a\3\uffff\1\u029b",
			"\1\uffff",
			"\1\u029f\24\uffff\1\161\12\uffff\1\u029e",
			"\1\u02a1\24\uffff\1\161\12\uffff\1\u02a0",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u02a3\27\uffff\1\u00c5\7\uffff\1\u02a2",
			"\1\u02a5\16\uffff\1\u00ca\20\uffff\1\u02a4",
			"\1\u02a7\27\uffff\1\u00c5\7\uffff\1\u02a6",
			"\1\u02a9\16\uffff\1\u00ca\20\uffff\1\u02a8",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u02aa\1\u02ab\1\u02aa\1\u02ab",
			"\1\u02ad\37\uffff\1\u02ac",
			"\1\u02ae",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u02b0\1\u02af\1\u02b0\1\u02af",
			"\1\u02b2\3\uffff\1\u02b1",
			"\1\u02b3",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u02b4\1\uffff\1\u02b4",
			"\1\u02b6\37\uffff\1\u02b5",
			"\1\uffff",
			"\1\uffff",
			"\1\u02b7\1\u02b8\1\u02b7\1\u02b8",
			"\1\u02ba\37\uffff\1\u02b9",
			"\1\u02bb",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u02bc\1\uffff\1\u02bc",
			"\1\u02be\37\uffff\1\u02bd",
			"\1\uffff",
			"\1\uffff",
			"\1\u02bf\3\uffff\1\u02c0\1\uffff\1\u02c0",
			"\1\u02c1",
			"\1\uffff",
			"\1\u02c2\1\uffff\1\u02c2",
			"\1\u02c3\3\uffff\1\u02c4",
			"\1\u02c6\27\uffff\1\u00c5\7\uffff\1\u02c5",
			"\1\u02c8\16\uffff\1\u00ca\20\uffff\1\u02c7",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u02c9\3\uffff\1\u02ca\1\uffff\1\u02ca",
			"\1\u02cc\37\uffff\1\u02cb",
			"\1\uffff",
			"\1\uffff",
			"\1\u02cd\1\uffff\1\u02cd",
			"\1\u02ce",
			"\1\u02d0\1\uffff\1\120\35\uffff\1\u02cf",
			"\1\uffff",
			"\1\uffff",
			"\1\u02d1\1\uffff\1\u02d1",
			"\1\u02d3\37\uffff\1\u02d2",
			"\1\uffff",
			"\1\uffff",
			"\1\u02d4\1\uffff\1\u02d4",
			"\1\u02d6\5\uffff\1\u02d5",
			"\1\uffff",
			"\1\u02d8\16\uffff\1\u00e3\20\uffff\1\u02d7",
			"\1\uffff",
			"\1\uffff",
			"\1\u02d9\3\uffff\1\u02da\1\uffff\1\u02da",
			"\1\u02dc\37\uffff\1\u02db",
			"\1\uffff",
			"\1\uffff",
			"\1\u02dd",
			"\1\u02de",
			"\1\u02e2\5\uffff\1\u02e1\22\uffff\1\147\6\uffff\1\u02e0\5\uffff\1\u02df",
			"\1\u02e4\24\uffff\1\161\12\uffff\1\u02e3",
			"\1\uffff",
			"\1\u02e6\16\uffff\1\u00e3\20\uffff\1\u02e5",
			"\1\uffff",
			"\1\u02e8\16\uffff\1\u00e3\20\uffff\1\u02e7",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u02e9\1\uffff\1\u02e9",
			"\1\u02ea",
			"\1\uffff",
			"\1\35\12\uffff\1\34\13\uffff\1\32\10\uffff\1\33\12\uffff\1\31",
			"\1\u02ed\12\uffff\1\u02ee\3\uffff\1\60\20\uffff\1\u02eb\12\uffff\1\u02ec",
			"\1\u02f0\16\uffff\1\74\20\uffff\1\u02ef",
			"\1\u02f3\5\uffff\1\u02f4\10\uffff\1\77\20\uffff\1\u02f1\5\uffff\1\u02f2",
			"\1\u02f6\15\uffff\1\104\21\uffff\1\u02f5",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u02f8\1\uffff\1\120\35\uffff\1\u02f7",
			"\1\u02fb\5\uffff\1\u02fc\10\uffff\1\77\20\uffff\1\u02f9\5\uffff\1\u02fa",
			"\1\116\23\uffff\1\115\13\uffff\1\114",
			"\1\u0302\20\uffff\1\u0301\3\uffff\1\u0300\3\uffff\1\65\6\uffff\1\u02ff"+
			"\20\uffff\1\u02fe\3\uffff\1\u02fd",
			"\1\111\3\uffff\1\112\26\uffff\1\107\4\uffff\1\106\3\uffff\1\110",
			"\1\uffff",
			"\1\u0304\24\uffff\1\161\12\uffff\1\u0303",
			"\1\u0306\24\uffff\1\161\12\uffff\1\u0305",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0308\27\uffff\1\u00c5\7\uffff\1\u0307",
			"\1\u030a\16\uffff\1\u00ca\20\uffff\1\u0309",
			"\1\u030c\27\uffff\1\u00c5\7\uffff\1\u030b",
			"\1\u030e\16\uffff\1\u00ca\20\uffff\1\u030d",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0310\37\uffff\1\u030f",
			"\1\u0311",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0313\3\uffff\1\u0312",
			"\1\u0314",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0316\37\uffff\1\u0315",
			"\1\uffff",
			"\1\uffff",
			"\1\u0318\37\uffff\1\u0317",
			"\1\u0319",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u031b\37\uffff\1\u031a",
			"\1\uffff",
			"\1\uffff",
			"\1\u031c\1\uffff\1\u031c",
			"\1\u031d",
			"\1\uffff",
			"\1\u031e\3\uffff\1\u031f",
			"\1\u0321\27\uffff\1\u00c5\7\uffff\1\u0320",
			"\1\u0323\16\uffff\1\u00ca\20\uffff\1\u0322",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0324\1\uffff\1\u0324",
			"\1\u0326\37\uffff\1\u0325",
			"\1\uffff",
			"\1\uffff",
			"\1\u0327",
			"\1\u0329\1\uffff\1\120\35\uffff\1\u0328",
			"\1\uffff",
			"\1\uffff",
			"\1\u032b\37\uffff\1\u032a",
			"\1\uffff",
			"\1\uffff",
			"\1\u032d\5\uffff\1\u032c",
			"\1\uffff",
			"\1\u032f\16\uffff\1\u00e3\20\uffff\1\u032e",
			"\1\uffff",
			"\1\uffff",
			"\1\u0330\1\uffff\1\u0330",
			"\1\u0332\37\uffff\1\u0331",
			"\1\uffff",
			"\1\uffff",
			"\1\152\5\uffff\1\u0334\22\uffff\1\147\6\uffff\1\150\5\uffff\1\u0333",
			"\1\u0336\24\uffff\1\161\12\uffff\1\u0335",
			"\1\uffff",
			"\1\u0338\16\uffff\1\u00e3\20\uffff\1\u0337",
			"\1\uffff",
			"\1\u033a\16\uffff\1\u00e3\20\uffff\1\u0339",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u033b",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u033c",
			"\1\uffff",
			"\1\u033e\27\uffff\1\u00c5\7\uffff\1\u033d",
			"\1\u0340\16\uffff\1\u00ca\20\uffff\1\u033f",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0342\37\uffff\1\u0341",
			"\1\uffff",
			"\1\uffff",
			"\1\u0344\1\uffff\1\120\35\uffff\1\u0343",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\u0346\16\uffff\1\u00e3\20\uffff\1\u0345",
			"\1\uffff",
			"\1\uffff",
			"\1\u0348\37\uffff\1\u0347",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff",
			"\1\uffff"
	};

	static final short[] DFA189_eot = DFA.unpackEncodedString(DFA189_eotS);
	static final short[] DFA189_eof = DFA.unpackEncodedString(DFA189_eofS);
	static final char[] DFA189_min = DFA.unpackEncodedStringToUnsignedChars(DFA189_minS);
	static final char[] DFA189_max = DFA.unpackEncodedStringToUnsignedChars(DFA189_maxS);
	static final short[] DFA189_accept = DFA.unpackEncodedString(DFA189_acceptS);
	static final short[] DFA189_special = DFA.unpackEncodedString(DFA189_specialS);
	static final short[][] DFA189_transition;

	static {
		int numStates = DFA189_transitionS.length;
		DFA189_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA189_transition[i] = DFA.unpackEncodedString(DFA189_transitionS[i]);
		}
	}

	protected class DFA189 extends DFA {

		public DFA189(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 189;
			this.eot = DFA189_eot;
			this.eof = DFA189_eof;
			this.min = DFA189_min;
			this.max = DFA189_max;
			this.accept = DFA189_accept;
			this.special = DFA189_special;
			this.transition = DFA189_transition;
		}
		@Override
		public String getDescription() {
			return "1841:9: ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R ( A | E ) )=> R ( A D | E M ) | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | PERCENTAGE_SYMBOL |)";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			IntStream input = _input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA189_427 = input.LA(1);
						 
						int index189_427 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_427);
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA189_699 = input.LA(1);
						 
						int index189_699 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_699);
						if ( s>=0 ) return s;
						break;

					case 2 : 
						int LA189_833 = input.LA(1);
						 
						int index189_833 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_833);
						if ( s>=0 ) return s;
						break;

					case 3 : 
						int LA189_834 = input.LA(1);
						 
						int index189_834 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_834);
						if ( s>=0 ) return s;
						break;

					case 4 : 
						int LA189_63 = input.LA(1);
						s = -1;
						if ( (LA189_63=='m') ) {s = 184;}
						else if ( (LA189_63=='M') ) {s = 185;}
						else if ( (LA189_63=='s') ) {s = 186;}
						else if ( (LA189_63=='0') ) {s = 187;}
						else if ( (LA189_63=='4'||LA189_63=='6') ) {s = 188;}
						else if ( (LA189_63=='S') ) {s = 189;}
						else if ( ((LA189_63 >= '\u0000' && LA189_63 <= '\t')||LA189_63=='\u000B'||(LA189_63 >= '\u000E' && LA189_63 <= '/')||(LA189_63 >= '1' && LA189_63 <= '3')||(LA189_63 >= '8' && LA189_63 <= 'L')||(LA189_63 >= 'N' && LA189_63 <= 'R')||(LA189_63 >= 'T' && LA189_63 <= 'l')||(LA189_63 >= 'n' && LA189_63 <= 'r')||(LA189_63 >= 't' && LA189_63 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA189_63=='5'||LA189_63=='7') ) {s = 190;}
						if ( s>=0 ) return s;
						break;

					case 5 : 
						int LA189_690 = input.LA(1);
						 
						int index189_690 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_690);
						if ( s>=0 ) return s;
						break;

					case 6 : 
						int LA189_295 = input.LA(1);
						 
						int index189_295 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_295);
						if ( s>=0 ) return s;
						break;

					case 7 : 
						int LA189_186 = input.LA(1);
						 
						int index189_186 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_186);
						if ( s>=0 ) return s;
						break;

					case 8 : 
						int LA189_189 = input.LA(1);
						 
						int index189_189 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_189);
						if ( s>=0 ) return s;
						break;

					case 9 : 
						int LA189_299 = input.LA(1);
						 
						int index189_299 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_299);
						if ( s>=0 ) return s;
						break;

					case 10 : 
						int LA189_584 = input.LA(1);
						 
						int index189_584 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_584);
						if ( s>=0 ) return s;
						break;

					case 11 : 
						int LA189_585 = input.LA(1);
						 
						int index189_585 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_585);
						if ( s>=0 ) return s;
						break;

					case 12 : 
						int LA189_121 = input.LA(1);
						 
						int index189_121 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_121);
						if ( s>=0 ) return s;
						break;

					case 13 : 
						int LA189_54 = input.LA(1);
						 
						int index189_54 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_54);
						if ( s>=0 ) return s;
						break;

					case 14 : 
						int LA189_766 = input.LA(1);
						 
						int index189_766 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_766);
						if ( s>=0 ) return s;
						break;

					case 15 : 
						int LA189_87 = input.LA(1);
						 
						int index189_87 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_87);
						if ( s>=0 ) return s;
						break;

					case 16 : 
						int LA189_139 = input.LA(1);
						 
						int index189_139 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_139);
						if ( s>=0 ) return s;
						break;

					case 17 : 
						int LA189_281 = input.LA(1);
						 
						int index189_281 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_281);
						if ( s>=0 ) return s;
						break;

					case 18 : 
						int LA189_396 = input.LA(1);
						 
						int index189_396 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_396);
						if ( s>=0 ) return s;
						break;

					case 19 : 
						int LA189_530 = input.LA(1);
						 
						int index189_530 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_530);
						if ( s>=0 ) return s;
						break;

					case 20 : 
						int LA189_661 = input.LA(1);
						 
						int index189_661 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_661);
						if ( s>=0 ) return s;
						break;

					case 21 : 
						int LA189_90 = input.LA(1);
						 
						int index189_90 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_90);
						if ( s>=0 ) return s;
						break;

					case 22 : 
						int LA189_57 = input.LA(1);
						 
						int index189_57 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_57);
						if ( s>=0 ) return s;
						break;

					case 23 : 
						int LA189_122 = input.LA(1);
						 
						int index189_122 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_122);
						if ( s>=0 ) return s;
						break;

					case 24 : 
						int LA189_140 = input.LA(1);
						 
						int index189_140 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_140);
						if ( s>=0 ) return s;
						break;

					case 25 : 
						int LA189_284 = input.LA(1);
						 
						int index189_284 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_284);
						if ( s>=0 ) return s;
						break;

					case 26 : 
						int LA189_399 = input.LA(1);
						 
						int index189_399 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_399);
						if ( s>=0 ) return s;
						break;

					case 27 : 
						int LA189_533 = input.LA(1);
						 
						int index189_533 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_533);
						if ( s>=0 ) return s;
						break;

					case 28 : 
						int LA189_664 = input.LA(1);
						 
						int index189_664 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_664);
						if ( s>=0 ) return s;
						break;

					case 29 : 
						int LA189_769 = input.LA(1);
						 
						int index189_769 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_769);
						if ( s>=0 ) return s;
						break;

					case 30 : 
						int LA189_173 = input.LA(1);
						 
						int index189_173 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_173);
						if ( s>=0 ) return s;
						break;

					case 31 : 
						int LA189_788 = input.LA(1);
						 
						int index189_788 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_788);
						if ( s>=0 ) return s;
						break;

					case 32 : 
						int LA189_95 = input.LA(1);
						 
						int index189_95 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_95);
						if ( s>=0 ) return s;
						break;

					case 33 : 
						int LA189_64 = input.LA(1);
						 
						int index189_64 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_64);
						if ( s>=0 ) return s;
						break;

					case 34 : 
						int LA189_649 = input.LA(1);
						 
						int index189_649 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_649);
						if ( s>=0 ) return s;
						break;

					case 35 : 
						int LA189_657 = input.LA(1);
						 
						int index189_657 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_657);
						if ( s>=0 ) return s;
						break;

					case 36 : 
						int LA189_145 = input.LA(1);
						 
						int index189_145 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_145);
						if ( s>=0 ) return s;
						break;

					case 37 : 
						int LA189_152 = input.LA(1);
						 
						int index189_152 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_152);
						if ( s>=0 ) return s;
						break;

					case 38 : 
						int LA189_176 = input.LA(1);
						 
						int index189_176 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_176);
						if ( s>=0 ) return s;
						break;

					case 39 : 
						int LA189_705 = input.LA(1);
						 
						int index189_705 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_705);
						if ( s>=0 ) return s;
						break;

					case 40 : 
						int LA189_269 = input.LA(1);
						 
						int index189_269 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_269);
						if ( s>=0 ) return s;
						break;

					case 41 : 
						int LA189_518 = input.LA(1);
						 
						int index189_518 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_518);
						if ( s>=0 ) return s;
						break;

					case 42 : 
						int LA189_526 = input.LA(1);
						 
						int index189_526 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_526);
						if ( s>=0 ) return s;
						break;

					case 43 : 
						int LA189_277 = input.LA(1);
						 
						int index189_277 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_277);
						if ( s>=0 ) return s;
						break;

					case 44 : 
						int LA189_754 = input.LA(1);
						 
						int index189_754 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_754);
						if ( s>=0 ) return s;
						break;

					case 45 : 
						int LA189_762 = input.LA(1);
						 
						int index189_762 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_762);
						if ( s>=0 ) return s;
						break;

					case 46 : 
						int LA189_384 = input.LA(1);
						 
						int index189_384 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_384);
						if ( s>=0 ) return s;
						break;

					case 47 : 
						int LA189_392 = input.LA(1);
						 
						int index189_392 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_392);
						if ( s>=0 ) return s;
						break;

					case 48 : 
						int LA189_97 = input.LA(1);
						 
						int index189_97 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_97);
						if ( s>=0 ) return s;
						break;

					case 49 : 
						int LA189_66 = input.LA(1);
						 
						int index189_66 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_66);
						if ( s>=0 ) return s;
						break;

					case 50 : 
						int LA189_651 = input.LA(1);
						 
						int index189_651 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_651);
						if ( s>=0 ) return s;
						break;

					case 51 : 
						int LA189_659 = input.LA(1);
						 
						int index189_659 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_659);
						if ( s>=0 ) return s;
						break;

					case 52 : 
						int LA189_146 = input.LA(1);
						 
						int index189_146 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_146);
						if ( s>=0 ) return s;
						break;

					case 53 : 
						int LA189_153 = input.LA(1);
						 
						int index189_153 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_153);
						if ( s>=0 ) return s;
						break;

					case 54 : 
						int LA189_271 = input.LA(1);
						 
						int index189_271 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_271);
						if ( s>=0 ) return s;
						break;

					case 55 : 
						int LA189_764 = input.LA(1);
						 
						int index189_764 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_764);
						if ( s>=0 ) return s;
						break;

					case 56 : 
						int LA189_279 = input.LA(1);
						 
						int index189_279 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_279);
						if ( s>=0 ) return s;
						break;

					case 57 : 
						int LA189_756 = input.LA(1);
						 
						int index189_756 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_756);
						if ( s>=0 ) return s;
						break;

					case 58 : 
						int LA189_386 = input.LA(1);
						 
						int index189_386 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_386);
						if ( s>=0 ) return s;
						break;

					case 59 : 
						int LA189_394 = input.LA(1);
						 
						int index189_394 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_394);
						if ( s>=0 ) return s;
						break;

					case 60 : 
						int LA189_520 = input.LA(1);
						 
						int index189_520 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_520);
						if ( s>=0 ) return s;
						break;

					case 61 : 
						int LA189_528 = input.LA(1);
						 
						int index189_528 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_528);
						if ( s>=0 ) return s;
						break;

					case 62 : 
						int LA189_336 = input.LA(1);
						 
						int index189_336 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_336);
						if ( s>=0 ) return s;
						break;

					case 63 : 
						int LA189_340 = input.LA(1);
						 
						int index189_340 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_340);
						if ( s>=0 ) return s;
						break;

					case 64 : 
						int LA189_337 = input.LA(1);
						 
						int index189_337 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_337);
						if ( s>=0 ) return s;
						break;

					case 65 : 
						int LA189_494 = input.LA(1);
						 
						int index189_494 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_494);
						if ( s>=0 ) return s;
						break;

					case 66 : 
						int LA189_113 = input.LA(1);
						s = -1;
						if ( (LA189_113=='g') ) {s = 241;}
						else if ( (LA189_113=='G') ) {s = 242;}
						else if ( ((LA189_113 >= '\u0000' && LA189_113 <= '\t')||LA189_113=='\u000B'||(LA189_113 >= '\u000E' && LA189_113 <= '/')||(LA189_113 >= '1' && LA189_113 <= '3')||LA189_113=='5'||(LA189_113 >= '7' && LA189_113 <= 'F')||(LA189_113 >= 'H' && LA189_113 <= 'f')||(LA189_113 >= 'h' && LA189_113 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA189_113=='0') ) {s = 243;}
						else if ( (LA189_113=='4'||LA189_113=='6') ) {s = 244;}
						if ( s>=0 ) return s;
						break;

					case 67 : 
						int LA189_746 = input.LA(1);
						 
						int index189_746 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_746);
						if ( s>=0 ) return s;
						break;

					case 68 : 
						int LA189_572 = input.LA(1);
						 
						int index189_572 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_572);
						if ( s>=0 ) return s;
						break;

					case 69 : 
						int LA189_197 = input.LA(1);
						s = -1;
						if ( ((LA189_197 >= '\u0000' && LA189_197 <= '\t')||LA189_197=='\u000B'||(LA189_197 >= '\u000E' && LA189_197 <= '/')||(LA189_197 >= '1' && LA189_197 <= '3')||LA189_197=='5'||(LA189_197 >= '7' && LA189_197 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA189_197=='0') ) {s = 317;}
						else if ( (LA189_197=='4'||LA189_197=='6') ) {s = 318;}
						if ( s>=0 ) return s;
						break;

					case 70 : 
						int LA189_561 = input.LA(1);
						 
						int index189_561 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_561);
						if ( s>=0 ) return s;
						break;

					case 71 : 
						int LA189_800 = input.LA(1);
						 
						int index189_800 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_800);
						if ( s>=0 ) return s;
						break;

					case 72 : 
						int LA189_801 = input.LA(1);
						 
						int index189_801 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_801);
						if ( s>=0 ) return s;
						break;

					case 73 : 
						int LA189_49 = input.LA(1);
						 
						int index189_49 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_49);
						if ( s>=0 ) return s;
						break;

					case 74 : 
						int LA189_83 = input.LA(1);
						 
						int index189_83 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_83);
						if ( s>=0 ) return s;
						break;

					case 75 : 
						int LA189_263 = input.LA(1);
						 
						int index189_263 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_263);
						if ( s>=0 ) return s;
						break;

					case 76 : 
						int LA189_378 = input.LA(1);
						 
						int index189_378 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_378);
						if ( s>=0 ) return s;
						break;

					case 77 : 
						int LA189_512 = input.LA(1);
						 
						int index189_512 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_512);
						if ( s>=0 ) return s;
						break;

					case 78 : 
						int LA189_643 = input.LA(1);
						 
						int index189_643 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_643);
						if ( s>=0 ) return s;
						break;

					case 79 : 
						int LA189_748 = input.LA(1);
						 
						int index189_748 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_748);
						if ( s>=0 ) return s;
						break;

					case 80 : 
						int LA189_51 = input.LA(1);
						 
						int index189_51 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_51);
						if ( s>=0 ) return s;
						break;

					case 81 : 
						int LA189_85 = input.LA(1);
						 
						int index189_85 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_85);
						if ( s>=0 ) return s;
						break;

					case 82 : 
						int LA189_265 = input.LA(1);
						 
						int index189_265 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_265);
						if ( s>=0 ) return s;
						break;

					case 83 : 
						int LA189_380 = input.LA(1);
						 
						int index189_380 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_380);
						if ( s>=0 ) return s;
						break;

					case 84 : 
						int LA189_514 = input.LA(1);
						 
						int index189_514 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_514);
						if ( s>=0 ) return s;
						break;

					case 85 : 
						int LA189_645 = input.LA(1);
						 
						int index189_645 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_645);
						if ( s>=0 ) return s;
						break;

					case 86 : 
						int LA189_750 = input.LA(1);
						 
						int index189_750 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_750);
						if ( s>=0 ) return s;
						break;

					case 87 : 
						int LA189_828 = input.LA(1);
						 
						int index189_828 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_828);
						if ( s>=0 ) return s;
						break;

					case 88 : 
						int LA189_241 = input.LA(1);
						 
						int index189_241 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_241);
						if ( s>=0 ) return s;
						break;

					case 89 : 
						int LA189_242 = input.LA(1);
						 
						int index189_242 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_242);
						if ( s>=0 ) return s;
						break;

					case 90 : 
						int LA189_422 = input.LA(1);
						 
						int index189_422 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_422);
						if ( s>=0 ) return s;
						break;

					case 91 : 
						int LA189_529 = input.LA(1);
						 
						int index189_529 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_529);
						if ( s>=0 ) return s;
						break;

					case 92 : 
						int LA189_52 = input.LA(1);
						 
						int index189_52 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_52);
						if ( s>=0 ) return s;
						break;

					case 93 : 
						int LA189_86 = input.LA(1);
						 
						int index189_86 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_86);
						if ( s>=0 ) return s;
						break;

					case 94 : 
						int LA189_119 = input.LA(1);
						 
						int index189_119 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_119);
						if ( s>=0 ) return s;
						break;

					case 95 : 
						int LA189_137 = input.LA(1);
						 
						int index189_137 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_137);
						if ( s>=0 ) return s;
						break;

					case 96 : 
						int LA189_280 = input.LA(1);
						 
						int index189_280 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_280);
						if ( s>=0 ) return s;
						break;

					case 97 : 
						int LA189_395 = input.LA(1);
						 
						int index189_395 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_395);
						if ( s>=0 ) return s;
						break;

					case 98 : 
						int LA189_660 = input.LA(1);
						 
						int index189_660 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_660);
						if ( s>=0 ) return s;
						break;

					case 99 : 
						int LA189_765 = input.LA(1);
						 
						int index189_765 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_765);
						if ( s>=0 ) return s;
						break;

					case 100 : 
						int LA189_283 = input.LA(1);
						 
						int index189_283 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_283);
						if ( s>=0 ) return s;
						break;

					case 101 : 
						int LA189_56 = input.LA(1);
						 
						int index189_56 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_56);
						if ( s>=0 ) return s;
						break;

					case 102 : 
						int LA189_89 = input.LA(1);
						 
						int index189_89 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_89);
						if ( s>=0 ) return s;
						break;

					case 103 : 
						int LA189_120 = input.LA(1);
						 
						int index189_120 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_120);
						if ( s>=0 ) return s;
						break;

					case 104 : 
						int LA189_138 = input.LA(1);
						 
						int index189_138 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_138);
						if ( s>=0 ) return s;
						break;

					case 105 : 
						int LA189_398 = input.LA(1);
						 
						int index189_398 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_398);
						if ( s>=0 ) return s;
						break;

					case 106 : 
						int LA189_768 = input.LA(1);
						 
						int index189_768 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_768);
						if ( s>=0 ) return s;
						break;

					case 107 : 
						int LA189_532 = input.LA(1);
						 
						int index189_532 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_532);
						if ( s>=0 ) return s;
						break;

					case 108 : 
						int LA189_663 = input.LA(1);
						 
						int index189_663 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_663);
						if ( s>=0 ) return s;
						break;

					case 109 : 
						int LA189_686 = input.LA(1);
						 
						int index189_686 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_686);
						if ( s>=0 ) return s;
						break;

					case 110 : 
						int LA189_426 = input.LA(1);
						 
						int index189_426 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_426);
						if ( s>=0 ) return s;
						break;

					case 111 : 
						int LA189_158 = input.LA(1);
						 
						int index189_158 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_158);
						if ( s>=0 ) return s;
						break;

					case 112 : 
						int LA189_79 = input.LA(1);
						 
						int index189_79 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_79);
						if ( s>=0 ) return s;
						break;

					case 113 : 
						int LA189_100 = input.LA(1);
						 
						int index189_100 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_100);
						if ( s>=0 ) return s;
						break;

					case 114 : 
						int LA189_160 = input.LA(1);
						 
						int index189_160 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_160);
						if ( s>=0 ) return s;
						break;

					case 115 : 
						int LA189_208 = input.LA(1);
						 
						int index189_208 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_208);
						if ( s>=0 ) return s;
						break;

					case 116 : 
						int LA189_523 = input.LA(1);
						 
						int index189_523 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_523);
						if ( s>=0 ) return s;
						break;

					case 117 : 
						int LA189_808 = input.LA(1);
						 
						int index189_808 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_808);
						if ( s>=0 ) return s;
						break;

					case 118 : 
						int LA189_389 = input.LA(1);
						 
						int index189_389 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_389);
						if ( s>=0 ) return s;
						break;

					case 119 : 
						int LA189_214 = input.LA(1);
						 
						int index189_214 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_214);
						if ( s>=0 ) return s;
						break;

					case 120 : 
						int LA189_274 = input.LA(1);
						 
						int index189_274 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_274);
						if ( s>=0 ) return s;
						break;

					case 121 : 
						int LA189_327 = input.LA(1);
						 
						int index189_327 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_327);
						if ( s>=0 ) return s;
						break;

					case 122 : 
						int LA189_759 = input.LA(1);
						 
						int index189_759 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_759);
						if ( s>=0 ) return s;
						break;

					case 123 : 
						int LA189_719 = input.LA(1);
						 
						int index189_719 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_719);
						if ( s>=0 ) return s;
						break;

					case 124 : 
						int LA189_835 = input.LA(1);
						 
						int index189_835 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_835);
						if ( s>=0 ) return s;
						break;

					case 125 : 
						int LA189_461 = input.LA(1);
						 
						int index189_461 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_461);
						if ( s>=0 ) return s;
						break;

					case 126 : 
						int LA189_595 = input.LA(1);
						 
						int index189_595 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_595);
						if ( s>=0 ) return s;
						break;

					case 127 : 
						int LA189_329 = input.LA(1);
						 
						int index189_329 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_329);
						if ( s>=0 ) return s;
						break;

					case 128 : 
						int LA189_654 = input.LA(1);
						 
						int index189_654 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_654);
						if ( s>=0 ) return s;
						break;

					case 129 : 
						int LA189_101 = input.LA(1);
						 
						int index189_101 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_101);
						if ( s>=0 ) return s;
						break;

					case 130 : 
						int LA189_81 = input.LA(1);
						 
						int index189_81 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_81);
						if ( s>=0 ) return s;
						break;

					case 131 : 
						int LA189_524 = input.LA(1);
						 
						int index189_524 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_524);
						if ( s>=0 ) return s;
						break;

					case 132 : 
						int LA189_760 = input.LA(1);
						 
						int index189_760 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_760);
						if ( s>=0 ) return s;
						break;

					case 133 : 
						int LA189_159 = input.LA(1);
						 
						int index189_159 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_159);
						if ( s>=0 ) return s;
						break;

					case 134 : 
						int LA189_161 = input.LA(1);
						 
						int index189_161 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_161);
						if ( s>=0 ) return s;
						break;

					case 135 : 
						int LA189_209 = input.LA(1);
						 
						int index189_209 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_209);
						if ( s>=0 ) return s;
						break;

					case 136 : 
						int LA189_215 = input.LA(1);
						 
						int index189_215 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_215);
						if ( s>=0 ) return s;
						break;

					case 137 : 
						int LA189_836 = input.LA(1);
						 
						int index189_836 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_836);
						if ( s>=0 ) return s;
						break;

					case 138 : 
						int LA189_275 = input.LA(1);
						 
						int index189_275 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_275);
						if ( s>=0 ) return s;
						break;

					case 139 : 
						int LA189_328 = input.LA(1);
						 
						int index189_328 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_328);
						if ( s>=0 ) return s;
						break;

					case 140 : 
						int LA189_809 = input.LA(1);
						 
						int index189_809 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_809);
						if ( s>=0 ) return s;
						break;

					case 141 : 
						int LA189_596 = input.LA(1);
						 
						int index189_596 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_596);
						if ( s>=0 ) return s;
						break;

					case 142 : 
						int LA189_330 = input.LA(1);
						 
						int index189_330 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_330);
						if ( s>=0 ) return s;
						break;

					case 143 : 
						int LA189_720 = input.LA(1);
						 
						int index189_720 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_720);
						if ( s>=0 ) return s;
						break;

					case 144 : 
						int LA189_390 = input.LA(1);
						 
						int index189_390 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_390);
						if ( s>=0 ) return s;
						break;

					case 145 : 
						int LA189_655 = input.LA(1);
						 
						int index189_655 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_655);
						if ( s>=0 ) return s;
						break;

					case 146 : 
						int LA189_462 = input.LA(1);
						 
						int index189_462 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_462);
						if ( s>=0 ) return s;
						break;

					case 147 : 
						int LA189_227 = input.LA(1);
						s = -1;
						if ( (LA189_227=='m') ) {s = 342;}
						else if ( (LA189_227=='M') ) {s = 343;}
						else if ( ((LA189_227 >= '\u0000' && LA189_227 <= '\t')||LA189_227=='\u000B'||(LA189_227 >= '\u000E' && LA189_227 <= '/')||(LA189_227 >= '1' && LA189_227 <= '3')||LA189_227=='5'||(LA189_227 >= '7' && LA189_227 <= 'L')||(LA189_227 >= 'N' && LA189_227 <= 'l')||(LA189_227 >= 'n' && LA189_227 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA189_227=='0') ) {s = 344;}
						else if ( (LA189_227=='4'||LA189_227=='6') ) {s = 345;}
						if ( s>=0 ) return s;
						break;

					case 148 : 
						int LA189_689 = input.LA(1);
						 
						int index189_689 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_689);
						if ( s>=0 ) return s;
						break;

					case 149 : 
						int LA189_304 = input.LA(1);
						 
						int index189_304 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_304);
						if ( s>=0 ) return s;
						break;

					case 150 : 
						int LA189_305 = input.LA(1);
						 
						int index189_305 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_305);
						if ( s>=0 ) return s;
						break;

					case 151 : 
						int LA189_60 = input.LA(1);
						s = -1;
						if ( (LA189_60=='m') ) {s = 179;}
						else if ( (LA189_60=='M') ) {s = 180;}
						else if ( ((LA189_60 >= '\u0000' && LA189_60 <= '\t')||LA189_60=='\u000B'||(LA189_60 >= '\u000E' && LA189_60 <= '/')||(LA189_60 >= '1' && LA189_60 <= '3')||LA189_60=='5'||(LA189_60 >= '7' && LA189_60 <= 'L')||(LA189_60 >= 'N' && LA189_60 <= 'l')||(LA189_60 >= 'n' && LA189_60 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA189_60=='0') ) {s = 181;}
						else if ( (LA189_60=='4'||LA189_60=='6') ) {s = 182;}
						if ( s>=0 ) return s;
						break;

					case 152 : 
						int LA189_165 = input.LA(1);
						 
						int index189_165 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_165);
						if ( s>=0 ) return s;
						break;

					case 153 : 
						int LA189_168 = input.LA(1);
						 
						int index189_168 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_168);
						if ( s>=0 ) return s;
						break;

					case 154 : 
						int LA189_202 = input.LA(1);
						s = -1;
						if ( (LA189_202=='m') ) {s = 323;}
						else if ( (LA189_202=='M') ) {s = 324;}
						else if ( ((LA189_202 >= '\u0000' && LA189_202 <= '\t')||LA189_202=='\u000B'||(LA189_202 >= '\u000E' && LA189_202 <= '/')||(LA189_202 >= '1' && LA189_202 <= '3')||LA189_202=='5'||(LA189_202 >= '7' && LA189_202 <= 'L')||(LA189_202 >= 'N' && LA189_202 <= 'l')||(LA189_202 >= 'n' && LA189_202 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA189_202=='0') ) {s = 325;}
						else if ( (LA189_202=='4'||LA189_202=='6') ) {s = 326;}
						if ( s>=0 ) return s;
						break;

					case 155 : 
						int LA189_793 = input.LA(1);
						 
						int index189_793 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_793);
						if ( s>=0 ) return s;
						break;

					case 156 : 
						int LA189_465 = input.LA(1);
						 
						int index189_465 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_465);
						if ( s>=0 ) return s;
						break;

					case 157 : 
						int LA189_469 = input.LA(1);
						 
						int index189_469 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_469);
						if ( s>=0 ) return s;
						break;

					case 158 : 
						int LA189_466 = input.LA(1);
						 
						int index189_466 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_466);
						if ( s>=0 ) return s;
						break;

					case 159 : 
						int LA189_171 = input.LA(1);
						 
						int index189_171 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_171);
						if ( s>=0 ) return s;
						break;

					case 160 : 
						int LA189_172 = input.LA(1);
						 
						int index189_172 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_172);
						if ( s>=0 ) return s;
						break;

					case 161 : 
						int LA189_787 = input.LA(1);
						 
						int index189_787 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_787);
						if ( s>=0 ) return s;
						break;

					case 162 : 
						int LA189_627 = input.LA(1);
						 
						int index189_627 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_627);
						if ( s>=0 ) return s;
						break;

					case 163 : 
						int LA189_722 = input.LA(1);
						 
						int index189_722 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_722);
						if ( s>=0 ) return s;
						break;

					case 164 : 
						int LA189_725 = input.LA(1);
						 
						int index189_725 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_725);
						if ( s>=0 ) return s;
						break;

					case 165 : 
						int LA189_723 = input.LA(1);
						 
						int index189_723 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_723);
						if ( s>=0 ) return s;
						break;

					case 166 : 
						int LA189_77 = input.LA(1);
						s = -1;
						if ( (LA189_77=='h') ) {s = 210;}
						else if ( (LA189_77=='H') ) {s = 211;}
						else if ( ((LA189_77 >= '\u0000' && LA189_77 <= '\t')||LA189_77=='\u000B'||(LA189_77 >= '\u000E' && LA189_77 <= '/')||(LA189_77 >= '1' && LA189_77 <= '3')||LA189_77=='5'||(LA189_77 >= '7' && LA189_77 <= 'G')||(LA189_77 >= 'I' && LA189_77 <= 'g')||(LA189_77 >= 'i' && LA189_77 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA189_77=='0') ) {s = 212;}
						else if ( (LA189_77=='4'||LA189_77=='6') ) {s = 213;}
						if ( s>=0 ) return s;
						break;

					case 167 : 
						int LA189_314 = input.LA(1);
						 
						int index189_314 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_314);
						if ( s>=0 ) return s;
						break;

					case 168 : 
						int LA189_315 = input.LA(1);
						 
						int index189_315 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_315);
						if ( s>=0 ) return s;
						break;

					case 169 : 
						int LA189_475 = input.LA(1);
						 
						int index189_475 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_475);
						if ( s>=0 ) return s;
						break;

					case 170 : 
						int LA189_476 = input.LA(1);
						 
						int index189_476 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_476);
						if ( s>=0 ) return s;
						break;

					case 171 : 
						int LA189_222 = input.LA(1);
						 
						int index189_222 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_222);
						if ( s>=0 ) return s;
						break;

					case 172 : 
						int LA189_223 = input.LA(1);
						 
						int index189_223 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_223);
						if ( s>=0 ) return s;
						break;

					case 173 : 
						int LA189_2 = input.LA(1);
						s = -1;
						if ( (LA189_2=='p') ) {s = 30;}
						else if ( (LA189_2=='0') ) {s = 31;}
						else if ( (LA189_2=='4'||LA189_2=='6') ) {s = 32;}
						else if ( (LA189_2=='P') ) {s = 33;}
						else if ( (LA189_2=='m') ) {s = 34;}
						else if ( (LA189_2=='5'||LA189_2=='7') ) {s = 35;}
						else if ( (LA189_2=='M') ) {s = 36;}
						else if ( (LA189_2=='i') ) {s = 37;}
						else if ( (LA189_2=='I') ) {s = 38;}
						else if ( (LA189_2=='r') ) {s = 39;}
						else if ( (LA189_2=='R') ) {s = 40;}
						else if ( (LA189_2=='s') ) {s = 41;}
						else if ( (LA189_2=='S') ) {s = 42;}
						else if ( (LA189_2=='k') ) {s = 43;}
						else if ( (LA189_2=='K') ) {s = 44;}
						else if ( (LA189_2=='h') ) {s = 45;}
						else if ( (LA189_2=='H') ) {s = 46;}
						else if ( ((LA189_2 >= '\u0000' && LA189_2 <= '\t')||LA189_2=='\u000B'||(LA189_2 >= '\u000E' && LA189_2 <= '/')||(LA189_2 >= '1' && LA189_2 <= '3')||(LA189_2 >= '8' && LA189_2 <= 'G')||LA189_2=='J'||LA189_2=='L'||(LA189_2 >= 'N' && LA189_2 <= 'O')||LA189_2=='Q'||(LA189_2 >= 'T' && LA189_2 <= 'g')||LA189_2=='j'||LA189_2=='l'||(LA189_2 >= 'n' && LA189_2 <= 'o')||LA189_2=='q'||(LA189_2 >= 't' && LA189_2 <= '\uFFFF')) ) {s = 12;}
						if ( s>=0 ) return s;
						break;

					case 174 : 
						int LA189_217 = input.LA(1);
						 
						int index189_217 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_217);
						if ( s>=0 ) return s;
						break;

					case 175 : 
						int LA189_218 = input.LA(1);
						 
						int index189_218 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_218);
						if ( s>=0 ) return s;
						break;

					case 176 : 
						int LA189_293 = input.LA(1);
						 
						int index189_293 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_293);
						if ( s>=0 ) return s;
						break;

					case 177 : 
						int LA189_294 = input.LA(1);
						 
						int index189_294 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_294);
						if ( s>=0 ) return s;
						break;

					case 178 : 
						int LA189_26 = input.LA(1);
						s = -1;
						if ( (LA189_26=='p') ) {s = 107;}
						else if ( (LA189_26=='P') ) {s = 108;}
						else if ( ((LA189_26 >= '\u0000' && LA189_26 <= '\t')||LA189_26=='\u000B'||(LA189_26 >= '\u000E' && LA189_26 <= '/')||(LA189_26 >= '1' && LA189_26 <= '3')||(LA189_26 >= '8' && LA189_26 <= 'O')||(LA189_26 >= 'Q' && LA189_26 <= 'o')||(LA189_26 >= 'q' && LA189_26 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA189_26=='0') ) {s = 109;}
						else if ( (LA189_26=='5'||LA189_26=='7') ) {s = 110;}
						else if ( (LA189_26=='4'||LA189_26=='6') ) {s = 111;}
						if ( s>=0 ) return s;
						break;

					case 179 : 
						int LA189_556 = input.LA(1);
						 
						int index189_556 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_556);
						if ( s>=0 ) return s;
						break;

					case 180 : 
						int LA189_149 = input.LA(1);
						 
						int index189_149 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_149);
						if ( s>=0 ) return s;
						break;

					case 181 : 
						int LA189_560 = input.LA(1);
						 
						int index189_560 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_560);
						if ( s>=0 ) return s;
						break;

					case 182 : 
						int LA189_55 = input.LA(1);
						 
						int index189_55 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_55);
						if ( s>=0 ) return s;
						break;

					case 183 : 
						int LA189_88 = input.LA(1);
						 
						int index189_88 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_88);
						if ( s>=0 ) return s;
						break;

					case 184 : 
						int LA189_123 = input.LA(1);
						 
						int index189_123 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_123);
						if ( s>=0 ) return s;
						break;

					case 185 : 
						int LA189_141 = input.LA(1);
						 
						int index189_141 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_141);
						if ( s>=0 ) return s;
						break;

					case 186 : 
						int LA189_767 = input.LA(1);
						 
						int index189_767 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_767);
						if ( s>=0 ) return s;
						break;

					case 187 : 
						int LA189_58 = input.LA(1);
						 
						int index189_58 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_58);
						if ( s>=0 ) return s;
						break;

					case 188 : 
						int LA189_91 = input.LA(1);
						 
						int index189_91 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_91);
						if ( s>=0 ) return s;
						break;

					case 189 : 
						int LA189_124 = input.LA(1);
						 
						int index189_124 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_124);
						if ( s>=0 ) return s;
						break;

					case 190 : 
						int LA189_142 = input.LA(1);
						 
						int index189_142 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_142);
						if ( s>=0 ) return s;
						break;

					case 191 : 
						int LA189_770 = input.LA(1);
						 
						int index189_770 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_770);
						if ( s>=0 ) return s;
						break;

					case 192 : 
						int LA189_431 = input.LA(1);
						 
						int index189_431 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_431);
						if ( s>=0 ) return s;
						break;

					case 193 : 
						int LA189_432 = input.LA(1);
						 
						int index189_432 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_432);
						if ( s>=0 ) return s;
						break;

					case 194 : 
						int LA189_309 = input.LA(1);
						 
						int index189_309 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_309);
						if ( s>=0 ) return s;
						break;

					case 195 : 
						int LA189_310 = input.LA(1);
						 
						int index189_310 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_310);
						if ( s>=0 ) return s;
						break;

					case 196 : 
						int LA189_827 = input.LA(1);
						 
						int index189_827 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_827);
						if ( s>=0 ) return s;
						break;

					case 197 : 
						int LA189_112 = input.LA(1);
						 
						int index189_112 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_112);
						if ( s>=0 ) return s;
						break;

					case 198 : 
						int LA189_117 = input.LA(1);
						 
						int index189_117 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_117);
						if ( s>=0 ) return s;
						break;

					case 199 : 
						int LA189_355 = input.LA(1);
						 
						int index189_355 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_355);
						if ( s>=0 ) return s;
						break;

					case 200 : 
						int LA189_405 = input.LA(1);
						 
						int index189_405 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_405);
						if ( s>=0 ) return s;
						break;

					case 201 : 
						int LA189_670 = input.LA(1);
						 
						int index189_670 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_670);
						if ( s>=0 ) return s;
						break;

					case 202 : 
						int LA189_771 = input.LA(1);
						 
						int index189_771 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_771);
						if ( s>=0 ) return s;
						break;

					case 203 : 
						int LA189_407 = input.LA(1);
						 
						int index189_407 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_407);
						if ( s>=0 ) return s;
						break;

					case 204 : 
						int LA189_486 = input.LA(1);
						 
						int index189_486 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_486);
						if ( s>=0 ) return s;
						break;

					case 205 : 
						int LA189_672 = input.LA(1);
						 
						int index189_672 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_672);
						if ( s>=0 ) return s;
						break;

					case 206 : 
						int LA189_739 = input.LA(1);
						 
						int index189_739 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_739);
						if ( s>=0 ) return s;
						break;

					case 207 : 
						int LA189_539 = input.LA(1);
						 
						int index189_539 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_539);
						if ( s>=0 ) return s;
						break;

					case 208 : 
						int LA189_821 = input.LA(1);
						 
						int index189_821 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_821);
						if ( s>=0 ) return s;
						break;

					case 209 : 
						int LA189_541 = input.LA(1);
						 
						int index189_541 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_541);
						if ( s>=0 ) return s;
						break;

					case 210 : 
						int LA189_619 = input.LA(1);
						 
						int index189_619 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_619);
						if ( s>=0 ) return s;
						break;

					case 211 : 
						int LA189_773 = input.LA(1);
						 
						int index189_773 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_773);
						if ( s>=0 ) return s;
						break;

					case 212 : 
						int LA189_118 = input.LA(1);
						 
						int index189_118 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_118);
						if ( s>=0 ) return s;
						break;

					case 213 : 
						int LA189_114 = input.LA(1);
						 
						int index189_114 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_114);
						if ( s>=0 ) return s;
						break;

					case 214 : 
						int LA189_356 = input.LA(1);
						 
						int index189_356 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_356);
						if ( s>=0 ) return s;
						break;

					case 215 : 
						int LA189_406 = input.LA(1);
						 
						int index189_406 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_406);
						if ( s>=0 ) return s;
						break;

					case 216 : 
						int LA189_408 = input.LA(1);
						 
						int index189_408 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_408);
						if ( s>=0 ) return s;
						break;

					case 217 : 
						int LA189_487 = input.LA(1);
						 
						int index189_487 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_487);
						if ( s>=0 ) return s;
						break;

					case 218 : 
						int LA189_671 = input.LA(1);
						 
						int index189_671 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_671);
						if ( s>=0 ) return s;
						break;

					case 219 : 
						int LA189_540 = input.LA(1);
						 
						int index189_540 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_540);
						if ( s>=0 ) return s;
						break;

					case 220 : 
						int LA189_740 = input.LA(1);
						 
						int index189_740 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_740);
						if ( s>=0 ) return s;
						break;

					case 221 : 
						int LA189_772 = input.LA(1);
						 
						int index189_772 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_772);
						if ( s>=0 ) return s;
						break;

					case 222 : 
						int LA189_542 = input.LA(1);
						 
						int index189_542 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_542);
						if ( s>=0 ) return s;
						break;

					case 223 : 
						int LA189_822 = input.LA(1);
						 
						int index189_822 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_822);
						if ( s>=0 ) return s;
						break;

					case 224 : 
						int LA189_673 = input.LA(1);
						 
						int index189_673 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_673);
						if ( s>=0 ) return s;
						break;

					case 225 : 
						int LA189_620 = input.LA(1);
						 
						int index189_620 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_620);
						if ( s>=0 ) return s;
						break;

					case 226 : 
						int LA189_774 = input.LA(1);
						 
						int index189_774 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_774);
						if ( s>=0 ) return s;
						break;

					case 227 : 
						int LA189_693 = input.LA(1);
						 
						int index189_693 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_693);
						if ( s>=0 ) return s;
						break;

					case 228 : 
						int LA189_694 = input.LA(1);
						 
						int index189_694 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_694);
						if ( s>=0 ) return s;
						break;

					case 229 : 
						int LA189_282 = input.LA(1);
						 
						int index189_282 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_282);
						if ( s>=0 ) return s;
						break;

					case 230 : 
						int LA189_285 = input.LA(1);
						 
						int index189_285 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_285);
						if ( s>=0 ) return s;
						break;

					case 231 : 
						int LA189_68 = input.LA(1);
						s = -1;
						if ( (LA189_68=='n') ) {s = 192;}
						else if ( (LA189_68=='N') ) {s = 193;}
						else if ( ((LA189_68 >= '\u0000' && LA189_68 <= '\t')||LA189_68=='\u000B'||(LA189_68 >= '\u000E' && LA189_68 <= '/')||(LA189_68 >= '1' && LA189_68 <= '3')||LA189_68=='5'||(LA189_68 >= '7' && LA189_68 <= 'M')||(LA189_68 >= 'O' && LA189_68 <= 'm')||(LA189_68 >= 'o' && LA189_68 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA189_68=='0') ) {s = 194;}
						else if ( (LA189_68=='4'||LA189_68=='6') ) {s = 195;}
						if ( s>=0 ) return s;
						break;

					case 232 : 
						int LA189_531 = input.LA(1);
						 
						int index189_531 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_531);
						if ( s>=0 ) return s;
						break;

					case 233 : 
						int LA189_534 = input.LA(1);
						 
						int index189_534 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_534);
						if ( s>=0 ) return s;
						break;

					case 234 : 
						int LA189_599 = input.LA(1);
						 
						int index189_599 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_599);
						if ( s>=0 ) return s;
						break;

					case 235 : 
						int LA189_603 = input.LA(1);
						 
						int index189_603 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_603);
						if ( s>=0 ) return s;
						break;

					case 236 : 
						int LA189_600 = input.LA(1);
						 
						int index189_600 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_600);
						if ( s>=0 ) return s;
						break;

					case 237 : 
						int LA189_179 = input.LA(1);
						 
						int index189_179 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_179);
						if ( s>=0 ) return s;
						break;

					case 238 : 
						int LA189_180 = input.LA(1);
						 
						int index189_180 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_180);
						if ( s>=0 ) return s;
						break;

					case 239 : 
						int LA189_71 = input.LA(1);
						s = -1;
						if ( ((LA189_71 >= '\u0000' && LA189_71 <= '\t')||LA189_71=='\u000B'||(LA189_71 >= '\u000E' && LA189_71 <= '/')||(LA189_71 >= '1' && LA189_71 <= '3')||LA189_71=='5'||(LA189_71 >= '7' && LA189_71 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA189_71=='0') ) {s = 199;}
						else if ( (LA189_71=='4'||LA189_71=='6') ) {s = 200;}
						if ( s>=0 ) return s;
						break;

					case 240 : 
						int LA189_456 = input.LA(1);
						 
						int index189_456 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_456);
						if ( s>=0 ) return s;
						break;

					case 241 : 
						int LA189_457 = input.LA(1);
						 
						int index189_457 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_457);
						if ( s>=0 ) return s;
						break;

					case 242 : 
						int LA189_785 = input.LA(1);
						 
						int index189_785 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_785);
						if ( s>=0 ) return s;
						break;

					case 243 : 
						int LA189_441 = input.LA(1);
						 
						int index189_441 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_441);
						if ( s>=0 ) return s;
						break;

					case 244 : 
						int LA189_442 = input.LA(1);
						 
						int index189_442 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_442);
						if ( s>=0 ) return s;
						break;

					case 245 : 
						int LA189_609 = input.LA(1);
						 
						int index189_609 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_609);
						if ( s>=0 ) return s;
						break;

					case 246 : 
						int LA189_610 = input.LA(1);
						 
						int index189_610 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_610);
						if ( s>=0 ) return s;
						break;

					case 247 : 
						int LA189_701 = input.LA(1);
						 
						int index189_701 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_701);
						if ( s>=0 ) return s;
						break;

					case 248 : 
						int LA189_702 = input.LA(1);
						 
						int index189_702 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_702);
						if ( s>=0 ) return s;
						break;

					case 249 : 
						int LA189_786 = input.LA(1);
						 
						int index189_786 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_786);
						if ( s>=0 ) return s;
						break;

					case 250 : 
						int LA189_817 = input.LA(1);
						 
						int index189_817 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_817);
						if ( s>=0 ) return s;
						break;

					case 251 : 
						int LA189_818 = input.LA(1);
						 
						int index189_818 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_818);
						if ( s>=0 ) return s;
						break;

					case 252 : 
						int LA189_420 = input.LA(1);
						 
						int index189_420 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_420);
						if ( s>=0 ) return s;
						break;

					case 253 : 
						int LA189_421 = input.LA(1);
						 
						int index189_421 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_421);
						if ( s>=0 ) return s;
						break;

					case 254 : 
						int LA189_196 = input.LA(1);
						 
						int index189_196 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_196);
						if ( s>=0 ) return s;
						break;

					case 255 : 
						int LA189_204 = input.LA(1);
						 
						int index189_204 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_204);
						if ( s>=0 ) return s;
						break;

					case 256 : 
						int LA189_775 = input.LA(1);
						 
						int index189_775 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_775);
						if ( s>=0 ) return s;
						break;

					case 257 : 
						int LA189_779 = input.LA(1);
						 
						int index189_779 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_779);
						if ( s>=0 ) return s;
						break;

					case 258 : 
						int LA189_829 = input.LA(1);
						 
						int index189_829 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_829);
						if ( s>=0 ) return s;
						break;

					case 259 : 
						int LA189_198 = input.LA(1);
						 
						int index189_198 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_198);
						if ( s>=0 ) return s;
						break;

					case 260 : 
						int LA189_205 = input.LA(1);
						 
						int index189_205 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_205);
						if ( s>=0 ) return s;
						break;

					case 261 : 
						int LA189_776 = input.LA(1);
						 
						int index189_776 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_776);
						if ( s>=0 ) return s;
						break;

					case 262 : 
						int LA189_780 = input.LA(1);
						 
						int index189_780 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_780);
						if ( s>=0 ) return s;
						break;

					case 263 : 
						int LA189_830 = input.LA(1);
						 
						int index189_830 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_830);
						if ( s>=0 ) return s;
						break;

					case 264 : 
						int LA189_684 = input.LA(1);
						 
						int index189_684 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_684);
						if ( s>=0 ) return s;
						break;

					case 265 : 
						int LA189_685 = input.LA(1);
						 
						int index189_685 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_685);
						if ( s>=0 ) return s;
						break;

					case 266 : 
						int LA189_301 = input.LA(1);
						 
						int index189_301 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_301);
						if ( s>=0 ) return s;
						break;

					case 267 : 
						int LA189_259 = input.LA(1);
						 
						int index189_259 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_259);
						if ( s>=0 ) return s;
						break;

					case 268 : 
						int LA189_102 = input.LA(1);
						 
						int index189_102 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_102);
						if ( s>=0 ) return s;
						break;

					case 269 : 
						int LA189_115 = input.LA(1);
						 
						int index189_115 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_115);
						if ( s>=0 ) return s;
						break;

					case 270 : 
						int LA189_231 = input.LA(1);
						 
						int index189_231 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_231);
						if ( s>=0 ) return s;
						break;

					case 271 : 
						int LA189_233 = input.LA(1);
						 
						int index189_233 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_233);
						if ( s>=0 ) return s;
						break;

					case 272 : 
						int LA189_351 = input.LA(1);
						 
						int index189_351 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_351);
						if ( s>=0 ) return s;
						break;

					case 273 : 
						int LA189_482 = input.LA(1);
						 
						int index189_482 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_482);
						if ( s>=0 ) return s;
						break;

					case 274 : 
						int LA189_819 = input.LA(1);
						 
						int index189_819 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_819);
						if ( s>=0 ) return s;
						break;

					case 275 : 
						int LA189_615 = input.LA(1);
						 
						int index189_615 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_615);
						if ( s>=0 ) return s;
						break;

					case 276 : 
						int LA189_735 = input.LA(1);
						 
						int index189_735 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_735);
						if ( s>=0 ) return s;
						break;

					case 277 : 
						int LA189_232 = input.LA(1);
						 
						int index189_232 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_232);
						if ( s>=0 ) return s;
						break;

					case 278 : 
						int LA189_105 = input.LA(1);
						 
						int index189_105 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_105);
						if ( s>=0 ) return s;
						break;

					case 279 : 
						int LA189_116 = input.LA(1);
						 
						int index189_116 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_116);
						if ( s>=0 ) return s;
						break;

					case 280 : 
						int LA189_234 = input.LA(1);
						 
						int index189_234 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_234);
						if ( s>=0 ) return s;
						break;

					case 281 : 
						int LA189_353 = input.LA(1);
						 
						int index189_353 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_353);
						if ( s>=0 ) return s;
						break;

					case 282 : 
						int LA189_484 = input.LA(1);
						 
						int index189_484 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_484);
						if ( s>=0 ) return s;
						break;

					case 283 : 
						int LA189_617 = input.LA(1);
						 
						int index189_617 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_617);
						if ( s>=0 ) return s;
						break;

					case 284 : 
						int LA189_737 = input.LA(1);
						 
						int index189_737 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_737);
						if ( s>=0 ) return s;
						break;

					case 285 : 
						int LA189_820 = input.LA(1);
						 
						int index189_820 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_820);
						if ( s>=0 ) return s;
						break;

					case 286 : 
						int LA189_41 = input.LA(1);
						 
						int index189_41 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_41);
						if ( s>=0 ) return s;
						break;

					case 287 : 
						int LA189_42 = input.LA(1);
						 
						int index189_42 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_42);
						if ( s>=0 ) return s;
						break;

					case 288 : 
						int LA189_508 = input.LA(1);
						 
						int index189_508 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_508);
						if ( s>=0 ) return s;
						break;

					case 289 : 
						int LA189_192 = input.LA(1);
						 
						int index189_192 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_192);
						if ( s>=0 ) return s;
						break;

					case 290 : 
						int LA189_193 = input.LA(1);
						 
						int index189_193 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_193);
						if ( s>=0 ) return s;
						break;

					case 291 : 
						int LA189_342 = input.LA(1);
						 
						int index189_342 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_342);
						if ( s>=0 ) return s;
						break;

					case 292 : 
						int LA189_343 = input.LA(1);
						 
						int index189_343 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_343);
						if ( s>=0 ) return s;
						break;

					case 293 : 
						int LA189_543 = input.LA(1);
						 
						int index189_543 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_543);
						if ( s>=0 ) return s;
						break;

					case 294 : 
						int LA189_547 = input.LA(1);
						 
						int index189_547 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_547);
						if ( s>=0 ) return s;
						break;

					case 295 : 
						int LA189_544 = input.LA(1);
						 
						int index189_544 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_544);
						if ( s>=0 ) return s;
						break;

					case 296 : 
						int LA189_548 = input.LA(1);
						 
						int index189_548 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_548);
						if ( s>=0 ) return s;
						break;

					case 297 : 
						int LA189_9 = input.LA(1);
						 
						int index189_9 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_9);
						if ( s>=0 ) return s;
						break;

					case 298 : 
						int LA189_20 = input.LA(1);
						 
						int index189_20 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_20);
						if ( s>=0 ) return s;
						break;

					case 299 : 
						int LA189_810 = input.LA(1);
						 
						int index189_810 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_810);
						if ( s>=0 ) return s;
						break;

					case 300 : 
						int LA189_812 = input.LA(1);
						 
						int index189_812 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_812);
						if ( s>=0 ) return s;
						break;

					case 301 : 
						int LA189_811 = input.LA(1);
						 
						int index189_811 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred11_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_811);
						if ( s>=0 ) return s;
						break;

					case 302 : 
						int LA189_565 = input.LA(1);
						 
						int index189_565 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_565);
						if ( s>=0 ) return s;
						break;

					case 303 : 
						int LA189_566 = input.LA(1);
						 
						int index189_566 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_566);
						if ( s>=0 ) return s;
						break;

					case 304 : 
						int LA189_436 = input.LA(1);
						 
						int index189_436 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_436);
						if ( s>=0 ) return s;
						break;

					case 305 : 
						int LA189_437 = input.LA(1);
						 
						int index189_437 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_437);
						if ( s>=0 ) return s;
						break;

					case 306 : 
						int LA189_163 = input.LA(1);
						 
						int index189_163 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_163);
						if ( s>=0 ) return s;
						break;

					case 307 : 
						int LA189_164 = input.LA(1);
						 
						int index189_164 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_164);
						if ( s>=0 ) return s;
						break;

					case 308 : 
						int LA189_397 = input.LA(1);
						 
						int index189_397 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_397);
						if ( s>=0 ) return s;
						break;

					case 309 : 
						int LA189_400 = input.LA(1);
						 
						int index189_400 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_400);
						if ( s>=0 ) return s;
						break;

					case 310 : 
						int LA189_697 = input.LA(1);
						 
						int index189_697 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_697);
						if ( s>=0 ) return s;
						break;

					case 311 : 
						int LA189_698 = input.LA(1);
						 
						int index189_698 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_698);
						if ( s>=0 ) return s;
						break;

					case 312 : 
						int LA189_646 = input.LA(1);
						 
						int index189_646 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_646);
						if ( s>=0 ) return s;
						break;

					case 313 : 
						int LA189_59 = input.LA(1);
						 
						int index189_59 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_59);
						if ( s>=0 ) return s;
						break;

					case 314 : 
						int LA189_92 = input.LA(1);
						 
						int index189_92 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_92);
						if ( s>=0 ) return s;
						break;

					case 315 : 
						int LA189_266 = input.LA(1);
						 
						int index189_266 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_266);
						if ( s>=0 ) return s;
						break;

					case 316 : 
						int LA189_381 = input.LA(1);
						 
						int index189_381 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_381);
						if ( s>=0 ) return s;
						break;

					case 317 : 
						int LA189_515 = input.LA(1);
						 
						int index189_515 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_515);
						if ( s>=0 ) return s;
						break;

					case 318 : 
						int LA189_751 = input.LA(1);
						 
						int index189_751 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_751);
						if ( s>=0 ) return s;
						break;

					case 319 : 
						int LA189_61 = input.LA(1);
						 
						int index189_61 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_61);
						if ( s>=0 ) return s;
						break;

					case 320 : 
						int LA189_93 = input.LA(1);
						 
						int index189_93 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_93);
						if ( s>=0 ) return s;
						break;

					case 321 : 
						int LA189_267 = input.LA(1);
						 
						int index189_267 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_267);
						if ( s>=0 ) return s;
						break;

					case 322 : 
						int LA189_382 = input.LA(1);
						 
						int index189_382 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_382);
						if ( s>=0 ) return s;
						break;

					case 323 : 
						int LA189_516 = input.LA(1);
						 
						int index189_516 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_516);
						if ( s>=0 ) return s;
						break;

					case 324 : 
						int LA189_647 = input.LA(1);
						 
						int index189_647 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_647);
						if ( s>=0 ) return s;
						break;

					case 325 : 
						int LA189_752 = input.LA(1);
						 
						int index189_752 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_752);
						if ( s>=0 ) return s;
						break;

					case 326 : 
						int LA189_590 = input.LA(1);
						 
						int index189_590 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_590);
						if ( s>=0 ) return s;
						break;

					case 327 : 
						int LA189_184 = input.LA(1);
						 
						int index189_184 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_184);
						if ( s>=0 ) return s;
						break;

					case 328 : 
						int LA189_591 = input.LA(1);
						 
						int index189_591 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_591);
						if ( s>=0 ) return s;
						break;

					case 329 : 
						int LA189_185 = input.LA(1);
						 
						int index189_185 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_185);
						if ( s>=0 ) return s;
						break;

					case 330 : 
						int LA189_575 = input.LA(1);
						 
						int index189_575 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_575);
						if ( s>=0 ) return s;
						break;

					case 331 : 
						int LA189_576 = input.LA(1);
						 
						int index189_576 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_576);
						if ( s>=0 ) return s;
						break;

					case 332 : 
						int LA189_272 = input.LA(1);
						 
						int index189_272 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_272);
						if ( s>=0 ) return s;
						break;

					case 333 : 
						int LA189_67 = input.LA(1);
						 
						int index189_67 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_67);
						if ( s>=0 ) return s;
						break;

					case 334 : 
						int LA189_98 = input.LA(1);
						 
						int index189_98 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_98);
						if ( s>=0 ) return s;
						break;

					case 335 : 
						int LA189_154 = input.LA(1);
						 
						int index189_154 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_154);
						if ( s>=0 ) return s;
						break;

					case 336 : 
						int LA189_156 = input.LA(1);
						 
						int index189_156 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_156);
						if ( s>=0 ) return s;
						break;

					case 337 : 
						int LA189_387 = input.LA(1);
						 
						int index189_387 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_387);
						if ( s>=0 ) return s;
						break;

					case 338 : 
						int LA189_757 = input.LA(1);
						 
						int index189_757 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_757);
						if ( s>=0 ) return s;
						break;

					case 339 : 
						int LA189_521 = input.LA(1);
						 
						int index189_521 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_521);
						if ( s>=0 ) return s;
						break;

					case 340 : 
						int LA189_652 = input.LA(1);
						 
						int index189_652 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_652);
						if ( s>=0 ) return s;
						break;

					case 341 : 
						int LA189_445 = input.LA(1);
						 
						int index189_445 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_445);
						if ( s>=0 ) return s;
						break;

					case 342 : 
						int LA189_731 = input.LA(1);
						 
						int index189_731 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_731);
						if ( s>=0 ) return s;
						break;

					case 343 : 
						int LA189_69 = input.LA(1);
						 
						int index189_69 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_69);
						if ( s>=0 ) return s;
						break;

					case 344 : 
						int LA189_99 = input.LA(1);
						 
						int index189_99 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_99);
						if ( s>=0 ) return s;
						break;

					case 345 : 
						int LA189_155 = input.LA(1);
						 
						int index189_155 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_155);
						if ( s>=0 ) return s;
						break;

					case 346 : 
						int LA189_157 = input.LA(1);
						 
						int index189_157 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_157);
						if ( s>=0 ) return s;
						break;

					case 347 : 
						int LA189_273 = input.LA(1);
						 
						int index189_273 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_273);
						if ( s>=0 ) return s;
						break;

					case 348 : 
						int LA189_388 = input.LA(1);
						 
						int index189_388 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_388);
						if ( s>=0 ) return s;
						break;

					case 349 : 
						int LA189_522 = input.LA(1);
						 
						int index189_522 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_522);
						if ( s>=0 ) return s;
						break;

					case 350 : 
						int LA189_758 = input.LA(1);
						 
						int index189_758 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_758);
						if ( s>=0 ) return s;
						break;

					case 351 : 
						int LA189_653 = input.LA(1);
						 
						int index189_653 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_653);
						if ( s>=0 ) return s;
						break;

					case 352 : 
						int LA189_732 = input.LA(1);
						 
						int index189_732 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_732);
						if ( s>=0 ) return s;
						break;

					case 353 : 
						int LA189_805 = input.LA(1);
						 
						int index189_805 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_805);
						if ( s>=0 ) return s;
						break;

					case 354 : 
						int LA189_806 = input.LA(1);
						 
						int index189_806 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_806);
						if ( s>=0 ) return s;
						break;

					case 355 : 
						int LA189_53 = input.LA(1);
						s = -1;
						if ( (LA189_53=='x') ) {s = 171;}
						else if ( (LA189_53=='X') ) {s = 172;}
						else if ( (LA189_53=='t') ) {s = 173;}
						else if ( (LA189_53=='0') ) {s = 174;}
						else if ( (LA189_53=='5'||LA189_53=='7') ) {s = 175;}
						else if ( (LA189_53=='T') ) {s = 176;}
						else if ( ((LA189_53 >= '\u0000' && LA189_53 <= '\t')||LA189_53=='\u000B'||(LA189_53 >= '\u000E' && LA189_53 <= '/')||(LA189_53 >= '1' && LA189_53 <= '3')||(LA189_53 >= '8' && LA189_53 <= 'S')||(LA189_53 >= 'U' && LA189_53 <= 'W')||(LA189_53 >= 'Y' && LA189_53 <= 's')||(LA189_53 >= 'u' && LA189_53 <= 'w')||(LA189_53 >= 'y' && LA189_53 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA189_53=='4'||LA189_53=='6') ) {s = 177;}
						if ( s>=0 ) return s;
						break;

					case 356 : 
						int LA189_554 = input.LA(1);
						 
						int index189_554 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_554);
						if ( s>=0 ) return s;
						break;

					case 357 : 
						int LA189_555 = input.LA(1);
						 
						int index189_555 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_555);
						if ( s>=0 ) return s;
						break;

					case 358 : 
						int LA189_226 = input.LA(1);
						 
						int index189_226 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_226);
						if ( s>=0 ) return s;
						break;

					case 359 : 
						int LA189_229 = input.LA(1);
						 
						int index189_229 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_229);
						if ( s>=0 ) return s;
						break;

					case 360 : 
						int LA189_471 = input.LA(1);
						 
						int index189_471 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_471);
						if ( s>=0 ) return s;
						break;

					case 361 : 
						int LA189_488 = input.LA(1);
						 
						int index189_488 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_488);
						if ( s>=0 ) return s;
						break;

					case 362 : 
						int LA189_743 = input.LA(1);
						 
						int index189_743 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_743);
						if ( s>=0 ) return s;
						break;

					case 363 : 
						int LA189_825 = input.LA(1);
						 
						int index189_825 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_825);
						if ( s>=0 ) return s;
						break;

					case 364 : 
						int LA189_823 = input.LA(1);
						 
						int index189_823 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_823);
						if ( s>=0 ) return s;
						break;

					case 365 : 
						int LA189_490 = input.LA(1);
						 
						int index189_490 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_490);
						if ( s>=0 ) return s;
						break;

					case 366 : 
						int LA189_837 = input.LA(1);
						 
						int index189_837 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_837);
						if ( s>=0 ) return s;
						break;

					case 367 : 
						int LA189_814 = input.LA(1);
						 
						int index189_814 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_814);
						if ( s>=0 ) return s;
						break;

					case 368 : 
						int LA189_741 = input.LA(1);
						 
						int index189_741 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_741);
						if ( s>=0 ) return s;
						break;

					case 369 : 
						int LA189_605 = input.LA(1);
						 
						int index189_605 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_605);
						if ( s>=0 ) return s;
						break;

					case 370 : 
						int LA189_621 = input.LA(1);
						 
						int index189_621 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_621);
						if ( s>=0 ) return s;
						break;

					case 371 : 
						int LA189_623 = input.LA(1);
						 
						int index189_623 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_623);
						if ( s>=0 ) return s;
						break;

					case 372 : 
						int LA189_727 = input.LA(1);
						 
						int index189_727 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_727);
						if ( s>=0 ) return s;
						break;

					case 373 : 
						int LA189_472 = input.LA(1);
						 
						int index189_472 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_472);
						if ( s>=0 ) return s;
						break;

					case 374 : 
						int LA189_228 = input.LA(1);
						 
						int index189_228 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_228);
						if ( s>=0 ) return s;
						break;

					case 375 : 
						int LA189_230 = input.LA(1);
						 
						int index189_230 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_230);
						if ( s>=0 ) return s;
						break;

					case 376 : 
						int LA189_744 = input.LA(1);
						 
						int index189_744 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_744);
						if ( s>=0 ) return s;
						break;

					case 377 : 
						int LA189_815 = input.LA(1);
						 
						int index189_815 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_815);
						if ( s>=0 ) return s;
						break;

					case 378 : 
						int LA189_489 = input.LA(1);
						 
						int index189_489 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_489);
						if ( s>=0 ) return s;
						break;

					case 379 : 
						int LA189_491 = input.LA(1);
						 
						int index189_491 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_491);
						if ( s>=0 ) return s;
						break;

					case 380 : 
						int LA189_606 = input.LA(1);
						 
						int index189_606 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_606);
						if ( s>=0 ) return s;
						break;

					case 381 : 
						int LA189_838 = input.LA(1);
						 
						int index189_838 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_838);
						if ( s>=0 ) return s;
						break;

					case 382 : 
						int LA189_742 = input.LA(1);
						 
						int index189_742 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_742);
						if ( s>=0 ) return s;
						break;

					case 383 : 
						int LA189_826 = input.LA(1);
						 
						int index189_826 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_826);
						if ( s>=0 ) return s;
						break;

					case 384 : 
						int LA189_622 = input.LA(1);
						 
						int index189_622 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_622);
						if ( s>=0 ) return s;
						break;

					case 385 : 
						int LA189_824 = input.LA(1);
						 
						int index189_824 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_824);
						if ( s>=0 ) return s;
						break;

					case 386 : 
						int LA189_624 = input.LA(1);
						 
						int index189_624 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_624);
						if ( s>=0 ) return s;
						break;

					case 387 : 
						int LA189_728 = input.LA(1);
						 
						int index189_728 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_728);
						if ( s>=0 ) return s;
						break;

					case 388 : 
						int LA189_789 = input.LA(1);
						 
						int index189_789 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_789);
						if ( s>=0 ) return s;
						break;

					case 389 : 
						int LA189_790 = input.LA(1);
						 
						int index189_790 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_790);
						if ( s>=0 ) return s;
						break;

					case 390 : 
						int LA189_428 = input.LA(1);
						 
						int index189_428 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_428);
						if ( s>=0 ) return s;
						break;

					case 391 : 
						int LA189_374 = input.LA(1);
						 
						int index189_374 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_374);
						if ( s>=0 ) return s;
						break;

					case 392 : 
						int LA189_47 = input.LA(1);
						 
						int index189_47 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_47);
						if ( s>=0 ) return s;
						break;

					case 393 : 
						int LA189_82 = input.LA(1);
						 
						int index189_82 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_82);
						if ( s>=0 ) return s;
						break;

					case 394 : 
						int LA189_262 = input.LA(1);
						 
						int index189_262 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_262);
						if ( s>=0 ) return s;
						break;

					case 395 : 
						int LA189_377 = input.LA(1);
						 
						int index189_377 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_377);
						if ( s>=0 ) return s;
						break;

					case 396 : 
						int LA189_511 = input.LA(1);
						 
						int index189_511 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_511);
						if ( s>=0 ) return s;
						break;

					case 397 : 
						int LA189_642 = input.LA(1);
						 
						int index189_642 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_642);
						if ( s>=0 ) return s;
						break;

					case 398 : 
						int LA189_747 = input.LA(1);
						 
						int index189_747 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_747);
						if ( s>=0 ) return s;
						break;

					case 399 : 
						int LA189_50 = input.LA(1);
						 
						int index189_50 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_50);
						if ( s>=0 ) return s;
						break;

					case 400 : 
						int LA189_84 = input.LA(1);
						 
						int index189_84 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_84);
						if ( s>=0 ) return s;
						break;

					case 401 : 
						int LA189_264 = input.LA(1);
						 
						int index189_264 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_264);
						if ( s>=0 ) return s;
						break;

					case 402 : 
						int LA189_379 = input.LA(1);
						 
						int index189_379 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_379);
						if ( s>=0 ) return s;
						break;

					case 403 : 
						int LA189_513 = input.LA(1);
						 
						int index189_513 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_513);
						if ( s>=0 ) return s;
						break;

					case 404 : 
						int LA189_644 = input.LA(1);
						 
						int index189_644 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_644);
						if ( s>=0 ) return s;
						break;

					case 405 : 
						int LA189_749 = input.LA(1);
						 
						int index189_749 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_749);
						if ( s>=0 ) return s;
						break;

					case 406 : 
						int LA189_662 = input.LA(1);
						 
						int index189_662 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_662);
						if ( s>=0 ) return s;
						break;

					case 407 : 
						int LA189_323 = input.LA(1);
						 
						int index189_323 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_323);
						if ( s>=0 ) return s;
						break;

					case 408 : 
						int LA189_409 = input.LA(1);
						 
						int index189_409 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_409);
						if ( s>=0 ) return s;
						break;

					case 409 : 
						int LA189_413 = input.LA(1);
						 
						int index189_413 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_413);
						if ( s>=0 ) return s;
						break;

					case 410 : 
						int LA189_665 = input.LA(1);
						 
						int index189_665 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_665);
						if ( s>=0 ) return s;
						break;

					case 411 : 
						int LA189_324 = input.LA(1);
						 
						int index189_324 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_324);
						if ( s>=0 ) return s;
						break;

					case 412 : 
						int LA189_410 = input.LA(1);
						 
						int index189_410 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_410);
						if ( s>=0 ) return s;
						break;

					case 413 : 
						int LA189_414 = input.LA(1);
						 
						int index189_414 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_414);
						if ( s>=0 ) return s;
						break;

					case 414 : 
						int LA189_691 = input.LA(1);
						 
						int index189_691 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_691);
						if ( s>=0 ) return s;
						break;

					case 415 : 
						int LA189_311 = input.LA(1);
						 
						int index189_311 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_311);
						if ( s>=0 ) return s;
						break;

					case 416 : 
						int LA189_570 = input.LA(1);
						 
						int index189_570 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_570);
						if ( s>=0 ) return s;
						break;

					case 417 : 
						int LA189_571 = input.LA(1);
						 
						int index189_571 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_571);
						if ( s>=0 ) return s;
						break;

					case 418 : 
						int LA189_300 = input.LA(1);
						 
						int index189_300 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_300);
						if ( s>=0 ) return s;
						break;

					case 419 : 
						int LA189_80 = input.LA(1);
						s = -1;
						if ( (LA189_80=='z') ) {s = 217;}
						else if ( (LA189_80=='Z') ) {s = 218;}
						else if ( ((LA189_80 >= '\u0000' && LA189_80 <= '\t')||LA189_80=='\u000B'||(LA189_80 >= '\u000E' && LA189_80 <= '/')||(LA189_80 >= '1' && LA189_80 <= '4')||LA189_80=='6'||(LA189_80 >= '8' && LA189_80 <= 'Y')||(LA189_80 >= '[' && LA189_80 <= 'y')||(LA189_80 >= '{' && LA189_80 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA189_80=='0') ) {s = 219;}
						else if ( (LA189_80=='5'||LA189_80=='7') ) {s = 220;}
						if ( s>=0 ) return s;
						break;

					case 420 : 
						int LA189_150 = input.LA(1);
						 
						int index189_150 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_150);
						if ( s>=0 ) return s;
						break;

					case 421 : 
						int LA189_143 = input.LA(1);
						 
						int index189_143 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_143);
						if ( s>=0 ) return s;
						break;

					case 422 : 
						int LA189_62 = input.LA(1);
						 
						int index189_62 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_62);
						if ( s>=0 ) return s;
						break;

					case 423 : 
						int LA189_753 = input.LA(1);
						 
						int index189_753 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_753);
						if ( s>=0 ) return s;
						break;

					case 424 : 
						int LA189_761 = input.LA(1);
						 
						int index189_761 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_761);
						if ( s>=0 ) return s;
						break;

					case 425 : 
						int LA189_94 = input.LA(1);
						 
						int index189_94 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_94);
						if ( s>=0 ) return s;
						break;

					case 426 : 
						int LA189_268 = input.LA(1);
						 
						int index189_268 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_268);
						if ( s>=0 ) return s;
						break;

					case 427 : 
						int LA189_276 = input.LA(1);
						 
						int index189_276 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_276);
						if ( s>=0 ) return s;
						break;

					case 428 : 
						int LA189_648 = input.LA(1);
						 
						int index189_648 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_648);
						if ( s>=0 ) return s;
						break;

					case 429 : 
						int LA189_383 = input.LA(1);
						 
						int index189_383 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_383);
						if ( s>=0 ) return s;
						break;

					case 430 : 
						int LA189_391 = input.LA(1);
						 
						int index189_391 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_391);
						if ( s>=0 ) return s;
						break;

					case 431 : 
						int LA189_517 = input.LA(1);
						 
						int index189_517 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_517);
						if ( s>=0 ) return s;
						break;

					case 432 : 
						int LA189_656 = input.LA(1);
						 
						int index189_656 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_656);
						if ( s>=0 ) return s;
						break;

					case 433 : 
						int LA189_525 = input.LA(1);
						 
						int index189_525 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_525);
						if ( s>=0 ) return s;
						break;

					case 434 : 
						int LA189_96 = input.LA(1);
						 
						int index189_96 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_96);
						if ( s>=0 ) return s;
						break;

					case 435 : 
						int LA189_65 = input.LA(1);
						 
						int index189_65 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_65);
						if ( s>=0 ) return s;
						break;

					case 436 : 
						int LA189_763 = input.LA(1);
						 
						int index189_763 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_763);
						if ( s>=0 ) return s;
						break;

					case 437 : 
						int LA189_144 = input.LA(1);
						 
						int index189_144 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_144);
						if ( s>=0 ) return s;
						break;

					case 438 : 
						int LA189_151 = input.LA(1);
						 
						int index189_151 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_151);
						if ( s>=0 ) return s;
						break;

					case 439 : 
						int LA189_270 = input.LA(1);
						 
						int index189_270 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_270);
						if ( s>=0 ) return s;
						break;

					case 440 : 
						int LA189_755 = input.LA(1);
						 
						int index189_755 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_755);
						if ( s>=0 ) return s;
						break;

					case 441 : 
						int LA189_527 = input.LA(1);
						 
						int index189_527 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_527);
						if ( s>=0 ) return s;
						break;

					case 442 : 
						int LA189_278 = input.LA(1);
						 
						int index189_278 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_278);
						if ( s>=0 ) return s;
						break;

					case 443 : 
						int LA189_385 = input.LA(1);
						 
						int index189_385 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_385);
						if ( s>=0 ) return s;
						break;

					case 444 : 
						int LA189_393 = input.LA(1);
						 
						int index189_393 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_393);
						if ( s>=0 ) return s;
						break;

					case 445 : 
						int LA189_519 = input.LA(1);
						 
						int index189_519 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_519);
						if ( s>=0 ) return s;
						break;

					case 446 : 
						int LA189_650 = input.LA(1);
						 
						int index189_650 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_650);
						if ( s>=0 ) return s;
						break;

					case 447 : 
						int LA189_658 = input.LA(1);
						 
						int index189_658 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_658);
						if ( s>=0 ) return s;
						break;

					case 448 : 
						int LA189_794 = input.LA(1);
						 
						int index189_794 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_794);
						if ( s>=0 ) return s;
						break;

					case 449 : 
						int LA189_795 = input.LA(1);
						 
						int index189_795 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_795);
						if ( s>=0 ) return s;
						break;

					case 450 : 
						int LA189_839 = input.LA(1);
						 
						int index189_839 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_839);
						if ( s>=0 ) return s;
						break;

					case 451 : 
						int LA189_840 = input.LA(1);
						 
						int index189_840 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_840);
						if ( s>=0 ) return s;
						break;

					case 452 : 
						int LA189_783 = input.LA(1);
						 
						int index189_783 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_783);
						if ( s>=0 ) return s;
						break;

					case 453 : 
						int LA189_784 = input.LA(1);
						 
						int index189_784 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_784);
						if ( s>=0 ) return s;
						break;

					case 454 : 
						int LA189_715 = input.LA(1);
						 
						int index189_715 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_715);
						if ( s>=0 ) return s;
						break;

					case 455 : 
						int LA189_716 = input.LA(1);
						 
						int index189_716 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_716);
						if ( s>=0 ) return s;
						break;

					case 456 : 
						int LA189_450 = input.LA(1);
						 
						int index189_450 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_450);
						if ( s>=0 ) return s;
						break;

					case 457 : 
						int LA189_451 = input.LA(1);
						 
						int index189_451 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_451);
						if ( s>=0 ) return s;
						break;

					case 458 : 
						int LA189_103 = input.LA(1);
						s = -1;
						if ( (LA189_103=='i') ) {s = 222;}
						else if ( (LA189_103=='I') ) {s = 223;}
						else if ( ((LA189_103 >= '\u0000' && LA189_103 <= '\t')||LA189_103=='\u000B'||(LA189_103 >= '\u000E' && LA189_103 <= '/')||(LA189_103 >= '1' && LA189_103 <= '3')||LA189_103=='5'||(LA189_103 >= '7' && LA189_103 <= 'H')||(LA189_103 >= 'J' && LA189_103 <= 'h')||(LA189_103 >= 'j' && LA189_103 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA189_103=='0') ) {s = 224;}
						else if ( (LA189_103=='4'||LA189_103=='6') ) {s = 225;}
						if ( s>=0 ) return s;
						break;

					case 459 : 
						int LA189_579 = input.LA(1);
						 
						int index189_579 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_579);
						if ( s>=0 ) return s;
						break;

					case 460 : 
						int LA189_639 = input.LA(1);
						 
						int index189_639 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_639);
						if ( s>=0 ) return s;
						break;

					case 461 : 
						int LA189_709 = input.LA(1);
						 
						int index189_709 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_709);
						if ( s>=0 ) return s;
						break;

					case 462 : 
						int LA189_710 = input.LA(1);
						 
						int index189_710 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_710);
						if ( s>=0 ) return s;
						break;

					case 463 : 
						int LA189_452 = input.LA(1);
						 
						int index189_452 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_452);
						if ( s>=0 ) return s;
						break;

					case 464 : 
						int LA189_201 = input.LA(1);
						 
						int index189_201 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_201);
						if ( s>=0 ) return s;
						break;

					case 465 : 
						int LA189_206 = input.LA(1);
						 
						int index189_206 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_206);
						if ( s>=0 ) return s;
						break;

					case 466 : 
						int LA189_411 = input.LA(1);
						 
						int index189_411 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_411);
						if ( s>=0 ) return s;
						break;

					case 467 : 
						int LA189_415 = input.LA(1);
						 
						int index189_415 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_415);
						if ( s>=0 ) return s;
						break;

					case 468 : 
						int LA189_711 = input.LA(1);
						 
						int index189_711 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_711);
						if ( s>=0 ) return s;
						break;

					case 469 : 
						int LA189_777 = input.LA(1);
						 
						int index189_777 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_777);
						if ( s>=0 ) return s;
						break;

					case 470 : 
						int LA189_680 = input.LA(1);
						 
						int index189_680 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_680);
						if ( s>=0 ) return s;
						break;

					case 471 : 
						int LA189_781 = input.LA(1);
						 
						int index189_781 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_781);
						if ( s>=0 ) return s;
						break;

					case 472 : 
						int LA189_802 = input.LA(1);
						 
						int index189_802 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_802);
						if ( s>=0 ) return s;
						break;

					case 473 : 
						int LA189_545 = input.LA(1);
						 
						int index189_545 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_545);
						if ( s>=0 ) return s;
						break;

					case 474 : 
						int LA189_549 = input.LA(1);
						 
						int index189_549 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_549);
						if ( s>=0 ) return s;
						break;

					case 475 : 
						int LA189_676 = input.LA(1);
						 
						int index189_676 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_676);
						if ( s>=0 ) return s;
						break;

					case 476 : 
						int LA189_586 = input.LA(1);
						 
						int index189_586 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_586);
						if ( s>=0 ) return s;
						break;

					case 477 : 
						int LA189_831 = input.LA(1);
						 
						int index189_831 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_831);
						if ( s>=0 ) return s;
						break;

					case 478 : 
						int LA189_203 = input.LA(1);
						 
						int index189_203 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_203);
						if ( s>=0 ) return s;
						break;

					case 479 : 
						int LA189_207 = input.LA(1);
						 
						int index189_207 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_207);
						if ( s>=0 ) return s;
						break;

					case 480 : 
						int LA189_674 = input.LA(1);
						 
						int index189_674 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_674);
						if ( s>=0 ) return s;
						break;

					case 481 : 
						int LA189_678 = input.LA(1);
						 
						int index189_678 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_678);
						if ( s>=0 ) return s;
						break;

					case 482 : 
						int LA189_412 = input.LA(1);
						 
						int index189_412 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_412);
						if ( s>=0 ) return s;
						break;

					case 483 : 
						int LA189_712 = input.LA(1);
						 
						int index189_712 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_712);
						if ( s>=0 ) return s;
						break;

					case 484 : 
						int LA189_803 = input.LA(1);
						 
						int index189_803 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_803);
						if ( s>=0 ) return s;
						break;

					case 485 : 
						int LA189_416 = input.LA(1);
						 
						int index189_416 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_416);
						if ( s>=0 ) return s;
						break;

					case 486 : 
						int LA189_453 = input.LA(1);
						 
						int index189_453 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_453);
						if ( s>=0 ) return s;
						break;

					case 487 : 
						int LA189_677 = input.LA(1);
						 
						int index189_677 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_677);
						if ( s>=0 ) return s;
						break;

					case 488 : 
						int LA189_546 = input.LA(1);
						 
						int index189_546 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_546);
						if ( s>=0 ) return s;
						break;

					case 489 : 
						int LA189_550 = input.LA(1);
						 
						int index189_550 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_550);
						if ( s>=0 ) return s;
						break;

					case 490 : 
						int LA189_778 = input.LA(1);
						 
						int index189_778 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_778);
						if ( s>=0 ) return s;
						break;

					case 491 : 
						int LA189_832 = input.LA(1);
						 
						int index189_832 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_832);
						if ( s>=0 ) return s;
						break;

					case 492 : 
						int LA189_587 = input.LA(1);
						 
						int index189_587 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_587);
						if ( s>=0 ) return s;
						break;

					case 493 : 
						int LA189_681 = input.LA(1);
						 
						int index189_681 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_681);
						if ( s>=0 ) return s;
						break;

					case 494 : 
						int LA189_782 = input.LA(1);
						 
						int index189_782 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_782);
						if ( s>=0 ) return s;
						break;

					case 495 : 
						int LA189_675 = input.LA(1);
						 
						int index189_675 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_675);
						if ( s>=0 ) return s;
						break;

					case 496 : 
						int LA189_679 = input.LA(1);
						 
						int index189_679 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_679);
						if ( s>=0 ) return s;
						break;

					case 497 : 
						int LA189_797 = input.LA(1);
						 
						int index189_797 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_797);
						if ( s>=0 ) return s;
						break;

					case 498 : 
						int LA189_791 = input.LA(1);
						 
						int index189_791 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_791);
						if ( s>=0 ) return s;
						break;

					case 499 : 
						int LA189_792 = input.LA(1);
						 
						int index189_792 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_792);
						if ( s>=0 ) return s;
						break;

					case 500 : 
						int LA189_562 = input.LA(1);
						 
						int index189_562 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_562);
						if ( s>=0 ) return s;
						break;

					case 501 : 
						int LA189_359 = input.LA(1);
						 
						int index189_359 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_359);
						if ( s>=0 ) return s;
						break;

					case 502 : 
						int LA189_48 = input.LA(1);
						s = -1;
						if ( (LA189_48=='m') ) {s = 163;}
						else if ( (LA189_48=='M') ) {s = 164;}
						else if ( (LA189_48=='x') ) {s = 165;}
						else if ( (LA189_48=='0') ) {s = 166;}
						else if ( (LA189_48=='4'||LA189_48=='6') ) {s = 167;}
						else if ( (LA189_48=='X') ) {s = 168;}
						else if ( ((LA189_48 >= '\u0000' && LA189_48 <= '\t')||LA189_48=='\u000B'||(LA189_48 >= '\u000E' && LA189_48 <= '/')||(LA189_48 >= '1' && LA189_48 <= '3')||(LA189_48 >= '8' && LA189_48 <= 'L')||(LA189_48 >= 'N' && LA189_48 <= 'W')||(LA189_48 >= 'Y' && LA189_48 <= 'l')||(LA189_48 >= 'n' && LA189_48 <= 'w')||(LA189_48 >= 'y' && LA189_48 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA189_48=='5'||LA189_48=='7') ) {s = 169;}
						if ( s>=0 ) return s;
						break;

					case 503 : 
						int LA189_438 = input.LA(1);
						 
						int index189_438 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index189_438);
						if ( s>=0 ) return s;
						break;
			}
			if (state.backtracking>0) {state.failed=true; return -1;}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 189, _s, input);
			error(nvae);
			throw nvae;
		}
	}

	static final String DFA205_eotS =
		"\1\uffff\1\70\1\74\1\76\1\100\1\102\2\uffff\1\106\1\110\4\uffff\1\112"+
		"\1\uffff\1\114\1\117\4\uffff\1\121\1\122\1\130\3\uffff\1\35\1\uffff\2"+
		"\35\1\uffff\1\143\1\uffff\2\35\3\uffff\20\71\42\uffff\3\35\1\uffff\5\35"+
		"\2\uffff\2\35\3\71\1\u0093\27\71\1\u00ad\2\35\1\uffff\12\35\3\71\1\uffff"+
		"\10\71\1\u00c7\20\71\2\uffff\15\35\3\71\1\u00e8\7\71\1\uffff\12\71\1\u00fd"+
		"\1\u00fe\1\71\1\u0100\2\71\15\35\1\uffff\2\71\1\uffff\1\u0111\1\u0112"+
		"\20\71\1\u0125\1\71\2\uffff\1\u0127\1\uffff\1\u0128\1\71\14\35\1\u0134"+
		"\1\71\2\uffff\16\71\1\u0146\2\71\1\u0149\1\uffff\1\u014a\2\uffff\1\71"+
		"\10\35\3\uffff\1\u0151\1\71\1\u0153\1\71\1\u0155\14\71\1\uffff\2\71\2"+
		"\uffff\1\u0166\5\35\1\uffff\1\71\1\uffff\1\71\1\uffff\1\71\1\u016d\1\u016f"+
		"\5\71\1\u0175\7\71\1\uffff\3\35\1\u017e\1\71\1\u0180\1\uffff\1\71\1\uffff"+
		"\1\71\1\u0184\3\71\1\uffff\2\71\1\u018a\4\71\1\35\1\uffff\1\71\1\uffff"+
		"\1\71\1\u0192\1\71\1\uffff\5\71\1\uffff\4\71\1\uffff\2\71\1\uffff\1\71"+
		"\1\u01a1\2\71\1\u01a4\1\u01a5\10\71\1\uffff\1\71\1\u01b0\2\uffff\1\u01b1"+
		"\1\u01b2\2\71\1\u01b5\3\71\1\u01b9\1\71\3\uffff\1\u01bb\1\71\1\uffff\3"+
		"\71\1\uffff\1\71\1\uffff\1\71\1\u01c2\4\71\1\uffff\1\u01c7\3\71\1\uffff"+
		"\2\71\1\u01cd\1\u01ce\1\71\2\uffff\1\u01d0\1\uffff";
	static final String DFA205_eofS =
		"\u01d1\uffff";
	static final String DFA205_minS =
		"\1\11\1\55\1\41\1\55\2\75\1\uffff\1\55\2\75\4\uffff\1\74\1\uffff\1\72"+
		"\1\52\4\uffff\1\56\1\55\1\11\3\uffff\1\117\1\uffff\2\122\1\0\1\55\1\uffff"+
		"\1\117\1\105\3\uffff\1\145\1\106\1\101\1\105\1\101\1\110\3\117\2\105\1"+
		"\115\1\101\1\105\1\101\1\124\42\uffff\1\124\2\114\1\0\1\114\1\122\1\60"+
		"\1\122\1\65\2\uffff\1\115\1\107\1\163\1\120\1\103\1\55\1\107\1\104\1\130"+
		"\1\115\1\101\3\116\1\120\1\124\1\106\1\107\1\124\1\117\1\105\1\124\1\122"+
		"\1\123\1\103\1\102\1\122\1\111\2\55\2\50\1\0\1\114\1\60\1\114\1\62\1\50"+
		"\1\60\1\65\1\122\1\101\1\105\1\164\1\117\1\114\1\uffff\1\105\2\111\1\105"+
		"\1\122\1\116\2\124\1\55\1\103\1\55\2\124\1\110\1\125\1\132\1\102\1\105"+
		"\1\117\1\105\1\110\1\125\1\116\1\114\1\122\2\uffff\1\50\1\60\1\50\1\103"+
		"\1\60\1\62\1\114\1\120\1\60\1\65\1\122\1\111\1\130\1\56\1\122\1\125\1"+
		"\55\1\101\1\116\2\123\1\124\1\105\1\55\1\uffff\1\124\1\103\1\117\1\55"+
		"\1\124\1\122\1\55\1\113\1\116\1\122\2\55\1\107\1\55\1\105\1\117\1\60\1"+
		"\103\2\50\1\60\1\62\1\114\1\122\2\65\1\122\1\116\1\120\1\uffff\1\124\1"+
		"\104\1\uffff\2\55\1\120\2\105\1\116\1\106\1\111\2\105\1\111\1\115\1\102"+
		"\1\55\1\116\1\104\1\111\1\104\1\55\1\106\2\uffff\1\55\1\uffff\1\55\1\117"+
		"\1\60\1\103\2\50\1\65\1\62\1\114\1\105\1\65\1\122\2\50\1\55\1\105\2\uffff"+
		"\1\101\1\124\1\122\1\124\1\101\1\117\1\106\1\116\1\107\1\55\1\117\1\111"+
		"\1\117\1\102\1\55\1\117\1\124\1\55\1\uffff\1\55\2\uffff\1\124\1\64\1\103"+
		"\2\50\1\62\1\114\1\106\1\122\3\uffff\1\55\1\103\3\55\1\103\1\116\2\124"+
		"\1\110\1\103\1\120\1\104\1\124\1\117\1\111\1\117\1\uffff\1\103\1\55\2"+
		"\uffff\1\55\1\103\2\50\1\114\1\111\1\uffff\1\105\1\uffff\1\123\1\uffff"+
		"\1\105\2\55\1\105\1\124\2\105\1\111\1\55\1\104\1\124\1\120\1\104\1\124"+
		"\1\125\1\113\1\uffff\2\50\1\130\1\55\1\124\1\55\1\uffff\1\103\1\uffff"+
		"\1\122\1\55\1\106\1\116\1\107\1\uffff\1\114\1\117\1\55\1\104\1\124\1\115"+
		"\1\105\1\50\1\uffff\1\131\1\uffff\1\117\1\55\1\103\1\uffff\2\124\1\110"+
		"\1\105\1\115\1\uffff\1\114\1\117\1\105\1\131\1\uffff\1\114\1\122\1\uffff"+
		"\1\117\1\55\1\105\1\124\2\55\1\105\1\115\1\116\1\106\1\105\1\116\1\122"+
		"\1\103\1\uffff\1\122\1\55\2\uffff\2\55\1\124\1\122\1\55\1\105\1\116\1"+
		"\117\1\55\1\103\3\uffff\1\55\1\101\1\uffff\1\122\1\105\1\122\1\uffff\1"+
		"\117\1\uffff\1\115\1\55\1\122\1\116\1\122\1\105\1\uffff\1\55\1\105\1\116"+
		"\1\123\1\uffff\1\122\1\105\2\55\1\122\2\uffff\1\55\1\uffff";
	static final String DFA205_maxS =
		"\2\uffff\1\75\1\uffff\2\75\1\uffff\1\uffff\2\75\4\uffff\1\76\1\uffff\1"+
		"\72\1\57\4\uffff\1\71\1\uffff\1\117\3\uffff\1\117\1\uffff\2\162\2\uffff"+
		"\1\uffff\1\117\1\105\3\uffff\1\145\1\116\1\101\1\111\1\101\1\117\1\125"+
		"\2\117\1\105\1\111\1\127\1\130\1\105\1\110\1\124\42\uffff\1\124\2\154"+
		"\1\uffff\1\154\1\162\1\67\1\162\1\65\2\uffff\1\115\1\107\1\163\1\120\1"+
		"\103\1\uffff\1\107\1\104\1\130\1\115\1\101\1\125\1\122\1\116\1\120\1\124"+
		"\1\106\1\107\1\124\1\117\1\105\1\124\1\122\1\123\1\103\1\102\1\122\1\111"+
		"\1\55\1\uffff\2\50\1\uffff\1\154\1\67\1\154\1\62\1\55\1\67\1\65\1\162"+
		"\1\101\1\105\1\164\1\117\1\114\1\uffff\1\105\2\111\1\105\1\122\1\116\2"+
		"\124\1\uffff\1\103\1\55\2\124\1\110\1\125\1\132\1\102\1\105\1\117\1\105"+
		"\1\110\1\125\1\116\1\114\1\122\2\uffff\1\50\1\66\1\50\1\143\1\67\1\62"+
		"\1\154\1\120\1\67\1\65\1\162\1\111\1\130\1\56\1\122\1\125\1\uffff\1\101"+
		"\1\116\2\123\1\124\1\105\1\55\1\uffff\1\124\1\122\1\117\1\55\1\124\1\122"+
		"\1\55\1\113\1\116\1\122\2\uffff\1\107\1\uffff\1\105\1\117\1\66\1\143\2"+
		"\50\1\67\1\62\1\154\1\122\1\67\1\65\1\162\1\116\1\120\1\uffff\1\124\1"+
		"\104\1\uffff\2\uffff\1\120\2\105\1\116\1\106\1\111\2\105\1\111\1\115\1"+
		"\124\1\55\1\116\1\104\1\111\1\104\1\uffff\1\106\2\uffff\1\uffff\1\uffff"+
		"\1\uffff\1\117\1\66\1\143\2\50\1\67\1\62\1\154\1\105\1\65\1\162\2\50\1"+
		"\uffff\1\105\2\uffff\1\101\1\124\1\122\1\124\1\101\1\117\1\106\1\116\1"+
		"\107\1\55\1\117\1\111\1\117\1\124\1\uffff\1\117\1\124\1\uffff\1\uffff"+
		"\1\uffff\2\uffff\1\124\1\66\1\143\2\50\1\62\1\154\1\106\1\162\3\uffff"+
		"\1\uffff\1\103\1\uffff\1\55\1\uffff\1\103\1\116\2\124\1\110\1\122\1\120"+
		"\1\104\1\124\1\117\1\111\1\117\1\uffff\1\103\1\55\2\uffff\1\uffff\1\143"+
		"\2\50\1\154\1\111\1\uffff\1\105\1\uffff\1\123\1\uffff\1\105\2\uffff\1"+
		"\105\1\124\2\105\1\111\1\uffff\1\104\1\124\1\120\1\104\1\124\1\125\1\113"+
		"\1\uffff\2\50\1\130\1\uffff\1\124\1\uffff\1\uffff\1\103\1\uffff\1\122"+
		"\1\uffff\1\106\1\116\1\107\1\uffff\1\114\1\117\1\uffff\1\104\1\124\1\115"+
		"\1\105\1\50\1\uffff\1\131\1\uffff\1\117\1\uffff\1\103\1\uffff\2\124\1"+
		"\110\1\105\1\115\1\uffff\1\114\1\117\1\105\1\131\1\uffff\1\114\1\122\1"+
		"\uffff\1\117\1\uffff\1\105\1\124\2\uffff\1\105\1\115\1\116\1\106\1\105"+
		"\1\116\1\122\1\103\1\uffff\1\122\1\uffff\2\uffff\2\uffff\1\124\1\122\1"+
		"\uffff\1\105\1\116\1\117\1\uffff\1\103\3\uffff\1\uffff\1\101\1\uffff\1"+
		"\122\1\105\1\122\1\uffff\1\117\1\uffff\1\115\1\uffff\1\122\1\116\1\122"+
		"\1\105\1\uffff\1\uffff\1\105\1\116\1\123\1\uffff\1\122\1\105\2\uffff\1"+
		"\122\2\uffff\1\uffff\1\uffff";
	static final String DFA205_acceptS =
		"\6\uffff\1\6\3\uffff\1\12\1\13\1\14\1\15\1\uffff\1\17\2\uffff\1\24\1\26"+
		"\1\27\1\30\3\uffff\1\43\1\46\1\47\1\uffff\1\51\4\uffff\1\135\2\uffff\1"+
		"\142\1\143\1\1\20\uffff\1\126\1\127\1\2\1\42\1\40\1\3\1\23\1\4\1\32\1"+
		"\5\1\33\1\7\1\130\1\10\1\25\1\41\1\11\1\36\1\16\1\21\1\20\1\144\1\145"+
		"\1\22\1\44\1\31\1\34\1\134\1\37\1\131\1\132\1\133\1\35\1\54\11\uffff\1"+
		"\52\1\53\56\uffff\1\115\31\uffff\1\50\1\136\30\uffff\1\120\35\uffff\1"+
		"\45\2\uffff\1\56\24\uffff\1\116\1\123\1\uffff\1\114\20\uffff\1\57\1\107"+
		"\22\uffff\1\113\1\uffff\1\112\1\124\11\uffff\1\140\1\141\1\55\21\uffff"+
		"\1\122\2\uffff\1\111\1\117\6\uffff\1\110\1\uffff\1\61\1\uffff\1\106\20"+
		"\uffff\1\125\6\uffff\1\121\1\uffff\1\65\5\uffff\1\76\10\uffff\1\60\1\uffff"+
		"\1\63\3\uffff\1\67\5\uffff\1\101\4\uffff\1\137\2\uffff\1\66\16\uffff\1"+
		"\72\2\uffff\1\77\1\100\12\uffff\1\74\1\102\1\103\2\uffff\1\62\3\uffff"+
		"\1\73\1\uffff\1\104\6\uffff\1\64\4\uffff\1\70\5\uffff\1\105\1\71\1\uffff"+
		"\1\75";
	static final String DFA205_specialS =
		"\40\uffff\1\2\74\uffff\1\1\47\uffff\1\0\u014b\uffff}>";
	static final String[] DFA205_transitionS = {
			"\1\45\1\46\2\uffff\1\46\22\uffff\1\45\1\30\1\32\1\41\1\7\1\27\1\31\1"+
			"\32\1\23\1\24\1\10\1\22\1\25\1\3\1\26\1\21\12\42\1\20\1\17\1\2\1\16\1"+
			"\11\1\uffff\1\1\3\35\1\43\11\35\1\34\3\35\1\44\2\35\1\37\5\35\1\14\1"+
			"\40\1\15\1\6\1\35\1\33\24\35\1\36\5\35\1\12\1\5\1\13\1\4\1\uffff\uff80"+
			"\35",
			"\1\63\2\uffff\12\71\6\uffff\1\47\1\67\1\60\1\55\1\65\1\64\1\56\2\71"+
			"\1\51\2\71\1\61\1\53\1\54\1\71\1\52\1\71\1\62\1\71\1\57\2\71\1\66\3\71"+
			"\1\uffff\1\71\2\uffff\1\71\1\uffff\21\71\1\50\10\71\5\uffff\uff80\71",
			"\1\72\33\uffff\1\73",
			"\1\75\23\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff\32\35\5\uffff"+
			"\uff80\35",
			"\1\77",
			"\1\101",
			"",
			"\1\104\2\uffff\12\104\3\uffff\1\103\3\uffff\32\104\1\uffff\1\104\2\uffff"+
			"\1\104\1\uffff\32\104\5\uffff\uff80\104",
			"\1\105",
			"\1\107",
			"",
			"",
			"",
			"",
			"\1\73\1\111\1\107",
			"",
			"\1\113",
			"\1\115\4\uffff\1\116",
			"",
			"",
			"",
			"",
			"\1\120\1\uffff\12\42",
			"\1\123\2\uffff\12\123\7\uffff\32\123\1\uffff\1\123\2\uffff\1\123\1\uffff"+
			"\32\123\5\uffff\uff80\123",
			"\1\131\26\uffff\1\131\16\uffff\1\131\15\uffff\1\124\6\uffff\1\125\2"+
			"\uffff\1\127\1\uffff\1\131\5\uffff\1\126",
			"",
			"",
			"",
			"\1\132",
			"",
			"\1\134\11\uffff\1\135\25\uffff\1\133",
			"\1\136\11\uffff\1\135\25\uffff\1\133",
			"\12\35\1\uffff\1\35\2\uffff\42\35\1\140\4\35\1\142\1\35\1\142\35\35"+
			"\1\141\37\35\1\137\uff8a\35",
			"\1\144\2\uffff\12\144\7\uffff\32\144\1\uffff\1\144\2\uffff\1\144\1\uffff"+
			"\32\144\5\uffff\uff80\144",
			"",
			"\1\145",
			"\1\146",
			"",
			"",
			"",
			"\1\147",
			"\1\152\6\uffff\1\150\1\151",
			"\1\153",
			"\1\154\3\uffff\1\155",
			"\1\156",
			"\1\157\6\uffff\1\160",
			"\1\161\5\uffff\1\162",
			"\1\163",
			"\1\164",
			"\1\165",
			"\1\167\3\uffff\1\166",
			"\1\170\11\uffff\1\171",
			"\1\175\12\uffff\1\174\5\uffff\1\173\5\uffff\1\172",
			"\1\176",
			"\1\177\6\uffff\1\u0080",
			"\1\u0081",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\u0082",
			"\1\u0084\17\uffff\1\u0085\17\uffff\1\u0083",
			"\1\u0084\17\uffff\1\u0085\17\uffff\1\u0083",
			"\12\35\1\uffff\1\35\2\uffff\42\35\1\u0087\4\35\1\u0089\1\35\1\u0089"+
			"\32\35\1\u0088\37\35\1\u0086\uff8d\35",
			"\1\u008a\17\uffff\1\u0085\17\uffff\1\u0083",
			"\1\134\11\uffff\1\135\25\uffff\1\133",
			"\1\u008b\4\uffff\1\u008c\1\uffff\1\u008c",
			"\1\134\11\uffff\1\135\25\uffff\1\133",
			"\1\u008d",
			"",
			"",
			"\1\u008e",
			"\1\u008f",
			"\1\u0090",
			"\1\u0091",
			"\1\u0092",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u0094",
			"\1\u0095",
			"\1\u0096",
			"\1\u0097",
			"\1\u0098",
			"\1\u009a\6\uffff\1\u0099",
			"\1\u009b\3\uffff\1\u009c",
			"\1\u009d",
			"\1\u009e",
			"\1\u009f",
			"\1\u00a0",
			"\1\u00a1",
			"\1\u00a2",
			"\1\u00a3",
			"\1\u00a4",
			"\1\u00a5",
			"\1\u00a6",
			"\1\u00a7",
			"\1\u00a8",
			"\1\u00a9",
			"\1\u00aa",
			"\1\u00ab",
			"\1\u00ac",
			"\1\35\2\uffff\12\35\7\uffff\32\35\1\uffff\1\35\2\uffff\1\35\1\uffff"+
			"\32\35\5\uffff\uff80\35",
			"\1\u00ae",
			"\1\u00ae",
			"\12\35\1\uffff\1\35\2\uffff\42\35\1\u00b0\3\35\1\u00b2\1\35\1\u00b2"+
			"\25\35\1\u00b1\37\35\1\u00af\uff93\35",
			"\1\u0084\17\uffff\1\u0085\17\uffff\1\u0083",
			"\1\u00b3\4\uffff\1\u00b4\1\uffff\1\u00b4",
			"\1\u0084\17\uffff\1\u0085\17\uffff\1\u0083",
			"\1\u00b5",
			"\1\u00ae\4\uffff\1\u00b6",
			"\1\u00b7\4\uffff\1\u00b8\1\uffff\1\u00b8",
			"\1\u00b9",
			"\1\134\11\uffff\1\135\25\uffff\1\133",
			"\1\u00ba",
			"\1\u00bb",
			"\1\u00bc",
			"\1\u00bd",
			"\1\u00be",
			"",
			"\1\u00bf",
			"\1\u00c0",
			"\1\u00c1",
			"\1\u00c2",
			"\1\u00c3",
			"\1\u00c4",
			"\1\u00c5",
			"\1\u00c6",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u00c8",
			"\1\u00c9",
			"\1\u00ca",
			"\1\u00cb",
			"\1\u00cc",
			"\1\u00cd",
			"\1\u00ce",
			"\1\u00cf",
			"\1\u00d0",
			"\1\u00d1",
			"\1\u00d2",
			"\1\u00d3",
			"\1\u00d4",
			"\1\u00d5",
			"\1\u00d6",
			"\1\u00d7",
			"",
			"",
			"\1\u00ae",
			"\1\u00d8\3\uffff\1\u00d9\1\uffff\1\u00d9",
			"\1\u00ae",
			"\1\u00db\37\uffff\1\u00da",
			"\1\u00dc\4\uffff\1\u00dd\1\uffff\1\u00dd",
			"\1\u00de",
			"\1\u0084\17\uffff\1\u0085\17\uffff\1\u0083",
			"\1\u00df",
			"\1\u00e0\4\uffff\1\u00e1\1\uffff\1\u00e1",
			"\1\u00e2",
			"\1\134\11\uffff\1\135\25\uffff\1\133",
			"\1\u00e3",
			"\1\u00e4",
			"\1\u00e5",
			"\1\u00e6",
			"\1\u00e7",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u00e9",
			"\1\u00ea",
			"\1\u00eb",
			"\1\u00ec",
			"\1\u00ed",
			"\1\u00ee",
			"\1\u00ef",
			"",
			"\1\u00f0",
			"\1\u00f2\10\uffff\1\u00f1\5\uffff\1\u00f3",
			"\1\u00f4",
			"\1\u00f5",
			"\1\u00f6",
			"\1\u00f7",
			"\1\u00f8",
			"\1\u00f9",
			"\1\u00fa",
			"\1\u00fb",
			"\1\71\2\uffff\12\71\7\uffff\10\71\1\u00fc\21\71\1\uffff\1\71\2\uffff"+
			"\1\71\1\uffff\32\71\5\uffff\uff80\71",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u00ff",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u0101",
			"\1\u0102",
			"\1\u0103\3\uffff\1\u0104\1\uffff\1\u0104",
			"\1\u0106\37\uffff\1\u0105",
			"\1\u00ae",
			"\1\u00ae",
			"\1\u0107\4\uffff\1\u0108\1\uffff\1\u0108",
			"\1\u0109",
			"\1\u0084\17\uffff\1\u0085\17\uffff\1\u0083",
			"\1\u010a",
			"\1\u010b\1\uffff\1\u010b",
			"\1\u010c",
			"\1\134\11\uffff\1\135\25\uffff\1\133",
			"\1\u010d",
			"\1\u010e",
			"",
			"\1\u010f",
			"\1\u0110",
			"",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u0113",
			"\1\u0114",
			"\1\u0115",
			"\1\u0116",
			"\1\u0117",
			"\1\u0118",
			"\1\u0119",
			"\1\u011a",
			"\1\u011b",
			"\1\u011c",
			"\1\u011f\12\uffff\1\u011e\6\uffff\1\u011d",
			"\1\u0120",
			"\1\u0121",
			"\1\u0122",
			"\1\u0123",
			"\1\u0124",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u0126",
			"",
			"",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u0129",
			"\1\u012a\3\uffff\1\u012b\1\uffff\1\u012b",
			"\1\u012d\37\uffff\1\u012c",
			"\1\u00ae",
			"\1\u00ae",
			"\1\u012e\1\uffff\1\u012e",
			"\1\u012f",
			"\1\u0084\17\uffff\1\u0085\17\uffff\1\u0083",
			"\1\u0130",
			"\1\u0131",
			"\1\134\11\uffff\1\135\25\uffff\1\133",
			"\1\u0132",
			"\1\u0133",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u0135",
			"",
			"",
			"\1\u0136",
			"\1\u0137",
			"\1\u0138",
			"\1\u0139",
			"\1\u013a",
			"\1\u013b",
			"\1\u013c",
			"\1\u013d",
			"\1\u013e",
			"\1\u013f",
			"\1\u0140",
			"\1\u0141",
			"\1\u0142",
			"\1\u0145\12\uffff\1\u0144\6\uffff\1\u0143",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u0147",
			"\1\u0148",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"",
			"",
			"\1\u014b",
			"\1\u014c\1\uffff\1\u014c",
			"\1\u014e\37\uffff\1\u014d",
			"\1\u00ae",
			"\1\u00ae",
			"\1\u014f",
			"\1\u0084\17\uffff\1\u0085\17\uffff\1\u0083",
			"\1\u0150",
			"\1\134\11\uffff\1\135\25\uffff\1\133",
			"",
			"",
			"",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u0152",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u0154",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u0156",
			"\1\u0157",
			"\1\u0158",
			"\1\u0159",
			"\1\u015a",
			"\1\u015c\10\uffff\1\u015b\5\uffff\1\u015d",
			"\1\u015e",
			"\1\u015f",
			"\1\u0160",
			"\1\u0161",
			"\1\u0162",
			"\1\u0163",
			"",
			"\1\u0164",
			"\1\u0165",
			"",
			"",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u0168\37\uffff\1\u0167",
			"\1\u00ae",
			"\1\u00ae",
			"\1\u0084\17\uffff\1\u0085\17\uffff\1\u0083",
			"\1\u0169",
			"",
			"\1\u016a",
			"",
			"\1\u016b",
			"",
			"\1\u016c",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u016e\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u0170",
			"\1\u0171",
			"\1\u0172",
			"\1\u0173",
			"\1\u0174",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u0176",
			"\1\u0177",
			"\1\u0178",
			"\1\u0179",
			"\1\u017a",
			"\1\u017b",
			"\1\u017c",
			"",
			"\1\u00ae",
			"\1\u00ae",
			"\1\u017d",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u017f",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"",
			"\1\u0181",
			"",
			"\1\u0182",
			"\1\u0183\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u0185",
			"\1\u0186",
			"\1\u0187",
			"",
			"\1\u0188",
			"\1\u0189",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u018b",
			"\1\u018c",
			"\1\u018d",
			"\1\u018e",
			"\1\u018f",
			"",
			"\1\u0190",
			"",
			"\1\u0191",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u0193",
			"",
			"\1\u0194",
			"\1\u0195",
			"\1\u0196",
			"\1\u0197",
			"\1\u0198",
			"",
			"\1\u0199",
			"\1\u019a",
			"\1\u019b",
			"\1\u019c",
			"",
			"\1\u019d",
			"\1\u019e",
			"",
			"\1\u019f",
			"\1\u01a0\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u01a2",
			"\1\u01a3",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u01a6",
			"\1\u01a7",
			"\1\u01a8",
			"\1\u01a9",
			"\1\u01aa",
			"\1\u01ab",
			"\1\u01ac",
			"\1\u01ad",
			"",
			"\1\u01ae",
			"\1\u01af\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"",
			"",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u01b3",
			"\1\u01b4",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u01b6",
			"\1\u01b7",
			"\1\u01b8",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u01ba",
			"",
			"",
			"",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u01bc",
			"",
			"\1\u01bd",
			"\1\u01be",
			"\1\u01bf",
			"",
			"\1\u01c0",
			"",
			"\1\u01c1",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u01c3",
			"\1\u01c4",
			"\1\u01c5",
			"\1\u01c6",
			"",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u01c8",
			"\1\u01c9",
			"\1\u01ca",
			"",
			"\1\u01cb",
			"\1\u01cc",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			"\1\u01cf",
			"",
			"",
			"\1\71\2\uffff\12\71\7\uffff\32\71\1\uffff\1\71\2\uffff\1\71\1\uffff"+
			"\32\71\5\uffff\uff80\71",
			""
	};

	static final short[] DFA205_eot = DFA.unpackEncodedString(DFA205_eotS);
	static final short[] DFA205_eof = DFA.unpackEncodedString(DFA205_eofS);
	static final char[] DFA205_min = DFA.unpackEncodedStringToUnsignedChars(DFA205_minS);
	static final char[] DFA205_max = DFA.unpackEncodedStringToUnsignedChars(DFA205_maxS);
	static final short[] DFA205_accept = DFA.unpackEncodedString(DFA205_acceptS);
	static final short[] DFA205_special = DFA.unpackEncodedString(DFA205_specialS);
	static final short[][] DFA205_transition;

	static {
		int numStates = DFA205_transitionS.length;
		DFA205_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA205_transition[i] = DFA.unpackEncodedString(DFA205_transitionS[i]);
		}
	}

	protected class DFA205 extends DFA {

		public DFA205(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 205;
			this.eot = DFA205_eot;
			this.eof = DFA205_eof;
			this.min = DFA205_min;
			this.max = DFA205_max;
			this.accept = DFA205_accept;
			this.special = DFA205_special;
			this.transition = DFA205_transition;
		}
		@Override
		public String getDescription() {
			return "1:1: Tokens : ( GEN | CDO | CDC | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS | GREATER | LBRACE | RBRACE | LBRACKET | RBRACKET | OPEQ | SEMI | COLON | DCOLON | SOLIDUS | MINUS | PLUS | STAR | LPAREN | RPAREN | COMMA | DOT | TILDE | PIPE | PERCENTAGE_SYMBOL | EXCLAMATION_MARK | CP_EQ | CP_NOT_EQ | LESS | GREATER_OR_EQ | LESS_OR_EQ | LESS_AND | CP_DOTS | LESS_REST | STRING | LESS_JS_STRING | NOT | IDENT | HASH_SYMBOL | HASH | IMPORTANT_SYM | IMPORT_SYM | PAGE_SYM | MEDIA_SYM | NAMESPACE_SYM | CHARSET_SYM | COUNTER_STYLE_SYM | FONT_FACE_SYM | TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM | MOZ_DOCUMENT_SYM | WEBKIT_KEYFRAMES_SYM | SASS_CONTENT | SASS_MIXIN | SASS_INCLUDE | SASS_EXTEND | SASS_DEBUG | SASS_ERROR | SASS_WARN | SASS_IF | SASS_ELSE | SASS_ELSEIF | SASS_FOR | SASS_FUNCTION | SASS_RETURN | SASS_EACH | SASS_WHILE | SASS_AT_ROOT | AT_SIGN | AT_IDENT | SASS_VAR | SASS_DEFAULT | SASS_OPTIONAL | SASS_GLOBAL | SASS_EXTEND_ONLY_SELECTOR | NUMBER | URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP | WS | NL | COMMENT | LINE_COMMENT );";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			IntStream input = _input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA205_133 = input.LA(1);
						s = -1;
						if ( (LA205_133=='l') ) {s = 175;}
						else if ( (LA205_133=='0') ) {s = 176;}
						else if ( (LA205_133=='L') ) {s = 177;}
						else if ( ((LA205_133 >= '\u0000' && LA205_133 <= '\t')||LA205_133=='\u000B'||(LA205_133 >= '\u000E' && LA205_133 <= '/')||(LA205_133 >= '1' && LA205_133 <= '3')||LA205_133=='5'||(LA205_133 >= '7' && LA205_133 <= 'K')||(LA205_133 >= 'M' && LA205_133 <= 'k')||(LA205_133 >= 'm' && LA205_133 <= '\uFFFF')) ) {s = 29;}
						else if ( (LA205_133=='4'||LA205_133=='6') ) {s = 178;}
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA205_93 = input.LA(1);
						s = -1;
						if ( (LA205_93=='r') ) {s = 134;}
						else if ( (LA205_93=='0') ) {s = 135;}
						else if ( (LA205_93=='R') ) {s = 136;}
						else if ( ((LA205_93 >= '\u0000' && LA205_93 <= '\t')||LA205_93=='\u000B'||(LA205_93 >= '\u000E' && LA205_93 <= '/')||(LA205_93 >= '1' && LA205_93 <= '4')||LA205_93=='6'||(LA205_93 >= '8' && LA205_93 <= 'Q')||(LA205_93 >= 'S' && LA205_93 <= 'q')||(LA205_93 >= 's' && LA205_93 <= '\uFFFF')) ) {s = 29;}
						else if ( (LA205_93=='5'||LA205_93=='7') ) {s = 137;}
						if ( s>=0 ) return s;
						break;

					case 2 : 
						int LA205_32 = input.LA(1);
						s = -1;
						if ( (LA205_32=='u') ) {s = 95;}
						else if ( (LA205_32=='0') ) {s = 96;}
						else if ( (LA205_32=='U') ) {s = 97;}
						else if ( ((LA205_32 >= '\u0000' && LA205_32 <= '\t')||LA205_32=='\u000B'||(LA205_32 >= '\u000E' && LA205_32 <= '/')||(LA205_32 >= '1' && LA205_32 <= '4')||LA205_32=='6'||(LA205_32 >= '8' && LA205_32 <= 'T')||(LA205_32 >= 'V' && LA205_32 <= 't')||(LA205_32 >= 'v' && LA205_32 <= '\uFFFF')) ) {s = 29;}
						else if ( (LA205_32=='5'||LA205_32=='7') ) {s = 98;}
						if ( s>=0 ) return s;
						break;
			}
			if (state.backtracking>0) {state.failed=true; return -1;}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 205, _s, input);
			error(nvae);
			throw nvae;
		}
	}

}
