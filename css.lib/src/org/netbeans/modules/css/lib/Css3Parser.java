// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2011-10-28 21:09:18

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
import org.antlr.runtime.debug.*;
import java.io.IOException;
public class Css3Parser extends DebugParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "WS", "NAMESPACE_SYM", "IDENT", "STRING", "URI", "CHARSET_SYM", "SEMI", "IMPORT_SYM", "MEDIA_SYM", "LBRACE", "RBRACE", "COMMA", "ONLY", "NOT", "AND", "GEN", "MOZ_DOCUMENT_SYM", "MOZ_URL_PREFIX", "MOZ_DOMAIN", "MOZ_REGEXP", "PAGE_SYM", "COUNTER_STYLE_SYM", "FONT_FACE_SYM", "TOPLEFTCORNER_SYM", "TOPLEFT_SYM", "TOPCENTER_SYM", "TOPRIGHT_SYM", "TOPRIGHTCORNER_SYM", "BOTTOMLEFTCORNER_SYM", "BOTTOMLEFT_SYM", "BOTTOMCENTER_SYM", "BOTTOMRIGHT_SYM", "BOTTOMRIGHTCORNER_SYM", "LEFTTOP_SYM", "LEFTMIDDLE_SYM", "LEFTBOTTOM_SYM", "RIGHTTOP_SYM", "RIGHTMIDDLE_SYM", "RIGHTBOTTOM_SYM", "COLON", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "STAR", "PIPE", "HASH", "DOT", "LBRACKET", "DCOLON", "NAME", "OPEQ", "INCLUDES", "DASHMATCH", "BEGINS", "ENDS", "CONTAINS", "RBRACKET", "LPAREN", "RPAREN", "IMPORTANT_SYM", "NUMBER", "PERCENTAGE", "LENGTH", "EMS", "EXS", "ANGLE", "TIME", "FREQ", "RESOLUTION", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "COMMENT", "CDO", "CDC", "INVALID", "DIMENSION", "NL", "'#'"
    };
    public static final int EOF=-1;
    public static final int T__114=114;
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
    public static final int MOZ_DOCUMENT_SYM=20;
    public static final int MOZ_URL_PREFIX=21;
    public static final int MOZ_DOMAIN=22;
    public static final int MOZ_REGEXP=23;
    public static final int PAGE_SYM=24;
    public static final int COUNTER_STYLE_SYM=25;
    public static final int FONT_FACE_SYM=26;
    public static final int TOPLEFTCORNER_SYM=27;
    public static final int TOPLEFT_SYM=28;
    public static final int TOPCENTER_SYM=29;
    public static final int TOPRIGHT_SYM=30;
    public static final int TOPRIGHTCORNER_SYM=31;
    public static final int BOTTOMLEFTCORNER_SYM=32;
    public static final int BOTTOMLEFT_SYM=33;
    public static final int BOTTOMCENTER_SYM=34;
    public static final int BOTTOMRIGHT_SYM=35;
    public static final int BOTTOMRIGHTCORNER_SYM=36;
    public static final int LEFTTOP_SYM=37;
    public static final int LEFTMIDDLE_SYM=38;
    public static final int LEFTBOTTOM_SYM=39;
    public static final int RIGHTTOP_SYM=40;
    public static final int RIGHTMIDDLE_SYM=41;
    public static final int RIGHTBOTTOM_SYM=42;
    public static final int COLON=43;
    public static final int SOLIDUS=44;
    public static final int PLUS=45;
    public static final int GREATER=46;
    public static final int TILDE=47;
    public static final int MINUS=48;
    public static final int STAR=49;
    public static final int PIPE=50;
    public static final int HASH=51;
    public static final int DOT=52;
    public static final int LBRACKET=53;
    public static final int DCOLON=54;
    public static final int NAME=55;
    public static final int OPEQ=56;
    public static final int INCLUDES=57;
    public static final int DASHMATCH=58;
    public static final int BEGINS=59;
    public static final int ENDS=60;
    public static final int CONTAINS=61;
    public static final int RBRACKET=62;
    public static final int LPAREN=63;
    public static final int RPAREN=64;
    public static final int IMPORTANT_SYM=65;
    public static final int NUMBER=66;
    public static final int PERCENTAGE=67;
    public static final int LENGTH=68;
    public static final int EMS=69;
    public static final int EXS=70;
    public static final int ANGLE=71;
    public static final int TIME=72;
    public static final int FREQ=73;
    public static final int RESOLUTION=74;
    public static final int HEXCHAR=75;
    public static final int NONASCII=76;
    public static final int UNICODE=77;
    public static final int ESCAPE=78;
    public static final int NMSTART=79;
    public static final int NMCHAR=80;
    public static final int URL=81;
    public static final int A=82;
    public static final int B=83;
    public static final int C=84;
    public static final int D=85;
    public static final int E=86;
    public static final int F=87;
    public static final int G=88;
    public static final int H=89;
    public static final int I=90;
    public static final int J=91;
    public static final int K=92;
    public static final int L=93;
    public static final int M=94;
    public static final int N=95;
    public static final int O=96;
    public static final int P=97;
    public static final int Q=98;
    public static final int R=99;
    public static final int S=100;
    public static final int T=101;
    public static final int U=102;
    public static final int V=103;
    public static final int W=104;
    public static final int X=105;
    public static final int Y=106;
    public static final int Z=107;
    public static final int COMMENT=108;
    public static final int CDO=109;
    public static final int CDC=110;
    public static final int INVALID=111;
    public static final int DIMENSION=112;
    public static final int NL=113;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "function_name", "selector", "syncTo_IDENT_RBRACE", 
        "resourceIdentifier", "attrib_name", "namespace_prefix", "selectorsGroup", 
        "attrvalue", "expr", "media_query_list", "synpred2_Css3", "combinator", 
        "margin", "moz_document", "cssClass", "media_type", "elementSubsequent", 
        "synpred1_Css3", "imports", "prio", "margin_sym", "namespace", "typeSelector", 
        "synpred3_Css3", "attrname", "styleSheet", "media", "media_feature", 
        "bodylist", "media_query", "property", "unaryOperator", "media_expression", 
        "simpleSelectorSequence", "moz_document_function", "declarations", 
        "operator", "syncTo_IDENT_RBRACKET_LBRACE", "medium", "page", "declaration", 
        "hexColor", "pseudo", "cssId", "namespace_wqname_prefix", "pseudoPage", 
        "counterStyle", "term", "namespace_wildcard_prefix", "ruleSet", 
        "fontFace", "esPred", "syncToFollow", "bodyset", "attrib_value", 
        "elementName", "function", "attribute", "charSet", "nsPred", "attrib"
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
            false, false, false, false, false, false, false, false, false, 
            false, true, true, false, true, false, false, false, false, 
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:255:1: styleSheet : ( charSet )? ( WS )* ( imports ( WS )* )* ( namespace )* bodylist EOF ;
    public final void styleSheet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "styleSheet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(255, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:256:5: ( ( charSet )? ( WS )* ( imports ( WS )* )* ( namespace )* bodylist EOF )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:256:9: ( charSet )? ( WS )* ( imports ( WS )* )* ( namespace )* bodylist EOF
            {
            dbg.location(256,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:256:9: ( charSet )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:256:9: charSet
                    {
                    dbg.location(256,9);
                    pushFollow(FOLLOW_charSet_in_styleSheet119);
                    charSet();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(1);}

            dbg.location(257,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:257:6: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:257:6: WS
            	    {
            	    dbg.location(257,6);
            	    match(input,WS,FOLLOW_WS_in_styleSheet127); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);
            } finally {dbg.exitSubRule(2);}

            dbg.location(258,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:258:9: ( imports ( WS )* )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:258:10: imports ( WS )*
            	    {
            	    dbg.location(258,10);
            	    pushFollow(FOLLOW_imports_in_styleSheet139);
            	    imports();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(258,18);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:258:18: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:258:18: WS
            	    	    {
            	    	    dbg.location(258,18);
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

            dbg.location(259,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:259:9: ( namespace )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:259:9: namespace
            	    {
            	    dbg.location(259,9);
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

            dbg.location(260,9);
            pushFollow(FOLLOW_bodylist_in_styleSheet167);
            bodylist();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(261,6);
            match(input,EOF,FOLLOW_EOF_in_styleSheet174); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "styleSheet");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "styleSheet"


    // $ANTLR start "namespace"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:264:1: namespace : NAMESPACE_SYM ( WS )* ( namespace_prefix ( WS )* )? ( resourceIdentifier ) ( WS )* ';' ( WS )* ;
    public final void namespace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(264, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:3: ( NAMESPACE_SYM ( WS )* ( namespace_prefix ( WS )* )? ( resourceIdentifier ) ( WS )* ';' ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:5: NAMESPACE_SYM ( WS )* ( namespace_prefix ( WS )* )? ( resourceIdentifier ) ( WS )* ';' ( WS )*
            {
            dbg.location(265,5);
            match(input,NAMESPACE_SYM,FOLLOW_NAMESPACE_SYM_in_namespace189); if (state.failed) return ;
            dbg.location(265,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:19: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:19: WS
            	    {
            	    dbg.location(265,19);
            	    match(input,WS,FOLLOW_WS_in_namespace191); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);
            } finally {dbg.exitSubRule(6);}

            dbg.location(265,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:23: ( namespace_prefix ( WS )* )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:24: namespace_prefix ( WS )*
                    {
                    dbg.location(265,24);
                    pushFollow(FOLLOW_namespace_prefix_in_namespace195);
                    namespace_prefix();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(265,41);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:41: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:41: WS
                    	    {
                    	    dbg.location(265,41);
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

            dbg.location(265,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:47: ( resourceIdentifier )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:48: resourceIdentifier
            {
            dbg.location(265,48);
            pushFollow(FOLLOW_resourceIdentifier_in_namespace203);
            resourceIdentifier();

            state._fsp--;
            if (state.failed) return ;

            }

            dbg.location(265,68);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:68: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:68: WS
            	    {
            	    dbg.location(265,68);
            	    match(input,WS,FOLLOW_WS_in_namespace206); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);
            } finally {dbg.exitSubRule(9);}

            dbg.location(265,72);
            match(input,SEMI,FOLLOW_SEMI_in_namespace209); if (state.failed) return ;
            dbg.location(265,76);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:76: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:265:76: WS
            	    {
            	    dbg.location(265,76);
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
        dbg.location(266, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:268:1: namespace_prefix : IDENT ;
    public final void namespace_prefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace_prefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(268, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:269:3: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:269:5: IDENT
            {
            dbg.location(269,5);
            match(input,IDENT,FOLLOW_IDENT_in_namespace_prefix225); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(270, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:272:1: resourceIdentifier : ( STRING | URI );
    public final void resourceIdentifier() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "resourceIdentifier");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(272, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:273:3: ( STRING | URI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(273,3);
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
        dbg.location(274, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:279:1: charSet : CHARSET_SYM ( WS )* STRING ( WS )* SEMI ;
    public final void charSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "charSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(279, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:280:5: ( CHARSET_SYM ( WS )* STRING ( WS )* SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:280:9: CHARSET_SYM ( WS )* STRING ( WS )* SEMI
            {
            dbg.location(280,9);
            match(input,CHARSET_SYM,FOLLOW_CHARSET_SYM_in_charSet264); if (state.failed) return ;
            dbg.location(280,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:280:21: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:280:21: WS
            	    {
            	    dbg.location(280,21);
            	    match(input,WS,FOLLOW_WS_in_charSet266); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);
            } finally {dbg.exitSubRule(11);}

            dbg.location(280,25);
            match(input,STRING,FOLLOW_STRING_in_charSet269); if (state.failed) return ;
            dbg.location(280,32);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:280:32: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:280:32: WS
            	    {
            	    dbg.location(280,32);
            	    match(input,WS,FOLLOW_WS_in_charSet271); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop12;
                }
            } while (true);
            } finally {dbg.exitSubRule(12);}

            dbg.location(280,36);
            match(input,SEMI,FOLLOW_SEMI_in_charSet274); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(281, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:286:1: imports : IMPORT_SYM ( WS )* ( resourceIdentifier ) ( WS )* media_query_list SEMI ;
    public final void imports() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "imports");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(286, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:5: ( IMPORT_SYM ( WS )* ( resourceIdentifier ) ( WS )* media_query_list SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:9: IMPORT_SYM ( WS )* ( resourceIdentifier ) ( WS )* media_query_list SEMI
            {
            dbg.location(287,9);
            match(input,IMPORT_SYM,FOLLOW_IMPORT_SYM_in_imports296); if (state.failed) return ;
            dbg.location(287,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:20: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:20: WS
            	    {
            	    dbg.location(287,20);
            	    match(input,WS,FOLLOW_WS_in_imports298); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);
            } finally {dbg.exitSubRule(13);}

            dbg.location(287,24);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:24: ( resourceIdentifier )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:25: resourceIdentifier
            {
            dbg.location(287,25);
            pushFollow(FOLLOW_resourceIdentifier_in_imports302);
            resourceIdentifier();

            state._fsp--;
            if (state.failed) return ;

            }

            dbg.location(287,45);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:45: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:287:45: WS
            	    {
            	    dbg.location(287,45);
            	    match(input,WS,FOLLOW_WS_in_imports305); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);
            } finally {dbg.exitSubRule(14);}

            dbg.location(287,49);
            pushFollow(FOLLOW_media_query_list_in_imports308);
            media_query_list();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(287,66);
            match(input,SEMI,FOLLOW_SEMI_in_imports310); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(288, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:294:1: media : MEDIA_SYM ( WS )* media_query_list LBRACE ( WS )* ( ( ruleSet | page ) ( WS )* )* RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(294, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:295:5: ( MEDIA_SYM ( WS )* media_query_list LBRACE ( WS )* ( ( ruleSet | page ) ( WS )* )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:295:7: MEDIA_SYM ( WS )* media_query_list LBRACE ( WS )* ( ( ruleSet | page ) ( WS )* )* RBRACE
            {
            dbg.location(295,7);
            match(input,MEDIA_SYM,FOLLOW_MEDIA_SYM_in_media331); if (state.failed) return ;
            dbg.location(295,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:295:17: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:295:17: WS
            	    {
            	    dbg.location(295,17);
            	    match(input,WS,FOLLOW_WS_in_media333); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);
            } finally {dbg.exitSubRule(15);}

            dbg.location(295,21);
            pushFollow(FOLLOW_media_query_list_in_media336);
            media_query_list();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(296,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_media346); if (state.failed) return ;
            dbg.location(296,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:296:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:296:16: WS
            	    {
            	    dbg.location(296,16);
            	    match(input,WS,FOLLOW_WS_in_media348); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);
            } finally {dbg.exitSubRule(16);}

            dbg.location(297,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:297:13: ( ( ruleSet | page ) ( WS )* )*
            try { dbg.enterSubRule(19);

            loop19:
            do {
                int alt19=2;
                try { dbg.enterDecision(19, decisionCanBacktrack[19]);

                int LA19_0 = input.LA(1);

                if ( (LA19_0==IDENT||LA19_0==GEN||LA19_0==PAGE_SYM||LA19_0==COLON||(LA19_0>=STAR && LA19_0<=DCOLON)||LA19_0==114) ) {
                    alt19=1;
                }


                } finally {dbg.exitDecision(19);}

                switch (alt19) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:297:15: ( ruleSet | page ) ( WS )*
            	    {
            	    dbg.location(297,15);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:297:15: ( ruleSet | page )
            	    int alt17=2;
            	    try { dbg.enterSubRule(17);
            	    try { dbg.enterDecision(17, decisionCanBacktrack[17]);

            	    int LA17_0 = input.LA(1);

            	    if ( (LA17_0==IDENT||LA17_0==GEN||LA17_0==COLON||(LA17_0>=STAR && LA17_0<=DCOLON)||LA17_0==114) ) {
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:297:17: ruleSet
            	            {
            	            dbg.location(297,17);
            	            pushFollow(FOLLOW_ruleSet_in_media367);
            	            ruleSet();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:297:27: page
            	            {
            	            dbg.location(297,27);
            	            pushFollow(FOLLOW_page_in_media371);
            	            page();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(17);}

            	    dbg.location(297,34);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:297:34: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:297:34: WS
            	    	    {
            	    	    dbg.location(297,34);
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

            dbg.location(298,10);
            match(input,RBRACE,FOLLOW_RBRACE_in_media389); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(299, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:305:1: media_query_list : ( media_query ( COMMA ( WS )* media_query )* )? ;
    public final void media_query_list() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_query_list");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(305, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:2: ( ( media_query ( COMMA ( WS )* media_query )* )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:4: ( media_query ( COMMA ( WS )* media_query )* )?
            {
            dbg.location(306,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:4: ( media_query ( COMMA ( WS )* media_query )* )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:6: media_query ( COMMA ( WS )* media_query )*
                    {
                    dbg.location(306,6);
                    pushFollow(FOLLOW_media_query_in_media_query_list409);
                    media_query();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(306,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:18: ( COMMA ( WS )* media_query )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:20: COMMA ( WS )* media_query
                    	    {
                    	    dbg.location(306,20);
                    	    match(input,COMMA,FOLLOW_COMMA_in_media_query_list413); if (state.failed) return ;
                    	    dbg.location(306,26);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:26: ( WS )*
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

                    	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:26: WS
                    	    	    {
                    	    	    dbg.location(306,26);
                    	    	    match(input,WS,FOLLOW_WS_in_media_query_list415); if (state.failed) return ;

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop20;
                    	        }
                    	    } while (true);
                    	    } finally {dbg.exitSubRule(20);}

                    	    dbg.location(306,30);
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
        dbg.location(307, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:309:1: media_query : ( ( ( ONLY | NOT ) ( WS )* )? media_type ( WS )* ( AND ( WS )* media_expression )* | media_expression ( AND ( WS )* media_expression )* );
    public final void media_query() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_query");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(309, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:2: ( ( ( ONLY | NOT ) ( WS )* )? media_type ( WS )* ( AND ( WS )* media_expression )* | media_expression ( AND ( WS )* media_expression )* )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:4: ( ( ONLY | NOT ) ( WS )* )? media_type ( WS )* ( AND ( WS )* media_expression )*
                    {
                    dbg.location(310,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:4: ( ( ONLY | NOT ) ( WS )* )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:5: ( ONLY | NOT ) ( WS )*
                            {
                            dbg.location(310,5);
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

                            dbg.location(310,18);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:18: ( WS )*
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

                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:18: WS
                            	    {
                            	    dbg.location(310,18);
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

                    dbg.location(310,26);
                    pushFollow(FOLLOW_media_type_in_media_query452);
                    media_type();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(310,37);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:37: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:37: WS
                    	    {
                    	    dbg.location(310,37);
                    	    match(input,WS,FOLLOW_WS_in_media_query454); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop25;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(25);}

                    dbg.location(310,41);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:41: ( AND ( WS )* media_expression )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:43: AND ( WS )* media_expression
                    	    {
                    	    dbg.location(310,43);
                    	    match(input,AND,FOLLOW_AND_in_media_query459); if (state.failed) return ;
                    	    dbg.location(310,47);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:47: ( WS )*
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

                    	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:47: WS
                    	    	    {
                    	    	    dbg.location(310,47);
                    	    	    match(input,WS,FOLLOW_WS_in_media_query461); if (state.failed) return ;

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop26;
                    	        }
                    	    } while (true);
                    	    } finally {dbg.exitSubRule(26);}

                    	    dbg.location(310,51);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:311:4: media_expression ( AND ( WS )* media_expression )*
                    {
                    dbg.location(311,4);
                    pushFollow(FOLLOW_media_expression_in_media_query472);
                    media_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(311,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:311:21: ( AND ( WS )* media_expression )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:311:23: AND ( WS )* media_expression
                    	    {
                    	    dbg.location(311,23);
                    	    match(input,AND,FOLLOW_AND_in_media_query476); if (state.failed) return ;
                    	    dbg.location(311,27);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:311:27: ( WS )*
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

                    	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:311:27: WS
                    	    	    {
                    	    	    dbg.location(311,27);
                    	    	    match(input,WS,FOLLOW_WS_in_media_query478); if (state.failed) return ;

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop28;
                    	        }
                    	    } while (true);
                    	    } finally {dbg.exitSubRule(28);}

                    	    dbg.location(311,31);
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
        dbg.location(312, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:314:1: media_type : ( IDENT | GEN );
    public final void media_type() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_type");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(314, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:315:2: ( IDENT | GEN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(315,2);
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
        dbg.location(316, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:318:1: media_expression : '(' ( WS )* media_feature ( WS )* ( ':' ( WS )* expr )? ')' ( WS )* ;
    public final void media_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(318, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:2: ( '(' ( WS )* media_feature ( WS )* ( ':' ( WS )* expr )? ')' ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:4: '(' ( WS )* media_feature ( WS )* ( ':' ( WS )* expr )? ')' ( WS )*
            {
            dbg.location(319,4);
            match(input,LPAREN,FOLLOW_LPAREN_in_media_expression512); if (state.failed) return ;
            dbg.location(319,8);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:8: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:8: WS
            	    {
            	    dbg.location(319,8);
            	    match(input,WS,FOLLOW_WS_in_media_expression514); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop31;
                }
            } while (true);
            } finally {dbg.exitSubRule(31);}

            dbg.location(319,12);
            pushFollow(FOLLOW_media_feature_in_media_expression517);
            media_feature();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(319,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:26: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:26: WS
            	    {
            	    dbg.location(319,26);
            	    match(input,WS,FOLLOW_WS_in_media_expression519); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop32;
                }
            } while (true);
            } finally {dbg.exitSubRule(32);}

            dbg.location(319,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:30: ( ':' ( WS )* expr )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:32: ':' ( WS )* expr
                    {
                    dbg.location(319,32);
                    match(input,COLON,FOLLOW_COLON_in_media_expression524); if (state.failed) return ;
                    dbg.location(319,36);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:36: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:36: WS
                    	    {
                    	    dbg.location(319,36);
                    	    match(input,WS,FOLLOW_WS_in_media_expression526); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop33;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(33);}

                    dbg.location(319,40);
                    pushFollow(FOLLOW_expr_in_media_expression529);
                    expr();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(34);}

            dbg.location(319,48);
            match(input,RPAREN,FOLLOW_RPAREN_in_media_expression534); if (state.failed) return ;
            dbg.location(319,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:52: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:52: WS
            	    {
            	    dbg.location(319,52);
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
        dbg.location(320, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:321:1: media_feature : IDENT ;
    public final void media_feature() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_feature");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(321, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:322:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:322:4: IDENT
            {
            dbg.location(322,4);
            match(input,IDENT,FOLLOW_IDENT_in_media_feature547); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(323, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:328:1: medium : ( IDENT | GEN ) ( WS )* ;
    public final void medium() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "medium");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(328, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:329:5: ( ( IDENT | GEN ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:329:7: ( IDENT | GEN ) ( WS )*
            {
            dbg.location(329,7);
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

            dbg.location(329,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:329:23: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:329:23: WS
            	    {
            	    dbg.location(329,23);
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
        dbg.location(330, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:333:1: bodylist : ( bodyset )* ;
    public final void bodylist() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodylist");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(333, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:334:5: ( ( bodyset )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:334:7: ( bodyset )*
            {
            dbg.location(334,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:334:7: ( bodyset )*
            try { dbg.enterSubRule(37);

            loop37:
            do {
                int alt37=2;
                try { dbg.enterDecision(37, decisionCanBacktrack[37]);

                int LA37_0 = input.LA(1);

                if ( (LA37_0==IDENT||LA37_0==MEDIA_SYM||(LA37_0>=GEN && LA37_0<=MOZ_DOCUMENT_SYM)||(LA37_0>=PAGE_SYM && LA37_0<=FONT_FACE_SYM)||LA37_0==COLON||(LA37_0>=STAR && LA37_0<=DCOLON)||LA37_0==114) ) {
                    alt37=1;
                }


                } finally {dbg.exitDecision(37);}

                switch (alt37) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:334:7: bodyset
            	    {
            	    dbg.location(334,7);
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
        dbg.location(335, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:337:1: bodyset : ( ruleSet | media | page | counterStyle | fontFace | moz_document ) ( WS )* ;
    public final void bodyset() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyset");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(337, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:5: ( ( ruleSet | media | page | counterStyle | fontFace | moz_document ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:7: ( ruleSet | media | page | counterStyle | fontFace | moz_document ) ( WS )*
            {
            dbg.location(338,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:7: ( ruleSet | media | page | counterStyle | fontFace | moz_document )
            int alt38=6;
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
            case 114:
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
            case MOZ_DOCUMENT_SYM:
                {
                alt38=6;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:339:6: ruleSet
                    {
                    dbg.location(339,6);
                    pushFollow(FOLLOW_ruleSet_in_bodyset626);
                    ruleSet();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:340:11: media
                    {
                    dbg.location(340,11);
                    pushFollow(FOLLOW_media_in_bodyset638);
                    media();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:11: page
                    {
                    dbg.location(341,11);
                    pushFollow(FOLLOW_page_in_bodyset650);
                    page();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:342:11: counterStyle
                    {
                    dbg.location(342,11);
                    pushFollow(FOLLOW_counterStyle_in_bodyset662);
                    counterStyle();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:343:11: fontFace
                    {
                    dbg.location(343,11);
                    pushFollow(FOLLOW_fontFace_in_bodyset674);
                    fontFace();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:344:11: moz_document
                    {
                    dbg.location(344,11);
                    pushFollow(FOLLOW_moz_document_in_bodyset686);
                    moz_document();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(38);}

            dbg.location(346,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:346:7: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:346:7: WS
            	    {
            	    dbg.location(346,7);
            	    match(input,WS,FOLLOW_WS_in_bodyset702); if (state.failed) return ;

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
        dbg.location(347, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "bodyset");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "bodyset"


    // $ANTLR start "moz_document"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:1: moz_document : MOZ_DOCUMENT_SYM ( WS )* ( moz_document_function ( WS )* ) ( COMMA ( WS )* moz_document_function ( WS )* )* LBRACE ( WS )* ( ( ruleSet | page ) ( WS )* )* RBRACE ;
    public final void moz_document() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(349, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:2: ( MOZ_DOCUMENT_SYM ( WS )* ( moz_document_function ( WS )* ) ( COMMA ( WS )* moz_document_function ( WS )* )* LBRACE ( WS )* ( ( ruleSet | page ) ( WS )* )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:351:2: MOZ_DOCUMENT_SYM ( WS )* ( moz_document_function ( WS )* ) ( COMMA ( WS )* moz_document_function ( WS )* )* LBRACE ( WS )* ( ( ruleSet | page ) ( WS )* )* RBRACE
            {
            dbg.location(351,2);
            match(input,MOZ_DOCUMENT_SYM,FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document723); if (state.failed) return ;
            dbg.location(351,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:351:19: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:351:19: WS
            	    {
            	    dbg.location(351,19);
            	    match(input,WS,FOLLOW_WS_in_moz_document725); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop40;
                }
            } while (true);
            } finally {dbg.exitSubRule(40);}

            dbg.location(351,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:351:23: ( moz_document_function ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:351:25: moz_document_function ( WS )*
            {
            dbg.location(351,25);
            pushFollow(FOLLOW_moz_document_function_in_moz_document730);
            moz_document_function();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(351,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:351:47: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:351:47: WS
            	    {
            	    dbg.location(351,47);
            	    match(input,WS,FOLLOW_WS_in_moz_document732); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop41;
                }
            } while (true);
            } finally {dbg.exitSubRule(41);}


            }

            dbg.location(351,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:351:52: ( COMMA ( WS )* moz_document_function ( WS )* )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:351:54: COMMA ( WS )* moz_document_function ( WS )*
            	    {
            	    dbg.location(351,54);
            	    match(input,COMMA,FOLLOW_COMMA_in_moz_document738); if (state.failed) return ;
            	    dbg.location(351,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:351:60: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:351:60: WS
            	    	    {
            	    	    dbg.location(351,60);
            	    	    match(input,WS,FOLLOW_WS_in_moz_document740); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop42;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(42);}

            	    dbg.location(351,64);
            	    pushFollow(FOLLOW_moz_document_function_in_moz_document743);
            	    moz_document_function();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(351,86);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:351:86: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:351:86: WS
            	    	    {
            	    	    dbg.location(351,86);
            	    	    match(input,WS,FOLLOW_WS_in_moz_document745); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop43;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(43);}


            	    }
            	    break;

            	default :
            	    break loop44;
                }
            } while (true);
            } finally {dbg.exitSubRule(44);}

            dbg.location(352,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_moz_document752); if (state.failed) return ;
            dbg.location(352,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:352:9: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:352:9: WS
            	    {
            	    dbg.location(352,9);
            	    match(input,WS,FOLLOW_WS_in_moz_document754); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop45;
                }
            } while (true);
            } finally {dbg.exitSubRule(45);}

            dbg.location(353,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:3: ( ( ruleSet | page ) ( WS )* )*
            try { dbg.enterSubRule(48);

            loop48:
            do {
                int alt48=2;
                try { dbg.enterDecision(48, decisionCanBacktrack[48]);

                int LA48_0 = input.LA(1);

                if ( (LA48_0==IDENT||LA48_0==GEN||LA48_0==PAGE_SYM||LA48_0==COLON||(LA48_0>=STAR && LA48_0<=DCOLON)||LA48_0==114) ) {
                    alt48=1;
                }


                } finally {dbg.exitDecision(48);}

                switch (alt48) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:5: ( ruleSet | page ) ( WS )*
            	    {
            	    dbg.location(353,5);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:5: ( ruleSet | page )
            	    int alt46=2;
            	    try { dbg.enterSubRule(46);
            	    try { dbg.enterDecision(46, decisionCanBacktrack[46]);

            	    int LA46_0 = input.LA(1);

            	    if ( (LA46_0==IDENT||LA46_0==GEN||LA46_0==COLON||(LA46_0>=STAR && LA46_0<=DCOLON)||LA46_0==114) ) {
            	        alt46=1;
            	    }
            	    else if ( (LA46_0==PAGE_SYM) ) {
            	        alt46=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 46, 0, input);

            	        dbg.recognitionException(nvae);
            	        throw nvae;
            	    }
            	    } finally {dbg.exitDecision(46);}

            	    switch (alt46) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:7: ruleSet
            	            {
            	            dbg.location(353,7);
            	            pushFollow(FOLLOW_ruleSet_in_moz_document763);
            	            ruleSet();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:17: page
            	            {
            	            dbg.location(353,17);
            	            pushFollow(FOLLOW_page_in_moz_document767);
            	            page();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(46);}

            	    dbg.location(353,24);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:24: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:24: WS
            	    	    {
            	    	    dbg.location(353,24);
            	    	    match(input,WS,FOLLOW_WS_in_moz_document771); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop47;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(47);}


            	    }
            	    break;

            	default :
            	    break loop48;
                }
            } while (true);
            } finally {dbg.exitSubRule(48);}

            dbg.location(354,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_moz_document777); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(355, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "moz_document");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "moz_document"


    // $ANTLR start "moz_document_function"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:357:1: moz_document_function : ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP );
    public final void moz_document_function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document_function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(357, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:358:2: ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(358,2);
            if ( input.LA(1)==URI||(input.LA(1)>=MOZ_URL_PREFIX && input.LA(1)<=MOZ_REGEXP) ) {
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
        dbg.location(360, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "moz_document_function");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "moz_document_function"


    // $ANTLR start "page"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:1: page : PAGE_SYM ( WS )* ( IDENT ( WS )* )? ( pseudoPage ( WS )* )? LBRACE ( WS )* ( declaration | margin ( WS )* )? ( SEMI ( WS )* ( declaration | margin ( WS )* )? )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(362, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:5: ( PAGE_SYM ( WS )* ( IDENT ( WS )* )? ( pseudoPage ( WS )* )? LBRACE ( WS )* ( declaration | margin ( WS )* )? ( SEMI ( WS )* ( declaration | margin ( WS )* )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:7: PAGE_SYM ( WS )* ( IDENT ( WS )* )? ( pseudoPage ( WS )* )? LBRACE ( WS )* ( declaration | margin ( WS )* )? ( SEMI ( WS )* ( declaration | margin ( WS )* )? )* RBRACE
            {
            dbg.location(363,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page819); if (state.failed) return ;
            dbg.location(363,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:16: WS
            	    {
            	    dbg.location(363,16);
            	    match(input,WS,FOLLOW_WS_in_page821); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop49;
                }
            } while (true);
            } finally {dbg.exitSubRule(49);}

            dbg.location(363,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:20: ( IDENT ( WS )* )?
            int alt51=2;
            try { dbg.enterSubRule(51);
            try { dbg.enterDecision(51, decisionCanBacktrack[51]);

            int LA51_0 = input.LA(1);

            if ( (LA51_0==IDENT) ) {
                alt51=1;
            }
            } finally {dbg.exitDecision(51);}

            switch (alt51) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:22: IDENT ( WS )*
                    {
                    dbg.location(363,22);
                    match(input,IDENT,FOLLOW_IDENT_in_page826); if (state.failed) return ;
                    dbg.location(363,28);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:28: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:28: WS
                    	    {
                    	    dbg.location(363,28);
                    	    match(input,WS,FOLLOW_WS_in_page828); if (state.failed) return ;

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

            dbg.location(363,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:35: ( pseudoPage ( WS )* )?
            int alt53=2;
            try { dbg.enterSubRule(53);
            try { dbg.enterDecision(53, decisionCanBacktrack[53]);

            int LA53_0 = input.LA(1);

            if ( (LA53_0==COLON) ) {
                alt53=1;
            }
            } finally {dbg.exitDecision(53);}

            switch (alt53) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:36: pseudoPage ( WS )*
                    {
                    dbg.location(363,36);
                    pushFollow(FOLLOW_pseudoPage_in_page835);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(363,47);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:47: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:47: WS
                    	    {
                    	    dbg.location(363,47);
                    	    match(input,WS,FOLLOW_WS_in_page837); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop52;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(52);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(53);}

            dbg.location(364,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page850); if (state.failed) return ;
            dbg.location(364,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:16: WS
            	    {
            	    dbg.location(364,16);
            	    match(input,WS,FOLLOW_WS_in_page852); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop54;
                }
            } while (true);
            } finally {dbg.exitSubRule(54);}

            dbg.location(369,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:13: ( declaration | margin ( WS )* )?
            int alt56=3;
            try { dbg.enterSubRule(56);
            try { dbg.enterDecision(56, decisionCanBacktrack[56]);

            int LA56_0 = input.LA(1);

            if ( (LA56_0==IDENT||LA56_0==GEN) ) {
                alt56=1;
            }
            else if ( ((LA56_0>=TOPLEFTCORNER_SYM && LA56_0<=RIGHTBOTTOM_SYM)) ) {
                alt56=2;
            }
            } finally {dbg.exitDecision(56);}

            switch (alt56) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:14: declaration
                    {
                    dbg.location(369,14);
                    pushFollow(FOLLOW_declaration_in_page920);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:26: margin ( WS )*
                    {
                    dbg.location(369,26);
                    pushFollow(FOLLOW_margin_in_page922);
                    margin();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(369,33);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:33: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:33: WS
                    	    {
                    	    dbg.location(369,33);
                    	    match(input,WS,FOLLOW_WS_in_page924); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop55;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(55);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(56);}

            dbg.location(369,39);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:39: ( SEMI ( WS )* ( declaration | margin ( WS )* )? )*
            try { dbg.enterSubRule(60);

            loop60:
            do {
                int alt60=2;
                try { dbg.enterDecision(60, decisionCanBacktrack[60]);

                int LA60_0 = input.LA(1);

                if ( (LA60_0==SEMI) ) {
                    alt60=1;
                }


                } finally {dbg.exitDecision(60);}

                switch (alt60) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:40: SEMI ( WS )* ( declaration | margin ( WS )* )?
            	    {
            	    dbg.location(369,40);
            	    match(input,SEMI,FOLLOW_SEMI_in_page930); if (state.failed) return ;
            	    dbg.location(369,45);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:45: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:45: WS
            	    	    {
            	    	    dbg.location(369,45);
            	    	    match(input,WS,FOLLOW_WS_in_page932); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop57;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(57);}

            	    dbg.location(369,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:49: ( declaration | margin ( WS )* )?
            	    int alt59=3;
            	    try { dbg.enterSubRule(59);
            	    try { dbg.enterDecision(59, decisionCanBacktrack[59]);

            	    int LA59_0 = input.LA(1);

            	    if ( (LA59_0==IDENT||LA59_0==GEN) ) {
            	        alt59=1;
            	    }
            	    else if ( ((LA59_0>=TOPLEFTCORNER_SYM && LA59_0<=RIGHTBOTTOM_SYM)) ) {
            	        alt59=2;
            	    }
            	    } finally {dbg.exitDecision(59);}

            	    switch (alt59) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:50: declaration
            	            {
            	            dbg.location(369,50);
            	            pushFollow(FOLLOW_declaration_in_page936);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:62: margin ( WS )*
            	            {
            	            dbg.location(369,62);
            	            pushFollow(FOLLOW_margin_in_page938);
            	            margin();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(369,69);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:69: ( WS )*
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

            	            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:369:69: WS
            	            	    {
            	            	    dbg.location(369,69);
            	            	    match(input,WS,FOLLOW_WS_in_page940); if (state.failed) return ;

            	            	    }
            	            	    break;

            	            	default :
            	            	    break loop58;
            	                }
            	            } while (true);
            	            } finally {dbg.exitSubRule(58);}


            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(59);}


            	    }
            	    break;

            	default :
            	    break loop60;
                }
            } while (true);
            } finally {dbg.exitSubRule(60);}

            dbg.location(370,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page955); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(371, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:373:1: counterStyle : COUNTER_STYLE_SYM ( WS )* IDENT ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void counterStyle() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "counterStyle");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(373, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:374:5: ( COUNTER_STYLE_SYM ( WS )* IDENT ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:374:7: COUNTER_STYLE_SYM ( WS )* IDENT ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(374,7);
            match(input,COUNTER_STYLE_SYM,FOLLOW_COUNTER_STYLE_SYM_in_counterStyle976); if (state.failed) return ;
            dbg.location(374,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:374:25: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:374:25: WS
            	    {
            	    dbg.location(374,25);
            	    match(input,WS,FOLLOW_WS_in_counterStyle978); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop61;
                }
            } while (true);
            } finally {dbg.exitSubRule(61);}

            dbg.location(374,29);
            match(input,IDENT,FOLLOW_IDENT_in_counterStyle981); if (state.failed) return ;
            dbg.location(374,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:374:35: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:374:35: WS
            	    {
            	    dbg.location(374,35);
            	    match(input,WS,FOLLOW_WS_in_counterStyle983); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop62;
                }
            } while (true);
            } finally {dbg.exitSubRule(62);}

            dbg.location(375,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_counterStyle994); if (state.failed) return ;
            dbg.location(375,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:375:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:375:16: WS
            	    {
            	    dbg.location(375,16);
            	    match(input,WS,FOLLOW_WS_in_counterStyle996); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop63;
                }
            } while (true);
            } finally {dbg.exitSubRule(63);}

            dbg.location(375,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_counterStyle999);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(376,3);
            pushFollow(FOLLOW_declarations_in_counterStyle1003);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(377,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_counterStyle1013); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(378, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:1: fontFace : FONT_FACE_SYM ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void fontFace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fontFace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(380, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:5: ( FONT_FACE_SYM ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:7: FONT_FACE_SYM ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(381,7);
            match(input,FONT_FACE_SYM,FOLLOW_FONT_FACE_SYM_in_fontFace1034); if (state.failed) return ;
            dbg.location(381,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:21: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:21: WS
            	    {
            	    dbg.location(381,21);
            	    match(input,WS,FOLLOW_WS_in_fontFace1036); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop64;
                }
            } while (true);
            } finally {dbg.exitSubRule(64);}

            dbg.location(382,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_fontFace1047); if (state.failed) return ;
            dbg.location(382,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:16: WS
            	    {
            	    dbg.location(382,16);
            	    match(input,WS,FOLLOW_WS_in_fontFace1049); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop65;
                }
            } while (true);
            } finally {dbg.exitSubRule(65);}

            dbg.location(382,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_fontFace1052);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(383,3);
            pushFollow(FOLLOW_declarations_in_fontFace1056);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(384,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_fontFace1066); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "fontFace");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "fontFace"


    // $ANTLR start "margin"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:388:1: margin : margin_sym ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void margin() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(388, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:2: ( margin_sym ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:4: margin_sym ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(389,4);
            pushFollow(FOLLOW_margin_sym_in_margin1086);
            margin_sym();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(389,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:15: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:15: WS
            	    {
            	    dbg.location(389,15);
            	    match(input,WS,FOLLOW_WS_in_margin1088); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop66;
                }
            } while (true);
            } finally {dbg.exitSubRule(66);}

            dbg.location(389,19);
            match(input,LBRACE,FOLLOW_LBRACE_in_margin1091); if (state.failed) return ;
            dbg.location(389,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:26: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:26: WS
            	    {
            	    dbg.location(389,26);
            	    match(input,WS,FOLLOW_WS_in_margin1093); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop67;
                }
            } while (true);
            } finally {dbg.exitSubRule(67);}

            dbg.location(389,30);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_margin1096);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(389,50);
            pushFollow(FOLLOW_declarations_in_margin1098);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(389,63);
            match(input,RBRACE,FOLLOW_RBRACE_in_margin1100); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(390, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:392:1: margin_sym : ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM );
    public final void margin_sym() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin_sym");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(392, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:393:2: ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(393,2);
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
        dbg.location(410, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:412:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(412, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:413:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:413:7: COLON IDENT
            {
            dbg.location(413,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage1329); if (state.failed) return ;
            dbg.location(413,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage1331); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(414, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:1: operator : ( SOLIDUS ( WS )* | COMMA ( WS )* | );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(416, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:5: ( SOLIDUS ( WS )* | COMMA ( WS )* | )
            int alt70=3;
            try { dbg.enterDecision(70, decisionCanBacktrack[70]);

            switch ( input.LA(1) ) {
            case SOLIDUS:
                {
                alt70=1;
                }
                break;
            case COMMA:
                {
                alt70=2;
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
                alt70=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 70, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(70);}

            switch (alt70) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:7: SOLIDUS ( WS )*
                    {
                    dbg.location(417,7);
                    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_operator1352); if (state.failed) return ;
                    dbg.location(417,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:15: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:15: WS
                    	    {
                    	    dbg.location(417,15);
                    	    match(input,WS,FOLLOW_WS_in_operator1354); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop68;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(68);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:7: COMMA ( WS )*
                    {
                    dbg.location(418,7);
                    match(input,COMMA,FOLLOW_COMMA_in_operator1363); if (state.failed) return ;
                    dbg.location(418,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:13: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:13: WS
                    	    {
                    	    dbg.location(418,13);
                    	    match(input,WS,FOLLOW_WS_in_operator1365); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop69;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(69);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:5: 
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
        dbg.location(420, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:1: combinator : ( PLUS ( WS )* | GREATER ( WS )* | TILDE ( WS )* | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(422, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:5: ( PLUS ( WS )* | GREATER ( WS )* | TILDE ( WS )* | )
            int alt74=4;
            try { dbg.enterDecision(74, decisionCanBacktrack[74]);

            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt74=1;
                }
                break;
            case GREATER:
                {
                alt74=2;
                }
                break;
            case TILDE:
                {
                alt74=3;
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
            case 114:
                {
                alt74=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 74, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(74);}

            switch (alt74) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:7: PLUS ( WS )*
                    {
                    dbg.location(423,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator1393); if (state.failed) return ;
                    dbg.location(423,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:12: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:12: WS
                    	    {
                    	    dbg.location(423,12);
                    	    match(input,WS,FOLLOW_WS_in_combinator1395); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop71;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(71);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:424:7: GREATER ( WS )*
                    {
                    dbg.location(424,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator1404); if (state.failed) return ;
                    dbg.location(424,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:424:15: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:424:15: WS
                    	    {
                    	    dbg.location(424,15);
                    	    match(input,WS,FOLLOW_WS_in_combinator1406); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop72;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(72);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:425:7: TILDE ( WS )*
                    {
                    dbg.location(425,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator1415); if (state.failed) return ;
                    dbg.location(425,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:425:13: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:425:13: WS
                    	    {
                    	    dbg.location(425,13);
                    	    match(input,WS,FOLLOW_WS_in_combinator1417); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop73;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(73);}


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:427:5: 
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
        dbg.location(427, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(429, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(430,5);
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
        dbg.location(432, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:434:1: property : ( IDENT | GEN ) ( WS )* ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(434, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:435:5: ( ( IDENT | GEN ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:435:7: ( IDENT | GEN ) ( WS )*
            {
            dbg.location(435,7);
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

            dbg.location(435,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:435:21: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:435:21: WS
            	    {
            	    dbg.location(435,21);
            	    match(input,WS,FOLLOW_WS_in_property1485); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop75;
                }
            } while (true);
            } finally {dbg.exitSubRule(75);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(436, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:1: ruleSet : selectorsGroup LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void ruleSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ruleSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(438, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:5: ( selectorsGroup LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:9: selectorsGroup LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(439,9);
            pushFollow(FOLLOW_selectorsGroup_in_ruleSet1510);
            selectorsGroup();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(440,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_ruleSet1520); if (state.failed) return ;
            dbg.location(440,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:440:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:440:16: WS
            	    {
            	    dbg.location(440,16);
            	    match(input,WS,FOLLOW_WS_in_ruleSet1522); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop76;
                }
            } while (true);
            } finally {dbg.exitSubRule(76);}

            dbg.location(440,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_ruleSet1525);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(441,13);
            pushFollow(FOLLOW_declarations_in_ruleSet1539);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(442,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_ruleSet1549); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "ruleSet");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "ruleSet"


    // $ANTLR start "declarations"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:1: declarations : ( declaration )? ( SEMI ( WS )* ( declaration )? )* ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(445, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:5: ( ( declaration )? ( SEMI ( WS )* ( declaration )? )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:448:9: ( declaration )? ( SEMI ( WS )* ( declaration )? )*
            {
            dbg.location(448,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:448:9: ( declaration )?
            int alt77=2;
            try { dbg.enterSubRule(77);
            try { dbg.enterDecision(77, decisionCanBacktrack[77]);

            int LA77_0 = input.LA(1);

            if ( (LA77_0==IDENT||LA77_0==GEN) ) {
                alt77=1;
            }
            } finally {dbg.exitDecision(77);}

            switch (alt77) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:448:9: declaration
                    {
                    dbg.location(448,9);
                    pushFollow(FOLLOW_declaration_in_declarations1587);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(77);}

            dbg.location(448,22);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:448:22: ( SEMI ( WS )* ( declaration )? )*
            try { dbg.enterSubRule(80);

            loop80:
            do {
                int alt80=2;
                try { dbg.enterDecision(80, decisionCanBacktrack[80]);

                int LA80_0 = input.LA(1);

                if ( (LA80_0==SEMI) ) {
                    alt80=1;
                }


                } finally {dbg.exitDecision(80);}

                switch (alt80) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:448:23: SEMI ( WS )* ( declaration )?
            	    {
            	    dbg.location(448,23);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations1591); if (state.failed) return ;
            	    dbg.location(448,28);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:448:28: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:448:28: WS
            	    	    {
            	    	    dbg.location(448,28);
            	    	    match(input,WS,FOLLOW_WS_in_declarations1593); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop78;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(78);}

            	    dbg.location(448,32);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:448:32: ( declaration )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:448:32: declaration
            	            {
            	            dbg.location(448,32);
            	            pushFollow(FOLLOW_declaration_in_declarations1596);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(79);}


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
        dbg.location(449, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:1: selectorsGroup : selector ( COMMA ( WS )* selector )* ;
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(451, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:5: ( selector ( COMMA ( WS )* selector )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:7: selector ( COMMA ( WS )* selector )*
            {
            dbg.location(452,7);
            pushFollow(FOLLOW_selector_in_selectorsGroup1620);
            selector();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(452,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:16: ( COMMA ( WS )* selector )*
            try { dbg.enterSubRule(82);

            loop82:
            do {
                int alt82=2;
                try { dbg.enterDecision(82, decisionCanBacktrack[82]);

                int LA82_0 = input.LA(1);

                if ( (LA82_0==COMMA) ) {
                    alt82=1;
                }


                } finally {dbg.exitDecision(82);}

                switch (alt82) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:17: COMMA ( WS )* selector
            	    {
            	    dbg.location(452,17);
            	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup1623); if (state.failed) return ;
            	    dbg.location(452,23);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:23: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:452:23: WS
            	    	    {
            	    	    dbg.location(452,23);
            	    	    match(input,WS,FOLLOW_WS_in_selectorsGroup1625); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop81;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(81);}

            	    dbg.location(452,27);
            	    pushFollow(FOLLOW_selector_in_selectorsGroup1628);
            	    selector();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop82;
                }
            } while (true);
            } finally {dbg.exitSubRule(82);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(453, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(455, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(456,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector1651);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(456,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(83);

            loop83:
            do {
                int alt83=2;
                try { dbg.enterDecision(83, decisionCanBacktrack[83]);

                int LA83_0 = input.LA(1);

                if ( (LA83_0==IDENT||LA83_0==GEN||LA83_0==COLON||(LA83_0>=PLUS && LA83_0<=TILDE)||(LA83_0>=STAR && LA83_0<=DCOLON)||LA83_0==114) ) {
                    alt83=1;
                }


                } finally {dbg.exitDecision(83);}

                switch (alt83) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:456:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(456,31);
            	    pushFollow(FOLLOW_combinator_in_selector1654);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(456,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector1656);
            	    simpleSelectorSequence();

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
        dbg.location(457, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(460, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:462:2: ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) )
            int alt86=2;
            try { dbg.enterDecision(86, decisionCanBacktrack[86]);

            int LA86_0 = input.LA(1);

            if ( (LA86_0==IDENT||LA86_0==GEN||(LA86_0>=STAR && LA86_0<=PIPE)) ) {
                alt86=1;
            }
            else if ( (LA86_0==COLON||(LA86_0>=HASH && LA86_0<=DCOLON)||LA86_0==114) ) {
                alt86=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 86, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(86);}

            switch (alt86) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    {
                    dbg.location(466,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:5: typeSelector ( ( esPred )=> elementSubsequent )*
                    {
                    dbg.location(466,5);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence1696);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(466,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:18: ( ( esPred )=> elementSubsequent )*
                    try { dbg.enterSubRule(84);

                    loop84:
                    do {
                        int alt84=2;
                        try { dbg.enterDecision(84, decisionCanBacktrack[84]);

                        try {
                            isCyclicDecision = true;
                            alt84 = dfa84.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(84);}

                        switch (alt84) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:19: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(466,29);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence1703);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop84;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(84);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:2: ( ( ( esPred )=> elementSubsequent )+ )
                    {
                    dbg.location(468,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:2: ( ( ( esPred )=> elementSubsequent )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:4: ( ( esPred )=> elementSubsequent )+
                    {
                    dbg.location(468,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:4: ( ( esPred )=> elementSubsequent )+
                    int cnt85=0;
                    try { dbg.enterSubRule(85);

                    loop85:
                    do {
                        int alt85=2;
                        try { dbg.enterDecision(85, decisionCanBacktrack[85]);

                        switch ( input.LA(1) ) {
                        case HASH:
                            {
                            int LA85_2 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt85=1;
                            }


                            }
                            break;
                        case 114:
                            {
                            int LA85_3 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt85=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA85_4 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt85=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA85_5 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt85=1;
                            }


                            }
                            break;
                        case COLON:
                        case DCOLON:
                            {
                            int LA85_6 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt85=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(85);}

                        switch (alt85) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:5: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(468,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence1721);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt85 >= 1 ) break loop85;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(85, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt85++;
                    } while (true);
                    } finally {dbg.exitSubRule(85);}


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
        dbg.location(469, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:1: typeSelector options {k=2; } : ( ( nsPred )=> namespace_wqname_prefix )? ( elementName ( WS )* ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(483, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:3: ( ( ( nsPred )=> namespace_wqname_prefix )? ( elementName ( WS )* ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:6: ( ( nsPred )=> namespace_wqname_prefix )? ( elementName ( WS )* )
            {
            dbg.location(485,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:6: ( ( nsPred )=> namespace_wqname_prefix )?
            int alt87=2;
            try { dbg.enterSubRule(87);
            try { dbg.enterDecision(87, decisionCanBacktrack[87]);

            try {
                isCyclicDecision = true;
                alt87 = dfa87.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(87);}

            switch (alt87) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:7: ( nsPred )=> namespace_wqname_prefix
                    {
                    dbg.location(485,17);
                    pushFollow(FOLLOW_namespace_wqname_prefix_in_typeSelector1772);
                    namespace_wqname_prefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(87);}

            dbg.location(485,43);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:43: ( elementName ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:45: elementName ( WS )*
            {
            dbg.location(485,45);
            pushFollow(FOLLOW_elementName_in_typeSelector1778);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(485,57);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:57: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:57: WS
            	    {
            	    dbg.location(485,57);
            	    match(input,WS,FOLLOW_WS_in_typeSelector1780); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop88;
                }
            } while (true);
            } finally {dbg.exitSubRule(88);}


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(486, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:488:2: nsPred : ( IDENT | STAR ) PIPE ;
    public final void nsPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "nsPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(488, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:489:3: ( ( IDENT | STAR ) PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:3: ( IDENT | STAR ) PIPE
            {
            dbg.location(490,3);
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

            dbg.location(490,18);
            match(input,PIPE,FOLLOW_PIPE_in_nsPred1813); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(491, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:499:2: namespace_wqname_prefix : ( ( namespace_prefix ( WS )* )? PIPE | namespace_wildcard_prefix ( WS )* PIPE );
    public final void namespace_wqname_prefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace_wqname_prefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(499, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:500:3: ( ( namespace_prefix ( WS )* )? PIPE | namespace_wildcard_prefix ( WS )* PIPE )
            int alt92=2;
            try { dbg.enterDecision(92, decisionCanBacktrack[92]);

            int LA92_0 = input.LA(1);

            if ( (LA92_0==IDENT||LA92_0==PIPE) ) {
                alt92=1;
            }
            else if ( (LA92_0==STAR) ) {
                alt92=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 92, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(92);}

            switch (alt92) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:500:5: ( namespace_prefix ( WS )* )? PIPE
                    {
                    dbg.location(500,5);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:500:5: ( namespace_prefix ( WS )* )?
                    int alt90=2;
                    try { dbg.enterSubRule(90);
                    try { dbg.enterDecision(90, decisionCanBacktrack[90]);

                    int LA90_0 = input.LA(1);

                    if ( (LA90_0==IDENT) ) {
                        alt90=1;
                    }
                    } finally {dbg.exitDecision(90);}

                    switch (alt90) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:500:7: namespace_prefix ( WS )*
                            {
                            dbg.location(500,7);
                            pushFollow(FOLLOW_namespace_prefix_in_namespace_wqname_prefix1843);
                            namespace_prefix();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(500,24);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:500:24: ( WS )*
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

                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:500:24: WS
                            	    {
                            	    dbg.location(500,24);
                            	    match(input,WS,FOLLOW_WS_in_namespace_wqname_prefix1845); if (state.failed) return ;

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

                    dbg.location(500,31);
                    match(input,PIPE,FOLLOW_PIPE_in_namespace_wqname_prefix1851); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:501:6: namespace_wildcard_prefix ( WS )* PIPE
                    {
                    dbg.location(501,6);
                    pushFollow(FOLLOW_namespace_wildcard_prefix_in_namespace_wqname_prefix1858);
                    namespace_wildcard_prefix();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(501,32);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:501:32: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:501:32: WS
                    	    {
                    	    dbg.location(501,32);
                    	    match(input,WS,FOLLOW_WS_in_namespace_wqname_prefix1860); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop91;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(91);}

                    dbg.location(501,36);
                    match(input,PIPE,FOLLOW_PIPE_in_namespace_wqname_prefix1863); if (state.failed) return ;

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
        dbg.location(502, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:504:1: namespace_wildcard_prefix : STAR ;
    public final void namespace_wildcard_prefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace_wildcard_prefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(504, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:505:4: ( STAR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:506:4: STAR
            {
            dbg.location(506,4);
            match(input,STAR,FOLLOW_STAR_in_namespace_wildcard_prefix1885); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(507, 4);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:1: esPred : ( '#' | HASH | DOT | LBRACKET | COLON | DCOLON );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(509, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:5: ( '#' | HASH | DOT | LBRACKET | COLON | DCOLON )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(510,5);
            if ( input.LA(1)==COLON||(input.LA(1)>=HASH && input.LA(1)<=DCOLON)||input.LA(1)==114 ) {
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
        dbg.location(511, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:1: elementSubsequent : ( cssId | cssClass | attrib | pseudo ) ( WS )* ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(513, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:5: ( ( cssId | cssClass | attrib | pseudo ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:5: ( cssId | cssClass | attrib | pseudo ) ( WS )*
            {
            dbg.location(515,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:5: ( cssId | cssClass | attrib | pseudo )
            int alt93=4;
            try { dbg.enterSubRule(93);
            try { dbg.enterDecision(93, decisionCanBacktrack[93]);

            switch ( input.LA(1) ) {
            case HASH:
            case 114:
                {
                alt93=1;
                }
                break;
            case DOT:
                {
                alt93=2;
                }
                break;
            case LBRACKET:
                {
                alt93=3;
                }
                break;
            case COLON:
            case DCOLON:
                {
                alt93=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 93, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(93);}

            switch (alt93) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:516:6: cssId
                    {
                    dbg.location(516,6);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent1961);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:517:8: cssClass
                    {
                    dbg.location(517,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent1970);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:11: attrib
                    {
                    dbg.location(518,11);
                    pushFollow(FOLLOW_attrib_in_elementSubsequent1982);
                    attrib();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:519:11: pseudo
                    {
                    dbg.location(519,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent1994);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(93);}

            dbg.location(521,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:5: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:5: WS
            	    {
            	    dbg.location(521,5);
            	    match(input,WS,FOLLOW_WS_in_elementSubsequent2006); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop94;
                }
            } while (true);
            } finally {dbg.exitSubRule(94);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(522, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:1: cssId : ( HASH | ( '#' NAME ) );
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(525, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:5: ( HASH | ( '#' NAME ) )
            int alt95=2;
            try { dbg.enterDecision(95, decisionCanBacktrack[95]);

            int LA95_0 = input.LA(1);

            if ( (LA95_0==HASH) ) {
                alt95=1;
            }
            else if ( (LA95_0==114) ) {
                alt95=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 95, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(95);}

            switch (alt95) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:7: HASH
                    {
                    dbg.location(526,7);
                    match(input,HASH,FOLLOW_HASH_in_cssId2029); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:14: ( '#' NAME )
                    {
                    dbg.location(526,14);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:14: ( '#' NAME )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:16: '#' NAME
                    {
                    dbg.location(526,16);
                    match(input,114,FOLLOW_114_in_cssId2035); if (state.failed) return ;
                    dbg.location(526,20);
                    match(input,NAME,FOLLOW_NAME_in_cssId2037); if (state.failed) return ;

                    }


                    }
                    break;

            }
        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(WS, IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(527, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:533:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(533, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:7: DOT ( IDENT | GEN )
            {
            dbg.location(534,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass2065); if (state.failed) return ;
            dbg.location(534,11);
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
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(WS, IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(535, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:542:1: elementName : ( ( IDENT | GEN ) | '*' );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(542, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:5: ( ( IDENT | GEN ) | '*' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(543,5);
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
        dbg.location(544, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:546:1: attrib : LBRACKET ( namespace_wqname_prefix )? ( WS )* attrib_name ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( WS )* attrib_value ( WS )* )? RBRACKET ;
    public final void attrib() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrib");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(546, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:5: ( LBRACKET ( namespace_wqname_prefix )? ( WS )* attrib_name ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( WS )* attrib_value ( WS )* )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:7: LBRACKET ( namespace_wqname_prefix )? ( WS )* attrib_name ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( WS )* attrib_value ( WS )* )? RBRACKET
            {
            dbg.location(547,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_attrib2140); if (state.failed) return ;
            dbg.location(548,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:6: ( namespace_wqname_prefix )?
            int alt96=2;
            try { dbg.enterSubRule(96);
            try { dbg.enterDecision(96, decisionCanBacktrack[96]);

            try {
                isCyclicDecision = true;
                alt96 = dfa96.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(96);}

            switch (alt96) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:6: namespace_wqname_prefix
                    {
                    dbg.location(548,6);
                    pushFollow(FOLLOW_namespace_wqname_prefix_in_attrib2147);
                    namespace_wqname_prefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(96);}

            dbg.location(548,31);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:31: ( WS )*
            try { dbg.enterSubRule(97);

            loop97:
            do {
                int alt97=2;
                try { dbg.enterDecision(97, decisionCanBacktrack[97]);

                int LA97_0 = input.LA(1);

                if ( (LA97_0==WS) ) {
                    alt97=1;
                }


                } finally {dbg.exitDecision(97);}

                switch (alt97) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:548:31: WS
            	    {
            	    dbg.location(548,31);
            	    match(input,WS,FOLLOW_WS_in_attrib2150); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop97;
                }
            } while (true);
            } finally {dbg.exitSubRule(97);}

            dbg.location(549,9);
            pushFollow(FOLLOW_attrib_name_in_attrib2161);
            attrib_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(549,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:549:21: ( WS )*
            try { dbg.enterSubRule(98);

            loop98:
            do {
                int alt98=2;
                try { dbg.enterDecision(98, decisionCanBacktrack[98]);

                int LA98_0 = input.LA(1);

                if ( (LA98_0==WS) ) {
                    alt98=1;
                }


                } finally {dbg.exitDecision(98);}

                switch (alt98) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:549:21: WS
            	    {
            	    dbg.location(549,21);
            	    match(input,WS,FOLLOW_WS_in_attrib2163); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop98;
                }
            } while (true);
            } finally {dbg.exitSubRule(98);}

            dbg.location(551,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:551:13: ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( WS )* attrib_value ( WS )* )?
            int alt101=2;
            try { dbg.enterSubRule(101);
            try { dbg.enterDecision(101, decisionCanBacktrack[101]);

            int LA101_0 = input.LA(1);

            if ( ((LA101_0>=OPEQ && LA101_0<=CONTAINS)) ) {
                alt101=1;
            }
            } finally {dbg.exitDecision(101);}

            switch (alt101) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:552:17: ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( WS )* attrib_value ( WS )*
                    {
                    dbg.location(552,17);
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

                    dbg.location(560,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:17: ( WS )*
                    try { dbg.enterSubRule(99);

                    loop99:
                    do {
                        int alt99=2;
                        try { dbg.enterDecision(99, decisionCanBacktrack[99]);

                        int LA99_0 = input.LA(1);

                        if ( (LA99_0==WS) ) {
                            alt99=1;
                        }


                        } finally {dbg.exitDecision(99);}

                        switch (alt99) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:17: WS
                    	    {
                    	    dbg.location(560,17);
                    	    match(input,WS,FOLLOW_WS_in_attrib2385); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop99;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(99);}

                    dbg.location(561,17);
                    pushFollow(FOLLOW_attrib_value_in_attrib2404);
                    attrib_value();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(562,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:17: ( WS )*
                    try { dbg.enterSubRule(100);

                    loop100:
                    do {
                        int alt100=2;
                        try { dbg.enterDecision(100, decisionCanBacktrack[100]);

                        int LA100_0 = input.LA(1);

                        if ( (LA100_0==WS) ) {
                            alt100=1;
                        }


                        } finally {dbg.exitDecision(100);}

                        switch (alt100) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:17: WS
                    	    {
                    	    dbg.location(562,17);
                    	    match(input,WS,FOLLOW_WS_in_attrib2422); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop100;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(100);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(101);}

            dbg.location(565,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_attrib2451); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(566, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:572:1: syncTo_IDENT_RBRACKET_LBRACE : ;
    public final void syncTo_IDENT_RBRACKET_LBRACE() throws RecognitionException {

                syncToSet(BitSet.of(IDENT, RBRACKET, LBRACE));
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_IDENT_RBRACKET_LBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(572, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:576:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:577:6: 
            {
            }

        }
        finally {
        }
        dbg.location(577, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:580:1: attrib_name : IDENT ;
    public final void attrib_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrib_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(580, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:4: IDENT
            {
            dbg.location(581,4);
            match(input,IDENT,FOLLOW_IDENT_in_attrib_name2494); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(582, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:1: attrib_value : ( IDENT | STRING ) ;
    public final void attrib_value() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrib_value");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(584, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:2: ( IDENT | STRING )
            {
            dbg.location(586,2);
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
        dbg.location(590, 9);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:1: pseudo : ( COLON | DCOLON ) ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )? ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(592, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:5: ( ( COLON | DCOLON ) ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:7: ( COLON | DCOLON ) ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )?
            {
            dbg.location(593,7);
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

            dbg.location(594,13);
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

            dbg.location(595,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:595:17: ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:21: ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN
                    {
                    dbg.location(596,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:21: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:21: WS
                    	    {
                    	    dbg.location(596,21);
                    	    match(input,WS,FOLLOW_WS_in_pseudo2639); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop102;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(102);}

                    dbg.location(596,25);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo2642); if (state.failed) return ;
                    dbg.location(596,32);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:32: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:32: WS
                    	    {
                    	    dbg.location(596,32);
                    	    match(input,WS,FOLLOW_WS_in_pseudo2644); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop103;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(103);}

                    dbg.location(596,36);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:36: ( ( IDENT | GEN ) ( WS )* )?
                    int alt105=2;
                    try { dbg.enterSubRule(105);
                    try { dbg.enterDecision(105, decisionCanBacktrack[105]);

                    int LA105_0 = input.LA(1);

                    if ( (LA105_0==IDENT||LA105_0==GEN) ) {
                        alt105=1;
                    }
                    } finally {dbg.exitDecision(105);}

                    switch (alt105) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:37: ( IDENT | GEN ) ( WS )*
                            {
                            dbg.location(596,37);
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

                            dbg.location(596,53);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:53: ( WS )*
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

                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:53: WS
                            	    {
                            	    dbg.location(596,53);
                            	    match(input,WS,FOLLOW_WS_in_pseudo2658); if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop104;
                                }
                            } while (true);
                            } finally {dbg.exitSubRule(104);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(105);}

                    dbg.location(596,59);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo2663); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(106);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(598, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:600:1: declaration : property COLON ( WS )* expr ( prio )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(600, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:601:5: ( property COLON ( WS )* expr ( prio )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:5: property COLON ( WS )* expr ( prio )?
            {
            dbg.location(603,5);
            pushFollow(FOLLOW_property_in_declaration2709);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(603,14);
            match(input,COLON,FOLLOW_COLON_in_declaration2711); if (state.failed) return ;
            dbg.location(603,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:20: ( WS )*
            try { dbg.enterSubRule(107);

            loop107:
            do {
                int alt107=2;
                try { dbg.enterDecision(107, decisionCanBacktrack[107]);

                int LA107_0 = input.LA(1);

                if ( (LA107_0==WS) ) {
                    alt107=1;
                }


                } finally {dbg.exitDecision(107);}

                switch (alt107) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:20: WS
            	    {
            	    dbg.location(603,20);
            	    match(input,WS,FOLLOW_WS_in_declaration2713); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop107;
                }
            } while (true);
            } finally {dbg.exitSubRule(107);}

            dbg.location(603,24);
            pushFollow(FOLLOW_expr_in_declaration2716);
            expr();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(603,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:29: ( prio )?
            int alt108=2;
            try { dbg.enterSubRule(108);
            try { dbg.enterDecision(108, decisionCanBacktrack[108]);

            int LA108_0 = input.LA(1);

            if ( (LA108_0==IMPORTANT_SYM) ) {
                alt108=1;
            }
            } finally {dbg.exitDecision(108);}

            switch (alt108) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:29: prio
                    {
                    dbg.location(603,29);
                    pushFollow(FOLLOW_prio_in_declaration2718);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(108);}


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
        dbg.location(604, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:614:1: syncTo_IDENT_RBRACE : ;
    public final void syncTo_IDENT_RBRACE() throws RecognitionException {

                syncToSet(BitSet.of(IDENT, RBRACE));
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_IDENT_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(614, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:619:6: 
            {
            }

        }
        finally {
        }
        dbg.location(619, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(622, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:626:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:627:6: 
            {
            }

        }
        finally {
        }
        dbg.location(627, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:630:1: prio : IMPORTANT_SYM ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(630, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:631:5: ( IMPORTANT_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:631:7: IMPORTANT_SYM
            {
            dbg.location(631,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio2811); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(632, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:1: expr : term ( operator term )* ;
    public final void expr() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expr");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(634, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:635:5: ( term ( operator term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:635:7: term ( operator term )*
            {
            dbg.location(635,7);
            pushFollow(FOLLOW_term_in_expr2832);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(635,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:635:12: ( operator term )*
            try { dbg.enterSubRule(109);

            loop109:
            do {
                int alt109=2;
                try { dbg.enterDecision(109, decisionCanBacktrack[109]);

                try {
                    isCyclicDecision = true;
                    alt109 = dfa109.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(109);}

                switch (alt109) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:635:13: operator term
            	    {
            	    dbg.location(635,13);
            	    pushFollow(FOLLOW_operator_in_expr2835);
            	    operator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(635,22);
            	    pushFollow(FOLLOW_term_in_expr2837);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop109;
                }
            } while (true);
            } finally {dbg.exitSubRule(109);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(636, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:638:1: term : ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( WS )* ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(638, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:639:5: ( ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:639:7: ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( WS )*
            {
            dbg.location(639,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:639:7: ( unaryOperator )?
            int alt110=2;
            try { dbg.enterSubRule(110);
            try { dbg.enterDecision(110, decisionCanBacktrack[110]);

            int LA110_0 = input.LA(1);

            if ( (LA110_0==PLUS||LA110_0==MINUS) ) {
                alt110=1;
            }
            } finally {dbg.exitDecision(110);}

            switch (alt110) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:639:7: unaryOperator
                    {
                    dbg.location(639,7);
                    pushFollow(FOLLOW_unaryOperator_in_term2860);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(110);}

            dbg.location(640,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION ) | STRING | IDENT | GEN | URI | hexColor | function )
            int alt111=7;
            try { dbg.enterSubRule(111);
            try { dbg.enterDecision(111, decisionCanBacktrack[111]);

            try {
                isCyclicDecision = true;
                alt111 = dfa111.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(111);}

            switch (alt111) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:641:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION )
                    {
                    dbg.location(641,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:7: STRING
                    {
                    dbg.location(652,7);
                    match(input,STRING,FOLLOW_STRING_in_term3043); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:653:7: IDENT
                    {
                    dbg.location(653,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term3051); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:7: GEN
                    {
                    dbg.location(654,7);
                    match(input,GEN,FOLLOW_GEN_in_term3059); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:655:7: URI
                    {
                    dbg.location(655,7);
                    match(input,URI,FOLLOW_URI_in_term3067); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:7: hexColor
                    {
                    dbg.location(656,7);
                    pushFollow(FOLLOW_hexColor_in_term3075);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:657:7: function
                    {
                    dbg.location(657,7);
                    pushFollow(FOLLOW_function_in_term3083);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(111);}

            dbg.location(659,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:659:5: ( WS )*
            try { dbg.enterSubRule(112);

            loop112:
            do {
                int alt112=2;
                try { dbg.enterDecision(112, decisionCanBacktrack[112]);

                int LA112_0 = input.LA(1);

                if ( (LA112_0==WS) ) {
                    alt112=1;
                }


                } finally {dbg.exitDecision(112);}

                switch (alt112) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:659:5: WS
            	    {
            	    dbg.location(659,5);
            	    match(input,WS,FOLLOW_WS_in_term3095); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop112;
                }
            } while (true);
            } finally {dbg.exitSubRule(112);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(660, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:662:1: function : function_name ( WS )* LPAREN ( WS )* ( expr | ( attribute ( COMMA ( WS )* attribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(662, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:2: ( function_name ( WS )* LPAREN ( WS )* ( expr | ( attribute ( COMMA ( WS )* attribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:5: function_name ( WS )* LPAREN ( WS )* ( expr | ( attribute ( COMMA ( WS )* attribute )* ) ) RPAREN
            {
            dbg.location(663,5);
            pushFollow(FOLLOW_function_name_in_function3111);
            function_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(663,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:19: ( WS )*
            try { dbg.enterSubRule(113);

            loop113:
            do {
                int alt113=2;
                try { dbg.enterDecision(113, decisionCanBacktrack[113]);

                int LA113_0 = input.LA(1);

                if ( (LA113_0==WS) ) {
                    alt113=1;
                }


                } finally {dbg.exitDecision(113);}

                switch (alt113) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:19: WS
            	    {
            	    dbg.location(663,19);
            	    match(input,WS,FOLLOW_WS_in_function3113); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop113;
                }
            } while (true);
            } finally {dbg.exitSubRule(113);}

            dbg.location(664,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function3118); if (state.failed) return ;
            dbg.location(664,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:10: ( WS )*
            try { dbg.enterSubRule(114);

            loop114:
            do {
                int alt114=2;
                try { dbg.enterDecision(114, decisionCanBacktrack[114]);

                int LA114_0 = input.LA(1);

                if ( (LA114_0==WS) ) {
                    alt114=1;
                }


                } finally {dbg.exitDecision(114);}

                switch (alt114) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:10: WS
            	    {
            	    dbg.location(664,10);
            	    match(input,WS,FOLLOW_WS_in_function3120); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop114;
                }
            } while (true);
            } finally {dbg.exitSubRule(114);}

            dbg.location(665,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:665:3: ( expr | ( attribute ( COMMA ( WS )* attribute )* ) )
            int alt117=2;
            try { dbg.enterSubRule(117);
            try { dbg.enterDecision(117, decisionCanBacktrack[117]);

            try {
                isCyclicDecision = true;
                alt117 = dfa117.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(117);}

            switch (alt117) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:666:4: expr
                    {
                    dbg.location(666,4);
                    pushFollow(FOLLOW_expr_in_function3131);
                    expr();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:6: ( attribute ( COMMA ( WS )* attribute )* )
                    {
                    dbg.location(668,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:6: ( attribute ( COMMA ( WS )* attribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:669:5: attribute ( COMMA ( WS )* attribute )*
                    {
                    dbg.location(669,5);
                    pushFollow(FOLLOW_attribute_in_function3149);
                    attribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(669,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:669:15: ( COMMA ( WS )* attribute )*
                    try { dbg.enterSubRule(116);

                    loop116:
                    do {
                        int alt116=2;
                        try { dbg.enterDecision(116, decisionCanBacktrack[116]);

                        int LA116_0 = input.LA(1);

                        if ( (LA116_0==COMMA) ) {
                            alt116=1;
                        }


                        } finally {dbg.exitDecision(116);}

                        switch (alt116) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:669:16: COMMA ( WS )* attribute
                    	    {
                    	    dbg.location(669,16);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function3152); if (state.failed) return ;
                    	    dbg.location(669,22);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:669:22: ( WS )*
                    	    try { dbg.enterSubRule(115);

                    	    loop115:
                    	    do {
                    	        int alt115=2;
                    	        try { dbg.enterDecision(115, decisionCanBacktrack[115]);

                    	        int LA115_0 = input.LA(1);

                    	        if ( (LA115_0==WS) ) {
                    	            alt115=1;
                    	        }


                    	        } finally {dbg.exitDecision(115);}

                    	        switch (alt115) {
                    	    	case 1 :
                    	    	    dbg.enterAlt(1);

                    	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:669:22: WS
                    	    	    {
                    	    	    dbg.location(669,22);
                    	    	    match(input,WS,FOLLOW_WS_in_function3154); if (state.failed) return ;

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop115;
                    	        }
                    	    } while (true);
                    	    } finally {dbg.exitSubRule(115);}

                    	    dbg.location(669,26);
                    	    pushFollow(FOLLOW_attribute_in_function3157);
                    	    attribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop116;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(116);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(117);}

            dbg.location(672,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function3178); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 

        }
        finally {
        }
        dbg.location(673, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:679:1: function_name : ( IDENT COLON )? IDENT ( DOT IDENT )* ;
    public final void function_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(679, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:683:2: ( ( IDENT COLON )? IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:683:4: ( IDENT COLON )? IDENT ( DOT IDENT )*
            {
            dbg.location(683,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:683:4: ( IDENT COLON )?
            int alt118=2;
            try { dbg.enterSubRule(118);
            try { dbg.enterDecision(118, decisionCanBacktrack[118]);

            int LA118_0 = input.LA(1);

            if ( (LA118_0==IDENT) ) {
                int LA118_1 = input.LA(2);

                if ( (LA118_1==COLON) ) {
                    alt118=1;
                }
            }
            } finally {dbg.exitDecision(118);}

            switch (alt118) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:683:5: IDENT COLON
                    {
                    dbg.location(683,5);
                    match(input,IDENT,FOLLOW_IDENT_in_function_name3226); if (state.failed) return ;
                    dbg.location(683,11);
                    match(input,COLON,FOLLOW_COLON_in_function_name3228); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(118);}

            dbg.location(683,19);
            match(input,IDENT,FOLLOW_IDENT_in_function_name3232); if (state.failed) return ;
            dbg.location(683,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:683:25: ( DOT IDENT )*
            try { dbg.enterSubRule(119);

            loop119:
            do {
                int alt119=2;
                try { dbg.enterDecision(119, decisionCanBacktrack[119]);

                int LA119_0 = input.LA(1);

                if ( (LA119_0==DOT) ) {
                    alt119=1;
                }


                } finally {dbg.exitDecision(119);}

                switch (alt119) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:683:26: DOT IDENT
            	    {
            	    dbg.location(683,26);
            	    match(input,DOT,FOLLOW_DOT_in_function_name3235); if (state.failed) return ;
            	    dbg.location(683,30);
            	    match(input,IDENT,FOLLOW_IDENT_in_function_name3237); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop119;
                }
            } while (true);
            } finally {dbg.exitSubRule(119);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(684, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:686:1: attribute : attrname ( WS )* OPEQ ( WS )* attrvalue ;
    public final void attribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(686, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:687:2: ( attrname ( WS )* OPEQ ( WS )* attrvalue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:687:4: attrname ( WS )* OPEQ ( WS )* attrvalue
            {
            dbg.location(687,4);
            pushFollow(FOLLOW_attrname_in_attribute3259);
            attrname();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(687,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:687:13: ( WS )*
            try { dbg.enterSubRule(120);

            loop120:
            do {
                int alt120=2;
                try { dbg.enterDecision(120, decisionCanBacktrack[120]);

                int LA120_0 = input.LA(1);

                if ( (LA120_0==WS) ) {
                    alt120=1;
                }


                } finally {dbg.exitDecision(120);}

                switch (alt120) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:687:13: WS
            	    {
            	    dbg.location(687,13);
            	    match(input,WS,FOLLOW_WS_in_attribute3261); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop120;
                }
            } while (true);
            } finally {dbg.exitSubRule(120);}

            dbg.location(687,17);
            match(input,OPEQ,FOLLOW_OPEQ_in_attribute3264); if (state.failed) return ;
            dbg.location(687,22);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:687:22: ( WS )*
            try { dbg.enterSubRule(121);

            loop121:
            do {
                int alt121=2;
                try { dbg.enterDecision(121, decisionCanBacktrack[121]);

                int LA121_0 = input.LA(1);

                if ( (LA121_0==WS) ) {
                    alt121=1;
                }


                } finally {dbg.exitDecision(121);}

                switch (alt121) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:687:22: WS
            	    {
            	    dbg.location(687,22);
            	    match(input,WS,FOLLOW_WS_in_attribute3266); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop121;
                }
            } while (true);
            } finally {dbg.exitSubRule(121);}

            dbg.location(687,26);
            pushFollow(FOLLOW_attrvalue_in_attribute3269);
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
        dbg.location(688, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:690:1: attrname : IDENT ( DOT IDENT )* ;
    public final void attrname() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrname");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(690, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:2: ( IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:4: IDENT ( DOT IDENT )*
            {
            dbg.location(691,4);
            match(input,IDENT,FOLLOW_IDENT_in_attrname3284); if (state.failed) return ;
            dbg.location(691,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:10: ( DOT IDENT )*
            try { dbg.enterSubRule(122);

            loop122:
            do {
                int alt122=2;
                try { dbg.enterDecision(122, decisionCanBacktrack[122]);

                int LA122_0 = input.LA(1);

                if ( (LA122_0==DOT) ) {
                    alt122=1;
                }


                } finally {dbg.exitDecision(122);}

                switch (alt122) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:11: DOT IDENT
            	    {
            	    dbg.location(691,11);
            	    match(input,DOT,FOLLOW_DOT_in_attrname3287); if (state.failed) return ;
            	    dbg.location(691,15);
            	    match(input,IDENT,FOLLOW_IDENT_in_attrname3289); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop122;
                }
            } while (true);
            } finally {dbg.exitSubRule(122);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(692, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:694:1: attrvalue : expr ;
    public final void attrvalue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrvalue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(694, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:2: ( expr )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:4: expr
            {
            dbg.location(695,4);
            pushFollow(FOLLOW_expr_in_attrvalue3303);
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
        dbg.location(696, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:698:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(698, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:7: HASH
            {
            dbg.location(699,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor3321); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(700, 5);

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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:19: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:20: esPred
        {
        dbg.location(466,20);
        pushFollow(FOLLOW_esPred_in_synpred1_Css31700);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:6: esPred
        {
        dbg.location(468,6);
        pushFollow(FOLLOW_esPred_in_synpred2_Css31718);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:7: ( nsPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:8: nsPred
        {
        dbg.location(485,8);
        pushFollow(FOLLOW_nsPred_in_synpred3_Css31769);
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


    protected DFA84 dfa84 = new DFA84(this);
    protected DFA87 dfa87 = new DFA87(this);
    protected DFA96 dfa96 = new DFA96(this);
    protected DFA106 dfa106 = new DFA106(this);
    protected DFA109 dfa109 = new DFA109(this);
    protected DFA111 dfa111 = new DFA111(this);
    protected DFA117 dfa117 = new DFA117(this);
    static final String DFA84_eotS =
        "\31\uffff";
    static final String DFA84_eofS =
        "\31\uffff";
    static final String DFA84_minS =
        "\1\6\1\uffff\1\0\1\67\1\6\1\4\1\6\1\uffff\2\0\4\4\1\0\2\4\1\0\7"+
        "\4";
    static final String DFA84_maxS =
        "\1\162\1\uffff\1\0\1\67\1\23\1\62\1\23\1\uffff\2\0\1\76\1\6\1\62"+
        "\1\6\1\0\1\76\1\7\1\0\1\76\1\62\1\6\1\7\3\76";
    static final String DFA84_acceptS =
        "\1\uffff\1\2\5\uffff\1\1\21\uffff";
    static final String DFA84_specialS =
        "\2\uffff\1\2\5\uffff\1\3\1\1\4\uffff\1\0\2\uffff\1\4\7\uffff}>";
    static final String[] DFA84_transitionS = {
            "\1\1\6\uffff\1\1\1\uffff\1\1\3\uffff\1\1\27\uffff\1\6\1\uffff"+
            "\3\1\1\uffff\2\1\1\2\1\4\1\5\1\6\73\uffff\1\3",
            "",
            "\1\uffff",
            "\1\10",
            "\1\11\14\uffff\1\11",
            "\1\15\1\uffff\1\12\52\uffff\1\14\1\13",
            "\1\16\14\uffff\1\16",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\17\55\uffff\1\13\5\uffff\6\20\1\21",
            "\1\15\1\uffff\1\22",
            "\1\23\55\uffff\1\24",
            "\1\15\1\uffff\1\22",
            "\1\uffff",
            "\1\17\55\uffff\1\13\5\uffff\6\20\1\21",
            "\1\25\1\uffff\2\26",
            "\1\uffff",
            "\1\27\63\uffff\6\20\1\21",
            "\1\23\55\uffff\1\24",
            "\1\15\1\uffff\1\22",
            "\1\25\1\uffff\2\26",
            "\1\30\71\uffff\1\21",
            "\1\27\63\uffff\6\20\1\21",
            "\1\30\71\uffff\1\21"
    };

    static final short[] DFA84_eot = DFA.unpackEncodedString(DFA84_eotS);
    static final short[] DFA84_eof = DFA.unpackEncodedString(DFA84_eofS);
    static final char[] DFA84_min = DFA.unpackEncodedStringToUnsignedChars(DFA84_minS);
    static final char[] DFA84_max = DFA.unpackEncodedStringToUnsignedChars(DFA84_maxS);
    static final short[] DFA84_accept = DFA.unpackEncodedString(DFA84_acceptS);
    static final short[] DFA84_special = DFA.unpackEncodedString(DFA84_specialS);
    static final short[][] DFA84_transition;

    static {
        int numStates = DFA84_transitionS.length;
        DFA84_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA84_transition[i] = DFA.unpackEncodedString(DFA84_transitionS[i]);
        }
    }

    class DFA84 extends DFA {

        public DFA84(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 84;
            this.eot = DFA84_eot;
            this.eof = DFA84_eof;
            this.min = DFA84_min;
            this.max = DFA84_max;
            this.accept = DFA84_accept;
            this.special = DFA84_special;
            this.transition = DFA84_transition;
        }
        public String getDescription() {
            return "()* loopback of 466:18: ( ( esPred )=> elementSubsequent )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA84_14 = input.LA(1);

                         
                        int index84_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 7;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index84_14);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA84_9 = input.LA(1);

                         
                        int index84_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 7;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index84_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA84_2 = input.LA(1);

                         
                        int index84_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 7;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index84_2);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA84_8 = input.LA(1);

                         
                        int index84_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 7;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index84_8);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA84_17 = input.LA(1);

                         
                        int index84_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 7;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index84_17);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 84, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA87_eotS =
        "\7\uffff";
    static final String DFA87_eofS =
        "\7\uffff";
    static final String DFA87_minS =
        "\1\6\1\0\1\uffff\1\4\1\uffff\1\4\1\0";
    static final String DFA87_maxS =
        "\1\62\1\0\1\uffff\1\162\1\uffff\1\162\1\0";
    static final String DFA87_acceptS =
        "\2\uffff\1\1\1\uffff\1\2\2\uffff";
    static final String DFA87_specialS =
        "\1\1\1\0\4\uffff\1\2}>";
    static final String[] DFA87_transitionS = {
            "\1\1\14\uffff\1\4\35\uffff\1\3\1\2",
            "\1\uffff",
            "",
            "\1\5\1\uffff\1\4\6\uffff\1\4\1\uffff\1\4\3\uffff\1\4\27\uffff"+
            "\1\4\1\uffff\3\4\1\uffff\1\4\1\6\4\4\73\uffff\1\4",
            "",
            "\1\5\1\uffff\1\4\6\uffff\1\4\1\uffff\1\4\3\uffff\1\4\27\uffff"+
            "\1\4\1\uffff\3\4\1\uffff\1\4\1\6\4\4\73\uffff\1\4",
            "\1\uffff"
    };

    static final short[] DFA87_eot = DFA.unpackEncodedString(DFA87_eotS);
    static final short[] DFA87_eof = DFA.unpackEncodedString(DFA87_eofS);
    static final char[] DFA87_min = DFA.unpackEncodedStringToUnsignedChars(DFA87_minS);
    static final char[] DFA87_max = DFA.unpackEncodedStringToUnsignedChars(DFA87_maxS);
    static final short[] DFA87_accept = DFA.unpackEncodedString(DFA87_acceptS);
    static final short[] DFA87_special = DFA.unpackEncodedString(DFA87_specialS);
    static final short[][] DFA87_transition;

    static {
        int numStates = DFA87_transitionS.length;
        DFA87_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA87_transition[i] = DFA.unpackEncodedString(DFA87_transitionS[i]);
        }
    }

    class DFA87 extends DFA {

        public DFA87(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 87;
            this.eot = DFA87_eot;
            this.eof = DFA87_eof;
            this.min = DFA87_min;
            this.max = DFA87_max;
            this.accept = DFA87_accept;
            this.special = DFA87_special;
            this.transition = DFA87_transition;
        }
        public String getDescription() {
            return "485:6: ( ( nsPred )=> namespace_wqname_prefix )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA87_1 = input.LA(1);

                         
                        int index87_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 2;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index87_1);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA87_0 = input.LA(1);

                         
                        int index87_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA87_0==IDENT) ) {s = 1;}

                        else if ( (LA87_0==PIPE) && (synpred3_Css3())) {s = 2;}

                        else if ( (LA87_0==STAR) ) {s = 3;}

                        else if ( (LA87_0==GEN) ) {s = 4;}

                         
                        input.seek(index87_0);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA87_6 = input.LA(1);

                         
                        int index87_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 2;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index87_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 87, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA96_eotS =
        "\5\uffff";
    static final String DFA96_eofS =
        "\5\uffff";
    static final String DFA96_minS =
        "\2\4\2\uffff\1\4";
    static final String DFA96_maxS =
        "\1\62\1\76\2\uffff\1\76";
    static final String DFA96_acceptS =
        "\2\uffff\1\1\1\2\1\uffff";
    static final String DFA96_specialS =
        "\5\uffff}>";
    static final String[] DFA96_transitionS = {
            "\1\3\1\uffff\1\1\52\uffff\2\2",
            "\1\4\55\uffff\1\2\5\uffff\7\3",
            "",
            "",
            "\1\4\55\uffff\1\2\5\uffff\7\3"
    };

    static final short[] DFA96_eot = DFA.unpackEncodedString(DFA96_eotS);
    static final short[] DFA96_eof = DFA.unpackEncodedString(DFA96_eofS);
    static final char[] DFA96_min = DFA.unpackEncodedStringToUnsignedChars(DFA96_minS);
    static final char[] DFA96_max = DFA.unpackEncodedStringToUnsignedChars(DFA96_maxS);
    static final short[] DFA96_accept = DFA.unpackEncodedString(DFA96_acceptS);
    static final short[] DFA96_special = DFA.unpackEncodedString(DFA96_specialS);
    static final short[][] DFA96_transition;

    static {
        int numStates = DFA96_transitionS.length;
        DFA96_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA96_transition[i] = DFA.unpackEncodedString(DFA96_transitionS[i]);
        }
    }

    class DFA96 extends DFA {

        public DFA96(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 96;
            this.eot = DFA96_eot;
            this.eof = DFA96_eof;
            this.min = DFA96_min;
            this.max = DFA96_max;
            this.accept = DFA96_accept;
            this.special = DFA96_special;
            this.transition = DFA96_transition;
        }
        public String getDescription() {
            return "548:6: ( namespace_wqname_prefix )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA106_eotS =
        "\4\uffff";
    static final String DFA106_eofS =
        "\4\uffff";
    static final String DFA106_minS =
        "\2\4\2\uffff";
    static final String DFA106_maxS =
        "\2\162\2\uffff";
    static final String DFA106_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA106_specialS =
        "\4\uffff}>";
    static final String[] DFA106_transitionS = {
            "\1\1\1\uffff\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\27\uffff"+
            "\1\3\1\uffff\3\3\1\uffff\6\3\10\uffff\1\2\62\uffff\1\3",
            "\1\1\1\uffff\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\27\uffff"+
            "\1\3\1\uffff\3\3\1\uffff\6\3\10\uffff\1\2\62\uffff\1\3",
            "",
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
            return "595:17: ( ( WS )* LPAREN ( WS )* ( ( IDENT | GEN ) ( WS )* )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA109_eotS =
        "\12\uffff";
    static final String DFA109_eofS =
        "\12\uffff";
    static final String DFA109_minS =
        "\1\6\1\uffff\1\4\1\uffff\2\4\1\6\3\4";
    static final String DFA109_maxS =
        "\1\112\1\uffff\1\112\1\uffff\2\112\1\6\1\112\2\77";
    static final String DFA109_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\6\uffff";
    static final String DFA109_specialS =
        "\12\uffff}>";
    static final String[] DFA109_transitionS = {
            "\3\3\1\uffff\1\1\3\uffff\1\1\1\2\3\uffff\1\3\30\uffff\2\3\2"+
            "\uffff\1\3\2\uffff\1\3\14\uffff\2\1\11\3",
            "",
            "\1\4\1\uffff\1\5\2\3\12\uffff\1\3\31\uffff\1\3\2\uffff\1\3"+
            "\2\uffff\1\3\16\uffff\11\3",
            "",
            "\1\4\1\uffff\1\5\2\3\12\uffff\1\3\31\uffff\1\3\2\uffff\1\3"+
            "\2\uffff\1\3\16\uffff\11\3",
            "\1\7\1\uffff\3\3\1\uffff\1\3\3\uffff\2\3\3\uffff\1\3\27\uffff"+
            "\3\3\2\uffff\1\3\2\uffff\1\3\1\6\3\uffff\1\1\6\uffff\14\3",
            "\1\10",
            "\1\7\1\uffff\3\3\1\uffff\1\3\3\uffff\2\3\3\uffff\1\3\30\uffff"+
            "\2\3\2\uffff\1\3\2\uffff\1\3\4\uffff\1\1\6\uffff\14\3",
            "\1\11\57\uffff\1\6\3\uffff\1\1\6\uffff\1\3",
            "\1\11\63\uffff\1\1\6\uffff\1\3"
    };

    static final short[] DFA109_eot = DFA.unpackEncodedString(DFA109_eotS);
    static final short[] DFA109_eof = DFA.unpackEncodedString(DFA109_eofS);
    static final char[] DFA109_min = DFA.unpackEncodedStringToUnsignedChars(DFA109_minS);
    static final char[] DFA109_max = DFA.unpackEncodedStringToUnsignedChars(DFA109_maxS);
    static final short[] DFA109_accept = DFA.unpackEncodedString(DFA109_acceptS);
    static final short[] DFA109_special = DFA.unpackEncodedString(DFA109_specialS);
    static final short[][] DFA109_transition;

    static {
        int numStates = DFA109_transitionS.length;
        DFA109_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA109_transition[i] = DFA.unpackEncodedString(DFA109_transitionS[i]);
        }
    }

    class DFA109 extends DFA {

        public DFA109(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 109;
            this.eot = DFA109_eot;
            this.eof = DFA109_eof;
            this.min = DFA109_min;
            this.max = DFA109_max;
            this.accept = DFA109_accept;
            this.special = DFA109_special;
            this.transition = DFA109_transition;
        }
        public String getDescription() {
            return "()* loopback of 635:12: ( operator term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA111_eotS =
        "\12\uffff";
    static final String DFA111_eofS =
        "\12\uffff";
    static final String DFA111_minS =
        "\1\6\2\uffff\1\4\4\uffff\1\4\1\uffff";
    static final String DFA111_maxS =
        "\1\112\2\uffff\1\112\4\uffff\1\112\1\uffff";
    static final String DFA111_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\7\1\uffff\1\3";
    static final String DFA111_specialS =
        "\12\uffff}>";
    static final String[] DFA111_transitionS = {
            "\1\3\1\2\1\5\12\uffff\1\4\37\uffff\1\6\16\uffff\11\1",
            "",
            "",
            "\1\10\1\uffff\3\11\1\uffff\1\11\3\uffff\2\11\3\uffff\1\11\27"+
            "\uffff\1\7\2\11\2\uffff\1\11\2\uffff\1\11\1\7\12\uffff\1\7\13"+
            "\11",
            "",
            "",
            "",
            "",
            "\1\10\1\uffff\3\11\1\uffff\1\11\3\uffff\2\11\3\uffff\1\11\30"+
            "\uffff\2\11\2\uffff\1\11\2\uffff\1\11\13\uffff\1\7\13\11",
            ""
    };

    static final short[] DFA111_eot = DFA.unpackEncodedString(DFA111_eotS);
    static final short[] DFA111_eof = DFA.unpackEncodedString(DFA111_eofS);
    static final char[] DFA111_min = DFA.unpackEncodedStringToUnsignedChars(DFA111_minS);
    static final char[] DFA111_max = DFA.unpackEncodedStringToUnsignedChars(DFA111_maxS);
    static final short[] DFA111_accept = DFA.unpackEncodedString(DFA111_acceptS);
    static final short[] DFA111_special = DFA.unpackEncodedString(DFA111_specialS);
    static final short[][] DFA111_transition;

    static {
        int numStates = DFA111_transitionS.length;
        DFA111_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA111_transition[i] = DFA.unpackEncodedString(DFA111_transitionS[i]);
        }
    }

    class DFA111 extends DFA {

        public DFA111(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 111;
            this.eot = DFA111_eot;
            this.eof = DFA111_eof;
            this.min = DFA111_min;
            this.max = DFA111_max;
            this.accept = DFA111_accept;
            this.special = DFA111_special;
            this.transition = DFA111_transition;
        }
        public String getDescription() {
            return "640:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION ) | STRING | IDENT | GEN | URI | hexColor | function )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA117_eotS =
        "\10\uffff";
    static final String DFA117_eofS =
        "\10\uffff";
    static final String DFA117_minS =
        "\1\6\1\uffff\2\4\1\6\1\uffff\2\4";
    static final String DFA117_maxS =
        "\1\112\1\uffff\2\112\1\6\1\uffff\2\77";
    static final String DFA117_acceptS =
        "\1\uffff\1\1\3\uffff\1\2\2\uffff";
    static final String DFA117_specialS =
        "\10\uffff}>";
    static final String[] DFA117_transitionS = {
            "\1\2\2\1\12\uffff\1\1\31\uffff\1\1\2\uffff\1\1\2\uffff\1\1\16"+
            "\uffff\11\1",
            "",
            "\1\3\1\uffff\3\1\6\uffff\1\1\3\uffff\1\1\27\uffff\3\1\2\uffff"+
            "\1\1\2\uffff\1\1\1\4\3\uffff\1\5\6\uffff\2\1\1\uffff\11\1",
            "\1\3\1\uffff\3\1\6\uffff\1\1\3\uffff\1\1\30\uffff\2\1\2\uffff"+
            "\1\1\2\uffff\1\1\4\uffff\1\5\6\uffff\2\1\1\uffff\11\1",
            "\1\6",
            "",
            "\1\7\57\uffff\1\4\3\uffff\1\5\6\uffff\1\1",
            "\1\7\63\uffff\1\5\6\uffff\1\1"
    };

    static final short[] DFA117_eot = DFA.unpackEncodedString(DFA117_eotS);
    static final short[] DFA117_eof = DFA.unpackEncodedString(DFA117_eofS);
    static final char[] DFA117_min = DFA.unpackEncodedStringToUnsignedChars(DFA117_minS);
    static final char[] DFA117_max = DFA.unpackEncodedStringToUnsignedChars(DFA117_maxS);
    static final short[] DFA117_accept = DFA.unpackEncodedString(DFA117_acceptS);
    static final short[] DFA117_special = DFA.unpackEncodedString(DFA117_specialS);
    static final short[][] DFA117_transition;

    static {
        int numStates = DFA117_transitionS.length;
        DFA117_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA117_transition[i] = DFA.unpackEncodedString(DFA117_transitionS[i]);
        }
    }

    class DFA117 extends DFA {

        public DFA117(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 117;
            this.eot = DFA117_eot;
            this.eof = DFA117_eof;
            this.min = DFA117_min;
            this.max = DFA117_max;
            this.accept = DFA117_accept;
            this.special = DFA117_special;
            this.transition = DFA117_transition;
        }
        public String getDescription() {
            return "665:3: ( expr | ( attribute ( COMMA ( WS )* attribute )* ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_charSet_in_styleSheet119 = new BitSet(new long[]{0x007E080007181870L,0x0004000000000000L});
    public static final BitSet FOLLOW_WS_in_styleSheet127 = new BitSet(new long[]{0x007E080007181870L,0x0004000000000000L});
    public static final BitSet FOLLOW_imports_in_styleSheet139 = new BitSet(new long[]{0x007E080007181870L,0x0004000000000000L});
    public static final BitSet FOLLOW_WS_in_styleSheet141 = new BitSet(new long[]{0x007E080007181870L,0x0004000000000000L});
    public static final BitSet FOLLOW_namespace_in_styleSheet156 = new BitSet(new long[]{0x007E080007181060L,0x0004000000000000L});
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
    public static final BitSet FOLLOW_resourceIdentifier_in_imports302 = new BitSet(new long[]{0x80000000000B0450L});
    public static final BitSet FOLLOW_WS_in_imports305 = new BitSet(new long[]{0x80000000000B0450L});
    public static final BitSet FOLLOW_media_query_list_in_imports308 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_SEMI_in_imports310 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEDIA_SYM_in_media331 = new BitSet(new long[]{0x80000000000B2050L});
    public static final BitSet FOLLOW_WS_in_media333 = new BitSet(new long[]{0x80000000000B2050L});
    public static final BitSet FOLLOW_media_query_list_in_media336 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_media346 = new BitSet(new long[]{0x007E080001084050L,0x0004000000000000L});
    public static final BitSet FOLLOW_WS_in_media348 = new BitSet(new long[]{0x007E080001084050L,0x0004000000000000L});
    public static final BitSet FOLLOW_ruleSet_in_media367 = new BitSet(new long[]{0x007E080001084050L,0x0004000000000000L});
    public static final BitSet FOLLOW_page_in_media371 = new BitSet(new long[]{0x007E080001084050L,0x0004000000000000L});
    public static final BitSet FOLLOW_WS_in_media375 = new BitSet(new long[]{0x007E080001084050L,0x0004000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_media389 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_media_query_in_media_query_list409 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_COMMA_in_media_query_list413 = new BitSet(new long[]{0x80000000000B0050L});
    public static final BitSet FOLLOW_WS_in_media_query_list415 = new BitSet(new long[]{0x80000000000B0050L});
    public static final BitSet FOLLOW_media_query_in_media_query_list418 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_set_in_media_query437 = new BitSet(new long[]{0x00000000000B0050L});
    public static final BitSet FOLLOW_WS_in_media_query445 = new BitSet(new long[]{0x00000000000B0050L});
    public static final BitSet FOLLOW_media_type_in_media_query452 = new BitSet(new long[]{0x0000000000040012L});
    public static final BitSet FOLLOW_WS_in_media_query454 = new BitSet(new long[]{0x0000000000040012L});
    public static final BitSet FOLLOW_AND_in_media_query459 = new BitSet(new long[]{0x80000000000B0050L});
    public static final BitSet FOLLOW_WS_in_media_query461 = new BitSet(new long[]{0x80000000000B0050L});
    public static final BitSet FOLLOW_media_expression_in_media_query464 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_media_expression_in_media_query472 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_AND_in_media_query476 = new BitSet(new long[]{0x80000000000B0050L});
    public static final BitSet FOLLOW_WS_in_media_query478 = new BitSet(new long[]{0x80000000000B0050L});
    public static final BitSet FOLLOW_media_expression_in_media_query481 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_set_in_media_type0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_media_expression512 = new BitSet(new long[]{0x0000000000000050L});
    public static final BitSet FOLLOW_WS_in_media_expression514 = new BitSet(new long[]{0x0000000000000050L});
    public static final BitSet FOLLOW_media_feature_in_media_expression517 = new BitSet(new long[]{0x0000080000000010L,0x0000000000000001L});
    public static final BitSet FOLLOW_WS_in_media_expression519 = new BitSet(new long[]{0x0000080000000010L,0x0000000000000001L});
    public static final BitSet FOLLOW_COLON_in_media_expression524 = new BitSet(new long[]{0x00092000000801D0L,0x00000000000007FCL});
    public static final BitSet FOLLOW_WS_in_media_expression526 = new BitSet(new long[]{0x00092000000801D0L,0x00000000000007FCL});
    public static final BitSet FOLLOW_expr_in_media_expression529 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_RPAREN_in_media_expression534 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_media_expression536 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_IDENT_in_media_feature547 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_medium564 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_medium574 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_bodyset_in_bodylist597 = new BitSet(new long[]{0x007E080007181042L,0x0004000000000000L});
    public static final BitSet FOLLOW_ruleSet_in_bodyset626 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_media_in_bodyset638 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_page_in_bodyset650 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_counterStyle_in_bodyset662 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_fontFace_in_bodyset674 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_moz_document_in_bodyset686 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_bodyset702 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document723 = new BitSet(new long[]{0x0000000000E00110L});
    public static final BitSet FOLLOW_WS_in_moz_document725 = new BitSet(new long[]{0x0000000000E00110L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document730 = new BitSet(new long[]{0x000000000000A010L});
    public static final BitSet FOLLOW_WS_in_moz_document732 = new BitSet(new long[]{0x000000000000A010L});
    public static final BitSet FOLLOW_COMMA_in_moz_document738 = new BitSet(new long[]{0x0000000000E00110L});
    public static final BitSet FOLLOW_WS_in_moz_document740 = new BitSet(new long[]{0x0000000000E00110L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document743 = new BitSet(new long[]{0x000000000000A010L});
    public static final BitSet FOLLOW_WS_in_moz_document745 = new BitSet(new long[]{0x000000000000A010L});
    public static final BitSet FOLLOW_LBRACE_in_moz_document752 = new BitSet(new long[]{0x007E080001084050L,0x0004000000000000L});
    public static final BitSet FOLLOW_WS_in_moz_document754 = new BitSet(new long[]{0x007E080001084050L,0x0004000000000000L});
    public static final BitSet FOLLOW_ruleSet_in_moz_document763 = new BitSet(new long[]{0x007E080001084050L,0x0004000000000000L});
    public static final BitSet FOLLOW_page_in_moz_document767 = new BitSet(new long[]{0x007E080001084050L,0x0004000000000000L});
    public static final BitSet FOLLOW_WS_in_moz_document771 = new BitSet(new long[]{0x007E080001084050L,0x0004000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_moz_document777 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_moz_document_function0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page819 = new BitSet(new long[]{0x0000080000002050L});
    public static final BitSet FOLLOW_WS_in_page821 = new BitSet(new long[]{0x0000080000002050L});
    public static final BitSet FOLLOW_IDENT_in_page826 = new BitSet(new long[]{0x0000080000002010L});
    public static final BitSet FOLLOW_WS_in_page828 = new BitSet(new long[]{0x0000080000002010L});
    public static final BitSet FOLLOW_pseudoPage_in_page835 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_WS_in_page837 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_LBRACE_in_page850 = new BitSet(new long[]{0x000007FFF8084450L});
    public static final BitSet FOLLOW_WS_in_page852 = new BitSet(new long[]{0x000007FFF8084450L});
    public static final BitSet FOLLOW_declaration_in_page920 = new BitSet(new long[]{0x0000000000004400L});
    public static final BitSet FOLLOW_margin_in_page922 = new BitSet(new long[]{0x0000000000004410L});
    public static final BitSet FOLLOW_WS_in_page924 = new BitSet(new long[]{0x0000000000004410L});
    public static final BitSet FOLLOW_SEMI_in_page930 = new BitSet(new long[]{0x000007FFF8084450L});
    public static final BitSet FOLLOW_WS_in_page932 = new BitSet(new long[]{0x000007FFF8084450L});
    public static final BitSet FOLLOW_declaration_in_page936 = new BitSet(new long[]{0x0000000000004400L});
    public static final BitSet FOLLOW_margin_in_page938 = new BitSet(new long[]{0x0000000000004410L});
    public static final BitSet FOLLOW_WS_in_page940 = new BitSet(new long[]{0x0000000000004410L});
    public static final BitSet FOLLOW_RBRACE_in_page955 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNTER_STYLE_SYM_in_counterStyle976 = new BitSet(new long[]{0x0000000000000050L});
    public static final BitSet FOLLOW_WS_in_counterStyle978 = new BitSet(new long[]{0x0000000000000050L});
    public static final BitSet FOLLOW_IDENT_in_counterStyle981 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_WS_in_counterStyle983 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_LBRACE_in_counterStyle994 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_WS_in_counterStyle996 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_counterStyle999 = new BitSet(new long[]{0x0000000000084440L});
    public static final BitSet FOLLOW_declarations_in_counterStyle1003 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_counterStyle1013 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FONT_FACE_SYM_in_fontFace1034 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_WS_in_fontFace1036 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_LBRACE_in_fontFace1047 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_WS_in_fontFace1049 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_fontFace1052 = new BitSet(new long[]{0x0000000000084440L});
    public static final BitSet FOLLOW_declarations_in_fontFace1056 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_fontFace1066 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_margin_sym_in_margin1086 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_WS_in_margin1088 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_LBRACE_in_margin1091 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_WS_in_margin1093 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_margin1096 = new BitSet(new long[]{0x0000000000084440L});
    public static final BitSet FOLLOW_declarations_in_margin1098 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_margin1100 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_margin_sym0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage1329 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage1331 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_operator1352 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_operator1354 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_COMMA_in_operator1363 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_operator1365 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_PLUS_in_combinator1393 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator1395 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_GREATER_in_combinator1404 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator1406 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_TILDE_in_combinator1415 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator1417 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_property1477 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_property1485 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_selectorsGroup_in_ruleSet1510 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_ruleSet1520 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_WS_in_ruleSet1522 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_ruleSet1525 = new BitSet(new long[]{0x0000000000084440L});
    public static final BitSet FOLLOW_declarations_in_ruleSet1539 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_ruleSet1549 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_declarations1587 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_SEMI_in_declarations1591 = new BitSet(new long[]{0x0000000000080452L});
    public static final BitSet FOLLOW_WS_in_declarations1593 = new BitSet(new long[]{0x0000000000080452L});
    public static final BitSet FOLLOW_declaration_in_declarations1596 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup1620 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup1623 = new BitSet(new long[]{0x007E080000080050L,0x0004000000000000L});
    public static final BitSet FOLLOW_WS_in_selectorsGroup1625 = new BitSet(new long[]{0x007E080000080050L,0x0004000000000000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup1628 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector1651 = new BitSet(new long[]{0x007EE80000080042L,0x0004000000000000L});
    public static final BitSet FOLLOW_combinator_in_selector1654 = new BitSet(new long[]{0x007E080000080040L,0x0004000000000000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector1656 = new BitSet(new long[]{0x007EE80000080042L,0x0004000000000000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence1696 = new BitSet(new long[]{0x007E080000080042L,0x0004000000000000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence1703 = new BitSet(new long[]{0x007E080000080042L,0x0004000000000000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence1721 = new BitSet(new long[]{0x007E080000080042L,0x0004000000000000L});
    public static final BitSet FOLLOW_namespace_wqname_prefix_in_typeSelector1772 = new BitSet(new long[]{0x0006000000080040L});
    public static final BitSet FOLLOW_elementName_in_typeSelector1778 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_typeSelector1780 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_set_in_nsPred1805 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_PIPE_in_nsPred1813 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespace_prefix_in_namespace_wqname_prefix1843 = new BitSet(new long[]{0x0004000000000010L});
    public static final BitSet FOLLOW_WS_in_namespace_wqname_prefix1845 = new BitSet(new long[]{0x0004000000000010L});
    public static final BitSet FOLLOW_PIPE_in_namespace_wqname_prefix1851 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespace_wildcard_prefix_in_namespace_wqname_prefix1858 = new BitSet(new long[]{0x0004000000000010L});
    public static final BitSet FOLLOW_WS_in_namespace_wqname_prefix1860 = new BitSet(new long[]{0x0004000000000010L});
    public static final BitSet FOLLOW_PIPE_in_namespace_wqname_prefix1863 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_namespace_wildcard_prefix1885 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent1961 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent1970 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_attrib_in_elementSubsequent1982 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent1994 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_elementSubsequent2006 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_HASH_in_cssId2029 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_114_in_cssId2035 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_NAME_in_cssId2037 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass2065 = new BitSet(new long[]{0x0000000000080040L});
    public static final BitSet FOLLOW_set_in_cssClass2067 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_attrib2140 = new BitSet(new long[]{0x0006000000000050L});
    public static final BitSet FOLLOW_namespace_wqname_prefix_in_attrib2147 = new BitSet(new long[]{0x0006000000000050L});
    public static final BitSet FOLLOW_WS_in_attrib2150 = new BitSet(new long[]{0x0006000000000050L});
    public static final BitSet FOLLOW_attrib_name_in_attrib2161 = new BitSet(new long[]{0x7F00000000000010L});
    public static final BitSet FOLLOW_WS_in_attrib2163 = new BitSet(new long[]{0x7F00000000000010L});
    public static final BitSet FOLLOW_set_in_attrib2205 = new BitSet(new long[]{0x00000000000000D0L});
    public static final BitSet FOLLOW_WS_in_attrib2385 = new BitSet(new long[]{0x00000000000000D0L});
    public static final BitSet FOLLOW_attrib_value_in_attrib2404 = new BitSet(new long[]{0x4000000000000010L});
    public static final BitSet FOLLOW_WS_in_attrib2422 = new BitSet(new long[]{0x4000000000000010L});
    public static final BitSet FOLLOW_RBRACKET_in_attrib2451 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_attrib_name2494 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_attrib_value2508 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pseudo2568 = new BitSet(new long[]{0x0000000000080040L});
    public static final BitSet FOLLOW_set_in_pseudo2590 = new BitSet(new long[]{0x8000000000000012L});
    public static final BitSet FOLLOW_WS_in_pseudo2639 = new BitSet(new long[]{0x8000000000000010L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo2642 = new BitSet(new long[]{0x0000000000080050L,0x0000000000000001L});
    public static final BitSet FOLLOW_WS_in_pseudo2644 = new BitSet(new long[]{0x0000000000080050L,0x0000000000000001L});
    public static final BitSet FOLLOW_set_in_pseudo2648 = new BitSet(new long[]{0x0000000000000010L,0x0000000000000001L});
    public static final BitSet FOLLOW_WS_in_pseudo2658 = new BitSet(new long[]{0x0000000000000010L,0x0000000000000001L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo2663 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_declaration2709 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_COLON_in_declaration2711 = new BitSet(new long[]{0x00092000000801D0L,0x00000000000007FCL});
    public static final BitSet FOLLOW_WS_in_declaration2713 = new BitSet(new long[]{0x00092000000801D0L,0x00000000000007FCL});
    public static final BitSet FOLLOW_expr_in_declaration2716 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000002L});
    public static final BitSet FOLLOW_prio_in_declaration2718 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio2811 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expr2832 = new BitSet(new long[]{0x00093000000881D2L,0x00000000000007FCL});
    public static final BitSet FOLLOW_operator_in_expr2835 = new BitSet(new long[]{0x00092000000801D0L,0x00000000000007FCL});
    public static final BitSet FOLLOW_term_in_expr2837 = new BitSet(new long[]{0x00093000000881D2L,0x00000000000007FCL});
    public static final BitSet FOLLOW_unaryOperator_in_term2860 = new BitSet(new long[]{0x00092000000801D0L,0x00000000000007FCL});
    public static final BitSet FOLLOW_set_in_term2881 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_STRING_in_term3043 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_IDENT_in_term3051 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_GEN_in_term3059 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_URI_in_term3067 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_hexColor_in_term3075 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_function_in_term3083 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_term3095 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_function_name_in_function3111 = new BitSet(new long[]{0x8000000000000010L});
    public static final BitSet FOLLOW_WS_in_function3113 = new BitSet(new long[]{0x8000000000000010L});
    public static final BitSet FOLLOW_LPAREN_in_function3118 = new BitSet(new long[]{0x00092000000801D0L,0x00000000000007FCL});
    public static final BitSet FOLLOW_WS_in_function3120 = new BitSet(new long[]{0x00092000000801D0L,0x00000000000007FCL});
    public static final BitSet FOLLOW_expr_in_function3131 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_attribute_in_function3149 = new BitSet(new long[]{0x0000000000008000L,0x0000000000000001L});
    public static final BitSet FOLLOW_COMMA_in_function3152 = new BitSet(new long[]{0x00092000000801D0L,0x00000000000007FCL});
    public static final BitSet FOLLOW_WS_in_function3154 = new BitSet(new long[]{0x00092000000801D0L,0x00000000000007FCL});
    public static final BitSet FOLLOW_attribute_in_function3157 = new BitSet(new long[]{0x0000000000008000L,0x0000000000000001L});
    public static final BitSet FOLLOW_RPAREN_in_function3178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_function_name3226 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_COLON_in_function_name3228 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_function_name3232 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_DOT_in_function_name3235 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_function_name3237 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_attrname_in_attribute3259 = new BitSet(new long[]{0x0100000000000010L});
    public static final BitSet FOLLOW_WS_in_attribute3261 = new BitSet(new long[]{0x0100000000000010L});
    public static final BitSet FOLLOW_OPEQ_in_attribute3264 = new BitSet(new long[]{0x00092000000801D0L,0x00000000000007FCL});
    public static final BitSet FOLLOW_WS_in_attribute3266 = new BitSet(new long[]{0x00092000000801D0L,0x00000000000007FCL});
    public static final BitSet FOLLOW_attrvalue_in_attribute3269 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_attrname3284 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_DOT_in_attrname3287 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_attrname3289 = new BitSet(new long[]{0x0010000000000002L});
    public static final BitSet FOLLOW_expr_in_attrvalue3303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor3321 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred1_Css31700 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred2_Css31718 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nsPred_in_synpred3_Css31769 = new BitSet(new long[]{0x0000000000000002L});

}