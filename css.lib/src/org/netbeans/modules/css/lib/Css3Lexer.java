// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2011-08-18 11:17:11

    package org.netbeans.modules.css.lib;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
public class Css3Lexer extends Lexer {
    public static final int EOF=-1;
    public static final int WS=4;
    public static final int NAMESPACE_SYM=5;
    public static final int IDENT=6;
    public static final int STRING=7;
    public static final int URI=8;
    public static final int CHARSET_SYM=9;
    public static final int SEMI=10;
    public static final int IMPORT_SYM=11;
    public static final int MEDIA_SYM=12;
    public static final int LBRACE=13;
    public static final int RBRACE=14;
    public static final int COMMA=15;
    public static final int GEN=16;
    public static final int PAGE_SYM=17;
    public static final int COLON=18;
    public static final int SOLIDUS=19;
    public static final int PLUS=20;
    public static final int GREATER=21;
    public static final int TILDE=22;
    public static final int MINUS=23;
    public static final int STAR=24;
    public static final int PIPE=25;
    public static final int HASH=26;
    public static final int DOT=27;
    public static final int LBRACKET=28;
    public static final int DCOLON=29;
    public static final int OPEQ=30;
    public static final int INCLUDES=31;
    public static final int DASHMATCH=32;
    public static final int RBRACKET=33;
    public static final int LPAREN=34;
    public static final int RPAREN=35;
    public static final int IMPORTANT_SYM=36;
    public static final int NUMBER=37;
    public static final int PERCENTAGE=38;
    public static final int LENGTH=39;
    public static final int EMS=40;
    public static final int EXS=41;
    public static final int ANGLE=42;
    public static final int TIME=43;
    public static final int FREQ=44;
    public static final int HEXCHAR=45;
    public static final int NONASCII=46;
    public static final int UNICODE=47;
    public static final int ESCAPE=48;
    public static final int NMSTART=49;
    public static final int NMCHAR=50;
    public static final int NAME=51;
    public static final int URL=52;
    public static final int A=53;
    public static final int B=54;
    public static final int C=55;
    public static final int D=56;
    public static final int E=57;
    public static final int F=58;
    public static final int G=59;
    public static final int H=60;
    public static final int I=61;
    public static final int J=62;
    public static final int K=63;
    public static final int L=64;
    public static final int M=65;
    public static final int N=66;
    public static final int O=67;
    public static final int P=68;
    public static final int Q=69;
    public static final int R=70;
    public static final int S=71;
    public static final int T=72;
    public static final int U=73;
    public static final int V=74;
    public static final int W=75;
    public static final int X=76;
    public static final int Y=77;
    public static final int Z=78;
    public static final int COMMENT=79;
    public static final int CDO=80;
    public static final int CDC=81;
    public static final int INVALID=82;
    public static final int DIMENSION=83;
    public static final int NL=84;

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

