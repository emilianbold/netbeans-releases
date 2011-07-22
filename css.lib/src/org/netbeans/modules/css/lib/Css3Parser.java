// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2011-07-22 16:11:28

    package org.netbeans.modules.css.lib;
    


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.antlr.runtime.debug.*;
import java.io.IOException;
public class Css3Parser extends DebugParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "WS", "CHARSET_SYM", "STRING", "SEMI", "IMPORT_SYM", "URI", "MEDIA_SYM", "LBRACE", "RBRACE", "COMMA", "IDENT", "GEN", "PAGE_SYM", "COLON", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "STAR", "HASH", "DOT", "LBRACKET", "OPEQ", "INCLUDES", "DASHMATCH", "RBRACKET", "LPAREN", "RPAREN", "IMPORTANT_SYM", "NUMBER", "PERCENTAGE", "LENGTH", "EMS", "EXS", "ANGLE", "TIME", "FREQ", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "NAME", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "COMMENT", "CDO", "CDC", "INVALID", "DIMENSION", "NL", "'|'"
    };
    public static final int EOF=-1;
    public static final int T__82=82;
    public static final int WS=4;
    public static final int CHARSET_SYM=5;
    public static final int STRING=6;
    public static final int SEMI=7;
    public static final int IMPORT_SYM=8;
    public static final int URI=9;
    public static final int MEDIA_SYM=10;
    public static final int LBRACE=11;
    public static final int RBRACE=12;
    public static final int COMMA=13;
    public static final int IDENT=14;
    public static final int GEN=15;
    public static final int PAGE_SYM=16;
    public static final int COLON=17;
    public static final int SOLIDUS=18;
    public static final int PLUS=19;
    public static final int GREATER=20;
    public static final int TILDE=21;
    public static final int MINUS=22;
    public static final int STAR=23;
    public static final int HASH=24;
    public static final int DOT=25;
    public static final int LBRACKET=26;
    public static final int OPEQ=27;
    public static final int INCLUDES=28;
    public static final int DASHMATCH=29;
    public static final int RBRACKET=30;
    public static final int LPAREN=31;
    public static final int RPAREN=32;
    public static final int IMPORTANT_SYM=33;
    public static final int NUMBER=34;
    public static final int PERCENTAGE=35;
    public static final int LENGTH=36;
    public static final int EMS=37;
    public static final int EXS=38;
    public static final int ANGLE=39;
    public static final int TIME=40;
    public static final int FREQ=41;
    public static final int HEXCHAR=42;
    public static final int NONASCII=43;
    public static final int UNICODE=44;
    public static final int ESCAPE=45;
    public static final int NMSTART=46;
    public static final int NMCHAR=47;
    public static final int NAME=48;
    public static final int URL=49;
    public static final int A=50;
    public static final int B=51;
    public static final int C=52;
    public static final int D=53;
    public static final int E=54;
    public static final int F=55;
    public static final int G=56;
    public static final int H=57;
    public static final int I=58;
    public static final int J=59;
    public static final int K=60;
    public static final int L=61;
    public static final int M=62;
    public static final int N=63;
    public static final int O=64;
    public static final int P=65;
    public static final int Q=66;
    public static final int R=67;
    public static final int S=68;
    public static final int T=69;
    public static final int U=70;
    public static final int V=71;
    public static final int W=72;
    public static final int X=73;
    public static final int Y=74;
    public static final int Z=75;
    public static final int COMMENT=76;
    public static final int CDO=77;
    public static final int CDC=78;
    public static final int INVALID=79;
    public static final int DIMENSION=80;
    public static final int NL=81;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "typeSelector", "attrib", "cssClass", "charSet", 
        "selector", "term", "syncToIdent", "esPred", "function_name", "bodylist", 
        "pseudo", "pseudoPage", "simpleSelectorSequence", "selectorsGroup", 
        "synpred3_Css3", "declarations", "nsPred", "attribute", "elementName", 
        "synpred2_Css3", "attrname", "attrvalue", "function", "expr", "media", 
        "synpred1_Css3", "operator", "combinator", "ruleSet", "namespaceName", 
        "unaryOperator", "hexColor", "mediaList", "page", "bodyset", "cssId", 
        "imports", "elementSubsequent", "property", "namespacePrefix", "declaration", 
        "styleSheet", "medium", "prio"
    };
    public static final boolean[] decisionCanBacktrack = new boolean[] {
        false, // invalid decision
        false, false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, true, 
            true, false, true, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false
    };

     
        public int ruleLevel = 0;
        public int getRuleLevel() { return ruleLevel; }
        public void incRuleLevel() { ruleLevel++; }
        public void decRuleLevel() { ruleLevel--; }
        public Css3Parser(TokenStream input) {
            this(input, DebugEventSocketProxy.DEFAULT_DEBUGGER_PORT, new RecognizerSharedState());
        }
        public Css3Parser(TokenStream input, int port, RecognizerSharedState state) {
            super(input, state);
            DebugEventSocketProxy proxy =
                new DebugEventSocketProxy(this, port, null);
            setDebugListener(proxy);
            try {
                proxy.handshake();
            }
            catch (IOException ioe) {
                reportError(ioe);
            }
        }
    public Css3Parser(TokenStream input, DebugEventListener dbg) {
        super(input, dbg, new RecognizerSharedState());

    }
    protected boolean evalPredicate(boolean result, String predicate) {
        dbg.semanticPredicate(result, predicate);
        return result;
    }


    public String[] getTokenNames() { return Css3Parser.tokenNames; }
    public String getGrammarFileName() { return "/Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g"; }


    /**
         * Use the current stacked followset to work out the valid tokens that
         * can follow on from the current point in the parse, then recover by
         * eating tokens that are not a member of the follow set we compute.
         *
         * This method is used whenever we wish to force a sync, even though
         * the parser has not yet checked LA(1) for alt selection. This is useful
         * in situations where only a subset of tokens can begin a new construct
         * (such as the start of a new statement in a block) and we want to
         * proactively detect garbage so that the current rule does not exit on
         * on an exception.
         *
         * We could override recover() to make this the default behavior but that
         * is too much like using a sledge hammer to crack a nut. We want finer
         * grained control of the recovery and error mechanisms.
         */
        protected void syncToSet()
        {
            // Compute the followset that is in context wherever we are in the
            // rule chain/stack
            //
             BitSet follow = state.following[state._fsp]; //computeContextSensitiveRuleFOLLOW();

             syncToSet(follow);
        }

        protected void syncToSet(BitSet follow)
        {
            int mark = -1;

            //create error-recovery node
            dbg.enterRule(getGrammarFileName(), "recovery");

            try {

                mark = input.mark();

                // Consume all tokens in the stream until we find a member of the follow
                // set, which means the next production should be guaranteed to be happy.
                //
                while (! follow.member(input.LA(1)) ) {

                    if  (input.LA(1) == Token.EOF) {

                        // Looks like we didn't find anything at all that can help us here
                        // so we need to rewind to where we were and let normal error handling
                        // bail out.
                        //
                        input.rewind();
                        mark = -1;
                        return;
                    }
                    input.consume();

                    // Now here, because you are consuming some tokens, yu will probably want
                    // to raise an error message such as "Spurious elements after the class member were discarded"
                    // using whatever your override of displayRecognitionError() routine does to record
                    // error messages. The exact error my depend on context etc.
                    //
                }
            } catch (Exception e) {

              // Just ignore any errors here, we will just let the recognizer
              // try to resync as normal - something must be very screwed.
              //
            }
            finally {
                dbg.exitRule(getGrammarFileName(), "recovery");

                // Always release the mark we took
                //
                if  (mark != -1) {
                    input.release(mark);
                }
            }
        }
        
        



    // $ANTLR start "styleSheet"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:131:1: styleSheet : ( charSet )? ( WS )* ( imports ( WS )* )* bodylist EOF ;
    public final void styleSheet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "styleSheet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(131, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:132:5: ( ( charSet )? ( WS )* ( imports ( WS )* )* bodylist EOF )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:132:9: ( charSet )? ( WS )* ( imports ( WS )* )* bodylist EOF
            {
            dbg.location(132,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:132:9: ( charSet )?
            int alt1=2;
            try { dbg.enterSubRule(1);
            try { dbg.enterDecision(1, decisionCanBacktrack[1]);

            int LA1_0 = input.LA(1);

            if ( (LA1_0==CHARSET_SYM) ) {
                alt1=1;
            }
            } finally {dbg.exitDecision(1);}

            switch (alt1) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:132:9: charSet
                    {
                    dbg.location(132,9);
                    pushFollow(FOLLOW_charSet_in_styleSheet77);
                    charSet();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(1);}

            dbg.location(133,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:133:6: ( WS )*
            try { dbg.enterSubRule(2);

            loop2:
            do {
                int alt2=2;
                try { dbg.enterDecision(2, decisionCanBacktrack[2]);

                int LA2_0 = input.LA(1);

                if ( (LA2_0==WS) ) {
                    alt2=1;
                }


                } finally {dbg.exitDecision(2);}

                switch (alt2) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:133:6: WS
            	    {
            	    dbg.location(133,6);
            	    match(input,WS,FOLLOW_WS_in_styleSheet85); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);
            } finally {dbg.exitSubRule(2);}

            dbg.location(134,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:134:9: ( imports ( WS )* )*
            try { dbg.enterSubRule(4);

            loop4:
            do {
                int alt4=2;
                try { dbg.enterDecision(4, decisionCanBacktrack[4]);

                int LA4_0 = input.LA(1);

                if ( (LA4_0==IMPORT_SYM) ) {
                    alt4=1;
                }


                } finally {dbg.exitDecision(4);}

                switch (alt4) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:134:10: imports ( WS )*
            	    {
            	    dbg.location(134,10);
            	    pushFollow(FOLLOW_imports_in_styleSheet97);
            	    imports();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(134,18);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:134:18: ( WS )*
            	    try { dbg.enterSubRule(3);

            	    loop3:
            	    do {
            	        int alt3=2;
            	        try { dbg.enterDecision(3, decisionCanBacktrack[3]);

            	        int LA3_0 = input.LA(1);

            	        if ( (LA3_0==WS) ) {
            	            alt3=1;
            	        }


            	        } finally {dbg.exitDecision(3);}

            	        switch (alt3) {
            	    	case 1 :
            	    	    dbg.enterAlt(1);

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:134:18: WS
            	    	    {
            	    	    dbg.location(134,18);
            	    	    match(input,WS,FOLLOW_WS_in_styleSheet99); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop3;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(3);}


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);
            } finally {dbg.exitSubRule(4);}

            dbg.location(135,9);
            pushFollow(FOLLOW_bodylist_in_styleSheet114);
            bodylist();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(136,6);
            match(input,EOF,FOLLOW_EOF_in_styleSheet121); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(137, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "styleSheet");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "styleSheet"


    // $ANTLR start "charSet"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:142:1: charSet : CHARSET_SYM WS STRING ( WS )? SEMI ;
    public final void charSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "charSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(142, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:143:5: ( CHARSET_SYM WS STRING ( WS )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:143:9: CHARSET_SYM WS STRING ( WS )? SEMI
            {
            dbg.location(143,9);
            match(input,CHARSET_SYM,FOLLOW_CHARSET_SYM_in_charSet147); if (state.failed) return ;
            dbg.location(143,21);
            match(input,WS,FOLLOW_WS_in_charSet149); if (state.failed) return ;
            dbg.location(143,24);
            match(input,STRING,FOLLOW_STRING_in_charSet151); if (state.failed) return ;
            dbg.location(143,31);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:143:31: ( WS )?
            int alt5=2;
            try { dbg.enterSubRule(5);
            try { dbg.enterDecision(5, decisionCanBacktrack[5]);

            int LA5_0 = input.LA(1);

            if ( (LA5_0==WS) ) {
                alt5=1;
            }
            } finally {dbg.exitDecision(5);}

            switch (alt5) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:143:31: WS
                    {
                    dbg.location(143,31);
                    match(input,WS,FOLLOW_WS_in_charSet153); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(5);}

            dbg.location(143,35);
            match(input,SEMI,FOLLOW_SEMI_in_charSet156); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(144, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "charSet");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "charSet"


    // $ANTLR start "imports"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:149:1: imports : IMPORT_SYM WS ( STRING | URI ) ( WS )* ( mediaList )? SEMI ;
    public final void imports() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "imports");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(149, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:150:5: ( IMPORT_SYM WS ( STRING | URI ) ( WS )* ( mediaList )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:150:9: IMPORT_SYM WS ( STRING | URI ) ( WS )* ( mediaList )? SEMI
            {
            dbg.location(150,9);
            match(input,IMPORT_SYM,FOLLOW_IMPORT_SYM_in_imports178); if (state.failed) return ;
            dbg.location(150,20);
            match(input,WS,FOLLOW_WS_in_imports180); if (state.failed) return ;
            dbg.location(150,23);
            if ( input.LA(1)==STRING||input.LA(1)==URI ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }

            dbg.location(150,36);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:150:36: ( WS )*
            try { dbg.enterSubRule(6);

            loop6:
            do {
                int alt6=2;
                try { dbg.enterDecision(6, decisionCanBacktrack[6]);

                int LA6_0 = input.LA(1);

                if ( (LA6_0==WS) ) {
                    alt6=1;
                }


                } finally {dbg.exitDecision(6);}

                switch (alt6) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:150:36: WS
            	    {
            	    dbg.location(150,36);
            	    match(input,WS,FOLLOW_WS_in_imports188); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);
            } finally {dbg.exitSubRule(6);}

            dbg.location(150,40);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:150:40: ( mediaList )?
            int alt7=2;
            try { dbg.enterSubRule(7);
            try { dbg.enterDecision(7, decisionCanBacktrack[7]);

            int LA7_0 = input.LA(1);

            if ( ((LA7_0>=IDENT && LA7_0<=GEN)) ) {
                alt7=1;
            }
            } finally {dbg.exitDecision(7);}

            switch (alt7) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:150:40: mediaList
                    {
                    dbg.location(150,40);
                    pushFollow(FOLLOW_mediaList_in_imports191);
                    mediaList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(7);}

            dbg.location(150,51);
            match(input,SEMI,FOLLOW_SEMI_in_imports194); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(151, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "imports");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "imports"


    // $ANTLR start "media"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:157:1: media : MEDIA_SYM ( WS )* mediaList LBRACE ( WS )* ruleSet RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(157, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:158:5: ( MEDIA_SYM ( WS )* mediaList LBRACE ( WS )* ruleSet RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:158:7: MEDIA_SYM ( WS )* mediaList LBRACE ( WS )* ruleSet RBRACE
            {
            dbg.location(158,7);
            match(input,MEDIA_SYM,FOLLOW_MEDIA_SYM_in_media215); if (state.failed) return ;
            dbg.location(158,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:158:17: ( WS )*
            try { dbg.enterSubRule(8);

            loop8:
            do {
                int alt8=2;
                try { dbg.enterDecision(8, decisionCanBacktrack[8]);

                int LA8_0 = input.LA(1);

                if ( (LA8_0==WS) ) {
                    alt8=1;
                }


                } finally {dbg.exitDecision(8);}

                switch (alt8) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:158:17: WS
            	    {
            	    dbg.location(158,17);
            	    match(input,WS,FOLLOW_WS_in_media217); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);
            } finally {dbg.exitSubRule(8);}

            dbg.location(158,21);
            pushFollow(FOLLOW_mediaList_in_media220);
            mediaList();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(159,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_media230); if (state.failed) return ;
            dbg.location(159,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:159:16: ( WS )*
            try { dbg.enterSubRule(9);

            loop9:
            do {
                int alt9=2;
                try { dbg.enterDecision(9, decisionCanBacktrack[9]);

                int LA9_0 = input.LA(1);

                if ( (LA9_0==WS) ) {
                    alt9=1;
                }


                } finally {dbg.exitDecision(9);}

                switch (alt9) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:159:16: WS
            	    {
            	    dbg.location(159,16);
            	    match(input,WS,FOLLOW_WS_in_media232); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);
            } finally {dbg.exitSubRule(9);}

            dbg.location(160,13);
            pushFollow(FOLLOW_ruleSet_in_media247);
            ruleSet();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(161,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_media257); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(162, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "media");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "media"


    // $ANTLR start "mediaList"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:164:1: mediaList : medium ( COMMA ( WS )* medium )* ;
    public final void mediaList() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaList");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(164, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:165:9: ( medium ( COMMA ( WS )* medium )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:165:11: medium ( COMMA ( WS )* medium )*
            {
            dbg.location(165,11);
            pushFollow(FOLLOW_medium_in_mediaList278);
            medium();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(165,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:165:18: ( COMMA ( WS )* medium )*
            try { dbg.enterSubRule(11);

            loop11:
            do {
                int alt11=2;
                try { dbg.enterDecision(11, decisionCanBacktrack[11]);

                int LA11_0 = input.LA(1);

                if ( (LA11_0==COMMA) ) {
                    alt11=1;
                }


                } finally {dbg.exitDecision(11);}

                switch (alt11) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:165:19: COMMA ( WS )* medium
            	    {
            	    dbg.location(165,19);
            	    match(input,COMMA,FOLLOW_COMMA_in_mediaList281); if (state.failed) return ;
            	    dbg.location(165,25);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:165:25: ( WS )*
            	    try { dbg.enterSubRule(10);

            	    loop10:
            	    do {
            	        int alt10=2;
            	        try { dbg.enterDecision(10, decisionCanBacktrack[10]);

            	        int LA10_0 = input.LA(1);

            	        if ( (LA10_0==WS) ) {
            	            alt10=1;
            	        }


            	        } finally {dbg.exitDecision(10);}

            	        switch (alt10) {
            	    	case 1 :
            	    	    dbg.enterAlt(1);

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:165:25: WS
            	    	    {
            	    	    dbg.location(165,25);
            	    	    match(input,WS,FOLLOW_WS_in_mediaList283); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop10;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(10);}

            	    dbg.location(165,29);
            	    pushFollow(FOLLOW_medium_in_mediaList286);
            	    medium();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);
            } finally {dbg.exitSubRule(11);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(166, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "mediaList");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "mediaList"


    // $ANTLR start "medium"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:171:1: medium : ( IDENT | GEN ) ( WS )* ;
    public final void medium() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "medium");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(171, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:172:5: ( ( IDENT | GEN ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:172:7: ( IDENT | GEN ) ( WS )*
            {
            dbg.location(172,7);
            if ( (input.LA(1)>=IDENT && input.LA(1)<=GEN) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }

            dbg.location(172,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:172:23: ( WS )*
            try { dbg.enterSubRule(12);

            loop12:
            do {
                int alt12=2;
                try { dbg.enterDecision(12, decisionCanBacktrack[12]);

                int LA12_0 = input.LA(1);

                if ( (LA12_0==WS) ) {
                    alt12=1;
                }


                } finally {dbg.exitDecision(12);}

                switch (alt12) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:172:23: WS
            	    {
            	    dbg.location(172,23);
            	    match(input,WS,FOLLOW_WS_in_medium315); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop12;
                }
            } while (true);
            } finally {dbg.exitSubRule(12);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(173, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "medium");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "medium"


    // $ANTLR start "bodylist"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:176:1: bodylist : ( bodyset )* ;
    public final void bodylist() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodylist");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(176, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:177:5: ( ( bodyset )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:177:7: ( bodyset )*
            {
            dbg.location(177,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:177:7: ( bodyset )*
            try { dbg.enterSubRule(13);

            loop13:
            do {
                int alt13=2;
                try { dbg.enterDecision(13, decisionCanBacktrack[13]);

                int LA13_0 = input.LA(1);

                if ( (LA13_0==MEDIA_SYM||LA13_0==IDENT||(LA13_0>=PAGE_SYM && LA13_0<=COLON)||(LA13_0>=STAR && LA13_0<=LBRACKET)||LA13_0==82) ) {
                    alt13=1;
                }


                } finally {dbg.exitDecision(13);}

                switch (alt13) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:177:7: bodyset
            	    {
            	    dbg.location(177,7);
            	    pushFollow(FOLLOW_bodyset_in_bodylist338);
            	    bodyset();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);
            } finally {dbg.exitSubRule(13);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(178, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "bodylist");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "bodylist"


    // $ANTLR start "bodyset"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:180:1: bodyset : ( ruleSet | media | page ) ( WS )* ;
    public final void bodyset() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyset");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(180, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:181:5: ( ( ruleSet | media | page ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:181:7: ( ruleSet | media | page ) ( WS )*
            {
            dbg.location(181,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:181:7: ( ruleSet | media | page )
            int alt14=3;
            try { dbg.enterSubRule(14);
            try { dbg.enterDecision(14, decisionCanBacktrack[14]);

            switch ( input.LA(1) ) {
            case IDENT:
            case COLON:
            case STAR:
            case HASH:
            case DOT:
            case LBRACKET:
            case 82:
                {
                alt14=1;
                }
                break;
            case MEDIA_SYM:
                {
                alt14=2;
                }
                break;
            case PAGE_SYM:
                {
                alt14=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(14);}

            switch (alt14) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:182:6: ruleSet
                    {
                    dbg.location(182,6);
                    pushFollow(FOLLOW_ruleSet_in_bodyset367);
                    ruleSet();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:11: media
                    {
                    dbg.location(183,11);
                    pushFollow(FOLLOW_media_in_bodyset379);
                    media();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:184:11: page
                    {
                    dbg.location(184,11);
                    pushFollow(FOLLOW_page_in_bodyset391);
                    page();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(14);}

            dbg.location(186,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:186:7: ( WS )*
            try { dbg.enterSubRule(15);

            loop15:
            do {
                int alt15=2;
                try { dbg.enterDecision(15, decisionCanBacktrack[15]);

                int LA15_0 = input.LA(1);

                if ( (LA15_0==WS) ) {
                    alt15=1;
                }


                } finally {dbg.exitDecision(15);}

                switch (alt15) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:186:7: WS
            	    {
            	    dbg.location(186,7);
            	    match(input,WS,FOLLOW_WS_in_bodyset407); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);
            } finally {dbg.exitSubRule(15);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(187, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "bodyset");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "bodyset"


    // $ANTLR start "page"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:189:1: page : PAGE_SYM ( WS )? ( pseudoPage ( WS )* )? LBRACE ( WS )* declaration SEMI ( declaration SEMI )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(189, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:190:5: ( PAGE_SYM ( WS )? ( pseudoPage ( WS )* )? LBRACE ( WS )* declaration SEMI ( declaration SEMI )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:190:7: PAGE_SYM ( WS )? ( pseudoPage ( WS )* )? LBRACE ( WS )* declaration SEMI ( declaration SEMI )* RBRACE
            {
            dbg.location(190,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page432); if (state.failed) return ;
            dbg.location(190,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:190:16: ( WS )?
            int alt16=2;
            try { dbg.enterSubRule(16);
            try { dbg.enterDecision(16, decisionCanBacktrack[16]);

            int LA16_0 = input.LA(1);

            if ( (LA16_0==WS) ) {
                alt16=1;
            }
            } finally {dbg.exitDecision(16);}

            switch (alt16) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:190:16: WS
                    {
                    dbg.location(190,16);
                    match(input,WS,FOLLOW_WS_in_page434); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(16);}

            dbg.location(190,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:190:20: ( pseudoPage ( WS )* )?
            int alt18=2;
            try { dbg.enterSubRule(18);
            try { dbg.enterDecision(18, decisionCanBacktrack[18]);

            int LA18_0 = input.LA(1);

            if ( (LA18_0==COLON) ) {
                alt18=1;
            }
            } finally {dbg.exitDecision(18);}

            switch (alt18) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:190:21: pseudoPage ( WS )*
                    {
                    dbg.location(190,21);
                    pushFollow(FOLLOW_pseudoPage_in_page438);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(190,32);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:190:32: ( WS )*
                    try { dbg.enterSubRule(17);

                    loop17:
                    do {
                        int alt17=2;
                        try { dbg.enterDecision(17, decisionCanBacktrack[17]);

                        int LA17_0 = input.LA(1);

                        if ( (LA17_0==WS) ) {
                            alt17=1;
                        }


                        } finally {dbg.exitDecision(17);}

                        switch (alt17) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:190:32: WS
                    	    {
                    	    dbg.location(190,32);
                    	    match(input,WS,FOLLOW_WS_in_page440); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop17;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(17);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(18);}

            dbg.location(191,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page453); if (state.failed) return ;
            dbg.location(191,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:191:16: ( WS )*
            try { dbg.enterSubRule(19);

            loop19:
            do {
                int alt19=2;
                try { dbg.enterDecision(19, decisionCanBacktrack[19]);

                int LA19_0 = input.LA(1);

                if ( (LA19_0==WS) ) {
                    alt19=1;
                }


                } finally {dbg.exitDecision(19);}

                switch (alt19) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:191:16: WS
            	    {
            	    dbg.location(191,16);
            	    match(input,WS,FOLLOW_WS_in_page455); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop19;
                }
            } while (true);
            } finally {dbg.exitSubRule(19);}

            dbg.location(192,13);
            pushFollow(FOLLOW_declaration_in_page470);
            declaration();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(192,25);
            match(input,SEMI,FOLLOW_SEMI_in_page472); if (state.failed) return ;
            dbg.location(192,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:192:30: ( declaration SEMI )*
            try { dbg.enterSubRule(20);

            loop20:
            do {
                int alt20=2;
                try { dbg.enterDecision(20, decisionCanBacktrack[20]);

                int LA20_0 = input.LA(1);

                if ( ((LA20_0>=IDENT && LA20_0<=GEN)) ) {
                    alt20=1;
                }


                } finally {dbg.exitDecision(20);}

                switch (alt20) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:192:31: declaration SEMI
            	    {
            	    dbg.location(192,31);
            	    pushFollow(FOLLOW_declaration_in_page475);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(192,43);
            	    match(input,SEMI,FOLLOW_SEMI_in_page477); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);
            } finally {dbg.exitSubRule(20);}

            dbg.location(193,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page489); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(194, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "page");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "page"


    // $ANTLR start "pseudoPage"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:196:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(196, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:197:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:197:7: COLON IDENT
            {
            dbg.location(197,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage510); if (state.failed) return ;
            dbg.location(197,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage512); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(198, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "pseudoPage");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "pseudoPage"


    // $ANTLR start "operator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:200:1: operator : ( SOLIDUS ( WS )* | COMMA ( WS )* | );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(200, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:201:5: ( SOLIDUS ( WS )* | COMMA ( WS )* | )
            int alt23=3;
            try { dbg.enterDecision(23, decisionCanBacktrack[23]);

            switch ( input.LA(1) ) {
            case SOLIDUS:
                {
                alt23=1;
                }
                break;
            case COMMA:
                {
                alt23=2;
                }
                break;
            case STRING:
            case URI:
            case IDENT:
            case PLUS:
            case MINUS:
            case HASH:
            case NUMBER:
            case PERCENTAGE:
            case LENGTH:
            case EMS:
            case EXS:
            case ANGLE:
            case TIME:
            case FREQ:
                {
                alt23=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 23, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(23);}

            switch (alt23) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:201:7: SOLIDUS ( WS )*
                    {
                    dbg.location(201,7);
                    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_operator533); if (state.failed) return ;
                    dbg.location(201,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:201:15: ( WS )*
                    try { dbg.enterSubRule(21);

                    loop21:
                    do {
                        int alt21=2;
                        try { dbg.enterDecision(21, decisionCanBacktrack[21]);

                        int LA21_0 = input.LA(1);

                        if ( (LA21_0==WS) ) {
                            alt21=1;
                        }


                        } finally {dbg.exitDecision(21);}

                        switch (alt21) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:201:15: WS
                    	    {
                    	    dbg.location(201,15);
                    	    match(input,WS,FOLLOW_WS_in_operator535); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop21;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(21);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:202:7: COMMA ( WS )*
                    {
                    dbg.location(202,7);
                    match(input,COMMA,FOLLOW_COMMA_in_operator544); if (state.failed) return ;
                    dbg.location(202,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:202:13: ( WS )*
                    try { dbg.enterSubRule(22);

                    loop22:
                    do {
                        int alt22=2;
                        try { dbg.enterDecision(22, decisionCanBacktrack[22]);

                        int LA22_0 = input.LA(1);

                        if ( (LA22_0==WS) ) {
                            alt22=1;
                        }


                        } finally {dbg.exitDecision(22);}

                        switch (alt22) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:202:13: WS
                    	    {
                    	    dbg.location(202,13);
                    	    match(input,WS,FOLLOW_WS_in_operator546); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop22;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(22);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:204:5: 
                    {
                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(204, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "operator");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "operator"


    // $ANTLR start "combinator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:206:1: combinator : ( PLUS ( WS )* | GREATER ( WS )* | TILDE ( WS )* | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(206, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:207:5: ( PLUS ( WS )* | GREATER ( WS )* | TILDE ( WS )* | )
            int alt27=4;
            try { dbg.enterDecision(27, decisionCanBacktrack[27]);

            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt27=1;
                }
                break;
            case GREATER:
                {
                alt27=2;
                }
                break;
            case TILDE:
                {
                alt27=3;
                }
                break;
            case IDENT:
            case COLON:
            case STAR:
            case HASH:
            case DOT:
            case LBRACKET:
            case 82:
                {
                alt27=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 27, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(27);}

            switch (alt27) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:207:7: PLUS ( WS )*
                    {
                    dbg.location(207,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator574); if (state.failed) return ;
                    dbg.location(207,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:207:12: ( WS )*
                    try { dbg.enterSubRule(24);

                    loop24:
                    do {
                        int alt24=2;
                        try { dbg.enterDecision(24, decisionCanBacktrack[24]);

                        int LA24_0 = input.LA(1);

                        if ( (LA24_0==WS) ) {
                            alt24=1;
                        }


                        } finally {dbg.exitDecision(24);}

                        switch (alt24) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:207:12: WS
                    	    {
                    	    dbg.location(207,12);
                    	    match(input,WS,FOLLOW_WS_in_combinator576); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop24;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(24);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:208:7: GREATER ( WS )*
                    {
                    dbg.location(208,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator585); if (state.failed) return ;
                    dbg.location(208,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:208:15: ( WS )*
                    try { dbg.enterSubRule(25);

                    loop25:
                    do {
                        int alt25=2;
                        try { dbg.enterDecision(25, decisionCanBacktrack[25]);

                        int LA25_0 = input.LA(1);

                        if ( (LA25_0==WS) ) {
                            alt25=1;
                        }


                        } finally {dbg.exitDecision(25);}

                        switch (alt25) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:208:15: WS
                    	    {
                    	    dbg.location(208,15);
                    	    match(input,WS,FOLLOW_WS_in_combinator587); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop25;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(25);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:209:7: TILDE ( WS )*
                    {
                    dbg.location(209,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator596); if (state.failed) return ;
                    dbg.location(209,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:209:13: ( WS )*
                    try { dbg.enterSubRule(26);

                    loop26:
                    do {
                        int alt26=2;
                        try { dbg.enterDecision(26, decisionCanBacktrack[26]);

                        int LA26_0 = input.LA(1);

                        if ( (LA26_0==WS) ) {
                            alt26=1;
                        }


                        } finally {dbg.exitDecision(26);}

                        switch (alt26) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:209:13: WS
                    	    {
                    	    dbg.location(209,13);
                    	    match(input,WS,FOLLOW_WS_in_combinator598); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop26;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(26);}


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:211:5: 
                    {
                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(211, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "combinator");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "combinator"


    // $ANTLR start "unaryOperator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:213:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(213, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:214:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(214,5);
            if ( input.LA(1)==PLUS||input.LA(1)==MINUS ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(216, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "unaryOperator");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "unaryOperator"


    // $ANTLR start "property"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:218:1: property : ( IDENT | GEN ) ( WS )* ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(218, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:219:5: ( ( IDENT | GEN ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:219:7: ( IDENT | GEN ) ( WS )*
            {
            dbg.location(219,7);
            if ( (input.LA(1)>=IDENT && input.LA(1)<=GEN) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }

            dbg.location(219,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:219:21: ( WS )*
            try { dbg.enterSubRule(28);

            loop28:
            do {
                int alt28=2;
                try { dbg.enterDecision(28, decisionCanBacktrack[28]);

                int LA28_0 = input.LA(1);

                if ( (LA28_0==WS) ) {
                    alt28=1;
                }


                } finally {dbg.exitDecision(28);}

                switch (alt28) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:219:21: WS
            	    {
            	    dbg.location(219,21);
            	    match(input,WS,FOLLOW_WS_in_property666); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop28;
                }
            } while (true);
            } finally {dbg.exitSubRule(28);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(220, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "property");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "property"


    // $ANTLR start "ruleSet"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:222:1: ruleSet : selectorsGroup LBRACE ( WS )* syncToIdent declarations RBRACE ;
    public final void ruleSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ruleSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(222, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:223:5: ( selectorsGroup LBRACE ( WS )* syncToIdent declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:223:9: selectorsGroup LBRACE ( WS )* syncToIdent declarations RBRACE
            {
            dbg.location(223,9);
            pushFollow(FOLLOW_selectorsGroup_in_ruleSet691);
            selectorsGroup();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(224,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_ruleSet701); if (state.failed) return ;
            dbg.location(224,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:224:16: ( WS )*
            try { dbg.enterSubRule(29);

            loop29:
            do {
                int alt29=2;
                try { dbg.enterDecision(29, decisionCanBacktrack[29]);

                int LA29_0 = input.LA(1);

                if ( (LA29_0==WS) ) {
                    alt29=1;
                }


                } finally {dbg.exitDecision(29);}

                switch (alt29) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:224:16: WS
            	    {
            	    dbg.location(224,16);
            	    match(input,WS,FOLLOW_WS_in_ruleSet703); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop29;
                }
            } while (true);
            } finally {dbg.exitSubRule(29);}

            dbg.location(224,20);
            pushFollow(FOLLOW_syncToIdent_in_ruleSet706);
            syncToIdent();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(225,13);
            pushFollow(FOLLOW_declarations_in_ruleSet720);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(226,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_ruleSet730); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(227, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "ruleSet");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "ruleSet"


    // $ANTLR start "declarations"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:229:1: declarations : ( declaration )? ( SEMI ( WS )* ( declaration )? )* ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(229, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:230:5: ( ( declaration )? ( SEMI ( WS )* ( declaration )? )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:232:9: ( declaration )? ( SEMI ( WS )* ( declaration )? )*
            {
            dbg.location(232,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:232:9: ( declaration )?
            int alt30=2;
            try { dbg.enterSubRule(30);
            try { dbg.enterDecision(30, decisionCanBacktrack[30]);

            int LA30_0 = input.LA(1);

            if ( ((LA30_0>=IDENT && LA30_0<=GEN)) ) {
                alt30=1;
            }
            } finally {dbg.exitDecision(30);}

            switch (alt30) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:232:9: declaration
                    {
                    dbg.location(232,9);
                    pushFollow(FOLLOW_declaration_in_declarations768);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(30);}

            dbg.location(232,22);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:232:22: ( SEMI ( WS )* ( declaration )? )*
            try { dbg.enterSubRule(33);

            loop33:
            do {
                int alt33=2;
                try { dbg.enterDecision(33, decisionCanBacktrack[33]);

                int LA33_0 = input.LA(1);

                if ( (LA33_0==SEMI) ) {
                    alt33=1;
                }


                } finally {dbg.exitDecision(33);}

                switch (alt33) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:232:23: SEMI ( WS )* ( declaration )?
            	    {
            	    dbg.location(232,23);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations772); if (state.failed) return ;
            	    dbg.location(232,28);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:232:28: ( WS )*
            	    try { dbg.enterSubRule(31);

            	    loop31:
            	    do {
            	        int alt31=2;
            	        try { dbg.enterDecision(31, decisionCanBacktrack[31]);

            	        int LA31_0 = input.LA(1);

            	        if ( (LA31_0==WS) ) {
            	            alt31=1;
            	        }


            	        } finally {dbg.exitDecision(31);}

            	        switch (alt31) {
            	    	case 1 :
            	    	    dbg.enterAlt(1);

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:232:28: WS
            	    	    {
            	    	    dbg.location(232,28);
            	    	    match(input,WS,FOLLOW_WS_in_declarations774); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop31;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(31);}

            	    dbg.location(232,32);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:232:32: ( declaration )?
            	    int alt32=2;
            	    try { dbg.enterSubRule(32);
            	    try { dbg.enterDecision(32, decisionCanBacktrack[32]);

            	    int LA32_0 = input.LA(1);

            	    if ( ((LA32_0>=IDENT && LA32_0<=GEN)) ) {
            	        alt32=1;
            	    }
            	    } finally {dbg.exitDecision(32);}

            	    switch (alt32) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:232:32: declaration
            	            {
            	            dbg.location(232,32);
            	            pushFollow(FOLLOW_declaration_in_declarations777);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(32);}


            	    }
            	    break;

            	default :
            	    break loop33;
                }
            } while (true);
            } finally {dbg.exitSubRule(33);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(233, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "declarations");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "declarations"


    // $ANTLR start "selectorsGroup"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:235:1: selectorsGroup : selector ( COMMA ( WS )* selector )* ;
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(235, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:236:5: ( selector ( COMMA ( WS )* selector )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:236:7: selector ( COMMA ( WS )* selector )*
            {
            dbg.location(236,7);
            pushFollow(FOLLOW_selector_in_selectorsGroup801);
            selector();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(236,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:236:16: ( COMMA ( WS )* selector )*
            try { dbg.enterSubRule(35);

            loop35:
            do {
                int alt35=2;
                try { dbg.enterDecision(35, decisionCanBacktrack[35]);

                int LA35_0 = input.LA(1);

                if ( (LA35_0==COMMA) ) {
                    alt35=1;
                }


                } finally {dbg.exitDecision(35);}

                switch (alt35) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:236:17: COMMA ( WS )* selector
            	    {
            	    dbg.location(236,17);
            	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup804); if (state.failed) return ;
            	    dbg.location(236,23);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:236:23: ( WS )*
            	    try { dbg.enterSubRule(34);

            	    loop34:
            	    do {
            	        int alt34=2;
            	        try { dbg.enterDecision(34, decisionCanBacktrack[34]);

            	        int LA34_0 = input.LA(1);

            	        if ( (LA34_0==WS) ) {
            	            alt34=1;
            	        }


            	        } finally {dbg.exitDecision(34);}

            	        switch (alt34) {
            	    	case 1 :
            	    	    dbg.enterAlt(1);

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:236:23: WS
            	    	    {
            	    	    dbg.location(236,23);
            	    	    match(input,WS,FOLLOW_WS_in_selectorsGroup806); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop34;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(34);}

            	    dbg.location(236,27);
            	    pushFollow(FOLLOW_selector_in_selectorsGroup809);
            	    selector();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop35;
                }
            } while (true);
            } finally {dbg.exitSubRule(35);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(237, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "selectorsGroup");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "selectorsGroup"


    // $ANTLR start "selector"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:239:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(239, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:240:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:240:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(240,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector832);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(240,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:240:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(36);

            loop36:
            do {
                int alt36=2;
                try { dbg.enterDecision(36, decisionCanBacktrack[36]);

                int LA36_0 = input.LA(1);

                if ( (LA36_0==IDENT||LA36_0==COLON||(LA36_0>=PLUS && LA36_0<=TILDE)||(LA36_0>=STAR && LA36_0<=LBRACKET)||LA36_0==82) ) {
                    alt36=1;
                }


                } finally {dbg.exitDecision(36);}

                switch (alt36) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:240:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(240,31);
            	    pushFollow(FOLLOW_combinator_in_selector835);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(240,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector837);
            	    simpleSelectorSequence();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop36;
                }
            } while (true);
            } finally {dbg.exitSubRule(36);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(241, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "selector");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "selector"


    // $ANTLR start "simpleSelectorSequence"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:244:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(244, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:246:2: ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) )
            int alt39=2;
            try { dbg.enterDecision(39, decisionCanBacktrack[39]);

            int LA39_0 = input.LA(1);

            if ( (LA39_0==IDENT||LA39_0==STAR||LA39_0==82) ) {
                alt39=1;
            }
            else if ( (LA39_0==COLON||(LA39_0>=HASH && LA39_0<=LBRACKET)) ) {
                alt39=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(39);}

            switch (alt39) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:250:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    {
                    dbg.location(250,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:250:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:250:5: typeSelector ( ( esPred )=> elementSubsequent )*
                    {
                    dbg.location(250,5);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence877);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(250,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:250:18: ( ( esPred )=> elementSubsequent )*
                    try { dbg.enterSubRule(37);

                    loop37:
                    do {
                        int alt37=2;
                        try { dbg.enterDecision(37, decisionCanBacktrack[37]);

                        try {
                            isCyclicDecision = true;
                            alt37 = dfa37.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(37);}

                        switch (alt37) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:250:19: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(250,29);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence884);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop37;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(37);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:252:2: ( ( ( esPred )=> elementSubsequent )+ )
                    {
                    dbg.location(252,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:252:2: ( ( ( esPred )=> elementSubsequent )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:252:4: ( ( esPred )=> elementSubsequent )+
                    {
                    dbg.location(252,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:252:4: ( ( esPred )=> elementSubsequent )+
                    int cnt38=0;
                    try { dbg.enterSubRule(38);

                    loop38:
                    do {
                        int alt38=2;
                        try { dbg.enterDecision(38, decisionCanBacktrack[38]);

                        switch ( input.LA(1) ) {
                        case HASH:
                            {
                            int LA38_2 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt38=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA38_3 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt38=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA38_4 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt38=1;
                            }


                            }
                            break;
                        case COLON:
                            {
                            int LA38_5 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt38=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(38);}

                        switch (alt38) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:252:5: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(252,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence902);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt38 >= 1 ) break loop38;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(38, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt38++;
                    } while (true);
                    } finally {dbg.exitSubRule(38);}


                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(253, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "simpleSelectorSequence");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "simpleSelectorSequence"


    // $ANTLR start "typeSelector"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:263:1: typeSelector options {k=2; } : ( ( nsPred )=> namespacePrefix )? ( elementName ( WS )* ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(263, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:3: ( ( ( nsPred )=> namespacePrefix )? ( elementName ( WS )* ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:6: ( ( nsPred )=> namespacePrefix )? ( elementName ( WS )* )
            {
            dbg.location(265,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:6: ( ( nsPred )=> namespacePrefix )?
            int alt40=2;
            try { dbg.enterSubRule(40);
            try { dbg.enterDecision(40, decisionCanBacktrack[40]);

            int LA40_0 = input.LA(1);

            if ( (LA40_0==IDENT||LA40_0==STAR) ) {
                int LA40_1 = input.LA(2);

                if ( (synpred3_Css3()) ) {
                    alt40=1;
                }
            }
            else if ( (LA40_0==82) && (synpred3_Css3())) {
                alt40=1;
            }
            } finally {dbg.exitDecision(40);}

            switch (alt40) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:7: ( nsPred )=> namespacePrefix
                    {
                    dbg.location(265,17);
                    pushFollow(FOLLOW_namespacePrefix_in_typeSelector944);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(40);}

            dbg.location(265,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:35: ( elementName ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:37: elementName ( WS )*
            {
            dbg.location(265,37);
            pushFollow(FOLLOW_elementName_in_typeSelector950);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(265,49);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:49: ( WS )*
            try { dbg.enterSubRule(41);

            loop41:
            do {
                int alt41=2;
                try { dbg.enterDecision(41, decisionCanBacktrack[41]);

                int LA41_0 = input.LA(1);

                if ( (LA41_0==WS) ) {
                    alt41=1;
                }


                } finally {dbg.exitDecision(41);}

                switch (alt41) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:49: WS
            	    {
            	    dbg.location(265,49);
            	    match(input,WS,FOLLOW_WS_in_typeSelector952); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop41;
                }
            } while (true);
            } finally {dbg.exitSubRule(41);}


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(266, 3);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "typeSelector");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "typeSelector"


    // $ANTLR start "nsPred"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:274:2: nsPred : ( IDENT | STAR ) '|' ;
    public final void nsPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "nsPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(274, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:275:3: ( ( IDENT | STAR ) '|' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:276:3: ( IDENT | STAR ) '|'
            {
            dbg.location(276,3);
            if ( input.LA(1)==IDENT||input.LA(1)==STAR ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }

            dbg.location(276,18);
            match(input,82,FOLLOW_82_in_nsPred990); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(277, 3);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "nsPred");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "nsPred"


    // $ANTLR start "namespacePrefix"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:279:2: namespacePrefix : ( namespaceName ( WS )* )? '|' ;
    public final void namespacePrefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(279, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:280:3: ( ( namespaceName ( WS )* )? '|' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:280:5: ( namespaceName ( WS )* )? '|'
            {
            dbg.location(280,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:280:5: ( namespaceName ( WS )* )?
            int alt43=2;
            try { dbg.enterSubRule(43);
            try { dbg.enterDecision(43, decisionCanBacktrack[43]);

            int LA43_0 = input.LA(1);

            if ( (LA43_0==IDENT||LA43_0==STAR) ) {
                alt43=1;
            }
            } finally {dbg.exitDecision(43);}

            switch (alt43) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:280:7: namespaceName ( WS )*
                    {
                    dbg.location(280,7);
                    pushFollow(FOLLOW_namespaceName_in_namespacePrefix1010);
                    namespaceName();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(280,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:280:21: ( WS )*
                    try { dbg.enterSubRule(42);

                    loop42:
                    do {
                        int alt42=2;
                        try { dbg.enterDecision(42, decisionCanBacktrack[42]);

                        int LA42_0 = input.LA(1);

                        if ( (LA42_0==WS) ) {
                            alt42=1;
                        }


                        } finally {dbg.exitDecision(42);}

                        switch (alt42) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:280:21: WS
                    	    {
                    	    dbg.location(280,21);
                    	    match(input,WS,FOLLOW_WS_in_namespacePrefix1012); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop42;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(42);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(43);}

            dbg.location(280,28);
            match(input,82,FOLLOW_82_in_namespacePrefix1018); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(281, 3);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "namespacePrefix");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "namespacePrefix"


    // $ANTLR start "namespaceName"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:283:2: namespaceName : ( IDENT | STAR );
    public final void namespaceName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespaceName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(283, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:284:3: ( IDENT | STAR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(284,3);
            if ( input.LA(1)==IDENT||input.LA(1)==STAR ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(285, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "namespaceName");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "namespaceName"


    // $ANTLR start "esPred"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:1: esPred : ( HASH | DOT | LBRACKET | COLON );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(287, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:288:5: ( HASH | DOT | LBRACKET | COLON )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(288,5);
            if ( input.LA(1)==COLON||(input.LA(1)>=HASH && input.LA(1)<=LBRACKET) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(289, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "esPred");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "esPred"


    // $ANTLR start "elementSubsequent"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:291:1: elementSubsequent : ( cssId | cssClass | attrib | pseudo ) ( WS )* ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(291, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:292:5: ( ( cssId | cssClass | attrib | pseudo ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:293:5: ( cssId | cssClass | attrib | pseudo ) ( WS )*
            {
            dbg.location(293,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:293:5: ( cssId | cssClass | attrib | pseudo )
            int alt44=4;
            try { dbg.enterSubRule(44);
            try { dbg.enterDecision(44, decisionCanBacktrack[44]);

            switch ( input.LA(1) ) {
            case HASH:
                {
                alt44=1;
                }
                break;
            case DOT:
                {
                alt44=2;
                }
                break;
            case LBRACKET:
                {
                alt44=3;
                }
                break;
            case COLON:
                {
                alt44=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 44, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(44);}

            switch (alt44) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:294:6: cssId
                    {
                    dbg.location(294,6);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent1112);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:295:8: cssClass
                    {
                    dbg.location(295,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent1121);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:296:11: attrib
                    {
                    dbg.location(296,11);
                    pushFollow(FOLLOW_attrib_in_elementSubsequent1133);
                    attrib();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:297:11: pseudo
                    {
                    dbg.location(297,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent1145);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(44);}

            dbg.location(299,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:299:5: ( WS )*
            try { dbg.enterSubRule(45);

            loop45:
            do {
                int alt45=2;
                try { dbg.enterDecision(45, decisionCanBacktrack[45]);

                int LA45_0 = input.LA(1);

                if ( (LA45_0==WS) ) {
                    alt45=1;
                }


                } finally {dbg.exitDecision(45);}

                switch (alt45) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:299:5: WS
            	    {
            	    dbg.location(299,5);
            	    match(input,WS,FOLLOW_WS_in_elementSubsequent1157); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop45;
                }
            } while (true);
            } finally {dbg.exitSubRule(45);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(300, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "elementSubsequent");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "elementSubsequent"


    // $ANTLR start "cssId"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:302:1: cssId : HASH ;
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(302, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:303:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:303:7: HASH
            {
            dbg.location(303,7);
            match(input,HASH,FOLLOW_HASH_in_cssId1179); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(304, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cssId");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cssId"


    // $ANTLR start "cssClass"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:1: cssClass : DOT IDENT ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(306, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:307:5: ( DOT IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:307:7: DOT IDENT
            {
            dbg.location(307,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass1196); if (state.failed) return ;
            dbg.location(307,11);
            match(input,IDENT,FOLLOW_IDENT_in_cssClass1198); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(308, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "cssClass");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cssClass"


    // $ANTLR start "elementName"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:311:1: elementName : ( IDENT | '*' );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(311, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:312:5: ( IDENT | '*' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(312,5);
            if ( input.LA(1)==IDENT||input.LA(1)==STAR ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(313, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "elementName");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "elementName"


    // $ANTLR start "attrib"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:315:1: attrib : LBRACKET ( namespacePrefix )? ( WS )* IDENT ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* ( IDENT | STRING ) ( WS )* )? RBRACKET ;
    public final void attrib() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrib");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(315, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:316:5: ( LBRACKET ( namespacePrefix )? ( WS )* IDENT ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* ( IDENT | STRING ) ( WS )* )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:316:7: LBRACKET ( namespacePrefix )? ( WS )* IDENT ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* ( IDENT | STRING ) ( WS )* )? RBRACKET
            {
            dbg.location(316,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_attrib1245); if (state.failed) return ;
            dbg.location(317,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:317:6: ( namespacePrefix )?
            int alt46=2;
            try { dbg.enterSubRule(46);
            try { dbg.enterDecision(46, decisionCanBacktrack[46]);

            try {
                isCyclicDecision = true;
                alt46 = dfa46.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(46);}

            switch (alt46) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:317:6: namespacePrefix
                    {
                    dbg.location(317,6);
                    pushFollow(FOLLOW_namespacePrefix_in_attrib1252);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(46);}

            dbg.location(317,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:317:23: ( WS )*
            try { dbg.enterSubRule(47);

            loop47:
            do {
                int alt47=2;
                try { dbg.enterDecision(47, decisionCanBacktrack[47]);

                int LA47_0 = input.LA(1);

                if ( (LA47_0==WS) ) {
                    alt47=1;
                }


                } finally {dbg.exitDecision(47);}

                switch (alt47) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:317:23: WS
            	    {
            	    dbg.location(317,23);
            	    match(input,WS,FOLLOW_WS_in_attrib1255); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop47;
                }
            } while (true);
            } finally {dbg.exitSubRule(47);}

            dbg.location(318,9);
            match(input,IDENT,FOLLOW_IDENT_in_attrib1266); if (state.failed) return ;
            dbg.location(318,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:318:15: ( WS )*
            try { dbg.enterSubRule(48);

            loop48:
            do {
                int alt48=2;
                try { dbg.enterDecision(48, decisionCanBacktrack[48]);

                int LA48_0 = input.LA(1);

                if ( (LA48_0==WS) ) {
                    alt48=1;
                }


                } finally {dbg.exitDecision(48);}

                switch (alt48) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:318:15: WS
            	    {
            	    dbg.location(318,15);
            	    match(input,WS,FOLLOW_WS_in_attrib1268); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop48;
                }
            } while (true);
            } finally {dbg.exitSubRule(48);}

            dbg.location(320,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:13: ( ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* ( IDENT | STRING ) ( WS )* )?
            int alt51=2;
            try { dbg.enterSubRule(51);
            try { dbg.enterDecision(51, decisionCanBacktrack[51]);

            int LA51_0 = input.LA(1);

            if ( ((LA51_0>=OPEQ && LA51_0<=DASHMATCH)) ) {
                alt51=1;
            }
            } finally {dbg.exitDecision(51);}

            switch (alt51) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:321:17: ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* ( IDENT | STRING ) ( WS )*
                    {
                    dbg.location(321,17);
                    if ( (input.LA(1)>=OPEQ && input.LA(1)<=DASHMATCH) ) {
                        input.consume();
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        dbg.recognitionException(mse);
                        throw mse;
                    }

                    dbg.location(326,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:326:17: ( WS )*
                    try { dbg.enterSubRule(49);

                    loop49:
                    do {
                        int alt49=2;
                        try { dbg.enterDecision(49, decisionCanBacktrack[49]);

                        int LA49_0 = input.LA(1);

                        if ( (LA49_0==WS) ) {
                            alt49=1;
                        }


                        } finally {dbg.exitDecision(49);}

                        switch (alt49) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:326:17: WS
                    	    {
                    	    dbg.location(326,17);
                    	    match(input,WS,FOLLOW_WS_in_attrib1418); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop49;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(49);}

                    dbg.location(327,17);
                    if ( input.LA(1)==STRING||input.LA(1)==IDENT ) {
                        input.consume();
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        dbg.recognitionException(mse);
                        throw mse;
                    }

                    dbg.location(331,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:331:17: ( WS )*
                    try { dbg.enterSubRule(50);

                    loop50:
                    do {
                        int alt50=2;
                        try { dbg.enterDecision(50, decisionCanBacktrack[50]);

                        int LA50_0 = input.LA(1);

                        if ( (LA50_0==WS) ) {
                            alt50=1;
                        }


                        } finally {dbg.exitDecision(50);}

                        switch (alt50) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:331:17: WS
                    	    {
                    	    dbg.location(331,17);
                    	    match(input,WS,FOLLOW_WS_in_attrib1521); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop50;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(50);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(51);}

            dbg.location(334,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_attrib1550); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(335, 1);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "attrib");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "attrib"


    // $ANTLR start "pseudo"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:337:1: pseudo : COLON IDENT ( ( WS )* LPAREN ( WS )* ( IDENT ( WS )* )? RPAREN )? ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(337, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:5: ( COLON IDENT ( ( WS )* LPAREN ( WS )* ( IDENT ( WS )* )? RPAREN )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:7: COLON IDENT ( ( WS )* LPAREN ( WS )* ( IDENT ( WS )* )? RPAREN )?
            {
            dbg.location(338,7);
            match(input,COLON,FOLLOW_COLON_in_pseudo1563); if (state.failed) return ;
            dbg.location(339,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudo1578); if (state.failed) return ;
            dbg.location(340,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:340:17: ( ( WS )* LPAREN ( WS )* ( IDENT ( WS )* )? RPAREN )?
            int alt56=2;
            try { dbg.enterSubRule(56);
            try { dbg.enterDecision(56, decisionCanBacktrack[56]);

            try {
                isCyclicDecision = true;
                alt56 = dfa56.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(56);}

            switch (alt56) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:21: ( WS )* LPAREN ( WS )* ( IDENT ( WS )* )? RPAREN
                    {
                    dbg.location(341,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:21: ( WS )*
                    try { dbg.enterSubRule(52);

                    loop52:
                    do {
                        int alt52=2;
                        try { dbg.enterDecision(52, decisionCanBacktrack[52]);

                        int LA52_0 = input.LA(1);

                        if ( (LA52_0==WS) ) {
                            alt52=1;
                        }


                        } finally {dbg.exitDecision(52);}

                        switch (alt52) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:21: WS
                    	    {
                    	    dbg.location(341,21);
                    	    match(input,WS,FOLLOW_WS_in_pseudo1620); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop52;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(52);}

                    dbg.location(341,25);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo1623); if (state.failed) return ;
                    dbg.location(341,32);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:32: ( WS )*
                    try { dbg.enterSubRule(53);

                    loop53:
                    do {
                        int alt53=2;
                        try { dbg.enterDecision(53, decisionCanBacktrack[53]);

                        int LA53_0 = input.LA(1);

                        if ( (LA53_0==WS) ) {
                            alt53=1;
                        }


                        } finally {dbg.exitDecision(53);}

                        switch (alt53) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:32: WS
                    	    {
                    	    dbg.location(341,32);
                    	    match(input,WS,FOLLOW_WS_in_pseudo1625); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop53;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(53);}

                    dbg.location(341,36);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:36: ( IDENT ( WS )* )?
                    int alt55=2;
                    try { dbg.enterSubRule(55);
                    try { dbg.enterDecision(55, decisionCanBacktrack[55]);

                    int LA55_0 = input.LA(1);

                    if ( (LA55_0==IDENT) ) {
                        alt55=1;
                    }
                    } finally {dbg.exitDecision(55);}

                    switch (alt55) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:37: IDENT ( WS )*
                            {
                            dbg.location(341,37);
                            match(input,IDENT,FOLLOW_IDENT_in_pseudo1629); if (state.failed) return ;
                            dbg.location(341,43);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:43: ( WS )*
                            try { dbg.enterSubRule(54);

                            loop54:
                            do {
                                int alt54=2;
                                try { dbg.enterDecision(54, decisionCanBacktrack[54]);

                                int LA54_0 = input.LA(1);

                                if ( (LA54_0==WS) ) {
                                    alt54=1;
                                }


                                } finally {dbg.exitDecision(54);}

                                switch (alt54) {
                            	case 1 :
                            	    dbg.enterAlt(1);

                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:43: WS
                            	    {
                            	    dbg.location(341,43);
                            	    match(input,WS,FOLLOW_WS_in_pseudo1631); if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop54;
                                }
                            } while (true);
                            } finally {dbg.exitSubRule(54);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(55);}

                    dbg.location(341,49);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo1636); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(56);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(343, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "pseudo");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "pseudo"


    // $ANTLR start "declaration"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:345:1: declaration : property COLON ( WS )* expr ( prio )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(345, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:346:5: ( property COLON ( WS )* expr ( prio )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:5: property COLON ( WS )* expr ( prio )?
            {
            dbg.location(348,5);
            pushFollow(FOLLOW_property_in_declaration1682);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(348,14);
            match(input,COLON,FOLLOW_COLON_in_declaration1684); if (state.failed) return ;
            dbg.location(348,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:20: ( WS )*
            try { dbg.enterSubRule(57);

            loop57:
            do {
                int alt57=2;
                try { dbg.enterDecision(57, decisionCanBacktrack[57]);

                int LA57_0 = input.LA(1);

                if ( (LA57_0==WS) ) {
                    alt57=1;
                }


                } finally {dbg.exitDecision(57);}

                switch (alt57) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:20: WS
            	    {
            	    dbg.location(348,20);
            	    match(input,WS,FOLLOW_WS_in_declaration1686); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop57;
                }
            } while (true);
            } finally {dbg.exitSubRule(57);}

            dbg.location(348,24);
            pushFollow(FOLLOW_expr_in_declaration1689);
            expr();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(348,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:29: ( prio )?
            int alt58=2;
            try { dbg.enterSubRule(58);
            try { dbg.enterDecision(58, decisionCanBacktrack[58]);

            int LA58_0 = input.LA(1);

            if ( (LA58_0==IMPORTANT_SYM) ) {
                alt58=1;
            }
            } finally {dbg.exitDecision(58);}

            switch (alt58) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:29: prio
                    {
                    dbg.location(348,29);
                    pushFollow(FOLLOW_prio_in_declaration1691);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(58);}


            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    //recovery: if an mismatched token occures inside a declaration is found,
                    //then skip all tokens until an end of the rule is found represented by right curly brace
                    consumeUntil(input, BitSet.of(SEMI, RBRACE)); 
                
        }
        finally {
        }
        dbg.location(349, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "declaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "declaration"


    // $ANTLR start "syncToIdent"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:1: syncToIdent : ;
    public final void syncToIdent() throws RecognitionException {

                syncToSet(BitSet.of(IDENT));
            
        try { dbg.enterRule(getGrammarFileName(), "syncToIdent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(359, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:6: 
            {
            }

        }
        finally {
        }
        dbg.location(365, 6);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "syncToIdent");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "syncToIdent"


    // $ANTLR start "prio"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:367:1: prio : IMPORTANT_SYM ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(367, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:5: ( IMPORTANT_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:7: IMPORTANT_SYM
            {
            dbg.location(368,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio1757); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(369, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "prio");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "prio"


    // $ANTLR start "expr"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:371:1: expr : term ( operator term )* ;
    public final void expr() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expr");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(371, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:5: ( term ( operator term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:7: term ( operator term )*
            {
            dbg.location(372,7);
            pushFollow(FOLLOW_term_in_expr1778);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(372,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:12: ( operator term )*
            try { dbg.enterSubRule(59);

            loop59:
            do {
                int alt59=2;
                try { dbg.enterDecision(59, decisionCanBacktrack[59]);

                int LA59_0 = input.LA(1);

                if ( (LA59_0==COMMA) ) {
                    int LA59_2 = input.LA(2);

                    if ( (LA59_2==IDENT) ) {
                        int LA59_4 = input.LA(3);

                        if ( (LA59_4==WS||(LA59_4>=STRING && LA59_4<=SEMI)||LA59_4==URI||(LA59_4>=RBRACE && LA59_4<=IDENT)||(LA59_4>=SOLIDUS && LA59_4<=PLUS)||LA59_4==MINUS||LA59_4==HASH||(LA59_4>=LPAREN && LA59_4<=FREQ)) ) {
                            alt59=1;
                        }


                    }
                    else if ( (LA59_2==WS||LA59_2==STRING||LA59_2==URI||LA59_2==PLUS||LA59_2==MINUS||LA59_2==HASH||(LA59_2>=NUMBER && LA59_2<=FREQ)) ) {
                        alt59=1;
                    }


                }
                else if ( (LA59_0==STRING||LA59_0==URI||LA59_0==IDENT||(LA59_0>=SOLIDUS && LA59_0<=PLUS)||LA59_0==MINUS||LA59_0==HASH||(LA59_0>=NUMBER && LA59_0<=FREQ)) ) {
                    alt59=1;
                }


                } finally {dbg.exitDecision(59);}

                switch (alt59) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:13: operator term
            	    {
            	    dbg.location(372,13);
            	    pushFollow(FOLLOW_operator_in_expr1781);
            	    operator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(372,22);
            	    pushFollow(FOLLOW_term_in_expr1783);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop59;
                }
            } while (true);
            } finally {dbg.exitSubRule(59);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(373, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "expr");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "expr"


    // $ANTLR start "term"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:375:1: term : ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | URI | hexColor | function ) ( WS )* ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(375, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:5: ( ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | URI | hexColor | function ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:7: ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | URI | hexColor | function ) ( WS )*
            {
            dbg.location(376,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:7: ( unaryOperator )?
            int alt60=2;
            try { dbg.enterSubRule(60);
            try { dbg.enterDecision(60, decisionCanBacktrack[60]);

            int LA60_0 = input.LA(1);

            if ( (LA60_0==PLUS||LA60_0==MINUS) ) {
                alt60=1;
            }
            } finally {dbg.exitDecision(60);}

            switch (alt60) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:7: unaryOperator
                    {
                    dbg.location(376,7);
                    pushFollow(FOLLOW_unaryOperator_in_term1806);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(60);}

            dbg.location(377,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | URI | hexColor | function )
            int alt61=6;
            try { dbg.enterSubRule(61);
            try { dbg.enterDecision(61, decisionCanBacktrack[61]);

            try {
                isCyclicDecision = true;
                alt61 = dfa61.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(61);}

            switch (alt61) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:378:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ )
                    {
                    dbg.location(378,9);
                    if ( (input.LA(1)>=NUMBER && input.LA(1)<=FREQ) ) {
                        input.consume();
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        dbg.recognitionException(mse);
                        throw mse;
                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:388:7: STRING
                    {
                    dbg.location(388,7);
                    match(input,STRING,FOLLOW_STRING_in_term1973); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:7: IDENT
                    {
                    dbg.location(389,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term1981); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:7: URI
                    {
                    dbg.location(390,7);
                    match(input,URI,FOLLOW_URI_in_term1989); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:391:7: hexColor
                    {
                    dbg.location(391,7);
                    pushFollow(FOLLOW_hexColor_in_term1997);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:392:7: function
                    {
                    dbg.location(392,7);
                    pushFollow(FOLLOW_function_in_term2005);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(61);}

            dbg.location(394,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:5: ( WS )*
            try { dbg.enterSubRule(62);

            loop62:
            do {
                int alt62=2;
                try { dbg.enterDecision(62, decisionCanBacktrack[62]);

                int LA62_0 = input.LA(1);

                if ( (LA62_0==WS) ) {
                    alt62=1;
                }


                } finally {dbg.exitDecision(62);}

                switch (alt62) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:5: WS
            	    {
            	    dbg.location(394,5);
            	    match(input,WS,FOLLOW_WS_in_term2017); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop62;
                }
            } while (true);
            } finally {dbg.exitSubRule(62);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(395, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "term");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "term"


    // $ANTLR start "function"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:1: function : function_name ( WS )* LPAREN ( expr | ( attribute ( COMMA attribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(397, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:398:2: ( function_name ( WS )* LPAREN ( expr | ( attribute ( COMMA attribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:398:5: function_name ( WS )* LPAREN ( expr | ( attribute ( COMMA attribute )* ) ) RPAREN
            {
            dbg.location(398,5);
            pushFollow(FOLLOW_function_name_in_function2033);
            function_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(398,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:398:19: ( WS )*
            try { dbg.enterSubRule(63);

            loop63:
            do {
                int alt63=2;
                try { dbg.enterDecision(63, decisionCanBacktrack[63]);

                int LA63_0 = input.LA(1);

                if ( (LA63_0==WS) ) {
                    alt63=1;
                }


                } finally {dbg.exitDecision(63);}

                switch (alt63) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:398:19: WS
            	    {
            	    dbg.location(398,19);
            	    match(input,WS,FOLLOW_WS_in_function2035); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop63;
                }
            } while (true);
            } finally {dbg.exitSubRule(63);}

            dbg.location(399,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function2040); if (state.failed) return ;
            dbg.location(400,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:400:3: ( expr | ( attribute ( COMMA attribute )* ) )
            int alt65=2;
            try { dbg.enterSubRule(65);
            try { dbg.enterDecision(65, decisionCanBacktrack[65]);

            int LA65_0 = input.LA(1);

            if ( (LA65_0==STRING||LA65_0==URI||LA65_0==PLUS||LA65_0==MINUS||LA65_0==HASH||(LA65_0>=NUMBER && LA65_0<=FREQ)) ) {
                alt65=1;
            }
            else if ( (LA65_0==IDENT) ) {
                int LA65_2 = input.LA(2);

                if ( (LA65_2==WS||LA65_2==STRING||LA65_2==URI||(LA65_2>=COMMA && LA65_2<=IDENT)||(LA65_2>=SOLIDUS && LA65_2<=PLUS)||LA65_2==MINUS||LA65_2==HASH||(LA65_2>=LPAREN && LA65_2<=RPAREN)||(LA65_2>=NUMBER && LA65_2<=FREQ)) ) {
                    alt65=1;
                }
                else if ( (LA65_2==OPEQ) ) {
                    alt65=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 65, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 65, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(65);}

            switch (alt65) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:4: expr
                    {
                    dbg.location(401,4);
                    pushFollow(FOLLOW_expr_in_function2050);
                    expr();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:6: ( attribute ( COMMA attribute )* )
                    {
                    dbg.location(403,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:6: ( attribute ( COMMA attribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:404:5: attribute ( COMMA attribute )*
                    {
                    dbg.location(404,5);
                    pushFollow(FOLLOW_attribute_in_function2069);
                    attribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(404,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:404:15: ( COMMA attribute )*
                    try { dbg.enterSubRule(64);

                    loop64:
                    do {
                        int alt64=2;
                        try { dbg.enterDecision(64, decisionCanBacktrack[64]);

                        int LA64_0 = input.LA(1);

                        if ( (LA64_0==COMMA) ) {
                            alt64=1;
                        }


                        } finally {dbg.exitDecision(64);}

                        switch (alt64) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:404:16: COMMA attribute
                    	    {
                    	    dbg.location(404,16);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function2072); if (state.failed) return ;
                    	    dbg.location(404,22);
                    	    pushFollow(FOLLOW_attribute_in_function2074);
                    	    attribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop64;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(64);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(65);}

            dbg.location(407,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function2094); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(408, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "function");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "function"


    // $ANTLR start "function_name"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:1: function_name : IDENT ;
    public final void function_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(410, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:4: IDENT
            {
            dbg.location(411,4);
            match(input,IDENT,FOLLOW_IDENT_in_function_name2109); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(412, 6);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "function_name");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "function_name"


    // $ANTLR start "attribute"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:1: attribute : attrname OPEQ attrvalue ;
    public final void attribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(414, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:2: ( attrname OPEQ attrvalue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:4: attrname OPEQ attrvalue
            {
            dbg.location(415,4);
            pushFollow(FOLLOW_attrname_in_attribute2129);
            attrname();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(415,13);
            match(input,OPEQ,FOLLOW_OPEQ_in_attribute2131); if (state.failed) return ;
            dbg.location(415,18);
            pushFollow(FOLLOW_attrvalue_in_attribute2133);
            attrvalue();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(416, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "attribute");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "attribute"


    // $ANTLR start "attrname"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:1: attrname : IDENT ;
    public final void attrname() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrname");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(418, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:419:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:419:4: IDENT
            {
            dbg.location(419,4);
            match(input,IDENT,FOLLOW_IDENT_in_attrname2148); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(420, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "attrname");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "attrname"


    // $ANTLR start "attrvalue"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:1: attrvalue : expr ;
    public final void attrvalue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrvalue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(422, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:2: ( expr )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:4: expr
            {
            dbg.location(423,4);
            pushFollow(FOLLOW_expr_in_attrvalue2160);
            expr();

            state._fsp--;
            if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(424, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "attrvalue");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "attrvalue"


    // $ANTLR start "hexColor"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(426, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:7: HASH
            {
            dbg.location(427,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor2178); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(428, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "hexColor");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "hexColor"

    // $ANTLR start synpred1_Css3
    public final void synpred1_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:250:19: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:250:20: esPred
        {
        dbg.location(250,20);
        pushFollow(FOLLOW_esPred_in_synpred1_Css3881);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:252:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:252:6: esPred
        {
        dbg.location(252,6);
        pushFollow(FOLLOW_esPred_in_synpred2_Css3899);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:7: ( nsPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:8: nsPred
        {
        dbg.location(265,8);
        pushFollow(FOLLOW_nsPred_in_synpred3_Css3941);
        nsPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred3_Css3

    // Delegated rules

    public final boolean synpred3_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred3_Css3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred2_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred2_Css3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred1_Css3() {
        state.backtracking++;
        dbg.beginBacktrack(state.backtracking);
        int start = input.mark();
        try {
            synpred1_Css3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        dbg.endBacktrack(state.backtracking, success);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA37 dfa37 = new DFA37(this);
    protected DFA46 dfa46 = new DFA46(this);
    protected DFA56 dfa56 = new DFA56(this);
    protected DFA61 dfa61 = new DFA61(this);
    static final String DFA37_eotS =
        "\26\uffff";
    static final String DFA37_eofS =
        "\26\uffff";
    static final String DFA37_minS =
        "\1\13\1\uffff\1\0\1\16\1\4\1\16\1\uffff\1\0\4\4\1\0\2\4\1\0\6\4";
    static final String DFA37_maxS =
        "\1\122\1\uffff\1\0\1\16\1\122\1\16\1\uffff\1\0\1\122\2\16\1\122"+
        "\1\0\1\122\1\16\1\0\1\36\1\122\1\16\3\36";
    static final String DFA37_acceptS =
        "\1\uffff\1\2\4\uffff\1\1\17\uffff";
    static final String DFA37_specialS =
        "\2\uffff\1\2\4\uffff\1\0\4\uffff\1\3\2\uffff\1\1\6\uffff}>";
    static final String[] DFA37_transitionS = {
            "\1\1\1\uffff\2\1\2\uffff\1\5\1\uffff\3\1\1\uffff\1\1\1\2\1\3"+
            "\1\4\67\uffff\1\1",
            "",
            "\1\uffff",
            "\1\7",
            "\1\12\11\uffff\1\10\10\uffff\1\13\72\uffff\1\11",
            "\1\14",
            "",
            "\1\uffff",
            "\1\15\26\uffff\3\16\1\17\63\uffff\1\11",
            "\1\12\11\uffff\1\20",
            "\1\12\11\uffff\1\20",
            "\1\21\115\uffff\1\11",
            "\1\uffff",
            "\1\15\26\uffff\3\16\1\17\63\uffff\1\11",
            "\1\22\1\uffff\1\23\7\uffff\1\23",
            "\1\uffff",
            "\1\24\26\uffff\3\16\1\17",
            "\1\21\115\uffff\1\11",
            "\1\22\1\uffff\1\23\7\uffff\1\23",
            "\1\25\31\uffff\1\17",
            "\1\24\26\uffff\3\16\1\17",
            "\1\25\31\uffff\1\17"
    };

    static final short[] DFA37_eot = DFA.unpackEncodedString(DFA37_eotS);
    static final short[] DFA37_eof = DFA.unpackEncodedString(DFA37_eofS);
    static final char[] DFA37_min = DFA.unpackEncodedStringToUnsignedChars(DFA37_minS);
    static final char[] DFA37_max = DFA.unpackEncodedStringToUnsignedChars(DFA37_maxS);
    static final short[] DFA37_accept = DFA.unpackEncodedString(DFA37_acceptS);
    static final short[] DFA37_special = DFA.unpackEncodedString(DFA37_specialS);
    static final short[][] DFA37_transition;

    static {
        int numStates = DFA37_transitionS.length;
        DFA37_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA37_transition[i] = DFA.unpackEncodedString(DFA37_transitionS[i]);
        }
    }

    class DFA37 extends DFA {

        public DFA37(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 37;
            this.eot = DFA37_eot;
            this.eof = DFA37_eof;
            this.min = DFA37_min;
            this.max = DFA37_max;
            this.accept = DFA37_accept;
            this.special = DFA37_special;
            this.transition = DFA37_transition;
        }
        public String getDescription() {
            return "()* loopback of 250:18: ( ( esPred )=> elementSubsequent )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA37_7 = input.LA(1);

                         
                        int index37_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index37_7);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA37_15 = input.LA(1);

                         
                        int index37_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index37_15);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA37_2 = input.LA(1);

                         
                        int index37_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index37_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA37_12 = input.LA(1);

                         
                        int index37_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index37_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 37, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA46_eotS =
        "\5\uffff";
    static final String DFA46_eofS =
        "\5\uffff";
    static final String DFA46_minS =
        "\2\4\2\uffff\1\4";
    static final String DFA46_maxS =
        "\2\122\2\uffff\1\122";
    static final String DFA46_acceptS =
        "\2\uffff\1\1\1\2\1\uffff";
    static final String DFA46_specialS =
        "\5\uffff}>";
    static final String[] DFA46_transitionS = {
            "\1\3\11\uffff\1\1\10\uffff\1\2\72\uffff\1\2",
            "\1\4\26\uffff\4\3\63\uffff\1\2",
            "",
            "",
            "\1\4\26\uffff\4\3\63\uffff\1\2"
    };

    static final short[] DFA46_eot = DFA.unpackEncodedString(DFA46_eotS);
    static final short[] DFA46_eof = DFA.unpackEncodedString(DFA46_eofS);
    static final char[] DFA46_min = DFA.unpackEncodedStringToUnsignedChars(DFA46_minS);
    static final char[] DFA46_max = DFA.unpackEncodedStringToUnsignedChars(DFA46_maxS);
    static final short[] DFA46_accept = DFA.unpackEncodedString(DFA46_acceptS);
    static final short[] DFA46_special = DFA.unpackEncodedString(DFA46_specialS);
    static final short[][] DFA46_transition;

    static {
        int numStates = DFA46_transitionS.length;
        DFA46_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA46_transition[i] = DFA.unpackEncodedString(DFA46_transitionS[i]);
        }
    }

    class DFA46 extends DFA {

        public DFA46(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 46;
            this.eot = DFA46_eot;
            this.eof = DFA46_eof;
            this.min = DFA46_min;
            this.max = DFA46_max;
            this.accept = DFA46_accept;
            this.special = DFA46_special;
            this.transition = DFA46_transition;
        }
        public String getDescription() {
            return "317:6: ( namespacePrefix )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA56_eotS =
        "\4\uffff";
    static final String DFA56_eofS =
        "\4\uffff";
    static final String DFA56_minS =
        "\2\4\2\uffff";
    static final String DFA56_maxS =
        "\2\122\2\uffff";
    static final String DFA56_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA56_specialS =
        "\4\uffff}>";
    static final String[] DFA56_transitionS = {
            "\1\1\6\uffff\1\3\1\uffff\2\3\2\uffff\1\3\1\uffff\3\3\1\uffff"+
            "\4\3\4\uffff\1\2\62\uffff\1\3",
            "\1\1\6\uffff\1\3\1\uffff\2\3\2\uffff\1\3\1\uffff\3\3\1\uffff"+
            "\4\3\4\uffff\1\2\62\uffff\1\3",
            "",
            ""
    };

    static final short[] DFA56_eot = DFA.unpackEncodedString(DFA56_eotS);
    static final short[] DFA56_eof = DFA.unpackEncodedString(DFA56_eofS);
    static final char[] DFA56_min = DFA.unpackEncodedStringToUnsignedChars(DFA56_minS);
    static final char[] DFA56_max = DFA.unpackEncodedStringToUnsignedChars(DFA56_maxS);
    static final short[] DFA56_accept = DFA.unpackEncodedString(DFA56_acceptS);
    static final short[] DFA56_special = DFA.unpackEncodedString(DFA56_specialS);
    static final short[][] DFA56_transition;

    static {
        int numStates = DFA56_transitionS.length;
        DFA56_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA56_transition[i] = DFA.unpackEncodedString(DFA56_transitionS[i]);
        }
    }

    class DFA56 extends DFA {

        public DFA56(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 56;
            this.eot = DFA56_eot;
            this.eof = DFA56_eof;
            this.min = DFA56_min;
            this.max = DFA56_max;
            this.accept = DFA56_accept;
            this.special = DFA56_special;
            this.transition = DFA56_transition;
        }
        public String getDescription() {
            return "340:17: ( ( WS )* LPAREN ( WS )* ( IDENT ( WS )* )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA61_eotS =
        "\11\uffff";
    static final String DFA61_eofS =
        "\11\uffff";
    static final String DFA61_minS =
        "\1\6\2\uffff\1\4\2\uffff\1\4\2\uffff";
    static final String DFA61_maxS =
        "\1\51\2\uffff\1\51\2\uffff\1\51\2\uffff";
    static final String DFA61_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\uffff\1\3\1\6";
    static final String DFA61_specialS =
        "\11\uffff}>";
    static final String[] DFA61_transitionS = {
            "\1\2\2\uffff\1\4\4\uffff\1\3\11\uffff\1\5\11\uffff\10\1",
            "",
            "",
            "\1\6\1\uffff\2\7\1\uffff\1\7\2\uffff\3\7\3\uffff\2\7\2\uffff"+
            "\1\7\1\uffff\1\7\6\uffff\1\10\12\7",
            "",
            "",
            "\1\6\1\uffff\2\7\1\uffff\1\7\2\uffff\3\7\3\uffff\2\7\2\uffff"+
            "\1\7\1\uffff\1\7\6\uffff\1\10\12\7",
            "",
            ""
    };

    static final short[] DFA61_eot = DFA.unpackEncodedString(DFA61_eotS);
    static final short[] DFA61_eof = DFA.unpackEncodedString(DFA61_eofS);
    static final char[] DFA61_min = DFA.unpackEncodedStringToUnsignedChars(DFA61_minS);
    static final char[] DFA61_max = DFA.unpackEncodedStringToUnsignedChars(DFA61_maxS);
    static final short[] DFA61_accept = DFA.unpackEncodedString(DFA61_acceptS);
    static final short[] DFA61_special = DFA.unpackEncodedString(DFA61_specialS);
    static final short[][] DFA61_transition;

    static {
        int numStates = DFA61_transitionS.length;
        DFA61_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA61_transition[i] = DFA.unpackEncodedString(DFA61_transitionS[i]);
        }
    }

    class DFA61 extends DFA {

        public DFA61(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 61;
            this.eot = DFA61_eot;
            this.eof = DFA61_eof;
            this.min = DFA61_min;
            this.max = DFA61_max;
            this.accept = DFA61_accept;
            this.special = DFA61_special;
            this.transition = DFA61_transition;
        }
        public String getDescription() {
            return "377:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | URI | hexColor | function )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_charSet_in_styleSheet77 = new BitSet(new long[]{0x0000000007834510L,0x0000000000040000L});
    public static final BitSet FOLLOW_WS_in_styleSheet85 = new BitSet(new long[]{0x0000000007834510L,0x0000000000040000L});
    public static final BitSet FOLLOW_imports_in_styleSheet97 = new BitSet(new long[]{0x0000000007834510L,0x0000000000040000L});
    public static final BitSet FOLLOW_WS_in_styleSheet99 = new BitSet(new long[]{0x0000000007834510L,0x0000000000040000L});
    public static final BitSet FOLLOW_bodylist_in_styleSheet114 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_styleSheet121 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHARSET_SYM_in_charSet147 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_WS_in_charSet149 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_STRING_in_charSet151 = new BitSet(new long[]{0x0000000000000090L});
    public static final BitSet FOLLOW_WS_in_charSet153 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_SEMI_in_charSet156 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORT_SYM_in_imports178 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_WS_in_imports180 = new BitSet(new long[]{0x0000000000000240L});
    public static final BitSet FOLLOW_set_in_imports182 = new BitSet(new long[]{0x000000000000C090L});
    public static final BitSet FOLLOW_WS_in_imports188 = new BitSet(new long[]{0x000000000000C090L});
    public static final BitSet FOLLOW_mediaList_in_imports191 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_SEMI_in_imports194 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEDIA_SYM_in_media215 = new BitSet(new long[]{0x000000000000C010L});
    public static final BitSet FOLLOW_WS_in_media217 = new BitSet(new long[]{0x000000000000C010L});
    public static final BitSet FOLLOW_mediaList_in_media220 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_LBRACE_in_media230 = new BitSet(new long[]{0x0000000007824010L,0x0000000000040000L});
    public static final BitSet FOLLOW_WS_in_media232 = new BitSet(new long[]{0x0000000007824010L,0x0000000000040000L});
    public static final BitSet FOLLOW_ruleSet_in_media247 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_RBRACE_in_media257 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_medium_in_mediaList278 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_COMMA_in_mediaList281 = new BitSet(new long[]{0x000000000000C010L});
    public static final BitSet FOLLOW_WS_in_mediaList283 = new BitSet(new long[]{0x000000000000C010L});
    public static final BitSet FOLLOW_medium_in_mediaList286 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_set_in_medium305 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_medium315 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_bodyset_in_bodylist338 = new BitSet(new long[]{0x0000000007834402L,0x0000000000040000L});
    public static final BitSet FOLLOW_ruleSet_in_bodyset367 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_media_in_bodyset379 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_page_in_bodyset391 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_bodyset407 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page432 = new BitSet(new long[]{0x0000000000020810L});
    public static final BitSet FOLLOW_WS_in_page434 = new BitSet(new long[]{0x0000000000020800L});
    public static final BitSet FOLLOW_pseudoPage_in_page438 = new BitSet(new long[]{0x0000000000000810L});
    public static final BitSet FOLLOW_WS_in_page440 = new BitSet(new long[]{0x0000000000000810L});
    public static final BitSet FOLLOW_LBRACE_in_page453 = new BitSet(new long[]{0x000000000000C010L});
    public static final BitSet FOLLOW_WS_in_page455 = new BitSet(new long[]{0x000000000000C010L});
    public static final BitSet FOLLOW_declaration_in_page470 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_SEMI_in_page472 = new BitSet(new long[]{0x000000000000D010L});
    public static final BitSet FOLLOW_declaration_in_page475 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_SEMI_in_page477 = new BitSet(new long[]{0x000000000000D010L});
    public static final BitSet FOLLOW_RBRACE_in_page489 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage510 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage512 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_operator533 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_operator535 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_COMMA_in_operator544 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_operator546 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_PLUS_in_combinator574 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator576 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_GREATER_in_combinator585 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator587 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_TILDE_in_combinator596 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator598 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_property658 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_property666 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_selectorsGroup_in_ruleSet691 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_LBRACE_in_ruleSet701 = new BitSet(new long[]{0x000000000000D090L});
    public static final BitSet FOLLOW_WS_in_ruleSet703 = new BitSet(new long[]{0x000000000000D090L});
    public static final BitSet FOLLOW_syncToIdent_in_ruleSet706 = new BitSet(new long[]{0x000000000000D090L});
    public static final BitSet FOLLOW_declarations_in_ruleSet720 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_RBRACE_in_ruleSet730 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_declarations768 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_SEMI_in_declarations772 = new BitSet(new long[]{0x000000000000C092L});
    public static final BitSet FOLLOW_WS_in_declarations774 = new BitSet(new long[]{0x000000000000C092L});
    public static final BitSet FOLLOW_declaration_in_declarations777 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup801 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup804 = new BitSet(new long[]{0x0000000007824010L,0x0000000000040000L});
    public static final BitSet FOLLOW_WS_in_selectorsGroup806 = new BitSet(new long[]{0x0000000007824010L,0x0000000000040000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup809 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector832 = new BitSet(new long[]{0x0000000007BA4002L,0x0000000000040000L});
    public static final BitSet FOLLOW_combinator_in_selector835 = new BitSet(new long[]{0x0000000007824000L,0x0000000000040000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector837 = new BitSet(new long[]{0x0000000007BA4002L,0x0000000000040000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence877 = new BitSet(new long[]{0x0000000007824002L,0x0000000000040000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence884 = new BitSet(new long[]{0x0000000007824002L,0x0000000000040000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence902 = new BitSet(new long[]{0x0000000007824002L,0x0000000000040000L});
    public static final BitSet FOLLOW_namespacePrefix_in_typeSelector944 = new BitSet(new long[]{0x0000000000804000L,0x0000000000040000L});
    public static final BitSet FOLLOW_elementName_in_typeSelector950 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_typeSelector952 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_set_in_nsPred982 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_82_in_nsPred990 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespaceName_in_namespacePrefix1010 = new BitSet(new long[]{0x0000000000000010L,0x0000000000040000L});
    public static final BitSet FOLLOW_WS_in_namespacePrefix1012 = new BitSet(new long[]{0x0000000000000010L,0x0000000000040000L});
    public static final BitSet FOLLOW_82_in_namespacePrefix1018 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_namespaceName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent1112 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent1121 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_attrib_in_elementSubsequent1133 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent1145 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_elementSubsequent1157 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_HASH_in_cssId1179 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass1196 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_IDENT_in_cssClass1198 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_attrib1245 = new BitSet(new long[]{0x0000000000804010L,0x0000000000040000L});
    public static final BitSet FOLLOW_namespacePrefix_in_attrib1252 = new BitSet(new long[]{0x0000000000004010L});
    public static final BitSet FOLLOW_WS_in_attrib1255 = new BitSet(new long[]{0x0000000000004010L});
    public static final BitSet FOLLOW_IDENT_in_attrib1266 = new BitSet(new long[]{0x0000000078000010L});
    public static final BitSet FOLLOW_WS_in_attrib1268 = new BitSet(new long[]{0x0000000078000010L});
    public static final BitSet FOLLOW_set_in_attrib1310 = new BitSet(new long[]{0x0000000000004050L});
    public static final BitSet FOLLOW_WS_in_attrib1418 = new BitSet(new long[]{0x0000000000004050L});
    public static final BitSet FOLLOW_set_in_attrib1437 = new BitSet(new long[]{0x0000000040000010L});
    public static final BitSet FOLLOW_WS_in_attrib1521 = new BitSet(new long[]{0x0000000040000010L});
    public static final BitSet FOLLOW_RBRACKET_in_attrib1550 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudo1563 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_IDENT_in_pseudo1578 = new BitSet(new long[]{0x0000000080000012L});
    public static final BitSet FOLLOW_WS_in_pseudo1620 = new BitSet(new long[]{0x0000000080000010L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo1623 = new BitSet(new long[]{0x0000000100004010L});
    public static final BitSet FOLLOW_WS_in_pseudo1625 = new BitSet(new long[]{0x0000000100004010L});
    public static final BitSet FOLLOW_IDENT_in_pseudo1629 = new BitSet(new long[]{0x0000000100000010L});
    public static final BitSet FOLLOW_WS_in_pseudo1631 = new BitSet(new long[]{0x0000000100000010L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo1636 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_declaration1682 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_COLON_in_declaration1684 = new BitSet(new long[]{0x000003FC01484250L});
    public static final BitSet FOLLOW_WS_in_declaration1686 = new BitSet(new long[]{0x000003FC01484250L});
    public static final BitSet FOLLOW_expr_in_declaration1689 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_prio_in_declaration1691 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio1757 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expr1778 = new BitSet(new long[]{0x000003FC014C6252L});
    public static final BitSet FOLLOW_operator_in_expr1781 = new BitSet(new long[]{0x000003FC01484250L});
    public static final BitSet FOLLOW_term_in_expr1783 = new BitSet(new long[]{0x000003FC014C6252L});
    public static final BitSet FOLLOW_unaryOperator_in_term1806 = new BitSet(new long[]{0x000003FC01484250L});
    public static final BitSet FOLLOW_set_in_term1827 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_STRING_in_term1973 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_IDENT_in_term1981 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_URI_in_term1989 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_hexColor_in_term1997 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_function_in_term2005 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_term2017 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_function_name_in_function2033 = new BitSet(new long[]{0x0000000080000010L});
    public static final BitSet FOLLOW_WS_in_function2035 = new BitSet(new long[]{0x0000000080000010L});
    public static final BitSet FOLLOW_LPAREN_in_function2040 = new BitSet(new long[]{0x000003FC01484250L});
    public static final BitSet FOLLOW_expr_in_function2050 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_attribute_in_function2069 = new BitSet(new long[]{0x0000000100002000L});
    public static final BitSet FOLLOW_COMMA_in_function2072 = new BitSet(new long[]{0x000003FC01484250L});
    public static final BitSet FOLLOW_attribute_in_function2074 = new BitSet(new long[]{0x0000000100002000L});
    public static final BitSet FOLLOW_RPAREN_in_function2094 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_function_name2109 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attrname_in_attribute2129 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_OPEQ_in_attribute2131 = new BitSet(new long[]{0x000003FC01484250L});
    public static final BitSet FOLLOW_attrvalue_in_attribute2133 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_attrname2148 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_in_attrvalue2160 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor2178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred1_Css3881 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred2_Css3899 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nsPred_in_synpred3_Css3941 = new BitSet(new long[]{0x0000000000000002L});

}