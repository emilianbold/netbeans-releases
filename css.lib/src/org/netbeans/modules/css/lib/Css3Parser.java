// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2011-08-03 15:02:47

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
        "invalidRule", "operator", "bodylist", "unaryOperator", "declaration", 
        "simpleSelectorSequence", "medium", "prio", "synpred3_Css3", "term", 
        "ruleSet", "attrname", "esPred", "elementSubsequent", "pseudoPage", 
        "bodyset", "elementName", "function", "mediaList", "synpred2_Css3", 
        "expr", "hexColor", "page", "syncToFollow", "namespaceName", "imports", 
        "selector", "synpred1_Css3", "cssClass", "typeSelector", "pseudo", 
        "selectorsGroup", "media", "nsPred", "declarations", "namespacePrefix", 
        "styleSheet", "function_name", "combinator", "attribute", "attrib", 
        "attrvalue", "cssId", "charSet", "property", "syncTo_IDENT_RBRACE"
    };
    public static final boolean[] decisionCanBacktrack = new boolean[] {
        false, // invalid decision
        false, false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, true, true, false, true, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:133:1: styleSheet : ( charSet )? ( WS )* ( imports ( WS )* )* bodylist EOF ;
    public final void styleSheet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "styleSheet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(133, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:134:5: ( ( charSet )? ( WS )* ( imports ( WS )* )* bodylist EOF )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:134:9: ( charSet )? ( WS )* ( imports ( WS )* )* bodylist EOF
            {
            dbg.location(134,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:134:9: ( charSet )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:134:9: charSet
                    {
                    dbg.location(134,9);
                    pushFollow(FOLLOW_charSet_in_styleSheet79);
                    charSet();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(1);}

            dbg.location(135,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:135:6: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:135:6: WS
            	    {
            	    dbg.location(135,6);
            	    match(input,WS,FOLLOW_WS_in_styleSheet87); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);
            } finally {dbg.exitSubRule(2);}

            dbg.location(136,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:136:9: ( imports ( WS )* )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:136:10: imports ( WS )*
            	    {
            	    dbg.location(136,10);
            	    pushFollow(FOLLOW_imports_in_styleSheet99);
            	    imports();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(136,18);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:136:18: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:136:18: WS
            	    	    {
            	    	    dbg.location(136,18);
            	    	    match(input,WS,FOLLOW_WS_in_styleSheet101); if (state.failed) return ;

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

            dbg.location(137,9);
            pushFollow(FOLLOW_bodylist_in_styleSheet116);
            bodylist();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(138,6);
            match(input,EOF,FOLLOW_EOF_in_styleSheet123); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(139, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:144:1: charSet : CHARSET_SYM ( WS )* STRING ( WS )* SEMI ;
    public final void charSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "charSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(144, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:145:5: ( CHARSET_SYM ( WS )* STRING ( WS )* SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:145:9: CHARSET_SYM ( WS )* STRING ( WS )* SEMI
            {
            dbg.location(145,9);
            match(input,CHARSET_SYM,FOLLOW_CHARSET_SYM_in_charSet149); if (state.failed) return ;
            dbg.location(145,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:145:21: ( WS )*
            try { dbg.enterSubRule(5);

            loop5:
            do {
                int alt5=2;
                try { dbg.enterDecision(5, decisionCanBacktrack[5]);

                int LA5_0 = input.LA(1);

                if ( (LA5_0==WS) ) {
                    alt5=1;
                }


                } finally {dbg.exitDecision(5);}

                switch (alt5) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:145:21: WS
            	    {
            	    dbg.location(145,21);
            	    match(input,WS,FOLLOW_WS_in_charSet151); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);
            } finally {dbg.exitSubRule(5);}

            dbg.location(145,25);
            match(input,STRING,FOLLOW_STRING_in_charSet154); if (state.failed) return ;
            dbg.location(145,32);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:145:32: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:145:32: WS
            	    {
            	    dbg.location(145,32);
            	    match(input,WS,FOLLOW_WS_in_charSet156); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);
            } finally {dbg.exitSubRule(6);}

            dbg.location(145,36);
            match(input,SEMI,FOLLOW_SEMI_in_charSet159); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(146, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:151:1: imports : IMPORT_SYM ( WS )* ( STRING | URI ) ( WS )* ( mediaList )? SEMI ;
    public final void imports() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "imports");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(151, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:152:5: ( IMPORT_SYM ( WS )* ( STRING | URI ) ( WS )* ( mediaList )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:152:9: IMPORT_SYM ( WS )* ( STRING | URI ) ( WS )* ( mediaList )? SEMI
            {
            dbg.location(152,9);
            match(input,IMPORT_SYM,FOLLOW_IMPORT_SYM_in_imports181); if (state.failed) return ;
            dbg.location(152,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:152:20: ( WS )*
            try { dbg.enterSubRule(7);

            loop7:
            do {
                int alt7=2;
                try { dbg.enterDecision(7, decisionCanBacktrack[7]);

                int LA7_0 = input.LA(1);

                if ( (LA7_0==WS) ) {
                    alt7=1;
                }


                } finally {dbg.exitDecision(7);}

                switch (alt7) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:152:20: WS
            	    {
            	    dbg.location(152,20);
            	    match(input,WS,FOLLOW_WS_in_imports183); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);
            } finally {dbg.exitSubRule(7);}

            dbg.location(152,24);
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

            dbg.location(152,37);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:152:37: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:152:37: WS
            	    {
            	    dbg.location(152,37);
            	    match(input,WS,FOLLOW_WS_in_imports192); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);
            } finally {dbg.exitSubRule(8);}

            dbg.location(152,41);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:152:41: ( mediaList )?
            int alt9=2;
            try { dbg.enterSubRule(9);
            try { dbg.enterDecision(9, decisionCanBacktrack[9]);

            int LA9_0 = input.LA(1);

            if ( ((LA9_0>=IDENT && LA9_0<=GEN)) ) {
                alt9=1;
            }
            } finally {dbg.exitDecision(9);}

            switch (alt9) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:152:41: mediaList
                    {
                    dbg.location(152,41);
                    pushFollow(FOLLOW_mediaList_in_imports195);
                    mediaList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(9);}

            dbg.location(152,52);
            match(input,SEMI,FOLLOW_SEMI_in_imports198); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(153, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:159:1: media : MEDIA_SYM ( WS )* mediaList LBRACE ( WS )* ruleSet ( WS )* RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(159, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:160:5: ( MEDIA_SYM ( WS )* mediaList LBRACE ( WS )* ruleSet ( WS )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:160:7: MEDIA_SYM ( WS )* mediaList LBRACE ( WS )* ruleSet ( WS )* RBRACE
            {
            dbg.location(160,7);
            match(input,MEDIA_SYM,FOLLOW_MEDIA_SYM_in_media219); if (state.failed) return ;
            dbg.location(160,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:160:17: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:160:17: WS
            	    {
            	    dbg.location(160,17);
            	    match(input,WS,FOLLOW_WS_in_media221); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);
            } finally {dbg.exitSubRule(10);}

            dbg.location(160,21);
            pushFollow(FOLLOW_mediaList_in_media224);
            mediaList();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(161,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_media234); if (state.failed) return ;
            dbg.location(161,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:161:16: ( WS )*
            try { dbg.enterSubRule(11);

            loop11:
            do {
                int alt11=2;
                try { dbg.enterDecision(11, decisionCanBacktrack[11]);

                int LA11_0 = input.LA(1);

                if ( (LA11_0==WS) ) {
                    alt11=1;
                }


                } finally {dbg.exitDecision(11);}

                switch (alt11) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:161:16: WS
            	    {
            	    dbg.location(161,16);
            	    match(input,WS,FOLLOW_WS_in_media236); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);
            } finally {dbg.exitSubRule(11);}

            dbg.location(162,13);
            pushFollow(FOLLOW_ruleSet_in_media251);
            ruleSet();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(163,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:163:9: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:163:9: WS
            	    {
            	    dbg.location(163,9);
            	    match(input,WS,FOLLOW_WS_in_media261); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop12;
                }
            } while (true);
            } finally {dbg.exitSubRule(12);}

            dbg.location(163,13);
            match(input,RBRACE,FOLLOW_RBRACE_in_media264); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(164, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:166:1: mediaList : medium ( COMMA ( WS )* medium )* ;
    public final void mediaList() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaList");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(166, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:167:9: ( medium ( COMMA ( WS )* medium )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:167:11: medium ( COMMA ( WS )* medium )*
            {
            dbg.location(167,11);
            pushFollow(FOLLOW_medium_in_mediaList285);
            medium();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(167,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:167:18: ( COMMA ( WS )* medium )*
            try { dbg.enterSubRule(14);

            loop14:
            do {
                int alt14=2;
                try { dbg.enterDecision(14, decisionCanBacktrack[14]);

                int LA14_0 = input.LA(1);

                if ( (LA14_0==COMMA) ) {
                    alt14=1;
                }


                } finally {dbg.exitDecision(14);}

                switch (alt14) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:167:19: COMMA ( WS )* medium
            	    {
            	    dbg.location(167,19);
            	    match(input,COMMA,FOLLOW_COMMA_in_mediaList288); if (state.failed) return ;
            	    dbg.location(167,25);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:167:25: ( WS )*
            	    try { dbg.enterSubRule(13);

            	    loop13:
            	    do {
            	        int alt13=2;
            	        try { dbg.enterDecision(13, decisionCanBacktrack[13]);

            	        int LA13_0 = input.LA(1);

            	        if ( (LA13_0==WS) ) {
            	            alt13=1;
            	        }


            	        } finally {dbg.exitDecision(13);}

            	        switch (alt13) {
            	    	case 1 :
            	    	    dbg.enterAlt(1);

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:167:25: WS
            	    	    {
            	    	    dbg.location(167,25);
            	    	    match(input,WS,FOLLOW_WS_in_mediaList290); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop13;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(13);}

            	    dbg.location(167,29);
            	    pushFollow(FOLLOW_medium_in_mediaList293);
            	    medium();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);
            } finally {dbg.exitSubRule(14);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(168, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:173:1: medium : ( IDENT | GEN ) ( WS )* ;
    public final void medium() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "medium");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(173, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:174:5: ( ( IDENT | GEN ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:174:7: ( IDENT | GEN ) ( WS )*
            {
            dbg.location(174,7);
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

            dbg.location(174,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:174:23: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:174:23: WS
            	    {
            	    dbg.location(174,23);
            	    match(input,WS,FOLLOW_WS_in_medium322); if (state.failed) return ;

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
        dbg.location(175, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:178:1: bodylist : ( bodyset )* ;
    public final void bodylist() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodylist");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(178, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:179:5: ( ( bodyset )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:179:7: ( bodyset )*
            {
            dbg.location(179,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:179:7: ( bodyset )*
            try { dbg.enterSubRule(16);

            loop16:
            do {
                int alt16=2;
                try { dbg.enterDecision(16, decisionCanBacktrack[16]);

                int LA16_0 = input.LA(1);

                if ( (LA16_0==MEDIA_SYM||(LA16_0>=IDENT && LA16_0<=COLON)||(LA16_0>=STAR && LA16_0<=LBRACKET)||LA16_0==82) ) {
                    alt16=1;
                }


                } finally {dbg.exitDecision(16);}

                switch (alt16) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:179:7: bodyset
            	    {
            	    dbg.location(179,7);
            	    pushFollow(FOLLOW_bodyset_in_bodylist345);
            	    bodyset();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);
            } finally {dbg.exitSubRule(16);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(180, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:182:1: bodyset : ( ruleSet | media | page ) ( WS )* ;
    public final void bodyset() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyset");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(182, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:5: ( ( ruleSet | media | page ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:7: ( ruleSet | media | page ) ( WS )*
            {
            dbg.location(183,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:7: ( ruleSet | media | page )
            int alt17=3;
            try { dbg.enterSubRule(17);
            try { dbg.enterDecision(17, decisionCanBacktrack[17]);

            switch ( input.LA(1) ) {
            case IDENT:
            case GEN:
            case COLON:
            case STAR:
            case HASH:
            case DOT:
            case LBRACKET:
            case 82:
                {
                alt17=1;
                }
                break;
            case MEDIA_SYM:
                {
                alt17=2;
                }
                break;
            case PAGE_SYM:
                {
                alt17=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(17);}

            switch (alt17) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:184:6: ruleSet
                    {
                    dbg.location(184,6);
                    pushFollow(FOLLOW_ruleSet_in_bodyset374);
                    ruleSet();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:185:11: media
                    {
                    dbg.location(185,11);
                    pushFollow(FOLLOW_media_in_bodyset386);
                    media();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:186:11: page
                    {
                    dbg.location(186,11);
                    pushFollow(FOLLOW_page_in_bodyset398);
                    page();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(17);}

            dbg.location(188,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:188:7: ( WS )*
            try { dbg.enterSubRule(18);

            loop18:
            do {
                int alt18=2;
                try { dbg.enterDecision(18, decisionCanBacktrack[18]);

                int LA18_0 = input.LA(1);

                if ( (LA18_0==WS) ) {
                    alt18=1;
                }


                } finally {dbg.exitDecision(18);}

                switch (alt18) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:188:7: WS
            	    {
            	    dbg.location(188,7);
            	    match(input,WS,FOLLOW_WS_in_bodyset414); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop18;
                }
            } while (true);
            } finally {dbg.exitSubRule(18);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(189, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:191:1: page : PAGE_SYM ( WS )? ( pseudoPage ( WS )* )? LBRACE ( WS )* declaration SEMI ( declaration SEMI )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(191, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:192:5: ( PAGE_SYM ( WS )? ( pseudoPage ( WS )* )? LBRACE ( WS )* declaration SEMI ( declaration SEMI )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:192:7: PAGE_SYM ( WS )? ( pseudoPage ( WS )* )? LBRACE ( WS )* declaration SEMI ( declaration SEMI )* RBRACE
            {
            dbg.location(192,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page436); if (state.failed) return ;
            dbg.location(192,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:192:16: ( WS )?
            int alt19=2;
            try { dbg.enterSubRule(19);
            try { dbg.enterDecision(19, decisionCanBacktrack[19]);

            int LA19_0 = input.LA(1);

            if ( (LA19_0==WS) ) {
                alt19=1;
            }
            } finally {dbg.exitDecision(19);}

            switch (alt19) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:192:16: WS
                    {
                    dbg.location(192,16);
                    match(input,WS,FOLLOW_WS_in_page438); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(19);}

            dbg.location(192,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:192:20: ( pseudoPage ( WS )* )?
            int alt21=2;
            try { dbg.enterSubRule(21);
            try { dbg.enterDecision(21, decisionCanBacktrack[21]);

            int LA21_0 = input.LA(1);

            if ( (LA21_0==COLON) ) {
                alt21=1;
            }
            } finally {dbg.exitDecision(21);}

            switch (alt21) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:192:21: pseudoPage ( WS )*
                    {
                    dbg.location(192,21);
                    pushFollow(FOLLOW_pseudoPage_in_page442);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(192,32);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:192:32: ( WS )*
                    try { dbg.enterSubRule(20);

                    loop20:
                    do {
                        int alt20=2;
                        try { dbg.enterDecision(20, decisionCanBacktrack[20]);

                        int LA20_0 = input.LA(1);

                        if ( (LA20_0==WS) ) {
                            alt20=1;
                        }


                        } finally {dbg.exitDecision(20);}

                        switch (alt20) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:192:32: WS
                    	    {
                    	    dbg.location(192,32);
                    	    match(input,WS,FOLLOW_WS_in_page444); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop20;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(20);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(21);}

            dbg.location(193,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page457); if (state.failed) return ;
            dbg.location(193,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:193:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:193:16: WS
            	    {
            	    dbg.location(193,16);
            	    match(input,WS,FOLLOW_WS_in_page459); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop22;
                }
            } while (true);
            } finally {dbg.exitSubRule(22);}

            dbg.location(194,13);
            pushFollow(FOLLOW_declaration_in_page474);
            declaration();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(194,25);
            match(input,SEMI,FOLLOW_SEMI_in_page476); if (state.failed) return ;
            dbg.location(194,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:194:30: ( declaration SEMI )*
            try { dbg.enterSubRule(23);

            loop23:
            do {
                int alt23=2;
                try { dbg.enterDecision(23, decisionCanBacktrack[23]);

                int LA23_0 = input.LA(1);

                if ( ((LA23_0>=IDENT && LA23_0<=GEN)) ) {
                    alt23=1;
                }


                } finally {dbg.exitDecision(23);}

                switch (alt23) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:194:31: declaration SEMI
            	    {
            	    dbg.location(194,31);
            	    pushFollow(FOLLOW_declaration_in_page479);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(194,43);
            	    match(input,SEMI,FOLLOW_SEMI_in_page481); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop23;
                }
            } while (true);
            } finally {dbg.exitSubRule(23);}

            dbg.location(195,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page493); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(196, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:198:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(198, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:199:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:199:7: COLON IDENT
            {
            dbg.location(199,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage514); if (state.failed) return ;
            dbg.location(199,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage516); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(200, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:202:1: operator : ( SOLIDUS ( WS )* | COMMA ( WS )* | );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(202, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:203:5: ( SOLIDUS ( WS )* | COMMA ( WS )* | )
            int alt26=3;
            try { dbg.enterDecision(26, decisionCanBacktrack[26]);

            switch ( input.LA(1) ) {
            case SOLIDUS:
                {
                alt26=1;
                }
                break;
            case COMMA:
                {
                alt26=2;
                }
                break;
            case STRING:
            case URI:
            case IDENT:
            case GEN:
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
                alt26=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(26);}

            switch (alt26) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:203:7: SOLIDUS ( WS )*
                    {
                    dbg.location(203,7);
                    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_operator537); if (state.failed) return ;
                    dbg.location(203,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:203:15: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:203:15: WS
                    	    {
                    	    dbg.location(203,15);
                    	    match(input,WS,FOLLOW_WS_in_operator539); if (state.failed) return ;

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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:204:7: COMMA ( WS )*
                    {
                    dbg.location(204,7);
                    match(input,COMMA,FOLLOW_COMMA_in_operator548); if (state.failed) return ;
                    dbg.location(204,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:204:13: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:204:13: WS
                    	    {
                    	    dbg.location(204,13);
                    	    match(input,WS,FOLLOW_WS_in_operator550); if (state.failed) return ;

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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:206:5: 
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
        dbg.location(206, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:208:1: combinator : ( PLUS ( WS )* | GREATER ( WS )* | TILDE ( WS )* | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(208, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:209:5: ( PLUS ( WS )* | GREATER ( WS )* | TILDE ( WS )* | )
            int alt30=4;
            try { dbg.enterDecision(30, decisionCanBacktrack[30]);

            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt30=1;
                }
                break;
            case GREATER:
                {
                alt30=2;
                }
                break;
            case TILDE:
                {
                alt30=3;
                }
                break;
            case IDENT:
            case GEN:
            case COLON:
            case STAR:
            case HASH:
            case DOT:
            case LBRACKET:
            case 82:
                {
                alt30=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(30);}

            switch (alt30) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:209:7: PLUS ( WS )*
                    {
                    dbg.location(209,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator578); if (state.failed) return ;
                    dbg.location(209,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:209:12: ( WS )*
                    try { dbg.enterSubRule(27);

                    loop27:
                    do {
                        int alt27=2;
                        try { dbg.enterDecision(27, decisionCanBacktrack[27]);

                        int LA27_0 = input.LA(1);

                        if ( (LA27_0==WS) ) {
                            alt27=1;
                        }


                        } finally {dbg.exitDecision(27);}

                        switch (alt27) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:209:12: WS
                    	    {
                    	    dbg.location(209,12);
                    	    match(input,WS,FOLLOW_WS_in_combinator580); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop27;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(27);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:210:7: GREATER ( WS )*
                    {
                    dbg.location(210,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator589); if (state.failed) return ;
                    dbg.location(210,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:210:15: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:210:15: WS
                    	    {
                    	    dbg.location(210,15);
                    	    match(input,WS,FOLLOW_WS_in_combinator591); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop28;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(28);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:211:7: TILDE ( WS )*
                    {
                    dbg.location(211,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator600); if (state.failed) return ;
                    dbg.location(211,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:211:13: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:211:13: WS
                    	    {
                    	    dbg.location(211,13);
                    	    match(input,WS,FOLLOW_WS_in_combinator602); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop29;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(29);}


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:213:5: 
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
        dbg.location(213, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:215:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(215, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:216:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(216,5);
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
        dbg.location(218, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:220:1: property : ( IDENT | GEN ) ( WS )* ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(220, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:221:5: ( ( IDENT | GEN ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:221:7: ( IDENT | GEN ) ( WS )*
            {
            dbg.location(221,7);
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

            dbg.location(221,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:221:21: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:221:21: WS
            	    {
            	    dbg.location(221,21);
            	    match(input,WS,FOLLOW_WS_in_property670); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop31;
                }
            } while (true);
            } finally {dbg.exitSubRule(31);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(222, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:224:1: ruleSet : selectorsGroup LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void ruleSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ruleSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(224, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:225:5: ( selectorsGroup LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:225:9: selectorsGroup LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(225,9);
            pushFollow(FOLLOW_selectorsGroup_in_ruleSet695);
            selectorsGroup();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(226,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_ruleSet705); if (state.failed) return ;
            dbg.location(226,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:226:16: ( WS )*
            try { dbg.enterSubRule(32);

            loop32:
            do {
                int alt32=2;
                try { dbg.enterDecision(32, decisionCanBacktrack[32]);

                int LA32_0 = input.LA(1);

                if ( (LA32_0==WS) ) {
                    alt32=1;
                }


                } finally {dbg.exitDecision(32);}

                switch (alt32) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:226:16: WS
            	    {
            	    dbg.location(226,16);
            	    match(input,WS,FOLLOW_WS_in_ruleSet707); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop32;
                }
            } while (true);
            } finally {dbg.exitSubRule(32);}

            dbg.location(226,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_ruleSet710);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(227,13);
            pushFollow(FOLLOW_declarations_in_ruleSet724);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(228,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_ruleSet734); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(229, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:231:1: declarations : ( declaration )? ( SEMI ( WS )* ( declaration )? )* ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(231, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:232:5: ( ( declaration )? ( SEMI ( WS )* ( declaration )? )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:234:9: ( declaration )? ( SEMI ( WS )* ( declaration )? )*
            {
            dbg.location(234,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:234:9: ( declaration )?
            int alt33=2;
            try { dbg.enterSubRule(33);
            try { dbg.enterDecision(33, decisionCanBacktrack[33]);

            int LA33_0 = input.LA(1);

            if ( ((LA33_0>=IDENT && LA33_0<=GEN)) ) {
                alt33=1;
            }
            } finally {dbg.exitDecision(33);}

            switch (alt33) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:234:9: declaration
                    {
                    dbg.location(234,9);
                    pushFollow(FOLLOW_declaration_in_declarations772);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(33);}

            dbg.location(234,22);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:234:22: ( SEMI ( WS )* ( declaration )? )*
            try { dbg.enterSubRule(36);

            loop36:
            do {
                int alt36=2;
                try { dbg.enterDecision(36, decisionCanBacktrack[36]);

                int LA36_0 = input.LA(1);

                if ( (LA36_0==SEMI) ) {
                    alt36=1;
                }


                } finally {dbg.exitDecision(36);}

                switch (alt36) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:234:23: SEMI ( WS )* ( declaration )?
            	    {
            	    dbg.location(234,23);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations776); if (state.failed) return ;
            	    dbg.location(234,28);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:234:28: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:234:28: WS
            	    	    {
            	    	    dbg.location(234,28);
            	    	    match(input,WS,FOLLOW_WS_in_declarations778); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop34;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(34);}

            	    dbg.location(234,32);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:234:32: ( declaration )?
            	    int alt35=2;
            	    try { dbg.enterSubRule(35);
            	    try { dbg.enterDecision(35, decisionCanBacktrack[35]);

            	    int LA35_0 = input.LA(1);

            	    if ( ((LA35_0>=IDENT && LA35_0<=GEN)) ) {
            	        alt35=1;
            	    }
            	    } finally {dbg.exitDecision(35);}

            	    switch (alt35) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:234:32: declaration
            	            {
            	            dbg.location(234,32);
            	            pushFollow(FOLLOW_declaration_in_declarations781);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(35);}


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
        dbg.location(235, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:237:1: selectorsGroup : selector ( COMMA ( WS )* selector )* ;
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(237, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:238:5: ( selector ( COMMA ( WS )* selector )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:238:7: selector ( COMMA ( WS )* selector )*
            {
            dbg.location(238,7);
            pushFollow(FOLLOW_selector_in_selectorsGroup805);
            selector();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(238,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:238:16: ( COMMA ( WS )* selector )*
            try { dbg.enterSubRule(38);

            loop38:
            do {
                int alt38=2;
                try { dbg.enterDecision(38, decisionCanBacktrack[38]);

                int LA38_0 = input.LA(1);

                if ( (LA38_0==COMMA) ) {
                    alt38=1;
                }


                } finally {dbg.exitDecision(38);}

                switch (alt38) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:238:17: COMMA ( WS )* selector
            	    {
            	    dbg.location(238,17);
            	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup808); if (state.failed) return ;
            	    dbg.location(238,23);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:238:23: ( WS )*
            	    try { dbg.enterSubRule(37);

            	    loop37:
            	    do {
            	        int alt37=2;
            	        try { dbg.enterDecision(37, decisionCanBacktrack[37]);

            	        int LA37_0 = input.LA(1);

            	        if ( (LA37_0==WS) ) {
            	            alt37=1;
            	        }


            	        } finally {dbg.exitDecision(37);}

            	        switch (alt37) {
            	    	case 1 :
            	    	    dbg.enterAlt(1);

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:238:23: WS
            	    	    {
            	    	    dbg.location(238,23);
            	    	    match(input,WS,FOLLOW_WS_in_selectorsGroup810); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop37;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(37);}

            	    dbg.location(238,27);
            	    pushFollow(FOLLOW_selector_in_selectorsGroup813);
            	    selector();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop38;
                }
            } while (true);
            } finally {dbg.exitSubRule(38);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(239, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:241:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(241, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:242:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:242:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(242,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector836);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(242,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:242:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(39);

            loop39:
            do {
                int alt39=2;
                try { dbg.enterDecision(39, decisionCanBacktrack[39]);

                int LA39_0 = input.LA(1);

                if ( ((LA39_0>=IDENT && LA39_0<=GEN)||LA39_0==COLON||(LA39_0>=PLUS && LA39_0<=TILDE)||(LA39_0>=STAR && LA39_0<=LBRACKET)||LA39_0==82) ) {
                    alt39=1;
                }


                } finally {dbg.exitDecision(39);}

                switch (alt39) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:242:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(242,31);
            	    pushFollow(FOLLOW_combinator_in_selector839);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(242,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector841);
            	    simpleSelectorSequence();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop39;
                }
            } while (true);
            } finally {dbg.exitSubRule(39);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(243, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:246:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(246, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:248:2: ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) )
            int alt42=2;
            try { dbg.enterDecision(42, decisionCanBacktrack[42]);

            int LA42_0 = input.LA(1);

            if ( ((LA42_0>=IDENT && LA42_0<=GEN)||LA42_0==STAR||LA42_0==82) ) {
                alt42=1;
            }
            else if ( (LA42_0==COLON||(LA42_0>=HASH && LA42_0<=LBRACKET)) ) {
                alt42=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 42, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(42);}

            switch (alt42) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:252:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    {
                    dbg.location(252,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:252:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:252:5: typeSelector ( ( esPred )=> elementSubsequent )*
                    {
                    dbg.location(252,5);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence881);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(252,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:252:18: ( ( esPred )=> elementSubsequent )*
                    try { dbg.enterSubRule(40);

                    loop40:
                    do {
                        int alt40=2;
                        try { dbg.enterDecision(40, decisionCanBacktrack[40]);

                        try {
                            isCyclicDecision = true;
                            alt40 = dfa40.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(40);}

                        switch (alt40) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:252:19: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(252,29);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence888);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop40;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(40);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:254:2: ( ( ( esPred )=> elementSubsequent )+ )
                    {
                    dbg.location(254,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:254:2: ( ( ( esPred )=> elementSubsequent )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:254:4: ( ( esPred )=> elementSubsequent )+
                    {
                    dbg.location(254,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:254:4: ( ( esPred )=> elementSubsequent )+
                    int cnt41=0;
                    try { dbg.enterSubRule(41);

                    loop41:
                    do {
                        int alt41=2;
                        try { dbg.enterDecision(41, decisionCanBacktrack[41]);

                        switch ( input.LA(1) ) {
                        case HASH:
                            {
                            int LA41_2 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt41=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA41_3 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt41=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA41_4 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt41=1;
                            }


                            }
                            break;
                        case COLON:
                            {
                            int LA41_5 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt41=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(41);}

                        switch (alt41) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:254:5: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(254,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence906);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt41 >= 1 ) break loop41;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(41, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt41++;
                    } while (true);
                    } finally {dbg.exitSubRule(41);}


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
        dbg.location(255, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:1: typeSelector options {k=2; } : ( ( nsPred )=> namespacePrefix )? ( elementName ( WS )* ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(265, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:3: ( ( ( nsPred )=> namespacePrefix )? ( elementName ( WS )* ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:6: ( ( nsPred )=> namespacePrefix )? ( elementName ( WS )* )
            {
            dbg.location(267,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:6: ( ( nsPred )=> namespacePrefix )?
            int alt43=2;
            try { dbg.enterSubRule(43);
            try { dbg.enterDecision(43, decisionCanBacktrack[43]);

            int LA43_0 = input.LA(1);

            if ( (LA43_0==IDENT||LA43_0==STAR) ) {
                int LA43_1 = input.LA(2);

                if ( (synpred3_Css3()) ) {
                    alt43=1;
                }
            }
            else if ( (LA43_0==82) && (synpred3_Css3())) {
                alt43=1;
            }
            } finally {dbg.exitDecision(43);}

            switch (alt43) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:7: ( nsPred )=> namespacePrefix
                    {
                    dbg.location(267,17);
                    pushFollow(FOLLOW_namespacePrefix_in_typeSelector948);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(43);}

            dbg.location(267,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:35: ( elementName ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:37: elementName ( WS )*
            {
            dbg.location(267,37);
            pushFollow(FOLLOW_elementName_in_typeSelector954);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(267,49);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:49: ( WS )*
            try { dbg.enterSubRule(44);

            loop44:
            do {
                int alt44=2;
                try { dbg.enterDecision(44, decisionCanBacktrack[44]);

                int LA44_0 = input.LA(1);

                if ( (LA44_0==WS) ) {
                    alt44=1;
                }


                } finally {dbg.exitDecision(44);}

                switch (alt44) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:49: WS
            	    {
            	    dbg.location(267,49);
            	    match(input,WS,FOLLOW_WS_in_typeSelector956); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop44;
                }
            } while (true);
            } finally {dbg.exitSubRule(44);}


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(268, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:276:2: nsPred : ( IDENT | STAR ) '|' ;
    public final void nsPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "nsPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(276, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:277:3: ( ( IDENT | STAR ) '|' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:278:3: ( IDENT | STAR ) '|'
            {
            dbg.location(278,3);
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

            dbg.location(278,18);
            match(input,82,FOLLOW_82_in_nsPred994); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(279, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:281:2: namespacePrefix : ( namespaceName ( WS )* )? '|' ;
    public final void namespacePrefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(281, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:282:3: ( ( namespaceName ( WS )* )? '|' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:282:5: ( namespaceName ( WS )* )? '|'
            {
            dbg.location(282,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:282:5: ( namespaceName ( WS )* )?
            int alt46=2;
            try { dbg.enterSubRule(46);
            try { dbg.enterDecision(46, decisionCanBacktrack[46]);

            int LA46_0 = input.LA(1);

            if ( (LA46_0==IDENT||LA46_0==STAR) ) {
                alt46=1;
            }
            } finally {dbg.exitDecision(46);}

            switch (alt46) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:282:7: namespaceName ( WS )*
                    {
                    dbg.location(282,7);
                    pushFollow(FOLLOW_namespaceName_in_namespacePrefix1014);
                    namespaceName();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(282,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:282:21: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:282:21: WS
                    	    {
                    	    dbg.location(282,21);
                    	    match(input,WS,FOLLOW_WS_in_namespacePrefix1016); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop45;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(45);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(46);}

            dbg.location(282,28);
            match(input,82,FOLLOW_82_in_namespacePrefix1022); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(283, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:285:2: namespaceName : ( IDENT | STAR );
    public final void namespaceName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespaceName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(285, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:286:3: ( IDENT | STAR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(286,3);
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
        dbg.location(287, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:289:1: esPred : ( HASH | DOT | LBRACKET | COLON );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(289, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:290:5: ( HASH | DOT | LBRACKET | COLON )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(290,5);
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
        dbg.location(291, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:293:1: elementSubsequent : ( cssId | cssClass | attrib | pseudo ) ( WS )* ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(293, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:294:5: ( ( cssId | cssClass | attrib | pseudo ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:295:5: ( cssId | cssClass | attrib | pseudo ) ( WS )*
            {
            dbg.location(295,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:295:5: ( cssId | cssClass | attrib | pseudo )
            int alt47=4;
            try { dbg.enterSubRule(47);
            try { dbg.enterDecision(47, decisionCanBacktrack[47]);

            switch ( input.LA(1) ) {
            case HASH:
                {
                alt47=1;
                }
                break;
            case DOT:
                {
                alt47=2;
                }
                break;
            case LBRACKET:
                {
                alt47=3;
                }
                break;
            case COLON:
                {
                alt47=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(47);}

            switch (alt47) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:296:6: cssId
                    {
                    dbg.location(296,6);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent1116);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:297:8: cssClass
                    {
                    dbg.location(297,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent1125);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:298:11: attrib
                    {
                    dbg.location(298,11);
                    pushFollow(FOLLOW_attrib_in_elementSubsequent1137);
                    attrib();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:299:11: pseudo
                    {
                    dbg.location(299,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent1149);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(47);}

            dbg.location(301,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:301:5: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:301:5: WS
            	    {
            	    dbg.location(301,5);
            	    match(input,WS,FOLLOW_WS_in_elementSubsequent1161); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop48;
                }
            } while (true);
            } finally {dbg.exitSubRule(48);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(302, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:304:1: cssId : HASH ;
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(304, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:305:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:305:7: HASH
            {
            dbg.location(305,7);
            match(input,HASH,FOLLOW_HASH_in_cssId1183); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(306, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(308, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:309:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:309:7: DOT ( IDENT | GEN )
            {
            dbg.location(309,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass1200); if (state.failed) return ;
            dbg.location(309,11);
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


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(310, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:313:1: elementName : ( ( IDENT | GEN ) | '*' );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(313, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:314:5: ( ( IDENT | GEN ) | '*' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(314,5);
            if ( (input.LA(1)>=IDENT && input.LA(1)<=GEN)||input.LA(1)==STAR ) {
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
        dbg.location(315, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:317:1: attrib : LBRACKET ( namespacePrefix )? ( WS )* IDENT ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* ( IDENT | STRING ) ( WS )* )? RBRACKET ;
    public final void attrib() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrib");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(317, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:318:5: ( LBRACKET ( namespacePrefix )? ( WS )* IDENT ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* ( IDENT | STRING ) ( WS )* )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:318:7: LBRACKET ( namespacePrefix )? ( WS )* IDENT ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* ( IDENT | STRING ) ( WS )* )? RBRACKET
            {
            dbg.location(318,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_attrib1266); if (state.failed) return ;
            dbg.location(319,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:6: ( namespacePrefix )?
            int alt49=2;
            try { dbg.enterSubRule(49);
            try { dbg.enterDecision(49, decisionCanBacktrack[49]);

            try {
                isCyclicDecision = true;
                alt49 = dfa49.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(49);}

            switch (alt49) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:6: namespacePrefix
                    {
                    dbg.location(319,6);
                    pushFollow(FOLLOW_namespacePrefix_in_attrib1273);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(49);}

            dbg.location(319,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:23: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:23: WS
            	    {
            	    dbg.location(319,23);
            	    match(input,WS,FOLLOW_WS_in_attrib1276); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop50;
                }
            } while (true);
            } finally {dbg.exitSubRule(50);}

            dbg.location(320,9);
            match(input,IDENT,FOLLOW_IDENT_in_attrib1287); if (state.failed) return ;
            dbg.location(320,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:15: ( WS )*
            try { dbg.enterSubRule(51);

            loop51:
            do {
                int alt51=2;
                try { dbg.enterDecision(51, decisionCanBacktrack[51]);

                int LA51_0 = input.LA(1);

                if ( (LA51_0==WS) ) {
                    alt51=1;
                }


                } finally {dbg.exitDecision(51);}

                switch (alt51) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:15: WS
            	    {
            	    dbg.location(320,15);
            	    match(input,WS,FOLLOW_WS_in_attrib1289); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop51;
                }
            } while (true);
            } finally {dbg.exitSubRule(51);}

            dbg.location(322,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:322:13: ( ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* ( IDENT | STRING ) ( WS )* )?
            int alt54=2;
            try { dbg.enterSubRule(54);
            try { dbg.enterDecision(54, decisionCanBacktrack[54]);

            int LA54_0 = input.LA(1);

            if ( ((LA54_0>=OPEQ && LA54_0<=DASHMATCH)) ) {
                alt54=1;
            }
            } finally {dbg.exitDecision(54);}

            switch (alt54) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:17: ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* ( IDENT | STRING ) ( WS )*
                    {
                    dbg.location(323,17);
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

                    dbg.location(328,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:328:17: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:328:17: WS
                    	    {
                    	    dbg.location(328,17);
                    	    match(input,WS,FOLLOW_WS_in_attrib1439); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop52;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(52);}

                    dbg.location(329,17);
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

                    dbg.location(333,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:17: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:17: WS
                    	    {
                    	    dbg.location(333,17);
                    	    match(input,WS,FOLLOW_WS_in_attrib1542); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop53;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(53);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(54);}

            dbg.location(336,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_attrib1571); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(337, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:339:1: pseudo : COLON ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )? ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(339, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:340:5: ( COLON ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:340:7: COLON ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )?
            {
            dbg.location(340,7);
            match(input,COLON,FOLLOW_COLON_in_pseudo1584); if (state.failed) return ;
            dbg.location(341,13);
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

            dbg.location(342,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:342:17: ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )?
            int alt59=2;
            try { dbg.enterSubRule(59);
            try { dbg.enterDecision(59, decisionCanBacktrack[59]);

            try {
                isCyclicDecision = true;
                alt59 = dfa59.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(59);}

            switch (alt59) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:343:21: ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN
                    {
                    dbg.location(343,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:343:21: ( WS )*
                    try { dbg.enterSubRule(55);

                    loop55:
                    do {
                        int alt55=2;
                        try { dbg.enterDecision(55, decisionCanBacktrack[55]);

                        int LA55_0 = input.LA(1);

                        if ( (LA55_0==WS) ) {
                            alt55=1;
                        }


                        } finally {dbg.exitDecision(55);}

                        switch (alt55) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:343:21: WS
                    	    {
                    	    dbg.location(343,21);
                    	    match(input,WS,FOLLOW_WS_in_pseudo1648); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop55;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(55);}

                    dbg.location(343,25);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo1651); if (state.failed) return ;
                    dbg.location(343,32);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:343:32: ( WS )*
                    try { dbg.enterSubRule(56);

                    loop56:
                    do {
                        int alt56=2;
                        try { dbg.enterDecision(56, decisionCanBacktrack[56]);

                        int LA56_0 = input.LA(1);

                        if ( (LA56_0==WS) ) {
                            alt56=1;
                        }


                        } finally {dbg.exitDecision(56);}

                        switch (alt56) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:343:32: WS
                    	    {
                    	    dbg.location(343,32);
                    	    match(input,WS,FOLLOW_WS_in_pseudo1653); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop56;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(56);}

                    dbg.location(343,36);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:343:36: ( ( IDENT | GEN ) ( WS )* )?
                    int alt58=2;
                    try { dbg.enterSubRule(58);
                    try { dbg.enterDecision(58, decisionCanBacktrack[58]);

                    int LA58_0 = input.LA(1);

                    if ( ((LA58_0>=IDENT && LA58_0<=GEN)) ) {
                        alt58=1;
                    }
                    } finally {dbg.exitDecision(58);}

                    switch (alt58) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:343:37: ( IDENT | GEN ) ( WS )*
                            {
                            dbg.location(343,37);
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

                            dbg.location(343,53);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:343:53: ( WS )*
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

                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:343:53: WS
                            	    {
                            	    dbg.location(343,53);
                            	    match(input,WS,FOLLOW_WS_in_pseudo1667); if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop57;
                                }
                            } while (true);
                            } finally {dbg.exitSubRule(57);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(58);}

                    dbg.location(343,59);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo1672); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(59);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(345, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:1: declaration : property COLON ( WS )* expr ( prio )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(347, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:5: ( property COLON ( WS )* expr ( prio )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:5: property COLON ( WS )* expr ( prio )?
            {
            dbg.location(350,5);
            pushFollow(FOLLOW_property_in_declaration1718);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(350,14);
            match(input,COLON,FOLLOW_COLON_in_declaration1720); if (state.failed) return ;
            dbg.location(350,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:20: ( WS )*
            try { dbg.enterSubRule(60);

            loop60:
            do {
                int alt60=2;
                try { dbg.enterDecision(60, decisionCanBacktrack[60]);

                int LA60_0 = input.LA(1);

                if ( (LA60_0==WS) ) {
                    alt60=1;
                }


                } finally {dbg.exitDecision(60);}

                switch (alt60) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:20: WS
            	    {
            	    dbg.location(350,20);
            	    match(input,WS,FOLLOW_WS_in_declaration1722); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop60;
                }
            } while (true);
            } finally {dbg.exitSubRule(60);}

            dbg.location(350,24);
            pushFollow(FOLLOW_expr_in_declaration1725);
            expr();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(350,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:29: ( prio )?
            int alt61=2;
            try { dbg.enterSubRule(61);
            try { dbg.enterDecision(61, decisionCanBacktrack[61]);

            int LA61_0 = input.LA(1);

            if ( (LA61_0==IMPORTANT_SYM) ) {
                alt61=1;
            }
            } finally {dbg.exitDecision(61);}

            switch (alt61) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:29: prio
                    {
                    dbg.location(350,29);
                    pushFollow(FOLLOW_prio_in_declaration1727);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(61);}


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
        dbg.location(351, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "declaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "declaration"


    // $ANTLR start "syncTo_IDENT_RBRACE"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:361:1: syncTo_IDENT_RBRACE : ;
    public final void syncTo_IDENT_RBRACE() throws RecognitionException {

                syncToSet(BitSet.of(IDENT, RBRACE));
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_IDENT_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(361, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:366:6: 
            {
            }

        }
        finally {
        }
        dbg.location(366, 6);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "syncTo_IDENT_RBRACE");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "syncTo_IDENT_RBRACE"


    // $ANTLR start "syncToFollow"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(369, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:373:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:374:6: 
            {
            }

        }
        finally {
        }
        dbg.location(374, 6);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "syncToFollow");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "syncToFollow"


    // $ANTLR start "prio"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:1: prio : IMPORTANT_SYM ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(377, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:378:5: ( IMPORTANT_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:378:7: IMPORTANT_SYM
            {
            dbg.location(378,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio1820); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(379, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:1: expr : term ( operator term )* ;
    public final void expr() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expr");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(381, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:5: ( term ( operator term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:7: term ( operator term )*
            {
            dbg.location(382,7);
            pushFollow(FOLLOW_term_in_expr1841);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(382,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:12: ( operator term )*
            try { dbg.enterSubRule(62);

            loop62:
            do {
                int alt62=2;
                try { dbg.enterDecision(62, decisionCanBacktrack[62]);

                try {
                    isCyclicDecision = true;
                    alt62 = dfa62.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(62);}

                switch (alt62) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:13: operator term
            	    {
            	    dbg.location(382,13);
            	    pushFollow(FOLLOW_operator_in_expr1844);
            	    operator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(382,22);
            	    pushFollow(FOLLOW_term_in_expr1846);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

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
        dbg.location(383, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:1: term : ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | GEN | URI | hexColor | function ) ( WS )* ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(385, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:386:5: ( ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | GEN | URI | hexColor | function ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:386:7: ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | GEN | URI | hexColor | function ) ( WS )*
            {
            dbg.location(386,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:386:7: ( unaryOperator )?
            int alt63=2;
            try { dbg.enterSubRule(63);
            try { dbg.enterDecision(63, decisionCanBacktrack[63]);

            int LA63_0 = input.LA(1);

            if ( (LA63_0==PLUS||LA63_0==MINUS) ) {
                alt63=1;
            }
            } finally {dbg.exitDecision(63);}

            switch (alt63) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:386:7: unaryOperator
                    {
                    dbg.location(386,7);
                    pushFollow(FOLLOW_unaryOperator_in_term1869);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(63);}

            dbg.location(387,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:387:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | GEN | URI | hexColor | function )
            int alt64=7;
            try { dbg.enterSubRule(64);
            try { dbg.enterDecision(64, decisionCanBacktrack[64]);

            try {
                isCyclicDecision = true;
                alt64 = dfa64.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(64);}

            switch (alt64) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:388:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ )
                    {
                    dbg.location(388,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:398:7: STRING
                    {
                    dbg.location(398,7);
                    match(input,STRING,FOLLOW_STRING_in_term2036); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:399:7: IDENT
                    {
                    dbg.location(399,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term2044); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:400:7: GEN
                    {
                    dbg.location(400,7);
                    match(input,GEN,FOLLOW_GEN_in_term2052); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:7: URI
                    {
                    dbg.location(401,7);
                    match(input,URI,FOLLOW_URI_in_term2060); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:7: hexColor
                    {
                    dbg.location(402,7);
                    pushFollow(FOLLOW_hexColor_in_term2068);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:7: function
                    {
                    dbg.location(403,7);
                    pushFollow(FOLLOW_function_in_term2076);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(64);}

            dbg.location(405,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:405:5: ( WS )*
            try { dbg.enterSubRule(65);

            loop65:
            do {
                int alt65=2;
                try { dbg.enterDecision(65, decisionCanBacktrack[65]);

                int LA65_0 = input.LA(1);

                if ( (LA65_0==WS) ) {
                    alt65=1;
                }


                } finally {dbg.exitDecision(65);}

                switch (alt65) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:405:5: WS
            	    {
            	    dbg.location(405,5);
            	    match(input,WS,FOLLOW_WS_in_term2088); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop65;
                }
            } while (true);
            } finally {dbg.exitSubRule(65);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(406, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:1: function : function_name ( WS )* LPAREN ( WS )* ( expr | ( attribute ( COMMA ( WS )* attribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(408, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:2: ( function_name ( WS )* LPAREN ( WS )* ( expr | ( attribute ( COMMA ( WS )* attribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:5: function_name ( WS )* LPAREN ( WS )* ( expr | ( attribute ( COMMA ( WS )* attribute )* ) ) RPAREN
            {
            dbg.location(409,5);
            pushFollow(FOLLOW_function_name_in_function2104);
            function_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(409,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:19: ( WS )*
            try { dbg.enterSubRule(66);

            loop66:
            do {
                int alt66=2;
                try { dbg.enterDecision(66, decisionCanBacktrack[66]);

                int LA66_0 = input.LA(1);

                if ( (LA66_0==WS) ) {
                    alt66=1;
                }


                } finally {dbg.exitDecision(66);}

                switch (alt66) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:19: WS
            	    {
            	    dbg.location(409,19);
            	    match(input,WS,FOLLOW_WS_in_function2106); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop66;
                }
            } while (true);
            } finally {dbg.exitSubRule(66);}

            dbg.location(410,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function2111); if (state.failed) return ;
            dbg.location(410,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:10: ( WS )*
            try { dbg.enterSubRule(67);

            loop67:
            do {
                int alt67=2;
                try { dbg.enterDecision(67, decisionCanBacktrack[67]);

                int LA67_0 = input.LA(1);

                if ( (LA67_0==WS) ) {
                    alt67=1;
                }


                } finally {dbg.exitDecision(67);}

                switch (alt67) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:10: WS
            	    {
            	    dbg.location(410,10);
            	    match(input,WS,FOLLOW_WS_in_function2113); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop67;
                }
            } while (true);
            } finally {dbg.exitSubRule(67);}

            dbg.location(411,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:3: ( expr | ( attribute ( COMMA ( WS )* attribute )* ) )
            int alt70=2;
            try { dbg.enterSubRule(70);
            try { dbg.enterDecision(70, decisionCanBacktrack[70]);

            try {
                isCyclicDecision = true;
                alt70 = dfa70.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(70);}

            switch (alt70) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:412:4: expr
                    {
                    dbg.location(412,4);
                    pushFollow(FOLLOW_expr_in_function2124);
                    expr();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:6: ( attribute ( COMMA ( WS )* attribute )* )
                    {
                    dbg.location(414,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:6: ( attribute ( COMMA ( WS )* attribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:5: attribute ( COMMA ( WS )* attribute )*
                    {
                    dbg.location(415,5);
                    pushFollow(FOLLOW_attribute_in_function2142);
                    attribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(415,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:15: ( COMMA ( WS )* attribute )*
                    try { dbg.enterSubRule(69);

                    loop69:
                    do {
                        int alt69=2;
                        try { dbg.enterDecision(69, decisionCanBacktrack[69]);

                        int LA69_0 = input.LA(1);

                        if ( (LA69_0==COMMA) ) {
                            alt69=1;
                        }


                        } finally {dbg.exitDecision(69);}

                        switch (alt69) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:16: COMMA ( WS )* attribute
                    	    {
                    	    dbg.location(415,16);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function2145); if (state.failed) return ;
                    	    dbg.location(415,22);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:22: ( WS )*
                    	    try { dbg.enterSubRule(68);

                    	    loop68:
                    	    do {
                    	        int alt68=2;
                    	        try { dbg.enterDecision(68, decisionCanBacktrack[68]);

                    	        int LA68_0 = input.LA(1);

                    	        if ( (LA68_0==WS) ) {
                    	            alt68=1;
                    	        }


                    	        } finally {dbg.exitDecision(68);}

                    	        switch (alt68) {
                    	    	case 1 :
                    	    	    dbg.enterAlt(1);

                    	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:22: WS
                    	    	    {
                    	    	    dbg.location(415,22);
                    	    	    match(input,WS,FOLLOW_WS_in_function2147); if (state.failed) return ;

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop68;
                    	        }
                    	    } while (true);
                    	    } finally {dbg.exitSubRule(68);}

                    	    dbg.location(415,26);
                    	    pushFollow(FOLLOW_attribute_in_function2150);
                    	    attribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop69;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(69);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(70);}

            dbg.location(418,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function2171); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(419, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:1: function_name : ( IDENT COLON )? IDENT ( DOT IDENT )* ;
    public final void function_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(421, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:425:2: ( ( IDENT COLON )? IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:425:4: ( IDENT COLON )? IDENT ( DOT IDENT )*
            {
            dbg.location(425,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:425:4: ( IDENT COLON )?
            int alt71=2;
            try { dbg.enterSubRule(71);
            try { dbg.enterDecision(71, decisionCanBacktrack[71]);

            int LA71_0 = input.LA(1);

            if ( (LA71_0==IDENT) ) {
                int LA71_1 = input.LA(2);

                if ( (LA71_1==COLON) ) {
                    alt71=1;
                }
            }
            } finally {dbg.exitDecision(71);}

            switch (alt71) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:425:5: IDENT COLON
                    {
                    dbg.location(425,5);
                    match(input,IDENT,FOLLOW_IDENT_in_function_name2214); if (state.failed) return ;
                    dbg.location(425,11);
                    match(input,COLON,FOLLOW_COLON_in_function_name2216); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(71);}

            dbg.location(425,19);
            match(input,IDENT,FOLLOW_IDENT_in_function_name2220); if (state.failed) return ;
            dbg.location(425,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:425:25: ( DOT IDENT )*
            try { dbg.enterSubRule(72);

            loop72:
            do {
                int alt72=2;
                try { dbg.enterDecision(72, decisionCanBacktrack[72]);

                int LA72_0 = input.LA(1);

                if ( (LA72_0==DOT) ) {
                    alt72=1;
                }


                } finally {dbg.exitDecision(72);}

                switch (alt72) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:425:26: DOT IDENT
            	    {
            	    dbg.location(425,26);
            	    match(input,DOT,FOLLOW_DOT_in_function_name2223); if (state.failed) return ;
            	    dbg.location(425,30);
            	    match(input,IDENT,FOLLOW_IDENT_in_function_name2225); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop72;
                }
            } while (true);
            } finally {dbg.exitSubRule(72);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(426, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:428:1: attribute : attrname ( WS )* OPEQ ( WS )* attrvalue ;
    public final void attribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(428, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:2: ( attrname ( WS )* OPEQ ( WS )* attrvalue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:4: attrname ( WS )* OPEQ ( WS )* attrvalue
            {
            dbg.location(429,4);
            pushFollow(FOLLOW_attrname_in_attribute2247);
            attrname();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(429,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:13: ( WS )*
            try { dbg.enterSubRule(73);

            loop73:
            do {
                int alt73=2;
                try { dbg.enterDecision(73, decisionCanBacktrack[73]);

                int LA73_0 = input.LA(1);

                if ( (LA73_0==WS) ) {
                    alt73=1;
                }


                } finally {dbg.exitDecision(73);}

                switch (alt73) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:13: WS
            	    {
            	    dbg.location(429,13);
            	    match(input,WS,FOLLOW_WS_in_attribute2249); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop73;
                }
            } while (true);
            } finally {dbg.exitSubRule(73);}

            dbg.location(429,17);
            match(input,OPEQ,FOLLOW_OPEQ_in_attribute2252); if (state.failed) return ;
            dbg.location(429,22);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:22: ( WS )*
            try { dbg.enterSubRule(74);

            loop74:
            do {
                int alt74=2;
                try { dbg.enterDecision(74, decisionCanBacktrack[74]);

                int LA74_0 = input.LA(1);

                if ( (LA74_0==WS) ) {
                    alt74=1;
                }


                } finally {dbg.exitDecision(74);}

                switch (alt74) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:22: WS
            	    {
            	    dbg.location(429,22);
            	    match(input,WS,FOLLOW_WS_in_attribute2254); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop74;
                }
            } while (true);
            } finally {dbg.exitSubRule(74);}

            dbg.location(429,26);
            pushFollow(FOLLOW_attrvalue_in_attribute2257);
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
        dbg.location(430, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:1: attrname : IDENT ;
    public final void attrname() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrname");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(432, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:433:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:433:4: IDENT
            {
            dbg.location(433,4);
            match(input,IDENT,FOLLOW_IDENT_in_attrname2272); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(434, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:436:1: attrvalue : expr ;
    public final void attrvalue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrvalue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(436, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:437:2: ( expr )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:437:4: expr
            {
            dbg.location(437,4);
            pushFollow(FOLLOW_expr_in_attrvalue2284);
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
        dbg.location(438, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:440:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(440, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:441:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:441:7: HASH
            {
            dbg.location(441,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor2302); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(442, 5);

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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:252:19: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:252:20: esPred
        {
        dbg.location(252,20);
        pushFollow(FOLLOW_esPred_in_synpred1_Css3885);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:254:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:254:6: esPred
        {
        dbg.location(254,6);
        pushFollow(FOLLOW_esPred_in_synpred2_Css3903);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:7: ( nsPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:8: nsPred
        {
        dbg.location(267,8);
        pushFollow(FOLLOW_nsPred_in_synpred3_Css3945);
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


    protected DFA40 dfa40 = new DFA40(this);
    protected DFA49 dfa49 = new DFA49(this);
    protected DFA59 dfa59 = new DFA59(this);
    protected DFA62 dfa62 = new DFA62(this);
    protected DFA64 dfa64 = new DFA64(this);
    protected DFA70 dfa70 = new DFA70(this);
    static final String DFA40_eotS =
        "\26\uffff";
    static final String DFA40_eofS =
        "\26\uffff";
    static final String DFA40_minS =
        "\1\13\1\uffff\1\0\1\16\1\4\1\16\1\uffff\1\0\4\4\1\0\2\4\1\0\6\4";
    static final String DFA40_maxS =
        "\1\122\1\uffff\1\0\1\17\1\122\1\17\1\uffff\1\0\1\122\2\16\1\122"+
        "\1\0\1\122\1\16\1\0\1\36\1\122\1\16\3\36";
    static final String DFA40_acceptS =
        "\1\uffff\1\2\4\uffff\1\1\17\uffff";
    static final String DFA40_specialS =
        "\2\uffff\1\2\4\uffff\1\3\4\uffff\1\1\2\uffff\1\0\6\uffff}>";
    static final String[] DFA40_transitionS = {
            "\1\1\1\uffff\3\1\1\uffff\1\5\1\uffff\3\1\1\uffff\1\1\1\2\1\3"+
            "\1\4\67\uffff\1\1",
            "",
            "\1\uffff",
            "\2\7",
            "\1\12\11\uffff\1\10\10\uffff\1\13\72\uffff\1\11",
            "\2\14",
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

    static final short[] DFA40_eot = DFA.unpackEncodedString(DFA40_eotS);
    static final short[] DFA40_eof = DFA.unpackEncodedString(DFA40_eofS);
    static final char[] DFA40_min = DFA.unpackEncodedStringToUnsignedChars(DFA40_minS);
    static final char[] DFA40_max = DFA.unpackEncodedStringToUnsignedChars(DFA40_maxS);
    static final short[] DFA40_accept = DFA.unpackEncodedString(DFA40_acceptS);
    static final short[] DFA40_special = DFA.unpackEncodedString(DFA40_specialS);
    static final short[][] DFA40_transition;

    static {
        int numStates = DFA40_transitionS.length;
        DFA40_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA40_transition[i] = DFA.unpackEncodedString(DFA40_transitionS[i]);
        }
    }

    class DFA40 extends DFA {

        public DFA40(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 40;
            this.eot = DFA40_eot;
            this.eof = DFA40_eof;
            this.min = DFA40_min;
            this.max = DFA40_max;
            this.accept = DFA40_accept;
            this.special = DFA40_special;
            this.transition = DFA40_transition;
        }
        public String getDescription() {
            return "()* loopback of 252:18: ( ( esPred )=> elementSubsequent )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA40_15 = input.LA(1);

                         
                        int index40_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index40_15);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA40_12 = input.LA(1);

                         
                        int index40_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index40_12);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA40_2 = input.LA(1);

                         
                        int index40_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index40_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA40_7 = input.LA(1);

                         
                        int index40_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index40_7);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 40, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA49_eotS =
        "\5\uffff";
    static final String DFA49_eofS =
        "\5\uffff";
    static final String DFA49_minS =
        "\2\4\2\uffff\1\4";
    static final String DFA49_maxS =
        "\2\122\2\uffff\1\122";
    static final String DFA49_acceptS =
        "\2\uffff\1\1\1\2\1\uffff";
    static final String DFA49_specialS =
        "\5\uffff}>";
    static final String[] DFA49_transitionS = {
            "\1\3\11\uffff\1\1\10\uffff\1\2\72\uffff\1\2",
            "\1\4\26\uffff\4\3\63\uffff\1\2",
            "",
            "",
            "\1\4\26\uffff\4\3\63\uffff\1\2"
    };

    static final short[] DFA49_eot = DFA.unpackEncodedString(DFA49_eotS);
    static final short[] DFA49_eof = DFA.unpackEncodedString(DFA49_eofS);
    static final char[] DFA49_min = DFA.unpackEncodedStringToUnsignedChars(DFA49_minS);
    static final char[] DFA49_max = DFA.unpackEncodedStringToUnsignedChars(DFA49_maxS);
    static final short[] DFA49_accept = DFA.unpackEncodedString(DFA49_acceptS);
    static final short[] DFA49_special = DFA.unpackEncodedString(DFA49_specialS);
    static final short[][] DFA49_transition;

    static {
        int numStates = DFA49_transitionS.length;
        DFA49_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA49_transition[i] = DFA.unpackEncodedString(DFA49_transitionS[i]);
        }
    }

    class DFA49 extends DFA {

        public DFA49(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 49;
            this.eot = DFA49_eot;
            this.eof = DFA49_eof;
            this.min = DFA49_min;
            this.max = DFA49_max;
            this.accept = DFA49_accept;
            this.special = DFA49_special;
            this.transition = DFA49_transition;
        }
        public String getDescription() {
            return "319:6: ( namespacePrefix )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA59_eotS =
        "\4\uffff";
    static final String DFA59_eofS =
        "\4\uffff";
    static final String DFA59_minS =
        "\2\4\2\uffff";
    static final String DFA59_maxS =
        "\2\122\2\uffff";
    static final String DFA59_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA59_specialS =
        "\4\uffff}>";
    static final String[] DFA59_transitionS = {
            "\1\1\6\uffff\1\3\1\uffff\3\3\1\uffff\1\3\1\uffff\3\3\1\uffff"+
            "\4\3\4\uffff\1\2\62\uffff\1\3",
            "\1\1\6\uffff\1\3\1\uffff\3\3\1\uffff\1\3\1\uffff\3\3\1\uffff"+
            "\4\3\4\uffff\1\2\62\uffff\1\3",
            "",
            ""
    };

    static final short[] DFA59_eot = DFA.unpackEncodedString(DFA59_eotS);
    static final short[] DFA59_eof = DFA.unpackEncodedString(DFA59_eofS);
    static final char[] DFA59_min = DFA.unpackEncodedStringToUnsignedChars(DFA59_minS);
    static final char[] DFA59_max = DFA.unpackEncodedStringToUnsignedChars(DFA59_maxS);
    static final short[] DFA59_accept = DFA.unpackEncodedString(DFA59_acceptS);
    static final short[] DFA59_special = DFA.unpackEncodedString(DFA59_specialS);
    static final short[][] DFA59_transition;

    static {
        int numStates = DFA59_transitionS.length;
        DFA59_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA59_transition[i] = DFA.unpackEncodedString(DFA59_transitionS[i]);
        }
    }

    class DFA59 extends DFA {

        public DFA59(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 59;
            this.eot = DFA59_eot;
            this.eof = DFA59_eof;
            this.min = DFA59_min;
            this.max = DFA59_max;
            this.accept = DFA59_accept;
            this.special = DFA59_special;
            this.transition = DFA59_transition;
        }
        public String getDescription() {
            return "342:17: ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA62_eotS =
        "\7\uffff";
    static final String DFA62_eofS =
        "\7\uffff";
    static final String DFA62_minS =
        "\1\6\1\uffff\1\4\1\uffff\3\4";
    static final String DFA62_maxS =
        "\1\51\1\uffff\1\51\1\uffff\3\51";
    static final String DFA62_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\3\uffff";
    static final String DFA62_specialS =
        "\7\uffff}>";
    static final String[] DFA62_transitionS = {
            "\1\3\1\1\1\uffff\1\3\2\uffff\1\1\1\2\2\3\2\uffff\2\3\2\uffff"+
            "\1\3\1\uffff\1\3\7\uffff\2\1\10\3",
            "",
            "\1\4\1\uffff\1\3\2\uffff\1\3\4\uffff\1\5\1\3\3\uffff\1\3\2"+
            "\uffff\1\3\1\uffff\1\3\11\uffff\10\3",
            "",
            "\1\4\1\uffff\1\3\2\uffff\1\3\4\uffff\1\5\1\3\3\uffff\1\3\2"+
            "\uffff\1\3\1\uffff\1\3\11\uffff\10\3",
            "\1\6\1\uffff\2\3\1\uffff\1\3\2\uffff\4\3\1\uffff\3\3\2\uffff"+
            "\1\3\1\uffff\2\3\1\uffff\1\1\3\uffff\13\3",
            "\1\6\1\uffff\2\3\1\uffff\1\3\2\uffff\4\3\2\uffff\2\3\2\uffff"+
            "\1\3\1\uffff\1\3\2\uffff\1\1\3\uffff\13\3"
    };

    static final short[] DFA62_eot = DFA.unpackEncodedString(DFA62_eotS);
    static final short[] DFA62_eof = DFA.unpackEncodedString(DFA62_eofS);
    static final char[] DFA62_min = DFA.unpackEncodedStringToUnsignedChars(DFA62_minS);
    static final char[] DFA62_max = DFA.unpackEncodedStringToUnsignedChars(DFA62_maxS);
    static final short[] DFA62_accept = DFA.unpackEncodedString(DFA62_acceptS);
    static final short[] DFA62_special = DFA.unpackEncodedString(DFA62_specialS);
    static final short[][] DFA62_transition;

    static {
        int numStates = DFA62_transitionS.length;
        DFA62_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA62_transition[i] = DFA.unpackEncodedString(DFA62_transitionS[i]);
        }
    }

    class DFA62 extends DFA {

        public DFA62(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 62;
            this.eot = DFA62_eot;
            this.eof = DFA62_eof;
            this.min = DFA62_min;
            this.max = DFA62_max;
            this.accept = DFA62_accept;
            this.special = DFA62_special;
            this.transition = DFA62_transition;
        }
        public String getDescription() {
            return "()* loopback of 382:12: ( operator term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA64_eotS =
        "\12\uffff";
    static final String DFA64_eofS =
        "\12\uffff";
    static final String DFA64_minS =
        "\1\6\2\uffff\1\4\4\uffff\1\4\1\uffff";
    static final String DFA64_maxS =
        "\1\51\2\uffff\1\51\4\uffff\1\51\1\uffff";
    static final String DFA64_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\7\1\uffff\1\3";
    static final String DFA64_specialS =
        "\12\uffff}>";
    static final String[] DFA64_transitionS = {
            "\1\2\2\uffff\1\5\4\uffff\1\3\1\4\10\uffff\1\6\11\uffff\10\1",
            "",
            "",
            "\1\10\1\uffff\2\11\1\uffff\1\11\2\uffff\4\11\1\uffff\1\7\2"+
            "\11\2\uffff\1\11\1\uffff\1\11\1\7\5\uffff\1\7\12\11",
            "",
            "",
            "",
            "",
            "\1\10\1\uffff\2\11\1\uffff\1\11\2\uffff\4\11\2\uffff\2\11\2"+
            "\uffff\1\11\1\uffff\1\11\6\uffff\1\7\12\11",
            ""
    };

    static final short[] DFA64_eot = DFA.unpackEncodedString(DFA64_eotS);
    static final short[] DFA64_eof = DFA.unpackEncodedString(DFA64_eofS);
    static final char[] DFA64_min = DFA.unpackEncodedStringToUnsignedChars(DFA64_minS);
    static final char[] DFA64_max = DFA.unpackEncodedStringToUnsignedChars(DFA64_maxS);
    static final short[] DFA64_accept = DFA.unpackEncodedString(DFA64_acceptS);
    static final short[] DFA64_special = DFA.unpackEncodedString(DFA64_specialS);
    static final short[][] DFA64_transition;

    static {
        int numStates = DFA64_transitionS.length;
        DFA64_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA64_transition[i] = DFA.unpackEncodedString(DFA64_transitionS[i]);
        }
    }

    class DFA64 extends DFA {

        public DFA64(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 64;
            this.eot = DFA64_eot;
            this.eof = DFA64_eof;
            this.min = DFA64_min;
            this.max = DFA64_max;
            this.accept = DFA64_accept;
            this.special = DFA64_special;
            this.transition = DFA64_transition;
        }
        public String getDescription() {
            return "387:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | GEN | URI | hexColor | function )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA70_eotS =
        "\5\uffff";
    static final String DFA70_eofS =
        "\5\uffff";
    static final String DFA70_minS =
        "\1\6\1\uffff\2\4\1\uffff";
    static final String DFA70_maxS =
        "\1\51\1\uffff\2\51\1\uffff";
    static final String DFA70_acceptS =
        "\1\uffff\1\1\2\uffff\1\2";
    static final String DFA70_specialS =
        "\5\uffff}>";
    static final String[] DFA70_transitionS = {
            "\1\1\2\uffff\1\1\4\uffff\1\2\1\1\3\uffff\1\1\2\uffff\1\1\1\uffff"+
            "\1\1\11\uffff\10\1",
            "",
            "\1\3\1\uffff\1\1\2\uffff\1\1\3\uffff\3\1\1\uffff\3\1\2\uffff"+
            "\1\1\1\uffff\2\1\1\uffff\1\4\3\uffff\2\1\1\uffff\10\1",
            "\1\3\1\uffff\1\1\2\uffff\1\1\3\uffff\3\1\2\uffff\2\1\2\uffff"+
            "\1\1\1\uffff\1\1\2\uffff\1\4\3\uffff\2\1\1\uffff\10\1",
            ""
    };

    static final short[] DFA70_eot = DFA.unpackEncodedString(DFA70_eotS);
    static final short[] DFA70_eof = DFA.unpackEncodedString(DFA70_eofS);
    static final char[] DFA70_min = DFA.unpackEncodedStringToUnsignedChars(DFA70_minS);
    static final char[] DFA70_max = DFA.unpackEncodedStringToUnsignedChars(DFA70_maxS);
    static final short[] DFA70_accept = DFA.unpackEncodedString(DFA70_acceptS);
    static final short[] DFA70_special = DFA.unpackEncodedString(DFA70_specialS);
    static final short[][] DFA70_transition;

    static {
        int numStates = DFA70_transitionS.length;
        DFA70_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA70_transition[i] = DFA.unpackEncodedString(DFA70_transitionS[i]);
        }
    }

    class DFA70 extends DFA {

        public DFA70(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 70;
            this.eot = DFA70_eot;
            this.eof = DFA70_eof;
            this.min = DFA70_min;
            this.max = DFA70_max;
            this.accept = DFA70_accept;
            this.special = DFA70_special;
            this.transition = DFA70_transition;
        }
        public String getDescription() {
            return "411:3: ( expr | ( attribute ( COMMA ( WS )* attribute )* ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_charSet_in_styleSheet79 = new BitSet(new long[]{0x000000000783C510L,0x0000000000040000L});
    public static final BitSet FOLLOW_WS_in_styleSheet87 = new BitSet(new long[]{0x000000000783C510L,0x0000000000040000L});
    public static final BitSet FOLLOW_imports_in_styleSheet99 = new BitSet(new long[]{0x000000000783C510L,0x0000000000040000L});
    public static final BitSet FOLLOW_WS_in_styleSheet101 = new BitSet(new long[]{0x000000000783C510L,0x0000000000040000L});
    public static final BitSet FOLLOW_bodylist_in_styleSheet116 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_styleSheet123 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHARSET_SYM_in_charSet149 = new BitSet(new long[]{0x0000000000000050L});
    public static final BitSet FOLLOW_WS_in_charSet151 = new BitSet(new long[]{0x0000000000000050L});
    public static final BitSet FOLLOW_STRING_in_charSet154 = new BitSet(new long[]{0x0000000000000090L});
    public static final BitSet FOLLOW_WS_in_charSet156 = new BitSet(new long[]{0x0000000000000090L});
    public static final BitSet FOLLOW_SEMI_in_charSet159 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORT_SYM_in_imports181 = new BitSet(new long[]{0x0000000000000250L});
    public static final BitSet FOLLOW_WS_in_imports183 = new BitSet(new long[]{0x0000000000000250L});
    public static final BitSet FOLLOW_set_in_imports186 = new BitSet(new long[]{0x000000000000C090L});
    public static final BitSet FOLLOW_WS_in_imports192 = new BitSet(new long[]{0x000000000000C090L});
    public static final BitSet FOLLOW_mediaList_in_imports195 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_SEMI_in_imports198 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEDIA_SYM_in_media219 = new BitSet(new long[]{0x000000000000C010L});
    public static final BitSet FOLLOW_WS_in_media221 = new BitSet(new long[]{0x000000000000C010L});
    public static final BitSet FOLLOW_mediaList_in_media224 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_LBRACE_in_media234 = new BitSet(new long[]{0x000000000782C010L,0x0000000000040000L});
    public static final BitSet FOLLOW_WS_in_media236 = new BitSet(new long[]{0x000000000782C010L,0x0000000000040000L});
    public static final BitSet FOLLOW_ruleSet_in_media251 = new BitSet(new long[]{0x0000000000001010L});
    public static final BitSet FOLLOW_WS_in_media261 = new BitSet(new long[]{0x0000000000001010L});
    public static final BitSet FOLLOW_RBRACE_in_media264 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_medium_in_mediaList285 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_COMMA_in_mediaList288 = new BitSet(new long[]{0x000000000000C010L});
    public static final BitSet FOLLOW_WS_in_mediaList290 = new BitSet(new long[]{0x000000000000C010L});
    public static final BitSet FOLLOW_medium_in_mediaList293 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_set_in_medium312 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_medium322 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_bodyset_in_bodylist345 = new BitSet(new long[]{0x000000000783C402L,0x0000000000040000L});
    public static final BitSet FOLLOW_ruleSet_in_bodyset374 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_media_in_bodyset386 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_page_in_bodyset398 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_bodyset414 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page436 = new BitSet(new long[]{0x0000000000020810L});
    public static final BitSet FOLLOW_WS_in_page438 = new BitSet(new long[]{0x0000000000020800L});
    public static final BitSet FOLLOW_pseudoPage_in_page442 = new BitSet(new long[]{0x0000000000000810L});
    public static final BitSet FOLLOW_WS_in_page444 = new BitSet(new long[]{0x0000000000000810L});
    public static final BitSet FOLLOW_LBRACE_in_page457 = new BitSet(new long[]{0x000000000000C010L});
    public static final BitSet FOLLOW_WS_in_page459 = new BitSet(new long[]{0x000000000000C010L});
    public static final BitSet FOLLOW_declaration_in_page474 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_SEMI_in_page476 = new BitSet(new long[]{0x000000000000D010L});
    public static final BitSet FOLLOW_declaration_in_page479 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_SEMI_in_page481 = new BitSet(new long[]{0x000000000000D010L});
    public static final BitSet FOLLOW_RBRACE_in_page493 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage514 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage516 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_operator537 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_operator539 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_COMMA_in_operator548 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_operator550 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_PLUS_in_combinator578 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator580 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_GREATER_in_combinator589 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator591 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_TILDE_in_combinator600 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator602 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_property662 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_property670 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_selectorsGroup_in_ruleSet695 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_LBRACE_in_ruleSet705 = new BitSet(new long[]{0x000000000000D090L});
    public static final BitSet FOLLOW_WS_in_ruleSet707 = new BitSet(new long[]{0x000000000000D090L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_ruleSet710 = new BitSet(new long[]{0x000000000000D090L});
    public static final BitSet FOLLOW_declarations_in_ruleSet724 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_RBRACE_in_ruleSet734 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_declarations772 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_SEMI_in_declarations776 = new BitSet(new long[]{0x000000000000C092L});
    public static final BitSet FOLLOW_WS_in_declarations778 = new BitSet(new long[]{0x000000000000C092L});
    public static final BitSet FOLLOW_declaration_in_declarations781 = new BitSet(new long[]{0x0000000000000082L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup805 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup808 = new BitSet(new long[]{0x000000000782C010L,0x0000000000040000L});
    public static final BitSet FOLLOW_WS_in_selectorsGroup810 = new BitSet(new long[]{0x000000000782C010L,0x0000000000040000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup813 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector836 = new BitSet(new long[]{0x0000000007BAC002L,0x0000000000040000L});
    public static final BitSet FOLLOW_combinator_in_selector839 = new BitSet(new long[]{0x000000000782C000L,0x0000000000040000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector841 = new BitSet(new long[]{0x0000000007BAC002L,0x0000000000040000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence881 = new BitSet(new long[]{0x000000000782C002L,0x0000000000040000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence888 = new BitSet(new long[]{0x000000000782C002L,0x0000000000040000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence906 = new BitSet(new long[]{0x000000000782C002L,0x0000000000040000L});
    public static final BitSet FOLLOW_namespacePrefix_in_typeSelector948 = new BitSet(new long[]{0x000000000080C000L,0x0000000000040000L});
    public static final BitSet FOLLOW_elementName_in_typeSelector954 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_typeSelector956 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_set_in_nsPred986 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_82_in_nsPred994 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespaceName_in_namespacePrefix1014 = new BitSet(new long[]{0x0000000000000010L,0x0000000000040000L});
    public static final BitSet FOLLOW_WS_in_namespacePrefix1016 = new BitSet(new long[]{0x0000000000000010L,0x0000000000040000L});
    public static final BitSet FOLLOW_82_in_namespacePrefix1022 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_namespaceName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent1116 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent1125 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_attrib_in_elementSubsequent1137 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent1149 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_elementSubsequent1161 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_HASH_in_cssId1183 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass1200 = new BitSet(new long[]{0x000000000000C000L});
    public static final BitSet FOLLOW_set_in_cssClass1202 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_attrib1266 = new BitSet(new long[]{0x0000000000804010L,0x0000000000040000L});
    public static final BitSet FOLLOW_namespacePrefix_in_attrib1273 = new BitSet(new long[]{0x0000000000004010L});
    public static final BitSet FOLLOW_WS_in_attrib1276 = new BitSet(new long[]{0x0000000000004010L});
    public static final BitSet FOLLOW_IDENT_in_attrib1287 = new BitSet(new long[]{0x0000000078000010L});
    public static final BitSet FOLLOW_WS_in_attrib1289 = new BitSet(new long[]{0x0000000078000010L});
    public static final BitSet FOLLOW_set_in_attrib1331 = new BitSet(new long[]{0x0000000000004050L});
    public static final BitSet FOLLOW_WS_in_attrib1439 = new BitSet(new long[]{0x0000000000004050L});
    public static final BitSet FOLLOW_set_in_attrib1458 = new BitSet(new long[]{0x0000000040000010L});
    public static final BitSet FOLLOW_WS_in_attrib1542 = new BitSet(new long[]{0x0000000040000010L});
    public static final BitSet FOLLOW_RBRACKET_in_attrib1571 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudo1584 = new BitSet(new long[]{0x000000000000C000L});
    public static final BitSet FOLLOW_set_in_pseudo1599 = new BitSet(new long[]{0x0000000080000012L});
    public static final BitSet FOLLOW_WS_in_pseudo1648 = new BitSet(new long[]{0x0000000080000010L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo1651 = new BitSet(new long[]{0x000000010000C010L});
    public static final BitSet FOLLOW_WS_in_pseudo1653 = new BitSet(new long[]{0x000000010000C010L});
    public static final BitSet FOLLOW_set_in_pseudo1657 = new BitSet(new long[]{0x0000000100000010L});
    public static final BitSet FOLLOW_WS_in_pseudo1667 = new BitSet(new long[]{0x0000000100000010L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo1672 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_declaration1718 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_COLON_in_declaration1720 = new BitSet(new long[]{0x000003FC0148C250L});
    public static final BitSet FOLLOW_WS_in_declaration1722 = new BitSet(new long[]{0x000003FC0148C250L});
    public static final BitSet FOLLOW_expr_in_declaration1725 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_prio_in_declaration1727 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio1820 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expr1841 = new BitSet(new long[]{0x000003FC014CE252L});
    public static final BitSet FOLLOW_operator_in_expr1844 = new BitSet(new long[]{0x000003FC0148C250L});
    public static final BitSet FOLLOW_term_in_expr1846 = new BitSet(new long[]{0x000003FC014CE252L});
    public static final BitSet FOLLOW_unaryOperator_in_term1869 = new BitSet(new long[]{0x000003FC0148C250L});
    public static final BitSet FOLLOW_set_in_term1890 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_STRING_in_term2036 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_IDENT_in_term2044 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_GEN_in_term2052 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_URI_in_term2060 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_hexColor_in_term2068 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_function_in_term2076 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_term2088 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_function_name_in_function2104 = new BitSet(new long[]{0x0000000080000010L});
    public static final BitSet FOLLOW_WS_in_function2106 = new BitSet(new long[]{0x0000000080000010L});
    public static final BitSet FOLLOW_LPAREN_in_function2111 = new BitSet(new long[]{0x000003FC0148C250L});
    public static final BitSet FOLLOW_WS_in_function2113 = new BitSet(new long[]{0x000003FC0148C250L});
    public static final BitSet FOLLOW_expr_in_function2124 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_attribute_in_function2142 = new BitSet(new long[]{0x0000000100002000L});
    public static final BitSet FOLLOW_COMMA_in_function2145 = new BitSet(new long[]{0x000003FC0148C250L});
    public static final BitSet FOLLOW_WS_in_function2147 = new BitSet(new long[]{0x000003FC0148C250L});
    public static final BitSet FOLLOW_attribute_in_function2150 = new BitSet(new long[]{0x0000000100002000L});
    public static final BitSet FOLLOW_RPAREN_in_function2171 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_function_name2214 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_COLON_in_function_name2216 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_IDENT_in_function_name2220 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_DOT_in_function_name2223 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_IDENT_in_function_name2225 = new BitSet(new long[]{0x0000000002000002L});
    public static final BitSet FOLLOW_attrname_in_attribute2247 = new BitSet(new long[]{0x0000000008000010L});
    public static final BitSet FOLLOW_WS_in_attribute2249 = new BitSet(new long[]{0x0000000008000010L});
    public static final BitSet FOLLOW_OPEQ_in_attribute2252 = new BitSet(new long[]{0x000003FC0148C250L});
    public static final BitSet FOLLOW_WS_in_attribute2254 = new BitSet(new long[]{0x000003FC0148C250L});
    public static final BitSet FOLLOW_attrvalue_in_attribute2257 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_attrname2272 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_in_attrvalue2284 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor2302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred1_Css3885 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred2_Css3903 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nsPred_in_synpred3_Css3945 = new BitSet(new long[]{0x0000000000000002L});

}