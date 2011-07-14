// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2011-07-14 12:51:36

    package org.netbeans.modules.css.lib;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
public class Css3Lexer extends Lexer {
    public static final int EOF=-1;
    public static final int T__81=81;
    public static final int CHARSET_SYM=4;
    public static final int STRING=5;
    public static final int SEMI=6;
    public static final int IMPORT_SYM=7;
    public static final int URI=8;
    public static final int COMMA=9;
    public static final int MEDIA_SYM=10;
    public static final int LBRACE=11;
    public static final int RBRACE=12;
    public static final int IDENT=13;
    public static final int PAGE_SYM=14;
    public static final int COLON=15;
    public static final int SOLIDUS=16;
    public static final int PLUS=17;
    public static final int GREATER=18;
    public static final int TILDE=19;
    public static final int MINUS=20;
    public static final int STAR=21;
    public static final int HASH=22;
    public static final int DOT=23;
    public static final int LBRACKET=24;
    public static final int OPEQ=25;
    public static final int INCLUDES=26;
    public static final int DASHMATCH=27;
    public static final int RBRACKET=28;
    public static final int LPAREN=29;
    public static final int RPAREN=30;
    public static final int IMPORTANT_SYM=31;
    public static final int NUMBER=32;
    public static final int PERCENTAGE=33;
    public static final int LENGTH=34;
    public static final int EMS=35;
    public static final int EXS=36;
    public static final int ANGLE=37;
    public static final int TIME=38;
    public static final int FREQ=39;
    public static final int HEXCHAR=40;
    public static final int NONASCII=41;
    public static final int UNICODE=42;
    public static final int ESCAPE=43;
    public static final int NMSTART=44;
    public static final int NMCHAR=45;
    public static final int NAME=46;
    public static final int URL=47;
    public static final int A=48;
    public static final int B=49;
    public static final int C=50;
    public static final int D=51;
    public static final int E=52;
    public static final int F=53;
    public static final int G=54;
    public static final int H=55;
    public static final int I=56;
    public static final int J=57;
    public static final int K=58;
    public static final int L=59;
    public static final int M=60;
    public static final int N=61;
    public static final int O=62;
    public static final int P=63;
    public static final int Q=64;
    public static final int R=65;
    public static final int S=66;
    public static final int T=67;
    public static final int U=68;
    public static final int V=69;
    public static final int W=70;
    public static final int X=71;
    public static final int Y=72;
    public static final int Z=73;
    public static final int COMMENT=74;
    public static final int CDO=75;
    public static final int CDC=76;
    public static final int INVALID=77;
    public static final int WS=78;
    public static final int DIMENSION=79;
    public static final int NL=80;

    // delegates
    // delegators

    public Css3Lexer() {;} 
    public Css3Lexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public Css3Lexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g"; }

