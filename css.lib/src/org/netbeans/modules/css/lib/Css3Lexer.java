// $ANTLR 3.5.1 /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2013-12-12 14:28:20

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
	public static final int AND=5;
	public static final int ANGLE=6;
	public static final int AT_IDENT=7;
	public static final int AT_SIGN=8;
	public static final int B=9;
	public static final int BEGINS=10;
	public static final int BOTTOMCENTER_SYM=11;
	public static final int BOTTOMLEFTCORNER_SYM=12;
	public static final int BOTTOMLEFT_SYM=13;
	public static final int BOTTOMRIGHTCORNER_SYM=14;
	public static final int BOTTOMRIGHT_SYM=15;
	public static final int C=16;
	public static final int CDC=17;
	public static final int CDO=18;
	public static final int CHARSET_SYM=19;
	public static final int COLON=20;
	public static final int COMMA=21;
	public static final int COMMENT=22;
	public static final int CONTAINS=23;
	public static final int COUNTER_STYLE_SYM=24;
	public static final int CP_DOTS=25;
	public static final int CP_EQ=26;
	public static final int CP_NOT_EQ=27;
	public static final int D=28;
	public static final int DASHMATCH=29;
	public static final int DCOLON=30;
	public static final int DIMENSION=31;
	public static final int DOT=32;
	public static final int E=33;
	public static final int EMS=34;
	public static final int ENDS=35;
	public static final int ESCAPE=36;
	public static final int EXCLAMATION_MARK=37;
	public static final int EXS=38;
	public static final int F=39;
	public static final int FONT_FACE_SYM=40;
	public static final int FREQ=41;
	public static final int G=42;
	public static final int GEN=43;
	public static final int GREATER=44;
	public static final int GREATER_OR_EQ=45;
	public static final int H=46;
	public static final int HASH=47;
	public static final int HASH_SYMBOL=48;
	public static final int HEXCHAR=49;
	public static final int I=50;
	public static final int IDENT=51;
	public static final int IMPORTANT_SYM=52;
	public static final int IMPORT_SYM=53;
	public static final int INCLUDES=54;
	public static final int INVALID=55;
	public static final int J=56;
	public static final int K=57;
	public static final int L=58;
	public static final int LBRACE=59;
	public static final int LBRACKET=60;
	public static final int LEFTBOTTOM_SYM=61;
	public static final int LEFTMIDDLE_SYM=62;
	public static final int LEFTTOP_SYM=63;
	public static final int LENGTH=64;
	public static final int LESS=65;
	public static final int LESS_AND=66;
	public static final int LESS_JS_STRING=67;
	public static final int LESS_OR_EQ=68;
	public static final int LESS_REST=69;
	public static final int LESS_WHEN=70;
	public static final int LINE_COMMENT=71;
	public static final int LPAREN=72;
	public static final int M=73;
	public static final int MEDIA_SYM=74;
	public static final int MINUS=75;
	public static final int MOZ_DOCUMENT_SYM=76;
	public static final int MOZ_DOMAIN=77;
	public static final int MOZ_REGEXP=78;
	public static final int MOZ_URL_PREFIX=79;
	public static final int N=80;
	public static final int NAME=81;
	public static final int NAMESPACE_SYM=82;
	public static final int NL=83;
	public static final int NMCHAR=84;
	public static final int NMSTART=85;
	public static final int NONASCII=86;
	public static final int NOT=87;
	public static final int NUMBER=88;
	public static final int O=89;
	public static final int ONLY=90;
	public static final int OPEQ=91;
	public static final int OR=92;
	public static final int P=93;
	public static final int PAGE_SYM=94;
	public static final int PERCENTAGE=95;
	public static final int PERCENTAGE_SYMBOL=96;
	public static final int PIPE=97;
	public static final int PLUS=98;
	public static final int Q=99;
	public static final int R=100;
	public static final int RBRACE=101;
	public static final int RBRACKET=102;
	public static final int REM=103;
	public static final int RESOLUTION=104;
	public static final int RIGHTBOTTOM_SYM=105;
	public static final int RIGHTMIDDLE_SYM=106;
	public static final int RIGHTTOP_SYM=107;
	public static final int RPAREN=108;
	public static final int S=109;
	public static final int SASS_CONTENT=110;
	public static final int SASS_DEBUG=111;
	public static final int SASS_DEFAULT=112;
	public static final int SASS_EACH=113;
	public static final int SASS_ELSE=114;
	public static final int SASS_EXTEND=115;
	public static final int SASS_EXTEND_ONLY_SELECTOR=116;
	public static final int SASS_FOR=117;
	public static final int SASS_FUNCTION=118;
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
	@Override public String getGrammarFileName() { return "/home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g"; }

	// $ANTLR start "GEN"
	public final void mGEN() throws RecognitionException {
		try {
			int _type = GEN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1362:25: ( '@@@' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1362:27: '@@@'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1364:25: ( ( 'a' .. 'f' | 'A' .. 'F' | '0' .. '9' ) )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1366:25: ( '\\u0080' .. '\\uFFFF' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1368:25: ( '\\\\' HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )? ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1368:27: '\\\\' HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )? ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
			{
			match('\\'); if (state.failed) return;
			mHEXCHAR(); if (state.failed) return;

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1369:33: ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )?
			int alt5=2;
			int LA5_0 = input.LA(1);
			if ( ((LA5_0 >= '0' && LA5_0 <= '9')||(LA5_0 >= 'A' && LA5_0 <= 'F')||(LA5_0 >= 'a' && LA5_0 <= 'f')) ) {
				alt5=1;
			}
			switch (alt5) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1369:34: HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )?
					{
					mHEXCHAR(); if (state.failed) return;

					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1370:37: ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )?
					int alt4=2;
					int LA4_0 = input.LA(1);
					if ( ((LA4_0 >= '0' && LA4_0 <= '9')||(LA4_0 >= 'A' && LA4_0 <= 'F')||(LA4_0 >= 'a' && LA4_0 <= 'f')) ) {
						alt4=1;
					}
					switch (alt4) {
						case 1 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1370:38: HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )?
							{
							mHEXCHAR(); if (state.failed) return;

							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1371:41: ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )?
							int alt3=2;
							int LA3_0 = input.LA(1);
							if ( ((LA3_0 >= '0' && LA3_0 <= '9')||(LA3_0 >= 'A' && LA3_0 <= 'F')||(LA3_0 >= 'a' && LA3_0 <= 'f')) ) {
								alt3=1;
							}
							switch (alt3) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1371:42: HEXCHAR ( HEXCHAR ( HEXCHAR )? )?
									{
									mHEXCHAR(); if (state.failed) return;

									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1372:45: ( HEXCHAR ( HEXCHAR )? )?
									int alt2=2;
									int LA2_0 = input.LA(1);
									if ( ((LA2_0 >= '0' && LA2_0 <= '9')||(LA2_0 >= 'A' && LA2_0 <= 'F')||(LA2_0 >= 'a' && LA2_0 <= 'f')) ) {
										alt2=1;
									}
									switch (alt2) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1372:46: HEXCHAR ( HEXCHAR )?
											{
											mHEXCHAR(); if (state.failed) return;

											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1372:54: ( HEXCHAR )?
											int alt1=2;
											int LA1_0 = input.LA(1);
											if ( ((LA1_0 >= '0' && LA1_0 <= '9')||(LA1_0 >= 'A' && LA1_0 <= 'F')||(LA1_0 >= 'a' && LA1_0 <= 'f')) ) {
												alt1=1;
											}
											switch (alt1) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
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

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1376:33: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
			loop6:
			while (true) {
				int alt6=2;
				int LA6_0 = input.LA(1);
				if ( ((LA6_0 >= '\t' && LA6_0 <= '\n')||(LA6_0 >= '\f' && LA6_0 <= '\r')||LA6_0==' ') ) {
					alt6=1;
				}

				switch (alt6) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1378:25: ( UNICODE | '\\\\' ~ ( '\\r' | '\\n' | '\\f' | HEXCHAR ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1378:27: UNICODE
					{
					mUNICODE(); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1378:37: '\\\\' ~ ( '\\r' | '\\n' | '\\f' | HEXCHAR )
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1380:25: ( '_' | 'a' .. 'z' | 'A' .. 'Z' | NONASCII | ESCAPE )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1380:27: '_'
					{
					match('_'); if (state.failed) return;
					}
					break;
				case 2 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1381:27: 'a' .. 'z'
					{
					matchRange('a','z'); if (state.failed) return;
					}
					break;
				case 3 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1382:27: 'A' .. 'Z'
					{
					matchRange('A','Z'); if (state.failed) return;
					}
					break;
				case 4 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1383:27: NONASCII
					{
					mNONASCII(); if (state.failed) return;

					}
					break;
				case 5 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1384:27: ESCAPE
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1387:25: ( '_' | 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '-' | NONASCII | ESCAPE )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1387:27: '_'
					{
					match('_'); if (state.failed) return;
					}
					break;
				case 2 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1388:27: 'a' .. 'z'
					{
					matchRange('a','z'); if (state.failed) return;
					}
					break;
				case 3 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1389:27: 'A' .. 'Z'
					{
					matchRange('A','Z'); if (state.failed) return;
					}
					break;
				case 4 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1390:27: '0' .. '9'
					{
					matchRange('0','9'); if (state.failed) return;
					}
					break;
				case 5 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1391:27: '-'
					{
					match('-'); if (state.failed) return;
					}
					break;
				case 6 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1392:27: NONASCII
					{
					mNONASCII(); if (state.failed) return;

					}
					break;
				case 7 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1393:27: ESCAPE
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1396:25: ( ( NMCHAR )+ )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1396:27: ( NMCHAR )+
			{
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1396:27: ( NMCHAR )+
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1396:27: NMCHAR
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1398:25: ( ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | '@' | '|' | NMCHAR )* )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1398:27: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | '@' | '|' | NMCHAR )*
			{
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1398:27: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | '?' | '=' | ';' | ',' | '+' | '@' | '|' | NMCHAR )*
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1399:31: '['
					{
					match('['); if (state.failed) return;
					}
					break;
				case 2 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1399:35: '!'
					{
					match('!'); if (state.failed) return;
					}
					break;
				case 3 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1399:39: '#'
					{
					match('#'); if (state.failed) return;
					}
					break;
				case 4 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1399:43: '$'
					{
					match('$'); if (state.failed) return;
					}
					break;
				case 5 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1399:47: '%'
					{
					match('%'); if (state.failed) return;
					}
					break;
				case 6 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1399:51: '&'
					{
					match('&'); if (state.failed) return;
					}
					break;
				case 7 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1399:55: '*'
					{
					match('*'); if (state.failed) return;
					}
					break;
				case 8 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1399:59: '~'
					{
					match('~'); if (state.failed) return;
					}
					break;
				case 9 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1399:63: '.'
					{
					match('.'); if (state.failed) return;
					}
					break;
				case 10 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1399:67: ':'
					{
					match(':'); if (state.failed) return;
					}
					break;
				case 11 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1399:71: '/'
					{
					match('/'); if (state.failed) return;
					}
					break;
				case 12 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1399:75: '?'
					{
					match('?'); if (state.failed) return;
					}
					break;
				case 13 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1399:79: '='
					{
					match('='); if (state.failed) return;
					}
					break;
				case 14 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1399:83: ';'
					{
					match(';'); if (state.failed) return;
					}
					break;
				case 15 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1399:87: ','
					{
					match(','); if (state.failed) return;
					}
					break;
				case 16 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1399:91: '+'
					{
					match('+'); if (state.failed) return;
					}
					break;
				case 17 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1399:95: '@'
					{
					match('@'); if (state.failed) return;
					}
					break;
				case 18 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1399:99: '|'
					{
					match('|'); if (state.failed) return;
					}
					break;
				case 19 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1400:31: NMCHAR
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1407:17: ( ( 'a' | 'A' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '1' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1407:21: ( 'a' | 'A' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1408:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '1'
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1408:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
					int alt15=2;
					int LA15_0 = input.LA(1);
					if ( (LA15_0=='0') ) {
						alt15=1;
					}
					switch (alt15) {
						case 1 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1408:27: '0' ( '0' ( '0' ( '0' )? )? )?
							{
							match('0'); if (state.failed) return;
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1408:31: ( '0' ( '0' ( '0' )? )? )?
							int alt14=2;
							int LA14_0 = input.LA(1);
							if ( (LA14_0=='0') ) {
								alt14=1;
							}
							switch (alt14) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1408:32: '0' ( '0' ( '0' )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1408:36: ( '0' ( '0' )? )?
									int alt13=2;
									int LA13_0 = input.LA(1);
									if ( (LA13_0=='0') ) {
										alt13=1;
									}
									switch (alt13) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1408:37: '0' ( '0' )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1408:41: ( '0' )?
											int alt12=2;
											int LA12_0 = input.LA(1);
											if ( (LA12_0=='0') ) {
												alt12=1;
											}
											switch (alt12) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1408:41: '0'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1410:17: ( ( 'b' | 'B' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '2' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1410:21: ( 'b' | 'B' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1411:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '2'
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1411:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
					int alt20=2;
					int LA20_0 = input.LA(1);
					if ( (LA20_0=='0') ) {
						alt20=1;
					}
					switch (alt20) {
						case 1 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1411:27: '0' ( '0' ( '0' ( '0' )? )? )?
							{
							match('0'); if (state.failed) return;
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1411:31: ( '0' ( '0' ( '0' )? )? )?
							int alt19=2;
							int LA19_0 = input.LA(1);
							if ( (LA19_0=='0') ) {
								alt19=1;
							}
							switch (alt19) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1411:32: '0' ( '0' ( '0' )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1411:36: ( '0' ( '0' )? )?
									int alt18=2;
									int LA18_0 = input.LA(1);
									if ( (LA18_0=='0') ) {
										alt18=1;
									}
									switch (alt18) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1411:37: '0' ( '0' )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1411:41: ( '0' )?
											int alt17=2;
											int LA17_0 = input.LA(1);
											if ( (LA17_0=='0') ) {
												alt17=1;
											}
											switch (alt17) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1411:41: '0'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1413:17: ( ( 'c' | 'C' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '3' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1413:21: ( 'c' | 'C' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1414:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '3'
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1414:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
					int alt25=2;
					int LA25_0 = input.LA(1);
					if ( (LA25_0=='0') ) {
						alt25=1;
					}
					switch (alt25) {
						case 1 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1414:27: '0' ( '0' ( '0' ( '0' )? )? )?
							{
							match('0'); if (state.failed) return;
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1414:31: ( '0' ( '0' ( '0' )? )? )?
							int alt24=2;
							int LA24_0 = input.LA(1);
							if ( (LA24_0=='0') ) {
								alt24=1;
							}
							switch (alt24) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1414:32: '0' ( '0' ( '0' )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1414:36: ( '0' ( '0' )? )?
									int alt23=2;
									int LA23_0 = input.LA(1);
									if ( (LA23_0=='0') ) {
										alt23=1;
									}
									switch (alt23) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1414:37: '0' ( '0' )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1414:41: ( '0' )?
											int alt22=2;
											int LA22_0 = input.LA(1);
											if ( (LA22_0=='0') ) {
												alt22=1;
											}
											switch (alt22) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1414:41: '0'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1416:17: ( ( 'd' | 'D' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '4' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1416:21: ( 'd' | 'D' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1417:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '4'
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1417:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
					int alt30=2;
					int LA30_0 = input.LA(1);
					if ( (LA30_0=='0') ) {
						alt30=1;
					}
					switch (alt30) {
						case 1 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1417:27: '0' ( '0' ( '0' ( '0' )? )? )?
							{
							match('0'); if (state.failed) return;
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1417:31: ( '0' ( '0' ( '0' )? )? )?
							int alt29=2;
							int LA29_0 = input.LA(1);
							if ( (LA29_0=='0') ) {
								alt29=1;
							}
							switch (alt29) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1417:32: '0' ( '0' ( '0' )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1417:36: ( '0' ( '0' )? )?
									int alt28=2;
									int LA28_0 = input.LA(1);
									if ( (LA28_0=='0') ) {
										alt28=1;
									}
									switch (alt28) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1417:37: '0' ( '0' )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1417:41: ( '0' )?
											int alt27=2;
											int LA27_0 = input.LA(1);
											if ( (LA27_0=='0') ) {
												alt27=1;
											}
											switch (alt27) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1417:41: '0'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1419:17: ( ( 'e' | 'E' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '5' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1419:21: ( 'e' | 'E' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1420:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '5'
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1420:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
					int alt35=2;
					int LA35_0 = input.LA(1);
					if ( (LA35_0=='0') ) {
						alt35=1;
					}
					switch (alt35) {
						case 1 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1420:27: '0' ( '0' ( '0' ( '0' )? )? )?
							{
							match('0'); if (state.failed) return;
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1420:31: ( '0' ( '0' ( '0' )? )? )?
							int alt34=2;
							int LA34_0 = input.LA(1);
							if ( (LA34_0=='0') ) {
								alt34=1;
							}
							switch (alt34) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1420:32: '0' ( '0' ( '0' )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1420:36: ( '0' ( '0' )? )?
									int alt33=2;
									int LA33_0 = input.LA(1);
									if ( (LA33_0=='0') ) {
										alt33=1;
									}
									switch (alt33) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1420:37: '0' ( '0' )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1420:41: ( '0' )?
											int alt32=2;
											int LA32_0 = input.LA(1);
											if ( (LA32_0=='0') ) {
												alt32=1;
											}
											switch (alt32) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1420:41: '0'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1422:17: ( ( 'f' | 'F' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '6' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1422:21: ( 'f' | 'F' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1423:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '6'
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1423:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
					int alt40=2;
					int LA40_0 = input.LA(1);
					if ( (LA40_0=='0') ) {
						alt40=1;
					}
					switch (alt40) {
						case 1 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1423:27: '0' ( '0' ( '0' ( '0' )? )? )?
							{
							match('0'); if (state.failed) return;
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1423:31: ( '0' ( '0' ( '0' )? )? )?
							int alt39=2;
							int LA39_0 = input.LA(1);
							if ( (LA39_0=='0') ) {
								alt39=1;
							}
							switch (alt39) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1423:32: '0' ( '0' ( '0' )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1423:36: ( '0' ( '0' )? )?
									int alt38=2;
									int LA38_0 = input.LA(1);
									if ( (LA38_0=='0') ) {
										alt38=1;
									}
									switch (alt38) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1423:37: '0' ( '0' )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1423:41: ( '0' )?
											int alt37=2;
											int LA37_0 = input.LA(1);
											if ( (LA37_0=='0') ) {
												alt37=1;
											}
											switch (alt37) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1423:41: '0'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1425:17: ( ( 'g' | 'G' ) | '\\\\' ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1425:21: ( 'g' | 'G' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1426:21: '\\\\' ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1427:25: ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1428:31: 'g'
							{
							match('g'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1429:31: 'G'
							{
							match('G'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1430:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7'
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1430:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt45=2;
							int LA45_0 = input.LA(1);
							if ( (LA45_0=='0') ) {
								alt45=1;
							}
							switch (alt45) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1430:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1430:36: ( '0' ( '0' ( '0' )? )? )?
									int alt44=2;
									int LA44_0 = input.LA(1);
									if ( (LA44_0=='0') ) {
										alt44=1;
									}
									switch (alt44) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1430:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1430:41: ( '0' ( '0' )? )?
											int alt43=2;
											int LA43_0 = input.LA(1);
											if ( (LA43_0=='0') ) {
												alt43=1;
											}
											switch (alt43) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1430:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1430:46: ( '0' )?
													int alt42=2;
													int LA42_0 = input.LA(1);
													if ( (LA42_0=='0') ) {
														alt42=1;
													}
													switch (alt42) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1430:46: '0'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1433:17: ( ( 'h' | 'H' ) | '\\\\' ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1433:21: ( 'h' | 'H' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1434:19: '\\\\' ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1435:25: ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1436:31: 'h'
							{
							match('h'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1437:31: 'H'
							{
							match('H'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8'
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt51=2;
							int LA51_0 = input.LA(1);
							if ( (LA51_0=='0') ) {
								alt51=1;
							}
							switch (alt51) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:36: ( '0' ( '0' ( '0' )? )? )?
									int alt50=2;
									int LA50_0 = input.LA(1);
									if ( (LA50_0=='0') ) {
										alt50=1;
									}
									switch (alt50) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:41: ( '0' ( '0' )? )?
											int alt49=2;
											int LA49_0 = input.LA(1);
											if ( (LA49_0=='0') ) {
												alt49=1;
											}
											switch (alt49) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:46: ( '0' )?
													int alt48=2;
													int LA48_0 = input.LA(1);
													if ( (LA48_0=='0') ) {
														alt48=1;
													}
													switch (alt48) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1438:46: '0'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1441:17: ( ( 'i' | 'I' ) | '\\\\' ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1441:21: ( 'i' | 'I' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1442:19: '\\\\' ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1443:25: ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1444:31: 'i'
							{
							match('i'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1445:31: 'I'
							{
							match('I'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1446:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9'
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1446:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt57=2;
							int LA57_0 = input.LA(1);
							if ( (LA57_0=='0') ) {
								alt57=1;
							}
							switch (alt57) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1446:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1446:36: ( '0' ( '0' ( '0' )? )? )?
									int alt56=2;
									int LA56_0 = input.LA(1);
									if ( (LA56_0=='0') ) {
										alt56=1;
									}
									switch (alt56) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1446:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1446:41: ( '0' ( '0' )? )?
											int alt55=2;
											int LA55_0 = input.LA(1);
											if ( (LA55_0=='0') ) {
												alt55=1;
											}
											switch (alt55) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1446:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1446:46: ( '0' )?
													int alt54=2;
													int LA54_0 = input.LA(1);
													if ( (LA54_0=='0') ) {
														alt54=1;
													}
													switch (alt54) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1446:46: '0'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1449:17: ( ( 'j' | 'J' ) | '\\\\' ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1449:21: ( 'j' | 'J' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1450:19: '\\\\' ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1451:25: ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1452:31: 'j'
							{
							match('j'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1453:31: 'J'
							{
							match('J'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' )
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt63=2;
							int LA63_0 = input.LA(1);
							if ( (LA63_0=='0') ) {
								alt63=1;
							}
							switch (alt63) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:36: ( '0' ( '0' ( '0' )? )? )?
									int alt62=2;
									int LA62_0 = input.LA(1);
									if ( (LA62_0=='0') ) {
										alt62=1;
									}
									switch (alt62) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:41: ( '0' ( '0' )? )?
											int alt61=2;
											int LA61_0 = input.LA(1);
											if ( (LA61_0=='0') ) {
												alt61=1;
											}
											switch (alt61) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:46: ( '0' )?
													int alt60=2;
													int LA60_0 = input.LA(1);
													if ( (LA60_0=='0') ) {
														alt60=1;
													}
													switch (alt60) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1454:46: '0'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1457:17: ( ( 'k' | 'K' ) | '\\\\' ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1457:21: ( 'k' | 'K' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1458:19: '\\\\' ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1459:25: ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1460:31: 'k'
							{
							match('k'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1461:31: 'K'
							{
							match('K'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1462:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' )
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1462:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt69=2;
							int LA69_0 = input.LA(1);
							if ( (LA69_0=='0') ) {
								alt69=1;
							}
							switch (alt69) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1462:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1462:36: ( '0' ( '0' ( '0' )? )? )?
									int alt68=2;
									int LA68_0 = input.LA(1);
									if ( (LA68_0=='0') ) {
										alt68=1;
									}
									switch (alt68) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1462:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1462:41: ( '0' ( '0' )? )?
											int alt67=2;
											int LA67_0 = input.LA(1);
											if ( (LA67_0=='0') ) {
												alt67=1;
											}
											switch (alt67) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1462:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1462:46: ( '0' )?
													int alt66=2;
													int LA66_0 = input.LA(1);
													if ( (LA66_0=='0') ) {
														alt66=1;
													}
													switch (alt66) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1462:46: '0'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1465:17: ( ( 'l' | 'L' ) | '\\\\' ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1465:21: ( 'l' | 'L' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1466:19: '\\\\' ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1467:25: ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1468:31: 'l'
							{
							match('l'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1469:31: 'L'
							{
							match('L'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1470:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' )
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1470:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt75=2;
							int LA75_0 = input.LA(1);
							if ( (LA75_0=='0') ) {
								alt75=1;
							}
							switch (alt75) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1470:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1470:36: ( '0' ( '0' ( '0' )? )? )?
									int alt74=2;
									int LA74_0 = input.LA(1);
									if ( (LA74_0=='0') ) {
										alt74=1;
									}
									switch (alt74) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1470:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1470:41: ( '0' ( '0' )? )?
											int alt73=2;
											int LA73_0 = input.LA(1);
											if ( (LA73_0=='0') ) {
												alt73=1;
											}
											switch (alt73) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1470:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1470:46: ( '0' )?
													int alt72=2;
													int LA72_0 = input.LA(1);
													if ( (LA72_0=='0') ) {
														alt72=1;
													}
													switch (alt72) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1470:46: '0'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1473:17: ( ( 'm' | 'M' ) | '\\\\' ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1473:21: ( 'm' | 'M' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1474:19: '\\\\' ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1475:25: ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1476:31: 'm'
							{
							match('m'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1477:31: 'M'
							{
							match('M'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' )
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt81=2;
							int LA81_0 = input.LA(1);
							if ( (LA81_0=='0') ) {
								alt81=1;
							}
							switch (alt81) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:36: ( '0' ( '0' ( '0' )? )? )?
									int alt80=2;
									int LA80_0 = input.LA(1);
									if ( (LA80_0=='0') ) {
										alt80=1;
									}
									switch (alt80) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:41: ( '0' ( '0' )? )?
											int alt79=2;
											int LA79_0 = input.LA(1);
											if ( (LA79_0=='0') ) {
												alt79=1;
											}
											switch (alt79) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:46: ( '0' )?
													int alt78=2;
													int LA78_0 = input.LA(1);
													if ( (LA78_0=='0') ) {
														alt78=1;
													}
													switch (alt78) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1478:46: '0'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1481:17: ( ( 'n' | 'N' ) | '\\\\' ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1481:21: ( 'n' | 'N' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1482:19: '\\\\' ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1483:25: ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1484:31: 'n'
							{
							match('n'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1485:31: 'N'
							{
							match('N'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' )
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt87=2;
							int LA87_0 = input.LA(1);
							if ( (LA87_0=='0') ) {
								alt87=1;
							}
							switch (alt87) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:36: ( '0' ( '0' ( '0' )? )? )?
									int alt86=2;
									int LA86_0 = input.LA(1);
									if ( (LA86_0=='0') ) {
										alt86=1;
									}
									switch (alt86) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:41: ( '0' ( '0' )? )?
											int alt85=2;
											int LA85_0 = input.LA(1);
											if ( (LA85_0=='0') ) {
												alt85=1;
											}
											switch (alt85) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:46: ( '0' )?
													int alt84=2;
													int LA84_0 = input.LA(1);
													if ( (LA84_0=='0') ) {
														alt84=1;
													}
													switch (alt84) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1486:46: '0'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1489:17: ( ( 'o' | 'O' ) | '\\\\' ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1489:21: ( 'o' | 'O' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1490:19: '\\\\' ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1491:25: ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1492:31: 'o'
							{
							match('o'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1493:31: 'O'
							{
							match('O'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' )
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt93=2;
							int LA93_0 = input.LA(1);
							if ( (LA93_0=='0') ) {
								alt93=1;
							}
							switch (alt93) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:36: ( '0' ( '0' ( '0' )? )? )?
									int alt92=2;
									int LA92_0 = input.LA(1);
									if ( (LA92_0=='0') ) {
										alt92=1;
									}
									switch (alt92) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:41: ( '0' ( '0' )? )?
											int alt91=2;
											int LA91_0 = input.LA(1);
											if ( (LA91_0=='0') ) {
												alt91=1;
											}
											switch (alt91) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:46: ( '0' )?
													int alt90=2;
													int LA90_0 = input.LA(1);
													if ( (LA90_0=='0') ) {
														alt90=1;
													}
													switch (alt90) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1494:46: '0'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1497:17: ( ( 'p' | 'P' ) | '\\\\' ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1497:21: ( 'p' | 'P' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1498:19: '\\\\' ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1499:25: ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1500:31: 'p'
							{
							match('p'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1501:31: 'P'
							{
							match('P'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' )
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt99=2;
							int LA99_0 = input.LA(1);
							if ( (LA99_0=='0') ) {
								alt99=1;
							}
							switch (alt99) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:36: ( '0' ( '0' ( '0' )? )? )?
									int alt98=2;
									int LA98_0 = input.LA(1);
									if ( (LA98_0=='0') ) {
										alt98=1;
									}
									switch (alt98) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:41: ( '0' ( '0' )? )?
											int alt97=2;
											int LA97_0 = input.LA(1);
											if ( (LA97_0=='0') ) {
												alt97=1;
											}
											switch (alt97) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:46: ( '0' )?
													int alt96=2;
													int LA96_0 = input.LA(1);
													if ( (LA96_0=='0') ) {
														alt96=1;
													}
													switch (alt96) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:46: '0'
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:66: ( '0' )
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1502:67: '0'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1505:17: ( ( 'q' | 'Q' ) | '\\\\' ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1505:21: ( 'q' | 'Q' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1506:19: '\\\\' ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1507:25: ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1508:31: 'q'
							{
							match('q'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1509:31: 'Q'
							{
							match('Q'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' )
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt105=2;
							int LA105_0 = input.LA(1);
							if ( (LA105_0=='0') ) {
								alt105=1;
							}
							switch (alt105) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:36: ( '0' ( '0' ( '0' )? )? )?
									int alt104=2;
									int LA104_0 = input.LA(1);
									if ( (LA104_0=='0') ) {
										alt104=1;
									}
									switch (alt104) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:41: ( '0' ( '0' )? )?
											int alt103=2;
											int LA103_0 = input.LA(1);
											if ( (LA103_0=='0') ) {
												alt103=1;
											}
											switch (alt103) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:46: ( '0' )?
													int alt102=2;
													int LA102_0 = input.LA(1);
													if ( (LA102_0=='0') ) {
														alt102=1;
													}
													switch (alt102) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:46: '0'
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:66: ( '1' )
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1510:67: '1'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1513:17: ( ( 'r' | 'R' ) | '\\\\' ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1513:21: ( 'r' | 'R' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1514:19: '\\\\' ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1515:25: ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1516:31: 'r'
							{
							match('r'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1517:31: 'R'
							{
							match('R'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' )
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt111=2;
							int LA111_0 = input.LA(1);
							if ( (LA111_0=='0') ) {
								alt111=1;
							}
							switch (alt111) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:36: ( '0' ( '0' ( '0' )? )? )?
									int alt110=2;
									int LA110_0 = input.LA(1);
									if ( (LA110_0=='0') ) {
										alt110=1;
									}
									switch (alt110) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:41: ( '0' ( '0' )? )?
											int alt109=2;
											int LA109_0 = input.LA(1);
											if ( (LA109_0=='0') ) {
												alt109=1;
											}
											switch (alt109) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:46: ( '0' )?
													int alt108=2;
													int LA108_0 = input.LA(1);
													if ( (LA108_0=='0') ) {
														alt108=1;
													}
													switch (alt108) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:46: '0'
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:66: ( '2' )
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1518:67: '2'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1521:17: ( ( 's' | 'S' ) | '\\\\' ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1521:21: ( 's' | 'S' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1522:19: '\\\\' ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1523:25: ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1524:31: 's'
							{
							match('s'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1525:31: 'S'
							{
							match('S'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' )
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt117=2;
							int LA117_0 = input.LA(1);
							if ( (LA117_0=='0') ) {
								alt117=1;
							}
							switch (alt117) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:36: ( '0' ( '0' ( '0' )? )? )?
									int alt116=2;
									int LA116_0 = input.LA(1);
									if ( (LA116_0=='0') ) {
										alt116=1;
									}
									switch (alt116) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:41: ( '0' ( '0' )? )?
											int alt115=2;
											int LA115_0 = input.LA(1);
											if ( (LA115_0=='0') ) {
												alt115=1;
											}
											switch (alt115) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:46: ( '0' )?
													int alt114=2;
													int LA114_0 = input.LA(1);
													if ( (LA114_0=='0') ) {
														alt114=1;
													}
													switch (alt114) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:46: '0'
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:66: ( '3' )
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1526:67: '3'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1529:17: ( ( 't' | 'T' ) | '\\\\' ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1529:21: ( 't' | 'T' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1530:19: '\\\\' ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1531:25: ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1532:31: 't'
							{
							match('t'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1533:31: 'T'
							{
							match('T'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1534:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' )
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1534:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt123=2;
							int LA123_0 = input.LA(1);
							if ( (LA123_0=='0') ) {
								alt123=1;
							}
							switch (alt123) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1534:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1534:36: ( '0' ( '0' ( '0' )? )? )?
									int alt122=2;
									int LA122_0 = input.LA(1);
									if ( (LA122_0=='0') ) {
										alt122=1;
									}
									switch (alt122) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1534:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1534:41: ( '0' ( '0' )? )?
											int alt121=2;
											int LA121_0 = input.LA(1);
											if ( (LA121_0=='0') ) {
												alt121=1;
											}
											switch (alt121) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1534:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1534:46: ( '0' )?
													int alt120=2;
													int LA120_0 = input.LA(1);
													if ( (LA120_0=='0') ) {
														alt120=1;
													}
													switch (alt120) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1534:46: '0'
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1534:66: ( '4' )
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1534:67: '4'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1537:17: ( ( 'u' | 'U' ) | '\\\\' ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1537:21: ( 'u' | 'U' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1538:19: '\\\\' ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1539:25: ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1540:31: 'u'
							{
							match('u'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1541:31: 'U'
							{
							match('U'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1542:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' )
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1542:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt129=2;
							int LA129_0 = input.LA(1);
							if ( (LA129_0=='0') ) {
								alt129=1;
							}
							switch (alt129) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1542:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1542:36: ( '0' ( '0' ( '0' )? )? )?
									int alt128=2;
									int LA128_0 = input.LA(1);
									if ( (LA128_0=='0') ) {
										alt128=1;
									}
									switch (alt128) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1542:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1542:41: ( '0' ( '0' )? )?
											int alt127=2;
											int LA127_0 = input.LA(1);
											if ( (LA127_0=='0') ) {
												alt127=1;
											}
											switch (alt127) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1542:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1542:46: ( '0' )?
													int alt126=2;
													int LA126_0 = input.LA(1);
													if ( (LA126_0=='0') ) {
														alt126=1;
													}
													switch (alt126) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1542:46: '0'
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1542:66: ( '5' )
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1542:67: '5'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1545:17: ( ( 'v' | 'V' ) | '\\\\' ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1545:21: ( 'v' | 'V' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1546:19: '\\\\' ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1547:25: ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1547:31: 'v'
							{
							match('v'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1548:31: 'V'
							{
							match('V'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' )
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt135=2;
							int LA135_0 = input.LA(1);
							if ( (LA135_0=='0') ) {
								alt135=1;
							}
							switch (alt135) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:36: ( '0' ( '0' ( '0' )? )? )?
									int alt134=2;
									int LA134_0 = input.LA(1);
									if ( (LA134_0=='0') ) {
										alt134=1;
									}
									switch (alt134) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:41: ( '0' ( '0' )? )?
											int alt133=2;
											int LA133_0 = input.LA(1);
											if ( (LA133_0=='0') ) {
												alt133=1;
											}
											switch (alt133) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:46: ( '0' )?
													int alt132=2;
													int LA132_0 = input.LA(1);
													if ( (LA132_0=='0') ) {
														alt132=1;
													}
													switch (alt132) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:46: '0'
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:66: ( '6' )
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1549:67: '6'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1552:17: ( ( 'w' | 'W' ) | '\\\\' ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1552:21: ( 'w' | 'W' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1553:19: '\\\\' ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1554:25: ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1555:31: 'w'
							{
							match('w'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1556:31: 'W'
							{
							match('W'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' )
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt141=2;
							int LA141_0 = input.LA(1);
							if ( (LA141_0=='0') ) {
								alt141=1;
							}
							switch (alt141) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:36: ( '0' ( '0' ( '0' )? )? )?
									int alt140=2;
									int LA140_0 = input.LA(1);
									if ( (LA140_0=='0') ) {
										alt140=1;
									}
									switch (alt140) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:41: ( '0' ( '0' )? )?
											int alt139=2;
											int LA139_0 = input.LA(1);
											if ( (LA139_0=='0') ) {
												alt139=1;
											}
											switch (alt139) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:46: ( '0' )?
													int alt138=2;
													int LA138_0 = input.LA(1);
													if ( (LA138_0=='0') ) {
														alt138=1;
													}
													switch (alt138) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:46: '0'
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:66: ( '7' )
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1557:67: '7'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1560:17: ( ( 'x' | 'X' ) | '\\\\' ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1560:21: ( 'x' | 'X' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1561:19: '\\\\' ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1562:25: ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1563:31: 'x'
							{
							match('x'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1564:31: 'X'
							{
							match('X'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' )
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt147=2;
							int LA147_0 = input.LA(1);
							if ( (LA147_0=='0') ) {
								alt147=1;
							}
							switch (alt147) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:36: ( '0' ( '0' ( '0' )? )? )?
									int alt146=2;
									int LA146_0 = input.LA(1);
									if ( (LA146_0=='0') ) {
										alt146=1;
									}
									switch (alt146) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:41: ( '0' ( '0' )? )?
											int alt145=2;
											int LA145_0 = input.LA(1);
											if ( (LA145_0=='0') ) {
												alt145=1;
											}
											switch (alt145) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:46: ( '0' )?
													int alt144=2;
													int LA144_0 = input.LA(1);
													if ( (LA144_0=='0') ) {
														alt144=1;
													}
													switch (alt144) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:46: '0'
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:66: ( '8' )
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1565:67: '8'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1568:17: ( ( 'y' | 'Y' ) | '\\\\' ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1568:21: ( 'y' | 'Y' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1569:19: '\\\\' ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1570:25: ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1571:31: 'y'
							{
							match('y'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1572:31: 'Y'
							{
							match('Y'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1573:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' )
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1573:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt153=2;
							int LA153_0 = input.LA(1);
							if ( (LA153_0=='0') ) {
								alt153=1;
							}
							switch (alt153) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1573:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1573:36: ( '0' ( '0' ( '0' )? )? )?
									int alt152=2;
									int LA152_0 = input.LA(1);
									if ( (LA152_0=='0') ) {
										alt152=1;
									}
									switch (alt152) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1573:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1573:41: ( '0' ( '0' )? )?
											int alt151=2;
											int LA151_0 = input.LA(1);
											if ( (LA151_0=='0') ) {
												alt151=1;
											}
											switch (alt151) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1573:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1573:46: ( '0' )?
													int alt150=2;
													int LA150_0 = input.LA(1);
													if ( (LA150_0=='0') ) {
														alt150=1;
													}
													switch (alt150) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1573:46: '0'
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1573:66: ( '9' )
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1573:67: '9'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1576:17: ( ( 'z' | 'Z' ) | '\\\\' ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) ) )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1576:21: ( 'z' | 'Z' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1577:19: '\\\\' ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) )
					{
					match('\\'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1578:25: ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) )
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1579:31: 'z'
							{
							match('z'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1580:31: 'Z'
							{
							match('Z'); if (state.failed) return;
							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1581:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' )
							{
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1581:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
							int alt159=2;
							int LA159_0 = input.LA(1);
							if ( (LA159_0=='0') ) {
								alt159=1;
							}
							switch (alt159) {
								case 1 :
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1581:32: '0' ( '0' ( '0' ( '0' )? )? )?
									{
									match('0'); if (state.failed) return;
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1581:36: ( '0' ( '0' ( '0' )? )? )?
									int alt158=2;
									int LA158_0 = input.LA(1);
									if ( (LA158_0=='0') ) {
										alt158=1;
									}
									switch (alt158) {
										case 1 :
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1581:37: '0' ( '0' ( '0' )? )?
											{
											match('0'); if (state.failed) return;
											// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1581:41: ( '0' ( '0' )? )?
											int alt157=2;
											int LA157_0 = input.LA(1);
											if ( (LA157_0=='0') ) {
												alt157=1;
											}
											switch (alt157) {
												case 1 :
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1581:42: '0' ( '0' )?
													{
													match('0'); if (state.failed) return;
													// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1581:46: ( '0' )?
													int alt156=2;
													int LA156_0 = input.LA(1);
													if ( (LA156_0=='0') ) {
														alt156=1;
													}
													switch (alt156) {
														case 1 :
															// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1581:46: '0'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1593:17: ( '<!--' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1593:19: '<!--'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1606:17: ( '-->' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1606:19: '-->'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1613:17: ( '~=' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1613:19: '~='
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1614:17: ( '|=' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1614:19: '|='
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1615:17: ( '^=' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1615:19: '^='
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1616:17: ( '$=' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1616:19: '$='
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1617:17: ( '*=' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1617:19: '*='
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1619:17: ( '>' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1619:19: '>'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1620:17: ( '{' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1620:19: '{'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1621:17: ( '}' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1621:19: '}'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1622:17: ( '[' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1622:19: '['
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1623:17: ( ']' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1623:19: ']'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1624:17: ( '=' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1624:19: '='
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1625:17: ( ';' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1625:19: ';'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1626:17: ( ':' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1626:19: ':'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1627:17: ( '::' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1627:19: '::'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1628:17: ( '/' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1628:19: '/'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1629:17: ( '-' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1629:19: '-'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1630:17: ( '+' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1630:19: '+'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1631:17: ( '*' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1631:19: '*'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1632:17: ( '(' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1632:19: '('
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1633:17: ( ')' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1633:19: ')'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1634:17: ( ',' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1634:19: ','
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1635:17: ( '.' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1635:19: '.'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1636:8: ( '~' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1636:10: '~'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1637:17: ( '|' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1637:19: '|'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1639:17: ( '%' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1639:19: '%'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1640:17: ( '!' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1640:19: '!'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1642:17: ( '==' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1642:19: '=='
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1643:17: ( '!=' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1643:19: '!='
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1644:17: ( '<' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1644:19: '<'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1645:17: ( '>=' | '=>' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1645:19: '>='
					{
					match(">="); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1645:26: '=>'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1646:17: ( '=<' | '<=' )
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1646:19: '=<'
					{
					match("=<"); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1646:26: '<='
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

	// $ANTLR start "LESS_WHEN"
	public final void mLESS_WHEN() throws RecognitionException {
		try {
			int _type = LESS_WHEN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1647:17: ( 'WHEN' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1647:19: 'WHEN'
			{
			match("WHEN"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "LESS_WHEN"

	// $ANTLR start "LESS_AND"
	public final void mLESS_AND() throws RecognitionException {
		try {
			int _type = LESS_AND;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1648:17: ( '&' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1648:19: '&'
			{
			match('&'); if (state.failed) return;
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1649:17: ( '...' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1649:19: '...'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1650:17: ( '@rest...' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1650:19: '@rest...'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1655:21: ()
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1655:22: 
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1656:17: ( '\\'' (~ ( '\\r' | '\\f' | '\\'' ) )* ( '\\'' |) | '\"' (~ ( '\\r' | '\\f' | '\"' ) )* ( '\"' |) )
			int alt168=2;
			int LA168_0 = input.LA(1);
			if ( (LA168_0=='\'') ) {
				alt168=1;
			}
			else if ( (LA168_0=='\"') ) {
				alt168=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 168, 0, input);
				throw nvae;
			}

			switch (alt168) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1656:19: '\\'' (~ ( '\\r' | '\\f' | '\\'' ) )* ( '\\'' |)
					{
					match('\''); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1656:24: (~ ( '\\r' | '\\f' | '\\'' ) )*
					loop164:
					while (true) {
						int alt164=2;
						int LA164_0 = input.LA(1);
						if ( ((LA164_0 >= '\u0000' && LA164_0 <= '\u000B')||(LA164_0 >= '\u000E' && LA164_0 <= '&')||(LA164_0 >= '(' && LA164_0 <= '\uFFFF')) ) {
							alt164=1;
						}

						switch (alt164) {
						case 1 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
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
							break loop164;
						}
					}

					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1657:21: ( '\\'' |)
					int alt165=2;
					int LA165_0 = input.LA(1);
					if ( (LA165_0=='\'') ) {
						alt165=1;
					}

					else {
						alt165=2;
					}

					switch (alt165) {
						case 1 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1658:27: '\\''
							{
							match('\''); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1659:27: 
							{
							if ( state.backtracking==0 ) { _type = INVALID; }
							}
							break;

					}

					}
					break;
				case 2 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1662:19: '\"' (~ ( '\\r' | '\\f' | '\"' ) )* ( '\"' |)
					{
					match('\"'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1662:23: (~ ( '\\r' | '\\f' | '\"' ) )*
					loop166:
					while (true) {
						int alt166=2;
						int LA166_0 = input.LA(1);
						if ( ((LA166_0 >= '\u0000' && LA166_0 <= '\u000B')||(LA166_0 >= '\u000E' && LA166_0 <= '!')||(LA166_0 >= '#' && LA166_0 <= '\uFFFF')) ) {
							alt166=1;
						}

						switch (alt166) {
						case 1 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
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
							break loop166;
						}
					}

					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1663:21: ( '\"' |)
					int alt167=2;
					int LA167_0 = input.LA(1);
					if ( (LA167_0=='\"') ) {
						alt167=1;
					}

					else {
						alt167=2;
					}

					switch (alt167) {
						case 1 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1664:27: '\"'
							{
							match('\"'); if (state.failed) return;
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1665:27: 
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1669:17: ( '`' (~ ( '\\r' | '\\f' | '`' ) )* ( '`' |) )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1669:19: '`' (~ ( '\\r' | '\\f' | '`' ) )* ( '`' |)
			{
			match('`'); if (state.failed) return;
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1669:23: (~ ( '\\r' | '\\f' | '`' ) )*
			loop169:
			while (true) {
				int alt169=2;
				int LA169_0 = input.LA(1);
				if ( ((LA169_0 >= '\u0000' && LA169_0 <= '\u000B')||(LA169_0 >= '\u000E' && LA169_0 <= '_')||(LA169_0 >= 'a' && LA169_0 <= '\uFFFF')) ) {
					alt169=1;
				}

				switch (alt169) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
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
					break loop169;
				}
			}

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1670:21: ( '`' |)
			int alt170=2;
			int LA170_0 = input.LA(1);
			if ( (LA170_0=='`') ) {
				alt170=1;
			}

			else {
				alt170=2;
			}

			switch (alt170) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1671:27: '`'
					{
					match('`'); if (state.failed) return;
					}
					break;
				case 2 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1672:27: 
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

	// $ANTLR start "ONLY"
	public final void mONLY() throws RecognitionException {
		try {
			int _type = ONLY;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1677:8: ( 'ONLY' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1677:10: 'ONLY'
			{
			match("ONLY"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ONLY"

	// $ANTLR start "NOT"
	public final void mNOT() throws RecognitionException {
		try {
			int _type = NOT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1678:6: ( 'NOT' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1678:8: 'NOT'
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

	// $ANTLR start "AND"
	public final void mAND() throws RecognitionException {
		try {
			int _type = AND;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1679:6: ( 'AND' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1679:8: 'AND'
			{
			match("AND"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "AND"

	// $ANTLR start "OR"
	public final void mOR() throws RecognitionException {
		try {
			int _type = OR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1680:5: ( 'OR' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1680:7: 'OR'
			{
			match("OR"); if (state.failed) return;

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "OR"

	// $ANTLR start "IDENT"
	public final void mIDENT() throws RecognitionException {
		try {
			int _type = IDENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1685:17: ( ( '-' )? NMSTART ( NMCHAR )* )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1685:19: ( '-' )? NMSTART ( NMCHAR )*
			{
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1685:19: ( '-' )?
			int alt171=2;
			int LA171_0 = input.LA(1);
			if ( (LA171_0=='-') ) {
				alt171=1;
			}
			switch (alt171) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1685:19: '-'
					{
					match('-'); if (state.failed) return;
					}
					break;

			}

			mNMSTART(); if (state.failed) return;

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1685:32: ( NMCHAR )*
			loop172:
			while (true) {
				int alt172=2;
				int LA172_0 = input.LA(1);
				if ( (LA172_0=='-'||(LA172_0 >= '0' && LA172_0 <= '9')||(LA172_0 >= 'A' && LA172_0 <= 'Z')||LA172_0=='\\'||LA172_0=='_'||(LA172_0 >= 'a' && LA172_0 <= 'z')||(LA172_0 >= '\u0080' && LA172_0 <= '\uFFFF')) ) {
					alt172=1;
				}

				switch (alt172) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1685:32: NMCHAR
					{
					mNMCHAR(); if (state.failed) return;

					}
					break;

				default :
					break loop172;
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1690:17: ( '#' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1690:19: '#'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1691:17: ( HASH_SYMBOL NAME )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1691:19: HASH_SYMBOL NAME
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1693:17: ( EXCLAMATION_MARK ( WS | COMMENT )* 'IMPORTANT' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1693:19: EXCLAMATION_MARK ( WS | COMMENT )* 'IMPORTANT'
			{
			mEXCLAMATION_MARK(); if (state.failed) return;

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1693:36: ( WS | COMMENT )*
			loop173:
			while (true) {
				int alt173=3;
				int LA173_0 = input.LA(1);
				if ( (LA173_0=='\t'||LA173_0==' ') ) {
					alt173=1;
				}
				else if ( (LA173_0=='/') ) {
					alt173=2;
				}

				switch (alt173) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1693:37: WS
					{
					mWS(); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1693:40: COMMENT
					{
					mCOMMENT(); if (state.failed) return;

					}
					break;

				default :
					break loop173;
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1695:21: ( '@IMPORT' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1695:23: '@IMPORT'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:21: ( '@PAGE' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1696:23: '@PAGE'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1697:21: ( '@MEDIA' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1697:23: '@MEDIA'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1698:21: ( '@NAMESPACE' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1698:23: '@NAMESPACE'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1699:21: ( '@CHARSET' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1699:23: '@CHARSET'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1700:21: ( '@COUNTER-STYLE' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1700:23: '@COUNTER-STYLE'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1701:21: ( '@FONT-FACE' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1701:23: '@FONT-FACE'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1703:23: ( '@TOP-LEFT-CORNER' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1703:24: '@TOP-LEFT-CORNER'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1704:23: ( '@TOP-LEFT' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1704:24: '@TOP-LEFT'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1705:23: ( '@TOP-CENTER' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1705:24: '@TOP-CENTER'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1706:23: ( '@TOP-RIGHT' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1706:24: '@TOP-RIGHT'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1707:23: ( '@TOP-RIGHT-CORNER' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1707:24: '@TOP-RIGHT-CORNER'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1708:23: ( '@BOTTOM-LEFT-CORNER' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1708:24: '@BOTTOM-LEFT-CORNER'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1709:23: ( '@BOTTOM-LEFT' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1709:24: '@BOTTOM-LEFT'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1710:23: ( '@BOTTOM-CENTER' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1710:24: '@BOTTOM-CENTER'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1711:23: ( '@BOTTOM-RIGHT' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1711:24: '@BOTTOM-RIGHT'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1712:23: ( '@BOTTOM-RIGHT-CORNER' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1712:24: '@BOTTOM-RIGHT-CORNER'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1713:23: ( '@LEFT-TOP' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1713:24: '@LEFT-TOP'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:23: ( '@LEFT-MIDDLE' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1714:24: '@LEFT-MIDDLE'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1715:23: ( '@LEFT-BOTTOM' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1715:24: '@LEFT-BOTTOM'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1716:23: ( '@RIGHT-TOP' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1716:24: '@RIGHT-TOP'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1717:23: ( '@RIGHT-MIDDLE' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1717:24: '@RIGHT-MIDDLE'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1718:23: ( '@RIGHT-BOTTOM' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1718:24: '@RIGHT-BOTTOM'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1720:23: ( '@-MOZ-DOCUMENT' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1720:25: '@-MOZ-DOCUMENT'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1721:23: ( '@-WEBKIT-KEYFRAMES' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1721:25: '@-WEBKIT-KEYFRAMES'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1724:21: ( '@CONTENT' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1724:23: '@CONTENT'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1725:21: ( '@MIXIN' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1725:23: '@MIXIN'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1726:21: ( '@INCLUDE' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1726:23: '@INCLUDE'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1727:21: ( '@EXTEND' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1727:23: '@EXTEND'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1728:21: ( '@DEBUG' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1728:23: '@DEBUG'
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

	// $ANTLR start "SASS_WARN"
	public final void mSASS_WARN() throws RecognitionException {
		try {
			int _type = SASS_WARN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1729:21: ( '@WARN' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1729:23: '@WARN'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1730:21: ( '@IF' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1730:23: '@IF'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1731:21: ( '@ELSE' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1731:23: '@ELSE'
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

	// $ANTLR start "SASS_FOR"
	public final void mSASS_FOR() throws RecognitionException {
		try {
			int _type = SASS_FOR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1733:21: ( '@FOR' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1733:23: '@FOR'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1734:21: ( '@FUNCTION' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1734:23: '@FUNCTION'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1735:21: ( '@RETURN' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1735:23: '@RETURN'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1737:21: ( '@EACH' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1737:23: '@EACH'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1738:21: ( '@WHILE' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1738:23: '@WHILE'
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

	// $ANTLR start "AT_SIGN"
	public final void mAT_SIGN() throws RecognitionException {
		try {
			int _type = AT_SIGN;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1740:21: ( '@' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1740:23: '@'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1741:14: ( AT_SIGN ( NMCHAR )+ )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1741:16: AT_SIGN ( NMCHAR )+
			{
			mAT_SIGN(); if (state.failed) return;

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1741:24: ( NMCHAR )+
			int cnt174=0;
			loop174:
			while (true) {
				int alt174=2;
				int LA174_0 = input.LA(1);
				if ( (LA174_0=='-'||(LA174_0 >= '0' && LA174_0 <= '9')||(LA174_0 >= 'A' && LA174_0 <= 'Z')||LA174_0=='\\'||LA174_0=='_'||(LA174_0 >= 'a' && LA174_0 <= 'z')||(LA174_0 >= '\u0080' && LA174_0 <= '\uFFFF')) ) {
					alt174=1;
				}

				switch (alt174) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1741:24: NMCHAR
					{
					mNMCHAR(); if (state.failed) return;

					}
					break;

				default :
					if ( cnt174 >= 1 ) break loop174;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(174, input);
					throw eee;
				}
				cnt174++;
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1743:21: ( '$' ( NMCHAR )+ )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1743:23: '$' ( NMCHAR )+
			{
			match('$'); if (state.failed) return;
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1743:27: ( NMCHAR )+
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1743:27: NMCHAR
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
	// $ANTLR end "SASS_VAR"

	// $ANTLR start "SASS_DEFAULT"
	public final void mSASS_DEFAULT() throws RecognitionException {
		try {
			int _type = SASS_DEFAULT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1744:21: ( '!DEFAULT' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1744:23: '!DEFAULT'
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1745:21: ( '!OPTIONAL' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1745:23: '!OPTIONAL'
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

	// $ANTLR start "SASS_EXTEND_ONLY_SELECTOR"
	public final void mSASS_EXTEND_ONLY_SELECTOR() throws RecognitionException {
		try {
			int _type = SASS_EXTEND_ONLY_SELECTOR;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1748:21: ( PERCENTAGE_SYMBOL ( NMCHAR )+ )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1748:23: PERCENTAGE_SYMBOL ( NMCHAR )+
			{
			mPERCENTAGE_SYMBOL(); if (state.failed) return;

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1748:41: ( NMCHAR )+
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1748:41: NMCHAR
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
	// $ANTLR end "SASS_EXTEND_ONLY_SELECTOR"

	// $ANTLR start "EMS"
	public final void mEMS() throws RecognitionException {
		try {
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1760:25: ()
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1760:26: 
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1761:25: ()
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1761:26: 
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1762:25: ()
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1762:26: 
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1763:18: ()
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1763:19: 
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1764:25: ()
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1764:26: 
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1765:25: ()
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1765:26: 
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1766:25: ()
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1766:26: 
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1767:25: ()
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1767:26: 
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1768:25: ()
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1768:26: 
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1769:25: ()
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1769:26: 
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1772:5: ( ( ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ ) ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R ( A | E ) )=> R ( A D | E M ) | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | PERCENTAGE_SYMBOL |) )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1772:9: ( ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ ) ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R ( A | E ) )=> R ( A D | E M ) | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | PERCENTAGE_SYMBOL |)
			{
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1772:9: ( ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ )
			int alt181=2;
			int LA181_0 = input.LA(1);
			if ( ((LA181_0 >= '0' && LA181_0 <= '9')) ) {
				alt181=1;
			}
			else if ( (LA181_0=='.') ) {
				alt181=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 181, 0, input);
				throw nvae;
			}

			switch (alt181) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1773:15: ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )?
					{
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1773:15: ( '0' .. '9' )+
					int cnt177=0;
					loop177:
					while (true) {
						int alt177=2;
						int LA177_0 = input.LA(1);
						if ( ((LA177_0 >= '0' && LA177_0 <= '9')) ) {
							alt177=1;
						}

						switch (alt177) {
						case 1 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
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
							if ( cnt177 >= 1 ) break loop177;
							if (state.backtracking>0) {state.failed=true; return;}
							EarlyExitException eee = new EarlyExitException(177, input);
							throw eee;
						}
						cnt177++;
					}

					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1773:25: ( '.' ( '0' .. '9' )+ )?
					int alt179=2;
					int LA179_0 = input.LA(1);
					if ( (LA179_0=='.') ) {
						alt179=1;
					}
					switch (alt179) {
						case 1 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1773:26: '.' ( '0' .. '9' )+
							{
							match('.'); if (state.failed) return;
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1773:30: ( '0' .. '9' )+
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
									// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
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

							}
							break;

					}

					}
					break;
				case 2 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1774:15: '.' ( '0' .. '9' )+
					{
					match('.'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1774:19: ( '0' .. '9' )+
					int cnt180=0;
					loop180:
					while (true) {
						int alt180=2;
						int LA180_0 = input.LA(1);
						if ( ((LA180_0 >= '0' && LA180_0 <= '9')) ) {
							alt180=1;
						}

						switch (alt180) {
						case 1 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
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
							if ( cnt180 >= 1 ) break loop180;
							if (state.backtracking>0) {state.failed=true; return;}
							EarlyExitException eee = new EarlyExitException(180, input);
							throw eee;
						}
						cnt180++;
					}

					}
					break;

			}

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1776:9: ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R ( A | E ) )=> R ( A D | E M ) | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | PERCENTAGE_SYMBOL |)
			int alt188=13;
			alt188 = dfa188.predict(input);
			switch (alt188) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1777:15: ( D P ( I | C ) )=> D P ( I | C M )
					{
					mD(); if (state.failed) return;

					mP(); if (state.failed) return;

					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1779:17: ( I | C M )
					int alt182=2;
					switch ( input.LA(1) ) {
					case 'I':
					case 'i':
						{
						alt182=1;
						}
						break;
					case '\\':
						{
						switch ( input.LA(2) ) {
						case 'I':
						case 'i':
							{
							alt182=1;
							}
							break;
						case '0':
							{
							int LA182_4 = input.LA(3);
							if ( (LA182_4=='0') ) {
								int LA182_6 = input.LA(4);
								if ( (LA182_6=='0') ) {
									int LA182_7 = input.LA(5);
									if ( (LA182_7=='0') ) {
										int LA182_8 = input.LA(6);
										if ( (LA182_8=='4'||LA182_8=='6') ) {
											int LA182_5 = input.LA(7);
											if ( (LA182_5=='9') ) {
												alt182=1;
											}
											else if ( (LA182_5=='3') ) {
												alt182=2;
											}

											else {
												if (state.backtracking>0) {state.failed=true; return;}
												int nvaeMark = input.mark();
												try {
													for (int nvaeConsume = 0; nvaeConsume < 7 - 1; nvaeConsume++) {
														input.consume();
													}
													NoViableAltException nvae =
														new NoViableAltException("", 182, 5, input);
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
													new NoViableAltException("", 182, 8, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

									}
									else if ( (LA182_7=='4'||LA182_7=='6') ) {
										int LA182_5 = input.LA(6);
										if ( (LA182_5=='9') ) {
											alt182=1;
										}
										else if ( (LA182_5=='3') ) {
											alt182=2;
										}

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 182, 5, input);
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
												new NoViableAltException("", 182, 7, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}

								}
								else if ( (LA182_6=='4'||LA182_6=='6') ) {
									int LA182_5 = input.LA(5);
									if ( (LA182_5=='9') ) {
										alt182=1;
									}
									else if ( (LA182_5=='3') ) {
										alt182=2;
									}

									else {
										if (state.backtracking>0) {state.failed=true; return;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
												input.consume();
											}
											NoViableAltException nvae =
												new NoViableAltException("", 182, 5, input);
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
											new NoViableAltException("", 182, 6, input);
										throw nvae;
									} finally {
										input.rewind(nvaeMark);
									}
								}

							}
							else if ( (LA182_4=='4'||LA182_4=='6') ) {
								int LA182_5 = input.LA(4);
								if ( (LA182_5=='9') ) {
									alt182=1;
								}
								else if ( (LA182_5=='3') ) {
									alt182=2;
								}

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
											input.consume();
										}
										NoViableAltException nvae =
											new NoViableAltException("", 182, 5, input);
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
										new NoViableAltException("", 182, 4, input);
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
							int LA182_5 = input.LA(3);
							if ( (LA182_5=='9') ) {
								alt182=1;
							}
							else if ( (LA182_5=='3') ) {
								alt182=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 182, 5, input);
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
									new NoViableAltException("", 182, 2, input);
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
						alt182=2;
						}
						break;
					default:
						if (state.backtracking>0) {state.failed=true; return;}
						NoViableAltException nvae =
							new NoViableAltException("", 182, 0, input);
						throw nvae;
					}
					switch (alt182) {
						case 1 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1780:22: I
							{
							mI(); if (state.failed) return;

							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1780:26: C M
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1784:15: ( E ( M | X ) )=> E ( M | X )
					{
					mE(); if (state.failed) return;

					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1786:17: ( M | X )
					int alt183=2;
					switch ( input.LA(1) ) {
					case 'M':
					case 'm':
						{
						alt183=1;
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
							alt183=1;
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
										int LA183_7 = input.LA(6);
										if ( (LA183_7=='4'||LA183_7=='6') ) {
											alt183=1;
										}
										else if ( (LA183_7=='5'||LA183_7=='7') ) {
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
													new NoViableAltException("", 183, 7, input);
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
										alt183=1;
										}
										break;
									case '5':
									case '7':
										{
										alt183=2;
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
												new NoViableAltException("", 183, 6, input);
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
									alt183=1;
									}
									break;
								case '5':
								case '7':
									{
									alt183=2;
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
											new NoViableAltException("", 183, 5, input);
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
								alt183=1;
								}
								break;
							case '5':
							case '7':
								{
								alt183=2;
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
										new NoViableAltException("", 183, 4, input);
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
							alt183=2;
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
					case 'X':
					case 'x':
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1787:23: M
							{
							mM(); if (state.failed) return;

							if ( state.backtracking==0 ) { _type = EMS;          }
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1788:23: X
							{
							mX(); if (state.failed) return;

							if ( state.backtracking==0 ) { _type = EXS;          }
							}
							break;

					}

					}
					break;
				case 3 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1790:15: ( P ( X | T | C ) )=> P ( X | T | C )
					{
					mP(); if (state.failed) return;

					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1792:17: ( X | T | C )
					int alt184=3;
					switch ( input.LA(1) ) {
					case 'X':
					case 'x':
						{
						alt184=1;
						}
						break;
					case '\\':
						{
						switch ( input.LA(2) ) {
						case 'X':
						case 'x':
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
										int LA184_9 = input.LA(6);
										if ( (LA184_9=='5'||LA184_9=='7') ) {
											int LA184_6 = input.LA(7);
											if ( (LA184_6=='8') ) {
												alt184=1;
											}
											else if ( (LA184_6=='4') ) {
												alt184=2;
											}

											else {
												if (state.backtracking>0) {state.failed=true; return;}
												int nvaeMark = input.mark();
												try {
													for (int nvaeConsume = 0; nvaeConsume < 7 - 1; nvaeConsume++) {
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
										else if ( (LA184_9=='4'||LA184_9=='6') ) {
											alt184=3;
										}

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 184, 9, input);
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
										int LA184_6 = input.LA(6);
										if ( (LA184_6=='8') ) {
											alt184=1;
										}
										else if ( (LA184_6=='4') ) {
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
										alt184=3;
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
												new NoViableAltException("", 184, 8, input);
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
									int LA184_6 = input.LA(5);
									if ( (LA184_6=='8') ) {
										alt184=1;
									}
									else if ( (LA184_6=='4') ) {
										alt184=2;
									}

									else {
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
									alt184=3;
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
											new NoViableAltException("", 184, 7, input);
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
								int LA184_6 = input.LA(4);
								if ( (LA184_6=='8') ) {
									alt184=1;
								}
								else if ( (LA184_6=='4') ) {
									alt184=2;
								}

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
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
								alt184=3;
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
										new NoViableAltException("", 184, 5, input);
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
							int LA184_6 = input.LA(3);
							if ( (LA184_6=='8') ) {
								alt184=1;
							}
							else if ( (LA184_6=='4') ) {
								alt184=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
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
						case 'T':
						case 't':
							{
							alt184=2;
							}
							break;
						case '4':
						case '6':
							{
							alt184=3;
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
					case 'T':
					case 't':
						{
						alt184=2;
						}
						break;
					case 'C':
					case 'c':
						{
						alt184=3;
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1793:23: X
							{
							mX(); if (state.failed) return;

							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1794:23: T
							{
							mT(); if (state.failed) return;

							}
							break;
						case 3 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1795:23: C
							{
							mC(); if (state.failed) return;

							}
							break;

					}

					if ( state.backtracking==0 ) { _type = LENGTH;       }
					}
					break;
				case 4 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1798:15: ( C M )=> C M
					{
					mC(); if (state.failed) return;

					mM(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = LENGTH;       }
					}
					break;
				case 5 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1800:15: ( M ( M | S ) )=> M ( M | S )
					{
					mM(); if (state.failed) return;

					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1802:17: ( M | S )
					int alt185=2;
					switch ( input.LA(1) ) {
					case 'M':
					case 'm':
						{
						alt185=1;
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
										int LA185_7 = input.LA(6);
										if ( (LA185_7=='4'||LA185_7=='6') ) {
											alt185=1;
										}
										else if ( (LA185_7=='5'||LA185_7=='7') ) {
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
													new NoViableAltException("", 185, 7, input);
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
										alt185=1;
										}
										break;
									case '5':
									case '7':
										{
										alt185=2;
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
									alt185=1;
									}
									break;
								case '5':
								case '7':
									{
									alt185=2;
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
											new NoViableAltException("", 185, 5, input);
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
								alt185=1;
								}
								break;
							case '5':
							case '7':
								{
								alt185=2;
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
										new NoViableAltException("", 185, 4, input);
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
							alt185=2;
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
					case 'S':
					case 's':
						{
						alt185=2;
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1803:23: M
							{
							mM(); if (state.failed) return;

							if ( state.backtracking==0 ) { _type = LENGTH;       }
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1805:23: S
							{
							mS(); if (state.failed) return;

							if ( state.backtracking==0 ) { _type = TIME;         }
							}
							break;

					}

					}
					break;
				case 6 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1807:15: ( I N )=> I N
					{
					mI(); if (state.failed) return;

					mN(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = LENGTH;       }
					}
					break;
				case 7 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1810:15: ( D E G )=> D E G
					{
					mD(); if (state.failed) return;

					mE(); if (state.failed) return;

					mG(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = ANGLE;        }
					}
					break;
				case 8 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1815:15: ( R ( A | E ) )=> R ( A D | E M )
					{
					mR(); if (state.failed) return;

					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1817:17: ( A D | E M )
					int alt186=2;
					switch ( input.LA(1) ) {
					case 'A':
					case 'a':
						{
						alt186=1;
						}
						break;
					case '\\':
						{
						int LA186_2 = input.LA(2);
						if ( (LA186_2=='0') ) {
							int LA186_4 = input.LA(3);
							if ( (LA186_4=='0') ) {
								int LA186_6 = input.LA(4);
								if ( (LA186_6=='0') ) {
									int LA186_7 = input.LA(5);
									if ( (LA186_7=='0') ) {
										int LA186_8 = input.LA(6);
										if ( (LA186_8=='4'||LA186_8=='6') ) {
											int LA186_5 = input.LA(7);
											if ( (LA186_5=='1') ) {
												alt186=1;
											}
											else if ( (LA186_5=='5') ) {
												alt186=2;
											}

											else {
												if (state.backtracking>0) {state.failed=true; return;}
												int nvaeMark = input.mark();
												try {
													for (int nvaeConsume = 0; nvaeConsume < 7 - 1; nvaeConsume++) {
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

										else {
											if (state.backtracking>0) {state.failed=true; return;}
											int nvaeMark = input.mark();
											try {
												for (int nvaeConsume = 0; nvaeConsume < 6 - 1; nvaeConsume++) {
													input.consume();
												}
												NoViableAltException nvae =
													new NoViableAltException("", 186, 8, input);
												throw nvae;
											} finally {
												input.rewind(nvaeMark);
											}
										}

									}
									else if ( (LA186_7=='4'||LA186_7=='6') ) {
										int LA186_5 = input.LA(6);
										if ( (LA186_5=='1') ) {
											alt186=1;
										}
										else if ( (LA186_5=='5') ) {
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
													new NoViableAltException("", 186, 5, input);
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
												new NoViableAltException("", 186, 7, input);
											throw nvae;
										} finally {
											input.rewind(nvaeMark);
										}
									}

								}
								else if ( (LA186_6=='4'||LA186_6=='6') ) {
									int LA186_5 = input.LA(5);
									if ( (LA186_5=='1') ) {
										alt186=1;
									}
									else if ( (LA186_5=='5') ) {
										alt186=2;
									}

									else {
										if (state.backtracking>0) {state.failed=true; return;}
										int nvaeMark = input.mark();
										try {
											for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
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

								else {
									if (state.backtracking>0) {state.failed=true; return;}
									int nvaeMark = input.mark();
									try {
										for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
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
							else if ( (LA186_4=='4'||LA186_4=='6') ) {
								int LA186_5 = input.LA(4);
								if ( (LA186_5=='1') ) {
									alt186=1;
								}
								else if ( (LA186_5=='5') ) {
									alt186=2;
								}

								else {
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

							else {
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
						else if ( (LA186_2=='4'||LA186_2=='6') ) {
							int LA186_5 = input.LA(3);
							if ( (LA186_5=='1') ) {
								alt186=1;
							}
							else if ( (LA186_5=='5') ) {
								alt186=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
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

						else {
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
					case 'E':
					case 'e':
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
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1818:20: A D
							{
							mA(); if (state.failed) return;

							mD(); if (state.failed) return;

							if ( state.backtracking==0 ) {_type = ANGLE;         }
							}
							break;
						case 2 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1819:20: E M
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1822:15: ( S )=> S
					{
					mS(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = TIME;         }
					}
					break;
				case 10 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1824:15: ( ( K )? H Z )=> ( K )? H Z
					{
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1825:17: ( K )?
					int alt187=2;
					int LA187_0 = input.LA(1);
					if ( (LA187_0=='K'||LA187_0=='k') ) {
						alt187=1;
					}
					else if ( (LA187_0=='\\') ) {
						switch ( input.LA(2) ) {
							case 'K':
							case 'k':
								{
								alt187=1;
								}
								break;
							case '0':
								{
								int LA187_4 = input.LA(3);
								if ( (LA187_4=='0') ) {
									int LA187_6 = input.LA(4);
									if ( (LA187_6=='0') ) {
										int LA187_7 = input.LA(5);
										if ( (LA187_7=='0') ) {
											int LA187_8 = input.LA(6);
											if ( (LA187_8=='4'||LA187_8=='6') ) {
												int LA187_5 = input.LA(7);
												if ( (LA187_5=='B'||LA187_5=='b') ) {
													alt187=1;
												}
											}
										}
										else if ( (LA187_7=='4'||LA187_7=='6') ) {
											int LA187_5 = input.LA(6);
											if ( (LA187_5=='B'||LA187_5=='b') ) {
												alt187=1;
											}
										}
									}
									else if ( (LA187_6=='4'||LA187_6=='6') ) {
										int LA187_5 = input.LA(5);
										if ( (LA187_5=='B'||LA187_5=='b') ) {
											alt187=1;
										}
									}
								}
								else if ( (LA187_4=='4'||LA187_4=='6') ) {
									int LA187_5 = input.LA(4);
									if ( (LA187_5=='B'||LA187_5=='b') ) {
										alt187=1;
									}
								}
								}
								break;
							case '4':
							case '6':
								{
								int LA187_5 = input.LA(3);
								if ( (LA187_5=='B'||LA187_5=='b') ) {
									alt187=1;
								}
								}
								break;
						}
					}
					switch (alt187) {
						case 1 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1825:17: K
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1827:15: IDENT
					{
					mIDENT(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = DIMENSION;    }
					}
					break;
				case 12 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1829:15: PERCENTAGE_SYMBOL
					{
					mPERCENTAGE_SYMBOL(); if (state.failed) return;

					if ( state.backtracking==0 ) { _type = PERCENTAGE;   }
					}
					break;
				case 13 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1832:9: 
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1838:5: ( U R L '(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1838:9: U R L '(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')'
			{
			mU(); if (state.failed) return;

			mR(); if (state.failed) return;

			mL(); if (state.failed) return;

			match('('); if (state.failed) return;
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1840:13: ( ( WS )=> WS )?
			int alt189=2;
			int LA189_0 = input.LA(1);
			if ( (LA189_0=='\t'||LA189_0==' ') ) {
				int LA189_1 = input.LA(2);
				if ( (synpred11_Css3()) ) {
					alt189=1;
				}
			}
			switch (alt189) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1840:14: ( WS )=> WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1840:25: ( URL | STRING )
			int alt190=2;
			int LA190_0 = input.LA(1);
			if ( (LA190_0=='\t'||(LA190_0 >= ' ' && LA190_0 <= '!')||(LA190_0 >= '#' && LA190_0 <= '&')||(LA190_0 >= ')' && LA190_0 <= ';')||LA190_0=='='||(LA190_0 >= '?' && LA190_0 <= '\\')||LA190_0=='_'||(LA190_0 >= 'a' && LA190_0 <= 'z')||LA190_0=='|'||LA190_0=='~'||(LA190_0 >= '\u0080' && LA190_0 <= '\uFFFF')) ) {
				alt190=1;
			}
			else if ( (LA190_0=='\"'||LA190_0=='\'') ) {
				alt190=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 190, 0, input);
				throw nvae;
			}

			switch (alt190) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1840:26: URL
					{
					mURL(); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1840:30: STRING
					{
					mSTRING(); if (state.failed) return;

					}
					break;

			}

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1840:38: ( WS )?
			int alt191=2;
			int LA191_0 = input.LA(1);
			if ( (LA191_0=='\t'||LA191_0==' ') ) {
				alt191=1;
			}
			switch (alt191) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1840:38: WS
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1845:2: ( 'URL-PREFIX(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1846:2: 'URL-PREFIX(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')'
			{
			match("URL-PREFIX("); if (state.failed) return;

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1847:13: ( ( WS )=> WS )?
			int alt192=2;
			int LA192_0 = input.LA(1);
			if ( (LA192_0=='\t'||LA192_0==' ') ) {
				int LA192_1 = input.LA(2);
				if ( (synpred12_Css3()) ) {
					alt192=1;
				}
			}
			switch (alt192) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1847:14: ( WS )=> WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1847:25: ( URL | STRING )
			int alt193=2;
			int LA193_0 = input.LA(1);
			if ( (LA193_0=='\t'||(LA193_0 >= ' ' && LA193_0 <= '!')||(LA193_0 >= '#' && LA193_0 <= '&')||(LA193_0 >= ')' && LA193_0 <= ';')||LA193_0=='='||(LA193_0 >= '?' && LA193_0 <= '\\')||LA193_0=='_'||(LA193_0 >= 'a' && LA193_0 <= 'z')||LA193_0=='|'||LA193_0=='~'||(LA193_0 >= '\u0080' && LA193_0 <= '\uFFFF')) ) {
				alt193=1;
			}
			else if ( (LA193_0=='\"'||LA193_0=='\'') ) {
				alt193=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 193, 0, input);
				throw nvae;
			}

			switch (alt193) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1847:26: URL
					{
					mURL(); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1847:30: STRING
					{
					mSTRING(); if (state.failed) return;

					}
					break;

			}

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1847:38: ( WS )?
			int alt194=2;
			int LA194_0 = input.LA(1);
			if ( (LA194_0=='\t'||LA194_0==' ') ) {
				alt194=1;
			}
			switch (alt194) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1847:38: WS
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1853:2: ( 'DOMAIN(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1854:2: 'DOMAIN(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')'
			{
			match("DOMAIN("); if (state.failed) return;

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:13: ( ( WS )=> WS )?
			int alt195=2;
			int LA195_0 = input.LA(1);
			if ( (LA195_0=='\t'||LA195_0==' ') ) {
				int LA195_1 = input.LA(2);
				if ( (synpred13_Css3()) ) {
					alt195=1;
				}
			}
			switch (alt195) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:14: ( WS )=> WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:25: ( URL | STRING )
			int alt196=2;
			int LA196_0 = input.LA(1);
			if ( (LA196_0=='\t'||(LA196_0 >= ' ' && LA196_0 <= '!')||(LA196_0 >= '#' && LA196_0 <= '&')||(LA196_0 >= ')' && LA196_0 <= ';')||LA196_0=='='||(LA196_0 >= '?' && LA196_0 <= '\\')||LA196_0=='_'||(LA196_0 >= 'a' && LA196_0 <= 'z')||LA196_0=='|'||LA196_0=='~'||(LA196_0 >= '\u0080' && LA196_0 <= '\uFFFF')) ) {
				alt196=1;
			}
			else if ( (LA196_0=='\"'||LA196_0=='\'') ) {
				alt196=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 196, 0, input);
				throw nvae;
			}

			switch (alt196) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:26: URL
					{
					mURL(); if (state.failed) return;

					}
					break;
				case 2 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:30: STRING
					{
					mSTRING(); if (state.failed) return;

					}
					break;

			}

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:38: ( WS )?
			int alt197=2;
			int LA197_0 = input.LA(1);
			if ( (LA197_0=='\t'||LA197_0==' ') ) {
				alt197=1;
			}
			switch (alt197) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:38: WS
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1861:2: ( 'REGEXP(' ( ( WS )=> WS )? STRING ( WS )? ')' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1862:2: 'REGEXP(' ( ( WS )=> WS )? STRING ( WS )? ')'
			{
			match("REGEXP("); if (state.failed) return;

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1863:13: ( ( WS )=> WS )?
			int alt198=2;
			int LA198_0 = input.LA(1);
			if ( (LA198_0=='\t'||LA198_0==' ') && (synpred14_Css3())) {
				alt198=1;
			}
			switch (alt198) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1863:14: ( WS )=> WS
					{
					mWS(); if (state.failed) return;

					}
					break;

			}

			mSTRING(); if (state.failed) return;

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1863:32: ( WS )?
			int alt199=2;
			int LA199_0 = input.LA(1);
			if ( (LA199_0=='\t'||LA199_0==' ') ) {
				alt199=1;
			}
			switch (alt199) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1863:32: WS
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1874:5: ( ( ' ' | '\\t' )+ )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1875:5: ( ' ' | '\\t' )+
			{
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1875:5: ( ' ' | '\\t' )+
			int cnt200=0;
			loop200:
			while (true) {
				int alt200=2;
				int LA200_0 = input.LA(1);
				if ( (LA200_0=='\t'||LA200_0==' ') ) {
					alt200=1;
				}

				switch (alt200) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
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
					if ( cnt200 >= 1 ) break loop200;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(200, input);
					throw eee;
				}
				cnt200++;
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1879:5: ( ( '\\r' ( '\\n' )? | '\\n' ) )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1880:5: ( '\\r' ( '\\n' )? | '\\n' )
			{
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1880:5: ( '\\r' ( '\\n' )? | '\\n' )
			int alt202=2;
			int LA202_0 = input.LA(1);
			if ( (LA202_0=='\r') ) {
				alt202=1;
			}
			else if ( (LA202_0=='\n') ) {
				alt202=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 202, 0, input);
				throw nvae;
			}

			switch (alt202) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1880:6: '\\r' ( '\\n' )?
					{
					match('\r'); if (state.failed) return;
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1880:11: ( '\\n' )?
					int alt201=2;
					int LA201_0 = input.LA(1);
					if ( (LA201_0=='\n') ) {
						alt201=1;
					}
					switch (alt201) {
						case 1 :
							// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1880:11: '\\n'
							{
							match('\n'); if (state.failed) return;
							}
							break;

					}

					}
					break;
				case 2 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1880:19: '\\n'
					{
					match('\n'); if (state.failed) return;
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
	// $ANTLR end "NL"

	// $ANTLR start "COMMENT"
	public final void mCOMMENT() throws RecognitionException {
		try {
			int _type = COMMENT;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1886:5: ( '/*' ( options {greedy=false; } : ( . )* ) '*/' )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1887:5: '/*' ( options {greedy=false; } : ( . )* ) '*/'
			{
			match("/*"); if (state.failed) return;

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1887:10: ( options {greedy=false; } : ( . )* )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1887:40: ( . )*
			{
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1887:40: ( . )*
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
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1887:40: .
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
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1891:5: ( '//' ( options {greedy=false; } : (~ ( '\\r' | '\\n' ) )* ) )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1892:5: '//' ( options {greedy=false; } : (~ ( '\\r' | '\\n' ) )* )
			{
			match("//"); if (state.failed) return;

			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1892:9: ( options {greedy=false; } : (~ ( '\\r' | '\\n' ) )* )
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1892:39: (~ ( '\\r' | '\\n' ) )*
			{
			// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1892:39: (~ ( '\\r' | '\\n' ) )*
			loop204:
			while (true) {
				int alt204=2;
				int LA204_0 = input.LA(1);
				if ( ((LA204_0 >= '\u0000' && LA204_0 <= '\t')||(LA204_0 >= '\u000B' && LA204_0 <= '\f')||(LA204_0 >= '\u000E' && LA204_0 <= '\uFFFF')) ) {
					alt204=1;
				}

				switch (alt204) {
				case 1 :
					// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
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
				_channel = HIDDEN;
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
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:8: ( GEN | CDO | CDC | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS | GREATER | LBRACE | RBRACE | LBRACKET | RBRACKET | OPEQ | SEMI | COLON | DCOLON | SOLIDUS | MINUS | PLUS | STAR | LPAREN | RPAREN | COMMA | DOT | TILDE | PIPE | PERCENTAGE_SYMBOL | EXCLAMATION_MARK | CP_EQ | CP_NOT_EQ | LESS | GREATER_OR_EQ | LESS_OR_EQ | LESS_WHEN | LESS_AND | CP_DOTS | LESS_REST | STRING | LESS_JS_STRING | ONLY | NOT | AND | OR | IDENT | HASH_SYMBOL | HASH | IMPORTANT_SYM | IMPORT_SYM | PAGE_SYM | MEDIA_SYM | NAMESPACE_SYM | CHARSET_SYM | COUNTER_STYLE_SYM | FONT_FACE_SYM | TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM | MOZ_DOCUMENT_SYM | WEBKIT_KEYFRAMES_SYM | SASS_CONTENT | SASS_MIXIN | SASS_INCLUDE | SASS_EXTEND | SASS_DEBUG | SASS_WARN | SASS_IF | SASS_ELSE | SASS_FOR | SASS_FUNCTION | SASS_RETURN | SASS_EACH | SASS_WHILE | AT_SIGN | AT_IDENT | SASS_VAR | SASS_DEFAULT | SASS_OPTIONAL | SASS_EXTEND_ONLY_SELECTOR | NUMBER | URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP | WS | NL | COMMENT | LINE_COMMENT )
		int alt205=101;
		alt205 = dfa205.predict(input);
		switch (alt205) {
			case 1 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:10: GEN
				{
				mGEN(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:14: CDO
				{
				mCDO(); if (state.failed) return;

				}
				break;
			case 3 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:18: CDC
				{
				mCDC(); if (state.failed) return;

				}
				break;
			case 4 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:22: INCLUDES
				{
				mINCLUDES(); if (state.failed) return;

				}
				break;
			case 5 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:31: DASHMATCH
				{
				mDASHMATCH(); if (state.failed) return;

				}
				break;
			case 6 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:41: BEGINS
				{
				mBEGINS(); if (state.failed) return;

				}
				break;
			case 7 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:48: ENDS
				{
				mENDS(); if (state.failed) return;

				}
				break;
			case 8 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:53: CONTAINS
				{
				mCONTAINS(); if (state.failed) return;

				}
				break;
			case 9 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:62: GREATER
				{
				mGREATER(); if (state.failed) return;

				}
				break;
			case 10 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:70: LBRACE
				{
				mLBRACE(); if (state.failed) return;

				}
				break;
			case 11 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:77: RBRACE
				{
				mRBRACE(); if (state.failed) return;

				}
				break;
			case 12 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:84: LBRACKET
				{
				mLBRACKET(); if (state.failed) return;

				}
				break;
			case 13 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:93: RBRACKET
				{
				mRBRACKET(); if (state.failed) return;

				}
				break;
			case 14 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:102: OPEQ
				{
				mOPEQ(); if (state.failed) return;

				}
				break;
			case 15 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:107: SEMI
				{
				mSEMI(); if (state.failed) return;

				}
				break;
			case 16 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:112: COLON
				{
				mCOLON(); if (state.failed) return;

				}
				break;
			case 17 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:118: DCOLON
				{
				mDCOLON(); if (state.failed) return;

				}
				break;
			case 18 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:125: SOLIDUS
				{
				mSOLIDUS(); if (state.failed) return;

				}
				break;
			case 19 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:133: MINUS
				{
				mMINUS(); if (state.failed) return;

				}
				break;
			case 20 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:139: PLUS
				{
				mPLUS(); if (state.failed) return;

				}
				break;
			case 21 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:144: STAR
				{
				mSTAR(); if (state.failed) return;

				}
				break;
			case 22 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:149: LPAREN
				{
				mLPAREN(); if (state.failed) return;

				}
				break;
			case 23 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:156: RPAREN
				{
				mRPAREN(); if (state.failed) return;

				}
				break;
			case 24 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:163: COMMA
				{
				mCOMMA(); if (state.failed) return;

				}
				break;
			case 25 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:169: DOT
				{
				mDOT(); if (state.failed) return;

				}
				break;
			case 26 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:173: TILDE
				{
				mTILDE(); if (state.failed) return;

				}
				break;
			case 27 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:179: PIPE
				{
				mPIPE(); if (state.failed) return;

				}
				break;
			case 28 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:184: PERCENTAGE_SYMBOL
				{
				mPERCENTAGE_SYMBOL(); if (state.failed) return;

				}
				break;
			case 29 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:202: EXCLAMATION_MARK
				{
				mEXCLAMATION_MARK(); if (state.failed) return;

				}
				break;
			case 30 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:219: CP_EQ
				{
				mCP_EQ(); if (state.failed) return;

				}
				break;
			case 31 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:225: CP_NOT_EQ
				{
				mCP_NOT_EQ(); if (state.failed) return;

				}
				break;
			case 32 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:235: LESS
				{
				mLESS(); if (state.failed) return;

				}
				break;
			case 33 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:240: GREATER_OR_EQ
				{
				mGREATER_OR_EQ(); if (state.failed) return;

				}
				break;
			case 34 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:254: LESS_OR_EQ
				{
				mLESS_OR_EQ(); if (state.failed) return;

				}
				break;
			case 35 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:265: LESS_WHEN
				{
				mLESS_WHEN(); if (state.failed) return;

				}
				break;
			case 36 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:275: LESS_AND
				{
				mLESS_AND(); if (state.failed) return;

				}
				break;
			case 37 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:284: CP_DOTS
				{
				mCP_DOTS(); if (state.failed) return;

				}
				break;
			case 38 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:292: LESS_REST
				{
				mLESS_REST(); if (state.failed) return;

				}
				break;
			case 39 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:302: STRING
				{
				mSTRING(); if (state.failed) return;

				}
				break;
			case 40 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:309: LESS_JS_STRING
				{
				mLESS_JS_STRING(); if (state.failed) return;

				}
				break;
			case 41 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:324: ONLY
				{
				mONLY(); if (state.failed) return;

				}
				break;
			case 42 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:329: NOT
				{
				mNOT(); if (state.failed) return;

				}
				break;
			case 43 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:333: AND
				{
				mAND(); if (state.failed) return;

				}
				break;
			case 44 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:337: OR
				{
				mOR(); if (state.failed) return;

				}
				break;
			case 45 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:340: IDENT
				{
				mIDENT(); if (state.failed) return;

				}
				break;
			case 46 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:346: HASH_SYMBOL
				{
				mHASH_SYMBOL(); if (state.failed) return;

				}
				break;
			case 47 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:358: HASH
				{
				mHASH(); if (state.failed) return;

				}
				break;
			case 48 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:363: IMPORTANT_SYM
				{
				mIMPORTANT_SYM(); if (state.failed) return;

				}
				break;
			case 49 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:377: IMPORT_SYM
				{
				mIMPORT_SYM(); if (state.failed) return;

				}
				break;
			case 50 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:388: PAGE_SYM
				{
				mPAGE_SYM(); if (state.failed) return;

				}
				break;
			case 51 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:397: MEDIA_SYM
				{
				mMEDIA_SYM(); if (state.failed) return;

				}
				break;
			case 52 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:407: NAMESPACE_SYM
				{
				mNAMESPACE_SYM(); if (state.failed) return;

				}
				break;
			case 53 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:421: CHARSET_SYM
				{
				mCHARSET_SYM(); if (state.failed) return;

				}
				break;
			case 54 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:433: COUNTER_STYLE_SYM
				{
				mCOUNTER_STYLE_SYM(); if (state.failed) return;

				}
				break;
			case 55 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:451: FONT_FACE_SYM
				{
				mFONT_FACE_SYM(); if (state.failed) return;

				}
				break;
			case 56 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:465: TOPLEFTCORNER_SYM
				{
				mTOPLEFTCORNER_SYM(); if (state.failed) return;

				}
				break;
			case 57 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:483: TOPLEFT_SYM
				{
				mTOPLEFT_SYM(); if (state.failed) return;

				}
				break;
			case 58 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:495: TOPCENTER_SYM
				{
				mTOPCENTER_SYM(); if (state.failed) return;

				}
				break;
			case 59 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:509: TOPRIGHT_SYM
				{
				mTOPRIGHT_SYM(); if (state.failed) return;

				}
				break;
			case 60 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:522: TOPRIGHTCORNER_SYM
				{
				mTOPRIGHTCORNER_SYM(); if (state.failed) return;

				}
				break;
			case 61 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:541: BOTTOMLEFTCORNER_SYM
				{
				mBOTTOMLEFTCORNER_SYM(); if (state.failed) return;

				}
				break;
			case 62 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:562: BOTTOMLEFT_SYM
				{
				mBOTTOMLEFT_SYM(); if (state.failed) return;

				}
				break;
			case 63 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:577: BOTTOMCENTER_SYM
				{
				mBOTTOMCENTER_SYM(); if (state.failed) return;

				}
				break;
			case 64 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:594: BOTTOMRIGHT_SYM
				{
				mBOTTOMRIGHT_SYM(); if (state.failed) return;

				}
				break;
			case 65 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:610: BOTTOMRIGHTCORNER_SYM
				{
				mBOTTOMRIGHTCORNER_SYM(); if (state.failed) return;

				}
				break;
			case 66 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:632: LEFTTOP_SYM
				{
				mLEFTTOP_SYM(); if (state.failed) return;

				}
				break;
			case 67 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:644: LEFTMIDDLE_SYM
				{
				mLEFTMIDDLE_SYM(); if (state.failed) return;

				}
				break;
			case 68 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:659: LEFTBOTTOM_SYM
				{
				mLEFTBOTTOM_SYM(); if (state.failed) return;

				}
				break;
			case 69 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:674: RIGHTTOP_SYM
				{
				mRIGHTTOP_SYM(); if (state.failed) return;

				}
				break;
			case 70 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:687: RIGHTMIDDLE_SYM
				{
				mRIGHTMIDDLE_SYM(); if (state.failed) return;

				}
				break;
			case 71 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:703: RIGHTBOTTOM_SYM
				{
				mRIGHTBOTTOM_SYM(); if (state.failed) return;

				}
				break;
			case 72 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:719: MOZ_DOCUMENT_SYM
				{
				mMOZ_DOCUMENT_SYM(); if (state.failed) return;

				}
				break;
			case 73 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:736: WEBKIT_KEYFRAMES_SYM
				{
				mWEBKIT_KEYFRAMES_SYM(); if (state.failed) return;

				}
				break;
			case 74 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:757: SASS_CONTENT
				{
				mSASS_CONTENT(); if (state.failed) return;

				}
				break;
			case 75 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:770: SASS_MIXIN
				{
				mSASS_MIXIN(); if (state.failed) return;

				}
				break;
			case 76 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:781: SASS_INCLUDE
				{
				mSASS_INCLUDE(); if (state.failed) return;

				}
				break;
			case 77 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:794: SASS_EXTEND
				{
				mSASS_EXTEND(); if (state.failed) return;

				}
				break;
			case 78 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:806: SASS_DEBUG
				{
				mSASS_DEBUG(); if (state.failed) return;

				}
				break;
			case 79 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:817: SASS_WARN
				{
				mSASS_WARN(); if (state.failed) return;

				}
				break;
			case 80 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:827: SASS_IF
				{
				mSASS_IF(); if (state.failed) return;

				}
				break;
			case 81 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:835: SASS_ELSE
				{
				mSASS_ELSE(); if (state.failed) return;

				}
				break;
			case 82 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:845: SASS_FOR
				{
				mSASS_FOR(); if (state.failed) return;

				}
				break;
			case 83 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:854: SASS_FUNCTION
				{
				mSASS_FUNCTION(); if (state.failed) return;

				}
				break;
			case 84 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:868: SASS_RETURN
				{
				mSASS_RETURN(); if (state.failed) return;

				}
				break;
			case 85 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:880: SASS_EACH
				{
				mSASS_EACH(); if (state.failed) return;

				}
				break;
			case 86 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:890: SASS_WHILE
				{
				mSASS_WHILE(); if (state.failed) return;

				}
				break;
			case 87 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:901: AT_SIGN
				{
				mAT_SIGN(); if (state.failed) return;

				}
				break;
			case 88 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:909: AT_IDENT
				{
				mAT_IDENT(); if (state.failed) return;

				}
				break;
			case 89 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:918: SASS_VAR
				{
				mSASS_VAR(); if (state.failed) return;

				}
				break;
			case 90 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:927: SASS_DEFAULT
				{
				mSASS_DEFAULT(); if (state.failed) return;

				}
				break;
			case 91 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:940: SASS_OPTIONAL
				{
				mSASS_OPTIONAL(); if (state.failed) return;

				}
				break;
			case 92 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:954: SASS_EXTEND_ONLY_SELECTOR
				{
				mSASS_EXTEND_ONLY_SELECTOR(); if (state.failed) return;

				}
				break;
			case 93 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:980: NUMBER
				{
				mNUMBER(); if (state.failed) return;

				}
				break;
			case 94 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:987: URI
				{
				mURI(); if (state.failed) return;

				}
				break;
			case 95 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:991: MOZ_URL_PREFIX
				{
				mMOZ_URL_PREFIX(); if (state.failed) return;

				}
				break;
			case 96 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1006: MOZ_DOMAIN
				{
				mMOZ_DOMAIN(); if (state.failed) return;

				}
				break;
			case 97 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1017: MOZ_REGEXP
				{
				mMOZ_REGEXP(); if (state.failed) return;

				}
				break;
			case 98 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1028: WS
				{
				mWS(); if (state.failed) return;

				}
				break;
			case 99 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1031: NL
				{
				mNL(); if (state.failed) return;

				}
				break;
			case 100 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1034: COMMENT
				{
				mCOMMENT(); if (state.failed) return;

				}
				break;
			case 101 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:1042: LINE_COMMENT
				{
				mLINE_COMMENT(); if (state.failed) return;

				}
				break;

		}
	}

	// $ANTLR start synpred1_Css3
	public final void synpred1_Css3_fragment() throws RecognitionException {
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1777:15: ( D P ( I | C ) )
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1777:16: D P ( I | C )
		{
		mD(); if (state.failed) return;

		mP(); if (state.failed) return;

		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1777:20: ( I | C )
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
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1777:21: I
				{
				mI(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1777:23: C
				{
				mC(); if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred1_Css3

	// $ANTLR start synpred2_Css3
	public final void synpred2_Css3_fragment() throws RecognitionException {
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1784:15: ( E ( M | X ) )
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1784:16: E ( M | X )
		{
		mE(); if (state.failed) return;

		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1784:18: ( M | X )
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
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1784:19: M
				{
				mM(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1784:21: X
				{
				mX(); if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred2_Css3

	// $ANTLR start synpred3_Css3
	public final void synpred3_Css3_fragment() throws RecognitionException {
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1790:15: ( P ( X | T | C ) )
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1790:16: P ( X | T | C )
		{
		mP(); if (state.failed) return;

		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1790:17: ( X | T | C )
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
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1790:18: X
				{
				mX(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1790:20: T
				{
				mT(); if (state.failed) return;

				}
				break;
			case 3 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1790:22: C
				{
				mC(); if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred3_Css3

	// $ANTLR start synpred4_Css3
	public final void synpred4_Css3_fragment() throws RecognitionException {
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1798:15: ( C M )
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1798:16: C M
		{
		mC(); if (state.failed) return;

		mM(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred4_Css3

	// $ANTLR start synpred5_Css3
	public final void synpred5_Css3_fragment() throws RecognitionException {
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1800:15: ( M ( M | S ) )
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1800:16: M ( M | S )
		{
		mM(); if (state.failed) return;

		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1800:18: ( M | S )
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
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1800:19: M
				{
				mM(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1800:21: S
				{
				mS(); if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred5_Css3

	// $ANTLR start synpred6_Css3
	public final void synpred6_Css3_fragment() throws RecognitionException {
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1807:15: ( I N )
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1807:16: I N
		{
		mI(); if (state.failed) return;

		mN(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred6_Css3

	// $ANTLR start synpred7_Css3
	public final void synpred7_Css3_fragment() throws RecognitionException {
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1810:15: ( D E G )
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1810:16: D E G
		{
		mD(); if (state.failed) return;

		mE(); if (state.failed) return;

		mG(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred7_Css3

	// $ANTLR start synpred8_Css3
	public final void synpred8_Css3_fragment() throws RecognitionException {
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1815:15: ( R ( A | E ) )
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1815:16: R ( A | E )
		{
		mR(); if (state.failed) return;

		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1815:18: ( A | E )
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
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1815:19: A
				{
				mA(); if (state.failed) return;

				}
				break;
			case 2 :
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1815:21: E
				{
				mE(); if (state.failed) return;

				}
				break;

		}

		}

	}
	// $ANTLR end synpred8_Css3

	// $ANTLR start synpred9_Css3
	public final void synpred9_Css3_fragment() throws RecognitionException {
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1822:15: ( S )
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1822:16: S
		{
		mS(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred9_Css3

	// $ANTLR start synpred10_Css3
	public final void synpred10_Css3_fragment() throws RecognitionException {
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1824:15: ( ( K )? H Z )
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1824:16: ( K )? H Z
		{
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1824:16: ( K )?
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
				// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1824:16: K
				{
				mK(); if (state.failed) return;

				}
				break;

		}

		mH(); if (state.failed) return;

		mZ(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred10_Css3

	// $ANTLR start synpred11_Css3
	public final void synpred11_Css3_fragment() throws RecognitionException {
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1840:14: ( WS )
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1840:15: WS
		{
		mWS(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred11_Css3

	// $ANTLR start synpred12_Css3
	public final void synpred12_Css3_fragment() throws RecognitionException {
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1847:14: ( WS )
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1847:15: WS
		{
		mWS(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred12_Css3

	// $ANTLR start synpred13_Css3
	public final void synpred13_Css3_fragment() throws RecognitionException {
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:14: ( WS )
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1855:15: WS
		{
		mWS(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred13_Css3

	// $ANTLR start synpred14_Css3
	public final void synpred14_Css3_fragment() throws RecognitionException {
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1863:14: ( WS )
		// /home/warden/NetBeansProjects/web-main-new/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1863:15: WS
		{
		mWS(); if (state.failed) return;

		}

	}
	// $ANTLR end synpred14_Css3

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


	protected DFA188 dfa188 = new DFA188(this);
	protected DFA205 dfa205 = new DFA205(this);
	static final String DFA188_eotS =
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
	static final String DFA188_eofS =
		"\u0349\uffff";
	static final String DFA188_minS =
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
	static final String DFA188_maxS =
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
	static final String DFA188_acceptS =
		"\14\uffff\1\13\12\uffff\1\14\1\15\62\uffff\1\11\126\uffff\1\2\7\uffff"+
		"\1\3\7\uffff\1\4\4\uffff\1\5\7\uffff\1\6\30\uffff\1\12\4\uffff\1\1\22"+
		"\uffff\1\7\113\uffff\1\10\u020c\uffff";
	static final String DFA188_specialS =
		"\2\uffff\1\u00c4\6\uffff\1\u0111\12\uffff\1\u0110\5\uffff\1\122\16\uffff"+
		"\1\u0138\1\u0139\4\uffff\1\u013d\1\u00b3\1\137\1\u0144\1\130\1\113\1\173"+
		"\1\u0186\1\u00b8\1\101\1\u01ac\1\u00be\1\u0150\1\112\1\u0159\1\u00fc\1"+
		"\u0155\1\u0190\1\u00ec\1\u019d\1\u0172\1\155\1\u0169\1\uffff\1\u017f\5"+
		"\uffff\1\27\1\uffff\1\62\1\u0149\1\40\1\u013c\1\136\1\u0143\1\127\1\111"+
		"\1\u0185\1\u00b7\1\100\1\u01ab\1\u00bd\1\u014f\1\u0158\1\u00fb\1\u018f"+
		"\1\u00ed\1\u019e\1\u0173\1\u016a\1\63\1\41\1\u00a4\1\u0174\1\uffff\1\u009b"+
		"\6\uffff\1\u00df\1\u00c1\1\u00c9\1\u00a5\1\u009c\1\u00e0\1\u00ca\1\114"+
		"\1\102\1\u0187\1\u01ad\1\u00b9\1\u00bf\14\uffff\1\115\1\103\1\u0188\1"+
		"\u01ae\1\u00ba\1\u00c0\1\u00fd\1\u00ee\1\u0191\1\u019f\2\uffff\1\21\1"+
		"\u00fe\1\u00ef\1\u0192\1\u01a0\1\u0175\1\u016b\1\u0176\1\u016c\1\64\1"+
		"\42\1\65\1\43\1\uffff\1\u010b\1\u0109\1\170\2\uffff\1\166\2\uffff\1\157"+
		"\1\160\1\u01f6\2\uffff\1\u01f7\2\uffff\1\u00b5\1\u00b4\3\uffff\1\u0137"+
		"\1\u0136\1\u01f5\2\uffff\1\u01f4\2\uffff\1\u017d\1\u017e\2\uffff\1\u0080"+
		"\1\11\1\u0085\2\uffff\1\u01d7\1\30\1\u01e6\1\177\1\u0084\1\u01d6\1\u01e5"+
		"\1\61\1\37\4\uffff\1\60\1\36\1\uffff\1\u008f\1\u008d\3\uffff\1\u008a\1"+
		"\u0089\2\uffff\1\u012b\1\144\1\u011c\1\u012a\1\u011b\1\u00a3\1\u009a\1"+
		"\u00a2\1\u0099\6\uffff\1\162\1\161\20\uffff\1\147\2\uffff\1\u013b\1\135"+
		"\1\u0142\1\126\1\u014e\1\u0157\1\u00fa\1\u018e\1\u00eb\1\u019c\1\u0171"+
		"\1\u0168\1\57\1\35\1\u00f9\1\u018d\1\u00ea\1\u019b\1\110\1\u0184\1\16"+
		"\1\77\1\u01aa\1\13\7\uffff\1\u0090\1\u008e\1\u0113\3\uffff\1\u0135\1\u0181"+
		"\1\123\2\uffff\1\u00aa\1\u00a9\3\uffff\1\164\1\163\1\u01b9\2\uffff\1\u00af"+
		"\1\u00ae\7\uffff\1\u015e\1\u015d\2\uffff\1\56\1\34\1\55\1\33\5\uffff\1"+
		"\u00d4\1\u00d6\2\uffff\1\u0116\1\uffff\1\u00d8\1\u00d9\7\uffff\1\u00a1"+
		"\1\uffff\1\u0098\1\uffff\1\u00de\1\u00c8\2\uffff\1\u0182\16\uffff\1\u01d1"+
		"\2\uffff\1\u013a\1\134\1\u0141\1\125\1\u014d\1\u0156\1\u00f8\1\u0193\1"+
		"\u00f0\1\u01a1\1\u0177\1\u016d\1\66\1\44\1\u00ff\1\u0194\1\u00f1\1\u01a2"+
		"\1\116\1\u0189\1\u01b4\1\104\1\u01af\1\u01b5\4\uffff\1\u00e1\1\u00cb\1"+
		"\u00e2\1\u00cc\1\u01cf\1\u01cc\1\u01d8\1\u01e7\1\u01d0\1\u01cd\1\u01d9"+
		"\1\u01e8\3\uffff\1\22\1\20\1\u00b2\3\uffff\1\u00ab\1\u0161\1\u0180\2\uffff"+
		"\1\172\1\171\3\uffff\1\6\1\5\1\u0165\2\uffff\1\7\1\10\2\uffff\1\u01f2"+
		"\4\uffff\1\u0154\1\u014c\1\u01da\1\u01e9\2\uffff\1\143\1\75\3\uffff\1"+
		"\67\1\45\2\uffff\1\u0092\1\u0093\2\uffff\1\u00ad\1\uffff\1\u012c\1\u011d"+
		"\2\uffff\1\u0087\1\u0088\5\uffff\1\u00a6\1\uffff\1\u009d\1\uffff\1\u00e3"+
		"\1\u00cd\1\u012d\1\u011e\1\u012e\1\u011f\2\uffff\1\u00c2\15\uffff\1\156"+
		"\2\uffff\1\u013e\1\140\1\u0145\1\131\1\u0151\1\u015a\1\u0100\1\u0195\1"+
		"\u00f2\1\u01a3\1\u0178\1\u016e\1\70\1\46\1\u0101\1\u0196\1\u00f3\1\u01a4"+
		"\1\117\1\u018a\1\1\1\105\1\u01b0\1\0\4\uffff\1\u00e4\1\u00ce\1\u00e5\1"+
		"\u00cf\1\153\1\150\1\u01db\1\u01ea\1\154\1\151\1\u01dc\1\u01eb\3\uffff"+
		"\1\u01ca\1\u01c9\1\12\3\uffff\1\4\1\u0086\1\u0163\2\uffff\1\u01c3\1\u01c0"+
		"\3\uffff\1\u01bc\1\u01bb\1\u008c\2\uffff\1\u01f1\1\u01f3\2\uffff\1\u014a"+
		"\4\uffff\1\u010a\1\u0108\1\u01dd\1\u01ec\2\uffff\1\u01c4\1\u01c1\3\uffff"+
		"\1\71\1\47\2\uffff\1\145\1\146\2\uffff\1\152\1\uffff\1\u012f\1\u0120\2"+
		"\uffff\1\106\1\26\4\uffff\1\u00a7\1\uffff\1\u009e\1\uffff\1\u00e6\1\u00d0"+
		"\1\u0130\1\u0121\1\u0131\1\u0122\2\uffff\1\u00b1\13\uffff\1\u0112\2\uffff"+
		"\1\u013f\1\141\1\u0146\1\132\1\u0152\1\u015b\1\u0102\1\u0197\1\u00f4\1"+
		"\u01a5\1\u0179\1\u016f\1\72\1\50\1\u0103\1\u0198\1\u00f5\1\u01a6\1\120"+
		"\1\u018b\1\u01be\1\107\1\u01b1\1\u01bd\4\uffff\1\u00e7\1\u00d1\1\u00e8"+
		"\1\u00d2\1\u010e\1\u010c\1\u01de\1\u01ed\1\u010f\1\u010d\1\u01df\1\u01ee"+
		"\2\uffff\1\14\1\15\1\u00b0\2\uffff\1\u00c3\1\u0115\1\u0183\1\uffff\1\167"+
		"\1\165\2\uffff\1\u01ce\1\u01f0\1\u0162\1\uffff\1\2\1\3\2\uffff\1\u0114"+
		"\3\uffff\1\u0148\1\u014b\1\u01e0\1\u01ef\2\uffff\1\u01b3\1\u01b2\2\uffff"+
		"\1\73\1\51\1\uffff\1\u0096\1\u0095\1\uffff\1\u0094\1\uffff\1\u0132\1\u0123"+
		"\2\uffff\1\u01c7\1\u01c8\2\uffff\1\u00a8\1\uffff\1\u009f\1\uffff\1\u00e9"+
		"\1\u00d3\1\u0133\1\u0124\1\u0134\1\u0125\1\uffff\1\u00b6\1\u0140\1\142"+
		"\1\u0147\1\133\1\u0153\1\u015c\1\u0104\1\u0199\1\u00f6\1\u01a7\1\u017a"+
		"\1\u0170\1\74\1\52\1\u0105\1\u019a\1\u00f7\1\u01a8\1\121\1\u018c\1\u00bb"+
		"\1\76\1\u01a9\1\u00bc\1\u00dd\1\u00c7\1\u00dc\1\u00c6\1\176\1\u0083\1"+
		"\u01d5\1\u01e4\1\175\1\u0082\1\u01d4\1\u01e3\1\u01ba\1\u01b8\1\124\1\17"+
		"\1\u0091\1\u01cb\1\u017b\1\u017c\1\u0167\1\u0166\1\u00ac\1\u0160\1\u015f"+
		"\1\uffff\1\u0164\2\uffff\1\u00d5\1\u00d7\1\u01d3\1\u01e2\1\uffff\1\u01c5"+
		"\1\u01c6\1\uffff\1\54\1\32\1\u01c2\1\u01bf\1\25\1\uffff\1\u0129\1\u011a"+
		"\1\uffff\1\24\1\23\1\u00a0\1\u0097\1\u00db\1\u00c5\1\u0128\1\u0119\1\u0127"+
		"\1\u0118\1\u008b\1\u00da\1\174\1\u0081\1\u01d2\1\u01e1\1\u0106\1\u0107"+
		"\1\53\1\31\1\u0126\1\u0117\1\u01b6\1\u01b7}>";
	static final String[] DFA188_transitionS = {
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

	static final short[] DFA188_eot = DFA.unpackEncodedString(DFA188_eotS);
	static final short[] DFA188_eof = DFA.unpackEncodedString(DFA188_eofS);
	static final char[] DFA188_min = DFA.unpackEncodedStringToUnsignedChars(DFA188_minS);
	static final char[] DFA188_max = DFA.unpackEncodedStringToUnsignedChars(DFA188_maxS);
	static final short[] DFA188_accept = DFA.unpackEncodedString(DFA188_acceptS);
	static final short[] DFA188_special = DFA.unpackEncodedString(DFA188_specialS);
	static final short[][] DFA188_transition;

	static {
		int numStates = DFA188_transitionS.length;
		DFA188_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA188_transition[i] = DFA.unpackEncodedString(DFA188_transitionS[i]);
		}
	}

	protected class DFA188 extends DFA {

		public DFA188(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 188;
			this.eot = DFA188_eot;
			this.eof = DFA188_eof;
			this.min = DFA188_min;
			this.max = DFA188_max;
			this.accept = DFA188_accept;
			this.special = DFA188_special;
			this.transition = DFA188_transition;
		}
		@Override
		public String getDescription() {
			return "1776:9: ( ( D P ( I | C ) )=> D P ( I | C M ) | ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R ( A | E ) )=> R ( A D | E M ) | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | PERCENTAGE_SYMBOL |)";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			IntStream input = _input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA188_534 = input.LA(1);
						 
						int index188_534 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_534);
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA188_531 = input.LA(1);
						 
						int index188_531 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_531);
						if ( s>=0 ) return s;
						break;

					case 2 : 
						int LA188_701 = input.LA(1);
						 
						int index188_701 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_701);
						if ( s>=0 ) return s;
						break;

					case 3 : 
						int LA188_702 = input.LA(1);
						 
						int index188_702 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_702);
						if ( s>=0 ) return s;
						break;

					case 4 : 
						int LA188_560 = input.LA(1);
						 
						int index188_560 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_560);
						if ( s>=0 ) return s;
						break;

					case 5 : 
						int LA188_437 = input.LA(1);
						 
						int index188_437 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_437);
						if ( s>=0 ) return s;
						break;

					case 6 : 
						int LA188_436 = input.LA(1);
						 
						int index188_436 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_436);
						if ( s>=0 ) return s;
						break;

					case 7 : 
						int LA188_441 = input.LA(1);
						 
						int index188_441 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_441);
						if ( s>=0 ) return s;
						break;

					case 8 : 
						int LA188_442 = input.LA(1);
						 
						int index188_442 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_442);
						if ( s>=0 ) return s;
						break;

					case 9 : 
						int LA188_197 = input.LA(1);
						s = -1;
						if ( ((LA188_197 >= '\u0000' && LA188_197 <= '\t')||LA188_197=='\u000B'||(LA188_197 >= '\u000E' && LA188_197 <= '/')||(LA188_197 >= '1' && LA188_197 <= '3')||LA188_197=='5'||(LA188_197 >= '7' && LA188_197 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA188_197=='0') ) {s = 317;}
						else if ( (LA188_197=='4'||LA188_197=='6') ) {s = 318;}
						if ( s>=0 ) return s;
						break;

					case 10 : 
						int LA188_556 = input.LA(1);
						 
						int index188_556 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_556);
						if ( s>=0 ) return s;
						break;

					case 11 : 
						int LA188_285 = input.LA(1);
						 
						int index188_285 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_285);
						if ( s>=0 ) return s;
						break;

					case 12 : 
						int LA188_684 = input.LA(1);
						 
						int index188_684 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_684);
						if ( s>=0 ) return s;
						break;

					case 13 : 
						int LA188_685 = input.LA(1);
						 
						int index188_685 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_685);
						if ( s>=0 ) return s;
						break;

					case 14 : 
						int LA188_282 = input.LA(1);
						 
						int index188_282 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_282);
						if ( s>=0 ) return s;
						break;

					case 15 : 
						int LA188_786 = input.LA(1);
						 
						int index188_786 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_786);
						if ( s>=0 ) return s;
						break;

					case 16 : 
						int LA188_421 = input.LA(1);
						 
						int index188_421 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_421);
						if ( s>=0 ) return s;
						break;

					case 17 : 
						int LA188_149 = input.LA(1);
						 
						int index188_149 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_149);
						if ( s>=0 ) return s;
						break;

					case 18 : 
						int LA188_420 = input.LA(1);
						 
						int index188_420 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_420);
						if ( s>=0 ) return s;
						break;

					case 19 : 
						int LA188_818 = input.LA(1);
						 
						int index188_818 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_818);
						if ( s>=0 ) return s;
						break;

					case 20 : 
						int LA188_817 = input.LA(1);
						 
						int index188_817 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_817);
						if ( s>=0 ) return s;
						break;

					case 21 : 
						int LA188_812 = input.LA(1);
						 
						int index188_812 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_812);
						if ( s>=0 ) return s;
						break;

					case 22 : 
						int LA188_610 = input.LA(1);
						 
						int index188_610 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_610);
						if ( s>=0 ) return s;
						break;

					case 23 : 
						int LA188_77 = input.LA(1);
						s = -1;
						if ( (LA188_77=='h') ) {s = 210;}
						else if ( (LA188_77=='H') ) {s = 211;}
						else if ( ((LA188_77 >= '\u0000' && LA188_77 <= '\t')||LA188_77=='\u000B'||(LA188_77 >= '\u000E' && LA188_77 <= '/')||(LA188_77 >= '1' && LA188_77 <= '3')||LA188_77=='5'||(LA188_77 >= '7' && LA188_77 <= 'G')||(LA188_77 >= 'I' && LA188_77 <= 'g')||(LA188_77 >= 'i' && LA188_77 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA188_77=='0') ) {s = 212;}
						else if ( (LA188_77=='4'||LA188_77=='6') ) {s = 213;}
						if ( s>=0 ) return s;
						break;

					case 24 : 
						int LA188_202 = input.LA(1);
						s = -1;
						if ( (LA188_202=='m') ) {s = 323;}
						else if ( (LA188_202=='M') ) {s = 324;}
						else if ( ((LA188_202 >= '\u0000' && LA188_202 <= '\t')||LA188_202=='\u000B'||(LA188_202 >= '\u000E' && LA188_202 <= '/')||(LA188_202 >= '1' && LA188_202 <= '3')||LA188_202=='5'||(LA188_202 >= '7' && LA188_202 <= 'L')||(LA188_202 >= 'N' && LA188_202 <= 'l')||(LA188_202 >= 'n' && LA188_202 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA188_202=='0') ) {s = 325;}
						else if ( (LA188_202=='4'||LA188_202=='6') ) {s = 326;}
						if ( s>=0 ) return s;
						break;

					case 25 : 
						int LA188_836 = input.LA(1);
						 
						int index188_836 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_836);
						if ( s>=0 ) return s;
						break;

					case 26 : 
						int LA188_809 = input.LA(1);
						 
						int index188_809 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_809);
						if ( s>=0 ) return s;
						break;

					case 27 : 
						int LA188_330 = input.LA(1);
						 
						int index188_330 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_330);
						if ( s>=0 ) return s;
						break;

					case 28 : 
						int LA188_328 = input.LA(1);
						 
						int index188_328 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_328);
						if ( s>=0 ) return s;
						break;

					case 29 : 
						int LA188_275 = input.LA(1);
						 
						int index188_275 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_275);
						if ( s>=0 ) return s;
						break;

					case 30 : 
						int LA188_215 = input.LA(1);
						 
						int index188_215 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_215);
						if ( s>=0 ) return s;
						break;

					case 31 : 
						int LA188_209 = input.LA(1);
						 
						int index188_209 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_209);
						if ( s>=0 ) return s;
						break;

					case 32 : 
						int LA188_81 = input.LA(1);
						 
						int index188_81 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_81);
						if ( s>=0 ) return s;
						break;

					case 33 : 
						int LA188_101 = input.LA(1);
						 
						int index188_101 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_101);
						if ( s>=0 ) return s;
						break;

					case 34 : 
						int LA188_159 = input.LA(1);
						 
						int index188_159 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_159);
						if ( s>=0 ) return s;
						break;

					case 35 : 
						int LA188_161 = input.LA(1);
						 
						int index188_161 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_161);
						if ( s>=0 ) return s;
						break;

					case 36 : 
						int LA188_390 = input.LA(1);
						 
						int index188_390 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_390);
						if ( s>=0 ) return s;
						break;

					case 37 : 
						int LA188_462 = input.LA(1);
						 
						int index188_462 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_462);
						if ( s>=0 ) return s;
						break;

					case 38 : 
						int LA188_524 = input.LA(1);
						 
						int index188_524 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_524);
						if ( s>=0 ) return s;
						break;

					case 39 : 
						int LA188_596 = input.LA(1);
						 
						int index188_596 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_596);
						if ( s>=0 ) return s;
						break;

					case 40 : 
						int LA188_655 = input.LA(1);
						 
						int index188_655 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_655);
						if ( s>=0 ) return s;
						break;

					case 41 : 
						int LA188_720 = input.LA(1);
						 
						int index188_720 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_720);
						if ( s>=0 ) return s;
						break;

					case 42 : 
						int LA188_760 = input.LA(1);
						 
						int index188_760 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_760);
						if ( s>=0 ) return s;
						break;

					case 43 : 
						int LA188_835 = input.LA(1);
						 
						int index188_835 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_835);
						if ( s>=0 ) return s;
						break;

					case 44 : 
						int LA188_808 = input.LA(1);
						 
						int index188_808 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_808);
						if ( s>=0 ) return s;
						break;

					case 45 : 
						int LA188_329 = input.LA(1);
						 
						int index188_329 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_329);
						if ( s>=0 ) return s;
						break;

					case 46 : 
						int LA188_327 = input.LA(1);
						 
						int index188_327 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_327);
						if ( s>=0 ) return s;
						break;

					case 47 : 
						int LA188_274 = input.LA(1);
						 
						int index188_274 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_274);
						if ( s>=0 ) return s;
						break;

					case 48 : 
						int LA188_214 = input.LA(1);
						 
						int index188_214 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_214);
						if ( s>=0 ) return s;
						break;

					case 49 : 
						int LA188_208 = input.LA(1);
						 
						int index188_208 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_208);
						if ( s>=0 ) return s;
						break;

					case 50 : 
						int LA188_79 = input.LA(1);
						 
						int index188_79 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_79);
						if ( s>=0 ) return s;
						break;

					case 51 : 
						int LA188_100 = input.LA(1);
						 
						int index188_100 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_100);
						if ( s>=0 ) return s;
						break;

					case 52 : 
						int LA188_158 = input.LA(1);
						 
						int index188_158 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_158);
						if ( s>=0 ) return s;
						break;

					case 53 : 
						int LA188_160 = input.LA(1);
						 
						int index188_160 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_160);
						if ( s>=0 ) return s;
						break;

					case 54 : 
						int LA188_389 = input.LA(1);
						 
						int index188_389 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_389);
						if ( s>=0 ) return s;
						break;

					case 55 : 
						int LA188_461 = input.LA(1);
						 
						int index188_461 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_461);
						if ( s>=0 ) return s;
						break;

					case 56 : 
						int LA188_523 = input.LA(1);
						 
						int index188_523 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_523);
						if ( s>=0 ) return s;
						break;

					case 57 : 
						int LA188_595 = input.LA(1);
						 
						int index188_595 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_595);
						if ( s>=0 ) return s;
						break;

					case 58 : 
						int LA188_654 = input.LA(1);
						 
						int index188_654 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_654);
						if ( s>=0 ) return s;
						break;

					case 59 : 
						int LA188_719 = input.LA(1);
						 
						int index188_719 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_719);
						if ( s>=0 ) return s;
						break;

					case 60 : 
						int LA188_759 = input.LA(1);
						 
						int index188_759 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_759);
						if ( s>=0 ) return s;
						break;

					case 61 : 
						int LA188_457 = input.LA(1);
						 
						int index188_457 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_457);
						if ( s>=0 ) return s;
						break;

					case 62 : 
						int LA188_768 = input.LA(1);
						 
						int index188_768 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_768);
						if ( s>=0 ) return s;
						break;

					case 63 : 
						int LA188_283 = input.LA(1);
						 
						int index188_283 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_283);
						if ( s>=0 ) return s;
						break;

					case 64 : 
						int LA188_89 = input.LA(1);
						 
						int index188_89 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_89);
						if ( s>=0 ) return s;
						break;

					case 65 : 
						int LA188_56 = input.LA(1);
						 
						int index188_56 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_56);
						if ( s>=0 ) return s;
						break;

					case 66 : 
						int LA188_120 = input.LA(1);
						 
						int index188_120 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_120);
						if ( s>=0 ) return s;
						break;

					case 67 : 
						int LA188_138 = input.LA(1);
						 
						int index188_138 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_138);
						if ( s>=0 ) return s;
						break;

					case 68 : 
						int LA188_398 = input.LA(1);
						 
						int index188_398 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_398);
						if ( s>=0 ) return s;
						break;

					case 69 : 
						int LA188_532 = input.LA(1);
						 
						int index188_532 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_532);
						if ( s>=0 ) return s;
						break;

					case 70 : 
						int LA188_609 = input.LA(1);
						 
						int index188_609 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_609);
						if ( s>=0 ) return s;
						break;

					case 71 : 
						int LA188_663 = input.LA(1);
						 
						int index188_663 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_663);
						if ( s>=0 ) return s;
						break;

					case 72 : 
						int LA188_280 = input.LA(1);
						 
						int index188_280 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_280);
						if ( s>=0 ) return s;
						break;

					case 73 : 
						int LA188_86 = input.LA(1);
						 
						int index188_86 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_86);
						if ( s>=0 ) return s;
						break;

					case 74 : 
						int LA188_60 = input.LA(1);
						s = -1;
						if ( (LA188_60=='m') ) {s = 179;}
						else if ( (LA188_60=='M') ) {s = 180;}
						else if ( ((LA188_60 >= '\u0000' && LA188_60 <= '\t')||LA188_60=='\u000B'||(LA188_60 >= '\u000E' && LA188_60 <= '/')||(LA188_60 >= '1' && LA188_60 <= '3')||LA188_60=='5'||(LA188_60 >= '7' && LA188_60 <= 'L')||(LA188_60 >= 'N' && LA188_60 <= 'l')||(LA188_60 >= 'n' && LA188_60 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA188_60=='0') ) {s = 181;}
						else if ( (LA188_60=='4'||LA188_60=='6') ) {s = 182;}
						if ( s>=0 ) return s;
						break;

					case 75 : 
						int LA188_52 = input.LA(1);
						 
						int index188_52 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_52);
						if ( s>=0 ) return s;
						break;

					case 76 : 
						int LA188_119 = input.LA(1);
						 
						int index188_119 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_119);
						if ( s>=0 ) return s;
						break;

					case 77 : 
						int LA188_137 = input.LA(1);
						 
						int index188_137 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_137);
						if ( s>=0 ) return s;
						break;

					case 78 : 
						int LA188_395 = input.LA(1);
						 
						int index188_395 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_395);
						if ( s>=0 ) return s;
						break;

					case 79 : 
						int LA188_529 = input.LA(1);
						 
						int index188_529 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_529);
						if ( s>=0 ) return s;
						break;

					case 80 : 
						int LA188_660 = input.LA(1);
						 
						int index188_660 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_660);
						if ( s>=0 ) return s;
						break;

					case 81 : 
						int LA188_765 = input.LA(1);
						 
						int index188_765 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_765);
						if ( s>=0 ) return s;
						break;

					case 82 : 
						int LA188_26 = input.LA(1);
						s = -1;
						if ( (LA188_26=='p') ) {s = 107;}
						else if ( (LA188_26=='P') ) {s = 108;}
						else if ( ((LA188_26 >= '\u0000' && LA188_26 <= '\t')||LA188_26=='\u000B'||(LA188_26 >= '\u000E' && LA188_26 <= '/')||(LA188_26 >= '1' && LA188_26 <= '3')||(LA188_26 >= '8' && LA188_26 <= 'O')||(LA188_26 >= 'Q' && LA188_26 <= 'o')||(LA188_26 >= 'q' && LA188_26 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA188_26=='0') ) {s = 109;}
						else if ( (LA188_26=='5'||LA188_26=='7') ) {s = 110;}
						else if ( (LA188_26=='4'||LA188_26=='6') ) {s = 111;}
						if ( s>=0 ) return s;
						break;

					case 83 : 
						int LA188_301 = input.LA(1);
						 
						int index188_301 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_301);
						if ( s>=0 ) return s;
						break;

					case 84 : 
						int LA188_785 = input.LA(1);
						 
						int index188_785 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_785);
						if ( s>=0 ) return s;
						break;

					case 85 : 
						int LA188_380 = input.LA(1);
						 
						int index188_380 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_380);
						if ( s>=0 ) return s;
						break;

					case 86 : 
						int LA188_265 = input.LA(1);
						 
						int index188_265 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_265);
						if ( s>=0 ) return s;
						break;

					case 87 : 
						int LA188_85 = input.LA(1);
						 
						int index188_85 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_85);
						if ( s>=0 ) return s;
						break;

					case 88 : 
						int LA188_51 = input.LA(1);
						 
						int index188_51 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_51);
						if ( s>=0 ) return s;
						break;

					case 89 : 
						int LA188_514 = input.LA(1);
						 
						int index188_514 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_514);
						if ( s>=0 ) return s;
						break;

					case 90 : 
						int LA188_645 = input.LA(1);
						 
						int index188_645 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_645);
						if ( s>=0 ) return s;
						break;

					case 91 : 
						int LA188_750 = input.LA(1);
						 
						int index188_750 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_750);
						if ( s>=0 ) return s;
						break;

					case 92 : 
						int LA188_378 = input.LA(1);
						 
						int index188_378 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_378);
						if ( s>=0 ) return s;
						break;

					case 93 : 
						int LA188_263 = input.LA(1);
						 
						int index188_263 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_263);
						if ( s>=0 ) return s;
						break;

					case 94 : 
						int LA188_83 = input.LA(1);
						 
						int index188_83 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_83);
						if ( s>=0 ) return s;
						break;

					case 95 : 
						int LA188_49 = input.LA(1);
						 
						int index188_49 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_49);
						if ( s>=0 ) return s;
						break;

					case 96 : 
						int LA188_512 = input.LA(1);
						 
						int index188_512 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_512);
						if ( s>=0 ) return s;
						break;

					case 97 : 
						int LA188_643 = input.LA(1);
						 
						int index188_643 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_643);
						if ( s>=0 ) return s;
						break;

					case 98 : 
						int LA188_748 = input.LA(1);
						 
						int index188_748 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_748);
						if ( s>=0 ) return s;
						break;

					case 99 : 
						int LA188_456 = input.LA(1);
						 
						int index188_456 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_456);
						if ( s>=0 ) return s;
						break;

					case 100 : 
						int LA188_227 = input.LA(1);
						s = -1;
						if ( (LA188_227=='m') ) {s = 342;}
						else if ( (LA188_227=='M') ) {s = 343;}
						else if ( ((LA188_227 >= '\u0000' && LA188_227 <= '\t')||LA188_227=='\u000B'||(LA188_227 >= '\u000E' && LA188_227 <= '/')||(LA188_227 >= '1' && LA188_227 <= '3')||LA188_227=='5'||(LA188_227 >= '7' && LA188_227 <= 'L')||(LA188_227 >= 'N' && LA188_227 <= 'l')||(LA188_227 >= 'n' && LA188_227 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA188_227=='0') ) {s = 344;}
						else if ( (LA188_227=='4'||LA188_227=='6') ) {s = 345;}
						if ( s>=0 ) return s;
						break;

					case 101 : 
						int LA188_599 = input.LA(1);
						 
						int index188_599 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_599);
						if ( s>=0 ) return s;
						break;

					case 102 : 
						int LA188_600 = input.LA(1);
						 
						int index188_600 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_600);
						if ( s>=0 ) return s;
						break;

					case 103 : 
						int LA188_259 = input.LA(1);
						 
						int index188_259 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_259);
						if ( s>=0 ) return s;
						break;

					case 104 : 
						int LA188_544 = input.LA(1);
						 
						int index188_544 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_544);
						if ( s>=0 ) return s;
						break;

					case 105 : 
						int LA188_548 = input.LA(1);
						 
						int index188_548 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_548);
						if ( s>=0 ) return s;
						break;

					case 106 : 
						int LA188_603 = input.LA(1);
						 
						int index188_603 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_603);
						if ( s>=0 ) return s;
						break;

					case 107 : 
						int LA188_543 = input.LA(1);
						 
						int index188_543 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_543);
						if ( s>=0 ) return s;
						break;

					case 108 : 
						int LA188_547 = input.LA(1);
						 
						int index188_547 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_547);
						if ( s>=0 ) return s;
						break;

					case 109 : 
						int LA188_68 = input.LA(1);
						s = -1;
						if ( (LA188_68=='n') ) {s = 192;}
						else if ( (LA188_68=='N') ) {s = 193;}
						else if ( ((LA188_68 >= '\u0000' && LA188_68 <= '\t')||LA188_68=='\u000B'||(LA188_68 >= '\u000E' && LA188_68 <= '/')||(LA188_68 >= '1' && LA188_68 <= '3')||LA188_68=='5'||(LA188_68 >= '7' && LA188_68 <= 'M')||(LA188_68 >= 'O' && LA188_68 <= 'm')||(LA188_68 >= 'o' && LA188_68 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA188_68=='0') ) {s = 194;}
						else if ( (LA188_68=='4'||LA188_68=='6') ) {s = 195;}
						if ( s>=0 ) return s;
						break;

					case 110 : 
						int LA188_508 = input.LA(1);
						 
						int index188_508 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_508);
						if ( s>=0 ) return s;
						break;

					case 111 : 
						int LA188_171 = input.LA(1);
						 
						int index188_171 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_171);
						if ( s>=0 ) return s;
						break;

					case 112 : 
						int LA188_172 = input.LA(1);
						 
						int index188_172 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_172);
						if ( s>=0 ) return s;
						break;

					case 113 : 
						int LA188_242 = input.LA(1);
						 
						int index188_242 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_242);
						if ( s>=0 ) return s;
						break;

					case 114 : 
						int LA188_241 = input.LA(1);
						 
						int index188_241 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_241);
						if ( s>=0 ) return s;
						break;

					case 115 : 
						int LA188_310 = input.LA(1);
						 
						int index188_310 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_310);
						if ( s>=0 ) return s;
						break;

					case 116 : 
						int LA188_309 = input.LA(1);
						 
						int index188_309 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_309);
						if ( s>=0 ) return s;
						break;

					case 117 : 
						int LA188_694 = input.LA(1);
						 
						int index188_694 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_694);
						if ( s>=0 ) return s;
						break;

					case 118 : 
						int LA188_168 = input.LA(1);
						 
						int index188_168 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_168);
						if ( s>=0 ) return s;
						break;

					case 119 : 
						int LA188_693 = input.LA(1);
						 
						int index188_693 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_693);
						if ( s>=0 ) return s;
						break;

					case 120 : 
						int LA188_165 = input.LA(1);
						 
						int index188_165 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_165);
						if ( s>=0 ) return s;
						break;

					case 121 : 
						int LA188_432 = input.LA(1);
						 
						int index188_432 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_432);
						if ( s>=0 ) return s;
						break;

					case 122 : 
						int LA188_431 = input.LA(1);
						 
						int index188_431 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_431);
						if ( s>=0 ) return s;
						break;

					case 123 : 
						int LA188_53 = input.LA(1);
						s = -1;
						if ( (LA188_53=='x') ) {s = 171;}
						else if ( (LA188_53=='X') ) {s = 172;}
						else if ( (LA188_53=='t') ) {s = 173;}
						else if ( (LA188_53=='0') ) {s = 174;}
						else if ( (LA188_53=='5'||LA188_53=='7') ) {s = 175;}
						else if ( (LA188_53=='T') ) {s = 176;}
						else if ( ((LA188_53 >= '\u0000' && LA188_53 <= '\t')||LA188_53=='\u000B'||(LA188_53 >= '\u000E' && LA188_53 <= '/')||(LA188_53 >= '1' && LA188_53 <= '3')||(LA188_53 >= '8' && LA188_53 <= 'S')||(LA188_53 >= 'U' && LA188_53 <= 'W')||(LA188_53 >= 'Y' && LA188_53 <= 's')||(LA188_53 >= 'u' && LA188_53 <= 'w')||(LA188_53 >= 'y' && LA188_53 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA188_53=='4'||LA188_53=='6') ) {s = 177;}
						if ( s>=0 ) return s;
						break;

					case 124 : 
						int LA188_829 = input.LA(1);
						 
						int index188_829 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_829);
						if ( s>=0 ) return s;
						break;

					case 125 : 
						int LA188_779 = input.LA(1);
						 
						int index188_779 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_779);
						if ( s>=0 ) return s;
						break;

					case 126 : 
						int LA188_775 = input.LA(1);
						 
						int index188_775 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_775);
						if ( s>=0 ) return s;
						break;

					case 127 : 
						int LA188_204 = input.LA(1);
						 
						int index188_204 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_204);
						if ( s>=0 ) return s;
						break;

					case 128 : 
						int LA188_196 = input.LA(1);
						 
						int index188_196 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_196);
						if ( s>=0 ) return s;
						break;

					case 129 : 
						int LA188_830 = input.LA(1);
						 
						int index188_830 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_830);
						if ( s>=0 ) return s;
						break;

					case 130 : 
						int LA188_780 = input.LA(1);
						 
						int index188_780 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_780);
						if ( s>=0 ) return s;
						break;

					case 131 : 
						int LA188_776 = input.LA(1);
						 
						int index188_776 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_776);
						if ( s>=0 ) return s;
						break;

					case 132 : 
						int LA188_205 = input.LA(1);
						 
						int index188_205 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_205);
						if ( s>=0 ) return s;
						break;

					case 133 : 
						int LA188_198 = input.LA(1);
						 
						int index188_198 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_198);
						if ( s>=0 ) return s;
						break;

					case 134 : 
						int LA188_561 = input.LA(1);
						 
						int index188_561 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_561);
						if ( s>=0 ) return s;
						break;

					case 135 : 
						int LA188_475 = input.LA(1);
						 
						int index188_475 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_475);
						if ( s>=0 ) return s;
						break;

					case 136 : 
						int LA188_476 = input.LA(1);
						 
						int index188_476 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_476);
						if ( s>=0 ) return s;
						break;

					case 137 : 
						int LA188_223 = input.LA(1);
						 
						int index188_223 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_223);
						if ( s>=0 ) return s;
						break;

					case 138 : 
						int LA188_222 = input.LA(1);
						 
						int index188_222 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_222);
						if ( s>=0 ) return s;
						break;

					case 139 : 
						int LA188_827 = input.LA(1);
						 
						int index188_827 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_827);
						if ( s>=0 ) return s;
						break;

					case 140 : 
						int LA188_572 = input.LA(1);
						 
						int index188_572 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_572);
						if ( s>=0 ) return s;
						break;

					case 141 : 
						int LA188_218 = input.LA(1);
						 
						int index188_218 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_218);
						if ( s>=0 ) return s;
						break;

					case 142 : 
						int LA188_294 = input.LA(1);
						 
						int index188_294 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_294);
						if ( s>=0 ) return s;
						break;

					case 143 : 
						int LA188_217 = input.LA(1);
						 
						int index188_217 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_217);
						if ( s>=0 ) return s;
						break;

					case 144 : 
						int LA188_293 = input.LA(1);
						 
						int index188_293 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_293);
						if ( s>=0 ) return s;
						break;

					case 145 : 
						int LA188_787 = input.LA(1);
						 
						int index188_787 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_787);
						if ( s>=0 ) return s;
						break;

					case 146 : 
						int LA188_465 = input.LA(1);
						 
						int index188_465 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_465);
						if ( s>=0 ) return s;
						break;

					case 147 : 
						int LA188_466 = input.LA(1);
						 
						int index188_466 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_466);
						if ( s>=0 ) return s;
						break;

					case 148 : 
						int LA188_725 = input.LA(1);
						 
						int index188_725 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_725);
						if ( s>=0 ) return s;
						break;

					case 149 : 
						int LA188_723 = input.LA(1);
						 
						int index188_723 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_723);
						if ( s>=0 ) return s;
						break;

					case 150 : 
						int LA188_722 = input.LA(1);
						 
						int index188_722 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_722);
						if ( s>=0 ) return s;
						break;

					case 151 : 
						int LA188_820 = input.LA(1);
						 
						int index188_820 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_820);
						if ( s>=0 ) return s;
						break;

					case 152 : 
						int LA188_353 = input.LA(1);
						 
						int index188_353 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_353);
						if ( s>=0 ) return s;
						break;

					case 153 : 
						int LA188_234 = input.LA(1);
						 
						int index188_234 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_234);
						if ( s>=0 ) return s;
						break;

					case 154 : 
						int LA188_232 = input.LA(1);
						 
						int index188_232 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_232);
						if ( s>=0 ) return s;
						break;

					case 155 : 
						int LA188_105 = input.LA(1);
						 
						int index188_105 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_105);
						if ( s>=0 ) return s;
						break;

					case 156 : 
						int LA188_116 = input.LA(1);
						 
						int index188_116 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_116);
						if ( s>=0 ) return s;
						break;

					case 157 : 
						int LA188_484 = input.LA(1);
						 
						int index188_484 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_484);
						if ( s>=0 ) return s;
						break;

					case 158 : 
						int LA188_617 = input.LA(1);
						 
						int index188_617 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_617);
						if ( s>=0 ) return s;
						break;

					case 159 : 
						int LA188_737 = input.LA(1);
						 
						int index188_737 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_737);
						if ( s>=0 ) return s;
						break;

					case 160 : 
						int LA188_819 = input.LA(1);
						 
						int index188_819 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_819);
						if ( s>=0 ) return s;
						break;

					case 161 : 
						int LA188_351 = input.LA(1);
						 
						int index188_351 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_351);
						if ( s>=0 ) return s;
						break;

					case 162 : 
						int LA188_233 = input.LA(1);
						 
						int index188_233 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_233);
						if ( s>=0 ) return s;
						break;

					case 163 : 
						int LA188_231 = input.LA(1);
						 
						int index188_231 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_231);
						if ( s>=0 ) return s;
						break;

					case 164 : 
						int LA188_102 = input.LA(1);
						 
						int index188_102 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_102);
						if ( s>=0 ) return s;
						break;

					case 165 : 
						int LA188_115 = input.LA(1);
						 
						int index188_115 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_115);
						if ( s>=0 ) return s;
						break;

					case 166 : 
						int LA188_482 = input.LA(1);
						 
						int index188_482 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_482);
						if ( s>=0 ) return s;
						break;

					case 167 : 
						int LA188_615 = input.LA(1);
						 
						int index188_615 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_615);
						if ( s>=0 ) return s;
						break;

					case 168 : 
						int LA188_735 = input.LA(1);
						 
						int index188_735 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_735);
						if ( s>=0 ) return s;
						break;

					case 169 : 
						int LA188_305 = input.LA(1);
						 
						int index188_305 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_305);
						if ( s>=0 ) return s;
						break;

					case 170 : 
						int LA188_304 = input.LA(1);
						 
						int index188_304 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_304);
						if ( s>=0 ) return s;
						break;

					case 171 : 
						int LA188_426 = input.LA(1);
						 
						int index188_426 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_426);
						if ( s>=0 ) return s;
						break;

					case 172 : 
						int LA188_793 = input.LA(1);
						 
						int index188_793 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_793);
						if ( s>=0 ) return s;
						break;

					case 173 : 
						int LA188_469 = input.LA(1);
						 
						int index188_469 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_469);
						if ( s>=0 ) return s;
						break;

					case 174 : 
						int LA188_315 = input.LA(1);
						 
						int index188_315 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_315);
						if ( s>=0 ) return s;
						break;

					case 175 : 
						int LA188_314 = input.LA(1);
						 
						int index188_314 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_314);
						if ( s>=0 ) return s;
						break;

					case 176 : 
						int LA188_686 = input.LA(1);
						 
						int index188_686 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_686);
						if ( s>=0 ) return s;
						break;

					case 177 : 
						int LA188_627 = input.LA(1);
						 
						int index188_627 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_627);
						if ( s>=0 ) return s;
						break;

					case 178 : 
						int LA188_422 = input.LA(1);
						 
						int index188_422 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_422);
						if ( s>=0 ) return s;
						break;

					case 179 : 
						int LA188_48 = input.LA(1);
						s = -1;
						if ( (LA188_48=='m') ) {s = 163;}
						else if ( (LA188_48=='M') ) {s = 164;}
						else if ( (LA188_48=='x') ) {s = 165;}
						else if ( (LA188_48=='0') ) {s = 166;}
						else if ( (LA188_48=='4'||LA188_48=='6') ) {s = 167;}
						else if ( (LA188_48=='X') ) {s = 168;}
						else if ( ((LA188_48 >= '\u0000' && LA188_48 <= '\t')||LA188_48=='\u000B'||(LA188_48 >= '\u000E' && LA188_48 <= '/')||(LA188_48 >= '1' && LA188_48 <= '3')||(LA188_48 >= '8' && LA188_48 <= 'L')||(LA188_48 >= 'N' && LA188_48 <= 'W')||(LA188_48 >= 'Y' && LA188_48 <= 'l')||(LA188_48 >= 'n' && LA188_48 <= 'w')||(LA188_48 >= 'y' && LA188_48 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA188_48=='5'||LA188_48=='7') ) {s = 169;}
						if ( s>=0 ) return s;
						break;

					case 180 : 
						int LA188_180 = input.LA(1);
						 
						int index188_180 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_180);
						if ( s>=0 ) return s;
						break;

					case 181 : 
						int LA188_179 = input.LA(1);
						 
						int index188_179 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_179);
						if ( s>=0 ) return s;
						break;

					case 182 : 
						int LA188_746 = input.LA(1);
						 
						int index188_746 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_746);
						if ( s>=0 ) return s;
						break;

					case 183 : 
						int LA188_88 = input.LA(1);
						 
						int index188_88 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_88);
						if ( s>=0 ) return s;
						break;

					case 184 : 
						int LA188_55 = input.LA(1);
						 
						int index188_55 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_55);
						if ( s>=0 ) return s;
						break;

					case 185 : 
						int LA188_123 = input.LA(1);
						 
						int index188_123 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_123);
						if ( s>=0 ) return s;
						break;

					case 186 : 
						int LA188_141 = input.LA(1);
						 
						int index188_141 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_141);
						if ( s>=0 ) return s;
						break;

					case 187 : 
						int LA188_767 = input.LA(1);
						 
						int index188_767 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_767);
						if ( s>=0 ) return s;
						break;

					case 188 : 
						int LA188_770 = input.LA(1);
						 
						int index188_770 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_770);
						if ( s>=0 ) return s;
						break;

					case 189 : 
						int LA188_91 = input.LA(1);
						 
						int index188_91 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_91);
						if ( s>=0 ) return s;
						break;

					case 190 : 
						int LA188_58 = input.LA(1);
						 
						int index188_58 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_58);
						if ( s>=0 ) return s;
						break;

					case 191 : 
						int LA188_124 = input.LA(1);
						 
						int index188_124 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_124);
						if ( s>=0 ) return s;
						break;

					case 192 : 
						int LA188_142 = input.LA(1);
						 
						int index188_142 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_142);
						if ( s>=0 ) return s;
						break;

					case 193 : 
						int LA188_113 = input.LA(1);
						s = -1;
						if ( (LA188_113=='g') ) {s = 241;}
						else if ( (LA188_113=='G') ) {s = 242;}
						else if ( ((LA188_113 >= '\u0000' && LA188_113 <= '\t')||LA188_113=='\u000B'||(LA188_113 >= '\u000E' && LA188_113 <= '/')||(LA188_113 >= '1' && LA188_113 <= '3')||LA188_113=='5'||(LA188_113 >= '7' && LA188_113 <= 'F')||(LA188_113 >= 'H' && LA188_113 <= 'f')||(LA188_113 >= 'h' && LA188_113 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA188_113=='0') ) {s = 243;}
						else if ( (LA188_113=='4'||LA188_113=='6') ) {s = 244;}
						if ( s>=0 ) return s;
						break;

					case 194 : 
						int LA188_494 = input.LA(1);
						 
						int index188_494 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_494);
						if ( s>=0 ) return s;
						break;

					case 195 : 
						int LA188_689 = input.LA(1);
						 
						int index188_689 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_689);
						if ( s>=0 ) return s;
						break;

					case 196 : 
						int LA188_2 = input.LA(1);
						s = -1;
						if ( (LA188_2=='p') ) {s = 30;}
						else if ( (LA188_2=='0') ) {s = 31;}
						else if ( (LA188_2=='4'||LA188_2=='6') ) {s = 32;}
						else if ( (LA188_2=='P') ) {s = 33;}
						else if ( (LA188_2=='m') ) {s = 34;}
						else if ( (LA188_2=='5'||LA188_2=='7') ) {s = 35;}
						else if ( (LA188_2=='M') ) {s = 36;}
						else if ( (LA188_2=='i') ) {s = 37;}
						else if ( (LA188_2=='I') ) {s = 38;}
						else if ( (LA188_2=='r') ) {s = 39;}
						else if ( (LA188_2=='R') ) {s = 40;}
						else if ( (LA188_2=='s') ) {s = 41;}
						else if ( (LA188_2=='S') ) {s = 42;}
						else if ( (LA188_2=='k') ) {s = 43;}
						else if ( (LA188_2=='K') ) {s = 44;}
						else if ( (LA188_2=='h') ) {s = 45;}
						else if ( (LA188_2=='H') ) {s = 46;}
						else if ( ((LA188_2 >= '\u0000' && LA188_2 <= '\t')||LA188_2=='\u000B'||(LA188_2 >= '\u000E' && LA188_2 <= '/')||(LA188_2 >= '1' && LA188_2 <= '3')||(LA188_2 >= '8' && LA188_2 <= 'G')||LA188_2=='J'||LA188_2=='L'||(LA188_2 >= 'N' && LA188_2 <= 'O')||LA188_2=='Q'||(LA188_2 >= 'T' && LA188_2 <= 'g')||LA188_2=='j'||LA188_2=='l'||(LA188_2 >= 'n' && LA188_2 <= 'o')||LA188_2=='q'||(LA188_2 >= 't' && LA188_2 <= '\uFFFF')) ) {s = 12;}
						if ( s>=0 ) return s;
						break;

					case 197 : 
						int LA188_822 = input.LA(1);
						 
						int index188_822 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_822);
						if ( s>=0 ) return s;
						break;

					case 198 : 
						int LA188_774 = input.LA(1);
						 
						int index188_774 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_774);
						if ( s>=0 ) return s;
						break;

					case 199 : 
						int LA188_772 = input.LA(1);
						 
						int index188_772 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_772);
						if ( s>=0 ) return s;
						break;

					case 200 : 
						int LA188_356 = input.LA(1);
						 
						int index188_356 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_356);
						if ( s>=0 ) return s;
						break;

					case 201 : 
						int LA188_114 = input.LA(1);
						 
						int index188_114 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_114);
						if ( s>=0 ) return s;
						break;

					case 202 : 
						int LA188_118 = input.LA(1);
						 
						int index188_118 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_118);
						if ( s>=0 ) return s;
						break;

					case 203 : 
						int LA188_406 = input.LA(1);
						 
						int index188_406 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_406);
						if ( s>=0 ) return s;
						break;

					case 204 : 
						int LA188_408 = input.LA(1);
						 
						int index188_408 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_408);
						if ( s>=0 ) return s;
						break;

					case 205 : 
						int LA188_487 = input.LA(1);
						 
						int index188_487 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_487);
						if ( s>=0 ) return s;
						break;

					case 206 : 
						int LA188_540 = input.LA(1);
						 
						int index188_540 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_540);
						if ( s>=0 ) return s;
						break;

					case 207 : 
						int LA188_542 = input.LA(1);
						 
						int index188_542 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_542);
						if ( s>=0 ) return s;
						break;

					case 208 : 
						int LA188_620 = input.LA(1);
						 
						int index188_620 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_620);
						if ( s>=0 ) return s;
						break;

					case 209 : 
						int LA188_671 = input.LA(1);
						 
						int index188_671 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_671);
						if ( s>=0 ) return s;
						break;

					case 210 : 
						int LA188_673 = input.LA(1);
						 
						int index188_673 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_673);
						if ( s>=0 ) return s;
						break;

					case 211 : 
						int LA188_740 = input.LA(1);
						 
						int index188_740 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_740);
						if ( s>=0 ) return s;
						break;

					case 212 : 
						int LA188_336 = input.LA(1);
						 
						int index188_336 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_336);
						if ( s>=0 ) return s;
						break;

					case 213 : 
						int LA188_800 = input.LA(1);
						 
						int index188_800 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_800);
						if ( s>=0 ) return s;
						break;

					case 214 : 
						int LA188_337 = input.LA(1);
						 
						int index188_337 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_337);
						if ( s>=0 ) return s;
						break;

					case 215 : 
						int LA188_801 = input.LA(1);
						 
						int index188_801 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_801);
						if ( s>=0 ) return s;
						break;

					case 216 : 
						int LA188_342 = input.LA(1);
						 
						int index188_342 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_342);
						if ( s>=0 ) return s;
						break;

					case 217 : 
						int LA188_343 = input.LA(1);
						 
						int index188_343 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_343);
						if ( s>=0 ) return s;
						break;

					case 218 : 
						int LA188_828 = input.LA(1);
						 
						int index188_828 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_828);
						if ( s>=0 ) return s;
						break;

					case 219 : 
						int LA188_821 = input.LA(1);
						 
						int index188_821 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_821);
						if ( s>=0 ) return s;
						break;

					case 220 : 
						int LA188_773 = input.LA(1);
						 
						int index188_773 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_773);
						if ( s>=0 ) return s;
						break;

					case 221 : 
						int LA188_771 = input.LA(1);
						 
						int index188_771 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_771);
						if ( s>=0 ) return s;
						break;

					case 222 : 
						int LA188_355 = input.LA(1);
						 
						int index188_355 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_355);
						if ( s>=0 ) return s;
						break;

					case 223 : 
						int LA188_112 = input.LA(1);
						 
						int index188_112 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_112);
						if ( s>=0 ) return s;
						break;

					case 224 : 
						int LA188_117 = input.LA(1);
						 
						int index188_117 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_117);
						if ( s>=0 ) return s;
						break;

					case 225 : 
						int LA188_405 = input.LA(1);
						 
						int index188_405 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_405);
						if ( s>=0 ) return s;
						break;

					case 226 : 
						int LA188_407 = input.LA(1);
						 
						int index188_407 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_407);
						if ( s>=0 ) return s;
						break;

					case 227 : 
						int LA188_486 = input.LA(1);
						 
						int index188_486 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_486);
						if ( s>=0 ) return s;
						break;

					case 228 : 
						int LA188_539 = input.LA(1);
						 
						int index188_539 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_539);
						if ( s>=0 ) return s;
						break;

					case 229 : 
						int LA188_541 = input.LA(1);
						 
						int index188_541 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_541);
						if ( s>=0 ) return s;
						break;

					case 230 : 
						int LA188_619 = input.LA(1);
						 
						int index188_619 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_619);
						if ( s>=0 ) return s;
						break;

					case 231 : 
						int LA188_670 = input.LA(1);
						 
						int index188_670 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_670);
						if ( s>=0 ) return s;
						break;

					case 232 : 
						int LA188_672 = input.LA(1);
						 
						int index188_672 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_672);
						if ( s>=0 ) return s;
						break;

					case 233 : 
						int LA188_739 = input.LA(1);
						 
						int index188_739 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_739);
						if ( s>=0 ) return s;
						break;

					case 234 : 
						int LA188_278 = input.LA(1);
						 
						int index188_278 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_278);
						if ( s>=0 ) return s;
						break;

					case 235 : 
						int LA188_270 = input.LA(1);
						 
						int index188_270 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_270);
						if ( s>=0 ) return s;
						break;

					case 236 : 
						int LA188_65 = input.LA(1);
						 
						int index188_65 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_65);
						if ( s>=0 ) return s;
						break;

					case 237 : 
						int LA188_96 = input.LA(1);
						 
						int index188_96 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_96);
						if ( s>=0 ) return s;
						break;

					case 238 : 
						int LA188_144 = input.LA(1);
						 
						int index188_144 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_144);
						if ( s>=0 ) return s;
						break;

					case 239 : 
						int LA188_151 = input.LA(1);
						 
						int index188_151 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_151);
						if ( s>=0 ) return s;
						break;

					case 240 : 
						int LA188_385 = input.LA(1);
						 
						int index188_385 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_385);
						if ( s>=0 ) return s;
						break;

					case 241 : 
						int LA188_393 = input.LA(1);
						 
						int index188_393 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_393);
						if ( s>=0 ) return s;
						break;

					case 242 : 
						int LA188_519 = input.LA(1);
						 
						int index188_519 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_519);
						if ( s>=0 ) return s;
						break;

					case 243 : 
						int LA188_527 = input.LA(1);
						 
						int index188_527 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_527);
						if ( s>=0 ) return s;
						break;

					case 244 : 
						int LA188_650 = input.LA(1);
						 
						int index188_650 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_650);
						if ( s>=0 ) return s;
						break;

					case 245 : 
						int LA188_658 = input.LA(1);
						 
						int index188_658 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_658);
						if ( s>=0 ) return s;
						break;

					case 246 : 
						int LA188_755 = input.LA(1);
						 
						int index188_755 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_755);
						if ( s>=0 ) return s;
						break;

					case 247 : 
						int LA188_763 = input.LA(1);
						 
						int index188_763 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_763);
						if ( s>=0 ) return s;
						break;

					case 248 : 
						int LA188_383 = input.LA(1);
						 
						int index188_383 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_383);
						if ( s>=0 ) return s;
						break;

					case 249 : 
						int LA188_276 = input.LA(1);
						 
						int index188_276 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_276);
						if ( s>=0 ) return s;
						break;

					case 250 : 
						int LA188_268 = input.LA(1);
						 
						int index188_268 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_268);
						if ( s>=0 ) return s;
						break;

					case 251 : 
						int LA188_94 = input.LA(1);
						 
						int index188_94 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_94);
						if ( s>=0 ) return s;
						break;

					case 252 : 
						int LA188_62 = input.LA(1);
						 
						int index188_62 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_62);
						if ( s>=0 ) return s;
						break;

					case 253 : 
						int LA188_143 = input.LA(1);
						 
						int index188_143 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_143);
						if ( s>=0 ) return s;
						break;

					case 254 : 
						int LA188_150 = input.LA(1);
						 
						int index188_150 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_150);
						if ( s>=0 ) return s;
						break;

					case 255 : 
						int LA188_391 = input.LA(1);
						 
						int index188_391 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_391);
						if ( s>=0 ) return s;
						break;

					case 256 : 
						int LA188_517 = input.LA(1);
						 
						int index188_517 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_517);
						if ( s>=0 ) return s;
						break;

					case 257 : 
						int LA188_525 = input.LA(1);
						 
						int index188_525 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_525);
						if ( s>=0 ) return s;
						break;

					case 258 : 
						int LA188_648 = input.LA(1);
						 
						int index188_648 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_648);
						if ( s>=0 ) return s;
						break;

					case 259 : 
						int LA188_656 = input.LA(1);
						 
						int index188_656 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_656);
						if ( s>=0 ) return s;
						break;

					case 260 : 
						int LA188_753 = input.LA(1);
						 
						int index188_753 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_753);
						if ( s>=0 ) return s;
						break;

					case 261 : 
						int LA188_761 = input.LA(1);
						 
						int index188_761 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_761);
						if ( s>=0 ) return s;
						break;

					case 262 : 
						int LA188_833 = input.LA(1);
						 
						int index188_833 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_833);
						if ( s>=0 ) return s;
						break;

					case 263 : 
						int LA188_834 = input.LA(1);
						 
						int index188_834 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_834);
						if ( s>=0 ) return s;
						break;

					case 264 : 
						int LA188_585 = input.LA(1);
						 
						int index188_585 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_585);
						if ( s>=0 ) return s;
						break;

					case 265 : 
						int LA188_164 = input.LA(1);
						 
						int index188_164 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_164);
						if ( s>=0 ) return s;
						break;

					case 266 : 
						int LA188_584 = input.LA(1);
						 
						int index188_584 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_584);
						if ( s>=0 ) return s;
						break;

					case 267 : 
						int LA188_163 = input.LA(1);
						 
						int index188_163 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_163);
						if ( s>=0 ) return s;
						break;

					case 268 : 
						int LA188_675 = input.LA(1);
						 
						int index188_675 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_675);
						if ( s>=0 ) return s;
						break;

					case 269 : 
						int LA188_679 = input.LA(1);
						 
						int index188_679 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_679);
						if ( s>=0 ) return s;
						break;

					case 270 : 
						int LA188_674 = input.LA(1);
						 
						int index188_674 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_674);
						if ( s>=0 ) return s;
						break;

					case 271 : 
						int LA188_678 = input.LA(1);
						 
						int index188_678 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_678);
						if ( s>=0 ) return s;
						break;

					case 272 : 
						int LA188_20 = input.LA(1);
						 
						int index188_20 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_20);
						if ( s>=0 ) return s;
						break;

					case 273 : 
						int LA188_9 = input.LA(1);
						 
						int index188_9 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_9);
						if ( s>=0 ) return s;
						break;

					case 274 : 
						int LA188_639 = input.LA(1);
						 
						int index188_639 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_639);
						if ( s>=0 ) return s;
						break;

					case 275 : 
						int LA188_295 = input.LA(1);
						 
						int index188_295 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_295);
						if ( s>=0 ) return s;
						break;

					case 276 : 
						int LA188_705 = input.LA(1);
						 
						int index188_705 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_705);
						if ( s>=0 ) return s;
						break;

					case 277 : 
						int LA188_690 = input.LA(1);
						 
						int index188_690 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_690);
						if ( s>=0 ) return s;
						break;

					case 278 : 
						int LA188_340 = input.LA(1);
						 
						int index188_340 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_340);
						if ( s>=0 ) return s;
						break;

					case 279 : 
						int LA188_838 = input.LA(1);
						 
						int index188_838 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_838);
						if ( s>=0 ) return s;
						break;

					case 280 : 
						int LA188_826 = input.LA(1);
						 
						int index188_826 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_826);
						if ( s>=0 ) return s;
						break;

					case 281 : 
						int LA188_824 = input.LA(1);
						 
						int index188_824 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_824);
						if ( s>=0 ) return s;
						break;

					case 282 : 
						int LA188_815 = input.LA(1);
						 
						int index188_815 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_815);
						if ( s>=0 ) return s;
						break;

					case 283 : 
						int LA188_230 = input.LA(1);
						 
						int index188_230 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_230);
						if ( s>=0 ) return s;
						break;

					case 284 : 
						int LA188_228 = input.LA(1);
						 
						int index188_228 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_228);
						if ( s>=0 ) return s;
						break;

					case 285 : 
						int LA188_472 = input.LA(1);
						 
						int index188_472 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_472);
						if ( s>=0 ) return s;
						break;

					case 286 : 
						int LA188_489 = input.LA(1);
						 
						int index188_489 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_489);
						if ( s>=0 ) return s;
						break;

					case 287 : 
						int LA188_491 = input.LA(1);
						 
						int index188_491 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_491);
						if ( s>=0 ) return s;
						break;

					case 288 : 
						int LA188_606 = input.LA(1);
						 
						int index188_606 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_606);
						if ( s>=0 ) return s;
						break;

					case 289 : 
						int LA188_622 = input.LA(1);
						 
						int index188_622 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_622);
						if ( s>=0 ) return s;
						break;

					case 290 : 
						int LA188_624 = input.LA(1);
						 
						int index188_624 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_624);
						if ( s>=0 ) return s;
						break;

					case 291 : 
						int LA188_728 = input.LA(1);
						 
						int index188_728 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_728);
						if ( s>=0 ) return s;
						break;

					case 292 : 
						int LA188_742 = input.LA(1);
						 
						int index188_742 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_742);
						if ( s>=0 ) return s;
						break;

					case 293 : 
						int LA188_744 = input.LA(1);
						 
						int index188_744 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_744);
						if ( s>=0 ) return s;
						break;

					case 294 : 
						int LA188_837 = input.LA(1);
						 
						int index188_837 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_837);
						if ( s>=0 ) return s;
						break;

					case 295 : 
						int LA188_825 = input.LA(1);
						 
						int index188_825 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_825);
						if ( s>=0 ) return s;
						break;

					case 296 : 
						int LA188_823 = input.LA(1);
						 
						int index188_823 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_823);
						if ( s>=0 ) return s;
						break;

					case 297 : 
						int LA188_814 = input.LA(1);
						 
						int index188_814 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_814);
						if ( s>=0 ) return s;
						break;

					case 298 : 
						int LA188_229 = input.LA(1);
						 
						int index188_229 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_229);
						if ( s>=0 ) return s;
						break;

					case 299 : 
						int LA188_226 = input.LA(1);
						 
						int index188_226 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_226);
						if ( s>=0 ) return s;
						break;

					case 300 : 
						int LA188_471 = input.LA(1);
						 
						int index188_471 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_471);
						if ( s>=0 ) return s;
						break;

					case 301 : 
						int LA188_488 = input.LA(1);
						 
						int index188_488 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_488);
						if ( s>=0 ) return s;
						break;

					case 302 : 
						int LA188_490 = input.LA(1);
						 
						int index188_490 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_490);
						if ( s>=0 ) return s;
						break;

					case 303 : 
						int LA188_605 = input.LA(1);
						 
						int index188_605 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_605);
						if ( s>=0 ) return s;
						break;

					case 304 : 
						int LA188_621 = input.LA(1);
						 
						int index188_621 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_621);
						if ( s>=0 ) return s;
						break;

					case 305 : 
						int LA188_623 = input.LA(1);
						 
						int index188_623 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_623);
						if ( s>=0 ) return s;
						break;

					case 306 : 
						int LA188_727 = input.LA(1);
						 
						int index188_727 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_727);
						if ( s>=0 ) return s;
						break;

					case 307 : 
						int LA188_741 = input.LA(1);
						 
						int index188_741 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_741);
						if ( s>=0 ) return s;
						break;

					case 308 : 
						int LA188_743 = input.LA(1);
						 
						int index188_743 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_743);
						if ( s>=0 ) return s;
						break;

					case 309 : 
						int LA188_299 = input.LA(1);
						 
						int index188_299 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_299);
						if ( s>=0 ) return s;
						break;

					case 310 : 
						int LA188_185 = input.LA(1);
						 
						int index188_185 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_185);
						if ( s>=0 ) return s;
						break;

					case 311 : 
						int LA188_184 = input.LA(1);
						 
						int index188_184 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_184);
						if ( s>=0 ) return s;
						break;

					case 312 : 
						int LA188_41 = input.LA(1);
						 
						int index188_41 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_41);
						if ( s>=0 ) return s;
						break;

					case 313 : 
						int LA188_42 = input.LA(1);
						 
						int index188_42 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_42);
						if ( s>=0 ) return s;
						break;

					case 314 : 
						int LA188_377 = input.LA(1);
						 
						int index188_377 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_377);
						if ( s>=0 ) return s;
						break;

					case 315 : 
						int LA188_262 = input.LA(1);
						 
						int index188_262 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_262);
						if ( s>=0 ) return s;
						break;

					case 316 : 
						int LA188_82 = input.LA(1);
						 
						int index188_82 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_82);
						if ( s>=0 ) return s;
						break;

					case 317 : 
						int LA188_47 = input.LA(1);
						 
						int index188_47 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_47);
						if ( s>=0 ) return s;
						break;

					case 318 : 
						int LA188_511 = input.LA(1);
						 
						int index188_511 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_511);
						if ( s>=0 ) return s;
						break;

					case 319 : 
						int LA188_642 = input.LA(1);
						 
						int index188_642 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_642);
						if ( s>=0 ) return s;
						break;

					case 320 : 
						int LA188_747 = input.LA(1);
						 
						int index188_747 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_747);
						if ( s>=0 ) return s;
						break;

					case 321 : 
						int LA188_379 = input.LA(1);
						 
						int index188_379 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_379);
						if ( s>=0 ) return s;
						break;

					case 322 : 
						int LA188_264 = input.LA(1);
						 
						int index188_264 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_264);
						if ( s>=0 ) return s;
						break;

					case 323 : 
						int LA188_84 = input.LA(1);
						 
						int index188_84 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_84);
						if ( s>=0 ) return s;
						break;

					case 324 : 
						int LA188_50 = input.LA(1);
						 
						int index188_50 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_50);
						if ( s>=0 ) return s;
						break;

					case 325 : 
						int LA188_513 = input.LA(1);
						 
						int index188_513 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_513);
						if ( s>=0 ) return s;
						break;

					case 326 : 
						int LA188_644 = input.LA(1);
						 
						int index188_644 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_644);
						if ( s>=0 ) return s;
						break;

					case 327 : 
						int LA188_749 = input.LA(1);
						 
						int index188_749 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_749);
						if ( s>=0 ) return s;
						break;

					case 328 : 
						int LA188_709 = input.LA(1);
						 
						int index188_709 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_709);
						if ( s>=0 ) return s;
						break;

					case 329 : 
						int LA188_80 = input.LA(1);
						s = -1;
						if ( (LA188_80=='z') ) {s = 217;}
						else if ( (LA188_80=='Z') ) {s = 218;}
						else if ( ((LA188_80 >= '\u0000' && LA188_80 <= '\t')||LA188_80=='\u000B'||(LA188_80 >= '\u000E' && LA188_80 <= '/')||(LA188_80 >= '1' && LA188_80 <= '4')||LA188_80=='6'||(LA188_80 >= '8' && LA188_80 <= 'Y')||(LA188_80 >= '[' && LA188_80 <= 'y')||(LA188_80 >= '{' && LA188_80 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA188_80=='0') ) {s = 219;}
						else if ( (LA188_80=='5'||LA188_80=='7') ) {s = 220;}
						if ( s>=0 ) return s;
						break;

					case 330 : 
						int LA188_579 = input.LA(1);
						 
						int index188_579 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_579);
						if ( s>=0 ) return s;
						break;

					case 331 : 
						int LA188_710 = input.LA(1);
						 
						int index188_710 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_710);
						if ( s>=0 ) return s;
						break;

					case 332 : 
						int LA188_451 = input.LA(1);
						 
						int index188_451 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_451);
						if ( s>=0 ) return s;
						break;

					case 333 : 
						int LA188_381 = input.LA(1);
						 
						int index188_381 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_381);
						if ( s>=0 ) return s;
						break;

					case 334 : 
						int LA188_266 = input.LA(1);
						 
						int index188_266 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_266);
						if ( s>=0 ) return s;
						break;

					case 335 : 
						int LA188_92 = input.LA(1);
						 
						int index188_92 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_92);
						if ( s>=0 ) return s;
						break;

					case 336 : 
						int LA188_59 = input.LA(1);
						 
						int index188_59 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_59);
						if ( s>=0 ) return s;
						break;

					case 337 : 
						int LA188_515 = input.LA(1);
						 
						int index188_515 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_515);
						if ( s>=0 ) return s;
						break;

					case 338 : 
						int LA188_646 = input.LA(1);
						 
						int index188_646 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_646);
						if ( s>=0 ) return s;
						break;

					case 339 : 
						int LA188_751 = input.LA(1);
						 
						int index188_751 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_751);
						if ( s>=0 ) return s;
						break;

					case 340 : 
						int LA188_450 = input.LA(1);
						 
						int index188_450 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_450);
						if ( s>=0 ) return s;
						break;

					case 341 : 
						int LA188_63 = input.LA(1);
						s = -1;
						if ( (LA188_63=='m') ) {s = 184;}
						else if ( (LA188_63=='M') ) {s = 185;}
						else if ( (LA188_63=='s') ) {s = 186;}
						else if ( (LA188_63=='0') ) {s = 187;}
						else if ( (LA188_63=='4'||LA188_63=='6') ) {s = 188;}
						else if ( (LA188_63=='S') ) {s = 189;}
						else if ( ((LA188_63 >= '\u0000' && LA188_63 <= '\t')||LA188_63=='\u000B'||(LA188_63 >= '\u000E' && LA188_63 <= '/')||(LA188_63 >= '1' && LA188_63 <= '3')||(LA188_63 >= '8' && LA188_63 <= 'L')||(LA188_63 >= 'N' && LA188_63 <= 'R')||(LA188_63 >= 'T' && LA188_63 <= 'l')||(LA188_63 >= 'n' && LA188_63 <= 'r')||(LA188_63 >= 't' && LA188_63 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA188_63=='5'||LA188_63=='7') ) {s = 190;}
						if ( s>=0 ) return s;
						break;

					case 342 : 
						int LA188_382 = input.LA(1);
						 
						int index188_382 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_382);
						if ( s>=0 ) return s;
						break;

					case 343 : 
						int LA188_267 = input.LA(1);
						 
						int index188_267 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_267);
						if ( s>=0 ) return s;
						break;

					case 344 : 
						int LA188_93 = input.LA(1);
						 
						int index188_93 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_93);
						if ( s>=0 ) return s;
						break;

					case 345 : 
						int LA188_61 = input.LA(1);
						 
						int index188_61 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_61);
						if ( s>=0 ) return s;
						break;

					case 346 : 
						int LA188_516 = input.LA(1);
						 
						int index188_516 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_516);
						if ( s>=0 ) return s;
						break;

					case 347 : 
						int LA188_647 = input.LA(1);
						 
						int index188_647 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_647);
						if ( s>=0 ) return s;
						break;

					case 348 : 
						int LA188_752 = input.LA(1);
						 
						int index188_752 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_752);
						if ( s>=0 ) return s;
						break;

					case 349 : 
						int LA188_324 = input.LA(1);
						 
						int index188_324 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_324);
						if ( s>=0 ) return s;
						break;

					case 350 : 
						int LA188_323 = input.LA(1);
						 
						int index188_323 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_323);
						if ( s>=0 ) return s;
						break;

					case 351 : 
						int LA188_795 = input.LA(1);
						 
						int index188_795 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_795);
						if ( s>=0 ) return s;
						break;

					case 352 : 
						int LA188_794 = input.LA(1);
						 
						int index188_794 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_794);
						if ( s>=0 ) return s;
						break;

					case 353 : 
						int LA188_427 = input.LA(1);
						 
						int index188_427 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_427);
						if ( s>=0 ) return s;
						break;

					case 354 : 
						int LA188_699 = input.LA(1);
						 
						int index188_699 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_699);
						if ( s>=0 ) return s;
						break;

					case 355 : 
						int LA188_562 = input.LA(1);
						 
						int index188_562 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_562);
						if ( s>=0 ) return s;
						break;

					case 356 : 
						int LA188_797 = input.LA(1);
						 
						int index188_797 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_797);
						if ( s>=0 ) return s;
						break;

					case 357 : 
						int LA188_438 = input.LA(1);
						 
						int index188_438 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_438);
						if ( s>=0 ) return s;
						break;

					case 358 : 
						int LA188_792 = input.LA(1);
						 
						int index188_792 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_792);
						if ( s>=0 ) return s;
						break;

					case 359 : 
						int LA188_791 = input.LA(1);
						 
						int index188_791 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_791);
						if ( s>=0 ) return s;
						break;

					case 360 : 
						int LA188_273 = input.LA(1);
						 
						int index188_273 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_273);
						if ( s>=0 ) return s;
						break;

					case 361 : 
						int LA188_69 = input.LA(1);
						 
						int index188_69 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_69);
						if ( s>=0 ) return s;
						break;

					case 362 : 
						int LA188_99 = input.LA(1);
						 
						int index188_99 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_99);
						if ( s>=0 ) return s;
						break;

					case 363 : 
						int LA188_155 = input.LA(1);
						 
						int index188_155 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_155);
						if ( s>=0 ) return s;
						break;

					case 364 : 
						int LA188_157 = input.LA(1);
						 
						int index188_157 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_157);
						if ( s>=0 ) return s;
						break;

					case 365 : 
						int LA188_388 = input.LA(1);
						 
						int index188_388 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_388);
						if ( s>=0 ) return s;
						break;

					case 366 : 
						int LA188_522 = input.LA(1);
						 
						int index188_522 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_522);
						if ( s>=0 ) return s;
						break;

					case 367 : 
						int LA188_653 = input.LA(1);
						 
						int index188_653 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_653);
						if ( s>=0 ) return s;
						break;

					case 368 : 
						int LA188_758 = input.LA(1);
						 
						int index188_758 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_758);
						if ( s>=0 ) return s;
						break;

					case 369 : 
						int LA188_272 = input.LA(1);
						 
						int index188_272 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_272);
						if ( s>=0 ) return s;
						break;

					case 370 : 
						int LA188_67 = input.LA(1);
						 
						int index188_67 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_67);
						if ( s>=0 ) return s;
						break;

					case 371 : 
						int LA188_98 = input.LA(1);
						 
						int index188_98 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_98);
						if ( s>=0 ) return s;
						break;

					case 372 : 
						int LA188_103 = input.LA(1);
						s = -1;
						if ( (LA188_103=='i') ) {s = 222;}
						else if ( (LA188_103=='I') ) {s = 223;}
						else if ( ((LA188_103 >= '\u0000' && LA188_103 <= '\t')||LA188_103=='\u000B'||(LA188_103 >= '\u000E' && LA188_103 <= '/')||(LA188_103 >= '1' && LA188_103 <= '3')||LA188_103=='5'||(LA188_103 >= '7' && LA188_103 <= 'H')||(LA188_103 >= 'J' && LA188_103 <= 'h')||(LA188_103 >= 'j' && LA188_103 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA188_103=='0') ) {s = 224;}
						else if ( (LA188_103=='4'||LA188_103=='6') ) {s = 225;}
						if ( s>=0 ) return s;
						break;

					case 373 : 
						int LA188_154 = input.LA(1);
						 
						int index188_154 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_154);
						if ( s>=0 ) return s;
						break;

					case 374 : 
						int LA188_156 = input.LA(1);
						 
						int index188_156 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_156);
						if ( s>=0 ) return s;
						break;

					case 375 : 
						int LA188_387 = input.LA(1);
						 
						int index188_387 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_387);
						if ( s>=0 ) return s;
						break;

					case 376 : 
						int LA188_521 = input.LA(1);
						 
						int index188_521 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_521);
						if ( s>=0 ) return s;
						break;

					case 377 : 
						int LA188_652 = input.LA(1);
						 
						int index188_652 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_652);
						if ( s>=0 ) return s;
						break;

					case 378 : 
						int LA188_757 = input.LA(1);
						 
						int index188_757 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_757);
						if ( s>=0 ) return s;
						break;

					case 379 : 
						int LA188_789 = input.LA(1);
						 
						int index188_789 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_789);
						if ( s>=0 ) return s;
						break;

					case 380 : 
						int LA188_790 = input.LA(1);
						 
						int index188_790 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_790);
						if ( s>=0 ) return s;
						break;

					case 381 : 
						int LA188_192 = input.LA(1);
						 
						int index188_192 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_192);
						if ( s>=0 ) return s;
						break;

					case 382 : 
						int LA188_193 = input.LA(1);
						 
						int index188_193 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_193);
						if ( s>=0 ) return s;
						break;

					case 383 : 
						int LA188_71 = input.LA(1);
						s = -1;
						if ( ((LA188_71 >= '\u0000' && LA188_71 <= '\t')||LA188_71=='\u000B'||(LA188_71 >= '\u000E' && LA188_71 <= '/')||(LA188_71 >= '1' && LA188_71 <= '3')||LA188_71=='5'||(LA188_71 >= '7' && LA188_71 <= '\uFFFF')) ) {s = 12;}
						else if ( (LA188_71=='0') ) {s = 199;}
						else if ( (LA188_71=='4'||LA188_71=='6') ) {s = 200;}
						if ( s>=0 ) return s;
						break;

					case 384 : 
						int LA188_428 = input.LA(1);
						 
						int index188_428 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_428);
						if ( s>=0 ) return s;
						break;

					case 385 : 
						int LA188_300 = input.LA(1);
						 
						int index188_300 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_300);
						if ( s>=0 ) return s;
						break;

					case 386 : 
						int LA188_359 = input.LA(1);
						 
						int index188_359 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred7_Css3()) ) {s = 240;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_359);
						if ( s>=0 ) return s;
						break;

					case 387 : 
						int LA188_691 = input.LA(1);
						 
						int index188_691 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_691);
						if ( s>=0 ) return s;
						break;

					case 388 : 
						int LA188_281 = input.LA(1);
						 
						int index188_281 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_281);
						if ( s>=0 ) return s;
						break;

					case 389 : 
						int LA188_87 = input.LA(1);
						 
						int index188_87 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_87);
						if ( s>=0 ) return s;
						break;

					case 390 : 
						int LA188_54 = input.LA(1);
						 
						int index188_54 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_54);
						if ( s>=0 ) return s;
						break;

					case 391 : 
						int LA188_121 = input.LA(1);
						 
						int index188_121 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_121);
						if ( s>=0 ) return s;
						break;

					case 392 : 
						int LA188_139 = input.LA(1);
						 
						int index188_139 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_139);
						if ( s>=0 ) return s;
						break;

					case 393 : 
						int LA188_396 = input.LA(1);
						 
						int index188_396 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_396);
						if ( s>=0 ) return s;
						break;

					case 394 : 
						int LA188_530 = input.LA(1);
						 
						int index188_530 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_530);
						if ( s>=0 ) return s;
						break;

					case 395 : 
						int LA188_661 = input.LA(1);
						 
						int index188_661 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_661);
						if ( s>=0 ) return s;
						break;

					case 396 : 
						int LA188_766 = input.LA(1);
						 
						int index188_766 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_766);
						if ( s>=0 ) return s;
						break;

					case 397 : 
						int LA188_277 = input.LA(1);
						 
						int index188_277 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_277);
						if ( s>=0 ) return s;
						break;

					case 398 : 
						int LA188_269 = input.LA(1);
						 
						int index188_269 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_269);
						if ( s>=0 ) return s;
						break;

					case 399 : 
						int LA188_95 = input.LA(1);
						 
						int index188_95 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_95);
						if ( s>=0 ) return s;
						break;

					case 400 : 
						int LA188_64 = input.LA(1);
						 
						int index188_64 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_64);
						if ( s>=0 ) return s;
						break;

					case 401 : 
						int LA188_145 = input.LA(1);
						 
						int index188_145 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_145);
						if ( s>=0 ) return s;
						break;

					case 402 : 
						int LA188_152 = input.LA(1);
						 
						int index188_152 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_152);
						if ( s>=0 ) return s;
						break;

					case 403 : 
						int LA188_384 = input.LA(1);
						 
						int index188_384 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_384);
						if ( s>=0 ) return s;
						break;

					case 404 : 
						int LA188_392 = input.LA(1);
						 
						int index188_392 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_392);
						if ( s>=0 ) return s;
						break;

					case 405 : 
						int LA188_518 = input.LA(1);
						 
						int index188_518 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_518);
						if ( s>=0 ) return s;
						break;

					case 406 : 
						int LA188_526 = input.LA(1);
						 
						int index188_526 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_526);
						if ( s>=0 ) return s;
						break;

					case 407 : 
						int LA188_649 = input.LA(1);
						 
						int index188_649 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_649);
						if ( s>=0 ) return s;
						break;

					case 408 : 
						int LA188_657 = input.LA(1);
						 
						int index188_657 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_657);
						if ( s>=0 ) return s;
						break;

					case 409 : 
						int LA188_754 = input.LA(1);
						 
						int index188_754 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_754);
						if ( s>=0 ) return s;
						break;

					case 410 : 
						int LA188_762 = input.LA(1);
						 
						int index188_762 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_762);
						if ( s>=0 ) return s;
						break;

					case 411 : 
						int LA188_279 = input.LA(1);
						 
						int index188_279 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_279);
						if ( s>=0 ) return s;
						break;

					case 412 : 
						int LA188_271 = input.LA(1);
						 
						int index188_271 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_271);
						if ( s>=0 ) return s;
						break;

					case 413 : 
						int LA188_66 = input.LA(1);
						 
						int index188_66 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_66);
						if ( s>=0 ) return s;
						break;

					case 414 : 
						int LA188_97 = input.LA(1);
						 
						int index188_97 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_97);
						if ( s>=0 ) return s;
						break;

					case 415 : 
						int LA188_146 = input.LA(1);
						 
						int index188_146 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_146);
						if ( s>=0 ) return s;
						break;

					case 416 : 
						int LA188_153 = input.LA(1);
						 
						int index188_153 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_153);
						if ( s>=0 ) return s;
						break;

					case 417 : 
						int LA188_386 = input.LA(1);
						 
						int index188_386 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_386);
						if ( s>=0 ) return s;
						break;

					case 418 : 
						int LA188_394 = input.LA(1);
						 
						int index188_394 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_394);
						if ( s>=0 ) return s;
						break;

					case 419 : 
						int LA188_520 = input.LA(1);
						 
						int index188_520 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_520);
						if ( s>=0 ) return s;
						break;

					case 420 : 
						int LA188_528 = input.LA(1);
						 
						int index188_528 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_528);
						if ( s>=0 ) return s;
						break;

					case 421 : 
						int LA188_651 = input.LA(1);
						 
						int index188_651 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_651);
						if ( s>=0 ) return s;
						break;

					case 422 : 
						int LA188_659 = input.LA(1);
						 
						int index188_659 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_659);
						if ( s>=0 ) return s;
						break;

					case 423 : 
						int LA188_756 = input.LA(1);
						 
						int index188_756 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_756);
						if ( s>=0 ) return s;
						break;

					case 424 : 
						int LA188_764 = input.LA(1);
						 
						int index188_764 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_764);
						if ( s>=0 ) return s;
						break;

					case 425 : 
						int LA188_769 = input.LA(1);
						 
						int index188_769 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_769);
						if ( s>=0 ) return s;
						break;

					case 426 : 
						int LA188_284 = input.LA(1);
						 
						int index188_284 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_284);
						if ( s>=0 ) return s;
						break;

					case 427 : 
						int LA188_90 = input.LA(1);
						 
						int index188_90 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_90);
						if ( s>=0 ) return s;
						break;

					case 428 : 
						int LA188_57 = input.LA(1);
						 
						int index188_57 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_57);
						if ( s>=0 ) return s;
						break;

					case 429 : 
						int LA188_122 = input.LA(1);
						 
						int index188_122 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_122);
						if ( s>=0 ) return s;
						break;

					case 430 : 
						int LA188_140 = input.LA(1);
						 
						int index188_140 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_140);
						if ( s>=0 ) return s;
						break;

					case 431 : 
						int LA188_399 = input.LA(1);
						 
						int index188_399 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_399);
						if ( s>=0 ) return s;
						break;

					case 432 : 
						int LA188_533 = input.LA(1);
						 
						int index188_533 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_533);
						if ( s>=0 ) return s;
						break;

					case 433 : 
						int LA188_664 = input.LA(1);
						 
						int index188_664 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_664);
						if ( s>=0 ) return s;
						break;

					case 434 : 
						int LA188_716 = input.LA(1);
						 
						int index188_716 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_716);
						if ( s>=0 ) return s;
						break;

					case 435 : 
						int LA188_715 = input.LA(1);
						 
						int index188_715 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_715);
						if ( s>=0 ) return s;
						break;

					case 436 : 
						int LA188_397 = input.LA(1);
						 
						int index188_397 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_397);
						if ( s>=0 ) return s;
						break;

					case 437 : 
						int LA188_400 = input.LA(1);
						 
						int index188_400 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_400);
						if ( s>=0 ) return s;
						break;

					case 438 : 
						int LA188_839 = input.LA(1);
						 
						int index188_839 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_839);
						if ( s>=0 ) return s;
						break;

					case 439 : 
						int LA188_840 = input.LA(1);
						 
						int index188_840 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_840);
						if ( s>=0 ) return s;
						break;

					case 440 : 
						int LA188_784 = input.LA(1);
						 
						int index188_784 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_784);
						if ( s>=0 ) return s;
						break;

					case 441 : 
						int LA188_311 = input.LA(1);
						 
						int index188_311 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_311);
						if ( s>=0 ) return s;
						break;

					case 442 : 
						int LA188_783 = input.LA(1);
						 
						int index188_783 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_783);
						if ( s>=0 ) return s;
						break;

					case 443 : 
						int LA188_571 = input.LA(1);
						 
						int index188_571 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_571);
						if ( s>=0 ) return s;
						break;

					case 444 : 
						int LA188_570 = input.LA(1);
						 
						int index188_570 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_570);
						if ( s>=0 ) return s;
						break;

					case 445 : 
						int LA188_665 = input.LA(1);
						 
						int index188_665 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_665);
						if ( s>=0 ) return s;
						break;

					case 446 : 
						int LA188_662 = input.LA(1);
						 
						int index188_662 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_662);
						if ( s>=0 ) return s;
						break;

					case 447 : 
						int LA188_811 = input.LA(1);
						 
						int index188_811 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_811);
						if ( s>=0 ) return s;
						break;

					case 448 : 
						int LA188_566 = input.LA(1);
						 
						int index188_566 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_566);
						if ( s>=0 ) return s;
						break;

					case 449 : 
						int LA188_591 = input.LA(1);
						 
						int index188_591 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_591);
						if ( s>=0 ) return s;
						break;

					case 450 : 
						int LA188_810 = input.LA(1);
						 
						int index188_810 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred10_Css3()) ) {s = 216;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_810);
						if ( s>=0 ) return s;
						break;

					case 451 : 
						int LA188_565 = input.LA(1);
						 
						int index188_565 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred4_Css3()) ) {s = 178;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_565);
						if ( s>=0 ) return s;
						break;

					case 452 : 
						int LA188_590 = input.LA(1);
						 
						int index188_590 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_590);
						if ( s>=0 ) return s;
						break;

					case 453 : 
						int LA188_805 = input.LA(1);
						 
						int index188_805 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_805);
						if ( s>=0 ) return s;
						break;

					case 454 : 
						int LA188_806 = input.LA(1);
						 
						int index188_806 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_806);
						if ( s>=0 ) return s;
						break;

					case 455 : 
						int LA188_731 = input.LA(1);
						 
						int index188_731 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_731);
						if ( s>=0 ) return s;
						break;

					case 456 : 
						int LA188_732 = input.LA(1);
						 
						int index188_732 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred1_Css3()) ) {s = 221;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_732);
						if ( s>=0 ) return s;
						break;

					case 457 : 
						int LA188_555 = input.LA(1);
						 
						int index188_555 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_555);
						if ( s>=0 ) return s;
						break;

					case 458 : 
						int LA188_554 = input.LA(1);
						 
						int index188_554 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred2_Css3()) ) {s = 162;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_554);
						if ( s>=0 ) return s;
						break;

					case 459 : 
						int LA188_788 = input.LA(1);
						 
						int index188_788 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_788);
						if ( s>=0 ) return s;
						break;

					case 460 : 
						int LA188_410 = input.LA(1);
						 
						int index188_410 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_410);
						if ( s>=0 ) return s;
						break;

					case 461 : 
						int LA188_414 = input.LA(1);
						 
						int index188_414 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_414);
						if ( s>=0 ) return s;
						break;

					case 462 : 
						int LA188_697 = input.LA(1);
						 
						int index188_697 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_697);
						if ( s>=0 ) return s;
						break;

					case 463 : 
						int LA188_409 = input.LA(1);
						 
						int index188_409 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_409);
						if ( s>=0 ) return s;
						break;

					case 464 : 
						int LA188_413 = input.LA(1);
						 
						int index188_413 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_413);
						if ( s>=0 ) return s;
						break;

					case 465 : 
						int LA188_374 = input.LA(1);
						 
						int index188_374 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred9_Css3()) ) {s = 75;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_374);
						if ( s>=0 ) return s;
						break;

					case 466 : 
						int LA188_831 = input.LA(1);
						 
						int index188_831 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_831);
						if ( s>=0 ) return s;
						break;

					case 467 : 
						int LA188_802 = input.LA(1);
						 
						int index188_802 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_802);
						if ( s>=0 ) return s;
						break;

					case 468 : 
						int LA188_781 = input.LA(1);
						 
						int index188_781 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_781);
						if ( s>=0 ) return s;
						break;

					case 469 : 
						int LA188_777 = input.LA(1);
						 
						int index188_777 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_777);
						if ( s>=0 ) return s;
						break;

					case 470 : 
						int LA188_206 = input.LA(1);
						 
						int index188_206 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_206);
						if ( s>=0 ) return s;
						break;

					case 471 : 
						int LA188_201 = input.LA(1);
						 
						int index188_201 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_201);
						if ( s>=0 ) return s;
						break;

					case 472 : 
						int LA188_411 = input.LA(1);
						 
						int index188_411 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_411);
						if ( s>=0 ) return s;
						break;

					case 473 : 
						int LA188_415 = input.LA(1);
						 
						int index188_415 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_415);
						if ( s>=0 ) return s;
						break;

					case 474 : 
						int LA188_452 = input.LA(1);
						 
						int index188_452 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_452);
						if ( s>=0 ) return s;
						break;

					case 475 : 
						int LA188_545 = input.LA(1);
						 
						int index188_545 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_545);
						if ( s>=0 ) return s;
						break;

					case 476 : 
						int LA188_549 = input.LA(1);
						 
						int index188_549 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_549);
						if ( s>=0 ) return s;
						break;

					case 477 : 
						int LA188_586 = input.LA(1);
						 
						int index188_586 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_586);
						if ( s>=0 ) return s;
						break;

					case 478 : 
						int LA188_676 = input.LA(1);
						 
						int index188_676 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_676);
						if ( s>=0 ) return s;
						break;

					case 479 : 
						int LA188_680 = input.LA(1);
						 
						int index188_680 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_680);
						if ( s>=0 ) return s;
						break;

					case 480 : 
						int LA188_711 = input.LA(1);
						 
						int index188_711 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_711);
						if ( s>=0 ) return s;
						break;

					case 481 : 
						int LA188_832 = input.LA(1);
						 
						int index188_832 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_832);
						if ( s>=0 ) return s;
						break;

					case 482 : 
						int LA188_803 = input.LA(1);
						 
						int index188_803 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_803);
						if ( s>=0 ) return s;
						break;

					case 483 : 
						int LA188_782 = input.LA(1);
						 
						int index188_782 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_782);
						if ( s>=0 ) return s;
						break;

					case 484 : 
						int LA188_778 = input.LA(1);
						 
						int index188_778 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_778);
						if ( s>=0 ) return s;
						break;

					case 485 : 
						int LA188_207 = input.LA(1);
						 
						int index188_207 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_207);
						if ( s>=0 ) return s;
						break;

					case 486 : 
						int LA188_203 = input.LA(1);
						 
						int index188_203 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_203);
						if ( s>=0 ) return s;
						break;

					case 487 : 
						int LA188_412 = input.LA(1);
						 
						int index188_412 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_412);
						if ( s>=0 ) return s;
						break;

					case 488 : 
						int LA188_416 = input.LA(1);
						 
						int index188_416 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_416);
						if ( s>=0 ) return s;
						break;

					case 489 : 
						int LA188_453 = input.LA(1);
						 
						int index188_453 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_453);
						if ( s>=0 ) return s;
						break;

					case 490 : 
						int LA188_546 = input.LA(1);
						 
						int index188_546 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_546);
						if ( s>=0 ) return s;
						break;

					case 491 : 
						int LA188_550 = input.LA(1);
						 
						int index188_550 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_550);
						if ( s>=0 ) return s;
						break;

					case 492 : 
						int LA188_587 = input.LA(1);
						 
						int index188_587 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_587);
						if ( s>=0 ) return s;
						break;

					case 493 : 
						int LA188_677 = input.LA(1);
						 
						int index188_677 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_677);
						if ( s>=0 ) return s;
						break;

					case 494 : 
						int LA188_681 = input.LA(1);
						 
						int index188_681 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_681);
						if ( s>=0 ) return s;
						break;

					case 495 : 
						int LA188_712 = input.LA(1);
						 
						int index188_712 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_712);
						if ( s>=0 ) return s;
						break;

					case 496 : 
						int LA188_698 = input.LA(1);
						 
						int index188_698 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_698);
						if ( s>=0 ) return s;
						break;

					case 497 : 
						int LA188_575 = input.LA(1);
						 
						int index188_575 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_575);
						if ( s>=0 ) return s;
						break;

					case 498 : 
						int LA188_445 = input.LA(1);
						 
						int index188_445 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred8_Css3()) ) {s = 316;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_445);
						if ( s>=0 ) return s;
						break;

					case 499 : 
						int LA188_576 = input.LA(1);
						 
						int index188_576 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred6_Css3()) ) {s = 191;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_576);
						if ( s>=0 ) return s;
						break;

					case 500 : 
						int LA188_189 = input.LA(1);
						 
						int index188_189 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_189);
						if ( s>=0 ) return s;
						break;

					case 501 : 
						int LA188_186 = input.LA(1);
						 
						int index188_186 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred5_Css3()) ) {s = 183;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_186);
						if ( s>=0 ) return s;
						break;

					case 502 : 
						int LA188_173 = input.LA(1);
						 
						int index188_173 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_173);
						if ( s>=0 ) return s;
						break;

					case 503 : 
						int LA188_176 = input.LA(1);
						 
						int index188_176 = input.index();
						input.rewind();
						s = -1;
						if ( (synpred3_Css3()) ) {s = 170;}
						else if ( (true) ) {s = 12;}
						 
						input.seek(index188_176);
						if ( s>=0 ) return s;
						break;
			}
			if (state.backtracking>0) {state.failed=true; return -1;}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 188, _s, input);
			error(nvae);
			throw nvae;
		}
	}

	static final String DFA205_eotS =
		"\1\uffff\1\72\1\76\1\100\1\102\1\104\2\uffff\1\110\1\112\4\uffff\1\114"+
		"\1\uffff\1\116\1\121\4\uffff\1\123\1\124\1\131\1\40\3\uffff\3\40\1\uffff"+
		"\2\40\1\uffff\1\150\1\uffff\2\40\3\uffff\17\73\41\uffff\2\40\1\u0087\4"+
		"\40\1\uffff\5\40\2\uffff\2\40\3\73\1\u009a\25\73\2\40\1\uffff\1\u00b4"+
		"\1\u00b5\2\40\1\uffff\12\40\3\73\1\uffff\10\73\1\u00cf\16\73\1\u00de\1"+
		"\u00df\3\uffff\15\40\3\73\1\u00f0\7\73\1\uffff\11\73\1\u0103\1\u0104\1"+
		"\73\1\u0106\1\73\2\uffff\15\40\1\uffff\2\73\1\uffff\1\u0116\1\u0117\20"+
		"\73\2\uffff\1\u012a\1\uffff\1\u012b\14\40\1\u0136\1\73\2\uffff\16\73\1"+
		"\u0148\2\73\1\u014b\2\uffff\10\40\3\uffff\1\u0151\1\73\1\u0153\1\73\1"+
		"\u0155\14\73\1\uffff\2\73\1\uffff\5\40\1\uffff\1\73\1\uffff\1\73\1\uffff"+
		"\1\73\1\u016c\1\u016e\5\73\1\u0174\7\73\3\40\1\u017d\1\73\1\u017f\1\uffff"+
		"\1\73\1\uffff\1\73\1\u0183\3\73\1\uffff\2\73\1\u0189\4\73\1\40\1\uffff"+
		"\1\73\1\uffff\1\73\1\u0191\1\73\1\uffff\5\73\1\uffff\4\73\1\uffff\2\73"+
		"\1\uffff\1\73\1\u01a0\2\73\1\u01a3\1\u01a4\10\73\1\uffff\1\73\1\u01af"+
		"\2\uffff\1\u01b0\1\u01b1\2\73\1\u01b4\3\73\1\u01b8\1\73\3\uffff\1\u01ba"+
		"\1\73\1\uffff\3\73\1\uffff\1\73\1\uffff\1\73\1\u01c1\4\73\1\uffff\1\u01c6"+
		"\3\73\1\uffff\2\73\1\u01cc\1\u01cd\1\73\2\uffff\1\u01cf\1\uffff";
	static final String DFA205_eofS =
		"\u01d0\uffff";
	static final String DFA205_minS =
		"\1\11\1\55\1\41\1\55\2\75\1\uffff\1\55\2\75\4\uffff\1\74\1\uffff\1\72"+
		"\1\52\4\uffff\1\56\1\55\1\11\1\110\3\uffff\1\116\1\117\1\116\1\uffff\2"+
		"\122\1\0\1\55\1\uffff\1\117\1\105\3\uffff\1\145\1\106\1\101\1\105\1\101"+
		"\1\110\3\117\2\105\1\115\1\101\1\105\1\101\41\uffff\1\105\1\114\1\55\1"+
		"\124\1\104\2\114\1\0\1\114\1\122\1\60\1\122\1\65\2\uffff\1\115\1\107\1"+
		"\163\1\120\1\103\1\55\1\107\1\104\1\130\1\115\1\101\3\116\1\120\1\124"+
		"\1\106\1\107\1\124\1\117\1\105\1\124\1\123\1\103\1\102\1\122\1\111\1\116"+
		"\1\131\1\uffff\2\55\2\50\1\0\1\114\1\60\1\114\1\62\1\50\1\60\1\65\1\122"+
		"\1\101\1\105\1\164\1\117\1\114\1\uffff\1\105\2\111\1\105\1\122\1\116\2"+
		"\124\1\55\1\103\1\55\2\124\1\110\1\125\1\132\1\102\2\105\1\110\1\125\1"+
		"\116\1\114\2\55\3\uffff\1\50\1\60\1\50\1\103\1\60\1\62\1\114\1\120\1\60"+
		"\1\65\1\122\1\111\1\130\1\56\1\122\1\125\1\55\1\101\1\116\2\123\1\124"+
		"\1\105\1\55\1\uffff\1\124\1\103\1\117\1\55\1\124\1\122\1\55\1\113\1\116"+
		"\2\55\1\107\1\55\1\105\2\uffff\1\60\1\103\2\50\1\60\1\62\1\114\1\122\2"+
		"\65\1\122\1\116\1\120\1\uffff\1\124\1\104\1\uffff\2\55\1\120\2\105\1\116"+
		"\1\106\1\111\2\105\1\111\1\115\1\102\1\55\1\116\1\104\1\111\1\104\2\uffff"+
		"\1\55\1\uffff\1\55\1\60\1\103\2\50\1\65\1\62\1\114\1\105\1\65\1\122\2"+
		"\50\1\55\1\105\2\uffff\1\101\1\124\1\122\1\124\1\101\1\117\1\106\1\116"+
		"\1\107\1\55\1\117\1\111\1\117\1\102\1\55\1\117\1\124\1\55\2\uffff\1\64"+
		"\1\103\2\50\1\62\1\114\1\106\1\122\3\uffff\1\55\1\103\3\55\1\103\1\116"+
		"\2\124\1\110\1\103\1\120\1\104\1\124\1\117\1\111\1\117\1\uffff\1\103\1"+
		"\55\1\uffff\1\103\2\50\1\114\1\111\1\uffff\1\105\1\uffff\1\123\1\uffff"+
		"\1\105\2\55\1\105\1\124\2\105\1\111\1\55\1\104\1\124\1\120\1\104\1\124"+
		"\1\125\1\113\2\50\1\130\1\55\1\124\1\55\1\uffff\1\103\1\uffff\1\122\1"+
		"\55\1\106\1\116\1\107\1\uffff\1\114\1\117\1\55\1\104\1\124\1\115\1\105"+
		"\1\50\1\uffff\1\131\1\uffff\1\117\1\55\1\103\1\uffff\2\124\1\110\1\105"+
		"\1\115\1\uffff\1\114\1\117\1\105\1\131\1\uffff\1\114\1\122\1\uffff\1\117"+
		"\1\55\1\105\1\124\2\55\1\105\1\115\1\116\1\106\1\105\1\116\1\122\1\103"+
		"\1\uffff\1\122\1\55\2\uffff\2\55\1\124\1\122\1\55\1\105\1\116\1\117\1"+
		"\55\1\103\3\uffff\1\55\1\101\1\uffff\1\122\1\105\1\122\1\uffff\1\117\1"+
		"\uffff\1\115\1\55\1\122\1\116\1\122\1\105\1\uffff\1\55\1\105\1\116\1\123"+
		"\1\uffff\1\122\1\105\2\55\1\122\2\uffff\1\55\1\uffff";
	static final String DFA205_maxS =
		"\2\uffff\1\75\1\uffff\2\75\1\uffff\1\uffff\2\75\4\uffff\1\76\1\uffff\1"+
		"\72\1\57\4\uffff\1\71\1\uffff\1\117\1\110\3\uffff\1\122\1\117\1\116\1"+
		"\uffff\2\162\2\uffff\1\uffff\1\117\1\105\3\uffff\1\145\1\116\1\101\1\111"+
		"\1\101\1\117\1\125\2\117\1\105\1\111\1\127\1\130\1\105\1\110\41\uffff"+
		"\1\105\1\114\1\uffff\1\124\1\104\2\154\1\uffff\1\154\1\162\1\67\1\162"+
		"\1\65\2\uffff\1\115\1\107\1\163\1\120\1\103\1\uffff\1\107\1\104\1\130"+
		"\1\115\1\101\1\125\1\122\1\116\1\120\1\124\1\106\1\107\1\124\1\117\1\105"+
		"\1\124\1\123\1\103\1\102\1\122\1\111\1\116\1\131\1\uffff\2\uffff\2\50"+
		"\1\uffff\1\154\1\67\1\154\1\62\1\55\1\67\1\65\1\162\1\101\1\105\1\164"+
		"\1\117\1\114\1\uffff\1\105\2\111\1\105\1\122\1\116\2\124\1\uffff\1\103"+
		"\1\55\2\124\1\110\1\125\1\132\1\102\2\105\1\110\1\125\1\116\1\114\2\uffff"+
		"\3\uffff\1\50\1\66\1\50\1\143\1\67\1\62\1\154\1\120\1\67\1\65\1\162\1"+
		"\111\1\130\1\56\1\122\1\125\1\uffff\1\101\1\116\2\123\1\124\1\105\1\55"+
		"\1\uffff\1\124\1\122\1\117\1\55\1\124\1\122\1\55\1\113\1\116\2\uffff\1"+
		"\107\1\uffff\1\105\2\uffff\1\66\1\143\2\50\1\67\1\62\1\154\1\122\1\67"+
		"\1\65\1\162\1\116\1\120\1\uffff\1\124\1\104\1\uffff\2\uffff\1\120\2\105"+
		"\1\116\1\106\1\111\2\105\1\111\1\115\1\124\1\55\1\116\1\104\1\111\1\104"+
		"\2\uffff\1\uffff\1\uffff\1\uffff\1\66\1\143\2\50\1\67\1\62\1\154\1\105"+
		"\1\65\1\162\2\50\1\uffff\1\105\2\uffff\1\101\1\124\1\122\1\124\1\101\1"+
		"\117\1\106\1\116\1\107\1\55\1\117\1\111\1\117\1\124\1\uffff\1\117\1\124"+
		"\1\uffff\2\uffff\1\66\1\143\2\50\1\62\1\154\1\106\1\162\3\uffff\1\uffff"+
		"\1\103\1\uffff\1\55\1\uffff\1\103\1\116\2\124\1\110\1\122\1\120\1\104"+
		"\1\124\1\117\1\111\1\117\1\uffff\1\103\1\55\1\uffff\1\143\2\50\1\154\1"+
		"\111\1\uffff\1\105\1\uffff\1\123\1\uffff\1\105\2\uffff\1\105\1\124\2\105"+
		"\1\111\1\uffff\1\104\1\124\1\120\1\104\1\124\1\125\1\113\2\50\1\130\1"+
		"\uffff\1\124\1\uffff\1\uffff\1\103\1\uffff\1\122\1\uffff\1\106\1\116\1"+
		"\107\1\uffff\1\114\1\117\1\uffff\1\104\1\124\1\115\1\105\1\50\1\uffff"+
		"\1\131\1\uffff\1\117\1\uffff\1\103\1\uffff\2\124\1\110\1\105\1\115\1\uffff"+
		"\1\114\1\117\1\105\1\131\1\uffff\1\114\1\122\1\uffff\1\117\1\uffff\1\105"+
		"\1\124\2\uffff\1\105\1\115\1\116\1\106\1\105\1\116\1\122\1\103\1\uffff"+
		"\1\122\1\uffff\2\uffff\2\uffff\1\124\1\122\1\uffff\1\105\1\116\1\117\1"+
		"\uffff\1\103\3\uffff\1\uffff\1\101\1\uffff\1\122\1\105\1\122\1\uffff\1"+
		"\117\1\uffff\1\115\1\uffff\1\122\1\116\1\122\1\105\1\uffff\1\uffff\1\105"+
		"\1\116\1\123\1\uffff\1\122\1\105\2\uffff\1\122\2\uffff\1\uffff\1\uffff";
	static final String DFA205_acceptS =
		"\6\uffff\1\6\3\uffff\1\12\1\13\1\14\1\15\1\uffff\1\17\2\uffff\1\24\1\26"+
		"\1\27\1\30\4\uffff\1\44\1\47\1\50\3\uffff\1\55\4\uffff\1\135\2\uffff\1"+
		"\142\1\143\1\1\17\uffff\1\127\1\130\1\2\1\42\1\40\1\3\1\23\1\4\1\32\1"+
		"\5\1\33\1\7\1\131\1\10\1\25\1\41\1\11\1\36\1\16\1\21\1\20\1\144\1\145"+
		"\1\22\1\45\1\31\1\34\1\134\1\37\1\132\1\133\1\35\1\60\15\uffff\1\56\1"+
		"\57\35\uffff\1\54\22\uffff\1\120\31\uffff\1\52\1\53\1\136\30\uffff\1\122"+
		"\16\uffff\1\43\1\51\15\uffff\1\46\2\uffff\1\62\22\uffff\1\121\1\125\1"+
		"\uffff\1\117\17\uffff\1\63\1\113\22\uffff\1\116\1\126\10\uffff\1\140\1"+
		"\141\1\61\21\uffff\1\124\2\uffff\1\115\5\uffff\1\114\1\uffff\1\65\1\uffff"+
		"\1\112\26\uffff\1\123\1\uffff\1\71\5\uffff\1\102\10\uffff\1\64\1\uffff"+
		"\1\67\3\uffff\1\73\5\uffff\1\105\4\uffff\1\137\2\uffff\1\72\16\uffff\1"+
		"\76\2\uffff\1\103\1\104\12\uffff\1\100\1\106\1\107\2\uffff\1\66\3\uffff"+
		"\1\77\1\uffff\1\110\6\uffff\1\70\4\uffff\1\74\5\uffff\1\111\1\75\1\uffff"+
		"\1\101";
	static final String DFA205_specialS =
		"\43\uffff\1\1\76\uffff\1\0\51\uffff\1\2\u0143\uffff}>";
	static final String[] DFA205_transitionS = {
			"\1\50\1\51\2\uffff\1\51\22\uffff\1\50\1\30\1\33\1\44\1\7\1\27\1\32\1"+
			"\33\1\23\1\24\1\10\1\22\1\25\1\3\1\26\1\21\12\45\1\20\1\17\1\2\1\16\1"+
			"\11\1\uffff\1\1\1\37\2\40\1\46\11\40\1\36\1\35\2\40\1\47\2\40\1\42\1"+
			"\40\1\31\3\40\1\14\1\43\1\15\1\6\1\40\1\34\24\40\1\41\5\40\1\12\1\5\1"+
			"\13\1\4\1\uffff\uff80\40",
			"\1\66\2\uffff\12\73\6\uffff\1\52\1\73\1\63\1\60\1\70\1\67\1\61\2\73"+
			"\1\54\2\73\1\64\1\56\1\57\1\73\1\55\1\73\1\65\1\73\1\62\2\73\1\71\3\73"+
			"\1\uffff\1\73\2\uffff\1\73\1\uffff\21\73\1\53\10\73\5\uffff\uff80\73",
			"\1\74\33\uffff\1\75",
			"\1\77\23\uffff\32\40\1\uffff\1\40\2\uffff\1\40\1\uffff\32\40\5\uffff"+
			"\uff80\40",
			"\1\101",
			"\1\103",
			"",
			"\1\106\2\uffff\12\106\3\uffff\1\105\3\uffff\32\106\1\uffff\1\106\2\uffff"+
			"\1\106\1\uffff\32\106\5\uffff\uff80\106",
			"\1\107",
			"\1\111",
			"",
			"",
			"",
			"",
			"\1\75\1\113\1\111",
			"",
			"\1\115",
			"\1\117\4\uffff\1\120",
			"",
			"",
			"",
			"",
			"\1\122\1\uffff\12\45",
			"\1\125\2\uffff\12\125\7\uffff\32\125\1\uffff\1\125\2\uffff\1\125\1\uffff"+
			"\32\125\5\uffff\uff80\125",
			"\1\132\26\uffff\1\132\16\uffff\1\132\15\uffff\1\126\6\uffff\1\127\4"+
			"\uffff\1\132\5\uffff\1\130",
			"\1\133",
			"",
			"",
			"",
			"\1\134\3\uffff\1\135",
			"\1\136",
			"\1\137",
			"",
			"\1\141\11\uffff\1\142\25\uffff\1\140",
			"\1\143\11\uffff\1\142\25\uffff\1\140",
			"\12\40\1\uffff\1\40\2\uffff\42\40\1\145\4\40\1\147\1\40\1\147\35\40"+
			"\1\146\37\40\1\144\uff8a\40",
			"\1\151\2\uffff\12\151\7\uffff\32\151\1\uffff\1\151\2\uffff\1\151\1\uffff"+
			"\32\151\5\uffff\uff80\151",
			"",
			"\1\152",
			"\1\153",
			"",
			"",
			"",
			"\1\154",
			"\1\157\6\uffff\1\155\1\156",
			"\1\160",
			"\1\161\3\uffff\1\162",
			"\1\163",
			"\1\164\6\uffff\1\165",
			"\1\166\5\uffff\1\167",
			"\1\170",
			"\1\171",
			"\1\172",
			"\1\174\3\uffff\1\173",
			"\1\175\11\uffff\1\176",
			"\1\u0081\12\uffff\1\u0080\13\uffff\1\177",
			"\1\u0082",
			"\1\u0083\6\uffff\1\u0084",
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
			"\1\u0085",
			"\1\u0086",
			"\1\40\2\uffff\12\40\7\uffff\32\40\1\uffff\1\40\2\uffff\1\40\1\uffff"+
			"\32\40\5\uffff\uff80\40",
			"\1\u0088",
			"\1\u0089",
			"\1\u008b\17\uffff\1\u008c\17\uffff\1\u008a",
			"\1\u008b\17\uffff\1\u008c\17\uffff\1\u008a",
			"\12\40\1\uffff\1\40\2\uffff\42\40\1\u008e\4\40\1\u0090\1\40\1\u0090"+
			"\32\40\1\u008f\37\40\1\u008d\uff8d\40",
			"\1\u0091\17\uffff\1\u008c\17\uffff\1\u008a",
			"\1\141\11\uffff\1\142\25\uffff\1\140",
			"\1\u0092\4\uffff\1\u0093\1\uffff\1\u0093",
			"\1\141\11\uffff\1\142\25\uffff\1\140",
			"\1\u0094",
			"",
			"",
			"\1\u0095",
			"\1\u0096",
			"\1\u0097",
			"\1\u0098",
			"\1\u0099",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u009b",
			"\1\u009c",
			"\1\u009d",
			"\1\u009e",
			"\1\u009f",
			"\1\u00a1\6\uffff\1\u00a0",
			"\1\u00a2\3\uffff\1\u00a3",
			"\1\u00a4",
			"\1\u00a5",
			"\1\u00a6",
			"\1\u00a7",
			"\1\u00a8",
			"\1\u00a9",
			"\1\u00aa",
			"\1\u00ab",
			"\1\u00ac",
			"\1\u00ad",
			"\1\u00ae",
			"\1\u00af",
			"\1\u00b0",
			"\1\u00b1",
			"\1\u00b2",
			"\1\u00b3",
			"",
			"\1\40\2\uffff\12\40\7\uffff\32\40\1\uffff\1\40\2\uffff\1\40\1\uffff"+
			"\32\40\5\uffff\uff80\40",
			"\1\40\2\uffff\12\40\7\uffff\32\40\1\uffff\1\40\2\uffff\1\40\1\uffff"+
			"\32\40\5\uffff\uff80\40",
			"\1\u00b6",
			"\1\u00b6",
			"\12\40\1\uffff\1\40\2\uffff\42\40\1\u00b8\3\40\1\u00ba\1\40\1\u00ba"+
			"\25\40\1\u00b9\37\40\1\u00b7\uff93\40",
			"\1\u008b\17\uffff\1\u008c\17\uffff\1\u008a",
			"\1\u00bb\4\uffff\1\u00bc\1\uffff\1\u00bc",
			"\1\u008b\17\uffff\1\u008c\17\uffff\1\u008a",
			"\1\u00bd",
			"\1\u00b6\4\uffff\1\u00be",
			"\1\u00bf\4\uffff\1\u00c0\1\uffff\1\u00c0",
			"\1\u00c1",
			"\1\141\11\uffff\1\142\25\uffff\1\140",
			"\1\u00c2",
			"\1\u00c3",
			"\1\u00c4",
			"\1\u00c5",
			"\1\u00c6",
			"",
			"\1\u00c7",
			"\1\u00c8",
			"\1\u00c9",
			"\1\u00ca",
			"\1\u00cb",
			"\1\u00cc",
			"\1\u00cd",
			"\1\u00ce",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u00d0",
			"\1\u00d1",
			"\1\u00d2",
			"\1\u00d3",
			"\1\u00d4",
			"\1\u00d5",
			"\1\u00d6",
			"\1\u00d7",
			"\1\u00d8",
			"\1\u00d9",
			"\1\u00da",
			"\1\u00db",
			"\1\u00dc",
			"\1\u00dd",
			"\1\40\2\uffff\12\40\7\uffff\32\40\1\uffff\1\40\2\uffff\1\40\1\uffff"+
			"\32\40\5\uffff\uff80\40",
			"\1\40\2\uffff\12\40\7\uffff\32\40\1\uffff\1\40\2\uffff\1\40\1\uffff"+
			"\32\40\5\uffff\uff80\40",
			"",
			"",
			"",
			"\1\u00b6",
			"\1\u00e0\3\uffff\1\u00e1\1\uffff\1\u00e1",
			"\1\u00b6",
			"\1\u00e3\37\uffff\1\u00e2",
			"\1\u00e4\4\uffff\1\u00e5\1\uffff\1\u00e5",
			"\1\u00e6",
			"\1\u008b\17\uffff\1\u008c\17\uffff\1\u008a",
			"\1\u00e7",
			"\1\u00e8\4\uffff\1\u00e9\1\uffff\1\u00e9",
			"\1\u00ea",
			"\1\141\11\uffff\1\142\25\uffff\1\140",
			"\1\u00eb",
			"\1\u00ec",
			"\1\u00ed",
			"\1\u00ee",
			"\1\u00ef",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u00f1",
			"\1\u00f2",
			"\1\u00f3",
			"\1\u00f4",
			"\1\u00f5",
			"\1\u00f6",
			"\1\u00f7",
			"",
			"\1\u00f8",
			"\1\u00fa\10\uffff\1\u00f9\5\uffff\1\u00fb",
			"\1\u00fc",
			"\1\u00fd",
			"\1\u00fe",
			"\1\u00ff",
			"\1\u0100",
			"\1\u0101",
			"\1\u0102",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0105",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0107",
			"",
			"",
			"\1\u0108\3\uffff\1\u0109\1\uffff\1\u0109",
			"\1\u010b\37\uffff\1\u010a",
			"\1\u00b6",
			"\1\u00b6",
			"\1\u010c\4\uffff\1\u010d\1\uffff\1\u010d",
			"\1\u010e",
			"\1\u008b\17\uffff\1\u008c\17\uffff\1\u008a",
			"\1\u010f",
			"\1\u0110\1\uffff\1\u0110",
			"\1\u0111",
			"\1\141\11\uffff\1\142\25\uffff\1\140",
			"\1\u0112",
			"\1\u0113",
			"",
			"\1\u0114",
			"\1\u0115",
			"",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0118",
			"\1\u0119",
			"\1\u011a",
			"\1\u011b",
			"\1\u011c",
			"\1\u011d",
			"\1\u011e",
			"\1\u011f",
			"\1\u0120",
			"\1\u0121",
			"\1\u0124\12\uffff\1\u0123\6\uffff\1\u0122",
			"\1\u0125",
			"\1\u0126",
			"\1\u0127",
			"\1\u0128",
			"\1\u0129",
			"",
			"",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u012c\3\uffff\1\u012d\1\uffff\1\u012d",
			"\1\u012f\37\uffff\1\u012e",
			"\1\u00b6",
			"\1\u00b6",
			"\1\u0130\1\uffff\1\u0130",
			"\1\u0131",
			"\1\u008b\17\uffff\1\u008c\17\uffff\1\u008a",
			"\1\u0132",
			"\1\u0133",
			"\1\141\11\uffff\1\142\25\uffff\1\140",
			"\1\u0134",
			"\1\u0135",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0137",
			"",
			"",
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
			"\1\u0143",
			"\1\u0144",
			"\1\u0147\12\uffff\1\u0146\6\uffff\1\u0145",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0149",
			"\1\u014a",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"",
			"",
			"\1\u014c\1\uffff\1\u014c",
			"\1\u014e\37\uffff\1\u014d",
			"\1\u00b6",
			"\1\u00b6",
			"\1\u014f",
			"\1\u008b\17\uffff\1\u008c\17\uffff\1\u008a",
			"\1\u0150",
			"\1\141\11\uffff\1\142\25\uffff\1\140",
			"",
			"",
			"",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0152",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0154",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
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
			"\1\u0167\37\uffff\1\u0166",
			"\1\u00b6",
			"\1\u00b6",
			"\1\u008b\17\uffff\1\u008c\17\uffff\1\u008a",
			"\1\u0168",
			"",
			"\1\u0169",
			"",
			"\1\u016a",
			"",
			"\1\u016b",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u016d\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u016f",
			"\1\u0170",
			"\1\u0171",
			"\1\u0172",
			"\1\u0173",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0175",
			"\1\u0176",
			"\1\u0177",
			"\1\u0178",
			"\1\u0179",
			"\1\u017a",
			"\1\u017b",
			"\1\u00b6",
			"\1\u00b6",
			"\1\u017c",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u017e",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"",
			"\1\u0180",
			"",
			"\1\u0181",
			"\1\u0182\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0184",
			"\1\u0185",
			"\1\u0186",
			"",
			"\1\u0187",
			"\1\u0188",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u018a",
			"\1\u018b",
			"\1\u018c",
			"\1\u018d",
			"\1\u018e",
			"",
			"\1\u018f",
			"",
			"\1\u0190",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u0192",
			"",
			"\1\u0193",
			"\1\u0194",
			"\1\u0195",
			"\1\u0196",
			"\1\u0197",
			"",
			"\1\u0198",
			"\1\u0199",
			"\1\u019a",
			"\1\u019b",
			"",
			"\1\u019c",
			"\1\u019d",
			"",
			"\1\u019e",
			"\1\u019f\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01a1",
			"\1\u01a2",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01a5",
			"\1\u01a6",
			"\1\u01a7",
			"\1\u01a8",
			"\1\u01a9",
			"\1\u01aa",
			"\1\u01ab",
			"\1\u01ac",
			"",
			"\1\u01ad",
			"\1\u01ae\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"",
			"",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01b2",
			"\1\u01b3",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01b5",
			"\1\u01b6",
			"\1\u01b7",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01b9",
			"",
			"",
			"",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01bb",
			"",
			"\1\u01bc",
			"\1\u01bd",
			"\1\u01be",
			"",
			"\1\u01bf",
			"",
			"\1\u01c0",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01c2",
			"\1\u01c3",
			"\1\u01c4",
			"\1\u01c5",
			"",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01c7",
			"\1\u01c8",
			"\1\u01c9",
			"",
			"\1\u01ca",
			"\1\u01cb",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
			"\1\u01ce",
			"",
			"",
			"\1\73\2\uffff\12\73\7\uffff\32\73\1\uffff\1\73\2\uffff\1\73\1\uffff"+
			"\32\73\5\uffff\uff80\73",
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
			return "1:1: Tokens : ( GEN | CDO | CDC | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS | GREATER | LBRACE | RBRACE | LBRACKET | RBRACKET | OPEQ | SEMI | COLON | DCOLON | SOLIDUS | MINUS | PLUS | STAR | LPAREN | RPAREN | COMMA | DOT | TILDE | PIPE | PERCENTAGE_SYMBOL | EXCLAMATION_MARK | CP_EQ | CP_NOT_EQ | LESS | GREATER_OR_EQ | LESS_OR_EQ | LESS_WHEN | LESS_AND | CP_DOTS | LESS_REST | STRING | LESS_JS_STRING | ONLY | NOT | AND | OR | IDENT | HASH_SYMBOL | HASH | IMPORTANT_SYM | IMPORT_SYM | PAGE_SYM | MEDIA_SYM | NAMESPACE_SYM | CHARSET_SYM | COUNTER_STYLE_SYM | FONT_FACE_SYM | TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM | MOZ_DOCUMENT_SYM | WEBKIT_KEYFRAMES_SYM | SASS_CONTENT | SASS_MIXIN | SASS_INCLUDE | SASS_EXTEND | SASS_DEBUG | SASS_WARN | SASS_IF | SASS_ELSE | SASS_FOR | SASS_FUNCTION | SASS_RETURN | SASS_EACH | SASS_WHILE | AT_SIGN | AT_IDENT | SASS_VAR | SASS_DEFAULT | SASS_OPTIONAL | SASS_EXTEND_ONLY_SELECTOR | NUMBER | URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP | WS | NL | COMMENT | LINE_COMMENT );";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			IntStream input = _input;
			int _s = s;
			switch ( s ) {
					case 0 : 
						int LA205_98 = input.LA(1);
						s = -1;
						if ( (LA205_98=='r') ) {s = 141;}
						else if ( (LA205_98=='0') ) {s = 142;}
						else if ( (LA205_98=='R') ) {s = 143;}
						else if ( ((LA205_98 >= '\u0000' && LA205_98 <= '\t')||LA205_98=='\u000B'||(LA205_98 >= '\u000E' && LA205_98 <= '/')||(LA205_98 >= '1' && LA205_98 <= '4')||LA205_98=='6'||(LA205_98 >= '8' && LA205_98 <= 'Q')||(LA205_98 >= 'S' && LA205_98 <= 'q')||(LA205_98 >= 's' && LA205_98 <= '\uFFFF')) ) {s = 32;}
						else if ( (LA205_98=='5'||LA205_98=='7') ) {s = 144;}
						if ( s>=0 ) return s;
						break;

					case 1 : 
						int LA205_35 = input.LA(1);
						s = -1;
						if ( (LA205_35=='u') ) {s = 100;}
						else if ( (LA205_35=='0') ) {s = 101;}
						else if ( (LA205_35=='U') ) {s = 102;}
						else if ( ((LA205_35 >= '\u0000' && LA205_35 <= '\t')||LA205_35=='\u000B'||(LA205_35 >= '\u000E' && LA205_35 <= '/')||(LA205_35 >= '1' && LA205_35 <= '4')||LA205_35=='6'||(LA205_35 >= '8' && LA205_35 <= 'T')||(LA205_35 >= 'V' && LA205_35 <= 't')||(LA205_35 >= 'v' && LA205_35 <= '\uFFFF')) ) {s = 32;}
						else if ( (LA205_35=='5'||LA205_35=='7') ) {s = 103;}
						if ( s>=0 ) return s;
						break;

					case 2 : 
						int LA205_140 = input.LA(1);
						s = -1;
						if ( (LA205_140=='l') ) {s = 183;}
						else if ( (LA205_140=='0') ) {s = 184;}
						else if ( (LA205_140=='L') ) {s = 185;}
						else if ( ((LA205_140 >= '\u0000' && LA205_140 <= '\t')||LA205_140=='\u000B'||(LA205_140 >= '\u000E' && LA205_140 <= '/')||(LA205_140 >= '1' && LA205_140 <= '3')||LA205_140=='5'||(LA205_140 >= '7' && LA205_140 <= 'K')||(LA205_140 >= 'M' && LA205_140 <= 'k')||(LA205_140 >= 'm' && LA205_140 <= '\uFFFF')) ) {s = 32;}
						else if ( (LA205_140=='4'||LA205_140=='6') ) {s = 186;}
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
