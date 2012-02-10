// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2012-02-10 10:03:54

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "WS", "NAMESPACE_SYM", "IDENT", "STRING", "URI", "CHARSET_SYM", "SEMI", "IMPORT_SYM", "MEDIA_SYM", "LBRACE", "RBRACE", "COMMA", "ONLY", "NOT", "AND", "GEN", "GENERIC_AT_RULE", "MOZ_DOCUMENT_SYM", "MOZ_URL_PREFIX", "MOZ_DOMAIN", "MOZ_REGEXP", "PAGE_SYM", "COUNTER_STYLE_SYM", "FONT_FACE_SYM", "TOPLEFTCORNER_SYM", "TOPLEFT_SYM", "TOPCENTER_SYM", "TOPRIGHT_SYM", "TOPRIGHTCORNER_SYM", "BOTTOMLEFTCORNER_SYM", "BOTTOMLEFT_SYM", "BOTTOMCENTER_SYM", "BOTTOMRIGHT_SYM", "BOTTOMRIGHTCORNER_SYM", "LEFTTOP_SYM", "LEFTMIDDLE_SYM", "LEFTBOTTOM_SYM", "RIGHTTOP_SYM", "RIGHTMIDDLE_SYM", "RIGHTBOTTOM_SYM", "COLON", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "STAR", "PIPE", "HASH", "DOT", "LBRACKET", "DCOLON", "NAME", "OPEQ", "INCLUDES", "DASHMATCH", "BEGINS", "ENDS", "CONTAINS", "RBRACKET", "LPAREN", "RPAREN", "IMPORTANT_SYM", "NUMBER", "PERCENTAGE", "LENGTH", "EMS", "EXS", "ANGLE", "TIME", "FREQ", "RESOLUTION", "DIMENSION", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "COMMENT", "CDO", "CDC", "INVALID", "NL", "'#'"
    };
    public static final int EOF=-1;
    public static final int T__115=115;
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
    public static final int GENERIC_AT_RULE=20;
    public static final int MOZ_DOCUMENT_SYM=21;
    public static final int MOZ_URL_PREFIX=22;
    public static final int MOZ_DOMAIN=23;
    public static final int MOZ_REGEXP=24;
    public static final int PAGE_SYM=25;
    public static final int COUNTER_STYLE_SYM=26;
    public static final int FONT_FACE_SYM=27;
    public static final int TOPLEFTCORNER_SYM=28;
    public static final int TOPLEFT_SYM=29;
    public static final int TOPCENTER_SYM=30;
    public static final int TOPRIGHT_SYM=31;
    public static final int TOPRIGHTCORNER_SYM=32;
    public static final int BOTTOMLEFTCORNER_SYM=33;
    public static final int BOTTOMLEFT_SYM=34;
    public static final int BOTTOMCENTER_SYM=35;
    public static final int BOTTOMRIGHT_SYM=36;
    public static final int BOTTOMRIGHTCORNER_SYM=37;
    public static final int LEFTTOP_SYM=38;
    public static final int LEFTMIDDLE_SYM=39;
    public static final int LEFTBOTTOM_SYM=40;
    public static final int RIGHTTOP_SYM=41;
    public static final int RIGHTMIDDLE_SYM=42;
    public static final int RIGHTBOTTOM_SYM=43;
    public static final int COLON=44;
    public static final int SOLIDUS=45;
    public static final int PLUS=46;
    public static final int GREATER=47;
    public static final int TILDE=48;
    public static final int MINUS=49;
    public static final int STAR=50;
    public static final int PIPE=51;
    public static final int HASH=52;
    public static final int DOT=53;
    public static final int LBRACKET=54;
    public static final int DCOLON=55;
    public static final int NAME=56;
    public static final int OPEQ=57;
    public static final int INCLUDES=58;
    public static final int DASHMATCH=59;
    public static final int BEGINS=60;
    public static final int ENDS=61;
    public static final int CONTAINS=62;
    public static final int RBRACKET=63;
    public static final int LPAREN=64;
    public static final int RPAREN=65;
    public static final int IMPORTANT_SYM=66;
    public static final int NUMBER=67;
    public static final int PERCENTAGE=68;
    public static final int LENGTH=69;
    public static final int EMS=70;
    public static final int EXS=71;
    public static final int ANGLE=72;
    public static final int TIME=73;
    public static final int FREQ=74;
    public static final int RESOLUTION=75;
    public static final int DIMENSION=76;
    public static final int HEXCHAR=77;
    public static final int NONASCII=78;
    public static final int UNICODE=79;
    public static final int ESCAPE=80;
    public static final int NMSTART=81;
    public static final int NMCHAR=82;
    public static final int URL=83;
    public static final int A=84;
    public static final int B=85;
    public static final int C=86;
    public static final int D=87;
    public static final int E=88;
    public static final int F=89;
    public static final int G=90;
    public static final int H=91;
    public static final int I=92;
    public static final int J=93;
    public static final int K=94;
    public static final int L=95;
    public static final int M=96;
    public static final int N=97;
    public static final int O=98;
    public static final int P=99;
    public static final int Q=100;
    public static final int R=101;
    public static final int S=102;
    public static final int T=103;
    public static final int U=104;
    public static final int V=105;
    public static final int W=106;
    public static final int X=107;
    public static final int Y=108;
    public static final int Z=109;
    public static final int COMMENT=110;
    public static final int CDO=111;
    public static final int CDC=112;
    public static final int INVALID=113;
    public static final int NL=114;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "term", "syncTo_IDENT_RBRACKET_LBRACE", "namespace", 
        "namespace_wqname_prefix", "attrib_value", "cssClass", "function_name", 
        "function", "declarations", "cssId", "margin", "fontFace", "charSet", 
        "bodyset", "media_query_list", "attrib_name", "synpred3_Css3", "selectorsGroup", 
        "namespace_wildcard_prefix", "selector", "media_expression", "ruleSet", 
        "synpred2_Css3", "declaration", "simpleSelectorSequence", "attrvalue", 
        "attrib", "media_query", "property", "styleSheet", "media_type", 
        "namespace_prefix", "nsPred", "bodylist", "moz_document", "imports", 
        "operator", "syncTo_IDENT_RBRACE", "elementSubsequent", "counterStyle", 
        "syncToFollow", "resourceIdentifier", "attrname", "media", "margin_sym", 
        "unaryOperator", "hexColor", "page", "typeSelector", "medium", "pseudoPage", 
        "elementName", "esPred", "generic_at_rule", "syncTo_RBRACE", "prio", 
        "combinator", "pseudo", "attribute", "synpred1_Css3", "expr", "media_feature", 
        "moz_document_function"
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
            false, false, false, false, true, true, false, true, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false
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
        
        /**
             * synces to next RBRACE "}" taking nesting into account
             */
            protected void syncToRBRACE(int nest)
            {
                //create error-recovery node
                dbg.enterRule(getGrammarFileName(), "recovery");

                try {
                    
                    for(;;) {
                        //read char
                        int c = input.LA(1);
                        
                        switch(c) {
                            case Token.EOF:
                                input.rewind();
                                return ;
                            case Css3Lexer.LBRACE:
                                nest++;
                                break;
                            case Css3Lexer.RBRACE:
                                nest--;
                                if(nest == 0) {
                                    //do not eat the final RBRACE
                                    return ;
                                }
                        }
                        
                        input.consume();
                                            
                    }

                } catch (Exception e) {

                  // Just ignore any errors here, we will just let the recognizer
                  // try to resync as normal - something must be very screwed.
                  //
                }
                finally {
                    dbg.exitRule(getGrammarFileName(), "recovery");
                }
            }
        



    // $ANTLR start "styleSheet"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:298:1: styleSheet : ( charSet )? ( WS )* ( imports ( WS )* )* ( namespace )* bodylist EOF ;
    public final void styleSheet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "styleSheet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(298, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:299:5: ( ( charSet )? ( WS )* ( imports ( WS )* )* ( namespace )* bodylist EOF )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:299:9: ( charSet )? ( WS )* ( imports ( WS )* )* ( namespace )* bodylist EOF
            {
            dbg.location(299,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:299:9: ( charSet )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:299:9: charSet
                    {
                    dbg.location(299,9);
                    pushFollow(FOLLOW_charSet_in_styleSheet119);
                    charSet();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(1);}

            dbg.location(300,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:300:6: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:300:6: WS
            	    {
            	    dbg.location(300,6);
            	    match(input,WS,FOLLOW_WS_in_styleSheet127); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);
            } finally {dbg.exitSubRule(2);}

            dbg.location(301,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:301:9: ( imports ( WS )* )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:301:10: imports ( WS )*
            	    {
            	    dbg.location(301,10);
            	    pushFollow(FOLLOW_imports_in_styleSheet139);
            	    imports();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(301,18);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:301:18: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:301:18: WS
            	    	    {
            	    	    dbg.location(301,18);
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

            dbg.location(302,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:302:9: ( namespace )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:302:9: namespace
            	    {
            	    dbg.location(302,9);
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

            dbg.location(303,9);
            pushFollow(FOLLOW_bodylist_in_styleSheet167);
            bodylist();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(304,6);
            match(input,EOF,FOLLOW_EOF_in_styleSheet174); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(305, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:307:1: namespace : NAMESPACE_SYM ( WS )* ( namespace_prefix ( WS )* )? ( resourceIdentifier ) ( WS )* ';' ( WS )* ;
    public final void namespace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(307, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:3: ( NAMESPACE_SYM ( WS )* ( namespace_prefix ( WS )* )? ( resourceIdentifier ) ( WS )* ';' ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:5: NAMESPACE_SYM ( WS )* ( namespace_prefix ( WS )* )? ( resourceIdentifier ) ( WS )* ';' ( WS )*
            {
            dbg.location(308,5);
            match(input,NAMESPACE_SYM,FOLLOW_NAMESPACE_SYM_in_namespace189); if (state.failed) return ;
            dbg.location(308,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:19: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:19: WS
            	    {
            	    dbg.location(308,19);
            	    match(input,WS,FOLLOW_WS_in_namespace191); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);
            } finally {dbg.exitSubRule(6);}

            dbg.location(308,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:23: ( namespace_prefix ( WS )* )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:24: namespace_prefix ( WS )*
                    {
                    dbg.location(308,24);
                    pushFollow(FOLLOW_namespace_prefix_in_namespace195);
                    namespace_prefix();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(308,41);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:41: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:41: WS
                    	    {
                    	    dbg.location(308,41);
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

            dbg.location(308,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:47: ( resourceIdentifier )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:48: resourceIdentifier
            {
            dbg.location(308,48);
            pushFollow(FOLLOW_resourceIdentifier_in_namespace203);
            resourceIdentifier();

            state._fsp--;
            if (state.failed) return ;

            }

            dbg.location(308,68);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:68: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:68: WS
            	    {
            	    dbg.location(308,68);
            	    match(input,WS,FOLLOW_WS_in_namespace206); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);
            } finally {dbg.exitSubRule(9);}

            dbg.location(308,72);
            match(input,SEMI,FOLLOW_SEMI_in_namespace209); if (state.failed) return ;
            dbg.location(308,76);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:76: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:76: WS
            	    {
            	    dbg.location(308,76);
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
        dbg.location(309, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:311:1: namespace_prefix : IDENT ;
    public final void namespace_prefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace_prefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(311, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:312:3: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:312:5: IDENT
            {
            dbg.location(312,5);
            match(input,IDENT,FOLLOW_IDENT_in_namespace_prefix225); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(313, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:315:1: resourceIdentifier : ( STRING | URI );
    public final void resourceIdentifier() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "resourceIdentifier");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(315, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:316:3: ( STRING | URI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(316,3);
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
        dbg.location(317, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:322:1: charSet : CHARSET_SYM ( WS )* STRING ( WS )* SEMI ;
    public final void charSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "charSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(322, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:5: ( CHARSET_SYM ( WS )* STRING ( WS )* SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:9: CHARSET_SYM ( WS )* STRING ( WS )* SEMI
            {
            dbg.location(323,9);
            match(input,CHARSET_SYM,FOLLOW_CHARSET_SYM_in_charSet264); if (state.failed) return ;
            dbg.location(323,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:21: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:21: WS
            	    {
            	    dbg.location(323,21);
            	    match(input,WS,FOLLOW_WS_in_charSet266); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);
            } finally {dbg.exitSubRule(11);}

            dbg.location(323,25);
            match(input,STRING,FOLLOW_STRING_in_charSet269); if (state.failed) return ;
            dbg.location(323,32);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:32: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:32: WS
            	    {
            	    dbg.location(323,32);
            	    match(input,WS,FOLLOW_WS_in_charSet271); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop12;
                }
            } while (true);
            } finally {dbg.exitSubRule(12);}

            dbg.location(323,36);
            match(input,SEMI,FOLLOW_SEMI_in_charSet274); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "charSet");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "charSet"


    // $ANTLR start "imports"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:329:1: imports : IMPORT_SYM ( WS )* ( resourceIdentifier ) ( WS )* media_query_list SEMI ;
    public final void imports() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "imports");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(329, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:330:5: ( IMPORT_SYM ( WS )* ( resourceIdentifier ) ( WS )* media_query_list SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:330:9: IMPORT_SYM ( WS )* ( resourceIdentifier ) ( WS )* media_query_list SEMI
            {
            dbg.location(330,9);
            match(input,IMPORT_SYM,FOLLOW_IMPORT_SYM_in_imports296); if (state.failed) return ;
            dbg.location(330,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:330:20: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:330:20: WS
            	    {
            	    dbg.location(330,20);
            	    match(input,WS,FOLLOW_WS_in_imports298); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);
            } finally {dbg.exitSubRule(13);}

            dbg.location(330,24);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:330:24: ( resourceIdentifier )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:330:25: resourceIdentifier
            {
            dbg.location(330,25);
            pushFollow(FOLLOW_resourceIdentifier_in_imports302);
            resourceIdentifier();

            state._fsp--;
            if (state.failed) return ;

            }

            dbg.location(330,45);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:330:45: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:330:45: WS
            	    {
            	    dbg.location(330,45);
            	    match(input,WS,FOLLOW_WS_in_imports305); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);
            } finally {dbg.exitSubRule(14);}

            dbg.location(330,49);
            pushFollow(FOLLOW_media_query_list_in_imports308);
            media_query_list();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(330,66);
            match(input,SEMI,FOLLOW_SEMI_in_imports310); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "imports");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "imports"


    // $ANTLR start "media"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:337:1: media : MEDIA_SYM ( WS )* media_query_list LBRACE ( WS )* ( ( ruleSet | page ) ( WS )* )* RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(337, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:5: ( MEDIA_SYM ( WS )* media_query_list LBRACE ( WS )* ( ( ruleSet | page ) ( WS )* )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:7: MEDIA_SYM ( WS )* media_query_list LBRACE ( WS )* ( ( ruleSet | page ) ( WS )* )* RBRACE
            {
            dbg.location(338,7);
            match(input,MEDIA_SYM,FOLLOW_MEDIA_SYM_in_media331); if (state.failed) return ;
            dbg.location(338,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:17: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:338:17: WS
            	    {
            	    dbg.location(338,17);
            	    match(input,WS,FOLLOW_WS_in_media333); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);
            } finally {dbg.exitSubRule(15);}

            dbg.location(338,21);
            pushFollow(FOLLOW_media_query_list_in_media336);
            media_query_list();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(339,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_media346); if (state.failed) return ;
            dbg.location(339,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:339:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:339:16: WS
            	    {
            	    dbg.location(339,16);
            	    match(input,WS,FOLLOW_WS_in_media348); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);
            } finally {dbg.exitSubRule(16);}

            dbg.location(340,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:340:13: ( ( ruleSet | page ) ( WS )* )*
            try { dbg.enterSubRule(19);

            loop19:
            do {
                int alt19=2;
                try { dbg.enterDecision(19, decisionCanBacktrack[19]);

                int LA19_0 = input.LA(1);

                if ( (LA19_0==IDENT||LA19_0==GEN||LA19_0==PAGE_SYM||LA19_0==COLON||(LA19_0>=STAR && LA19_0<=DCOLON)||LA19_0==115) ) {
                    alt19=1;
                }


                } finally {dbg.exitDecision(19);}

                switch (alt19) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:340:15: ( ruleSet | page ) ( WS )*
            	    {
            	    dbg.location(340,15);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:340:15: ( ruleSet | page )
            	    int alt17=2;
            	    try { dbg.enterSubRule(17);
            	    try { dbg.enterDecision(17, decisionCanBacktrack[17]);

            	    int LA17_0 = input.LA(1);

            	    if ( (LA17_0==IDENT||LA17_0==GEN||LA17_0==COLON||(LA17_0>=STAR && LA17_0<=DCOLON)||LA17_0==115) ) {
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:340:17: ruleSet
            	            {
            	            dbg.location(340,17);
            	            pushFollow(FOLLOW_ruleSet_in_media367);
            	            ruleSet();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:340:27: page
            	            {
            	            dbg.location(340,27);
            	            pushFollow(FOLLOW_page_in_media371);
            	            page();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(17);}

            	    dbg.location(340,34);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:340:34: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:340:34: WS
            	    	    {
            	    	    dbg.location(340,34);
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

            dbg.location(341,10);
            match(input,RBRACE,FOLLOW_RBRACE_in_media389); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(342, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:1: media_query_list : ( media_query ( COMMA ( WS )* media_query )* )? ;
    public final void media_query_list() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_query_list");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(348, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:2: ( ( media_query ( COMMA ( WS )* media_query )* )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:4: ( media_query ( COMMA ( WS )* media_query )* )?
            {
            dbg.location(349,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:4: ( media_query ( COMMA ( WS )* media_query )* )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:6: media_query ( COMMA ( WS )* media_query )*
                    {
                    dbg.location(349,6);
                    pushFollow(FOLLOW_media_query_in_media_query_list409);
                    media_query();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(349,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:18: ( COMMA ( WS )* media_query )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:20: COMMA ( WS )* media_query
                    	    {
                    	    dbg.location(349,20);
                    	    match(input,COMMA,FOLLOW_COMMA_in_media_query_list413); if (state.failed) return ;
                    	    dbg.location(349,26);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:26: ( WS )*
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

                    	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:26: WS
                    	    	    {
                    	    	    dbg.location(349,26);
                    	    	    match(input,WS,FOLLOW_WS_in_media_query_list415); if (state.failed) return ;

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop20;
                    	        }
                    	    } while (true);
                    	    } finally {dbg.exitSubRule(20);}

                    	    dbg.location(349,30);
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
        dbg.location(350, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:352:1: media_query : ( ( ( ONLY | NOT ) ( WS )* )? media_type ( WS )* ( AND ( WS )* media_expression )* | media_expression ( AND ( WS )* media_expression )* );
    public final void media_query() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_query");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(352, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:2: ( ( ( ONLY | NOT ) ( WS )* )? media_type ( WS )* ( AND ( WS )* media_expression )* | media_expression ( AND ( WS )* media_expression )* )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:4: ( ( ONLY | NOT ) ( WS )* )? media_type ( WS )* ( AND ( WS )* media_expression )*
                    {
                    dbg.location(353,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:4: ( ( ONLY | NOT ) ( WS )* )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:5: ( ONLY | NOT ) ( WS )*
                            {
                            dbg.location(353,5);
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

                            dbg.location(353,18);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:18: ( WS )*
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

                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:18: WS
                            	    {
                            	    dbg.location(353,18);
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

                    dbg.location(353,26);
                    pushFollow(FOLLOW_media_type_in_media_query452);
                    media_type();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(353,37);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:37: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:37: WS
                    	    {
                    	    dbg.location(353,37);
                    	    match(input,WS,FOLLOW_WS_in_media_query454); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop25;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(25);}

                    dbg.location(353,41);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:41: ( AND ( WS )* media_expression )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:43: AND ( WS )* media_expression
                    	    {
                    	    dbg.location(353,43);
                    	    match(input,AND,FOLLOW_AND_in_media_query459); if (state.failed) return ;
                    	    dbg.location(353,47);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:47: ( WS )*
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

                    	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:353:47: WS
                    	    	    {
                    	    	    dbg.location(353,47);
                    	    	    match(input,WS,FOLLOW_WS_in_media_query461); if (state.failed) return ;

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop26;
                    	        }
                    	    } while (true);
                    	    } finally {dbg.exitSubRule(26);}

                    	    dbg.location(353,51);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:354:4: media_expression ( AND ( WS )* media_expression )*
                    {
                    dbg.location(354,4);
                    pushFollow(FOLLOW_media_expression_in_media_query472);
                    media_expression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(354,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:354:21: ( AND ( WS )* media_expression )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:354:23: AND ( WS )* media_expression
                    	    {
                    	    dbg.location(354,23);
                    	    match(input,AND,FOLLOW_AND_in_media_query476); if (state.failed) return ;
                    	    dbg.location(354,27);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:354:27: ( WS )*
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

                    	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:354:27: WS
                    	    	    {
                    	    	    dbg.location(354,27);
                    	    	    match(input,WS,FOLLOW_WS_in_media_query478); if (state.failed) return ;

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop28;
                    	        }
                    	    } while (true);
                    	    } finally {dbg.exitSubRule(28);}

                    	    dbg.location(354,31);
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
        dbg.location(355, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:357:1: media_type : ( IDENT | GEN );
    public final void media_type() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_type");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(357, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:358:2: ( IDENT | GEN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(358,2);
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
        dbg.location(359, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:361:1: media_expression : '(' ( WS )* media_feature ( WS )* ( ':' ( WS )* expr )? ')' ( WS )* ;
    public final void media_expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(361, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:2: ( '(' ( WS )* media_feature ( WS )* ( ':' ( WS )* expr )? ')' ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:4: '(' ( WS )* media_feature ( WS )* ( ':' ( WS )* expr )? ')' ( WS )*
            {
            dbg.location(362,4);
            match(input,LPAREN,FOLLOW_LPAREN_in_media_expression512); if (state.failed) return ;
            dbg.location(362,8);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:8: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:8: WS
            	    {
            	    dbg.location(362,8);
            	    match(input,WS,FOLLOW_WS_in_media_expression514); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop31;
                }
            } while (true);
            } finally {dbg.exitSubRule(31);}

            dbg.location(362,12);
            pushFollow(FOLLOW_media_feature_in_media_expression517);
            media_feature();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(362,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:26: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:26: WS
            	    {
            	    dbg.location(362,26);
            	    match(input,WS,FOLLOW_WS_in_media_expression519); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop32;
                }
            } while (true);
            } finally {dbg.exitSubRule(32);}

            dbg.location(362,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:30: ( ':' ( WS )* expr )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:32: ':' ( WS )* expr
                    {
                    dbg.location(362,32);
                    match(input,COLON,FOLLOW_COLON_in_media_expression524); if (state.failed) return ;
                    dbg.location(362,36);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:36: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:36: WS
                    	    {
                    	    dbg.location(362,36);
                    	    match(input,WS,FOLLOW_WS_in_media_expression526); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop33;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(33);}

                    dbg.location(362,40);
                    pushFollow(FOLLOW_expr_in_media_expression529);
                    expr();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(34);}

            dbg.location(362,48);
            match(input,RPAREN,FOLLOW_RPAREN_in_media_expression534); if (state.failed) return ;
            dbg.location(362,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:52: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:362:52: WS
            	    {
            	    dbg.location(362,52);
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
        dbg.location(363, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:1: media_feature : IDENT ;
    public final void media_feature() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media_feature");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(364, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:365:4: IDENT
            {
            dbg.location(365,4);
            match(input,IDENT,FOLLOW_IDENT_in_media_feature547); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(366, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:371:1: medium : ( IDENT | GEN ) ( WS )* ;
    public final void medium() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "medium");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(371, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:5: ( ( IDENT | GEN ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:7: ( IDENT | GEN ) ( WS )*
            {
            dbg.location(372,7);
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

            dbg.location(372,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:23: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:23: WS
            	    {
            	    dbg.location(372,23);
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
        dbg.location(373, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:1: bodylist : ( bodyset )* ;
    public final void bodylist() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodylist");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(376, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:5: ( ( bodyset )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:7: ( bodyset )*
            {
            dbg.location(377,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:7: ( bodyset )*
            try { dbg.enterSubRule(37);

            loop37:
            do {
                int alt37=2;
                try { dbg.enterDecision(37, decisionCanBacktrack[37]);

                int LA37_0 = input.LA(1);

                if ( (LA37_0==IDENT||LA37_0==MEDIA_SYM||(LA37_0>=GEN && LA37_0<=MOZ_DOCUMENT_SYM)||(LA37_0>=PAGE_SYM && LA37_0<=FONT_FACE_SYM)||LA37_0==COLON||(LA37_0>=STAR && LA37_0<=DCOLON)||LA37_0==115) ) {
                    alt37=1;
                }


                } finally {dbg.exitDecision(37);}

                switch (alt37) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:377:7: bodyset
            	    {
            	    dbg.location(377,7);
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
        dbg.location(378, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:1: bodyset : ( ruleSet | media | page | counterStyle | fontFace | moz_document | generic_at_rule ) ( WS )* ;
    public final void bodyset() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyset");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(380, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:5: ( ( ruleSet | media | page | counterStyle | fontFace | moz_document | generic_at_rule ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:7: ( ruleSet | media | page | counterStyle | fontFace | moz_document | generic_at_rule ) ( WS )*
            {
            dbg.location(381,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:381:7: ( ruleSet | media | page | counterStyle | fontFace | moz_document | generic_at_rule )
            int alt38=7;
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
            case 115:
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
            case GENERIC_AT_RULE:
                {
                alt38=7;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:382:6: ruleSet
                    {
                    dbg.location(382,6);
                    pushFollow(FOLLOW_ruleSet_in_bodyset626);
                    ruleSet();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:11: media
                    {
                    dbg.location(383,11);
                    pushFollow(FOLLOW_media_in_bodyset638);
                    media();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:384:11: page
                    {
                    dbg.location(384,11);
                    pushFollow(FOLLOW_page_in_bodyset650);
                    page();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:11: counterStyle
                    {
                    dbg.location(385,11);
                    pushFollow(FOLLOW_counterStyle_in_bodyset662);
                    counterStyle();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:386:11: fontFace
                    {
                    dbg.location(386,11);
                    pushFollow(FOLLOW_fontFace_in_bodyset674);
                    fontFace();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:387:11: moz_document
                    {
                    dbg.location(387,11);
                    pushFollow(FOLLOW_moz_document_in_bodyset686);
                    moz_document();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:388:11: generic_at_rule
                    {
                    dbg.location(388,11);
                    pushFollow(FOLLOW_generic_at_rule_in_bodyset698);
                    generic_at_rule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(38);}

            dbg.location(390,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:7: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:7: WS
            	    {
            	    dbg.location(390,7);
            	    match(input,WS,FOLLOW_WS_in_bodyset714); if (state.failed) return ;

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
        dbg.location(391, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "bodyset");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "bodyset"


    // $ANTLR start "generic_at_rule"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:400:1: generic_at_rule : GENERIC_AT_RULE ( WS )* ( ( IDENT | STRING ) ( WS )* )? LBRACE syncTo_RBRACE RBRACE ;
    public final void generic_at_rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "generic_at_rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(400, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:5: ( GENERIC_AT_RULE ( WS )* ( ( IDENT | STRING ) ( WS )* )? LBRACE syncTo_RBRACE RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:7: GENERIC_AT_RULE ( WS )* ( ( IDENT | STRING ) ( WS )* )? LBRACE syncTo_RBRACE RBRACE
            {
            dbg.location(401,7);
            match(input,GENERIC_AT_RULE,FOLLOW_GENERIC_AT_RULE_in_generic_at_rule747); if (state.failed) return ;
            dbg.location(401,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:23: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:23: WS
            	    {
            	    dbg.location(401,23);
            	    match(input,WS,FOLLOW_WS_in_generic_at_rule749); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop40;
                }
            } while (true);
            } finally {dbg.exitSubRule(40);}

            dbg.location(401,27);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:27: ( ( IDENT | STRING ) ( WS )* )?
            int alt42=2;
            try { dbg.enterSubRule(42);
            try { dbg.enterDecision(42, decisionCanBacktrack[42]);

            int LA42_0 = input.LA(1);

            if ( ((LA42_0>=IDENT && LA42_0<=STRING)) ) {
                alt42=1;
            }
            } finally {dbg.exitDecision(42);}

            switch (alt42) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:29: ( IDENT | STRING ) ( WS )*
                    {
                    dbg.location(401,29);
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

                    dbg.location(401,48);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:48: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:48: WS
                    	    {
                    	    dbg.location(401,48);
                    	    match(input,WS,FOLLOW_WS_in_generic_at_rule764); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop41;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(41);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(42);}

            dbg.location(402,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_generic_at_rule779); if (state.failed) return ;
            dbg.location(403,10);
            pushFollow(FOLLOW_syncTo_RBRACE_in_generic_at_rule791);
            syncTo_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(404,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_generic_at_rule801); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(405, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "generic_at_rule");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "generic_at_rule"


    // $ANTLR start "moz_document"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:406:1: moz_document : MOZ_DOCUMENT_SYM ( WS )* ( moz_document_function ( WS )* ) ( COMMA ( WS )* moz_document_function ( WS )* )* LBRACE ( WS )* ( ( ruleSet | page ) ( WS )* )* RBRACE ;
    public final void moz_document() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(406, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:2: ( MOZ_DOCUMENT_SYM ( WS )* ( moz_document_function ( WS )* ) ( COMMA ( WS )* moz_document_function ( WS )* )* LBRACE ( WS )* ( ( ruleSet | page ) ( WS )* )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:2: MOZ_DOCUMENT_SYM ( WS )* ( moz_document_function ( WS )* ) ( COMMA ( WS )* moz_document_function ( WS )* )* LBRACE ( WS )* ( ( ruleSet | page ) ( WS )* )* RBRACE
            {
            dbg.location(408,2);
            match(input,MOZ_DOCUMENT_SYM,FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document817); if (state.failed) return ;
            dbg.location(408,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:19: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:19: WS
            	    {
            	    dbg.location(408,19);
            	    match(input,WS,FOLLOW_WS_in_moz_document819); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop43;
                }
            } while (true);
            } finally {dbg.exitSubRule(43);}

            dbg.location(408,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:23: ( moz_document_function ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:25: moz_document_function ( WS )*
            {
            dbg.location(408,25);
            pushFollow(FOLLOW_moz_document_function_in_moz_document824);
            moz_document_function();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(408,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:47: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:47: WS
            	    {
            	    dbg.location(408,47);
            	    match(input,WS,FOLLOW_WS_in_moz_document826); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop44;
                }
            } while (true);
            } finally {dbg.exitSubRule(44);}


            }

            dbg.location(408,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:52: ( COMMA ( WS )* moz_document_function ( WS )* )*
            try { dbg.enterSubRule(47);

            loop47:
            do {
                int alt47=2;
                try { dbg.enterDecision(47, decisionCanBacktrack[47]);

                int LA47_0 = input.LA(1);

                if ( (LA47_0==COMMA) ) {
                    alt47=1;
                }


                } finally {dbg.exitDecision(47);}

                switch (alt47) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:54: COMMA ( WS )* moz_document_function ( WS )*
            	    {
            	    dbg.location(408,54);
            	    match(input,COMMA,FOLLOW_COMMA_in_moz_document832); if (state.failed) return ;
            	    dbg.location(408,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:60: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:60: WS
            	    	    {
            	    	    dbg.location(408,60);
            	    	    match(input,WS,FOLLOW_WS_in_moz_document834); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop45;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(45);}

            	    dbg.location(408,64);
            	    pushFollow(FOLLOW_moz_document_function_in_moz_document837);
            	    moz_document_function();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(408,86);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:86: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:86: WS
            	    	    {
            	    	    dbg.location(408,86);
            	    	    match(input,WS,FOLLOW_WS_in_moz_document839); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop46;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(46);}


            	    }
            	    break;

            	default :
            	    break loop47;
                }
            } while (true);
            } finally {dbg.exitSubRule(47);}

            dbg.location(409,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_moz_document846); if (state.failed) return ;
            dbg.location(409,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:9: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:9: WS
            	    {
            	    dbg.location(409,9);
            	    match(input,WS,FOLLOW_WS_in_moz_document848); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop48;
                }
            } while (true);
            } finally {dbg.exitSubRule(48);}

            dbg.location(410,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:3: ( ( ruleSet | page ) ( WS )* )*
            try { dbg.enterSubRule(51);

            loop51:
            do {
                int alt51=2;
                try { dbg.enterDecision(51, decisionCanBacktrack[51]);

                int LA51_0 = input.LA(1);

                if ( (LA51_0==IDENT||LA51_0==GEN||LA51_0==PAGE_SYM||LA51_0==COLON||(LA51_0>=STAR && LA51_0<=DCOLON)||LA51_0==115) ) {
                    alt51=1;
                }


                } finally {dbg.exitDecision(51);}

                switch (alt51) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:5: ( ruleSet | page ) ( WS )*
            	    {
            	    dbg.location(410,5);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:5: ( ruleSet | page )
            	    int alt49=2;
            	    try { dbg.enterSubRule(49);
            	    try { dbg.enterDecision(49, decisionCanBacktrack[49]);

            	    int LA49_0 = input.LA(1);

            	    if ( (LA49_0==IDENT||LA49_0==GEN||LA49_0==COLON||(LA49_0>=STAR && LA49_0<=DCOLON)||LA49_0==115) ) {
            	        alt49=1;
            	    }
            	    else if ( (LA49_0==PAGE_SYM) ) {
            	        alt49=2;
            	    }
            	    else {
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:7: ruleSet
            	            {
            	            dbg.location(410,7);
            	            pushFollow(FOLLOW_ruleSet_in_moz_document857);
            	            ruleSet();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:17: page
            	            {
            	            dbg.location(410,17);
            	            pushFollow(FOLLOW_page_in_moz_document861);
            	            page();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(49);}

            	    dbg.location(410,24);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:24: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:24: WS
            	    	    {
            	    	    dbg.location(410,24);
            	    	    match(input,WS,FOLLOW_WS_in_moz_document865); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop50;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(50);}


            	    }
            	    break;

            	default :
            	    break loop51;
                }
            } while (true);
            } finally {dbg.exitSubRule(51);}

            dbg.location(411,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_moz_document871); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(412, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:1: moz_document_function : ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP );
    public final void moz_document_function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document_function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(414, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:2: ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(415,2);
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
        dbg.location(417, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:419:1: page : PAGE_SYM ( WS )* ( IDENT ( WS )* )? ( pseudoPage ( WS )* )? LBRACE ( WS )* ( declaration | margin ( WS )* )? ( SEMI ( WS )* ( declaration | margin ( WS )* )? )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(419, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:5: ( PAGE_SYM ( WS )* ( IDENT ( WS )* )? ( pseudoPage ( WS )* )? LBRACE ( WS )* ( declaration | margin ( WS )* )? ( SEMI ( WS )* ( declaration | margin ( WS )* )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:7: PAGE_SYM ( WS )* ( IDENT ( WS )* )? ( pseudoPage ( WS )* )? LBRACE ( WS )* ( declaration | margin ( WS )* )? ( SEMI ( WS )* ( declaration | margin ( WS )* )? )* RBRACE
            {
            dbg.location(420,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page913); if (state.failed) return ;
            dbg.location(420,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:16: WS
            	    {
            	    dbg.location(420,16);
            	    match(input,WS,FOLLOW_WS_in_page915); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop52;
                }
            } while (true);
            } finally {dbg.exitSubRule(52);}

            dbg.location(420,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:20: ( IDENT ( WS )* )?
            int alt54=2;
            try { dbg.enterSubRule(54);
            try { dbg.enterDecision(54, decisionCanBacktrack[54]);

            int LA54_0 = input.LA(1);

            if ( (LA54_0==IDENT) ) {
                alt54=1;
            }
            } finally {dbg.exitDecision(54);}

            switch (alt54) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:22: IDENT ( WS )*
                    {
                    dbg.location(420,22);
                    match(input,IDENT,FOLLOW_IDENT_in_page920); if (state.failed) return ;
                    dbg.location(420,28);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:28: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:28: WS
                    	    {
                    	    dbg.location(420,28);
                    	    match(input,WS,FOLLOW_WS_in_page922); if (state.failed) return ;

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

            dbg.location(420,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:35: ( pseudoPage ( WS )* )?
            int alt56=2;
            try { dbg.enterSubRule(56);
            try { dbg.enterDecision(56, decisionCanBacktrack[56]);

            int LA56_0 = input.LA(1);

            if ( (LA56_0==COLON) ) {
                alt56=1;
            }
            } finally {dbg.exitDecision(56);}

            switch (alt56) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:36: pseudoPage ( WS )*
                    {
                    dbg.location(420,36);
                    pushFollow(FOLLOW_pseudoPage_in_page929);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(420,47);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:47: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:47: WS
                    	    {
                    	    dbg.location(420,47);
                    	    match(input,WS,FOLLOW_WS_in_page931); if (state.failed) return ;

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

            dbg.location(421,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page944); if (state.failed) return ;
            dbg.location(421,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:16: WS
            	    {
            	    dbg.location(421,16);
            	    match(input,WS,FOLLOW_WS_in_page946); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop57;
                }
            } while (true);
            } finally {dbg.exitSubRule(57);}

            dbg.location(426,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:13: ( declaration | margin ( WS )* )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:14: declaration
                    {
                    dbg.location(426,14);
                    pushFollow(FOLLOW_declaration_in_page1014);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:26: margin ( WS )*
                    {
                    dbg.location(426,26);
                    pushFollow(FOLLOW_margin_in_page1016);
                    margin();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(426,33);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:33: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:33: WS
                    	    {
                    	    dbg.location(426,33);
                    	    match(input,WS,FOLLOW_WS_in_page1018); if (state.failed) return ;

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

            dbg.location(426,39);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:39: ( SEMI ( WS )* ( declaration | margin ( WS )* )? )*
            try { dbg.enterSubRule(63);

            loop63:
            do {
                int alt63=2;
                try { dbg.enterDecision(63, decisionCanBacktrack[63]);

                int LA63_0 = input.LA(1);

                if ( (LA63_0==SEMI) ) {
                    alt63=1;
                }


                } finally {dbg.exitDecision(63);}

                switch (alt63) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:40: SEMI ( WS )* ( declaration | margin ( WS )* )?
            	    {
            	    dbg.location(426,40);
            	    match(input,SEMI,FOLLOW_SEMI_in_page1024); if (state.failed) return ;
            	    dbg.location(426,45);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:45: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:45: WS
            	    	    {
            	    	    dbg.location(426,45);
            	    	    match(input,WS,FOLLOW_WS_in_page1026); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop60;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(60);}

            	    dbg.location(426,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:49: ( declaration | margin ( WS )* )?
            	    int alt62=3;
            	    try { dbg.enterSubRule(62);
            	    try { dbg.enterDecision(62, decisionCanBacktrack[62]);

            	    int LA62_0 = input.LA(1);

            	    if ( (LA62_0==IDENT||LA62_0==GEN) ) {
            	        alt62=1;
            	    }
            	    else if ( ((LA62_0>=TOPLEFTCORNER_SYM && LA62_0<=RIGHTBOTTOM_SYM)) ) {
            	        alt62=2;
            	    }
            	    } finally {dbg.exitDecision(62);}

            	    switch (alt62) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:50: declaration
            	            {
            	            dbg.location(426,50);
            	            pushFollow(FOLLOW_declaration_in_page1030);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:62: margin ( WS )*
            	            {
            	            dbg.location(426,62);
            	            pushFollow(FOLLOW_margin_in_page1032);
            	            margin();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(426,69);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:69: ( WS )*
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

            	            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:69: WS
            	            	    {
            	            	    dbg.location(426,69);
            	            	    match(input,WS,FOLLOW_WS_in_page1034); if (state.failed) return ;

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


            	    }
            	    break;

            	default :
            	    break loop63;
                }
            } while (true);
            } finally {dbg.exitSubRule(63);}

            dbg.location(427,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page1049); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "page");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "page"


    // $ANTLR start "counterStyle"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:1: counterStyle : COUNTER_STYLE_SYM ( WS )* IDENT ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void counterStyle() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "counterStyle");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(430, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:5: ( COUNTER_STYLE_SYM ( WS )* IDENT ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:7: COUNTER_STYLE_SYM ( WS )* IDENT ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(431,7);
            match(input,COUNTER_STYLE_SYM,FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1070); if (state.failed) return ;
            dbg.location(431,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:25: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:25: WS
            	    {
            	    dbg.location(431,25);
            	    match(input,WS,FOLLOW_WS_in_counterStyle1072); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop64;
                }
            } while (true);
            } finally {dbg.exitSubRule(64);}

            dbg.location(431,29);
            match(input,IDENT,FOLLOW_IDENT_in_counterStyle1075); if (state.failed) return ;
            dbg.location(431,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:35: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:35: WS
            	    {
            	    dbg.location(431,35);
            	    match(input,WS,FOLLOW_WS_in_counterStyle1077); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop65;
                }
            } while (true);
            } finally {dbg.exitSubRule(65);}

            dbg.location(432,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_counterStyle1088); if (state.failed) return ;
            dbg.location(432,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:16: WS
            	    {
            	    dbg.location(432,16);
            	    match(input,WS,FOLLOW_WS_in_counterStyle1090); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop66;
                }
            } while (true);
            } finally {dbg.exitSubRule(66);}

            dbg.location(432,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_counterStyle1093);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(433,3);
            pushFollow(FOLLOW_declarations_in_counterStyle1097);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(434,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_counterStyle1107); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(435, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:437:1: fontFace : FONT_FACE_SYM ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void fontFace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fontFace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(437, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:5: ( FONT_FACE_SYM ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:7: FONT_FACE_SYM ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(438,7);
            match(input,FONT_FACE_SYM,FOLLOW_FONT_FACE_SYM_in_fontFace1128); if (state.failed) return ;
            dbg.location(438,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:21: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:21: WS
            	    {
            	    dbg.location(438,21);
            	    match(input,WS,FOLLOW_WS_in_fontFace1130); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop67;
                }
            } while (true);
            } finally {dbg.exitSubRule(67);}

            dbg.location(439,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_fontFace1141); if (state.failed) return ;
            dbg.location(439,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:16: WS
            	    {
            	    dbg.location(439,16);
            	    match(input,WS,FOLLOW_WS_in_fontFace1143); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop68;
                }
            } while (true);
            } finally {dbg.exitSubRule(68);}

            dbg.location(439,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_fontFace1146);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(440,3);
            pushFollow(FOLLOW_declarations_in_fontFace1150);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(441,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_fontFace1160); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "fontFace");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "fontFace"


    // $ANTLR start "margin"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:1: margin : margin_sym ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void margin() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(445, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:2: ( margin_sym ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:4: margin_sym ( WS )* LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(446,4);
            pushFollow(FOLLOW_margin_sym_in_margin1180);
            margin_sym();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(446,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:15: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:15: WS
            	    {
            	    dbg.location(446,15);
            	    match(input,WS,FOLLOW_WS_in_margin1182); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop69;
                }
            } while (true);
            } finally {dbg.exitSubRule(69);}

            dbg.location(446,19);
            match(input,LBRACE,FOLLOW_LBRACE_in_margin1185); if (state.failed) return ;
            dbg.location(446,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:26: ( WS )*
            try { dbg.enterSubRule(70);

            loop70:
            do {
                int alt70=2;
                try { dbg.enterDecision(70, decisionCanBacktrack[70]);

                int LA70_0 = input.LA(1);

                if ( (LA70_0==WS) ) {
                    alt70=1;
                }


                } finally {dbg.exitDecision(70);}

                switch (alt70) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:26: WS
            	    {
            	    dbg.location(446,26);
            	    match(input,WS,FOLLOW_WS_in_margin1187); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop70;
                }
            } while (true);
            } finally {dbg.exitSubRule(70);}

            dbg.location(446,30);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_margin1190);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(446,50);
            pushFollow(FOLLOW_declarations_in_margin1192);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(446,63);
            match(input,RBRACE,FOLLOW_RBRACE_in_margin1194); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(447, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:449:1: margin_sym : ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM );
    public final void margin_sym() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin_sym");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(449, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:2: ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(450,2);
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
        dbg.location(467, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(469, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:470:7: COLON IDENT
            {
            dbg.location(470,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage1423); if (state.failed) return ;
            dbg.location(470,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage1425); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(471, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:1: operator : ( SOLIDUS ( WS )* | COMMA ( WS )* | );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(473, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:5: ( SOLIDUS ( WS )* | COMMA ( WS )* | )
            int alt73=3;
            try { dbg.enterDecision(73, decisionCanBacktrack[73]);

            switch ( input.LA(1) ) {
            case SOLIDUS:
                {
                alt73=1;
                }
                break;
            case COMMA:
                {
                alt73=2;
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
            case DIMENSION:
                {
                alt73=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 73, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(73);}

            switch (alt73) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:7: SOLIDUS ( WS )*
                    {
                    dbg.location(474,7);
                    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_operator1446); if (state.failed) return ;
                    dbg.location(474,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:15: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:15: WS
                    	    {
                    	    dbg.location(474,15);
                    	    match(input,WS,FOLLOW_WS_in_operator1448); if (state.failed) return ;

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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:7: COMMA ( WS )*
                    {
                    dbg.location(475,7);
                    match(input,COMMA,FOLLOW_COMMA_in_operator1457); if (state.failed) return ;
                    dbg.location(475,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:13: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:475:13: WS
                    	    {
                    	    dbg.location(475,13);
                    	    match(input,WS,FOLLOW_WS_in_operator1459); if (state.failed) return ;

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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:477:5: 
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
        dbg.location(477, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:1: combinator : ( PLUS ( WS )* | GREATER ( WS )* | TILDE ( WS )* | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(479, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:5: ( PLUS ( WS )* | GREATER ( WS )* | TILDE ( WS )* | )
            int alt77=4;
            try { dbg.enterDecision(77, decisionCanBacktrack[77]);

            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt77=1;
                }
                break;
            case GREATER:
                {
                alt77=2;
                }
                break;
            case TILDE:
                {
                alt77=3;
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
            case 115:
                {
                alt77=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 77, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(77);}

            switch (alt77) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:7: PLUS ( WS )*
                    {
                    dbg.location(480,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator1487); if (state.failed) return ;
                    dbg.location(480,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:12: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:12: WS
                    	    {
                    	    dbg.location(480,12);
                    	    match(input,WS,FOLLOW_WS_in_combinator1489); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop74;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(74);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:7: GREATER ( WS )*
                    {
                    dbg.location(481,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator1498); if (state.failed) return ;
                    dbg.location(481,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:15: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:15: WS
                    	    {
                    	    dbg.location(481,15);
                    	    match(input,WS,FOLLOW_WS_in_combinator1500); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop75;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(75);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:7: TILDE ( WS )*
                    {
                    dbg.location(482,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator1509); if (state.failed) return ;
                    dbg.location(482,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:13: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:482:13: WS
                    	    {
                    	    dbg.location(482,13);
                    	    match(input,WS,FOLLOW_WS_in_combinator1511); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop76;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(76);}


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:484:5: 
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
        dbg.location(484, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(486, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:487:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(487,5);
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
        dbg.location(489, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:1: property : ( IDENT | GEN ) ( WS )* ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(491, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:5: ( ( IDENT | GEN ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:7: ( IDENT | GEN ) ( WS )*
            {
            dbg.location(492,7);
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

            dbg.location(492,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:21: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:492:21: WS
            	    {
            	    dbg.location(492,21);
            	    match(input,WS,FOLLOW_WS_in_property1579); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop78;
                }
            } while (true);
            } finally {dbg.exitSubRule(78);}


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
            dbg.exitRule(getGrammarFileName(), "property");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "property"


    // $ANTLR start "ruleSet"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:495:1: ruleSet : selectorsGroup LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void ruleSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ruleSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(495, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:496:5: ( selectorsGroup LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:496:9: selectorsGroup LBRACE ( WS )* syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(496,9);
            pushFollow(FOLLOW_selectorsGroup_in_ruleSet1604);
            selectorsGroup();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(497,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_ruleSet1614); if (state.failed) return ;
            dbg.location(497,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:16: WS
            	    {
            	    dbg.location(497,16);
            	    match(input,WS,FOLLOW_WS_in_ruleSet1616); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop79;
                }
            } while (true);
            } finally {dbg.exitSubRule(79);}

            dbg.location(497,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_ruleSet1619);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(498,13);
            pushFollow(FOLLOW_declarations_in_ruleSet1633);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(499,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_ruleSet1643); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RBRACE));
                    input.consume(); //consume the RBRACE as well
                    
        }
        finally {
        }
        dbg.location(500, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:507:1: declarations : ( declaration )? ( SEMI ( WS )* ( declaration )? )* ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(507, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:508:5: ( ( declaration )? ( SEMI ( WS )* ( declaration )? )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:9: ( declaration )? ( SEMI ( WS )* ( declaration )? )*
            {
            dbg.location(510,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:9: ( declaration )?
            int alt80=2;
            try { dbg.enterSubRule(80);
            try { dbg.enterDecision(80, decisionCanBacktrack[80]);

            int LA80_0 = input.LA(1);

            if ( (LA80_0==IDENT||LA80_0==GEN) ) {
                alt80=1;
            }
            } finally {dbg.exitDecision(80);}

            switch (alt80) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:9: declaration
                    {
                    dbg.location(510,9);
                    pushFollow(FOLLOW_declaration_in_declarations1691);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(80);}

            dbg.location(510,22);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:22: ( SEMI ( WS )* ( declaration )? )*
            try { dbg.enterSubRule(83);

            loop83:
            do {
                int alt83=2;
                try { dbg.enterDecision(83, decisionCanBacktrack[83]);

                int LA83_0 = input.LA(1);

                if ( (LA83_0==SEMI) ) {
                    alt83=1;
                }


                } finally {dbg.exitDecision(83);}

                switch (alt83) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:23: SEMI ( WS )* ( declaration )?
            	    {
            	    dbg.location(510,23);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations1695); if (state.failed) return ;
            	    dbg.location(510,28);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:28: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:28: WS
            	    	    {
            	    	    dbg.location(510,28);
            	    	    match(input,WS,FOLLOW_WS_in_declarations1697); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop81;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(81);}

            	    dbg.location(510,32);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:32: ( declaration )?
            	    int alt82=2;
            	    try { dbg.enterSubRule(82);
            	    try { dbg.enterDecision(82, decisionCanBacktrack[82]);

            	    int LA82_0 = input.LA(1);

            	    if ( (LA82_0==IDENT||LA82_0==GEN) ) {
            	        alt82=1;
            	    }
            	    } finally {dbg.exitDecision(82);}

            	    switch (alt82) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:32: declaration
            	            {
            	            dbg.location(510,32);
            	            pushFollow(FOLLOW_declaration_in_declarations1700);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(82);}


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
        dbg.location(511, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:1: selectorsGroup : selector ( COMMA ( WS )* selector )* ;
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(513, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:5: ( selector ( COMMA ( WS )* selector )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:7: selector ( COMMA ( WS )* selector )*
            {
            dbg.location(514,7);
            pushFollow(FOLLOW_selector_in_selectorsGroup1724);
            selector();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(514,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:16: ( COMMA ( WS )* selector )*
            try { dbg.enterSubRule(85);

            loop85:
            do {
                int alt85=2;
                try { dbg.enterDecision(85, decisionCanBacktrack[85]);

                int LA85_0 = input.LA(1);

                if ( (LA85_0==COMMA) ) {
                    alt85=1;
                }


                } finally {dbg.exitDecision(85);}

                switch (alt85) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:17: COMMA ( WS )* selector
            	    {
            	    dbg.location(514,17);
            	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup1727); if (state.failed) return ;
            	    dbg.location(514,23);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:23: ( WS )*
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

            	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:23: WS
            	    	    {
            	    	    dbg.location(514,23);
            	    	    match(input,WS,FOLLOW_WS_in_selectorsGroup1729); if (state.failed) return ;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop84;
            	        }
            	    } while (true);
            	    } finally {dbg.exitSubRule(84);}

            	    dbg.location(514,27);
            	    pushFollow(FOLLOW_selector_in_selectorsGroup1732);
            	    selector();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop85;
                }
            } while (true);
            } finally {dbg.exitSubRule(85);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(515, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:517:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(517, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(518,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector1755);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(518,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(86);

            loop86:
            do {
                int alt86=2;
                try { dbg.enterDecision(86, decisionCanBacktrack[86]);

                int LA86_0 = input.LA(1);

                if ( (LA86_0==IDENT||LA86_0==GEN||LA86_0==COLON||(LA86_0>=PLUS && LA86_0<=TILDE)||(LA86_0>=STAR && LA86_0<=DCOLON)||LA86_0==115) ) {
                    alt86=1;
                }


                } finally {dbg.exitDecision(86);}

                switch (alt86) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:518:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(518,31);
            	    pushFollow(FOLLOW_combinator_in_selector1758);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(518,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector1760);
            	    simpleSelectorSequence();

            	    state._fsp--;
            	    if (state.failed) return ;

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
        dbg.location(519, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:522:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(522, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:2: ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) )
            int alt89=2;
            try { dbg.enterDecision(89, decisionCanBacktrack[89]);

            int LA89_0 = input.LA(1);

            if ( (LA89_0==IDENT||LA89_0==GEN||(LA89_0>=STAR && LA89_0<=PIPE)) ) {
                alt89=1;
            }
            else if ( (LA89_0==COLON||(LA89_0>=HASH && LA89_0<=DCOLON)||LA89_0==115) ) {
                alt89=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 89, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(89);}

            switch (alt89) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:528:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    {
                    dbg.location(528,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:528:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:528:5: typeSelector ( ( esPred )=> elementSubsequent )*
                    {
                    dbg.location(528,5);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence1800);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(528,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:528:18: ( ( esPred )=> elementSubsequent )*
                    try { dbg.enterSubRule(87);

                    loop87:
                    do {
                        int alt87=2;
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:528:19: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(528,29);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence1807);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop87;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(87);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:530:2: ( ( ( esPred )=> elementSubsequent )+ )
                    {
                    dbg.location(530,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:530:2: ( ( ( esPred )=> elementSubsequent )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:530:4: ( ( esPred )=> elementSubsequent )+
                    {
                    dbg.location(530,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:530:4: ( ( esPred )=> elementSubsequent )+
                    int cnt88=0;
                    try { dbg.enterSubRule(88);

                    loop88:
                    do {
                        int alt88=2;
                        try { dbg.enterDecision(88, decisionCanBacktrack[88]);

                        switch ( input.LA(1) ) {
                        case HASH:
                            {
                            int LA88_2 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt88=1;
                            }


                            }
                            break;
                        case 115:
                            {
                            int LA88_3 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt88=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA88_4 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt88=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA88_5 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt88=1;
                            }


                            }
                            break;
                        case COLON:
                        case DCOLON:
                            {
                            int LA88_6 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt88=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(88);}

                        switch (alt88) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:530:5: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(530,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence1825);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt88 >= 1 ) break loop88;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(88, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt88++;
                    } while (true);
                    } finally {dbg.exitSubRule(88);}


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
        dbg.location(531, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:1: typeSelector options {k=2; } : ( ( nsPred )=> namespace_wqname_prefix )? ( elementName ( WS )* ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(545, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:3: ( ( ( nsPred )=> namespace_wqname_prefix )? ( elementName ( WS )* ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:6: ( ( nsPred )=> namespace_wqname_prefix )? ( elementName ( WS )* )
            {
            dbg.location(547,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:6: ( ( nsPred )=> namespace_wqname_prefix )?
            int alt90=2;
            try { dbg.enterSubRule(90);
            try { dbg.enterDecision(90, decisionCanBacktrack[90]);

            try {
                isCyclicDecision = true;
                alt90 = dfa90.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(90);}

            switch (alt90) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:7: ( nsPred )=> namespace_wqname_prefix
                    {
                    dbg.location(547,17);
                    pushFollow(FOLLOW_namespace_wqname_prefix_in_typeSelector1876);
                    namespace_wqname_prefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(90);}

            dbg.location(547,43);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:43: ( elementName ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:45: elementName ( WS )*
            {
            dbg.location(547,45);
            pushFollow(FOLLOW_elementName_in_typeSelector1882);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(547,57);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:57: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:57: WS
            	    {
            	    dbg.location(547,57);
            	    match(input,WS,FOLLOW_WS_in_typeSelector1884); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop91;
                }
            } while (true);
            } finally {dbg.exitSubRule(91);}


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(548, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:550:2: nsPred : ( IDENT | STAR )? PIPE ;
    public final void nsPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "nsPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(550, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:551:3: ( ( IDENT | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:552:3: ( IDENT | STAR )? PIPE
            {
            dbg.location(552,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:552:3: ( IDENT | STAR )?
            int alt92=2;
            try { dbg.enterSubRule(92);
            try { dbg.enterDecision(92, decisionCanBacktrack[92]);

            int LA92_0 = input.LA(1);

            if ( (LA92_0==IDENT||LA92_0==STAR) ) {
                alt92=1;
            }
            } finally {dbg.exitDecision(92);}

            switch (alt92) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    {
                    dbg.location(552,3);
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
                    break;

            }
            } finally {dbg.exitSubRule(92);}

            dbg.location(552,19);
            match(input,PIPE,FOLLOW_PIPE_in_nsPred1918); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(553, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:561:2: namespace_wqname_prefix : ( ( namespace_prefix ( WS )* )? PIPE | namespace_wildcard_prefix ( WS )* PIPE );
    public final void namespace_wqname_prefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace_wqname_prefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(561, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:3: ( ( namespace_prefix ( WS )* )? PIPE | namespace_wildcard_prefix ( WS )* PIPE )
            int alt96=2;
            try { dbg.enterDecision(96, decisionCanBacktrack[96]);

            int LA96_0 = input.LA(1);

            if ( (LA96_0==IDENT||LA96_0==PIPE) ) {
                alt96=1;
            }
            else if ( (LA96_0==STAR) ) {
                alt96=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 96, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(96);}

            switch (alt96) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:5: ( namespace_prefix ( WS )* )? PIPE
                    {
                    dbg.location(562,5);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:5: ( namespace_prefix ( WS )* )?
                    int alt94=2;
                    try { dbg.enterSubRule(94);
                    try { dbg.enterDecision(94, decisionCanBacktrack[94]);

                    int LA94_0 = input.LA(1);

                    if ( (LA94_0==IDENT) ) {
                        alt94=1;
                    }
                    } finally {dbg.exitDecision(94);}

                    switch (alt94) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:7: namespace_prefix ( WS )*
                            {
                            dbg.location(562,7);
                            pushFollow(FOLLOW_namespace_prefix_in_namespace_wqname_prefix1948);
                            namespace_prefix();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(562,24);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:24: ( WS )*
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

                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:24: WS
                            	    {
                            	    dbg.location(562,24);
                            	    match(input,WS,FOLLOW_WS_in_namespace_wqname_prefix1950); if (state.failed) return ;

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

                    dbg.location(562,31);
                    match(input,PIPE,FOLLOW_PIPE_in_namespace_wqname_prefix1956); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:6: namespace_wildcard_prefix ( WS )* PIPE
                    {
                    dbg.location(563,6);
                    pushFollow(FOLLOW_namespace_wildcard_prefix_in_namespace_wqname_prefix1963);
                    namespace_wildcard_prefix();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(563,32);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:32: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:563:32: WS
                    	    {
                    	    dbg.location(563,32);
                    	    match(input,WS,FOLLOW_WS_in_namespace_wqname_prefix1965); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop95;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(95);}

                    dbg.location(563,36);
                    match(input,PIPE,FOLLOW_PIPE_in_namespace_wqname_prefix1968); if (state.failed) return ;

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
        dbg.location(564, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:566:1: namespace_wildcard_prefix : STAR ;
    public final void namespace_wildcard_prefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace_wildcard_prefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(566, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:4: ( STAR )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:568:4: STAR
            {
            dbg.location(568,4);
            match(input,STAR,FOLLOW_STAR_in_namespace_wildcard_prefix1990); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(569, 4);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:571:1: esPred : ( '#' | HASH | DOT | LBRACKET | COLON | DCOLON );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(571, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:572:5: ( '#' | HASH | DOT | LBRACKET | COLON | DCOLON )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(572,5);
            if ( input.LA(1)==COLON||(input.LA(1)>=HASH && input.LA(1)<=DCOLON)||input.LA(1)==115 ) {
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
        dbg.location(573, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:575:1: elementSubsequent : ( cssId | cssClass | attrib | pseudo ) ( WS )* ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(575, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:576:5: ( ( cssId | cssClass | attrib | pseudo ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:577:5: ( cssId | cssClass | attrib | pseudo ) ( WS )*
            {
            dbg.location(577,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:577:5: ( cssId | cssClass | attrib | pseudo )
            int alt97=4;
            try { dbg.enterSubRule(97);
            try { dbg.enterDecision(97, decisionCanBacktrack[97]);

            switch ( input.LA(1) ) {
            case HASH:
            case 115:
                {
                alt97=1;
                }
                break;
            case DOT:
                {
                alt97=2;
                }
                break;
            case LBRACKET:
                {
                alt97=3;
                }
                break;
            case COLON:
            case DCOLON:
                {
                alt97=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 97, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(97);}

            switch (alt97) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:578:6: cssId
                    {
                    dbg.location(578,6);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent2066);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:8: cssClass
                    {
                    dbg.location(579,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent2075);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:580:11: attrib
                    {
                    dbg.location(580,11);
                    pushFollow(FOLLOW_attrib_in_elementSubsequent2087);
                    attrib();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:581:11: pseudo
                    {
                    dbg.location(581,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent2099);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(97);}

            dbg.location(583,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:5: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:5: WS
            	    {
            	    dbg.location(583,5);
            	    match(input,WS,FOLLOW_WS_in_elementSubsequent2111); if (state.failed) return ;

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
        dbg.location(584, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:1: cssId : ( HASH | ( '#' NAME ) );
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(587, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:5: ( HASH | ( '#' NAME ) )
            int alt99=2;
            try { dbg.enterDecision(99, decisionCanBacktrack[99]);

            int LA99_0 = input.LA(1);

            if ( (LA99_0==HASH) ) {
                alt99=1;
            }
            else if ( (LA99_0==115) ) {
                alt99=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 99, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(99);}

            switch (alt99) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:7: HASH
                    {
                    dbg.location(588,7);
                    match(input,HASH,FOLLOW_HASH_in_cssId2134); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:14: ( '#' NAME )
                    {
                    dbg.location(588,14);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:14: ( '#' NAME )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:16: '#' NAME
                    {
                    dbg.location(588,16);
                    match(input,115,FOLLOW_115_in_cssId2140); if (state.failed) return ;
                    dbg.location(588,20);
                    match(input,NAME,FOLLOW_NAME_in_cssId2142); if (state.failed) return ;

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
        dbg.location(589, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:595:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(595, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:7: DOT ( IDENT | GEN )
            {
            dbg.location(596,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass2170); if (state.failed) return ;
            dbg.location(596,11);
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
        dbg.location(597, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:604:1: elementName : ( ( IDENT | GEN ) | '*' );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(604, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:5: ( ( IDENT | GEN ) | '*' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(605,5);
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
        dbg.location(606, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:608:1: attrib : LBRACKET ( namespace_wqname_prefix )? ( WS )* attrib_name ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( WS )* attrib_value ( WS )* )? RBRACKET ;
    public final void attrib() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrib");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(608, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:5: ( LBRACKET ( namespace_wqname_prefix )? ( WS )* attrib_name ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( WS )* attrib_value ( WS )* )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:609:7: LBRACKET ( namespace_wqname_prefix )? ( WS )* attrib_name ( WS )* ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( WS )* attrib_value ( WS )* )? RBRACKET
            {
            dbg.location(609,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_attrib2245); if (state.failed) return ;
            dbg.location(610,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:6: ( namespace_wqname_prefix )?
            int alt100=2;
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:6: namespace_wqname_prefix
                    {
                    dbg.location(610,6);
                    pushFollow(FOLLOW_namespace_wqname_prefix_in_attrib2252);
                    namespace_wqname_prefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(100);}

            dbg.location(610,31);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:31: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:610:31: WS
            	    {
            	    dbg.location(610,31);
            	    match(input,WS,FOLLOW_WS_in_attrib2255); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop101;
                }
            } while (true);
            } finally {dbg.exitSubRule(101);}

            dbg.location(611,9);
            pushFollow(FOLLOW_attrib_name_in_attrib2266);
            attrib_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(611,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:611:21: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:611:21: WS
            	    {
            	    dbg.location(611,21);
            	    match(input,WS,FOLLOW_WS_in_attrib2268); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop102;
                }
            } while (true);
            } finally {dbg.exitSubRule(102);}

            dbg.location(613,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:13: ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( WS )* attrib_value ( WS )* )?
            int alt105=2;
            try { dbg.enterSubRule(105);
            try { dbg.enterDecision(105, decisionCanBacktrack[105]);

            int LA105_0 = input.LA(1);

            if ( ((LA105_0>=OPEQ && LA105_0<=CONTAINS)) ) {
                alt105=1;
            }
            } finally {dbg.exitDecision(105);}

            switch (alt105) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:614:17: ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( WS )* attrib_value ( WS )*
                    {
                    dbg.location(614,17);
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

                    dbg.location(622,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:17: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:17: WS
                    	    {
                    	    dbg.location(622,17);
                    	    match(input,WS,FOLLOW_WS_in_attrib2490); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop103;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(103);}

                    dbg.location(623,17);
                    pushFollow(FOLLOW_attrib_value_in_attrib2509);
                    attrib_value();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(624,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:17: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:624:17: WS
                    	    {
                    	    dbg.location(624,17);
                    	    match(input,WS,FOLLOW_WS_in_attrib2527); if (state.failed) return ;

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

            dbg.location(627,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_attrib2556); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(628, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:634:1: syncTo_IDENT_RBRACKET_LBRACE : ;
    public final void syncTo_IDENT_RBRACKET_LBRACE() throws RecognitionException {

                syncToSet(BitSet.of(IDENT, RBRACKET, LBRACE));
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_IDENT_RBRACKET_LBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(634, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:638:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:639:6: 
            {
            }

        }
        finally {
        }
        dbg.location(639, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:642:1: attrib_name : IDENT ;
    public final void attrib_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrib_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(642, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:4: IDENT
            {
            dbg.location(643,4);
            match(input,IDENT,FOLLOW_IDENT_in_attrib_name2599); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(644, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:646:1: attrib_value : ( IDENT | STRING ) ;
    public final void attrib_value() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrib_value");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(646, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:647:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:2: ( IDENT | STRING )
            {
            dbg.location(648,2);
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
        dbg.location(652, 9);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:654:1: pseudo : ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( expr | '*' )? RPAREN )? ) | ( NOT ( WS )* LPAREN ( WS )* ( simpleSelectorSequence )? RPAREN ) ) ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(654, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:655:5: ( ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( expr | '*' )? RPAREN )? ) | ( NOT ( WS )* LPAREN ( WS )* ( simpleSelectorSequence )? RPAREN ) ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:655:7: ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( expr | '*' )? RPAREN )? ) | ( NOT ( WS )* LPAREN ( WS )* ( simpleSelectorSequence )? RPAREN ) )
            {
            dbg.location(655,7);
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

            dbg.location(656,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:14: ( ( ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( expr | '*' )? RPAREN )? ) | ( NOT ( WS )* LPAREN ( WS )* ( simpleSelectorSequence )? RPAREN ) )
            int alt113=2;
            try { dbg.enterSubRule(113);
            try { dbg.enterDecision(113, decisionCanBacktrack[113]);

            int LA113_0 = input.LA(1);

            if ( (LA113_0==IDENT||LA113_0==GEN) ) {
                alt113=1;
            }
            else if ( (LA113_0==NOT) ) {
                alt113=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 113, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(113);}

            switch (alt113) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:657:17: ( ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( expr | '*' )? RPAREN )? )
                    {
                    dbg.location(657,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:657:17: ( ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( expr | '*' )? RPAREN )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:658:21: ( IDENT | GEN ) ( ( WS )* LPAREN ( WS )* ( expr | '*' )? RPAREN )?
                    {
                    dbg.location(658,21);
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

                    dbg.location(659,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:659:21: ( ( WS )* LPAREN ( WS )* ( expr | '*' )? RPAREN )?
                    int alt109=2;
                    try { dbg.enterSubRule(109);
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:25: ( WS )* LPAREN ( WS )* ( expr | '*' )? RPAREN
                            {
                            dbg.location(660,25);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:25: ( WS )*
                            try { dbg.enterSubRule(106);

                            loop106:
                            do {
                                int alt106=2;
                                try { dbg.enterDecision(106, decisionCanBacktrack[106]);

                                int LA106_0 = input.LA(1);

                                if ( (LA106_0==WS) ) {
                                    alt106=1;
                                }


                                } finally {dbg.exitDecision(106);}

                                switch (alt106) {
                            	case 1 :
                            	    dbg.enterAlt(1);

                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:25: WS
                            	    {
                            	    dbg.location(660,25);
                            	    match(input,WS,FOLLOW_WS_in_pseudo2794); if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop106;
                                }
                            } while (true);
                            } finally {dbg.exitSubRule(106);}

                            dbg.location(660,29);
                            match(input,LPAREN,FOLLOW_LPAREN_in_pseudo2797); if (state.failed) return ;
                            dbg.location(660,36);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:36: ( WS )*
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

                            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:36: WS
                            	    {
                            	    dbg.location(660,36);
                            	    match(input,WS,FOLLOW_WS_in_pseudo2799); if (state.failed) return ;

                            	    }
                            	    break;

                            	default :
                            	    break loop107;
                                }
                            } while (true);
                            } finally {dbg.exitSubRule(107);}

                            dbg.location(660,40);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:40: ( expr | '*' )?
                            int alt108=3;
                            try { dbg.enterSubRule(108);
                            try { dbg.enterDecision(108, decisionCanBacktrack[108]);

                            int LA108_0 = input.LA(1);

                            if ( ((LA108_0>=IDENT && LA108_0<=URI)||LA108_0==GEN||LA108_0==PLUS||LA108_0==MINUS||LA108_0==HASH||(LA108_0>=NUMBER && LA108_0<=DIMENSION)) ) {
                                alt108=1;
                            }
                            else if ( (LA108_0==STAR) ) {
                                alt108=2;
                            }
                            } finally {dbg.exitDecision(108);}

                            switch (alt108) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:42: expr
                                    {
                                    dbg.location(660,42);
                                    pushFollow(FOLLOW_expr_in_pseudo2804);
                                    expr();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;
                                case 2 :
                                    dbg.enterAlt(2);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:49: '*'
                                    {
                                    dbg.location(660,49);
                                    match(input,STAR,FOLLOW_STAR_in_pseudo2808); if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(108);}

                            dbg.location(660,56);
                            match(input,RPAREN,FOLLOW_RPAREN_in_pseudo2813); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(109);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:17: ( NOT ( WS )* LPAREN ( WS )* ( simpleSelectorSequence )? RPAREN )
                    {
                    dbg.location(664,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:17: ( NOT ( WS )* LPAREN ( WS )* ( simpleSelectorSequence )? RPAREN )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:19: NOT ( WS )* LPAREN ( WS )* ( simpleSelectorSequence )? RPAREN
                    {
                    dbg.location(664,19);
                    match(input,NOT,FOLLOW_NOT_in_pseudo2892); if (state.failed) return ;
                    dbg.location(664,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:23: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:23: WS
                    	    {
                    	    dbg.location(664,23);
                    	    match(input,WS,FOLLOW_WS_in_pseudo2894); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop110;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(110);}

                    dbg.location(664,27);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo2897); if (state.failed) return ;
                    dbg.location(664,34);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:34: ( WS )*
                    try { dbg.enterSubRule(111);

                    loop111:
                    do {
                        int alt111=2;
                        try { dbg.enterDecision(111, decisionCanBacktrack[111]);

                        int LA111_0 = input.LA(1);

                        if ( (LA111_0==WS) ) {
                            alt111=1;
                        }


                        } finally {dbg.exitDecision(111);}

                        switch (alt111) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:34: WS
                    	    {
                    	    dbg.location(664,34);
                    	    match(input,WS,FOLLOW_WS_in_pseudo2899); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop111;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(111);}

                    dbg.location(664,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:38: ( simpleSelectorSequence )?
                    int alt112=2;
                    try { dbg.enterSubRule(112);
                    try { dbg.enterDecision(112, decisionCanBacktrack[112]);

                    int LA112_0 = input.LA(1);

                    if ( (LA112_0==IDENT||LA112_0==GEN||LA112_0==COLON||(LA112_0>=STAR && LA112_0<=DCOLON)||LA112_0==115) ) {
                        alt112=1;
                    }
                    } finally {dbg.exitDecision(112);}

                    switch (alt112) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:664:38: simpleSelectorSequence
                            {
                            dbg.location(664,38);
                            pushFollow(FOLLOW_simpleSelectorSequence_in_pseudo2902);
                            simpleSelectorSequence();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(112);}

                    dbg.location(664,62);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo2905); if (state.failed) return ;

                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(113);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(666, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:668:1: declaration : property COLON ( WS )* expr ( prio )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(668, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:669:5: ( property COLON ( WS )* expr ( prio )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:671:5: property COLON ( WS )* expr ( prio )?
            {
            dbg.location(671,5);
            pushFollow(FOLLOW_property_in_declaration2949);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(671,14);
            match(input,COLON,FOLLOW_COLON_in_declaration2951); if (state.failed) return ;
            dbg.location(671,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:671:20: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:671:20: WS
            	    {
            	    dbg.location(671,20);
            	    match(input,WS,FOLLOW_WS_in_declaration2953); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop114;
                }
            } while (true);
            } finally {dbg.exitSubRule(114);}

            dbg.location(671,24);
            pushFollow(FOLLOW_expr_in_declaration2956);
            expr();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(671,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:671:29: ( prio )?
            int alt115=2;
            try { dbg.enterSubRule(115);
            try { dbg.enterDecision(115, decisionCanBacktrack[115]);

            int LA115_0 = input.LA(1);

            if ( (LA115_0==IMPORTANT_SYM) ) {
                alt115=1;
            }
            } finally {dbg.exitDecision(115);}

            switch (alt115) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:671:29: prio
                    {
                    dbg.location(671,29);
                    pushFollow(FOLLOW_prio_in_declaration2958);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(115);}


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
        dbg.location(672, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:682:1: syncTo_IDENT_RBRACE : ;
    public final void syncTo_IDENT_RBRACE() throws RecognitionException {

                syncToSet(BitSet.of(IDENT, RBRACE));
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_IDENT_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(682, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:686:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:687:6: 
            {
            }

        }
        finally {
        }
        dbg.location(687, 6);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "syncTo_IDENT_RBRACE");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "syncTo_IDENT_RBRACE"


    // $ANTLR start "syncTo_RBRACE"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:689:1: syncTo_RBRACE : ;
    public final void syncTo_RBRACE() throws RecognitionException {

                syncToRBRACE(1); //initial nest == 1
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(689, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:693:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:694:6: 
            {
            }

        }
        finally {
        }
        dbg.location(694, 6);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "syncTo_RBRACE");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "syncTo_RBRACE"


    // $ANTLR start "syncToFollow"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:697:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(697, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:701:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:702:6: 
            {
            }

        }
        finally {
        }
        dbg.location(702, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:705:1: prio : IMPORTANT_SYM ( WS )* ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(705, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:706:5: ( IMPORTANT_SYM ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:706:7: IMPORTANT_SYM ( WS )*
            {
            dbg.location(706,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio3088); if (state.failed) return ;
            dbg.location(706,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:706:21: ( WS )*
            try { dbg.enterSubRule(116);

            loop116:
            do {
                int alt116=2;
                try { dbg.enterDecision(116, decisionCanBacktrack[116]);

                int LA116_0 = input.LA(1);

                if ( (LA116_0==WS) ) {
                    alt116=1;
                }


                } finally {dbg.exitDecision(116);}

                switch (alt116) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:706:21: WS
            	    {
            	    dbg.location(706,21);
            	    match(input,WS,FOLLOW_WS_in_prio3090); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop116;
                }
            } while (true);
            } finally {dbg.exitSubRule(116);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(707, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:709:1: expr : term ( operator term )* ;
    public final void expr() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expr");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(709, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:710:5: ( term ( operator term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:710:7: term ( operator term )*
            {
            dbg.location(710,7);
            pushFollow(FOLLOW_term_in_expr3112);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(710,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:710:12: ( operator term )*
            try { dbg.enterSubRule(117);

            loop117:
            do {
                int alt117=2;
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:710:13: operator term
            	    {
            	    dbg.location(710,13);
            	    pushFollow(FOLLOW_operator_in_expr3115);
            	    operator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(710,22);
            	    pushFollow(FOLLOW_term_in_expr3117);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop117;
                }
            } while (true);
            } finally {dbg.exitSubRule(117);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(711, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:713:1: term : ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( WS )* ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(713, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:5: ( ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( WS )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:7: ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( WS )*
            {
            dbg.location(714,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:7: ( unaryOperator )?
            int alt118=2;
            try { dbg.enterSubRule(118);
            try { dbg.enterDecision(118, decisionCanBacktrack[118]);

            int LA118_0 = input.LA(1);

            if ( (LA118_0==PLUS||LA118_0==MINUS) ) {
                alt118=1;
            }
            } finally {dbg.exitDecision(118);}

            switch (alt118) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:7: unaryOperator
                    {
                    dbg.location(714,7);
                    pushFollow(FOLLOW_unaryOperator_in_term3140);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(118);}

            dbg.location(715,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:715:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function )
            int alt119=7;
            try { dbg.enterSubRule(119);
            try { dbg.enterDecision(119, decisionCanBacktrack[119]);

            try {
                isCyclicDecision = true;
                alt119 = dfa119.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(119);}

            switch (alt119) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:716:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(716,9);
                    if ( (input.LA(1)>=NUMBER && input.LA(1)<=DIMENSION) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:728:7: STRING
                    {
                    dbg.location(728,7);
                    match(input,STRING,FOLLOW_STRING_in_term3344); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:729:7: IDENT
                    {
                    dbg.location(729,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term3352); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:730:7: GEN
                    {
                    dbg.location(730,7);
                    match(input,GEN,FOLLOW_GEN_in_term3360); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:7: URI
                    {
                    dbg.location(731,7);
                    match(input,URI,FOLLOW_URI_in_term3368); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:732:7: hexColor
                    {
                    dbg.location(732,7);
                    pushFollow(FOLLOW_hexColor_in_term3376);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:733:7: function
                    {
                    dbg.location(733,7);
                    pushFollow(FOLLOW_function_in_term3384);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(119);}

            dbg.location(735,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:5: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:5: WS
            	    {
            	    dbg.location(735,5);
            	    match(input,WS,FOLLOW_WS_in_term3396); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop120;
                }
            } while (true);
            } finally {dbg.exitSubRule(120);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(736, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:738:1: function : function_name ( WS )* LPAREN ( WS )* ( expr | ( attribute ( COMMA ( WS )* attribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(738, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:2: ( function_name ( WS )* LPAREN ( WS )* ( expr | ( attribute ( COMMA ( WS )* attribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:5: function_name ( WS )* LPAREN ( WS )* ( expr | ( attribute ( COMMA ( WS )* attribute )* ) ) RPAREN
            {
            dbg.location(739,5);
            pushFollow(FOLLOW_function_name_in_function3412);
            function_name();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(739,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:19: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:19: WS
            	    {
            	    dbg.location(739,19);
            	    match(input,WS,FOLLOW_WS_in_function3414); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop121;
                }
            } while (true);
            } finally {dbg.exitSubRule(121);}

            dbg.location(740,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function3419); if (state.failed) return ;
            dbg.location(740,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:740:10: ( WS )*
            try { dbg.enterSubRule(122);

            loop122:
            do {
                int alt122=2;
                try { dbg.enterDecision(122, decisionCanBacktrack[122]);

                int LA122_0 = input.LA(1);

                if ( (LA122_0==WS) ) {
                    alt122=1;
                }


                } finally {dbg.exitDecision(122);}

                switch (alt122) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:740:10: WS
            	    {
            	    dbg.location(740,10);
            	    match(input,WS,FOLLOW_WS_in_function3421); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop122;
                }
            } while (true);
            } finally {dbg.exitSubRule(122);}

            dbg.location(741,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:741:3: ( expr | ( attribute ( COMMA ( WS )* attribute )* ) )
            int alt125=2;
            try { dbg.enterSubRule(125);
            try { dbg.enterDecision(125, decisionCanBacktrack[125]);

            try {
                isCyclicDecision = true;
                alt125 = dfa125.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(125);}

            switch (alt125) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:742:4: expr
                    {
                    dbg.location(742,4);
                    pushFollow(FOLLOW_expr_in_function3432);
                    expr();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:744:6: ( attribute ( COMMA ( WS )* attribute )* )
                    {
                    dbg.location(744,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:744:6: ( attribute ( COMMA ( WS )* attribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:745:5: attribute ( COMMA ( WS )* attribute )*
                    {
                    dbg.location(745,5);
                    pushFollow(FOLLOW_attribute_in_function3450);
                    attribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(745,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:745:15: ( COMMA ( WS )* attribute )*
                    try { dbg.enterSubRule(124);

                    loop124:
                    do {
                        int alt124=2;
                        try { dbg.enterDecision(124, decisionCanBacktrack[124]);

                        int LA124_0 = input.LA(1);

                        if ( (LA124_0==COMMA) ) {
                            alt124=1;
                        }


                        } finally {dbg.exitDecision(124);}

                        switch (alt124) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:745:16: COMMA ( WS )* attribute
                    	    {
                    	    dbg.location(745,16);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function3453); if (state.failed) return ;
                    	    dbg.location(745,22);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:745:22: ( WS )*
                    	    try { dbg.enterSubRule(123);

                    	    loop123:
                    	    do {
                    	        int alt123=2;
                    	        try { dbg.enterDecision(123, decisionCanBacktrack[123]);

                    	        int LA123_0 = input.LA(1);

                    	        if ( (LA123_0==WS) ) {
                    	            alt123=1;
                    	        }


                    	        } finally {dbg.exitDecision(123);}

                    	        switch (alt123) {
                    	    	case 1 :
                    	    	    dbg.enterAlt(1);

                    	    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:745:22: WS
                    	    	    {
                    	    	    dbg.location(745,22);
                    	    	    match(input,WS,FOLLOW_WS_in_function3455); if (state.failed) return ;

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop123;
                    	        }
                    	    } while (true);
                    	    } finally {dbg.exitSubRule(123);}

                    	    dbg.location(745,26);
                    	    pushFollow(FOLLOW_attribute_in_function3458);
                    	    attribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop124;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(124);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(125);}

            dbg.location(748,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function3479); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 

        }
        finally {
        }
        dbg.location(749, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:755:1: function_name : ( IDENT COLON )? IDENT ( DOT IDENT )* ;
    public final void function_name() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function_name");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(755, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:759:2: ( ( IDENT COLON )? IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:759:4: ( IDENT COLON )? IDENT ( DOT IDENT )*
            {
            dbg.location(759,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:759:4: ( IDENT COLON )?
            int alt126=2;
            try { dbg.enterSubRule(126);
            try { dbg.enterDecision(126, decisionCanBacktrack[126]);

            int LA126_0 = input.LA(1);

            if ( (LA126_0==IDENT) ) {
                int LA126_1 = input.LA(2);

                if ( (LA126_1==COLON) ) {
                    alt126=1;
                }
            }
            } finally {dbg.exitDecision(126);}

            switch (alt126) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:759:5: IDENT COLON
                    {
                    dbg.location(759,5);
                    match(input,IDENT,FOLLOW_IDENT_in_function_name3527); if (state.failed) return ;
                    dbg.location(759,11);
                    match(input,COLON,FOLLOW_COLON_in_function_name3529); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(126);}

            dbg.location(759,19);
            match(input,IDENT,FOLLOW_IDENT_in_function_name3533); if (state.failed) return ;
            dbg.location(759,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:759:25: ( DOT IDENT )*
            try { dbg.enterSubRule(127);

            loop127:
            do {
                int alt127=2;
                try { dbg.enterDecision(127, decisionCanBacktrack[127]);

                int LA127_0 = input.LA(1);

                if ( (LA127_0==DOT) ) {
                    alt127=1;
                }


                } finally {dbg.exitDecision(127);}

                switch (alt127) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:759:26: DOT IDENT
            	    {
            	    dbg.location(759,26);
            	    match(input,DOT,FOLLOW_DOT_in_function_name3536); if (state.failed) return ;
            	    dbg.location(759,30);
            	    match(input,IDENT,FOLLOW_IDENT_in_function_name3538); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop127;
                }
            } while (true);
            } finally {dbg.exitSubRule(127);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(760, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:762:1: attribute : attrname ( WS )* OPEQ ( WS )* attrvalue ;
    public final void attribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(762, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:763:2: ( attrname ( WS )* OPEQ ( WS )* attrvalue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:763:4: attrname ( WS )* OPEQ ( WS )* attrvalue
            {
            dbg.location(763,4);
            pushFollow(FOLLOW_attrname_in_attribute3560);
            attrname();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(763,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:763:13: ( WS )*
            try { dbg.enterSubRule(128);

            loop128:
            do {
                int alt128=2;
                try { dbg.enterDecision(128, decisionCanBacktrack[128]);

                int LA128_0 = input.LA(1);

                if ( (LA128_0==WS) ) {
                    alt128=1;
                }


                } finally {dbg.exitDecision(128);}

                switch (alt128) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:763:13: WS
            	    {
            	    dbg.location(763,13);
            	    match(input,WS,FOLLOW_WS_in_attribute3562); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop128;
                }
            } while (true);
            } finally {dbg.exitSubRule(128);}

            dbg.location(763,17);
            match(input,OPEQ,FOLLOW_OPEQ_in_attribute3565); if (state.failed) return ;
            dbg.location(763,22);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:763:22: ( WS )*
            try { dbg.enterSubRule(129);

            loop129:
            do {
                int alt129=2;
                try { dbg.enterDecision(129, decisionCanBacktrack[129]);

                int LA129_0 = input.LA(1);

                if ( (LA129_0==WS) ) {
                    alt129=1;
                }


                } finally {dbg.exitDecision(129);}

                switch (alt129) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:763:22: WS
            	    {
            	    dbg.location(763,22);
            	    match(input,WS,FOLLOW_WS_in_attribute3567); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop129;
                }
            } while (true);
            } finally {dbg.exitSubRule(129);}

            dbg.location(763,26);
            pushFollow(FOLLOW_attrvalue_in_attribute3570);
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
        dbg.location(764, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:1: attrname : IDENT ( DOT IDENT )* ;
    public final void attrname() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrname");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(766, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:767:2: ( IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:767:4: IDENT ( DOT IDENT )*
            {
            dbg.location(767,4);
            match(input,IDENT,FOLLOW_IDENT_in_attrname3585); if (state.failed) return ;
            dbg.location(767,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:767:10: ( DOT IDENT )*
            try { dbg.enterSubRule(130);

            loop130:
            do {
                int alt130=2;
                try { dbg.enterDecision(130, decisionCanBacktrack[130]);

                int LA130_0 = input.LA(1);

                if ( (LA130_0==DOT) ) {
                    alt130=1;
                }


                } finally {dbg.exitDecision(130);}

                switch (alt130) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:767:11: DOT IDENT
            	    {
            	    dbg.location(767,11);
            	    match(input,DOT,FOLLOW_DOT_in_attrname3588); if (state.failed) return ;
            	    dbg.location(767,15);
            	    match(input,IDENT,FOLLOW_IDENT_in_attrname3590); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop130;
                }
            } while (true);
            } finally {dbg.exitSubRule(130);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(768, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:770:1: attrvalue : expr ;
    public final void attrvalue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "attrvalue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(770, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:771:2: ( expr )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:771:4: expr
            {
            dbg.location(771,4);
            pushFollow(FOLLOW_expr_in_attrvalue3604);
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
        dbg.location(772, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:774:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(774, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:775:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:775:7: HASH
            {
            dbg.location(775,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor3622); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(776, 5);

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
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:528:19: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:528:20: esPred
        {
        dbg.location(528,20);
        pushFollow(FOLLOW_esPred_in_synpred1_Css31804);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:530:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:530:6: esPred
        {
        dbg.location(530,6);
        pushFollow(FOLLOW_esPred_in_synpred2_Css31822);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:7: ( nsPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:8: nsPred
        {
        dbg.location(547,8);
        pushFollow(FOLLOW_nsPred_in_synpred3_Css31873);
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


    protected DFA87 dfa87 = new DFA87(this);
    protected DFA90 dfa90 = new DFA90(this);
    protected DFA100 dfa100 = new DFA100(this);
    protected DFA109 dfa109 = new DFA109(this);
    protected DFA117 dfa117 = new DFA117(this);
    protected DFA119 dfa119 = new DFA119(this);
    protected DFA125 dfa125 = new DFA125(this);
    static final String DFA87_eotS =
        "\21\uffff";
    static final String DFA87_eofS =
        "\21\uffff";
    static final String DFA87_minS =
        "\1\6\7\uffff\5\0\4\uffff";
    static final String DFA87_maxS =
        "\1\163\7\uffff\5\0\4\uffff";
    static final String DFA87_acceptS =
        "\1\uffff\1\2\16\uffff\1\1";
    static final String DFA87_specialS =
        "\10\uffff\1\0\1\1\1\2\1\3\1\4\4\uffff}>";
    static final String[] DFA87_transitionS = {
            "\1\1\6\uffff\1\1\1\uffff\1\1\3\uffff\1\1\30\uffff\1\14\1\uffff"+
            "\3\1\1\uffff\2\1\1\10\1\12\1\13\1\14\11\uffff\1\1\61\uffff\1"+
            "\11",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            ""
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
            return "()* loopback of 528:18: ( ( esPred )=> elementSubsequent )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA87_8 = input.LA(1);

                         
                        int index87_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index87_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA87_9 = input.LA(1);

                         
                        int index87_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index87_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA87_10 = input.LA(1);

                         
                        int index87_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index87_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA87_11 = input.LA(1);

                         
                        int index87_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index87_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA87_12 = input.LA(1);

                         
                        int index87_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index87_12);
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
    static final String DFA90_eotS =
        "\7\uffff";
    static final String DFA90_eofS =
        "\7\uffff";
    static final String DFA90_minS =
        "\1\6\1\0\1\uffff\1\4\1\uffff\1\4\1\0";
    static final String DFA90_maxS =
        "\1\63\1\0\1\uffff\1\163\1\uffff\1\163\1\0";
    static final String DFA90_acceptS =
        "\2\uffff\1\1\1\uffff\1\2\2\uffff";
    static final String DFA90_specialS =
        "\1\0\1\1\4\uffff\1\2}>";
    static final String[] DFA90_transitionS = {
            "\1\1\14\uffff\1\4\36\uffff\1\3\1\2",
            "\1\uffff",
            "",
            "\1\5\1\uffff\1\4\6\uffff\1\4\1\uffff\1\4\3\uffff\1\4\30\uffff"+
            "\1\4\1\uffff\3\4\1\uffff\1\4\1\6\4\4\11\uffff\1\4\61\uffff\1"+
            "\4",
            "",
            "\1\5\1\uffff\1\4\6\uffff\1\4\1\uffff\1\4\3\uffff\1\4\30\uffff"+
            "\1\4\1\uffff\3\4\1\uffff\1\4\1\6\4\4\11\uffff\1\4\61\uffff\1"+
            "\4",
            "\1\uffff"
    };

    static final short[] DFA90_eot = DFA.unpackEncodedString(DFA90_eotS);
    static final short[] DFA90_eof = DFA.unpackEncodedString(DFA90_eofS);
    static final char[] DFA90_min = DFA.unpackEncodedStringToUnsignedChars(DFA90_minS);
    static final char[] DFA90_max = DFA.unpackEncodedStringToUnsignedChars(DFA90_maxS);
    static final short[] DFA90_accept = DFA.unpackEncodedString(DFA90_acceptS);
    static final short[] DFA90_special = DFA.unpackEncodedString(DFA90_specialS);
    static final short[][] DFA90_transition;

    static {
        int numStates = DFA90_transitionS.length;
        DFA90_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA90_transition[i] = DFA.unpackEncodedString(DFA90_transitionS[i]);
        }
    }

    class DFA90 extends DFA {

        public DFA90(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 90;
            this.eot = DFA90_eot;
            this.eof = DFA90_eof;
            this.min = DFA90_min;
            this.max = DFA90_max;
            this.accept = DFA90_accept;
            this.special = DFA90_special;
            this.transition = DFA90_transition;
        }
        public String getDescription() {
            return "547:6: ( ( nsPred )=> namespace_wqname_prefix )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA90_0 = input.LA(1);

                         
                        int index90_0 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA90_0==IDENT) ) {s = 1;}

                        else if ( (LA90_0==PIPE) && (synpred3_Css3())) {s = 2;}

                        else if ( (LA90_0==STAR) ) {s = 3;}

                        else if ( (LA90_0==GEN) ) {s = 4;}

                         
                        input.seek(index90_0);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA90_1 = input.LA(1);

                         
                        int index90_1 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 2;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index90_1);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA90_6 = input.LA(1);

                         
                        int index90_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred3_Css3()) ) {s = 2;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index90_6);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 90, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA100_eotS =
        "\5\uffff";
    static final String DFA100_eofS =
        "\5\uffff";
    static final String DFA100_minS =
        "\2\4\2\uffff\1\4";
    static final String DFA100_maxS =
        "\1\63\1\77\2\uffff\1\77";
    static final String DFA100_acceptS =
        "\2\uffff\1\1\1\2\1\uffff";
    static final String DFA100_specialS =
        "\5\uffff}>";
    static final String[] DFA100_transitionS = {
            "\1\3\1\uffff\1\1\53\uffff\2\2",
            "\1\4\56\uffff\1\2\5\uffff\7\3",
            "",
            "",
            "\1\4\56\uffff\1\2\5\uffff\7\3"
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
            return "610:6: ( namespace_wqname_prefix )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA109_eotS =
        "\4\uffff";
    static final String DFA109_eofS =
        "\4\uffff";
    static final String DFA109_minS =
        "\2\4\2\uffff";
    static final String DFA109_maxS =
        "\2\163\2\uffff";
    static final String DFA109_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA109_specialS =
        "\4\uffff}>";
    static final String[] DFA109_transitionS = {
            "\1\1\1\uffff\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\30\uffff"+
            "\1\3\1\uffff\3\3\1\uffff\6\3\10\uffff\1\2\1\3\61\uffff\1\3",
            "\1\1\1\uffff\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\30\uffff"+
            "\1\3\1\uffff\3\3\1\uffff\6\3\10\uffff\1\2\1\3\61\uffff\1\3",
            "",
            ""
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
            return "659:21: ( ( WS )* LPAREN ( WS )* ( expr | '*' )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA117_eotS =
        "\12\uffff";
    static final String DFA117_eofS =
        "\12\uffff";
    static final String DFA117_minS =
        "\1\6\1\uffff\1\4\1\uffff\2\4\1\6\3\4";
    static final String DFA117_maxS =
        "\1\114\1\uffff\1\114\1\uffff\2\114\1\6\1\114\2\100";
    static final String DFA117_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\6\uffff";
    static final String DFA117_specialS =
        "\12\uffff}>";
    static final String[] DFA117_transitionS = {
            "\3\3\1\uffff\1\1\3\uffff\1\1\1\2\3\uffff\1\3\31\uffff\2\3\2"+
            "\uffff\1\3\2\uffff\1\3\14\uffff\2\1\12\3",
            "",
            "\1\4\1\uffff\1\5\2\3\12\uffff\1\3\32\uffff\1\3\2\uffff\1\3"+
            "\2\uffff\1\3\16\uffff\12\3",
            "",
            "\1\4\1\uffff\1\5\2\3\12\uffff\1\3\32\uffff\1\3\2\uffff\1\3"+
            "\2\uffff\1\3\16\uffff\12\3",
            "\1\7\1\uffff\3\3\1\uffff\1\3\3\uffff\2\3\3\uffff\1\3\30\uffff"+
            "\3\3\2\uffff\1\3\2\uffff\1\3\1\6\3\uffff\1\1\6\uffff\15\3",
            "\1\10",
            "\1\7\1\uffff\3\3\1\uffff\1\3\3\uffff\2\3\3\uffff\1\3\31\uffff"+
            "\2\3\2\uffff\1\3\2\uffff\1\3\4\uffff\1\1\6\uffff\15\3",
            "\1\11\60\uffff\1\6\3\uffff\1\1\6\uffff\1\3",
            "\1\11\64\uffff\1\1\6\uffff\1\3"
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
            return "()* loopback of 710:12: ( operator term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA119_eotS =
        "\12\uffff";
    static final String DFA119_eofS =
        "\12\uffff";
    static final String DFA119_minS =
        "\1\6\2\uffff\1\4\4\uffff\1\4\1\uffff";
    static final String DFA119_maxS =
        "\1\114\2\uffff\1\114\4\uffff\1\114\1\uffff";
    static final String DFA119_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\7\1\uffff\1\3";
    static final String DFA119_specialS =
        "\12\uffff}>";
    static final String[] DFA119_transitionS = {
            "\1\3\1\2\1\5\12\uffff\1\4\40\uffff\1\6\16\uffff\12\1",
            "",
            "",
            "\1\10\1\uffff\3\11\1\uffff\1\11\3\uffff\2\11\3\uffff\1\11\30"+
            "\uffff\1\7\2\11\2\uffff\1\11\2\uffff\1\11\1\7\12\uffff\1\7\14"+
            "\11",
            "",
            "",
            "",
            "",
            "\1\10\1\uffff\3\11\1\uffff\1\11\3\uffff\2\11\3\uffff\1\11\31"+
            "\uffff\2\11\2\uffff\1\11\2\uffff\1\11\13\uffff\1\7\14\11",
            ""
    };

    static final short[] DFA119_eot = DFA.unpackEncodedString(DFA119_eotS);
    static final short[] DFA119_eof = DFA.unpackEncodedString(DFA119_eofS);
    static final char[] DFA119_min = DFA.unpackEncodedStringToUnsignedChars(DFA119_minS);
    static final char[] DFA119_max = DFA.unpackEncodedStringToUnsignedChars(DFA119_maxS);
    static final short[] DFA119_accept = DFA.unpackEncodedString(DFA119_acceptS);
    static final short[] DFA119_special = DFA.unpackEncodedString(DFA119_specialS);
    static final short[][] DFA119_transition;

    static {
        int numStates = DFA119_transitionS.length;
        DFA119_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA119_transition[i] = DFA.unpackEncodedString(DFA119_transitionS[i]);
        }
    }

    class DFA119 extends DFA {

        public DFA119(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 119;
            this.eot = DFA119_eot;
            this.eof = DFA119_eof;
            this.min = DFA119_min;
            this.max = DFA119_max;
            this.accept = DFA119_accept;
            this.special = DFA119_special;
            this.transition = DFA119_transition;
        }
        public String getDescription() {
            return "715:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA125_eotS =
        "\10\uffff";
    static final String DFA125_eofS =
        "\10\uffff";
    static final String DFA125_minS =
        "\1\6\1\uffff\2\4\1\6\1\uffff\2\4";
    static final String DFA125_maxS =
        "\1\114\1\uffff\2\114\1\6\1\uffff\2\100";
    static final String DFA125_acceptS =
        "\1\uffff\1\1\3\uffff\1\2\2\uffff";
    static final String DFA125_specialS =
        "\10\uffff}>";
    static final String[] DFA125_transitionS = {
            "\1\2\2\1\12\uffff\1\1\32\uffff\1\1\2\uffff\1\1\2\uffff\1\1\16"+
            "\uffff\12\1",
            "",
            "\1\3\1\uffff\3\1\6\uffff\1\1\3\uffff\1\1\30\uffff\3\1\2\uffff"+
            "\1\1\2\uffff\1\1\1\4\3\uffff\1\5\6\uffff\2\1\1\uffff\12\1",
            "\1\3\1\uffff\3\1\6\uffff\1\1\3\uffff\1\1\31\uffff\2\1\2\uffff"+
            "\1\1\2\uffff\1\1\4\uffff\1\5\6\uffff\2\1\1\uffff\12\1",
            "\1\6",
            "",
            "\1\7\60\uffff\1\4\3\uffff\1\5\6\uffff\1\1",
            "\1\7\64\uffff\1\5\6\uffff\1\1"
    };

    static final short[] DFA125_eot = DFA.unpackEncodedString(DFA125_eotS);
    static final short[] DFA125_eof = DFA.unpackEncodedString(DFA125_eofS);
    static final char[] DFA125_min = DFA.unpackEncodedStringToUnsignedChars(DFA125_minS);
    static final char[] DFA125_max = DFA.unpackEncodedStringToUnsignedChars(DFA125_maxS);
    static final short[] DFA125_accept = DFA.unpackEncodedString(DFA125_acceptS);
    static final short[] DFA125_special = DFA.unpackEncodedString(DFA125_specialS);
    static final short[][] DFA125_transition;

    static {
        int numStates = DFA125_transitionS.length;
        DFA125_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA125_transition[i] = DFA.unpackEncodedString(DFA125_transitionS[i]);
        }
    }

    class DFA125 extends DFA {

        public DFA125(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 125;
            this.eot = DFA125_eot;
            this.eof = DFA125_eof;
            this.min = DFA125_min;
            this.max = DFA125_max;
            this.accept = DFA125_accept;
            this.special = DFA125_special;
            this.transition = DFA125_transition;
        }
        public String getDescription() {
            return "741:3: ( expr | ( attribute ( COMMA ( WS )* attribute )* ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_charSet_in_styleSheet119 = new BitSet(new long[]{0x00FC10000E381870L,0x0008000000000000L});
    public static final BitSet FOLLOW_WS_in_styleSheet127 = new BitSet(new long[]{0x00FC10000E381870L,0x0008000000000000L});
    public static final BitSet FOLLOW_imports_in_styleSheet139 = new BitSet(new long[]{0x00FC10000E381870L,0x0008000000000000L});
    public static final BitSet FOLLOW_WS_in_styleSheet141 = new BitSet(new long[]{0x00FC10000E381870L,0x0008000000000000L});
    public static final BitSet FOLLOW_namespace_in_styleSheet156 = new BitSet(new long[]{0x00FC10000E381060L,0x0008000000000000L});
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
    public static final BitSet FOLLOW_resourceIdentifier_in_imports302 = new BitSet(new long[]{0x00000000000B0450L,0x0000000000000001L});
    public static final BitSet FOLLOW_WS_in_imports305 = new BitSet(new long[]{0x00000000000B0450L,0x0000000000000001L});
    public static final BitSet FOLLOW_media_query_list_in_imports308 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_SEMI_in_imports310 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEDIA_SYM_in_media331 = new BitSet(new long[]{0x00000000000B2050L,0x0000000000000001L});
    public static final BitSet FOLLOW_WS_in_media333 = new BitSet(new long[]{0x00000000000B2050L,0x0000000000000001L});
    public static final BitSet FOLLOW_media_query_list_in_media336 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_media346 = new BitSet(new long[]{0x00FC100002084050L,0x0008000000000000L});
    public static final BitSet FOLLOW_WS_in_media348 = new BitSet(new long[]{0x00FC100002084050L,0x0008000000000000L});
    public static final BitSet FOLLOW_ruleSet_in_media367 = new BitSet(new long[]{0x00FC100002084050L,0x0008000000000000L});
    public static final BitSet FOLLOW_page_in_media371 = new BitSet(new long[]{0x00FC100002084050L,0x0008000000000000L});
    public static final BitSet FOLLOW_WS_in_media375 = new BitSet(new long[]{0x00FC100002084050L,0x0008000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_media389 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_media_query_in_media_query_list409 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_COMMA_in_media_query_list413 = new BitSet(new long[]{0x00000000000B0050L,0x0000000000000001L});
    public static final BitSet FOLLOW_WS_in_media_query_list415 = new BitSet(new long[]{0x00000000000B0050L,0x0000000000000001L});
    public static final BitSet FOLLOW_media_query_in_media_query_list418 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_set_in_media_query437 = new BitSet(new long[]{0x00000000000B0050L});
    public static final BitSet FOLLOW_WS_in_media_query445 = new BitSet(new long[]{0x00000000000B0050L});
    public static final BitSet FOLLOW_media_type_in_media_query452 = new BitSet(new long[]{0x0000000000040012L});
    public static final BitSet FOLLOW_WS_in_media_query454 = new BitSet(new long[]{0x0000000000040012L});
    public static final BitSet FOLLOW_AND_in_media_query459 = new BitSet(new long[]{0x00000000000B0050L,0x0000000000000001L});
    public static final BitSet FOLLOW_WS_in_media_query461 = new BitSet(new long[]{0x00000000000B0050L,0x0000000000000001L});
    public static final BitSet FOLLOW_media_expression_in_media_query464 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_media_expression_in_media_query472 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_AND_in_media_query476 = new BitSet(new long[]{0x00000000000B0050L,0x0000000000000001L});
    public static final BitSet FOLLOW_WS_in_media_query478 = new BitSet(new long[]{0x00000000000B0050L,0x0000000000000001L});
    public static final BitSet FOLLOW_media_expression_in_media_query481 = new BitSet(new long[]{0x0000000000040002L});
    public static final BitSet FOLLOW_set_in_media_type0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_media_expression512 = new BitSet(new long[]{0x0000000000000050L});
    public static final BitSet FOLLOW_WS_in_media_expression514 = new BitSet(new long[]{0x0000000000000050L});
    public static final BitSet FOLLOW_media_feature_in_media_expression517 = new BitSet(new long[]{0x0000100000000010L,0x0000000000000002L});
    public static final BitSet FOLLOW_WS_in_media_expression519 = new BitSet(new long[]{0x0000100000000010L,0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_media_expression524 = new BitSet(new long[]{0x00124000000801D0L,0x0000000000001FF8L});
    public static final BitSet FOLLOW_WS_in_media_expression526 = new BitSet(new long[]{0x00124000000801D0L,0x0000000000001FF8L});
    public static final BitSet FOLLOW_expr_in_media_expression529 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RPAREN_in_media_expression534 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_media_expression536 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_IDENT_in_media_feature547 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_medium564 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_medium574 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_bodyset_in_bodylist597 = new BitSet(new long[]{0x00FC10000E381042L,0x0008000000000000L});
    public static final BitSet FOLLOW_ruleSet_in_bodyset626 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_media_in_bodyset638 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_page_in_bodyset650 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_counterStyle_in_bodyset662 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_fontFace_in_bodyset674 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_moz_document_in_bodyset686 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_generic_at_rule_in_bodyset698 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_bodyset714 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_GENERIC_AT_RULE_in_generic_at_rule747 = new BitSet(new long[]{0x00000000000020D0L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule749 = new BitSet(new long[]{0x00000000000020D0L});
    public static final BitSet FOLLOW_set_in_generic_at_rule754 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule764 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_LBRACE_in_generic_at_rule779 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_syncTo_RBRACE_in_generic_at_rule791 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_generic_at_rule801 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document817 = new BitSet(new long[]{0x0000000001C00110L});
    public static final BitSet FOLLOW_WS_in_moz_document819 = new BitSet(new long[]{0x0000000001C00110L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document824 = new BitSet(new long[]{0x000000000000A010L});
    public static final BitSet FOLLOW_WS_in_moz_document826 = new BitSet(new long[]{0x000000000000A010L});
    public static final BitSet FOLLOW_COMMA_in_moz_document832 = new BitSet(new long[]{0x0000000001C00110L});
    public static final BitSet FOLLOW_WS_in_moz_document834 = new BitSet(new long[]{0x0000000001C00110L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document837 = new BitSet(new long[]{0x000000000000A010L});
    public static final BitSet FOLLOW_WS_in_moz_document839 = new BitSet(new long[]{0x000000000000A010L});
    public static final BitSet FOLLOW_LBRACE_in_moz_document846 = new BitSet(new long[]{0x00FC100002084050L,0x0008000000000000L});
    public static final BitSet FOLLOW_WS_in_moz_document848 = new BitSet(new long[]{0x00FC100002084050L,0x0008000000000000L});
    public static final BitSet FOLLOW_ruleSet_in_moz_document857 = new BitSet(new long[]{0x00FC100002084050L,0x0008000000000000L});
    public static final BitSet FOLLOW_page_in_moz_document861 = new BitSet(new long[]{0x00FC100002084050L,0x0008000000000000L});
    public static final BitSet FOLLOW_WS_in_moz_document865 = new BitSet(new long[]{0x00FC100002084050L,0x0008000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_moz_document871 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_moz_document_function0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page913 = new BitSet(new long[]{0x0000100000002050L});
    public static final BitSet FOLLOW_WS_in_page915 = new BitSet(new long[]{0x0000100000002050L});
    public static final BitSet FOLLOW_IDENT_in_page920 = new BitSet(new long[]{0x0000100000002010L});
    public static final BitSet FOLLOW_WS_in_page922 = new BitSet(new long[]{0x0000100000002010L});
    public static final BitSet FOLLOW_pseudoPage_in_page929 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_WS_in_page931 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_LBRACE_in_page944 = new BitSet(new long[]{0x00000FFFF0084450L});
    public static final BitSet FOLLOW_WS_in_page946 = new BitSet(new long[]{0x00000FFFF0084450L});
    public static final BitSet FOLLOW_declaration_in_page1014 = new BitSet(new long[]{0x0000000000004400L});
    public static final BitSet FOLLOW_margin_in_page1016 = new BitSet(new long[]{0x0000000000004410L});
    public static final BitSet FOLLOW_WS_in_page1018 = new BitSet(new long[]{0x0000000000004410L});
    public static final BitSet FOLLOW_SEMI_in_page1024 = new BitSet(new long[]{0x00000FFFF0084450L});
    public static final BitSet FOLLOW_WS_in_page1026 = new BitSet(new long[]{0x00000FFFF0084450L});
    public static final BitSet FOLLOW_declaration_in_page1030 = new BitSet(new long[]{0x0000000000004400L});
    public static final BitSet FOLLOW_margin_in_page1032 = new BitSet(new long[]{0x0000000000004410L});
    public static final BitSet FOLLOW_WS_in_page1034 = new BitSet(new long[]{0x0000000000004410L});
    public static final BitSet FOLLOW_RBRACE_in_page1049 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1070 = new BitSet(new long[]{0x0000000000000050L});
    public static final BitSet FOLLOW_WS_in_counterStyle1072 = new BitSet(new long[]{0x0000000000000050L});
    public static final BitSet FOLLOW_IDENT_in_counterStyle1075 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_WS_in_counterStyle1077 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_LBRACE_in_counterStyle1088 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_WS_in_counterStyle1090 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_counterStyle1093 = new BitSet(new long[]{0x0000000000084440L});
    public static final BitSet FOLLOW_declarations_in_counterStyle1097 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_counterStyle1107 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FONT_FACE_SYM_in_fontFace1128 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_WS_in_fontFace1130 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_LBRACE_in_fontFace1141 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_WS_in_fontFace1143 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_fontFace1146 = new BitSet(new long[]{0x0000000000084440L});
    public static final BitSet FOLLOW_declarations_in_fontFace1150 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_fontFace1160 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_margin_sym_in_margin1180 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_WS_in_margin1182 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_LBRACE_in_margin1185 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_WS_in_margin1187 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_margin1190 = new BitSet(new long[]{0x0000000000084440L});
    public static final BitSet FOLLOW_declarations_in_margin1192 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_margin1194 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_margin_sym0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage1423 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage1425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_operator1446 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_operator1448 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_COMMA_in_operator1457 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_operator1459 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_PLUS_in_combinator1487 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator1489 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_GREATER_in_combinator1498 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator1500 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_TILDE_in_combinator1509 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_combinator1511 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_property1571 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_property1579 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_selectorsGroup_in_ruleSet1604 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_LBRACE_in_ruleSet1614 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_WS_in_ruleSet1616 = new BitSet(new long[]{0x0000000000084450L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_ruleSet1619 = new BitSet(new long[]{0x0000000000084440L});
    public static final BitSet FOLLOW_declarations_in_ruleSet1633 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_RBRACE_in_ruleSet1643 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_declarations1691 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_SEMI_in_declarations1695 = new BitSet(new long[]{0x0000000000080452L});
    public static final BitSet FOLLOW_WS_in_declarations1697 = new BitSet(new long[]{0x0000000000080452L});
    public static final BitSet FOLLOW_declaration_in_declarations1700 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup1724 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup1727 = new BitSet(new long[]{0x00FC100000080050L,0x0008000000000000L});
    public static final BitSet FOLLOW_WS_in_selectorsGroup1729 = new BitSet(new long[]{0x00FC100000080050L,0x0008000000000000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup1732 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector1755 = new BitSet(new long[]{0x00FDD00000080042L,0x0008000000000000L});
    public static final BitSet FOLLOW_combinator_in_selector1758 = new BitSet(new long[]{0x00FC100000080040L,0x0008000000000000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector1760 = new BitSet(new long[]{0x00FDD00000080042L,0x0008000000000000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence1800 = new BitSet(new long[]{0x00FC100000080042L,0x0008000000000000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence1807 = new BitSet(new long[]{0x00FC100000080042L,0x0008000000000000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence1825 = new BitSet(new long[]{0x00FC100000080042L,0x0008000000000000L});
    public static final BitSet FOLLOW_namespace_wqname_prefix_in_typeSelector1876 = new BitSet(new long[]{0x000C000000080040L});
    public static final BitSet FOLLOW_elementName_in_typeSelector1882 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_typeSelector1884 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_set_in_nsPred1909 = new BitSet(new long[]{0x0008000000000000L});
    public static final BitSet FOLLOW_PIPE_in_nsPred1918 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespace_prefix_in_namespace_wqname_prefix1948 = new BitSet(new long[]{0x0008000000000010L});
    public static final BitSet FOLLOW_WS_in_namespace_wqname_prefix1950 = new BitSet(new long[]{0x0008000000000010L});
    public static final BitSet FOLLOW_PIPE_in_namespace_wqname_prefix1956 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespace_wildcard_prefix_in_namespace_wqname_prefix1963 = new BitSet(new long[]{0x0008000000000010L});
    public static final BitSet FOLLOW_WS_in_namespace_wqname_prefix1965 = new BitSet(new long[]{0x0008000000000010L});
    public static final BitSet FOLLOW_PIPE_in_namespace_wqname_prefix1968 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_namespace_wildcard_prefix1990 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent2066 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent2075 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_attrib_in_elementSubsequent2087 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent2099 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_elementSubsequent2111 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_HASH_in_cssId2134 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_115_in_cssId2140 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_NAME_in_cssId2142 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass2170 = new BitSet(new long[]{0x0000000000080040L});
    public static final BitSet FOLLOW_set_in_cssClass2172 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_attrib2245 = new BitSet(new long[]{0x000C000000000050L});
    public static final BitSet FOLLOW_namespace_wqname_prefix_in_attrib2252 = new BitSet(new long[]{0x000C000000000050L});
    public static final BitSet FOLLOW_WS_in_attrib2255 = new BitSet(new long[]{0x000C000000000050L});
    public static final BitSet FOLLOW_attrib_name_in_attrib2266 = new BitSet(new long[]{0xFE00000000000010L});
    public static final BitSet FOLLOW_WS_in_attrib2268 = new BitSet(new long[]{0xFE00000000000010L});
    public static final BitSet FOLLOW_set_in_attrib2310 = new BitSet(new long[]{0x00000000000000D0L});
    public static final BitSet FOLLOW_WS_in_attrib2490 = new BitSet(new long[]{0x00000000000000D0L});
    public static final BitSet FOLLOW_attrib_value_in_attrib2509 = new BitSet(new long[]{0x8000000000000010L});
    public static final BitSet FOLLOW_WS_in_attrib2527 = new BitSet(new long[]{0x8000000000000010L});
    public static final BitSet FOLLOW_RBRACKET_in_attrib2556 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_attrib_name2599 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_attrib_value2613 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pseudo2673 = new BitSet(new long[]{0x00000000000A0040L});
    public static final BitSet FOLLOW_set_in_pseudo2737 = new BitSet(new long[]{0x0000000000000012L,0x0000000000000001L});
    public static final BitSet FOLLOW_WS_in_pseudo2794 = new BitSet(new long[]{0x0000000000000010L,0x0000000000000001L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo2797 = new BitSet(new long[]{0x00164000000801D0L,0x0000000000001FFAL});
    public static final BitSet FOLLOW_WS_in_pseudo2799 = new BitSet(new long[]{0x00164000000801D0L,0x0000000000001FFAL});
    public static final BitSet FOLLOW_expr_in_pseudo2804 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_pseudo2808 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo2813 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_pseudo2892 = new BitSet(new long[]{0x0000000000000010L,0x0000000000000001L});
    public static final BitSet FOLLOW_WS_in_pseudo2894 = new BitSet(new long[]{0x0000000000000010L,0x0000000000000001L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo2897 = new BitSet(new long[]{0x00FC100000080050L,0x0008000000000002L});
    public static final BitSet FOLLOW_WS_in_pseudo2899 = new BitSet(new long[]{0x00FC100000080050L,0x0008000000000002L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_pseudo2902 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo2905 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_declaration2949 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_COLON_in_declaration2951 = new BitSet(new long[]{0x00124000000801D0L,0x0000000000001FF8L});
    public static final BitSet FOLLOW_WS_in_declaration2953 = new BitSet(new long[]{0x00124000000801D0L,0x0000000000001FF8L});
    public static final BitSet FOLLOW_expr_in_declaration2956 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_prio_in_declaration2958 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio3088 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_prio3090 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_term_in_expr3112 = new BitSet(new long[]{0x00126000000881D2L,0x0000000000001FF8L});
    public static final BitSet FOLLOW_operator_in_expr3115 = new BitSet(new long[]{0x00124000000801D0L,0x0000000000001FF8L});
    public static final BitSet FOLLOW_term_in_expr3117 = new BitSet(new long[]{0x00126000000881D2L,0x0000000000001FF8L});
    public static final BitSet FOLLOW_unaryOperator_in_term3140 = new BitSet(new long[]{0x00124000000801D0L,0x0000000000001FF8L});
    public static final BitSet FOLLOW_set_in_term3161 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_STRING_in_term3344 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_IDENT_in_term3352 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_GEN_in_term3360 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_URI_in_term3368 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_hexColor_in_term3376 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_function_in_term3384 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_WS_in_term3396 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_function_name_in_function3412 = new BitSet(new long[]{0x0000000000000010L,0x0000000000000001L});
    public static final BitSet FOLLOW_WS_in_function3414 = new BitSet(new long[]{0x0000000000000010L,0x0000000000000001L});
    public static final BitSet FOLLOW_LPAREN_in_function3419 = new BitSet(new long[]{0x00124000000801D0L,0x0000000000001FF8L});
    public static final BitSet FOLLOW_WS_in_function3421 = new BitSet(new long[]{0x00124000000801D0L,0x0000000000001FF8L});
    public static final BitSet FOLLOW_expr_in_function3432 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_attribute_in_function3450 = new BitSet(new long[]{0x0000000000008000L,0x0000000000000002L});
    public static final BitSet FOLLOW_COMMA_in_function3453 = new BitSet(new long[]{0x00124000000801D0L,0x0000000000001FF8L});
    public static final BitSet FOLLOW_WS_in_function3455 = new BitSet(new long[]{0x00124000000801D0L,0x0000000000001FF8L});
    public static final BitSet FOLLOW_attribute_in_function3458 = new BitSet(new long[]{0x0000000000008000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RPAREN_in_function3479 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_function_name3527 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_COLON_in_function_name3529 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_function_name3533 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_DOT_in_function_name3536 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_function_name3538 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_attrname_in_attribute3560 = new BitSet(new long[]{0x0200000000000010L});
    public static final BitSet FOLLOW_WS_in_attribute3562 = new BitSet(new long[]{0x0200000000000010L});
    public static final BitSet FOLLOW_OPEQ_in_attribute3565 = new BitSet(new long[]{0x00124000000801D0L,0x0000000000001FF8L});
    public static final BitSet FOLLOW_WS_in_attribute3567 = new BitSet(new long[]{0x00124000000801D0L,0x0000000000001FF8L});
    public static final BitSet FOLLOW_attrvalue_in_attribute3570 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_attrname3585 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_DOT_in_attrname3588 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_IDENT_in_attrname3590 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_expr_in_attrvalue3604 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor3622 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred1_Css31804 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred2_Css31822 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nsPred_in_synpred3_Css31873 = new BitSet(new long[]{0x0000000000000002L});

}