    // $ANTLR start "T__81"
    public final void mT__81() throws RecognitionException {
        try {
            int _type = T__81;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:7:7: ( '|' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:7:9: '|'
            {
            match('|'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__81"

    // $ANTLR start "HEXCHAR"
    public final void mHEXCHAR() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:25: ( ( 'a' .. 'f' | 'A' .. 'F' | '0' .. '9' ) )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:27: ( 'a' .. 'f' | 'A' .. 'F' | '0' .. '9' )
            {
            if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='F')||(input.LA(1)>='a' && input.LA(1)<='f') ) {
                input.consume();
            state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "HEXCHAR"

    // $ANTLR start "NONASCII"
    public final void mNONASCII() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:457:25: ( '\\u0080' .. '\\uFFFF' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:457:27: '\\u0080' .. '\\uFFFF'
            {
            matchRange('\u0080','\uFFFF'); if (state.failed) return ;

            }

        }
        finally {
        }
    }
    // $ANTLR end "NONASCII"

    // $ANTLR start "UNICODE"
    public final void mUNICODE() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:25: ( '\\\\' HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )? ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:27: '\\\\' HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )? ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
            {
            match('\\'); if (state.failed) return ;
            mHEXCHAR(); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:33: ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( ((LA5_0>='0' && LA5_0<='9')||(LA5_0>='A' && LA5_0<='F')||(LA5_0>='a' && LA5_0<='f')) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:34: HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )?
                    {
                    mHEXCHAR(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:37: ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( ((LA4_0>='0' && LA4_0<='9')||(LA4_0>='A' && LA4_0<='F')||(LA4_0>='a' && LA4_0<='f')) ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:38: HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )?
                            {
                            mHEXCHAR(); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:41: ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )?
                            int alt3=2;
                            int LA3_0 = input.LA(1);

                            if ( ((LA3_0>='0' && LA3_0<='9')||(LA3_0>='A' && LA3_0<='F')||(LA3_0>='a' && LA3_0<='f')) ) {
                                alt3=1;
                            }
                            switch (alt3) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:42: HEXCHAR ( HEXCHAR ( HEXCHAR )? )?
                                    {
                                    mHEXCHAR(); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:463:45: ( HEXCHAR ( HEXCHAR )? )?
                                    int alt2=2;
                                    int LA2_0 = input.LA(1);

                                    if ( ((LA2_0>='0' && LA2_0<='9')||(LA2_0>='A' && LA2_0<='F')||(LA2_0>='a' && LA2_0<='f')) ) {
                                        alt2=1;
                                    }
                                    switch (alt2) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:463:46: HEXCHAR ( HEXCHAR )?
                                            {
                                            mHEXCHAR(); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:463:54: ( HEXCHAR )?
                                            int alt1=2;
                                            int LA1_0 = input.LA(1);

                                            if ( ((LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='F')||(LA1_0>='a' && LA1_0<='f')) ) {
                                                alt1=1;
                                            }
                                            switch (alt1) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:463:54: HEXCHAR
                                                    {
                                                    mHEXCHAR(); if (state.failed) return ;

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

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:467:33: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>='\t' && LA6_0<='\n')||(LA6_0>='\f' && LA6_0<='\r')||LA6_0==' ') ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
            	        input.consume();
            	    state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "UNICODE"

    // $ANTLR start "ESCAPE"
    public final void mESCAPE() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:25: ( UNICODE | '\\\\' ~ ( '\\r' | '\\n' | '\\f' | HEXCHAR ) )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='\\') ) {
                int LA7_1 = input.LA(2);

                if ( ((LA7_1>='\u0000' && LA7_1<='\t')||LA7_1=='\u000B'||(LA7_1>='\u000E' && LA7_1<='/')||(LA7_1>=':' && LA7_1<='@')||(LA7_1>='G' && LA7_1<='`')||(LA7_1>='g' && LA7_1<='\uFFFF')) ) {
                    alt7=2;
                }
                else if ( ((LA7_1>='0' && LA7_1<='9')||(LA7_1>='A' && LA7_1<='F')||(LA7_1>='a' && LA7_1<='f')) ) {
                    alt7=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 7, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:27: UNICODE
                    {
                    mUNICODE(); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:37: '\\\\' ~ ( '\\r' | '\\n' | '\\f' | HEXCHAR )
                    {
                    match('\\'); if (state.failed) return ;
                    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||input.LA(1)=='\u000B'||(input.LA(1)>='\u000E' && input.LA(1)<='/')||(input.LA(1)>=':' && input.LA(1)<='@')||(input.LA(1)>='G' && input.LA(1)<='`')||(input.LA(1)>='g' && input.LA(1)<='\uFFFF') ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "ESCAPE"

    // $ANTLR start "NMSTART"
    public final void mNMSTART() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:471:25: ( '_' | 'a' .. 'z' | 'A' .. 'Z' | NONASCII | ESCAPE )
            int alt8=5;
            int LA8_0 = input.LA(1);

            if ( (LA8_0=='_') ) {
                alt8=1;
            }
            else if ( ((LA8_0>='a' && LA8_0<='z')) ) {
                alt8=2;
            }
            else if ( ((LA8_0>='A' && LA8_0<='Z')) ) {
                alt8=3;
            }
            else if ( ((LA8_0>='\u0080' && LA8_0<='\uFFFF')) ) {
                alt8=4;
            }
            else if ( (LA8_0=='\\') ) {
                alt8=5;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:471:27: '_'
                    {
                    match('_'); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:472:27: 'a' .. 'z'
                    {
                    matchRange('a','z'); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:27: 'A' .. 'Z'
                    {
                    matchRange('A','Z'); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:27: NONASCII
                    {
                    mNONASCII(); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:27: ESCAPE
                    {
                    mESCAPE(); if (state.failed) return ;

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "NMSTART"

    // $ANTLR start "NMCHAR"
    public final void mNMCHAR() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:478:25: ( '_' | 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '-' | NONASCII | ESCAPE )
            int alt9=7;
            int LA9_0 = input.LA(1);

            if ( (LA9_0=='_') ) {
                alt9=1;
            }
            else if ( ((LA9_0>='a' && LA9_0<='z')) ) {
                alt9=2;
            }
            else if ( ((LA9_0>='A' && LA9_0<='Z')) ) {
                alt9=3;
            }
            else if ( ((LA9_0>='0' && LA9_0<='9')) ) {
                alt9=4;
            }
            else if ( (LA9_0=='-') ) {
                alt9=5;
            }
            else if ( ((LA9_0>='\u0080' && LA9_0<='\uFFFF')) ) {
                alt9=6;
            }
            else if ( (LA9_0=='\\') ) {
                alt9=7;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:478:27: '_'
                    {
                    match('_'); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:27: 'a' .. 'z'
                    {
                    matchRange('a','z'); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:27: 'A' .. 'Z'
                    {
                    matchRange('A','Z'); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:27: '0' .. '9'
                    {
                    matchRange('0','9'); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:27: '-'
                    {
                    match('-'); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:27: NONASCII
                    {
                    mNONASCII(); if (state.failed) return ;

                    }
                    break;
                case 7 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:484:27: ESCAPE
                    {
                    mESCAPE(); if (state.failed) return ;

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "NMCHAR"

    // $ANTLR start "NAME"
    public final void mNAME() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:25: ( ( NMCHAR )+ )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:27: ( NMCHAR )+
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:27: ( NMCHAR )+
            int cnt10=0;
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0=='-'||(LA10_0>='0' && LA10_0<='9')||(LA10_0>='A' && LA10_0<='Z')||LA10_0=='\\'||LA10_0=='_'||(LA10_0>='a' && LA10_0<='z')||(LA10_0>='\u0080' && LA10_0<='\uFFFF')) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:27: NMCHAR
            	    {
            	    mNMCHAR(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt10 >= 1 ) break loop10;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(10, input);
                        throw eee;
                }
                cnt10++;
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "NAME"

    // $ANTLR start "URL"
    public final void mURL() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:25: ( ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '-' | '~' | NONASCII | ESCAPE )* )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:27: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '-' | '~' | NONASCII | ESCAPE )*
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:27: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '-' | '~' | NONASCII | ESCAPE )*
            loop11:
            do {
                int alt11=12;
                alt11 = dfa11.predict(input);
                switch (alt11) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:31: '['
            	    {
            	    match('['); if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:35: '!'
            	    {
            	    match('!'); if (state.failed) return ;

            	    }
            	    break;
            	case 3 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:39: '#'
            	    {
            	    match('#'); if (state.failed) return ;

            	    }
            	    break;
            	case 4 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:43: '$'
            	    {
            	    match('$'); if (state.failed) return ;

            	    }
            	    break;
            	case 5 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:47: '%'
            	    {
            	    match('%'); if (state.failed) return ;

            	    }
            	    break;
            	case 6 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:51: '&'
            	    {
            	    match('&'); if (state.failed) return ;

            	    }
            	    break;
            	case 7 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:55: '*'
            	    {
            	    match('*'); if (state.failed) return ;

            	    }
            	    break;
            	case 8 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:59: '-'
            	    {
            	    match('-'); if (state.failed) return ;

            	    }
            	    break;
            	case 9 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:63: '~'
            	    {
            	    match('~'); if (state.failed) return ;

            	    }
            	    break;
            	case 10 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:31: NONASCII
            	    {
            	    mNONASCII(); if (state.failed) return ;

            	    }
            	    break;
            	case 11 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:31: ESCAPE
            	    {
            	    mESCAPE(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "URL"

    // $ANTLR start "A"
    public final void mA() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:17: ( ( 'a' | 'A' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '1' )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0=='A'||LA17_0=='a') ) {
                alt17=1;
            }
            else if ( (LA17_0=='\\') ) {
                alt17=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }
            switch (alt17) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:21: ( 'a' | 'A' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( ((LA12_0>='\t' && LA12_0<='\n')||(LA12_0>='\f' && LA12_0<='\r')||LA12_0==' ') ) {
                            alt12=1;
                        }


                        switch (alt12) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop12;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '1'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt16=2;
                    int LA16_0 = input.LA(1);

                    if ( (LA16_0=='0') ) {
                        alt16=1;
                    }
                    switch (alt16) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt15=2;
                            int LA15_0 = input.LA(1);

                            if ( (LA15_0=='0') ) {
                                alt15=1;
                            }
                            switch (alt15) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:36: ( '0' ( '0' )? )?
                                    int alt14=2;
                                    int LA14_0 = input.LA(1);

                                    if ( (LA14_0=='0') ) {
                                        alt14=1;
                                    }
                                    switch (alt14) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:41: ( '0' )?
                                            int alt13=2;
                                            int LA13_0 = input.LA(1);

                                            if ( (LA13_0=='0') ) {
                                                alt13=1;
                                            }
                                            switch (alt13) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:41: '0'
                                                    {
                                                    match('0'); if (state.failed) return ;

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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    match('1'); if (state.failed) return ;

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "A"

    // $ANTLR start "B"
    public final void mB() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:505:17: ( ( 'b' | 'B' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '2' )
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0=='B'||LA23_0=='b') ) {
                alt23=1;
            }
            else if ( (LA23_0=='\\') ) {
                alt23=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 23, 0, input);

                throw nvae;
            }
            switch (alt23) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:505:21: ( 'b' | 'B' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:505:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop18:
                    do {
                        int alt18=2;
                        int LA18_0 = input.LA(1);

                        if ( ((LA18_0>='\t' && LA18_0<='\n')||(LA18_0>='\f' && LA18_0<='\r')||LA18_0==' ') ) {
                            alt18=1;
                        }


                        switch (alt18) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop18;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:506:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '2'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:506:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt22=2;
                    int LA22_0 = input.LA(1);

                    if ( (LA22_0=='0') ) {
                        alt22=1;
                    }
                    switch (alt22) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:506:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:506:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt21=2;
                            int LA21_0 = input.LA(1);

                            if ( (LA21_0=='0') ) {
                                alt21=1;
                            }
                            switch (alt21) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:506:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:506:36: ( '0' ( '0' )? )?
                                    int alt20=2;
                                    int LA20_0 = input.LA(1);

                                    if ( (LA20_0=='0') ) {
                                        alt20=1;
                                    }
                                    switch (alt20) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:506:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:506:41: ( '0' )?
                                            int alt19=2;
                                            int LA19_0 = input.LA(1);

                                            if ( (LA19_0=='0') ) {
                                                alt19=1;
                                            }
                                            switch (alt19) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:506:41: '0'
                                                    {
                                                    match('0'); if (state.failed) return ;

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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    match('2'); if (state.failed) return ;

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "B"

    // $ANTLR start "C"
    public final void mC() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:508:17: ( ( 'c' | 'C' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '3' )
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0=='C'||LA29_0=='c') ) {
                alt29=1;
            }
            else if ( (LA29_0=='\\') ) {
                alt29=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 29, 0, input);

                throw nvae;
            }
            switch (alt29) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:508:21: ( 'c' | 'C' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:508:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop24:
                    do {
                        int alt24=2;
                        int LA24_0 = input.LA(1);

                        if ( ((LA24_0>='\t' && LA24_0<='\n')||(LA24_0>='\f' && LA24_0<='\r')||LA24_0==' ') ) {
                            alt24=1;
                        }


                        switch (alt24) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop24;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '3'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt28=2;
                    int LA28_0 = input.LA(1);

                    if ( (LA28_0=='0') ) {
                        alt28=1;
                    }
                    switch (alt28) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt27=2;
                            int LA27_0 = input.LA(1);

                            if ( (LA27_0=='0') ) {
                                alt27=1;
                            }
                            switch (alt27) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:36: ( '0' ( '0' )? )?
                                    int alt26=2;
                                    int LA26_0 = input.LA(1);

                                    if ( (LA26_0=='0') ) {
                                        alt26=1;
                                    }
                                    switch (alt26) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:41: ( '0' )?
                                            int alt25=2;
                                            int LA25_0 = input.LA(1);

                                            if ( (LA25_0=='0') ) {
                                                alt25=1;
                                            }
                                            switch (alt25) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:41: '0'
                                                    {
                                                    match('0'); if (state.failed) return ;

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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    match('3'); if (state.failed) return ;

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "C"

    // $ANTLR start "D"
    public final void mD() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:511:17: ( ( 'd' | 'D' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '4' )
            int alt35=2;
            int LA35_0 = input.LA(1);

            if ( (LA35_0=='D'||LA35_0=='d') ) {
                alt35=1;
            }
            else if ( (LA35_0=='\\') ) {
                alt35=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 35, 0, input);

                throw nvae;
            }
            switch (alt35) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:511:21: ( 'd' | 'D' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:511:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop30:
                    do {
                        int alt30=2;
                        int LA30_0 = input.LA(1);

                        if ( ((LA30_0>='\t' && LA30_0<='\n')||(LA30_0>='\f' && LA30_0<='\r')||LA30_0==' ') ) {
                            alt30=1;
                        }


                        switch (alt30) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop30;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:512:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '4'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:512:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt34=2;
                    int LA34_0 = input.LA(1);

                    if ( (LA34_0=='0') ) {
                        alt34=1;
                    }
                    switch (alt34) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:512:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:512:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt33=2;
                            int LA33_0 = input.LA(1);

                            if ( (LA33_0=='0') ) {
                                alt33=1;
                            }
                            switch (alt33) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:512:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:512:36: ( '0' ( '0' )? )?
                                    int alt32=2;
                                    int LA32_0 = input.LA(1);

                                    if ( (LA32_0=='0') ) {
                                        alt32=1;
                                    }
                                    switch (alt32) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:512:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:512:41: ( '0' )?
                                            int alt31=2;
                                            int LA31_0 = input.LA(1);

                                            if ( (LA31_0=='0') ) {
                                                alt31=1;
                                            }
                                            switch (alt31) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:512:41: '0'
                                                    {
                                                    match('0'); if (state.failed) return ;

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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    match('4'); if (state.failed) return ;

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "D"

    // $ANTLR start "E"
    public final void mE() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:17: ( ( 'e' | 'E' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '5' )
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0=='E'||LA41_0=='e') ) {
                alt41=1;
            }
            else if ( (LA41_0=='\\') ) {
                alt41=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 41, 0, input);

                throw nvae;
            }
            switch (alt41) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:21: ( 'e' | 'E' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop36:
                    do {
                        int alt36=2;
                        int LA36_0 = input.LA(1);

                        if ( ((LA36_0>='\t' && LA36_0<='\n')||(LA36_0>='\f' && LA36_0<='\r')||LA36_0==' ') ) {
                            alt36=1;
                        }


                        switch (alt36) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop36;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '5'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt40=2;
                    int LA40_0 = input.LA(1);

                    if ( (LA40_0=='0') ) {
                        alt40=1;
                    }
                    switch (alt40) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt39=2;
                            int LA39_0 = input.LA(1);

                            if ( (LA39_0=='0') ) {
                                alt39=1;
                            }
                            switch (alt39) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:36: ( '0' ( '0' )? )?
                                    int alt38=2;
                                    int LA38_0 = input.LA(1);

                                    if ( (LA38_0=='0') ) {
                                        alt38=1;
                                    }
                                    switch (alt38) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:41: ( '0' )?
                                            int alt37=2;
                                            int LA37_0 = input.LA(1);

                                            if ( (LA37_0=='0') ) {
                                                alt37=1;
                                            }
                                            switch (alt37) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:41: '0'
                                                    {
                                                    match('0'); if (state.failed) return ;

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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    match('5'); if (state.failed) return ;

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "E"

    // $ANTLR start "F"
    public final void mF() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:517:17: ( ( 'f' | 'F' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '6' )
            int alt47=2;
            int LA47_0 = input.LA(1);

            if ( (LA47_0=='F'||LA47_0=='f') ) {
                alt47=1;
            }
            else if ( (LA47_0=='\\') ) {
                alt47=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                throw nvae;
            }
            switch (alt47) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:517:21: ( 'f' | 'F' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:517:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop42:
                    do {
                        int alt42=2;
                        int LA42_0 = input.LA(1);

                        if ( ((LA42_0>='\t' && LA42_0<='\n')||(LA42_0>='\f' && LA42_0<='\r')||LA42_0==' ') ) {
                            alt42=1;
                        }


                        switch (alt42) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop42;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '6'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt46=2;
                    int LA46_0 = input.LA(1);

                    if ( (LA46_0=='0') ) {
                        alt46=1;
                    }
                    switch (alt46) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt45=2;
                            int LA45_0 = input.LA(1);

                            if ( (LA45_0=='0') ) {
                                alt45=1;
                            }
                            switch (alt45) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:36: ( '0' ( '0' )? )?
                                    int alt44=2;
                                    int LA44_0 = input.LA(1);

                                    if ( (LA44_0=='0') ) {
                                        alt44=1;
                                    }
                                    switch (alt44) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:41: ( '0' )?
                                            int alt43=2;
                                            int LA43_0 = input.LA(1);

                                            if ( (LA43_0=='0') ) {
                                                alt43=1;
                                            }
                                            switch (alt43) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:41: '0'
                                                    {
                                                    match('0'); if (state.failed) return ;

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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    match('6'); if (state.failed) return ;

                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "F"

    // $ANTLR start "G"
    public final void mG() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:17: ( ( 'g' | 'G' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' ) )
            int alt54=2;
            int LA54_0 = input.LA(1);

            if ( (LA54_0=='G'||LA54_0=='g') ) {
                alt54=1;
            }
            else if ( (LA54_0=='\\') ) {
                alt54=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 54, 0, input);

                throw nvae;
            }
            switch (alt54) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:21: ( 'g' | 'G' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop48:
                    do {
                        int alt48=2;
                        int LA48_0 = input.LA(1);

                        if ( ((LA48_0>='\t' && LA48_0<='\n')||(LA48_0>='\f' && LA48_0<='\r')||LA48_0==' ') ) {
                            alt48=1;
                        }


                        switch (alt48) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop48;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:21: '\\\\' ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:522:25: ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' )
                    int alt53=3;
                    switch ( input.LA(1) ) {
                    case 'g':
                        {
                        alt53=1;
                        }
                        break;
                    case 'G':
                        {
                        alt53=2;
                        }
                        break;
                    case '0':
                    case '4':
                    case '6':
                        {
                        alt53=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 53, 0, input);

                        throw nvae;
                    }

                    switch (alt53) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:523:31: 'g'
                            {
                            match('g'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:31: 'G'
                            {
                            match('G'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7'
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt52=2;
                            int LA52_0 = input.LA(1);

                            if ( (LA52_0=='0') ) {
                                alt52=1;
                            }
                            switch (alt52) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt51=2;
                                    int LA51_0 = input.LA(1);

                                    if ( (LA51_0=='0') ) {
                                        alt51=1;
                                    }
                                    switch (alt51) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:41: ( '0' ( '0' )? )?
                                            int alt50=2;
                                            int LA50_0 = input.LA(1);

                                            if ( (LA50_0=='0') ) {
                                                alt50=1;
                                            }
                                            switch (alt50) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:46: ( '0' )?
                                                    int alt49=2;
                                                    int LA49_0 = input.LA(1);

                                                    if ( (LA49_0=='0') ) {
                                                        alt49=1;
                                                    }
                                                    switch (alt49) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            match('7'); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "G"

    // $ANTLR start "H"
    public final void mH() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:528:17: ( ( 'h' | 'H' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' ) )
            int alt61=2;
            int LA61_0 = input.LA(1);

            if ( (LA61_0=='H'||LA61_0=='h') ) {
                alt61=1;
            }
            else if ( (LA61_0=='\\') ) {
                alt61=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 61, 0, input);

                throw nvae;
            }
            switch (alt61) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:528:21: ( 'h' | 'H' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:528:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop55:
                    do {
                        int alt55=2;
                        int LA55_0 = input.LA(1);

                        if ( ((LA55_0>='\t' && LA55_0<='\n')||(LA55_0>='\f' && LA55_0<='\r')||LA55_0==' ') ) {
                            alt55=1;
                        }


                        switch (alt55) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop55;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:529:19: '\\\\' ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:530:25: ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' )
                    int alt60=3;
                    switch ( input.LA(1) ) {
                    case 'h':
                        {
                        alt60=1;
                        }
                        break;
                    case 'H':
                        {
                        alt60=2;
                        }
                        break;
                    case '0':
                    case '4':
                    case '6':
                        {
                        alt60=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 60, 0, input);

                        throw nvae;
                    }

                    switch (alt60) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:531:31: 'h'
                            {
                            match('h'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:532:31: 'H'
                            {
                            match('H'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8'
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt59=2;
                            int LA59_0 = input.LA(1);

                            if ( (LA59_0=='0') ) {
                                alt59=1;
                            }
                            switch (alt59) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt58=2;
                                    int LA58_0 = input.LA(1);

                                    if ( (LA58_0=='0') ) {
                                        alt58=1;
                                    }
                                    switch (alt58) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:41: ( '0' ( '0' )? )?
                                            int alt57=2;
                                            int LA57_0 = input.LA(1);

                                            if ( (LA57_0=='0') ) {
                                                alt57=1;
                                            }
                                            switch (alt57) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:46: ( '0' )?
                                                    int alt56=2;
                                                    int LA56_0 = input.LA(1);

                                                    if ( (LA56_0=='0') ) {
                                                        alt56=1;
                                                    }
                                                    switch (alt56) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            match('8'); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "H"

    // $ANTLR start "I"
    public final void mI() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:536:17: ( ( 'i' | 'I' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' ) )
            int alt68=2;
            int LA68_0 = input.LA(1);

            if ( (LA68_0=='I'||LA68_0=='i') ) {
                alt68=1;
            }
            else if ( (LA68_0=='\\') ) {
                alt68=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 68, 0, input);

                throw nvae;
            }
            switch (alt68) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:536:21: ( 'i' | 'I' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:536:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop62:
                    do {
                        int alt62=2;
                        int LA62_0 = input.LA(1);

                        if ( ((LA62_0>='\t' && LA62_0<='\n')||(LA62_0>='\f' && LA62_0<='\r')||LA62_0==' ') ) {
                            alt62=1;
                        }


                        switch (alt62) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop62;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:537:19: '\\\\' ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:25: ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' )
                    int alt67=3;
                    switch ( input.LA(1) ) {
                    case 'i':
                        {
                        alt67=1;
                        }
                        break;
                    case 'I':
                        {
                        alt67=2;
                        }
                        break;
                    case '0':
                    case '4':
                    case '6':
                        {
                        alt67=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 67, 0, input);

                        throw nvae;
                    }

                    switch (alt67) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:31: 'i'
                            {
                            match('i'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:31: 'I'
                            {
                            match('I'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9'
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt66=2;
                            int LA66_0 = input.LA(1);

                            if ( (LA66_0=='0') ) {
                                alt66=1;
                            }
                            switch (alt66) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt65=2;
                                    int LA65_0 = input.LA(1);

                                    if ( (LA65_0=='0') ) {
                                        alt65=1;
                                    }
                                    switch (alt65) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:41: ( '0' ( '0' )? )?
                                            int alt64=2;
                                            int LA64_0 = input.LA(1);

                                            if ( (LA64_0=='0') ) {
                                                alt64=1;
                                            }
                                            switch (alt64) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:46: ( '0' )?
                                                    int alt63=2;
                                                    int LA63_0 = input.LA(1);

                                                    if ( (LA63_0=='0') ) {
                                                        alt63=1;
                                                    }
                                                    switch (alt63) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            match('9'); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "I"

    // $ANTLR start "J"
    public final void mJ() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:544:17: ( ( 'j' | 'J' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) ) )
            int alt75=2;
            int LA75_0 = input.LA(1);

            if ( (LA75_0=='J'||LA75_0=='j') ) {
                alt75=1;
            }
            else if ( (LA75_0=='\\') ) {
                alt75=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 75, 0, input);

                throw nvae;
            }
            switch (alt75) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:544:21: ( 'j' | 'J' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='J'||input.LA(1)=='j' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:544:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop69:
                    do {
                        int alt69=2;
                        int LA69_0 = input.LA(1);

                        if ( ((LA69_0>='\t' && LA69_0<='\n')||(LA69_0>='\f' && LA69_0<='\r')||LA69_0==' ') ) {
                            alt69=1;
                        }


                        switch (alt69) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop69;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:19: '\\\\' ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:546:25: ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) )
                    int alt74=3;
                    switch ( input.LA(1) ) {
                    case 'j':
                        {
                        alt74=1;
                        }
                        break;
                    case 'J':
                        {
                        alt74=2;
                        }
                        break;
                    case '0':
                    case '4':
                    case '6':
                        {
                        alt74=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 74, 0, input);

                        throw nvae;
                    }

                    switch (alt74) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:31: 'j'
                            {
                            match('j'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:31: 'J'
                            {
                            match('J'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:549:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:549:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt73=2;
                            int LA73_0 = input.LA(1);

                            if ( (LA73_0=='0') ) {
                                alt73=1;
                            }
                            switch (alt73) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:549:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:549:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt72=2;
                                    int LA72_0 = input.LA(1);

                                    if ( (LA72_0=='0') ) {
                                        alt72=1;
                                    }
                                    switch (alt72) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:549:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:549:41: ( '0' ( '0' )? )?
                                            int alt71=2;
                                            int LA71_0 = input.LA(1);

                                            if ( (LA71_0=='0') ) {
                                                alt71=1;
                                            }
                                            switch (alt71) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:549:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:549:46: ( '0' )?
                                                    int alt70=2;
                                                    int LA70_0 = input.LA(1);

                                                    if ( (LA70_0=='0') ) {
                                                        alt70=1;
                                                    }
                                                    switch (alt70) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:549:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "J"

    // $ANTLR start "K"
    public final void mK() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:552:17: ( ( 'k' | 'K' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) ) )
            int alt82=2;
            int LA82_0 = input.LA(1);

            if ( (LA82_0=='K'||LA82_0=='k') ) {
                alt82=1;
            }
            else if ( (LA82_0=='\\') ) {
                alt82=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 82, 0, input);

                throw nvae;
            }
            switch (alt82) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:552:21: ( 'k' | 'K' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='K'||input.LA(1)=='k' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:552:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop76:
                    do {
                        int alt76=2;
                        int LA76_0 = input.LA(1);

                        if ( ((LA76_0>='\t' && LA76_0<='\n')||(LA76_0>='\f' && LA76_0<='\r')||LA76_0==' ') ) {
                            alt76=1;
                        }


                        switch (alt76) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop76;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:553:19: '\\\\' ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:554:25: ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) )
                    int alt81=3;
                    switch ( input.LA(1) ) {
                    case 'k':
                        {
                        alt81=1;
                        }
                        break;
                    case 'K':
                        {
                        alt81=2;
                        }
                        break;
                    case '0':
                    case '4':
                    case '6':
                        {
                        alt81=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 81, 0, input);

                        throw nvae;
                    }

                    switch (alt81) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:31: 'k'
                            {
                            match('k'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:31: 'K'
                            {
                            match('K'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt80=2;
                            int LA80_0 = input.LA(1);

                            if ( (LA80_0=='0') ) {
                                alt80=1;
                            }
                            switch (alt80) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt79=2;
                                    int LA79_0 = input.LA(1);

                                    if ( (LA79_0=='0') ) {
                                        alt79=1;
                                    }
                                    switch (alt79) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:41: ( '0' ( '0' )? )?
                                            int alt78=2;
                                            int LA78_0 = input.LA(1);

                                            if ( (LA78_0=='0') ) {
                                                alt78=1;
                                            }
                                            switch (alt78) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:46: ( '0' )?
                                                    int alt77=2;
                                                    int LA77_0 = input.LA(1);

                                                    if ( (LA77_0=='0') ) {
                                                        alt77=1;
                                                    }
                                                    switch (alt77) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "K"

    // $ANTLR start "L"
    public final void mL() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:17: ( ( 'l' | 'L' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) ) )
            int alt89=2;
            int LA89_0 = input.LA(1);

            if ( (LA89_0=='L'||LA89_0=='l') ) {
                alt89=1;
            }
            else if ( (LA89_0=='\\') ) {
                alt89=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 89, 0, input);

                throw nvae;
            }
            switch (alt89) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:21: ( 'l' | 'L' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop83:
                    do {
                        int alt83=2;
                        int LA83_0 = input.LA(1);

                        if ( ((LA83_0>='\t' && LA83_0<='\n')||(LA83_0>='\f' && LA83_0<='\r')||LA83_0==' ') ) {
                            alt83=1;
                        }


                        switch (alt83) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop83;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:19: '\\\\' ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:25: ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) )
                    int alt88=3;
                    switch ( input.LA(1) ) {
                    case 'l':
                        {
                        alt88=1;
                        }
                        break;
                    case 'L':
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 88, 0, input);

                        throw nvae;
                    }

                    switch (alt88) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:31: 'l'
                            {
                            match('l'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:564:31: 'L'
                            {
                            match('L'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt87=2;
                            int LA87_0 = input.LA(1);

                            if ( (LA87_0=='0') ) {
                                alt87=1;
                            }
                            switch (alt87) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt86=2;
                                    int LA86_0 = input.LA(1);

                                    if ( (LA86_0=='0') ) {
                                        alt86=1;
                                    }
                                    switch (alt86) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:41: ( '0' ( '0' )? )?
                                            int alt85=2;
                                            int LA85_0 = input.LA(1);

                                            if ( (LA85_0=='0') ) {
                                                alt85=1;
                                            }
                                            switch (alt85) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:46: ( '0' )?
                                                    int alt84=2;
                                                    int LA84_0 = input.LA(1);

                                                    if ( (LA84_0=='0') ) {
                                                        alt84=1;
                                                    }
                                                    switch (alt84) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "L"

    // $ANTLR start "M"
    public final void mM() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:568:17: ( ( 'm' | 'M' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) ) )
            int alt96=2;
            int LA96_0 = input.LA(1);

            if ( (LA96_0=='M'||LA96_0=='m') ) {
                alt96=1;
            }
            else if ( (LA96_0=='\\') ) {
                alt96=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 96, 0, input);

                throw nvae;
            }
            switch (alt96) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:568:21: ( 'm' | 'M' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:568:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop90:
                    do {
                        int alt90=2;
                        int LA90_0 = input.LA(1);

                        if ( ((LA90_0>='\t' && LA90_0<='\n')||(LA90_0>='\f' && LA90_0<='\r')||LA90_0==' ') ) {
                            alt90=1;
                        }


                        switch (alt90) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop90;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:19: '\\\\' ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:570:25: ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) )
                    int alt95=3;
                    switch ( input.LA(1) ) {
                    case 'm':
                        {
                        alt95=1;
                        }
                        break;
                    case 'M':
                        {
                        alt95=2;
                        }
                        break;
                    case '0':
                    case '4':
                    case '6':
                        {
                        alt95=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 95, 0, input);

                        throw nvae;
                    }

                    switch (alt95) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:571:31: 'm'
                            {
                            match('m'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:572:31: 'M'
                            {
                            match('M'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:573:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:573:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt94=2;
                            int LA94_0 = input.LA(1);

                            if ( (LA94_0=='0') ) {
                                alt94=1;
                            }
                            switch (alt94) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:573:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:573:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt93=2;
                                    int LA93_0 = input.LA(1);

                                    if ( (LA93_0=='0') ) {
                                        alt93=1;
                                    }
                                    switch (alt93) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:573:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:573:41: ( '0' ( '0' )? )?
                                            int alt92=2;
                                            int LA92_0 = input.LA(1);

                                            if ( (LA92_0=='0') ) {
                                                alt92=1;
                                            }
                                            switch (alt92) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:573:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:573:46: ( '0' )?
                                                    int alt91=2;
                                                    int LA91_0 = input.LA(1);

                                                    if ( (LA91_0=='0') ) {
                                                        alt91=1;
                                                    }
                                                    switch (alt91) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:573:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "M"

    // $ANTLR start "N"
    public final void mN() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:576:17: ( ( 'n' | 'N' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) ) )
            int alt103=2;
            int LA103_0 = input.LA(1);

            if ( (LA103_0=='N'||LA103_0=='n') ) {
                alt103=1;
            }
            else if ( (LA103_0=='\\') ) {
                alt103=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 103, 0, input);

                throw nvae;
            }
            switch (alt103) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:576:21: ( 'n' | 'N' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:576:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop97:
                    do {
                        int alt97=2;
                        int LA97_0 = input.LA(1);

                        if ( ((LA97_0>='\t' && LA97_0<='\n')||(LA97_0>='\f' && LA97_0<='\r')||LA97_0==' ') ) {
                            alt97=1;
                        }


                        switch (alt97) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop97;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:577:19: '\\\\' ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:25: ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) )
                    int alt102=3;
                    switch ( input.LA(1) ) {
                    case 'n':
                        {
                        alt102=1;
                        }
                        break;
                    case 'N':
                        {
                        alt102=2;
                        }
                        break;
                    case '0':
                    case '4':
                    case '6':
                        {
                        alt102=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 102, 0, input);

                        throw nvae;
                    }

                    switch (alt102) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:31: 'n'
                            {
                            match('n'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:580:31: 'N'
                            {
                            match('N'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt101=2;
                            int LA101_0 = input.LA(1);

                            if ( (LA101_0=='0') ) {
                                alt101=1;
                            }
                            switch (alt101) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt100=2;
                                    int LA100_0 = input.LA(1);

                                    if ( (LA100_0=='0') ) {
                                        alt100=1;
                                    }
                                    switch (alt100) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:41: ( '0' ( '0' )? )?
                                            int alt99=2;
                                            int LA99_0 = input.LA(1);

                                            if ( (LA99_0=='0') ) {
                                                alt99=1;
                                            }
                                            switch (alt99) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:46: ( '0' )?
                                                    int alt98=2;
                                                    int LA98_0 = input.LA(1);

                                                    if ( (LA98_0=='0') ) {
                                                        alt98=1;
                                                    }
                                                    switch (alt98) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "N"

    // $ANTLR start "O"
    public final void mO() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:17: ( ( 'o' | 'O' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) ) )
            int alt110=2;
            int LA110_0 = input.LA(1);

            if ( (LA110_0=='O'||LA110_0=='o') ) {
                alt110=1;
            }
            else if ( (LA110_0=='\\') ) {
                alt110=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 110, 0, input);

                throw nvae;
            }
            switch (alt110) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:21: ( 'o' | 'O' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop104:
                    do {
                        int alt104=2;
                        int LA104_0 = input.LA(1);

                        if ( ((LA104_0>='\t' && LA104_0<='\n')||(LA104_0>='\f' && LA104_0<='\r')||LA104_0==' ') ) {
                            alt104=1;
                        }


                        switch (alt104) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop104;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:19: '\\\\' ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:25: ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) )
                    int alt109=3;
                    switch ( input.LA(1) ) {
                    case 'o':
                        {
                        alt109=1;
                        }
                        break;
                    case 'O':
                        {
                        alt109=2;
                        }
                        break;
                    case '0':
                    case '4':
                    case '6':
                        {
                        alt109=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 109, 0, input);

                        throw nvae;
                    }

                    switch (alt109) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:31: 'o'
                            {
                            match('o'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:31: 'O'
                            {
                            match('O'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt108=2;
                            int LA108_0 = input.LA(1);

                            if ( (LA108_0=='0') ) {
                                alt108=1;
                            }
                            switch (alt108) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt107=2;
                                    int LA107_0 = input.LA(1);

                                    if ( (LA107_0=='0') ) {
                                        alt107=1;
                                    }
                                    switch (alt107) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:41: ( '0' ( '0' )? )?
                                            int alt106=2;
                                            int LA106_0 = input.LA(1);

                                            if ( (LA106_0=='0') ) {
                                                alt106=1;
                                            }
                                            switch (alt106) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:46: ( '0' )?
                                                    int alt105=2;
                                                    int LA105_0 = input.LA(1);

                                                    if ( (LA105_0=='0') ) {
                                                        alt105=1;
                                                    }
                                                    switch (alt105) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "O"

    // $ANTLR start "P"
    public final void mP() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:17: ( ( 'p' | 'P' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) ) )
            int alt117=2;
            int LA117_0 = input.LA(1);

            if ( (LA117_0=='P'||LA117_0=='p') ) {
                alt117=1;
            }
            else if ( (LA117_0=='\\') ) {
                alt117=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 117, 0, input);

                throw nvae;
            }
            switch (alt117) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:21: ( 'p' | 'P' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop111:
                    do {
                        int alt111=2;
                        int LA111_0 = input.LA(1);

                        if ( ((LA111_0>='\t' && LA111_0<='\n')||(LA111_0>='\f' && LA111_0<='\r')||LA111_0==' ') ) {
                            alt111=1;
                        }


                        switch (alt111) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop111;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:19: '\\\\' ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:25: ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) )
                    int alt116=3;
                    switch ( input.LA(1) ) {
                    case 'p':
                        {
                        alt116=1;
                        }
                        break;
                    case 'P':
                        {
                        alt116=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt116=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 116, 0, input);

                        throw nvae;
                    }

                    switch (alt116) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:595:31: 'p'
                            {
                            match('p'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:31: 'P'
                            {
                            match('P'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt115=2;
                            int LA115_0 = input.LA(1);

                            if ( (LA115_0=='0') ) {
                                alt115=1;
                            }
                            switch (alt115) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt114=2;
                                    int LA114_0 = input.LA(1);

                                    if ( (LA114_0=='0') ) {
                                        alt114=1;
                                    }
                                    switch (alt114) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:41: ( '0' ( '0' )? )?
                                            int alt113=2;
                                            int LA113_0 = input.LA(1);

                                            if ( (LA113_0=='0') ) {
                                                alt113=1;
                                            }
                                            switch (alt113) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:46: ( '0' )?
                                                    int alt112=2;
                                                    int LA112_0 = input.LA(1);

                                                    if ( (LA112_0=='0') ) {
                                                        alt112=1;
                                                    }
                                                    switch (alt112) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:66: ( '0' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:67: '0'
                            {
                            match('0'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "P"

    // $ANTLR start "Q"
    public final void mQ() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:17: ( ( 'q' | 'Q' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) ) )
            int alt124=2;
            int LA124_0 = input.LA(1);

            if ( (LA124_0=='Q'||LA124_0=='q') ) {
                alt124=1;
            }
            else if ( (LA124_0=='\\') ) {
                alt124=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 124, 0, input);

                throw nvae;
            }
            switch (alt124) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:21: ( 'q' | 'Q' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='Q'||input.LA(1)=='q' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop118:
                    do {
                        int alt118=2;
                        int LA118_0 = input.LA(1);

                        if ( ((LA118_0>='\t' && LA118_0<='\n')||(LA118_0>='\f' && LA118_0<='\r')||LA118_0==' ') ) {
                            alt118=1;
                        }


                        switch (alt118) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop118;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:601:19: '\\\\' ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:602:25: ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) )
                    int alt123=3;
                    switch ( input.LA(1) ) {
                    case 'q':
                        {
                        alt123=1;
                        }
                        break;
                    case 'Q':
                        {
                        alt123=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt123=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 123, 0, input);

                        throw nvae;
                    }

                    switch (alt123) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:31: 'q'
                            {
                            match('q'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:604:31: 'Q'
                            {
                            match('Q'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt122=2;
                            int LA122_0 = input.LA(1);

                            if ( (LA122_0=='0') ) {
                                alt122=1;
                            }
                            switch (alt122) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt121=2;
                                    int LA121_0 = input.LA(1);

                                    if ( (LA121_0=='0') ) {
                                        alt121=1;
                                    }
                                    switch (alt121) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:41: ( '0' ( '0' )? )?
                                            int alt120=2;
                                            int LA120_0 = input.LA(1);

                                            if ( (LA120_0=='0') ) {
                                                alt120=1;
                                            }
                                            switch (alt120) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:46: ( '0' )?
                                                    int alt119=2;
                                                    int LA119_0 = input.LA(1);

                                                    if ( (LA119_0=='0') ) {
                                                        alt119=1;
                                                    }
                                                    switch (alt119) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:66: ( '1' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:67: '1'
                            {
                            match('1'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "Q"

    // $ANTLR start "R"
    public final void mR() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:17: ( ( 'r' | 'R' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) ) )
            int alt131=2;
            int LA131_0 = input.LA(1);

            if ( (LA131_0=='R'||LA131_0=='r') ) {
                alt131=1;
            }
            else if ( (LA131_0=='\\') ) {
                alt131=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 131, 0, input);

                throw nvae;
            }
            switch (alt131) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:21: ( 'r' | 'R' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop125:
                    do {
                        int alt125=2;
                        int LA125_0 = input.LA(1);

                        if ( ((LA125_0>='\t' && LA125_0<='\n')||(LA125_0>='\f' && LA125_0<='\r')||LA125_0==' ') ) {
                            alt125=1;
                        }


                        switch (alt125) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop125;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:19: '\\\\' ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:25: ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) )
                    int alt130=3;
                    switch ( input.LA(1) ) {
                    case 'r':
                        {
                        alt130=1;
                        }
                        break;
                    case 'R':
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 130, 0, input);

                        throw nvae;
                    }

                    switch (alt130) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:611:31: 'r'
                            {
                            match('r'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:612:31: 'R'
                            {
                            match('R'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt129=2;
                            int LA129_0 = input.LA(1);

                            if ( (LA129_0=='0') ) {
                                alt129=1;
                            }
                            switch (alt129) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt128=2;
                                    int LA128_0 = input.LA(1);

                                    if ( (LA128_0=='0') ) {
                                        alt128=1;
                                    }
                                    switch (alt128) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:41: ( '0' ( '0' )? )?
                                            int alt127=2;
                                            int LA127_0 = input.LA(1);

                                            if ( (LA127_0=='0') ) {
                                                alt127=1;
                                            }
                                            switch (alt127) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:46: ( '0' )?
                                                    int alt126=2;
                                                    int LA126_0 = input.LA(1);

                                                    if ( (LA126_0=='0') ) {
                                                        alt126=1;
                                                    }
                                                    switch (alt126) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:66: ( '2' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:67: '2'
                            {
                            match('2'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "R"

    // $ANTLR start "S"
    public final void mS() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:17: ( ( 's' | 'S' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) ) )
            int alt138=2;
            int LA138_0 = input.LA(1);

            if ( (LA138_0=='S'||LA138_0=='s') ) {
                alt138=1;
            }
            else if ( (LA138_0=='\\') ) {
                alt138=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 138, 0, input);

                throw nvae;
            }
            switch (alt138) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:21: ( 's' | 'S' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop132:
                    do {
                        int alt132=2;
                        int LA132_0 = input.LA(1);

                        if ( ((LA132_0>='\t' && LA132_0<='\n')||(LA132_0>='\f' && LA132_0<='\r')||LA132_0==' ') ) {
                            alt132=1;
                        }


                        switch (alt132) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop132;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:617:19: '\\\\' ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:25: ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) )
                    int alt137=3;
                    switch ( input.LA(1) ) {
                    case 's':
                        {
                        alt137=1;
                        }
                        break;
                    case 'S':
                        {
                        alt137=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt137=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 137, 0, input);

                        throw nvae;
                    }

                    switch (alt137) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:619:31: 's'
                            {
                            match('s'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:31: 'S'
                            {
                            match('S'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:621:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:621:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt136=2;
                            int LA136_0 = input.LA(1);

                            if ( (LA136_0=='0') ) {
                                alt136=1;
                            }
                            switch (alt136) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:621:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:621:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt135=2;
                                    int LA135_0 = input.LA(1);

                                    if ( (LA135_0=='0') ) {
                                        alt135=1;
                                    }
                                    switch (alt135) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:621:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:621:41: ( '0' ( '0' )? )?
                                            int alt134=2;
                                            int LA134_0 = input.LA(1);

                                            if ( (LA134_0=='0') ) {
                                                alt134=1;
                                            }
                                            switch (alt134) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:621:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:621:46: ( '0' )?
                                                    int alt133=2;
                                                    int LA133_0 = input.LA(1);

                                                    if ( (LA133_0=='0') ) {
                                                        alt133=1;
                                                    }
                                                    switch (alt133) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:621:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:621:66: ( '3' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:621:67: '3'
                            {
                            match('3'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "S"

    // $ANTLR start "T"
    public final void mT() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:17: ( ( 't' | 'T' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) ) )
            int alt145=2;
            int LA145_0 = input.LA(1);

            if ( (LA145_0=='T'||LA145_0=='t') ) {
                alt145=1;
            }
            else if ( (LA145_0=='\\') ) {
                alt145=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 145, 0, input);

                throw nvae;
            }
            switch (alt145) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:21: ( 't' | 'T' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop139:
                    do {
                        int alt139=2;
                        int LA139_0 = input.LA(1);

                        if ( ((LA139_0>='\t' && LA139_0<='\n')||(LA139_0>='\f' && LA139_0<='\r')||LA139_0==' ') ) {
                            alt139=1;
                        }


                        switch (alt139) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop139;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:625:19: '\\\\' ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:626:25: ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) )
                    int alt144=3;
                    switch ( input.LA(1) ) {
                    case 't':
                        {
                        alt144=1;
                        }
                        break;
                    case 'T':
                        {
                        alt144=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt144=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 144, 0, input);

                        throw nvae;
                    }

                    switch (alt144) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:627:31: 't'
                            {
                            match('t'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:628:31: 'T'
                            {
                            match('T'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt143=2;
                            int LA143_0 = input.LA(1);

                            if ( (LA143_0=='0') ) {
                                alt143=1;
                            }
                            switch (alt143) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt142=2;
                                    int LA142_0 = input.LA(1);

                                    if ( (LA142_0=='0') ) {
                                        alt142=1;
                                    }
                                    switch (alt142) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:41: ( '0' ( '0' )? )?
                                            int alt141=2;
                                            int LA141_0 = input.LA(1);

                                            if ( (LA141_0=='0') ) {
                                                alt141=1;
                                            }
                                            switch (alt141) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:46: ( '0' )?
                                                    int alt140=2;
                                                    int LA140_0 = input.LA(1);

                                                    if ( (LA140_0=='0') ) {
                                                        alt140=1;
                                                    }
                                                    switch (alt140) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:66: ( '4' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:67: '4'
                            {
                            match('4'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "T"

    // $ANTLR start "U"
    public final void mU() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:17: ( ( 'u' | 'U' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) ) )
            int alt152=2;
            int LA152_0 = input.LA(1);

            if ( (LA152_0=='U'||LA152_0=='u') ) {
                alt152=1;
            }
            else if ( (LA152_0=='\\') ) {
                alt152=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 152, 0, input);

                throw nvae;
            }
            switch (alt152) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:21: ( 'u' | 'U' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop146:
                    do {
                        int alt146=2;
                        int LA146_0 = input.LA(1);

                        if ( ((LA146_0>='\t' && LA146_0<='\n')||(LA146_0>='\f' && LA146_0<='\r')||LA146_0==' ') ) {
                            alt146=1;
                        }


                        switch (alt146) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop146;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:19: '\\\\' ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:25: ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) )
                    int alt151=3;
                    switch ( input.LA(1) ) {
                    case 'u':
                        {
                        alt151=1;
                        }
                        break;
                    case 'U':
                        {
                        alt151=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt151=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 151, 0, input);

                        throw nvae;
                    }

                    switch (alt151) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:635:31: 'u'
                            {
                            match('u'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:31: 'U'
                            {
                            match('U'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt150=2;
                            int LA150_0 = input.LA(1);

                            if ( (LA150_0=='0') ) {
                                alt150=1;
                            }
                            switch (alt150) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt149=2;
                                    int LA149_0 = input.LA(1);

                                    if ( (LA149_0=='0') ) {
                                        alt149=1;
                                    }
                                    switch (alt149) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:41: ( '0' ( '0' )? )?
                                            int alt148=2;
                                            int LA148_0 = input.LA(1);

                                            if ( (LA148_0=='0') ) {
                                                alt148=1;
                                            }
                                            switch (alt148) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:46: ( '0' )?
                                                    int alt147=2;
                                                    int LA147_0 = input.LA(1);

                                                    if ( (LA147_0=='0') ) {
                                                        alt147=1;
                                                    }
                                                    switch (alt147) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:66: ( '5' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:67: '5'
                            {
                            match('5'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "U"

    // $ANTLR start "V"
    public final void mV() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:17: ( ( 'v' | 'V' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) ) )
            int alt159=2;
            int LA159_0 = input.LA(1);

            if ( (LA159_0=='V'||LA159_0=='v') ) {
                alt159=1;
            }
            else if ( (LA159_0=='\\') ) {
                alt159=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 159, 0, input);

                throw nvae;
            }
            switch (alt159) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:21: ( 'v' | 'V' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop153:
                    do {
                        int alt153=2;
                        int LA153_0 = input.LA(1);

                        if ( ((LA153_0>='\t' && LA153_0<='\n')||(LA153_0>='\f' && LA153_0<='\r')||LA153_0==' ') ) {
                            alt153=1;
                        }


                        switch (alt153) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop153;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:641:19: '\\\\' ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:642:25: ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) )
                    int alt158=3;
                    switch ( input.LA(1) ) {
                    case 'v':
                        {
                        alt158=1;
                        }
                        break;
                    case 'V':
                        {
                        alt158=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt158=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 158, 0, input);

                        throw nvae;
                    }

                    switch (alt158) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:642:31: 'v'
                            {
                            match('v'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:31: 'V'
                            {
                            match('V'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt157=2;
                            int LA157_0 = input.LA(1);

                            if ( (LA157_0=='0') ) {
                                alt157=1;
                            }
                            switch (alt157) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt156=2;
                                    int LA156_0 = input.LA(1);

                                    if ( (LA156_0=='0') ) {
                                        alt156=1;
                                    }
                                    switch (alt156) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:41: ( '0' ( '0' )? )?
                                            int alt155=2;
                                            int LA155_0 = input.LA(1);

                                            if ( (LA155_0=='0') ) {
                                                alt155=1;
                                            }
                                            switch (alt155) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:46: ( '0' )?
                                                    int alt154=2;
                                                    int LA154_0 = input.LA(1);

                                                    if ( (LA154_0=='0') ) {
                                                        alt154=1;
                                                    }
                                                    switch (alt154) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:66: ( '6' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:67: '6'
                            {
                            match('6'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "V"

    // $ANTLR start "W"
    public final void mW() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:17: ( ( 'w' | 'W' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) ) )
            int alt166=2;
            int LA166_0 = input.LA(1);

            if ( (LA166_0=='W'||LA166_0=='w') ) {
                alt166=1;
            }
            else if ( (LA166_0=='\\') ) {
                alt166=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 166, 0, input);

                throw nvae;
            }
            switch (alt166) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:21: ( 'w' | 'W' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='W'||input.LA(1)=='w' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop160:
                    do {
                        int alt160=2;
                        int LA160_0 = input.LA(1);

                        if ( ((LA160_0>='\t' && LA160_0<='\n')||(LA160_0>='\f' && LA160_0<='\r')||LA160_0==' ') ) {
                            alt160=1;
                        }


                        switch (alt160) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop160;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:19: '\\\\' ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:25: ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) )
                    int alt165=3;
                    switch ( input.LA(1) ) {
                    case 'w':
                        {
                        alt165=1;
                        }
                        break;
                    case 'W':
                        {
                        alt165=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt165=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 165, 0, input);

                        throw nvae;
                    }

                    switch (alt165) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:650:31: 'w'
                            {
                            match('w'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:651:31: 'W'
                            {
                            match('W'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt164=2;
                            int LA164_0 = input.LA(1);

                            if ( (LA164_0=='0') ) {
                                alt164=1;
                            }
                            switch (alt164) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt163=2;
                                    int LA163_0 = input.LA(1);

                                    if ( (LA163_0=='0') ) {
                                        alt163=1;
                                    }
                                    switch (alt163) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:41: ( '0' ( '0' )? )?
                                            int alt162=2;
                                            int LA162_0 = input.LA(1);

                                            if ( (LA162_0=='0') ) {
                                                alt162=1;
                                            }
                                            switch (alt162) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:46: ( '0' )?
                                                    int alt161=2;
                                                    int LA161_0 = input.LA(1);

                                                    if ( (LA161_0=='0') ) {
                                                        alt161=1;
                                                    }
                                                    switch (alt161) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:66: ( '7' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:67: '7'
                            {
                            match('7'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "W"

    // $ANTLR start "X"
    public final void mX() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:655:17: ( ( 'x' | 'X' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) ) )
            int alt173=2;
            int LA173_0 = input.LA(1);

            if ( (LA173_0=='X'||LA173_0=='x') ) {
                alt173=1;
            }
            else if ( (LA173_0=='\\') ) {
                alt173=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 173, 0, input);

                throw nvae;
            }
            switch (alt173) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:655:21: ( 'x' | 'X' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:655:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop167:
                    do {
                        int alt167=2;
                        int LA167_0 = input.LA(1);

                        if ( ((LA167_0>='\t' && LA167_0<='\n')||(LA167_0>='\f' && LA167_0<='\r')||LA167_0==' ') ) {
                            alt167=1;
                        }


                        switch (alt167) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop167;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:19: '\\\\' ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:657:25: ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) )
                    int alt172=3;
                    switch ( input.LA(1) ) {
                    case 'x':
                        {
                        alt172=1;
                        }
                        break;
                    case 'X':
                        {
                        alt172=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt172=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 172, 0, input);

                        throw nvae;
                    }

                    switch (alt172) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:658:31: 'x'
                            {
                            match('x'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:659:31: 'X'
                            {
                            match('X'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt171=2;
                            int LA171_0 = input.LA(1);

                            if ( (LA171_0=='0') ) {
                                alt171=1;
                            }
                            switch (alt171) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt170=2;
                                    int LA170_0 = input.LA(1);

                                    if ( (LA170_0=='0') ) {
                                        alt170=1;
                                    }
                                    switch (alt170) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:41: ( '0' ( '0' )? )?
                                            int alt169=2;
                                            int LA169_0 = input.LA(1);

                                            if ( (LA169_0=='0') ) {
                                                alt169=1;
                                            }
                                            switch (alt169) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:46: ( '0' )?
                                                    int alt168=2;
                                                    int LA168_0 = input.LA(1);

                                                    if ( (LA168_0=='0') ) {
                                                        alt168=1;
                                                    }
                                                    switch (alt168) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:66: ( '8' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:67: '8'
                            {
                            match('8'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "X"

    // $ANTLR start "Y"
    public final void mY() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:17: ( ( 'y' | 'Y' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) ) )
            int alt180=2;
            int LA180_0 = input.LA(1);

            if ( (LA180_0=='Y'||LA180_0=='y') ) {
                alt180=1;
            }
            else if ( (LA180_0=='\\') ) {
                alt180=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 180, 0, input);

                throw nvae;
            }
            switch (alt180) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:21: ( 'y' | 'Y' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='Y'||input.LA(1)=='y' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop174:
                    do {
                        int alt174=2;
                        int LA174_0 = input.LA(1);

                        if ( ((LA174_0>='\t' && LA174_0<='\n')||(LA174_0>='\f' && LA174_0<='\r')||LA174_0==' ') ) {
                            alt174=1;
                        }


                        switch (alt174) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop174;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:19: '\\\\' ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:665:25: ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) )
                    int alt179=3;
                    switch ( input.LA(1) ) {
                    case 'y':
                        {
                        alt179=1;
                        }
                        break;
                    case 'Y':
                        {
                        alt179=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt179=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 179, 0, input);

                        throw nvae;
                    }

                    switch (alt179) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:666:31: 'y'
                            {
                            match('y'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:667:31: 'Y'
                            {
                            match('Y'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt178=2;
                            int LA178_0 = input.LA(1);

                            if ( (LA178_0=='0') ) {
                                alt178=1;
                            }
                            switch (alt178) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt177=2;
                                    int LA177_0 = input.LA(1);

                                    if ( (LA177_0=='0') ) {
                                        alt177=1;
                                    }
                                    switch (alt177) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:41: ( '0' ( '0' )? )?
                                            int alt176=2;
                                            int LA176_0 = input.LA(1);

                                            if ( (LA176_0=='0') ) {
                                                alt176=1;
                                            }
                                            switch (alt176) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:46: ( '0' )?
                                                    int alt175=2;
                                                    int LA175_0 = input.LA(1);

                                                    if ( (LA175_0=='0') ) {
                                                        alt175=1;
                                                    }
                                                    switch (alt175) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:66: ( '9' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:67: '9'
                            {
                            match('9'); if (state.failed) return ;

                            }


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "Y"

    // $ANTLR start "Z"
    public final void mZ() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:671:17: ( ( 'z' | 'Z' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* | '\\\\' ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) ) )
            int alt187=2;
            int LA187_0 = input.LA(1);

            if ( (LA187_0=='Z'||LA187_0=='z') ) {
                alt187=1;
            }
            else if ( (LA187_0=='\\') ) {
                alt187=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 187, 0, input);

                throw nvae;
            }
            switch (alt187) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:671:21: ( 'z' | 'Z' ) ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    {
                    if ( input.LA(1)=='Z'||input.LA(1)=='z' ) {
                        input.consume();
                    state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:671:31: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
                    loop181:
                    do {
                        int alt181=2;
                        int LA181_0 = input.LA(1);

                        if ( ((LA181_0>='\t' && LA181_0<='\n')||(LA181_0>='\f' && LA181_0<='\r')||LA181_0==' ') ) {
                            alt181=1;
                        }


                        switch (alt181) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    	    {
                    	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop181;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:672:19: '\\\\' ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:673:25: ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) )
                    int alt186=3;
                    switch ( input.LA(1) ) {
                    case 'z':
                        {
                        alt186=1;
                        }
                        break;
                    case 'Z':
                        {
                        alt186=2;
                        }
                        break;
                    case '0':
                    case '5':
                    case '7':
                        {
                        alt186=3;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 186, 0, input);

                        throw nvae;
                    }

                    switch (alt186) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:674:31: 'z'
                            {
                            match('z'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:675:31: 'Z'
                            {
                            match('Z'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt185=2;
                            int LA185_0 = input.LA(1);

                            if ( (LA185_0=='0') ) {
                                alt185=1;
                            }
                            switch (alt185) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt184=2;
                                    int LA184_0 = input.LA(1);

                                    if ( (LA184_0=='0') ) {
                                        alt184=1;
                                    }
                                    switch (alt184) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:41: ( '0' ( '0' )? )?
                                            int alt183=2;
                                            int LA183_0 = input.LA(1);

                                            if ( (LA183_0=='0') ) {
                                                alt183=1;
                                            }
                                            switch (alt183) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:46: ( '0' )?
                                                    int alt182=2;
                                                    int LA182_0 = input.LA(1);

                                                    if ( (LA182_0=='0') ) {
                                                        alt182=1;
                                                    }
                                                    switch (alt182) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:46: '0'
                                                            {
                                                            match('0'); if (state.failed) return ;

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
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}

                            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                                input.consume();
                            state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }


                    }
                    break;

            }
        }
        finally {
        }
    }
    // $ANTLR end "Z"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:687:17: ( '/*' ( options {greedy=false; } : ( . )* ) '*/' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:687:19: '/*' ( options {greedy=false; } : ( . )* ) '*/'
            {
            match("/*"); if (state.failed) return ;

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:687:24: ( options {greedy=false; } : ( . )* )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:687:54: ( . )*
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:687:54: ( . )*
            loop188:
            do {
                int alt188=2;
                int LA188_0 = input.LA(1);

                if ( (LA188_0=='*') ) {
                    int LA188_1 = input.LA(2);

                    if ( (LA188_1=='/') ) {
                        alt188=2;
                    }
                    else if ( ((LA188_1>='\u0000' && LA188_1<='.')||(LA188_1>='0' && LA188_1<='\uFFFF')) ) {
                        alt188=1;
                    }


                }
                else if ( ((LA188_0>='\u0000' && LA188_0<=')')||(LA188_0>='+' && LA188_0<='\uFFFF')) ) {
                    alt188=1;
                }


                switch (alt188) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:687:54: .
            	    {
            	    matchAny(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop188;
                }
            } while (true);


            }

            match("*/"); if (state.failed) return ;

            if ( state.backtracking==0 ) {

                                      _channel = 2;   // Comments on channel 2 in case we want to find them
                                  
            }

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMENT"

    // $ANTLR start "CDO"
    public final void mCDO() throws RecognitionException {
        try {
            int _type = CDO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:700:17: ( '<!--' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:700:19: '<!--'
            {
            match("<!--"); if (state.failed) return ;

            if ( state.backtracking==0 ) {

                                      _channel = 3;   // CDO on channel 3 in case we want it later
                                  
            }

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CDO"

    // $ANTLR start "CDC"
    public final void mCDC() throws RecognitionException {
        try {
            int _type = CDC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:713:17: ( '-->' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:713:19: '-->'
            {
            match("-->"); if (state.failed) return ;

            if ( state.backtracking==0 ) {

                                      _channel = 4;   // CDC on channel 4 in case we want it later
                                  
            }

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CDC"

    // $ANTLR start "INCLUDES"
    public final void mINCLUDES() throws RecognitionException {
        try {
            int _type = INCLUDES;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:720:17: ( '~=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:720:19: '~='
            {
            match("~="); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INCLUDES"

    // $ANTLR start "DASHMATCH"
    public final void mDASHMATCH() throws RecognitionException {
        try {
            int _type = DASHMATCH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:721:17: ( '|=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:721:19: '|='
            {
            match("|="); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DASHMATCH"

    // $ANTLR start "GREATER"
    public final void mGREATER() throws RecognitionException {
        try {
            int _type = GREATER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:17: ( '>' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:19: '>'
            {
            match('>'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATER"

    // $ANTLR start "LBRACE"
    public final void mLBRACE() throws RecognitionException {
        try {
            int _type = LBRACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:724:17: ( '{' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:724:19: '{'
            {
            match('{'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LBRACE"

    // $ANTLR start "RBRACE"
    public final void mRBRACE() throws RecognitionException {
        try {
            int _type = RBRACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:725:17: ( '}' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:725:19: '}'
            {
            match('}'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RBRACE"

    // $ANTLR start "LBRACKET"
    public final void mLBRACKET() throws RecognitionException {
        try {
            int _type = LBRACKET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:726:17: ( '[' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:726:19: '['
            {
            match('['); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LBRACKET"

    // $ANTLR start "RBRACKET"
    public final void mRBRACKET() throws RecognitionException {
        try {
            int _type = RBRACKET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:17: ( ']' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:19: ']'
            {
            match(']'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RBRACKET"

    // $ANTLR start "OPEQ"
    public final void mOPEQ() throws RecognitionException {
        try {
            int _type = OPEQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:728:17: ( '=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:728:19: '='
            {
            match('='); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OPEQ"

    // $ANTLR start "SEMI"
    public final void mSEMI() throws RecognitionException {
        try {
            int _type = SEMI;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:729:17: ( ';' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:729:19: ';'
            {
            match(';'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SEMI"

    // $ANTLR start "COLON"
    public final void mCOLON() throws RecognitionException {
        try {
            int _type = COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:730:17: ( ':' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:730:19: ':'
            {
            match(':'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COLON"

    // $ANTLR start "SOLIDUS"
    public final void mSOLIDUS() throws RecognitionException {
        try {
            int _type = SOLIDUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:17: ( '/' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:19: '/'
            {
            match('/'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SOLIDUS"

    // $ANTLR start "MINUS"
    public final void mMINUS() throws RecognitionException {
        try {
            int _type = MINUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:732:17: ( '-' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:732:19: '-'
            {
            match('-'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MINUS"

    // $ANTLR start "PLUS"
    public final void mPLUS() throws RecognitionException {
        try {
            int _type = PLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:733:17: ( '+' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:733:19: '+'
            {
            match('+'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PLUS"

    // $ANTLR start "STAR"
    public final void mSTAR() throws RecognitionException {
        try {
            int _type = STAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:734:17: ( '*' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:734:19: '*'
            {
            match('*'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STAR"

    // $ANTLR start "LPAREN"
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:17: ( '(' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:19: '('
            {
            match('('); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LPAREN"

    // $ANTLR start "RPAREN"
    public final void mRPAREN() throws RecognitionException {
        try {
            int _type = RPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:736:17: ( ')' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:736:19: ')'
            {
            match(')'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RPAREN"

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:737:17: ( ',' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:737:19: ','
            {
            match(','); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMA"

    // $ANTLR start "DOT"
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:738:17: ( '.' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:738:19: '.'
            {
            match('.'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOT"

    // $ANTLR start "TILDE"
    public final void mTILDE() throws RecognitionException {
        try {
            int _type = TILDE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:8: ( '~' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:10: '~'
            {
            match('~'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TILDE"

    // $ANTLR start "INVALID"
    public final void mINVALID() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:744:21: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:744:22: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "INVALID"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:745:17: ( '\\'' (~ ( '\\n' | '\\r' | '\\f' | '\\'' ) )* ( '\\'' | ) | '\"' (~ ( '\\n' | '\\r' | '\\f' | '\"' ) )* ( '\"' | ) )
            int alt193=2;
            int LA193_0 = input.LA(1);

            if ( (LA193_0=='\'') ) {
                alt193=1;
            }
            else if ( (LA193_0=='\"') ) {
                alt193=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 193, 0, input);

                throw nvae;
            }
            switch (alt193) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:745:19: '\\'' (~ ( '\\n' | '\\r' | '\\f' | '\\'' ) )* ( '\\'' | )
                    {
                    match('\''); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:745:24: (~ ( '\\n' | '\\r' | '\\f' | '\\'' ) )*
                    loop189:
                    do {
                        int alt189=2;
                        int LA189_0 = input.LA(1);

                        if ( ((LA189_0>='\u0000' && LA189_0<='\t')||LA189_0=='\u000B'||(LA189_0>='\u000E' && LA189_0<='&')||(LA189_0>='(' && LA189_0<='\uFFFF')) ) {
                            alt189=1;
                        }


                        switch (alt189) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:745:26: ~ ( '\\n' | '\\r' | '\\f' | '\\'' )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||input.LA(1)=='\u000B'||(input.LA(1)>='\u000E' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop189;
                        }
                    } while (true);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:746:21: ( '\\'' | )
                    int alt190=2;
                    int LA190_0 = input.LA(1);

                    if ( (LA190_0=='\'') ) {
                        alt190=1;
                    }
                    else {
                        alt190=2;}
                    switch (alt190) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:747:27: '\\''
                            {
                            match('\''); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:748:27: 
                            {
                            if ( state.backtracking==0 ) {
                               _type = INVALID; 
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:19: '\"' (~ ( '\\n' | '\\r' | '\\f' | '\"' ) )* ( '\"' | )
                    {
                    match('\"'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:23: (~ ( '\\n' | '\\r' | '\\f' | '\"' ) )*
                    loop191:
                    do {
                        int alt191=2;
                        int LA191_0 = input.LA(1);

                        if ( ((LA191_0>='\u0000' && LA191_0<='\t')||LA191_0=='\u000B'||(LA191_0>='\u000E' && LA191_0<='!')||(LA191_0>='#' && LA191_0<='\uFFFF')) ) {
                            alt191=1;
                        }


                        switch (alt191) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:25: ~ ( '\\n' | '\\r' | '\\f' | '\"' )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||input.LA(1)=='\u000B'||(input.LA(1)>='\u000E' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();
                    	    state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return ;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop191;
                        }
                    } while (true);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:21: ( '\"' | )
                    int alt192=2;
                    int LA192_0 = input.LA(1);

                    if ( (LA192_0=='\"') ) {
                        alt192=1;
                    }
                    else {
                        alt192=2;}
                    switch (alt192) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:753:27: '\"'
                            {
                            match('\"'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:754:27: 
                            {
                            if ( state.backtracking==0 ) {
                               _type = INVALID; 
                            }

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
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "IDENT"
    public final void mIDENT() throws RecognitionException {
        try {
            int _type = IDENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:761:17: ( ( '-' )? NMSTART ( NMCHAR )* )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:761:19: ( '-' )? NMSTART ( NMCHAR )*
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:761:19: ( '-' )?
            int alt194=2;
            int LA194_0 = input.LA(1);

            if ( (LA194_0=='-') ) {
                alt194=1;
            }
            switch (alt194) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:761:19: '-'
                    {
                    match('-'); if (state.failed) return ;

                    }
                    break;

            }

            mNMSTART(); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:761:32: ( NMCHAR )*
            loop195:
            do {
                int alt195=2;
                int LA195_0 = input.LA(1);

                if ( (LA195_0=='-'||(LA195_0>='0' && LA195_0<='9')||(LA195_0>='A' && LA195_0<='Z')||LA195_0=='\\'||LA195_0=='_'||(LA195_0>='a' && LA195_0<='z')||(LA195_0>='\u0080' && LA195_0<='\uFFFF')) ) {
                    alt195=1;
                }


                switch (alt195) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:761:32: NMCHAR
            	    {
            	    mNMCHAR(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop195;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IDENT"

    // $ANTLR start "HASH"
    public final void mHASH() throws RecognitionException {
        try {
            int _type = HASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:17: ( '#' NAME )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:19: '#' NAME
            {
            match('#'); if (state.failed) return ;
            mNAME(); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "HASH"

    // $ANTLR start "IMPORT_SYM"
    public final void mIMPORT_SYM() throws RecognitionException {
        try {
            int _type = IMPORT_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:768:17: ( '@' I M P O R T )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:768:19: '@' I M P O R T
            {
            match('@'); if (state.failed) return ;
            mI(); if (state.failed) return ;
            mM(); if (state.failed) return ;
            mP(); if (state.failed) return ;
            mO(); if (state.failed) return ;
            mR(); if (state.failed) return ;
            mT(); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IMPORT_SYM"

    // $ANTLR start "PAGE_SYM"
    public final void mPAGE_SYM() throws RecognitionException {
        try {
            int _type = PAGE_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:769:17: ( '@' P A G E )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:769:19: '@' P A G E
            {
            match('@'); if (state.failed) return ;
            mP(); if (state.failed) return ;
            mA(); if (state.failed) return ;
            mG(); if (state.failed) return ;
            mE(); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PAGE_SYM"

    // $ANTLR start "MEDIA_SYM"
    public final void mMEDIA_SYM() throws RecognitionException {
        try {
            int _type = MEDIA_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:770:17: ( '@' M E D I A )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:770:19: '@' M E D I A
            {
            match('@'); if (state.failed) return ;
            mM(); if (state.failed) return ;
            mE(); if (state.failed) return ;
            mD(); if (state.failed) return ;
            mI(); if (state.failed) return ;
            mA(); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MEDIA_SYM"

    // $ANTLR start "CHARSET_SYM"
    public final void mCHARSET_SYM() throws RecognitionException {
        try {
            int _type = CHARSET_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:771:17: ( '@charset ' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:771:19: '@charset '
            {
            match("@charset "); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CHARSET_SYM"

    // $ANTLR start "IMPORTANT_SYM"
    public final void mIMPORTANT_SYM() throws RecognitionException {
        try {
            int _type = IMPORTANT_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:17: ( '!' ( WS | COMMENT )* I M P O R T A N T )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:19: '!' ( WS | COMMENT )* I M P O R T A N T
            {
            match('!'); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:23: ( WS | COMMENT )*
            loop196:
            do {
                int alt196=3;
                int LA196_0 = input.LA(1);

                if ( (LA196_0=='\t'||LA196_0==' ') ) {
                    alt196=1;
                }
                else if ( (LA196_0=='/') ) {
                    alt196=2;
                }


                switch (alt196) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:24: WS
            	    {
            	    mWS(); if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:27: COMMENT
            	    {
            	    mCOMMENT(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop196;
                }
            } while (true);

            mI(); if (state.failed) return ;
            mM(); if (state.failed) return ;
            mP(); if (state.failed) return ;
            mO(); if (state.failed) return ;
            mR(); if (state.failed) return ;
            mT(); if (state.failed) return ;
            mA(); if (state.failed) return ;
            mN(); if (state.failed) return ;
            mT(); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IMPORTANT_SYM"

    // $ANTLR start "EMS"
    public final void mEMS() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:26: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "EMS"

    // $ANTLR start "EXS"
    public final void mEXS() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:786:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:786:26: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "EXS"

    // $ANTLR start "LENGTH"
    public final void mLENGTH() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:787:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:787:26: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "LENGTH"

    // $ANTLR start "ANGLE"
    public final void mANGLE() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:788:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:788:26: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "ANGLE"

    // $ANTLR start "TIME"
    public final void mTIME() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:789:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:789:26: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "TIME"

    // $ANTLR start "FREQ"
    public final void mFREQ() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:790:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:790:26: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "FREQ"

    // $ANTLR start "DIMENSION"
    public final void mDIMENSION() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:791:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:791:26: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "DIMENSION"

    // $ANTLR start "PERCENTAGE"
    public final void mPERCENTAGE() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:792:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:792:26: 
            {
            }

        }
        finally {
        }
    }
    // $ANTLR end "PERCENTAGE"

    // $ANTLR start "NUMBER"
    public final void mNUMBER() throws RecognitionException {
        try {
            int _type = NUMBER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:5: ( ( '0' .. '9' ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ ) ( ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R A D )=> R A D | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | '%' | ) )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:9: ( '0' .. '9' ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ ) ( ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R A D )=> R A D | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | '%' | )
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:9: ( '0' .. '9' ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ )
            int alt200=2;
            int LA200_0 = input.LA(1);

            if ( ((LA200_0>='0' && LA200_0<='9')) ) {
                alt200=1;
            }
            else if ( (LA200_0=='.') ) {
                alt200=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 200, 0, input);

                throw nvae;
            }
            switch (alt200) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:796:15: '0' .. '9' ( '.' ( '0' .. '9' )+ )?
                    {
                    matchRange('0','9'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:796:24: ( '.' ( '0' .. '9' )+ )?
                    int alt198=2;
                    int LA198_0 = input.LA(1);

                    if ( (LA198_0=='.') ) {
                        alt198=1;
                    }
                    switch (alt198) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:796:25: '.' ( '0' .. '9' )+
                            {
                            match('.'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:796:29: ( '0' .. '9' )+
                            int cnt197=0;
                            loop197:
                            do {
                                int alt197=2;
                                int LA197_0 = input.LA(1);

                                if ( ((LA197_0>='0' && LA197_0<='9')) ) {
                                    alt197=1;
                                }


                                switch (alt197) {
                            	case 1 :
                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:796:29: '0' .. '9'
                            	    {
                            	    matchRange('0','9'); if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    if ( cnt197 >= 1 ) break loop197;
                            	    if (state.backtracking>0) {state.failed=true; return ;}
                                        EarlyExitException eee =
                                            new EarlyExitException(197, input);
                                        throw eee;
                                }
                                cnt197++;
                            } while (true);


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:797:15: '.' ( '0' .. '9' )+
                    {
                    match('.'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:797:19: ( '0' .. '9' )+
                    int cnt199=0;
                    loop199:
                    do {
                        int alt199=2;
                        int LA199_0 = input.LA(1);

                        if ( ((LA199_0>='0' && LA199_0<='9')) ) {
                            alt199=1;
                        }


                        switch (alt199) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:797:19: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt199 >= 1 ) break loop199;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(199, input);
                                throw eee;
                        }
                        cnt199++;
                    } while (true);


                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:799:9: ( ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R A D )=> R A D | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | '%' | )
            int alt205=12;
            alt205 = dfa205.predict(input);
            switch (alt205) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:15: ( E ( M | X ) )=> E ( M | X )
                    {
                    mE(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:802:17: ( M | X )
                    int alt201=2;
                    switch ( input.LA(1) ) {
                    case 'M':
                    case 'm':
                        {
                        alt201=1;
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
                            alt201=1;
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
                                        int LA201_7 = input.LA(6);

                                        if ( (LA201_7=='4'||LA201_7=='6') ) {
                                            alt201=1;
                                        }
                                        else if ( (LA201_7=='5'||LA201_7=='7') ) {
                                            alt201=2;
                                        }
                                        else {
                                            if (state.backtracking>0) {state.failed=true; return ;}
                                            NoViableAltException nvae =
                                                new NoViableAltException("", 201, 7, input);

                                            throw nvae;
                                        }
                                        }
                                        break;
                                    case '4':
                                    case '6':
                                        {
                                        alt201=1;
                                        }
                                        break;
                                    case '5':
                                    case '7':
                                        {
                                        alt201=2;
                                        }
                                        break;
                                    default:
                                        if (state.backtracking>0) {state.failed=true; return ;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 201, 6, input);

                                        throw nvae;
                                    }

                                    }
                                    break;
                                case '4':
                                case '6':
                                    {
                                    alt201=1;
                                    }
                                    break;
                                case '5':
                                case '7':
                                    {
                                    alt201=2;
                                    }
                                    break;
                                default:
                                    if (state.backtracking>0) {state.failed=true; return ;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 201, 5, input);

                                    throw nvae;
                                }

                                }
                                break;
                            case '4':
                            case '6':
                                {
                                alt201=1;
                                }
                                break;
                            case '5':
                            case '7':
                                {
                                alt201=2;
                                }
                                break;
                            default:
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 201, 4, input);

                                throw nvae;
                            }

                            }
                            break;
                        case '5':
                        case '7':
                        case 'X':
                        case 'x':
                            {
                            alt201=2;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 201, 2, input);

                            throw nvae;
                        }

                        }
                        break;
                    case 'X':
                    case 'x':
                        {
                        alt201=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 201, 0, input);

                        throw nvae;
                    }

                    switch (alt201) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:803:23: M
                            {
                            mM(); if (state.failed) return ;
                            if ( state.backtracking==0 ) {
                               _type = EMS;          
                            }

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:23: X
                            {
                            mX(); if (state.failed) return ;
                            if ( state.backtracking==0 ) {
                               _type = EXS;          
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:806:15: ( P ( X | T | C ) )=> P ( X | T | C )
                    {
                    mP(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:17: ( X | T | C )
                    int alt202=3;
                    alt202 = dfa202.predict(input);
                    switch (alt202) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:809:23: X
                            {
                            mX(); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:810:23: T
                            {
                            mT(); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:811:23: C
                            {
                            mC(); if (state.failed) return ;

                            }
                            break;

                    }

                    if ( state.backtracking==0 ) {
                       _type = LENGTH;       
                    }

                    }
                    break;
                case 3 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:814:15: ( C M )=> C M
                    {
                    mC(); if (state.failed) return ;
                    mM(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = LENGTH;       
                    }

                    }
                    break;
                case 4 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:816:15: ( M ( M | S ) )=> M ( M | S )
                    {
                    mM(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:818:17: ( M | S )
                    int alt203=2;
                    switch ( input.LA(1) ) {
                    case 'M':
                    case 'm':
                        {
                        alt203=1;
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
                            alt203=1;
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
                                        int LA203_7 = input.LA(6);

                                        if ( (LA203_7=='4'||LA203_7=='6') ) {
                                            alt203=1;
                                        }
                                        else if ( (LA203_7=='5'||LA203_7=='7') ) {
                                            alt203=2;
                                        }
                                        else {
                                            if (state.backtracking>0) {state.failed=true; return ;}
                                            NoViableAltException nvae =
                                                new NoViableAltException("", 203, 7, input);

                                            throw nvae;
                                        }
                                        }
                                        break;
                                    case '4':
                                    case '6':
                                        {
                                        alt203=1;
                                        }
                                        break;
                                    case '5':
                                    case '7':
                                        {
                                        alt203=2;
                                        }
                                        break;
                                    default:
                                        if (state.backtracking>0) {state.failed=true; return ;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 203, 6, input);

                                        throw nvae;
                                    }

                                    }
                                    break;
                                case '4':
                                case '6':
                                    {
                                    alt203=1;
                                    }
                                    break;
                                case '5':
                                case '7':
                                    {
                                    alt203=2;
                                    }
                                    break;
                                default:
                                    if (state.backtracking>0) {state.failed=true; return ;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 203, 5, input);

                                    throw nvae;
                                }

                                }
                                break;
                            case '4':
                            case '6':
                                {
                                alt203=1;
                                }
                                break;
                            case '5':
                            case '7':
                                {
                                alt203=2;
                                }
                                break;
                            default:
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 203, 4, input);

                                throw nvae;
                            }

                            }
                            break;
                        case '5':
                        case '7':
                        case 'S':
                        case 's':
                            {
                            alt203=2;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 203, 2, input);

                            throw nvae;
                        }

                        }
                        break;
                    case 'S':
                    case 's':
                        {
                        alt203=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 203, 0, input);

                        throw nvae;
                    }

                    switch (alt203) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:819:23: M
                            {
                            mM(); if (state.failed) return ;
                            if ( state.backtracking==0 ) {
                               _type = LENGTH;       
                            }

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:821:23: S
                            {
                            mS(); if (state.failed) return ;
                            if ( state.backtracking==0 ) {
                               _type = TIME;         
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 5 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:823:15: ( I N )=> I N
                    {
                    mI(); if (state.failed) return ;
                    mN(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = LENGTH;       
                    }

                    }
                    break;
                case 6 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:826:15: ( D E G )=> D E G
                    {
                    mD(); if (state.failed) return ;
                    mE(); if (state.failed) return ;
                    mG(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = ANGLE;        
                    }

                    }
                    break;
                case 7 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:15: ( R A D )=> R A D
                    {
                    mR(); if (state.failed) return ;
                    mA(); if (state.failed) return ;
                    mD(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = ANGLE;        
                    }

                    }
                    break;
                case 8 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:15: ( S )=> S
                    {
                    mS(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = TIME;         
                    }

                    }
                    break;
                case 9 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:833:15: ( ( K )? H Z )=> ( K )? H Z
                    {
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:834:17: ( K )?
                    int alt204=2;
                    int LA204_0 = input.LA(1);

                    if ( (LA204_0=='K'||LA204_0=='k') ) {
                        alt204=1;
                    }
                    else if ( (LA204_0=='\\') ) {
                        switch ( input.LA(2) ) {
                            case 'K':
                            case 'k':
                                {
                                alt204=1;
                                }
                                break;
                            case '0':
                                {
                                int LA204_4 = input.LA(3);

                                if ( (LA204_4=='0') ) {
                                    int LA204_6 = input.LA(4);

                                    if ( (LA204_6=='0') ) {
                                        int LA204_7 = input.LA(5);

                                        if ( (LA204_7=='0') ) {
                                            int LA204_8 = input.LA(6);

                                            if ( (LA204_8=='4'||LA204_8=='6') ) {
                                                int LA204_5 = input.LA(7);

                                                if ( (LA204_5=='B'||LA204_5=='b') ) {
                                                    alt204=1;
                                                }
                                            }
                                        }
                                        else if ( (LA204_7=='4'||LA204_7=='6') ) {
                                            int LA204_5 = input.LA(6);

                                            if ( (LA204_5=='B'||LA204_5=='b') ) {
                                                alt204=1;
                                            }
                                        }
                                    }
                                    else if ( (LA204_6=='4'||LA204_6=='6') ) {
                                        int LA204_5 = input.LA(5);

                                        if ( (LA204_5=='B'||LA204_5=='b') ) {
                                            alt204=1;
                                        }
                                    }
                                }
                                else if ( (LA204_4=='4'||LA204_4=='6') ) {
                                    int LA204_5 = input.LA(4);

                                    if ( (LA204_5=='B'||LA204_5=='b') ) {
                                        alt204=1;
                                    }
                                }
                                }
                                break;
                            case '4':
                            case '6':
                                {
                                int LA204_5 = input.LA(3);

                                if ( (LA204_5=='B'||LA204_5=='b') ) {
                                    alt204=1;
                                }
                                }
                                break;
                        }

                    }
                    switch (alt204) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:834:17: K
                            {
                            mK(); if (state.failed) return ;

                            }
                            break;

                    }

                    mH(); if (state.failed) return ;
                    mZ(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = FREQ;         
                    }

                    }
                    break;
                case 10 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:836:15: IDENT
                    {
                    mIDENT(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = DIMENSION;    
                    }

                    }
                    break;
                case 11 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:15: '%'
                    {
                    match('%'); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = PERCENTAGE;   
                    }

                    }
                    break;
                case 12 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:841:9: 
                    {
                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NUMBER"

    // $ANTLR start "URI"
    public final void mURI() throws RecognitionException {
        try {
            int _type = URI;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:847:5: ( U R L '(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:847:9: U R L '(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')'
            {
            mU(); if (state.failed) return ;
            mR(); if (state.failed) return ;
            mL(); if (state.failed) return ;
            match('('); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:849:13: ( ( WS )=> WS )?
            int alt206=2;
            int LA206_0 = input.LA(1);

            if ( (LA206_0=='\t'||LA206_0==' ') ) {
                int LA206_1 = input.LA(2);

                if ( (synpred10_Css3()) ) {
                    alt206=1;
                }
            }
            switch (alt206) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:849:14: ( WS )=> WS
                    {
                    mWS(); if (state.failed) return ;

                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:849:25: ( URL | STRING )
            int alt207=2;
            int LA207_0 = input.LA(1);

            if ( (LA207_0=='\t'||(LA207_0>=' ' && LA207_0<='!')||(LA207_0>='#' && LA207_0<='&')||(LA207_0>=')' && LA207_0<='*')||LA207_0=='-'||(LA207_0>='[' && LA207_0<='\\')||LA207_0=='~'||(LA207_0>='\u0080' && LA207_0<='\uFFFF')) ) {
                alt207=1;
            }
            else if ( (LA207_0=='\"'||LA207_0=='\'') ) {
                alt207=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 207, 0, input);

                throw nvae;
            }
            switch (alt207) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:849:26: URL
                    {
                    mURL(); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:849:30: STRING
                    {
                    mSTRING(); if (state.failed) return ;

                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:849:38: ( WS )?
            int alt208=2;
            int LA208_0 = input.LA(1);

            if ( (LA208_0=='\t'||LA208_0==' ') ) {
                alt208=1;
            }
            switch (alt208) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:849:38: WS
                    {
                    mWS(); if (state.failed) return ;

                    }
                    break;

            }

            match(')'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "URI"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:858:9: ( ( ' ' | '\\t' )+ )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:858:11: ( ' ' | '\\t' )+
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:858:11: ( ' ' | '\\t' )+
            int cnt209=0;
            loop209:
            do {
                int alt209=2;
                int LA209_0 = input.LA(1);

                if ( (LA209_0=='\t'||LA209_0==' ') ) {
                    alt209=1;
                }


                switch (alt209) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            	    {
            	    if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
            	        input.consume();
            	    state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt209 >= 1 ) break loop209;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(209, input);
                        throw eee;
                }
                cnt209++;
            } while (true);

            if ( state.backtracking==0 ) {
               _channel = HIDDEN;    
            }

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "NL"
    public final void mNL() throws RecognitionException {
        try {
            int _type = NL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:9: ( ( '\\r' ( '\\n' )? | '\\n' ) )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:11: ( '\\r' ( '\\n' )? | '\\n' )
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:11: ( '\\r' ( '\\n' )? | '\\n' )
            int alt211=2;
            int LA211_0 = input.LA(1);

            if ( (LA211_0=='\r') ) {
                alt211=1;
            }
            else if ( (LA211_0=='\n') ) {
                alt211=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 211, 0, input);

                throw nvae;
            }
            switch (alt211) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:12: '\\r' ( '\\n' )?
                    {
                    match('\r'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:17: ( '\\n' )?
                    int alt210=2;
                    int LA210_0 = input.LA(1);

                    if ( (LA210_0=='\n') ) {
                        alt210=1;
                    }
                    switch (alt210) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:17: '\\n'
                            {
                            match('\n'); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:859:25: '\\n'
                    {
                    match('\n'); if (state.failed) return ;

                    }
                    break;

            }

            if ( state.backtracking==0 ) {
               _channel = HIDDEN;    
            }

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NL"

    public void mTokens() throws RecognitionException {
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:8: ( T__81 | COMMENT | CDO | CDC | INCLUDES | DASHMATCH | GREATER | LBRACE | RBRACE | LBRACKET | RBRACKET | OPEQ | SEMI | COLON | SOLIDUS | MINUS | PLUS | STAR | LPAREN | RPAREN | COMMA | DOT | TILDE | STRING | IDENT | HASH | IMPORT_SYM | PAGE_SYM | MEDIA_SYM | CHARSET_SYM | IMPORTANT_SYM | NUMBER | URI | WS | NL )
        int alt212=35;
        alt212 = dfa212.predict(input);
        switch (alt212) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:10: T__81
                {
                mT__81(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:16: COMMENT
                {
                mCOMMENT(); if (state.failed) return ;

                }
                break;
            case 3 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:24: CDO
                {
                mCDO(); if (state.failed) return ;

                }
                break;
            case 4 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:28: CDC
                {
                mCDC(); if (state.failed) return ;

                }
                break;
            case 5 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:32: INCLUDES
                {
                mINCLUDES(); if (state.failed) return ;

                }
                break;
            case 6 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:41: DASHMATCH
                {
                mDASHMATCH(); if (state.failed) return ;

                }
                break;
            case 7 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:51: GREATER
                {
                mGREATER(); if (state.failed) return ;

                }
                break;
            case 8 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:59: LBRACE
                {
                mLBRACE(); if (state.failed) return ;

                }
                break;
            case 9 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:66: RBRACE
                {
                mRBRACE(); if (state.failed) return ;

                }
                break;
            case 10 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:73: LBRACKET
                {
                mLBRACKET(); if (state.failed) return ;

                }
                break;
            case 11 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:82: RBRACKET
                {
                mRBRACKET(); if (state.failed) return ;

                }
                break;
            case 12 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:91: OPEQ
                {
                mOPEQ(); if (state.failed) return ;

                }
                break;
            case 13 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:96: SEMI
                {
                mSEMI(); if (state.failed) return ;

                }
                break;
            case 14 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:101: COLON
                {
                mCOLON(); if (state.failed) return ;

                }
                break;
            case 15 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:107: SOLIDUS
                {
                mSOLIDUS(); if (state.failed) return ;

                }
                break;
            case 16 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:115: MINUS
                {
                mMINUS(); if (state.failed) return ;

                }
                break;
            case 17 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:121: PLUS
                {
                mPLUS(); if (state.failed) return ;

                }
                break;
            case 18 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:126: STAR
                {
                mSTAR(); if (state.failed) return ;

                }
                break;
            case 19 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:131: LPAREN
                {
                mLPAREN(); if (state.failed) return ;

                }
                break;
            case 20 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:138: RPAREN
                {
                mRPAREN(); if (state.failed) return ;

                }
                break;
            case 21 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:145: COMMA
                {
                mCOMMA(); if (state.failed) return ;

                }
                break;
            case 22 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:151: DOT
                {
                mDOT(); if (state.failed) return ;

                }
                break;
            case 23 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:155: TILDE
                {
                mTILDE(); if (state.failed) return ;

                }
                break;
            case 24 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:161: STRING
                {
                mSTRING(); if (state.failed) return ;

                }
                break;
            case 25 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:168: IDENT
                {
                mIDENT(); if (state.failed) return ;

                }
                break;
            case 26 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:174: HASH
                {
                mHASH(); if (state.failed) return ;

                }
                break;
            case 27 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:179: IMPORT_SYM
                {
                mIMPORT_SYM(); if (state.failed) return ;

                }
                break;
            case 28 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:190: PAGE_SYM
                {
                mPAGE_SYM(); if (state.failed) return ;

                }
                break;
            case 29 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:199: MEDIA_SYM
                {
                mMEDIA_SYM(); if (state.failed) return ;

                }
                break;
            case 30 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:209: CHARSET_SYM
                {
                mCHARSET_SYM(); if (state.failed) return ;

                }
                break;
            case 31 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:221: IMPORTANT_SYM
                {
                mIMPORTANT_SYM(); if (state.failed) return ;

                }
                break;
            case 32 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:235: NUMBER
                {
                mNUMBER(); if (state.failed) return ;

                }
                break;
            case 33 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:242: URI
                {
                mURI(); if (state.failed) return ;

                }
                break;
            case 34 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:246: WS
                {
                mWS(); if (state.failed) return ;

                }
                break;
            case 35 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:249: NL
                {
                mNL(); if (state.failed) return ;

                }
                break;

        }

    }

    // $ANTLR start synpred1_Css3
    public final void synpred1_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:15: ( E ( M | X ) )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:16: E ( M | X )
        {
        mE(); if (state.failed) return ;
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:18: ( M | X )
        int alt213=2;
        switch ( input.LA(1) ) {
        case 'M':
        case 'm':
            {
            alt213=1;
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
                alt213=1;
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
                            int LA213_7 = input.LA(6);

                            if ( (LA213_7=='4'||LA213_7=='6') ) {
                                alt213=1;
                            }
                            else if ( (LA213_7=='5'||LA213_7=='7') ) {
                                alt213=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 213, 7, input);

                                throw nvae;
                            }
                            }
                            break;
                        case '4':
                        case '6':
                            {
                            alt213=1;
                            }
                            break;
                        case '5':
                        case '7':
                            {
                            alt213=2;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 213, 6, input);

                            throw nvae;
                        }

                        }
                        break;
                    case '4':
                    case '6':
                        {
                        alt213=1;
                        }
                        break;
                    case '5':
                    case '7':
                        {
                        alt213=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 213, 5, input);

                        throw nvae;
                    }

                    }
                    break;
                case '4':
                case '6':
                    {
                    alt213=1;
                    }
                    break;
                case '5':
                case '7':
                    {
                    alt213=2;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 213, 4, input);

                    throw nvae;
                }

                }
                break;
            case '5':
            case '7':
            case 'X':
            case 'x':
                {
                alt213=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 213, 2, input);

                throw nvae;
            }

            }
            break;
        case 'X':
        case 'x':
            {
            alt213=2;
            }
            break;
        default:
            if (state.backtracking>0) {state.failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("", 213, 0, input);

            throw nvae;
        }

        switch (alt213) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:19: M
                {
                mM(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:21: X
                {
                mX(); if (state.failed) return ;

                }
                break;

        }


        }
    }
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:806:15: ( P ( X | T | C ) )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:806:16: P ( X | T | C )
        {
        mP(); if (state.failed) return ;
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:806:17: ( X | T | C )
        int alt214=3;
        alt214 = dfa214.predict(input);
        switch (alt214) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:806:18: X
                {
                mX(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:806:20: T
                {
                mT(); if (state.failed) return ;

                }
                break;
            case 3 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:806:22: C
                {
                mC(); if (state.failed) return ;

                }
                break;

        }


        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:814:15: ( C M )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:814:16: C M
        {
        mC(); if (state.failed) return ;
        mM(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_Css3

    // $ANTLR start synpred4_Css3
    public final void synpred4_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:816:15: ( M ( M | S ) )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:816:16: M ( M | S )
        {
        mM(); if (state.failed) return ;
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:816:18: ( M | S )
        int alt215=2;
        switch ( input.LA(1) ) {
        case 'M':
        case 'm':
            {
            alt215=1;
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
                alt215=1;
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
                            int LA215_7 = input.LA(6);

                            if ( (LA215_7=='4'||LA215_7=='6') ) {
                                alt215=1;
                            }
                            else if ( (LA215_7=='5'||LA215_7=='7') ) {
                                alt215=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 215, 7, input);

                                throw nvae;
                            }
                            }
                            break;
                        case '4':
                        case '6':
                            {
                            alt215=1;
                            }
                            break;
                        case '5':
                        case '7':
                            {
                            alt215=2;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 215, 6, input);

                            throw nvae;
                        }

                        }
                        break;
                    case '4':
                    case '6':
                        {
                        alt215=1;
                        }
                        break;
                    case '5':
                    case '7':
                        {
                        alt215=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 215, 5, input);

                        throw nvae;
                    }

                    }
                    break;
                case '4':
                case '6':
                    {
                    alt215=1;
                    }
                    break;
                case '5':
                case '7':
                    {
                    alt215=2;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 215, 4, input);

                    throw nvae;
                }

                }
                break;
            case '5':
            case '7':
            case 'S':
            case 's':
                {
                alt215=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 215, 2, input);

                throw nvae;
            }

            }
            break;
        case 'S':
        case 's':
            {
            alt215=2;
            }
            break;
        default:
            if (state.backtracking>0) {state.failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("", 215, 0, input);

            throw nvae;
        }

        switch (alt215) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:816:19: M
                {
                mM(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:816:21: S
                {
                mS(); if (state.failed) return ;

                }
                break;

        }


        }
    }
    // $ANTLR end synpred4_Css3

    // $ANTLR start synpred5_Css3
    public final void synpred5_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:823:15: ( I N )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:823:16: I N
        {
        mI(); if (state.failed) return ;
        mN(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_Css3

    // $ANTLR start synpred6_Css3
    public final void synpred6_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:826:15: ( D E G )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:826:16: D E G
        {
        mD(); if (state.failed) return ;
        mE(); if (state.failed) return ;
        mG(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Css3

    // $ANTLR start synpred7_Css3
    public final void synpred7_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:15: ( R A D )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:16: R A D
        {
        mR(); if (state.failed) return ;
        mA(); if (state.failed) return ;
        mD(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Css3

    // $ANTLR start synpred8_Css3
    public final void synpred8_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:15: ( S )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:16: S
        {
        mS(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_Css3

    // $ANTLR start synpred9_Css3
    public final void synpred9_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:833:15: ( ( K )? H Z )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:833:16: ( K )? H Z
        {
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:833:16: ( K )?
        int alt216=2;
        int LA216_0 = input.LA(1);

        if ( (LA216_0=='K'||LA216_0=='k') ) {
            alt216=1;
        }
        else if ( (LA216_0=='\\') ) {
            switch ( input.LA(2) ) {
                case 'K':
                case 'k':
                    {
                    alt216=1;
                    }
                    break;
                case '0':
                    {
                    int LA216_4 = input.LA(3);

                    if ( (LA216_4=='0') ) {
                        int LA216_6 = input.LA(4);

                        if ( (LA216_6=='0') ) {
                            int LA216_7 = input.LA(5);

                            if ( (LA216_7=='0') ) {
                                int LA216_8 = input.LA(6);

                                if ( (LA216_8=='4'||LA216_8=='6') ) {
                                    int LA216_5 = input.LA(7);

                                    if ( (LA216_5=='B'||LA216_5=='b') ) {
                                        alt216=1;
                                    }
                                }
                            }
                            else if ( (LA216_7=='4'||LA216_7=='6') ) {
                                int LA216_5 = input.LA(6);

                                if ( (LA216_5=='B'||LA216_5=='b') ) {
                                    alt216=1;
                                }
                            }
                        }
                        else if ( (LA216_6=='4'||LA216_6=='6') ) {
                            int LA216_5 = input.LA(5);

                            if ( (LA216_5=='B'||LA216_5=='b') ) {
                                alt216=1;
                            }
                        }
                    }
                    else if ( (LA216_4=='4'||LA216_4=='6') ) {
                        int LA216_5 = input.LA(4);

                        if ( (LA216_5=='B'||LA216_5=='b') ) {
                            alt216=1;
                        }
                    }
                    }
                    break;
                case '4':
                case '6':
                    {
                    int LA216_5 = input.LA(3);

                    if ( (LA216_5=='B'||LA216_5=='b') ) {
                        alt216=1;
                    }
                    }
                    break;
            }

        }
        switch (alt216) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:833:16: K
                {
                mK(); if (state.failed) return ;

                }
                break;

        }

        mH(); if (state.failed) return ;
        mZ(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred9_Css3

    // $ANTLR start synpred10_Css3
    public final void synpred10_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:849:14: ( WS )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:849:15: WS
        {
        mWS(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred10_Css3

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


    protected DFA11 dfa11 = new DFA11(this);
    protected DFA205 dfa205 = new DFA205(this);
    protected DFA202 dfa202 = new DFA202(this);
    protected DFA212 dfa212 = new DFA212(this);
    protected DFA214 dfa214 = new DFA214(this);
    static final String DFA11_eotS =
        "\1\1\14\uffff";
    static final String DFA11_eofS =
        "\15\uffff";
    static final String DFA11_minS =
        "\1\41\14\uffff";
    static final String DFA11_maxS =
        "\1\uffff\14\uffff";
    static final String DFA11_acceptS =
        "\1\uffff\1\14\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13";
    static final String DFA11_specialS =
        "\15\uffff}>";
    static final String[] DFA11_transitionS = {
            "\1\3\1\uffff\1\4\1\5\1\6\1\7\3\uffff\1\10\2\uffff\1\11\55\uffff"+
            "\1\2\1\14\41\uffff\1\12\1\uffff\uff80\13",
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
            ""
    };

    static final short[] DFA11_eot = DFA.unpackEncodedString(DFA11_eotS);
    static final short[] DFA11_eof = DFA.unpackEncodedString(DFA11_eofS);
    static final char[] DFA11_min = DFA.unpackEncodedStringToUnsignedChars(DFA11_minS);
    static final char[] DFA11_max = DFA.unpackEncodedStringToUnsignedChars(DFA11_maxS);
    static final short[] DFA11_accept = DFA.unpackEncodedString(DFA11_acceptS);
    static final short[] DFA11_special = DFA.unpackEncodedString(DFA11_specialS);
    static final short[][] DFA11_transition;

    static {
        int numStates = DFA11_transitionS.length;
        DFA11_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA11_transition[i] = DFA.unpackEncodedString(DFA11_transitionS[i]);
        }
    }

    class DFA11 extends DFA {

        public DFA11(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 11;
            this.eot = DFA11_eot;
            this.eof = DFA11_eof;
            this.min = DFA11_min;
            this.max = DFA11_max;
            this.accept = DFA11_accept;
            this.special = DFA11_special;
            this.transition = DFA11_transition;
        }
        public String getDescription() {
            return "()* loopback of 489:27: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '-' | '~' | NONASCII | ESCAPE )*";
        }
    }
    static final String DFA205_eotS =
        "\1\30\1\14\1\uffff\6\14\1\uffff\2\14\1\uffff\7\14\1\uffff\2\14\10"+
        "\uffff\13\14\2\uffff\4\14\27\uffff\1\14\1\uffff\1\14\1\uffff\1\14"+
        "\1\uffff\1\14\2\uffff\1\14\1\uffff\1\14\7\uffff\2\14\1\uffff\17"+
        "\14\4\uffff\2\14\1\uffff\1\14\2\uffff\2\14\3\uffff\2\14\1\uffff"+
        "\1\14\2\uffff\2\14\4\uffff\2\14\4\uffff\6\14\2\uffff\5\14\3\uffff"+
        "\16\14\1\uffff\2\14\2\uffff\5\14\3\uffff\2\14\2\uffff\3\14\3\uffff"+
        "\2\14\4\uffff\17\14\2\uffff\3\14\3\uffff\16\14\1\uffff\2\14\2\uffff"+
        "\4\14\2\uffff\3\14\3\uffff\2\14\2\uffff\3\14\3\uffff\2\14\2\uffff"+
        "\2\14\1\uffff\5\14\1\uffff\3\14\2\uffff\5\14\2\uffff\3\14\3\uffff"+
        "\15\14\1\uffff\2\14\2\uffff\2\14\2\uffff\3\14\3\uffff\2\14\2\uffff"+
        "\3\14\3\uffff\2\14\2\uffff\2\14\1\uffff\5\14\1\uffff\3\14\2\uffff"+
        "\5\14\2\uffff\2\14\3\uffff\13\14\1\uffff\2\14\2\uffff\2\14\2\uffff"+
        "\2\14\3\uffff\1\14\2\uffff\2\14\3\uffff\1\14\2\uffff\2\14\1\uffff"+
        "\4\14\1\uffff\2\14\2\uffff\3\14\17\uffff\1\14\1\uffff\2\14\1\uffff"+
        "\1\14\2\uffff\1\14\4\uffff";
    static final String DFA205_eofS =
        "\u01ba\uffff";
    static final String DFA205_minS =
        "\1\45\1\11\1\0\6\11\1\0\2\11\1\uffff\7\11\1\0\2\11\3\uffff\5\0\1"+
        "\103\1\60\1\63\1\103\1\115\1\60\1\115\2\116\2\101\2\0\2\110\2\132"+
        "\1\uffff\7\0\1\uffff\3\0\1\uffff\5\0\1\uffff\3\0\1\uffff\1\11\1"+
        "\0\1\11\1\uffff\1\11\1\0\1\11\2\uffff\1\11\1\0\1\11\1\uffff\6\0"+
        "\1\60\1\104\1\0\1\70\1\60\1\63\1\60\3\115\1\116\1\105\1\110\1\132"+
        "\1\115\1\110\1\103\1\101\4\0\1\60\1\64\1\0\1\63\2\0\1\60\1\104\3"+
        "\0\1\60\1\104\1\0\1\63\2\0\1\60\1\105\1\uffff\3\0\1\60\1\65\1\uffff"+
        "\3\0\1\60\1\61\2\132\1\60\1\70\2\0\1\60\1\101\1\60\1\104\1\70\3"+
        "\0\1\60\1\63\1\60\3\115\1\116\1\105\1\110\1\132\1\115\1\110\1\103"+
        "\1\101\1\0\2\11\2\0\2\11\1\60\1\64\1\63\3\0\1\60\1\104\2\0\1\60"+
        "\1\104\1\63\3\0\1\60\1\105\4\0\1\60\1\67\1\60\1\65\1\107\1\60\1"+
        "\64\1\60\1\61\1\104\1\60\1\70\1\132\1\60\1\101\2\0\1\60\1\104\1"+
        "\70\3\0\1\64\1\63\1\60\3\115\1\116\1\105\1\110\1\132\1\115\1\110"+
        "\1\103\1\101\1\0\2\11\2\0\4\11\2\0\1\60\1\64\1\63\3\0\1\60\1\104"+
        "\2\0\1\60\1\104\1\63\3\0\1\60\1\105\2\0\1\60\1\67\1\0\1\60\1\65"+
        "\1\107\1\60\1\64\1\0\1\60\1\61\1\104\2\0\1\60\1\70\1\132\1\60\1"+
        "\101\2\0\1\64\1\104\1\70\3\0\1\63\1\60\3\115\1\116\1\105\1\110\1"+
        "\132\1\115\1\110\1\103\1\101\1\0\2\11\2\0\2\11\2\0\2\64\1\63\3\0"+
        "\1\64\1\104\2\0\1\64\1\104\1\63\3\0\1\64\1\105\2\0\1\60\1\67\1\0"+
        "\1\64\1\65\1\107\1\60\1\64\1\0\1\64\1\61\1\104\2\0\1\64\1\70\1\132"+
        "\1\65\1\101\2\0\1\104\1\70\3\0\3\115\1\116\1\105\1\110\1\132\1\115"+
        "\1\110\1\103\1\101\1\0\2\11\2\0\2\11\2\0\1\64\1\63\3\0\1\104\2\0"+
        "\1\104\1\63\3\0\1\105\2\0\1\64\1\67\1\0\1\65\1\107\2\64\1\0\1\61"+
        "\1\104\2\0\1\70\1\132\1\101\17\0\1\67\1\0\1\107\1\64\1\0\1\104\2"+
        "\0\1\132\4\0";
    static final String DFA205_maxS =
        "\1\uffff\1\170\1\uffff\1\170\1\155\1\163\1\156\1\145\1\141\1\0\1"+
        "\150\1\172\1\uffff\2\170\1\155\1\163\1\156\1\145\1\141\1\0\1\150"+
        "\1\172\3\uffff\1\0\1\uffff\3\0\1\170\1\67\1\144\1\170\1\163\1\63"+
        "\1\163\2\156\2\141\2\0\2\150\2\172\1\uffff\1\0\1\uffff\5\0\1\uffff"+
        "\1\0\1\uffff\1\0\1\uffff\1\0\1\uffff\3\0\1\uffff\1\0\1\uffff\1\0"+
        "\1\uffff\1\147\1\uffff\1\147\1\uffff\1\144\1\uffff\1\144\2\uffff"+
        "\1\172\1\uffff\1\172\1\uffff\1\0\1\uffff\4\0\1\67\1\144\1\0\1\70"+
        "\1\67\1\144\1\63\1\170\1\155\1\163\1\156\1\145\1\150\1\172\1\163"+
        "\1\150\1\170\1\141\4\0\1\67\1\70\1\0\1\63\2\0\1\66\1\144\3\0\1\67"+
        "\1\144\1\0\1\63\2\0\1\66\1\145\1\uffff\1\0\1\uffff\1\0\1\66\1\65"+
        "\1\uffff\1\0\1\uffff\1\0\1\66\1\61\2\172\1\66\1\70\2\0\1\67\1\141"+
        "\1\67\1\144\1\70\3\0\1\67\1\144\1\63\1\170\1\155\1\163\1\156\1\145"+
        "\1\150\1\172\1\163\1\150\1\170\1\141\1\0\2\147\2\0\2\144\1\67\1"+
        "\70\1\63\3\0\1\66\1\144\2\0\1\67\1\144\1\63\3\0\1\66\1\145\4\0\1"+
        "\66\1\67\1\66\1\65\1\147\1\66\1\64\1\66\1\61\1\144\1\66\1\70\1\172"+
        "\1\67\1\141\2\0\1\67\1\144\1\70\3\0\1\67\1\144\1\63\1\170\1\155"+
        "\1\163\1\156\1\145\1\150\1\172\1\163\1\150\1\170\1\141\1\0\2\147"+
        "\2\0\2\144\1\147\1\144\2\0\1\67\1\70\1\63\3\0\1\66\1\144\2\0\1\67"+
        "\1\144\1\63\3\0\1\66\1\145\2\0\1\66\1\67\1\0\1\66\1\65\1\147\1\66"+
        "\1\64\1\0\1\66\1\61\1\144\2\0\1\66\1\70\1\172\1\67\1\141\2\0\1\67"+
        "\1\144\1\70\3\0\1\144\1\63\1\170\1\155\1\163\1\156\1\145\1\150\1"+
        "\172\1\163\1\150\1\170\1\141\1\0\2\147\2\0\2\144\2\0\1\67\1\70\1"+
        "\63\3\0\1\66\1\144\2\0\1\67\1\144\1\63\3\0\1\66\1\145\2\0\1\66\1"+
        "\67\1\0\1\66\1\65\1\147\1\66\1\64\1\0\1\66\1\61\1\144\2\0\1\66\1"+
        "\70\1\172\1\67\1\141\2\0\1\144\1\70\3\0\1\170\1\155\1\163\1\156"+
        "\1\145\1\150\1\172\1\163\1\150\1\170\1\141\1\0\2\147\2\0\2\144\2"+
        "\0\1\70\1\63\3\0\1\144\2\0\1\144\1\63\3\0\1\145\2\0\1\66\1\67\1"+
        "\0\1\65\1\147\1\66\1\64\1\0\1\61\1\144\2\0\1\70\1\172\1\141\17\0"+
        "\1\67\1\0\1\147\1\64\1\0\1\144\2\0\1\172\4\0";
    static final String DFA205_acceptS =
        "\14\uffff\1\12\12\uffff\1\13\1\14\1\1\26\uffff\1\2\7\uffff\1\3\3"+
        "\uffff\1\4\5\uffff\1\5\3\uffff\1\6\3\uffff\1\7\3\uffff\1\10\1\11"+
        "\3\uffff\1\11\57\uffff\1\6\5\uffff\1\7\u0130\uffff";
    static final String DFA205_specialS =
        "\1\uffff\1\u008b\1\u00a5\1\151\1\32\1\u00a4\1\35\1\131\1\71\1\105"+
        "\1\152\1\170\1\uffff\1\u008c\1\147\1\31\1\u00a1\1\34\1\132\1\70"+
        "\1\102\1\150\1\167\3\uffff\1\125\1\121\1\100\1\124\1\76\13\uffff"+
        "\1\u0087\1\u0089\5\uffff\1\117\1\u008f\1\u0090\1\135\1\114\1\u0093"+
        "\1\136\1\uffff\1\u00c2\1\156\1\u00bd\1\uffff\1\12\1\u00b9\1\u00b4"+
        "\1\10\1\u00b3\1\uffff\1\177\1\u00b2\1\u0081\1\uffff\1\u00c0\1\1"+
        "\1\u00be\1\uffff\1\u008d\1\115\1\u008e\2\uffff\1\73\1\u0096\1\74"+
        "\1\uffff\1\13\1\51\1\5\1\u00b6\1\u00b5\1\141\2\uffff\1\144\17\uffff"+
        "\1\u00af\1\155\1\153\1\u00b8\2\uffff\1\u00b7\1\uffff\1\u00bc\1\u00bb"+
        "\2\uffff\1\u00a0\1\u00a2\1\u00b1\2\uffff\1\u00b0\1\uffff\1\64\1"+
        "\63\3\uffff\1\u00a7\1\21\1\u00aa\3\uffff\1\11\1\61\1\7\6\uffff\1"+
        "\u0085\1\u0084\5\uffff\1\u0080\1\u0082\1\u0083\16\uffff\1\u008a"+
        "\2\uffff\1\u009d\1\u009c\5\uffff\1\u0088\1\24\1\111\2\uffff\1\103"+
        "\1\107\3\uffff\1\u0095\1\u0097\1\17\2\uffff\1\4\1\0\1\22\1\23\17"+
        "\uffff\1\126\1\130\3\uffff\1\66\1\65\1\113\16\uffff\1\6\2\uffff"+
        "\1\u00bf\1\u00c1\4\uffff\1\u00a9\1\u00a8\3\uffff\1\72\1\u0099\1"+
        "\20\2\uffff\1\16\1\14\3\uffff\1\142\1\143\1\60\2\uffff\1\u009e\1"+
        "\u009f\2\uffff\1\15\5\uffff\1\u00a6\3\uffff\1\116\1\120\5\uffff"+
        "\1\25\1\33\3\uffff\1\146\1\145\1\140\15\uffff\1\62\2\uffff\1\55"+
        "\1\54\2\uffff\1\176\1\175\3\uffff\1\30\1\171\1\u00c3\2\uffff\1\u009a"+
        "\1\u009b\3\uffff\1\41\1\42\1\u00ba\2\uffff\1\174\1\173\2\uffff\1"+
        "\u00ac\5\uffff\1\172\3\uffff\1\133\1\134\5\uffff\1\77\1\75\2\uffff"+
        "\1\52\1\53\1\45\13\uffff\1\67\2\uffff\1\46\1\47\2\uffff\1\165\1"+
        "\164\2\uffff\1\u00ab\1\u0091\1\166\1\uffff\1\160\1\161\2\uffff\1"+
        "\112\1\110\1\u0086\1\uffff\1\104\1\101\2\uffff\1\163\4\uffff\1\106"+
        "\2\uffff\1\37\1\40\3\uffff\1\3\1\2\1\43\1\44\1\57\1\u00a3\1\56\1"+
        "\u0092\1\u0098\1\u0094\1\u00ae\1\u00ad\1\50\1\123\1\127\1\uffff"+
        "\1\154\2\uffff\1\36\1\uffff\1\27\1\26\1\uffff\1\162\1\157\1\137"+
        "\1\122}>";
    static final String[] DFA205_transitionS = {
            "\1\27\7\uffff\1\14\23\uffff\2\14\1\17\1\22\1\15\2\14\1\26\1"+
            "\21\1\14\1\25\1\14\1\20\2\14\1\16\1\14\1\23\1\24\7\14\1\uffff"+
            "\1\2\2\uffff\1\14\1\uffff\2\14\1\4\1\7\1\1\2\14\1\13\1\6\1\14"+
            "\1\12\1\14\1\5\2\14\1\3\1\14\1\10\1\11\7\14\5\uffff\uff80\14",
            "\2\31\1\uffff\2\31\22\uffff\1\31\54\uffff\1\35\12\uffff\1\36"+
            "\3\uffff\1\33\20\uffff\1\32\12\uffff\1\34",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\40\3\14\1\41\1\44\1\41"+
            "\1\44\20\14\1\57\1\47\1\14\1\55\1\14\1\45\2\14\1\42\1\14\1\51"+
            "\1\53\24\14\1\56\1\46\1\14\1\54\1\14\1\43\2\14\1\37\1\14\1\50"+
            "\1\52\uff8c\14",
            "\2\60\1\uffff\2\60\22\uffff\1\60\42\uffff\1\67\20\uffff\1\66"+
            "\3\uffff\1\65\3\uffff\1\62\6\uffff\1\64\20\uffff\1\63\3\uffff"+
            "\1\61",
            "\2\70\1\uffff\2\70\22\uffff\1\70\54\uffff\1\73\16\uffff\1\72"+
            "\20\uffff\1\71",
            "\2\74\1\uffff\2\74\22\uffff\1\74\54\uffff\1\100\5\uffff\1\101"+
            "\10\uffff\1\76\20\uffff\1\75\5\uffff\1\77",
            "\2\102\1\uffff\2\102\22\uffff\1\102\55\uffff\1\105\15\uffff"+
            "\1\104\21\uffff\1\103",
            "\2\106\1\uffff\2\106\22\uffff\1\106\44\uffff\1\111\26\uffff"+
            "\1\110\10\uffff\1\107",
            "\2\112\1\uffff\2\112\22\uffff\1\112\40\uffff\1\115\32\uffff"+
            "\1\114\4\uffff\1\113",
            "\1\uffff",
            "\2\117\1\uffff\2\117\22\uffff\1\117\47\uffff\1\122\23\uffff"+
            "\1\121\13\uffff\1\120",
            "\2\123\1\uffff\2\123\22\uffff\1\123\71\uffff\1\126\1\uffff"+
            "\1\125\35\uffff\1\124",
            "",
            "\2\31\1\uffff\2\31\22\uffff\1\31\54\uffff\1\35\12\uffff\1\36"+
            "\3\uffff\1\33\20\uffff\1\32\12\uffff\1\34",
            "\2\60\1\uffff\2\60\22\uffff\1\60\42\uffff\1\67\20\uffff\1\66"+
            "\3\uffff\1\65\3\uffff\1\62\6\uffff\1\64\20\uffff\1\63\3\uffff"+
            "\1\61",
            "\2\70\1\uffff\2\70\22\uffff\1\70\54\uffff\1\73\16\uffff\1\72"+
            "\20\uffff\1\71",
            "\2\74\1\uffff\2\74\22\uffff\1\74\54\uffff\1\100\5\uffff\1\101"+
            "\10\uffff\1\76\20\uffff\1\75\5\uffff\1\77",
            "\2\102\1\uffff\2\102\22\uffff\1\102\55\uffff\1\105\15\uffff"+
            "\1\104\21\uffff\1\103",
            "\2\106\1\uffff\2\106\22\uffff\1\106\44\uffff\1\111\26\uffff"+
            "\1\110\10\uffff\1\107",
            "\2\112\1\uffff\2\112\22\uffff\1\112\40\uffff\1\115\32\uffff"+
            "\1\114\4\uffff\1\113",
            "\1\uffff",
            "\2\117\1\uffff\2\117\22\uffff\1\117\47\uffff\1\122\23\uffff"+
            "\1\121\13\uffff\1\120",
            "\2\123\1\uffff\2\123\22\uffff\1\123\71\uffff\1\126\1\uffff"+
            "\1\125\35\uffff\1\124",
            "",
            "",
            "",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\132\3\14\1\133\1\135\1"+
            "\133\1\135\25\14\1\130\12\14\1\134\24\14\1\127\12\14\1\131\uff87"+
            "\14",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\67\20\uffff\1\66\3\uffff\1\65\3\uffff\1\62\6\uffff\1\64"+
            "\20\uffff\1\63\3\uffff\1\61",
            "\1\136\3\uffff\1\137\1\140\1\137\1\140",
            "\1\142\1\145\1\141\2\uffff\1\147\1\144\10\uffff\1\151\1\uffff"+
            "\1\150\35\uffff\1\146\1\uffff\1\143",
            "\1\67\20\uffff\1\66\3\uffff\1\65\3\uffff\1\62\6\uffff\1\64"+
            "\20\uffff\1\63\3\uffff\1\61",
            "\1\100\5\uffff\1\101\10\uffff\1\76\20\uffff\1\75\5\uffff\1"+
            "\77",
            "\1\152\1\uffff\1\153\1\154",
            "\1\100\5\uffff\1\101\10\uffff\1\76\20\uffff\1\75\5\uffff\1"+
            "\77",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\115\32\uffff\1\114\4\uffff\1\113",
            "\1\115\32\uffff\1\114\4\uffff\1\113",
            "\1\uffff",
            "\1\uffff",
            "\1\122\23\uffff\1\121\13\uffff\1\120",
            "\1\122\23\uffff\1\121\13\uffff\1\120",
            "\1\126\1\uffff\1\125\35\uffff\1\124",
            "\1\126\1\uffff\1\125\35\uffff\1\124",
            "",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\160\3\14\1\163\1\161\1"+
            "\163\1\161\34\14\1\162\3\14\1\156\33\14\1\157\3\14\1\155\uff87"+
            "\14",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\166\3\14\1\167\1\14\1\167"+
            "\26\14\1\165\37\14\1\164\uff92\14",
            "\1\uffff",
            "",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\173\3\14\1\174\1\176\1"+
            "\174\1\176\25\14\1\171\5\14\1\175\31\14\1\170\5\14\1\172\uff8c"+
            "\14",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u0081\3\14\1\u0082\1\14"+
            "\1\u0082\27\14\1\u0080\37\14\1\177\uff91\14",
            "\1\uffff",
            "",
            "\2\u0083\1\uffff\2\u0083\22\uffff\1\u0083\46\uffff\1\u0086"+
            "\24\uffff\1\u0085\12\uffff\1\u0084",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u0087\3\14\1\u0088\1\14"+
            "\1\u0088\uffc9\14",
            "\2\u0083\1\uffff\2\u0083\22\uffff\1\u0083\46\uffff\1\u0086"+
            "\24\uffff\1\u0085\12\uffff\1\u0084",
            "",
            "\2\u0089\1\uffff\2\u0089\22\uffff\1\u0089\43\uffff\1\u008c"+
            "\27\uffff\1\u008b\7\uffff\1\u008a",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u008d\3\14\1\u008e\1\14"+
            "\1\u008e\uffc9\14",
            "\2\u0089\1\uffff\2\u0089\22\uffff\1\u0089\43\uffff\1\u008c"+
            "\27\uffff\1\u008b\7\uffff\1\u008a",
            "",
            "",
            "\2\123\1\uffff\2\123\22\uffff\1\123\71\uffff\1\126\1\uffff"+
            "\1\125\35\uffff\1\124",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u0091\3\14\1\u0092\1\14"+
            "\1\u0092\21\14\1\u0090\37\14\1\u008f\uff97\14",
            "\2\123\1\uffff\2\123\22\uffff\1\123\71\uffff\1\126\1\uffff"+
            "\1\125\35\uffff\1\124",
            "",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u0095\4\14\1\u0096\1\14"+
            "\1\u0096\42\14\1\u0094\37\14\1\u0093\uff85\14",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0097\3\uffff\1\u0098\1\u0099\1\u0098\1\u0099",
            "\1\u009b\37\uffff\1\u009a",
            "\1\uffff",
            "\1\u009c",
            "\1\u009d\3\uffff\1\u009e\1\u009f\1\u009e\1\u009f",
            "\1\u00a1\1\u00a4\1\u00a0\2\uffff\1\u00a6\1\u00a3\10\uffff\1"+
            "\u00a8\1\uffff\1\u00a7\35\uffff\1\u00a5\1\uffff\1\u00a2",
            "\1\u00a9\1\uffff\1\u00aa\1\u00ab",
            "\1\35\12\uffff\1\36\3\uffff\1\33\20\uffff\1\32\12\uffff\1\34",
            "\1\73\16\uffff\1\72\20\uffff\1\71",
            "\1\100\5\uffff\1\101\10\uffff\1\76\20\uffff\1\75\5\uffff\1"+
            "\77",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\u00ad\26\uffff\1\110\10\uffff\1\u00ac",
            "\1\122\23\uffff\1\121\13\uffff\1\120",
            "\1\126\1\uffff\1\125\35\uffff\1\124",
            "\1\100\5\uffff\1\101\10\uffff\1\76\20\uffff\1\75\5\uffff\1"+
            "\77",
            "\1\122\23\uffff\1\121\13\uffff\1\120",
            "\1\u00af\20\uffff\1\66\3\uffff\1\65\3\uffff\1\62\6\uffff\1"+
            "\u00ae\20\uffff\1\63\3\uffff\1\61",
            "\1\u00b1\32\uffff\1\114\4\uffff\1\u00b0",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u00b2\3\uffff\1\u00b4\1\u00b3\1\u00b4\1\u00b3",
            "\1\u00b6\3\uffff\1\u00b5",
            "\1\uffff",
            "\1\u00b7",
            "\1\uffff",
            "\1\uffff",
            "\1\u00b8\3\uffff\1\u00b9\1\uffff\1\u00b9",
            "\1\u00bb\37\uffff\1\u00ba",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u00bc\3\uffff\1\u00bd\1\u00be\1\u00bd\1\u00be",
            "\1\u00c0\37\uffff\1\u00bf",
            "\1\uffff",
            "\1\u00c1",
            "\1\uffff",
            "\1\uffff",
            "\1\u00c2\3\uffff\1\u00c3\1\uffff\1\u00c3",
            "\1\u00c5\37\uffff\1\u00c4",
            "",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u00c8\3\14\1\u00c9\1\14"+
            "\1\u00c9\20\14\1\u00c7\37\14\1\u00c6\uff98\14",
            "\1\uffff",
            "\1\u00ca\3\uffff\1\u00cb\1\uffff\1\u00cb",
            "\1\u00cc",
            "",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u00cd\3\14\1\u00ce\1\14"+
            "\1\u00ce\uffc9\14",
            "\1\uffff",
            "\1\u00cf\3\uffff\1\u00d0\1\uffff\1\u00d0",
            "\1\u00d1",
            "\1\126\1\uffff\1\125\35\uffff\1\124",
            "\1\126\1\uffff\1\125\35\uffff\1\124",
            "\1\u00d2\3\uffff\1\u00d3\1\uffff\1\u00d3",
            "\1\u00d4",
            "\1\uffff",
            "\1\uffff",
            "\1\u00d5\4\uffff\1\u00d6\1\uffff\1\u00d6",
            "\1\u00d8\37\uffff\1\u00d7",
            "\1\u00d9\3\uffff\1\u00da\1\u00db\1\u00da\1\u00db",
            "\1\u00dd\37\uffff\1\u00dc",
            "\1\u00de",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u00df\3\uffff\1\u00e0\1\u00e1\1\u00e0\1\u00e1",
            "\1\u00e3\1\u00e6\1\u00e2\2\uffff\1\u00e8\1\u00e5\10\uffff\1"+
            "\u00ea\1\uffff\1\u00e9\35\uffff\1\u00e7\1\uffff\1\u00e4",
            "\1\u00eb\1\uffff\1\u00ec\1\u00ed",
            "\1\35\12\uffff\1\36\3\uffff\1\33\20\uffff\1\32\12\uffff\1\34",
            "\1\73\16\uffff\1\72\20\uffff\1\71",
            "\1\100\5\uffff\1\101\10\uffff\1\76\20\uffff\1\75\5\uffff\1"+
            "\77",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\u00ef\26\uffff\1\110\10\uffff\1\u00ee",
            "\1\122\23\uffff\1\121\13\uffff\1\120",
            "\1\126\1\uffff\1\125\35\uffff\1\124",
            "\1\100\5\uffff\1\101\10\uffff\1\76\20\uffff\1\75\5\uffff\1"+
            "\77",
            "\1\122\23\uffff\1\121\13\uffff\1\120",
            "\1\u00f1\20\uffff\1\66\3\uffff\1\65\3\uffff\1\62\6\uffff\1"+
            "\u00f0\20\uffff\1\63\3\uffff\1\61",
            "\1\u00f3\32\uffff\1\114\4\uffff\1\u00f2",
            "\1\uffff",
            "\2\u00f4\1\uffff\2\u00f4\22\uffff\1\u00f4\46\uffff\1\u0086"+
            "\24\uffff\1\u0085\12\uffff\1\u0084",
            "\2\u00f4\1\uffff\2\u00f4\22\uffff\1\u00f4\46\uffff\1\u0086"+
            "\24\uffff\1\u0085\12\uffff\1\u0084",
            "\1\uffff",
            "\1\uffff",
            "\2\u00f5\1\uffff\2\u00f5\22\uffff\1\u00f5\43\uffff\1\u00f7"+
            "\27\uffff\1\u008b\7\uffff\1\u00f6",
            "\2\u00f5\1\uffff\2\u00f5\22\uffff\1\u00f5\43\uffff\1\u00f7"+
            "\27\uffff\1\u008b\7\uffff\1\u00f6",
            "\1\u00f8\3\uffff\1\u00fa\1\u00f9\1\u00fa\1\u00f9",
            "\1\u00fc\3\uffff\1\u00fb",
            "\1\u00fd",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u00fe\3\uffff\1\u00ff\1\uffff\1\u00ff",
            "\1\u0101\37\uffff\1\u0100",
            "\1\uffff",
            "\1\uffff",
            "\1\u0102\3\uffff\1\u0103\1\u0104\1\u0103\1\u0104",
            "\1\u0106\37\uffff\1\u0105",
            "\1\u0107",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0108\3\uffff\1\u0109\1\uffff\1\u0109",
            "\1\u010b\37\uffff\1\u010a",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u010c\3\uffff\1\u010d\1\uffff\1\u010d",
            "\1\u010e",
            "\1\u010f\3\uffff\1\u0110\1\uffff\1\u0110",
            "\1\u0111",
            "\1\u0086\24\uffff\1\u0085\12\uffff\1\u0084",
            "\1\u0112\3\uffff\1\u0113\1\uffff\1\u0113",
            "\1\u0114",
            "\1\u0115\3\uffff\1\u0116\1\uffff\1\u0116",
            "\1\u0117",
            "\1\u0119\27\uffff\1\u008b\7\uffff\1\u0118",
            "\1\u011a\3\uffff\1\u011b\1\uffff\1\u011b",
            "\1\u011c",
            "\1\126\1\uffff\1\125\35\uffff\1\124",
            "\1\u011d\4\uffff\1\u011e\1\uffff\1\u011e",
            "\1\u0120\37\uffff\1\u011f",
            "\1\uffff",
            "\1\uffff",
            "\1\u0121\3\uffff\1\u0122\1\u0123\1\u0122\1\u0123",
            "\1\u0125\37\uffff\1\u0124",
            "\1\u0126",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0127\1\u0128\1\u0127\1\u0128",
            "\1\u012a\1\u012d\1\u0129\2\uffff\1\u012f\1\u012c\10\uffff\1"+
            "\u0131\1\uffff\1\u0130\35\uffff\1\u012e\1\uffff\1\u012b",
            "\1\u0132\1\uffff\1\u0133\1\u0134",
            "\1\35\12\uffff\1\36\3\uffff\1\33\20\uffff\1\32\12\uffff\1\34",
            "\1\73\16\uffff\1\72\20\uffff\1\71",
            "\1\100\5\uffff\1\101\10\uffff\1\76\20\uffff\1\75\5\uffff\1"+
            "\77",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\u0136\26\uffff\1\110\10\uffff\1\u0135",
            "\1\122\23\uffff\1\121\13\uffff\1\120",
            "\1\126\1\uffff\1\125\35\uffff\1\124",
            "\1\100\5\uffff\1\101\10\uffff\1\76\20\uffff\1\75\5\uffff\1"+
            "\77",
            "\1\122\23\uffff\1\121\13\uffff\1\120",
            "\1\u0138\20\uffff\1\66\3\uffff\1\65\3\uffff\1\62\6\uffff\1"+
            "\u0137\20\uffff\1\63\3\uffff\1\61",
            "\1\u013a\32\uffff\1\114\4\uffff\1\u0139",
            "\1\uffff",
            "\2\u00f4\1\uffff\2\u00f4\22\uffff\1\u00f4\46\uffff\1\u0086"+
            "\24\uffff\1\u0085\12\uffff\1\u0084",
            "\2\u00f4\1\uffff\2\u00f4\22\uffff\1\u00f4\46\uffff\1\u0086"+
            "\24\uffff\1\u0085\12\uffff\1\u0084",
            "\1\uffff",
            "\1\uffff",
            "\2\u00f5\1\uffff\2\u00f5\22\uffff\1\u00f5\43\uffff\1\u013c"+
            "\27\uffff\1\u008b\7\uffff\1\u013b",
            "\2\u00f5\1\uffff\2\u00f5\22\uffff\1\u00f5\43\uffff\1\u013c"+
            "\27\uffff\1\u008b\7\uffff\1\u013b",
            "\2\u00f4\1\uffff\2\u00f4\22\uffff\1\u00f4\46\uffff\1\u0086"+
            "\24\uffff\1\u0085\12\uffff\1\u0084",
            "\2\u00f5\1\uffff\2\u00f5\22\uffff\1\u00f5\43\uffff\1\u008c"+
            "\27\uffff\1\u008b\7\uffff\1\u008a",
            "\1\uffff",
            "\1\uffff",
            "\1\u013d\3\uffff\1\u013f\1\u013e\1\u013f\1\u013e",
            "\1\u0141\3\uffff\1\u0140",
            "\1\u0142",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0143\3\uffff\1\u0144\1\uffff\1\u0144",
            "\1\u0146\37\uffff\1\u0145",
            "\1\uffff",
            "\1\uffff",
            "\1\u0147\3\uffff\1\u0148\1\u0149\1\u0148\1\u0149",
            "\1\u014b\37\uffff\1\u014a",
            "\1\u014c",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u014d\3\uffff\1\u014e\1\uffff\1\u014e",
            "\1\u0150\37\uffff\1\u014f",
            "\1\uffff",
            "\1\uffff",
            "\1\u0151\3\uffff\1\u0152\1\uffff\1\u0152",
            "\1\u0153",
            "\1\uffff",
            "\1\u0154\3\uffff\1\u0155\1\uffff\1\u0155",
            "\1\u0156",
            "\1\u0086\24\uffff\1\u0085\12\uffff\1\u0084",
            "\1\u0157\3\uffff\1\u0158\1\uffff\1\u0158",
            "\1\u0159",
            "\1\uffff",
            "\1\u015a\3\uffff\1\u015b\1\uffff\1\u015b",
            "\1\u015c",
            "\1\u015e\27\uffff\1\u008b\7\uffff\1\u015d",
            "\1\uffff",
            "\1\uffff",
            "\1\u015f\3\uffff\1\u0160\1\uffff\1\u0160",
            "\1\u0161",
            "\1\126\1\uffff\1\125\35\uffff\1\124",
            "\1\u0162\4\uffff\1\u0163\1\uffff\1\u0163",
            "\1\u0165\37\uffff\1\u0164",
            "\1\uffff",
            "\1\uffff",
            "\1\u0166\1\u0167\1\u0166\1\u0167",
            "\1\u0169\37\uffff\1\u0168",
            "\1\u016a",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u016c\1\u016f\1\u016b\2\uffff\1\u0171\1\u016e\10\uffff\1"+
            "\u0173\1\uffff\1\u0172\35\uffff\1\u0170\1\uffff\1\u016d",
            "\1\u0174\1\uffff\1\u0175\1\u0176",
            "\1\35\12\uffff\1\36\3\uffff\1\33\20\uffff\1\32\12\uffff\1\34",
            "\1\73\16\uffff\1\72\20\uffff\1\71",
            "\1\100\5\uffff\1\101\10\uffff\1\76\20\uffff\1\75\5\uffff\1"+
            "\77",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\u0178\26\uffff\1\110\10\uffff\1\u0177",
            "\1\122\23\uffff\1\121\13\uffff\1\120",
            "\1\126\1\uffff\1\125\35\uffff\1\124",
            "\1\100\5\uffff\1\101\10\uffff\1\76\20\uffff\1\75\5\uffff\1"+
            "\77",
            "\1\122\23\uffff\1\121\13\uffff\1\120",
            "\1\u017a\20\uffff\1\66\3\uffff\1\65\3\uffff\1\62\6\uffff\1"+
            "\u0179\20\uffff\1\63\3\uffff\1\61",
            "\1\u017c\32\uffff\1\114\4\uffff\1\u017b",
            "\1\uffff",
            "\2\u00f4\1\uffff\2\u00f4\22\uffff\1\u00f4\46\uffff\1\u0086"+
            "\24\uffff\1\u0085\12\uffff\1\u0084",
            "\2\u00f4\1\uffff\2\u00f4\22\uffff\1\u00f4\46\uffff\1\u0086"+
            "\24\uffff\1\u0085\12\uffff\1\u0084",
            "\1\uffff",
            "\1\uffff",
            "\2\u00f5\1\uffff\2\u00f5\22\uffff\1\u00f5\43\uffff\1\u017e"+
            "\27\uffff\1\u008b\7\uffff\1\u017d",
            "\2\u00f5\1\uffff\2\u00f5\22\uffff\1\u00f5\43\uffff\1\u017e"+
            "\27\uffff\1\u008b\7\uffff\1\u017d",
            "\1\uffff",
            "\1\uffff",
            "\1\u0180\1\u017f\1\u0180\1\u017f",
            "\1\u0182\3\uffff\1\u0181",
            "\1\u0183",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0184\1\uffff\1\u0184",
            "\1\u0186\37\uffff\1\u0185",
            "\1\uffff",
            "\1\uffff",
            "\1\u0187\1\u0188\1\u0187\1\u0188",
            "\1\u018a\37\uffff\1\u0189",
            "\1\u018b",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u018c\1\uffff\1\u018c",
            "\1\u018e\37\uffff\1\u018d",
            "\1\uffff",
            "\1\uffff",
            "\1\u018f\3\uffff\1\u0190\1\uffff\1\u0190",
            "\1\u0191",
            "\1\uffff",
            "\1\u0192\1\uffff\1\u0192",
            "\1\u0193",
            "\1\u0086\24\uffff\1\u0085\12\uffff\1\u0084",
            "\1\u0194\3\uffff\1\u0195\1\uffff\1\u0195",
            "\1\u0196",
            "\1\uffff",
            "\1\u0197\1\uffff\1\u0197",
            "\1\u0198",
            "\1\u019a\27\uffff\1\u008b\7\uffff\1\u0199",
            "\1\uffff",
            "\1\uffff",
            "\1\u019b\1\uffff\1\u019b",
            "\1\u019c",
            "\1\126\1\uffff\1\125\35\uffff\1\124",
            "\1\u019d\1\uffff\1\u019d",
            "\1\u019f\37\uffff\1\u019e",
            "\1\uffff",
            "\1\uffff",
            "\1\u01a1\37\uffff\1\u01a0",
            "\1\u01a2",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\35\12\uffff\1\36\3\uffff\1\33\20\uffff\1\32\12\uffff\1\34",
            "\1\73\16\uffff\1\72\20\uffff\1\71",
            "\1\100\5\uffff\1\101\10\uffff\1\76\20\uffff\1\75\5\uffff\1"+
            "\77",
            "\1\105\15\uffff\1\104\21\uffff\1\103",
            "\1\111\26\uffff\1\110\10\uffff\1\107",
            "\1\122\23\uffff\1\121\13\uffff\1\120",
            "\1\126\1\uffff\1\125\35\uffff\1\124",
            "\1\100\5\uffff\1\101\10\uffff\1\76\20\uffff\1\75\5\uffff\1"+
            "\77",
            "\1\122\23\uffff\1\121\13\uffff\1\120",
            "\1\67\20\uffff\1\66\3\uffff\1\65\3\uffff\1\62\6\uffff\1\64"+
            "\20\uffff\1\63\3\uffff\1\61",
            "\1\115\32\uffff\1\114\4\uffff\1\113",
            "\1\uffff",
            "\2\u00f4\1\uffff\2\u00f4\22\uffff\1\u00f4\46\uffff\1\u0086"+
            "\24\uffff\1\u0085\12\uffff\1\u0084",
            "\2\u00f4\1\uffff\2\u00f4\22\uffff\1\u00f4\46\uffff\1\u0086"+
            "\24\uffff\1\u0085\12\uffff\1\u0084",
            "\1\uffff",
            "\1\uffff",
            "\2\u00f5\1\uffff\2\u00f5\22\uffff\1\u00f5\43\uffff\1\u008c"+
            "\27\uffff\1\u008b\7\uffff\1\u008a",
            "\2\u00f5\1\uffff\2\u00f5\22\uffff\1\u00f5\43\uffff\1\u008c"+
            "\27\uffff\1\u008b\7\uffff\1\u008a",
            "\1\uffff",
            "\1\uffff",
            "\1\u01a4\3\uffff\1\u01a3",
            "\1\u01a5",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u01a7\37\uffff\1\u01a6",
            "\1\uffff",
            "\1\uffff",
            "\1\u01a9\37\uffff\1\u01a8",
            "\1\u01aa",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u01ac\37\uffff\1\u01ab",
            "\1\uffff",
            "\1\uffff",
            "\1\u01ad\1\uffff\1\u01ad",
            "\1\u01ae",
            "\1\uffff",
            "\1\u01af",
            "\1\u0086\24\uffff\1\u0085\12\uffff\1\u0084",
            "\1\u01b0\1\uffff\1\u01b0",
            "\1\u01b1",
            "\1\uffff",
            "\1\u01b2",
            "\1\u01b4\27\uffff\1\u008b\7\uffff\1\u01b3",
            "\1\uffff",
            "\1\uffff",
            "\1\u01b5",
            "\1\126\1\uffff\1\125\35\uffff\1\124",
            "\1\u01b7\37\uffff\1\u01b6",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u01b8",
            "\1\uffff",
            "\1\u0086\24\uffff\1\u0085\12\uffff\1\u0084",
            "\1\u01b9",
            "\1\uffff",
            "\1\u008c\27\uffff\1\u008b\7\uffff\1\u008a",
            "\1\uffff",
            "\1\uffff",
            "\1\126\1\uffff\1\125\35\uffff\1\124",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff"
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

    class DFA205 extends DFA {

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
        public String getDescription() {
            return "799:9: ( ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R A D )=> R A D | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | '%' | )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA205_197 = input.LA(1);

                         
                        int index205_197 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 66;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_197);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA205_72 = input.LA(1);

                        s = -1;
                        if ( ((LA205_72>='\u0000' && LA205_72<='\t')||LA205_72=='\u000B'||(LA205_72>='\u000E' && LA205_72<='/')||(LA205_72>='1' && LA205_72<='3')||LA205_72=='5'||(LA205_72>='7' && LA205_72<='\uFFFF')) ) {s = 12;}

                        else if ( (LA205_72=='0') ) {s = 135;}

                        else if ( (LA205_72=='4'||LA205_72=='6') ) {s = 136;}

                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA205_415 = input.LA(1);

                         
                        int index205_415 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 83;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_415);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA205_414 = input.LA(1);

                         
                        int index205_414 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 83;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_414);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA205_196 = input.LA(1);

                         
                        int index205_196 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 66;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_196);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA205_86 = input.LA(1);

                         
                        int index205_86 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 83;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_86);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA205_237 = input.LA(1);

                         
                        int index205_237 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_237);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA205_140 = input.LA(1);

                         
                        int index205_140 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_140);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA205_64 = input.LA(1);

                         
                        int index205_64 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_64);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA205_138 = input.LA(1);

                         
                        int index205_138 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_138);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA205_61 = input.LA(1);

                         
                        int index205_61 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_61);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA205_84 = input.LA(1);

                         
                        int index205_84 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 83;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_84);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA205_257 = input.LA(1);

                         
                        int index205_257 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 56;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_257);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA205_270 = input.LA(1);

                         
                        int index205_270 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_270);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA205_256 = input.LA(1);

                         
                        int index205_256 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 56;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_256);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA205_193 = input.LA(1);

                         
                        int index205_193 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_193);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA205_253 = input.LA(1);

                         
                        int index205_253 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_253);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA205_133 = input.LA(1);

                        s = -1;
                        if ( (LA205_133=='g') ) {s = 198;}

                        else if ( (LA205_133=='G') ) {s = 199;}

                        else if ( ((LA205_133>='\u0000' && LA205_133<='\t')||LA205_133=='\u000B'||(LA205_133>='\u000E' && LA205_133<='/')||(LA205_133>='1' && LA205_133<='3')||LA205_133=='5'||(LA205_133>='7' && LA205_133<='F')||(LA205_133>='H' && LA205_133<='f')||(LA205_133>='h' && LA205_133<='\uFFFF')) ) {s = 12;}

                        else if ( (LA205_133=='0') ) {s = 200;}

                        else if ( (LA205_133=='4'||LA205_133=='6') ) {s = 201;}

                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA205_198 = input.LA(1);

                         
                        int index205_198 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_198);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA205_199 = input.LA(1);

                         
                        int index205_199 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_199);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA205_182 = input.LA(1);

                         
                        int index205_182 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_182);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA205_287 = input.LA(1);

                         
                        int index205_287 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 83;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_287);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA205_436 = input.LA(1);

                         
                        int index205_436 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_436);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA205_435 = input.LA(1);

                         
                        int index205_435 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_435);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA205_320 = input.LA(1);

                         
                        int index205_320 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_320);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA205_15 = input.LA(1);

                         
                        int index205_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_15>='\t' && LA205_15<='\n')||(LA205_15>='\f' && LA205_15<='\r')||LA205_15==' ') && (synpred3_Css3())) {s = 56;}

                        else if ( (LA205_15=='m') ) {s = 57;}

                        else if ( (LA205_15=='\\') ) {s = 58;}

                        else if ( (LA205_15=='M') ) {s = 59;}

                        else s = 12;

                         
                        input.seek(index205_15);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA205_4 = input.LA(1);

                         
                        int index205_4 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_4>='\t' && LA205_4<='\n')||(LA205_4>='\f' && LA205_4<='\r')||LA205_4==' ') && (synpred3_Css3())) {s = 56;}

                        else if ( (LA205_4=='m') ) {s = 57;}

                        else if ( (LA205_4=='\\') ) {s = 58;}

                        else if ( (LA205_4=='M') ) {s = 59;}

                        else s = 12;

                         
                        input.seek(index205_4);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA205_288 = input.LA(1);

                         
                        int index205_288 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 83;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_288);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA205_17 = input.LA(1);

                         
                        int index205_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_17>='\t' && LA205_17<='\n')||(LA205_17>='\f' && LA205_17<='\r')||LA205_17==' ') && (synpred5_Css3())) {s = 66;}

                        else if ( (LA205_17=='n') ) {s = 67;}

                        else if ( (LA205_17=='\\') ) {s = 68;}

                        else if ( (LA205_17=='N') ) {s = 69;}

                        else s = 12;

                         
                        input.seek(index205_17);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA205_6 = input.LA(1);

                         
                        int index205_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_6>='\t' && LA205_6<='\n')||(LA205_6>='\f' && LA205_6<='\r')||LA205_6==' ') && (synpred5_Css3())) {s = 66;}

                        else if ( (LA205_6=='n') ) {s = 67;}

                        else if ( (LA205_6=='\\') ) {s = 68;}

                        else if ( (LA205_6=='N') ) {s = 69;}

                        else s = 12;

                         
                        input.seek(index205_6);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA205_433 = input.LA(1);

                         
                        int index205_433 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_433);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA205_409 = input.LA(1);

                         
                        int index205_409 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_409);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA205_410 = input.LA(1);

                         
                        int index205_410 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_410);
                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA205_330 = input.LA(1);

                         
                        int index205_330 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_330);
                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA205_331 = input.LA(1);

                         
                        int index205_331 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_331);
                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA205_416 = input.LA(1);

                         
                        int index205_416 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_416);
                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA205_417 = input.LA(1);

                         
                        int index205_417 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_417);
                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA205_362 = input.LA(1);

                         
                        int index205_362 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_362);
                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA205_377 = input.LA(1);

                         
                        int index205_377 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_377);
                        if ( s>=0 ) return s;
                        break;
                    case 39 : 
                        int LA205_378 = input.LA(1);

                         
                        int index205_378 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_378);
                        if ( s>=0 ) return s;
                        break;
                    case 40 : 
                        int LA205_426 = input.LA(1);

                         
                        int index205_426 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_426);
                        if ( s>=0 ) return s;
                        break;
                    case 41 : 
                        int LA205_85 = input.LA(1);

                        s = -1;
                        if ( (LA205_85=='z') ) {s = 147;}

                        else if ( (LA205_85=='Z') ) {s = 148;}

                        else if ( ((LA205_85>='\u0000' && LA205_85<='\t')||LA205_85=='\u000B'||(LA205_85>='\u000E' && LA205_85<='/')||(LA205_85>='1' && LA205_85<='4')||LA205_85=='6'||(LA205_85>='8' && LA205_85<='Y')||(LA205_85>='[' && LA205_85<='y')||(LA205_85>='{' && LA205_85<='\uFFFF')) ) {s = 12;}

                        else if ( (LA205_85=='0') ) {s = 149;}

                        else if ( (LA205_85=='5'||LA205_85=='7') ) {s = 150;}

                        if ( s>=0 ) return s;
                        break;
                    case 42 : 
                        int LA205_360 = input.LA(1);

                         
                        int index205_360 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_360);
                        if ( s>=0 ) return s;
                        break;
                    case 43 : 
                        int LA205_361 = input.LA(1);

                         
                        int index205_361 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_361);
                        if ( s>=0 ) return s;
                        break;
                    case 44 : 
                        int LA205_312 = input.LA(1);

                         
                        int index205_312 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_312);
                        if ( s>=0 ) return s;
                        break;
                    case 45 : 
                        int LA205_311 = input.LA(1);

                         
                        int index205_311 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_311);
                        if ( s>=0 ) return s;
                        break;
                    case 46 : 
                        int LA205_420 = input.LA(1);

                         
                        int index205_420 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_420);
                        if ( s>=0 ) return s;
                        break;
                    case 47 : 
                        int LA205_418 = input.LA(1);

                         
                        int index205_418 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_418);
                        if ( s>=0 ) return s;
                        break;
                    case 48 : 
                        int LA205_263 = input.LA(1);

                         
                        int index205_263 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_263);
                        if ( s>=0 ) return s;
                        break;
                    case 49 : 
                        int LA205_139 = input.LA(1);

                        s = -1;
                        if ( ((LA205_139>='\u0000' && LA205_139<='\t')||LA205_139=='\u000B'||(LA205_139>='\u000E' && LA205_139<='/')||(LA205_139>='1' && LA205_139<='3')||LA205_139=='5'||(LA205_139>='7' && LA205_139<='\uFFFF')) ) {s = 12;}

                        else if ( (LA205_139=='0') ) {s = 205;}

                        else if ( (LA205_139=='4'||LA205_139=='6') ) {s = 206;}

                        if ( s>=0 ) return s;
                        break;
                    case 50 : 
                        int LA205_308 = input.LA(1);

                         
                        int index205_308 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_308);
                        if ( s>=0 ) return s;
                        break;
                    case 51 : 
                        int LA205_128 = input.LA(1);

                         
                        int index205_128 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 66;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_128);
                        if ( s>=0 ) return s;
                        break;
                    case 52 : 
                        int LA205_127 = input.LA(1);

                         
                        int index205_127 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 66;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_127);
                        if ( s>=0 ) return s;
                        break;
                    case 53 : 
                        int LA205_221 = input.LA(1);

                         
                        int index205_221 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_221);
                        if ( s>=0 ) return s;
                        break;
                    case 54 : 
                        int LA205_220 = input.LA(1);

                         
                        int index205_220 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_220);
                        if ( s>=0 ) return s;
                        break;
                    case 55 : 
                        int LA205_374 = input.LA(1);

                         
                        int index205_374 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_374);
                        if ( s>=0 ) return s;
                        break;
                    case 56 : 
                        int LA205_19 = input.LA(1);

                         
                        int index205_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_19>='\t' && LA205_19<='\n')||(LA205_19>='\f' && LA205_19<='\r')||LA205_19==' ') && (synpred7_Css3())) {s = 74;}

                        else if ( (LA205_19=='a') ) {s = 75;}

                        else if ( (LA205_19=='\\') ) {s = 76;}

                        else if ( (LA205_19=='A') ) {s = 77;}

                        else s = 12;

                         
                        input.seek(index205_19);
                        if ( s>=0 ) return s;
                        break;
                    case 57 : 
                        int LA205_8 = input.LA(1);

                         
                        int index205_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_8>='\t' && LA205_8<='\n')||(LA205_8>='\f' && LA205_8<='\r')||LA205_8==' ') && (synpred7_Css3())) {s = 74;}

                        else if ( (LA205_8=='a') ) {s = 75;}

                        else if ( (LA205_8=='\\') ) {s = 76;}

                        else if ( (LA205_8=='A') ) {s = 77;}

                        else s = 12;

                         
                        input.seek(index205_8);
                        if ( s>=0 ) return s;
                        break;
                    case 58 : 
                        int LA205_251 = input.LA(1);

                         
                        int index205_251 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_251);
                        if ( s>=0 ) return s;
                        break;
                    case 59 : 
                        int LA205_80 = input.LA(1);

                         
                        int index205_80 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_80>='\t' && LA205_80<='\n')||(LA205_80>='\f' && LA205_80<='\r')||LA205_80==' ') && (synpred9_Css3())) {s = 83;}

                        else if ( (LA205_80=='z') ) {s = 84;}

                        else if ( (LA205_80=='\\') ) {s = 85;}

                        else if ( (LA205_80=='Z') ) {s = 86;}

                        else s = 12;

                         
                        input.seek(index205_80);
                        if ( s>=0 ) return s;
                        break;
                    case 60 : 
                        int LA205_82 = input.LA(1);

                         
                        int index205_82 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_82>='\t' && LA205_82<='\n')||(LA205_82>='\f' && LA205_82<='\r')||LA205_82==' ') && (synpred9_Css3())) {s = 83;}

                        else if ( (LA205_82=='z') ) {s = 84;}

                        else if ( (LA205_82=='\\') ) {s = 85;}

                        else if ( (LA205_82=='Z') ) {s = 86;}

                        else s = 12;

                         
                        input.seek(index205_82);
                        if ( s>=0 ) return s;
                        break;
                    case 61 : 
                        int LA205_357 = input.LA(1);

                         
                        int index205_357 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 83;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_357);
                        if ( s>=0 ) return s;
                        break;
                    case 62 : 
                        int LA205_30 = input.LA(1);

                         
                        int index205_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_30);
                        if ( s>=0 ) return s;
                        break;
                    case 63 : 
                        int LA205_356 = input.LA(1);

                         
                        int index205_356 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 83;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_356);
                        if ( s>=0 ) return s;
                        break;
                    case 64 : 
                        int LA205_28 = input.LA(1);

                         
                        int index205_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_28);
                        if ( s>=0 ) return s;
                        break;
                    case 65 : 
                        int LA205_398 = input.LA(1);

                         
                        int index205_398 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 66;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_398);
                        if ( s>=0 ) return s;
                        break;
                    case 66 : 
                        int LA205_20 = input.LA(1);

                         
                        int index205_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_20);
                        if ( s>=0 ) return s;
                        break;
                    case 67 : 
                        int LA205_186 = input.LA(1);

                         
                        int index205_186 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 56;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_186);
                        if ( s>=0 ) return s;
                        break;
                    case 68 : 
                        int LA205_397 = input.LA(1);

                         
                        int index205_397 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 66;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_397);
                        if ( s>=0 ) return s;
                        break;
                    case 69 : 
                        int LA205_9 = input.LA(1);

                         
                        int index205_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_9);
                        if ( s>=0 ) return s;
                        break;
                    case 70 : 
                        int LA205_406 = input.LA(1);

                         
                        int index205_406 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_406);
                        if ( s>=0 ) return s;
                        break;
                    case 71 : 
                        int LA205_187 = input.LA(1);

                         
                        int index205_187 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 56;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_187);
                        if ( s>=0 ) return s;
                        break;
                    case 72 : 
                        int LA205_394 = input.LA(1);

                         
                        int index205_394 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_394);
                        if ( s>=0 ) return s;
                        break;
                    case 73 : 
                        int LA205_183 = input.LA(1);

                         
                        int index205_183 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_183);
                        if ( s>=0 ) return s;
                        break;
                    case 74 : 
                        int LA205_393 = input.LA(1);

                         
                        int index205_393 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_393);
                        if ( s>=0 ) return s;
                        break;
                    case 75 : 
                        int LA205_222 = input.LA(1);

                         
                        int index205_222 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_222);
                        if ( s>=0 ) return s;
                        break;
                    case 76 : 
                        int LA205_53 = input.LA(1);

                         
                        int index205_53 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_53);
                        if ( s>=0 ) return s;
                        break;
                    case 77 : 
                        int LA205_76 = input.LA(1);

                        s = -1;
                        if ( ((LA205_76>='\u0000' && LA205_76<='\t')||LA205_76=='\u000B'||(LA205_76>='\u000E' && LA205_76<='/')||(LA205_76>='1' && LA205_76<='3')||LA205_76=='5'||(LA205_76>='7' && LA205_76<='\uFFFF')) ) {s = 12;}

                        else if ( (LA205_76=='0') ) {s = 141;}

                        else if ( (LA205_76=='4'||LA205_76=='6') ) {s = 142;}

                        if ( s>=0 ) return s;
                        break;
                    case 78 : 
                        int LA205_280 = input.LA(1);

                         
                        int index205_280 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_280);
                        if ( s>=0 ) return s;
                        break;
                    case 79 : 
                        int LA205_49 = input.LA(1);

                         
                        int index205_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_49);
                        if ( s>=0 ) return s;
                        break;
                    case 80 : 
                        int LA205_281 = input.LA(1);

                         
                        int index205_281 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_281);
                        if ( s>=0 ) return s;
                        break;
                    case 81 : 
                        int LA205_27 = input.LA(1);

                        s = -1;
                        if ( (LA205_27=='m') ) {s = 87;}

                        else if ( (LA205_27=='M') ) {s = 88;}

                        else if ( (LA205_27=='x') ) {s = 89;}

                        else if ( (LA205_27=='0') ) {s = 90;}

                        else if ( (LA205_27=='4'||LA205_27=='6') ) {s = 91;}

                        else if ( (LA205_27=='X') ) {s = 92;}

                        else if ( ((LA205_27>='\u0000' && LA205_27<='\t')||LA205_27=='\u000B'||(LA205_27>='\u000E' && LA205_27<='/')||(LA205_27>='1' && LA205_27<='3')||(LA205_27>='8' && LA205_27<='L')||(LA205_27>='N' && LA205_27<='W')||(LA205_27>='Y' && LA205_27<='l')||(LA205_27>='n' && LA205_27<='w')||(LA205_27>='y' && LA205_27<='\uFFFF')) ) {s = 12;}

                        else if ( (LA205_27=='5'||LA205_27=='7') ) {s = 93;}

                        if ( s>=0 ) return s;
                        break;
                    case 82 : 
                        int LA205_441 = input.LA(1);

                         
                        int index205_441 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_441);
                        if ( s>=0 ) return s;
                        break;
                    case 83 : 
                        int LA205_427 = input.LA(1);

                         
                        int index205_427 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 66;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_427);
                        if ( s>=0 ) return s;
                        break;
                    case 84 : 
                        int LA205_29 = input.LA(1);

                         
                        int index205_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_29);
                        if ( s>=0 ) return s;
                        break;
                    case 85 : 
                        int LA205_26 = input.LA(1);

                         
                        int index205_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_26);
                        if ( s>=0 ) return s;
                        break;
                    case 86 : 
                        int LA205_215 = input.LA(1);

                         
                        int index205_215 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 83;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_215);
                        if ( s>=0 ) return s;
                        break;
                    case 87 : 
                        int LA205_428 = input.LA(1);

                         
                        int index205_428 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 66;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_428);
                        if ( s>=0 ) return s;
                        break;
                    case 88 : 
                        int LA205_216 = input.LA(1);

                         
                        int index205_216 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 83;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_216);
                        if ( s>=0 ) return s;
                        break;
                    case 89 : 
                        int LA205_7 = input.LA(1);

                         
                        int index205_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_7>='\t' && LA205_7<='\n')||(LA205_7>='\f' && LA205_7<='\r')||LA205_7==' ') && (synpred6_Css3())) {s = 70;}

                        else if ( (LA205_7=='e') ) {s = 71;}

                        else if ( (LA205_7=='\\') ) {s = 72;}

                        else if ( (LA205_7=='E') ) {s = 73;}

                        else s = 12;

                         
                        input.seek(index205_7);
                        if ( s>=0 ) return s;
                        break;
                    case 90 : 
                        int LA205_18 = input.LA(1);

                         
                        int index205_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_18>='\t' && LA205_18<='\n')||(LA205_18>='\f' && LA205_18<='\r')||LA205_18==' ') && (synpred6_Css3())) {s = 70;}

                        else if ( (LA205_18=='e') ) {s = 71;}

                        else if ( (LA205_18=='\\') ) {s = 72;}

                        else if ( (LA205_18=='E') ) {s = 73;}

                        else s = 12;

                         
                        input.seek(index205_18);
                        if ( s>=0 ) return s;
                        break;
                    case 91 : 
                        int LA205_349 = input.LA(1);

                         
                        int index205_349 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_349);
                        if ( s>=0 ) return s;
                        break;
                    case 92 : 
                        int LA205_350 = input.LA(1);

                         
                        int index205_350 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_350);
                        if ( s>=0 ) return s;
                        break;
                    case 93 : 
                        int LA205_52 = input.LA(1);

                         
                        int index205_52 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_52);
                        if ( s>=0 ) return s;
                        break;
                    case 94 : 
                        int LA205_55 = input.LA(1);

                         
                        int index205_55 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_55);
                        if ( s>=0 ) return s;
                        break;
                    case 95 : 
                        int LA205_440 = input.LA(1);

                         
                        int index205_440 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_440);
                        if ( s>=0 ) return s;
                        break;
                    case 96 : 
                        int LA205_294 = input.LA(1);

                         
                        int index205_294 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_294);
                        if ( s>=0 ) return s;
                        break;
                    case 97 : 
                        int LA205_89 = input.LA(1);

                         
                        int index205_89 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_89);
                        if ( s>=0 ) return s;
                        break;
                    case 98 : 
                        int LA205_261 = input.LA(1);

                         
                        int index205_261 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_261);
                        if ( s>=0 ) return s;
                        break;
                    case 99 : 
                        int LA205_262 = input.LA(1);

                         
                        int index205_262 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_262);
                        if ( s>=0 ) return s;
                        break;
                    case 100 : 
                        int LA205_92 = input.LA(1);

                         
                        int index205_92 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_92);
                        if ( s>=0 ) return s;
                        break;
                    case 101 : 
                        int LA205_293 = input.LA(1);

                         
                        int index205_293 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_293);
                        if ( s>=0 ) return s;
                        break;
                    case 102 : 
                        int LA205_292 = input.LA(1);

                         
                        int index205_292 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_292);
                        if ( s>=0 ) return s;
                        break;
                    case 103 : 
                        int LA205_14 = input.LA(1);

                         
                        int index205_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_14>='\t' && LA205_14<='\n')||(LA205_14>='\f' && LA205_14<='\r')||LA205_14==' ') && (synpred2_Css3())) {s = 48;}

                        else if ( (LA205_14=='x') ) {s = 49;}

                        else if ( (LA205_14=='\\') ) {s = 50;}

                        else if ( (LA205_14=='t') ) {s = 51;}

                        else if ( (LA205_14=='c') ) {s = 52;}

                        else if ( (LA205_14=='X') ) {s = 53;}

                        else if ( (LA205_14=='T') ) {s = 54;}

                        else if ( (LA205_14=='C') ) {s = 55;}

                        else s = 12;

                         
                        input.seek(index205_14);
                        if ( s>=0 ) return s;
                        break;
                    case 104 : 
                        int LA205_21 = input.LA(1);

                         
                        int index205_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_21>='\t' && LA205_21<='\n')||(LA205_21>='\f' && LA205_21<='\r')||LA205_21==' ') && (synpred9_Css3())) {s = 79;}

                        else if ( (LA205_21=='h') ) {s = 80;}

                        else if ( (LA205_21=='\\') ) {s = 81;}

                        else if ( (LA205_21=='H') ) {s = 82;}

                        else s = 12;

                         
                        input.seek(index205_21);
                        if ( s>=0 ) return s;
                        break;
                    case 105 : 
                        int LA205_3 = input.LA(1);

                         
                        int index205_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_3>='\t' && LA205_3<='\n')||(LA205_3>='\f' && LA205_3<='\r')||LA205_3==' ') && (synpred2_Css3())) {s = 48;}

                        else if ( (LA205_3=='x') ) {s = 49;}

                        else if ( (LA205_3=='\\') ) {s = 50;}

                        else if ( (LA205_3=='t') ) {s = 51;}

                        else if ( (LA205_3=='c') ) {s = 52;}

                        else if ( (LA205_3=='X') ) {s = 53;}

                        else if ( (LA205_3=='T') ) {s = 54;}

                        else if ( (LA205_3=='C') ) {s = 55;}

                        else s = 12;

                         
                        input.seek(index205_3);
                        if ( s>=0 ) return s;
                        break;
                    case 106 : 
                        int LA205_10 = input.LA(1);

                         
                        int index205_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_10>='\t' && LA205_10<='\n')||(LA205_10>='\f' && LA205_10<='\r')||LA205_10==' ') && (synpred9_Css3())) {s = 79;}

                        else if ( (LA205_10=='h') ) {s = 80;}

                        else if ( (LA205_10=='\\') ) {s = 81;}

                        else if ( (LA205_10=='H') ) {s = 82;}

                        else s = 12;

                         
                        input.seek(index205_10);
                        if ( s>=0 ) return s;
                        break;
                    case 107 : 
                        int LA205_110 = input.LA(1);

                         
                        int index205_110 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_110);
                        if ( s>=0 ) return s;
                        break;
                    case 108 : 
                        int LA205_430 = input.LA(1);

                         
                        int index205_430 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_430);
                        if ( s>=0 ) return s;
                        break;
                    case 109 : 
                        int LA205_109 = input.LA(1);

                         
                        int index205_109 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_109);
                        if ( s>=0 ) return s;
                        break;
                    case 110 : 
                        int LA205_58 = input.LA(1);

                        s = -1;
                        if ( (LA205_58=='m') ) {s = 116;}

                        else if ( (LA205_58=='M') ) {s = 117;}

                        else if ( ((LA205_58>='\u0000' && LA205_58<='\t')||LA205_58=='\u000B'||(LA205_58>='\u000E' && LA205_58<='/')||(LA205_58>='1' && LA205_58<='3')||LA205_58=='5'||(LA205_58>='7' && LA205_58<='L')||(LA205_58>='N' && LA205_58<='l')||(LA205_58>='n' && LA205_58<='\uFFFF')) ) {s = 12;}

                        else if ( (LA205_58=='0') ) {s = 118;}

                        else if ( (LA205_58=='4'||LA205_58=='6') ) {s = 119;}

                        if ( s>=0 ) return s;
                        break;
                    case 111 : 
                        int LA205_439 = input.LA(1);

                         
                        int index205_439 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 83;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_439);
                        if ( s>=0 ) return s;
                        break;
                    case 112 : 
                        int LA205_389 = input.LA(1);

                         
                        int index205_389 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 56;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_389);
                        if ( s>=0 ) return s;
                        break;
                    case 113 : 
                        int LA205_390 = input.LA(1);

                         
                        int index205_390 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 56;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_390);
                        if ( s>=0 ) return s;
                        break;
                    case 114 : 
                        int LA205_438 = input.LA(1);

                         
                        int index205_438 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 83;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_438);
                        if ( s>=0 ) return s;
                        break;
                    case 115 : 
                        int LA205_401 = input.LA(1);

                         
                        int index205_401 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_401);
                        if ( s>=0 ) return s;
                        break;
                    case 116 : 
                        int LA205_382 = input.LA(1);

                         
                        int index205_382 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_382);
                        if ( s>=0 ) return s;
                        break;
                    case 117 : 
                        int LA205_381 = input.LA(1);

                         
                        int index205_381 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_381);
                        if ( s>=0 ) return s;
                        break;
                    case 118 : 
                        int LA205_387 = input.LA(1);

                         
                        int index205_387 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_387);
                        if ( s>=0 ) return s;
                        break;
                    case 119 : 
                        int LA205_22 = input.LA(1);

                         
                        int index205_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_22>='\t' && LA205_22<='\n')||(LA205_22>='\f' && LA205_22<='\r')||LA205_22==' ') && (synpred9_Css3())) {s = 83;}

                        else if ( (LA205_22=='z') ) {s = 84;}

                        else if ( (LA205_22=='\\') ) {s = 85;}

                        else if ( (LA205_22=='Z') ) {s = 86;}

                        else s = 12;

                         
                        input.seek(index205_22);
                        if ( s>=0 ) return s;
                        break;
                    case 120 : 
                        int LA205_11 = input.LA(1);

                         
                        int index205_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_11>='\t' && LA205_11<='\n')||(LA205_11>='\f' && LA205_11<='\r')||LA205_11==' ') && (synpred9_Css3())) {s = 83;}

                        else if ( (LA205_11=='z') ) {s = 84;}

                        else if ( (LA205_11=='\\') ) {s = 85;}

                        else if ( (LA205_11=='Z') ) {s = 86;}

                        else s = 12;

                         
                        input.seek(index205_11);
                        if ( s>=0 ) return s;
                        break;
                    case 121 : 
                        int LA205_321 = input.LA(1);

                         
                        int index205_321 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_321);
                        if ( s>=0 ) return s;
                        break;
                    case 122 : 
                        int LA205_345 = input.LA(1);

                         
                        int index205_345 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_345);
                        if ( s>=0 ) return s;
                        break;
                    case 123 : 
                        int LA205_336 = input.LA(1);

                         
                        int index205_336 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 66;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_336);
                        if ( s>=0 ) return s;
                        break;
                    case 124 : 
                        int LA205_335 = input.LA(1);

                         
                        int index205_335 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 66;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_335);
                        if ( s>=0 ) return s;
                        break;
                    case 125 : 
                        int LA205_316 = input.LA(1);

                         
                        int index205_316 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_316);
                        if ( s>=0 ) return s;
                        break;
                    case 126 : 
                        int LA205_315 = input.LA(1);

                         
                        int index205_315 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_315);
                        if ( s>=0 ) return s;
                        break;
                    case 127 : 
                        int LA205_67 = input.LA(1);

                         
                        int index205_67 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 66;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_67);
                        if ( s>=0 ) return s;
                        break;
                    case 128 : 
                        int LA205_154 = input.LA(1);

                         
                        int index205_154 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_154);
                        if ( s>=0 ) return s;
                        break;
                    case 129 : 
                        int LA205_69 = input.LA(1);

                         
                        int index205_69 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 66;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_69);
                        if ( s>=0 ) return s;
                        break;
                    case 130 : 
                        int LA205_155 = input.LA(1);

                         
                        int index205_155 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_155);
                        if ( s>=0 ) return s;
                        break;
                    case 131 : 
                        int LA205_156 = input.LA(1);

                         
                        int index205_156 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_156);
                        if ( s>=0 ) return s;
                        break;
                    case 132 : 
                        int LA205_148 = input.LA(1);

                         
                        int index205_148 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 83;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_148);
                        if ( s>=0 ) return s;
                        break;
                    case 133 : 
                        int LA205_147 = input.LA(1);

                         
                        int index205_147 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 83;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_147);
                        if ( s>=0 ) return s;
                        break;
                    case 134 : 
                        int LA205_395 = input.LA(1);

                         
                        int index205_395 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_395);
                        if ( s>=0 ) return s;
                        break;
                    case 135 : 
                        int LA205_42 = input.LA(1);

                         
                        int index205_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_42);
                        if ( s>=0 ) return s;
                        break;
                    case 136 : 
                        int LA205_181 = input.LA(1);

                         
                        int index205_181 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_181);
                        if ( s>=0 ) return s;
                        break;
                    case 137 : 
                        int LA205_43 = input.LA(1);

                         
                        int index205_43 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_43);
                        if ( s>=0 ) return s;
                        break;
                    case 138 : 
                        int LA205_171 = input.LA(1);

                         
                        int index205_171 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_171);
                        if ( s>=0 ) return s;
                        break;
                    case 139 : 
                        int LA205_1 = input.LA(1);

                         
                        int index205_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_1>='\t' && LA205_1<='\n')||(LA205_1>='\f' && LA205_1<='\r')||LA205_1==' ') && (synpred1_Css3())) {s = 25;}

                        else if ( (LA205_1=='m') ) {s = 26;}

                        else if ( (LA205_1=='\\') ) {s = 27;}

                        else if ( (LA205_1=='x') ) {s = 28;}

                        else if ( (LA205_1=='M') ) {s = 29;}

                        else if ( (LA205_1=='X') ) {s = 30;}

                        else s = 12;

                         
                        input.seek(index205_1);
                        if ( s>=0 ) return s;
                        break;
                    case 140 : 
                        int LA205_13 = input.LA(1);

                         
                        int index205_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_13>='\t' && LA205_13<='\n')||(LA205_13>='\f' && LA205_13<='\r')||LA205_13==' ') && (synpred1_Css3())) {s = 25;}

                        else if ( (LA205_13=='m') ) {s = 26;}

                        else if ( (LA205_13=='\\') ) {s = 27;}

                        else if ( (LA205_13=='x') ) {s = 28;}

                        else if ( (LA205_13=='M') ) {s = 29;}

                        else if ( (LA205_13=='X') ) {s = 30;}

                        else s = 12;

                         
                        input.seek(index205_13);
                        if ( s>=0 ) return s;
                        break;
                    case 141 : 
                        int LA205_75 = input.LA(1);

                         
                        int index205_75 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_75>='\t' && LA205_75<='\n')||(LA205_75>='\f' && LA205_75<='\r')||LA205_75==' ') && (synpred7_Css3())) {s = 137;}

                        else if ( (LA205_75=='d') ) {s = 138;}

                        else if ( (LA205_75=='\\') ) {s = 139;}

                        else if ( (LA205_75=='D') ) {s = 140;}

                        else s = 12;

                         
                        input.seek(index205_75);
                        if ( s>=0 ) return s;
                        break;
                    case 142 : 
                        int LA205_77 = input.LA(1);

                         
                        int index205_77 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_77>='\t' && LA205_77<='\n')||(LA205_77>='\f' && LA205_77<='\r')||LA205_77==' ') && (synpred7_Css3())) {s = 137;}

                        else if ( (LA205_77=='d') ) {s = 138;}

                        else if ( (LA205_77=='\\') ) {s = 139;}

                        else if ( (LA205_77=='D') ) {s = 140;}

                        else s = 12;

                         
                        input.seek(index205_77);
                        if ( s>=0 ) return s;
                        break;
                    case 143 : 
                        int LA205_50 = input.LA(1);

                        s = -1;
                        if ( (LA205_50=='x') ) {s = 109;}

                        else if ( (LA205_50=='X') ) {s = 110;}

                        else if ( (LA205_50=='t') ) {s = 111;}

                        else if ( (LA205_50=='0') ) {s = 112;}

                        else if ( (LA205_50=='5'||LA205_50=='7') ) {s = 113;}

                        else if ( (LA205_50=='T') ) {s = 114;}

                        else if ( ((LA205_50>='\u0000' && LA205_50<='\t')||LA205_50=='\u000B'||(LA205_50>='\u000E' && LA205_50<='/')||(LA205_50>='1' && LA205_50<='3')||(LA205_50>='8' && LA205_50<='S')||(LA205_50>='U' && LA205_50<='W')||(LA205_50>='Y' && LA205_50<='s')||(LA205_50>='u' && LA205_50<='w')||(LA205_50>='y' && LA205_50<='\uFFFF')) ) {s = 12;}

                        else if ( (LA205_50=='4'||LA205_50=='6') ) {s = 115;}

                        if ( s>=0 ) return s;
                        break;
                    case 144 : 
                        int LA205_51 = input.LA(1);

                         
                        int index205_51 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_51);
                        if ( s>=0 ) return s;
                        break;
                    case 145 : 
                        int LA205_386 = input.LA(1);

                         
                        int index205_386 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_386);
                        if ( s>=0 ) return s;
                        break;
                    case 146 : 
                        int LA205_421 = input.LA(1);

                         
                        int index205_421 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_421);
                        if ( s>=0 ) return s;
                        break;
                    case 147 : 
                        int LA205_54 = input.LA(1);

                         
                        int index205_54 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_54);
                        if ( s>=0 ) return s;
                        break;
                    case 148 : 
                        int LA205_423 = input.LA(1);

                         
                        int index205_423 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 56;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_423);
                        if ( s>=0 ) return s;
                        break;
                    case 149 : 
                        int LA205_191 = input.LA(1);

                         
                        int index205_191 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_191);
                        if ( s>=0 ) return s;
                        break;
                    case 150 : 
                        int LA205_81 = input.LA(1);

                        s = -1;
                        if ( (LA205_81=='h') ) {s = 143;}

                        else if ( (LA205_81=='H') ) {s = 144;}

                        else if ( ((LA205_81>='\u0000' && LA205_81<='\t')||LA205_81=='\u000B'||(LA205_81>='\u000E' && LA205_81<='/')||(LA205_81>='1' && LA205_81<='3')||LA205_81=='5'||(LA205_81>='7' && LA205_81<='G')||(LA205_81>='I' && LA205_81<='g')||(LA205_81>='i' && LA205_81<='\uFFFF')) ) {s = 12;}

                        else if ( (LA205_81=='0') ) {s = 145;}

                        else if ( (LA205_81=='4'||LA205_81=='6') ) {s = 146;}

                        if ( s>=0 ) return s;
                        break;
                    case 151 : 
                        int LA205_192 = input.LA(1);

                         
                        int index205_192 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_192);
                        if ( s>=0 ) return s;
                        break;
                    case 152 : 
                        int LA205_422 = input.LA(1);

                         
                        int index205_422 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 56;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_422);
                        if ( s>=0 ) return s;
                        break;
                    case 153 : 
                        int LA205_252 = input.LA(1);

                         
                        int index205_252 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_252);
                        if ( s>=0 ) return s;
                        break;
                    case 154 : 
                        int LA205_325 = input.LA(1);

                         
                        int index205_325 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 56;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_325);
                        if ( s>=0 ) return s;
                        break;
                    case 155 : 
                        int LA205_326 = input.LA(1);

                         
                        int index205_326 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 56;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_326);
                        if ( s>=0 ) return s;
                        break;
                    case 156 : 
                        int LA205_175 = input.LA(1);

                         
                        int index205_175 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_175);
                        if ( s>=0 ) return s;
                        break;
                    case 157 : 
                        int LA205_174 = input.LA(1);

                         
                        int index205_174 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_174);
                        if ( s>=0 ) return s;
                        break;
                    case 158 : 
                        int LA205_266 = input.LA(1);

                         
                        int index205_266 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 66;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_266);
                        if ( s>=0 ) return s;
                        break;
                    case 159 : 
                        int LA205_267 = input.LA(1);

                         
                        int index205_267 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 66;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_267);
                        if ( s>=0 ) return s;
                        break;
                    case 160 : 
                        int LA205_120 = input.LA(1);

                         
                        int index205_120 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_120);
                        if ( s>=0 ) return s;
                        break;
                    case 161 : 
                        int LA205_16 = input.LA(1);

                         
                        int index205_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_16>='\t' && LA205_16<='\n')||(LA205_16>='\f' && LA205_16<='\r')||LA205_16==' ') && (synpred4_Css3())) {s = 60;}

                        else if ( (LA205_16=='m') ) {s = 61;}

                        else if ( (LA205_16=='\\') ) {s = 62;}

                        else if ( (LA205_16=='s') ) {s = 63;}

                        else if ( (LA205_16=='M') ) {s = 64;}

                        else if ( (LA205_16=='S') ) {s = 65;}

                        else s = 12;

                         
                        input.seek(index205_16);
                        if ( s>=0 ) return s;
                        break;
                    case 162 : 
                        int LA205_121 = input.LA(1);

                         
                        int index205_121 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_121);
                        if ( s>=0 ) return s;
                        break;
                    case 163 : 
                        int LA205_419 = input.LA(1);

                         
                        int index205_419 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_419);
                        if ( s>=0 ) return s;
                        break;
                    case 164 : 
                        int LA205_5 = input.LA(1);

                         
                        int index205_5 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_5>='\t' && LA205_5<='\n')||(LA205_5>='\f' && LA205_5<='\r')||LA205_5==' ') && (synpred4_Css3())) {s = 60;}

                        else if ( (LA205_5=='m') ) {s = 61;}

                        else if ( (LA205_5=='\\') ) {s = 62;}

                        else if ( (LA205_5=='s') ) {s = 63;}

                        else if ( (LA205_5=='M') ) {s = 64;}

                        else if ( (LA205_5=='S') ) {s = 65;}

                        else s = 12;

                         
                        input.seek(index205_5);
                        if ( s>=0 ) return s;
                        break;
                    case 165 : 
                        int LA205_2 = input.LA(1);

                        s = -1;
                        if ( (LA205_2=='p') ) {s = 31;}

                        else if ( (LA205_2=='0') ) {s = 32;}

                        else if ( (LA205_2=='4'||LA205_2=='6') ) {s = 33;}

                        else if ( (LA205_2=='P') ) {s = 34;}

                        else if ( (LA205_2=='m') ) {s = 35;}

                        else if ( (LA205_2=='5'||LA205_2=='7') ) {s = 36;}

                        else if ( (LA205_2=='M') ) {s = 37;}

                        else if ( (LA205_2=='i') ) {s = 38;}

                        else if ( (LA205_2=='I') ) {s = 39;}

                        else if ( (LA205_2=='r') ) {s = 40;}

                        else if ( (LA205_2=='R') ) {s = 41;}

                        else if ( (LA205_2=='s') ) {s = 42;}

                        else if ( (LA205_2=='S') ) {s = 43;}

                        else if ( (LA205_2=='k') ) {s = 44;}

                        else if ( (LA205_2=='K') ) {s = 45;}

                        else if ( (LA205_2=='h') ) {s = 46;}

                        else if ( (LA205_2=='H') ) {s = 47;}

                        else if ( ((LA205_2>='\u0000' && LA205_2<='\t')||LA205_2=='\u000B'||(LA205_2>='\u000E' && LA205_2<='/')||(LA205_2>='1' && LA205_2<='3')||(LA205_2>='8' && LA205_2<='G')||LA205_2=='J'||LA205_2=='L'||(LA205_2>='N' && LA205_2<='O')||LA205_2=='Q'||(LA205_2>='T' && LA205_2<='g')||LA205_2=='j'||LA205_2=='l'||(LA205_2>='n' && LA205_2<='o')||LA205_2=='q'||(LA205_2>='t' && LA205_2<='\uFFFF')) ) {s = 12;}

                        if ( s>=0 ) return s;
                        break;
                    case 166 : 
                        int LA205_276 = input.LA(1);

                         
                        int index205_276 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_276);
                        if ( s>=0 ) return s;
                        break;
                    case 167 : 
                        int LA205_132 = input.LA(1);

                         
                        int index205_132 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_132);
                        if ( s>=0 ) return s;
                        break;
                    case 168 : 
                        int LA205_247 = input.LA(1);

                         
                        int index205_247 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_247);
                        if ( s>=0 ) return s;
                        break;
                    case 169 : 
                        int LA205_246 = input.LA(1);

                         
                        int index205_246 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 137;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_246);
                        if ( s>=0 ) return s;
                        break;
                    case 170 : 
                        int LA205_134 = input.LA(1);

                         
                        int index205_134 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_134);
                        if ( s>=0 ) return s;
                        break;
                    case 171 : 
                        int LA205_385 = input.LA(1);

                         
                        int index205_385 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_385);
                        if ( s>=0 ) return s;
                        break;
                    case 172 : 
                        int LA205_339 = input.LA(1);

                         
                        int index205_339 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 131;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_339);
                        if ( s>=0 ) return s;
                        break;
                    case 173 : 
                        int LA205_425 = input.LA(1);

                         
                        int index205_425 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_425);
                        if ( s>=0 ) return s;
                        break;
                    case 174 : 
                        int LA205_424 = input.LA(1);

                         
                        int index205_424 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_424);
                        if ( s>=0 ) return s;
                        break;
                    case 175 : 
                        int LA205_108 = input.LA(1);

                         
                        int index205_108 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_108);
                        if ( s>=0 ) return s;
                        break;
                    case 176 : 
                        int LA205_125 = input.LA(1);

                         
                        int index205_125 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_125);
                        if ( s>=0 ) return s;
                        break;
                    case 177 : 
                        int LA205_122 = input.LA(1);

                         
                        int index205_122 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_122);
                        if ( s>=0 ) return s;
                        break;
                    case 178 : 
                        int LA205_68 = input.LA(1);

                        s = -1;
                        if ( (LA205_68=='n') ) {s = 127;}

                        else if ( (LA205_68=='N') ) {s = 128;}

                        else if ( ((LA205_68>='\u0000' && LA205_68<='\t')||LA205_68=='\u000B'||(LA205_68>='\u000E' && LA205_68<='/')||(LA205_68>='1' && LA205_68<='3')||LA205_68=='5'||(LA205_68>='7' && LA205_68<='M')||(LA205_68>='O' && LA205_68<='m')||(LA205_68>='o' && LA205_68<='\uFFFF')) ) {s = 12;}

                        else if ( (LA205_68=='0') ) {s = 129;}

                        else if ( (LA205_68=='4'||LA205_68=='6') ) {s = 130;}

                        if ( s>=0 ) return s;
                        break;
                    case 179 : 
                        int LA205_65 = input.LA(1);

                         
                        int index205_65 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_65);
                        if ( s>=0 ) return s;
                        break;
                    case 180 : 
                        int LA205_63 = input.LA(1);

                         
                        int index205_63 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_63);
                        if ( s>=0 ) return s;
                        break;
                    case 181 : 
                        int LA205_88 = input.LA(1);

                         
                        int index205_88 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_88);
                        if ( s>=0 ) return s;
                        break;
                    case 182 : 
                        int LA205_87 = input.LA(1);

                         
                        int index205_87 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 25;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_87);
                        if ( s>=0 ) return s;
                        break;
                    case 183 : 
                        int LA205_114 = input.LA(1);

                         
                        int index205_114 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_114);
                        if ( s>=0 ) return s;
                        break;
                    case 184 : 
                        int LA205_111 = input.LA(1);

                         
                        int index205_111 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_111);
                        if ( s>=0 ) return s;
                        break;
                    case 185 : 
                        int LA205_62 = input.LA(1);

                        s = -1;
                        if ( (LA205_62=='m') ) {s = 120;}

                        else if ( (LA205_62=='M') ) {s = 121;}

                        else if ( (LA205_62=='s') ) {s = 122;}

                        else if ( (LA205_62=='0') ) {s = 123;}

                        else if ( (LA205_62=='4'||LA205_62=='6') ) {s = 124;}

                        else if ( (LA205_62=='S') ) {s = 125;}

                        else if ( ((LA205_62>='\u0000' && LA205_62<='\t')||LA205_62=='\u000B'||(LA205_62>='\u000E' && LA205_62<='/')||(LA205_62>='1' && LA205_62<='3')||(LA205_62>='8' && LA205_62<='L')||(LA205_62>='N' && LA205_62<='R')||(LA205_62>='T' && LA205_62<='l')||(LA205_62>='n' && LA205_62<='r')||(LA205_62>='t' && LA205_62<='\uFFFF')) ) {s = 12;}

                        else if ( (LA205_62=='5'||LA205_62=='7') ) {s = 126;}

                        if ( s>=0 ) return s;
                        break;
                    case 186 : 
                        int LA205_332 = input.LA(1);

                         
                        int index205_332 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 60;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_332);
                        if ( s>=0 ) return s;
                        break;
                    case 187 : 
                        int LA205_117 = input.LA(1);

                         
                        int index205_117 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 56;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_117);
                        if ( s>=0 ) return s;
                        break;
                    case 188 : 
                        int LA205_116 = input.LA(1);

                         
                        int index205_116 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 56;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_116);
                        if ( s>=0 ) return s;
                        break;
                    case 189 : 
                        int LA205_59 = input.LA(1);

                         
                        int index205_59 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 56;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_59);
                        if ( s>=0 ) return s;
                        break;
                    case 190 : 
                        int LA205_73 = input.LA(1);

                         
                        int index205_73 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_73>='\t' && LA205_73<='\n')||(LA205_73>='\f' && LA205_73<='\r')||LA205_73==' ') && (synpred6_Css3())) {s = 131;}

                        else if ( (LA205_73=='g') ) {s = 132;}

                        else if ( (LA205_73=='\\') ) {s = 133;}

                        else if ( (LA205_73=='G') ) {s = 134;}

                        else s = 12;

                         
                        input.seek(index205_73);
                        if ( s>=0 ) return s;
                        break;
                    case 191 : 
                        int LA205_240 = input.LA(1);

                         
                        int index205_240 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_240);
                        if ( s>=0 ) return s;
                        break;
                    case 192 : 
                        int LA205_71 = input.LA(1);

                         
                        int index205_71 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA205_71>='\t' && LA205_71<='\n')||(LA205_71>='\f' && LA205_71<='\r')||LA205_71==' ') && (synpred6_Css3())) {s = 131;}

                        else if ( (LA205_71=='g') ) {s = 132;}

                        else if ( (LA205_71=='\\') ) {s = 133;}

                        else if ( (LA205_71=='G') ) {s = 134;}

                        else s = 12;

                         
                        input.seek(index205_71);
                        if ( s>=0 ) return s;
                        break;
                    case 193 : 
                        int LA205_241 = input.LA(1);

                         
                        int index205_241 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_241);
                        if ( s>=0 ) return s;
                        break;
                    case 194 : 
                        int LA205_57 = input.LA(1);

                         
                        int index205_57 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 56;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_57);
                        if ( s>=0 ) return s;
                        break;
                    case 195 : 
                        int LA205_322 = input.LA(1);

                         
                        int index205_322 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 48;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index205_322);
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
    static final String DFA202_eotS =
        "\12\uffff";
    static final String DFA202_eofS =
        "\12\uffff";
    static final String DFA202_minS =
        "\1\103\1\uffff\1\60\2\uffff\1\60\1\64\2\60\1\64";
    static final String DFA202_maxS =
        "\1\170\1\uffff\1\170\2\uffff\1\67\1\70\3\67";
    static final String DFA202_acceptS =
        "\1\uffff\1\1\1\uffff\1\2\1\3\5\uffff";
    static final String DFA202_specialS =
        "\12\uffff}>";
    static final String[] DFA202_transitionS = {
            "\1\4\20\uffff\1\3\3\uffff\1\1\3\uffff\1\2\6\uffff\1\4\20\uffff"+
            "\1\3\3\uffff\1\1",
            "",
            "\1\5\3\uffff\1\4\1\6\1\4\1\6\34\uffff\1\3\3\uffff\1\1\33\uffff"+
            "\1\3\3\uffff\1\1",
            "",
            "",
            "\1\7\3\uffff\1\4\1\6\1\4\1\6",
            "\1\3\3\uffff\1\1",
            "\1\10\3\uffff\1\4\1\6\1\4\1\6",
            "\1\11\3\uffff\1\4\1\6\1\4\1\6",
            "\1\4\1\6\1\4\1\6"
    };

    static final short[] DFA202_eot = DFA.unpackEncodedString(DFA202_eotS);
    static final short[] DFA202_eof = DFA.unpackEncodedString(DFA202_eofS);
    static final char[] DFA202_min = DFA.unpackEncodedStringToUnsignedChars(DFA202_minS);
    static final char[] DFA202_max = DFA.unpackEncodedStringToUnsignedChars(DFA202_maxS);
    static final short[] DFA202_accept = DFA.unpackEncodedString(DFA202_acceptS);
    static final short[] DFA202_special = DFA.unpackEncodedString(DFA202_specialS);
    static final short[][] DFA202_transition;

    static {
        int numStates = DFA202_transitionS.length;
        DFA202_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA202_transition[i] = DFA.unpackEncodedString(DFA202_transitionS[i]);
        }
    }

    class DFA202 extends DFA {

        public DFA202(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 202;
            this.eot = DFA202_eot;
            this.eof = DFA202_eof;
            this.min = DFA202_min;
            this.max = DFA202_max;
            this.accept = DFA202_accept;
            this.special = DFA202_special;
            this.transition = DFA202_transition;
        }
        public String getDescription() {
            return "808:17: ( X | T | C )";
        }
    }
    static final String DFA212_eotS =
        "\1\uffff\1\40\1\42\1\uffff\1\44\1\46\15\uffff\1\47\2\uffff\2\25"+
        "\20\uffff\2\25\2\uffff\4\25\5\uffff\2\25\1\uffff\7\25\2\uffff\12"+
        "\25\1\uffff\12\25\1\uffff\11\25\1\uffff\15\25";
    static final String DFA212_eofS =
        "\156\uffff";
    static final String DFA212_minS =
        "\1\11\1\75\1\52\1\uffff\1\55\1\75\15\uffff\1\60\2\uffff\2\11\1\0"+
        "\1\uffff\1\111\15\uffff\2\11\1\0\1\uffff\1\122\1\60\1\122\1\65\2"+
        "\uffff\1\60\2\uffff\2\11\1\0\1\114\1\60\1\114\1\62\1\60\1\65\1\122"+
        "\1\60\1\71\1\50\1\60\1\50\1\103\1\60\1\62\1\114\1\60\1\65\1\122"+
        "\2\60\1\103\2\50\1\60\1\62\1\114\2\65\1\122\2\60\1\103\2\50\1\65"+
        "\1\62\1\114\1\65\1\122\2\64\1\103\2\50\1\62\1\114\1\122\1\103\2"+
        "\50\1\114\2\50";
    static final String DFA212_maxS =
        "\1\uffff\1\75\1\52\1\uffff\1\uffff\1\75\15\uffff\1\71\2\uffff\2"+
        "\162\1\uffff\1\uffff\1\160\15\uffff\2\154\1\uffff\1\uffff\1\162"+
        "\1\67\1\162\1\65\2\uffff\1\160\2\uffff\2\50\1\uffff\1\154\1\67\1"+
        "\154\1\62\1\67\1\65\1\162\1\67\1\144\1\50\1\66\1\50\1\143\1\67\1"+
        "\62\1\154\1\67\1\65\1\162\1\67\1\66\1\143\2\50\1\67\1\62\1\154\1"+
        "\67\1\65\1\162\1\67\1\66\1\143\2\50\1\67\1\62\1\154\1\65\1\162\1"+
        "\67\1\66\1\143\2\50\1\62\1\154\1\162\1\143\2\50\1\154\2\50";
    static final String DFA212_acceptS =
        "\3\uffff\1\3\2\uffff\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\16\1\21"+
        "\1\22\1\23\1\24\1\25\1\uffff\1\30\1\31\3\uffff\1\32\1\uffff\1\37"+
        "\1\40\1\42\1\43\1\6\1\1\1\2\1\17\1\4\1\20\1\5\1\27\1\26\3\uffff"+
        "\1\41\4\uffff\1\36\1\33\1\uffff\1\34\1\35\71\uffff";
    static final String DFA212_specialS =
        "\30\uffff\1\0\21\uffff\1\2\14\uffff\1\1\66\uffff}>";
    static final String[] DFA212_transitionS = {
            "\1\35\1\36\2\uffff\1\36\22\uffff\1\35\1\33\1\24\1\31\3\uffff"+
            "\1\24\1\20\1\21\1\17\1\16\1\22\1\4\1\23\1\2\12\34\1\15\1\14"+
            "\1\3\1\13\1\6\1\uffff\1\32\24\25\1\27\5\25\1\11\1\30\1\12\1"+
            "\uffff\1\25\1\uffff\24\25\1\26\5\25\1\7\1\1\1\10\1\5\1\uffff"+
            "\uff80\25",
            "\1\37",
            "\1\41",
            "",
            "\1\43\23\uffff\32\25\1\uffff\1\25\2\uffff\1\25\1\uffff\32\25"+
            "\5\uffff\uff80\25",
            "\1\45",
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
            "\12\34",
            "",
            "",
            "\2\53\1\uffff\2\53\22\uffff\1\53\61\uffff\1\51\11\uffff\1\52"+
            "\25\uffff\1\50",
            "\2\53\1\uffff\2\53\22\uffff\1\53\61\uffff\1\51\11\uffff\1\52"+
            "\25\uffff\1\50",
            "\12\25\1\uffff\1\25\2\uffff\42\25\1\55\4\25\1\57\1\25\1\57"+
            "\35\25\1\56\37\25\1\54\uff8a\25",
            "",
            "\1\61\3\uffff\1\64\2\uffff\1\63\13\uffff\1\62\6\uffff\1\60"+
            "\5\uffff\1\61\3\uffff\1\64\2\uffff\1\63",
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
            "\2\53\1\uffff\2\53\22\uffff\1\53\53\uffff\1\66\17\uffff\1\67"+
            "\17\uffff\1\65",
            "\2\53\1\uffff\2\53\22\uffff\1\53\53\uffff\1\66\17\uffff\1\67"+
            "\17\uffff\1\65",
            "\12\25\1\uffff\1\25\2\uffff\42\25\1\71\4\25\1\73\1\25\1\73"+
            "\32\25\1\72\37\25\1\70\uff8d\25",
            "",
            "\1\51\11\uffff\1\52\25\uffff\1\50",
            "\1\74\4\uffff\1\75\1\uffff\1\75",
            "\1\51\11\uffff\1\52\25\uffff\1\50",
            "\1\76",
            "",
            "",
            "\1\77\3\uffff\1\100\1\63\1\100\1\63\21\uffff\1\61\3\uffff\1"+
            "\64\2\uffff\1\63\30\uffff\1\61\3\uffff\1\64\2\uffff\1\63",
            "",
            "",
            "\2\53\1\uffff\2\53\22\uffff\1\53\7\uffff\1\53",
            "\2\53\1\uffff\2\53\22\uffff\1\53\7\uffff\1\53",
            "\12\25\1\uffff\1\25\2\uffff\42\25\1\102\3\25\1\104\1\25\1\104"+
            "\25\25\1\103\37\25\1\101\uff93\25",
            "\1\66\17\uffff\1\67\17\uffff\1\65",
            "\1\105\4\uffff\1\106\1\uffff\1\106",
            "\1\66\17\uffff\1\67\17\uffff\1\65",
            "\1\107",
            "\1\110\4\uffff\1\111\1\uffff\1\111",
            "\1\112",
            "\1\51\11\uffff\1\52\25\uffff\1\50",
            "\1\113\3\uffff\1\100\1\63\1\100\1\63",
            "\1\61\12\uffff\1\64\37\uffff\1\64",
            "\1\53",
            "\1\114\3\uffff\1\115\1\uffff\1\115",
            "\1\53",
            "\1\117\37\uffff\1\116",
            "\1\120\4\uffff\1\121\1\uffff\1\121",
            "\1\122",
            "\1\66\17\uffff\1\67\17\uffff\1\65",
            "\1\123\4\uffff\1\124\1\uffff\1\124",
            "\1\125",
            "\1\51\11\uffff\1\52\25\uffff\1\50",
            "\1\126\3\uffff\1\100\1\63\1\100\1\63",
            "\1\127\3\uffff\1\130\1\uffff\1\130",
            "\1\132\37\uffff\1\131",
            "\1\53",
            "\1\53",
            "\1\133\4\uffff\1\134\1\uffff\1\134",
            "\1\135",
            "\1\66\17\uffff\1\67\17\uffff\1\65",
            "\1\136\1\uffff\1\136",
            "\1\137",
            "\1\51\11\uffff\1\52\25\uffff\1\50",
            "\1\140\3\uffff\1\100\1\63\1\100\1\63",
            "\1\141\3\uffff\1\142\1\uffff\1\142",
            "\1\144\37\uffff\1\143",
            "\1\53",
            "\1\53",
            "\1\145\1\uffff\1\145",
            "\1\146",
            "\1\66\17\uffff\1\67\17\uffff\1\65",
            "\1\147",
            "\1\51\11\uffff\1\52\25\uffff\1\50",
            "\1\100\1\63\1\100\1\63",
            "\1\150\1\uffff\1\150",
            "\1\152\37\uffff\1\151",
            "\1\53",
            "\1\53",
            "\1\153",
            "\1\66\17\uffff\1\67\17\uffff\1\65",
            "\1\51\11\uffff\1\52\25\uffff\1\50",
            "\1\155\37\uffff\1\154",
            "\1\53",
            "\1\53",
            "\1\66\17\uffff\1\67\17\uffff\1\65",
            "\1\53",
            "\1\53"
    };

    static final short[] DFA212_eot = DFA.unpackEncodedString(DFA212_eotS);
    static final short[] DFA212_eof = DFA.unpackEncodedString(DFA212_eofS);
    static final char[] DFA212_min = DFA.unpackEncodedStringToUnsignedChars(DFA212_minS);
    static final char[] DFA212_max = DFA.unpackEncodedStringToUnsignedChars(DFA212_maxS);
    static final short[] DFA212_accept = DFA.unpackEncodedString(DFA212_acceptS);
    static final short[] DFA212_special = DFA.unpackEncodedString(DFA212_specialS);
    static final short[][] DFA212_transition;

    static {
        int numStates = DFA212_transitionS.length;
        DFA212_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA212_transition[i] = DFA.unpackEncodedString(DFA212_transitionS[i]);
        }
    }

    class DFA212 extends DFA {

        public DFA212(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 212;
            this.eot = DFA212_eot;
            this.eof = DFA212_eof;
            this.min = DFA212_min;
            this.max = DFA212_max;
            this.accept = DFA212_accept;
            this.special = DFA212_special;
            this.transition = DFA212_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__81 | COMMENT | CDO | CDC | INCLUDES | DASHMATCH | GREATER | LBRACE | RBRACE | LBRACKET | RBRACKET | OPEQ | SEMI | COLON | SOLIDUS | MINUS | PLUS | STAR | LPAREN | RPAREN | COMMA | DOT | TILDE | STRING | IDENT | HASH | IMPORT_SYM | PAGE_SYM | MEDIA_SYM | CHARSET_SYM | IMPORTANT_SYM | NUMBER | URI | WS | NL );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA212_24 = input.LA(1);

                        s = -1;
                        if ( (LA212_24=='u') ) {s = 44;}

                        else if ( (LA212_24=='0') ) {s = 45;}

                        else if ( (LA212_24=='U') ) {s = 46;}

                        else if ( ((LA212_24>='\u0000' && LA212_24<='\t')||LA212_24=='\u000B'||(LA212_24>='\u000E' && LA212_24<='/')||(LA212_24>='1' && LA212_24<='4')||LA212_24=='6'||(LA212_24>='8' && LA212_24<='T')||(LA212_24>='V' && LA212_24<='t')||(LA212_24>='v' && LA212_24<='\uFFFF')) ) {s = 21;}

                        else if ( (LA212_24=='5'||LA212_24=='7') ) {s = 47;}

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA212_55 = input.LA(1);

                        s = -1;
                        if ( (LA212_55=='l') ) {s = 65;}

                        else if ( (LA212_55=='0') ) {s = 66;}

                        else if ( (LA212_55=='L') ) {s = 67;}

                        else if ( ((LA212_55>='\u0000' && LA212_55<='\t')||LA212_55=='\u000B'||(LA212_55>='\u000E' && LA212_55<='/')||(LA212_55>='1' && LA212_55<='3')||LA212_55=='5'||(LA212_55>='7' && LA212_55<='K')||(LA212_55>='M' && LA212_55<='k')||(LA212_55>='m' && LA212_55<='\uFFFF')) ) {s = 21;}

                        else if ( (LA212_55=='4'||LA212_55=='6') ) {s = 68;}

                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA212_42 = input.LA(1);

                        s = -1;
                        if ( (LA212_42=='r') ) {s = 56;}

                        else if ( (LA212_42=='0') ) {s = 57;}

                        else if ( (LA212_42=='R') ) {s = 58;}

                        else if ( ((LA212_42>='\u0000' && LA212_42<='\t')||LA212_42=='\u000B'||(LA212_42>='\u000E' && LA212_42<='/')||(LA212_42>='1' && LA212_42<='4')||LA212_42=='6'||(LA212_42>='8' && LA212_42<='Q')||(LA212_42>='S' && LA212_42<='q')||(LA212_42>='s' && LA212_42<='\uFFFF')) ) {s = 21;}

                        else if ( (LA212_42=='5'||LA212_42=='7') ) {s = 59;}

                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 212, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA214_eotS =
        "\12\uffff";
    static final String DFA214_eofS =
        "\12\uffff";
    static final String DFA214_minS =
        "\1\103\1\uffff\1\60\2\uffff\1\60\1\64\2\60\1\64";
    static final String DFA214_maxS =
        "\1\170\1\uffff\1\170\2\uffff\1\67\1\70\3\67";
    static final String DFA214_acceptS =
        "\1\uffff\1\1\1\uffff\1\2\1\3\5\uffff";
    static final String DFA214_specialS =
        "\12\uffff}>";
    static final String[] DFA214_transitionS = {
            "\1\4\20\uffff\1\3\3\uffff\1\1\3\uffff\1\2\6\uffff\1\4\20\uffff"+
            "\1\3\3\uffff\1\1",
            "",
            "\1\5\3\uffff\1\4\1\6\1\4\1\6\34\uffff\1\3\3\uffff\1\1\33\uffff"+
            "\1\3\3\uffff\1\1",
            "",
            "",
            "\1\7\3\uffff\1\4\1\6\1\4\1\6",
            "\1\3\3\uffff\1\1",
            "\1\10\3\uffff\1\4\1\6\1\4\1\6",
            "\1\11\3\uffff\1\4\1\6\1\4\1\6",
            "\1\4\1\6\1\4\1\6"
    };

    static final short[] DFA214_eot = DFA.unpackEncodedString(DFA214_eotS);
    static final short[] DFA214_eof = DFA.unpackEncodedString(DFA214_eofS);
    static final char[] DFA214_min = DFA.unpackEncodedStringToUnsignedChars(DFA214_minS);
    static final char[] DFA214_max = DFA.unpackEncodedStringToUnsignedChars(DFA214_maxS);
    static final short[] DFA214_accept = DFA.unpackEncodedString(DFA214_acceptS);
    static final short[] DFA214_special = DFA.unpackEncodedString(DFA214_specialS);
    static final short[][] DFA214_transition;

    static {
        int numStates = DFA214_transitionS.length;
        DFA214_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA214_transition[i] = DFA.unpackEncodedString(DFA214_transitionS[i]);
        }
    }

    class DFA214 extends DFA {

        public DFA214(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 214;
            this.eot = DFA214_eot;
            this.eof = DFA214_eof;
            this.min = DFA214_min;
            this.max = DFA214_max;
            this.accept = DFA214_accept;
            this.special = DFA214_special;
            this.transition = DFA214_transition;
        }
        public String getDescription() {
            return "806:17: ( X | T | C )";
        }
    }
 

}