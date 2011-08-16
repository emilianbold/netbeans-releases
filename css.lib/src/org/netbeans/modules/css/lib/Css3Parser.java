// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2011-08-15 15:40:09

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "WS", "NAMESPACE_SYM", "IDENT", "STRING", "URI", "CHARSET_SYM", "SEMI", "IMPORT_SYM", "MEDIA_SYM", "LBRACE", "RBRACE", "COMMA", "GEN", "PAGE_SYM", "COLON", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "STAR", "PIPE", "HASH", "DOT", "LBRACKET", "OPEQ", "INCLUDES", "DASHMATCH", "RBRACKET", "LPAREN", "RPAREN", "IMPORTANT_SYM", "NUMBER", "PERCENTAGE", "LENGTH", "EMS", "EXS", "ANGLE", "TIME", "FREQ", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "NAME", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "COMMENT", "CDO", "CDC", "INVALID", "DIMENSION", "NL"
    };
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
    public static final int OPEQ=29;
    public static final int INCLUDES=30;
    public static final int DASHMATCH=31;
    public static final int RBRACKET=32;
    public static final int LPAREN=33;
    public static final int RPAREN=34;
    public static final int IMPORTANT_SYM=35;
    public static final int NUMBER=36;
    public static final int PERCENTAGE=37;
    public static final int LENGTH=38;
    public static final int EMS=39;
    public static final int EXS=40;
    public static final int ANGLE=41;
    public static final int TIME=42;
    public static final int FREQ=43;
    public static final int HEXCHAR=44;
    public static final int NONASCII=45;
    public static final int UNICODE=46;
    public static final int ESCAPE=47;
    public static final int NMSTART=48;
    public static final int NMCHAR=49;
    public static final int NAME=50;
    public static final int URL=51;
    public static final int A=52;
    public static final int B=53;
    public static final int C=54;
    public static final int D=55;
    public static final int E=56;
    public static final int F=57;
    public static final int G=58;
    public static final int H=59;
    public static final int I=60;
    public static final int J=61;
    public static final int K=62;
    public static final int L=63;
    public static final int M=64;
    public static final int N=65;
    public static final int O=66;
    public static final int P=67;
    public static final int Q=68;
    public static final int R=69;
    public static final int S=70;
    public static final int T=71;
    public static final int U=72;
    public static final int V=73;
    public static final int W=74;
    public static final int X=75;
    public static final int Y=76;
    public static final int Z=77;
    public static final int COMMENT=78;
    public static final int CDO=79;
    public static final int CDC=80;
    public static final int INVALID=81;
    public static final int DIMENSION=82;
    public static final int NL=83;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "expr", "resourceIdentifier", "bodylist", "medium", 
        "namespace_wildcard_prefix", "styleSheet", "media", "function", 
        "ruleSet", "namespace_wqname_prefix", "syncTo_IDENT_RBRACKET_LBRACE", 
        "attrvalue", "combinator", "cssClass", "declaration", "selectorsGroup", 
        "property", "attrib_value", "typeSelector", "elementSubsequent", 
        "function_name", "hexColor", "selector", "synpred3_Css3", "attrib_name", 
        "syncToFollow", "term", "simpleSelectorSequence", "namespace_prefix", 
        "namespace", "imports", "cssId", "pseudo", "bodyset", "elementName", 
        "attrname", "attrib", "unaryOperator", "nsPred", "esPred", "synpred1_Css3", 
        "attribute", "charSet", "declarations", "mediaList", "syncTo_IDENT_RBRACE", 
        "operator", "pseudoPage", "prio", "page", "synpred2_Css3"
    };
    public static final boolean[] decisionCanBacktrack = new boolean[] {
        false, // invalid decision
        false, false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, true, 
            true, false, true, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:133:1: styleSheet : ( charSet )? ( WS )* ( imports ( WS )* )* ( namespace )* bodylist EOF ;
    public final void styleSheet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "styleSheet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(133, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:134:5: ( ( charSet )? ( WS )* ( imports ( WS )* )* ( namespace )* bodylist EOF )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:134:9: ( charSet )? ( WS )* ( imports ( WS )* )* ( namespace )* bodylist EOF
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:137:9: ( namespace )*
            try { dbg.enterSubRule(5);

            loop5:
            do {
                int alt5=2;
                try { dbg.enterDecision(5, decisionCanBacktrack[5]);

                int LA5_0 = input.LA(1);

                if ( (LA5_0==NAMESPACE_SYM) ) {
                    alt5=1;
                }


                } finally {dbg.exitDecision(5);}

                switch (alt5) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:137:9: namespace
            	    {
            	    dbg.location(137,9);
            	    pushFollow(FOLLOW_namespace_in_styleSheet116);
            	    namespace();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);
            } finally {dbg.exitSubRule(5);}

            dbg.location(138,9);
            pushFollow(FOLLOW_bodylist_in_styleSheet127);
            bodylist();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(139,6);
            match(input,EOF,FOLLOW_EOF_in_styleSheet134); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(140, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "styleSheet");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "styleSheet"


    // $ANTLR start "namespace"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:142:1: namespace : NAMESPACE_SYM ( WS )* ( namespace_prefix ( WS )* )? ( resourceIdentifier ) ( WS )* ';' ( WS )* ;
    public final void namespace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(142, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:143:3: ( NAMESPACE_SYM ( WS )* ( namespace_prefix ( WS )* )? ( resourceIdentifier ) ( WS )* ';' ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:143:5: NAMESPACE_SYM ( WS )* ( namespace_prefix ( WS )* )? ( resourceIdentifier ) ( WS )* ';' ( WS )*
            {
            dbg.location(143,5);
            match(input,NAMESPACE_SYM,FOLLOW_NAMESPACE_SYM_in_namespace149); if (state.failed) return ;
            dbg.location(143,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:143:19: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:143:19: WS
            	    {
            	    dbg.location(143,19);
            	    match(input,WS,FOLLOW_WS_in_namespace151); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);
            } finally {dbg.exitSubRule(6);}

            dbg.location(143,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:143:23: ( namespace_prefix ( WS )* )?
            int alt8=2;
            try { dbg.enterSubRule(8);
            try { dbg.enterDecision(8, decisionCanBacktrack[8]);

            int LA8_0 = input.LA(1);

            if ( (LA8_0==IDENT) ) {
                alt8=1;
            }
            } finally {dbg.exitDecision(8);}

            switch (alt8) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:143:24: namespace_prefix ( WS )*
                    {
                    dbg.location(143,24);
                    pushFollow(FOLLOW_namespace_prefix_in_namespace155);
                    namespace_prefix();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(143,41);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:143:41: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:143:41: WS
                    	    {
                    	    dbg.location(143,41);
                    	    match(input,WS,FOLLOW_WS_in_namespace157); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop7;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(7);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(8);}

            dbg.location(143,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:143:47: ( resourceIdentifier )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:143:48: resourceIdentifier
            {
            dbg.location(143,48);
            pushFollow(FOLLOW_resourceIdentifier_in_namespace163);
            resourceIdentifier();

            state._fsp--;
            if (state.failed) return ;

            }

            dbg.location(143,68);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:143:68: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:143:68: WS
            	    {
            	    dbg.location(143,68);
            	    match(input,WS,FOLLOW_WS_in_namespace166); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);
            } finally {dbg.exitSubRule(9);}

            dbg.location(143,72);
            match(input,SEMI,FOLLOW_SEMI_in_namespace169); if (state.failed) return ;
            dbg.location(143,76);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:143:76: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:143:76: WS
            	    {
            	    dbg.location(143,76);
            	    match(input,WS,FOLLOW_WS_in_namespace171); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);
            } finally {dbg.exitSubRule(10);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(144, 3);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "namespace");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "namespace"


    // $ANTLR start "namespace_prefix"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:146:1: namespace_prefix : IDENT ;
    public final void namespace_prefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace_prefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(146, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:147:3: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:147:5: IDENT
            {
            dbg.location(147,5);
            match(input,IDENT,FOLLOW_IDENT_in_namespace_prefix185); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(148, 3);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "namespace_prefix");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "namespace_prefix"


    // $ANTLR start "resourceIdentifier"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:150:1: resourceIdentifier : ( STRING | URI );
    public final void resourceIdentifier() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "resourceIdentifier");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(150, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:151:3: ( STRING | URI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(151,3);
            if ( (input.LA(1)>=STRING && input.LA(1)<=URI) ) {
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
        dbg.location(152, 3);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "resourceIdentifier");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "resourceIdentifier"


    // $ANTLR start "charSet"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:157:1: charSet : CHARSET_SYM ( WS )* STRING ( WS )* SEMI ;
    public final void charSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "charSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(157, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:158:5: ( CHARSET_SYM ( WS )* STRING ( WS )* SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:158:9: CHARSET_SYM ( WS )* STRING ( WS )* SEMI
            {
            dbg.location(158,9);
            match(input,CHARSET_SYM,FOLLOW_CHARSET_SYM_in_charSet224); if (state.failed) return ;
            dbg.location(158,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:158:21: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:158:21: WS
            	    {
            	    dbg.location(158,21);
            	    match(input,WS,FOLLOW_WS_in_charSet226); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);
            } finally {dbg.exitSubRule(11);}

            dbg.location(158,25);
            match(input,STRING,FOLLOW_STRING_in_charSet229); if (state.failed) return ;
            dbg.location(158,32);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:158:32: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:158:32: WS
            	    {
            	    dbg.location(158,32);
            	    match(input,WS,FOLLOW_WS_in_charSet231); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop12;
                }
            } while (true);
            } finally {dbg.exitSubRule(12);}

            dbg.location(158,36);
            match(input,SEMI,FOLLOW_SEMI_in_charSet234); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(159, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:164:1: imports : IMPORT_SYM ( WS )* ( resourceIdentifier ) ( WS )* ( mediaList )? SEMI ;
    public final void imports() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "imports");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(164, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:165:5: ( IMPORT_SYM ( WS )* ( resourceIdentifier ) ( WS )* ( mediaList )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:165:9: IMPORT_SYM ( WS )* ( resourceIdentifier ) ( WS )* ( mediaList )? SEMI
            {
            dbg.location(165,9);
            match(input,IMPORT_SYM,FOLLOW_IMPORT_SYM_in_imports256); if (state.failed) return ;
            dbg.location(165,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:165:20: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:165:20: WS
            	    {
            	    dbg.location(165,20);
            	    match(input,WS,FOLLOW_WS_in_imports258); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);
            } finally {dbg.exitSubRule(13);}

            dbg.location(165,24);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:165:24: ( resourceIdentifier )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:165:25: resourceIdentifier
            {
            dbg.location(165,25);
            pushFollow(FOLLOW_resourceIdentifier_in_imports262);
            resourceIdentifier();

            state._fsp--;
            if (state.failed) return ;

            }

            dbg.location(165,45);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:165:45: ( WS )*
            try { dbg.enterSubRule(14);

            loop14:
            do {
                int alt14=2;
                try { dbg.enterDecision(14, decisionCanBacktrack[14]);

                int LA14_0 = input.LA(1);

                if ( (LA14_0==WS) ) {
                    alt14=1;
                }


                } finally {dbg.exitDecision(14);}

                switch (alt14) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:165:45: WS
            	    {
            	    dbg.location(165,45);
            	    match(input,WS,FOLLOW_WS_in_imports265); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);
            } finally {dbg.exitSubRule(14);}

            dbg.location(165,49);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:165:49: ( mediaList )?
            int alt15=2;
            try { dbg.enterSubRule(15);
            try { dbg.enterDecision(15, decisionCanBacktrack[15]);

            int LA15_0 = input.LA(1);

            if ( (LA15_0==IDENT||LA15_0==GEN) ) {
                alt15=1;
            }
            } finally {dbg.exitDecision(15);}

            switch (alt15) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:165:49: mediaList
                    {
                    dbg.location(165,49);
                    pushFollow(FOLLOW_mediaList_in_imports268);
                    mediaList();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(15);}

            dbg.location(165,60);
            match(input,SEMI,FOLLOW_SEMI_in_imports271); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(166, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:172:1: media : MEDIA_SYM ( WS )* mediaList LBRACE ( WS )* ruleSet ( WS )* RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(172, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:173:5: ( MEDIA_SYM ( WS )* mediaList LBRACE ( WS )* ruleSet ( WS )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:173:7: MEDIA_SYM ( WS )* mediaList LBRACE ( WS )* ruleSet ( WS )* RBRACE
            {
            dbg.location(173,7);
            match(input,MEDIA_SYM,FOLLOW_MEDIA_SYM_in_media292); if (state.failed) return ;
            dbg.location(173,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:173:17: ( WS )*
            try { dbg.enterSubRule(16);

            loop16:
            do {
                int alt16=2;
                try { dbg.enterDecision(16, decisionCanBacktrack[16]);

                int LA16_0 = input.LA(1);

                if ( (LA16_0==WS) ) {
                    alt16=1;
                }


                } finally {dbg.exitDecision(16);}

                switch (alt16) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:173:17: WS
            	    {
            	    dbg.location(173,17);
            	    match(input,WS,FOLLOW_WS_in_media294); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);
            } finally {dbg.exitSubRule(16);}

            dbg.location(173,21);
            pushFollow(FOLLOW_mediaList_in_media297);
            mediaList();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(174,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_media307); if (state.failed) return ;
            dbg.location(174,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:174:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:174:16: WS
            	    {
            	    dbg.location(174,16);
            	    match(input,WS,FOLLOW_WS_in_media309); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop17;
                }
            } while (true);
            } finally {dbg.exitSubRule(17);}

            dbg.location(175,13);
            pushFollow(FOLLOW_ruleSet_in_media324);
            ruleSet();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(176,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:176:9: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:176:9: WS
            	    {
            	    dbg.location(176,9);
            	    match(input,WS,FOLLOW_WS_in_media334); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop18;
                }
            } while (true);
            } finally {dbg.exitSubRule(18);}

            dbg.location(176,13);
            match(input,RBRACE,FOLLOW_RBRACE_in_media337); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(177, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:179:1: mediaList : medium ( COMMA ( WS )* medium )* ;
    public final void mediaList() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaList");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(179, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:180:9: ( medium ( COMMA ( WS )* medium )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:180:11: medium ( COMMA ( WS )* medium )*
            {
            dbg.location(180,11);
            pushFollow(FOLLOW_medium_in_mediaList358);
            medium();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(180,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:180:18: ( COMMA ( WS )* medium )*
            try { dbg.enterSubRule(20);

            loop20:
            do {
                int alt20=2;
                try { dbg.enterDecision(20, decisionCanBacktrack[20]);

                int LA20_0 = input.LA(1);

                if ( (LA20_0==COMMA) ) {
                    alt20=1;
                }


                } finally {dbg.exitDecision(20);}

                switch (alt20) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:180:19: COMMA ( WS )* medium
            	    {
            	    dbg.location(180,19);
            	    match(input,COMMA,FOLLOW_COMMA_in_mediaList361); if (state.failed) return ;
            	    dbg.location(180,25);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:180:25: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:180:25: WS
            	    	    {
            	    	    dbg.location(180,25);
            	    	    match(input,WS,FOLLOW_WS_in_mediaList363); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop19;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(19);}

            	    dbg.location(180,29);
            	    pushFollow(FOLLOW_medium_in_mediaList366);
            	    medium();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);
            } finally {dbg.exitSubRule(20);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(181, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:186:1: medium : ( IDENT | GEN ) ( WS )* ;
    public final void medium() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "medium");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(186, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:187:5: ( ( IDENT | GEN ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:187:7: ( IDENT | GEN ) ( WS )*
            {
            dbg.location(187,7);
            if ( input.LA(1)==IDENT||input.LA(1)==GEN ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }

            dbg.location(187,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:187:23: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:187:23: WS
            	    {
            	    dbg.location(187,23);
            	    match(input,WS,FOLLOW_WS_in_medium395); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop21;
                }
            } while (true);
            } finally {dbg.exitSubRule(21);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(188, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:191:1: bodylist : ( bodyset )* ;
    public final void bodylist() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodylist");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(191, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:192:5: ( ( bodyset )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:192:7: ( bodyset )*
            {
            dbg.location(192,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:192:7: ( bodyset )*
            try { dbg.enterSubRule(22);

            loop22:
            do {
                int alt22=2;
                try { dbg.enterDecision(22, decisionCanBacktrack[22]);

                int LA22_0 = input.LA(1);

                if ( (LA22_0==IDENT||LA22_0==MEDIA_SYM||(LA22_0>=GEN && LA22_0<=COLON)||(LA22_0>=STAR && LA22_0<=LBRACKET)) ) {
                    alt22=1;
                }


                } finally {dbg.exitDecision(22);}

                switch (alt22) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:192:7: bodyset
            	    {
            	    dbg.location(192,7);
            	    pushFollow(FOLLOW_bodyset_in_bodylist418);
            	    bodyset();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop22;
                }
            } while (true);
            } finally {dbg.exitSubRule(22);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(193, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:195:1: bodyset : ( ruleSet | media | page ) ( WS )* ;
    public final void bodyset() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyset");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(195, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:196:5: ( ( ruleSet | media | page ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:196:7: ( ruleSet | media | page ) ( WS )*
            {
            dbg.location(196,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:196:7: ( ruleSet | media | page )
            int alt23=3;
            try { dbg.enterSubRule(23);
            try { dbg.enterDecision(23, decisionCanBacktrack[23]);

            switch ( input.LA(1) ) {
            case IDENT:
            case GEN:
            case COLON:
            case STAR:
            case PIPE:
            case HASH:
            case DOT:
            case LBRACKET:
                {
                alt23=1;
                }
                break;
            case MEDIA_SYM:
                {
                alt23=2;
                }
                break;
            case PAGE_SYM:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:197:6: ruleSet
                    {
                    dbg.location(197,6);
                    pushFollow(FOLLOW_ruleSet_in_bodyset447);
                    ruleSet();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:198:11: media
                    {
                    dbg.location(198,11);
                    pushFollow(FOLLOW_media_in_bodyset459);
                    media();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:199:11: page
                    {
                    dbg.location(199,11);
                    pushFollow(FOLLOW_page_in_bodyset471);
                    page();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(23);}

            dbg.location(201,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:201:7: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:201:7: WS
            	    {
            	    dbg.location(201,7);
            	    match(input,WS,FOLLOW_WS_in_bodyset487); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop24;
                }
            } while (true);
            } finally {dbg.exitSubRule(24);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(202, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:204:1: page : PAGE_SYM ( WS )? ( pseudoPage ( WS )* )? LBRACE ( WS )* declaration SEMI ( declaration SEMI )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(204, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:205:5: ( PAGE_SYM ( WS )? ( pseudoPage ( WS )* )? LBRACE ( WS )* declaration SEMI ( declaration SEMI )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:205:7: PAGE_SYM ( WS )? ( pseudoPage ( WS )* )? LBRACE ( WS )* declaration SEMI ( declaration SEMI )* RBRACE
            {
            dbg.location(205,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page509); if (state.failed) return ;
            dbg.location(205,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:205:16: ( WS )?
            int alt25=2;
            try { dbg.enterSubRule(25);
            try { dbg.enterDecision(25, decisionCanBacktrack[25]);

            int LA25_0 = input.LA(1);

            if ( (LA25_0==WS) ) {
                alt25=1;
            }
            } finally {dbg.exitDecision(25);}

            switch (alt25) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:205:16: WS
                    {
                    dbg.location(205,16);
                    match(input,WS,FOLLOW_WS_in_page511); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(25);}

            dbg.location(205,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:205:20: ( pseudoPage ( WS )* )?
            int alt27=2;
            try { dbg.enterSubRule(27);
            try { dbg.enterDecision(27, decisionCanBacktrack[27]);

            int LA27_0 = input.LA(1);

            if ( (LA27_0==COLON) ) {
                alt27=1;
            }
            } finally {dbg.exitDecision(27);}

            switch (alt27) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:205:21: pseudoPage ( WS )*
                    {
                    dbg.location(205,21);
                    pushFollow(FOLLOW_pseudoPage_in_page515);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(205,32);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:205:32: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:205:32: WS
                    	    {
                    	    dbg.location(205,32);
                    	    match(input,WS,FOLLOW_WS_in_page517); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop26;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(26);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(27);}

            dbg.location(206,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page530); if (state.failed) return ;
            dbg.location(206,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:206:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:206:16: WS
            	    {
            	    dbg.location(206,16);
            	    match(input,WS,FOLLOW_WS_in_page532); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop28;
                }
            } while (true);
            } finally {dbg.exitSubRule(28);}

            dbg.location(207,13);
            pushFollow(FOLLOW_declaration_in_page547);
            declaration();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(207,25);
            match(input,SEMI,FOLLOW_SEMI_in_page549); if (state.failed) return ;
            dbg.location(207,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:207:30: ( declaration SEMI )*
            try { dbg.enterSubRule(29);

            loop29:
            do {
                int alt29=2;
                try { dbg.enterDecision(29, decisionCanBacktrack[29]);

                int LA29_0 = input.LA(1);

                if ( (LA29_0==IDENT||LA29_0==GEN) ) {
                    alt29=1;
                }


                } finally {dbg.exitDecision(29);}

                switch (alt29) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:207:31: declaration SEMI
            	    {
            	    dbg.location(207,31);
            	    pushFollow(FOLLOW_declaration_in_page552);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(207,43);
            	    match(input,SEMI,FOLLOW_SEMI_in_page554); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop29;
                }
            } while (true);
            } finally {dbg.exitSubRule(29);}

            dbg.location(208,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page566); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "page");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "page"


    // $ANTLR start "pseudoPage"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:211:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(211, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:212:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:212:7: COLON IDENT
            {
            dbg.location(212,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage587); if (state.failed) return ;
            dbg.location(212,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage589); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "pseudoPage");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "pseudoPage"


    // $ANTLR start "operator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:215:1: operator : ( SOLIDUS ( WS )* | COMMA ( WS )* | );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(215, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:216:5: ( SOLIDUS ( WS )* | COMMA ( WS )* | )
            int alt32=3;
            try { dbg.enterDecision(32, decisionCanBacktrack[32]);

            switch ( input.LA(1) ) {
            case SOLIDUS:
                {
                alt32=1;
                }
                break;
            case COMMA:
                {
                alt32=2;
                }
                break;
            case IDENT:
            case STRING:
            case URI:
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
                alt32=3;
                }
                break;
            default:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:216:7: SOLIDUS ( WS )*
                    {
                    dbg.location(216,7);
                    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_operator610); if (state.failed) return ;
                    dbg.location(216,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:216:15: ( WS )*
                    try { dbg.enterSubRule(30);

                    loop30:
                    do {
                        int alt30=2;
                        try { dbg.enterDecision(30, decisionCanBacktrack[30]);

                        int LA30_0 = input.LA(1);

                        if ( (LA30_0==WS) ) {
                            alt30=1;
                        }


                        } finally {dbg.exitDecision(30);}

                        switch (alt30) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:216:15: WS
                    	    {
                    	    dbg.location(216,15);
                    	    match(input,WS,FOLLOW_WS_in_operator612); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop30;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(30);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:217:7: COMMA ( WS )*
                    {
                    dbg.location(217,7);
                    match(input,COMMA,FOLLOW_COMMA_in_operator621); if (state.failed) return ;
                    dbg.location(217,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:217:13: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:217:13: WS
                    	    {
                    	    dbg.location(217,13);
                    	    match(input,WS,FOLLOW_WS_in_operator623); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop31;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(31);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:219:5: 
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
        dbg.location(219, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:221:1: combinator : ( PLUS ( WS )* | GREATER ( WS )* | TILDE ( WS )* | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(221, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:222:5: ( PLUS ( WS )* | GREATER ( WS )* | TILDE ( WS )* | )
            int alt36=4;
            try { dbg.enterDecision(36, decisionCanBacktrack[36]);

            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt36=1;
                }
                break;
            case GREATER:
                {
                alt36=2;
                }
                break;
            case TILDE:
                {
                alt36=3;
                }
                break;
            case IDENT:
            case GEN:
            case COLON:
            case STAR:
            case PIPE:
            case HASH:
            case DOT:
            case LBRACKET:
                {
                alt36=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(36);}

            switch (alt36) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:222:7: PLUS ( WS )*
                    {
                    dbg.location(222,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator651); if (state.failed) return ;
                    dbg.location(222,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:222:12: ( WS )*
                    try { dbg.enterSubRule(33);

                    loop33:
                    do {
                        int alt33=2;
                        try { dbg.enterDecision(33, decisionCanBacktrack[33]);

                        int LA33_0 = input.LA(1);

                        if ( (LA33_0==WS) ) {
                            alt33=1;
                        }


                        } finally {dbg.exitDecision(33);}

                        switch (alt33) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:222:12: WS
                    	    {
                    	    dbg.location(222,12);
                    	    match(input,WS,FOLLOW_WS_in_combinator653); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop33;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(33);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:223:7: GREATER ( WS )*
                    {
                    dbg.location(223,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator662); if (state.failed) return ;
                    dbg.location(223,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:223:15: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:223:15: WS
                    	    {
                    	    dbg.location(223,15);
                    	    match(input,WS,FOLLOW_WS_in_combinator664); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop34;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(34);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:224:7: TILDE ( WS )*
                    {
                    dbg.location(224,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator673); if (state.failed) return ;
                    dbg.location(224,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:224:13: ( WS )*
                    try { dbg.enterSubRule(35);

                    loop35:
                    do {
                        int alt35=2;
                        try { dbg.enterDecision(35, decisionCanBacktrack[35]);

                        int LA35_0 = input.LA(1);

                        if ( (LA35_0==WS) ) {
                            alt35=1;
                        }


                        } finally {dbg.exitDecision(35);}

                        switch (alt35) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:224:13: WS
                    	    {
                    	    dbg.location(224,13);
                    	    match(input,WS,FOLLOW_WS_in_combinator675); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop35;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(35);}


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:226:5: 
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
        dbg.location(226, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:228:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(228, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:229:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(229,5);
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
        dbg.location(231, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:233:1: property : ( IDENT | GEN ) ( WS )* ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(233, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:234:5: ( ( IDENT | GEN ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:234:7: ( IDENT | GEN ) ( WS )*
            {
            dbg.location(234,7);
            if ( input.LA(1)==IDENT||input.LA(1)==GEN ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }

            dbg.location(234,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:234:21: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:234:21: WS
            	    {
            	    dbg.location(234,21);
            	    match(input,WS,FOLLOW_WS_in_property743); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop37;
                }
            } while (true);
            } finally {dbg.exitSubRule(37);}


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
            dbg.exitRule(getGrammarFileName(), "property");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "property"


    // $ANTLR start "ruleSet"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:237:1: ruleSet : selectorsGroup LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void ruleSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ruleSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(237, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:238:5: ( selectorsGroup LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:238:9: selectorsGroup LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(238,9);
            pushFollow(FOLLOW_selectorsGroup_in_ruleSet768);
            selectorsGroup();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(239,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_ruleSet778); if (state.failed) return ;
            dbg.location(239,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:239:16: ( WS )*
            try { dbg.enterSubRule(38);

            loop38:
            do {
                int alt38=2;
                try { dbg.enterDecision(38, decisionCanBacktrack[38]);

                int LA38_0 = input.LA(1);

                if ( (LA38_0==WS) ) {
                    alt38=1;
                }


                } finally {dbg.exitDecision(38);}

                switch (alt38) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:239:16: WS
            	    {
            	    dbg.location(239,16);
            	    match(input,WS,FOLLOW_WS_in_ruleSet780); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop38;
                }
            } while (true);
            } finally {dbg.exitSubRule(38);}

            dbg.location(239,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_ruleSet783);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(240,13);
            pushFollow(FOLLOW_declarations_in_ruleSet797);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(241,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_ruleSet807); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(242, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:244:1: declarations : ( declaration )? ( SEMI ( WS )* ( declaration )? )* ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(244, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:245:5: ( ( declaration )? ( SEMI ( WS )* ( declaration )? )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:247:9: ( declaration )? ( SEMI ( WS )* ( declaration )? )*
            {
            dbg.location(247,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:247:9: ( declaration )?
            int alt39=2;
            try { dbg.enterSubRule(39);
            try { dbg.enterDecision(39, decisionCanBacktrack[39]);

            int LA39_0 = input.LA(1);

            if ( (LA39_0==IDENT||LA39_0==GEN) ) {
                alt39=1;
            }
            } finally {dbg.exitDecision(39);}

            switch (alt39) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:247:9: declaration
                    {
                    dbg.location(247,9);
                    pushFollow(FOLLOW_declaration_in_declarations845);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(39);}

            dbg.location(247,22);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:247:22: ( SEMI ( WS )* ( declaration )? )*
            try { dbg.enterSubRule(42);

            loop42:
            do {
                int alt42=2;
                try { dbg.enterDecision(42, decisionCanBacktrack[42]);

                int LA42_0 = input.LA(1);

                if ( (LA42_0==SEMI) ) {
                    alt42=1;
                }


                } finally {dbg.exitDecision(42);}

                switch (alt42) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:247:23: SEMI ( WS )* ( declaration )?
            	    {
            	    dbg.location(247,23);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations849); if (state.failed) return ;
            	    dbg.location(247,28);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:247:28: ( WS )*
            	    try { dbg.enterSubRule(40);

            	    loop40:
            	    do {
            	        int alt40=2;
            	        try { dbg.enterDecision(40, decisionCanBacktrack[40]);

            	        int LA40_0 = input.LA(1);

            	        if ( (LA40_0==WS) ) {
            	            alt40=1;
            	        }


            	        } finally {dbg.exitDecision(40);}

            	        switch (alt40) {
            	    	case 1 :
            	    	    dbg.enterAlt(1);

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:247:28: WS
            	    	    {
            	    	    dbg.location(247,28);
            	    	    match(input,WS,FOLLOW_WS_in_declarations851); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop40;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(40);}

            	    dbg.location(247,32);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:247:32: ( declaration )?
            	    int alt41=2;
            	    try { dbg.enterSubRule(41);
            	    try { dbg.enterDecision(41, decisionCanBacktrack[41]);

            	    int LA41_0 = input.LA(1);

            	    if ( (LA41_0==IDENT||LA41_0==GEN) ) {
            	        alt41=1;
            	    }
            	    } finally {dbg.exitDecision(41);}

            	    switch (alt41) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:247:32: declaration
            	            {
            	            dbg.location(247,32);
            	            pushFollow(FOLLOW_declaration_in_declarations854);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(41);}


            	    }
            	    break;

            	default :
            	    break loop42;
                }
            } while (true);
            } finally {dbg.exitSubRule(42);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(248, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:250:1: selectorsGroup : selector ( COMMA ( WS )* selector )* ;
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(250, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:251:5: ( selector ( COMMA ( WS )* selector )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:251:7: selector ( COMMA ( WS )* selector )*
            {
            dbg.location(251,7);
            pushFollow(FOLLOW_selector_in_selectorsGroup878);
            selector();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(251,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:251:16: ( COMMA ( WS )* selector )*
            try { dbg.enterSubRule(44);

            loop44:
            do {
                int alt44=2;
                try { dbg.enterDecision(44, decisionCanBacktrack[44]);

                int LA44_0 = input.LA(1);

                if ( (LA44_0==COMMA) ) {
                    alt44=1;
                }


                } finally {dbg.exitDecision(44);}

                switch (alt44) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:251:17: COMMA ( WS )* selector
            	    {
            	    dbg.location(251,17);
            	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup881); if (state.failed) return ;
            	    dbg.location(251,23);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:251:23: ( WS )*
            	    try { dbg.enterSubRule(43);

            	    loop43:
            	    do {
            	        int alt43=2;
            	        try { dbg.enterDecision(43, decisionCanBacktrack[43]);

            	        int LA43_0 = input.LA(1);

            	        if ( (LA43_0==WS) ) {
            	            alt43=1;
            	        }


            	        } finally {dbg.exitDecision(43);}

            	        switch (alt43) {
            	    	case 1 :
            	    	    dbg.enterAlt(1);

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:251:23: WS
            	    	    {
            	    	    dbg.location(251,23);
            	    	    match(input,WS,FOLLOW_WS_in_selectorsGroup883); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop43;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(43);}

            	    dbg.location(251,27);
            	    pushFollow(FOLLOW_selector_in_selectorsGroup886);
            	    selector();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop44;
                }
            } while (true);
            } finally {dbg.exitSubRule(44);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(252, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:254:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(254, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:255:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:255:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(255,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector909);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(255,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:255:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(45);

            loop45:
            do {
                int alt45=2;
                try { dbg.enterDecision(45, decisionCanBacktrack[45]);

                int LA45_0 = input.LA(1);

                if ( (LA45_0==IDENT||LA45_0==GEN||LA45_0==COLON||(LA45_0>=PLUS && LA45_0<=TILDE)||(LA45_0>=STAR && LA45_0<=LBRACKET)) ) {
                    alt45=1;
                }


                } finally {dbg.exitDecision(45);}

                switch (alt45) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:255:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(255,31);
            	    pushFollow(FOLLOW_combinator_in_selector912);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(255,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector914);
            	    simpleSelectorSequence();

            	    state._fsp--;
            	    if (state.failed) return ;

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
        dbg.location(256, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:259:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(259, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:261:2: ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) )
            int alt48=2;
            try { dbg.enterDecision(48, decisionCanBacktrack[48]);

            int LA48_0 = input.LA(1);

            if ( (LA48_0==IDENT||LA48_0==GEN||(LA48_0>=STAR && LA48_0<=PIPE)) ) {
                alt48=1;
            }
            else if ( (LA48_0==COLON||(LA48_0>=HASH && LA48_0<=LBRACKET)) ) {
                alt48=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 48, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(48);}

            switch (alt48) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    {
                    dbg.location(265,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:5: typeSelector ( ( esPred )=> elementSubsequent )*
                    {
                    dbg.location(265,5);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence954);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(265,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:18: ( ( esPred )=> elementSubsequent )*
                    try { dbg.enterSubRule(46);

                    loop46:
                    do {
                        int alt46=2;
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:19: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(265,29);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence961);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop46;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(46);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:2: ( ( ( esPred )=> elementSubsequent )+ )
                    {
                    dbg.location(267,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:2: ( ( ( esPred )=> elementSubsequent )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:4: ( ( esPred )=> elementSubsequent )+
                    {
                    dbg.location(267,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:4: ( ( esPred )=> elementSubsequent )+
                    int cnt47=0;
                    try { dbg.enterSubRule(47);

                    loop47:
                    do {
                        int alt47=2;
                        try { dbg.enterDecision(47, decisionCanBacktrack[47]);

                        switch ( input.LA(1) ) {
                        case HASH:
                            {
                            int LA47_2 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt47=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA47_3 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt47=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA47_4 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt47=1;
                            }


                            }
                            break;
                        case COLON:
                            {
                            int LA47_5 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt47=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(47);}

                        switch (alt47) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:5: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(267,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence979);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt47 >= 1 ) break loop47;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(47, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt47++;
                    } while (true);
                    } finally {dbg.exitSubRule(47);}


                    }


                    }
                    break;

            }
        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(LBRACE)); 
                
        }
        finally {
        }
        dbg.location(268, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:282:1: typeSelector options {k=2; } : ( ( nsPred )=> namespace_wqname_prefix )? ( elementName ( WS )* ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(282, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:284:3: ( ( ( nsPred )=> namespace_wqname_prefix )? ( elementName ( WS )* ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:284:6: ( ( nsPred )=> namespace_wqname_prefix )? ( elementName ( WS )* )
            {
            dbg.location(284,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:284:6: ( ( nsPred )=> namespace_wqname_prefix )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:284:7: ( nsPred )=> namespace_wqname_prefix
                    {
                    dbg.location(284,17);
                    pushFollow(FOLLOW_namespace_wqname_prefix_in_typeSelector1030);
                    namespace_wqname_prefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(49);}

            dbg.location(284,43);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:284:43: ( elementName ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:284:45: elementName ( WS )*
            {
            dbg.location(284,45);
            pushFollow(FOLLOW_elementName_in_typeSelector1036);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(284,57);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:284:57: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:284:57: WS
            	    {
            	    dbg.location(284,57);
            	    match(input,WS,FOLLOW_WS_in_typeSelector1038); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop50;
                }
            } while (true);
            } finally {dbg.exitSubRule(50);}


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(285, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:2: nsPred : ( IDENT | STAR ) PIPE ;
    public final void nsPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "nsPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(287, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:288:3: ( ( IDENT | STAR ) PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:289:3: ( IDENT | STAR ) PIPE
            {
            dbg.location(289,3);
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

            dbg.location(289,18);
            match(input,PIPE,FOLLOW_PIPE_in_nsPred1071); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(290, 3);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "nsPred");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "nsPred"


    // $ANTLR start "namespace_wqname_prefix"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:298:2: namespace_wqname_prefix : ( ( namespace_prefix ( WS )* )? PIPE | namespace_wildcard_prefix ( WS )* PIPE );
    public final void namespace_wqname_prefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace_wqname_prefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(298, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:299:3: ( ( namespace_prefix ( WS )* )? PIPE | namespace_wildcard_prefix ( WS )* PIPE )
            int alt54=2;
            try { dbg.enterDecision(54, decisionCanBacktrack[54]);

            int LA54_0 = input.LA(1);

            if ( (LA54_0==IDENT||LA54_0==PIPE) ) {
                alt54=1;
            }
            else if ( (LA54_0==STAR) ) {
                alt54=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 54, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(54);}

            switch (alt54) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:299:5: ( namespace_prefix ( WS )* )? PIPE
                    {
                    dbg.location(299,5);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:299:5: ( namespace_prefix ( WS )* )?
                    int alt52=2;
                    try { dbg.enterSubRule(52);
                    try { dbg.enterDecision(52, decisionCanBacktrack[52]);

                    int LA52_0 = input.LA(1);

                    if ( (LA52_0==IDENT) ) {
                        alt52=1;
                    }
                    } finally {dbg.exitDecision(52);}

                    switch (alt52) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:299:7: namespace_prefix ( WS )*
                            {
                            dbg.location(299,7);
                            pushFollow(FOLLOW_namespace_prefix_in_namespace_wqname_prefix1101);
                            namespace_prefix();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(299,24);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:299:24: ( WS )*
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

                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:299:24: WS
                            	    {
                            	    dbg.location(299,24);
                            	    match(input,WS,FOLLOW_WS_in_namespace_wqname_prefix1103); if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop51;
                                }
                            } while (true);
                            } finally {dbg.exitSubRule(51);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(52);}

                    dbg.location(299,31);
                    match(input,PIPE,FOLLOW_PIPE_in_namespace_wqname_prefix1109); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:300:6: namespace_wildcard_prefix ( WS )* PIPE
                    {
                    dbg.location(300,6);
                    pushFollow(FOLLOW_namespace_wildcard_prefix_in_namespace_wqname_prefix1116);
                    namespace_wildcard_prefix();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(300,32);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:300:32: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:300:32: WS
                    	    {
                    	    dbg.location(300,32);
                    	    match(input,WS,FOLLOW_WS_in_namespace_wqname_prefix1118); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop53;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(53);}

                    dbg.location(300,36);
                    match(input,PIPE,FOLLOW_PIPE_in_namespace_wqname_prefix1121); if (state.failed) return ;

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
        dbg.location(301, 3);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "namespace_wqname_prefix");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "namespace_wqname_prefix"


    // $ANTLR start "namespace_wildcard_prefix"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:303:1: namespace_wildcard_prefix : STAR ;
    public final void namespace_wildcard_prefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace_wildcard_prefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(303, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:304:4: ( STAR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:305:4: STAR
            {
            dbg.location(305,4);
            match(input,STAR,FOLLOW_STAR_in_namespace_wildcard_prefix1143); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(306, 4);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "namespace_wildcard_prefix");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "namespace_wildcard_prefix"


    // $ANTLR start "esPred"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:1: esPred : ( HASH | DOT | LBRACKET | COLON );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(308, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:309:5: ( HASH | DOT | LBRACKET | COLON )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(309,5);
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
        dbg.location(310, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:312:1: elementSubsequent : ( cssId | cssClass | attrib | pseudo ) ( WS )* ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(312, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:313:5: ( ( cssId | cssClass | attrib | pseudo ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:314:5: ( cssId | cssClass | attrib | pseudo ) ( WS )*
            {
            dbg.location(314,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:314:5: ( cssId | cssClass | attrib | pseudo )
            int alt55=4;
            try { dbg.enterSubRule(55);
            try { dbg.enterDecision(55, decisionCanBacktrack[55]);

            switch ( input.LA(1) ) {
            case HASH:
                {
                alt55=1;
                }
                break;
            case DOT:
                {
                alt55=2;
                }
                break;
            case LBRACKET:
                {
                alt55=3;
                }
                break;
            case COLON:
                {
                alt55=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 55, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(55);}

            switch (alt55) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:315:6: cssId
                    {
                    dbg.location(315,6);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent1212);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:316:8: cssClass
                    {
                    dbg.location(316,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent1221);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:317:11: attrib
                    {
                    dbg.location(317,11);
                    pushFollow(FOLLOW_attrib_in_elementSubsequent1233);
                    attrib();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:318:11: pseudo
                    {
                    dbg.location(318,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent1245);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(55);}

            dbg.location(320,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:5: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:5: WS
            	    {
            	    dbg.location(320,5);
            	    match(input,WS,FOLLOW_WS_in_elementSubsequent1257); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop56;
                }
            } while (true);
            } finally {dbg.exitSubRule(56);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(321, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:1: cssId : HASH ;
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(323, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:324:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:324:7: HASH
            {
            dbg.location(324,7);
            match(input,HASH,FOLLOW_HASH_in_cssId1279); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(325, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:327:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(327, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:328:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:328:7: DOT ( IDENT | GEN )
            {
            dbg.location(328,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass1296); if (state.failed) return ;
            dbg.location(328,11);
            if ( input.LA(1)==IDENT||input.LA(1)==GEN ) {
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
        dbg.location(329, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:332:1: elementName : ( ( IDENT | GEN ) | '*' );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(332, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:5: ( ( IDENT | GEN ) | '*' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(333,5);
            if ( input.LA(1)==IDENT||input.LA(1)==GEN||input.LA(1)==STAR ) {
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
        dbg.location(334, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:336:1: attrib : LBRACKET ( namespace_wqname_prefix )? ( WS )* attrib_name ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* attrib_value ( WS )* )? RBRACKET ;
    public final void attrib() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrib");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(336, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:337:5: ( LBRACKET ( namespace_wqname_prefix )? ( WS )* attrib_name ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* attrib_value ( WS )* )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:337:7: LBRACKET ( namespace_wqname_prefix )? ( WS )* attrib_name ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* attrib_value ( WS )* )? RBRACKET
            {
            dbg.location(337,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_attrib1362); if (state.failed) return ;
            dbg.location(338,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:6: ( namespace_wqname_prefix )?
            int alt57=2;
            try { dbg.enterSubRule(57);
            try { dbg.enterDecision(57, decisionCanBacktrack[57]);

            try {
                isCyclicDecision = true;
                alt57 = dfa57.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(57);}

            switch (alt57) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:6: namespace_wqname_prefix
                    {
                    dbg.location(338,6);
                    pushFollow(FOLLOW_namespace_wqname_prefix_in_attrib1369);
                    namespace_wqname_prefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(57);}

            dbg.location(338,31);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:31: ( WS )*
            try { dbg.enterSubRule(58);

            loop58:
            do {
                int alt58=2;
                try { dbg.enterDecision(58, decisionCanBacktrack[58]);

                int LA58_0 = input.LA(1);

                if ( (LA58_0==WS) ) {
                    alt58=1;
                }


                } finally {dbg.exitDecision(58);}

                switch (alt58) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:31: WS
            	    {
            	    dbg.location(338,31);
            	    match(input,WS,FOLLOW_WS_in_attrib1372); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop58;
                }
            } while (true);
            } finally {dbg.exitSubRule(58);}

            dbg.location(339,9);
            pushFollow(FOLLOW_attrib_name_in_attrib1383);
            attrib_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(339,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:339:21: ( WS )*
            try { dbg.enterSubRule(59);

            loop59:
            do {
                int alt59=2;
                try { dbg.enterDecision(59, decisionCanBacktrack[59]);

                int LA59_0 = input.LA(1);

                if ( (LA59_0==WS) ) {
                    alt59=1;
                }


                } finally {dbg.exitDecision(59);}

                switch (alt59) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:339:21: WS
            	    {
            	    dbg.location(339,21);
            	    match(input,WS,FOLLOW_WS_in_attrib1385); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop59;
                }
            } while (true);
            } finally {dbg.exitSubRule(59);}

            dbg.location(341,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:13: ( ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* attrib_value ( WS )* )?
            int alt62=2;
            try { dbg.enterSubRule(62);
            try { dbg.enterDecision(62, decisionCanBacktrack[62]);

            int LA62_0 = input.LA(1);

            if ( ((LA62_0>=OPEQ && LA62_0<=DASHMATCH)) ) {
                alt62=1;
            }
            } finally {dbg.exitDecision(62);}

            switch (alt62) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:342:17: ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* attrib_value ( WS )*
                    {
                    dbg.location(342,17);
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

                    dbg.location(347,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:17: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:17: WS
                    	    {
                    	    dbg.location(347,17);
                    	    match(input,WS,FOLLOW_WS_in_attrib1535); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop60;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(60);}

                    dbg.location(348,17);
                    pushFollow(FOLLOW_attrib_value_in_attrib1554);
                    attrib_value();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(349,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:17: ( WS )*
                    try { dbg.enterSubRule(61);

                    loop61:
                    do {
                        int alt61=2;
                        try { dbg.enterDecision(61, decisionCanBacktrack[61]);

                        int LA61_0 = input.LA(1);

                        if ( (LA61_0==WS) ) {
                            alt61=1;
                        }


                        } finally {dbg.exitDecision(61);}

                        switch (alt61) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:17: WS
                    	    {
                    	    dbg.location(349,17);
                    	    match(input,WS,FOLLOW_WS_in_attrib1572); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop61;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(61);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(62);}

            dbg.location(352,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_attrib1601); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(353, 1);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "attrib");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "attrib"


    // $ANTLR start "syncTo_IDENT_RBRACKET_LBRACE"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:1: syncTo_IDENT_RBRACKET_LBRACE : ;
    public final void syncTo_IDENT_RBRACKET_LBRACE() throws RecognitionException {

                syncToSet(BitSet.of(IDENT, RBRACKET, LBRACE));
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_IDENT_RBRACKET_LBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(359, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:6: 
            {
            }

        }
        finally {
        }
        dbg.location(364, 6);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "syncTo_IDENT_RBRACKET_LBRACE");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "syncTo_IDENT_RBRACKET_LBRACE"


    // $ANTLR start "attrib_name"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:367:1: attrib_name : IDENT ;
    public final void attrib_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrib_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(367, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:4: IDENT
            {
            dbg.location(368,4);
            match(input,IDENT,FOLLOW_IDENT_in_attrib_name1644); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(369, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "attrib_name");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "attrib_name"


    // $ANTLR start "attrib_value"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:371:1: attrib_value : ( IDENT | STRING ) ;
    public final void attrib_value() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrib_value");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(371, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:373:2: ( IDENT | STRING )
            {
            dbg.location(373,2);
            if ( (input.LA(1)>=IDENT && input.LA(1)<=STRING) ) {
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
        dbg.location(377, 9);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "attrib_value");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "attrib_value"


    // $ANTLR start "pseudo"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:379:1: pseudo : COLON ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )? ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(379, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:5: ( COLON ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:7: COLON ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )?
            {
            dbg.location(380,7);
            match(input,COLON,FOLLOW_COLON_in_pseudo1718); if (state.failed) return ;
            dbg.location(381,13);
            if ( input.LA(1)==IDENT||input.LA(1)==GEN ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }

            dbg.location(382,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:17: ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )?
            int alt67=2;
            try { dbg.enterSubRule(67);
            try { dbg.enterDecision(67, decisionCanBacktrack[67]);

            try {
                isCyclicDecision = true;
                alt67 = dfa67.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(67);}

            switch (alt67) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:21: ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN
                    {
                    dbg.location(383,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:21: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:21: WS
                    	    {
                    	    dbg.location(383,21);
                    	    match(input,WS,FOLLOW_WS_in_pseudo1782); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop63;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(63);}

                    dbg.location(383,25);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo1785); if (state.failed) return ;
                    dbg.location(383,32);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:32: ( WS )*
                    try { dbg.enterSubRule(64);

                    loop64:
                    do {
                        int alt64=2;
                        try { dbg.enterDecision(64, decisionCanBacktrack[64]);

                        int LA64_0 = input.LA(1);

                        if ( (LA64_0==WS) ) {
                            alt64=1;
                        }


                        } finally {dbg.exitDecision(64);}

                        switch (alt64) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:32: WS
                    	    {
                    	    dbg.location(383,32);
                    	    match(input,WS,FOLLOW_WS_in_pseudo1787); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop64;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(64);}

                    dbg.location(383,36);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:36: ( ( IDENT | GEN ) ( WS )* )?
                    int alt66=2;
                    try { dbg.enterSubRule(66);
                    try { dbg.enterDecision(66, decisionCanBacktrack[66]);

                    int LA66_0 = input.LA(1);

                    if ( (LA66_0==IDENT||LA66_0==GEN) ) {
                        alt66=1;
                    }
                    } finally {dbg.exitDecision(66);}

                    switch (alt66) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:37: ( IDENT | GEN ) ( WS )*
                            {
                            dbg.location(383,37);
                            if ( input.LA(1)==IDENT||input.LA(1)==GEN ) {
                                input.consume();
                                state.errorRecovery=false;state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                dbg.recognitionException(mse);
                                throw mse;
                            }

                            dbg.location(383,53);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:53: ( WS )*
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

                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:53: WS
                            	    {
                            	    dbg.location(383,53);
                            	    match(input,WS,FOLLOW_WS_in_pseudo1801); if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop65;
                                }
                            } while (true);
                            } finally {dbg.exitSubRule(65);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(66);}

                    dbg.location(383,59);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo1806); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(67);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(385, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:387:1: declaration : property COLON ( WS )* expr ( prio )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(387, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:388:5: ( property COLON ( WS )* expr ( prio )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:5: property COLON ( WS )* expr ( prio )?
            {
            dbg.location(390,5);
            pushFollow(FOLLOW_property_in_declaration1852);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(390,14);
            match(input,COLON,FOLLOW_COLON_in_declaration1854); if (state.failed) return ;
            dbg.location(390,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:20: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:20: WS
            	    {
            	    dbg.location(390,20);
            	    match(input,WS,FOLLOW_WS_in_declaration1856); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop68;
                }
            } while (true);
            } finally {dbg.exitSubRule(68);}

            dbg.location(390,24);
            pushFollow(FOLLOW_expr_in_declaration1859);
            expr();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(390,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:29: ( prio )?
            int alt69=2;
            try { dbg.enterSubRule(69);
            try { dbg.enterDecision(69, decisionCanBacktrack[69]);

            int LA69_0 = input.LA(1);

            if ( (LA69_0==IMPORTANT_SYM) ) {
                alt69=1;
            }
            } finally {dbg.exitDecision(69);}

            switch (alt69) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:29: prio
                    {
                    dbg.location(390,29);
                    pushFollow(FOLLOW_prio_in_declaration1861);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(69);}


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
        dbg.location(391, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:1: syncTo_IDENT_RBRACE : ;
    public final void syncTo_IDENT_RBRACE() throws RecognitionException {

                syncToSet(BitSet.of(IDENT, RBRACE));
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_IDENT_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(401, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:405:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:6: 
            {
            }

        }
        finally {
        }
        dbg.location(406, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(409, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:413:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:6: 
            {
            }

        }
        finally {
        }
        dbg.location(414, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:1: prio : IMPORTANT_SYM ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(417, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:5: ( IMPORTANT_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:7: IMPORTANT_SYM
            {
            dbg.location(418,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio1954); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(419, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:1: expr : term ( operator term )* ;
    public final void expr() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expr");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(421, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:5: ( term ( operator term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:7: term ( operator term )*
            {
            dbg.location(422,7);
            pushFollow(FOLLOW_term_in_expr1975);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(422,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:12: ( operator term )*
            try { dbg.enterSubRule(70);

            loop70:
            do {
                int alt70=2;
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:13: operator term
            	    {
            	    dbg.location(422,13);
            	    pushFollow(FOLLOW_operator_in_expr1978);
            	    operator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(422,22);
            	    pushFollow(FOLLOW_term_in_expr1980);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop70;
                }
            } while (true);
            } finally {dbg.exitSubRule(70);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(423, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:425:1: term : ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | GEN | URI | hexColor | function ) ( WS )* ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(425, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:5: ( ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | GEN | URI | hexColor | function ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:7: ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | GEN | URI | hexColor | function ) ( WS )*
            {
            dbg.location(426,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:7: ( unaryOperator )?
            int alt71=2;
            try { dbg.enterSubRule(71);
            try { dbg.enterDecision(71, decisionCanBacktrack[71]);

            int LA71_0 = input.LA(1);

            if ( (LA71_0==PLUS||LA71_0==MINUS) ) {
                alt71=1;
            }
            } finally {dbg.exitDecision(71);}

            switch (alt71) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:7: unaryOperator
                    {
                    dbg.location(426,7);
                    pushFollow(FOLLOW_unaryOperator_in_term2003);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(71);}

            dbg.location(427,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | GEN | URI | hexColor | function )
            int alt72=7;
            try { dbg.enterSubRule(72);
            try { dbg.enterDecision(72, decisionCanBacktrack[72]);

            try {
                isCyclicDecision = true;
                alt72 = dfa72.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(72);}

            switch (alt72) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:428:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ )
                    {
                    dbg.location(428,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:7: STRING
                    {
                    dbg.location(438,7);
                    match(input,STRING,FOLLOW_STRING_in_term2170); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:7: IDENT
                    {
                    dbg.location(439,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term2178); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:440:7: GEN
                    {
                    dbg.location(440,7);
                    match(input,GEN,FOLLOW_GEN_in_term2186); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:441:7: URI
                    {
                    dbg.location(441,7);
                    match(input,URI,FOLLOW_URI_in_term2194); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:7: hexColor
                    {
                    dbg.location(442,7);
                    pushFollow(FOLLOW_hexColor_in_term2202);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:443:7: function
                    {
                    dbg.location(443,7);
                    pushFollow(FOLLOW_function_in_term2210);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(72);}

            dbg.location(445,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:5: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:5: WS
            	    {
            	    dbg.location(445,5);
            	    match(input,WS,FOLLOW_WS_in_term2222); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop73;
                }
            } while (true);
            } finally {dbg.exitSubRule(73);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(446, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:448:1: function : function_name ( WS )* LPAREN ( WS )* ( expr | ( attribute ( COMMA ( WS )* attribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(448, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:449:2: ( function_name ( WS )* LPAREN ( WS )* ( expr | ( attribute ( COMMA ( WS )* attribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:449:5: function_name ( WS )* LPAREN ( WS )* ( expr | ( attribute ( COMMA ( WS )* attribute )* ) ) RPAREN
            {
            dbg.location(449,5);
            pushFollow(FOLLOW_function_name_in_function2238);
            function_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(449,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:449:19: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:449:19: WS
            	    {
            	    dbg.location(449,19);
            	    match(input,WS,FOLLOW_WS_in_function2240); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop74;
                }
            } while (true);
            } finally {dbg.exitSubRule(74);}

            dbg.location(450,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function2245); if (state.failed) return ;
            dbg.location(450,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:10: ( WS )*
            try { dbg.enterSubRule(75);

            loop75:
            do {
                int alt75=2;
                try { dbg.enterDecision(75, decisionCanBacktrack[75]);

                int LA75_0 = input.LA(1);

                if ( (LA75_0==WS) ) {
                    alt75=1;
                }


                } finally {dbg.exitDecision(75);}

                switch (alt75) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:10: WS
            	    {
            	    dbg.location(450,10);
            	    match(input,WS,FOLLOW_WS_in_function2247); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop75;
                }
            } while (true);
            } finally {dbg.exitSubRule(75);}

            dbg.location(451,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:3: ( expr | ( attribute ( COMMA ( WS )* attribute )* ) )
            int alt78=2;
            try { dbg.enterSubRule(78);
            try { dbg.enterDecision(78, decisionCanBacktrack[78]);

            try {
                isCyclicDecision = true;
                alt78 = dfa78.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(78);}

            switch (alt78) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:4: expr
                    {
                    dbg.location(452,4);
                    pushFollow(FOLLOW_expr_in_function2258);
                    expr();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:6: ( attribute ( COMMA ( WS )* attribute )* )
                    {
                    dbg.location(454,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:454:6: ( attribute ( COMMA ( WS )* attribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:5: attribute ( COMMA ( WS )* attribute )*
                    {
                    dbg.location(455,5);
                    pushFollow(FOLLOW_attribute_in_function2276);
                    attribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(455,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:15: ( COMMA ( WS )* attribute )*
                    try { dbg.enterSubRule(77);

                    loop77:
                    do {
                        int alt77=2;
                        try { dbg.enterDecision(77, decisionCanBacktrack[77]);

                        int LA77_0 = input.LA(1);

                        if ( (LA77_0==COMMA) ) {
                            alt77=1;
                        }


                        } finally {dbg.exitDecision(77);}

                        switch (alt77) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:16: COMMA ( WS )* attribute
                    	    {
                    	    dbg.location(455,16);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function2279); if (state.failed) return ;
                    	    dbg.location(455,22);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:22: ( WS )*
                    	    try { dbg.enterSubRule(76);

                    	    loop76:
                    	    do {
                    	        int alt76=2;
                    	        try { dbg.enterDecision(76, decisionCanBacktrack[76]);

                    	        int LA76_0 = input.LA(1);

                    	        if ( (LA76_0==WS) ) {
                    	            alt76=1;
                    	        }


                    	        } finally {dbg.exitDecision(76);}

                    	        switch (alt76) {
                    	    	case 1 :
                    	    	    dbg.enterAlt(1);

                    	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:22: WS
                    	    	    {
                    	    	    dbg.location(455,22);
                    	    	    match(input,WS,FOLLOW_WS_in_function2281); if (state.failed) return ;

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop76;
                    	        }
                    	    } while (true);
                    	    } finally {dbg.exitSubRule(76);}

                    	    dbg.location(455,26);
                    	    pushFollow(FOLLOW_attribute_in_function2284);
                    	    attribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop77;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(77);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(78);}

            dbg.location(458,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function2305); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(459, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:1: function_name : ( IDENT COLON )? IDENT ( DOT IDENT )* ;
    public final void function_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(461, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:2: ( ( IDENT COLON )? IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:4: ( IDENT COLON )? IDENT ( DOT IDENT )*
            {
            dbg.location(465,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:4: ( IDENT COLON )?
            int alt79=2;
            try { dbg.enterSubRule(79);
            try { dbg.enterDecision(79, decisionCanBacktrack[79]);

            int LA79_0 = input.LA(1);

            if ( (LA79_0==IDENT) ) {
                int LA79_1 = input.LA(2);

                if ( (LA79_1==COLON) ) {
                    alt79=1;
                }
            }
            } finally {dbg.exitDecision(79);}

            switch (alt79) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:5: IDENT COLON
                    {
                    dbg.location(465,5);
                    match(input,IDENT,FOLLOW_IDENT_in_function_name2348); if (state.failed) return ;
                    dbg.location(465,11);
                    match(input,COLON,FOLLOW_COLON_in_function_name2350); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(79);}

            dbg.location(465,19);
            match(input,IDENT,FOLLOW_IDENT_in_function_name2354); if (state.failed) return ;
            dbg.location(465,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:25: ( DOT IDENT )*
            try { dbg.enterSubRule(80);

            loop80:
            do {
                int alt80=2;
                try { dbg.enterDecision(80, decisionCanBacktrack[80]);

                int LA80_0 = input.LA(1);

                if ( (LA80_0==DOT) ) {
                    alt80=1;
                }


                } finally {dbg.exitDecision(80);}

                switch (alt80) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:465:26: DOT IDENT
            	    {
            	    dbg.location(465,26);
            	    match(input,DOT,FOLLOW_DOT_in_function_name2357); if (state.failed) return ;
            	    dbg.location(465,30);
            	    match(input,IDENT,FOLLOW_IDENT_in_function_name2359); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop80;
                }
            } while (true);
            } finally {dbg.exitSubRule(80);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(466, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:1: attribute : attrname ( WS )* OPEQ ( WS )* attrvalue ;
    public final void attribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(468, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:2: ( attrname ( WS )* OPEQ ( WS )* attrvalue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:4: attrname ( WS )* OPEQ ( WS )* attrvalue
            {
            dbg.location(469,4);
            pushFollow(FOLLOW_attrname_in_attribute2381);
            attrname();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(469,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:13: ( WS )*
            try { dbg.enterSubRule(81);

            loop81:
            do {
                int alt81=2;
                try { dbg.enterDecision(81, decisionCanBacktrack[81]);

                int LA81_0 = input.LA(1);

                if ( (LA81_0==WS) ) {
                    alt81=1;
                }


                } finally {dbg.exitDecision(81);}

                switch (alt81) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:13: WS
            	    {
            	    dbg.location(469,13);
            	    match(input,WS,FOLLOW_WS_in_attribute2383); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop81;
                }
            } while (true);
            } finally {dbg.exitSubRule(81);}

            dbg.location(469,17);
            match(input,OPEQ,FOLLOW_OPEQ_in_attribute2386); if (state.failed) return ;
            dbg.location(469,22);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:22: ( WS )*
            try { dbg.enterSubRule(82);

            loop82:
            do {
                int alt82=2;
                try { dbg.enterDecision(82, decisionCanBacktrack[82]);

                int LA82_0 = input.LA(1);

                if ( (LA82_0==WS) ) {
                    alt82=1;
                }


                } finally {dbg.exitDecision(82);}

                switch (alt82) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:22: WS
            	    {
            	    dbg.location(469,22);
            	    match(input,WS,FOLLOW_WS_in_attribute2388); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop82;
                }
            } while (true);
            } finally {dbg.exitSubRule(82);}

            dbg.location(469,26);
            pushFollow(FOLLOW_attrvalue_in_attribute2391);
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
        dbg.location(470, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:472:1: attrname : IDENT ;
    public final void attrname() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrname");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(472, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:4: IDENT
            {
            dbg.location(473,4);
            match(input,IDENT,FOLLOW_IDENT_in_attrname2406); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(474, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:1: attrvalue : expr ;
    public final void attrvalue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrvalue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(476, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:477:2: ( expr )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:477:4: expr
            {
            dbg.location(477,4);
            pushFollow(FOLLOW_expr_in_attrvalue2418);
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
        dbg.location(478, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(480, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:7: HASH
            {
            dbg.location(481,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor2436); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(482, 5);

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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:19: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:20: esPred
        {
        dbg.location(265,20);
        pushFollow(FOLLOW_esPred_in_synpred1_Css3958);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:6: esPred
        {
        dbg.location(267,6);
        pushFollow(FOLLOW_esPred_in_synpred2_Css3976);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:284:7: ( nsPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:284:8: nsPred
        {
        dbg.location(284,8);
        pushFollow(FOLLOW_nsPred_in_synpred3_Css31027);
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


    protected DFA46 dfa46 = new DFA46(this);
    protected DFA49 dfa49 = new DFA49(this);
    protected DFA57 dfa57 = new DFA57(this);
    protected DFA67 dfa67 = new DFA67(this);
    protected DFA70 dfa70 = new DFA70(this);
    protected DFA72 dfa72 = new DFA72(this);
    protected DFA78 dfa78 = new DFA78(this);
    static final String DFA46_eotS =
        "\27\uffff";
    static final String DFA46_eofS =
        "\27\uffff";
    static final String DFA46_minS =
        "\1\6\1\uffff\1\0\1\6\1\4\1\6\1\uffff\1\0\4\4\1\0\2\4\1\0\7\4";
    static final String DFA46_maxS =
        "\1\34\1\uffff\1\0\1\20\1\31\1\20\1\uffff\1\0\1\40\1\6\1\31\1\6\1"+
        "\0\1\40\1\7\1\0\1\40\1\31\1\6\1\7\3\40";
    static final String DFA46_acceptS =
        "\1\uffff\1\2\4\uffff\1\1\20\uffff";
    static final String DFA46_specialS =
        "\2\uffff\1\2\4\uffff\1\3\4\uffff\1\1\2\uffff\1\0\7\uffff}>";
    static final String[] DFA46_transitionS = {
            "\1\1\6\uffff\1\1\1\uffff\2\1\1\uffff\1\5\1\uffff\3\1\1\uffff"+
            "\2\1\1\2\1\3\1\4",
            "",
            "\1\uffff",
            "\1\7\11\uffff\1\7",
            "\1\13\1\uffff\1\10\21\uffff\1\12\1\11",
            "\1\14\11\uffff\1\14",
            "",
            "\1\uffff",
            "\1\15\24\uffff\1\11\3\uffff\3\16\1\17",
            "\1\13\1\uffff\1\20",
            "\1\21\24\uffff\1\22",
            "\1\13\1\uffff\1\20",
            "\1\uffff",
            "\1\15\24\uffff\1\11\3\uffff\3\16\1\17",
            "\1\23\1\uffff\2\24",
            "\1\uffff",
            "\1\25\30\uffff\3\16\1\17",
            "\1\21\24\uffff\1\22",
            "\1\13\1\uffff\1\20",
            "\1\23\1\uffff\2\24",
            "\1\26\33\uffff\1\17",
            "\1\25\30\uffff\3\16\1\17",
            "\1\26\33\uffff\1\17"
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
            return "()* loopback of 265:18: ( ( esPred )=> elementSubsequent )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA46_15 = input.LA(1);

                         
                        int index46_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index46_15);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA46_12 = input.LA(1);

                         
                        int index46_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index46_12);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA46_2 = input.LA(1);

                         
                        int index46_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index46_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA46_7 = input.LA(1);

                         
                        int index46_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index46_7);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 46, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA49_eotS =
        "\7\uffff";
    static final String DFA49_eofS =
        "\7\uffff";
    static final String DFA49_minS =
        "\1\6\1\0\1\uffff\1\4\1\uffff\1\4\1\0";
    static final String DFA49_maxS =
        "\1\31\1\0\1\uffff\1\34\1\uffff\1\34\1\0";
    static final String DFA49_acceptS =
        "\2\uffff\1\1\1\uffff\1\2\2\uffff";
    static final String DFA49_specialS =
        "\1\2\1\1\4\uffff\1\0}>";
    static final String[] DFA49_transitionS = {
            "\1\1\11\uffff\1\4\7\uffff\1\3\1\2",
            "\1\uffff",
            "",
            "\1\5\1\uffff\1\4\6\uffff\1\4\1\uffff\2\4\1\uffff\1\4\1\uffff"+
            "\3\4\1\uffff\1\4\1\6\3\4",
            "",
            "\1\5\1\uffff\1\4\6\uffff\1\4\1\uffff\2\4\1\uffff\1\4\1\uffff"+
            "\3\4\1\uffff\1\4\1\6\3\4",
            "\1\uffff"
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
            return "284:6: ( ( nsPred )=> namespace_wqname_prefix )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA49_6 = input.LA(1);

                         
                        int index49_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 2;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index49_6);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA49_1 = input.LA(1);

                         
                        int index49_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 2;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index49_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA49_0 = input.LA(1);

                         
                        int index49_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA49_0==IDENT) ) {s = 1;}

                        else if ( (LA49_0==PIPE) && (synpred3_Css3())) {s = 2;}

                        else if ( (LA49_0==STAR) ) {s = 3;}

                        else if ( (LA49_0==GEN) ) {s = 4;}

                         
                        input.seek(index49_0);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 49, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA57_eotS =
        "\5\uffff";
    static final String DFA57_eofS =
        "\5\uffff";
    static final String DFA57_minS =
        "\2\4\2\uffff\1\4";
    static final String DFA57_maxS =
        "\1\31\1\40\2\uffff\1\40";
    static final String DFA57_acceptS =
        "\2\uffff\1\1\1\2\1\uffff";
    static final String DFA57_specialS =
        "\5\uffff}>";
    static final String[] DFA57_transitionS = {
            "\1\3\1\uffff\1\1\21\uffff\2\2",
            "\1\4\24\uffff\1\2\3\uffff\4\3",
            "",
            "",
            "\1\4\24\uffff\1\2\3\uffff\4\3"
    };

    static final short[] DFA57_eot = DFA.unpackEncodedString(DFA57_eotS);
    static final short[] DFA57_eof = DFA.unpackEncodedString(DFA57_eofS);
    static final char[] DFA57_min = DFA.unpackEncodedStringToUnsignedChars(DFA57_minS);
    static final char[] DFA57_max = DFA.unpackEncodedStringToUnsignedChars(DFA57_maxS);
    static final short[] DFA57_accept = DFA.unpackEncodedString(DFA57_acceptS);
    static final short[] DFA57_special = DFA.unpackEncodedString(DFA57_specialS);
    static final short[][] DFA57_transition;

    static {
        int numStates = DFA57_transitionS.length;
        DFA57_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA57_transition[i] = DFA.unpackEncodedString(DFA57_transitionS[i]);
        }
    }

    class DFA57 extends DFA {

        public DFA57(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 57;
            this.eot = DFA57_eot;
            this.eof = DFA57_eof;
            this.min = DFA57_min;
            this.max = DFA57_max;
            this.accept = DFA57_accept;
            this.special = DFA57_special;
            this.transition = DFA57_transition;
        }
        public String getDescription() {
            return "338:6: ( namespace_wqname_prefix )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA67_eotS =
        "\4\uffff";
    static final String DFA67_eofS =
        "\4\uffff";
    static final String DFA67_minS =
        "\2\4\2\uffff";
    static final String DFA67_maxS =
        "\2\41\2\uffff";
    static final String DFA67_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA67_specialS =
        "\4\uffff}>";
    static final String[] DFA67_transitionS = {
            "\1\1\1\uffff\1\3\6\uffff\1\3\1\uffff\2\3\1\uffff\1\3\1\uffff"+
            "\3\3\1\uffff\5\3\4\uffff\1\2",
            "\1\1\1\uffff\1\3\6\uffff\1\3\1\uffff\2\3\1\uffff\1\3\1\uffff"+
            "\3\3\1\uffff\5\3\4\uffff\1\2",
            "",
            ""
    };

    static final short[] DFA67_eot = DFA.unpackEncodedString(DFA67_eotS);
    static final short[] DFA67_eof = DFA.unpackEncodedString(DFA67_eofS);
    static final char[] DFA67_min = DFA.unpackEncodedStringToUnsignedChars(DFA67_minS);
    static final char[] DFA67_max = DFA.unpackEncodedStringToUnsignedChars(DFA67_maxS);
    static final short[] DFA67_accept = DFA.unpackEncodedString(DFA67_acceptS);
    static final short[] DFA67_special = DFA.unpackEncodedString(DFA67_specialS);
    static final short[][] DFA67_transition;

    static {
        int numStates = DFA67_transitionS.length;
        DFA67_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA67_transition[i] = DFA.unpackEncodedString(DFA67_transitionS[i]);
        }
    }

    class DFA67 extends DFA {

        public DFA67(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 67;
            this.eot = DFA67_eot;
            this.eof = DFA67_eof;
            this.min = DFA67_min;
            this.max = DFA67_max;
            this.accept = DFA67_accept;
            this.special = DFA67_special;
            this.transition = DFA67_transition;
        }
        public String getDescription() {
            return "382:17: ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA70_eotS =
        "\7\uffff";
    static final String DFA70_eofS =
        "\7\uffff";
    static final String DFA70_minS =
        "\1\6\1\uffff\1\4\1\uffff\3\4";
    static final String DFA70_maxS =
        "\1\53\1\uffff\1\53\1\uffff\3\53";
    static final String DFA70_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\3\uffff";
    static final String DFA70_specialS =
        "\7\uffff}>";
    static final String[] DFA70_transitionS = {
            "\3\3\1\uffff\1\1\3\uffff\1\1\1\2\1\3\2\uffff\2\3\2\uffff\1\3"+
            "\2\uffff\1\3\7\uffff\2\1\10\3",
            "",
            "\1\4\1\uffff\1\5\2\3\7\uffff\1\3\3\uffff\1\3\2\uffff\1\3\2"+
            "\uffff\1\3\11\uffff\10\3",
            "",
            "\1\4\1\uffff\1\5\2\3\7\uffff\1\3\3\uffff\1\3\2\uffff\1\3\2"+
            "\uffff\1\3\11\uffff\10\3",
            "\1\6\1\uffff\3\3\1\uffff\1\3\3\uffff\3\3\1\uffff\3\3\2\uffff"+
            "\1\3\2\uffff\2\3\1\uffff\1\1\3\uffff\13\3",
            "\1\6\1\uffff\3\3\1\uffff\1\3\3\uffff\3\3\2\uffff\2\3\2\uffff"+
            "\1\3\2\uffff\1\3\2\uffff\1\1\3\uffff\13\3"
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
            return "()* loopback of 422:12: ( operator term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA72_eotS =
        "\12\uffff";
    static final String DFA72_eofS =
        "\12\uffff";
    static final String DFA72_minS =
        "\1\6\2\uffff\1\4\4\uffff\1\4\1\uffff";
    static final String DFA72_maxS =
        "\1\53\2\uffff\1\53\4\uffff\1\53\1\uffff";
    static final String DFA72_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\7\1\uffff\1\3";
    static final String DFA72_specialS =
        "\12\uffff}>";
    static final String[] DFA72_transitionS = {
            "\1\3\1\2\1\5\7\uffff\1\4\11\uffff\1\6\11\uffff\10\1",
            "",
            "",
            "\1\10\1\uffff\3\11\1\uffff\1\11\3\uffff\3\11\1\uffff\1\7\2"+
            "\11\2\uffff\1\11\2\uffff\1\11\1\7\5\uffff\1\7\12\11",
            "",
            "",
            "",
            "",
            "\1\10\1\uffff\3\11\1\uffff\1\11\3\uffff\3\11\2\uffff\2\11\2"+
            "\uffff\1\11\2\uffff\1\11\6\uffff\1\7\12\11",
            ""
    };

    static final short[] DFA72_eot = DFA.unpackEncodedString(DFA72_eotS);
    static final short[] DFA72_eof = DFA.unpackEncodedString(DFA72_eofS);
    static final char[] DFA72_min = DFA.unpackEncodedStringToUnsignedChars(DFA72_minS);
    static final char[] DFA72_max = DFA.unpackEncodedStringToUnsignedChars(DFA72_maxS);
    static final short[] DFA72_accept = DFA.unpackEncodedString(DFA72_acceptS);
    static final short[] DFA72_special = DFA.unpackEncodedString(DFA72_specialS);
    static final short[][] DFA72_transition;

    static {
        int numStates = DFA72_transitionS.length;
        DFA72_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA72_transition[i] = DFA.unpackEncodedString(DFA72_transitionS[i]);
        }
    }

    class DFA72 extends DFA {

        public DFA72(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 72;
            this.eot = DFA72_eot;
            this.eof = DFA72_eof;
            this.min = DFA72_min;
            this.max = DFA72_max;
            this.accept = DFA72_accept;
            this.special = DFA72_special;
            this.transition = DFA72_transition;
        }
        public String getDescription() {
            return "427:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ ) | STRING | IDENT | GEN | URI | hexColor | function )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA78_eotS =
        "\5\uffff";
    static final String DFA78_eofS =
        "\5\uffff";
    static final String DFA78_minS =
        "\1\6\1\uffff\2\4\1\uffff";
    static final String DFA78_maxS =
        "\1\53\1\uffff\2\53\1\uffff";
    static final String DFA78_acceptS =
        "\1\uffff\1\1\2\uffff\1\2";
    static final String DFA78_specialS =
        "\5\uffff}>";
    static final String[] DFA78_transitionS = {
            "\1\2\2\1\7\uffff\1\1\3\uffff\1\1\2\uffff\1\1\2\uffff\1\1\11"+
            "\uffff\10\1",
            "",
            "\1\3\1\uffff\3\1\6\uffff\2\1\1\uffff\3\1\2\uffff\1\1\2\uffff"+
            "\2\1\1\uffff\1\4\3\uffff\2\1\1\uffff\10\1",
            "\1\3\1\uffff\3\1\6\uffff\2\1\2\uffff\2\1\2\uffff\1\1\2\uffff"+
            "\1\1\2\uffff\1\4\3\uffff\2\1\1\uffff\10\1",
            ""
    };

    static final short[] DFA78_eot = DFA.unpackEncodedString(DFA78_eotS);
    static final short[] DFA78_eof = DFA.unpackEncodedString(DFA78_eofS);
    static final char[] DFA78_min = DFA.unpackEncodedStringToUnsignedChars(DFA78_minS);
    static final char[] DFA78_max = DFA.unpackEncodedStringToUnsignedChars(DFA78_maxS);
    static final short[] DFA78_accept = DFA.unpackEncodedString(DFA78_acceptS);
    static final short[] DFA78_special = DFA.unpackEncodedString(DFA78_specialS);
    static final short[][] DFA78_transition;

    static {
        int numStates = DFA78_transitionS.length;
        DFA78_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA78_transition[i] = DFA.unpackEncodedString(DFA78_transitionS[i]);
        }
    }

    class DFA78 extends DFA {

        public DFA78(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 78;
            this.eot = DFA78_eot;
            this.eof = DFA78_eof;
            this.min = DFA78_min;
            this.max = DFA78_max;
            this.accept = DFA78_accept;
            this.special = DFA78_special;
            this.transition = DFA78_transition;
        }
        public String getDescription() {
            return "451:3: ( expr | ( attribute ( COMMA ( WS )* attribute )* ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_charSet_in_styleSheet79 = new BitSet(new long[]{0x000000001F071870L});
    public static final BitSet FOLLOW_WS_in_styleSheet87 = new BitSet(new long[]{0x000000001F071870L});
    public static final BitSet FOLLOW_imports_in_styleSheet99 = new BitSet(new long[]{0x000000001F071870L});
    public static final BitSet FOLLOW_WS_in_styleSheet101 = new BitSet(new long[]{0x000000001F071870L});
    public static final BitSet FOLLOW_namespace_in_styleSheet116 = new BitSet(new long[]{0x000000001F071060L});
    public static final BitSet FOLLOW_bodylist_in_styleSheet127 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_styleSheet134 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAMESPACE_SYM_in_namespace149 = new BitSet(new long[]{0x00000000000001D0L});
    public static final BitSet FOLLOW_WS_in_namespace151 = new BitSet(new long[]{0x00000000000001D0L});
    public static final BitSet FOLLOW_namespace_prefix_in_namespace155 = new BitSet(new long[]{0x00000000000001D0L});
    public static final BitSet FOLLOW_WS_in_namespace157 = new BitSet(new long[]{0x00000000000001D0L});
    public static final BitSet FOLLOW_resourceIdentifier_in_namespace163 = new BitSet(new long[]{0x0000000000000410L});
    public static final BitSet FOLLOW_WS_in_namespace166 = new BitSet(new long[]{0x0000000000000410L});
    public static final BitSet FOLLOW_SEMI_in_namespace169 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_namespace171 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_IDENT_in_namespace_prefix185 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_resourceIdentifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHARSET_SYM_in_charSet224 = new BitSet(new long[]{0x0000000000000090L});
    public static final BitSet FOLLOW_WS_in_charSet226 = new BitSet(new long[]{0x0000000000000090L});
    public static final BitSet FOLLOW_STRING_in_charSet229 = new BitSet(new long[]{0x0000000000000410L});
    public static final BitSet FOLLOW_WS_in_charSet231 = new BitSet(new long[]{0x0000000000000410L});
    public static final BitSet FOLLOW_SEMI_in_charSet234 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORT_SYM_in_imports256 = new BitSet(new long[]{0x00000000000001D0L});
    public static final BitSet FOLLOW_WS_in_imports258 = new BitSet(new long[]{0x00000000000001D0L});
    public static final BitSet FOLLOW_resourceIdentifier_in_imports262 = new BitSet(new long[]{0x0000000000010450L});
    public static final BitSet FOLLOW_WS_in_imports265 = new BitSet(new long[]{0x0000000000010450L});
    public static final BitSet FOLLOW_mediaList_in_imports268 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_SEMI_in_imports271 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEDIA_SYM_in_media292 = new BitSet(new long[]{0x0000000000010050L});
    public static final BitSet FOLLOW_WS_in_media294 = new BitSet(new long[]{0x0000000000010050L});
    public static final BitSet FOLLOW_mediaList_in_media297 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_media307 = new BitSet(new long[]{0x000000001F050050L});
    public static final BitSet FOLLOW_WS_in_media309 = new BitSet(new long[]{0x000000001F050050L});
    public static final BitSet FOLLOW_ruleSet_in_media324 = new BitSet(new long[]{0x0000000000004010L});
    public static final BitSet FOLLOW_WS_in_media334 = new BitSet(new long[]{0x0000000000004010L});
    public static final BitSet FOLLOW_RBRACE_in_media337 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_medium_in_mediaList358 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_COMMA_in_mediaList361 = new BitSet(new long[]{0x0000000000010050L});
    public static final BitSet FOLLOW_WS_in_mediaList363 = new BitSet(new long[]{0x0000000000010050L});
    public static final BitSet FOLLOW_medium_in_mediaList366 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_set_in_medium385 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_medium395 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_bodyset_in_bodylist418 = new BitSet(new long[]{0x000000001F071042L});
    public static final BitSet FOLLOW_ruleSet_in_bodyset447 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_media_in_bodyset459 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_page_in_bodyset471 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_bodyset487 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page509 = new BitSet(new long[]{0x0000000000042010L});
    public static final BitSet FOLLOW_WS_in_page511 = new BitSet(new long[]{0x0000000000042000L});
    public static final BitSet FOLLOW_pseudoPage_in_page515 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_WS_in_page517 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_LBRACE_in_page530 = new BitSet(new long[]{0x0000000000010050L});
    public static final BitSet FOLLOW_WS_in_page532 = new BitSet(new long[]{0x0000000000010050L});
    public static final BitSet FOLLOW_declaration_in_page547 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_SEMI_in_page549 = new BitSet(new long[]{0x0000000000014050L});
    public static final BitSet FOLLOW_declaration_in_page552 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_SEMI_in_page554 = new BitSet(new long[]{0x0000000000014050L});
    public static final BitSet FOLLOW_RBRACE_in_page566 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage587 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage589 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_operator610 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_operator612 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_COMMA_in_operator621 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_operator623 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_PLUS_in_combinator651 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator653 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_GREATER_in_combinator662 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator664 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_TILDE_in_combinator673 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator675 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_property735 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_property743 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_selectorsGroup_in_ruleSet768 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_ruleSet778 = new BitSet(new long[]{0x0000000000014450L});
    public static final BitSet FOLLOW_WS_in_ruleSet780 = new BitSet(new long[]{0x0000000000014450L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_ruleSet783 = new BitSet(new long[]{0x0000000000014450L});
    public static final BitSet FOLLOW_declarations_in_ruleSet797 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_ruleSet807 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_declarations845 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_SEMI_in_declarations849 = new BitSet(new long[]{0x0000000000010452L});
    public static final BitSet FOLLOW_WS_in_declarations851 = new BitSet(new long[]{0x0000000000010452L});
    public static final BitSet FOLLOW_declaration_in_declarations854 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup878 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup881 = new BitSet(new long[]{0x000000001F050050L});
    public static final BitSet FOLLOW_WS_in_selectorsGroup883 = new BitSet(new long[]{0x000000001F050050L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup886 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector909 = new BitSet(new long[]{0x000000001F750042L});
    public static final BitSet FOLLOW_combinator_in_selector912 = new BitSet(new long[]{0x000000001F050040L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector914 = new BitSet(new long[]{0x000000001F750042L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence954 = new BitSet(new long[]{0x000000001F050042L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence961 = new BitSet(new long[]{0x000000001F050042L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence979 = new BitSet(new long[]{0x000000001F050042L});
    public static final BitSet FOLLOW_namespace_wqname_prefix_in_typeSelector1030 = new BitSet(new long[]{0x0000000003010040L});
    public static final BitSet FOLLOW_elementName_in_typeSelector1036 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_typeSelector1038 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_set_in_nsPred1063 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_PIPE_in_nsPred1071 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespace_prefix_in_namespace_wqname_prefix1101 = new BitSet(new long[]{0x0000000002000010L});
    public static final BitSet FOLLOW_WS_in_namespace_wqname_prefix1103 = new BitSet(new long[]{0x0000000002000010L});
    public static final BitSet FOLLOW_PIPE_in_namespace_wqname_prefix1109 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespace_wildcard_prefix_in_namespace_wqname_prefix1116 = new BitSet(new long[]{0x0000000002000010L});
    public static final BitSet FOLLOW_WS_in_namespace_wqname_prefix1118 = new BitSet(new long[]{0x0000000002000010L});
    public static final BitSet FOLLOW_PIPE_in_namespace_wqname_prefix1121 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_namespace_wildcard_prefix1143 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent1212 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent1221 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_attrib_in_elementSubsequent1233 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent1245 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_elementSubsequent1257 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_HASH_in_cssId1279 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass1296 = new BitSet(new long[]{0x0000000000010040L});
    public static final BitSet FOLLOW_set_in_cssClass1298 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_attrib1362 = new BitSet(new long[]{0x0000000003000050L});
    public static final BitSet FOLLOW_namespace_wqname_prefix_in_attrib1369 = new BitSet(new long[]{0x0000000003000050L});
    public static final BitSet FOLLOW_WS_in_attrib1372 = new BitSet(new long[]{0x0000000003000050L});
    public static final BitSet FOLLOW_attrib_name_in_attrib1383 = new BitSet(new long[]{0x00000001E0000010L});
    public static final BitSet FOLLOW_WS_in_attrib1385 = new BitSet(new long[]{0x00000001E0000010L});
    public static final BitSet FOLLOW_set_in_attrib1427 = new BitSet(new long[]{0x00000000000000D0L});
    public static final BitSet FOLLOW_WS_in_attrib1535 = new BitSet(new long[]{0x00000000000000D0L});
    public static final BitSet FOLLOW_attrib_value_in_attrib1554 = new BitSet(new long[]{0x0000000100000010L});
    public static final BitSet FOLLOW_WS_in_attrib1572 = new BitSet(new long[]{0x0000000100000010L});
    public static final BitSet FOLLOW_RBRACKET_in_attrib1601 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_attrib_name1644 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_attrib_value1658 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudo1718 = new BitSet(new long[]{0x0000000000010040L});
    public static final BitSet FOLLOW_set_in_pseudo1733 = new BitSet(new long[]{0x0000000200000012L});
    public static final BitSet FOLLOW_WS_in_pseudo1782 = new BitSet(new long[]{0x0000000200000010L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo1785 = new BitSet(new long[]{0x0000000400010050L});
    public static final BitSet FOLLOW_WS_in_pseudo1787 = new BitSet(new long[]{0x0000000400010050L});
    public static final BitSet FOLLOW_set_in_pseudo1791 = new BitSet(new long[]{0x0000000400000010L});
    public static final BitSet FOLLOW_WS_in_pseudo1801 = new BitSet(new long[]{0x0000000400000010L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo1806 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_declaration1852 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_COLON_in_declaration1854 = new BitSet(new long[]{0x00000FF0049101D0L});
    public static final BitSet FOLLOW_WS_in_declaration1856 = new BitSet(new long[]{0x00000FF0049101D0L});
    public static final BitSet FOLLOW_expr_in_declaration1859 = new BitSet(new long[]{0x0000000800000002L});
    public static final BitSet FOLLOW_prio_in_declaration1861 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio1954 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expr1975 = new BitSet(new long[]{0x00000FF0049981D2L});
    public static final BitSet FOLLOW_operator_in_expr1978 = new BitSet(new long[]{0x00000FF0049101D0L});
    public static final BitSet FOLLOW_term_in_expr1980 = new BitSet(new long[]{0x00000FF0049981D2L});
    public static final BitSet FOLLOW_unaryOperator_in_term2003 = new BitSet(new long[]{0x00000FF0049101D0L});
    public static final BitSet FOLLOW_set_in_term2024 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_STRING_in_term2170 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_IDENT_in_term2178 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_GEN_in_term2186 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_URI_in_term2194 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_hexColor_in_term2202 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_function_in_term2210 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_term2222 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_function_name_in_function2238 = new BitSet(new long[]{0x0000000200000010L});
    public static final BitSet FOLLOW_WS_in_function2240 = new BitSet(new long[]{0x0000000200000010L});
    public static final BitSet FOLLOW_LPAREN_in_function2245 = new BitSet(new long[]{0x00000FF0049101D0L});
    public static final BitSet FOLLOW_WS_in_function2247 = new BitSet(new long[]{0x00000FF0049101D0L});
    public static final BitSet FOLLOW_expr_in_function2258 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_attribute_in_function2276 = new BitSet(new long[]{0x0000000400008000L});
    public static final BitSet FOLLOW_COMMA_in_function2279 = new BitSet(new long[]{0x00000FF0049101D0L});
    public static final BitSet FOLLOW_WS_in_function2281 = new BitSet(new long[]{0x00000FF0049101D0L});
    public static final BitSet FOLLOW_attribute_in_function2284 = new BitSet(new long[]{0x0000000400008000L});
    public static final BitSet FOLLOW_RPAREN_in_function2305 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_function_name2348 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_COLON_in_function_name2350 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_function_name2354 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_DOT_in_function_name2357 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_function_name2359 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_attrname_in_attribute2381 = new BitSet(new long[]{0x0000000020000010L});
    public static final BitSet FOLLOW_WS_in_attribute2383 = new BitSet(new long[]{0x0000000020000010L});
    public static final BitSet FOLLOW_OPEQ_in_attribute2386 = new BitSet(new long[]{0x00000FF0049101D0L});
    public static final BitSet FOLLOW_WS_in_attribute2388 = new BitSet(new long[]{0x00000FF0049101D0L});
    public static final BitSet FOLLOW_attrvalue_in_attribute2391 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_attrname2406 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_in_attrvalue2418 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor2436 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred1_Css3958 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred2_Css3976 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nsPred_in_synpred3_Css31027 = new BitSet(new long[]{0x0000000000000002L});

}