// $ANTLR 3.1.3 Mar 17, 2009 19:23:44 C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g 2010-10-21 13:38:39

package org.netbeans.modules.java.j2seproject.ui.customizer.vmo.gen;

import java.util.LinkedList;
import java.util.Queue;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class CommandLineLexer extends Lexer {
    public static final int HELP=13;
    public static final int T__42=42;
    public static final int DEA=33;
    public static final int SVERION=12;
    public static final int T__40=40;
    public static final int XBATCH=16;
    public static final int T__41=41;
    public static final int EA=32;
    public static final int CLIENT=6;
    public static final int DSA=8;
    public static final int XNOCLSGC=19;
    public static final int BOOTCP=24;
    public static final int CPROP=38;
    public static final int MEMSIZE=25;
    public static final int ESA=7;
    public static final int SS=28;
    public static final int VERSION=11;
    public static final int MEMS=26;
    public static final int EOF=-1;
    public static final int JRE_NO_SEARCH=36;
    public static final int X=14;
    public static final int XFUTURE=18;
    public static final int SERVER=5;
    public static final int VERBOSE=9;
    public static final int XSHARE=23;
    public static final int WS=4;
    public static final int XPROF=21;
    public static final int SPLASH=30;
    public static final int XRS=22;
    public static final int MEMX=27;
    public static final int Text=10;
    public static final int AGENT=34;
    public static final int JAGENT=31;
    public static final int JRE_SEARCH=35;
    public static final int XINCGC=20;
    public static final int XINT=15;
    public static final int XCJNI=17;
    public static final int LOGGC=29;
    public static final int CP=37;
    public static final int Letter=39;

        private Queue<Token> safeguard = new LinkedList<Token>();

        @Override
        public void recover(RecognitionException re) {
    	
            input.rewind();
            while (input.getCharPositionInLine() <= re.charPositionInLine) {
                try {
                    state.token = null;
                    state.channel = Token.DEFAULT_CHANNEL;
                    state.tokenStartCharIndex = input.index();
                    state.tokenStartCharPositionInLine = input.getCharPositionInLine();
                    state.tokenStartLine = input.getLine();
                    state.text = null;
                    mLetter();
                    safeguard.offer(emit());
                } catch (RecognitionException e) {
                    input.consume();
                }
            }
            skip();

        }

        @Override
        public void reportError(RecognitionException e) {
            //System.out.println("ERROR: " + e);        // no reporting yet.
        }

        @Override
        public Token nextToken() {        
            safeguard.offer(super.nextToken());
            return safeguard.poll();
        }


    // delegates
    // delegators

    public CommandLineLexer() {;} 
    public CommandLineLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public CommandLineLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g"; }

    // $ANTLR start "T__40"
    public final void mT__40() throws RecognitionException {
        try {
            int _type = T__40;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:46:7: ( '-' )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:46:9: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__40"

    // $ANTLR start "T__41"
    public final void mT__41() throws RecognitionException {
        try {
            int _type = T__41;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:47:7: ( '=' )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:47:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__41"

    // $ANTLR start "T__42"
    public final void mT__42() throws RecognitionException {
        try {
            int _type = T__42;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:48:7: ( ':' )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:48:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__42"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:165:4: ( ( ' ' | '\\r' | '\\t' | '\\u000C' | '\\n' ) )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:165:6: ( ' ' | '\\r' | '\\t' | '\\u000C' | '\\n' )
            {
            if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "SERVER"
    public final void mSERVER() throws RecognitionException {
        try {
            int _type = SERVER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:168:8: ( 'server' )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:168:10: 'server'
            {
            match("server"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SERVER"

    // $ANTLR start "CLIENT"
    public final void mCLIENT() throws RecognitionException {
        try {
            int _type = CLIENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:169:8: ( 'client' )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:169:10: 'client'
            {
            match("client"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CLIENT"

    // $ANTLR start "ESA"
    public final void mESA() throws RecognitionException {
        try {
            int _type = ESA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:170:5: ( 'enablesystemassertions' | 'esa' )
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0=='e') ) {
                int LA1_1 = input.LA(2);

                if ( (LA1_1=='n') ) {
                    alt1=1;
                }
                else if ( (LA1_1=='s') ) {
                    alt1=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 1, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }
            switch (alt1) {
                case 1 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:170:7: 'enablesystemassertions'
                    {
                    match("enablesystemassertions"); 


                    }
                    break;
                case 2 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:170:32: 'esa'
                    {
                    match("esa"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ESA"

    // $ANTLR start "DSA"
    public final void mDSA() throws RecognitionException {
        try {
            int _type = DSA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:171:5: ( 'disablesystemassertions' | 'dsa' )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='d') ) {
                int LA2_1 = input.LA(2);

                if ( (LA2_1=='i') ) {
                    alt2=1;
                }
                else if ( (LA2_1=='s') ) {
                    alt2=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:171:7: 'disablesystemassertions'
                    {
                    match("disablesystemassertions"); 


                    }
                    break;
                case 2 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:171:35: 'dsa'
                    {
                    match("dsa"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DSA"

    // $ANTLR start "VERBOSE"
    public final void mVERBOSE() throws RecognitionException {
        try {
            int _type = VERBOSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:172:9: ( 'verbose' ( ':' ( 'class' | 'gc' | 'jni' ) )? )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:172:11: 'verbose' ( ':' ( 'class' | 'gc' | 'jni' ) )?
            {
            match("verbose"); 

            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:172:21: ( ':' ( 'class' | 'gc' | 'jni' ) )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==':') ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:172:22: ':' ( 'class' | 'gc' | 'jni' )
                    {
                    match(':'); 
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:172:26: ( 'class' | 'gc' | 'jni' )
                    int alt3=3;
                    switch ( input.LA(1) ) {
                    case 'c':
                        {
                        alt3=1;
                        }
                        break;
                    case 'g':
                        {
                        alt3=2;
                        }
                        break;
                    case 'j':
                        {
                        alt3=3;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 3, 0, input);

                        throw nvae;
                    }

                    switch (alt3) {
                        case 1 :
                            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:172:27: 'class'
                            {
                            match("class"); 


                            }
                            break;
                        case 2 :
                            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:172:35: 'gc'
                            {
                            match("gc"); 


                            }
                            break;
                        case 3 :
                            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:172:40: 'jni'
                            {
                            match("jni"); 


                            }
                            break;

                    }


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
    // $ANTLR end "VERBOSE"

    // $ANTLR start "VERSION"
    public final void mVERSION() throws RecognitionException {
        try {
            int _type = VERSION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:173:9: ( 'version' ( ':' Text )? )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:173:11: 'version' ( ':' Text )?
            {
            match("version"); 

            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:173:21: ( ':' Text )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==':') ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:173:22: ':' Text
                    {
                    match(':'); 
                    mText(); 

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
    // $ANTLR end "VERSION"

    // $ANTLR start "SVERION"
    public final void mSVERION() throws RecognitionException {
        try {
            int _type = SVERION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:174:9: ( 'showversion' )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:174:11: 'showversion'
            {
            match("showversion"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SVERION"

    // $ANTLR start "HELP"
    public final void mHELP() throws RecognitionException {
        try {
            int _type = HELP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:175:6: ( 'help' | '?' )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0=='h') ) {
                alt6=1;
            }
            else if ( (LA6_0=='?') ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:175:8: 'help'
                    {
                    match("help"); 


                    }
                    break;
                case 2 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:175:17: '?'
                    {
                    match('?'); 

                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "HELP"

    // $ANTLR start "X"
    public final void mX() throws RecognitionException {
        try {
            int _type = X;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:176:3: ( 'X' )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:176:5: 'X'
            {
            match('X'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "X"

    // $ANTLR start "XINT"
    public final void mXINT() throws RecognitionException {
        try {
            int _type = XINT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:177:6: ( 'Xint' )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:177:8: 'Xint'
            {
            match("Xint"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "XINT"

    // $ANTLR start "XBATCH"
    public final void mXBATCH() throws RecognitionException {
        try {
            int _type = XBATCH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:178:8: ( 'Xbatch' )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:178:10: 'Xbatch'
            {
            match("Xbatch"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "XBATCH"

    // $ANTLR start "XCJNI"
    public final void mXCJNI() throws RecognitionException {
        try {
            int _type = XCJNI;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:179:7: ( 'Xcheck:jni' )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:179:9: 'Xcheck:jni'
            {
            match("Xcheck:jni"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "XCJNI"

    // $ANTLR start "XFUTURE"
    public final void mXFUTURE() throws RecognitionException {
        try {
            int _type = XFUTURE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:180:9: ( 'Xfuture' )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:180:11: 'Xfuture'
            {
            match("Xfuture"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "XFUTURE"

    // $ANTLR start "XNOCLSGC"
    public final void mXNOCLSGC() throws RecognitionException {
        try {
            int _type = XNOCLSGC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:181:9: ( 'Xnoclassgc' )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:181:11: 'Xnoclassgc'
            {
            match("Xnoclassgc"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "XNOCLSGC"

    // $ANTLR start "XINCGC"
    public final void mXINCGC() throws RecognitionException {
        try {
            int _type = XINCGC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:182:8: ( 'Xincgc' )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:182:10: 'Xincgc'
            {
            match("Xincgc"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "XINCGC"

    // $ANTLR start "XPROF"
    public final void mXPROF() throws RecognitionException {
        try {
            int _type = XPROF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:183:7: ( 'Xprof' )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:183:9: 'Xprof'
            {
            match("Xprof"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "XPROF"

    // $ANTLR start "XRS"
    public final void mXRS() throws RecognitionException {
        try {
            int _type = XRS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:184:5: ( 'Xrs' )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:184:7: 'Xrs'
            {
            match("Xrs"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "XRS"

    // $ANTLR start "XSHARE"
    public final void mXSHARE() throws RecognitionException {
        try {
            int _type = XSHARE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:185:8: ( 'Xshare:' ( 'off' | 'on' | 'auto' ) )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:185:10: 'Xshare:' ( 'off' | 'on' | 'auto' )
            {
            match("Xshare:"); 

            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:185:19: ( 'off' | 'on' | 'auto' )
            int alt7=3;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='o') ) {
                int LA7_1 = input.LA(2);

                if ( (LA7_1=='f') ) {
                    alt7=1;
                }
                else if ( (LA7_1=='n') ) {
                    alt7=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 7, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA7_0=='a') ) {
                alt7=3;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:185:20: 'off'
                    {
                    match("off"); 


                    }
                    break;
                case 2 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:185:26: 'on'
                    {
                    match("on"); 


                    }
                    break;
                case 3 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:185:31: 'auto'
                    {
                    match("auto"); 


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
    // $ANTLR end "XSHARE"

    // $ANTLR start "BOOTCP"
    public final void mBOOTCP() throws RecognitionException {
        try {
            int _type = BOOTCP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:186:8: ( 'Xbootclasspath' ( '/a' | '/p' )? ':' Text )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:186:10: 'Xbootclasspath' ( '/a' | '/p' )? ':' Text
            {
            match("Xbootclasspath"); 

            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:186:26: ( '/a' | '/p' )?
            int alt8=3;
            int LA8_0 = input.LA(1);

            if ( (LA8_0=='/') ) {
                int LA8_1 = input.LA(2);

                if ( (LA8_1=='a') ) {
                    alt8=1;
                }
                else if ( (LA8_1=='p') ) {
                    alt8=2;
                }
            }
            switch (alt8) {
                case 1 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:186:27: '/a'
                    {
                    match("/a"); 


                    }
                    break;
                case 2 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:186:32: '/p'
                    {
                    match("/p"); 


                    }
                    break;

            }

            match(':'); 
            mText(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BOOTCP"

    // $ANTLR start "MEMS"
    public final void mMEMS() throws RecognitionException {
        try {
            int _type = MEMS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:187:6: ( 'Xms' MEMSIZE )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:187:8: 'Xms' MEMSIZE
            {
            match("Xms"); 

            mMEMSIZE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MEMS"

    // $ANTLR start "MEMX"
    public final void mMEMX() throws RecognitionException {
        try {
            int _type = MEMX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:188:6: ( 'Xmx' MEMSIZE )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:188:8: 'Xmx' MEMSIZE
            {
            match("Xmx"); 

            mMEMSIZE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MEMX"

    // $ANTLR start "SS"
    public final void mSS() throws RecognitionException {
        try {
            int _type = SS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:189:4: ( 'Xss' MEMSIZE )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:189:6: 'Xss' MEMSIZE
            {
            match("Xss"); 

            mMEMSIZE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SS"

    // $ANTLR start "LOGGC"
    public final void mLOGGC() throws RecognitionException {
        try {
            int _type = LOGGC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:190:7: ( 'Xloggc:' Text )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:190:9: 'Xloggc:' Text
            {
            match("Xloggc:"); 

            mText(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LOGGC"

    // $ANTLR start "SPLASH"
    public final void mSPLASH() throws RecognitionException {
        try {
            int _type = SPLASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:191:8: ( 'splash:' Text )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:191:10: 'splash:' Text
            {
            match("splash:"); 

            mText(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SPLASH"

    // $ANTLR start "JAGENT"
    public final void mJAGENT() throws RecognitionException {
        try {
            int _type = JAGENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:192:8: ( 'javaagent:' Text )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:192:10: 'javaagent:' Text
            {
            match("javaagent:"); 

            mText(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "JAGENT"

    // $ANTLR start "EA"
    public final void mEA() throws RecognitionException {
        try {
            int _type = EA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:193:4: ( 'ea' | 'enableassertions' )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0=='e') ) {
                int LA9_1 = input.LA(2);

                if ( (LA9_1=='a') ) {
                    alt9=1;
                }
                else if ( (LA9_1=='n') ) {
                    alt9=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 9, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:193:6: 'ea'
                    {
                    match("ea"); 


                    }
                    break;
                case 2 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:193:12: 'enableassertions'
                    {
                    match("enableassertions"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EA"

    // $ANTLR start "DEA"
    public final void mDEA() throws RecognitionException {
        try {
            int _type = DEA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:194:5: ( 'disableassertions' | 'da' )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0=='d') ) {
                int LA10_1 = input.LA(2);

                if ( (LA10_1=='i') ) {
                    alt10=1;
                }
                else if ( (LA10_1=='a') ) {
                    alt10=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 10, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:194:7: 'disableassertions'
                    {
                    match("disableassertions"); 


                    }
                    break;
                case 2 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:194:27: 'da'
                    {
                    match("da"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DEA"

    // $ANTLR start "AGENT"
    public final void mAGENT() throws RecognitionException {
        try {
            int _type = AGENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:195:7: ( ( 'agentlib' | 'agentpath' ) ':' Text )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:195:9: ( 'agentlib' | 'agentpath' ) ':' Text
            {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:195:9: ( 'agentlib' | 'agentpath' )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0=='a') ) {
                int LA11_1 = input.LA(2);

                if ( (LA11_1=='g') ) {
                    int LA11_2 = input.LA(3);

                    if ( (LA11_2=='e') ) {
                        int LA11_3 = input.LA(4);

                        if ( (LA11_3=='n') ) {
                            int LA11_4 = input.LA(5);

                            if ( (LA11_4=='t') ) {
                                int LA11_5 = input.LA(6);

                                if ( (LA11_5=='l') ) {
                                    alt11=1;
                                }
                                else if ( (LA11_5=='p') ) {
                                    alt11=2;
                                }
                                else {
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 11, 5, input);

                                    throw nvae;
                                }
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 11, 4, input);

                                throw nvae;
                            }
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 11, 3, input);

                            throw nvae;
                        }
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 11, 2, input);

                        throw nvae;
                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 11, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:195:10: 'agentlib'
                    {
                    match("agentlib"); 


                    }
                    break;
                case 2 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:195:23: 'agentpath'
                    {
                    match("agentpath"); 


                    }
                    break;

            }

            match(':'); 
            mText(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AGENT"

    // $ANTLR start "JRE_SEARCH"
    public final void mJRE_SEARCH() throws RecognitionException {
        try {
            int _type = JRE_SEARCH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:197:2: ( 'jre-restrict-search' )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:197:4: 'jre-restrict-search'
            {
            match("jre-restrict-search"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "JRE_SEARCH"

    // $ANTLR start "JRE_NO_SEARCH"
    public final void mJRE_NO_SEARCH() throws RecognitionException {
        try {
            int _type = JRE_NO_SEARCH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:199:2: ( 'jre-no-restrict-search' )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:199:4: 'jre-no-restrict-search'
            {
            match("jre-no-restrict-search"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "JRE_NO_SEARCH"

    // $ANTLR start "CP"
    public final void mCP() throws RecognitionException {
        try {
            int _type = CP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:200:4: ( 'cp' | 'classpath' )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0=='c') ) {
                int LA12_1 = input.LA(2);

                if ( (LA12_1=='p') ) {
                    alt12=1;
                }
                else if ( (LA12_1=='l') ) {
                    alt12=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 12, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:200:6: 'cp'
                    {
                    match("cp"); 


                    }
                    break;
                case 2 :
                    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:200:13: 'classpath'
                    {
                    match("classpath"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CP"

    // $ANTLR start "CPROP"
    public final void mCPROP() throws RecognitionException {
        try {
            int _type = CPROP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:202:7: ( 'D' Text )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:202:9: 'D' Text
            {
            match('D'); 
            mText(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CPROP"

    // $ANTLR start "Letter"
    public final void mLetter() throws RecognitionException {
        try {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:206:5: ( '\\u0021' | '\\u0023' .. '\\u0026' | '\\u002b' | '\\u002e' .. '\\u0039' | '\\u0041' .. '\\u005a' | '\\u005c' | '\\u005f' | '\\u0061' .. '\\u007a' | '\\u007e' | '\\u00c0' .. '\\u00d6' | '\\u00d8' .. '\\u00f6' | '\\u00f8' .. '\\u00ff' | '\\u0100' .. '\\u1fff' | '\\u3040' .. '\\u318f' | '\\u3300' .. '\\u337f' | '\\u3400' .. '\\u3d2d' | '\\u4e00' .. '\\u9fff' | '\\uf900' .. '\\ufaff' )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:
            {
            if ( input.LA(1)=='!'||(input.LA(1)>='#' && input.LA(1)<='&')||input.LA(1)=='+'||(input.LA(1)>='.' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='\\'||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z')||input.LA(1)=='~'||(input.LA(1)>='\u00C0' && input.LA(1)<='\u00D6')||(input.LA(1)>='\u00D8' && input.LA(1)<='\u00F6')||(input.LA(1)>='\u00F8' && input.LA(1)<='\u1FFF')||(input.LA(1)>='\u3040' && input.LA(1)<='\u318F')||(input.LA(1)>='\u3300' && input.LA(1)<='\u337F')||(input.LA(1)>='\u3400' && input.LA(1)<='\u3D2D')||(input.LA(1)>='\u4E00' && input.LA(1)<='\u9FFF')||(input.LA(1)>='\uF900' && input.LA(1)<='\uFAFF') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Letter"

    // $ANTLR start "MEMSIZE"
    public final void mMEMSIZE() throws RecognitionException {
        try {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:227:9: ( ( '0' .. '9' )+ ( 'k' | 'm' | 'K' | 'M' ) )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:227:11: ( '0' .. '9' )+ ( 'k' | 'm' | 'K' | 'M' )
            {
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:227:11: ( '0' .. '9' )+
            int cnt13=0;
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( ((LA13_0>='0' && LA13_0<='9')) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:227:12: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt13 >= 1 ) break loop13;
                        EarlyExitException eee =
                            new EarlyExitException(13, input);
                        throw eee;
                }
                cnt13++;
            } while (true);

            if ( input.LA(1)=='K'||input.LA(1)=='M'||input.LA(1)=='k'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "MEMSIZE"

    // $ANTLR start "Text"
    public final void mText() throws RecognitionException {
        try {
            int _type = Text;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:229:6: ( Letter ( Letter | '-' | ':' )* )
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:229:8: Letter ( Letter | '-' | ':' )*
            {
            mLetter(); 
            // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:229:15: ( Letter | '-' | ':' )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0=='!'||(LA14_0>='#' && LA14_0<='&')||LA14_0=='+'||(LA14_0>='-' && LA14_0<=':')||(LA14_0>='A' && LA14_0<='Z')||LA14_0=='\\'||LA14_0=='_'||(LA14_0>='a' && LA14_0<='z')||LA14_0=='~'||(LA14_0>='\u00C0' && LA14_0<='\u00D6')||(LA14_0>='\u00D8' && LA14_0<='\u00F6')||(LA14_0>='\u00F8' && LA14_0<='\u1FFF')||(LA14_0>='\u3040' && LA14_0<='\u318F')||(LA14_0>='\u3300' && LA14_0<='\u337F')||(LA14_0>='\u3400' && LA14_0<='\u3D2D')||(LA14_0>='\u4E00' && LA14_0<='\u9FFF')||(LA14_0>='\uF900' && LA14_0<='\uFAFF')) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:
            	    {
            	    if ( input.LA(1)=='!'||(input.LA(1)>='#' && input.LA(1)<='&')||input.LA(1)=='+'||(input.LA(1)>='-' && input.LA(1)<=':')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='\\'||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z')||input.LA(1)=='~'||(input.LA(1)>='\u00C0' && input.LA(1)<='\u00D6')||(input.LA(1)>='\u00D8' && input.LA(1)<='\u00F6')||(input.LA(1)>='\u00F8' && input.LA(1)<='\u1FFF')||(input.LA(1)>='\u3040' && input.LA(1)<='\u318F')||(input.LA(1)>='\u3300' && input.LA(1)<='\u337F')||(input.LA(1)>='\u3400' && input.LA(1)<='\u3D2D')||(input.LA(1)>='\u4E00' && input.LA(1)<='\u9FFF')||(input.LA(1)>='\uF900' && input.LA(1)<='\uFAFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Text"

    public void mTokens() throws RecognitionException {
        // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:8: ( T__40 | T__41 | T__42 | WS | SERVER | CLIENT | ESA | DSA | VERBOSE | VERSION | SVERION | HELP | X | XINT | XBATCH | XCJNI | XFUTURE | XNOCLSGC | XINCGC | XPROF | XRS | XSHARE | BOOTCP | MEMS | MEMX | SS | LOGGC | SPLASH | JAGENT | EA | DEA | AGENT | JRE_SEARCH | JRE_NO_SEARCH | CP | CPROP | Text )
        int alt15=37;
        alt15 = dfa15.predict(input);
        switch (alt15) {
            case 1 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:10: T__40
                {
                mT__40(); 

                }
                break;
            case 2 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:16: T__41
                {
                mT__41(); 

                }
                break;
            case 3 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:22: T__42
                {
                mT__42(); 

                }
                break;
            case 4 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:28: WS
                {
                mWS(); 

                }
                break;
            case 5 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:31: SERVER
                {
                mSERVER(); 

                }
                break;
            case 6 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:38: CLIENT
                {
                mCLIENT(); 

                }
                break;
            case 7 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:45: ESA
                {
                mESA(); 

                }
                break;
            case 8 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:49: DSA
                {
                mDSA(); 

                }
                break;
            case 9 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:53: VERBOSE
                {
                mVERBOSE(); 

                }
                break;
            case 10 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:61: VERSION
                {
                mVERSION(); 

                }
                break;
            case 11 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:69: SVERION
                {
                mSVERION(); 

                }
                break;
            case 12 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:77: HELP
                {
                mHELP(); 

                }
                break;
            case 13 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:82: X
                {
                mX(); 

                }
                break;
            case 14 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:84: XINT
                {
                mXINT(); 

                }
                break;
            case 15 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:89: XBATCH
                {
                mXBATCH(); 

                }
                break;
            case 16 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:96: XCJNI
                {
                mXCJNI(); 

                }
                break;
            case 17 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:102: XFUTURE
                {
                mXFUTURE(); 

                }
                break;
            case 18 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:110: XNOCLSGC
                {
                mXNOCLSGC(); 

                }
                break;
            case 19 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:119: XINCGC
                {
                mXINCGC(); 

                }
                break;
            case 20 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:126: XPROF
                {
                mXPROF(); 

                }
                break;
            case 21 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:132: XRS
                {
                mXRS(); 

                }
                break;
            case 22 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:136: XSHARE
                {
                mXSHARE(); 

                }
                break;
            case 23 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:143: BOOTCP
                {
                mBOOTCP(); 

                }
                break;
            case 24 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:150: MEMS
                {
                mMEMS(); 

                }
                break;
            case 25 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:155: MEMX
                {
                mMEMX(); 

                }
                break;
            case 26 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:160: SS
                {
                mSS(); 

                }
                break;
            case 27 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:163: LOGGC
                {
                mLOGGC(); 

                }
                break;
            case 28 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:169: SPLASH
                {
                mSPLASH(); 

                }
                break;
            case 29 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:176: JAGENT
                {
                mJAGENT(); 

                }
                break;
            case 30 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:183: EA
                {
                mEA(); 

                }
                break;
            case 31 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:186: DEA
                {
                mDEA(); 

                }
                break;
            case 32 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:190: AGENT
                {
                mAGENT(); 

                }
                break;
            case 33 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:196: JRE_SEARCH
                {
                mJRE_SEARCH(); 

                }
                break;
            case 34 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:207: JRE_NO_SEARCH
                {
                mJRE_NO_SEARCH(); 

                }
                break;
            case 35 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:221: CP
                {
                mCP(); 

                }
                break;
            case 36 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:224: CPROP
                {
                mCPROP(); 

                }
                break;
            case 37 :
                // C:\\SunWork\\nb-jet\\main\\java.j2seproject\\src/org/netbeans/modules/java/j2seproject/ui/customizer/vmo/gen/CommandLine.g:1:230: Text
                {
                mText(); 

                }
                break;

        }

    }


    protected DFA15 dfa15 = new DFA15(this);
    static final String DFA15_eotS =
        "\5\uffff\6\20\1\uffff\1\50\3\20\1\uffff\4\20\1\62\2\20\1\65\2\20"+
        "\1\70\14\20\1\uffff\3\20\1\113\5\20\1\uffff\1\20\1\123\1\uffff\1"+
        "\20\1\125\1\uffff\11\20\1\141\10\20\1\uffff\1\113\6\20\1\uffff\1"+
        "\20\1\uffff\2\20\1\13\1\163\7\20\1\uffff\21\20\1\uffff\6\20\1\u0093"+
        "\1\20\1\u0095\1\u0096\1\u0097\5\20\1\u009e\2\20\1\u00a1\5\20\1\u00a8"+
        "\1\u00a9\4\20\1\uffff\1\20\3\uffff\6\20\1\uffff\2\20\1\uffff\4\20"+
        "\1\u00bc\1\u00bf\2\uffff\2\20\1\u00c2\11\20\1\u00cd\5\20\1\uffff"+
        "\2\20\1\uffff\2\20\1\uffff\3\20\1\u00de\6\20\1\uffff\1\u00cd\1\62"+
        "\7\20\1\u00bf\4\20\1\u00f2\1\20\1\uffff\1\u00de\13\20\1\u00bc\1"+
        "\20\1\u00bf\1\20\1\u0100\1\u0101\1\u00f2\1\uffff\4\20\1\u0106\1"+
        "\u0108\5\20\1\u00bc\1\20\2\uffff\1\u00f2\1\u010f\2\20\1\uffff\1"+
        "\u0106\1\uffff\6\20\1\uffff\1\u010f\6\20\1\u00bc\23\20\1\65\4\20"+
        "\1\u0136\4\20\1\70\1\uffff\1\u0136\10\20\1\u0144\3\20\1\uffff\4"+
        "\20\1\123\1\20\1\u014d\1\125\1\uffff";
    static final String DFA15_eofS =
        "\u014e\uffff";
    static final String DFA15_minS =
        "\1\11\4\uffff\1\145\1\154\2\141\2\145\1\uffff\1\41\1\141\1\147"+
        "\1\41\1\uffff\1\162\1\157\1\154\1\141\1\41\2\141\1\41\1\163\1\141"+
        "\1\41\1\162\1\154\1\156\1\141\1\150\1\165\1\157\1\162\1\163\1\150"+
        "\1\163\1\157\1\uffff\1\166\2\145\1\41\1\166\1\167\1\141\1\145\1"+
        "\163\1\uffff\1\142\1\41\1\uffff\1\141\1\41\1\uffff\1\142\1\160\1"+
        "\143\1\164\1\157\1\145\1\164\1\143\1\157\1\41\1\141\3\60\1\147\1"+
        "\141\1\55\1\156\1\uffff\1\41\1\145\1\166\1\163\1\156\1\163\1\154"+
        "\1\uffff\1\142\1\uffff\1\157\1\151\2\41\1\147\1\143\1\164\1\143"+
        "\1\165\1\154\1\146\1\uffff\1\162\3\60\1\147\1\141\1\156\1\164\1"+
        "\162\1\145\1\150\1\164\1\160\1\145\1\154\1\163\1\157\1\uffff\1\143"+
        "\1\150\1\143\1\153\1\162\1\141\1\41\1\145\3\41\1\143\1\147\1\145"+
        "\1\157\1\154\1\41\1\162\1\72\1\41\2\141\2\145\1\156\2\41\1\154\1"+
        "\72\1\145\1\163\1\uffff\1\72\3\uffff\1\72\1\145\1\163\1\55\1\151"+
        "\1\141\1\uffff\1\163\1\41\1\uffff\1\164\1\171\1\163\1\141\2\41\2"+
        "\uffff\1\141\1\152\1\41\1\163\1\141\1\41\1\156\1\164\1\162\1\142"+
        "\1\164\1\151\1\41\1\150\2\163\1\171\1\163\1\uffff\1\143\1\41\1\uffff"+
        "\1\163\1\156\1\uffff\1\147\1\146\1\165\1\41\1\164\1\162\1\145\1"+
        "\72\1\150\1\157\1\uffff\2\41\1\164\1\145\2\163\1\154\1\143\1\156"+
        "\1\41\1\163\1\151\1\143\1\146\1\41\1\164\1\uffff\1\41\1\72\1\151"+
        "\1\163\1\41\1\72\1\156\1\145\1\162\1\164\1\145\1\141\1\41\1\151"+
        "\1\41\1\160\3\41\1\uffff\1\157\1\41\1\143\1\164\2\41\1\155\1\164"+
        "\1\145\1\162\1\163\1\41\1\141\2\uffff\2\41\1\164\1\162\1\uffff\1"+
        "\41\1\uffff\1\141\1\151\1\155\1\164\1\163\1\164\1\uffff\1\41\1\55"+
        "\1\151\1\163\1\157\1\141\1\151\1\41\1\150\1\163\1\143\1\163\1\156"+
        "\1\163\1\157\1\57\1\145\1\164\1\145\2\163\1\156\1\141\1\41\1\141"+
        "\1\55\1\162\1\41\1\145\1\163\2\72\1\41\1\162\1\163\1\164\1\162\1"+
        "\41\1\uffff\1\41\1\143\1\145\1\151\1\164\1\150\1\141\1\157\1\151"+
        "\1\41\1\162\1\156\1\157\1\uffff\1\143\1\163\1\156\1\150\1\41\1\163"+
        "\2\41\1\uffff";
    static final String DFA15_maxS =
        "\1\ufaff\4\uffff\2\160\2\163\2\145\1\uffff\1\ufaff\1\162\1\147"+
        "\1\ufaff\1\uffff\1\162\1\157\1\154\1\151\1\ufaff\2\141\1\ufaff\1"+
        "\163\1\141\1\ufaff\1\162\1\154\1\156\1\157\1\150\1\165\1\157\1\162"+
        "\2\163\1\170\1\157\1\uffff\1\166\2\145\1\ufaff\1\166\1\167\1\141"+
        "\1\145\1\163\1\uffff\1\142\1\ufaff\1\uffff\1\141\1\ufaff\1\uffff"+
        "\1\163\1\160\2\164\1\157\1\145\1\164\1\143\1\157\1\ufaff\1\141\3"+
        "\71\1\147\1\141\1\55\1\156\1\uffff\1\ufaff\1\145\1\166\1\163\1\156"+
        "\1\163\1\154\1\uffff\1\142\1\uffff\1\157\1\151\2\ufaff\1\147\1\143"+
        "\1\164\1\143\1\165\1\154\1\146\1\uffff\1\162\3\155\1\147\1\141\1"+
        "\162\1\164\1\162\1\145\1\150\1\164\1\160\1\145\1\154\1\163\1\157"+
        "\1\uffff\1\143\1\150\1\143\1\153\1\162\1\141\1\ufaff\1\145\3\ufaff"+
        "\1\143\1\147\1\145\1\157\1\160\1\ufaff\1\162\1\72\1\ufaff\1\141"+
        "\1\163\2\145\1\156\2\ufaff\1\154\1\72\1\145\1\163\1\uffff\1\72\3"+
        "\uffff\1\72\1\145\1\163\1\55\1\151\1\141\1\uffff\1\163\1\ufaff\1"+
        "\uffff\1\164\1\171\2\163\2\ufaff\2\uffff\1\141\1\152\1\ufaff\1\163"+
        "\1\157\1\ufaff\1\156\1\164\1\162\1\142\1\164\1\151\1\ufaff\1\150"+
        "\2\163\1\171\1\163\1\uffff\1\152\1\ufaff\1\uffff\1\163\1\156\1\uffff"+
        "\1\147\1\156\1\165\1\ufaff\1\164\1\162\1\145\1\72\1\150\1\157\1"+
        "\uffff\2\ufaff\1\164\1\145\2\163\1\154\1\143\1\156\1\ufaff\1\163"+
        "\1\151\1\143\1\146\1\ufaff\1\164\1\uffff\1\ufaff\1\72\1\151\1\163"+
        "\1\ufaff\1\72\1\156\1\145\1\162\1\164\1\145\1\141\1\ufaff\1\151"+
        "\1\ufaff\1\160\3\ufaff\1\uffff\1\157\1\ufaff\1\143\1\164\2\ufaff"+
        "\1\155\1\164\1\145\1\162\1\163\1\ufaff\1\141\2\uffff\2\ufaff\1\164"+
        "\1\162\1\uffff\1\ufaff\1\uffff\1\141\1\151\1\155\1\164\1\163\1\164"+
        "\1\uffff\1\ufaff\1\55\1\151\1\163\1\157\1\141\1\151\1\ufaff\1\150"+
        "\1\163\1\143\1\163\1\156\1\163\1\157\1\72\1\145\1\164\1\145\2\163"+
        "\1\156\1\160\1\ufaff\1\141\1\55\1\162\1\ufaff\1\145\1\163\2\72\1"+
        "\ufaff\1\162\1\163\1\164\1\162\1\ufaff\1\uffff\1\ufaff\1\143\1\145"+
        "\1\151\1\164\1\150\1\141\1\157\1\151\1\ufaff\1\162\1\156\1\157\1"+
        "\uffff\1\143\1\163\1\156\1\150\1\ufaff\1\163\2\ufaff\1\uffff";
    static final String DFA15_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\6\uffff\1\14\4\uffff\1\45\27\uffff\1\15"+
        "\11\uffff\1\43\2\uffff\1\36\2\uffff\1\37\22\uffff\1\44\7\uffff\1"+
        "\7\1\uffff\1\10\13\uffff\1\25\21\uffff\1\16\37\uffff\1\24\1\uffff"+
        "\1\32\1\30\1\31\6\uffff\1\5\2\uffff\1\6\6\uffff\1\23\1\17\22\uffff"+
        "\1\11\2\uffff\1\12\2\uffff\1\21\12\uffff\1\34\20\uffff\1\33\23\uffff"+
        "\1\26\15\uffff\1\20\1\22\4\uffff\1\40\1\uffff\1\13\6\uffff\1\35"+
        "\46\uffff\1\27\15\uffff\1\41\10\uffff\1\42";
    static final String DFA15_specialS =
        "\u014e\uffff}>";
    static final String[] DFA15_transitionS = {
            "\2\4\1\uffff\2\4\22\uffff\1\4\1\20\1\uffff\4\20\4\uffff\1\20"+
            "\1\uffff\1\1\14\20\1\3\2\uffff\1\2\1\uffff\1\13\1\uffff\3\20"+
            "\1\17\23\20\1\14\2\20\1\uffff\1\20\2\uffff\1\20\1\uffff\1\16"+
            "\1\20\1\6\1\10\1\7\2\20\1\12\1\20\1\15\10\20\1\5\2\20\1\11\4"+
            "\20\3\uffff\1\20\101\uffff\27\20\1\uffff\37\20\1\uffff\u1f08"+
            "\20\u1040\uffff\u0150\20\u0170\uffff\u0080\20\u0080\uffff\u092e"+
            "\20\u10d2\uffff\u5200\20\u5900\uffff\u0200\20",
            "",
            "",
            "",
            "",
            "\1\21\2\uffff\1\22\7\uffff\1\23",
            "\1\24\3\uffff\1\25",
            "\1\30\14\uffff\1\26\4\uffff\1\27",
            "\1\33\7\uffff\1\31\11\uffff\1\32",
            "\1\34",
            "\1\35",
            "",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\1\20\1\37\1\40\2\20\1\41"+
            "\2\20\1\36\2\20\1\47\1\46\1\42\1\20\1\43\1\20\1\44\1\45\7\20"+
            "\3\uffff\1\20\101\uffff\27\20\1\uffff\37\20\1\uffff\u1f08\20"+
            "\u1040\uffff\u0150\20\u0170\uffff\u0080\20\u0080\uffff\u092e"+
            "\20\u10d2\uffff\u5200\20\u5900\uffff\u0200\20",
            "\1\51\20\uffff\1\52",
            "\1\53",
            "\1\54\1\uffff\4\54\4\uffff\1\54\2\uffff\14\54\7\uffff\32\54"+
            "\1\uffff\1\54\2\uffff\1\54\1\uffff\32\54\3\uffff\1\54\101\uffff"+
            "\27\54\1\uffff\37\54\1\uffff\u1f08\54\u1040\uffff\u0150\54\u0170"+
            "\uffff\u0080\54\u0080\uffff\u092e\54\u10d2\uffff\u5200\54\u5900"+
            "\uffff\u0200\54",
            "",
            "\1\55",
            "\1\56",
            "\1\57",
            "\1\61\7\uffff\1\60",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\63",
            "\1\64",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\66",
            "\1\67",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\71",
            "\1\72",
            "\1\73",
            "\1\74\15\uffff\1\75",
            "\1\76",
            "\1\77",
            "\1\100",
            "\1\101",
            "\1\102",
            "\1\103\12\uffff\1\104",
            "\1\105\4\uffff\1\106",
            "\1\107",
            "",
            "\1\110",
            "\1\111",
            "\1\112",
            "\1\114\1\uffff\4\114\4\uffff\1\114\1\uffff\16\114\6\uffff"+
            "\32\114\1\uffff\1\114\2\uffff\1\114\1\uffff\32\114\3\uffff\1"+
            "\114\101\uffff\27\114\1\uffff\37\114\1\uffff\u1f08\114\u1040"+
            "\uffff\u0150\114\u0170\uffff\u0080\114\u0080\uffff\u092e\114"+
            "\u10d2\uffff\u5200\114\u5900\uffff\u0200\114",
            "\1\115",
            "\1\116",
            "\1\117",
            "\1\120",
            "\1\121",
            "",
            "\1\122",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "",
            "\1\124",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "",
            "\1\126\20\uffff\1\127",
            "\1\130",
            "\1\132\20\uffff\1\131",
            "\1\133",
            "\1\134",
            "\1\135",
            "\1\136",
            "\1\137",
            "\1\140",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\142",
            "\12\143",
            "\12\144",
            "\12\145",
            "\1\146",
            "\1\147",
            "\1\150",
            "\1\151",
            "",
            "\1\114\1\uffff\4\114\4\uffff\1\114\1\uffff\16\114\6\uffff"+
            "\32\114\1\uffff\1\114\2\uffff\1\114\1\uffff\32\114\3\uffff\1"+
            "\114\101\uffff\27\114\1\uffff\37\114\1\uffff\u1f08\114\u1040"+
            "\uffff\u0150\114\u0170\uffff\u0080\114\u0080\uffff\u092e\114"+
            "\u10d2\uffff\u5200\114\u5900\uffff\u0200\114",
            "\1\152",
            "\1\153",
            "\1\154",
            "\1\155",
            "\1\156",
            "\1\157",
            "",
            "\1\160",
            "",
            "\1\161",
            "\1\162",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\164",
            "\1\165",
            "\1\166",
            "\1\167",
            "\1\170",
            "\1\171",
            "\1\172",
            "",
            "\1\173",
            "\12\143\21\uffff\1\174\1\uffff\1\174\35\uffff\1\174\1\uffff"+
            "\1\174",
            "\12\144\21\uffff\1\175\1\uffff\1\175\35\uffff\1\175\1\uffff"+
            "\1\175",
            "\12\145\21\uffff\1\176\1\uffff\1\176\35\uffff\1\176\1\uffff"+
            "\1\176",
            "\1\177",
            "\1\u0080",
            "\1\u0082\3\uffff\1\u0081",
            "\1\u0083",
            "\1\u0084",
            "\1\u0085",
            "\1\u0086",
            "\1\u0087",
            "\1\u0088",
            "\1\u0089",
            "\1\u008a",
            "\1\u008b",
            "\1\u008c",
            "",
            "\1\u008d",
            "\1\u008e",
            "\1\u008f",
            "\1\u0090",
            "\1\u0091",
            "\1\u0092",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\u0094",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\u0098",
            "\1\u0099",
            "\1\u009a",
            "\1\u009b",
            "\1\u009c\3\uffff\1\u009d",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\u009f",
            "\1\u00a0",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\u00a2",
            "\1\u00a4\21\uffff\1\u00a3",
            "\1\u00a5",
            "\1\u00a6",
            "\1\u00a7",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\u00aa",
            "\1\u00ab",
            "\1\u00ac",
            "\1\u00ad",
            "",
            "\1\u00ae",
            "",
            "",
            "",
            "\1\u00af",
            "\1\u00b0",
            "\1\u00b1",
            "\1\u00b2",
            "\1\u00b3",
            "\1\u00b4",
            "",
            "\1\u00b5",
            "\1\u00b6\1\uffff\4\u00b6\4\uffff\1\u00b6\2\uffff\14\u00b6"+
            "\7\uffff\32\u00b6\1\uffff\1\u00b6\2\uffff\1\u00b6\1\uffff\32"+
            "\u00b6\3\uffff\1\u00b6\101\uffff\27\u00b6\1\uffff\37\u00b6\1"+
            "\uffff\u1f08\u00b6\u1040\uffff\u0150\u00b6\u0170\uffff\u0080"+
            "\u00b6\u0080\uffff\u092e\u00b6\u10d2\uffff\u5200\u00b6\u5900"+
            "\uffff\u0200\u00b6",
            "",
            "\1\u00b7",
            "\1\u00b8",
            "\1\u00b9",
            "\1\u00bb\21\uffff\1\u00ba",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\15\20\1\u00bd\6\uffff"+
            "\32\20\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20"+
            "\101\uffff\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff"+
            "\u0150\20\u0170\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff"+
            "\u5200\20\u5900\uffff\u0200\20",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\15\20\1\u00be\6\uffff"+
            "\32\20\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20"+
            "\101\uffff\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff"+
            "\u0150\20\u0170\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff"+
            "\u5200\20\u5900\uffff\u0200\20",
            "",
            "",
            "\1\u00c0",
            "\1\u00c1",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\u00c3",
            "\1\u00c5\15\uffff\1\u00c4",
            "\1\u00c6\1\uffff\4\u00c6\4\uffff\1\u00c6\2\uffff\14\u00c6"+
            "\7\uffff\32\u00c6\1\uffff\1\u00c6\2\uffff\1\u00c6\1\uffff\32"+
            "\u00c6\3\uffff\1\u00c6\101\uffff\27\u00c6\1\uffff\37\u00c6\1"+
            "\uffff\u1f08\u00c6\u1040\uffff\u0150\u00c6\u0170\uffff\u0080"+
            "\u00c6\u0080\uffff\u092e\u00c6\u10d2\uffff\u5200\u00c6\u5900"+
            "\uffff\u0200\u00c6",
            "\1\u00c7",
            "\1\u00c8",
            "\1\u00c9",
            "\1\u00ca",
            "\1\u00cb",
            "\1\u00cc",
            "\1\u00ce\1\uffff\4\u00ce\4\uffff\1\u00ce\1\uffff\16\u00ce"+
            "\6\uffff\32\u00ce\1\uffff\1\u00ce\2\uffff\1\u00ce\1\uffff\32"+
            "\u00ce\3\uffff\1\u00ce\101\uffff\27\u00ce\1\uffff\37\u00ce\1"+
            "\uffff\u1f08\u00ce\u1040\uffff\u0150\u00ce\u0170\uffff\u0080"+
            "\u00ce\u0080\uffff\u092e\u00ce\u10d2\uffff\u5200\u00ce\u5900"+
            "\uffff\u0200\u00ce",
            "\1\u00cf",
            "\1\u00d0",
            "\1\u00d1",
            "\1\u00d2",
            "\1\u00d3",
            "",
            "\1\u00d4\3\uffff\1\u00d5\2\uffff\1\u00d6",
            "\1\u00d7\1\uffff\4\u00d7\4\uffff\1\u00d7\2\uffff\14\u00d7"+
            "\7\uffff\32\u00d7\1\uffff\1\u00d7\2\uffff\1\u00d7\1\uffff\32"+
            "\u00d7\3\uffff\1\u00d7\101\uffff\27\u00d7\1\uffff\37\u00d7\1"+
            "\uffff\u1f08\u00d7\u1040\uffff\u0150\u00d7\u0170\uffff\u0080"+
            "\u00d7\u0080\uffff\u092e\u00d7\u10d2\uffff\u5200\u00d7\u5900"+
            "\uffff\u0200\u00d7",
            "",
            "\1\u00d8",
            "\1\u00d9",
            "",
            "\1\u00da",
            "\1\u00db\7\uffff\1\u00dc",
            "\1\u00dd",
            "\1\u00df\1\uffff\4\u00df\4\uffff\1\u00df\1\uffff\16\u00df"+
            "\6\uffff\32\u00df\1\uffff\1\u00df\2\uffff\1\u00df\1\uffff\32"+
            "\u00df\3\uffff\1\u00df\101\uffff\27\u00df\1\uffff\37\u00df\1"+
            "\uffff\u1f08\u00df\u1040\uffff\u0150\u00df\u0170\uffff\u0080"+
            "\u00df\u0080\uffff\u092e\u00df\u10d2\uffff\u5200\u00df\u5900"+
            "\uffff\u0200\u00df",
            "\1\u00e0",
            "\1\u00e1",
            "\1\u00e2",
            "\1\u00e3",
            "\1\u00e4",
            "\1\u00e5",
            "",
            "\1\u00ce\1\uffff\4\u00ce\4\uffff\1\u00ce\1\uffff\16\u00ce"+
            "\6\uffff\32\u00ce\1\uffff\1\u00ce\2\uffff\1\u00ce\1\uffff\32"+
            "\u00ce\3\uffff\1\u00ce\101\uffff\27\u00ce\1\uffff\37\u00ce\1"+
            "\uffff\u1f08\u00ce\u1040\uffff\u0150\u00ce\u0170\uffff\u0080"+
            "\u00ce\u0080\uffff\u092e\u00ce\u10d2\uffff\u5200\u00ce\u5900"+
            "\uffff\u0200\u00ce",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\u00e6",
            "\1\u00e7",
            "\1\u00e8",
            "\1\u00e9",
            "\1\u00ea",
            "\1\u00eb",
            "\1\u00ec",
            "\1\u00ed\1\uffff\4\u00ed\4\uffff\1\u00ed\1\uffff\16\u00ed"+
            "\6\uffff\32\u00ed\1\uffff\1\u00ed\2\uffff\1\u00ed\1\uffff\32"+
            "\u00ed\3\uffff\1\u00ed\101\uffff\27\u00ed\1\uffff\37\u00ed\1"+
            "\uffff\u1f08\u00ed\u1040\uffff\u0150\u00ed\u0170\uffff\u0080"+
            "\u00ed\u0080\uffff\u092e\u00ed\u10d2\uffff\u5200\u00ed\u5900"+
            "\uffff\u0200\u00ed",
            "\1\u00ee",
            "\1\u00ef",
            "\1\u00f0",
            "\1\u00f1",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\u00f3",
            "",
            "\1\u00df\1\uffff\4\u00df\4\uffff\1\u00df\1\uffff\16\u00df"+
            "\6\uffff\32\u00df\1\uffff\1\u00df\2\uffff\1\u00df\1\uffff\32"+
            "\u00df\3\uffff\1\u00df\101\uffff\27\u00df\1\uffff\37\u00df\1"+
            "\uffff\u1f08\u00df\u1040\uffff\u0150\u00df\u0170\uffff\u0080"+
            "\u00df\u0080\uffff\u092e\u00df\u10d2\uffff\u5200\u00df\u5900"+
            "\uffff\u0200\u00df",
            "\1\u00f4",
            "\1\u00f5",
            "\1\u00f6",
            "\1\u00f7\1\uffff\4\u00f7\4\uffff\1\u00f7\2\uffff\14\u00f7"+
            "\7\uffff\32\u00f7\1\uffff\1\u00f7\2\uffff\1\u00f7\1\uffff\32"+
            "\u00f7\3\uffff\1\u00f7\101\uffff\27\u00f7\1\uffff\37\u00f7\1"+
            "\uffff\u1f08\u00f7\u1040\uffff\u0150\u00f7\u0170\uffff\u0080"+
            "\u00f7\u0080\uffff\u092e\u00f7\u10d2\uffff\u5200\u00f7\u5900"+
            "\uffff\u0200\u00f7",
            "\1\u00e3",
            "\1\u00f8",
            "\1\u00f9",
            "\1\u00fa",
            "\1\u00fb",
            "\1\u00fc",
            "\1\u00fd",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\u00fe",
            "\1\u00ed\1\uffff\4\u00ed\4\uffff\1\u00ed\1\uffff\16\u00ed"+
            "\6\uffff\32\u00ed\1\uffff\1\u00ed\2\uffff\1\u00ed\1\uffff\32"+
            "\u00ed\3\uffff\1\u00ed\101\uffff\27\u00ed\1\uffff\37\u00ed\1"+
            "\uffff\u1f08\u00ed\u1040\uffff\u0150\u00ed\u0170\uffff\u0080"+
            "\u00ed\u0080\uffff\u092e\u00ed\u10d2\uffff\u5200\u00ed\u5900"+
            "\uffff\u0200\u00ed",
            "\1\u00ff",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "",
            "\1\u0102",
            "\1\u0103\1\uffff\4\u0103\4\uffff\1\u0103\2\uffff\14\u0103"+
            "\7\uffff\32\u0103\1\uffff\1\u0103\2\uffff\1\u0103\1\uffff\32"+
            "\u0103\3\uffff\1\u0103\101\uffff\27\u0103\1\uffff\37\u0103\1"+
            "\uffff\u1f08\u0103\u1040\uffff\u0150\u0103\u0170\uffff\u0080"+
            "\u0103\u0080\uffff\u092e\u0103\u10d2\uffff\u5200\u0103\u5900"+
            "\uffff\u0200\u0103",
            "\1\u0104",
            "\1\u0105",
            "\1\u0107\1\uffff\4\u0107\4\uffff\1\u0107\1\uffff\16\u0107"+
            "\6\uffff\32\u0107\1\uffff\1\u0107\2\uffff\1\u0107\1\uffff\32"+
            "\u0107\3\uffff\1\u0107\101\uffff\27\u0107\1\uffff\37\u0107\1"+
            "\uffff\u1f08\u0107\u1040\uffff\u0150\u0107\u0170\uffff\u0080"+
            "\u0107\u0080\uffff\u092e\u0107\u10d2\uffff\u5200\u0107\u5900"+
            "\uffff\u0200\u0107",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\u0109",
            "\1\u010a",
            "\1\u010b",
            "\1\u010c",
            "\1\u010d",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\u010e",
            "",
            "",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\u0110\1\uffff\4\u0110\4\uffff\1\u0110\1\uffff\16\u0110"+
            "\6\uffff\32\u0110\1\uffff\1\u0110\2\uffff\1\u0110\1\uffff\32"+
            "\u0110\3\uffff\1\u0110\101\uffff\27\u0110\1\uffff\37\u0110\1"+
            "\uffff\u1f08\u0110\u1040\uffff\u0150\u0110\u0170\uffff\u0080"+
            "\u0110\u0080\uffff\u092e\u0110\u10d2\uffff\u5200\u0110\u5900"+
            "\uffff\u0200\u0110",
            "\1\u0111",
            "\1\u0112",
            "",
            "\1\u0107\1\uffff\4\u0107\4\uffff\1\u0107\1\uffff\16\u0107"+
            "\6\uffff\32\u0107\1\uffff\1\u0107\2\uffff\1\u0107\1\uffff\32"+
            "\u0107\3\uffff\1\u0107\101\uffff\27\u0107\1\uffff\37\u0107\1"+
            "\uffff\u1f08\u0107\u1040\uffff\u0150\u0107\u0170\uffff\u0080"+
            "\u0107\u0080\uffff\u092e\u0107\u10d2\uffff\u5200\u0107\u5900"+
            "\uffff\u0200\u0107",
            "",
            "\1\u0113",
            "\1\u0114",
            "\1\u0115",
            "\1\u0116",
            "\1\u0117",
            "\1\u0118",
            "",
            "\1\u0110\1\uffff\4\u0110\4\uffff\1\u0110\1\uffff\16\u0110"+
            "\6\uffff\32\u0110\1\uffff\1\u0110\2\uffff\1\u0110\1\uffff\32"+
            "\u0110\3\uffff\1\u0110\101\uffff\27\u0110\1\uffff\37\u0110\1"+
            "\uffff\u1f08\u0110\u1040\uffff\u0150\u0110\u0170\uffff\u0080"+
            "\u0110\u0080\uffff\u092e\u0110\u10d2\uffff\u5200\u0110\u5900"+
            "\uffff\u0200\u0110",
            "\1\u0119",
            "\1\u011a",
            "\1\u011b",
            "\1\u011c",
            "\1\u011d",
            "\1\u011e",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\u011f",
            "\1\u0120",
            "\1\u0121",
            "\1\u0122",
            "\1\u0123",
            "\1\u0124",
            "\1\u0125",
            "\1\u0126\12\uffff\1\u0127",
            "\1\u0128",
            "\1\u0129",
            "\1\u012a",
            "\1\u012b",
            "\1\u012c",
            "\1\u012d",
            "\1\u012e\16\uffff\1\u012f",
            "\1\u0130\1\uffff\4\u0130\4\uffff\1\u0130\2\uffff\14\u0130"+
            "\7\uffff\32\u0130\1\uffff\1\u0130\2\uffff\1\u0130\1\uffff\32"+
            "\u0130\3\uffff\1\u0130\101\uffff\27\u0130\1\uffff\37\u0130\1"+
            "\uffff\u1f08\u0130\u1040\uffff\u0150\u0130\u0170\uffff\u0080"+
            "\u0130\u0080\uffff\u092e\u0130\u10d2\uffff\u5200\u0130\u5900"+
            "\uffff\u0200\u0130",
            "\1\u0131",
            "\1\u0132",
            "\1\u0133",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\u0134",
            "\1\u0135",
            "\1\u0127",
            "\1\u0127",
            "\1\u0137\1\uffff\4\u0137\4\uffff\1\u0137\1\uffff\16\u0137"+
            "\6\uffff\32\u0137\1\uffff\1\u0137\2\uffff\1\u0137\1\uffff\32"+
            "\u0137\3\uffff\1\u0137\101\uffff\27\u0137\1\uffff\37\u0137\1"+
            "\uffff\u1f08\u0137\u1040\uffff\u0150\u0137\u0170\uffff\u0080"+
            "\u0137\u0080\uffff\u092e\u0137\u10d2\uffff\u5200\u0137\u5900"+
            "\uffff\u0200\u0137",
            "\1\u0138",
            "\1\u0139",
            "\1\u013a",
            "\1\u013b",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "",
            "\1\u0137\1\uffff\4\u0137\4\uffff\1\u0137\1\uffff\16\u0137"+
            "\6\uffff\32\u0137\1\uffff\1\u0137\2\uffff\1\u0137\1\uffff\32"+
            "\u0137\3\uffff\1\u0137\101\uffff\27\u0137\1\uffff\37\u0137\1"+
            "\uffff\u1f08\u0137\u1040\uffff\u0150\u0137\u0170\uffff\u0080"+
            "\u0137\u0080\uffff\u092e\u0137\u10d2\uffff\u5200\u0137\u5900"+
            "\uffff\u0200\u0137",
            "\1\u013c",
            "\1\u013d",
            "\1\u013e",
            "\1\u013f",
            "\1\u0140",
            "\1\u0141",
            "\1\u0142",
            "\1\u0143",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\u0145",
            "\1\u0146",
            "\1\u0147",
            "",
            "\1\u0148",
            "\1\u0149",
            "\1\u014a",
            "\1\u014b",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\u014c",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            "\1\20\1\uffff\4\20\4\uffff\1\20\1\uffff\16\20\6\uffff\32\20"+
            "\1\uffff\1\20\2\uffff\1\20\1\uffff\32\20\3\uffff\1\20\101\uffff"+
            "\27\20\1\uffff\37\20\1\uffff\u1f08\20\u1040\uffff\u0150\20\u0170"+
            "\uffff\u0080\20\u0080\uffff\u092e\20\u10d2\uffff\u5200\20\u5900"+
            "\uffff\u0200\20",
            ""
    };

    static final short[] DFA15_eot = DFA.unpackEncodedString(DFA15_eotS);
    static final short[] DFA15_eof = DFA.unpackEncodedString(DFA15_eofS);
    static final char[] DFA15_min = DFA.unpackEncodedStringToUnsignedChars(DFA15_minS);
    static final char[] DFA15_max = DFA.unpackEncodedStringToUnsignedChars(DFA15_maxS);
    static final short[] DFA15_accept = DFA.unpackEncodedString(DFA15_acceptS);
    static final short[] DFA15_special = DFA.unpackEncodedString(DFA15_specialS);
    static final short[][] DFA15_transition;

    static {
        int numStates = DFA15_transitionS.length;
        DFA15_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA15_transition[i] = DFA.unpackEncodedString(DFA15_transitionS[i]);
        }
    }

    class DFA15 extends DFA {

        public DFA15(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 15;
            this.eot = DFA15_eot;
            this.eof = DFA15_eof;
            this.min = DFA15_min;
            this.max = DFA15_max;
            this.accept = DFA15_accept;
            this.special = DFA15_special;
            this.transition = DFA15_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__40 | T__41 | T__42 | WS | SERVER | CLIENT | ESA | DSA | VERBOSE | VERSION | SVERION | HELP | X | XINT | XBATCH | XCJNI | XFUTURE | XNOCLSGC | XINCGC | XPROF | XRS | XSHARE | BOOTCP | MEMS | MEMX | SS | LOGGC | SPLASH | JAGENT | EA | DEA | AGENT | JRE_SEARCH | JRE_NO_SEARCH | CP | CPROP | Text );";
        }
    }
 

}