    // $ANTLR start "GEN"
    public final void mGEN() throws RecognitionException {
        try {
            int _type = GEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:532:25: ( '@@@' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:532:27: '@@@'
            {
            match("@@@"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GEN"

    // $ANTLR start "HEXCHAR"
    public final void mHEXCHAR() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:25: ( ( 'a' .. 'f' | 'A' .. 'F' | '0' .. '9' ) )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:27: ( 'a' .. 'f' | 'A' .. 'F' | '0' .. '9' )
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:536:25: ( '\\u0080' .. '\\uFFFF' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:536:27: '\\u0080' .. '\\uFFFF'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:25: ( '\\\\' HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )? ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )* )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:27: '\\\\' HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )? ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
            {
            match('\\'); if (state.failed) return ;
            mHEXCHAR(); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:33: ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )? )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( ((LA5_0>='0' && LA5_0<='9')||(LA5_0>='A' && LA5_0<='F')||(LA5_0>='a' && LA5_0<='f')) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:34: HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )?
                    {
                    mHEXCHAR(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:37: ( HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )? )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( ((LA4_0>='0' && LA4_0<='9')||(LA4_0>='A' && LA4_0<='F')||(LA4_0>='a' && LA4_0<='f')) ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:38: HEXCHAR ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )?
                            {
                            mHEXCHAR(); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:41: ( HEXCHAR ( HEXCHAR ( HEXCHAR )? )? )?
                            int alt3=2;
                            int LA3_0 = input.LA(1);

                            if ( ((LA3_0>='0' && LA3_0<='9')||(LA3_0>='A' && LA3_0<='F')||(LA3_0>='a' && LA3_0<='f')) ) {
                                alt3=1;
                            }
                            switch (alt3) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:541:42: HEXCHAR ( HEXCHAR ( HEXCHAR )? )?
                                    {
                                    mHEXCHAR(); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:542:45: ( HEXCHAR ( HEXCHAR )? )?
                                    int alt2=2;
                                    int LA2_0 = input.LA(1);

                                    if ( ((LA2_0>='0' && LA2_0<='9')||(LA2_0>='A' && LA2_0<='F')||(LA2_0>='a' && LA2_0<='f')) ) {
                                        alt2=1;
                                    }
                                    switch (alt2) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:542:46: HEXCHAR ( HEXCHAR )?
                                            {
                                            mHEXCHAR(); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:542:54: ( HEXCHAR )?
                                            int alt1=2;
                                            int LA1_0 = input.LA(1);

                                            if ( ((LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='F')||(LA1_0>='a' && LA1_0<='f')) ) {
                                                alt1=1;
                                            }
                                            switch (alt1) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:542:54: HEXCHAR
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

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:546:33: ( '\\r' | '\\n' | '\\t' | '\\f' | ' ' )*
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:25: ( UNICODE | '\\\\' ~ ( '\\r' | '\\n' | '\\f' | HEXCHAR ) )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:27: UNICODE
                    {
                    mUNICODE(); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:37: '\\\\' ~ ( '\\r' | '\\n' | '\\f' | HEXCHAR )
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:550:25: ( '_' | 'a' .. 'z' | 'A' .. 'Z' | NONASCII | ESCAPE )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:550:27: '_'
                    {
                    match('_'); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:551:27: 'a' .. 'z'
                    {
                    matchRange('a','z'); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:552:27: 'A' .. 'Z'
                    {
                    matchRange('A','Z'); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:553:27: NONASCII
                    {
                    mNONASCII(); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:554:27: ESCAPE
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:25: ( '_' | 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '-' | NONASCII | ESCAPE )
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:27: '_'
                    {
                    match('_'); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:27: 'a' .. 'z'
                    {
                    matchRange('a','z'); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:559:27: 'A' .. 'Z'
                    {
                    matchRange('A','Z'); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:27: '0' .. '9'
                    {
                    matchRange('0','9'); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:27: '-'
                    {
                    match('-'); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:27: NONASCII
                    {
                    mNONASCII(); if (state.failed) return ;

                    }
                    break;
                case 7 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:27: ESCAPE
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:566:25: ( ( NMCHAR )+ )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:566:27: ( NMCHAR )+
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:566:27: ( NMCHAR )+
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
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:566:27: NMCHAR
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:568:25: ( ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | NMCHAR )* )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:568:27: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | NMCHAR )*
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:568:27: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | NMCHAR )*
            loop11:
            do {
                int alt11=13;
                alt11 = dfa11.predict(input);
                switch (alt11) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:31: '['
            	    {
            	    match('['); if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:35: '!'
            	    {
            	    match('!'); if (state.failed) return ;

            	    }
            	    break;
            	case 3 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:39: '#'
            	    {
            	    match('#'); if (state.failed) return ;

            	    }
            	    break;
            	case 4 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:43: '$'
            	    {
            	    match('$'); if (state.failed) return ;

            	    }
            	    break;
            	case 5 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:47: '%'
            	    {
            	    match('%'); if (state.failed) return ;

            	    }
            	    break;
            	case 6 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:51: '&'
            	    {
            	    match('&'); if (state.failed) return ;

            	    }
            	    break;
            	case 7 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:55: '*'
            	    {
            	    match('*'); if (state.failed) return ;

            	    }
            	    break;
            	case 8 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:59: '~'
            	    {
            	    match('~'); if (state.failed) return ;

            	    }
            	    break;
            	case 9 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:63: '.'
            	    {
            	    match('.'); if (state.failed) return ;

            	    }
            	    break;
            	case 10 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:67: ':'
            	    {
            	    match(':'); if (state.failed) return ;

            	    }
            	    break;
            	case 11 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:569:71: '/'
            	    {
            	    match('/'); if (state.failed) return ;

            	    }
            	    break;
            	case 12 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:570:31: NMCHAR
            	    {
            	    mNMCHAR(); if (state.failed) return ;

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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:577:17: ( ( 'a' | 'A' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '1' )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0=='A'||LA16_0=='a') ) {
                alt16=1;
            }
            else if ( (LA16_0=='\\') ) {
                alt16=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:577:21: ( 'a' | 'A' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '1'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0=='0') ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt14=2;
                            int LA14_0 = input.LA(1);

                            if ( (LA14_0=='0') ) {
                                alt14=1;
                            }
                            switch (alt14) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:36: ( '0' ( '0' )? )?
                                    int alt13=2;
                                    int LA13_0 = input.LA(1);

                                    if ( (LA13_0=='0') ) {
                                        alt13=1;
                                    }
                                    switch (alt13) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:41: ( '0' )?
                                            int alt12=2;
                                            int LA12_0 = input.LA(1);

                                            if ( (LA12_0=='0') ) {
                                                alt12=1;
                                            }
                                            switch (alt12) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:41: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:580:17: ( ( 'b' | 'B' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '2' )
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0=='B'||LA21_0=='b') ) {
                alt21=1;
            }
            else if ( (LA21_0=='\\') ) {
                alt21=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;
            }
            switch (alt21) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:580:21: ( 'b' | 'B' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '2'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt20=2;
                    int LA20_0 = input.LA(1);

                    if ( (LA20_0=='0') ) {
                        alt20=1;
                    }
                    switch (alt20) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt19=2;
                            int LA19_0 = input.LA(1);

                            if ( (LA19_0=='0') ) {
                                alt19=1;
                            }
                            switch (alt19) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:36: ( '0' ( '0' )? )?
                                    int alt18=2;
                                    int LA18_0 = input.LA(1);

                                    if ( (LA18_0=='0') ) {
                                        alt18=1;
                                    }
                                    switch (alt18) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:41: ( '0' )?
                                            int alt17=2;
                                            int LA17_0 = input.LA(1);

                                            if ( (LA17_0=='0') ) {
                                                alt17=1;
                                            }
                                            switch (alt17) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:41: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:17: ( ( 'c' | 'C' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '3' )
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0=='C'||LA26_0=='c') ) {
                alt26=1;
            }
            else if ( (LA26_0=='\\') ) {
                alt26=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;
            }
            switch (alt26) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:21: ( 'c' | 'C' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '3'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt25=2;
                    int LA25_0 = input.LA(1);

                    if ( (LA25_0=='0') ) {
                        alt25=1;
                    }
                    switch (alt25) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt24=2;
                            int LA24_0 = input.LA(1);

                            if ( (LA24_0=='0') ) {
                                alt24=1;
                            }
                            switch (alt24) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:36: ( '0' ( '0' )? )?
                                    int alt23=2;
                                    int LA23_0 = input.LA(1);

                                    if ( (LA23_0=='0') ) {
                                        alt23=1;
                                    }
                                    switch (alt23) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:41: ( '0' )?
                                            int alt22=2;
                                            int LA22_0 = input.LA(1);

                                            if ( (LA22_0=='0') ) {
                                                alt22=1;
                                            }
                                            switch (alt22) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:41: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:17: ( ( 'd' | 'D' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '4' )
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0=='D'||LA31_0=='d') ) {
                alt31=1;
            }
            else if ( (LA31_0=='\\') ) {
                alt31=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 31, 0, input);

                throw nvae;
            }
            switch (alt31) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:21: ( 'd' | 'D' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '4'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt30=2;
                    int LA30_0 = input.LA(1);

                    if ( (LA30_0=='0') ) {
                        alt30=1;
                    }
                    switch (alt30) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt29=2;
                            int LA29_0 = input.LA(1);

                            if ( (LA29_0=='0') ) {
                                alt29=1;
                            }
                            switch (alt29) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:36: ( '0' ( '0' )? )?
                                    int alt28=2;
                                    int LA28_0 = input.LA(1);

                                    if ( (LA28_0=='0') ) {
                                        alt28=1;
                                    }
                                    switch (alt28) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:41: ( '0' )?
                                            int alt27=2;
                                            int LA27_0 = input.LA(1);

                                            if ( (LA27_0=='0') ) {
                                                alt27=1;
                                            }
                                            switch (alt27) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:41: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:17: ( ( 'e' | 'E' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '5' )
            int alt36=2;
            int LA36_0 = input.LA(1);

            if ( (LA36_0=='E'||LA36_0=='e') ) {
                alt36=1;
            }
            else if ( (LA36_0=='\\') ) {
                alt36=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                throw nvae;
            }
            switch (alt36) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:21: ( 'e' | 'E' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '5'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt35=2;
                    int LA35_0 = input.LA(1);

                    if ( (LA35_0=='0') ) {
                        alt35=1;
                    }
                    switch (alt35) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt34=2;
                            int LA34_0 = input.LA(1);

                            if ( (LA34_0=='0') ) {
                                alt34=1;
                            }
                            switch (alt34) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:36: ( '0' ( '0' )? )?
                                    int alt33=2;
                                    int LA33_0 = input.LA(1);

                                    if ( (LA33_0=='0') ) {
                                        alt33=1;
                                    }
                                    switch (alt33) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:41: ( '0' )?
                                            int alt32=2;
                                            int LA32_0 = input.LA(1);

                                            if ( (LA32_0=='0') ) {
                                                alt32=1;
                                            }
                                            switch (alt32) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:41: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:17: ( ( 'f' | 'F' ) | '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '6' )
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0=='F'||LA41_0=='f') ) {
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:21: ( 'f' | 'F' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:21: '\\\\' ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '6'
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:26: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                    int alt40=2;
                    int LA40_0 = input.LA(1);

                    if ( (LA40_0=='0') ) {
                        alt40=1;
                    }
                    switch (alt40) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:27: '0' ( '0' ( '0' ( '0' )? )? )?
                            {
                            match('0'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:31: ( '0' ( '0' ( '0' )? )? )?
                            int alt39=2;
                            int LA39_0 = input.LA(1);

                            if ( (LA39_0=='0') ) {
                                alt39=1;
                            }
                            switch (alt39) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:32: '0' ( '0' ( '0' )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:36: ( '0' ( '0' )? )?
                                    int alt38=2;
                                    int LA38_0 = input.LA(1);

                                    if ( (LA38_0=='0') ) {
                                        alt38=1;
                                    }
                                    switch (alt38) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:37: '0' ( '0' )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:41: ( '0' )?
                                            int alt37=2;
                                            int LA37_0 = input.LA(1);

                                            if ( (LA37_0=='0') ) {
                                                alt37=1;
                                            }
                                            switch (alt37) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:41: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:595:17: ( ( 'g' | 'G' ) | '\\\\' ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' ) )
            int alt47=2;
            int LA47_0 = input.LA(1);

            if ( (LA47_0=='G'||LA47_0=='g') ) {
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:595:21: ( 'g' | 'G' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:21: '\\\\' ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:25: ( 'g' | 'G' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7' )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 46, 0, input);

                        throw nvae;
                    }

                    switch (alt46) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:598:31: 'g'
                            {
                            match('g'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:599:31: 'G'
                            {
                            match('G'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '7'
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt45=2;
                            int LA45_0 = input.LA(1);

                            if ( (LA45_0=='0') ) {
                                alt45=1;
                            }
                            switch (alt45) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt44=2;
                                    int LA44_0 = input.LA(1);

                                    if ( (LA44_0=='0') ) {
                                        alt44=1;
                                    }
                                    switch (alt44) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:41: ( '0' ( '0' )? )?
                                            int alt43=2;
                                            int LA43_0 = input.LA(1);

                                            if ( (LA43_0=='0') ) {
                                                alt43=1;
                                            }
                                            switch (alt43) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:46: ( '0' )?
                                                    int alt42=2;
                                                    int LA42_0 = input.LA(1);

                                                    if ( (LA42_0=='0') ) {
                                                        alt42=1;
                                                    }
                                                    switch (alt42) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:17: ( ( 'h' | 'H' ) | '\\\\' ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' ) )
            int alt53=2;
            int LA53_0 = input.LA(1);

            if ( (LA53_0=='H'||LA53_0=='h') ) {
                alt53=1;
            }
            else if ( (LA53_0=='\\') ) {
                alt53=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 53, 0, input);

                throw nvae;
            }
            switch (alt53) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:21: ( 'h' | 'H' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:604:19: '\\\\' ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:25: ( 'h' | 'H' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8' )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 52, 0, input);

                        throw nvae;
                    }

                    switch (alt52) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:606:31: 'h'
                            {
                            match('h'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:607:31: 'H'
                            {
                            match('H'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '8'
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt51=2;
                            int LA51_0 = input.LA(1);

                            if ( (LA51_0=='0') ) {
                                alt51=1;
                            }
                            switch (alt51) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt50=2;
                                    int LA50_0 = input.LA(1);

                                    if ( (LA50_0=='0') ) {
                                        alt50=1;
                                    }
                                    switch (alt50) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:41: ( '0' ( '0' )? )?
                                            int alt49=2;
                                            int LA49_0 = input.LA(1);

                                            if ( (LA49_0=='0') ) {
                                                alt49=1;
                                            }
                                            switch (alt49) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:46: ( '0' )?
                                                    int alt48=2;
                                                    int LA48_0 = input.LA(1);

                                                    if ( (LA48_0=='0') ) {
                                                        alt48=1;
                                                    }
                                                    switch (alt48) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:611:17: ( ( 'i' | 'I' ) | '\\\\' ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' ) )
            int alt59=2;
            int LA59_0 = input.LA(1);

            if ( (LA59_0=='I'||LA59_0=='i') ) {
                alt59=1;
            }
            else if ( (LA59_0=='\\') ) {
                alt59=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 59, 0, input);

                throw nvae;
            }
            switch (alt59) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:611:21: ( 'i' | 'I' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:612:19: '\\\\' ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:25: ( 'i' | 'I' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9' )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 58, 0, input);

                        throw nvae;
                    }

                    switch (alt58) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:614:31: 'i'
                            {
                            match('i'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:31: 'I'
                            {
                            match('I'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) '9'
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt57=2;
                            int LA57_0 = input.LA(1);

                            if ( (LA57_0=='0') ) {
                                alt57=1;
                            }
                            switch (alt57) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt56=2;
                                    int LA56_0 = input.LA(1);

                                    if ( (LA56_0=='0') ) {
                                        alt56=1;
                                    }
                                    switch (alt56) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:41: ( '0' ( '0' )? )?
                                            int alt55=2;
                                            int LA55_0 = input.LA(1);

                                            if ( (LA55_0=='0') ) {
                                                alt55=1;
                                            }
                                            switch (alt55) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:46: ( '0' )?
                                                    int alt54=2;
                                                    int LA54_0 = input.LA(1);

                                                    if ( (LA54_0=='0') ) {
                                                        alt54=1;
                                                    }
                                                    switch (alt54) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:616:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:619:17: ( ( 'j' | 'J' ) | '\\\\' ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) ) )
            int alt65=2;
            int LA65_0 = input.LA(1);

            if ( (LA65_0=='J'||LA65_0=='j') ) {
                alt65=1;
            }
            else if ( (LA65_0=='\\') ) {
                alt65=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 65, 0, input);

                throw nvae;
            }
            switch (alt65) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:619:21: ( 'j' | 'J' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:19: '\\\\' ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:621:25: ( 'j' | 'J' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' ) )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 64, 0, input);

                        throw nvae;
                    }

                    switch (alt64) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:31: 'j'
                            {
                            match('j'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:623:31: 'J'
                            {
                            match('J'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'A' | 'a' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt63=2;
                            int LA63_0 = input.LA(1);

                            if ( (LA63_0=='0') ) {
                                alt63=1;
                            }
                            switch (alt63) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt62=2;
                                    int LA62_0 = input.LA(1);

                                    if ( (LA62_0=='0') ) {
                                        alt62=1;
                                    }
                                    switch (alt62) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:41: ( '0' ( '0' )? )?
                                            int alt61=2;
                                            int LA61_0 = input.LA(1);

                                            if ( (LA61_0=='0') ) {
                                                alt61=1;
                                            }
                                            switch (alt61) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:46: ( '0' )?
                                                    int alt60=2;
                                                    int LA60_0 = input.LA(1);

                                                    if ( (LA60_0=='0') ) {
                                                        alt60=1;
                                                    }
                                                    switch (alt60) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:627:17: ( ( 'k' | 'K' ) | '\\\\' ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) ) )
            int alt71=2;
            int LA71_0 = input.LA(1);

            if ( (LA71_0=='K'||LA71_0=='k') ) {
                alt71=1;
            }
            else if ( (LA71_0=='\\') ) {
                alt71=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 71, 0, input);

                throw nvae;
            }
            switch (alt71) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:627:21: ( 'k' | 'K' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:628:19: '\\\\' ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:25: ( 'k' | 'K' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' ) )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 70, 0, input);

                        throw nvae;
                    }

                    switch (alt70) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:630:31: 'k'
                            {
                            match('k'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:631:31: 'K'
                            {
                            match('K'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'B' | 'b' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt69=2;
                            int LA69_0 = input.LA(1);

                            if ( (LA69_0=='0') ) {
                                alt69=1;
                            }
                            switch (alt69) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt68=2;
                                    int LA68_0 = input.LA(1);

                                    if ( (LA68_0=='0') ) {
                                        alt68=1;
                                    }
                                    switch (alt68) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:41: ( '0' ( '0' )? )?
                                            int alt67=2;
                                            int LA67_0 = input.LA(1);

                                            if ( (LA67_0=='0') ) {
                                                alt67=1;
                                            }
                                            switch (alt67) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:46: ( '0' )?
                                                    int alt66=2;
                                                    int LA66_0 = input.LA(1);

                                                    if ( (LA66_0=='0') ) {
                                                        alt66=1;
                                                    }
                                                    switch (alt66) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:635:17: ( ( 'l' | 'L' ) | '\\\\' ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) ) )
            int alt77=2;
            int LA77_0 = input.LA(1);

            if ( (LA77_0=='L'||LA77_0=='l') ) {
                alt77=1;
            }
            else if ( (LA77_0=='\\') ) {
                alt77=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 77, 0, input);

                throw nvae;
            }
            switch (alt77) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:635:21: ( 'l' | 'L' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:19: '\\\\' ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:637:25: ( 'l' | 'L' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' ) )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 76, 0, input);

                        throw nvae;
                    }

                    switch (alt76) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:638:31: 'l'
                            {
                            match('l'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:639:31: 'L'
                            {
                            match('L'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'C' | 'c' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt75=2;
                            int LA75_0 = input.LA(1);

                            if ( (LA75_0=='0') ) {
                                alt75=1;
                            }
                            switch (alt75) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt74=2;
                                    int LA74_0 = input.LA(1);

                                    if ( (LA74_0=='0') ) {
                                        alt74=1;
                                    }
                                    switch (alt74) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:41: ( '0' ( '0' )? )?
                                            int alt73=2;
                                            int LA73_0 = input.LA(1);

                                            if ( (LA73_0=='0') ) {
                                                alt73=1;
                                            }
                                            switch (alt73) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:46: ( '0' )?
                                                    int alt72=2;
                                                    int LA72_0 = input.LA(1);

                                                    if ( (LA72_0=='0') ) {
                                                        alt72=1;
                                                    }
                                                    switch (alt72) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:17: ( ( 'm' | 'M' ) | '\\\\' ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) ) )
            int alt83=2;
            int LA83_0 = input.LA(1);

            if ( (LA83_0=='M'||LA83_0=='m') ) {
                alt83=1;
            }
            else if ( (LA83_0=='\\') ) {
                alt83=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 83, 0, input);

                throw nvae;
            }
            switch (alt83) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:21: ( 'm' | 'M' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:19: '\\\\' ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:645:25: ( 'm' | 'M' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' ) )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 82, 0, input);

                        throw nvae;
                    }

                    switch (alt82) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:646:31: 'm'
                            {
                            match('m'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:31: 'M'
                            {
                            match('M'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'D' | 'd' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt81=2;
                            int LA81_0 = input.LA(1);

                            if ( (LA81_0=='0') ) {
                                alt81=1;
                            }
                            switch (alt81) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt80=2;
                                    int LA80_0 = input.LA(1);

                                    if ( (LA80_0=='0') ) {
                                        alt80=1;
                                    }
                                    switch (alt80) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:41: ( '0' ( '0' )? )?
                                            int alt79=2;
                                            int LA79_0 = input.LA(1);

                                            if ( (LA79_0=='0') ) {
                                                alt79=1;
                                            }
                                            switch (alt79) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:46: ( '0' )?
                                                    int alt78=2;
                                                    int LA78_0 = input.LA(1);

                                                    if ( (LA78_0=='0') ) {
                                                        alt78=1;
                                                    }
                                                    switch (alt78) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:651:17: ( ( 'n' | 'N' ) | '\\\\' ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) ) )
            int alt89=2;
            int LA89_0 = input.LA(1);

            if ( (LA89_0=='N'||LA89_0=='n') ) {
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:651:21: ( 'n' | 'N' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:19: '\\\\' ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:653:25: ( 'n' | 'N' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' ) )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 88, 0, input);

                        throw nvae;
                    }

                    switch (alt88) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:31: 'n'
                            {
                            match('n'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:655:31: 'N'
                            {
                            match('N'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'E' | 'e' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt87=2;
                            int LA87_0 = input.LA(1);

                            if ( (LA87_0=='0') ) {
                                alt87=1;
                            }
                            switch (alt87) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt86=2;
                                    int LA86_0 = input.LA(1);

                                    if ( (LA86_0=='0') ) {
                                        alt86=1;
                                    }
                                    switch (alt86) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:41: ( '0' ( '0' )? )?
                                            int alt85=2;
                                            int LA85_0 = input.LA(1);

                                            if ( (LA85_0=='0') ) {
                                                alt85=1;
                                            }
                                            switch (alt85) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:46: ( '0' )?
                                                    int alt84=2;
                                                    int LA84_0 = input.LA(1);

                                                    if ( (LA84_0=='0') ) {
                                                        alt84=1;
                                                    }
                                                    switch (alt84) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:659:17: ( ( 'o' | 'O' ) | '\\\\' ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) ) )
            int alt95=2;
            int LA95_0 = input.LA(1);

            if ( (LA95_0=='O'||LA95_0=='o') ) {
                alt95=1;
            }
            else if ( (LA95_0=='\\') ) {
                alt95=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 95, 0, input);

                throw nvae;
            }
            switch (alt95) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:659:21: ( 'o' | 'O' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:19: '\\\\' ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:661:25: ( 'o' | 'O' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' ) )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 94, 0, input);

                        throw nvae;
                    }

                    switch (alt94) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:662:31: 'o'
                            {
                            match('o'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:31: 'O'
                            {
                            match('O'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '4' | '6' ) ( 'F' | 'f' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt93=2;
                            int LA93_0 = input.LA(1);

                            if ( (LA93_0=='0') ) {
                                alt93=1;
                            }
                            switch (alt93) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt92=2;
                                    int LA92_0 = input.LA(1);

                                    if ( (LA92_0=='0') ) {
                                        alt92=1;
                                    }
                                    switch (alt92) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:41: ( '0' ( '0' )? )?
                                            int alt91=2;
                                            int LA91_0 = input.LA(1);

                                            if ( (LA91_0=='0') ) {
                                                alt91=1;
                                            }
                                            switch (alt91) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:46: ( '0' )?
                                                    int alt90=2;
                                                    int LA90_0 = input.LA(1);

                                                    if ( (LA90_0=='0') ) {
                                                        alt90=1;
                                                    }
                                                    switch (alt90) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:667:17: ( ( 'p' | 'P' ) | '\\\\' ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) ) )
            int alt101=2;
            int LA101_0 = input.LA(1);

            if ( (LA101_0=='P'||LA101_0=='p') ) {
                alt101=1;
            }
            else if ( (LA101_0=='\\') ) {
                alt101=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 101, 0, input);

                throw nvae;
            }
            switch (alt101) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:667:21: ( 'p' | 'P' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:19: '\\\\' ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:669:25: ( 'p' | 'P' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' ) )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 100, 0, input);

                        throw nvae;
                    }

                    switch (alt100) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:31: 'p'
                            {
                            match('p'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:671:31: 'P'
                            {
                            match('P'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:672:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '0' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:672:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt99=2;
                            int LA99_0 = input.LA(1);

                            if ( (LA99_0=='0') ) {
                                alt99=1;
                            }
                            switch (alt99) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:672:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:672:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt98=2;
                                    int LA98_0 = input.LA(1);

                                    if ( (LA98_0=='0') ) {
                                        alt98=1;
                                    }
                                    switch (alt98) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:672:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:672:41: ( '0' ( '0' )? )?
                                            int alt97=2;
                                            int LA97_0 = input.LA(1);

                                            if ( (LA97_0=='0') ) {
                                                alt97=1;
                                            }
                                            switch (alt97) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:672:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:672:46: ( '0' )?
                                                    int alt96=2;
                                                    int LA96_0 = input.LA(1);

                                                    if ( (LA96_0=='0') ) {
                                                        alt96=1;
                                                    }
                                                    switch (alt96) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:672:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:672:66: ( '0' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:672:67: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:675:17: ( ( 'q' | 'Q' ) | '\\\\' ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) ) )
            int alt107=2;
            int LA107_0 = input.LA(1);

            if ( (LA107_0=='Q'||LA107_0=='q') ) {
                alt107=1;
            }
            else if ( (LA107_0=='\\') ) {
                alt107=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 107, 0, input);

                throw nvae;
            }
            switch (alt107) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:675:21: ( 'q' | 'Q' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:676:19: '\\\\' ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:677:25: ( 'q' | 'Q' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' ) )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 106, 0, input);

                        throw nvae;
                    }

                    switch (alt106) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:678:31: 'q'
                            {
                            match('q'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:679:31: 'Q'
                            {
                            match('Q'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '1' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt105=2;
                            int LA105_0 = input.LA(1);

                            if ( (LA105_0=='0') ) {
                                alt105=1;
                            }
                            switch (alt105) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt104=2;
                                    int LA104_0 = input.LA(1);

                                    if ( (LA104_0=='0') ) {
                                        alt104=1;
                                    }
                                    switch (alt104) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:41: ( '0' ( '0' )? )?
                                            int alt103=2;
                                            int LA103_0 = input.LA(1);

                                            if ( (LA103_0=='0') ) {
                                                alt103=1;
                                            }
                                            switch (alt103) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:46: ( '0' )?
                                                    int alt102=2;
                                                    int LA102_0 = input.LA(1);

                                                    if ( (LA102_0=='0') ) {
                                                        alt102=1;
                                                    }
                                                    switch (alt102) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:66: ( '1' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:67: '1'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:683:17: ( ( 'r' | 'R' ) | '\\\\' ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) ) )
            int alt113=2;
            int LA113_0 = input.LA(1);

            if ( (LA113_0=='R'||LA113_0=='r') ) {
                alt113=1;
            }
            else if ( (LA113_0=='\\') ) {
                alt113=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 113, 0, input);

                throw nvae;
            }
            switch (alt113) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:683:21: ( 'r' | 'R' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:684:19: '\\\\' ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:685:25: ( 'r' | 'R' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' ) )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 112, 0, input);

                        throw nvae;
                    }

                    switch (alt112) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:686:31: 'r'
                            {
                            match('r'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:687:31: 'R'
                            {
                            match('R'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:688:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '2' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:688:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt111=2;
                            int LA111_0 = input.LA(1);

                            if ( (LA111_0=='0') ) {
                                alt111=1;
                            }
                            switch (alt111) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:688:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:688:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt110=2;
                                    int LA110_0 = input.LA(1);

                                    if ( (LA110_0=='0') ) {
                                        alt110=1;
                                    }
                                    switch (alt110) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:688:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:688:41: ( '0' ( '0' )? )?
                                            int alt109=2;
                                            int LA109_0 = input.LA(1);

                                            if ( (LA109_0=='0') ) {
                                                alt109=1;
                                            }
                                            switch (alt109) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:688:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:688:46: ( '0' )?
                                                    int alt108=2;
                                                    int LA108_0 = input.LA(1);

                                                    if ( (LA108_0=='0') ) {
                                                        alt108=1;
                                                    }
                                                    switch (alt108) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:688:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:688:66: ( '2' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:688:67: '2'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:17: ( ( 's' | 'S' ) | '\\\\' ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) ) )
            int alt119=2;
            int LA119_0 = input.LA(1);

            if ( (LA119_0=='S'||LA119_0=='s') ) {
                alt119=1;
            }
            else if ( (LA119_0=='\\') ) {
                alt119=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 119, 0, input);

                throw nvae;
            }
            switch (alt119) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:21: ( 's' | 'S' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:692:19: '\\\\' ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:693:25: ( 's' | 'S' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' ) )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 118, 0, input);

                        throw nvae;
                    }

                    switch (alt118) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:694:31: 's'
                            {
                            match('s'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:31: 'S'
                            {
                            match('S'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:696:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '3' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:696:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt117=2;
                            int LA117_0 = input.LA(1);

                            if ( (LA117_0=='0') ) {
                                alt117=1;
                            }
                            switch (alt117) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:696:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:696:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt116=2;
                                    int LA116_0 = input.LA(1);

                                    if ( (LA116_0=='0') ) {
                                        alt116=1;
                                    }
                                    switch (alt116) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:696:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:696:41: ( '0' ( '0' )? )?
                                            int alt115=2;
                                            int LA115_0 = input.LA(1);

                                            if ( (LA115_0=='0') ) {
                                                alt115=1;
                                            }
                                            switch (alt115) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:696:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:696:46: ( '0' )?
                                                    int alt114=2;
                                                    int LA114_0 = input.LA(1);

                                                    if ( (LA114_0=='0') ) {
                                                        alt114=1;
                                                    }
                                                    switch (alt114) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:696:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:696:66: ( '3' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:696:67: '3'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:17: ( ( 't' | 'T' ) | '\\\\' ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) ) )
            int alt125=2;
            int LA125_0 = input.LA(1);

            if ( (LA125_0=='T'||LA125_0=='t') ) {
                alt125=1;
            }
            else if ( (LA125_0=='\\') ) {
                alt125=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 125, 0, input);

                throw nvae;
            }
            switch (alt125) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:21: ( 't' | 'T' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:700:19: '\\\\' ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:701:25: ( 't' | 'T' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' ) )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 124, 0, input);

                        throw nvae;
                    }

                    switch (alt124) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:702:31: 't'
                            {
                            match('t'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:703:31: 'T'
                            {
                            match('T'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '4' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt123=2;
                            int LA123_0 = input.LA(1);

                            if ( (LA123_0=='0') ) {
                                alt123=1;
                            }
                            switch (alt123) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt122=2;
                                    int LA122_0 = input.LA(1);

                                    if ( (LA122_0=='0') ) {
                                        alt122=1;
                                    }
                                    switch (alt122) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:41: ( '0' ( '0' )? )?
                                            int alt121=2;
                                            int LA121_0 = input.LA(1);

                                            if ( (LA121_0=='0') ) {
                                                alt121=1;
                                            }
                                            switch (alt121) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:46: ( '0' )?
                                                    int alt120=2;
                                                    int LA120_0 = input.LA(1);

                                                    if ( (LA120_0=='0') ) {
                                                        alt120=1;
                                                    }
                                                    switch (alt120) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:66: ( '4' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:67: '4'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:707:17: ( ( 'u' | 'U' ) | '\\\\' ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) ) )
            int alt131=2;
            int LA131_0 = input.LA(1);

            if ( (LA131_0=='U'||LA131_0=='u') ) {
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:707:21: ( 'u' | 'U' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:19: '\\\\' ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:709:25: ( 'u' | 'U' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' ) )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 130, 0, input);

                        throw nvae;
                    }

                    switch (alt130) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:710:31: 'u'
                            {
                            match('u'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:711:31: 'U'
                            {
                            match('U'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '5' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt129=2;
                            int LA129_0 = input.LA(1);

                            if ( (LA129_0=='0') ) {
                                alt129=1;
                            }
                            switch (alt129) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt128=2;
                                    int LA128_0 = input.LA(1);

                                    if ( (LA128_0=='0') ) {
                                        alt128=1;
                                    }
                                    switch (alt128) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:41: ( '0' ( '0' )? )?
                                            int alt127=2;
                                            int LA127_0 = input.LA(1);

                                            if ( (LA127_0=='0') ) {
                                                alt127=1;
                                            }
                                            switch (alt127) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:46: ( '0' )?
                                                    int alt126=2;
                                                    int LA126_0 = input.LA(1);

                                                    if ( (LA126_0=='0') ) {
                                                        alt126=1;
                                                    }
                                                    switch (alt126) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:66: ( '5' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:712:67: '5'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:715:17: ( ( 'v' | 'V' ) | '\\\\' ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) ) )
            int alt137=2;
            int LA137_0 = input.LA(1);

            if ( (LA137_0=='V'||LA137_0=='v') ) {
                alt137=1;
            }
            else if ( (LA137_0=='\\') ) {
                alt137=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 137, 0, input);

                throw nvae;
            }
            switch (alt137) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:715:21: ( 'v' | 'V' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:716:19: '\\\\' ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:717:25: ( 'v' | 'V' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' ) )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 136, 0, input);

                        throw nvae;
                    }

                    switch (alt136) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:717:31: 'v'
                            {
                            match('v'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:31: 'V'
                            {
                            match('V'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '6' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt135=2;
                            int LA135_0 = input.LA(1);

                            if ( (LA135_0=='0') ) {
                                alt135=1;
                            }
                            switch (alt135) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt134=2;
                                    int LA134_0 = input.LA(1);

                                    if ( (LA134_0=='0') ) {
                                        alt134=1;
                                    }
                                    switch (alt134) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:41: ( '0' ( '0' )? )?
                                            int alt133=2;
                                            int LA133_0 = input.LA(1);

                                            if ( (LA133_0=='0') ) {
                                                alt133=1;
                                            }
                                            switch (alt133) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:46: ( '0' )?
                                                    int alt132=2;
                                                    int LA132_0 = input.LA(1);

                                                    if ( (LA132_0=='0') ) {
                                                        alt132=1;
                                                    }
                                                    switch (alt132) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:66: ( '6' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:67: '6'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:17: ( ( 'w' | 'W' ) | '\\\\' ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) ) )
            int alt143=2;
            int LA143_0 = input.LA(1);

            if ( (LA143_0=='W'||LA143_0=='w') ) {
                alt143=1;
            }
            else if ( (LA143_0=='\\') ) {
                alt143=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 143, 0, input);

                throw nvae;
            }
            switch (alt143) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:722:21: ( 'w' | 'W' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:723:19: '\\\\' ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:724:25: ( 'w' | 'W' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' ) )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 142, 0, input);

                        throw nvae;
                    }

                    switch (alt142) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:725:31: 'w'
                            {
                            match('w'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:726:31: 'W'
                            {
                            match('W'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '7' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt141=2;
                            int LA141_0 = input.LA(1);

                            if ( (LA141_0=='0') ) {
                                alt141=1;
                            }
                            switch (alt141) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt140=2;
                                    int LA140_0 = input.LA(1);

                                    if ( (LA140_0=='0') ) {
                                        alt140=1;
                                    }
                                    switch (alt140) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:41: ( '0' ( '0' )? )?
                                            int alt139=2;
                                            int LA139_0 = input.LA(1);

                                            if ( (LA139_0=='0') ) {
                                                alt139=1;
                                            }
                                            switch (alt139) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:46: ( '0' )?
                                                    int alt138=2;
                                                    int LA138_0 = input.LA(1);

                                                    if ( (LA138_0=='0') ) {
                                                        alt138=1;
                                                    }
                                                    switch (alt138) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:66: ( '7' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:727:67: '7'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:730:17: ( ( 'x' | 'X' ) | '\\\\' ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) ) )
            int alt149=2;
            int LA149_0 = input.LA(1);

            if ( (LA149_0=='X'||LA149_0=='x') ) {
                alt149=1;
            }
            else if ( (LA149_0=='\\') ) {
                alt149=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 149, 0, input);

                throw nvae;
            }
            switch (alt149) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:730:21: ( 'x' | 'X' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:19: '\\\\' ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:732:25: ( 'x' | 'X' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' ) )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 148, 0, input);

                        throw nvae;
                    }

                    switch (alt148) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:733:31: 'x'
                            {
                            match('x'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:734:31: 'X'
                            {
                            match('X'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '8' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt147=2;
                            int LA147_0 = input.LA(1);

                            if ( (LA147_0=='0') ) {
                                alt147=1;
                            }
                            switch (alt147) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt146=2;
                                    int LA146_0 = input.LA(1);

                                    if ( (LA146_0=='0') ) {
                                        alt146=1;
                                    }
                                    switch (alt146) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:41: ( '0' ( '0' )? )?
                                            int alt145=2;
                                            int LA145_0 = input.LA(1);

                                            if ( (LA145_0=='0') ) {
                                                alt145=1;
                                            }
                                            switch (alt145) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:46: ( '0' )?
                                                    int alt144=2;
                                                    int LA144_0 = input.LA(1);

                                                    if ( (LA144_0=='0') ) {
                                                        alt144=1;
                                                    }
                                                    switch (alt144) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:66: ( '8' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:67: '8'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:738:17: ( ( 'y' | 'Y' ) | '\\\\' ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) ) )
            int alt155=2;
            int LA155_0 = input.LA(1);

            if ( (LA155_0=='Y'||LA155_0=='y') ) {
                alt155=1;
            }
            else if ( (LA155_0=='\\') ) {
                alt155=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 155, 0, input);

                throw nvae;
            }
            switch (alt155) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:738:21: ( 'y' | 'Y' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:19: '\\\\' ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:740:25: ( 'y' | 'Y' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' ) )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 154, 0, input);

                        throw nvae;
                    }

                    switch (alt154) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:741:31: 'y'
                            {
                            match('y'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:742:31: 'Y'
                            {
                            match('Y'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( '9' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt153=2;
                            int LA153_0 = input.LA(1);

                            if ( (LA153_0=='0') ) {
                                alt153=1;
                            }
                            switch (alt153) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt152=2;
                                    int LA152_0 = input.LA(1);

                                    if ( (LA152_0=='0') ) {
                                        alt152=1;
                                    }
                                    switch (alt152) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:41: ( '0' ( '0' )? )?
                                            int alt151=2;
                                            int LA151_0 = input.LA(1);

                                            if ( (LA151_0=='0') ) {
                                                alt151=1;
                                            }
                                            switch (alt151) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:46: ( '0' )?
                                                    int alt150=2;
                                                    int LA150_0 = input.LA(1);

                                                    if ( (LA150_0=='0') ) {
                                                        alt150=1;
                                                    }
                                                    switch (alt150) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:46: '0'
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:66: ( '9' )
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:67: '9'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:746:17: ( ( 'z' | 'Z' ) | '\\\\' ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) ) )
            int alt161=2;
            int LA161_0 = input.LA(1);

            if ( (LA161_0=='Z'||LA161_0=='z') ) {
                alt161=1;
            }
            else if ( (LA161_0=='\\') ) {
                alt161=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 161, 0, input);

                throw nvae;
            }
            switch (alt161) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:746:21: ( 'z' | 'Z' )
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


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:747:19: '\\\\' ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) )
                    {
                    match('\\'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:748:25: ( 'z' | 'Z' | ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' ) )
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
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 160, 0, input);

                        throw nvae;
                    }

                    switch (alt160) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:749:31: 'z'
                            {
                            match('z'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:750:31: 'Z'
                            {
                            match('Z'); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )? ( '5' | '7' ) ( 'A' | 'a' )
                            {
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:31: ( '0' ( '0' ( '0' ( '0' )? )? )? )?
                            int alt159=2;
                            int LA159_0 = input.LA(1);

                            if ( (LA159_0=='0') ) {
                                alt159=1;
                            }
                            switch (alt159) {
                                case 1 :
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:32: '0' ( '0' ( '0' ( '0' )? )? )?
                                    {
                                    match('0'); if (state.failed) return ;
                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:36: ( '0' ( '0' ( '0' )? )? )?
                                    int alt158=2;
                                    int LA158_0 = input.LA(1);

                                    if ( (LA158_0=='0') ) {
                                        alt158=1;
                                    }
                                    switch (alt158) {
                                        case 1 :
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:37: '0' ( '0' ( '0' )? )?
                                            {
                                            match('0'); if (state.failed) return ;
                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:41: ( '0' ( '0' )? )?
                                            int alt157=2;
                                            int LA157_0 = input.LA(1);

                                            if ( (LA157_0=='0') ) {
                                                alt157=1;
                                            }
                                            switch (alt157) {
                                                case 1 :
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:42: '0' ( '0' )?
                                                    {
                                                    match('0'); if (state.failed) return ;
                                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:46: ( '0' )?
                                                    int alt156=2;
                                                    int LA156_0 = input.LA(1);

                                                    if ( (LA156_0=='0') ) {
                                                        alt156=1;
                                                    }
                                                    switch (alt156) {
                                                        case 1 :
                                                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:46: '0'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:762:17: ( '/*' ( options {greedy=false; } : ( . )* ) '*/' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:762:19: '/*' ( options {greedy=false; } : ( . )* ) '*/'
            {
            match("/*"); if (state.failed) return ;

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:762:24: ( options {greedy=false; } : ( . )* )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:762:54: ( . )*
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:762:54: ( . )*
            loop162:
            do {
                int alt162=2;
                int LA162_0 = input.LA(1);

                if ( (LA162_0=='*') ) {
                    int LA162_1 = input.LA(2);

                    if ( (LA162_1=='/') ) {
                        alt162=2;
                    }
                    else if ( ((LA162_1>='\u0000' && LA162_1<='.')||(LA162_1>='0' && LA162_1<='\uFFFF')) ) {
                        alt162=1;
                    }


                }
                else if ( ((LA162_0>='\u0000' && LA162_0<=')')||(LA162_0>='+' && LA162_0<='\uFFFF')) ) {
                    alt162=1;
                }


                switch (alt162) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:762:54: .
            	    {
            	    matchAny(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop162;
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:775:17: ( '<!--' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:775:19: '<!--'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:788:17: ( '-->' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:788:19: '-->'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:17: ( '~=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:19: '~='
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:796:17: ( '|=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:796:19: '|='
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:798:17: ( '>' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:798:19: '>'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:799:17: ( '{' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:799:19: '{'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:17: ( '}' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:800:19: '}'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:801:17: ( '[' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:801:19: '['
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:802:17: ( ']' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:802:19: ']'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:803:17: ( '=' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:803:19: '='
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:17: ( ';' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:804:19: ';'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:805:17: ( ':' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:805:19: ':'
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

    // $ANTLR start "DCOLON"
    public final void mDCOLON() throws RecognitionException {
        try {
            int _type = DCOLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:806:17: ( '::' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:806:19: '::'
            {
            match("::"); if (state.failed) return ;


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DCOLON"

    // $ANTLR start "SOLIDUS"
    public final void mSOLIDUS() throws RecognitionException {
        try {
            int _type = SOLIDUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:807:17: ( '/' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:807:19: '/'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:17: ( '-' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:808:19: '-'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:809:17: ( '+' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:809:19: '+'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:810:17: ( '*' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:810:19: '*'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:811:17: ( '(' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:811:19: '('
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:812:17: ( ')' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:812:19: ')'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:813:17: ( ',' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:813:19: ','
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:814:17: ( '.' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:814:19: '.'
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:815:8: ( '~' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:815:10: '~'
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

    // $ANTLR start "PIPE"
    public final void mPIPE() throws RecognitionException {
        try {
            int _type = PIPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:816:17: ( '|' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:816:19: '|'
            {
            match('|'); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PIPE"

    // $ANTLR start "INVALID"
    public final void mINVALID() throws RecognitionException {
        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:821:21: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:821:22: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:822:17: ( '\\'' (~ ( '\\n' | '\\r' | '\\f' | '\\'' ) )* ( '\\'' | ) | '\"' (~ ( '\\n' | '\\r' | '\\f' | '\"' ) )* ( '\"' | ) )
            int alt167=2;
            int LA167_0 = input.LA(1);

            if ( (LA167_0=='\'') ) {
                alt167=1;
            }
            else if ( (LA167_0=='\"') ) {
                alt167=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 167, 0, input);

                throw nvae;
            }
            switch (alt167) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:822:19: '\\'' (~ ( '\\n' | '\\r' | '\\f' | '\\'' ) )* ( '\\'' | )
                    {
                    match('\''); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:822:24: (~ ( '\\n' | '\\r' | '\\f' | '\\'' ) )*
                    loop163:
                    do {
                        int alt163=2;
                        int LA163_0 = input.LA(1);

                        if ( ((LA163_0>='\u0000' && LA163_0<='\t')||LA163_0=='\u000B'||(LA163_0>='\u000E' && LA163_0<='&')||(LA163_0>='(' && LA163_0<='\uFFFF')) ) {
                            alt163=1;
                        }


                        switch (alt163) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:822:26: ~ ( '\\n' | '\\r' | '\\f' | '\\'' )
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
                    	    break loop163;
                        }
                    } while (true);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:823:21: ( '\\'' | )
                    int alt164=2;
                    int LA164_0 = input.LA(1);

                    if ( (LA164_0=='\'') ) {
                        alt164=1;
                    }
                    else {
                        alt164=2;}
                    switch (alt164) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:824:27: '\\''
                            {
                            match('\''); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:825:27: 
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:19: '\"' (~ ( '\\n' | '\\r' | '\\f' | '\"' ) )* ( '\"' | )
                    {
                    match('\"'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:23: (~ ( '\\n' | '\\r' | '\\f' | '\"' ) )*
                    loop165:
                    do {
                        int alt165=2;
                        int LA165_0 = input.LA(1);

                        if ( ((LA165_0>='\u0000' && LA165_0<='\t')||LA165_0=='\u000B'||(LA165_0>='\u000E' && LA165_0<='!')||(LA165_0>='#' && LA165_0<='\uFFFF')) ) {
                            alt165=1;
                        }


                        switch (alt165) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:828:25: ~ ( '\\n' | '\\r' | '\\f' | '\"' )
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
                    	    break loop165;
                        }
                    } while (true);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:829:21: ( '\"' | )
                    int alt166=2;
                    int LA166_0 = input.LA(1);

                    if ( (LA166_0=='\"') ) {
                        alt166=1;
                    }
                    else {
                        alt166=2;}
                    switch (alt166) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:830:27: '\"'
                            {
                            match('\"'); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:831:27: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:17: ( ( '-' )? NMSTART ( NMCHAR )* )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:19: ( '-' )? NMSTART ( NMCHAR )*
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:19: ( '-' )?
            int alt168=2;
            int LA168_0 = input.LA(1);

            if ( (LA168_0=='-') ) {
                alt168=1;
            }
            switch (alt168) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:19: '-'
                    {
                    match('-'); if (state.failed) return ;

                    }
                    break;

            }

            mNMSTART(); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:32: ( NMCHAR )*
            loop169:
            do {
                int alt169=2;
                int LA169_0 = input.LA(1);

                if ( (LA169_0=='-'||(LA169_0>='0' && LA169_0<='9')||(LA169_0>='A' && LA169_0<='Z')||LA169_0=='\\'||LA169_0=='_'||(LA169_0>='a' && LA169_0<='z')||(LA169_0>='\u0080' && LA169_0<='\uFFFF')) ) {
                    alt169=1;
                }


                switch (alt169) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:838:32: NMCHAR
            	    {
            	    mNMCHAR(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop169;
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:843:17: ( '#' NAME )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:843:19: '#' NAME
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:845:17: ( '@' I M P O R T )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:845:19: '@' I M P O R T
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:846:17: ( '@' P A G E )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:846:19: '@' P A G E
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:847:17: ( '@' M E D I A )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:847:19: '@' M E D I A
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:848:17: ( '@charset ' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:848:19: '@charset '
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

    // $ANTLR start "NAMESPACE_SYM"
    public final void mNAMESPACE_SYM() throws RecognitionException {
        try {
            int _type = NAMESPACE_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:849:21: ( '@' N A M E S P A C E )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:849:23: '@' N A M E S P A C E
            {
            match('@'); if (state.failed) return ;
            mN(); if (state.failed) return ;
            mA(); if (state.failed) return ;
            mM(); if (state.failed) return ;
            mE(); if (state.failed) return ;
            mS(); if (state.failed) return ;
            mP(); if (state.failed) return ;
            mA(); if (state.failed) return ;
            mC(); if (state.failed) return ;
            mE(); if (state.failed) return ;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NAMESPACE_SYM"

    // $ANTLR start "IMPORTANT_SYM"
    public final void mIMPORTANT_SYM() throws RecognitionException {
        try {
            int _type = IMPORTANT_SYM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:17: ( '!' ( WS | COMMENT )* I M P O R T A N T )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:19: '!' ( WS | COMMENT )* I M P O R T A N T
            {
            match('!'); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:23: ( WS | COMMENT )*
            loop170:
            do {
                int alt170=3;
                int LA170_0 = input.LA(1);

                if ( (LA170_0=='\t'||LA170_0==' ') ) {
                    alt170=1;
                }
                else if ( (LA170_0=='/') ) {
                    alt170=2;
                }


                switch (alt170) {
            	case 1 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:24: WS
            	    {
            	    mWS(); if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:851:27: COMMENT
            	    {
            	    mCOMMENT(); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop170;
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:863:26: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:864:26: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:865:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:865:26: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:866:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:866:26: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:867:26: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:868:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:868:26: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:869:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:869:26: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:870:25: ()
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:870:26: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:873:5: ( ( ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ ) ( ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R A D )=> R A D | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | '%' | ) )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:873:9: ( ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ ) ( ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R A D )=> R A D | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | '%' | )
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:873:9: ( ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )? | '.' ( '0' .. '9' )+ )
            int alt175=2;
            int LA175_0 = input.LA(1);

            if ( ((LA175_0>='0' && LA175_0<='9')) ) {
                alt175=1;
            }
            else if ( (LA175_0=='.') ) {
                alt175=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 175, 0, input);

                throw nvae;
            }
            switch (alt175) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:874:15: ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ )?
                    {
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:874:15: ( '0' .. '9' )+
                    int cnt171=0;
                    loop171:
                    do {
                        int alt171=2;
                        int LA171_0 = input.LA(1);

                        if ( ((LA171_0>='0' && LA171_0<='9')) ) {
                            alt171=1;
                        }


                        switch (alt171) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:874:15: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt171 >= 1 ) break loop171;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(171, input);
                                throw eee;
                        }
                        cnt171++;
                    } while (true);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:874:25: ( '.' ( '0' .. '9' )+ )?
                    int alt173=2;
                    int LA173_0 = input.LA(1);

                    if ( (LA173_0=='.') ) {
                        alt173=1;
                    }
                    switch (alt173) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:874:26: '.' ( '0' .. '9' )+
                            {
                            match('.'); if (state.failed) return ;
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:874:30: ( '0' .. '9' )+
                            int cnt172=0;
                            loop172:
                            do {
                                int alt172=2;
                                int LA172_0 = input.LA(1);

                                if ( ((LA172_0>='0' && LA172_0<='9')) ) {
                                    alt172=1;
                                }


                                switch (alt172) {
                            	case 1 :
                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:874:30: '0' .. '9'
                            	    {
                            	    matchRange('0','9'); if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    if ( cnt172 >= 1 ) break loop172;
                            	    if (state.backtracking>0) {state.failed=true; return ;}
                                        EarlyExitException eee =
                                            new EarlyExitException(172, input);
                                        throw eee;
                                }
                                cnt172++;
                            } while (true);


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:875:15: '.' ( '0' .. '9' )+
                    {
                    match('.'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:875:19: ( '0' .. '9' )+
                    int cnt174=0;
                    loop174:
                    do {
                        int alt174=2;
                        int LA174_0 = input.LA(1);

                        if ( ((LA174_0>='0' && LA174_0<='9')) ) {
                            alt174=1;
                        }


                        switch (alt174) {
                    	case 1 :
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:875:19: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt174 >= 1 ) break loop174;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(174, input);
                                throw eee;
                        }
                        cnt174++;
                    } while (true);


                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:877:9: ( ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R A D )=> R A D | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | '%' | )
            int alt180=12;
            alt180 = dfa180.predict(input);
            switch (alt180) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:878:15: ( E ( M | X ) )=> E ( M | X )
                    {
                    mE(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:880:17: ( M | X )
                    int alt176=2;
                    switch ( input.LA(1) ) {
                    case 'M':
                    case 'm':
                        {
                        alt176=1;
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
                            alt176=1;
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
                                        int LA176_7 = input.LA(6);

                                        if ( (LA176_7=='4'||LA176_7=='6') ) {
                                            alt176=1;
                                        }
                                        else if ( (LA176_7=='5'||LA176_7=='7') ) {
                                            alt176=2;
                                        }
                                        else {
                                            if (state.backtracking>0) {state.failed=true; return ;}
                                            NoViableAltException nvae =
                                                new NoViableAltException("", 176, 7, input);

                                            throw nvae;
                                        }
                                        }
                                        break;
                                    case '4':
                                    case '6':
                                        {
                                        alt176=1;
                                        }
                                        break;
                                    case '5':
                                    case '7':
                                        {
                                        alt176=2;
                                        }
                                        break;
                                    default:
                                        if (state.backtracking>0) {state.failed=true; return ;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 176, 6, input);

                                        throw nvae;
                                    }

                                    }
                                    break;
                                case '4':
                                case '6':
                                    {
                                    alt176=1;
                                    }
                                    break;
                                case '5':
                                case '7':
                                    {
                                    alt176=2;
                                    }
                                    break;
                                default:
                                    if (state.backtracking>0) {state.failed=true; return ;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 176, 5, input);

                                    throw nvae;
                                }

                                }
                                break;
                            case '4':
                            case '6':
                                {
                                alt176=1;
                                }
                                break;
                            case '5':
                            case '7':
                                {
                                alt176=2;
                                }
                                break;
                            default:
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 176, 4, input);

                                throw nvae;
                            }

                            }
                            break;
                        case '5':
                        case '7':
                        case 'X':
                        case 'x':
                            {
                            alt176=2;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 176, 2, input);

                            throw nvae;
                        }

                        }
                        break;
                    case 'X':
                    case 'x':
                        {
                        alt176=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 176, 0, input);

                        throw nvae;
                    }

                    switch (alt176) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:881:23: M
                            {
                            mM(); if (state.failed) return ;
                            if ( state.backtracking==0 ) {
                               _type = EMS;          
                            }

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:882:23: X
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:15: ( P ( X | T | C ) )=> P ( X | T | C )
                    {
                    mP(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:886:17: ( X | T | C )
                    int alt177=3;
                    alt177 = dfa177.predict(input);
                    switch (alt177) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:887:23: X
                            {
                            mX(); if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:888:23: T
                            {
                            mT(); if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:889:23: C
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:892:15: ( C M )=> C M
                    {
                    mC(); if (state.failed) return ;
                    mM(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = LENGTH;       
                    }

                    }
                    break;
                case 4 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:894:15: ( M ( M | S ) )=> M ( M | S )
                    {
                    mM(); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:896:17: ( M | S )
                    int alt178=2;
                    switch ( input.LA(1) ) {
                    case 'M':
                    case 'm':
                        {
                        alt178=1;
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
                            alt178=1;
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
                                        int LA178_7 = input.LA(6);

                                        if ( (LA178_7=='4'||LA178_7=='6') ) {
                                            alt178=1;
                                        }
                                        else if ( (LA178_7=='5'||LA178_7=='7') ) {
                                            alt178=2;
                                        }
                                        else {
                                            if (state.backtracking>0) {state.failed=true; return ;}
                                            NoViableAltException nvae =
                                                new NoViableAltException("", 178, 7, input);

                                            throw nvae;
                                        }
                                        }
                                        break;
                                    case '4':
                                    case '6':
                                        {
                                        alt178=1;
                                        }
                                        break;
                                    case '5':
                                    case '7':
                                        {
                                        alt178=2;
                                        }
                                        break;
                                    default:
                                        if (state.backtracking>0) {state.failed=true; return ;}
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 178, 6, input);

                                        throw nvae;
                                    }

                                    }
                                    break;
                                case '4':
                                case '6':
                                    {
                                    alt178=1;
                                    }
                                    break;
                                case '5':
                                case '7':
                                    {
                                    alt178=2;
                                    }
                                    break;
                                default:
                                    if (state.backtracking>0) {state.failed=true; return ;}
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 178, 5, input);

                                    throw nvae;
                                }

                                }
                                break;
                            case '4':
                            case '6':
                                {
                                alt178=1;
                                }
                                break;
                            case '5':
                            case '7':
                                {
                                alt178=2;
                                }
                                break;
                            default:
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 178, 4, input);

                                throw nvae;
                            }

                            }
                            break;
                        case '5':
                        case '7':
                        case 'S':
                        case 's':
                            {
                            alt178=2;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 178, 2, input);

                            throw nvae;
                        }

                        }
                        break;
                    case 'S':
                    case 's':
                        {
                        alt178=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 178, 0, input);

                        throw nvae;
                    }

                    switch (alt178) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:897:23: M
                            {
                            mM(); if (state.failed) return ;
                            if ( state.backtracking==0 ) {
                               _type = LENGTH;       
                            }

                            }
                            break;
                        case 2 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:899:23: S
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:15: ( I N )=> I N
                    {
                    mI(); if (state.failed) return ;
                    mN(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = LENGTH;       
                    }

                    }
                    break;
                case 6 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:904:15: ( D E G )=> D E G
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:906:15: ( R A D )=> R A D
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:909:15: ( S )=> S
                    {
                    mS(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = TIME;         
                    }

                    }
                    break;
                case 9 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:911:15: ( ( K )? H Z )=> ( K )? H Z
                    {
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:912:17: ( K )?
                    int alt179=2;
                    int LA179_0 = input.LA(1);

                    if ( (LA179_0=='K'||LA179_0=='k') ) {
                        alt179=1;
                    }
                    else if ( (LA179_0=='\\') ) {
                        switch ( input.LA(2) ) {
                            case 'K':
                            case 'k':
                                {
                                alt179=1;
                                }
                                break;
                            case '0':
                                {
                                int LA179_4 = input.LA(3);

                                if ( (LA179_4=='0') ) {
                                    int LA179_6 = input.LA(4);

                                    if ( (LA179_6=='0') ) {
                                        int LA179_7 = input.LA(5);

                                        if ( (LA179_7=='0') ) {
                                            int LA179_8 = input.LA(6);

                                            if ( (LA179_8=='4'||LA179_8=='6') ) {
                                                int LA179_5 = input.LA(7);

                                                if ( (LA179_5=='B'||LA179_5=='b') ) {
                                                    alt179=1;
                                                }
                                            }
                                        }
                                        else if ( (LA179_7=='4'||LA179_7=='6') ) {
                                            int LA179_5 = input.LA(6);

                                            if ( (LA179_5=='B'||LA179_5=='b') ) {
                                                alt179=1;
                                            }
                                        }
                                    }
                                    else if ( (LA179_6=='4'||LA179_6=='6') ) {
                                        int LA179_5 = input.LA(5);

                                        if ( (LA179_5=='B'||LA179_5=='b') ) {
                                            alt179=1;
                                        }
                                    }
                                }
                                else if ( (LA179_4=='4'||LA179_4=='6') ) {
                                    int LA179_5 = input.LA(4);

                                    if ( (LA179_5=='B'||LA179_5=='b') ) {
                                        alt179=1;
                                    }
                                }
                                }
                                break;
                            case '4':
                            case '6':
                                {
                                int LA179_5 = input.LA(3);

                                if ( (LA179_5=='B'||LA179_5=='b') ) {
                                    alt179=1;
                                }
                                }
                                break;
                        }

                    }
                    switch (alt179) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:912:17: K
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
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:914:15: IDENT
                    {
                    mIDENT(); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = DIMENSION;    
                    }

                    }
                    break;
                case 11 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:916:15: '%'
                    {
                    match('%'); if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                       _type = PERCENTAGE;   
                    }

                    }
                    break;
                case 12 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:919:9: 
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:925:5: ( U R L '(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')' )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:925:9: U R L '(' ( ( WS )=> WS )? ( URL | STRING ) ( WS )? ')'
            {
            mU(); if (state.failed) return ;
            mR(); if (state.failed) return ;
            mL(); if (state.failed) return ;
            match('('); if (state.failed) return ;
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:927:13: ( ( WS )=> WS )?
            int alt181=2;
            int LA181_0 = input.LA(1);

            if ( (LA181_0=='\t'||LA181_0==' ') ) {
                int LA181_1 = input.LA(2);

                if ( (synpred10_Css3()) ) {
                    alt181=1;
                }
            }
            switch (alt181) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:927:14: ( WS )=> WS
                    {
                    mWS(); if (state.failed) return ;

                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:927:25: ( URL | STRING )
            int alt182=2;
            int LA182_0 = input.LA(1);

            if ( (LA182_0=='\t'||(LA182_0>=' ' && LA182_0<='!')||(LA182_0>='#' && LA182_0<='&')||(LA182_0>=')' && LA182_0<='*')||(LA182_0>='-' && LA182_0<=':')||(LA182_0>='A' && LA182_0<='\\')||LA182_0=='_'||(LA182_0>='a' && LA182_0<='z')||LA182_0=='~'||(LA182_0>='\u0080' && LA182_0<='\uFFFF')) ) {
                alt182=1;
            }
            else if ( (LA182_0=='\"'||LA182_0=='\'') ) {
                alt182=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 182, 0, input);

                throw nvae;
            }
            switch (alt182) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:927:26: URL
                    {
                    mURL(); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:927:30: STRING
                    {
                    mSTRING(); if (state.failed) return ;

                    }
                    break;

            }

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:927:38: ( WS )?
            int alt183=2;
            int LA183_0 = input.LA(1);

            if ( (LA183_0=='\t'||LA183_0==' ') ) {
                alt183=1;
            }
            switch (alt183) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:927:38: WS
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:937:9: ( ( ' ' | '\\t' )+ )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:937:11: ( ' ' | '\\t' )+
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:937:11: ( ' ' | '\\t' )+
            int cnt184=0;
            loop184:
            do {
                int alt184=2;
                int LA184_0 = input.LA(1);

                if ( (LA184_0=='\t'||LA184_0==' ') ) {
                    alt184=1;
                }


                switch (alt184) {
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
            	    if ( cnt184 >= 1 ) break loop184;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(184, input);
                        throw eee;
                }
                cnt184++;
            } while (true);


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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:9: ( ( '\\r' ( '\\n' )? | '\\n' ) )
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:11: ( '\\r' ( '\\n' )? | '\\n' )
            {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:11: ( '\\r' ( '\\n' )? | '\\n' )
            int alt186=2;
            int LA186_0 = input.LA(1);

            if ( (LA186_0=='\r') ) {
                alt186=1;
            }
            else if ( (LA186_0=='\n') ) {
                alt186=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 186, 0, input);

                throw nvae;
            }
            switch (alt186) {
                case 1 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:12: '\\r' ( '\\n' )?
                    {
                    match('\r'); if (state.failed) return ;
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:17: ( '\\n' )?
                    int alt185=2;
                    int LA185_0 = input.LA(1);

                    if ( (LA185_0=='\n') ) {
                        alt185=1;
                    }
                    switch (alt185) {
                        case 1 :
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:17: '\\n'
                            {
                            match('\n'); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:938:25: '\\n'
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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:8: ( GEN | COMMENT | CDO | CDC | INCLUDES | DASHMATCH | GREATER | LBRACE | RBRACE | LBRACKET | RBRACKET | OPEQ | SEMI | COLON | DCOLON | SOLIDUS | MINUS | PLUS | STAR | LPAREN | RPAREN | COMMA | DOT | TILDE | PIPE | STRING | IDENT | HASH | IMPORT_SYM | PAGE_SYM | MEDIA_SYM | CHARSET_SYM | NAMESPACE_SYM | IMPORTANT_SYM | NUMBER | URI | WS | NL )
        int alt187=38;
        alt187 = dfa187.predict(input);
        switch (alt187) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:10: GEN
                {
                mGEN(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:14: COMMENT
                {
                mCOMMENT(); if (state.failed) return ;

                }
                break;
            case 3 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:22: CDO
                {
                mCDO(); if (state.failed) return ;

                }
                break;
            case 4 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:26: CDC
                {
                mCDC(); if (state.failed) return ;

                }
                break;
            case 5 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:30: INCLUDES
                {
                mINCLUDES(); if (state.failed) return ;

                }
                break;
            case 6 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:39: DASHMATCH
                {
                mDASHMATCH(); if (state.failed) return ;

                }
                break;
            case 7 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:49: GREATER
                {
                mGREATER(); if (state.failed) return ;

                }
                break;
            case 8 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:57: LBRACE
                {
                mLBRACE(); if (state.failed) return ;

                }
                break;
            case 9 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:64: RBRACE
                {
                mRBRACE(); if (state.failed) return ;

                }
                break;
            case 10 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:71: LBRACKET
                {
                mLBRACKET(); if (state.failed) return ;

                }
                break;
            case 11 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:80: RBRACKET
                {
                mRBRACKET(); if (state.failed) return ;

                }
                break;
            case 12 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:89: OPEQ
                {
                mOPEQ(); if (state.failed) return ;

                }
                break;
            case 13 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:94: SEMI
                {
                mSEMI(); if (state.failed) return ;

                }
                break;
            case 14 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:99: COLON
                {
                mCOLON(); if (state.failed) return ;

                }
                break;
            case 15 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:105: DCOLON
                {
                mDCOLON(); if (state.failed) return ;

                }
                break;
            case 16 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:112: SOLIDUS
                {
                mSOLIDUS(); if (state.failed) return ;

                }
                break;
            case 17 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:120: MINUS
                {
                mMINUS(); if (state.failed) return ;

                }
                break;
            case 18 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:126: PLUS
                {
                mPLUS(); if (state.failed) return ;

                }
                break;
            case 19 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:131: STAR
                {
                mSTAR(); if (state.failed) return ;

                }
                break;
            case 20 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:136: LPAREN
                {
                mLPAREN(); if (state.failed) return ;

                }
                break;
            case 21 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:143: RPAREN
                {
                mRPAREN(); if (state.failed) return ;

                }
                break;
            case 22 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:150: COMMA
                {
                mCOMMA(); if (state.failed) return ;

                }
                break;
            case 23 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:156: DOT
                {
                mDOT(); if (state.failed) return ;

                }
                break;
            case 24 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:160: TILDE
                {
                mTILDE(); if (state.failed) return ;

                }
                break;
            case 25 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:166: PIPE
                {
                mPIPE(); if (state.failed) return ;

                }
                break;
            case 26 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:171: STRING
                {
                mSTRING(); if (state.failed) return ;

                }
                break;
            case 27 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:178: IDENT
                {
                mIDENT(); if (state.failed) return ;

                }
                break;
            case 28 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:184: HASH
                {
                mHASH(); if (state.failed) return ;

                }
                break;
            case 29 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:189: IMPORT_SYM
                {
                mIMPORT_SYM(); if (state.failed) return ;

                }
                break;
            case 30 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:200: PAGE_SYM
                {
                mPAGE_SYM(); if (state.failed) return ;

                }
                break;
            case 31 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:209: MEDIA_SYM
                {
                mMEDIA_SYM(); if (state.failed) return ;

                }
                break;
            case 32 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:219: CHARSET_SYM
                {
                mCHARSET_SYM(); if (state.failed) return ;

                }
                break;
            case 33 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:231: NAMESPACE_SYM
                {
                mNAMESPACE_SYM(); if (state.failed) return ;

                }
                break;
            case 34 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:245: IMPORTANT_SYM
                {
                mIMPORTANT_SYM(); if (state.failed) return ;

                }
                break;
            case 35 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:259: NUMBER
                {
                mNUMBER(); if (state.failed) return ;

                }
                break;
            case 36 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:266: URI
                {
                mURI(); if (state.failed) return ;

                }
                break;
            case 37 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:270: WS
                {
                mWS(); if (state.failed) return ;

                }
                break;
            case 38 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:1:273: NL
                {
                mNL(); if (state.failed) return ;

                }
                break;

        }

    }

    // $ANTLR start synpred1_Css3
    public final void synpred1_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:878:15: ( E ( M | X ) )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:878:16: E ( M | X )
        {
        mE(); if (state.failed) return ;
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:878:18: ( M | X )
        int alt188=2;
        switch ( input.LA(1) ) {
        case 'M':
        case 'm':
            {
            alt188=1;
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
                alt188=1;
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
                            int LA188_7 = input.LA(6);

                            if ( (LA188_7=='4'||LA188_7=='6') ) {
                                alt188=1;
                            }
                            else if ( (LA188_7=='5'||LA188_7=='7') ) {
                                alt188=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 188, 7, input);

                                throw nvae;
                            }
                            }
                            break;
                        case '4':
                        case '6':
                            {
                            alt188=1;
                            }
                            break;
                        case '5':
                        case '7':
                            {
                            alt188=2;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 188, 6, input);

                            throw nvae;
                        }

                        }
                        break;
                    case '4':
                    case '6':
                        {
                        alt188=1;
                        }
                        break;
                    case '5':
                    case '7':
                        {
                        alt188=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 188, 5, input);

                        throw nvae;
                    }

                    }
                    break;
                case '4':
                case '6':
                    {
                    alt188=1;
                    }
                    break;
                case '5':
                case '7':
                    {
                    alt188=2;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 188, 4, input);

                    throw nvae;
                }

                }
                break;
            case '5':
            case '7':
            case 'X':
            case 'x':
                {
                alt188=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 188, 2, input);

                throw nvae;
            }

            }
            break;
        case 'X':
        case 'x':
            {
            alt188=2;
            }
            break;
        default:
            if (state.backtracking>0) {state.failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("", 188, 0, input);

            throw nvae;
        }

        switch (alt188) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:878:19: M
                {
                mM(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:878:21: X
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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:15: ( P ( X | T | C ) )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:16: P ( X | T | C )
        {
        mP(); if (state.failed) return ;
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:17: ( X | T | C )
        int alt189=3;
        alt189 = dfa189.predict(input);
        switch (alt189) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:18: X
                {
                mX(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:20: T
                {
                mT(); if (state.failed) return ;

                }
                break;
            case 3 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:884:22: C
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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:892:15: ( C M )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:892:16: C M
        {
        mC(); if (state.failed) return ;
        mM(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_Css3

    // $ANTLR start synpred4_Css3
    public final void synpred4_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:894:15: ( M ( M | S ) )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:894:16: M ( M | S )
        {
        mM(); if (state.failed) return ;
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:894:18: ( M | S )
        int alt190=2;
        switch ( input.LA(1) ) {
        case 'M':
        case 'm':
            {
            alt190=1;
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
                alt190=1;
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
                            int LA190_7 = input.LA(6);

                            if ( (LA190_7=='4'||LA190_7=='6') ) {
                                alt190=1;
                            }
                            else if ( (LA190_7=='5'||LA190_7=='7') ) {
                                alt190=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 190, 7, input);

                                throw nvae;
                            }
                            }
                            break;
                        case '4':
                        case '6':
                            {
                            alt190=1;
                            }
                            break;
                        case '5':
                        case '7':
                            {
                            alt190=2;
                            }
                            break;
                        default:
                            if (state.backtracking>0) {state.failed=true; return ;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 190, 6, input);

                            throw nvae;
                        }

                        }
                        break;
                    case '4':
                    case '6':
                        {
                        alt190=1;
                        }
                        break;
                    case '5':
                    case '7':
                        {
                        alt190=2;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return ;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 190, 5, input);

                        throw nvae;
                    }

                    }
                    break;
                case '4':
                case '6':
                    {
                    alt190=1;
                    }
                    break;
                case '5':
                case '7':
                    {
                    alt190=2;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 190, 4, input);

                    throw nvae;
                }

                }
                break;
            case '5':
            case '7':
            case 'S':
            case 's':
                {
                alt190=2;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 190, 2, input);

                throw nvae;
            }

            }
            break;
        case 'S':
        case 's':
            {
            alt190=2;
            }
            break;
        default:
            if (state.backtracking>0) {state.failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("", 190, 0, input);

            throw nvae;
        }

        switch (alt190) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:894:19: M
                {
                mM(); if (state.failed) return ;

                }
                break;
            case 2 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:894:21: S
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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:15: ( I N )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:901:16: I N
        {
        mI(); if (state.failed) return ;
        mN(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_Css3

    // $ANTLR start synpred6_Css3
    public final void synpred6_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:904:15: ( D E G )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:904:16: D E G
        {
        mD(); if (state.failed) return ;
        mE(); if (state.failed) return ;
        mG(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Css3

    // $ANTLR start synpred7_Css3
    public final void synpred7_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:906:15: ( R A D )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:906:16: R A D
        {
        mR(); if (state.failed) return ;
        mA(); if (state.failed) return ;
        mD(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Css3

    // $ANTLR start synpred8_Css3
    public final void synpred8_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:909:15: ( S )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:909:16: S
        {
        mS(); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred8_Css3

    // $ANTLR start synpred9_Css3
    public final void synpred9_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:911:15: ( ( K )? H Z )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:911:16: ( K )? H Z
        {
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:911:16: ( K )?
        int alt191=2;
        int LA191_0 = input.LA(1);

        if ( (LA191_0=='K'||LA191_0=='k') ) {
            alt191=1;
        }
        else if ( (LA191_0=='\\') ) {
            switch ( input.LA(2) ) {
                case 'K':
                case 'k':
                    {
                    alt191=1;
                    }
                    break;
                case '0':
                    {
                    int LA191_4 = input.LA(3);

                    if ( (LA191_4=='0') ) {
                        int LA191_6 = input.LA(4);

                        if ( (LA191_6=='0') ) {
                            int LA191_7 = input.LA(5);

                            if ( (LA191_7=='0') ) {
                                int LA191_8 = input.LA(6);

                                if ( (LA191_8=='4'||LA191_8=='6') ) {
                                    int LA191_5 = input.LA(7);

                                    if ( (LA191_5=='B'||LA191_5=='b') ) {
                                        alt191=1;
                                    }
                                }
                            }
                            else if ( (LA191_7=='4'||LA191_7=='6') ) {
                                int LA191_5 = input.LA(6);

                                if ( (LA191_5=='B'||LA191_5=='b') ) {
                                    alt191=1;
                                }
                            }
                        }
                        else if ( (LA191_6=='4'||LA191_6=='6') ) {
                            int LA191_5 = input.LA(5);

                            if ( (LA191_5=='B'||LA191_5=='b') ) {
                                alt191=1;
                            }
                        }
                    }
                    else if ( (LA191_4=='4'||LA191_4=='6') ) {
                        int LA191_5 = input.LA(4);

                        if ( (LA191_5=='B'||LA191_5=='b') ) {
                            alt191=1;
                        }
                    }
                    }
                    break;
                case '4':
                case '6':
                    {
                    int LA191_5 = input.LA(3);

                    if ( (LA191_5=='B'||LA191_5=='b') ) {
                        alt191=1;
                    }
                    }
                    break;
            }

        }
        switch (alt191) {
            case 1 :
                // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:911:16: K
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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:927:14: ( WS )
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:927:15: WS
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
    protected DFA180 dfa180 = new DFA180(this);
    protected DFA177 dfa177 = new DFA177(this);
    protected DFA187 dfa187 = new DFA187(this);
    protected DFA189 dfa189 = new DFA189(this);
    static final String DFA11_eotS =
        "\1\1\15\uffff";
    static final String DFA11_eofS =
        "\16\uffff";
    static final String DFA11_minS =
        "\1\41\15\uffff";
    static final String DFA11_maxS =
        "\1\uffff\15\uffff";
    static final String DFA11_acceptS =
        "\1\uffff\1\15\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14";
    static final String DFA11_specialS =
        "\16\uffff}>";
    static final String[] DFA11_transitionS = {
            "\1\3\1\uffff\1\4\1\5\1\6\1\7\3\uffff\1\10\2\uffff\1\15\1\12"+
            "\1\14\12\15\1\13\6\uffff\32\15\1\2\1\15\2\uffff\1\15\1\uffff"+
            "\32\15\3\uffff\1\11\1\uffff\uff80\15",
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
            return "()* loopback of 568:27: ( '[' | '!' | '#' | '$' | '%' | '&' | '*' | '~' | '.' | ':' | '/' | NMCHAR )*";
        }
    }
    static final String DFA180_eotS =
        "\1\30\1\14\1\uffff\6\14\1\uffff\2\14\1\uffff\7\14\1\uffff\2\14\7"+
        "\uffff\13\14\2\uffff\4\14\22\uffff\1\14\1\uffff\2\14\1\uffff\1\14"+
        "\1\uffff\1\14\1\uffff\1\14\7\uffff\2\14\1\uffff\17\14\5\uffff\2"+
        "\14\1\uffff\1\14\3\uffff\2\14\4\uffff\2\14\1\uffff\1\14\3\uffff"+
        "\2\14\3\uffff\2\14\3\uffff\6\14\3\uffff\5\14\3\uffff\16\14\1\uffff"+
        "\2\14\2\uffff\5\14\3\uffff\2\14\2\uffff\3\14\3\uffff\2\14\5\uffff"+
        "\5\14\1\uffff\12\14\2\uffff\3\14\3\uffff\16\14\1\uffff\2\14\2\uffff"+
        "\2\14\2\uffff\3\14\3\uffff\2\14\2\uffff\3\14\3\uffff\2\14\2\uffff"+
        "\2\14\1\uffff\5\14\1\uffff\3\14\2\uffff\5\14\2\uffff\3\14\3\uffff"+
        "\15\14\1\uffff\2\14\2\uffff\2\14\2\uffff\3\14\3\uffff\2\14\2\uffff"+
        "\3\14\3\uffff\2\14\2\uffff\2\14\1\uffff\5\14\1\uffff\3\14\2\uffff"+
        "\5\14\2\uffff\2\14\3\uffff\13\14\1\uffff\2\14\2\uffff\2\14\2\uffff"+
        "\2\14\3\uffff\1\14\2\uffff\2\14\3\uffff\1\14\2\uffff\2\14\1\uffff"+
        "\4\14\1\uffff\2\14\2\uffff\3\14\17\uffff\1\14\1\uffff\2\14\1\uffff"+
        "\1\14\2\uffff\1\14\4\uffff";
    static final String DFA180_eofS =
        "\u01b5\uffff";
    static final String DFA180_minS =
        "\1\45\1\115\1\0\1\103\2\115\1\116\1\105\1\101\1\0\1\110\1\132\1"+
        "\uffff\1\115\1\103\2\115\1\116\1\105\1\101\1\0\1\110\1\132\2\uffff"+
        "\5\0\1\103\1\60\1\63\1\103\1\115\1\60\1\115\2\116\2\101\2\0\2\110"+
        "\2\132\22\0\1\107\1\0\1\107\1\104\1\0\1\104\1\uffff\1\132\1\0\1"+
        "\132\3\0\1\uffff\3\0\1\60\1\104\1\0\1\70\1\60\1\63\1\60\3\115\1"+
        "\116\1\105\1\110\1\132\1\115\1\110\1\103\1\101\1\0\1\uffff\3\0\1"+
        "\60\1\64\1\0\1\63\1\uffff\2\0\1\60\1\104\1\uffff\3\0\1\60\1\104"+
        "\1\0\1\63\1\uffff\2\0\1\60\1\105\3\0\1\60\1\65\3\0\1\60\1\61\2\132"+
        "\1\60\1\70\1\uffff\2\0\1\60\1\101\1\60\1\104\1\70\3\0\1\60\1\63"+
        "\1\60\3\115\1\116\1\105\1\110\1\132\1\115\1\110\1\103\1\101\1\0"+
        "\2\107\2\0\2\104\1\60\1\64\1\63\3\0\1\60\1\104\2\0\1\60\1\104\1"+
        "\63\3\0\1\60\1\105\2\0\1\uffff\2\0\1\60\1\67\1\60\1\65\1\107\1\uffff"+
        "\1\60\1\64\1\60\1\61\1\104\1\60\1\70\1\132\1\60\1\101\2\0\1\60\1"+
        "\104\1\70\3\0\1\64\1\63\1\60\3\115\1\116\1\105\1\110\1\132\1\115"+
        "\1\110\1\103\1\101\1\0\2\107\2\0\2\104\2\0\1\60\1\64\1\63\3\0\1"+
        "\60\1\104\2\0\1\60\1\104\1\63\3\0\1\60\1\105\2\0\1\60\1\67\1\0\1"+
        "\60\1\65\1\107\1\60\1\64\1\0\1\60\1\61\1\104\2\0\1\60\1\70\1\132"+
        "\1\60\1\101\2\0\1\64\1\104\1\70\3\0\1\63\1\60\3\115\1\116\1\105"+
        "\1\110\1\132\1\115\1\110\1\103\1\101\1\0\2\107\2\0\2\104\2\0\2\64"+
        "\1\63\3\0\1\64\1\104\2\0\1\64\1\104\1\63\3\0\1\64\1\105\2\0\1\60"+
        "\1\67\1\0\1\64\1\65\1\107\1\60\1\64\1\0\1\64\1\61\1\104\2\0\1\64"+
        "\1\70\1\132\1\65\1\101\2\0\1\104\1\70\3\0\3\115\1\116\1\105\1\110"+
        "\1\132\1\115\1\110\1\103\1\101\1\0\2\107\2\0\2\104\2\0\1\64\1\63"+
        "\3\0\1\104\2\0\1\104\1\63\3\0\1\105\2\0\1\64\1\67\1\0\1\65\1\107"+
        "\2\64\1\0\1\61\1\104\2\0\1\70\1\132\1\101\17\0\1\67\1\0\1\107\1"+
        "\64\1\0\1\104\2\0\1\132\4\0";
    static final String DFA180_maxS =
        "\1\uffff\1\170\1\uffff\1\170\1\155\1\163\1\156\1\145\1\141\1\0\1"+
        "\150\1\172\1\uffff\2\170\1\155\1\163\1\156\1\145\1\141\1\0\1\150"+
        "\1\172\2\uffff\1\0\1\uffff\3\0\1\170\1\67\1\144\1\170\1\163\1\63"+
        "\1\163\2\156\2\141\2\0\2\150\2\172\1\0\1\uffff\6\0\1\uffff\2\0\1"+
        "\uffff\4\0\1\uffff\1\0\1\147\1\uffff\1\147\1\144\1\uffff\1\144\1"+
        "\uffff\1\172\1\uffff\1\172\1\0\1\uffff\1\0\1\uffff\3\0\1\67\1\144"+
        "\1\0\1\70\1\67\1\144\1\63\1\170\1\155\1\163\1\156\1\145\1\150\1"+
        "\172\1\163\1\150\1\170\1\141\1\0\1\uffff\3\0\1\67\1\70\1\0\1\63"+
        "\1\uffff\2\0\1\66\1\144\1\uffff\3\0\1\67\1\144\1\0\1\63\1\uffff"+
        "\2\0\1\66\1\145\1\0\1\uffff\1\0\1\66\1\65\1\0\1\uffff\1\0\1\66\1"+
        "\61\2\172\1\66\1\70\1\uffff\2\0\1\67\1\141\1\67\1\144\1\70\3\0\1"+
        "\67\1\144\1\63\1\170\1\155\1\163\1\156\1\145\1\150\1\172\1\163\1"+
        "\150\1\170\1\141\1\0\2\147\2\0\2\144\1\67\1\70\1\63\3\0\1\66\1\144"+
        "\2\0\1\67\1\144\1\63\3\0\1\66\1\145\2\0\1\uffff\2\0\1\66\1\67\1"+
        "\66\1\65\1\147\1\uffff\1\66\1\64\1\66\1\61\1\144\1\66\1\70\1\172"+
        "\1\67\1\141\2\0\1\67\1\144\1\70\3\0\1\67\1\144\1\63\1\170\1\155"+
        "\1\163\1\156\1\145\1\150\1\172\1\163\1\150\1\170\1\141\1\0\2\147"+
        "\2\0\2\144\2\0\1\67\1\70\1\63\3\0\1\66\1\144\2\0\1\67\1\144\1\63"+
        "\3\0\1\66\1\145\2\0\1\66\1\67\1\0\1\66\1\65\1\147\1\66\1\64\1\0"+
        "\1\66\1\61\1\144\2\0\1\66\1\70\1\172\1\67\1\141\2\0\1\67\1\144\1"+
        "\70\3\0\1\144\1\63\1\170\1\155\1\163\1\156\1\145\1\150\1\172\1\163"+
        "\1\150\1\170\1\141\1\0\2\147\2\0\2\144\2\0\1\67\1\70\1\63\3\0\1"+
        "\66\1\144\2\0\1\67\1\144\1\63\3\0\1\66\1\145\2\0\1\66\1\67\1\0\1"+
        "\66\1\65\1\147\1\66\1\64\1\0\1\66\1\61\1\144\2\0\1\66\1\70\1\172"+
        "\1\67\1\141\2\0\1\144\1\70\3\0\1\170\1\155\1\163\1\156\1\145\1\150"+
        "\1\172\1\163\1\150\1\170\1\141\1\0\2\147\2\0\2\144\2\0\1\70\1\63"+
        "\3\0\1\144\2\0\1\144\1\63\3\0\1\145\2\0\1\66\1\67\1\0\1\65\1\147"+
        "\1\66\1\64\1\0\1\61\1\144\2\0\1\70\1\172\1\141\17\0\1\67\1\0\1\147"+
        "\1\64\1\0\1\144\2\0\1\172\4\0";
    static final String DFA180_acceptS =
        "\14\uffff\1\12\12\uffff\1\13\1\14\56\uffff\1\10\6\uffff\1\1\26\uffff"+
        "\1\2\7\uffff\1\3\4\uffff\1\4\7\uffff\1\5\22\uffff\1\11\63\uffff"+
        "\1\6\7\uffff\1\7\u00eb\uffff";
    static final String DFA180_specialS =
        "\2\uffff\1\u008e\6\uffff\1\u0096\12\uffff\1\u0095\4\uffff\1\u0092"+
        "\1\64\1\u008b\1\u0093\1\u0089\13\uffff\1\23\1\21\4\uffff\1\u00a5"+
        "\1\107\1\136\1\122\1\u00a3\1\133\1\116\1\164\1\u00a1\1\163\1\45"+
        "\1\57\1\143\1\46\1\144\1\170\1\72\1\171\1\uffff\1\11\2\uffff\1\157"+
        "\3\uffff\1\u009c\1\uffff\1\140\1\173\1\141\1\uffff\1\76\1\77\1\u009a"+
        "\2\uffff\1\u009b\17\uffff\1\110\1\uffff\1\u0099\1\u0091\1\31\2\uffff"+
        "\1\30\2\uffff\1\u0097\1\u0098\3\uffff\1\127\1\130\1\6\2\uffff\1"+
        "\7\2\uffff\1\u0088\1\u0087\2\uffff\1\177\1\65\1\u0080\2\uffff\1"+
        "\40\1\44\1\41\7\uffff\1\117\1\120\5\uffff\1\u00a9\1\u00a8\1\175"+
        "\16\uffff\1\102\2\uffff\1\27\1\26\5\uffff\1\63\1\u0086\1\60\2\uffff"+
        "\1\67\1\70\3\uffff\1\155\1\156\1\32\2\uffff\1\47\1\51\1\uffff\1"+
        "\24\1\22\20\uffff\1\145\1\146\3\uffff\1\134\1\137\1\114\16\uffff"+
        "\1\10\2\uffff\1\u00aa\1\u009f\2\uffff\1\3\1\1\3\uffff\1\125\1\u00a0"+
        "\1\20\2\uffff\1\34\1\33\3\uffff\1\161\1\71\1\u00a6\2\uffff\1\u009e"+
        "\1\u009d\2\uffff\1\u00a7\5\uffff\1\u008c\3\uffff\1\u00ab\1\5\5\uffff"+
        "\1\74\1\75\3\uffff\1\62\1\61\1\25\15\uffff\1\u0081\2\uffff\1\132"+
        "\1\135\2\uffff\1\54\1\55\3\uffff\1\42\1\176\1\u008a\2\uffff\1\u00a4"+
        "\1\u00a2\3\uffff\1\105\1\104\1\162\2\uffff\1\u008f\1\u0090\2\uffff"+
        "\1\u0084\5\uffff\1\4\3\uffff\1\52\1\50\5\uffff\1\13\1\14\2\uffff"+
        "\1\16\1\17\1\73\13\uffff\1\u0085\2\uffff\1\154\1\153\2\uffff\1\160"+
        "\1\165\2\uffff\1\113\1\115\1\174\1\uffff\1\166\1\167\2\uffff\1\56"+
        "\1\53\1\106\1\uffff\1\151\1\152\2\uffff\1\142\4\uffff\1\43\2\uffff"+
        "\1\112\1\111\3\uffff\1\37\1\36\1\100\1\103\1\u0094\1\u008d\1\123"+
        "\1\172\1\u0082\1\u0083\1\0\1\2\1\66\1\150\1\147\1\uffff\1\101\2"+
        "\uffff\1\126\1\uffff\1\124\1\131\1\uffff\1\12\1\15\1\35\1\121}>";
    static final String[] DFA180_transitionS = {
            "\1\27\7\uffff\1\14\23\uffff\2\14\1\17\1\22\1\15\2\14\1\26\1"+
            "\21\1\14\1\25\1\14\1\20\2\14\1\16\1\14\1\23\1\24\7\14\1\uffff"+
            "\1\2\2\uffff\1\14\1\uffff\2\14\1\4\1\7\1\1\2\14\1\13\1\6\1\14"+
            "\1\12\1\14\1\5\2\14\1\3\1\14\1\10\1\11\7\14\5\uffff\uff80\14",
            "\1\34\12\uffff\1\35\3\uffff\1\32\20\uffff\1\31\12\uffff\1\33",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\37\3\14\1\40\1\43\1\40"+
            "\1\43\20\14\1\56\1\46\1\14\1\54\1\14\1\44\2\14\1\41\1\14\1\50"+
            "\1\52\24\14\1\55\1\45\1\14\1\53\1\14\1\42\2\14\1\36\1\14\1\47"+
            "\1\51\uff8c\14",
            "\1\65\20\uffff\1\64\3\uffff\1\63\3\uffff\1\60\6\uffff\1\62"+
            "\20\uffff\1\61\3\uffff\1\57",
            "\1\70\16\uffff\1\67\20\uffff\1\66",
            "\1\74\5\uffff\1\75\10\uffff\1\72\20\uffff\1\71\5\uffff\1\73",
            "\1\100\15\uffff\1\77\21\uffff\1\76",
            "\1\103\26\uffff\1\102\10\uffff\1\101",
            "\1\106\32\uffff\1\105\4\uffff\1\104",
            "\1\uffff",
            "\1\112\23\uffff\1\111\13\uffff\1\110",
            "\1\115\1\uffff\1\114\35\uffff\1\113",
            "",
            "\1\34\12\uffff\1\35\3\uffff\1\32\20\uffff\1\31\12\uffff\1\33",
            "\1\65\20\uffff\1\64\3\uffff\1\63\3\uffff\1\60\6\uffff\1\62"+
            "\20\uffff\1\61\3\uffff\1\57",
            "\1\70\16\uffff\1\67\20\uffff\1\66",
            "\1\74\5\uffff\1\75\10\uffff\1\72\20\uffff\1\71\5\uffff\1\73",
            "\1\100\15\uffff\1\77\21\uffff\1\76",
            "\1\103\26\uffff\1\102\10\uffff\1\101",
            "\1\106\32\uffff\1\105\4\uffff\1\104",
            "\1\uffff",
            "\1\112\23\uffff\1\111\13\uffff\1\110",
            "\1\115\1\uffff\1\114\35\uffff\1\113",
            "",
            "",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\122\3\14\1\123\1\125\1"+
            "\123\1\125\25\14\1\120\12\14\1\124\24\14\1\117\12\14\1\121\uff87"+
            "\14",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\65\20\uffff\1\64\3\uffff\1\63\3\uffff\1\60\6\uffff\1\62"+
            "\20\uffff\1\61\3\uffff\1\57",
            "\1\126\3\uffff\1\127\1\130\1\127\1\130",
            "\1\132\1\135\1\131\2\uffff\1\137\1\134\10\uffff\1\141\1\uffff"+
            "\1\140\35\uffff\1\136\1\uffff\1\133",
            "\1\65\20\uffff\1\64\3\uffff\1\63\3\uffff\1\60\6\uffff\1\62"+
            "\20\uffff\1\61\3\uffff\1\57",
            "\1\74\5\uffff\1\75\10\uffff\1\72\20\uffff\1\71\5\uffff\1\73",
            "\1\142\1\uffff\1\143\1\144",
            "\1\74\5\uffff\1\75\10\uffff\1\72\20\uffff\1\71\5\uffff\1\73",
            "\1\100\15\uffff\1\77\21\uffff\1\76",
            "\1\100\15\uffff\1\77\21\uffff\1\76",
            "\1\106\32\uffff\1\105\4\uffff\1\104",
            "\1\106\32\uffff\1\105\4\uffff\1\104",
            "\1\uffff",
            "\1\uffff",
            "\1\112\23\uffff\1\111\13\uffff\1\110",
            "\1\112\23\uffff\1\111\13\uffff\1\110",
            "\1\115\1\uffff\1\114\35\uffff\1\113",
            "\1\115\1\uffff\1\114\35\uffff\1\113",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\151\3\14\1\154\1\152\1"+
            "\154\1\152\34\14\1\153\3\14\1\147\33\14\1\150\3\14\1\146\uff87"+
            "\14",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\160\3\14\1\161\1\14\1\161"+
            "\26\14\1\157\37\14\1\156\uff92\14",
            "\1\uffff",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\166\3\14\1\167\1\171\1"+
            "\167\1\171\25\14\1\164\5\14\1\170\31\14\1\163\5\14\1\165\uff8c"+
            "\14",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\175\3\14\1\176\1\14\1\176"+
            "\27\14\1\174\37\14\1\173\uff91\14",
            "\1\uffff",
            "\1\u0081\24\uffff\1\u0080\12\uffff\1\177",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u0082\3\14\1\u0083\1\14"+
            "\1\u0083\uffc9\14",
            "\1\u0081\24\uffff\1\u0080\12\uffff\1\177",
            "\1\u0086\27\uffff\1\u0085\7\uffff\1\u0084",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u0087\3\14\1\u0088\1\14"+
            "\1\u0088\uffc9\14",
            "\1\u0086\27\uffff\1\u0085\7\uffff\1\u0084",
            "",
            "\1\115\1\uffff\1\114\35\uffff\1\113",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u008b\3\14\1\u008c\1\14"+
            "\1\u008c\21\14\1\u008a\37\14\1\u0089\uff97\14",
            "\1\115\1\uffff\1\114\35\uffff\1\113",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u0090\4\14\1\u0091\1\14"+
            "\1\u0091\42\14\1\u008f\37\14\1\u008e\uff85\14",
            "\1\uffff",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0092\3\uffff\1\u0093\1\u0094\1\u0093\1\u0094",
            "\1\u0096\37\uffff\1\u0095",
            "\1\uffff",
            "\1\u0097",
            "\1\u0098\3\uffff\1\u0099\1\u009a\1\u0099\1\u009a",
            "\1\u009c\1\u009f\1\u009b\2\uffff\1\u00a1\1\u009e\10\uffff\1"+
            "\u00a3\1\uffff\1\u00a2\35\uffff\1\u00a0\1\uffff\1\u009d",
            "\1\u00a4\1\uffff\1\u00a5\1\u00a6",
            "\1\34\12\uffff\1\35\3\uffff\1\32\20\uffff\1\31\12\uffff\1\33",
            "\1\70\16\uffff\1\67\20\uffff\1\66",
            "\1\74\5\uffff\1\75\10\uffff\1\72\20\uffff\1\71\5\uffff\1\73",
            "\1\100\15\uffff\1\77\21\uffff\1\76",
            "\1\u00a8\26\uffff\1\102\10\uffff\1\u00a7",
            "\1\112\23\uffff\1\111\13\uffff\1\110",
            "\1\115\1\uffff\1\114\35\uffff\1\113",
            "\1\74\5\uffff\1\75\10\uffff\1\72\20\uffff\1\71\5\uffff\1\73",
            "\1\112\23\uffff\1\111\13\uffff\1\110",
            "\1\u00aa\20\uffff\1\64\3\uffff\1\63\3\uffff\1\60\6\uffff\1"+
            "\u00a9\20\uffff\1\61\3\uffff\1\57",
            "\1\u00ac\32\uffff\1\105\4\uffff\1\u00ab",
            "\1\uffff",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u00ad\3\uffff\1\u00af\1\u00ae\1\u00af\1\u00ae",
            "\1\u00b1\3\uffff\1\u00b0",
            "\1\uffff",
            "\1\u00b2",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\u00b3\3\uffff\1\u00b4\1\uffff\1\u00b4",
            "\1\u00b6\37\uffff\1\u00b5",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u00b7\3\uffff\1\u00b8\1\u00b9\1\u00b8\1\u00b9",
            "\1\u00bb\37\uffff\1\u00ba",
            "\1\uffff",
            "\1\u00bc",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\u00bd\3\uffff\1\u00be\1\uffff\1\u00be",
            "\1\u00c0\37\uffff\1\u00bf",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u00c4\3\14\1\u00c5\1\14"+
            "\1\u00c5\20\14\1\u00c3\37\14\1\u00c2\uff98\14",
            "\1\uffff",
            "\1\u00c6\3\uffff\1\u00c7\1\uffff\1\u00c7",
            "\1\u00c8",
            "\1\uffff",
            "\12\14\1\uffff\1\14\2\uffff\42\14\1\u00ca\3\14\1\u00cb\1\14"+
            "\1\u00cb\uffc9\14",
            "\1\uffff",
            "\1\u00cc\3\uffff\1\u00cd\1\uffff\1\u00cd",
            "\1\u00ce",
            "\1\115\1\uffff\1\114\35\uffff\1\113",
            "\1\115\1\uffff\1\114\35\uffff\1\113",
            "\1\u00cf\3\uffff\1\u00d0\1\uffff\1\u00d0",
            "\1\u00d1",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\u00d2\4\uffff\1\u00d3\1\uffff\1\u00d3",
            "\1\u00d5\37\uffff\1\u00d4",
            "\1\u00d6\3\uffff\1\u00d7\1\u00d8\1\u00d7\1\u00d8",
            "\1\u00da\37\uffff\1\u00d9",
            "\1\u00db",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u00dc\3\uffff\1\u00dd\1\u00de\1\u00dd\1\u00de",
            "\1\u00e0\1\u00e3\1\u00df\2\uffff\1\u00e5\1\u00e2\10\uffff\1"+
            "\u00e7\1\uffff\1\u00e6\35\uffff\1\u00e4\1\uffff\1\u00e1",
            "\1\u00e8\1\uffff\1\u00e9\1\u00ea",
            "\1\34\12\uffff\1\35\3\uffff\1\32\20\uffff\1\31\12\uffff\1\33",
            "\1\70\16\uffff\1\67\20\uffff\1\66",
            "\1\74\5\uffff\1\75\10\uffff\1\72\20\uffff\1\71\5\uffff\1\73",
            "\1\100\15\uffff\1\77\21\uffff\1\76",
            "\1\u00ec\26\uffff\1\102\10\uffff\1\u00eb",
            "\1\112\23\uffff\1\111\13\uffff\1\110",
            "\1\115\1\uffff\1\114\35\uffff\1\113",
            "\1\74\5\uffff\1\75\10\uffff\1\72\20\uffff\1\71\5\uffff\1\73",
            "\1\112\23\uffff\1\111\13\uffff\1\110",
            "\1\u00ee\20\uffff\1\64\3\uffff\1\63\3\uffff\1\60\6\uffff\1"+
            "\u00ed\20\uffff\1\61\3\uffff\1\57",
            "\1\u00f0\32\uffff\1\105\4\uffff\1\u00ef",
            "\1\uffff",
            "\1\u0081\24\uffff\1\u0080\12\uffff\1\177",
            "\1\u0081\24\uffff\1\u0080\12\uffff\1\177",
            "\1\uffff",
            "\1\uffff",
            "\1\u00f2\27\uffff\1\u0085\7\uffff\1\u00f1",
            "\1\u00f2\27\uffff\1\u0085\7\uffff\1\u00f1",
            "\1\u00f3\3\uffff\1\u00f5\1\u00f4\1\u00f5\1\u00f4",
            "\1\u00f7\3\uffff\1\u00f6",
            "\1\u00f8",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u00f9\3\uffff\1\u00fa\1\uffff\1\u00fa",
            "\1\u00fc\37\uffff\1\u00fb",
            "\1\uffff",
            "\1\uffff",
            "\1\u00fd\3\uffff\1\u00fe\1\u00ff\1\u00fe\1\u00ff",
            "\1\u0101\37\uffff\1\u0100",
            "\1\u0102",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0103\3\uffff\1\u0104\1\uffff\1\u0104",
            "\1\u0106\37\uffff\1\u0105",
            "\1\uffff",
            "\1\uffff",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\u0107\3\uffff\1\u0108\1\uffff\1\u0108",
            "\1\u0109",
            "\1\u010a\3\uffff\1\u010b\1\uffff\1\u010b",
            "\1\u010c",
            "\1\u0081\24\uffff\1\u0080\12\uffff\1\177",
            "",
            "\1\u010d\3\uffff\1\u010e\1\uffff\1\u010e",
            "\1\u010f",
            "\1\u0110\3\uffff\1\u0111\1\uffff\1\u0111",
            "\1\u0112",
            "\1\u0114\27\uffff\1\u0085\7\uffff\1\u0113",
            "\1\u0115\3\uffff\1\u0116\1\uffff\1\u0116",
            "\1\u0117",
            "\1\115\1\uffff\1\114\35\uffff\1\113",
            "\1\u0118\4\uffff\1\u0119\1\uffff\1\u0119",
            "\1\u011b\37\uffff\1\u011a",
            "\1\uffff",
            "\1\uffff",
            "\1\u011c\3\uffff\1\u011d\1\u011e\1\u011d\1\u011e",
            "\1\u0120\37\uffff\1\u011f",
            "\1\u0121",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0122\1\u0123\1\u0122\1\u0123",
            "\1\u0125\1\u0128\1\u0124\2\uffff\1\u012a\1\u0127\10\uffff\1"+
            "\u012c\1\uffff\1\u012b\35\uffff\1\u0129\1\uffff\1\u0126",
            "\1\u012d\1\uffff\1\u012e\1\u012f",
            "\1\34\12\uffff\1\35\3\uffff\1\32\20\uffff\1\31\12\uffff\1\33",
            "\1\70\16\uffff\1\67\20\uffff\1\66",
            "\1\74\5\uffff\1\75\10\uffff\1\72\20\uffff\1\71\5\uffff\1\73",
            "\1\100\15\uffff\1\77\21\uffff\1\76",
            "\1\u0131\26\uffff\1\102\10\uffff\1\u0130",
            "\1\112\23\uffff\1\111\13\uffff\1\110",
            "\1\115\1\uffff\1\114\35\uffff\1\113",
            "\1\74\5\uffff\1\75\10\uffff\1\72\20\uffff\1\71\5\uffff\1\73",
            "\1\112\23\uffff\1\111\13\uffff\1\110",
            "\1\u0133\20\uffff\1\64\3\uffff\1\63\3\uffff\1\60\6\uffff\1"+
            "\u0132\20\uffff\1\61\3\uffff\1\57",
            "\1\u0135\32\uffff\1\105\4\uffff\1\u0134",
            "\1\uffff",
            "\1\u0081\24\uffff\1\u0080\12\uffff\1\177",
            "\1\u0081\24\uffff\1\u0080\12\uffff\1\177",
            "\1\uffff",
            "\1\uffff",
            "\1\u0137\27\uffff\1\u0085\7\uffff\1\u0136",
            "\1\u0137\27\uffff\1\u0085\7\uffff\1\u0136",
            "\1\uffff",
            "\1\uffff",
            "\1\u0138\3\uffff\1\u013a\1\u0139\1\u013a\1\u0139",
            "\1\u013c\3\uffff\1\u013b",
            "\1\u013d",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u013e\3\uffff\1\u013f\1\uffff\1\u013f",
            "\1\u0141\37\uffff\1\u0140",
            "\1\uffff",
            "\1\uffff",
            "\1\u0142\3\uffff\1\u0143\1\u0144\1\u0143\1\u0144",
            "\1\u0146\37\uffff\1\u0145",
            "\1\u0147",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0148\3\uffff\1\u0149\1\uffff\1\u0149",
            "\1\u014b\37\uffff\1\u014a",
            "\1\uffff",
            "\1\uffff",
            "\1\u014c\3\uffff\1\u014d\1\uffff\1\u014d",
            "\1\u014e",
            "\1\uffff",
            "\1\u014f\3\uffff\1\u0150\1\uffff\1\u0150",
            "\1\u0151",
            "\1\u0081\24\uffff\1\u0080\12\uffff\1\177",
            "\1\u0152\3\uffff\1\u0153\1\uffff\1\u0153",
            "\1\u0154",
            "\1\uffff",
            "\1\u0155\3\uffff\1\u0156\1\uffff\1\u0156",
            "\1\u0157",
            "\1\u0159\27\uffff\1\u0085\7\uffff\1\u0158",
            "\1\uffff",
            "\1\uffff",
            "\1\u015a\3\uffff\1\u015b\1\uffff\1\u015b",
            "\1\u015c",
            "\1\115\1\uffff\1\114\35\uffff\1\113",
            "\1\u015d\4\uffff\1\u015e\1\uffff\1\u015e",
            "\1\u0160\37\uffff\1\u015f",
            "\1\uffff",
            "\1\uffff",
            "\1\u0161\1\u0162\1\u0161\1\u0162",
            "\1\u0164\37\uffff\1\u0163",
            "\1\u0165",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0167\1\u016a\1\u0166\2\uffff\1\u016c\1\u0169\10\uffff\1"+
            "\u016e\1\uffff\1\u016d\35\uffff\1\u016b\1\uffff\1\u0168",
            "\1\u016f\1\uffff\1\u0170\1\u0171",
            "\1\34\12\uffff\1\35\3\uffff\1\32\20\uffff\1\31\12\uffff\1\33",
            "\1\70\16\uffff\1\67\20\uffff\1\66",
            "\1\74\5\uffff\1\75\10\uffff\1\72\20\uffff\1\71\5\uffff\1\73",
            "\1\100\15\uffff\1\77\21\uffff\1\76",
            "\1\u0173\26\uffff\1\102\10\uffff\1\u0172",
            "\1\112\23\uffff\1\111\13\uffff\1\110",
            "\1\115\1\uffff\1\114\35\uffff\1\113",
            "\1\74\5\uffff\1\75\10\uffff\1\72\20\uffff\1\71\5\uffff\1\73",
            "\1\112\23\uffff\1\111\13\uffff\1\110",
            "\1\u0175\20\uffff\1\64\3\uffff\1\63\3\uffff\1\60\6\uffff\1"+
            "\u0174\20\uffff\1\61\3\uffff\1\57",
            "\1\u0177\32\uffff\1\105\4\uffff\1\u0176",
            "\1\uffff",
            "\1\u0081\24\uffff\1\u0080\12\uffff\1\177",
            "\1\u0081\24\uffff\1\u0080\12\uffff\1\177",
            "\1\uffff",
            "\1\uffff",
            "\1\u0179\27\uffff\1\u0085\7\uffff\1\u0178",
            "\1\u0179\27\uffff\1\u0085\7\uffff\1\u0178",
            "\1\uffff",
            "\1\uffff",
            "\1\u017b\1\u017a\1\u017b\1\u017a",
            "\1\u017d\3\uffff\1\u017c",
            "\1\u017e",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u017f\1\uffff\1\u017f",
            "\1\u0181\37\uffff\1\u0180",
            "\1\uffff",
            "\1\uffff",
            "\1\u0182\1\u0183\1\u0182\1\u0183",
            "\1\u0185\37\uffff\1\u0184",
            "\1\u0186",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u0187\1\uffff\1\u0187",
            "\1\u0189\37\uffff\1\u0188",
            "\1\uffff",
            "\1\uffff",
            "\1\u018a\3\uffff\1\u018b\1\uffff\1\u018b",
            "\1\u018c",
            "\1\uffff",
            "\1\u018d\1\uffff\1\u018d",
            "\1\u018e",
            "\1\u0081\24\uffff\1\u0080\12\uffff\1\177",
            "\1\u018f\3\uffff\1\u0190\1\uffff\1\u0190",
            "\1\u0191",
            "\1\uffff",
            "\1\u0192\1\uffff\1\u0192",
            "\1\u0193",
            "\1\u0195\27\uffff\1\u0085\7\uffff\1\u0194",
            "\1\uffff",
            "\1\uffff",
            "\1\u0196\1\uffff\1\u0196",
            "\1\u0197",
            "\1\115\1\uffff\1\114\35\uffff\1\113",
            "\1\u0198\1\uffff\1\u0198",
            "\1\u019a\37\uffff\1\u0199",
            "\1\uffff",
            "\1\uffff",
            "\1\u019c\37\uffff\1\u019b",
            "\1\u019d",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\34\12\uffff\1\35\3\uffff\1\32\20\uffff\1\31\12\uffff\1\33",
            "\1\70\16\uffff\1\67\20\uffff\1\66",
            "\1\74\5\uffff\1\75\10\uffff\1\72\20\uffff\1\71\5\uffff\1\73",
            "\1\100\15\uffff\1\77\21\uffff\1\76",
            "\1\103\26\uffff\1\102\10\uffff\1\101",
            "\1\112\23\uffff\1\111\13\uffff\1\110",
            "\1\115\1\uffff\1\114\35\uffff\1\113",
            "\1\74\5\uffff\1\75\10\uffff\1\72\20\uffff\1\71\5\uffff\1\73",
            "\1\112\23\uffff\1\111\13\uffff\1\110",
            "\1\65\20\uffff\1\64\3\uffff\1\63\3\uffff\1\60\6\uffff\1\62"+
            "\20\uffff\1\61\3\uffff\1\57",
            "\1\106\32\uffff\1\105\4\uffff\1\104",
            "\1\uffff",
            "\1\u0081\24\uffff\1\u0080\12\uffff\1\177",
            "\1\u0081\24\uffff\1\u0080\12\uffff\1\177",
            "\1\uffff",
            "\1\uffff",
            "\1\u0086\27\uffff\1\u0085\7\uffff\1\u0084",
            "\1\u0086\27\uffff\1\u0085\7\uffff\1\u0084",
            "\1\uffff",
            "\1\uffff",
            "\1\u019f\3\uffff\1\u019e",
            "\1\u01a0",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u01a2\37\uffff\1\u01a1",
            "\1\uffff",
            "\1\uffff",
            "\1\u01a4\37\uffff\1\u01a3",
            "\1\u01a5",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u01a7\37\uffff\1\u01a6",
            "\1\uffff",
            "\1\uffff",
            "\1\u01a8\1\uffff\1\u01a8",
            "\1\u01a9",
            "\1\uffff",
            "\1\u01aa",
            "\1\u0081\24\uffff\1\u0080\12\uffff\1\177",
            "\1\u01ab\1\uffff\1\u01ab",
            "\1\u01ac",
            "\1\uffff",
            "\1\u01ad",
            "\1\u01af\27\uffff\1\u0085\7\uffff\1\u01ae",
            "\1\uffff",
            "\1\uffff",
            "\1\u01b0",
            "\1\115\1\uffff\1\114\35\uffff\1\113",
            "\1\u01b2\37\uffff\1\u01b1",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\u01b3",
            "\1\uffff",
            "\1\u0081\24\uffff\1\u0080\12\uffff\1\177",
            "\1\u01b4",
            "\1\uffff",
            "\1\u0086\27\uffff\1\u0085\7\uffff\1\u0084",
            "\1\uffff",
            "\1\uffff",
            "\1\115\1\uffff\1\114\35\uffff\1\113",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff"
    };

    static final short[] DFA180_eot = DFA.unpackEncodedString(DFA180_eotS);
    static final short[] DFA180_eof = DFA.unpackEncodedString(DFA180_eofS);
    static final char[] DFA180_min = DFA.unpackEncodedStringToUnsignedChars(DFA180_minS);
    static final char[] DFA180_max = DFA.unpackEncodedStringToUnsignedChars(DFA180_maxS);
    static final short[] DFA180_accept = DFA.unpackEncodedString(DFA180_acceptS);
    static final short[] DFA180_special = DFA.unpackEncodedString(DFA180_specialS);
    static final short[][] DFA180_transition;

    static {
        int numStates = DFA180_transitionS.length;
        DFA180_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA180_transition[i] = DFA.unpackEncodedString(DFA180_transitionS[i]);
        }
    }

    class DFA180 extends DFA {

        public DFA180(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 180;
            this.eot = DFA180_eot;
            this.eof = DFA180_eof;
            this.min = DFA180_min;
            this.max = DFA180_max;
            this.accept = DFA180_accept;
            this.special = DFA180_special;
            this.transition = DFA180_transition;
        }
        public String getDescription() {
            return "877:9: ( ( E ( M | X ) )=> E ( M | X ) | ( P ( X | T | C ) )=> P ( X | T | C ) | ( C M )=> C M | ( M ( M | S ) )=> M ( M | S ) | ( I N )=> I N | ( D E G )=> D E G | ( R A D )=> R A D | ( S )=> S | ( ( K )? H Z )=> ( K )? H Z | IDENT | '%' | )";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA180_419 = input.LA(1);

                         
                        int index180_419 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_419);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA180_242 = input.LA(1);

                         
                        int index180_242 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_242);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA180_420 = input.LA(1);

                         
                        int index180_420 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_420);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA180_241 = input.LA(1);

                         
                        int index180_241 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_241);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA180_340 = input.LA(1);

                         
                        int index180_340 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_340);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA180_276 = input.LA(1);

                         
                        int index180_276 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_276);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA180_117 = input.LA(1);

                         
                        int index180_117 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_117);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA180_120 = input.LA(1);

                         
                        int index180_120 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_120);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA180_234 = input.LA(1);

                         
                        int index180_234 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 71;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_234);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA180_66 = input.LA(1);

                        s = -1;
                        if ( ((LA180_66>='\u0000' && LA180_66<='\t')||LA180_66=='\u000B'||(LA180_66>='\u000E' && LA180_66<='/')||(LA180_66>='1' && LA180_66<='3')||LA180_66=='5'||(LA180_66>='7' && LA180_66<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_66=='0') ) {s = 130;}

                        else if ( (LA180_66=='4'||LA180_66=='6') ) {s = 131;}

                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA180_433 = input.LA(1);

                         
                        int index180_433 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 141;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_433);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA180_351 = input.LA(1);

                         
                        int index180_351 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 141;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_351);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA180_352 = input.LA(1);

                         
                        int index180_352 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 141;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_352);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA180_434 = input.LA(1);

                         
                        int index180_434 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 141;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_434);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA180_355 = input.LA(1);

                         
                        int index180_355 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_355);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA180_356 = input.LA(1);

                         
                        int index180_356 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_356);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA180_248 = input.LA(1);

                         
                        int index180_248 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_248);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA180_42 = input.LA(1);

                         
                        int index180_42 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 71;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_42);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA180_195 = input.LA(1);

                         
                        int index180_195 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 193;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_195);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA180_41 = input.LA(1);

                         
                        int index180_41 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 71;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_41);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA180_194 = input.LA(1);

                         
                        int index180_194 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 193;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_194);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA180_289 = input.LA(1);

                         
                        int index180_289 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_289);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA180_170 = input.LA(1);

                         
                        int index180_170 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_170);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA180_169 = input.LA(1);

                         
                        int index180_169 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_169);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA180_107 = input.LA(1);

                         
                        int index180_107 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_107);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA180_104 = input.LA(1);

                         
                        int index180_104 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_104);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA180_188 = input.LA(1);

                         
                        int index180_188 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_188);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA180_252 = input.LA(1);

                         
                        int index180_252 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 109;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_252);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA180_251 = input.LA(1);

                         
                        int index180_251 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 109;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_251);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA180_435 = input.LA(1);

                         
                        int index180_435 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 193;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_435);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA180_410 = input.LA(1);

                         
                        int index180_410 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 141;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_410);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA180_409 = input.LA(1);

                         
                        int index180_409 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 141;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_409);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA180_132 = input.LA(1);

                         
                        int index180_132 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_132);
                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA180_134 = input.LA(1);

                         
                        int index180_134 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_134);
                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA180_315 = input.LA(1);

                         
                        int index180_315 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_315);
                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA180_401 = input.LA(1);

                         
                        int index180_401 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_401);
                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA180_133 = input.LA(1);

                        s = -1;
                        if ( ((LA180_133>='\u0000' && LA180_133<='\t')||LA180_133=='\u000B'||(LA180_133>='\u000E' && LA180_133<='/')||(LA180_133>='1' && LA180_133<='3')||LA180_133=='5'||(LA180_133>='7' && LA180_133<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_133=='0') ) {s = 202;}

                        else if ( (LA180_133=='4'||LA180_133=='6') ) {s = 203;}

                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA180_57 = input.LA(1);

                         
                        int index180_57 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_57);
                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA180_60 = input.LA(1);

                         
                        int index180_60 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_60);
                        if ( s>=0 ) return s;
                        break;
                    case 39 : 
                        int LA180_191 = input.LA(1);

                         
                        int index180_191 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 122;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_191);
                        if ( s>=0 ) return s;
                        break;
                    case 40 : 
                        int LA180_345 = input.LA(1);

                         
                        int index180_345 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_345);
                        if ( s>=0 ) return s;
                        break;
                    case 41 : 
                        int LA180_192 = input.LA(1);

                         
                        int index180_192 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 122;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_192);
                        if ( s>=0 ) return s;
                        break;
                    case 42 : 
                        int LA180_344 = input.LA(1);

                         
                        int index180_344 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_344);
                        if ( s>=0 ) return s;
                        break;
                    case 43 : 
                        int LA180_389 = input.LA(1);

                         
                        int index180_389 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_389);
                        if ( s>=0 ) return s;
                        break;
                    case 44 : 
                        int LA180_310 = input.LA(1);

                         
                        int index180_310 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_310);
                        if ( s>=0 ) return s;
                        break;
                    case 45 : 
                        int LA180_311 = input.LA(1);

                         
                        int index180_311 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_311);
                        if ( s>=0 ) return s;
                        break;
                    case 46 : 
                        int LA180_388 = input.LA(1);

                         
                        int index180_388 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_388);
                        if ( s>=0 ) return s;
                        break;
                    case 47 : 
                        int LA180_58 = input.LA(1);

                        s = -1;
                        if ( (LA180_58=='m') ) {s = 115;}

                        else if ( (LA180_58=='M') ) {s = 116;}

                        else if ( (LA180_58=='s') ) {s = 117;}

                        else if ( (LA180_58=='0') ) {s = 118;}

                        else if ( (LA180_58=='4'||LA180_58=='6') ) {s = 119;}

                        else if ( (LA180_58=='S') ) {s = 120;}

                        else if ( ((LA180_58>='\u0000' && LA180_58<='\t')||LA180_58=='\u000B'||(LA180_58>='\u000E' && LA180_58<='/')||(LA180_58>='1' && LA180_58<='3')||(LA180_58>='8' && LA180_58<='L')||(LA180_58>='N' && LA180_58<='R')||(LA180_58>='T' && LA180_58<='l')||(LA180_58>='n' && LA180_58<='r')||(LA180_58>='t' && LA180_58<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_58=='5'||LA180_58=='7') ) {s = 121;}

                        if ( s>=0 ) return s;
                        break;
                    case 48 : 
                        int LA180_178 = input.LA(1);

                         
                        int index180_178 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_178);
                        if ( s>=0 ) return s;
                        break;
                    case 49 : 
                        int LA180_288 = input.LA(1);

                         
                        int index180_288 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_288);
                        if ( s>=0 ) return s;
                        break;
                    case 50 : 
                        int LA180_287 = input.LA(1);

                         
                        int index180_287 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_287);
                        if ( s>=0 ) return s;
                        break;
                    case 51 : 
                        int LA180_176 = input.LA(1);

                         
                        int index180_176 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_176);
                        if ( s>=0 ) return s;
                        break;
                    case 52 : 
                        int LA180_26 = input.LA(1);

                        s = -1;
                        if ( (LA180_26=='m') ) {s = 79;}

                        else if ( (LA180_26=='M') ) {s = 80;}

                        else if ( (LA180_26=='x') ) {s = 81;}

                        else if ( (LA180_26=='0') ) {s = 82;}

                        else if ( (LA180_26=='4'||LA180_26=='6') ) {s = 83;}

                        else if ( (LA180_26=='X') ) {s = 84;}

                        else if ( ((LA180_26>='\u0000' && LA180_26<='\t')||LA180_26=='\u000B'||(LA180_26>='\u000E' && LA180_26<='/')||(LA180_26>='1' && LA180_26<='3')||(LA180_26>='8' && LA180_26<='L')||(LA180_26>='N' && LA180_26<='W')||(LA180_26>='Y' && LA180_26<='l')||(LA180_26>='n' && LA180_26<='w')||(LA180_26>='y' && LA180_26<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_26=='5'||LA180_26=='7') ) {s = 85;}

                        if ( s>=0 ) return s;
                        break;
                    case 53 : 
                        int LA180_128 = input.LA(1);

                        s = -1;
                        if ( (LA180_128=='g') ) {s = 194;}

                        else if ( (LA180_128=='G') ) {s = 195;}

                        else if ( ((LA180_128>='\u0000' && LA180_128<='\t')||LA180_128=='\u000B'||(LA180_128>='\u000E' && LA180_128<='/')||(LA180_128>='1' && LA180_128<='3')||LA180_128=='5'||(LA180_128>='7' && LA180_128<='F')||(LA180_128>='H' && LA180_128<='f')||(LA180_128>='h' && LA180_128<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_128=='0') ) {s = 196;}

                        else if ( (LA180_128=='4'||LA180_128=='6') ) {s = 197;}

                        if ( s>=0 ) return s;
                        break;
                    case 54 : 
                        int LA180_421 = input.LA(1);

                         
                        int index180_421 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_421);
                        if ( s>=0 ) return s;
                        break;
                    case 55 : 
                        int LA180_181 = input.LA(1);

                         
                        int index180_181 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 109;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_181);
                        if ( s>=0 ) return s;
                        break;
                    case 56 : 
                        int LA180_182 = input.LA(1);

                         
                        int index180_182 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 109;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_182);
                        if ( s>=0 ) return s;
                        break;
                    case 57 : 
                        int LA180_257 = input.LA(1);

                         
                        int index180_257 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_257);
                        if ( s>=0 ) return s;
                        break;
                    case 58 : 
                        int LA180_63 = input.LA(1);

                        s = -1;
                        if ( (LA180_63=='n') ) {s = 123;}

                        else if ( (LA180_63=='N') ) {s = 124;}

                        else if ( ((LA180_63>='\u0000' && LA180_63<='\t')||LA180_63=='\u000B'||(LA180_63>='\u000E' && LA180_63<='/')||(LA180_63>='1' && LA180_63<='3')||LA180_63=='5'||(LA180_63>='7' && LA180_63<='M')||(LA180_63>='O' && LA180_63<='m')||(LA180_63>='o' && LA180_63<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_63=='0') ) {s = 125;}

                        else if ( (LA180_63=='4'||LA180_63=='6') ) {s = 126;}

                        if ( s>=0 ) return s;
                        break;
                    case 59 : 
                        int LA180_357 = input.LA(1);

                         
                        int index180_357 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_357);
                        if ( s>=0 ) return s;
                        break;
                    case 60 : 
                        int LA180_282 = input.LA(1);

                         
                        int index180_282 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 141;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_282);
                        if ( s>=0 ) return s;
                        break;
                    case 61 : 
                        int LA180_283 = input.LA(1);

                         
                        int index180_283 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 141;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_283);
                        if ( s>=0 ) return s;
                        break;
                    case 62 : 
                        int LA180_79 = input.LA(1);

                         
                        int index180_79 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_79);
                        if ( s>=0 ) return s;
                        break;
                    case 63 : 
                        int LA180_80 = input.LA(1);

                         
                        int index180_80 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_80);
                        if ( s>=0 ) return s;
                        break;
                    case 64 : 
                        int LA180_411 = input.LA(1);

                         
                        int index180_411 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_411);
                        if ( s>=0 ) return s;
                        break;
                    case 65 : 
                        int LA180_425 = input.LA(1);

                         
                        int index180_425 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 193;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_425);
                        if ( s>=0 ) return s;
                        break;
                    case 66 : 
                        int LA180_166 = input.LA(1);

                         
                        int index180_166 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 71;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_166);
                        if ( s>=0 ) return s;
                        break;
                    case 67 : 
                        int LA180_412 = input.LA(1);

                         
                        int index180_412 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_412);
                        if ( s>=0 ) return s;
                        break;
                    case 68 : 
                        int LA180_326 = input.LA(1);

                         
                        int index180_326 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_326);
                        if ( s>=0 ) return s;
                        break;
                    case 69 : 
                        int LA180_325 = input.LA(1);

                         
                        int index180_325 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_325);
                        if ( s>=0 ) return s;
                        break;
                    case 70 : 
                        int LA180_390 = input.LA(1);

                         
                        int index180_390 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_390);
                        if ( s>=0 ) return s;
                        break;
                    case 71 : 
                        int LA180_48 = input.LA(1);

                        s = -1;
                        if ( (LA180_48=='x') ) {s = 102;}

                        else if ( (LA180_48=='X') ) {s = 103;}

                        else if ( (LA180_48=='t') ) {s = 104;}

                        else if ( (LA180_48=='0') ) {s = 105;}

                        else if ( (LA180_48=='5'||LA180_48=='7') ) {s = 106;}

                        else if ( (LA180_48=='T') ) {s = 107;}

                        else if ( ((LA180_48>='\u0000' && LA180_48<='\t')||LA180_48=='\u000B'||(LA180_48>='\u000E' && LA180_48<='/')||(LA180_48>='1' && LA180_48<='3')||(LA180_48>='8' && LA180_48<='S')||(LA180_48>='U' && LA180_48<='W')||(LA180_48>='Y' && LA180_48<='s')||(LA180_48>='u' && LA180_48<='w')||(LA180_48>='y' && LA180_48<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_48=='4'||LA180_48=='6') ) {s = 108;}

                        if ( s>=0 ) return s;
                        break;
                    case 72 : 
                        int LA180_100 = input.LA(1);

                         
                        int index180_100 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 71;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_100);
                        if ( s>=0 ) return s;
                        break;
                    case 73 : 
                        int LA180_405 = input.LA(1);

                         
                        int index180_405 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_405);
                        if ( s>=0 ) return s;
                        break;
                    case 74 : 
                        int LA180_404 = input.LA(1);

                         
                        int index180_404 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_404);
                        if ( s>=0 ) return s;
                        break;
                    case 75 : 
                        int LA180_380 = input.LA(1);

                         
                        int index180_380 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_380);
                        if ( s>=0 ) return s;
                        break;
                    case 76 : 
                        int LA180_219 = input.LA(1);

                         
                        int index180_219 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_219);
                        if ( s>=0 ) return s;
                        break;
                    case 77 : 
                        int LA180_381 = input.LA(1);

                         
                        int index180_381 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_381);
                        if ( s>=0 ) return s;
                        break;
                    case 78 : 
                        int LA180_53 = input.LA(1);

                         
                        int index180_53 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_53);
                        if ( s>=0 ) return s;
                        break;
                    case 79 : 
                        int LA180_142 = input.LA(1);

                         
                        int index180_142 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 141;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_142);
                        if ( s>=0 ) return s;
                        break;
                    case 80 : 
                        int LA180_143 = input.LA(1);

                         
                        int index180_143 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 141;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_143);
                        if ( s>=0 ) return s;
                        break;
                    case 81 : 
                        int LA180_436 = input.LA(1);

                         
                        int index180_436 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_436);
                        if ( s>=0 ) return s;
                        break;
                    case 82 : 
                        int LA180_50 = input.LA(1);

                         
                        int index180_50 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_50);
                        if ( s>=0 ) return s;
                        break;
                    case 83 : 
                        int LA180_415 = input.LA(1);

                         
                        int index180_415 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_415);
                        if ( s>=0 ) return s;
                        break;
                    case 84 : 
                        int LA180_430 = input.LA(1);

                         
                        int index180_430 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_430);
                        if ( s>=0 ) return s;
                        break;
                    case 85 : 
                        int LA180_246 = input.LA(1);

                         
                        int index180_246 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_246);
                        if ( s>=0 ) return s;
                        break;
                    case 86 : 
                        int LA180_428 = input.LA(1);

                         
                        int index180_428 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_428);
                        if ( s>=0 ) return s;
                        break;
                    case 87 : 
                        int LA180_115 = input.LA(1);

                         
                        int index180_115 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_115);
                        if ( s>=0 ) return s;
                        break;
                    case 88 : 
                        int LA180_116 = input.LA(1);

                         
                        int index180_116 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_116);
                        if ( s>=0 ) return s;
                        break;
                    case 89 : 
                        int LA180_431 = input.LA(1);

                         
                        int index180_431 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_431);
                        if ( s>=0 ) return s;
                        break;
                    case 90 : 
                        int LA180_306 = input.LA(1);

                         
                        int index180_306 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_306);
                        if ( s>=0 ) return s;
                        break;
                    case 91 : 
                        int LA180_52 = input.LA(1);

                         
                        int index180_52 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_52);
                        if ( s>=0 ) return s;
                        break;
                    case 92 : 
                        int LA180_217 = input.LA(1);

                         
                        int index180_217 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_217);
                        if ( s>=0 ) return s;
                        break;
                    case 93 : 
                        int LA180_307 = input.LA(1);

                         
                        int index180_307 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_307);
                        if ( s>=0 ) return s;
                        break;
                    case 94 : 
                        int LA180_49 = input.LA(1);

                         
                        int index180_49 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_49);
                        if ( s>=0 ) return s;
                        break;
                    case 95 : 
                        int LA180_218 = input.LA(1);

                         
                        int index180_218 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_218);
                        if ( s>=0 ) return s;
                        break;
                    case 96 : 
                        int LA180_75 = input.LA(1);

                         
                        int index180_75 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 141;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_75);
                        if ( s>=0 ) return s;
                        break;
                    case 97 : 
                        int LA180_77 = input.LA(1);

                         
                        int index180_77 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 141;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_77);
                        if ( s>=0 ) return s;
                        break;
                    case 98 : 
                        int LA180_396 = input.LA(1);

                         
                        int index180_396 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 193;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_396);
                        if ( s>=0 ) return s;
                        break;
                    case 99 : 
                        int LA180_59 = input.LA(1);

                         
                        int index180_59 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_59);
                        if ( s>=0 ) return s;
                        break;
                    case 100 : 
                        int LA180_61 = input.LA(1);

                         
                        int index180_61 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_61);
                        if ( s>=0 ) return s;
                        break;
                    case 101 : 
                        int LA180_212 = input.LA(1);

                         
                        int index180_212 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 141;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_212);
                        if ( s>=0 ) return s;
                        break;
                    case 102 : 
                        int LA180_213 = input.LA(1);

                         
                        int index180_213 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred9_Css3()) ) {s = 141;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_213);
                        if ( s>=0 ) return s;
                        break;
                    case 103 : 
                        int LA180_423 = input.LA(1);

                         
                        int index180_423 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 122;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_423);
                        if ( s>=0 ) return s;
                        break;
                    case 104 : 
                        int LA180_422 = input.LA(1);

                         
                        int index180_422 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 122;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_422);
                        if ( s>=0 ) return s;
                        break;
                    case 105 : 
                        int LA180_392 = input.LA(1);

                         
                        int index180_392 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 122;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_392);
                        if ( s>=0 ) return s;
                        break;
                    case 106 : 
                        int LA180_393 = input.LA(1);

                         
                        int index180_393 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 122;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_393);
                        if ( s>=0 ) return s;
                        break;
                    case 107 : 
                        int LA180_373 = input.LA(1);

                         
                        int index180_373 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_373);
                        if ( s>=0 ) return s;
                        break;
                    case 108 : 
                        int LA180_372 = input.LA(1);

                         
                        int index180_372 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_372);
                        if ( s>=0 ) return s;
                        break;
                    case 109 : 
                        int LA180_186 = input.LA(1);

                         
                        int index180_186 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_186);
                        if ( s>=0 ) return s;
                        break;
                    case 110 : 
                        int LA180_187 = input.LA(1);

                         
                        int index180_187 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_187);
                        if ( s>=0 ) return s;
                        break;
                    case 111 : 
                        int LA180_69 = input.LA(1);

                        s = -1;
                        if ( ((LA180_69>='\u0000' && LA180_69<='\t')||LA180_69=='\u000B'||(LA180_69>='\u000E' && LA180_69<='/')||(LA180_69>='1' && LA180_69<='3')||LA180_69=='5'||(LA180_69>='7' && LA180_69<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_69=='0') ) {s = 135;}

                        else if ( (LA180_69=='4'||LA180_69=='6') ) {s = 136;}

                        if ( s>=0 ) return s;
                        break;
                    case 112 : 
                        int LA180_376 = input.LA(1);

                         
                        int index180_376 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_376);
                        if ( s>=0 ) return s;
                        break;
                    case 113 : 
                        int LA180_256 = input.LA(1);

                         
                        int index180_256 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_256);
                        if ( s>=0 ) return s;
                        break;
                    case 114 : 
                        int LA180_327 = input.LA(1);

                         
                        int index180_327 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_327);
                        if ( s>=0 ) return s;
                        break;
                    case 115 : 
                        int LA180_56 = input.LA(1);

                         
                        int index180_56 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 109;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_56);
                        if ( s>=0 ) return s;
                        break;
                    case 116 : 
                        int LA180_54 = input.LA(1);

                         
                        int index180_54 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 109;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_54);
                        if ( s>=0 ) return s;
                        break;
                    case 117 : 
                        int LA180_377 = input.LA(1);

                         
                        int index180_377 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_377);
                        if ( s>=0 ) return s;
                        break;
                    case 118 : 
                        int LA180_384 = input.LA(1);

                         
                        int index180_384 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 109;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_384);
                        if ( s>=0 ) return s;
                        break;
                    case 119 : 
                        int LA180_385 = input.LA(1);

                         
                        int index180_385 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 109;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_385);
                        if ( s>=0 ) return s;
                        break;
                    case 120 : 
                        int LA180_62 = input.LA(1);

                         
                        int index180_62 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 122;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_62);
                        if ( s>=0 ) return s;
                        break;
                    case 121 : 
                        int LA180_64 = input.LA(1);

                         
                        int index180_64 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 122;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_64);
                        if ( s>=0 ) return s;
                        break;
                    case 122 : 
                        int LA180_416 = input.LA(1);

                         
                        int index180_416 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_416);
                        if ( s>=0 ) return s;
                        break;
                    case 123 : 
                        int LA180_76 = input.LA(1);

                        s = -1;
                        if ( (LA180_76=='z') ) {s = 142;}

                        else if ( (LA180_76=='Z') ) {s = 143;}

                        else if ( ((LA180_76>='\u0000' && LA180_76<='\t')||LA180_76=='\u000B'||(LA180_76>='\u000E' && LA180_76<='/')||(LA180_76>='1' && LA180_76<='4')||LA180_76=='6'||(LA180_76>='8' && LA180_76<='Y')||(LA180_76>='[' && LA180_76<='y')||(LA180_76>='{' && LA180_76<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_76=='0') ) {s = 144;}

                        else if ( (LA180_76=='5'||LA180_76=='7') ) {s = 145;}

                        if ( s>=0 ) return s;
                        break;
                    case 124 : 
                        int LA180_382 = input.LA(1);

                         
                        int index180_382 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_382);
                        if ( s>=0 ) return s;
                        break;
                    case 125 : 
                        int LA180_151 = input.LA(1);

                         
                        int index180_151 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_151);
                        if ( s>=0 ) return s;
                        break;
                    case 126 : 
                        int LA180_316 = input.LA(1);

                         
                        int index180_316 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_316);
                        if ( s>=0 ) return s;
                        break;
                    case 127 : 
                        int LA180_127 = input.LA(1);

                         
                        int index180_127 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 193;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_127);
                        if ( s>=0 ) return s;
                        break;
                    case 128 : 
                        int LA180_129 = input.LA(1);

                         
                        int index180_129 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 193;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_129);
                        if ( s>=0 ) return s;
                        break;
                    case 129 : 
                        int LA180_303 = input.LA(1);

                         
                        int index180_303 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 71;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_303);
                        if ( s>=0 ) return s;
                        break;
                    case 130 : 
                        int LA180_417 = input.LA(1);

                         
                        int index180_417 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 109;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_417);
                        if ( s>=0 ) return s;
                        break;
                    case 131 : 
                        int LA180_418 = input.LA(1);

                         
                        int index180_418 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 109;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_418);
                        if ( s>=0 ) return s;
                        break;
                    case 132 : 
                        int LA180_334 = input.LA(1);

                         
                        int index180_334 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 193;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_334);
                        if ( s>=0 ) return s;
                        break;
                    case 133 : 
                        int LA180_369 = input.LA(1);

                         
                        int index180_369 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 71;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_369);
                        if ( s>=0 ) return s;
                        break;
                    case 134 : 
                        int LA180_177 = input.LA(1);

                         
                        int index180_177 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_177);
                        if ( s>=0 ) return s;
                        break;
                    case 135 : 
                        int LA180_124 = input.LA(1);

                         
                        int index180_124 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 122;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_124);
                        if ( s>=0 ) return s;
                        break;
                    case 136 : 
                        int LA180_123 = input.LA(1);

                         
                        int index180_123 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 122;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_123);
                        if ( s>=0 ) return s;
                        break;
                    case 137 : 
                        int LA180_29 = input.LA(1);

                         
                        int index180_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_29);
                        if ( s>=0 ) return s;
                        break;
                    case 138 : 
                        int LA180_317 = input.LA(1);

                         
                        int index180_317 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_317);
                        if ( s>=0 ) return s;
                        break;
                    case 139 : 
                        int LA180_27 = input.LA(1);

                         
                        int index180_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_27);
                        if ( s>=0 ) return s;
                        break;
                    case 140 : 
                        int LA180_271 = input.LA(1);

                         
                        int index180_271 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_271);
                        if ( s>=0 ) return s;
                        break;
                    case 141 : 
                        int LA180_414 = input.LA(1);

                         
                        int index180_414 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_414);
                        if ( s>=0 ) return s;
                        break;
                    case 142 : 
                        int LA180_2 = input.LA(1);

                        s = -1;
                        if ( (LA180_2=='p') ) {s = 30;}

                        else if ( (LA180_2=='0') ) {s = 31;}

                        else if ( (LA180_2=='4'||LA180_2=='6') ) {s = 32;}

                        else if ( (LA180_2=='P') ) {s = 33;}

                        else if ( (LA180_2=='m') ) {s = 34;}

                        else if ( (LA180_2=='5'||LA180_2=='7') ) {s = 35;}

                        else if ( (LA180_2=='M') ) {s = 36;}

                        else if ( (LA180_2=='i') ) {s = 37;}

                        else if ( (LA180_2=='I') ) {s = 38;}

                        else if ( (LA180_2=='r') ) {s = 39;}

                        else if ( (LA180_2=='R') ) {s = 40;}

                        else if ( (LA180_2=='s') ) {s = 41;}

                        else if ( (LA180_2=='S') ) {s = 42;}

                        else if ( (LA180_2=='k') ) {s = 43;}

                        else if ( (LA180_2=='K') ) {s = 44;}

                        else if ( (LA180_2=='h') ) {s = 45;}

                        else if ( (LA180_2=='H') ) {s = 46;}

                        else if ( ((LA180_2>='\u0000' && LA180_2<='\t')||LA180_2=='\u000B'||(LA180_2>='\u000E' && LA180_2<='/')||(LA180_2>='1' && LA180_2<='3')||(LA180_2>='8' && LA180_2<='G')||LA180_2=='J'||LA180_2=='L'||(LA180_2>='N' && LA180_2<='O')||LA180_2=='Q'||(LA180_2>='T' && LA180_2<='g')||LA180_2=='j'||LA180_2=='l'||(LA180_2>='n' && LA180_2<='o')||LA180_2=='q'||(LA180_2>='t' && LA180_2<='\uFFFF')) ) {s = 12;}

                        if ( s>=0 ) return s;
                        break;
                    case 143 : 
                        int LA180_330 = input.LA(1);

                         
                        int index180_330 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 122;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_330);
                        if ( s>=0 ) return s;
                        break;
                    case 144 : 
                        int LA180_331 = input.LA(1);

                         
                        int index180_331 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 122;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_331);
                        if ( s>=0 ) return s;
                        break;
                    case 145 : 
                        int LA180_103 = input.LA(1);

                         
                        int index180_103 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_103);
                        if ( s>=0 ) return s;
                        break;
                    case 146 : 
                        int LA180_25 = input.LA(1);

                         
                        int index180_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_25);
                        if ( s>=0 ) return s;
                        break;
                    case 147 : 
                        int LA180_28 = input.LA(1);

                         
                        int index180_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_28);
                        if ( s>=0 ) return s;
                        break;
                    case 148 : 
                        int LA180_413 = input.LA(1);

                         
                        int index180_413 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_413);
                        if ( s>=0 ) return s;
                        break;
                    case 149 : 
                        int LA180_20 = input.LA(1);

                         
                        int index180_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 71;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_20);
                        if ( s>=0 ) return s;
                        break;
                    case 150 : 
                        int LA180_9 = input.LA(1);

                         
                        int index180_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred8_Css3()) ) {s = 71;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_9);
                        if ( s>=0 ) return s;
                        break;
                    case 151 : 
                        int LA180_110 = input.LA(1);

                         
                        int index180_110 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 109;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_110);
                        if ( s>=0 ) return s;
                        break;
                    case 152 : 
                        int LA180_111 = input.LA(1);

                         
                        int index180_111 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 109;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_111);
                        if ( s>=0 ) return s;
                        break;
                    case 153 : 
                        int LA180_102 = input.LA(1);

                         
                        int index180_102 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_102);
                        if ( s>=0 ) return s;
                        break;
                    case 154 : 
                        int LA180_81 = input.LA(1);

                         
                        int index180_81 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_81);
                        if ( s>=0 ) return s;
                        break;
                    case 155 : 
                        int LA180_84 = input.LA(1);

                         
                        int index180_84 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_84);
                        if ( s>=0 ) return s;
                        break;
                    case 156 : 
                        int LA180_73 = input.LA(1);

                        s = -1;
                        if ( (LA180_73=='h') ) {s = 137;}

                        else if ( (LA180_73=='H') ) {s = 138;}

                        else if ( ((LA180_73>='\u0000' && LA180_73<='\t')||LA180_73=='\u000B'||(LA180_73>='\u000E' && LA180_73<='/')||(LA180_73>='1' && LA180_73<='3')||LA180_73=='5'||(LA180_73>='7' && LA180_73<='G')||(LA180_73>='I' && LA180_73<='g')||(LA180_73>='i' && LA180_73<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_73=='0') ) {s = 139;}

                        else if ( (LA180_73=='4'||LA180_73=='6') ) {s = 140;}

                        if ( s>=0 ) return s;
                        break;
                    case 157 : 
                        int LA180_262 = input.LA(1);

                         
                        int index180_262 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 122;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_262);
                        if ( s>=0 ) return s;
                        break;
                    case 158 : 
                        int LA180_261 = input.LA(1);

                         
                        int index180_261 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Css3()) ) {s = 122;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_261);
                        if ( s>=0 ) return s;
                        break;
                    case 159 : 
                        int LA180_238 = input.LA(1);

                         
                        int index180_238 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_238);
                        if ( s>=0 ) return s;
                        break;
                    case 160 : 
                        int LA180_247 = input.LA(1);

                         
                        int index180_247 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_247);
                        if ( s>=0 ) return s;
                        break;
                    case 161 : 
                        int LA180_55 = input.LA(1);

                        s = -1;
                        if ( (LA180_55=='m') ) {s = 110;}

                        else if ( (LA180_55=='M') ) {s = 111;}

                        else if ( ((LA180_55>='\u0000' && LA180_55<='\t')||LA180_55=='\u000B'||(LA180_55>='\u000E' && LA180_55<='/')||(LA180_55>='1' && LA180_55<='3')||LA180_55=='5'||(LA180_55>='7' && LA180_55<='L')||(LA180_55>='N' && LA180_55<='l')||(LA180_55>='n' && LA180_55<='\uFFFF')) ) {s = 12;}

                        else if ( (LA180_55=='0') ) {s = 112;}

                        else if ( (LA180_55=='4'||LA180_55=='6') ) {s = 113;}

                        if ( s>=0 ) return s;
                        break;
                    case 162 : 
                        int LA180_321 = input.LA(1);

                         
                        int index180_321 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 109;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_321);
                        if ( s>=0 ) return s;
                        break;
                    case 163 : 
                        int LA180_51 = input.LA(1);

                         
                        int index180_51 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_51);
                        if ( s>=0 ) return s;
                        break;
                    case 164 : 
                        int LA180_320 = input.LA(1);

                         
                        int index180_320 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 109;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_320);
                        if ( s>=0 ) return s;
                        break;
                    case 165 : 
                        int LA180_47 = input.LA(1);

                         
                        int index180_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_47);
                        if ( s>=0 ) return s;
                        break;
                    case 166 : 
                        int LA180_258 = input.LA(1);

                         
                        int index180_258 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred4_Css3()) ) {s = 114;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_258);
                        if ( s>=0 ) return s;
                        break;
                    case 167 : 
                        int LA180_265 = input.LA(1);

                         
                        int index180_265 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Css3()) ) {s = 193;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_265);
                        if ( s>=0 ) return s;
                        break;
                    case 168 : 
                        int LA180_150 = input.LA(1);

                         
                        int index180_150 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_150);
                        if ( s>=0 ) return s;
                        break;
                    case 169 : 
                        int LA180_149 = input.LA(1);

                         
                        int index180_149 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 78;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_149);
                        if ( s>=0 ) return s;
                        break;
                    case 170 : 
                        int LA180_237 = input.LA(1);

                         
                        int index180_237 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred2_Css3()) ) {s = 101;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_237);
                        if ( s>=0 ) return s;
                        break;
                    case 171 : 
                        int LA180_275 = input.LA(1);

                         
                        int index180_275 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred7_Css3()) ) {s = 201;}

                        else if ( (true) ) {s = 12;}

                         
                        input.seek(index180_275);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 180, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA177_eotS =
        "\12\uffff";
    static final String DFA177_eofS =
        "\12\uffff";
    static final String DFA177_minS =
        "\1\103\1\uffff\1\60\2\uffff\1\60\1\64\2\60\1\64";
    static final String DFA177_maxS =
        "\1\170\1\uffff\1\170\2\uffff\1\67\1\70\3\67";
    static final String DFA177_acceptS =
        "\1\uffff\1\1\1\uffff\1\2\1\3\5\uffff";
    static final String DFA177_specialS =
        "\12\uffff}>";
    static final String[] DFA177_transitionS = {
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

    static final short[] DFA177_eot = DFA.unpackEncodedString(DFA177_eotS);
    static final short[] DFA177_eof = DFA.unpackEncodedString(DFA177_eofS);
    static final char[] DFA177_min = DFA.unpackEncodedStringToUnsignedChars(DFA177_minS);
    static final char[] DFA177_max = DFA.unpackEncodedStringToUnsignedChars(DFA177_maxS);
    static final short[] DFA177_accept = DFA.unpackEncodedString(DFA177_acceptS);
    static final short[] DFA177_special = DFA.unpackEncodedString(DFA177_specialS);
    static final short[][] DFA177_transition;

    static {
        int numStates = DFA177_transitionS.length;
        DFA177_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA177_transition[i] = DFA.unpackEncodedString(DFA177_transitionS[i]);
        }
    }

    class DFA177 extends DFA {

        public DFA177(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 177;
            this.eot = DFA177_eot;
            this.eof = DFA177_eof;
            this.min = DFA177_min;
            this.max = DFA177_max;
            this.accept = DFA177_accept;
            this.special = DFA177_special;
            this.transition = DFA177_transition;
        }
        public String getDescription() {
            return "886:17: ( X | T | C )";
        }
    }
    static final String DFA187_eotS =
        "\2\uffff\1\47\1\uffff\1\51\1\53\1\55\7\uffff\1\57\5\uffff\1\60\2"+
        "\uffff\2\26\30\uffff\2\26\1\uffff\4\26\2\uffff\2\26\1\uffff\7\26"+
        "\2\uffff\12\26\1\uffff\12\26\1\uffff\26\26";
    static final String DFA187_eofS =
        "\162\uffff";
    static final String DFA187_minS =
        "\1\11\1\100\1\52\1\uffff\1\55\2\75\7\uffff\1\72\5\uffff\1\60\2\uffff"+
        "\2\122\1\0\10\uffff\1\60\16\uffff\2\114\1\0\1\122\1\60\1\122\1\65"+
        "\1\60\1\71\2\50\1\0\1\114\1\60\1\114\1\62\1\60\1\65\1\122\1\60\1"+
        "\uffff\1\50\1\60\1\50\1\103\1\60\1\62\1\114\1\60\1\65\1\122\2\60"+
        "\1\103\2\50\1\60\1\62\1\114\2\65\1\122\1\64\1\60\1\103\2\50\1\65"+
        "\1\62\1\114\1\65\1\122\1\64\1\103\2\50\1\62\1\114\1\122\1\103\2"+
        "\50\1\114\2\50";
    static final String DFA187_maxS =
        "\1\uffff\1\160\1\52\1\uffff\1\uffff\2\75\7\uffff\1\72\5\uffff\1"+
        "\71\2\uffff\2\162\1\uffff\10\uffff\1\160\16\uffff\2\154\1\uffff"+
        "\1\162\1\67\1\162\1\65\1\67\1\145\2\50\1\uffff\1\154\1\67\1\154"+
        "\1\62\1\67\1\65\1\162\1\67\1\uffff\1\50\1\66\1\50\1\143\1\67\1\62"+
        "\1\154\1\67\1\65\1\162\1\67\1\66\1\143\2\50\1\67\1\62\1\154\1\67"+
        "\1\65\1\162\1\67\1\66\1\143\2\50\1\67\1\62\1\154\1\65\1\162\1\66"+
        "\1\143\2\50\1\62\1\154\1\162\1\143\2\50\1\154\2\50";
    static final String DFA187_acceptS =
        "\3\uffff\1\3\3\uffff\1\7\1\10\1\11\1\12\1\13\1\14\1\15\1\uffff\1"+
        "\22\1\23\1\24\1\25\1\26\1\uffff\1\32\1\33\3\uffff\1\34\1\42\1\43"+
        "\1\45\1\46\1\1\1\40\1\35\1\uffff\1\36\1\37\1\41\1\2\1\20\1\4\1\21"+
        "\1\5\1\30\1\6\1\31\1\17\1\16\1\27\24\uffff\1\44\54\uffff";
    static final String DFA187_specialS =
        "\31\uffff\1\1\31\uffff\1\0\10\uffff\1\2\65\uffff}>";
    static final String[] DFA187_transitionS = {
            "\1\35\1\36\2\uffff\1\36\22\uffff\1\35\1\33\1\25\1\32\3\uffff"+
            "\1\25\1\21\1\22\1\20\1\17\1\23\1\4\1\24\1\2\12\34\1\16\1\15"+
            "\1\3\1\14\1\7\1\uffff\1\1\24\26\1\30\5\26\1\12\1\31\1\13\1\uffff"+
            "\1\26\1\uffff\24\26\1\27\5\26\1\10\1\6\1\11\1\5\1\uffff\uff80"+
            "\26",
            "\1\37\10\uffff\1\41\3\uffff\1\44\1\45\1\uffff\1\43\13\uffff"+
            "\1\42\6\uffff\1\40\5\uffff\1\41\3\uffff\1\44\1\45\1\uffff\1"+
            "\43",
            "\1\46",
            "",
            "\1\50\23\uffff\32\26\1\uffff\1\26\2\uffff\1\26\1\uffff\32\26"+
            "\5\uffff\uff80\26",
            "\1\52",
            "\1\54",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\56",
            "",
            "",
            "",
            "",
            "",
            "\12\34",
            "",
            "",
            "\1\62\11\uffff\1\63\25\uffff\1\61",
            "\1\62\11\uffff\1\63\25\uffff\1\61",
            "\12\26\1\uffff\1\26\2\uffff\42\26\1\65\4\26\1\67\1\26\1\67"+
            "\35\26\1\66\37\26\1\64\uff8a\26",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\70\3\uffff\1\71\1\43\1\71\1\43\21\uffff\1\41\3\uffff\1\44"+
            "\1\45\1\uffff\1\43\30\uffff\1\41\3\uffff\1\44\1\45\1\uffff\1"+
            "\43",
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
            "\1\73\17\uffff\1\74\17\uffff\1\72",
            "\1\73\17\uffff\1\74\17\uffff\1\72",
            "\12\26\1\uffff\1\26\2\uffff\42\26\1\76\4\26\1\100\1\26\1\100"+
            "\32\26\1\77\37\26\1\75\uff8d\26",
            "\1\62\11\uffff\1\63\25\uffff\1\61",
            "\1\101\4\uffff\1\102\1\uffff\1\102",
            "\1\62\11\uffff\1\63\25\uffff\1\61",
            "\1\103",
            "\1\104\3\uffff\1\71\1\43\1\71\1\43",
            "\1\41\12\uffff\1\44\1\45\36\uffff\1\44\1\45",
            "\1\105",
            "\1\105",
            "\12\26\1\uffff\1\26\2\uffff\42\26\1\107\3\26\1\111\1\26\1\111"+
            "\25\26\1\110\37\26\1\106\uff93\26",
            "\1\73\17\uffff\1\74\17\uffff\1\72",
            "\1\112\4\uffff\1\113\1\uffff\1\113",
            "\1\73\17\uffff\1\74\17\uffff\1\72",
            "\1\114",
            "\1\115\4\uffff\1\116\1\uffff\1\116",
            "\1\117",
            "\1\62\11\uffff\1\63\25\uffff\1\61",
            "\1\120\3\uffff\1\71\1\43\1\71\1\43",
            "",
            "\1\105",
            "\1\121\3\uffff\1\122\1\uffff\1\122",
            "\1\105",
            "\1\124\37\uffff\1\123",
            "\1\125\4\uffff\1\126\1\uffff\1\126",
            "\1\127",
            "\1\73\17\uffff\1\74\17\uffff\1\72",
            "\1\130\4\uffff\1\131\1\uffff\1\131",
            "\1\132",
            "\1\62\11\uffff\1\63\25\uffff\1\61",
            "\1\133\3\uffff\1\71\1\43\1\71\1\43",
            "\1\134\3\uffff\1\135\1\uffff\1\135",
            "\1\137\37\uffff\1\136",
            "\1\105",
            "\1\105",
            "\1\140\4\uffff\1\141\1\uffff\1\141",
            "\1\142",
            "\1\73\17\uffff\1\74\17\uffff\1\72",
            "\1\143\1\uffff\1\143",
            "\1\144",
            "\1\62\11\uffff\1\63\25\uffff\1\61",
            "\1\71\1\43\1\71\1\43",
            "\1\145\3\uffff\1\146\1\uffff\1\146",
            "\1\150\37\uffff\1\147",
            "\1\105",
            "\1\105",
            "\1\151\1\uffff\1\151",
            "\1\152",
            "\1\73\17\uffff\1\74\17\uffff\1\72",
            "\1\153",
            "\1\62\11\uffff\1\63\25\uffff\1\61",
            "\1\154\1\uffff\1\154",
            "\1\156\37\uffff\1\155",
            "\1\105",
            "\1\105",
            "\1\157",
            "\1\73\17\uffff\1\74\17\uffff\1\72",
            "\1\62\11\uffff\1\63\25\uffff\1\61",
            "\1\161\37\uffff\1\160",
            "\1\105",
            "\1\105",
            "\1\73\17\uffff\1\74\17\uffff\1\72",
            "\1\105",
            "\1\105"
    };

    static final short[] DFA187_eot = DFA.unpackEncodedString(DFA187_eotS);
    static final short[] DFA187_eof = DFA.unpackEncodedString(DFA187_eofS);
    static final char[] DFA187_min = DFA.unpackEncodedStringToUnsignedChars(DFA187_minS);
    static final char[] DFA187_max = DFA.unpackEncodedStringToUnsignedChars(DFA187_maxS);
    static final short[] DFA187_accept = DFA.unpackEncodedString(DFA187_acceptS);
    static final short[] DFA187_special = DFA.unpackEncodedString(DFA187_specialS);
    static final short[][] DFA187_transition;

    static {
        int numStates = DFA187_transitionS.length;
        DFA187_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA187_transition[i] = DFA.unpackEncodedString(DFA187_transitionS[i]);
        }
    }

    class DFA187 extends DFA {

        public DFA187(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 187;
            this.eot = DFA187_eot;
            this.eof = DFA187_eof;
            this.min = DFA187_min;
            this.max = DFA187_max;
            this.accept = DFA187_accept;
            this.special = DFA187_special;
            this.transition = DFA187_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( GEN | COMMENT | CDO | CDC | INCLUDES | DASHMATCH | GREATER | LBRACE | RBRACE | LBRACKET | RBRACKET | OPEQ | SEMI | COLON | DCOLON | SOLIDUS | MINUS | PLUS | STAR | LPAREN | RPAREN | COMMA | DOT | TILDE | PIPE | STRING | IDENT | HASH | IMPORT_SYM | PAGE_SYM | MEDIA_SYM | CHARSET_SYM | NAMESPACE_SYM | IMPORTANT_SYM | NUMBER | URI | WS | NL );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA187_51 = input.LA(1);

                        s = -1;
                        if ( (LA187_51=='r') ) {s = 61;}

                        else if ( (LA187_51=='0') ) {s = 62;}

                        else if ( (LA187_51=='R') ) {s = 63;}

                        else if ( ((LA187_51>='\u0000' && LA187_51<='\t')||LA187_51=='\u000B'||(LA187_51>='\u000E' && LA187_51<='/')||(LA187_51>='1' && LA187_51<='4')||LA187_51=='6'||(LA187_51>='8' && LA187_51<='Q')||(LA187_51>='S' && LA187_51<='q')||(LA187_51>='s' && LA187_51<='\uFFFF')) ) {s = 22;}

                        else if ( (LA187_51=='5'||LA187_51=='7') ) {s = 64;}

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA187_25 = input.LA(1);

                        s = -1;
                        if ( (LA187_25=='u') ) {s = 52;}

                        else if ( (LA187_25=='0') ) {s = 53;}

                        else if ( (LA187_25=='U') ) {s = 54;}

                        else if ( ((LA187_25>='\u0000' && LA187_25<='\t')||LA187_25=='\u000B'||(LA187_25>='\u000E' && LA187_25<='/')||(LA187_25>='1' && LA187_25<='4')||LA187_25=='6'||(LA187_25>='8' && LA187_25<='T')||(LA187_25>='V' && LA187_25<='t')||(LA187_25>='v' && LA187_25<='\uFFFF')) ) {s = 22;}

                        else if ( (LA187_25=='5'||LA187_25=='7') ) {s = 55;}

                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA187_60 = input.LA(1);

                        s = -1;
                        if ( (LA187_60=='l') ) {s = 70;}

                        else if ( (LA187_60=='0') ) {s = 71;}

                        else if ( (LA187_60=='L') ) {s = 72;}

                        else if ( ((LA187_60>='\u0000' && LA187_60<='\t')||LA187_60=='\u000B'||(LA187_60>='\u000E' && LA187_60<='/')||(LA187_60>='1' && LA187_60<='3')||LA187_60=='5'||(LA187_60>='7' && LA187_60<='K')||(LA187_60>='M' && LA187_60<='k')||(LA187_60>='m' && LA187_60<='\uFFFF')) ) {s = 22;}

                        else if ( (LA187_60=='4'||LA187_60=='6') ) {s = 73;}

                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 187, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA189_eotS =
        "\12\uffff";
    static final String DFA189_eofS =
        "\12\uffff";
    static final String DFA189_minS =
        "\1\103\1\uffff\1\60\2\uffff\1\60\1\64\2\60\1\64";
    static final String DFA189_maxS =
        "\1\170\1\uffff\1\170\2\uffff\1\67\1\70\3\67";
    static final String DFA189_acceptS =
        "\1\uffff\1\1\1\uffff\1\2\1\3\5\uffff";
    static final String DFA189_specialS =
        "\12\uffff}>";
    static final String[] DFA189_transitionS = {
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

    class DFA189 extends DFA {

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
        public String getDescription() {
            return "884:17: ( X | T | C )";
        }
    }
 

}