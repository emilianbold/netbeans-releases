// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2011-09-27 15:04:27

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "WS", "NAMESPACE_SYM", "IDENT", "STRING", "URI", "CHARSET_SYM", "SEMI", "IMPORT_SYM", "MEDIA_SYM", "LBRACE", "RBRACE", "COMMA", "ONLY", "NOT", "AND", "GEN", "PAGE_SYM", "COUNTER_STYLE_SYM", "FONT_FACE_SYM", "TOPLEFTCORNER_SYM", "TOPLEFT_SYM", "TOPCENTER_SYM", "TOPRIGHT_SYM", "TOPRIGHTCORNER_SYM", "BOTTOMLEFTCORNER_SYM", "BOTTOMLEFT_SYM", "BOTTOMCENTER_SYM", "BOTTOMRIGHT_SYM", "BOTTOMRIGHTCORNER_SYM", "LEFTTOP_SYM", "LEFTMIDDLE_SYM", "LEFTBOTTOM_SYM", "RIGHTTOP_SYM", "RIGHTMIDDLE_SYM", "RIGHTBOTTOM_SYM", "COLON", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "STAR", "PIPE", "HASH", "DOT", "LBRACKET", "DCOLON", "OPEQ", "INCLUDES", "DASHMATCH", "BEGINS", "ENDS", "CONTAINS", "RBRACKET", "LPAREN", "RPAREN", "IMPORTANT_SYM", "NUMBER", "PERCENTAGE", "LENGTH", "EMS", "EXS", "ANGLE", "TIME", "FREQ", "RESOLUTION", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "NAME", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "COMMENT", "CDO", "CDC", "INVALID", "DIMENSION", "NL"
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
    public static final int COUNTER_STYLE_SYM=21;
    public static final int FONT_FACE_SYM=22;
    public static final int TOPLEFTCORNER_SYM=23;
    public static final int TOPLEFT_SYM=24;
    public static final int TOPCENTER_SYM=25;
    public static final int TOPRIGHT_SYM=26;
    public static final int TOPRIGHTCORNER_SYM=27;
    public static final int BOTTOMLEFTCORNER_SYM=28;
    public static final int BOTTOMLEFT_SYM=29;
    public static final int BOTTOMCENTER_SYM=30;
    public static final int BOTTOMRIGHT_SYM=31;
    public static final int BOTTOMRIGHTCORNER_SYM=32;
    public static final int LEFTTOP_SYM=33;
    public static final int LEFTMIDDLE_SYM=34;
    public static final int LEFTBOTTOM_SYM=35;
    public static final int RIGHTTOP_SYM=36;
    public static final int RIGHTMIDDLE_SYM=37;
    public static final int RIGHTBOTTOM_SYM=38;
    public static final int COLON=39;
    public static final int SOLIDUS=40;
    public static final int PLUS=41;
    public static final int GREATER=42;
    public static final int TILDE=43;
    public static final int MINUS=44;
    public static final int STAR=45;
    public static final int PIPE=46;
    public static final int HASH=47;
    public static final int DOT=48;
    public static final int LBRACKET=49;
    public static final int DCOLON=50;
    public static final int OPEQ=51;
    public static final int INCLUDES=52;
    public static final int DASHMATCH=53;
    public static final int BEGINS=54;
    public static final int ENDS=55;
    public static final int CONTAINS=56;
    public static final int RBRACKET=57;
    public static final int LPAREN=58;
    public static final int RPAREN=59;
    public static final int IMPORTANT_SYM=60;
    public static final int NUMBER=61;
    public static final int PERCENTAGE=62;
    public static final int LENGTH=63;
    public static final int EMS=64;
    public static final int EXS=65;
    public static final int ANGLE=66;
    public static final int TIME=67;
    public static final int FREQ=68;
    public static final int RESOLUTION=69;
    public static final int HEXCHAR=70;
    public static final int NONASCII=71;
    public static final int UNICODE=72;
    public static final int ESCAPE=73;
    public static final int NMSTART=74;
    public static final int NMCHAR=75;
    public static final int NAME=76;
    public static final int URL=77;
    public static final int A=78;
    public static final int B=79;
    public static final int C=80;
    public static final int D=81;
    public static final int E=82;
    public static final int F=83;
    public static final int G=84;
    public static final int H=85;
    public static final int I=86;
    public static final int J=87;
    public static final int K=88;
    public static final int L=89;
    public static final int M=90;
    public static final int N=91;
    public static final int O=92;
    public static final int P=93;
    public static final int Q=94;
    public static final int R=95;
    public static final int S=96;
    public static final int T=97;
    public static final int U=98;
    public static final int V=99;
    public static final int W=100;
    public static final int X=101;
    public static final int Y=102;
    public static final int Z=103;
    public static final int COMMENT=104;
    public static final int CDO=105;
    public static final int CDC=106;
    public static final int INVALID=107;
    public static final int DIMENSION=108;
    public static final int NL=109;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "function", "attrname", "nsPred", "unaryOperator", 
        "combinator", "typeSelector", "cssClass", "media_feature", "hexColor", 
        "media_expression", "namespace_prefix", "resourceIdentifier", "margin_sym", 
        "margin", "page", "attribute", "prio", "function_name", "bodyset", 
        "selector", "property", "synpred2_Css3", "fontFace", "imports", 
        "syncTo_IDENT_RBRACKET_LBRACE", "term", "elementSubsequent", "pseudoPage", 
        "declarations", "attrib", "charSet", "cssId", "attrib_value", "attrvalue", 
        "ruleSet", "attrib_name", "counterStyle", "esPred", "selectorsGroup", 
        "syncTo_IDENT_RBRACE", "expr", "medium", "pseudo", "syncToFollow", 
        "synpred3_Css3", "media_query", "media_type", "operator", "namespace", 
        "media", "namespace_wqname_prefix", "bodylist", "namespace_wildcard_prefix", 
        "styleSheet", "simpleSelectorSequence", "media_query_list", "declaration", 
        "synpred1_Css3", "elementName"
    };
    public static final boolean[] decisionCanBacktrack = new boolean[] {
        false, // invalid decision
        false, false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            true, true, false, true, false, false, false, false, false, 
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:173:1: styleSheet : ( charSet )? ( WS )* ( imports ( WS )* )* ( namespace )* bodylist EOF ;
    public final void styleSheet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "styleSheet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(173, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:174:5: ( ( charSet )? ( WS )* ( imports ( WS )* )* ( namespace )* bodylist EOF )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:174:9: ( charSet )? ( WS )* ( imports ( WS )* )* ( namespace )* bodylist EOF
            {
            dbg.location(174,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:174:9: ( charSet )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:174:9: charSet
                    {
                    dbg.location(174,9);
                    pushFollow(FOLLOW_charSet_in_styleSheet119);
                    charSet();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(1);}

            dbg.location(175,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:175:6: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:175:6: WS
            	    {
            	    dbg.location(175,6);
            	    match(input,WS,FOLLOW_WS_in_styleSheet127); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);
            } finally {dbg.exitSubRule(2);}

            dbg.location(176,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:176:9: ( imports ( WS )* )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:176:10: imports ( WS )*
            	    {
            	    dbg.location(176,10);
            	    pushFollow(FOLLOW_imports_in_styleSheet139);
            	    imports();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(176,18);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:176:18: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:176:18: WS
            	    	    {
            	    	    dbg.location(176,18);
            	    	    match(input,WS,FOLLOW_WS_in_styleSheet141); if (state.failed) return ;

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

            dbg.location(177,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:177:9: ( namespace )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:177:9: namespace
            	    {
            	    dbg.location(177,9);
            	    pushFollow(FOLLOW_namespace_in_styleSheet156);
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

            dbg.location(178,9);
            pushFollow(FOLLOW_bodylist_in_styleSheet167);
            bodylist();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(179,6);
            match(input,EOF,FOLLOW_EOF_in_styleSheet174); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "styleSheet");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "styleSheet"


    // $ANTLR start "namespace"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:182:1: namespace : NAMESPACE_SYM ( WS )* ( namespace_prefix ( WS )* )? ( resourceIdentifier ) ( WS )* ';' ( WS )* ;
    public final void namespace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(182, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:3: ( NAMESPACE_SYM ( WS )* ( namespace_prefix ( WS )* )? ( resourceIdentifier ) ( WS )* ';' ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:5: NAMESPACE_SYM ( WS )* ( namespace_prefix ( WS )* )? ( resourceIdentifier ) ( WS )* ';' ( WS )*
            {
            dbg.location(183,5);
            match(input,NAMESPACE_SYM,FOLLOW_NAMESPACE_SYM_in_namespace189); if (state.failed) return ;
            dbg.location(183,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:19: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:19: WS
            	    {
            	    dbg.location(183,19);
            	    match(input,WS,FOLLOW_WS_in_namespace191); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);
            } finally {dbg.exitSubRule(6);}

            dbg.location(183,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:23: ( namespace_prefix ( WS )* )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:24: namespace_prefix ( WS )*
                    {
                    dbg.location(183,24);
                    pushFollow(FOLLOW_namespace_prefix_in_namespace195);
                    namespace_prefix();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(183,41);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:41: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:41: WS
                    	    {
                    	    dbg.location(183,41);
                    	    match(input,WS,FOLLOW_WS_in_namespace197); if (state.failed) return ;

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

            dbg.location(183,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:47: ( resourceIdentifier )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:48: resourceIdentifier
            {
            dbg.location(183,48);
            pushFollow(FOLLOW_resourceIdentifier_in_namespace203);
            resourceIdentifier();

            state._fsp--;
            if (state.failed) return ;

            }

            dbg.location(183,68);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:68: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:68: WS
            	    {
            	    dbg.location(183,68);
            	    match(input,WS,FOLLOW_WS_in_namespace206); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);
            } finally {dbg.exitSubRule(9);}

            dbg.location(183,72);
            match(input,SEMI,FOLLOW_SEMI_in_namespace209); if (state.failed) return ;
            dbg.location(183,76);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:76: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:183:76: WS
            	    {
            	    dbg.location(183,76);
            	    match(input,WS,FOLLOW_WS_in_namespace211); if (state.failed) return ;

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
        dbg.location(184, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:186:1: namespace_prefix : IDENT ;
    public final void namespace_prefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace_prefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(186, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:187:3: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:187:5: IDENT
            {
            dbg.location(187,5);
            match(input,IDENT,FOLLOW_IDENT_in_namespace_prefix225); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(188, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:190:1: resourceIdentifier : ( STRING | URI );
    public final void resourceIdentifier() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "resourceIdentifier");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(190, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:191:3: ( STRING | URI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(191,3);
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
        dbg.location(192, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:197:1: charSet : CHARSET_SYM ( WS )* STRING ( WS )* SEMI ;
    public final void charSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "charSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(197, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:198:5: ( CHARSET_SYM ( WS )* STRING ( WS )* SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:198:9: CHARSET_SYM ( WS )* STRING ( WS )* SEMI
            {
            dbg.location(198,9);
            match(input,CHARSET_SYM,FOLLOW_CHARSET_SYM_in_charSet264); if (state.failed) return ;
            dbg.location(198,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:198:21: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:198:21: WS
            	    {
            	    dbg.location(198,21);
            	    match(input,WS,FOLLOW_WS_in_charSet266); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);
            } finally {dbg.exitSubRule(11);}

            dbg.location(198,25);
            match(input,STRING,FOLLOW_STRING_in_charSet269); if (state.failed) return ;
            dbg.location(198,32);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:198:32: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:198:32: WS
            	    {
            	    dbg.location(198,32);
            	    match(input,WS,FOLLOW_WS_in_charSet271); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop12;
                }
            } while (true);
            } finally {dbg.exitSubRule(12);}

            dbg.location(198,36);
            match(input,SEMI,FOLLOW_SEMI_in_charSet274); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(199, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:204:1: imports : IMPORT_SYM ( WS )* ( resourceIdentifier ) ( WS )* media_query_list SEMI ;
    public final void imports() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "imports");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(204, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:205:5: ( IMPORT_SYM ( WS )* ( resourceIdentifier ) ( WS )* media_query_list SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:205:9: IMPORT_SYM ( WS )* ( resourceIdentifier ) ( WS )* media_query_list SEMI
            {
            dbg.location(205,9);
            match(input,IMPORT_SYM,FOLLOW_IMPORT_SYM_in_imports296); if (state.failed) return ;
            dbg.location(205,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:205:20: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:205:20: WS
            	    {
            	    dbg.location(205,20);
            	    match(input,WS,FOLLOW_WS_in_imports298); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);
            } finally {dbg.exitSubRule(13);}

            dbg.location(205,24);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:205:24: ( resourceIdentifier )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:205:25: resourceIdentifier
            {
            dbg.location(205,25);
            pushFollow(FOLLOW_resourceIdentifier_in_imports302);
            resourceIdentifier();

            state._fsp--;
            if (state.failed) return ;

            }

            dbg.location(205,45);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:205:45: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:205:45: WS
            	    {
            	    dbg.location(205,45);
            	    match(input,WS,FOLLOW_WS_in_imports305); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);
            } finally {dbg.exitSubRule(14);}

            dbg.location(205,49);
            pushFollow(FOLLOW_media_query_list_in_imports308);
            media_query_list();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(205,66);
            match(input,SEMI,FOLLOW_SEMI_in_imports310); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "imports");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "imports"


    // $ANTLR start "media"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:212:1: media : MEDIA_SYM ( WS )* media_query_list LBRACE ( WS )* ( ( ruleSet | page ) ( WS )* )* RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(212, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:213:5: ( MEDIA_SYM ( WS )* media_query_list LBRACE ( WS )* ( ( ruleSet | page ) ( WS )* )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:213:7: MEDIA_SYM ( WS )* media_query_list LBRACE ( WS )* ( ( ruleSet | page ) ( WS )* )* RBRACE
            {
            dbg.location(213,7);
            match(input,MEDIA_SYM,FOLLOW_MEDIA_SYM_in_media331); if (state.failed) return ;
            dbg.location(213,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:213:17: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:213:17: WS
            	    {
            	    dbg.location(213,17);
            	    match(input,WS,FOLLOW_WS_in_media333); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);
            } finally {dbg.exitSubRule(15);}

            dbg.location(213,21);
            pushFollow(FOLLOW_media_query_list_in_media336);
            media_query_list();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(214,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_media346); if (state.failed) return ;
            dbg.location(214,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:214:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:214:16: WS
            	    {
            	    dbg.location(214,16);
            	    match(input,WS,FOLLOW_WS_in_media348); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);
            } finally {dbg.exitSubRule(16);}

            dbg.location(215,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:215:13: ( ( ruleSet | page ) ( WS )* )*
            try { dbg.enterSubRule(19);

            loop19:
            do {
                int alt19=2;
                try { dbg.enterDecision(19, decisionCanBacktrack[19]);

                int LA19_0 = input.LA(1);

                if ( (LA19_0==IDENT||(LA19_0>=GEN && LA19_0<=PAGE_SYM)||LA19_0==COLON||(LA19_0>=STAR && LA19_0<=DCOLON)) ) {
                    alt19=1;
                }


                } finally {dbg.exitDecision(19);}

                switch (alt19) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:215:15: ( ruleSet | page ) ( WS )*
            	    {
            	    dbg.location(215,15);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:215:15: ( ruleSet | page )
            	    int alt17=2;
            	    try { dbg.enterSubRule(17);
            	    try { dbg.enterDecision(17, decisionCanBacktrack[17]);

            	    int LA17_0 = input.LA(1);

            	    if ( (LA17_0==IDENT||LA17_0==GEN||LA17_0==COLON||(LA17_0>=STAR && LA17_0<=DCOLON)) ) {
            	        alt17=1;
            	    }
            	    else if ( (LA17_0==PAGE_SYM) ) {
            	        alt17=2;
            	    }
            	    else {
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:215:17: ruleSet
            	            {
            	            dbg.location(215,17);
            	            pushFollow(FOLLOW_ruleSet_in_media367);
            	            ruleSet();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:215:27: page
            	            {
            	            dbg.location(215,27);
            	            pushFollow(FOLLOW_page_in_media371);
            	            page();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(17);}

            	    dbg.location(215,34);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:215:34: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:215:34: WS
            	    	    {
            	    	    dbg.location(215,34);
            	    	    match(input,WS,FOLLOW_WS_in_media375); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop18;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(18);}


            	    }
            	    break;

            	default :
            	    break loop19;
                }
            } while (true);
            } finally {dbg.exitSubRule(19);}

            dbg.location(216,10);
            match(input,RBRACE,FOLLOW_RBRACE_in_media389); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(217, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:223:1: media_query_list : ( media_query ( COMMA ( WS )* media_query )* )? ;
    public final void media_query_list() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_query_list");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(223, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:224:2: ( ( media_query ( COMMA ( WS )* media_query )* )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:224:4: ( media_query ( COMMA ( WS )* media_query )* )?
            {
            dbg.location(224,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:224:4: ( media_query ( COMMA ( WS )* media_query )* )?
            int alt22=2;
            try { dbg.enterSubRule(22);
            try { dbg.enterDecision(22, decisionCanBacktrack[22]);

            int LA22_0 = input.LA(1);

            if ( (LA22_0==IDENT||(LA22_0>=ONLY && LA22_0<=NOT)||LA22_0==GEN||LA22_0==LPAREN) ) {
                alt22=1;
            }
            } finally {dbg.exitDecision(22);}

            switch (alt22) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:224:6: media_query ( COMMA ( WS )* media_query )*
                    {
                    dbg.location(224,6);
                    pushFollow(FOLLOW_media_query_in_media_query_list409);
                    media_query();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(224,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:224:18: ( COMMA ( WS )* media_query )*
                    try { dbg.enterSubRule(21);

                    loop21:
                    do {
                        int alt21=2;
                        try { dbg.enterDecision(21, decisionCanBacktrack[21]);

                        int LA21_0 = input.LA(1);

                        if ( (LA21_0==COMMA) ) {
                            alt21=1;
                        }


                        } finally {dbg.exitDecision(21);}

                        switch (alt21) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:224:20: COMMA ( WS )* media_query
                    	    {
                    	    dbg.location(224,20);
                    	    match(input,COMMA,FOLLOW_COMMA_in_media_query_list413); if (state.failed) return ;
                    	    dbg.location(224,26);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:224:26: ( WS )*
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

                    	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:224:26: WS
                    	    	    {
                    	    	    dbg.location(224,26);
                    	    	    match(input,WS,FOLLOW_WS_in_media_query_list415); if (state.failed) return ;

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop20;
                    	        }
                    	    } while (true);
                    	    } finally {dbg.exitSubRule(20);}

                    	    dbg.location(224,30);
                    	    pushFollow(FOLLOW_media_query_in_media_query_list418);
                    	    media_query();

                    	    state._fsp--;
                    	    if (state.failed) return ;

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


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(225, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:227:1: media_query : ( ( ( ONLY | NOT ) ( WS )* )? media_type ( WS )* ( AND ( WS )* media_expression )* | media_expression ( AND ( WS )* media_expression )* );
    public final void media_query() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_query");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(227, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:228:2: ( ( ( ONLY | NOT ) ( WS )* )? media_type ( WS )* ( AND ( WS )* media_expression )* | media_expression ( AND ( WS )* media_expression )* )
            int alt30=2;
            try { dbg.enterDecision(30, decisionCanBacktrack[30]);

            int LA30_0 = input.LA(1);

            if ( (LA30_0==IDENT||(LA30_0>=ONLY && LA30_0<=NOT)||LA30_0==GEN) ) {
                alt30=1;
            }
            else if ( (LA30_0==LPAREN) ) {
                alt30=2;
            }
            else {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:228:4: ( ( ONLY | NOT ) ( WS )* )? media_type ( WS )* ( AND ( WS )* media_expression )*
                    {
                    dbg.location(228,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:228:4: ( ( ONLY | NOT ) ( WS )* )?
                    int alt24=2;
                    try { dbg.enterSubRule(24);
                    try { dbg.enterDecision(24, decisionCanBacktrack[24]);

                    int LA24_0 = input.LA(1);

                    if ( ((LA24_0>=ONLY && LA24_0<=NOT)) ) {
                        alt24=1;
                    }
                    } finally {dbg.exitDecision(24);}

                    switch (alt24) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:228:5: ( ONLY | NOT ) ( WS )*
                            {
                            dbg.location(228,5);
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

                            dbg.location(228,18);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:228:18: ( WS )*
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

                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:228:18: WS
                            	    {
                            	    dbg.location(228,18);
                            	    match(input,WS,FOLLOW_WS_in_media_query445); if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop23;
                                }
                            } while (true);
                            } finally {dbg.exitSubRule(23);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(24);}

                    dbg.location(228,26);
                    pushFollow(FOLLOW_media_type_in_media_query452);
                    media_type();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(228,37);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:228:37: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:228:37: WS
                    	    {
                    	    dbg.location(228,37);
                    	    match(input,WS,FOLLOW_WS_in_media_query454); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop25;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(25);}

                    dbg.location(228,41);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:228:41: ( AND ( WS )* media_expression )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:228:43: AND ( WS )* media_expression
                    	    {
                    	    dbg.location(228,43);
                    	    match(input,AND,FOLLOW_AND_in_media_query459); if (state.failed) return ;
                    	    dbg.location(228,47);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:228:47: ( WS )*
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

                    	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:228:47: WS
                    	    	    {
                    	    	    dbg.location(228,47);
                    	    	    match(input,WS,FOLLOW_WS_in_media_query461); if (state.failed) return ;

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop26;
                    	        }
                    	    } while (true);
                    	    } finally {dbg.exitSubRule(26);}

                    	    dbg.location(228,51);
                    	    pushFollow(FOLLOW_media_expression_in_media_query464);
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
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:229:4: media_expression ( AND ( WS )* media_expression )*
                    {
                    dbg.location(229,4);
                    pushFollow(FOLLOW_media_expression_in_media_query472);
                    media_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(229,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:229:21: ( AND ( WS )* media_expression )*
                    try { dbg.enterSubRule(29);

                    loop29:
                    do {
                        int alt29=2;
                        try { dbg.enterDecision(29, decisionCanBacktrack[29]);

                        int LA29_0 = input.LA(1);

                        if ( (LA29_0==AND) ) {
                            alt29=1;
                        }


                        } finally {dbg.exitDecision(29);}

                        switch (alt29) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:229:23: AND ( WS )* media_expression
                    	    {
                    	    dbg.location(229,23);
                    	    match(input,AND,FOLLOW_AND_in_media_query476); if (state.failed) return ;
                    	    dbg.location(229,27);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:229:27: ( WS )*
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

                    	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:229:27: WS
                    	    	    {
                    	    	    dbg.location(229,27);
                    	    	    match(input,WS,FOLLOW_WS_in_media_query478); if (state.failed) return ;

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop28;
                    	        }
                    	    } while (true);
                    	    } finally {dbg.exitSubRule(28);}

                    	    dbg.location(229,31);
                    	    pushFollow(FOLLOW_media_expression_in_media_query481);
                    	    media_expression();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop29;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(29);}


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
        dbg.location(230, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:232:1: media_type : ( IDENT | GEN );
    public final void media_type() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_type");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(232, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:233:2: ( IDENT | GEN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(233,2);
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
        dbg.location(234, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:236:1: media_expression : '(' ( WS )* media_feature ( WS )* ( ':' ( WS )* expr )? ')' ( WS )* ;
    public final void media_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(236, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:237:2: ( '(' ( WS )* media_feature ( WS )* ( ':' ( WS )* expr )? ')' ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:237:4: '(' ( WS )* media_feature ( WS )* ( ':' ( WS )* expr )? ')' ( WS )*
            {
            dbg.location(237,4);
            match(input,LPAREN,FOLLOW_LPAREN_in_media_expression512); if (state.failed) return ;
            dbg.location(237,8);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:237:8: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:237:8: WS
            	    {
            	    dbg.location(237,8);
            	    match(input,WS,FOLLOW_WS_in_media_expression514); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop31;
                }
            } while (true);
            } finally {dbg.exitSubRule(31);}

            dbg.location(237,12);
            pushFollow(FOLLOW_media_feature_in_media_expression517);
            media_feature();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(237,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:237:26: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:237:26: WS
            	    {
            	    dbg.location(237,26);
            	    match(input,WS,FOLLOW_WS_in_media_expression519); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop32;
                }
            } while (true);
            } finally {dbg.exitSubRule(32);}

            dbg.location(237,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:237:30: ( ':' ( WS )* expr )?
            int alt34=2;
            try { dbg.enterSubRule(34);
            try { dbg.enterDecision(34, decisionCanBacktrack[34]);

            int LA34_0 = input.LA(1);

            if ( (LA34_0==COLON) ) {
                alt34=1;
            }
            } finally {dbg.exitDecision(34);}

            switch (alt34) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:237:32: ':' ( WS )* expr
                    {
                    dbg.location(237,32);
                    match(input,COLON,FOLLOW_COLON_in_media_expression524); if (state.failed) return ;
                    dbg.location(237,36);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:237:36: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:237:36: WS
                    	    {
                    	    dbg.location(237,36);
                    	    match(input,WS,FOLLOW_WS_in_media_expression526); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop33;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(33);}

                    dbg.location(237,40);
                    pushFollow(FOLLOW_expr_in_media_expression529);
                    expr();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(34);}

            dbg.location(237,48);
            match(input,RPAREN,FOLLOW_RPAREN_in_media_expression534); if (state.failed) return ;
            dbg.location(237,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:237:52: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:237:52: WS
            	    {
            	    dbg.location(237,52);
            	    match(input,WS,FOLLOW_WS_in_media_expression536); if (state.failed) return ;

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
        dbg.location(238, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:239:1: media_feature : IDENT ;
    public final void media_feature() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_feature");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(239, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:240:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:240:4: IDENT
            {
            dbg.location(240,4);
            match(input,IDENT,FOLLOW_IDENT_in_media_feature547); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(241, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:246:1: medium : ( IDENT | GEN ) ( WS )* ;
    public final void medium() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "medium");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(246, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:247:5: ( ( IDENT | GEN ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:247:7: ( IDENT | GEN ) ( WS )*
            {
            dbg.location(247,7);
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

            dbg.location(247,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:247:23: ( WS )*
            try { dbg.enterSubRule(36);

            loop36:
            do {
                int alt36=2;
                try { dbg.enterDecision(36, decisionCanBacktrack[36]);

                int LA36_0 = input.LA(1);

                if ( (LA36_0==WS) ) {
                    alt36=1;
                }


                } finally {dbg.exitDecision(36);}

                switch (alt36) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:247:23: WS
            	    {
            	    dbg.location(247,23);
            	    match(input,WS,FOLLOW_WS_in_medium574); if (state.failed) return ;

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
        dbg.location(248, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:251:1: bodylist : ( bodyset )* ;
    public final void bodylist() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodylist");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(251, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:252:5: ( ( bodyset )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:252:7: ( bodyset )*
            {
            dbg.location(252,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:252:7: ( bodyset )*
            try { dbg.enterSubRule(37);

            loop37:
            do {
                int alt37=2;
                try { dbg.enterDecision(37, decisionCanBacktrack[37]);

                int LA37_0 = input.LA(1);

                if ( (LA37_0==IDENT||LA37_0==MEDIA_SYM||(LA37_0>=GEN && LA37_0<=FONT_FACE_SYM)||LA37_0==COLON||(LA37_0>=STAR && LA37_0<=DCOLON)) ) {
                    alt37=1;
                }


                } finally {dbg.exitDecision(37);}

                switch (alt37) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:252:7: bodyset
            	    {
            	    dbg.location(252,7);
            	    pushFollow(FOLLOW_bodyset_in_bodylist597);
            	    bodyset();

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
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(253, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:255:1: bodyset : ( ruleSet | media | page | counterStyle | fontFace ) ( WS )* ;
    public final void bodyset() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyset");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(255, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:256:5: ( ( ruleSet | media | page | counterStyle | fontFace ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:256:7: ( ruleSet | media | page | counterStyle | fontFace ) ( WS )*
            {
            dbg.location(256,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:256:7: ( ruleSet | media | page | counterStyle | fontFace )
            int alt38=5;
            try { dbg.enterSubRule(38);
            try { dbg.enterDecision(38, decisionCanBacktrack[38]);

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
                alt38=1;
                }
                break;
            case MEDIA_SYM:
                {
                alt38=2;
                }
                break;
            case PAGE_SYM:
                {
                alt38=3;
                }
                break;
            case COUNTER_STYLE_SYM:
                {
                alt38=4;
                }
                break;
            case FONT_FACE_SYM:
                {
                alt38=5;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 38, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(38);}

            switch (alt38) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:257:6: ruleSet
                    {
                    dbg.location(257,6);
                    pushFollow(FOLLOW_ruleSet_in_bodyset626);
                    ruleSet();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:258:11: media
                    {
                    dbg.location(258,11);
                    pushFollow(FOLLOW_media_in_bodyset638);
                    media();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:259:11: page
                    {
                    dbg.location(259,11);
                    pushFollow(FOLLOW_page_in_bodyset650);
                    page();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:260:11: counterStyle
                    {
                    dbg.location(260,11);
                    pushFollow(FOLLOW_counterStyle_in_bodyset662);
                    counterStyle();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:261:11: fontFace
                    {
                    dbg.location(261,11);
                    pushFollow(FOLLOW_fontFace_in_bodyset674);
                    fontFace();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(38);}

            dbg.location(263,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:263:7: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:263:7: WS
            	    {
            	    dbg.location(263,7);
            	    match(input,WS,FOLLOW_WS_in_bodyset690); if (state.failed) return ;

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
        dbg.location(264, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:266:1: page : PAGE_SYM ( WS )? ( IDENT )? ( pseudoPage ( WS )* )? LBRACE ( WS )* ( declaration | margin ( WS )* )? ( SEMI ( WS )* ( declaration | margin ( WS )* )? )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(266, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:5: ( PAGE_SYM ( WS )? ( IDENT )? ( pseudoPage ( WS )* )? LBRACE ( WS )* ( declaration | margin ( WS )* )? ( SEMI ( WS )* ( declaration | margin ( WS )* )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:7: PAGE_SYM ( WS )? ( IDENT )? ( pseudoPage ( WS )* )? LBRACE ( WS )* ( declaration | margin ( WS )* )? ( SEMI ( WS )* ( declaration | margin ( WS )* )? )* RBRACE
            {
            dbg.location(267,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page712); if (state.failed) return ;
            dbg.location(267,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:16: ( WS )?
            int alt40=2;
            try { dbg.enterSubRule(40);
            try { dbg.enterDecision(40, decisionCanBacktrack[40]);

            int LA40_0 = input.LA(1);

            if ( (LA40_0==WS) ) {
                alt40=1;
            }
            } finally {dbg.exitDecision(40);}

            switch (alt40) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:16: WS
                    {
                    dbg.location(267,16);
                    match(input,WS,FOLLOW_WS_in_page714); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(40);}

            dbg.location(267,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:20: ( IDENT )?
            int alt41=2;
            try { dbg.enterSubRule(41);
            try { dbg.enterDecision(41, decisionCanBacktrack[41]);

            int LA41_0 = input.LA(1);

            if ( (LA41_0==IDENT) ) {
                alt41=1;
            }
            } finally {dbg.exitDecision(41);}

            switch (alt41) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:20: IDENT
                    {
                    dbg.location(267,20);
                    match(input,IDENT,FOLLOW_IDENT_in_page717); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(41);}

            dbg.location(267,27);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:27: ( pseudoPage ( WS )* )?
            int alt43=2;
            try { dbg.enterSubRule(43);
            try { dbg.enterDecision(43, decisionCanBacktrack[43]);

            int LA43_0 = input.LA(1);

            if ( (LA43_0==COLON) ) {
                alt43=1;
            }
            } finally {dbg.exitDecision(43);}

            switch (alt43) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:28: pseudoPage ( WS )*
                    {
                    dbg.location(267,28);
                    pushFollow(FOLLOW_pseudoPage_in_page721);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(267,39);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:39: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:267:39: WS
                    	    {
                    	    dbg.location(267,39);
                    	    match(input,WS,FOLLOW_WS_in_page723); if (state.failed) return ;

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

            dbg.location(268,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page736); if (state.failed) return ;
            dbg.location(268,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:268:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:268:16: WS
            	    {
            	    dbg.location(268,16);
            	    match(input,WS,FOLLOW_WS_in_page738); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop44;
                }
            } while (true);
            } finally {dbg.exitSubRule(44);}

            dbg.location(273,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:273:13: ( declaration | margin ( WS )* )?
            int alt46=3;
            try { dbg.enterSubRule(46);
            try { dbg.enterDecision(46, decisionCanBacktrack[46]);

            int LA46_0 = input.LA(1);

            if ( (LA46_0==IDENT||LA46_0==GEN) ) {
                alt46=1;
            }
            else if ( ((LA46_0>=TOPLEFTCORNER_SYM && LA46_0<=RIGHTBOTTOM_SYM)) ) {
                alt46=2;
            }
            } finally {dbg.exitDecision(46);}

            switch (alt46) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:273:14: declaration
                    {
                    dbg.location(273,14);
                    pushFollow(FOLLOW_declaration_in_page806);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:273:26: margin ( WS )*
                    {
                    dbg.location(273,26);
                    pushFollow(FOLLOW_margin_in_page808);
                    margin();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(273,33);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:273:33: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:273:33: WS
                    	    {
                    	    dbg.location(273,33);
                    	    match(input,WS,FOLLOW_WS_in_page810); if (state.failed) return ;

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

            dbg.location(273,39);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:273:39: ( SEMI ( WS )* ( declaration | margin ( WS )* )? )*
            try { dbg.enterSubRule(50);

            loop50:
            do {
                int alt50=2;
                try { dbg.enterDecision(50, decisionCanBacktrack[50]);

                int LA50_0 = input.LA(1);

                if ( (LA50_0==SEMI) ) {
                    alt50=1;
                }


                } finally {dbg.exitDecision(50);}

                switch (alt50) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:273:40: SEMI ( WS )* ( declaration | margin ( WS )* )?
            	    {
            	    dbg.location(273,40);
            	    match(input,SEMI,FOLLOW_SEMI_in_page816); if (state.failed) return ;
            	    dbg.location(273,45);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:273:45: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:273:45: WS
            	    	    {
            	    	    dbg.location(273,45);
            	    	    match(input,WS,FOLLOW_WS_in_page818); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop47;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(47);}

            	    dbg.location(273,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:273:49: ( declaration | margin ( WS )* )?
            	    int alt49=3;
            	    try { dbg.enterSubRule(49);
            	    try { dbg.enterDecision(49, decisionCanBacktrack[49]);

            	    int LA49_0 = input.LA(1);

            	    if ( (LA49_0==IDENT||LA49_0==GEN) ) {
            	        alt49=1;
            	    }
            	    else if ( ((LA49_0>=TOPLEFTCORNER_SYM && LA49_0<=RIGHTBOTTOM_SYM)) ) {
            	        alt49=2;
            	    }
            	    } finally {dbg.exitDecision(49);}

            	    switch (alt49) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:273:50: declaration
            	            {
            	            dbg.location(273,50);
            	            pushFollow(FOLLOW_declaration_in_page822);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:273:62: margin ( WS )*
            	            {
            	            dbg.location(273,62);
            	            pushFollow(FOLLOW_margin_in_page824);
            	            margin();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(273,69);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:273:69: ( WS )*
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

            	            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:273:69: WS
            	            	    {
            	            	    dbg.location(273,69);
            	            	    match(input,WS,FOLLOW_WS_in_page826); if (state.failed) return ;

            	            	    }
            	            	    break;

            	            	default :
            	            	    break loop48;
            	                }
            	            } while (true);
            	            } finally {dbg.exitSubRule(48);}


            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(49);}


            	    }
            	    break;

            	default :
            	    break loop50;
                }
            } while (true);
            } finally {dbg.exitSubRule(50);}

            dbg.location(274,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page841); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(275, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "page");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "page"


    // $ANTLR start "counterStyle"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:277:1: counterStyle : COUNTER_STYLE_SYM ( WS )* IDENT ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void counterStyle() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "counterStyle");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(277, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:278:5: ( COUNTER_STYLE_SYM ( WS )* IDENT ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:278:7: COUNTER_STYLE_SYM ( WS )* IDENT ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(278,7);
            match(input,COUNTER_STYLE_SYM,FOLLOW_COUNTER_STYLE_SYM_in_counterStyle862); if (state.failed) return ;
            dbg.location(278,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:278:25: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:278:25: WS
            	    {
            	    dbg.location(278,25);
            	    match(input,WS,FOLLOW_WS_in_counterStyle864); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop51;
                }
            } while (true);
            } finally {dbg.exitSubRule(51);}

            dbg.location(278,29);
            match(input,IDENT,FOLLOW_IDENT_in_counterStyle867); if (state.failed) return ;
            dbg.location(278,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:278:35: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:278:35: WS
            	    {
            	    dbg.location(278,35);
            	    match(input,WS,FOLLOW_WS_in_counterStyle869); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop52;
                }
            } while (true);
            } finally {dbg.exitSubRule(52);}

            dbg.location(279,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_counterStyle880); if (state.failed) return ;
            dbg.location(279,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:279:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:279:16: WS
            	    {
            	    dbg.location(279,16);
            	    match(input,WS,FOLLOW_WS_in_counterStyle882); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop53;
                }
            } while (true);
            } finally {dbg.exitSubRule(53);}

            dbg.location(279,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_counterStyle885);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(280,3);
            pushFollow(FOLLOW_declarations_in_counterStyle889);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(281,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_counterStyle899); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(282, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "counterStyle");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "counterStyle"


    // $ANTLR start "fontFace"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:284:1: fontFace : FONT_FACE_SYM ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void fontFace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fontFace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(284, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:285:5: ( FONT_FACE_SYM ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:285:7: FONT_FACE_SYM ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(285,7);
            match(input,FONT_FACE_SYM,FOLLOW_FONT_FACE_SYM_in_fontFace920); if (state.failed) return ;
            dbg.location(285,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:285:21: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:285:21: WS
            	    {
            	    dbg.location(285,21);
            	    match(input,WS,FOLLOW_WS_in_fontFace922); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop54;
                }
            } while (true);
            } finally {dbg.exitSubRule(54);}

            dbg.location(286,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_fontFace933); if (state.failed) return ;
            dbg.location(286,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:286:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:286:16: WS
            	    {
            	    dbg.location(286,16);
            	    match(input,WS,FOLLOW_WS_in_fontFace935); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop55;
                }
            } while (true);
            } finally {dbg.exitSubRule(55);}

            dbg.location(286,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_fontFace938);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(287,3);
            pushFollow(FOLLOW_declarations_in_fontFace942);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(288,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_fontFace952); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "fontFace");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "fontFace"


    // $ANTLR start "margin"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:292:1: margin : margin_sym ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void margin() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(292, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:293:2: ( margin_sym ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:293:4: margin_sym ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(293,4);
            pushFollow(FOLLOW_margin_sym_in_margin972);
            margin_sym();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(293,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:293:15: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:293:15: WS
            	    {
            	    dbg.location(293,15);
            	    match(input,WS,FOLLOW_WS_in_margin974); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop56;
                }
            } while (true);
            } finally {dbg.exitSubRule(56);}

            dbg.location(293,19);
            match(input,LBRACE,FOLLOW_LBRACE_in_margin977); if (state.failed) return ;
            dbg.location(293,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:293:26: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:293:26: WS
            	    {
            	    dbg.location(293,26);
            	    match(input,WS,FOLLOW_WS_in_margin979); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop57;
                }
            } while (true);
            } finally {dbg.exitSubRule(57);}

            dbg.location(293,30);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_margin982);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(293,50);
            pushFollow(FOLLOW_declarations_in_margin984);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(293,63);
            match(input,RBRACE,FOLLOW_RBRACE_in_margin986); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(294, 8);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "margin");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "margin"


    // $ANTLR start "margin_sym"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:296:1: margin_sym : ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM );
    public final void margin_sym() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin_sym");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(296, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:297:2: ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(297,2);
            if ( (input.LA(1)>=TOPLEFTCORNER_SYM && input.LA(1)<=RIGHTBOTTOM_SYM) ) {
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
        dbg.location(314, 8);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "margin_sym");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "margin_sym"


    // $ANTLR start "pseudoPage"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:316:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(316, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:317:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:317:7: COLON IDENT
            {
            dbg.location(317,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage1215); if (state.failed) return ;
            dbg.location(317,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage1217); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(318, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:1: operator : ( SOLIDUS ( WS )* | COMMA ( WS )* | );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(320, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:321:5: ( SOLIDUS ( WS )* | COMMA ( WS )* | )
            int alt60=3;
            try { dbg.enterDecision(60, decisionCanBacktrack[60]);

            switch ( input.LA(1) ) {
            case SOLIDUS:
                {
                alt60=1;
                }
                break;
            case COMMA:
                {
                alt60=2;
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
                alt60=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 60, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(60);}

            switch (alt60) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:321:7: SOLIDUS ( WS )*
                    {
                    dbg.location(321,7);
                    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_operator1238); if (state.failed) return ;
                    dbg.location(321,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:321:15: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:321:15: WS
                    	    {
                    	    dbg.location(321,15);
                    	    match(input,WS,FOLLOW_WS_in_operator1240); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop58;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(58);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:322:7: COMMA ( WS )*
                    {
                    dbg.location(322,7);
                    match(input,COMMA,FOLLOW_COMMA_in_operator1249); if (state.failed) return ;
                    dbg.location(322,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:322:13: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:322:13: WS
                    	    {
                    	    dbg.location(322,13);
                    	    match(input,WS,FOLLOW_WS_in_operator1251); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop59;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(59);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:324:5: 
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
        dbg.location(324, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:326:1: combinator : ( PLUS ( WS )* | GREATER ( WS )* | TILDE ( WS )* | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(326, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:327:5: ( PLUS ( WS )* | GREATER ( WS )* | TILDE ( WS )* | )
            int alt64=4;
            try { dbg.enterDecision(64, decisionCanBacktrack[64]);

            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt64=1;
                }
                break;
            case GREATER:
                {
                alt64=2;
                }
                break;
            case TILDE:
                {
                alt64=3;
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
                alt64=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 64, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(64);}

            switch (alt64) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:327:7: PLUS ( WS )*
                    {
                    dbg.location(327,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator1279); if (state.failed) return ;
                    dbg.location(327,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:327:12: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:327:12: WS
                    	    {
                    	    dbg.location(327,12);
                    	    match(input,WS,FOLLOW_WS_in_combinator1281); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop61;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(61);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:328:7: GREATER ( WS )*
                    {
                    dbg.location(328,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator1290); if (state.failed) return ;
                    dbg.location(328,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:328:15: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:328:15: WS
                    	    {
                    	    dbg.location(328,15);
                    	    match(input,WS,FOLLOW_WS_in_combinator1292); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop62;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(62);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:329:7: TILDE ( WS )*
                    {
                    dbg.location(329,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator1301); if (state.failed) return ;
                    dbg.location(329,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:329:13: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:329:13: WS
                    	    {
                    	    dbg.location(329,13);
                    	    match(input,WS,FOLLOW_WS_in_combinator1303); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop63;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(63);}


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:331:5: 
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
        dbg.location(331, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(333, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:334:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(334,5);
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
        dbg.location(336, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:1: property : ( IDENT | GEN ) ( WS )* ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(338, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:339:5: ( ( IDENT | GEN ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:339:7: ( IDENT | GEN ) ( WS )*
            {
            dbg.location(339,7);
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

            dbg.location(339,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:339:21: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:339:21: WS
            	    {
            	    dbg.location(339,21);
            	    match(input,WS,FOLLOW_WS_in_property1371); if (state.failed) return ;

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
        dbg.location(340, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:342:1: ruleSet : selectorsGroup LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void ruleSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ruleSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(342, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:343:5: ( selectorsGroup LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:343:9: selectorsGroup LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(343,9);
            pushFollow(FOLLOW_selectorsGroup_in_ruleSet1396);
            selectorsGroup();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(344,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_ruleSet1406); if (state.failed) return ;
            dbg.location(344,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:344:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:344:16: WS
            	    {
            	    dbg.location(344,16);
            	    match(input,WS,FOLLOW_WS_in_ruleSet1408); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop66;
                }
            } while (true);
            } finally {dbg.exitSubRule(66);}

            dbg.location(344,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_ruleSet1411);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(345,13);
            pushFollow(FOLLOW_declarations_in_ruleSet1425);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(346,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_ruleSet1435); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(347, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:1: declarations : ( declaration )? ( SEMI ( WS )* ( declaration )? )* ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(349, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:5: ( ( declaration )? ( SEMI ( WS )* ( declaration )? )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:352:9: ( declaration )? ( SEMI ( WS )* ( declaration )? )*
            {
            dbg.location(352,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:352:9: ( declaration )?
            int alt67=2;
            try { dbg.enterSubRule(67);
            try { dbg.enterDecision(67, decisionCanBacktrack[67]);

            int LA67_0 = input.LA(1);

            if ( (LA67_0==IDENT||LA67_0==GEN) ) {
                alt67=1;
            }
            } finally {dbg.exitDecision(67);}

            switch (alt67) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:352:9: declaration
                    {
                    dbg.location(352,9);
                    pushFollow(FOLLOW_declaration_in_declarations1473);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(67);}

            dbg.location(352,22);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:352:22: ( SEMI ( WS )* ( declaration )? )*
            try { dbg.enterSubRule(70);

            loop70:
            do {
                int alt70=2;
                try { dbg.enterDecision(70, decisionCanBacktrack[70]);

                int LA70_0 = input.LA(1);

                if ( (LA70_0==SEMI) ) {
                    alt70=1;
                }


                } finally {dbg.exitDecision(70);}

                switch (alt70) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:352:23: SEMI ( WS )* ( declaration )?
            	    {
            	    dbg.location(352,23);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations1477); if (state.failed) return ;
            	    dbg.location(352,28);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:352:28: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:352:28: WS
            	    	    {
            	    	    dbg.location(352,28);
            	    	    match(input,WS,FOLLOW_WS_in_declarations1479); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop68;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(68);}

            	    dbg.location(352,32);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:352:32: ( declaration )?
            	    int alt69=2;
            	    try { dbg.enterSubRule(69);
            	    try { dbg.enterDecision(69, decisionCanBacktrack[69]);

            	    int LA69_0 = input.LA(1);

            	    if ( (LA69_0==IDENT||LA69_0==GEN) ) {
            	        alt69=1;
            	    }
            	    } finally {dbg.exitDecision(69);}

            	    switch (alt69) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:352:32: declaration
            	            {
            	            dbg.location(352,32);
            	            pushFollow(FOLLOW_declaration_in_declarations1482);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(69);}


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
        dbg.location(353, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:1: selectorsGroup : selector ( COMMA ( WS )* selector )* ;
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(355, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:356:5: ( selector ( COMMA ( WS )* selector )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:356:7: selector ( COMMA ( WS )* selector )*
            {
            dbg.location(356,7);
            pushFollow(FOLLOW_selector_in_selectorsGroup1506);
            selector();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(356,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:356:16: ( COMMA ( WS )* selector )*
            try { dbg.enterSubRule(72);

            loop72:
            do {
                int alt72=2;
                try { dbg.enterDecision(72, decisionCanBacktrack[72]);

                int LA72_0 = input.LA(1);

                if ( (LA72_0==COMMA) ) {
                    alt72=1;
                }


                } finally {dbg.exitDecision(72);}

                switch (alt72) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:356:17: COMMA ( WS )* selector
            	    {
            	    dbg.location(356,17);
            	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup1509); if (state.failed) return ;
            	    dbg.location(356,23);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:356:23: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:356:23: WS
            	    	    {
            	    	    dbg.location(356,23);
            	    	    match(input,WS,FOLLOW_WS_in_selectorsGroup1511); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop71;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(71);}

            	    dbg.location(356,27);
            	    pushFollow(FOLLOW_selector_in_selectorsGroup1514);
            	    selector();

            	    state._fsp--;
            	    if (state.failed) return ;

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
        dbg.location(357, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(359, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(360,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector1537);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(360,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(73);

            loop73:
            do {
                int alt73=2;
                try { dbg.enterDecision(73, decisionCanBacktrack[73]);

                int LA73_0 = input.LA(1);

                if ( (LA73_0==IDENT||LA73_0==GEN||LA73_0==COLON||(LA73_0>=PLUS && LA73_0<=TILDE)||(LA73_0>=STAR && LA73_0<=DCOLON)) ) {
                    alt73=1;
                }


                } finally {dbg.exitDecision(73);}

                switch (alt73) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(360,31);
            	    pushFollow(FOLLOW_combinator_in_selector1540);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(360,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector1542);
            	    simpleSelectorSequence();

            	    state._fsp--;
            	    if (state.failed) return ;

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
        dbg.location(361, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(364, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:366:2: ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) )
            int alt76=2;
            try { dbg.enterDecision(76, decisionCanBacktrack[76]);

            int LA76_0 = input.LA(1);

            if ( (LA76_0==IDENT||LA76_0==GEN||(LA76_0>=STAR && LA76_0<=PIPE)) ) {
                alt76=1;
            }
            else if ( (LA76_0==COLON||(LA76_0>=HASH && LA76_0<=DCOLON)) ) {
                alt76=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 76, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(76);}

            switch (alt76) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:370:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    {
                    dbg.location(370,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:370:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:370:5: typeSelector ( ( esPred )=> elementSubsequent )*
                    {
                    dbg.location(370,5);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence1582);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(370,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:370:18: ( ( esPred )=> elementSubsequent )*
                    try { dbg.enterSubRule(74);

                    loop74:
                    do {
                        int alt74=2;
                        try { dbg.enterDecision(74, decisionCanBacktrack[74]);

                        try {
                            isCyclicDecision = true;
                            alt74 = dfa74.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(74);}

                        switch (alt74) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:370:19: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(370,29);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence1589);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop74;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(74);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:2: ( ( ( esPred )=> elementSubsequent )+ )
                    {
                    dbg.location(372,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:2: ( ( ( esPred )=> elementSubsequent )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:4: ( ( esPred )=> elementSubsequent )+
                    {
                    dbg.location(372,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:4: ( ( esPred )=> elementSubsequent )+
                    int cnt75=0;
                    try { dbg.enterSubRule(75);

                    loop75:
                    do {
                        int alt75=2;
                        try { dbg.enterDecision(75, decisionCanBacktrack[75]);

                        switch ( input.LA(1) ) {
                        case HASH:
                            {
                            int LA75_2 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt75=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA75_3 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt75=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA75_4 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt75=1;
                            }


                            }
                            break;
                        case COLON:
                        case DCOLON:
                            {
                            int LA75_5 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt75=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(75);}

                        switch (alt75) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:5: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(372,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence1607);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt75 >= 1 ) break loop75;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(75, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt75++;
                    } while (true);
                    } finally {dbg.exitSubRule(75);}


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
        dbg.location(373, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:387:1: typeSelector options {k=2; } : ( ( nsPred )=> namespace_wqname_prefix )? ( elementName ( WS )* ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(387, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:3: ( ( ( nsPred )=> namespace_wqname_prefix )? ( elementName ( WS )* ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:6: ( ( nsPred )=> namespace_wqname_prefix )? ( elementName ( WS )* )
            {
            dbg.location(389,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:6: ( ( nsPred )=> namespace_wqname_prefix )?
            int alt77=2;
            try { dbg.enterSubRule(77);
            try { dbg.enterDecision(77, decisionCanBacktrack[77]);

            try {
                isCyclicDecision = true;
                alt77 = dfa77.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(77);}

            switch (alt77) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:7: ( nsPred )=> namespace_wqname_prefix
                    {
                    dbg.location(389,17);
                    pushFollow(FOLLOW_namespace_wqname_prefix_in_typeSelector1658);
                    namespace_wqname_prefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(77);}

            dbg.location(389,43);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:43: ( elementName ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:45: elementName ( WS )*
            {
            dbg.location(389,45);
            pushFollow(FOLLOW_elementName_in_typeSelector1664);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(389,57);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:57: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:57: WS
            	    {
            	    dbg.location(389,57);
            	    match(input,WS,FOLLOW_WS_in_typeSelector1666); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop78;
                }
            } while (true);
            } finally {dbg.exitSubRule(78);}


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(390, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:392:2: nsPred : ( IDENT | STAR ) PIPE ;
    public final void nsPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "nsPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(392, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:3: ( ( IDENT | STAR ) PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:394:3: ( IDENT | STAR ) PIPE
            {
            dbg.location(394,3);
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

            dbg.location(394,18);
            match(input,PIPE,FOLLOW_PIPE_in_nsPred1699); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(395, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:2: namespace_wqname_prefix : ( ( namespace_prefix ( WS )* )? PIPE | namespace_wildcard_prefix ( WS )* PIPE );
    public final void namespace_wqname_prefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace_wqname_prefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(403, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:404:3: ( ( namespace_prefix ( WS )* )? PIPE | namespace_wildcard_prefix ( WS )* PIPE )
            int alt82=2;
            try { dbg.enterDecision(82, decisionCanBacktrack[82]);

            int LA82_0 = input.LA(1);

            if ( (LA82_0==IDENT||LA82_0==PIPE) ) {
                alt82=1;
            }
            else if ( (LA82_0==STAR) ) {
                alt82=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 82, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(82);}

            switch (alt82) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:404:5: ( namespace_prefix ( WS )* )? PIPE
                    {
                    dbg.location(404,5);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:404:5: ( namespace_prefix ( WS )* )?
                    int alt80=2;
                    try { dbg.enterSubRule(80);
                    try { dbg.enterDecision(80, decisionCanBacktrack[80]);

                    int LA80_0 = input.LA(1);

                    if ( (LA80_0==IDENT) ) {
                        alt80=1;
                    }
                    } finally {dbg.exitDecision(80);}

                    switch (alt80) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:404:7: namespace_prefix ( WS )*
                            {
                            dbg.location(404,7);
                            pushFollow(FOLLOW_namespace_prefix_in_namespace_wqname_prefix1729);
                            namespace_prefix();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(404,24);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:404:24: ( WS )*
                            try { dbg.enterSubRule(79);

                            loop79:
                            do {
                                int alt79=2;
                                try { dbg.enterDecision(79, decisionCanBacktrack[79]);

                                int LA79_0 = input.LA(1);

                                if ( (LA79_0==WS) ) {
                                    alt79=1;
                                }


                                } finally {dbg.exitDecision(79);}

                                switch (alt79) {
                            	case 1 :
                            	    dbg.enterAlt(1);

                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:404:24: WS
                            	    {
                            	    dbg.location(404,24);
                            	    match(input,WS,FOLLOW_WS_in_namespace_wqname_prefix1731); if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop79;
                                }
                            } while (true);
                            } finally {dbg.exitSubRule(79);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(80);}

                    dbg.location(404,31);
                    match(input,PIPE,FOLLOW_PIPE_in_namespace_wqname_prefix1737); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:405:6: namespace_wildcard_prefix ( WS )* PIPE
                    {
                    dbg.location(405,6);
                    pushFollow(FOLLOW_namespace_wildcard_prefix_in_namespace_wqname_prefix1744);
                    namespace_wildcard_prefix();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(405,32);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:405:32: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:405:32: WS
                    	    {
                    	    dbg.location(405,32);
                    	    match(input,WS,FOLLOW_WS_in_namespace_wqname_prefix1746); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop81;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(81);}

                    dbg.location(405,36);
                    match(input,PIPE,FOLLOW_PIPE_in_namespace_wqname_prefix1749); if (state.failed) return ;

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
        dbg.location(406, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:1: namespace_wildcard_prefix : STAR ;
    public final void namespace_wildcard_prefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace_wildcard_prefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(408, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:4: ( STAR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:4: STAR
            {
            dbg.location(410,4);
            match(input,STAR,FOLLOW_STAR_in_namespace_wildcard_prefix1771); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(411, 4);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:413:1: esPred : ( HASH | DOT | LBRACKET | COLON | DCOLON );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(413, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:5: ( HASH | DOT | LBRACKET | COLON | DCOLON )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(414,5);
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
        dbg.location(415, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:1: elementSubsequent : ( cssId | cssClass | attrib | pseudo ) ( WS )* ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(417, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:5: ( ( cssId | cssClass | attrib | pseudo ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:419:5: ( cssId | cssClass | attrib | pseudo ) ( WS )*
            {
            dbg.location(419,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:419:5: ( cssId | cssClass | attrib | pseudo )
            int alt83=4;
            try { dbg.enterSubRule(83);
            try { dbg.enterDecision(83, decisionCanBacktrack[83]);

            switch ( input.LA(1) ) {
            case HASH:
                {
                alt83=1;
                }
                break;
            case DOT:
                {
                alt83=2;
                }
                break;
            case LBRACKET:
                {
                alt83=3;
                }
                break;
            case COLON:
            case DCOLON:
                {
                alt83=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 83, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(83);}

            switch (alt83) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:6: cssId
                    {
                    dbg.location(420,6);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent1844);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:8: cssClass
                    {
                    dbg.location(421,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent1853);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:11: attrib
                    {
                    dbg.location(422,11);
                    pushFollow(FOLLOW_attrib_in_elementSubsequent1865);
                    attrib();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:11: pseudo
                    {
                    dbg.location(423,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent1877);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(83);}

            dbg.location(425,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:425:5: ( WS )*
            try { dbg.enterSubRule(84);

            loop84:
            do {
                int alt84=2;
                try { dbg.enterDecision(84, decisionCanBacktrack[84]);

                int LA84_0 = input.LA(1);

                if ( (LA84_0==WS) ) {
                    alt84=1;
                }


                } finally {dbg.exitDecision(84);}

                switch (alt84) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:425:5: WS
            	    {
            	    dbg.location(425,5);
            	    match(input,WS,FOLLOW_WS_in_elementSubsequent1889); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop84;
                }
            } while (true);
            } finally {dbg.exitSubRule(84);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(426, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:428:1: cssId : HASH ;
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(428, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:7: HASH
            {
            dbg.location(429,7);
            match(input,HASH,FOLLOW_HASH_in_cssId1911); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(430, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(432, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:433:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:433:7: DOT ( IDENT | GEN )
            {
            dbg.location(433,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass1928); if (state.failed) return ;
            dbg.location(433,11);
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
        dbg.location(434, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:437:1: elementName : ( ( IDENT | GEN ) | '*' );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(437, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:5: ( ( IDENT | GEN ) | '*' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(438,5);
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
        dbg.location(439, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:441:1: attrib : LBRACKET ( namespace_wqname_prefix )? ( WS )* attrib_name ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( WS )* attrib_value ( WS )* )? RBRACKET ;
    public final void attrib() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrib");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(441, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:5: ( LBRACKET ( namespace_wqname_prefix )? ( WS )* attrib_name ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( WS )* attrib_value ( WS )* )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:442:7: LBRACKET ( namespace_wqname_prefix )? ( WS )* attrib_name ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( WS )* attrib_value ( WS )* )? RBRACKET
            {
            dbg.location(442,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_attrib1994); if (state.failed) return ;
            dbg.location(443,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:443:6: ( namespace_wqname_prefix )?
            int alt85=2;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:443:6: namespace_wqname_prefix
                    {
                    dbg.location(443,6);
                    pushFollow(FOLLOW_namespace_wqname_prefix_in_attrib2001);
                    namespace_wqname_prefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(85);}

            dbg.location(443,31);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:443:31: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:443:31: WS
            	    {
            	    dbg.location(443,31);
            	    match(input,WS,FOLLOW_WS_in_attrib2004); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop86;
                }
            } while (true);
            } finally {dbg.exitSubRule(86);}

            dbg.location(444,9);
            pushFollow(FOLLOW_attrib_name_in_attrib2015);
            attrib_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(444,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:21: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:21: WS
            	    {
            	    dbg.location(444,21);
            	    match(input,WS,FOLLOW_WS_in_attrib2017); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop87;
                }
            } while (true);
            } finally {dbg.exitSubRule(87);}

            dbg.location(446,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:13: ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( WS )* attrib_value ( WS )* )?
            int alt90=2;
            try { dbg.enterSubRule(90);
            try { dbg.enterDecision(90, decisionCanBacktrack[90]);

            int LA90_0 = input.LA(1);

            if ( ((LA90_0>=OPEQ && LA90_0<=CONTAINS)) ) {
                alt90=1;
            }
            } finally {dbg.exitDecision(90);}

            switch (alt90) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:447:17: ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( WS )* attrib_value ( WS )*
                    {
                    dbg.location(447,17);
                    if ( (input.LA(1)>=OPEQ && input.LA(1)<=CONTAINS) ) {
                        input.consume();
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        dbg.recognitionException(mse);
                        throw mse;
                    }

                    dbg.location(455,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:17: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:17: WS
                    	    {
                    	    dbg.location(455,17);
                    	    match(input,WS,FOLLOW_WS_in_attrib2239); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop88;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(88);}

                    dbg.location(456,17);
                    pushFollow(FOLLOW_attrib_value_in_attrib2258);
                    attrib_value();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(457,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:457:17: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:457:17: WS
                    	    {
                    	    dbg.location(457,17);
                    	    match(input,WS,FOLLOW_WS_in_attrib2276); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop89;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(89);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(90);}

            dbg.location(460,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_attrib2305); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(461, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:467:1: syncTo_IDENT_RBRACKET_LBRACE : ;
    public final void syncTo_IDENT_RBRACKET_LBRACE() throws RecognitionException {

                syncToSet(BitSet.of(IDENT, RBRACKET, LBRACE));
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_IDENT_RBRACKET_LBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(467, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:471:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:472:6: 
            {
            }

        }
        finally {
        }
        dbg.location(472, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:1: attrib_name : IDENT ;
    public final void attrib_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrib_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(475, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:4: IDENT
            {
            dbg.location(476,4);
            match(input,IDENT,FOLLOW_IDENT_in_attrib_name2348); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(477, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:1: attrib_value : ( IDENT | STRING ) ;
    public final void attrib_value() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrib_value");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(479, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:2: ( IDENT | STRING )
            {
            dbg.location(481,2);
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
        dbg.location(485, 9);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:1: pseudo : ( COLON | DCOLON ) ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )? ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(487, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:488:5: ( ( COLON | DCOLON ) ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:488:7: ( COLON | DCOLON ) ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )?
            {
            dbg.location(488,7);
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

            dbg.location(489,13);
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

            dbg.location(490,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:17: ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )?
            int alt95=2;
            try { dbg.enterSubRule(95);
            try { dbg.enterDecision(95, decisionCanBacktrack[95]);

            try {
                isCyclicDecision = true;
                alt95 = dfa95.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(95);}

            switch (alt95) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:21: ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN
                    {
                    dbg.location(491,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:21: ( WS )*
                    try { dbg.enterSubRule(91);

                    loop91:
                    do {
                        int alt91=2;
                        try { dbg.enterDecision(91, decisionCanBacktrack[91]);

                        int LA91_0 = input.LA(1);

                        if ( (LA91_0==WS) ) {
                            alt91=1;
                        }


                        } finally {dbg.exitDecision(91);}

                        switch (alt91) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:21: WS
                    	    {
                    	    dbg.location(491,21);
                    	    match(input,WS,FOLLOW_WS_in_pseudo2493); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop91;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(91);}

                    dbg.location(491,25);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo2496); if (state.failed) return ;
                    dbg.location(491,32);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:32: ( WS )*
                    try { dbg.enterSubRule(92);

                    loop92:
                    do {
                        int alt92=2;
                        try { dbg.enterDecision(92, decisionCanBacktrack[92]);

                        int LA92_0 = input.LA(1);

                        if ( (LA92_0==WS) ) {
                            alt92=1;
                        }


                        } finally {dbg.exitDecision(92);}

                        switch (alt92) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:32: WS
                    	    {
                    	    dbg.location(491,32);
                    	    match(input,WS,FOLLOW_WS_in_pseudo2498); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop92;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(92);}

                    dbg.location(491,36);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:36: ( ( IDENT | GEN ) ( WS )* )?
                    int alt94=2;
                    try { dbg.enterSubRule(94);
                    try { dbg.enterDecision(94, decisionCanBacktrack[94]);

                    int LA94_0 = input.LA(1);

                    if ( (LA94_0==IDENT||LA94_0==GEN) ) {
                        alt94=1;
                    }
                    } finally {dbg.exitDecision(94);}

                    switch (alt94) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:37: ( IDENT | GEN ) ( WS )*
                            {
                            dbg.location(491,37);
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

                            dbg.location(491,53);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:53: ( WS )*
                            try { dbg.enterSubRule(93);

                            loop93:
                            do {
                                int alt93=2;
                                try { dbg.enterDecision(93, decisionCanBacktrack[93]);

                                int LA93_0 = input.LA(1);

                                if ( (LA93_0==WS) ) {
                                    alt93=1;
                                }


                                } finally {dbg.exitDecision(93);}

                                switch (alt93) {
                            	case 1 :
                            	    dbg.enterAlt(1);

                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:53: WS
                            	    {
                            	    dbg.location(491,53);
                            	    match(input,WS,FOLLOW_WS_in_pseudo2512); if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop93;
                                }
                            } while (true);
                            } finally {dbg.exitSubRule(93);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(94);}

                    dbg.location(491,59);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo2517); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(95);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(493, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:495:1: declaration : property COLON ( WS )* expr ( prio )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(495, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:496:5: ( property COLON ( WS )* expr ( prio )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:498:5: property COLON ( WS )* expr ( prio )?
            {
            dbg.location(498,5);
            pushFollow(FOLLOW_property_in_declaration2563);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(498,14);
            match(input,COLON,FOLLOW_COLON_in_declaration2565); if (state.failed) return ;
            dbg.location(498,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:498:20: ( WS )*
            try { dbg.enterSubRule(96);

            loop96:
            do {
                int alt96=2;
                try { dbg.enterDecision(96, decisionCanBacktrack[96]);

                int LA96_0 = input.LA(1);

                if ( (LA96_0==WS) ) {
                    alt96=1;
                }


                } finally {dbg.exitDecision(96);}

                switch (alt96) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:498:20: WS
            	    {
            	    dbg.location(498,20);
            	    match(input,WS,FOLLOW_WS_in_declaration2567); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop96;
                }
            } while (true);
            } finally {dbg.exitSubRule(96);}

            dbg.location(498,24);
            pushFollow(FOLLOW_expr_in_declaration2570);
            expr();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(498,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:498:29: ( prio )?
            int alt97=2;
            try { dbg.enterSubRule(97);
            try { dbg.enterDecision(97, decisionCanBacktrack[97]);

            int LA97_0 = input.LA(1);

            if ( (LA97_0==IMPORTANT_SYM) ) {
                alt97=1;
            }
            } finally {dbg.exitDecision(97);}

            switch (alt97) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:498:29: prio
                    {
                    dbg.location(498,29);
                    pushFollow(FOLLOW_prio_in_declaration2572);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(97);}


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
        dbg.location(499, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:1: syncTo_IDENT_RBRACE : ;
    public final void syncTo_IDENT_RBRACE() throws RecognitionException {

                syncToSet(BitSet.of(IDENT, RBRACE));
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_IDENT_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(509, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:6: 
            {
            }

        }
        finally {
        }
        dbg.location(514, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:517:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(517, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:522:6: 
            {
            }

        }
        finally {
        }
        dbg.location(522, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:1: prio : IMPORTANT_SYM ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(525, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:5: ( IMPORTANT_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:7: IMPORTANT_SYM
            {
            dbg.location(526,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio2665); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(527, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:529:1: expr : term ( operator term )* ;
    public final void expr() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expr");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(529, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:530:5: ( term ( operator term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:530:7: term ( operator term )*
            {
            dbg.location(530,7);
            pushFollow(FOLLOW_term_in_expr2686);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(530,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:530:12: ( operator term )*
            try { dbg.enterSubRule(98);

            loop98:
            do {
                int alt98=2;
                try { dbg.enterDecision(98, decisionCanBacktrack[98]);

                try {
                    isCyclicDecision = true;
                    alt98 = dfa98.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(98);}

                switch (alt98) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:530:13: operator term
            	    {
            	    dbg.location(530,13);
            	    pushFollow(FOLLOW_operator_in_expr2689);
            	    operator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(530,22);
            	    pushFollow(FOLLOW_term_in_expr2691);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop98;
                }
            } while (true);
            } finally {dbg.exitSubRule(98);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(531, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:1: term : ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( WS )* ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(533, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:5: ( ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:7: ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( WS )*
            {
            dbg.location(534,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:7: ( unaryOperator )?
            int alt99=2;
            try { dbg.enterSubRule(99);
            try { dbg.enterDecision(99, decisionCanBacktrack[99]);

            int LA99_0 = input.LA(1);

            if ( (LA99_0==PLUS||LA99_0==MINUS) ) {
                alt99=1;
            }
            } finally {dbg.exitDecision(99);}

            switch (alt99) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:7: unaryOperator
                    {
                    dbg.location(534,7);
                    pushFollow(FOLLOW_unaryOperator_in_term2714);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(99);}

            dbg.location(535,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:535:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION ) | STRING | IDENT | GEN | URI | hexColor | function )
            int alt100=7;
            try { dbg.enterSubRule(100);
            try { dbg.enterDecision(100, decisionCanBacktrack[100]);

            try {
                isCyclicDecision = true;
                alt100 = dfa100.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(100);}

            switch (alt100) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:536:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION )
                    {
                    dbg.location(536,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:7: STRING
                    {
                    dbg.location(547,7);
                    match(input,STRING,FOLLOW_STRING_in_term2897); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:7: IDENT
                    {
                    dbg.location(548,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term2905); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:549:7: GEN
                    {
                    dbg.location(549,7);
                    match(input,GEN,FOLLOW_GEN_in_term2913); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:550:7: URI
                    {
                    dbg.location(550,7);
                    match(input,URI,FOLLOW_URI_in_term2921); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:551:7: hexColor
                    {
                    dbg.location(551,7);
                    pushFollow(FOLLOW_hexColor_in_term2929);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:552:7: function
                    {
                    dbg.location(552,7);
                    pushFollow(FOLLOW_function_in_term2937);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(100);}

            dbg.location(554,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:554:5: ( WS )*
            try { dbg.enterSubRule(101);

            loop101:
            do {
                int alt101=2;
                try { dbg.enterDecision(101, decisionCanBacktrack[101]);

                int LA101_0 = input.LA(1);

                if ( (LA101_0==WS) ) {
                    alt101=1;
                }


                } finally {dbg.exitDecision(101);}

                switch (alt101) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:554:5: WS
            	    {
            	    dbg.location(554,5);
            	    match(input,WS,FOLLOW_WS_in_term2949); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop101;
                }
            } while (true);
            } finally {dbg.exitSubRule(101);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(555, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:1: function : function_name ( WS )* LPAREN ( WS )* ( expr | ( attribute ( COMMA ( WS )* attribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(557, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:2: ( function_name ( WS )* LPAREN ( WS )* ( expr | ( attribute ( COMMA ( WS )* attribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:5: function_name ( WS )* LPAREN ( WS )* ( expr | ( attribute ( COMMA ( WS )* attribute )* ) ) RPAREN
            {
            dbg.location(558,5);
            pushFollow(FOLLOW_function_name_in_function2965);
            function_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(558,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:19: ( WS )*
            try { dbg.enterSubRule(102);

            loop102:
            do {
                int alt102=2;
                try { dbg.enterDecision(102, decisionCanBacktrack[102]);

                int LA102_0 = input.LA(1);

                if ( (LA102_0==WS) ) {
                    alt102=1;
                }


                } finally {dbg.exitDecision(102);}

                switch (alt102) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:19: WS
            	    {
            	    dbg.location(558,19);
            	    match(input,WS,FOLLOW_WS_in_function2967); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop102;
                }
            } while (true);
            } finally {dbg.exitSubRule(102);}

            dbg.location(559,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function2972); if (state.failed) return ;
            dbg.location(559,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:559:10: ( WS )*
            try { dbg.enterSubRule(103);

            loop103:
            do {
                int alt103=2;
                try { dbg.enterDecision(103, decisionCanBacktrack[103]);

                int LA103_0 = input.LA(1);

                if ( (LA103_0==WS) ) {
                    alt103=1;
                }


                } finally {dbg.exitDecision(103);}

                switch (alt103) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:559:10: WS
            	    {
            	    dbg.location(559,10);
            	    match(input,WS,FOLLOW_WS_in_function2974); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop103;
                }
            } while (true);
            } finally {dbg.exitSubRule(103);}

            dbg.location(560,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:3: ( expr | ( attribute ( COMMA ( WS )* attribute )* ) )
            int alt106=2;
            try { dbg.enterSubRule(106);
            try { dbg.enterDecision(106, decisionCanBacktrack[106]);

            try {
                isCyclicDecision = true;
                alt106 = dfa106.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(106);}

            switch (alt106) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:4: expr
                    {
                    dbg.location(561,4);
                    pushFollow(FOLLOW_expr_in_function2985);
                    expr();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:6: ( attribute ( COMMA ( WS )* attribute )* )
                    {
                    dbg.location(563,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:6: ( attribute ( COMMA ( WS )* attribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:564:5: attribute ( COMMA ( WS )* attribute )*
                    {
                    dbg.location(564,5);
                    pushFollow(FOLLOW_attribute_in_function3003);
                    attribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(564,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:564:15: ( COMMA ( WS )* attribute )*
                    try { dbg.enterSubRule(105);

                    loop105:
                    do {
                        int alt105=2;
                        try { dbg.enterDecision(105, decisionCanBacktrack[105]);

                        int LA105_0 = input.LA(1);

                        if ( (LA105_0==COMMA) ) {
                            alt105=1;
                        }


                        } finally {dbg.exitDecision(105);}

                        switch (alt105) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:564:16: COMMA ( WS )* attribute
                    	    {
                    	    dbg.location(564,16);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function3006); if (state.failed) return ;
                    	    dbg.location(564,22);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:564:22: ( WS )*
                    	    try { dbg.enterSubRule(104);

                    	    loop104:
                    	    do {
                    	        int alt104=2;
                    	        try { dbg.enterDecision(104, decisionCanBacktrack[104]);

                    	        int LA104_0 = input.LA(1);

                    	        if ( (LA104_0==WS) ) {
                    	            alt104=1;
                    	        }


                    	        } finally {dbg.exitDecision(104);}

                    	        switch (alt104) {
                    	    	case 1 :
                    	    	    dbg.enterAlt(1);

                    	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:564:22: WS
                    	    	    {
                    	    	    dbg.location(564,22);
                    	    	    match(input,WS,FOLLOW_WS_in_function3008); if (state.failed) return ;

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop104;
                    	        }
                    	    } while (true);
                    	    } finally {dbg.exitSubRule(104);}

                    	    dbg.location(564,26);
                    	    pushFollow(FOLLOW_attribute_in_function3011);
                    	    attribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop105;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(105);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(106);}

            dbg.location(567,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function3032); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 

        }
        finally {
        }
        dbg.location(568, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:574:1: function_name : ( IDENT COLON )? IDENT ( DOT IDENT )* ;
    public final void function_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(574, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:2: ( ( IDENT COLON )? IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:4: ( IDENT COLON )? IDENT ( DOT IDENT )*
            {
            dbg.location(578,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:4: ( IDENT COLON )?
            int alt107=2;
            try { dbg.enterSubRule(107);
            try { dbg.enterDecision(107, decisionCanBacktrack[107]);

            int LA107_0 = input.LA(1);

            if ( (LA107_0==IDENT) ) {
                int LA107_1 = input.LA(2);

                if ( (LA107_1==COLON) ) {
                    alt107=1;
                }
            }
            } finally {dbg.exitDecision(107);}

            switch (alt107) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:5: IDENT COLON
                    {
                    dbg.location(578,5);
                    match(input,IDENT,FOLLOW_IDENT_in_function_name3080); if (state.failed) return ;
                    dbg.location(578,11);
                    match(input,COLON,FOLLOW_COLON_in_function_name3082); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(107);}

            dbg.location(578,19);
            match(input,IDENT,FOLLOW_IDENT_in_function_name3086); if (state.failed) return ;
            dbg.location(578,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:25: ( DOT IDENT )*
            try { dbg.enterSubRule(108);

            loop108:
            do {
                int alt108=2;
                try { dbg.enterDecision(108, decisionCanBacktrack[108]);

                int LA108_0 = input.LA(1);

                if ( (LA108_0==DOT) ) {
                    alt108=1;
                }


                } finally {dbg.exitDecision(108);}

                switch (alt108) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:26: DOT IDENT
            	    {
            	    dbg.location(578,26);
            	    match(input,DOT,FOLLOW_DOT_in_function_name3089); if (state.failed) return ;
            	    dbg.location(578,30);
            	    match(input,IDENT,FOLLOW_IDENT_in_function_name3091); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop108;
                }
            } while (true);
            } finally {dbg.exitSubRule(108);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(579, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:1: attribute : attrname ( WS )* OPEQ ( WS )* attrvalue ;
    public final void attribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(581, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:582:2: ( attrname ( WS )* OPEQ ( WS )* attrvalue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:582:4: attrname ( WS )* OPEQ ( WS )* attrvalue
            {
            dbg.location(582,4);
            pushFollow(FOLLOW_attrname_in_attribute3113);
            attrname();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(582,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:582:13: ( WS )*
            try { dbg.enterSubRule(109);

            loop109:
            do {
                int alt109=2;
                try { dbg.enterDecision(109, decisionCanBacktrack[109]);

                int LA109_0 = input.LA(1);

                if ( (LA109_0==WS) ) {
                    alt109=1;
                }


                } finally {dbg.exitDecision(109);}

                switch (alt109) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:582:13: WS
            	    {
            	    dbg.location(582,13);
            	    match(input,WS,FOLLOW_WS_in_attribute3115); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop109;
                }
            } while (true);
            } finally {dbg.exitSubRule(109);}

            dbg.location(582,17);
            match(input,OPEQ,FOLLOW_OPEQ_in_attribute3118); if (state.failed) return ;
            dbg.location(582,22);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:582:22: ( WS )*
            try { dbg.enterSubRule(110);

            loop110:
            do {
                int alt110=2;
                try { dbg.enterDecision(110, decisionCanBacktrack[110]);

                int LA110_0 = input.LA(1);

                if ( (LA110_0==WS) ) {
                    alt110=1;
                }


                } finally {dbg.exitDecision(110);}

                switch (alt110) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:582:22: WS
            	    {
            	    dbg.location(582,22);
            	    match(input,WS,FOLLOW_WS_in_attribute3120); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop110;
                }
            } while (true);
            } finally {dbg.exitSubRule(110);}

            dbg.location(582,26);
            pushFollow(FOLLOW_attrvalue_in_attribute3123);
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
        dbg.location(583, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:1: attrname : IDENT ;
    public final void attrname() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrname");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(585, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:4: IDENT
            {
            dbg.location(586,4);
            match(input,IDENT,FOLLOW_IDENT_in_attrname3138); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(587, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:1: attrvalue : expr ;
    public final void attrvalue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrvalue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(589, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:2: ( expr )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:4: expr
            {
            dbg.location(590,4);
            pushFollow(FOLLOW_expr_in_attrvalue3150);
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
        dbg.location(591, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(593, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:594:7: HASH
            {
            dbg.location(594,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor3168); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(595, 5);

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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:370:19: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:370:20: esPred
        {
        dbg.location(370,20);
        pushFollow(FOLLOW_esPred_in_synpred1_Css31586);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:6: esPred
        {
        dbg.location(372,6);
        pushFollow(FOLLOW_esPred_in_synpred2_Css31604);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:7: ( nsPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:8: nsPred
        {
        dbg.location(389,8);
        pushFollow(FOLLOW_nsPred_in_synpred3_Css31655);
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


    protected DFA74 dfa74 = new DFA74(this);
    protected DFA77 dfa77 = new DFA77(this);
    protected DFA85 dfa85 = new DFA85(this);
    protected DFA95 dfa95 = new DFA95(this);
    protected DFA98 dfa98 = new DFA98(this);
    protected DFA100 dfa100 = new DFA100(this);
    protected DFA106 dfa106 = new DFA106(this);
    static final String DFA74_eotS =
        "\27\uffff";
    static final String DFA74_eofS =
        "\27\uffff";
    static final String DFA74_minS =
        "\1\6\1\uffff\1\0\1\6\1\4\1\6\1\uffff\1\0\4\4\1\0\2\4\1\0\7\4";
    static final String DFA74_maxS =
        "\1\62\1\uffff\1\0\1\23\1\56\1\23\1\uffff\1\0\1\71\1\6\1\56\1\6\1"+
        "\0\1\71\1\7\1\0\1\71\1\56\1\6\1\7\3\71";
    static final String DFA74_acceptS =
        "\1\uffff\1\2\4\uffff\1\1\20\uffff";
    static final String DFA74_specialS =
        "\2\uffff\1\0\4\uffff\1\1\4\uffff\1\3\2\uffff\1\2\7\uffff}>";
    static final String[] DFA74_transitionS = {
            "\1\1\6\uffff\1\1\1\uffff\1\1\3\uffff\1\1\23\uffff\1\5\1\uffff"+
            "\3\1\1\uffff\2\1\1\2\1\3\1\4\1\5",
            "",
            "\1\uffff",
            "\1\7\14\uffff\1\7",
            "\1\13\1\uffff\1\10\46\uffff\1\12\1\11",
            "\1\14\14\uffff\1\14",
            "",
            "\1\uffff",
            "\1\15\51\uffff\1\11\4\uffff\6\16\1\17",
            "\1\13\1\uffff\1\20",
            "\1\21\51\uffff\1\22",
            "\1\13\1\uffff\1\20",
            "\1\uffff",
            "\1\15\51\uffff\1\11\4\uffff\6\16\1\17",
            "\1\23\1\uffff\2\24",
            "\1\uffff",
            "\1\25\56\uffff\6\16\1\17",
            "\1\21\51\uffff\1\22",
            "\1\13\1\uffff\1\20",
            "\1\23\1\uffff\2\24",
            "\1\26\64\uffff\1\17",
            "\1\25\56\uffff\6\16\1\17",
            "\1\26\64\uffff\1\17"
    };

    static final short[] DFA74_eot = DFA.unpackEncodedString(DFA74_eotS);
    static final short[] DFA74_eof = DFA.unpackEncodedString(DFA74_eofS);
    static final char[] DFA74_min = DFA.unpackEncodedStringToUnsignedChars(DFA74_minS);
    static final char[] DFA74_max = DFA.unpackEncodedStringToUnsignedChars(DFA74_maxS);
    static final short[] DFA74_accept = DFA.unpackEncodedString(DFA74_acceptS);
    static final short[] DFA74_special = DFA.unpackEncodedString(DFA74_specialS);
    static final short[][] DFA74_transition;

    static {
        int numStates = DFA74_transitionS.length;
        DFA74_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA74_transition[i] = DFA.unpackEncodedString(DFA74_transitionS[i]);
        }
    }

    class DFA74 extends DFA {

        public DFA74(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 74;
            this.eot = DFA74_eot;
            this.eof = DFA74_eof;
            this.min = DFA74_min;
            this.max = DFA74_max;
            this.accept = DFA74_accept;
            this.special = DFA74_special;
            this.transition = DFA74_transition;
        }
        public String getDescription() {
            return "()* loopback of 370:18: ( ( esPred )=> elementSubsequent )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA74_2 = input.LA(1);

                         
                        int index74_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index74_2);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA74_7 = input.LA(1);

                         
                        int index74_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index74_7);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA74_15 = input.LA(1);

                         
                        int index74_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index74_15);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA74_12 = input.LA(1);

                         
                        int index74_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 6;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index74_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 74, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA77_eotS =
        "\7\uffff";
    static final String DFA77_eofS =
        "\7\uffff";
    static final String DFA77_minS =
        "\1\6\1\0\1\uffff\1\4\1\uffff\1\4\1\0";
    static final String DFA77_maxS =
        "\1\56\1\0\1\uffff\1\62\1\uffff\1\62\1\0";
    static final String DFA77_acceptS =
        "\2\uffff\1\1\1\uffff\1\2\2\uffff";
    static final String DFA77_specialS =
        "\1\1\1\0\4\uffff\1\2}>";
    static final String[] DFA77_transitionS = {
            "\1\1\14\uffff\1\4\31\uffff\1\3\1\2",
            "\1\uffff",
            "",
            "\1\5\1\uffff\1\4\6\uffff\1\4\1\uffff\1\4\3\uffff\1\4\23\uffff"+
            "\1\4\1\uffff\3\4\1\uffff\1\4\1\6\4\4",
            "",
            "\1\5\1\uffff\1\4\6\uffff\1\4\1\uffff\1\4\3\uffff\1\4\23\uffff"+
            "\1\4\1\uffff\3\4\1\uffff\1\4\1\6\4\4",
            "\1\uffff"
    };

    static final short[] DFA77_eot = DFA.unpackEncodedString(DFA77_eotS);
    static final short[] DFA77_eof = DFA.unpackEncodedString(DFA77_eofS);
    static final char[] DFA77_min = DFA.unpackEncodedStringToUnsignedChars(DFA77_minS);
    static final char[] DFA77_max = DFA.unpackEncodedStringToUnsignedChars(DFA77_maxS);
    static final short[] DFA77_accept = DFA.unpackEncodedString(DFA77_acceptS);
    static final short[] DFA77_special = DFA.unpackEncodedString(DFA77_specialS);
    static final short[][] DFA77_transition;

    static {
        int numStates = DFA77_transitionS.length;
        DFA77_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA77_transition[i] = DFA.unpackEncodedString(DFA77_transitionS[i]);
        }
    }

    class DFA77 extends DFA {

        public DFA77(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 77;
            this.eot = DFA77_eot;
            this.eof = DFA77_eof;
            this.min = DFA77_min;
            this.max = DFA77_max;
            this.accept = DFA77_accept;
            this.special = DFA77_special;
            this.transition = DFA77_transition;
        }
        public String getDescription() {
            return "389:6: ( ( nsPred )=> namespace_wqname_prefix )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA77_1 = input.LA(1);

                         
                        int index77_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 2;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index77_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA77_0 = input.LA(1);

                         
                        int index77_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA77_0==IDENT) ) {s = 1;}

                        else if ( (LA77_0==PIPE) && (synpred3_Css3())) {s = 2;}

                        else if ( (LA77_0==STAR) ) {s = 3;}

                        else if ( (LA77_0==GEN) ) {s = 4;}

                         
                        input.seek(index77_0);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA77_6 = input.LA(1);

                         
                        int index77_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 2;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index77_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 77, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA85_eotS =
        "\5\uffff";
    static final String DFA85_eofS =
        "\5\uffff";
    static final String DFA85_minS =
        "\2\4\2\uffff\1\4";
    static final String DFA85_maxS =
        "\1\56\1\71\2\uffff\1\71";
    static final String DFA85_acceptS =
        "\2\uffff\1\1\1\2\1\uffff";
    static final String DFA85_specialS =
        "\5\uffff}>";
    static final String[] DFA85_transitionS = {
            "\1\3\1\uffff\1\1\46\uffff\2\2",
            "\1\4\51\uffff\1\2\4\uffff\7\3",
            "",
            "",
            "\1\4\51\uffff\1\2\4\uffff\7\3"
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
            return "443:6: ( namespace_wqname_prefix )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA95_eotS =
        "\4\uffff";
    static final String DFA95_eofS =
        "\4\uffff";
    static final String DFA95_minS =
        "\2\4\2\uffff";
    static final String DFA95_maxS =
        "\2\72\2\uffff";
    static final String DFA95_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA95_specialS =
        "\4\uffff}>";
    static final String[] DFA95_transitionS = {
            "\1\1\1\uffff\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\23\uffff"+
            "\1\3\1\uffff\3\3\1\uffff\6\3\7\uffff\1\2",
            "\1\1\1\uffff\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\23\uffff"+
            "\1\3\1\uffff\3\3\1\uffff\6\3\7\uffff\1\2",
            "",
            ""
    };

    static final short[] DFA95_eot = DFA.unpackEncodedString(DFA95_eotS);
    static final short[] DFA95_eof = DFA.unpackEncodedString(DFA95_eofS);
    static final char[] DFA95_min = DFA.unpackEncodedStringToUnsignedChars(DFA95_minS);
    static final char[] DFA95_max = DFA.unpackEncodedStringToUnsignedChars(DFA95_maxS);
    static final short[] DFA95_accept = DFA.unpackEncodedString(DFA95_acceptS);
    static final short[] DFA95_special = DFA.unpackEncodedString(DFA95_specialS);
    static final short[][] DFA95_transition;

    static {
        int numStates = DFA95_transitionS.length;
        DFA95_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA95_transition[i] = DFA.unpackEncodedString(DFA95_transitionS[i]);
        }
    }

    class DFA95 extends DFA {

        public DFA95(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 95;
            this.eot = DFA95_eot;
            this.eof = DFA95_eof;
            this.min = DFA95_min;
            this.max = DFA95_max;
            this.accept = DFA95_accept;
            this.special = DFA95_special;
            this.transition = DFA95_transition;
        }
        public String getDescription() {
            return "490:17: ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA98_eotS =
        "\7\uffff";
    static final String DFA98_eofS =
        "\7\uffff";
    static final String DFA98_minS =
        "\1\6\1\uffff\1\4\1\uffff\3\4";
    static final String DFA98_maxS =
        "\1\105\1\uffff\1\105\1\uffff\3\105";
    static final String DFA98_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\3\uffff";
    static final String DFA98_specialS =
        "\7\uffff}>";
    static final String[] DFA98_transitionS = {
            "\3\3\1\uffff\1\1\3\uffff\1\1\1\2\3\uffff\1\3\24\uffff\2\3\2"+
            "\uffff\1\3\2\uffff\1\3\13\uffff\2\1\11\3",
            "",
            "\1\4\1\uffff\1\5\2\3\12\uffff\1\3\25\uffff\1\3\2\uffff\1\3"+
            "\2\uffff\1\3\15\uffff\11\3",
            "",
            "\1\4\1\uffff\1\5\2\3\12\uffff\1\3\25\uffff\1\3\2\uffff\1\3"+
            "\2\uffff\1\3\15\uffff\11\3",
            "\1\6\1\uffff\3\3\1\uffff\1\3\3\uffff\2\3\3\uffff\1\3\23\uffff"+
            "\3\3\2\uffff\1\3\2\uffff\2\3\2\uffff\1\1\6\uffff\14\3",
            "\1\6\1\uffff\3\3\1\uffff\1\3\3\uffff\2\3\3\uffff\1\3\24\uffff"+
            "\2\3\2\uffff\1\3\2\uffff\1\3\3\uffff\1\1\6\uffff\14\3"
    };

    static final short[] DFA98_eot = DFA.unpackEncodedString(DFA98_eotS);
    static final short[] DFA98_eof = DFA.unpackEncodedString(DFA98_eofS);
    static final char[] DFA98_min = DFA.unpackEncodedStringToUnsignedChars(DFA98_minS);
    static final char[] DFA98_max = DFA.unpackEncodedStringToUnsignedChars(DFA98_maxS);
    static final short[] DFA98_accept = DFA.unpackEncodedString(DFA98_acceptS);
    static final short[] DFA98_special = DFA.unpackEncodedString(DFA98_specialS);
    static final short[][] DFA98_transition;

    static {
        int numStates = DFA98_transitionS.length;
        DFA98_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA98_transition[i] = DFA.unpackEncodedString(DFA98_transitionS[i]);
        }
    }

    class DFA98 extends DFA {

        public DFA98(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 98;
            this.eot = DFA98_eot;
            this.eof = DFA98_eof;
            this.min = DFA98_min;
            this.max = DFA98_max;
            this.accept = DFA98_accept;
            this.special = DFA98_special;
            this.transition = DFA98_transition;
        }
        public String getDescription() {
            return "()* loopback of 530:12: ( operator term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA100_eotS =
        "\12\uffff";
    static final String DFA100_eofS =
        "\12\uffff";
    static final String DFA100_minS =
        "\1\6\2\uffff\1\4\4\uffff\1\4\1\uffff";
    static final String DFA100_maxS =
        "\1\105\2\uffff\1\105\4\uffff\1\105\1\uffff";
    static final String DFA100_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\7\1\uffff\1\3";
    static final String DFA100_specialS =
        "\12\uffff}>";
    static final String[] DFA100_transitionS = {
            "\1\3\1\2\1\5\12\uffff\1\4\33\uffff\1\6\15\uffff\11\1",
            "",
            "",
            "\1\10\1\uffff\3\11\1\uffff\1\11\3\uffff\2\11\3\uffff\1\11\23"+
            "\uffff\1\7\2\11\2\uffff\1\11\2\uffff\1\11\1\7\11\uffff\1\7\13"+
            "\11",
            "",
            "",
            "",
            "",
            "\1\10\1\uffff\3\11\1\uffff\1\11\3\uffff\2\11\3\uffff\1\11\24"+
            "\uffff\2\11\2\uffff\1\11\2\uffff\1\11\12\uffff\1\7\13\11",
            ""
    };

    static final short[] DFA100_eot = DFA.unpackEncodedString(DFA100_eotS);
    static final short[] DFA100_eof = DFA.unpackEncodedString(DFA100_eofS);
    static final char[] DFA100_min = DFA.unpackEncodedStringToUnsignedChars(DFA100_minS);
    static final char[] DFA100_max = DFA.unpackEncodedStringToUnsignedChars(DFA100_maxS);
    static final short[] DFA100_accept = DFA.unpackEncodedString(DFA100_acceptS);
    static final short[] DFA100_special = DFA.unpackEncodedString(DFA100_specialS);
    static final short[][] DFA100_transition;

    static {
        int numStates = DFA100_transitionS.length;
        DFA100_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA100_transition[i] = DFA.unpackEncodedString(DFA100_transitionS[i]);
        }
    }

    class DFA100 extends DFA {

        public DFA100(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 100;
            this.eot = DFA100_eot;
            this.eof = DFA100_eof;
            this.min = DFA100_min;
            this.max = DFA100_max;
            this.accept = DFA100_accept;
            this.special = DFA100_special;
            this.transition = DFA100_transition;
        }
        public String getDescription() {
            return "535:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION ) | STRING | IDENT | GEN | URI | hexColor | function )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA106_eotS =
        "\5\uffff";
    static final String DFA106_eofS =
        "\5\uffff";
    static final String DFA106_minS =
        "\1\6\1\uffff\2\4\1\uffff";
    static final String DFA106_maxS =
        "\1\105\1\uffff\2\105\1\uffff";
    static final String DFA106_acceptS =
        "\1\uffff\1\1\2\uffff\1\2";
    static final String DFA106_specialS =
        "\5\uffff}>";
    static final String[] DFA106_transitionS = {
            "\1\2\2\1\12\uffff\1\1\25\uffff\1\1\2\uffff\1\1\2\uffff\1\1\15"+
            "\uffff\11\1",
            "",
            "\1\3\1\uffff\3\1\6\uffff\1\1\3\uffff\1\1\23\uffff\3\1\2\uffff"+
            "\1\1\2\uffff\2\1\2\uffff\1\4\6\uffff\2\1\1\uffff\11\1",
            "\1\3\1\uffff\3\1\6\uffff\1\1\3\uffff\1\1\24\uffff\2\1\2\uffff"+
            "\1\1\2\uffff\1\1\3\uffff\1\4\6\uffff\2\1\1\uffff\11\1",
            ""
    };

    static final short[] DFA106_eot = DFA.unpackEncodedString(DFA106_eotS);
    static final short[] DFA106_eof = DFA.unpackEncodedString(DFA106_eofS);
    static final char[] DFA106_min = DFA.unpackEncodedStringToUnsignedChars(DFA106_minS);
    static final char[] DFA106_max = DFA.unpackEncodedStringToUnsignedChars(DFA106_maxS);
    static final short[] DFA106_accept = DFA.unpackEncodedString(DFA106_acceptS);
    static final short[] DFA106_special = DFA.unpackEncodedString(DFA106_specialS);
    static final short[][] DFA106_transition;

    static {
        int numStates = DFA106_transitionS.length;
        DFA106_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA106_transition[i] = DFA.unpackEncodedString(DFA106_transitionS[i]);
        }
    }

    class DFA106 extends DFA {

        public DFA106(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 106;
            this.eot = DFA106_eot;
            this.eof = DFA106_eof;
            this.min = DFA106_min;
            this.max = DFA106_max;
            this.accept = DFA106_accept;
            this.special = DFA106_special;
            this.transition = DFA106_transition;
        }
        public String getDescription() {
            return "560:3: ( expr | ( attribute ( COMMA ( WS )* attribute )* ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_charSet_in_styleSheet119 = new BitSet(new long[]{0x0007E08000781870L});
    public static final BitSet FOLLOW_WS_in_styleSheet127 = new BitSet(new long[]{0x0007E08000781870L});
    public static final BitSet FOLLOW_imports_in_styleSheet139 = new BitSet(new long[]{0x0007E08000781870L});
    public static final BitSet FOLLOW_WS_in_styleSheet141 = new BitSet(new long[]{0x0007E08000781870L});
    public static final BitSet FOLLOW_namespace_in_styleSheet156 = new BitSet(new long[]{0x0007E08000781060L});
    public static final BitSet FOLLOW_bodylist_in_styleSheet167 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_styleSheet174 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAMESPACE_SYM_in_namespace189 = new BitSet(new long[]{0x00000000000001D0L});
    public static final BitSet FOLLOW_WS_in_namespace191 = new BitSet(new long[]{0x00000000000001D0L});
    public static final BitSet FOLLOW_namespace_prefix_in_namespace195 = new BitSet(new long[]{0x00000000000001D0L});
    public static final BitSet FOLLOW_WS_in_namespace197 = new BitSet(new long[]{0x00000000000001D0L});
    public static final BitSet FOLLOW_resourceIdentifier_in_namespace203 = new BitSet(new long[]{0x0000000000000410L});
    public static final BitSet FOLLOW_WS_in_namespace206 = new BitSet(new long[]{0x0000000000000410L});
    public static final BitSet FOLLOW_SEMI_in_namespace209 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_namespace211 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_IDENT_in_namespace_prefix225 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_resourceIdentifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHARSET_SYM_in_charSet264 = new BitSet(new long[]{0x0000000000000090L});
    public static final BitSet FOLLOW_WS_in_charSet266 = new BitSet(new long[]{0x0000000000000090L});
    public static final BitSet FOLLOW_STRING_in_charSet269 = new BitSet(new long[]{0x0000000000000410L});
    public static final BitSet FOLLOW_WS_in_charSet271 = new BitSet(new long[]{0x0000000000000410L});
    public static final BitSet FOLLOW_SEMI_in_charSet274 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORT_SYM_in_imports296 = new BitSet(new long[]{0x00000000000001D0L});
    public static final BitSet FOLLOW_WS_in_imports298 = new BitSet(new long[]{0x00000000000001D0L});
    public static final BitSet FOLLOW_resourceIdentifier_in_imports302 = new BitSet(new long[]{0x04000000000B0450L});
    public static final BitSet FOLLOW_WS_in_imports305 = new BitSet(new long[]{0x04000000000B0450L});
    public static final BitSet FOLLOW_media_query_list_in_imports308 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_SEMI_in_imports310 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEDIA_SYM_in_media331 = new BitSet(new long[]{0x04000000000B2050L});
    public static final BitSet FOLLOW_WS_in_media333 = new BitSet(new long[]{0x04000000000B2050L});
    public static final BitSet FOLLOW_media_query_list_in_media336 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_media346 = new BitSet(new long[]{0x0007E08000184050L});
    public static final BitSet FOLLOW_WS_in_media348 = new BitSet(new long[]{0x0007E08000184050L});
    public static final BitSet FOLLOW_ruleSet_in_media367 = new BitSet(new long[]{0x0007E08000184050L});
    public static final BitSet FOLLOW_page_in_media371 = new BitSet(new long[]{0x0007E08000184050L});
    public static final BitSet FOLLOW_WS_in_media375 = new BitSet(new long[]{0x0007E08000184050L});
    public static final BitSet FOLLOW_RBRACE_in_media389 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_media_query_in_media_query_list409 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_COMMA_in_media_query_list413 = new BitSet(new long[]{0x04000000000B0050L});
    public static final BitSet FOLLOW_WS_in_media_query_list415 = new BitSet(new long[]{0x04000000000B0050L});
    public static final BitSet FOLLOW_media_query_in_media_query_list418 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_set_in_media_query437 = new BitSet(new long[]{0x00000000000B0050L});
    public static final BitSet FOLLOW_WS_in_media_query445 = new BitSet(new long[]{0x00000000000B0050L});
    public static final BitSet FOLLOW_media_type_in_media_query452 = new BitSet(new long[]{0x0000000000040012L});
    public static final BitSet FOLLOW_WS_in_media_query454 = new BitSet(new long[]{0x0000000000040012L});
    public static final BitSet FOLLOW_AND_in_media_query459 = new BitSet(new long[]{0x04000000000B0050L});
    public static final BitSet FOLLOW_WS_in_media_query461 = new BitSet(new long[]{0x04000000000B0050L});
    public static final BitSet FOLLOW_media_expression_in_media_query464 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_media_expression_in_media_query472 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_AND_in_media_query476 = new BitSet(new long[]{0x04000000000B0050L});
    public static final BitSet FOLLOW_WS_in_media_query478 = new BitSet(new long[]{0x04000000000B0050L});
    public static final BitSet FOLLOW_media_expression_in_media_query481 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_set_in_media_type0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_media_expression512 = new BitSet(new long[]{0x0000000000000050L});
    public static final BitSet FOLLOW_WS_in_media_expression514 = new BitSet(new long[]{0x0000000000000050L});
    public static final BitSet FOLLOW_media_feature_in_media_expression517 = new BitSet(new long[]{0x0800008000000010L});
    public static final BitSet FOLLOW_WS_in_media_expression519 = new BitSet(new long[]{0x0800008000000010L});
    public static final BitSet FOLLOW_COLON_in_media_expression524 = new BitSet(new long[]{0xE0009200000801D0L,0x000000000000003FL});
    public static final BitSet FOLLOW_WS_in_media_expression526 = new BitSet(new long[]{0xE0009200000801D0L,0x000000000000003FL});
    public static final BitSet FOLLOW_expr_in_media_expression529 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_media_expression534 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_media_expression536 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_IDENT_in_media_feature547 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_medium564 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_medium574 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_bodyset_in_bodylist597 = new BitSet(new long[]{0x0007E08000781042L});
    public static final BitSet FOLLOW_ruleSet_in_bodyset626 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_media_in_bodyset638 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_page_in_bodyset650 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_counterStyle_in_bodyset662 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_fontFace_in_bodyset674 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_bodyset690 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page712 = new BitSet(new long[]{0x0000008000002050L});
    public static final BitSet FOLLOW_WS_in_page714 = new BitSet(new long[]{0x0000008000002040L});
    public static final BitSet FOLLOW_IDENT_in_page717 = new BitSet(new long[]{0x0000008000002000L});
    public static final BitSet FOLLOW_pseudoPage_in_page721 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_WS_in_page723 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_LBRACE_in_page736 = new BitSet(new long[]{0x0000007FFF884450L});
    public static final BitSet FOLLOW_WS_in_page738 = new BitSet(new long[]{0x0000007FFF884450L});
    public static final BitSet FOLLOW_declaration_in_page806 = new BitSet(new long[]{0x0000000000004400L});
    public static final BitSet FOLLOW_margin_in_page808 = new BitSet(new long[]{0x0000000000004410L});
    public static final BitSet FOLLOW_WS_in_page810 = new BitSet(new long[]{0x0000000000004410L});
    public static final BitSet FOLLOW_SEMI_in_page816 = new BitSet(new long[]{0x0000007FFF884450L});
    public static final BitSet FOLLOW_WS_in_page818 = new BitSet(new long[]{0x0000007FFF884450L});
    public static final BitSet FOLLOW_declaration_in_page822 = new BitSet(new long[]{0x0000000000004400L});
    public static final BitSet FOLLOW_margin_in_page824 = new BitSet(new long[]{0x0000000000004410L});
    public static final BitSet FOLLOW_WS_in_page826 = new BitSet(new long[]{0x0000000000004410L});
    public static final BitSet FOLLOW_RBRACE_in_page841 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNTER_STYLE_SYM_in_counterStyle862 = new BitSet(new long[]{0x0000000000000050L});
    public static final BitSet FOLLOW_WS_in_counterStyle864 = new BitSet(new long[]{0x0000000000000050L});
    public static final BitSet FOLLOW_IDENT_in_counterStyle867 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_WS_in_counterStyle869 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_LBRACE_in_counterStyle880 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_WS_in_counterStyle882 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_counterStyle885 = new BitSet(new long[]{0x0000000000084440L});
    public static final BitSet FOLLOW_declarations_in_counterStyle889 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_counterStyle899 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FONT_FACE_SYM_in_fontFace920 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_WS_in_fontFace922 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_LBRACE_in_fontFace933 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_WS_in_fontFace935 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_fontFace938 = new BitSet(new long[]{0x0000000000084440L});
    public static final BitSet FOLLOW_declarations_in_fontFace942 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_fontFace952 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_margin_sym_in_margin972 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_WS_in_margin974 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_LBRACE_in_margin977 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_WS_in_margin979 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_margin982 = new BitSet(new long[]{0x0000000000084440L});
    public static final BitSet FOLLOW_declarations_in_margin984 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_margin986 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_margin_sym0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage1215 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage1217 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_operator1238 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_operator1240 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_COMMA_in_operator1249 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_operator1251 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_PLUS_in_combinator1279 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator1281 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_GREATER_in_combinator1290 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator1292 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_TILDE_in_combinator1301 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator1303 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_property1363 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_property1371 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_selectorsGroup_in_ruleSet1396 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_ruleSet1406 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_WS_in_ruleSet1408 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_ruleSet1411 = new BitSet(new long[]{0x0000000000084440L});
    public static final BitSet FOLLOW_declarations_in_ruleSet1425 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_ruleSet1435 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_declarations1473 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_SEMI_in_declarations1477 = new BitSet(new long[]{0x0000000000080452L});
    public static final BitSet FOLLOW_WS_in_declarations1479 = new BitSet(new long[]{0x0000000000080452L});
    public static final BitSet FOLLOW_declaration_in_declarations1482 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup1506 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup1509 = new BitSet(new long[]{0x0007E08000080050L});
    public static final BitSet FOLLOW_WS_in_selectorsGroup1511 = new BitSet(new long[]{0x0007E08000080050L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup1514 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector1537 = new BitSet(new long[]{0x0007EE8000080042L});
    public static final BitSet FOLLOW_combinator_in_selector1540 = new BitSet(new long[]{0x0007E08000080040L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector1542 = new BitSet(new long[]{0x0007EE8000080042L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence1582 = new BitSet(new long[]{0x0007E08000080042L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence1589 = new BitSet(new long[]{0x0007E08000080042L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence1607 = new BitSet(new long[]{0x0007E08000080042L});
    public static final BitSet FOLLOW_namespace_wqname_prefix_in_typeSelector1658 = new BitSet(new long[]{0x0000600000080040L});
    public static final BitSet FOLLOW_elementName_in_typeSelector1664 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_typeSelector1666 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_set_in_nsPred1691 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_PIPE_in_nsPred1699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespace_prefix_in_namespace_wqname_prefix1729 = new BitSet(new long[]{0x0000400000000010L});
    public static final BitSet FOLLOW_WS_in_namespace_wqname_prefix1731 = new BitSet(new long[]{0x0000400000000010L});
    public static final BitSet FOLLOW_PIPE_in_namespace_wqname_prefix1737 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespace_wildcard_prefix_in_namespace_wqname_prefix1744 = new BitSet(new long[]{0x0000400000000010L});
    public static final BitSet FOLLOW_WS_in_namespace_wqname_prefix1746 = new BitSet(new long[]{0x0000400000000010L});
    public static final BitSet FOLLOW_PIPE_in_namespace_wqname_prefix1749 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_namespace_wildcard_prefix1771 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent1844 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent1853 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_attrib_in_elementSubsequent1865 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent1877 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_elementSubsequent1889 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_HASH_in_cssId1911 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass1928 = new BitSet(new long[]{0x0000000000080040L});
    public static final BitSet FOLLOW_set_in_cssClass1930 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_attrib1994 = new BitSet(new long[]{0x0000600000000050L});
    public static final BitSet FOLLOW_namespace_wqname_prefix_in_attrib2001 = new BitSet(new long[]{0x0000600000000050L});
    public static final BitSet FOLLOW_WS_in_attrib2004 = new BitSet(new long[]{0x0000600000000050L});
    public static final BitSet FOLLOW_attrib_name_in_attrib2015 = new BitSet(new long[]{0x03F8000000000010L});
    public static final BitSet FOLLOW_WS_in_attrib2017 = new BitSet(new long[]{0x03F8000000000010L});
    public static final BitSet FOLLOW_set_in_attrib2059 = new BitSet(new long[]{0x00000000000000D0L});
    public static final BitSet FOLLOW_WS_in_attrib2239 = new BitSet(new long[]{0x00000000000000D0L});
    public static final BitSet FOLLOW_attrib_value_in_attrib2258 = new BitSet(new long[]{0x0200000000000010L});
    public static final BitSet FOLLOW_WS_in_attrib2276 = new BitSet(new long[]{0x0200000000000010L});
    public static final BitSet FOLLOW_RBRACKET_in_attrib2305 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_attrib_name2348 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_attrib_value2362 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pseudo2422 = new BitSet(new long[]{0x0000000000080040L});
    public static final BitSet FOLLOW_set_in_pseudo2444 = new BitSet(new long[]{0x0400000000000012L});
    public static final BitSet FOLLOW_WS_in_pseudo2493 = new BitSet(new long[]{0x0400000000000010L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo2496 = new BitSet(new long[]{0x0800000000080050L});
    public static final BitSet FOLLOW_WS_in_pseudo2498 = new BitSet(new long[]{0x0800000000080050L});
    public static final BitSet FOLLOW_set_in_pseudo2502 = new BitSet(new long[]{0x0800000000000010L});
    public static final BitSet FOLLOW_WS_in_pseudo2512 = new BitSet(new long[]{0x0800000000000010L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo2517 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_declaration2563 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_COLON_in_declaration2565 = new BitSet(new long[]{0xE0009200000801D0L,0x000000000000003FL});
    public static final BitSet FOLLOW_WS_in_declaration2567 = new BitSet(new long[]{0xE0009200000801D0L,0x000000000000003FL});
    public static final BitSet FOLLOW_expr_in_declaration2570 = new BitSet(new long[]{0x1000000000000002L});
    public static final BitSet FOLLOW_prio_in_declaration2572 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio2665 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expr2686 = new BitSet(new long[]{0xE0009300000881D2L,0x000000000000003FL});
    public static final BitSet FOLLOW_operator_in_expr2689 = new BitSet(new long[]{0xE0009200000801D0L,0x000000000000003FL});
    public static final BitSet FOLLOW_term_in_expr2691 = new BitSet(new long[]{0xE0009300000881D2L,0x000000000000003FL});
    public static final BitSet FOLLOW_unaryOperator_in_term2714 = new BitSet(new long[]{0xE0009200000801D0L,0x000000000000003FL});
    public static final BitSet FOLLOW_set_in_term2735 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_STRING_in_term2897 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_IDENT_in_term2905 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_GEN_in_term2913 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_URI_in_term2921 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_hexColor_in_term2929 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_function_in_term2937 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_term2949 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_function_name_in_function2965 = new BitSet(new long[]{0x0400000000000010L});
    public static final BitSet FOLLOW_WS_in_function2967 = new BitSet(new long[]{0x0400000000000010L});
    public static final BitSet FOLLOW_LPAREN_in_function2972 = new BitSet(new long[]{0xE0009200000801D0L,0x000000000000003FL});
    public static final BitSet FOLLOW_WS_in_function2974 = new BitSet(new long[]{0xE0009200000801D0L,0x000000000000003FL});
    public static final BitSet FOLLOW_expr_in_function2985 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_attribute_in_function3003 = new BitSet(new long[]{0x0800000000008000L});
    public static final BitSet FOLLOW_COMMA_in_function3006 = new BitSet(new long[]{0xE0009200000801D0L,0x000000000000003FL});
    public static final BitSet FOLLOW_WS_in_function3008 = new BitSet(new long[]{0xE0009200000801D0L,0x000000000000003FL});
    public static final BitSet FOLLOW_attribute_in_function3011 = new BitSet(new long[]{0x0800000000008000L});
    public static final BitSet FOLLOW_RPAREN_in_function3032 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_function_name3080 = new BitSet(new long[]{0x0000008000000000L});
    public static final BitSet FOLLOW_COLON_in_function_name3082 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_function_name3086 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_DOT_in_function_name3089 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_function_name3091 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_attrname_in_attribute3113 = new BitSet(new long[]{0x0008000000000010L});
    public static final BitSet FOLLOW_WS_in_attribute3115 = new BitSet(new long[]{0x0008000000000010L});
    public static final BitSet FOLLOW_OPEQ_in_attribute3118 = new BitSet(new long[]{0xE0009200000801D0L,0x000000000000003FL});
    public static final BitSet FOLLOW_WS_in_attribute3120 = new BitSet(new long[]{0xE0009200000801D0L,0x000000000000003FL});
    public static final BitSet FOLLOW_attrvalue_in_attribute3123 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_attrname3138 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_in_attrvalue3150 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor3168 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred1_Css31586 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred2_Css31604 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nsPred_in_synpred3_Css31655 = new BitSet(new long[]{0x0000000000000002L});

}