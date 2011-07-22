// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2011-07-21 18:20:02

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "CHARSET_SYM", "STRING", "SEMI", "IMPORT_SYM", "URI", "COMMA", "MEDIA_SYM", "LBRACE", "RBRACE", "IDENT", "PAGE_SYM", "COLON", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "STAR", "HASH", "DOT", "LBRACKET", "OPEQ", "INCLUDES", "DASHMATCH", "RBRACKET", "LPAREN", "RPAREN", "IMPORTANT_SYM", "NUMBER", "PERCENTAGE", "LENGTH", "EMS", "EXS", "ANGLE", "TIME", "FREQ", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "NAME", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "COMMENT", "CDO", "CDC", "INVALID", "WS", "DIMENSION", "NL", "'|'"
    };
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

    public static final String[] ruleNames = new String[] {
        "invalidRule", "combinator", "expr", "cssId", "pseudoPage", "hexColor", 
        "attrname", "declarations", "term", "attribute", "esPred", "elementSubsequent", 
        "function_name", "cssClass", "operator", "attrib", "ruleSet", "styleSheet", 
        "declaration", "namespaceName", "synpred1_Css3", "page", "nsPred", 
        "synpred2_Css3", "function", "bodyset", "attrvalue", "selector", 
        "synpred3_Css3", "unaryOperator", "typeSelector", "bodylist", "selectorsGroup", 
        "elementName", "imports", "prio", "syncToIdent", "medium", "media", 
        "namespacePrefix", "pseudo", "property", "charSet", "simpleSelectorSequence"
    };
    public static final boolean[] decisionCanBacktrack = new boolean[] {
        false, // invalid decision
        false, false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, true, true, false, 
            true, false, false, false, false, false, false, false, false, 
            false, false, false, false
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:131:1: styleSheet : charSet ( imports )* bodylist EOF ;
    public final void styleSheet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "styleSheet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(131, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:132:5: ( charSet ( imports )* bodylist EOF )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:132:9: charSet ( imports )* bodylist EOF
            {
            dbg.location(132,9);
            pushFollow(FOLLOW_charSet_in_styleSheet77);
            charSet();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(133,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:133:9: ( imports )*
            try { dbg.enterSubRule(1);

            loop1:
            do {
                int alt1=2;
                try { dbg.enterDecision(1, decisionCanBacktrack[1]);

                int LA1_0 = input.LA(1);

                if ( (LA1_0==IMPORT_SYM) ) {
                    alt1=1;
                }


                } finally {dbg.exitDecision(1);}

                switch (alt1) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:133:9: imports
            	    {
            	    dbg.location(133,9);
            	    pushFollow(FOLLOW_imports_in_styleSheet87);
            	    imports();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);
            } finally {dbg.exitSubRule(1);}

            dbg.location(134,9);
            pushFollow(FOLLOW_bodylist_in_styleSheet98);
            bodylist();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(135,6);
            match(input,EOF,FOLLOW_EOF_in_styleSheet105); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(136, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:141:1: charSet : ( CHARSET_SYM STRING SEMI | );
    public final void charSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "charSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(141, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:142:5: ( CHARSET_SYM STRING SEMI | )
            int alt2=2;
            try { dbg.enterDecision(2, decisionCanBacktrack[2]);

            int LA2_0 = input.LA(1);

            if ( (LA2_0==CHARSET_SYM) ) {
                alt2=1;
            }
            else if ( (LA2_0==EOF||LA2_0==IMPORT_SYM||LA2_0==MEDIA_SYM||(LA2_0>=IDENT && LA2_0<=COLON)||(LA2_0>=STAR && LA2_0<=LBRACKET)||LA2_0==81) ) {
                alt2=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(2);}

            switch (alt2) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:142:9: CHARSET_SYM STRING SEMI
                    {
                    dbg.location(142,9);
                    match(input,CHARSET_SYM,FOLLOW_CHARSET_SYM_in_charSet131); if (state.failed) return ;
                    dbg.location(142,21);
                    match(input,STRING,FOLLOW_STRING_in_charSet133); if (state.failed) return ;
                    dbg.location(142,28);
                    match(input,SEMI,FOLLOW_SEMI_in_charSet135); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:144:5: 
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:149:1: imports : IMPORT_SYM ( STRING | URI ) ( medium ( COMMA medium )* )? SEMI ;
    public final void imports() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "imports");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(149, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:150:5: ( IMPORT_SYM ( STRING | URI ) ( medium ( COMMA medium )* )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:150:9: IMPORT_SYM ( STRING | URI ) ( medium ( COMMA medium )* )? SEMI
            {
            dbg.location(150,9);
            match(input,IMPORT_SYM,FOLLOW_IMPORT_SYM_in_imports163); if (state.failed) return ;
            dbg.location(150,20);
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

            dbg.location(150,33);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:150:33: ( medium ( COMMA medium )* )?
            int alt4=2;
            try { dbg.enterSubRule(4);
            try { dbg.enterDecision(4, decisionCanBacktrack[4]);

            int LA4_0 = input.LA(1);

            if ( (LA4_0==IDENT) ) {
                alt4=1;
            }
            } finally {dbg.exitDecision(4);}

            switch (alt4) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:150:34: medium ( COMMA medium )*
                    {
                    dbg.location(150,34);
                    pushFollow(FOLLOW_medium_in_imports172);
                    medium();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(150,41);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:150:41: ( COMMA medium )*
                    try { dbg.enterSubRule(3);

                    loop3:
                    do {
                        int alt3=2;
                        try { dbg.enterDecision(3, decisionCanBacktrack[3]);

                        int LA3_0 = input.LA(1);

                        if ( (LA3_0==COMMA) ) {
                            alt3=1;
                        }


                        } finally {dbg.exitDecision(3);}

                        switch (alt3) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:150:42: COMMA medium
                    	    {
                    	    dbg.location(150,42);
                    	    match(input,COMMA,FOLLOW_COMMA_in_imports175); if (state.failed) return ;
                    	    dbg.location(150,48);
                    	    pushFollow(FOLLOW_medium_in_imports177);
                    	    medium();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop3;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(3);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(4);}

            dbg.location(150,59);
            match(input,SEMI,FOLLOW_SEMI_in_imports183); if (state.failed) return ;

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:157:1: media : MEDIA_SYM medium ( COMMA medium )* LBRACE ruleSet RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(157, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:158:5: ( MEDIA_SYM medium ( COMMA medium )* LBRACE ruleSet RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:158:7: MEDIA_SYM medium ( COMMA medium )* LBRACE ruleSet RBRACE
            {
            dbg.location(158,7);
            match(input,MEDIA_SYM,FOLLOW_MEDIA_SYM_in_media204); if (state.failed) return ;
            dbg.location(158,17);
            pushFollow(FOLLOW_medium_in_media206);
            medium();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(158,24);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:158:24: ( COMMA medium )*
            try { dbg.enterSubRule(5);

            loop5:
            do {
                int alt5=2;
                try { dbg.enterDecision(5, decisionCanBacktrack[5]);

                int LA5_0 = input.LA(1);

                if ( (LA5_0==COMMA) ) {
                    alt5=1;
                }


                } finally {dbg.exitDecision(5);}

                switch (alt5) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:158:25: COMMA medium
            	    {
            	    dbg.location(158,25);
            	    match(input,COMMA,FOLLOW_COMMA_in_media209); if (state.failed) return ;
            	    dbg.location(158,31);
            	    pushFollow(FOLLOW_medium_in_media211);
            	    medium();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);
            } finally {dbg.exitSubRule(5);}

            dbg.location(159,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_media223); if (state.failed) return ;
            dbg.location(160,13);
            pushFollow(FOLLOW_ruleSet_in_media237);
            ruleSet();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(161,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_media247); if (state.failed) return ;

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


    // $ANTLR start "medium"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:167:1: medium : IDENT ;
    public final void medium() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "medium");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(167, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:168:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:168:7: IDENT
            {
            dbg.location(168,7);
            match(input,IDENT,FOLLOW_IDENT_in_medium267); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(169, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:172:1: bodylist : ( bodyset )* ;
    public final void bodylist() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodylist");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(172, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:173:5: ( ( bodyset )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:173:7: ( bodyset )*
            {
            dbg.location(173,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:173:7: ( bodyset )*
            try { dbg.enterSubRule(6);

            loop6:
            do {
                int alt6=2;
                try { dbg.enterDecision(6, decisionCanBacktrack[6]);

                int LA6_0 = input.LA(1);

                if ( (LA6_0==MEDIA_SYM||(LA6_0>=IDENT && LA6_0<=COLON)||(LA6_0>=STAR && LA6_0<=LBRACKET)||LA6_0==81) ) {
                    alt6=1;
                }


                } finally {dbg.exitDecision(6);}

                switch (alt6) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:173:7: bodyset
            	    {
            	    dbg.location(173,7);
            	    pushFollow(FOLLOW_bodyset_in_bodylist290);
            	    bodyset();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);
            } finally {dbg.exitSubRule(6);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(174, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:176:1: bodyset : ( ruleSet | media | page );
    public final void bodyset() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyset");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(176, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:177:5: ( ruleSet | media | page )
            int alt7=3;
            try { dbg.enterDecision(7, decisionCanBacktrack[7]);

            switch ( input.LA(1) ) {
            case IDENT:
            case COLON:
            case STAR:
            case HASH:
            case DOT:
            case LBRACKET:
            case 81:
                {
                alt7=1;
                }
                break;
            case MEDIA_SYM:
                {
                alt7=2;
                }
                break;
            case PAGE_SYM:
                {
                alt7=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(7);}

            switch (alt7) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:177:7: ruleSet
                    {
                    dbg.location(177,7);
                    pushFollow(FOLLOW_ruleSet_in_bodyset312);
                    ruleSet();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:178:7: media
                    {
                    dbg.location(178,7);
                    pushFollow(FOLLOW_media_in_bodyset320);
                    media();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:179:7: page
                    {
                    dbg.location(179,7);
                    pushFollow(FOLLOW_page_in_bodyset328);
                    page();

                    state._fsp--;
                    if (state.failed) return ;

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
        dbg.location(180, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:182:1: page : PAGE_SYM ( pseudoPage )? LBRACE declaration SEMI ( declaration SEMI )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(182, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:5: ( PAGE_SYM ( pseudoPage )? LBRACE declaration SEMI ( declaration SEMI )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:7: PAGE_SYM ( pseudoPage )? LBRACE declaration SEMI ( declaration SEMI )* RBRACE
            {
            dbg.location(183,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page352); if (state.failed) return ;
            dbg.location(183,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:16: ( pseudoPage )?
            int alt8=2;
            try { dbg.enterSubRule(8);
            try { dbg.enterDecision(8, decisionCanBacktrack[8]);

            int LA8_0 = input.LA(1);

            if ( (LA8_0==COLON) ) {
                alt8=1;
            }
            } finally {dbg.exitDecision(8);}

            switch (alt8) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:16: pseudoPage
                    {
                    dbg.location(183,16);
                    pushFollow(FOLLOW_pseudoPage_in_page354);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(8);}

            dbg.location(184,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page365); if (state.failed) return ;
            dbg.location(185,13);
            pushFollow(FOLLOW_declaration_in_page379);
            declaration();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(185,25);
            match(input,SEMI,FOLLOW_SEMI_in_page381); if (state.failed) return ;
            dbg.location(185,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:185:30: ( declaration SEMI )*
            try { dbg.enterSubRule(9);

            loop9:
            do {
                int alt9=2;
                try { dbg.enterDecision(9, decisionCanBacktrack[9]);

                int LA9_0 = input.LA(1);

                if ( (LA9_0==IDENT) ) {
                    alt9=1;
                }


                } finally {dbg.exitDecision(9);}

                switch (alt9) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:185:31: declaration SEMI
            	    {
            	    dbg.location(185,31);
            	    pushFollow(FOLLOW_declaration_in_page384);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(185,43);
            	    match(input,SEMI,FOLLOW_SEMI_in_page386); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);
            } finally {dbg.exitSubRule(9);}

            dbg.location(186,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page398); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "page");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "page"


    // $ANTLR start "pseudoPage"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:189:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(189, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:190:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:190:7: COLON IDENT
            {
            dbg.location(190,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage419); if (state.failed) return ;
            dbg.location(190,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage421); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(191, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:193:1: operator : ( SOLIDUS | COMMA | );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(193, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:194:5: ( SOLIDUS | COMMA | )
            int alt10=3;
            try { dbg.enterDecision(10, decisionCanBacktrack[10]);

            switch ( input.LA(1) ) {
            case SOLIDUS:
                {
                alt10=1;
                }
                break;
            case COMMA:
                {
                alt10=2;
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
                alt10=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(10);}

            switch (alt10) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:194:7: SOLIDUS
                    {
                    dbg.location(194,7);
                    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_operator442); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:195:7: COMMA
                    {
                    dbg.location(195,7);
                    match(input,COMMA,FOLLOW_COMMA_in_operator450); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:197:5: 
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
        dbg.location(197, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:199:1: combinator : ( PLUS | GREATER | TILDE | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(199, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:200:5: ( PLUS | GREATER | TILDE | )
            int alt11=4;
            try { dbg.enterDecision(11, decisionCanBacktrack[11]);

            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt11=1;
                }
                break;
            case GREATER:
                {
                alt11=2;
                }
                break;
            case TILDE:
                {
                alt11=3;
                }
                break;
            case IDENT:
            case COLON:
            case STAR:
            case HASH:
            case DOT:
            case LBRACKET:
            case 81:
                {
                alt11=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(11);}

            switch (alt11) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:200:7: PLUS
                    {
                    dbg.location(200,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator477); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:201:7: GREATER
                    {
                    dbg.location(201,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator485); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:202:7: TILDE
                    {
                    dbg.location(202,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator493); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

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
            dbg.exitRule(getGrammarFileName(), "combinator");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "combinator"


    // $ANTLR start "unaryOperator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:206:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(206, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:207:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(207,5);
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
        dbg.location(209, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:211:1: property : IDENT ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(211, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:212:5: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:212:7: IDENT
            {
            dbg.location(212,7);
            match(input,IDENT,FOLLOW_IDENT_in_property553); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "property");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "property"


    // $ANTLR start "ruleSet"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:215:1: ruleSet : selectorsGroup LBRACE syncToIdent declarations RBRACE ;
    public final void ruleSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ruleSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(215, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:216:5: ( selectorsGroup LBRACE syncToIdent declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:216:9: selectorsGroup LBRACE syncToIdent declarations RBRACE
            {
            dbg.location(216,9);
            pushFollow(FOLLOW_selectorsGroup_in_ruleSet577);
            selectorsGroup();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(217,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_ruleSet587); if (state.failed) return ;
            dbg.location(218,13);
            pushFollow(FOLLOW_syncToIdent_in_ruleSet601);
            syncToIdent();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(219,13);
            pushFollow(FOLLOW_declarations_in_ruleSet616);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(220,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_ruleSet626); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(221, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:223:1: declarations : ( declaration )? ( SEMI ( declaration )? )* ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(223, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:224:5: ( ( declaration )? ( SEMI ( declaration )? )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:226:9: ( declaration )? ( SEMI ( declaration )? )*
            {
            dbg.location(226,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:226:9: ( declaration )?
            int alt12=2;
            try { dbg.enterSubRule(12);
            try { dbg.enterDecision(12, decisionCanBacktrack[12]);

            int LA12_0 = input.LA(1);

            if ( (LA12_0==IDENT) ) {
                alt12=1;
            }
            } finally {dbg.exitDecision(12);}

            switch (alt12) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:226:9: declaration
                    {
                    dbg.location(226,9);
                    pushFollow(FOLLOW_declaration_in_declarations664);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(12);}

            dbg.location(226,22);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:226:22: ( SEMI ( declaration )? )*
            try { dbg.enterSubRule(14);

            loop14:
            do {
                int alt14=2;
                try { dbg.enterDecision(14, decisionCanBacktrack[14]);

                int LA14_0 = input.LA(1);

                if ( (LA14_0==SEMI) ) {
                    alt14=1;
                }


                } finally {dbg.exitDecision(14);}

                switch (alt14) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:226:23: SEMI ( declaration )?
            	    {
            	    dbg.location(226,23);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations668); if (state.failed) return ;
            	    dbg.location(226,28);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:226:28: ( declaration )?
            	    int alt13=2;
            	    try { dbg.enterSubRule(13);
            	    try { dbg.enterDecision(13, decisionCanBacktrack[13]);

            	    int LA13_0 = input.LA(1);

            	    if ( (LA13_0==IDENT) ) {
            	        alt13=1;
            	    }
            	    } finally {dbg.exitDecision(13);}

            	    switch (alt13) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:226:28: declaration
            	            {
            	            dbg.location(226,28);
            	            pushFollow(FOLLOW_declaration_in_declarations670);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(13);}


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
        dbg.location(227, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:229:1: selectorsGroup : selector ( COMMA selector )* ;
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(229, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:230:5: ( selector ( COMMA selector )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:230:7: selector ( COMMA selector )*
            {
            dbg.location(230,7);
            pushFollow(FOLLOW_selector_in_selectorsGroup694);
            selector();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(230,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:230:16: ( COMMA selector )*
            try { dbg.enterSubRule(15);

            loop15:
            do {
                int alt15=2;
                try { dbg.enterDecision(15, decisionCanBacktrack[15]);

                int LA15_0 = input.LA(1);

                if ( (LA15_0==COMMA) ) {
                    alt15=1;
                }


                } finally {dbg.exitDecision(15);}

                switch (alt15) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:230:17: COMMA selector
            	    {
            	    dbg.location(230,17);
            	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup697); if (state.failed) return ;
            	    dbg.location(230,23);
            	    pushFollow(FOLLOW_selector_in_selectorsGroup699);
            	    selector();

            	    state._fsp--;
            	    if (state.failed) return ;

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
        dbg.location(231, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:233:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(233, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:234:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:234:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(234,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector722);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(234,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:234:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(16);

            loop16:
            do {
                int alt16=2;
                try { dbg.enterDecision(16, decisionCanBacktrack[16]);

                int LA16_0 = input.LA(1);

                if ( (LA16_0==IDENT||LA16_0==COLON||(LA16_0>=PLUS && LA16_0<=TILDE)||(LA16_0>=STAR && LA16_0<=LBRACKET)||LA16_0==81) ) {
                    alt16=1;
                }


                } finally {dbg.exitDecision(16);}

                switch (alt16) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:234:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(234,31);
            	    pushFollow(FOLLOW_combinator_in_selector725);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(234,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector727);
            	    simpleSelectorSequence();

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
        dbg.location(235, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:238:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(238, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:240:2: ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) )
            int alt19=2;
            try { dbg.enterDecision(19, decisionCanBacktrack[19]);

            int LA19_0 = input.LA(1);

            if ( (LA19_0==IDENT||LA19_0==STAR||LA19_0==81) ) {
                alt19=1;
            }
            else if ( (LA19_0==COLON||(LA19_0>=HASH && LA19_0<=LBRACKET)) ) {
                alt19=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(19);}

            switch (alt19) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:244:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    {
                    dbg.location(244,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:244:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:244:5: typeSelector ( ( esPred )=> elementSubsequent )*
                    {
                    dbg.location(244,5);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence766);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(244,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:244:18: ( ( esPred )=> elementSubsequent )*
                    try { dbg.enterSubRule(17);

                    loop17:
                    do {
                        int alt17=2;
                        try { dbg.enterDecision(17, decisionCanBacktrack[17]);

                        try {
                            isCyclicDecision = true;
                            alt17 = dfa17.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(17);}

                        switch (alt17) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:244:19: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(244,29);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence773);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop17;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(17);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:246:2: ( ( ( esPred )=> elementSubsequent )+ )
                    {
                    dbg.location(246,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:246:2: ( ( ( esPred )=> elementSubsequent )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:246:4: ( ( esPred )=> elementSubsequent )+
                    {
                    dbg.location(246,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:246:4: ( ( esPred )=> elementSubsequent )+
                    int cnt18=0;
                    try { dbg.enterSubRule(18);

                    loop18:
                    do {
                        int alt18=2;
                        try { dbg.enterDecision(18, decisionCanBacktrack[18]);

                        switch ( input.LA(1) ) {
                        case HASH:
                            {
                            int LA18_2 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt18=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA18_3 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt18=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA18_4 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt18=1;
                            }


                            }
                            break;
                        case COLON:
                            {
                            int LA18_5 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt18=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(18);}

                        switch (alt18) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:246:5: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(246,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence791);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt18 >= 1 ) break loop18;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(18, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt18++;
                    } while (true);
                    } finally {dbg.exitSubRule(18);}


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
        dbg.location(247, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:257:1: typeSelector options {k=2; } : ( ( nsPred )=> namespacePrefix )? ( elementName ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(257, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:259:3: ( ( ( nsPred )=> namespacePrefix )? ( elementName ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:259:6: ( ( nsPred )=> namespacePrefix )? ( elementName )
            {
            dbg.location(259,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:259:6: ( ( nsPred )=> namespacePrefix )?
            int alt20=2;
            try { dbg.enterSubRule(20);
            try { dbg.enterDecision(20, decisionCanBacktrack[20]);

            int LA20_0 = input.LA(1);

            if ( (LA20_0==IDENT||LA20_0==STAR) ) {
                int LA20_1 = input.LA(2);

                if ( (synpred3_Css3()) ) {
                    alt20=1;
                }
            }
            else if ( (LA20_0==81) && (synpred3_Css3())) {
                alt20=1;
            }
            } finally {dbg.exitDecision(20);}

            switch (alt20) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:259:7: ( nsPred )=> namespacePrefix
                    {
                    dbg.location(259,17);
                    pushFollow(FOLLOW_namespacePrefix_in_typeSelector833);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(20);}

            dbg.location(259,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:259:35: ( elementName )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:259:37: elementName
            {
            dbg.location(259,37);
            pushFollow(FOLLOW_elementName_in_typeSelector839);
            elementName();

            state._fsp--;
            if (state.failed) return ;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(260, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:268:2: nsPred : ( IDENT | STAR ) '|' ;
    public final void nsPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "nsPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(268, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:269:3: ( ( IDENT | STAR ) '|' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:270:3: ( IDENT | STAR ) '|'
            {
            dbg.location(270,3);
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

            dbg.location(270,18);
            match(input,81,FOLLOW_81_in_nsPred876); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(271, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:273:2: namespacePrefix : ( namespaceName )? '|' ;
    public final void namespacePrefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(273, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:274:3: ( ( namespaceName )? '|' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:274:5: ( namespaceName )? '|'
            {
            dbg.location(274,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:274:5: ( namespaceName )?
            int alt21=2;
            try { dbg.enterSubRule(21);
            try { dbg.enterDecision(21, decisionCanBacktrack[21]);

            int LA21_0 = input.LA(1);

            if ( (LA21_0==IDENT||LA21_0==STAR) ) {
                alt21=1;
            }
            } finally {dbg.exitDecision(21);}

            switch (alt21) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:274:7: namespaceName
                    {
                    dbg.location(274,7);
                    pushFollow(FOLLOW_namespaceName_in_namespacePrefix896);
                    namespaceName();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(21);}

            dbg.location(274,23);
            match(input,81,FOLLOW_81_in_namespacePrefix900); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(275, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:277:2: namespaceName : ( IDENT | STAR );
    public final void namespaceName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespaceName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(277, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:278:3: ( IDENT | STAR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
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


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(279, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:281:1: esPred : ( HASH | DOT | LBRACKET | COLON );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(281, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:282:5: ( HASH | DOT | LBRACKET | COLON )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(282,5);
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
        dbg.location(283, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:285:1: elementSubsequent : ( cssId | cssClass | attrib | pseudo );
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(285, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:286:5: ( cssId | cssClass | attrib | pseudo )
            int alt22=4;
            try { dbg.enterDecision(22, decisionCanBacktrack[22]);

            switch ( input.LA(1) ) {
            case HASH:
                {
                alt22=1;
                }
                break;
            case DOT:
                {
                alt22=2;
                }
                break;
            case LBRACKET:
                {
                alt22=3;
                }
                break;
            case COLON:
                {
                alt22=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(22);}

            switch (alt22) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:286:7: cssId
                    {
                    dbg.location(286,7);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent982);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:7: cssClass
                    {
                    dbg.location(287,7);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent990);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:288:7: attrib
                    {
                    dbg.location(288,7);
                    pushFollow(FOLLOW_attrib_in_elementSubsequent998);
                    attrib();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:289:7: pseudo
                    {
                    dbg.location(289,7);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent1006);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

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
        dbg.location(290, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:292:1: cssId : HASH ;
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(292, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:293:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:293:7: HASH
            {
            dbg.location(293,7);
            match(input,HASH,FOLLOW_HASH_in_cssId1027); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(294, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:296:1: cssClass : DOT IDENT ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(296, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:297:5: ( DOT IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:297:7: DOT IDENT
            {
            dbg.location(297,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass1044); if (state.failed) return ;
            dbg.location(297,11);
            match(input,IDENT,FOLLOW_IDENT_in_cssClass1046); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(298, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:301:1: elementName : ( IDENT | '*' );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(301, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:302:5: ( IDENT | '*' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(302,5);
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
        dbg.location(303, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:305:1: attrib : LBRACKET ( namespacePrefix )? IDENT ( ( OPEQ | INCLUDES | DASHMATCH ) ( IDENT | STRING ) )? RBRACKET ;
    public final void attrib() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrib");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(305, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:5: ( LBRACKET ( namespacePrefix )? IDENT ( ( OPEQ | INCLUDES | DASHMATCH ) ( IDENT | STRING ) )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:7: LBRACKET ( namespacePrefix )? IDENT ( ( OPEQ | INCLUDES | DASHMATCH ) ( IDENT | STRING ) )? RBRACKET
            {
            dbg.location(306,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_attrib1093); if (state.failed) return ;
            dbg.location(307,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:307:6: ( namespacePrefix )?
            int alt23=2;
            try { dbg.enterSubRule(23);
            try { dbg.enterDecision(23, decisionCanBacktrack[23]);

            int LA23_0 = input.LA(1);

            if ( (LA23_0==IDENT) ) {
                int LA23_1 = input.LA(2);

                if ( (LA23_1==81) ) {
                    alt23=1;
                }
            }
            else if ( (LA23_0==STAR||LA23_0==81) ) {
                alt23=1;
            }
            } finally {dbg.exitDecision(23);}

            switch (alt23) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:307:6: namespacePrefix
                    {
                    dbg.location(307,6);
                    pushFollow(FOLLOW_namespacePrefix_in_attrib1100);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(23);}

            dbg.location(308,9);
            match(input,IDENT,FOLLOW_IDENT_in_attrib1111); if (state.failed) return ;
            dbg.location(310,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:13: ( ( OPEQ | INCLUDES | DASHMATCH ) ( IDENT | STRING ) )?
            int alt24=2;
            try { dbg.enterSubRule(24);
            try { dbg.enterDecision(24, decisionCanBacktrack[24]);

            int LA24_0 = input.LA(1);

            if ( ((LA24_0>=OPEQ && LA24_0<=DASHMATCH)) ) {
                alt24=1;
            }
            } finally {dbg.exitDecision(24);}

            switch (alt24) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:311:17: ( OPEQ | INCLUDES | DASHMATCH ) ( IDENT | STRING )
                    {
                    dbg.location(311,17);
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

                    dbg.location(316,17);
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


                    }
                    break;

            }
            } finally {dbg.exitSubRule(24);}

            dbg.location(322,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_attrib1361); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(323, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:325:1: pseudo : COLON IDENT ( LPAREN ( IDENT )? RPAREN )? ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(325, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:326:5: ( COLON IDENT ( LPAREN ( IDENT )? RPAREN )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:326:7: COLON IDENT ( LPAREN ( IDENT )? RPAREN )?
            {
            dbg.location(326,7);
            match(input,COLON,FOLLOW_COLON_in_pseudo1374); if (state.failed) return ;
            dbg.location(327,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudo1389); if (state.failed) return ;
            dbg.location(328,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:328:17: ( LPAREN ( IDENT )? RPAREN )?
            int alt26=2;
            try { dbg.enterSubRule(26);
            try { dbg.enterDecision(26, decisionCanBacktrack[26]);

            int LA26_0 = input.LA(1);

            if ( (LA26_0==LPAREN) ) {
                alt26=1;
            }
            } finally {dbg.exitDecision(26);}

            switch (alt26) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:330:21: LPAREN ( IDENT )? RPAREN
                    {
                    dbg.location(330,21);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo1447); if (state.failed) return ;
                    dbg.location(330,28);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:330:28: ( IDENT )?
                    int alt25=2;
                    try { dbg.enterSubRule(25);
                    try { dbg.enterDecision(25, decisionCanBacktrack[25]);

                    int LA25_0 = input.LA(1);

                    if ( (LA25_0==IDENT) ) {
                        alt25=1;
                    }
                    } finally {dbg.exitDecision(25);}

                    switch (alt25) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:330:28: IDENT
                            {
                            dbg.location(330,28);
                            match(input,IDENT,FOLLOW_IDENT_in_pseudo1449); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(25);}

                    dbg.location(330,35);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo1452); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(26);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(332, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:334:1: declaration : property COLON expr ( prio )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(334, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:335:5: ( property COLON expr ( prio )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:5: property COLON expr ( prio )?
            {
            dbg.location(338,5);
            pushFollow(FOLLOW_property_in_declaration1503);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(338,14);
            match(input,COLON,FOLLOW_COLON_in_declaration1505); if (state.failed) return ;
            dbg.location(338,20);
            pushFollow(FOLLOW_expr_in_declaration1507);
            expr();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(338,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:25: ( prio )?
            int alt27=2;
            try { dbg.enterSubRule(27);
            try { dbg.enterDecision(27, decisionCanBacktrack[27]);

            int LA27_0 = input.LA(1);

            if ( (LA27_0==IMPORTANT_SYM) ) {
                alt27=1;
            }
            } finally {dbg.exitDecision(27);}

            switch (alt27) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:25: prio
                    {
                    dbg.location(338,25);
                    pushFollow(FOLLOW_prio_in_declaration1509);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(27);}


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
        dbg.location(339, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:1: syncToIdent : ;
    public final void syncToIdent() throws RecognitionException {

                syncToSet(BitSet.of(IDENT));
            
        try { dbg.enterRule(getGrammarFileName(), "syncToIdent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(349, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:6: 
            {
            }

        }
        finally {
        }
        dbg.location(355, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:357:1: prio : IMPORTANT_SYM ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(357, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:358:5: ( IMPORTANT_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:358:7: IMPORTANT_SYM
            {
            dbg.location(358,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio1575); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(359, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:361:1: expr : term ( operator term )* ;
    public final void expr() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expr");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(361, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:5: ( term ( operator term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:7: term ( operator term )*
            {
            dbg.location(362,7);
            pushFollow(FOLLOW_term_in_expr1596);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(362,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:12: ( operator term )*
            try { dbg.enterSubRule(28);

            loop28:
            do {
                int alt28=2;
                try { dbg.enterDecision(28, decisionCanBacktrack[28]);

                int LA28_0 = input.LA(1);

                if ( (LA28_0==COMMA) ) {
                    int LA28_2 = input.LA(2);

                    if ( (LA28_2==IDENT) ) {
                        int LA28_4 = input.LA(3);

                        if ( ((LA28_4>=STRING && LA28_4<=SEMI)||(LA28_4>=URI && LA28_4<=COMMA)||(LA28_4>=RBRACE && LA28_4<=IDENT)||(LA28_4>=SOLIDUS && LA28_4<=PLUS)||LA28_4==MINUS||LA28_4==HASH||(LA28_4>=LPAREN && LA28_4<=FREQ)) ) {
                            alt28=1;
                        }


                    }
                    else if ( (LA28_2==STRING||LA28_2==URI||LA28_2==PLUS||LA28_2==MINUS||LA28_2==HASH||(LA28_2>=NUMBER && LA28_2<=FREQ)) ) {
                        alt28=1;
                    }


                }
                else if ( (LA28_0==STRING||LA28_0==URI||LA28_0==IDENT||(LA28_0>=SOLIDUS && LA28_0<=PLUS)||LA28_0==MINUS||LA28_0==HASH||(LA28_0>=NUMBER && LA28_0<=FREQ)) ) {
                    alt28=1;
                }


                } finally {dbg.exitDecision(28);}

                switch (alt28) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:13: operator term
            	    {
            	    dbg.location(362,13);
            	    pushFollow(FOLLOW_operator_in_expr1599);
            	    operator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(362,22);
            	    pushFollow(FOLLOW_term_in_expr1601);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

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
        dbg.location(363, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:1: term : ( ( unaryOperator )? ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | URI | hexColor | function );
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(365, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:366:5: ( ( unaryOperator )? ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | URI | hexColor | function )
            int alt30=6;
            try { dbg.enterDecision(30, decisionCanBacktrack[30]);

            switch ( input.LA(1) ) {
            case PLUS:
            case MINUS:
            case NUMBER:
            case PERCENTAGE:
            case LENGTH:
            case EMS:
            case EXS:
            case ANGLE:
            case TIME:
            case FREQ:
                {
                alt30=1;
                }
                break;
            case STRING:
                {
                alt30=2;
                }
                break;
            case IDENT:
                {
                int LA30_3 = input.LA(2);

                if ( ((LA30_3>=STRING && LA30_3<=SEMI)||(LA30_3>=URI && LA30_3<=COMMA)||(LA30_3>=RBRACE && LA30_3<=IDENT)||(LA30_3>=SOLIDUS && LA30_3<=PLUS)||LA30_3==MINUS||LA30_3==HASH||(LA30_3>=RPAREN && LA30_3<=FREQ)) ) {
                    alt30=3;
                }
                else if ( (LA30_3==LPAREN) ) {
                    alt30=6;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 30, 3, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                }
                break;
            case URI:
                {
                alt30=4;
                }
                break;
            case HASH:
                {
                alt30=5;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:366:7: ( unaryOperator )? ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ )
                    {
                    dbg.location(366,7);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:366:7: ( unaryOperator )?
                    int alt29=2;
                    try { dbg.enterSubRule(29);
                    try { dbg.enterDecision(29, decisionCanBacktrack[29]);

                    int LA29_0 = input.LA(1);

                    if ( (LA29_0==PLUS||LA29_0==MINUS) ) {
                        alt29=1;
                    }
                    } finally {dbg.exitDecision(29);}

                    switch (alt29) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:366:7: unaryOperator
                            {
                            dbg.location(366,7);
                            pushFollow(FOLLOW_unaryOperator_in_term1624);
                            unaryOperator();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(29);}

                    dbg.location(367,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:7: STRING
                    {
                    dbg.location(377,7);
                    match(input,STRING,FOLLOW_STRING_in_term1781); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:378:7: IDENT
                    {
                    dbg.location(378,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term1789); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:379:7: URI
                    {
                    dbg.location(379,7);
                    match(input,URI,FOLLOW_URI_in_term1797); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:7: hexColor
                    {
                    dbg.location(380,7);
                    pushFollow(FOLLOW_hexColor_in_term1805);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:7: function
                    {
                    dbg.location(381,7);
                    pushFollow(FOLLOW_function_in_term1813);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

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
        dbg.location(382, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:384:1: function : function_name LPAREN ( expr | ( attribute ( COMMA attribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(384, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:2: ( function_name LPAREN ( expr | ( attribute ( COMMA attribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:5: function_name LPAREN ( expr | ( attribute ( COMMA attribute )* ) ) RPAREN
            {
            dbg.location(385,5);
            pushFollow(FOLLOW_function_name_in_function1828);
            function_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(386,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function1833); if (state.failed) return ;
            dbg.location(387,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:387:3: ( expr | ( attribute ( COMMA attribute )* ) )
            int alt32=2;
            try { dbg.enterSubRule(32);
            try { dbg.enterDecision(32, decisionCanBacktrack[32]);

            int LA32_0 = input.LA(1);

            if ( (LA32_0==STRING||LA32_0==URI||LA32_0==PLUS||LA32_0==MINUS||LA32_0==HASH||(LA32_0>=NUMBER && LA32_0<=FREQ)) ) {
                alt32=1;
            }
            else if ( (LA32_0==IDENT) ) {
                int LA32_2 = input.LA(2);

                if ( (LA32_2==STRING||(LA32_2>=URI && LA32_2<=COMMA)||LA32_2==IDENT||(LA32_2>=SOLIDUS && LA32_2<=PLUS)||LA32_2==MINUS||LA32_2==HASH||(LA32_2>=LPAREN && LA32_2<=RPAREN)||(LA32_2>=NUMBER && LA32_2<=FREQ)) ) {
                    alt32=1;
                }
                else if ( (LA32_2==OPEQ) ) {
                    alt32=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 32, 2, input);

                    dbg.recognitionException(nvae);
                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(32);}

            switch (alt32) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:388:4: expr
                    {
                    dbg.location(388,4);
                    pushFollow(FOLLOW_expr_in_function1844);
                    expr();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:6: ( attribute ( COMMA attribute )* )
                    {
                    dbg.location(390,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:6: ( attribute ( COMMA attribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:391:5: attribute ( COMMA attribute )*
                    {
                    dbg.location(391,5);
                    pushFollow(FOLLOW_attribute_in_function1863);
                    attribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(391,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:391:15: ( COMMA attribute )*
                    try { dbg.enterSubRule(31);

                    loop31:
                    do {
                        int alt31=2;
                        try { dbg.enterDecision(31, decisionCanBacktrack[31]);

                        int LA31_0 = input.LA(1);

                        if ( (LA31_0==COMMA) ) {
                            alt31=1;
                        }


                        } finally {dbg.exitDecision(31);}

                        switch (alt31) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:391:16: COMMA attribute
                    	    {
                    	    dbg.location(391,16);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function1866); if (state.failed) return ;
                    	    dbg.location(391,22);
                    	    pushFollow(FOLLOW_attribute_in_function1868);
                    	    attribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop31;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(31);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(32);}

            dbg.location(394,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function1888); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(395, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:397:1: function_name : IDENT ;
    public final void function_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(397, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:398:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:398:4: IDENT
            {
            dbg.location(398,4);
            match(input,IDENT,FOLLOW_IDENT_in_function_name1903); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(399, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:1: attribute : attrname OPEQ attrvalue ;
    public final void attribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(401, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:2: ( attrname OPEQ attrvalue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:4: attrname OPEQ attrvalue
            {
            dbg.location(402,4);
            pushFollow(FOLLOW_attrname_in_attribute1923);
            attrname();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(402,13);
            match(input,OPEQ,FOLLOW_OPEQ_in_attribute1925); if (state.failed) return ;
            dbg.location(402,18);
            pushFollow(FOLLOW_attrvalue_in_attribute1927);
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
        dbg.location(403, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:405:1: attrname : IDENT ;
    public final void attrname() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrname");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(405, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:4: IDENT
            {
            dbg.location(406,4);
            match(input,IDENT,FOLLOW_IDENT_in_attrname1942); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(407, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:1: attrvalue : expr ;
    public final void attrvalue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrvalue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(409, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:2: ( expr )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:4: expr
            {
            dbg.location(410,4);
            pushFollow(FOLLOW_expr_in_attrvalue1954);
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
        dbg.location(411, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:413:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(413, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:7: HASH
            {
            dbg.location(414,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor1972); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(415, 5);

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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:244:19: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:244:20: esPred
        {
        dbg.location(244,20);
        pushFollow(FOLLOW_esPred_in_synpred1_Css3770);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:246:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:246:6: esPred
        {
        dbg.location(246,6);
        pushFollow(FOLLOW_esPred_in_synpred2_Css3788);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:259:7: ( nsPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:259:8: nsPred
        {
        dbg.location(259,8);
        pushFollow(FOLLOW_nsPred_in_synpred3_Css3830);
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


    protected DFA17 dfa17 = new DFA17(this);
    static final String DFA17_eotS =
        "\20\uffff";
    static final String DFA17_eofS =
        "\20\uffff";
    static final String DFA17_minS =
        "\1\11\1\uffff\1\0\3\15\1\uffff\1\0\1\31\1\15\1\121\1\0\1\5\1\0\1"+
        "\31\1\34";
    static final String DFA17_maxS =
        "\1\121\1\uffff\1\0\1\15\1\121\1\15\1\uffff\1\0\1\121\1\15\1\121"+
        "\1\0\1\15\1\0\2\34";
    static final String DFA17_acceptS =
        "\1\uffff\1\2\4\uffff\1\1\11\uffff";
    static final String DFA17_specialS =
        "\2\uffff\1\2\4\uffff\1\0\3\uffff\1\1\1\uffff\1\3\2\uffff}>";
    static final String[] DFA17_transitionS = {
            "\1\1\1\uffff\1\1\1\uffff\1\1\1\uffff\1\5\1\uffff\3\1\1\uffff"+
            "\1\1\1\2\1\3\1\4\70\uffff\1\1",
            "",
            "\1\uffff",
            "\1\7",
            "\1\10\7\uffff\1\12\73\uffff\1\11",
            "\1\13",
            "",
            "\1\uffff",
            "\3\14\1\15\64\uffff\1\11",
            "\1\16",
            "\1\11",
            "\1\uffff",
            "\1\17\7\uffff\1\17",
            "\1\uffff",
            "\3\14\1\15",
            "\1\15"
    };

    static final short[] DFA17_eot = DFA.unpackEncodedString(DFA17_eotS);
    static final short[] DFA17_eof = DFA.unpackEncodedString(DFA17_eofS);
    static final char[] DFA17_min = DFA.unpackEncodedStringToUnsignedChars(DFA17_minS);
    static final char[] DFA17_max = DFA.unpackEncodedStringToUnsignedChars(DFA17_maxS);
    static final short[] DFA17_accept = DFA.unpackEncodedString(DFA17_acceptS);
    static final short[] DFA17_special = DFA.unpackEncodedString(DFA17_specialS);
    static final short[][] DFA17_transition;

    static {
        int numStates = DFA17_transitionS.length;
        DFA17_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA17_transition[i] = DFA.unpackEncodedString(DFA17_transitionS[i]);
        }
    }

    class DFA17 extends DFA {

        public DFA17(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 17;
            this.eot = DFA17_eot;
            this.eof = DFA17_eof;
            this.min = DFA17_min;
            this.max = DFA17_max;
            this.accept = DFA17_accept;
            this.special = DFA17_special;
            this.transition = DFA17_transition;
        }
        public String getDescription() {
            return "()* loopback of 244:18: ( ( esPred )=> elementSubsequent )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA17_7 = input.LA(1);

                         
                        int index17_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index17_7);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA17_11 = input.LA(1);

                         
                        int index17_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index17_11);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA17_2 = input.LA(1);

                         
                        int index17_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index17_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA17_13 = input.LA(1);

                         
                        int index17_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index17_13);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 17, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_charSet_in_styleSheet77 = new BitSet(new long[]{0x0000000001E0E480L,0x0000000000020000L});
    public static final BitSet FOLLOW_imports_in_styleSheet87 = new BitSet(new long[]{0x0000000001E0E480L,0x0000000000020000L});
    public static final BitSet FOLLOW_bodylist_in_styleSheet98 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_styleSheet105 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHARSET_SYM_in_charSet131 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_STRING_in_charSet133 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_SEMI_in_charSet135 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORT_SYM_in_imports163 = new BitSet(new long[]{0x0000000000000120L});
    public static final BitSet FOLLOW_set_in_imports165 = new BitSet(new long[]{0x0000000000002040L});
    public static final BitSet FOLLOW_medium_in_imports172 = new BitSet(new long[]{0x0000000000000240L});
    public static final BitSet FOLLOW_COMMA_in_imports175 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_medium_in_imports177 = new BitSet(new long[]{0x0000000000000240L});
    public static final BitSet FOLLOW_SEMI_in_imports183 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEDIA_SYM_in_media204 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_medium_in_media206 = new BitSet(new long[]{0x0000000000000A00L});
    public static final BitSet FOLLOW_COMMA_in_media209 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_medium_in_media211 = new BitSet(new long[]{0x0000000000000A00L});
    public static final BitSet FOLLOW_LBRACE_in_media223 = new BitSet(new long[]{0x0000000001E0A000L,0x0000000000020000L});
    public static final BitSet FOLLOW_ruleSet_in_media237 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_RBRACE_in_media247 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_medium267 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bodyset_in_bodylist290 = new BitSet(new long[]{0x0000000001E0E402L,0x0000000000020000L});
    public static final BitSet FOLLOW_ruleSet_in_bodyset312 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_media_in_bodyset320 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_page_in_bodyset328 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page352 = new BitSet(new long[]{0x0000000000008800L});
    public static final BitSet FOLLOW_pseudoPage_in_page354 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_LBRACE_in_page365 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_declaration_in_page379 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_SEMI_in_page381 = new BitSet(new long[]{0x0000000000003000L});
    public static final BitSet FOLLOW_declaration_in_page384 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_SEMI_in_page386 = new BitSet(new long[]{0x0000000000003000L});
    public static final BitSet FOLLOW_RBRACE_in_page398 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage419 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage421 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_operator442 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMMA_in_operator450 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_combinator477 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_combinator485 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_combinator493 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_property553 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectorsGroup_in_ruleSet577 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_LBRACE_in_ruleSet587 = new BitSet(new long[]{0x0000000000003040L});
    public static final BitSet FOLLOW_syncToIdent_in_ruleSet601 = new BitSet(new long[]{0x0000000000003040L});
    public static final BitSet FOLLOW_declarations_in_ruleSet616 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_RBRACE_in_ruleSet626 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_declarations664 = new BitSet(new long[]{0x0000000000000042L});
    public static final BitSet FOLLOW_SEMI_in_declarations668 = new BitSet(new long[]{0x0000000000002042L});
    public static final BitSet FOLLOW_declaration_in_declarations670 = new BitSet(new long[]{0x0000000000000042L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup694 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup697 = new BitSet(new long[]{0x0000000001E0A000L,0x0000000000020000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup699 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector722 = new BitSet(new long[]{0x0000000001EEA002L,0x0000000000020000L});
    public static final BitSet FOLLOW_combinator_in_selector725 = new BitSet(new long[]{0x0000000001E0A000L,0x0000000000020000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector727 = new BitSet(new long[]{0x0000000001EEA002L,0x0000000000020000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence766 = new BitSet(new long[]{0x0000000001E0A002L,0x0000000000020000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence773 = new BitSet(new long[]{0x0000000001E0A002L,0x0000000000020000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence791 = new BitSet(new long[]{0x0000000001E0A002L,0x0000000000020000L});
    public static final BitSet FOLLOW_namespacePrefix_in_typeSelector833 = new BitSet(new long[]{0x0000000000202000L,0x0000000000020000L});
    public static final BitSet FOLLOW_elementName_in_typeSelector839 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_nsPred868 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_81_in_nsPred876 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespaceName_in_namespacePrefix896 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_81_in_namespacePrefix900 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_namespaceName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent982 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent990 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attrib_in_elementSubsequent998 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent1006 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_cssId1027 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass1044 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_IDENT_in_cssClass1046 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_attrib1093 = new BitSet(new long[]{0x0000000000202000L,0x0000000000020000L});
    public static final BitSet FOLLOW_namespacePrefix_in_attrib1100 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_IDENT_in_attrib1111 = new BitSet(new long[]{0x000000001E000000L});
    public static final BitSet FOLLOW_set_in_attrib1152 = new BitSet(new long[]{0x0000000000002020L});
    public static final BitSet FOLLOW_set_in_attrib1260 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RBRACKET_in_attrib1361 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudo1374 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_IDENT_in_pseudo1389 = new BitSet(new long[]{0x0000000020000002L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo1447 = new BitSet(new long[]{0x0000000040002000L});
    public static final BitSet FOLLOW_IDENT_in_pseudo1449 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo1452 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_declaration1503 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_COLON_in_declaration1505 = new BitSet(new long[]{0x000000FF00522120L});
    public static final BitSet FOLLOW_expr_in_declaration1507 = new BitSet(new long[]{0x0000000080000002L});
    public static final BitSet FOLLOW_prio_in_declaration1509 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio1575 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expr1596 = new BitSet(new long[]{0x000000FF00532322L});
    public static final BitSet FOLLOW_operator_in_expr1599 = new BitSet(new long[]{0x000000FF00522120L});
    public static final BitSet FOLLOW_term_in_expr1601 = new BitSet(new long[]{0x000000FF00532322L});
    public static final BitSet FOLLOW_unaryOperator_in_term1624 = new BitSet(new long[]{0x000000FF00000000L});
    public static final BitSet FOLLOW_set_in_term1635 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_term1781 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_term1789 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_URI_in_term1797 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_hexColor_in_term1805 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_in_term1813 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_name_in_function1828 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_LPAREN_in_function1833 = new BitSet(new long[]{0x000000FF00522120L});
    public static final BitSet FOLLOW_expr_in_function1844 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_attribute_in_function1863 = new BitSet(new long[]{0x0000000040000200L});
    public static final BitSet FOLLOW_COMMA_in_function1866 = new BitSet(new long[]{0x000000FF00522120L});
    public static final BitSet FOLLOW_attribute_in_function1868 = new BitSet(new long[]{0x0000000040000200L});
    public static final BitSet FOLLOW_RPAREN_in_function1888 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_function_name1903 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attrname_in_attribute1923 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_OPEQ_in_attribute1925 = new BitSet(new long[]{0x000000FF00522120L});
    public static final BitSet FOLLOW_attrvalue_in_attribute1927 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_attrname1942 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_in_attrvalue1954 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor1972 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred1_Css3770 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred2_Css3788 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nsPred_in_synpred3_Css3830 = new BitSet(new long[]{0x0000000000000002L});

}