// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2011-08-19 16:01:12

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "WS", "NAMESPACE_SYM", "IDENT", "STRING", "URI", "CHARSET_SYM", "SEMI", "IMPORT_SYM", "MEDIA_SYM", "LBRACE", "RBRACE", "COMMA", "ONLY", "NOT", "AND", "GEN", "PAGE_SYM", "COLON", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "STAR", "PIPE", "HASH", "DOT", "LBRACKET", "DCOLON", "OPEQ", "INCLUDES", "DASHMATCH", "RBRACKET", "LPAREN", "RPAREN", "IMPORTANT_SYM", "NUMBER", "PERCENTAGE", "LENGTH", "EMS", "EXS", "ANGLE", "TIME", "FREQ", "RESOLUTION", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "NAME", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "COMMENT", "CDO", "CDC", "INVALID", "DIMENSION", "NL"
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
    public static final int ONLY=16;
    public static final int NOT=17;
    public static final int AND=18;
    public static final int GEN=19;
    public static final int PAGE_SYM=20;
    public static final int COLON=21;
    public static final int SOLIDUS=22;
    public static final int PLUS=23;
    public static final int GREATER=24;
    public static final int TILDE=25;
    public static final int MINUS=26;
    public static final int STAR=27;
    public static final int PIPE=28;
    public static final int HASH=29;
    public static final int DOT=30;
    public static final int LBRACKET=31;
    public static final int DCOLON=32;
    public static final int OPEQ=33;
    public static final int INCLUDES=34;
    public static final int DASHMATCH=35;
    public static final int RBRACKET=36;
    public static final int LPAREN=37;
    public static final int RPAREN=38;
    public static final int IMPORTANT_SYM=39;
    public static final int NUMBER=40;
    public static final int PERCENTAGE=41;
    public static final int LENGTH=42;
    public static final int EMS=43;
    public static final int EXS=44;
    public static final int ANGLE=45;
    public static final int TIME=46;
    public static final int FREQ=47;
    public static final int RESOLUTION=48;
    public static final int HEXCHAR=49;
    public static final int NONASCII=50;
    public static final int UNICODE=51;
    public static final int ESCAPE=52;
    public static final int NMSTART=53;
    public static final int NMCHAR=54;
    public static final int NAME=55;
    public static final int URL=56;
    public static final int A=57;
    public static final int B=58;
    public static final int C=59;
    public static final int D=60;
    public static final int E=61;
    public static final int F=62;
    public static final int G=63;
    public static final int H=64;
    public static final int I=65;
    public static final int J=66;
    public static final int K=67;
    public static final int L=68;
    public static final int M=69;
    public static final int N=70;
    public static final int O=71;
    public static final int P=72;
    public static final int Q=73;
    public static final int R=74;
    public static final int S=75;
    public static final int T=76;
    public static final int U=77;
    public static final int V=78;
    public static final int W=79;
    public static final int X=80;
    public static final int Y=81;
    public static final int Z=82;
    public static final int COMMENT=83;
    public static final int CDO=84;
    public static final int CDC=85;
    public static final int INVALID=86;
    public static final int DIMENSION=87;
    public static final int NL=88;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "pseudo", "hexColor", "esPred", "synpred3_Css3", 
        "selectorsGroup", "unaryOperator", "cssId", "media_feature", "media_query", 
        "bodylist", "attrib_name", "elementSubsequent", "page", "expr", 
        "imports", "combinator", "function_name", "medium", "elementName", 
        "namespace_prefix", "bodyset", "attrib_value", "attrib", "charSet", 
        "media", "declaration", "synpred1_Css3", "attrvalue", "namespace", 
        "resourceIdentifier", "prio", "synpred2_Css3", "attribute", "nsPred", 
        "declarations", "syncTo_IDENT_RBRACKET_LBRACE", "syncToFollow", 
        "attrname", "media_query_list", "operator", "selector", "namespace_wqname_prefix", 
        "term", "styleSheet", "media_type", "media_expression", "pseudoPage", 
        "syncTo_IDENT_RBRACE", "ruleSet", "typeSelector", "namespace_wildcard_prefix", 
        "function", "cssClass", "property", "simpleSelectorSequence"
    };
    public static final boolean[] decisionCanBacktrack = new boolean[] {
        false, // invalid decision
        false, false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, true, true, false, true, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:164:1: imports : IMPORT_SYM ( WS )* ( resourceIdentifier ) ( WS )* media_query_list SEMI ;
    public final void imports() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "imports");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(164, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:165:5: ( IMPORT_SYM ( WS )* ( resourceIdentifier ) ( WS )* media_query_list SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:165:9: IMPORT_SYM ( WS )* ( resourceIdentifier ) ( WS )* media_query_list SEMI
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
            pushFollow(FOLLOW_media_query_list_in_imports268);
            media_query_list();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(165,66);
            match(input,SEMI,FOLLOW_SEMI_in_imports270); if (state.failed) return ;

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:172:1: media : MEDIA_SYM ( WS )* media_query_list LBRACE ( WS )* ruleSet ( WS )* RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(172, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:173:5: ( MEDIA_SYM ( WS )* media_query_list LBRACE ( WS )* ruleSet ( WS )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:173:7: MEDIA_SYM ( WS )* media_query_list LBRACE ( WS )* ruleSet ( WS )* RBRACE
            {
            dbg.location(173,7);
            match(input,MEDIA_SYM,FOLLOW_MEDIA_SYM_in_media291); if (state.failed) return ;
            dbg.location(173,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:173:17: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:173:17: WS
            	    {
            	    dbg.location(173,17);
            	    match(input,WS,FOLLOW_WS_in_media293); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);
            } finally {dbg.exitSubRule(15);}

            dbg.location(173,21);
            pushFollow(FOLLOW_media_query_list_in_media296);
            media_query_list();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(174,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_media306); if (state.failed) return ;
            dbg.location(174,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:174:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:174:16: WS
            	    {
            	    dbg.location(174,16);
            	    match(input,WS,FOLLOW_WS_in_media308); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);
            } finally {dbg.exitSubRule(16);}

            dbg.location(175,13);
            pushFollow(FOLLOW_ruleSet_in_media323);
            ruleSet();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(176,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:176:9: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:176:9: WS
            	    {
            	    dbg.location(176,9);
            	    match(input,WS,FOLLOW_WS_in_media333); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop17;
                }
            } while (true);
            } finally {dbg.exitSubRule(17);}

            dbg.location(176,13);
            match(input,RBRACE,FOLLOW_RBRACE_in_media336); if (state.failed) return ;

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


    // $ANTLR start "media_query_list"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:1: media_query_list : ( media_query ( COMMA ( WS )* media_query )* )? ;
    public final void media_query_list() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_query_list");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(183, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:184:2: ( ( media_query ( COMMA ( WS )* media_query )* )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:184:4: ( media_query ( COMMA ( WS )* media_query )* )?
            {
            dbg.location(184,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:184:4: ( media_query ( COMMA ( WS )* media_query )* )?
            int alt20=2;
            try { dbg.enterSubRule(20);
            try { dbg.enterDecision(20, decisionCanBacktrack[20]);

            int LA20_0 = input.LA(1);

            if ( (LA20_0==IDENT||(LA20_0>=ONLY && LA20_0<=NOT)||LA20_0==GEN||LA20_0==LPAREN) ) {
                alt20=1;
            }
            } finally {dbg.exitDecision(20);}

            switch (alt20) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:184:6: media_query ( COMMA ( WS )* media_query )*
                    {
                    dbg.location(184,6);
                    pushFollow(FOLLOW_media_query_in_media_query_list356);
                    media_query();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(184,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:184:18: ( COMMA ( WS )* media_query )*
                    try { dbg.enterSubRule(19);

                    loop19:
                    do {
                        int alt19=2;
                        try { dbg.enterDecision(19, decisionCanBacktrack[19]);

                        int LA19_0 = input.LA(1);

                        if ( (LA19_0==COMMA) ) {
                            alt19=1;
                        }


                        } finally {dbg.exitDecision(19);}

                        switch (alt19) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:184:20: COMMA ( WS )* media_query
                    	    {
                    	    dbg.location(184,20);
                    	    match(input,COMMA,FOLLOW_COMMA_in_media_query_list360); if (state.failed) return ;
                    	    dbg.location(184,26);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:184:26: ( WS )*
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

                    	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:184:26: WS
                    	    	    {
                    	    	    dbg.location(184,26);
                    	    	    match(input,WS,FOLLOW_WS_in_media_query_list362); if (state.failed) return ;

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop18;
                    	        }
                    	    } while (true);
                    	    } finally {dbg.exitSubRule(18);}

                    	    dbg.location(184,30);
                    	    pushFollow(FOLLOW_media_query_in_media_query_list365);
                    	    media_query();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop19;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(19);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(20);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(185, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "media_query_list");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "media_query_list"


    // $ANTLR start "media_query"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:187:1: media_query : ( ( ( ONLY | NOT ) ( WS )* )? media_type ( WS )* ( AND ( WS )* media_expression )* | media_expression ( AND ( WS )* media_expression )* );
    public final void media_query() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_query");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(187, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:188:2: ( ( ( ONLY | NOT ) ( WS )* )? media_type ( WS )* ( AND ( WS )* media_expression )* | media_expression ( AND ( WS )* media_expression )* )
            int alt28=2;
            try { dbg.enterDecision(28, decisionCanBacktrack[28]);

            int LA28_0 = input.LA(1);

            if ( (LA28_0==IDENT||(LA28_0>=ONLY && LA28_0<=NOT)||LA28_0==GEN) ) {
                alt28=1;
            }
            else if ( (LA28_0==LPAREN) ) {
                alt28=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 28, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(28);}

            switch (alt28) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:188:4: ( ( ONLY | NOT ) ( WS )* )? media_type ( WS )* ( AND ( WS )* media_expression )*
                    {
                    dbg.location(188,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:188:4: ( ( ONLY | NOT ) ( WS )* )?
                    int alt22=2;
                    try { dbg.enterSubRule(22);
                    try { dbg.enterDecision(22, decisionCanBacktrack[22]);

                    int LA22_0 = input.LA(1);

                    if ( ((LA22_0>=ONLY && LA22_0<=NOT)) ) {
                        alt22=1;
                    }
                    } finally {dbg.exitDecision(22);}

                    switch (alt22) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:188:5: ( ONLY | NOT ) ( WS )*
                            {
                            dbg.location(188,5);
                            if ( (input.LA(1)>=ONLY && input.LA(1)<=NOT) ) {
                                input.consume();
                                state.errorRecovery=false;state.failed=false;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return ;}
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                dbg.recognitionException(mse);
                                throw mse;
                            }

                            dbg.location(188,18);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:188:18: ( WS )*
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

                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:188:18: WS
                            	    {
                            	    dbg.location(188,18);
                            	    match(input,WS,FOLLOW_WS_in_media_query392); if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop21;
                                }
                            } while (true);
                            } finally {dbg.exitSubRule(21);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(22);}

                    dbg.location(188,26);
                    pushFollow(FOLLOW_media_type_in_media_query399);
                    media_type();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(188,37);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:188:37: ( WS )*
                    try { dbg.enterSubRule(23);

                    loop23:
                    do {
                        int alt23=2;
                        try { dbg.enterDecision(23, decisionCanBacktrack[23]);

                        int LA23_0 = input.LA(1);

                        if ( (LA23_0==WS) ) {
                            alt23=1;
                        }


                        } finally {dbg.exitDecision(23);}

                        switch (alt23) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:188:37: WS
                    	    {
                    	    dbg.location(188,37);
                    	    match(input,WS,FOLLOW_WS_in_media_query401); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop23;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(23);}

                    dbg.location(188,41);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:188:41: ( AND ( WS )* media_expression )*
                    try { dbg.enterSubRule(25);

                    loop25:
                    do {
                        int alt25=2;
                        try { dbg.enterDecision(25, decisionCanBacktrack[25]);

                        int LA25_0 = input.LA(1);

                        if ( (LA25_0==AND) ) {
                            alt25=1;
                        }


                        } finally {dbg.exitDecision(25);}

                        switch (alt25) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:188:43: AND ( WS )* media_expression
                    	    {
                    	    dbg.location(188,43);
                    	    match(input,AND,FOLLOW_AND_in_media_query406); if (state.failed) return ;
                    	    dbg.location(188,47);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:188:47: ( WS )*
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

                    	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:188:47: WS
                    	    	    {
                    	    	    dbg.location(188,47);
                    	    	    match(input,WS,FOLLOW_WS_in_media_query408); if (state.failed) return ;

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop24;
                    	        }
                    	    } while (true);
                    	    } finally {dbg.exitSubRule(24);}

                    	    dbg.location(188,51);
                    	    pushFollow(FOLLOW_media_expression_in_media_query411);
                    	    media_expression();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop25;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(25);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:189:4: media_expression ( AND ( WS )* media_expression )*
                    {
                    dbg.location(189,4);
                    pushFollow(FOLLOW_media_expression_in_media_query419);
                    media_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(189,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:189:21: ( AND ( WS )* media_expression )*
                    try { dbg.enterSubRule(27);

                    loop27:
                    do {
                        int alt27=2;
                        try { dbg.enterDecision(27, decisionCanBacktrack[27]);

                        int LA27_0 = input.LA(1);

                        if ( (LA27_0==AND) ) {
                            alt27=1;
                        }


                        } finally {dbg.exitDecision(27);}

                        switch (alt27) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:189:23: AND ( WS )* media_expression
                    	    {
                    	    dbg.location(189,23);
                    	    match(input,AND,FOLLOW_AND_in_media_query423); if (state.failed) return ;
                    	    dbg.location(189,27);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:189:27: ( WS )*
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

                    	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:189:27: WS
                    	    	    {
                    	    	    dbg.location(189,27);
                    	    	    match(input,WS,FOLLOW_WS_in_media_query425); if (state.failed) return ;

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop26;
                    	        }
                    	    } while (true);
                    	    } finally {dbg.exitSubRule(26);}

                    	    dbg.location(189,31);
                    	    pushFollow(FOLLOW_media_expression_in_media_query428);
                    	    media_expression();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop27;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(27);}


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
        dbg.location(190, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "media_query");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "media_query"


    // $ANTLR start "media_type"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:192:1: media_type : ( IDENT | GEN );
    public final void media_type() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_type");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(192, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:193:2: ( IDENT | GEN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(193,2);
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
        dbg.location(194, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "media_type");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "media_type"


    // $ANTLR start "media_expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:196:1: media_expression : '(' ( WS )* media_feature ( WS )* ( ':' ( WS )* expr )? ')' ( WS )* ;
    public final void media_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(196, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:197:2: ( '(' ( WS )* media_feature ( WS )* ( ':' ( WS )* expr )? ')' ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:197:4: '(' ( WS )* media_feature ( WS )* ( ':' ( WS )* expr )? ')' ( WS )*
            {
            dbg.location(197,4);
            match(input,LPAREN,FOLLOW_LPAREN_in_media_expression459); if (state.failed) return ;
            dbg.location(197,8);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:197:8: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:197:8: WS
            	    {
            	    dbg.location(197,8);
            	    match(input,WS,FOLLOW_WS_in_media_expression461); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop29;
                }
            } while (true);
            } finally {dbg.exitSubRule(29);}

            dbg.location(197,12);
            pushFollow(FOLLOW_media_feature_in_media_expression464);
            media_feature();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(197,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:197:26: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:197:26: WS
            	    {
            	    dbg.location(197,26);
            	    match(input,WS,FOLLOW_WS_in_media_expression466); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop30;
                }
            } while (true);
            } finally {dbg.exitSubRule(30);}

            dbg.location(197,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:197:30: ( ':' ( WS )* expr )?
            int alt32=2;
            try { dbg.enterSubRule(32);
            try { dbg.enterDecision(32, decisionCanBacktrack[32]);

            int LA32_0 = input.LA(1);

            if ( (LA32_0==COLON) ) {
                alt32=1;
            }
            } finally {dbg.exitDecision(32);}

            switch (alt32) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:197:32: ':' ( WS )* expr
                    {
                    dbg.location(197,32);
                    match(input,COLON,FOLLOW_COLON_in_media_expression471); if (state.failed) return ;
                    dbg.location(197,36);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:197:36: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:197:36: WS
                    	    {
                    	    dbg.location(197,36);
                    	    match(input,WS,FOLLOW_WS_in_media_expression473); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop31;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(31);}

                    dbg.location(197,40);
                    pushFollow(FOLLOW_expr_in_media_expression476);
                    expr();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(32);}

            dbg.location(197,48);
            match(input,RPAREN,FOLLOW_RPAREN_in_media_expression481); if (state.failed) return ;
            dbg.location(197,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:197:52: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:197:52: WS
            	    {
            	    dbg.location(197,52);
            	    match(input,WS,FOLLOW_WS_in_media_expression483); if (state.failed) return ;

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
        dbg.location(198, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "media_expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "media_expression"


    // $ANTLR start "media_feature"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:199:1: media_feature : IDENT ;
    public final void media_feature() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_feature");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(199, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:200:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:200:4: IDENT
            {
            dbg.location(200,4);
            match(input,IDENT,FOLLOW_IDENT_in_media_feature494); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(201, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "media_feature");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "media_feature"


    // $ANTLR start "medium"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:206:1: medium : ( IDENT | GEN ) ( WS )* ;
    public final void medium() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "medium");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(206, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:207:5: ( ( IDENT | GEN ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:207:7: ( IDENT | GEN ) ( WS )*
            {
            dbg.location(207,7);
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

            dbg.location(207,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:207:23: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:207:23: WS
            	    {
            	    dbg.location(207,23);
            	    match(input,WS,FOLLOW_WS_in_medium521); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop34;
                }
            } while (true);
            } finally {dbg.exitSubRule(34);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(208, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:211:1: bodylist : ( bodyset )* ;
    public final void bodylist() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodylist");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(211, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:212:5: ( ( bodyset )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:212:7: ( bodyset )*
            {
            dbg.location(212,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:212:7: ( bodyset )*
            try { dbg.enterSubRule(35);

            loop35:
            do {
                int alt35=2;
                try { dbg.enterDecision(35, decisionCanBacktrack[35]);

                int LA35_0 = input.LA(1);

                if ( (LA35_0==IDENT||LA35_0==MEDIA_SYM||(LA35_0>=GEN && LA35_0<=COLON)||(LA35_0>=STAR && LA35_0<=DCOLON)) ) {
                    alt35=1;
                }


                } finally {dbg.exitDecision(35);}

                switch (alt35) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:212:7: bodyset
            	    {
            	    dbg.location(212,7);
            	    pushFollow(FOLLOW_bodyset_in_bodylist544);
            	    bodyset();

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
        dbg.location(213, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:215:1: bodyset : ( ruleSet | media | page ) ( WS )* ;
    public final void bodyset() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyset");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(215, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:216:5: ( ( ruleSet | media | page ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:216:7: ( ruleSet | media | page ) ( WS )*
            {
            dbg.location(216,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:216:7: ( ruleSet | media | page )
            int alt36=3;
            try { dbg.enterSubRule(36);
            try { dbg.enterDecision(36, decisionCanBacktrack[36]);

            switch ( input.LA(1) ) {
            case IDENT:
            case GEN:
            case COLON:
            case STAR:
            case PIPE:
            case HASH:
            case DOT:
            case LBRACKET:
            case DCOLON:
                {
                alt36=1;
                }
                break;
            case MEDIA_SYM:
                {
                alt36=2;
                }
                break;
            case PAGE_SYM:
                {
                alt36=3;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:217:6: ruleSet
                    {
                    dbg.location(217,6);
                    pushFollow(FOLLOW_ruleSet_in_bodyset573);
                    ruleSet();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:218:11: media
                    {
                    dbg.location(218,11);
                    pushFollow(FOLLOW_media_in_bodyset585);
                    media();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:219:11: page
                    {
                    dbg.location(219,11);
                    pushFollow(FOLLOW_page_in_bodyset597);
                    page();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(36);}

            dbg.location(221,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:221:7: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:221:7: WS
            	    {
            	    dbg.location(221,7);
            	    match(input,WS,FOLLOW_WS_in_bodyset613); if (state.failed) return ;

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
        dbg.location(222, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:224:1: page : PAGE_SYM ( WS )? ( pseudoPage ( WS )* )? LBRACE ( WS )* declaration SEMI ( declaration SEMI )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(224, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:225:5: ( PAGE_SYM ( WS )? ( pseudoPage ( WS )* )? LBRACE ( WS )* declaration SEMI ( declaration SEMI )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:225:7: PAGE_SYM ( WS )? ( pseudoPage ( WS )* )? LBRACE ( WS )* declaration SEMI ( declaration SEMI )* RBRACE
            {
            dbg.location(225,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page635); if (state.failed) return ;
            dbg.location(225,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:225:16: ( WS )?
            int alt38=2;
            try { dbg.enterSubRule(38);
            try { dbg.enterDecision(38, decisionCanBacktrack[38]);

            int LA38_0 = input.LA(1);

            if ( (LA38_0==WS) ) {
                alt38=1;
            }
            } finally {dbg.exitDecision(38);}

            switch (alt38) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:225:16: WS
                    {
                    dbg.location(225,16);
                    match(input,WS,FOLLOW_WS_in_page637); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(38);}

            dbg.location(225,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:225:20: ( pseudoPage ( WS )* )?
            int alt40=2;
            try { dbg.enterSubRule(40);
            try { dbg.enterDecision(40, decisionCanBacktrack[40]);

            int LA40_0 = input.LA(1);

            if ( (LA40_0==COLON) ) {
                alt40=1;
            }
            } finally {dbg.exitDecision(40);}

            switch (alt40) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:225:21: pseudoPage ( WS )*
                    {
                    dbg.location(225,21);
                    pushFollow(FOLLOW_pseudoPage_in_page641);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(225,32);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:225:32: ( WS )*
                    try { dbg.enterSubRule(39);

                    loop39:
                    do {
                        int alt39=2;
                        try { dbg.enterDecision(39, decisionCanBacktrack[39]);

                        int LA39_0 = input.LA(1);

                        if ( (LA39_0==WS) ) {
                            alt39=1;
                        }


                        } finally {dbg.exitDecision(39);}

                        switch (alt39) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:225:32: WS
                    	    {
                    	    dbg.location(225,32);
                    	    match(input,WS,FOLLOW_WS_in_page643); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop39;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(39);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(40);}

            dbg.location(226,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page656); if (state.failed) return ;
            dbg.location(226,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:226:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:226:16: WS
            	    {
            	    dbg.location(226,16);
            	    match(input,WS,FOLLOW_WS_in_page658); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop41;
                }
            } while (true);
            } finally {dbg.exitSubRule(41);}

            dbg.location(227,13);
            pushFollow(FOLLOW_declaration_in_page673);
            declaration();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(227,25);
            match(input,SEMI,FOLLOW_SEMI_in_page675); if (state.failed) return ;
            dbg.location(227,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:227:30: ( declaration SEMI )*
            try { dbg.enterSubRule(42);

            loop42:
            do {
                int alt42=2;
                try { dbg.enterDecision(42, decisionCanBacktrack[42]);

                int LA42_0 = input.LA(1);

                if ( (LA42_0==IDENT||LA42_0==GEN) ) {
                    alt42=1;
                }


                } finally {dbg.exitDecision(42);}

                switch (alt42) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:227:31: declaration SEMI
            	    {
            	    dbg.location(227,31);
            	    pushFollow(FOLLOW_declaration_in_page678);
            	    declaration();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(227,43);
            	    match(input,SEMI,FOLLOW_SEMI_in_page680); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop42;
                }
            } while (true);
            } finally {dbg.exitSubRule(42);}

            dbg.location(228,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page692); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "page");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "page"


    // $ANTLR start "pseudoPage"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:231:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(231, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:232:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:232:7: COLON IDENT
            {
            dbg.location(232,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage713); if (state.failed) return ;
            dbg.location(232,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage715); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "pseudoPage");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "pseudoPage"


    // $ANTLR start "operator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:235:1: operator : ( SOLIDUS ( WS )* | COMMA ( WS )* | );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(235, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:236:5: ( SOLIDUS ( WS )* | COMMA ( WS )* | )
            int alt45=3;
            try { dbg.enterDecision(45, decisionCanBacktrack[45]);

            switch ( input.LA(1) ) {
            case SOLIDUS:
                {
                alt45=1;
                }
                break;
            case COMMA:
                {
                alt45=2;
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
            case RESOLUTION:
                {
                alt45=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 45, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(45);}

            switch (alt45) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:236:7: SOLIDUS ( WS )*
                    {
                    dbg.location(236,7);
                    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_operator736); if (state.failed) return ;
                    dbg.location(236,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:236:15: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:236:15: WS
                    	    {
                    	    dbg.location(236,15);
                    	    match(input,WS,FOLLOW_WS_in_operator738); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop43;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(43);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:237:7: COMMA ( WS )*
                    {
                    dbg.location(237,7);
                    match(input,COMMA,FOLLOW_COMMA_in_operator747); if (state.failed) return ;
                    dbg.location(237,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:237:13: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:237:13: WS
                    	    {
                    	    dbg.location(237,13);
                    	    match(input,WS,FOLLOW_WS_in_operator749); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop44;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(44);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:239:5: 
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
        dbg.location(239, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:241:1: combinator : ( PLUS ( WS )* | GREATER ( WS )* | TILDE ( WS )* | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(241, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:242:5: ( PLUS ( WS )* | GREATER ( WS )* | TILDE ( WS )* | )
            int alt49=4;
            try { dbg.enterDecision(49, decisionCanBacktrack[49]);

            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt49=1;
                }
                break;
            case GREATER:
                {
                alt49=2;
                }
                break;
            case TILDE:
                {
                alt49=3;
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
            case DCOLON:
                {
                alt49=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 49, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(49);}

            switch (alt49) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:242:7: PLUS ( WS )*
                    {
                    dbg.location(242,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator777); if (state.failed) return ;
                    dbg.location(242,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:242:12: ( WS )*
                    try { dbg.enterSubRule(46);

                    loop46:
                    do {
                        int alt46=2;
                        try { dbg.enterDecision(46, decisionCanBacktrack[46]);

                        int LA46_0 = input.LA(1);

                        if ( (LA46_0==WS) ) {
                            alt46=1;
                        }


                        } finally {dbg.exitDecision(46);}

                        switch (alt46) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:242:12: WS
                    	    {
                    	    dbg.location(242,12);
                    	    match(input,WS,FOLLOW_WS_in_combinator779); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop46;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(46);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:243:7: GREATER ( WS )*
                    {
                    dbg.location(243,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator788); if (state.failed) return ;
                    dbg.location(243,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:243:15: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:243:15: WS
                    	    {
                    	    dbg.location(243,15);
                    	    match(input,WS,FOLLOW_WS_in_combinator790); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop47;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(47);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:244:7: TILDE ( WS )*
                    {
                    dbg.location(244,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator799); if (state.failed) return ;
                    dbg.location(244,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:244:13: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:244:13: WS
                    	    {
                    	    dbg.location(244,13);
                    	    match(input,WS,FOLLOW_WS_in_combinator801); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop48;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(48);}


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:246:5: 
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
        dbg.location(246, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:248:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(248, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:249:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(249,5);
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
        dbg.location(251, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:253:1: property : ( IDENT | GEN ) ( WS )* ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(253, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:254:5: ( ( IDENT | GEN ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:254:7: ( IDENT | GEN ) ( WS )*
            {
            dbg.location(254,7);
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

            dbg.location(254,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:254:21: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:254:21: WS
            	    {
            	    dbg.location(254,21);
            	    match(input,WS,FOLLOW_WS_in_property869); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop50;
                }
            } while (true);
            } finally {dbg.exitSubRule(50);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(255, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:257:1: ruleSet : selectorsGroup LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void ruleSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ruleSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(257, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:258:5: ( selectorsGroup LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:258:9: selectorsGroup LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(258,9);
            pushFollow(FOLLOW_selectorsGroup_in_ruleSet894);
            selectorsGroup();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(259,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_ruleSet904); if (state.failed) return ;
            dbg.location(259,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:259:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:259:16: WS
            	    {
            	    dbg.location(259,16);
            	    match(input,WS,FOLLOW_WS_in_ruleSet906); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop51;
                }
            } while (true);
            } finally {dbg.exitSubRule(51);}

            dbg.location(259,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_ruleSet909);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(260,13);
            pushFollow(FOLLOW_declarations_in_ruleSet923);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(261,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_ruleSet933); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(262, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:264:1: declarations : ( declaration )? ( SEMI ( WS )* ( declaration )? )* ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(264, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:5: ( ( declaration )? ( SEMI ( WS )* ( declaration )? )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:9: ( declaration )? ( SEMI ( WS )* ( declaration )? )*
            {
            dbg.location(267,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:9: ( declaration )?
            int alt52=2;
            try { dbg.enterSubRule(52);
            try { dbg.enterDecision(52, decisionCanBacktrack[52]);

            int LA52_0 = input.LA(1);

            if ( (LA52_0==IDENT||LA52_0==GEN) ) {
                alt52=1;
            }
            } finally {dbg.exitDecision(52);}

            switch (alt52) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:9: declaration
                    {
                    dbg.location(267,9);
                    pushFollow(FOLLOW_declaration_in_declarations971);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(52);}

            dbg.location(267,22);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:22: ( SEMI ( WS )* ( declaration )? )*
            try { dbg.enterSubRule(55);

            loop55:
            do {
                int alt55=2;
                try { dbg.enterDecision(55, decisionCanBacktrack[55]);

                int LA55_0 = input.LA(1);

                if ( (LA55_0==SEMI) ) {
                    alt55=1;
                }


                } finally {dbg.exitDecision(55);}

                switch (alt55) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:23: SEMI ( WS )* ( declaration )?
            	    {
            	    dbg.location(267,23);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations975); if (state.failed) return ;
            	    dbg.location(267,28);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:28: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:28: WS
            	    	    {
            	    	    dbg.location(267,28);
            	    	    match(input,WS,FOLLOW_WS_in_declarations977); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop53;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(53);}

            	    dbg.location(267,32);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:32: ( declaration )?
            	    int alt54=2;
            	    try { dbg.enterSubRule(54);
            	    try { dbg.enterDecision(54, decisionCanBacktrack[54]);

            	    int LA54_0 = input.LA(1);

            	    if ( (LA54_0==IDENT||LA54_0==GEN) ) {
            	        alt54=1;
            	    }
            	    } finally {dbg.exitDecision(54);}

            	    switch (alt54) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:32: declaration
            	            {
            	            dbg.location(267,32);
            	            pushFollow(FOLLOW_declaration_in_declarations980);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(54);}


            	    }
            	    break;

            	default :
            	    break loop55;
                }
            } while (true);
            } finally {dbg.exitSubRule(55);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(268, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:270:1: selectorsGroup : selector ( COMMA ( WS )* selector )* ;
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(270, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:271:5: ( selector ( COMMA ( WS )* selector )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:271:7: selector ( COMMA ( WS )* selector )*
            {
            dbg.location(271,7);
            pushFollow(FOLLOW_selector_in_selectorsGroup1004);
            selector();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(271,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:271:16: ( COMMA ( WS )* selector )*
            try { dbg.enterSubRule(57);

            loop57:
            do {
                int alt57=2;
                try { dbg.enterDecision(57, decisionCanBacktrack[57]);

                int LA57_0 = input.LA(1);

                if ( (LA57_0==COMMA) ) {
                    alt57=1;
                }


                } finally {dbg.exitDecision(57);}

                switch (alt57) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:271:17: COMMA ( WS )* selector
            	    {
            	    dbg.location(271,17);
            	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup1007); if (state.failed) return ;
            	    dbg.location(271,23);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:271:23: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:271:23: WS
            	    	    {
            	    	    dbg.location(271,23);
            	    	    match(input,WS,FOLLOW_WS_in_selectorsGroup1009); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop56;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(56);}

            	    dbg.location(271,27);
            	    pushFollow(FOLLOW_selector_in_selectorsGroup1012);
            	    selector();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop57;
                }
            } while (true);
            } finally {dbg.exitSubRule(57);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(272, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:274:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(274, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:275:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:275:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(275,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector1035);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(275,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:275:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(58);

            loop58:
            do {
                int alt58=2;
                try { dbg.enterDecision(58, decisionCanBacktrack[58]);

                int LA58_0 = input.LA(1);

                if ( (LA58_0==IDENT||LA58_0==GEN||LA58_0==COLON||(LA58_0>=PLUS && LA58_0<=TILDE)||(LA58_0>=STAR && LA58_0<=DCOLON)) ) {
                    alt58=1;
                }


                } finally {dbg.exitDecision(58);}

                switch (alt58) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:275:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(275,31);
            	    pushFollow(FOLLOW_combinator_in_selector1038);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(275,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector1040);
            	    simpleSelectorSequence();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop58;
                }
            } while (true);
            } finally {dbg.exitSubRule(58);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(276, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:279:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(279, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:281:2: ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) )
            int alt61=2;
            try { dbg.enterDecision(61, decisionCanBacktrack[61]);

            int LA61_0 = input.LA(1);

            if ( (LA61_0==IDENT||LA61_0==GEN||(LA61_0>=STAR && LA61_0<=PIPE)) ) {
                alt61=1;
            }
            else if ( (LA61_0==COLON||(LA61_0>=HASH && LA61_0<=DCOLON)) ) {
                alt61=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 61, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(61);}

            switch (alt61) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:285:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    {
                    dbg.location(285,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:285:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:285:5: typeSelector ( ( esPred )=> elementSubsequent )*
                    {
                    dbg.location(285,5);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence1080);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(285,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:285:18: ( ( esPred )=> elementSubsequent )*
                    try { dbg.enterSubRule(59);

                    loop59:
                    do {
                        int alt59=2;
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:285:19: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(285,29);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence1087);
                    	    elementSubsequent();

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
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:2: ( ( ( esPred )=> elementSubsequent )+ )
                    {
                    dbg.location(287,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:2: ( ( ( esPred )=> elementSubsequent )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:4: ( ( esPred )=> elementSubsequent )+
                    {
                    dbg.location(287,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:4: ( ( esPred )=> elementSubsequent )+
                    int cnt60=0;
                    try { dbg.enterSubRule(60);

                    loop60:
                    do {
                        int alt60=2;
                        try { dbg.enterDecision(60, decisionCanBacktrack[60]);

                        switch ( input.LA(1) ) {
                        case HASH:
                            {
                            int LA60_2 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt60=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA60_3 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt60=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA60_4 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt60=1;
                            }


                            }
                            break;
                        case COLON:
                        case DCOLON:
                            {
                            int LA60_5 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt60=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(60);}

                        switch (alt60) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:5: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(287,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence1105);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt60 >= 1 ) break loop60;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(60, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt60++;
                    } while (true);
                    } finally {dbg.exitSubRule(60);}


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
        dbg.location(288, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:302:1: typeSelector options {k=2; } : ( ( nsPred )=> namespace_wqname_prefix )? ( elementName ( WS )* ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(302, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:304:3: ( ( ( nsPred )=> namespace_wqname_prefix )? ( elementName ( WS )* ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:304:6: ( ( nsPred )=> namespace_wqname_prefix )? ( elementName ( WS )* )
            {
            dbg.location(304,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:304:6: ( ( nsPred )=> namespace_wqname_prefix )?
            int alt62=2;
            try { dbg.enterSubRule(62);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:304:7: ( nsPred )=> namespace_wqname_prefix
                    {
                    dbg.location(304,17);
                    pushFollow(FOLLOW_namespace_wqname_prefix_in_typeSelector1156);
                    namespace_wqname_prefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(62);}

            dbg.location(304,43);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:304:43: ( elementName ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:304:45: elementName ( WS )*
            {
            dbg.location(304,45);
            pushFollow(FOLLOW_elementName_in_typeSelector1162);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(304,57);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:304:57: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:304:57: WS
            	    {
            	    dbg.location(304,57);
            	    match(input,WS,FOLLOW_WS_in_typeSelector1164); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop63;
                }
            } while (true);
            } finally {dbg.exitSubRule(63);}


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(305, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:307:2: nsPred : ( IDENT | STAR ) PIPE ;
    public final void nsPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "nsPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(307, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:3: ( ( IDENT | STAR ) PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:309:3: ( IDENT | STAR ) PIPE
            {
            dbg.location(309,3);
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

            dbg.location(309,18);
            match(input,PIPE,FOLLOW_PIPE_in_nsPred1197); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(310, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:318:2: namespace_wqname_prefix : ( ( namespace_prefix ( WS )* )? PIPE | namespace_wildcard_prefix ( WS )* PIPE );
    public final void namespace_wqname_prefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace_wqname_prefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(318, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:3: ( ( namespace_prefix ( WS )* )? PIPE | namespace_wildcard_prefix ( WS )* PIPE )
            int alt67=2;
            try { dbg.enterDecision(67, decisionCanBacktrack[67]);

            int LA67_0 = input.LA(1);

            if ( (LA67_0==IDENT||LA67_0==PIPE) ) {
                alt67=1;
            }
            else if ( (LA67_0==STAR) ) {
                alt67=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 67, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(67);}

            switch (alt67) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:5: ( namespace_prefix ( WS )* )? PIPE
                    {
                    dbg.location(319,5);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:5: ( namespace_prefix ( WS )* )?
                    int alt65=2;
                    try { dbg.enterSubRule(65);
                    try { dbg.enterDecision(65, decisionCanBacktrack[65]);

                    int LA65_0 = input.LA(1);

                    if ( (LA65_0==IDENT) ) {
                        alt65=1;
                    }
                    } finally {dbg.exitDecision(65);}

                    switch (alt65) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:7: namespace_prefix ( WS )*
                            {
                            dbg.location(319,7);
                            pushFollow(FOLLOW_namespace_prefix_in_namespace_wqname_prefix1227);
                            namespace_prefix();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(319,24);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:24: ( WS )*
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

                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:24: WS
                            	    {
                            	    dbg.location(319,24);
                            	    match(input,WS,FOLLOW_WS_in_namespace_wqname_prefix1229); if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop64;
                                }
                            } while (true);
                            } finally {dbg.exitSubRule(64);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(65);}

                    dbg.location(319,31);
                    match(input,PIPE,FOLLOW_PIPE_in_namespace_wqname_prefix1235); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:6: namespace_wildcard_prefix ( WS )* PIPE
                    {
                    dbg.location(320,6);
                    pushFollow(FOLLOW_namespace_wildcard_prefix_in_namespace_wqname_prefix1242);
                    namespace_wildcard_prefix();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(320,32);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:32: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:32: WS
                    	    {
                    	    dbg.location(320,32);
                    	    match(input,WS,FOLLOW_WS_in_namespace_wqname_prefix1244); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop66;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(66);}

                    dbg.location(320,36);
                    match(input,PIPE,FOLLOW_PIPE_in_namespace_wqname_prefix1247); if (state.failed) return ;

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
        dbg.location(321, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:1: namespace_wildcard_prefix : STAR ;
    public final void namespace_wildcard_prefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace_wildcard_prefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(323, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:324:4: ( STAR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:325:4: STAR
            {
            dbg.location(325,4);
            match(input,STAR,FOLLOW_STAR_in_namespace_wildcard_prefix1269); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(326, 4);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:328:1: esPred : ( HASH | DOT | LBRACKET | COLON | DCOLON );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(328, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:329:5: ( HASH | DOT | LBRACKET | COLON | DCOLON )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(329,5);
            if ( input.LA(1)==COLON||(input.LA(1)>=HASH && input.LA(1)<=DCOLON) ) {
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
        dbg.location(330, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:332:1: elementSubsequent : ( cssId | cssClass | attrib | pseudo ) ( WS )* ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(332, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:5: ( ( cssId | cssClass | attrib | pseudo ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:334:5: ( cssId | cssClass | attrib | pseudo ) ( WS )*
            {
            dbg.location(334,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:334:5: ( cssId | cssClass | attrib | pseudo )
            int alt68=4;
            try { dbg.enterSubRule(68);
            try { dbg.enterDecision(68, decisionCanBacktrack[68]);

            switch ( input.LA(1) ) {
            case HASH:
                {
                alt68=1;
                }
                break;
            case DOT:
                {
                alt68=2;
                }
                break;
            case LBRACKET:
                {
                alt68=3;
                }
                break;
            case COLON:
            case DCOLON:
                {
                alt68=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 68, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(68);}

            switch (alt68) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:335:6: cssId
                    {
                    dbg.location(335,6);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent1342);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:336:8: cssClass
                    {
                    dbg.location(336,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent1351);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:337:11: attrib
                    {
                    dbg.location(337,11);
                    pushFollow(FOLLOW_attrib_in_elementSubsequent1363);
                    attrib();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:11: pseudo
                    {
                    dbg.location(338,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent1375);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(68);}

            dbg.location(340,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:340:5: ( WS )*
            try { dbg.enterSubRule(69);

            loop69:
            do {
                int alt69=2;
                try { dbg.enterDecision(69, decisionCanBacktrack[69]);

                int LA69_0 = input.LA(1);

                if ( (LA69_0==WS) ) {
                    alt69=1;
                }


                } finally {dbg.exitDecision(69);}

                switch (alt69) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:340:5: WS
            	    {
            	    dbg.location(340,5);
            	    match(input,WS,FOLLOW_WS_in_elementSubsequent1387); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop69;
                }
            } while (true);
            } finally {dbg.exitSubRule(69);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(341, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:343:1: cssId : HASH ;
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(343, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:344:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:344:7: HASH
            {
            dbg.location(344,7);
            match(input,HASH,FOLLOW_HASH_in_cssId1409); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "cssId");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "cssId"


    // $ANTLR start "cssClass"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(347, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:7: DOT ( IDENT | GEN )
            {
            dbg.location(348,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass1426); if (state.failed) return ;
            dbg.location(348,11);
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
        dbg.location(349, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:352:1: elementName : ( ( IDENT | GEN ) | '*' );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(352, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:5: ( ( IDENT | GEN ) | '*' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(353,5);
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
        dbg.location(354, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:356:1: attrib : LBRACKET ( namespace_wqname_prefix )? ( WS )* attrib_name ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* attrib_value ( WS )* )? RBRACKET ;
    public final void attrib() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrib");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(356, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:357:5: ( LBRACKET ( namespace_wqname_prefix )? ( WS )* attrib_name ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* attrib_value ( WS )* )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:357:7: LBRACKET ( namespace_wqname_prefix )? ( WS )* attrib_name ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* attrib_value ( WS )* )? RBRACKET
            {
            dbg.location(357,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_attrib1492); if (state.failed) return ;
            dbg.location(358,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:358:6: ( namespace_wqname_prefix )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:358:6: namespace_wqname_prefix
                    {
                    dbg.location(358,6);
                    pushFollow(FOLLOW_namespace_wqname_prefix_in_attrib1499);
                    namespace_wqname_prefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(70);}

            dbg.location(358,31);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:358:31: ( WS )*
            try { dbg.enterSubRule(71);

            loop71:
            do {
                int alt71=2;
                try { dbg.enterDecision(71, decisionCanBacktrack[71]);

                int LA71_0 = input.LA(1);

                if ( (LA71_0==WS) ) {
                    alt71=1;
                }


                } finally {dbg.exitDecision(71);}

                switch (alt71) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:358:31: WS
            	    {
            	    dbg.location(358,31);
            	    match(input,WS,FOLLOW_WS_in_attrib1502); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop71;
                }
            } while (true);
            } finally {dbg.exitSubRule(71);}

            dbg.location(359,9);
            pushFollow(FOLLOW_attrib_name_in_attrib1513);
            attrib_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(359,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:21: ( WS )*
            try { dbg.enterSubRule(72);

            loop72:
            do {
                int alt72=2;
                try { dbg.enterDecision(72, decisionCanBacktrack[72]);

                int LA72_0 = input.LA(1);

                if ( (LA72_0==WS) ) {
                    alt72=1;
                }


                } finally {dbg.exitDecision(72);}

                switch (alt72) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:21: WS
            	    {
            	    dbg.location(359,21);
            	    match(input,WS,FOLLOW_WS_in_attrib1515); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop72;
                }
            } while (true);
            } finally {dbg.exitSubRule(72);}

            dbg.location(361,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:361:13: ( ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* attrib_value ( WS )* )?
            int alt75=2;
            try { dbg.enterSubRule(75);
            try { dbg.enterDecision(75, decisionCanBacktrack[75]);

            int LA75_0 = input.LA(1);

            if ( ((LA75_0>=OPEQ && LA75_0<=DASHMATCH)) ) {
                alt75=1;
            }
            } finally {dbg.exitDecision(75);}

            switch (alt75) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:17: ( OPEQ | INCLUDES | DASHMATCH ) ( WS )* attrib_value ( WS )*
                    {
                    dbg.location(362,17);
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

                    dbg.location(367,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:367:17: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:367:17: WS
                    	    {
                    	    dbg.location(367,17);
                    	    match(input,WS,FOLLOW_WS_in_attrib1665); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop73;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(73);}

                    dbg.location(368,17);
                    pushFollow(FOLLOW_attrib_value_in_attrib1684);
                    attrib_value();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(369,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:17: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:17: WS
                    	    {
                    	    dbg.location(369,17);
                    	    match(input,WS,FOLLOW_WS_in_attrib1702); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop74;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(74);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(75);}

            dbg.location(372,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_attrib1731); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(373, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:379:1: syncTo_IDENT_RBRACKET_LBRACE : ;
    public final void syncTo_IDENT_RBRACKET_LBRACE() throws RecognitionException {

                syncToSet(BitSet.of(IDENT, RBRACKET, LBRACE));
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_IDENT_RBRACKET_LBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(379, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:384:6: 
            {
            }

        }
        finally {
        }
        dbg.location(384, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:387:1: attrib_name : IDENT ;
    public final void attrib_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrib_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(387, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:388:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:388:4: IDENT
            {
            dbg.location(388,4);
            match(input,IDENT,FOLLOW_IDENT_in_attrib_name1774); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(389, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:391:1: attrib_value : ( IDENT | STRING ) ;
    public final void attrib_value() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrib_value");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(391, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:392:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:2: ( IDENT | STRING )
            {
            dbg.location(393,2);
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
        dbg.location(397, 9);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:399:1: pseudo : ( COLON | DCOLON ) ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )? ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(399, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:400:5: ( ( COLON | DCOLON ) ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:400:7: ( COLON | DCOLON ) ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )?
            {
            dbg.location(400,7);
            if ( input.LA(1)==COLON||input.LA(1)==DCOLON ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }

            dbg.location(401,13);
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

            dbg.location(402,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:17: ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )?
            int alt80=2;
            try { dbg.enterSubRule(80);
            try { dbg.enterDecision(80, decisionCanBacktrack[80]);

            try {
                isCyclicDecision = true;
                alt80 = dfa80.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(80);}

            switch (alt80) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:21: ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN
                    {
                    dbg.location(403,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:21: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:21: WS
                    	    {
                    	    dbg.location(403,21);
                    	    match(input,WS,FOLLOW_WS_in_pseudo1919); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop76;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(76);}

                    dbg.location(403,25);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo1922); if (state.failed) return ;
                    dbg.location(403,32);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:32: ( WS )*
                    try { dbg.enterSubRule(77);

                    loop77:
                    do {
                        int alt77=2;
                        try { dbg.enterDecision(77, decisionCanBacktrack[77]);

                        int LA77_0 = input.LA(1);

                        if ( (LA77_0==WS) ) {
                            alt77=1;
                        }


                        } finally {dbg.exitDecision(77);}

                        switch (alt77) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:32: WS
                    	    {
                    	    dbg.location(403,32);
                    	    match(input,WS,FOLLOW_WS_in_pseudo1924); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop77;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(77);}

                    dbg.location(403,36);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:36: ( ( IDENT | GEN ) ( WS )* )?
                    int alt79=2;
                    try { dbg.enterSubRule(79);
                    try { dbg.enterDecision(79, decisionCanBacktrack[79]);

                    int LA79_0 = input.LA(1);

                    if ( (LA79_0==IDENT||LA79_0==GEN) ) {
                        alt79=1;
                    }
                    } finally {dbg.exitDecision(79);}

                    switch (alt79) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:37: ( IDENT | GEN ) ( WS )*
                            {
                            dbg.location(403,37);
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

                            dbg.location(403,53);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:53: ( WS )*
                            try { dbg.enterSubRule(78);

                            loop78:
                            do {
                                int alt78=2;
                                try { dbg.enterDecision(78, decisionCanBacktrack[78]);

                                int LA78_0 = input.LA(1);

                                if ( (LA78_0==WS) ) {
                                    alt78=1;
                                }


                                } finally {dbg.exitDecision(78);}

                                switch (alt78) {
                            	case 1 :
                            	    dbg.enterAlt(1);

                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:53: WS
                            	    {
                            	    dbg.location(403,53);
                            	    match(input,WS,FOLLOW_WS_in_pseudo1938); if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop78;
                                }
                            } while (true);
                            } finally {dbg.exitSubRule(78);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(79);}

                    dbg.location(403,59);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo1943); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(80);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(405, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:1: declaration : property COLON ( WS )* expr ( prio )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(407, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:5: ( property COLON ( WS )* expr ( prio )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:5: property COLON ( WS )* expr ( prio )?
            {
            dbg.location(410,5);
            pushFollow(FOLLOW_property_in_declaration1989);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(410,14);
            match(input,COLON,FOLLOW_COLON_in_declaration1991); if (state.failed) return ;
            dbg.location(410,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:20: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:20: WS
            	    {
            	    dbg.location(410,20);
            	    match(input,WS,FOLLOW_WS_in_declaration1993); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop81;
                }
            } while (true);
            } finally {dbg.exitSubRule(81);}

            dbg.location(410,24);
            pushFollow(FOLLOW_expr_in_declaration1996);
            expr();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(410,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:29: ( prio )?
            int alt82=2;
            try { dbg.enterSubRule(82);
            try { dbg.enterDecision(82, decisionCanBacktrack[82]);

            int LA82_0 = input.LA(1);

            if ( (LA82_0==IMPORTANT_SYM) ) {
                alt82=1;
            }
            } finally {dbg.exitDecision(82);}

            switch (alt82) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:29: prio
                    {
                    dbg.location(410,29);
                    pushFollow(FOLLOW_prio_in_declaration1998);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(82);}


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
        dbg.location(411, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:1: syncTo_IDENT_RBRACE : ;
    public final void syncTo_IDENT_RBRACE() throws RecognitionException {

                syncToSet(BitSet.of(IDENT, RBRACE));
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_IDENT_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(421, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:425:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:6: 
            {
            }

        }
        finally {
        }
        dbg.location(426, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(429, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:433:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:6: 
            {
            }

        }
        finally {
        }
        dbg.location(434, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:437:1: prio : IMPORTANT_SYM ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(437, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:5: ( IMPORTANT_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:7: IMPORTANT_SYM
            {
            dbg.location(438,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio2091); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(439, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:441:1: expr : term ( operator term )* ;
    public final void expr() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expr");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(441, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:5: ( term ( operator term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:7: term ( operator term )*
            {
            dbg.location(442,7);
            pushFollow(FOLLOW_term_in_expr2112);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(442,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:12: ( operator term )*
            try { dbg.enterSubRule(83);

            loop83:
            do {
                int alt83=2;
                try { dbg.enterDecision(83, decisionCanBacktrack[83]);

                try {
                    isCyclicDecision = true;
                    alt83 = dfa83.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(83);}

                switch (alt83) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:13: operator term
            	    {
            	    dbg.location(442,13);
            	    pushFollow(FOLLOW_operator_in_expr2115);
            	    operator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(442,22);
            	    pushFollow(FOLLOW_term_in_expr2117);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop83;
                }
            } while (true);
            } finally {dbg.exitSubRule(83);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(443, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:1: term : ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( WS )* ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(445, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:5: ( ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:7: ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( WS )*
            {
            dbg.location(446,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:7: ( unaryOperator )?
            int alt84=2;
            try { dbg.enterSubRule(84);
            try { dbg.enterDecision(84, decisionCanBacktrack[84]);

            int LA84_0 = input.LA(1);

            if ( (LA84_0==PLUS||LA84_0==MINUS) ) {
                alt84=1;
            }
            } finally {dbg.exitDecision(84);}

            switch (alt84) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:7: unaryOperator
                    {
                    dbg.location(446,7);
                    pushFollow(FOLLOW_unaryOperator_in_term2140);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(84);}

            dbg.location(447,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION ) | STRING | IDENT | GEN | URI | hexColor | function )
            int alt85=7;
            try { dbg.enterSubRule(85);
            try { dbg.enterDecision(85, decisionCanBacktrack[85]);

            try {
                isCyclicDecision = true;
                alt85 = dfa85.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(85);}

            switch (alt85) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:448:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION )
                    {
                    dbg.location(448,9);
                    if ( (input.LA(1)>=NUMBER && input.LA(1)<=RESOLUTION) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:7: STRING
                    {
                    dbg.location(459,7);
                    match(input,STRING,FOLLOW_STRING_in_term2323); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:7: IDENT
                    {
                    dbg.location(460,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term2331); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:7: GEN
                    {
                    dbg.location(461,7);
                    match(input,GEN,FOLLOW_GEN_in_term2339); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:7: URI
                    {
                    dbg.location(462,7);
                    match(input,URI,FOLLOW_URI_in_term2347); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:463:7: hexColor
                    {
                    dbg.location(463,7);
                    pushFollow(FOLLOW_hexColor_in_term2355);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:464:7: function
                    {
                    dbg.location(464,7);
                    pushFollow(FOLLOW_function_in_term2363);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(85);}

            dbg.location(466,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:5: ( WS )*
            try { dbg.enterSubRule(86);

            loop86:
            do {
                int alt86=2;
                try { dbg.enterDecision(86, decisionCanBacktrack[86]);

                int LA86_0 = input.LA(1);

                if ( (LA86_0==WS) ) {
                    alt86=1;
                }


                } finally {dbg.exitDecision(86);}

                switch (alt86) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:5: WS
            	    {
            	    dbg.location(466,5);
            	    match(input,WS,FOLLOW_WS_in_term2375); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop86;
                }
            } while (true);
            } finally {dbg.exitSubRule(86);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(467, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:1: function : function_name ( WS )* LPAREN ( WS )* ( expr | ( attribute ( COMMA ( WS )* attribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(469, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:2: ( function_name ( WS )* LPAREN ( WS )* ( expr | ( attribute ( COMMA ( WS )* attribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:5: function_name ( WS )* LPAREN ( WS )* ( expr | ( attribute ( COMMA ( WS )* attribute )* ) ) RPAREN
            {
            dbg.location(470,5);
            pushFollow(FOLLOW_function_name_in_function2391);
            function_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(470,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:19: ( WS )*
            try { dbg.enterSubRule(87);

            loop87:
            do {
                int alt87=2;
                try { dbg.enterDecision(87, decisionCanBacktrack[87]);

                int LA87_0 = input.LA(1);

                if ( (LA87_0==WS) ) {
                    alt87=1;
                }


                } finally {dbg.exitDecision(87);}

                switch (alt87) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:19: WS
            	    {
            	    dbg.location(470,19);
            	    match(input,WS,FOLLOW_WS_in_function2393); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop87;
                }
            } while (true);
            } finally {dbg.exitSubRule(87);}

            dbg.location(471,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function2398); if (state.failed) return ;
            dbg.location(471,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:471:10: ( WS )*
            try { dbg.enterSubRule(88);

            loop88:
            do {
                int alt88=2;
                try { dbg.enterDecision(88, decisionCanBacktrack[88]);

                int LA88_0 = input.LA(1);

                if ( (LA88_0==WS) ) {
                    alt88=1;
                }


                } finally {dbg.exitDecision(88);}

                switch (alt88) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:471:10: WS
            	    {
            	    dbg.location(471,10);
            	    match(input,WS,FOLLOW_WS_in_function2400); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop88;
                }
            } while (true);
            } finally {dbg.exitSubRule(88);}

            dbg.location(472,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:472:3: ( expr | ( attribute ( COMMA ( WS )* attribute )* ) )
            int alt91=2;
            try { dbg.enterSubRule(91);
            try { dbg.enterDecision(91, decisionCanBacktrack[91]);

            try {
                isCyclicDecision = true;
                alt91 = dfa91.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(91);}

            switch (alt91) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:4: expr
                    {
                    dbg.location(473,4);
                    pushFollow(FOLLOW_expr_in_function2411);
                    expr();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:6: ( attribute ( COMMA ( WS )* attribute )* )
                    {
                    dbg.location(475,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:6: ( attribute ( COMMA ( WS )* attribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:5: attribute ( COMMA ( WS )* attribute )*
                    {
                    dbg.location(476,5);
                    pushFollow(FOLLOW_attribute_in_function2429);
                    attribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(476,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:15: ( COMMA ( WS )* attribute )*
                    try { dbg.enterSubRule(90);

                    loop90:
                    do {
                        int alt90=2;
                        try { dbg.enterDecision(90, decisionCanBacktrack[90]);

                        int LA90_0 = input.LA(1);

                        if ( (LA90_0==COMMA) ) {
                            alt90=1;
                        }


                        } finally {dbg.exitDecision(90);}

                        switch (alt90) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:16: COMMA ( WS )* attribute
                    	    {
                    	    dbg.location(476,16);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function2432); if (state.failed) return ;
                    	    dbg.location(476,22);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:22: ( WS )*
                    	    try { dbg.enterSubRule(89);

                    	    loop89:
                    	    do {
                    	        int alt89=2;
                    	        try { dbg.enterDecision(89, decisionCanBacktrack[89]);

                    	        int LA89_0 = input.LA(1);

                    	        if ( (LA89_0==WS) ) {
                    	            alt89=1;
                    	        }


                    	        } finally {dbg.exitDecision(89);}

                    	        switch (alt89) {
                    	    	case 1 :
                    	    	    dbg.enterAlt(1);

                    	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:22: WS
                    	    	    {
                    	    	    dbg.location(476,22);
                    	    	    match(input,WS,FOLLOW_WS_in_function2434); if (state.failed) return ;

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop89;
                    	        }
                    	    } while (true);
                    	    } finally {dbg.exitSubRule(89);}

                    	    dbg.location(476,26);
                    	    pushFollow(FOLLOW_attribute_in_function2437);
                    	    attribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop90;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(90);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(91);}

            dbg.location(479,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function2458); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 

        }
        finally {
        }
        dbg.location(480, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:1: function_name : ( IDENT COLON )? IDENT ( DOT IDENT )* ;
    public final void function_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(486, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:2: ( ( IDENT COLON )? IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:4: ( IDENT COLON )? IDENT ( DOT IDENT )*
            {
            dbg.location(490,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:4: ( IDENT COLON )?
            int alt92=2;
            try { dbg.enterSubRule(92);
            try { dbg.enterDecision(92, decisionCanBacktrack[92]);

            int LA92_0 = input.LA(1);

            if ( (LA92_0==IDENT) ) {
                int LA92_1 = input.LA(2);

                if ( (LA92_1==COLON) ) {
                    alt92=1;
                }
            }
            } finally {dbg.exitDecision(92);}

            switch (alt92) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:5: IDENT COLON
                    {
                    dbg.location(490,5);
                    match(input,IDENT,FOLLOW_IDENT_in_function_name2506); if (state.failed) return ;
                    dbg.location(490,11);
                    match(input,COLON,FOLLOW_COLON_in_function_name2508); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(92);}

            dbg.location(490,19);
            match(input,IDENT,FOLLOW_IDENT_in_function_name2512); if (state.failed) return ;
            dbg.location(490,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:25: ( DOT IDENT )*
            try { dbg.enterSubRule(93);

            loop93:
            do {
                int alt93=2;
                try { dbg.enterDecision(93, decisionCanBacktrack[93]);

                int LA93_0 = input.LA(1);

                if ( (LA93_0==DOT) ) {
                    alt93=1;
                }


                } finally {dbg.exitDecision(93);}

                switch (alt93) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:26: DOT IDENT
            	    {
            	    dbg.location(490,26);
            	    match(input,DOT,FOLLOW_DOT_in_function_name2515); if (state.failed) return ;
            	    dbg.location(490,30);
            	    match(input,IDENT,FOLLOW_IDENT_in_function_name2517); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop93;
                }
            } while (true);
            } finally {dbg.exitSubRule(93);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(491, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:493:1: attribute : attrname ( WS )* OPEQ ( WS )* attrvalue ;
    public final void attribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(493, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:2: ( attrname ( WS )* OPEQ ( WS )* attrvalue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:4: attrname ( WS )* OPEQ ( WS )* attrvalue
            {
            dbg.location(494,4);
            pushFollow(FOLLOW_attrname_in_attribute2539);
            attrname();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(494,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:13: ( WS )*
            try { dbg.enterSubRule(94);

            loop94:
            do {
                int alt94=2;
                try { dbg.enterDecision(94, decisionCanBacktrack[94]);

                int LA94_0 = input.LA(1);

                if ( (LA94_0==WS) ) {
                    alt94=1;
                }


                } finally {dbg.exitDecision(94);}

                switch (alt94) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:13: WS
            	    {
            	    dbg.location(494,13);
            	    match(input,WS,FOLLOW_WS_in_attribute2541); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop94;
                }
            } while (true);
            } finally {dbg.exitSubRule(94);}

            dbg.location(494,17);
            match(input,OPEQ,FOLLOW_OPEQ_in_attribute2544); if (state.failed) return ;
            dbg.location(494,22);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:22: ( WS )*
            try { dbg.enterSubRule(95);

            loop95:
            do {
                int alt95=2;
                try { dbg.enterDecision(95, decisionCanBacktrack[95]);

                int LA95_0 = input.LA(1);

                if ( (LA95_0==WS) ) {
                    alt95=1;
                }


                } finally {dbg.exitDecision(95);}

                switch (alt95) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:22: WS
            	    {
            	    dbg.location(494,22);
            	    match(input,WS,FOLLOW_WS_in_attribute2546); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop95;
                }
            } while (true);
            } finally {dbg.exitSubRule(95);}

            dbg.location(494,26);
            pushFollow(FOLLOW_attrvalue_in_attribute2549);
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
        dbg.location(495, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:1: attrname : IDENT ;
    public final void attrname() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrname");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(497, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:498:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:498:4: IDENT
            {
            dbg.location(498,4);
            match(input,IDENT,FOLLOW_IDENT_in_attrname2564); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(499, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:501:1: attrvalue : expr ;
    public final void attrvalue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrvalue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(501, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:2: ( expr )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:4: expr
            {
            dbg.location(502,4);
            pushFollow(FOLLOW_expr_in_attrvalue2576);
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
        dbg.location(503, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:505:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(505, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:506:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:506:7: HASH
            {
            dbg.location(506,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor2594); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(507, 5);

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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:285:19: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:285:20: esPred
        {
        dbg.location(285,20);
        pushFollow(FOLLOW_esPred_in_synpred1_Css31084);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:6: esPred
        {
        dbg.location(287,6);
        pushFollow(FOLLOW_esPred_in_synpred2_Css31102);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:304:7: ( nsPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:304:8: nsPred
        {
        dbg.location(304,8);
        pushFollow(FOLLOW_nsPred_in_synpred3_Css31153);
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


    protected DFA59 dfa59 = new DFA59(this);
    protected DFA62 dfa62 = new DFA62(this);
    protected DFA70 dfa70 = new DFA70(this);
    protected DFA80 dfa80 = new DFA80(this);
    protected DFA83 dfa83 = new DFA83(this);
    protected DFA85 dfa85 = new DFA85(this);
    protected DFA91 dfa91 = new DFA91(this);
    static final String DFA59_eotS =
        "\27\uffff";
    static final String DFA59_eofS =
        "\27\uffff";
    static final String DFA59_minS =
        "\1\6\1\uffff\1\0\1\6\1\4\1\6\1\uffff\1\0\4\4\1\0\2\4\1\0\7\4";
    static final String DFA59_maxS =
        "\1\40\1\uffff\1\0\1\23\1\34\1\23\1\uffff\1\0\1\44\1\6\1\34\1\6\1"+
        "\0\1\44\1\7\1\0\1\44\1\34\1\6\1\7\3\44";
    static final String DFA59_acceptS =
        "\1\uffff\1\2\4\uffff\1\1\20\uffff";
    static final String DFA59_specialS =
        "\2\uffff\1\1\4\uffff\1\0\4\uffff\1\2\2\uffff\1\3\7\uffff}>";
    static final String[] DFA59_transitionS = {
            "\1\1\6\uffff\1\1\1\uffff\1\1\3\uffff\1\1\1\uffff\1\5\1\uffff"+
            "\3\1\1\uffff\2\1\1\2\1\3\1\4\1\5",
            "",
            "\1\uffff",
            "\1\7\14\uffff\1\7",
            "\1\13\1\uffff\1\10\24\uffff\1\12\1\11",
            "\1\14\14\uffff\1\14",
            "",
            "\1\uffff",
            "\1\15\27\uffff\1\11\4\uffff\3\16\1\17",
            "\1\13\1\uffff\1\20",
            "\1\21\27\uffff\1\22",
            "\1\13\1\uffff\1\20",
            "\1\uffff",
            "\1\15\27\uffff\1\11\4\uffff\3\16\1\17",
            "\1\23\1\uffff\2\24",
            "\1\uffff",
            "\1\25\34\uffff\3\16\1\17",
            "\1\21\27\uffff\1\22",
            "\1\13\1\uffff\1\20",
            "\1\23\1\uffff\2\24",
            "\1\26\37\uffff\1\17",
            "\1\25\34\uffff\3\16\1\17",
            "\1\26\37\uffff\1\17"
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
            return "()* loopback of 285:18: ( ( esPred )=> elementSubsequent )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA59_7 = input.LA(1);

                         
                        int index59_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index59_7);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA59_2 = input.LA(1);

                         
                        int index59_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index59_2);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA59_12 = input.LA(1);

                         
                        int index59_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index59_12);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA59_15 = input.LA(1);

                         
                        int index59_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index59_15);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 59, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA62_eotS =
        "\7\uffff";
    static final String DFA62_eofS =
        "\7\uffff";
    static final String DFA62_minS =
        "\1\6\1\0\1\uffff\1\4\1\uffff\1\4\1\0";
    static final String DFA62_maxS =
        "\1\34\1\0\1\uffff\1\40\1\uffff\1\40\1\0";
    static final String DFA62_acceptS =
        "\2\uffff\1\1\1\uffff\1\2\2\uffff";
    static final String DFA62_specialS =
        "\1\0\1\2\4\uffff\1\1}>";
    static final String[] DFA62_transitionS = {
            "\1\1\14\uffff\1\4\7\uffff\1\3\1\2",
            "\1\uffff",
            "",
            "\1\5\1\uffff\1\4\6\uffff\1\4\1\uffff\1\4\3\uffff\1\4\1\uffff"+
            "\1\4\1\uffff\3\4\1\uffff\1\4\1\6\4\4",
            "",
            "\1\5\1\uffff\1\4\6\uffff\1\4\1\uffff\1\4\3\uffff\1\4\1\uffff"+
            "\1\4\1\uffff\3\4\1\uffff\1\4\1\6\4\4",
            "\1\uffff"
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
            return "304:6: ( ( nsPred )=> namespace_wqname_prefix )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA62_0 = input.LA(1);

                         
                        int index62_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA62_0==IDENT) ) {s = 1;}

                        else if ( (LA62_0==PIPE) && (synpred3_Css3())) {s = 2;}

                        else if ( (LA62_0==STAR) ) {s = 3;}

                        else if ( (LA62_0==GEN) ) {s = 4;}

                         
                        input.seek(index62_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA62_6 = input.LA(1);

                         
                        int index62_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 2;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index62_6);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA62_1 = input.LA(1);

                         
                        int index62_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 2;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index62_1);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 62, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA70_eotS =
        "\5\uffff";
    static final String DFA70_eofS =
        "\5\uffff";
    static final String DFA70_minS =
        "\2\4\2\uffff\1\4";
    static final String DFA70_maxS =
        "\1\34\1\44\2\uffff\1\44";
    static final String DFA70_acceptS =
        "\2\uffff\1\1\1\2\1\uffff";
    static final String DFA70_specialS =
        "\5\uffff}>";
    static final String[] DFA70_transitionS = {
            "\1\3\1\uffff\1\1\24\uffff\2\2",
            "\1\4\27\uffff\1\2\4\uffff\4\3",
            "",
            "",
            "\1\4\27\uffff\1\2\4\uffff\4\3"
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
            return "358:6: ( namespace_wqname_prefix )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA80_eotS =
        "\4\uffff";
    static final String DFA80_eofS =
        "\4\uffff";
    static final String DFA80_minS =
        "\2\4\2\uffff";
    static final String DFA80_maxS =
        "\2\45\2\uffff";
    static final String DFA80_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA80_specialS =
        "\4\uffff}>";
    static final String[] DFA80_transitionS = {
            "\1\1\1\uffff\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff"+
            "\1\3\1\uffff\3\3\1\uffff\6\3\4\uffff\1\2",
            "\1\1\1\uffff\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff"+
            "\1\3\1\uffff\3\3\1\uffff\6\3\4\uffff\1\2",
            "",
            ""
    };

    static final short[] DFA80_eot = DFA.unpackEncodedString(DFA80_eotS);
    static final short[] DFA80_eof = DFA.unpackEncodedString(DFA80_eofS);
    static final char[] DFA80_min = DFA.unpackEncodedStringToUnsignedChars(DFA80_minS);
    static final char[] DFA80_max = DFA.unpackEncodedStringToUnsignedChars(DFA80_maxS);
    static final short[] DFA80_accept = DFA.unpackEncodedString(DFA80_acceptS);
    static final short[] DFA80_special = DFA.unpackEncodedString(DFA80_specialS);
    static final short[][] DFA80_transition;

    static {
        int numStates = DFA80_transitionS.length;
        DFA80_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA80_transition[i] = DFA.unpackEncodedString(DFA80_transitionS[i]);
        }
    }

    class DFA80 extends DFA {

        public DFA80(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 80;
            this.eot = DFA80_eot;
            this.eof = DFA80_eof;
            this.min = DFA80_min;
            this.max = DFA80_max;
            this.accept = DFA80_accept;
            this.special = DFA80_special;
            this.transition = DFA80_transition;
        }
        public String getDescription() {
            return "402:17: ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA83_eotS =
        "\7\uffff";
    static final String DFA83_eofS =
        "\7\uffff";
    static final String DFA83_minS =
        "\1\6\1\uffff\1\4\1\uffff\3\4";
    static final String DFA83_maxS =
        "\1\60\1\uffff\1\60\1\uffff\3\60";
    static final String DFA83_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\3\uffff";
    static final String DFA83_specialS =
        "\7\uffff}>";
    static final String[] DFA83_transitionS = {
            "\3\3\1\uffff\1\1\3\uffff\1\1\1\2\3\uffff\1\3\2\uffff\2\3\2\uffff"+
            "\1\3\2\uffff\1\3\10\uffff\2\1\11\3",
            "",
            "\1\4\1\uffff\1\5\2\3\12\uffff\1\3\3\uffff\1\3\2\uffff\1\3\2"+
            "\uffff\1\3\12\uffff\11\3",
            "",
            "\1\4\1\uffff\1\5\2\3\12\uffff\1\3\3\uffff\1\3\2\uffff\1\3\2"+
            "\uffff\1\3\12\uffff\11\3",
            "\1\6\1\uffff\3\3\1\uffff\1\3\3\uffff\2\3\3\uffff\1\3\1\uffff"+
            "\3\3\2\uffff\1\3\2\uffff\2\3\2\uffff\1\1\3\uffff\14\3",
            "\1\6\1\uffff\3\3\1\uffff\1\3\3\uffff\2\3\3\uffff\1\3\2\uffff"+
            "\2\3\2\uffff\1\3\2\uffff\1\3\3\uffff\1\1\3\uffff\14\3"
    };

    static final short[] DFA83_eot = DFA.unpackEncodedString(DFA83_eotS);
    static final short[] DFA83_eof = DFA.unpackEncodedString(DFA83_eofS);
    static final char[] DFA83_min = DFA.unpackEncodedStringToUnsignedChars(DFA83_minS);
    static final char[] DFA83_max = DFA.unpackEncodedStringToUnsignedChars(DFA83_maxS);
    static final short[] DFA83_accept = DFA.unpackEncodedString(DFA83_acceptS);
    static final short[] DFA83_special = DFA.unpackEncodedString(DFA83_specialS);
    static final short[][] DFA83_transition;

    static {
        int numStates = DFA83_transitionS.length;
        DFA83_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA83_transition[i] = DFA.unpackEncodedString(DFA83_transitionS[i]);
        }
    }

    class DFA83 extends DFA {

        public DFA83(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 83;
            this.eot = DFA83_eot;
            this.eof = DFA83_eof;
            this.min = DFA83_min;
            this.max = DFA83_max;
            this.accept = DFA83_accept;
            this.special = DFA83_special;
            this.transition = DFA83_transition;
        }
        public String getDescription() {
            return "()* loopback of 442:12: ( operator term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA85_eotS =
        "\12\uffff";
    static final String DFA85_eofS =
        "\12\uffff";
    static final String DFA85_minS =
        "\1\6\2\uffff\1\4\4\uffff\1\4\1\uffff";
    static final String DFA85_maxS =
        "\1\60\2\uffff\1\60\4\uffff\1\60\1\uffff";
    static final String DFA85_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\7\1\uffff\1\3";
    static final String DFA85_specialS =
        "\12\uffff}>";
    static final String[] DFA85_transitionS = {
            "\1\3\1\2\1\5\12\uffff\1\4\11\uffff\1\6\12\uffff\11\1",
            "",
            "",
            "\1\10\1\uffff\3\11\1\uffff\1\11\3\uffff\2\11\3\uffff\1\11\1"+
            "\uffff\1\7\2\11\2\uffff\1\11\2\uffff\1\11\1\7\6\uffff\1\7\13"+
            "\11",
            "",
            "",
            "",
            "",
            "\1\10\1\uffff\3\11\1\uffff\1\11\3\uffff\2\11\3\uffff\1\11\2"+
            "\uffff\2\11\2\uffff\1\11\2\uffff\1\11\7\uffff\1\7\13\11",
            ""
    };

    static final short[] DFA85_eot = DFA.unpackEncodedString(DFA85_eotS);
    static final short[] DFA85_eof = DFA.unpackEncodedString(DFA85_eofS);
    static final char[] DFA85_min = DFA.unpackEncodedStringToUnsignedChars(DFA85_minS);
    static final char[] DFA85_max = DFA.unpackEncodedStringToUnsignedChars(DFA85_maxS);
    static final short[] DFA85_accept = DFA.unpackEncodedString(DFA85_acceptS);
    static final short[] DFA85_special = DFA.unpackEncodedString(DFA85_specialS);
    static final short[][] DFA85_transition;

    static {
        int numStates = DFA85_transitionS.length;
        DFA85_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA85_transition[i] = DFA.unpackEncodedString(DFA85_transitionS[i]);
        }
    }

    class DFA85 extends DFA {

        public DFA85(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 85;
            this.eot = DFA85_eot;
            this.eof = DFA85_eof;
            this.min = DFA85_min;
            this.max = DFA85_max;
            this.accept = DFA85_accept;
            this.special = DFA85_special;
            this.transition = DFA85_transition;
        }
        public String getDescription() {
            return "447:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION ) | STRING | IDENT | GEN | URI | hexColor | function )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA91_eotS =
        "\5\uffff";
    static final String DFA91_eofS =
        "\5\uffff";
    static final String DFA91_minS =
        "\1\6\1\uffff\2\4\1\uffff";
    static final String DFA91_maxS =
        "\1\60\1\uffff\2\60\1\uffff";
    static final String DFA91_acceptS =
        "\1\uffff\1\1\2\uffff\1\2";
    static final String DFA91_specialS =
        "\5\uffff}>";
    static final String[] DFA91_transitionS = {
            "\1\2\2\1\12\uffff\1\1\3\uffff\1\1\2\uffff\1\1\2\uffff\1\1\12"+
            "\uffff\11\1",
            "",
            "\1\3\1\uffff\3\1\6\uffff\1\1\3\uffff\1\1\1\uffff\3\1\2\uffff"+
            "\1\1\2\uffff\2\1\2\uffff\1\4\3\uffff\2\1\1\uffff\11\1",
            "\1\3\1\uffff\3\1\6\uffff\1\1\3\uffff\1\1\2\uffff\2\1\2\uffff"+
            "\1\1\2\uffff\1\1\3\uffff\1\4\3\uffff\2\1\1\uffff\11\1",
            ""
    };

    static final short[] DFA91_eot = DFA.unpackEncodedString(DFA91_eotS);
    static final short[] DFA91_eof = DFA.unpackEncodedString(DFA91_eofS);
    static final char[] DFA91_min = DFA.unpackEncodedStringToUnsignedChars(DFA91_minS);
    static final char[] DFA91_max = DFA.unpackEncodedStringToUnsignedChars(DFA91_maxS);
    static final short[] DFA91_accept = DFA.unpackEncodedString(DFA91_acceptS);
    static final short[] DFA91_special = DFA.unpackEncodedString(DFA91_specialS);
    static final short[][] DFA91_transition;

    static {
        int numStates = DFA91_transitionS.length;
        DFA91_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA91_transition[i] = DFA.unpackEncodedString(DFA91_transitionS[i]);
        }
    }

    class DFA91 extends DFA {

        public DFA91(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 91;
            this.eot = DFA91_eot;
            this.eof = DFA91_eof;
            this.min = DFA91_min;
            this.max = DFA91_max;
            this.accept = DFA91_accept;
            this.special = DFA91_special;
            this.transition = DFA91_transition;
        }
        public String getDescription() {
            return "472:3: ( expr | ( attribute ( COMMA ( WS )* attribute )* ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_charSet_in_styleSheet79 = new BitSet(new long[]{0x00000001F8381870L});
    public static final BitSet FOLLOW_WS_in_styleSheet87 = new BitSet(new long[]{0x00000001F8381870L});
    public static final BitSet FOLLOW_imports_in_styleSheet99 = new BitSet(new long[]{0x00000001F8381870L});
    public static final BitSet FOLLOW_WS_in_styleSheet101 = new BitSet(new long[]{0x00000001F8381870L});
    public static final BitSet FOLLOW_namespace_in_styleSheet116 = new BitSet(new long[]{0x00000001F8381060L});
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
    public static final BitSet FOLLOW_resourceIdentifier_in_imports262 = new BitSet(new long[]{0x00000020000B0450L});
    public static final BitSet FOLLOW_WS_in_imports265 = new BitSet(new long[]{0x00000020000B0450L});
    public static final BitSet FOLLOW_media_query_list_in_imports268 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_SEMI_in_imports270 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEDIA_SYM_in_media291 = new BitSet(new long[]{0x00000020000B2050L});
    public static final BitSet FOLLOW_WS_in_media293 = new BitSet(new long[]{0x00000020000B2050L});
    public static final BitSet FOLLOW_media_query_list_in_media296 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_media306 = new BitSet(new long[]{0x00000001F8280050L});
    public static final BitSet FOLLOW_WS_in_media308 = new BitSet(new long[]{0x00000001F8280050L});
    public static final BitSet FOLLOW_ruleSet_in_media323 = new BitSet(new long[]{0x0000000000004010L});
    public static final BitSet FOLLOW_WS_in_media333 = new BitSet(new long[]{0x0000000000004010L});
    public static final BitSet FOLLOW_RBRACE_in_media336 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_media_query_in_media_query_list356 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_COMMA_in_media_query_list360 = new BitSet(new long[]{0x00000020000B0050L});
    public static final BitSet FOLLOW_WS_in_media_query_list362 = new BitSet(new long[]{0x00000020000B0050L});
    public static final BitSet FOLLOW_media_query_in_media_query_list365 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_set_in_media_query384 = new BitSet(new long[]{0x00000000000B0050L});
    public static final BitSet FOLLOW_WS_in_media_query392 = new BitSet(new long[]{0x00000000000B0050L});
    public static final BitSet FOLLOW_media_type_in_media_query399 = new BitSet(new long[]{0x0000000000040012L});
    public static final BitSet FOLLOW_WS_in_media_query401 = new BitSet(new long[]{0x0000000000040012L});
    public static final BitSet FOLLOW_AND_in_media_query406 = new BitSet(new long[]{0x00000020000B0050L});
    public static final BitSet FOLLOW_WS_in_media_query408 = new BitSet(new long[]{0x00000020000B0050L});
    public static final BitSet FOLLOW_media_expression_in_media_query411 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_media_expression_in_media_query419 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_AND_in_media_query423 = new BitSet(new long[]{0x00000020000B0050L});
    public static final BitSet FOLLOW_WS_in_media_query425 = new BitSet(new long[]{0x00000020000B0050L});
    public static final BitSet FOLLOW_media_expression_in_media_query428 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_set_in_media_type0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_media_expression459 = new BitSet(new long[]{0x0000000000000050L});
    public static final BitSet FOLLOW_WS_in_media_expression461 = new BitSet(new long[]{0x0000000000000050L});
    public static final BitSet FOLLOW_media_feature_in_media_expression464 = new BitSet(new long[]{0x0000004000200010L});
    public static final BitSet FOLLOW_WS_in_media_expression466 = new BitSet(new long[]{0x0000004000200010L});
    public static final BitSet FOLLOW_COLON_in_media_expression471 = new BitSet(new long[]{0x0001FF00248801D0L});
    public static final BitSet FOLLOW_WS_in_media_expression473 = new BitSet(new long[]{0x0001FF00248801D0L});
    public static final BitSet FOLLOW_expr_in_media_expression476 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_RPAREN_in_media_expression481 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_media_expression483 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_IDENT_in_media_feature494 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_medium511 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_medium521 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_bodyset_in_bodylist544 = new BitSet(new long[]{0x00000001F8381042L});
    public static final BitSet FOLLOW_ruleSet_in_bodyset573 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_media_in_bodyset585 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_page_in_bodyset597 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_bodyset613 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page635 = new BitSet(new long[]{0x0000000000202010L});
    public static final BitSet FOLLOW_WS_in_page637 = new BitSet(new long[]{0x0000000000202000L});
    public static final BitSet FOLLOW_pseudoPage_in_page641 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_WS_in_page643 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_LBRACE_in_page656 = new BitSet(new long[]{0x0000000000080050L});
    public static final BitSet FOLLOW_WS_in_page658 = new BitSet(new long[]{0x0000000000080050L});
    public static final BitSet FOLLOW_declaration_in_page673 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_SEMI_in_page675 = new BitSet(new long[]{0x0000000000084050L});
    public static final BitSet FOLLOW_declaration_in_page678 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_SEMI_in_page680 = new BitSet(new long[]{0x0000000000084050L});
    public static final BitSet FOLLOW_RBRACE_in_page692 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage713 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage715 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_operator736 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_operator738 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_COMMA_in_operator747 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_operator749 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_PLUS_in_combinator777 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator779 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_GREATER_in_combinator788 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator790 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_TILDE_in_combinator799 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator801 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_property861 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_property869 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_selectorsGroup_in_ruleSet894 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_ruleSet904 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_WS_in_ruleSet906 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_ruleSet909 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_declarations_in_ruleSet923 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_ruleSet933 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_declarations971 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_SEMI_in_declarations975 = new BitSet(new long[]{0x0000000000080452L});
    public static final BitSet FOLLOW_WS_in_declarations977 = new BitSet(new long[]{0x0000000000080452L});
    public static final BitSet FOLLOW_declaration_in_declarations980 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup1004 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup1007 = new BitSet(new long[]{0x00000001F8280050L});
    public static final BitSet FOLLOW_WS_in_selectorsGroup1009 = new BitSet(new long[]{0x00000001F8280050L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup1012 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector1035 = new BitSet(new long[]{0x00000001FBA80042L});
    public static final BitSet FOLLOW_combinator_in_selector1038 = new BitSet(new long[]{0x00000001F8280040L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector1040 = new BitSet(new long[]{0x00000001FBA80042L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence1080 = new BitSet(new long[]{0x00000001F8280042L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence1087 = new BitSet(new long[]{0x00000001F8280042L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence1105 = new BitSet(new long[]{0x00000001F8280042L});
    public static final BitSet FOLLOW_namespace_wqname_prefix_in_typeSelector1156 = new BitSet(new long[]{0x0000000018080040L});
    public static final BitSet FOLLOW_elementName_in_typeSelector1162 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_typeSelector1164 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_set_in_nsPred1189 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_PIPE_in_nsPred1197 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespace_prefix_in_namespace_wqname_prefix1227 = new BitSet(new long[]{0x0000000010000010L});
    public static final BitSet FOLLOW_WS_in_namespace_wqname_prefix1229 = new BitSet(new long[]{0x0000000010000010L});
    public static final BitSet FOLLOW_PIPE_in_namespace_wqname_prefix1235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespace_wildcard_prefix_in_namespace_wqname_prefix1242 = new BitSet(new long[]{0x0000000010000010L});
    public static final BitSet FOLLOW_WS_in_namespace_wqname_prefix1244 = new BitSet(new long[]{0x0000000010000010L});
    public static final BitSet FOLLOW_PIPE_in_namespace_wqname_prefix1247 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_namespace_wildcard_prefix1269 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent1342 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent1351 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_attrib_in_elementSubsequent1363 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent1375 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_elementSubsequent1387 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_HASH_in_cssId1409 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass1426 = new BitSet(new long[]{0x0000000000080040L});
    public static final BitSet FOLLOW_set_in_cssClass1428 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_attrib1492 = new BitSet(new long[]{0x0000000018000050L});
    public static final BitSet FOLLOW_namespace_wqname_prefix_in_attrib1499 = new BitSet(new long[]{0x0000000018000050L});
    public static final BitSet FOLLOW_WS_in_attrib1502 = new BitSet(new long[]{0x0000000018000050L});
    public static final BitSet FOLLOW_attrib_name_in_attrib1513 = new BitSet(new long[]{0x0000001E00000010L});
    public static final BitSet FOLLOW_WS_in_attrib1515 = new BitSet(new long[]{0x0000001E00000010L});
    public static final BitSet FOLLOW_set_in_attrib1557 = new BitSet(new long[]{0x00000000000000D0L});
    public static final BitSet FOLLOW_WS_in_attrib1665 = new BitSet(new long[]{0x00000000000000D0L});
    public static final BitSet FOLLOW_attrib_value_in_attrib1684 = new BitSet(new long[]{0x0000001000000010L});
    public static final BitSet FOLLOW_WS_in_attrib1702 = new BitSet(new long[]{0x0000001000000010L});
    public static final BitSet FOLLOW_RBRACKET_in_attrib1731 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_attrib_name1774 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_attrib_value1788 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pseudo1848 = new BitSet(new long[]{0x0000000000080040L});
    public static final BitSet FOLLOW_set_in_pseudo1870 = new BitSet(new long[]{0x0000002000000012L});
    public static final BitSet FOLLOW_WS_in_pseudo1919 = new BitSet(new long[]{0x0000002000000010L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo1922 = new BitSet(new long[]{0x0000004000080050L});
    public static final BitSet FOLLOW_WS_in_pseudo1924 = new BitSet(new long[]{0x0000004000080050L});
    public static final BitSet FOLLOW_set_in_pseudo1928 = new BitSet(new long[]{0x0000004000000010L});
    public static final BitSet FOLLOW_WS_in_pseudo1938 = new BitSet(new long[]{0x0000004000000010L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo1943 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_declaration1989 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_COLON_in_declaration1991 = new BitSet(new long[]{0x0001FF00248801D0L});
    public static final BitSet FOLLOW_WS_in_declaration1993 = new BitSet(new long[]{0x0001FF00248801D0L});
    public static final BitSet FOLLOW_expr_in_declaration1996 = new BitSet(new long[]{0x0000008000000002L});
    public static final BitSet FOLLOW_prio_in_declaration1998 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio2091 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expr2112 = new BitSet(new long[]{0x0001FF0024C881D2L});
    public static final BitSet FOLLOW_operator_in_expr2115 = new BitSet(new long[]{0x0001FF00248801D0L});
    public static final BitSet FOLLOW_term_in_expr2117 = new BitSet(new long[]{0x0001FF0024C881D2L});
    public static final BitSet FOLLOW_unaryOperator_in_term2140 = new BitSet(new long[]{0x0001FF00248801D0L});
    public static final BitSet FOLLOW_set_in_term2161 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_STRING_in_term2323 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_IDENT_in_term2331 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_GEN_in_term2339 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_URI_in_term2347 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_hexColor_in_term2355 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_function_in_term2363 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_term2375 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_function_name_in_function2391 = new BitSet(new long[]{0x0000002000000010L});
    public static final BitSet FOLLOW_WS_in_function2393 = new BitSet(new long[]{0x0000002000000010L});
    public static final BitSet FOLLOW_LPAREN_in_function2398 = new BitSet(new long[]{0x0001FF00248801D0L});
    public static final BitSet FOLLOW_WS_in_function2400 = new BitSet(new long[]{0x0001FF00248801D0L});
    public static final BitSet FOLLOW_expr_in_function2411 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_attribute_in_function2429 = new BitSet(new long[]{0x0000004000008000L});
    public static final BitSet FOLLOW_COMMA_in_function2432 = new BitSet(new long[]{0x0001FF00248801D0L});
    public static final BitSet FOLLOW_WS_in_function2434 = new BitSet(new long[]{0x0001FF00248801D0L});
    public static final BitSet FOLLOW_attribute_in_function2437 = new BitSet(new long[]{0x0000004000008000L});
    public static final BitSet FOLLOW_RPAREN_in_function2458 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_function_name2506 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_COLON_in_function_name2508 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_function_name2512 = new BitSet(new long[]{0x0000000040000002L});
    public static final BitSet FOLLOW_DOT_in_function_name2515 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_function_name2517 = new BitSet(new long[]{0x0000000040000002L});
    public static final BitSet FOLLOW_attrname_in_attribute2539 = new BitSet(new long[]{0x0000000200000010L});
    public static final BitSet FOLLOW_WS_in_attribute2541 = new BitSet(new long[]{0x0000000200000010L});
    public static final BitSet FOLLOW_OPEQ_in_attribute2544 = new BitSet(new long[]{0x0001FF00248801D0L});
    public static final BitSet FOLLOW_WS_in_attribute2546 = new BitSet(new long[]{0x0001FF00248801D0L});
    public static final BitSet FOLLOW_attrvalue_in_attribute2549 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_attrname2564 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_in_attrvalue2576 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor2594 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred1_Css31084 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred2_Css31102 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nsPred_in_synpred3_Css31153 = new BitSet(new long[]{0x0000000000000002L});

}