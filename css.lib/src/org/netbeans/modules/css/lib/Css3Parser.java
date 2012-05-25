// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2012-05-25 09:58:54

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "NAMESPACE_SYM", "IDENT", "STRING", "URI", "CHARSET_SYM", "SEMI", "IMPORT_SYM", "MEDIA_SYM", "LBRACE", "RBRACE", "COMMA", "AND", "ONLY", "NOT", "GEN", "GENERIC_AT_RULE", "WS", "MOZ_DOCUMENT_SYM", "MOZ_URL_PREFIX", "MOZ_DOMAIN", "MOZ_REGEXP", "PAGE_SYM", "COUNTER_STYLE_SYM", "FONT_FACE_SYM", "TOPLEFTCORNER_SYM", "TOPLEFT_SYM", "TOPCENTER_SYM", "TOPRIGHT_SYM", "TOPRIGHTCORNER_SYM", "BOTTOMLEFTCORNER_SYM", "BOTTOMLEFT_SYM", "BOTTOMCENTER_SYM", "BOTTOMRIGHT_SYM", "BOTTOMRIGHTCORNER_SYM", "LEFTTOP_SYM", "LEFTMIDDLE_SYM", "LEFTBOTTOM_SYM", "RIGHTTOP_SYM", "RIGHTMIDDLE_SYM", "RIGHTBOTTOM_SYM", "COLON", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "HASH", "DOT", "LBRACKET", "DCOLON", "STAR", "PIPE", "NAME", "OPEQ", "INCLUDES", "DASHMATCH", "BEGINS", "ENDS", "CONTAINS", "RBRACKET", "LPAREN", "RPAREN", "IMPORTANT_SYM", "NUMBER", "PERCENTAGE", "LENGTH", "EMS", "REM", "EXS", "ANGLE", "TIME", "FREQ", "RESOLUTION", "DIMENSION", "NL", "COMMENT", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "CDO", "CDC", "INVALID", "'#'"
    };
    public static final int EOF=-1;
    public static final int T__116=116;
    public static final int NAMESPACE_SYM=4;
    public static final int IDENT=5;
    public static final int STRING=6;
    public static final int URI=7;
    public static final int CHARSET_SYM=8;
    public static final int SEMI=9;
    public static final int IMPORT_SYM=10;
    public static final int MEDIA_SYM=11;
    public static final int LBRACE=12;
    public static final int RBRACE=13;
    public static final int COMMA=14;
    public static final int AND=15;
    public static final int ONLY=16;
    public static final int NOT=17;
    public static final int GEN=18;
    public static final int GENERIC_AT_RULE=19;
    public static final int WS=20;
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
    public static final int HASH=50;
    public static final int DOT=51;
    public static final int LBRACKET=52;
    public static final int DCOLON=53;
    public static final int STAR=54;
    public static final int PIPE=55;
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
    public static final int REM=71;
    public static final int EXS=72;
    public static final int ANGLE=73;
    public static final int TIME=74;
    public static final int FREQ=75;
    public static final int RESOLUTION=76;
    public static final int DIMENSION=77;
    public static final int NL=78;
    public static final int COMMENT=79;
    public static final int HEXCHAR=80;
    public static final int NONASCII=81;
    public static final int UNICODE=82;
    public static final int ESCAPE=83;
    public static final int NMSTART=84;
    public static final int NMCHAR=85;
    public static final int URL=86;
    public static final int A=87;
    public static final int B=88;
    public static final int C=89;
    public static final int D=90;
    public static final int E=91;
    public static final int F=92;
    public static final int G=93;
    public static final int H=94;
    public static final int I=95;
    public static final int J=96;
    public static final int K=97;
    public static final int L=98;
    public static final int M=99;
    public static final int N=100;
    public static final int O=101;
    public static final int P=102;
    public static final int Q=103;
    public static final int R=104;
    public static final int S=105;
    public static final int T=106;
    public static final int U=107;
    public static final int V=108;
    public static final int W=109;
    public static final int X=110;
    public static final int Y=111;
    public static final int Z=112;
    public static final int CDO=113;
    public static final int CDC=114;
    public static final int INVALID=115;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "term", "mediaType", "moz_document", "resourceIdentifier", 
        "pseudo", "property", "esPred", "mediaFeature", "typeSelector", 
        "slAttribute", "selector", "page", "operator", "importItem", "syncToFollow", 
        "simpleSelectorSequence", "propertyValue", "hexColor", "declaration", 
        "fnAttribute", "synpred3_Css3", "rule", "prio", "charSetValue", 
        "margin", "elementSubsequent", "function", "generic_at_rule", "body", 
        "mediaQueryList", "media", "fnAttributeValue", "namespacePrefixName", 
        "charSet", "styleSheet", "nsPred", "fontFace", "namespace", "namespaces", 
        "slAttributeValue", "syncTo_RBRACE", "moz_document_function", "bodyItem", 
        "mediaExpression", "combinator", "cssId", "syncTo_IDENT_RBRACE", 
        "cssClass", "elementName", "namespacePrefix", "margin_sym", "declarations", 
        "pseudoPage", "fnAttributeName", "selectorsGroup", "imports", "synpred2_Css3", 
        "ws", "synpred1_Css3", "unaryOperator", "expression", "functionName", 
        "slAttributeName", "mediaQueryOperator", "counterStyle", "mediaQuery"
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
            false, false, false, false, false, false, false, true, true, 
            false, true, false, false, false, false, false, false, false, 
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
        
        /**
             * synces to next RBRACE "}" taking nesting into account
             */
            protected void syncToRBRACE(int nest)
                {
                    
                    int mark = -1;
                    //create error-recovery node
                    //dbg.enterRule(getGrammarFileName(), "recovery");

                    try {
                        mark = input.mark();
                        for(;;) {
                            //read char
                            int c = input.LA(1);
                            
                            switch(c) {
                                case Token.EOF:
                                    input.rewind();
                                    mark = -1;
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
                        if  (mark != -1) {
                            input.release(mark);
                        }
                        //dbg.exitRule(getGrammarFileName(), "recovery");
                    }
                }
        



    // $ANTLR start "styleSheet"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:304:1: styleSheet : ( ws )? ( charSet ( ws )? )? ( imports )? ( namespaces )? ( body )? EOF ;
    public final void styleSheet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "styleSheet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(304, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:305:5: ( ( ws )? ( charSet ( ws )? )? ( imports )? ( namespaces )? ( body )? EOF )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:6: ( ws )? ( charSet ( ws )? )? ( imports )? ( namespaces )? ( body )? EOF
            {
            dbg.location(306,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:6: ( ws )?
            int alt1=2;
            try { dbg.enterSubRule(1);
            try { dbg.enterDecision(1, decisionCanBacktrack[1]);

            int LA1_0 = input.LA(1);

            if ( (LA1_0==WS||(LA1_0>=NL && LA1_0<=COMMENT)) ) {
                alt1=1;
            }
            } finally {dbg.exitDecision(1);}

            switch (alt1) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:306:6: ws
                    {
                    dbg.location(306,6);
                    pushFollow(FOLLOW_ws_in_styleSheet125);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(1);}

            dbg.location(307,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:307:6: ( charSet ( ws )? )?
            int alt3=2;
            try { dbg.enterSubRule(3);
            try { dbg.enterDecision(3, decisionCanBacktrack[3]);

            int LA3_0 = input.LA(1);

            if ( (LA3_0==CHARSET_SYM) ) {
                alt3=1;
            }
            } finally {dbg.exitDecision(3);}

            switch (alt3) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:307:8: charSet ( ws )?
                    {
                    dbg.location(307,8);
                    pushFollow(FOLLOW_charSet_in_styleSheet135);
                    charSet();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(307,16);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:307:16: ( ws )?
                    int alt2=2;
                    try { dbg.enterSubRule(2);
                    try { dbg.enterDecision(2, decisionCanBacktrack[2]);

                    int LA2_0 = input.LA(1);

                    if ( (LA2_0==WS||(LA2_0>=NL && LA2_0<=COMMENT)) ) {
                        alt2=1;
                    }
                    } finally {dbg.exitDecision(2);}

                    switch (alt2) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:307:16: ws
                            {
                            dbg.location(307,16);
                            pushFollow(FOLLOW_ws_in_styleSheet137);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(2);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(3);}

            dbg.location(308,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:9: ( imports )?
            int alt4=2;
            try { dbg.enterSubRule(4);
            try { dbg.enterDecision(4, decisionCanBacktrack[4]);

            int LA4_0 = input.LA(1);

            if ( (LA4_0==IMPORT_SYM) ) {
                alt4=1;
            }
            } finally {dbg.exitDecision(4);}

            switch (alt4) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:308:9: imports
                    {
                    dbg.location(308,9);
                    pushFollow(FOLLOW_imports_in_styleSheet151);
                    imports();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(4);}

            dbg.location(309,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:309:9: ( namespaces )?
            int alt5=2;
            try { dbg.enterSubRule(5);
            try { dbg.enterDecision(5, decisionCanBacktrack[5]);

            int LA5_0 = input.LA(1);

            if ( (LA5_0==NAMESPACE_SYM) ) {
                alt5=1;
            }
            } finally {dbg.exitDecision(5);}

            switch (alt5) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:309:9: namespaces
                    {
                    dbg.location(309,9);
                    pushFollow(FOLLOW_namespaces_in_styleSheet162);
                    namespaces();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(5);}

            dbg.location(310,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:9: ( body )?
            int alt6=2;
            try { dbg.enterSubRule(6);
            try { dbg.enterDecision(6, decisionCanBacktrack[6]);

            int LA6_0 = input.LA(1);

            if ( (LA6_0==IDENT||LA6_0==MEDIA_SYM||(LA6_0>=GEN && LA6_0<=GENERIC_AT_RULE)||LA6_0==MOZ_DOCUMENT_SYM||(LA6_0>=PAGE_SYM && LA6_0<=FONT_FACE_SYM)||LA6_0==COLON||(LA6_0>=HASH && LA6_0<=PIPE)||LA6_0==116) ) {
                alt6=1;
            }
            } finally {dbg.exitDecision(6);}

            switch (alt6) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:310:9: body
                    {
                    dbg.location(310,9);
                    pushFollow(FOLLOW_body_in_styleSheet174);
                    body();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(6);}

            dbg.location(311,6);
            match(input,EOF,FOLLOW_EOF_in_styleSheet182); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(312, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "styleSheet");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "styleSheet"


    // $ANTLR start "namespaces"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:314:1: namespaces : ( namespace ( ws )? )+ ;
    public final void namespaces() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespaces");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(314, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:315:2: ( ( namespace ( ws )? )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:316:2: ( namespace ( ws )? )+
            {
            dbg.location(316,2);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:316:2: ( namespace ( ws )? )+
            int cnt8=0;
            try { dbg.enterSubRule(8);

            loop8:
            do {
                int alt8=2;
                try { dbg.enterDecision(8, decisionCanBacktrack[8]);

                int LA8_0 = input.LA(1);

                if ( (LA8_0==NAMESPACE_SYM) ) {
                    alt8=1;
                }


                } finally {dbg.exitDecision(8);}

                switch (alt8) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:316:4: namespace ( ws )?
            	    {
            	    dbg.location(316,4);
            	    pushFollow(FOLLOW_namespace_in_namespaces199);
            	    namespace();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(316,14);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:316:14: ( ws )?
            	    int alt7=2;
            	    try { dbg.enterSubRule(7);
            	    try { dbg.enterDecision(7, decisionCanBacktrack[7]);

            	    int LA7_0 = input.LA(1);

            	    if ( (LA7_0==WS||(LA7_0>=NL && LA7_0<=COMMENT)) ) {
            	        alt7=1;
            	    }
            	    } finally {dbg.exitDecision(7);}

            	    switch (alt7) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:316:14: ws
            	            {
            	            dbg.location(316,14);
            	            pushFollow(FOLLOW_ws_in_namespaces201);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(7);}


            	    }
            	    break;

            	default :
            	    if ( cnt8 >= 1 ) break loop8;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(8, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt8++;
            } while (true);
            } finally {dbg.exitSubRule(8);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(317, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "namespaces");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "namespaces"


    // $ANTLR start "namespace"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:319:1: namespace : NAMESPACE_SYM ( ws )? ( namespacePrefixName ( ws )? )? resourceIdentifier ( ws )? ';' ;
    public final void namespace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(319, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:3: ( NAMESPACE_SYM ( ws )? ( namespacePrefixName ( ws )? )? resourceIdentifier ( ws )? ';' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:5: NAMESPACE_SYM ( ws )? ( namespacePrefixName ( ws )? )? resourceIdentifier ( ws )? ';'
            {
            dbg.location(320,5);
            match(input,NAMESPACE_SYM,FOLLOW_NAMESPACE_SYM_in_namespace217); if (state.failed) return ;
            dbg.location(320,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:19: ( ws )?
            int alt9=2;
            try { dbg.enterSubRule(9);
            try { dbg.enterDecision(9, decisionCanBacktrack[9]);

            int LA9_0 = input.LA(1);

            if ( (LA9_0==WS||(LA9_0>=NL && LA9_0<=COMMENT)) ) {
                alt9=1;
            }
            } finally {dbg.exitDecision(9);}

            switch (alt9) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:19: ws
                    {
                    dbg.location(320,19);
                    pushFollow(FOLLOW_ws_in_namespace219);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(9);}

            dbg.location(320,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:23: ( namespacePrefixName ( ws )? )?
            int alt11=2;
            try { dbg.enterSubRule(11);
            try { dbg.enterDecision(11, decisionCanBacktrack[11]);

            int LA11_0 = input.LA(1);

            if ( (LA11_0==IDENT) ) {
                alt11=1;
            }
            } finally {dbg.exitDecision(11);}

            switch (alt11) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:24: namespacePrefixName ( ws )?
                    {
                    dbg.location(320,24);
                    pushFollow(FOLLOW_namespacePrefixName_in_namespace223);
                    namespacePrefixName();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(320,44);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:44: ( ws )?
                    int alt10=2;
                    try { dbg.enterSubRule(10);
                    try { dbg.enterDecision(10, decisionCanBacktrack[10]);

                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==WS||(LA10_0>=NL && LA10_0<=COMMENT)) ) {
                        alt10=1;
                    }
                    } finally {dbg.exitDecision(10);}

                    switch (alt10) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:44: ws
                            {
                            dbg.location(320,44);
                            pushFollow(FOLLOW_ws_in_namespace225);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(10);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(11);}

            dbg.location(320,50);
            pushFollow(FOLLOW_resourceIdentifier_in_namespace230);
            resourceIdentifier();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(320,69);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:69: ( ws )?
            int alt12=2;
            try { dbg.enterSubRule(12);
            try { dbg.enterDecision(12, decisionCanBacktrack[12]);

            int LA12_0 = input.LA(1);

            if ( (LA12_0==WS||(LA12_0>=NL && LA12_0<=COMMENT)) ) {
                alt12=1;
            }
            } finally {dbg.exitDecision(12);}

            switch (alt12) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:320:69: ws
                    {
                    dbg.location(320,69);
                    pushFollow(FOLLOW_ws_in_namespace232);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(12);}

            dbg.location(320,73);
            match(input,SEMI,FOLLOW_SEMI_in_namespace235); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "namespace");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "namespace"


    // $ANTLR start "namespacePrefixName"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:323:1: namespacePrefixName : IDENT ;
    public final void namespacePrefixName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefixName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(323, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:324:3: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:324:5: IDENT
            {
            dbg.location(324,5);
            match(input,IDENT,FOLLOW_IDENT_in_namespacePrefixName248); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(325, 3);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "namespacePrefixName");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "namespacePrefixName"


    // $ANTLR start "resourceIdentifier"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:327:1: resourceIdentifier : ( STRING | URI );
    public final void resourceIdentifier() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "resourceIdentifier");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(327, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:328:3: ( STRING | URI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(328,3);
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
        dbg.location(329, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:331:1: charSet : CHARSET_SYM ( ws )? charSetValue ( ws )? SEMI ;
    public final void charSet() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "charSet");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(331, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:332:5: ( CHARSET_SYM ( ws )? charSetValue ( ws )? SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:332:9: CHARSET_SYM ( ws )? charSetValue ( ws )? SEMI
            {
            dbg.location(332,9);
            match(input,CHARSET_SYM,FOLLOW_CHARSET_SYM_in_charSet286); if (state.failed) return ;
            dbg.location(332,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:332:21: ( ws )?
            int alt13=2;
            try { dbg.enterSubRule(13);
            try { dbg.enterDecision(13, decisionCanBacktrack[13]);

            int LA13_0 = input.LA(1);

            if ( (LA13_0==WS||(LA13_0>=NL && LA13_0<=COMMENT)) ) {
                alt13=1;
            }
            } finally {dbg.exitDecision(13);}

            switch (alt13) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:332:21: ws
                    {
                    dbg.location(332,21);
                    pushFollow(FOLLOW_ws_in_charSet288);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(13);}

            dbg.location(332,25);
            pushFollow(FOLLOW_charSetValue_in_charSet291);
            charSetValue();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(332,38);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:332:38: ( ws )?
            int alt14=2;
            try { dbg.enterSubRule(14);
            try { dbg.enterDecision(14, decisionCanBacktrack[14]);

            int LA14_0 = input.LA(1);

            if ( (LA14_0==WS||(LA14_0>=NL && LA14_0<=COMMENT)) ) {
                alt14=1;
            }
            } finally {dbg.exitDecision(14);}

            switch (alt14) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:332:38: ws
                    {
                    dbg.location(332,38);
                    pushFollow(FOLLOW_ws_in_charSet293);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(14);}

            dbg.location(332,42);
            match(input,SEMI,FOLLOW_SEMI_in_charSet296); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(333, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "charSet");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "charSet"


    // $ANTLR start "charSetValue"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:335:1: charSetValue : STRING ;
    public final void charSetValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "charSetValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(335, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:336:2: ( STRING )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:336:4: STRING
            {
            dbg.location(336,4);
            match(input,STRING,FOLLOW_STRING_in_charSetValue310); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(337, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "charSetValue");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "charSetValue"


    // $ANTLR start "imports"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:339:1: imports : ( importItem ( ws )? )+ ;
    public final void imports() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "imports");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(339, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:340:2: ( ( importItem ( ws )? )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:2: ( importItem ( ws )? )+
            {
            dbg.location(341,2);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:2: ( importItem ( ws )? )+
            int cnt16=0;
            try { dbg.enterSubRule(16);

            loop16:
            do {
                int alt16=2;
                try { dbg.enterDecision(16, decisionCanBacktrack[16]);

                int LA16_0 = input.LA(1);

                if ( (LA16_0==IMPORT_SYM) ) {
                    alt16=1;
                }


                } finally {dbg.exitDecision(16);}

                switch (alt16) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:4: importItem ( ws )?
            	    {
            	    dbg.location(341,4);
            	    pushFollow(FOLLOW_importItem_in_imports324);
            	    importItem();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(341,15);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:15: ( ws )?
            	    int alt15=2;
            	    try { dbg.enterSubRule(15);
            	    try { dbg.enterDecision(15, decisionCanBacktrack[15]);

            	    int LA15_0 = input.LA(1);

            	    if ( (LA15_0==WS||(LA15_0>=NL && LA15_0<=COMMENT)) ) {
            	        alt15=1;
            	    }
            	    } finally {dbg.exitDecision(15);}

            	    switch (alt15) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:341:15: ws
            	            {
            	            dbg.location(341,15);
            	            pushFollow(FOLLOW_ws_in_imports326);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(15);}


            	    }
            	    break;

            	default :
            	    if ( cnt16 >= 1 ) break loop16;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(16, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt16++;
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
        dbg.location(342, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "imports");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "imports"


    // $ANTLR start "importItem"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:344:1: importItem : IMPORT_SYM ( ws )? resourceIdentifier ( ws )? mediaQueryList SEMI ;
    public final void importItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "importItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(344, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:345:5: ( IMPORT_SYM ( ws )? resourceIdentifier ( ws )? mediaQueryList SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:345:9: IMPORT_SYM ( ws )? resourceIdentifier ( ws )? mediaQueryList SEMI
            {
            dbg.location(345,9);
            match(input,IMPORT_SYM,FOLLOW_IMPORT_SYM_in_importItem347); if (state.failed) return ;
            dbg.location(345,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:345:20: ( ws )?
            int alt17=2;
            try { dbg.enterSubRule(17);
            try { dbg.enterDecision(17, decisionCanBacktrack[17]);

            int LA17_0 = input.LA(1);

            if ( (LA17_0==WS||(LA17_0>=NL && LA17_0<=COMMENT)) ) {
                alt17=1;
            }
            } finally {dbg.exitDecision(17);}

            switch (alt17) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:345:20: ws
                    {
                    dbg.location(345,20);
                    pushFollow(FOLLOW_ws_in_importItem349);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(17);}

            dbg.location(345,24);
            pushFollow(FOLLOW_resourceIdentifier_in_importItem352);
            resourceIdentifier();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(345,43);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:345:43: ( ws )?
            int alt18=2;
            try { dbg.enterSubRule(18);
            try { dbg.enterDecision(18, decisionCanBacktrack[18]);

            int LA18_0 = input.LA(1);

            if ( (LA18_0==WS||(LA18_0>=NL && LA18_0<=COMMENT)) ) {
                alt18=1;
            }
            } finally {dbg.exitDecision(18);}

            switch (alt18) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:345:43: ws
                    {
                    dbg.location(345,43);
                    pushFollow(FOLLOW_ws_in_importItem354);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(18);}

            dbg.location(345,47);
            pushFollow(FOLLOW_mediaQueryList_in_importItem357);
            mediaQueryList();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(345,62);
            match(input,SEMI,FOLLOW_SEMI_in_importItem359); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(346, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "importItem");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "importItem"


    // $ANTLR start "media"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:1: media : MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page | fontFace ) ( ws )? )* RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(347, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:5: ( MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page | fontFace ) ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:7: MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page | fontFace ) ( ws )? )* RBRACE
            {
            dbg.location(348,7);
            match(input,MEDIA_SYM,FOLLOW_MEDIA_SYM_in_media375); if (state.failed) return ;
            dbg.location(348,17);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:17: ( ws )?
            int alt19=2;
            try { dbg.enterSubRule(19);
            try { dbg.enterDecision(19, decisionCanBacktrack[19]);

            int LA19_0 = input.LA(1);

            if ( (LA19_0==WS||(LA19_0>=NL && LA19_0<=COMMENT)) ) {
                alt19=1;
            }
            } finally {dbg.exitDecision(19);}

            switch (alt19) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:17: ws
                    {
                    dbg.location(348,17);
                    pushFollow(FOLLOW_ws_in_media377);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(19);}

            dbg.location(348,21);
            pushFollow(FOLLOW_mediaQueryList_in_media380);
            mediaQueryList();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(349,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_media390); if (state.failed) return ;
            dbg.location(349,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:16: ( ws )?
            int alt20=2;
            try { dbg.enterSubRule(20);
            try { dbg.enterDecision(20, decisionCanBacktrack[20]);

            int LA20_0 = input.LA(1);

            if ( (LA20_0==WS||(LA20_0>=NL && LA20_0<=COMMENT)) ) {
                alt20=1;
            }
            } finally {dbg.exitDecision(20);}

            switch (alt20) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:349:16: ws
                    {
                    dbg.location(349,16);
                    pushFollow(FOLLOW_ws_in_media392);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(20);}

            dbg.location(350,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:13: ( ( rule | page | fontFace ) ( ws )? )*
            try { dbg.enterSubRule(23);

            loop23:
            do {
                int alt23=2;
                try { dbg.enterDecision(23, decisionCanBacktrack[23]);

                int LA23_0 = input.LA(1);

                if ( (LA23_0==IDENT||LA23_0==GEN||LA23_0==PAGE_SYM||LA23_0==FONT_FACE_SYM||LA23_0==COLON||(LA23_0>=HASH && LA23_0<=PIPE)||LA23_0==116) ) {
                    alt23=1;
                }


                } finally {dbg.exitDecision(23);}

                switch (alt23) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:15: ( rule | page | fontFace ) ( ws )?
            	    {
            	    dbg.location(350,15);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:15: ( rule | page | fontFace )
            	    int alt21=3;
            	    try { dbg.enterSubRule(21);
            	    try { dbg.enterDecision(21, decisionCanBacktrack[21]);

            	    switch ( input.LA(1) ) {
            	    case IDENT:
            	    case GEN:
            	    case COLON:
            	    case HASH:
            	    case DOT:
            	    case LBRACKET:
            	    case DCOLON:
            	    case STAR:
            	    case PIPE:
            	    case 116:
            	        {
            	        alt21=1;
            	        }
            	        break;
            	    case PAGE_SYM:
            	        {
            	        alt21=2;
            	        }
            	        break;
            	    case FONT_FACE_SYM:
            	        {
            	        alt21=3;
            	        }
            	        break;
            	    default:
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 21, 0, input);

            	        dbg.recognitionException(nvae);
            	        throw nvae;
            	    }

            	    } finally {dbg.exitDecision(21);}

            	    switch (alt21) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:17: rule
            	            {
            	            dbg.location(350,17);
            	            pushFollow(FOLLOW_rule_in_media411);
            	            rule();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:24: page
            	            {
            	            dbg.location(350,24);
            	            pushFollow(FOLLOW_page_in_media415);
            	            page();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 3 :
            	            dbg.enterAlt(3);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:31: fontFace
            	            {
            	            dbg.location(350,31);
            	            pushFollow(FOLLOW_fontFace_in_media419);
            	            fontFace();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(21);}

            	    dbg.location(350,42);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:42: ( ws )?
            	    int alt22=2;
            	    try { dbg.enterSubRule(22);
            	    try { dbg.enterDecision(22, decisionCanBacktrack[22]);

            	    int LA22_0 = input.LA(1);

            	    if ( (LA22_0==WS||(LA22_0>=NL && LA22_0<=COMMENT)) ) {
            	        alt22=1;
            	    }
            	    } finally {dbg.exitDecision(22);}

            	    switch (alt22) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:42: ws
            	            {
            	            dbg.location(350,42);
            	            pushFollow(FOLLOW_ws_in_media423);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(22);}


            	    }
            	    break;

            	default :
            	    break loop23;
                }
            } while (true);
            } finally {dbg.exitSubRule(23);}

            dbg.location(351,10);
            match(input,RBRACE,FOLLOW_RBRACE_in_media437); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(352, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "media");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "media"


    // $ANTLR start "mediaQueryList"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:354:1: mediaQueryList : ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? ;
    public final void mediaQueryList() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryList");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(354, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:2: ( ( mediaQuery ( COMMA ( ws )? mediaQuery )* )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
            {
            dbg.location(355,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:4: ( mediaQuery ( COMMA ( ws )? mediaQuery )* )?
            int alt26=2;
            try { dbg.enterSubRule(26);
            try { dbg.enterDecision(26, decisionCanBacktrack[26]);

            int LA26_0 = input.LA(1);

            if ( (LA26_0==IDENT||(LA26_0>=ONLY && LA26_0<=GEN)||LA26_0==LPAREN) ) {
                alt26=1;
            }
            } finally {dbg.exitDecision(26);}

            switch (alt26) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:6: mediaQuery ( COMMA ( ws )? mediaQuery )*
                    {
                    dbg.location(355,6);
                    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList453);
                    mediaQuery();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(355,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:17: ( COMMA ( ws )? mediaQuery )*
                    try { dbg.enterSubRule(25);

                    loop25:
                    do {
                        int alt25=2;
                        try { dbg.enterDecision(25, decisionCanBacktrack[25]);

                        int LA25_0 = input.LA(1);

                        if ( (LA25_0==COMMA) ) {
                            alt25=1;
                        }


                        } finally {dbg.exitDecision(25);}

                        switch (alt25) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:19: COMMA ( ws )? mediaQuery
                    	    {
                    	    dbg.location(355,19);
                    	    match(input,COMMA,FOLLOW_COMMA_in_mediaQueryList457); if (state.failed) return ;
                    	    dbg.location(355,25);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:25: ( ws )?
                    	    int alt24=2;
                    	    try { dbg.enterSubRule(24);
                    	    try { dbg.enterDecision(24, decisionCanBacktrack[24]);

                    	    int LA24_0 = input.LA(1);

                    	    if ( (LA24_0==WS||(LA24_0>=NL && LA24_0<=COMMENT)) ) {
                    	        alt24=1;
                    	    }
                    	    } finally {dbg.exitDecision(24);}

                    	    switch (alt24) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:355:25: ws
                    	            {
                    	            dbg.location(355,25);
                    	            pushFollow(FOLLOW_ws_in_mediaQueryList459);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(24);}

                    	    dbg.location(355,29);
                    	    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList462);
                    	    mediaQuery();

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
        dbg.location(356, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "mediaQueryList");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "mediaQueryList"


    // $ANTLR start "mediaQuery"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:358:1: mediaQuery : ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* );
    public final void mediaQuery() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQuery");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(358, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:2: ( ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )* | mediaExpression ( AND ( ws )? mediaExpression )* )
            int alt34=2;
            try { dbg.enterDecision(34, decisionCanBacktrack[34]);

            int LA34_0 = input.LA(1);

            if ( (LA34_0==IDENT||(LA34_0>=ONLY && LA34_0<=GEN)) ) {
                alt34=1;
            }
            else if ( (LA34_0==LPAREN) ) {
                alt34=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(34);}

            switch (alt34) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:4: ( mediaQueryOperator ( ws )? )? mediaType ( ws )? ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(359,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:4: ( mediaQueryOperator ( ws )? )?
                    int alt28=2;
                    try { dbg.enterSubRule(28);
                    try { dbg.enterDecision(28, decisionCanBacktrack[28]);

                    int LA28_0 = input.LA(1);

                    if ( ((LA28_0>=ONLY && LA28_0<=NOT)) ) {
                        alt28=1;
                    }
                    } finally {dbg.exitDecision(28);}

                    switch (alt28) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:5: mediaQueryOperator ( ws )?
                            {
                            dbg.location(359,5);
                            pushFollow(FOLLOW_mediaQueryOperator_in_mediaQuery481);
                            mediaQueryOperator();

                            state._fsp--;
                            if (state.failed) return ;
                            dbg.location(359,24);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:24: ( ws )?
                            int alt27=2;
                            try { dbg.enterSubRule(27);
                            try { dbg.enterDecision(27, decisionCanBacktrack[27]);

                            int LA27_0 = input.LA(1);

                            if ( (LA27_0==WS||(LA27_0>=NL && LA27_0<=COMMENT)) ) {
                                alt27=1;
                            }
                            } finally {dbg.exitDecision(27);}

                            switch (alt27) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:24: ws
                                    {
                                    dbg.location(359,24);
                                    pushFollow(FOLLOW_ws_in_mediaQuery483);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(27);}


                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(28);}

                    dbg.location(359,32);
                    pushFollow(FOLLOW_mediaType_in_mediaQuery490);
                    mediaType();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(359,42);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:42: ( ws )?
                    int alt29=2;
                    try { dbg.enterSubRule(29);
                    try { dbg.enterDecision(29, decisionCanBacktrack[29]);

                    int LA29_0 = input.LA(1);

                    if ( (LA29_0==WS||(LA29_0>=NL && LA29_0<=COMMENT)) ) {
                        alt29=1;
                    }
                    } finally {dbg.exitDecision(29);}

                    switch (alt29) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:42: ws
                            {
                            dbg.location(359,42);
                            pushFollow(FOLLOW_ws_in_mediaQuery492);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(29);}

                    dbg.location(359,46);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:46: ( AND ( ws )? mediaExpression )*
                    try { dbg.enterSubRule(31);

                    loop31:
                    do {
                        int alt31=2;
                        try { dbg.enterDecision(31, decisionCanBacktrack[31]);

                        int LA31_0 = input.LA(1);

                        if ( (LA31_0==AND) ) {
                            alt31=1;
                        }


                        } finally {dbg.exitDecision(31);}

                        switch (alt31) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:48: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(359,48);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery497); if (state.failed) return ;
                    	    dbg.location(359,52);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:52: ( ws )?
                    	    int alt30=2;
                    	    try { dbg.enterSubRule(30);
                    	    try { dbg.enterDecision(30, decisionCanBacktrack[30]);

                    	    int LA30_0 = input.LA(1);

                    	    if ( (LA30_0==WS||(LA30_0>=NL && LA30_0<=COMMENT)) ) {
                    	        alt30=1;
                    	    }
                    	    } finally {dbg.exitDecision(30);}

                    	    switch (alt30) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:359:52: ws
                    	            {
                    	            dbg.location(359,52);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery499);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(30);}

                    	    dbg.location(359,56);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery502);
                    	    mediaExpression();

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
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:4: mediaExpression ( AND ( ws )? mediaExpression )*
                    {
                    dbg.location(360,4);
                    pushFollow(FOLLOW_mediaExpression_in_mediaQuery510);
                    mediaExpression();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(360,20);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:20: ( AND ( ws )? mediaExpression )*
                    try { dbg.enterSubRule(33);

                    loop33:
                    do {
                        int alt33=2;
                        try { dbg.enterDecision(33, decisionCanBacktrack[33]);

                        int LA33_0 = input.LA(1);

                        if ( (LA33_0==AND) ) {
                            alt33=1;
                        }


                        } finally {dbg.exitDecision(33);}

                        switch (alt33) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:22: AND ( ws )? mediaExpression
                    	    {
                    	    dbg.location(360,22);
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery514); if (state.failed) return ;
                    	    dbg.location(360,26);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:26: ( ws )?
                    	    int alt32=2;
                    	    try { dbg.enterSubRule(32);
                    	    try { dbg.enterDecision(32, decisionCanBacktrack[32]);

                    	    int LA32_0 = input.LA(1);

                    	    if ( (LA32_0==WS||(LA32_0>=NL && LA32_0<=COMMENT)) ) {
                    	        alt32=1;
                    	    }
                    	    } finally {dbg.exitDecision(32);}

                    	    switch (alt32) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:360:26: ws
                    	            {
                    	            dbg.location(360,26);
                    	            pushFollow(FOLLOW_ws_in_mediaQuery516);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(32);}

                    	    dbg.location(360,30);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery519);
                    	    mediaExpression();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop33;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(33);}


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
        dbg.location(361, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "mediaQuery");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "mediaQuery"


    // $ANTLR start "mediaQueryOperator"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:363:1: mediaQueryOperator : ( ONLY | NOT );
    public final void mediaQueryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaQueryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(363, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:364:3: ( ONLY | NOT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(364,3);
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


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(365, 3);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "mediaQueryOperator");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "mediaQueryOperator"


    // $ANTLR start "mediaType"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:367:1: mediaType : ( IDENT | GEN );
    public final void mediaType() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaType");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(367, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:368:2: ( IDENT | GEN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(368,2);
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
        dbg.location(369, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "mediaType");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "mediaType"


    // $ANTLR start "mediaExpression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:371:1: mediaExpression : '(' ( ws )? mediaFeature ( ws )? ( ':' ( ws )? expression )? ')' ( ws )? ;
    public final void mediaExpression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaExpression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(371, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:2: ( '(' ( ws )? mediaFeature ( ws )? ( ':' ( ws )? expression )? ')' ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:4: '(' ( ws )? mediaFeature ( ws )? ( ':' ( ws )? expression )? ')' ( ws )?
            {
            dbg.location(372,4);
            match(input,LPAREN,FOLLOW_LPAREN_in_mediaExpression571); if (state.failed) return ;
            dbg.location(372,8);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:8: ( ws )?
            int alt35=2;
            try { dbg.enterSubRule(35);
            try { dbg.enterDecision(35, decisionCanBacktrack[35]);

            int LA35_0 = input.LA(1);

            if ( (LA35_0==WS||(LA35_0>=NL && LA35_0<=COMMENT)) ) {
                alt35=1;
            }
            } finally {dbg.exitDecision(35);}

            switch (alt35) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:8: ws
                    {
                    dbg.location(372,8);
                    pushFollow(FOLLOW_ws_in_mediaExpression573);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(35);}

            dbg.location(372,12);
            pushFollow(FOLLOW_mediaFeature_in_mediaExpression576);
            mediaFeature();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(372,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:25: ( ws )?
            int alt36=2;
            try { dbg.enterSubRule(36);
            try { dbg.enterDecision(36, decisionCanBacktrack[36]);

            int LA36_0 = input.LA(1);

            if ( (LA36_0==WS||(LA36_0>=NL && LA36_0<=COMMENT)) ) {
                alt36=1;
            }
            } finally {dbg.exitDecision(36);}

            switch (alt36) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:25: ws
                    {
                    dbg.location(372,25);
                    pushFollow(FOLLOW_ws_in_mediaExpression578);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(36);}

            dbg.location(372,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:29: ( ':' ( ws )? expression )?
            int alt38=2;
            try { dbg.enterSubRule(38);
            try { dbg.enterDecision(38, decisionCanBacktrack[38]);

            int LA38_0 = input.LA(1);

            if ( (LA38_0==COLON) ) {
                alt38=1;
            }
            } finally {dbg.exitDecision(38);}

            switch (alt38) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:31: ':' ( ws )? expression
                    {
                    dbg.location(372,31);
                    match(input,COLON,FOLLOW_COLON_in_mediaExpression583); if (state.failed) return ;
                    dbg.location(372,35);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:35: ( ws )?
                    int alt37=2;
                    try { dbg.enterSubRule(37);
                    try { dbg.enterDecision(37, decisionCanBacktrack[37]);

                    int LA37_0 = input.LA(1);

                    if ( (LA37_0==WS||(LA37_0>=NL && LA37_0<=COMMENT)) ) {
                        alt37=1;
                    }
                    } finally {dbg.exitDecision(37);}

                    switch (alt37) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:35: ws
                            {
                            dbg.location(372,35);
                            pushFollow(FOLLOW_ws_in_mediaExpression585);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(37);}

                    dbg.location(372,39);
                    pushFollow(FOLLOW_expression_in_mediaExpression588);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(38);}

            dbg.location(372,53);
            match(input,RPAREN,FOLLOW_RPAREN_in_mediaExpression593); if (state.failed) return ;
            dbg.location(372,57);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:57: ( ws )?
            int alt39=2;
            try { dbg.enterSubRule(39);
            try { dbg.enterDecision(39, decisionCanBacktrack[39]);

            int LA39_0 = input.LA(1);

            if ( (LA39_0==WS||(LA39_0>=NL && LA39_0<=COMMENT)) ) {
                alt39=1;
            }
            } finally {dbg.exitDecision(39);}

            switch (alt39) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:57: ws
                    {
                    dbg.location(372,57);
                    pushFollow(FOLLOW_ws_in_mediaExpression595);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(39);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(373, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "mediaExpression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "mediaExpression"


    // $ANTLR start "mediaFeature"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:375:1: mediaFeature : IDENT ;
    public final void mediaFeature() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "mediaFeature");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(375, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:376:4: IDENT
            {
            dbg.location(376,4);
            match(input,IDENT,FOLLOW_IDENT_in_mediaFeature608); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(377, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "mediaFeature");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "mediaFeature"


    // $ANTLR start "body"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:379:2: body : ( bodyItem ( ws )? )+ ;
    public final void body() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "body");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(379, 2);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:379:7: ( ( bodyItem ( ws )? )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:2: ( bodyItem ( ws )? )+
            {
            dbg.location(380,2);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:2: ( bodyItem ( ws )? )+
            int cnt41=0;
            try { dbg.enterSubRule(41);

            loop41:
            do {
                int alt41=2;
                try { dbg.enterDecision(41, decisionCanBacktrack[41]);

                int LA41_0 = input.LA(1);

                if ( (LA41_0==IDENT||LA41_0==MEDIA_SYM||(LA41_0>=GEN && LA41_0<=GENERIC_AT_RULE)||LA41_0==MOZ_DOCUMENT_SYM||(LA41_0>=PAGE_SYM && LA41_0<=FONT_FACE_SYM)||LA41_0==COLON||(LA41_0>=HASH && LA41_0<=PIPE)||LA41_0==116) ) {
                    alt41=1;
                }


                } finally {dbg.exitDecision(41);}

                switch (alt41) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:4: bodyItem ( ws )?
            	    {
            	    dbg.location(380,4);
            	    pushFollow(FOLLOW_bodyItem_in_body624);
            	    bodyItem();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(380,13);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:13: ( ws )?
            	    int alt40=2;
            	    try { dbg.enterSubRule(40);
            	    try { dbg.enterDecision(40, decisionCanBacktrack[40]);

            	    int LA40_0 = input.LA(1);

            	    if ( (LA40_0==WS||(LA40_0>=NL && LA40_0<=COMMENT)) ) {
            	        alt40=1;
            	    }
            	    } finally {dbg.exitDecision(40);}

            	    switch (alt40) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:13: ws
            	            {
            	            dbg.location(380,13);
            	            pushFollow(FOLLOW_ws_in_body626);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(40);}


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
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(381, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "body");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "body"


    // $ANTLR start "bodyItem"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:1: bodyItem : ( rule | media | page | counterStyle | fontFace | moz_document | generic_at_rule );
    public final void bodyItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(383, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:384:5: ( rule | media | page | counterStyle | fontFace | moz_document | generic_at_rule )
            int alt42=7;
            try { dbg.enterDecision(42, decisionCanBacktrack[42]);

            switch ( input.LA(1) ) {
            case IDENT:
            case GEN:
            case COLON:
            case HASH:
            case DOT:
            case LBRACKET:
            case DCOLON:
            case STAR:
            case PIPE:
            case 116:
                {
                alt42=1;
                }
                break;
            case MEDIA_SYM:
                {
                alt42=2;
                }
                break;
            case PAGE_SYM:
                {
                alt42=3;
                }
                break;
            case COUNTER_STYLE_SYM:
                {
                alt42=4;
                }
                break;
            case FONT_FACE_SYM:
                {
                alt42=5;
                }
                break;
            case MOZ_DOCUMENT_SYM:
                {
                alt42=6;
                }
                break;
            case GENERIC_AT_RULE:
                {
                alt42=7;
                }
                break;
            default:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:385:6: rule
                    {
                    dbg.location(385,6);
                    pushFollow(FOLLOW_rule_in_bodyItem651);
                    rule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:386:11: media
                    {
                    dbg.location(386,11);
                    pushFollow(FOLLOW_media_in_bodyItem663);
                    media();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:387:11: page
                    {
                    dbg.location(387,11);
                    pushFollow(FOLLOW_page_in_bodyItem675);
                    page();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:388:11: counterStyle
                    {
                    dbg.location(388,11);
                    pushFollow(FOLLOW_counterStyle_in_bodyItem687);
                    counterStyle();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:389:11: fontFace
                    {
                    dbg.location(389,11);
                    pushFollow(FOLLOW_fontFace_in_bodyItem699);
                    fontFace();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:11: moz_document
                    {
                    dbg.location(390,11);
                    pushFollow(FOLLOW_moz_document_in_bodyItem711);
                    moz_document();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:391:11: generic_at_rule
                    {
                    dbg.location(391,11);
                    pushFollow(FOLLOW_generic_at_rule_in_bodyItem723);
                    generic_at_rule();

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
        dbg.location(392, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "bodyItem");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "bodyItem"


    // $ANTLR start "generic_at_rule"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:1: generic_at_rule : GENERIC_AT_RULE ( WS )* ( ( IDENT | STRING ) ( WS )* )? LBRACE syncTo_RBRACE RBRACE ;
    public final void generic_at_rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "generic_at_rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(401, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:5: ( GENERIC_AT_RULE ( WS )* ( ( IDENT | STRING ) ( WS )* )? LBRACE syncTo_RBRACE RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:7: GENERIC_AT_RULE ( WS )* ( ( IDENT | STRING ) ( WS )* )? LBRACE syncTo_RBRACE RBRACE
            {
            dbg.location(402,7);
            match(input,GENERIC_AT_RULE,FOLLOW_GENERIC_AT_RULE_in_generic_at_rule755); if (state.failed) return ;
            dbg.location(402,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:23: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:23: WS
            	    {
            	    dbg.location(402,23);
            	    match(input,WS,FOLLOW_WS_in_generic_at_rule757); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop43;
                }
            } while (true);
            } finally {dbg.exitSubRule(43);}

            dbg.location(402,27);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:27: ( ( IDENT | STRING ) ( WS )* )?
            int alt45=2;
            try { dbg.enterSubRule(45);
            try { dbg.enterDecision(45, decisionCanBacktrack[45]);

            int LA45_0 = input.LA(1);

            if ( ((LA45_0>=IDENT && LA45_0<=STRING)) ) {
                alt45=1;
            }
            } finally {dbg.exitDecision(45);}

            switch (alt45) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:29: ( IDENT | STRING ) ( WS )*
                    {
                    dbg.location(402,29);
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

                    dbg.location(402,48);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:48: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:402:48: WS
                    	    {
                    	    dbg.location(402,48);
                    	    match(input,WS,FOLLOW_WS_in_generic_at_rule772); if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop44;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(44);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(45);}

            dbg.location(403,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_generic_at_rule787); if (state.failed) return ;
            dbg.location(404,10);
            pushFollow(FOLLOW_syncTo_RBRACE_in_generic_at_rule799);
            syncTo_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(405,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_generic_at_rule809); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(406, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:407:1: moz_document : MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( ( rule | page ) ( ws )? )* RBRACE ;
    public final void moz_document() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(407, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:2: ( MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( ( rule | page ) ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:2: MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( ( rule | page ) ( ws )? )* RBRACE
            {
            dbg.location(409,2);
            match(input,MOZ_DOCUMENT_SYM,FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document825); if (state.failed) return ;
            dbg.location(409,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:19: ( ws )?
            int alt46=2;
            try { dbg.enterSubRule(46);
            try { dbg.enterDecision(46, decisionCanBacktrack[46]);

            int LA46_0 = input.LA(1);

            if ( (LA46_0==WS||(LA46_0>=NL && LA46_0<=COMMENT)) ) {
                alt46=1;
            }
            } finally {dbg.exitDecision(46);}

            switch (alt46) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:19: ws
                    {
                    dbg.location(409,19);
                    pushFollow(FOLLOW_ws_in_moz_document827);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(46);}

            dbg.location(409,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:23: ( moz_document_function ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:25: moz_document_function ( ws )?
            {
            dbg.location(409,25);
            pushFollow(FOLLOW_moz_document_function_in_moz_document832);
            moz_document_function();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(409,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:47: ( ws )?
            int alt47=2;
            try { dbg.enterSubRule(47);
            try { dbg.enterDecision(47, decisionCanBacktrack[47]);

            int LA47_0 = input.LA(1);

            if ( (LA47_0==WS||(LA47_0>=NL && LA47_0<=COMMENT)) ) {
                alt47=1;
            }
            } finally {dbg.exitDecision(47);}

            switch (alt47) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:47: ws
                    {
                    dbg.location(409,47);
                    pushFollow(FOLLOW_ws_in_moz_document834);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(47);}


            }

            dbg.location(409,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:52: ( COMMA ( ws )? moz_document_function ( ws )? )*
            try { dbg.enterSubRule(50);

            loop50:
            do {
                int alt50=2;
                try { dbg.enterDecision(50, decisionCanBacktrack[50]);

                int LA50_0 = input.LA(1);

                if ( (LA50_0==COMMA) ) {
                    alt50=1;
                }


                } finally {dbg.exitDecision(50);}

                switch (alt50) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:54: COMMA ( ws )? moz_document_function ( ws )?
            	    {
            	    dbg.location(409,54);
            	    match(input,COMMA,FOLLOW_COMMA_in_moz_document840); if (state.failed) return ;
            	    dbg.location(409,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:60: ( ws )?
            	    int alt48=2;
            	    try { dbg.enterSubRule(48);
            	    try { dbg.enterDecision(48, decisionCanBacktrack[48]);

            	    int LA48_0 = input.LA(1);

            	    if ( (LA48_0==WS||(LA48_0>=NL && LA48_0<=COMMENT)) ) {
            	        alt48=1;
            	    }
            	    } finally {dbg.exitDecision(48);}

            	    switch (alt48) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:60: ws
            	            {
            	            dbg.location(409,60);
            	            pushFollow(FOLLOW_ws_in_moz_document842);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(48);}

            	    dbg.location(409,64);
            	    pushFollow(FOLLOW_moz_document_function_in_moz_document845);
            	    moz_document_function();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(409,86);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:86: ( ws )?
            	    int alt49=2;
            	    try { dbg.enterSubRule(49);
            	    try { dbg.enterDecision(49, decisionCanBacktrack[49]);

            	    int LA49_0 = input.LA(1);

            	    if ( (LA49_0==WS||(LA49_0>=NL && LA49_0<=COMMENT)) ) {
            	        alt49=1;
            	    }
            	    } finally {dbg.exitDecision(49);}

            	    switch (alt49) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:86: ws
            	            {
            	            dbg.location(409,86);
            	            pushFollow(FOLLOW_ws_in_moz_document847);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

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

            dbg.location(410,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_moz_document854); if (state.failed) return ;
            dbg.location(410,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:9: ( ws )?
            int alt51=2;
            try { dbg.enterSubRule(51);
            try { dbg.enterDecision(51, decisionCanBacktrack[51]);

            int LA51_0 = input.LA(1);

            if ( (LA51_0==WS||(LA51_0>=NL && LA51_0<=COMMENT)) ) {
                alt51=1;
            }
            } finally {dbg.exitDecision(51);}

            switch (alt51) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:410:9: ws
                    {
                    dbg.location(410,9);
                    pushFollow(FOLLOW_ws_in_moz_document856);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(51);}

            dbg.location(411,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:3: ( ( rule | page ) ( ws )? )*
            try { dbg.enterSubRule(54);

            loop54:
            do {
                int alt54=2;
                try { dbg.enterDecision(54, decisionCanBacktrack[54]);

                int LA54_0 = input.LA(1);

                if ( (LA54_0==IDENT||LA54_0==GEN||LA54_0==PAGE_SYM||LA54_0==COLON||(LA54_0>=HASH && LA54_0<=PIPE)||LA54_0==116) ) {
                    alt54=1;
                }


                } finally {dbg.exitDecision(54);}

                switch (alt54) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:5: ( rule | page ) ( ws )?
            	    {
            	    dbg.location(411,5);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:5: ( rule | page )
            	    int alt52=2;
            	    try { dbg.enterSubRule(52);
            	    try { dbg.enterDecision(52, decisionCanBacktrack[52]);

            	    int LA52_0 = input.LA(1);

            	    if ( (LA52_0==IDENT||LA52_0==GEN||LA52_0==COLON||(LA52_0>=HASH && LA52_0<=PIPE)||LA52_0==116) ) {
            	        alt52=1;
            	    }
            	    else if ( (LA52_0==PAGE_SYM) ) {
            	        alt52=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 52, 0, input);

            	        dbg.recognitionException(nvae);
            	        throw nvae;
            	    }
            	    } finally {dbg.exitDecision(52);}

            	    switch (alt52) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:7: rule
            	            {
            	            dbg.location(411,7);
            	            pushFollow(FOLLOW_rule_in_moz_document865);
            	            rule();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:14: page
            	            {
            	            dbg.location(411,14);
            	            pushFollow(FOLLOW_page_in_moz_document869);
            	            page();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(52);}

            	    dbg.location(411,21);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:21: ( ws )?
            	    int alt53=2;
            	    try { dbg.enterSubRule(53);
            	    try { dbg.enterDecision(53, decisionCanBacktrack[53]);

            	    int LA53_0 = input.LA(1);

            	    if ( (LA53_0==WS||(LA53_0>=NL && LA53_0<=COMMENT)) ) {
            	        alt53=1;
            	    }
            	    } finally {dbg.exitDecision(53);}

            	    switch (alt53) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:411:21: ws
            	            {
            	            dbg.location(411,21);
            	            pushFollow(FOLLOW_ws_in_moz_document873);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(53);}


            	    }
            	    break;

            	default :
            	    break loop54;
                }
            } while (true);
            } finally {dbg.exitSubRule(54);}

            dbg.location(412,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_moz_document879); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(413, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:1: moz_document_function : ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP );
    public final void moz_document_function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document_function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(415, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:2: ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(416,2);
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
        dbg.location(418, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:420:1: page : PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(420, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:5: ( PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:7: PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE
            {
            dbg.location(421,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page921); if (state.failed) return ;
            dbg.location(421,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:16: ( ws )?
            int alt55=2;
            try { dbg.enterSubRule(55);
            try { dbg.enterDecision(55, decisionCanBacktrack[55]);

            int LA55_0 = input.LA(1);

            if ( (LA55_0==WS||(LA55_0>=NL && LA55_0<=COMMENT)) ) {
                alt55=1;
            }
            } finally {dbg.exitDecision(55);}

            switch (alt55) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:16: ws
                    {
                    dbg.location(421,16);
                    pushFollow(FOLLOW_ws_in_page923);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(55);}

            dbg.location(421,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:20: ( IDENT ( ws )? )?
            int alt57=2;
            try { dbg.enterSubRule(57);
            try { dbg.enterDecision(57, decisionCanBacktrack[57]);

            int LA57_0 = input.LA(1);

            if ( (LA57_0==IDENT) ) {
                alt57=1;
            }
            } finally {dbg.exitDecision(57);}

            switch (alt57) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:22: IDENT ( ws )?
                    {
                    dbg.location(421,22);
                    match(input,IDENT,FOLLOW_IDENT_in_page928); if (state.failed) return ;
                    dbg.location(421,28);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:28: ( ws )?
                    int alt56=2;
                    try { dbg.enterSubRule(56);
                    try { dbg.enterDecision(56, decisionCanBacktrack[56]);

                    int LA56_0 = input.LA(1);

                    if ( (LA56_0==WS||(LA56_0>=NL && LA56_0<=COMMENT)) ) {
                        alt56=1;
                    }
                    } finally {dbg.exitDecision(56);}

                    switch (alt56) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:28: ws
                            {
                            dbg.location(421,28);
                            pushFollow(FOLLOW_ws_in_page930);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(56);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(57);}

            dbg.location(421,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:35: ( pseudoPage ( ws )? )?
            int alt59=2;
            try { dbg.enterSubRule(59);
            try { dbg.enterDecision(59, decisionCanBacktrack[59]);

            int LA59_0 = input.LA(1);

            if ( (LA59_0==COLON) ) {
                alt59=1;
            }
            } finally {dbg.exitDecision(59);}

            switch (alt59) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:36: pseudoPage ( ws )?
                    {
                    dbg.location(421,36);
                    pushFollow(FOLLOW_pseudoPage_in_page937);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(421,47);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:47: ( ws )?
                    int alt58=2;
                    try { dbg.enterSubRule(58);
                    try { dbg.enterDecision(58, decisionCanBacktrack[58]);

                    int LA58_0 = input.LA(1);

                    if ( (LA58_0==WS||(LA58_0>=NL && LA58_0<=COMMENT)) ) {
                        alt58=1;
                    }
                    } finally {dbg.exitDecision(58);}

                    switch (alt58) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:421:47: ws
                            {
                            dbg.location(421,47);
                            pushFollow(FOLLOW_ws_in_page939);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(58);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(59);}

            dbg.location(422,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page952); if (state.failed) return ;
            dbg.location(422,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:16: ( ws )?
            int alt60=2;
            try { dbg.enterSubRule(60);
            try { dbg.enterDecision(60, decisionCanBacktrack[60]);

            int LA60_0 = input.LA(1);

            if ( (LA60_0==WS||(LA60_0>=NL && LA60_0<=COMMENT)) ) {
                alt60=1;
            }
            } finally {dbg.exitDecision(60);}

            switch (alt60) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:16: ws
                    {
                    dbg.location(422,16);
                    pushFollow(FOLLOW_ws_in_page954);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(60);}

            dbg.location(426,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:13: ( declaration | margin ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:14: declaration
                    {
                    dbg.location(426,14);
                    pushFollow(FOLLOW_declaration_in_page1009);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:26: margin ( ws )?
                    {
                    dbg.location(426,26);
                    pushFollow(FOLLOW_margin_in_page1011);
                    margin();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(426,33);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:33: ( ws )?
                    int alt61=2;
                    try { dbg.enterSubRule(61);
                    try { dbg.enterDecision(61, decisionCanBacktrack[61]);

                    int LA61_0 = input.LA(1);

                    if ( (LA61_0==WS||(LA61_0>=NL && LA61_0<=COMMENT)) ) {
                        alt61=1;
                    }
                    } finally {dbg.exitDecision(61);}

                    switch (alt61) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:33: ws
                            {
                            dbg.location(426,33);
                            pushFollow(FOLLOW_ws_in_page1013);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(61);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(62);}

            dbg.location(426,39);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:39: ( SEMI ( ws )? ( declaration | margin ( ws )? )? )*
            try { dbg.enterSubRule(66);

            loop66:
            do {
                int alt66=2;
                try { dbg.enterDecision(66, decisionCanBacktrack[66]);

                int LA66_0 = input.LA(1);

                if ( (LA66_0==SEMI) ) {
                    alt66=1;
                }


                } finally {dbg.exitDecision(66);}

                switch (alt66) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:40: SEMI ( ws )? ( declaration | margin ( ws )? )?
            	    {
            	    dbg.location(426,40);
            	    match(input,SEMI,FOLLOW_SEMI_in_page1019); if (state.failed) return ;
            	    dbg.location(426,45);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:45: ( ws )?
            	    int alt63=2;
            	    try { dbg.enterSubRule(63);
            	    try { dbg.enterDecision(63, decisionCanBacktrack[63]);

            	    int LA63_0 = input.LA(1);

            	    if ( (LA63_0==WS||(LA63_0>=NL && LA63_0<=COMMENT)) ) {
            	        alt63=1;
            	    }
            	    } finally {dbg.exitDecision(63);}

            	    switch (alt63) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:45: ws
            	            {
            	            dbg.location(426,45);
            	            pushFollow(FOLLOW_ws_in_page1021);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(63);}

            	    dbg.location(426,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:49: ( declaration | margin ( ws )? )?
            	    int alt65=3;
            	    try { dbg.enterSubRule(65);
            	    try { dbg.enterDecision(65, decisionCanBacktrack[65]);

            	    int LA65_0 = input.LA(1);

            	    if ( (LA65_0==IDENT||LA65_0==GEN) ) {
            	        alt65=1;
            	    }
            	    else if ( ((LA65_0>=TOPLEFTCORNER_SYM && LA65_0<=RIGHTBOTTOM_SYM)) ) {
            	        alt65=2;
            	    }
            	    } finally {dbg.exitDecision(65);}

            	    switch (alt65) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:50: declaration
            	            {
            	            dbg.location(426,50);
            	            pushFollow(FOLLOW_declaration_in_page1025);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:62: margin ( ws )?
            	            {
            	            dbg.location(426,62);
            	            pushFollow(FOLLOW_margin_in_page1027);
            	            margin();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(426,69);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:69: ( ws )?
            	            int alt64=2;
            	            try { dbg.enterSubRule(64);
            	            try { dbg.enterDecision(64, decisionCanBacktrack[64]);

            	            int LA64_0 = input.LA(1);

            	            if ( (LA64_0==WS||(LA64_0>=NL && LA64_0<=COMMENT)) ) {
            	                alt64=1;
            	            }
            	            } finally {dbg.exitDecision(64);}

            	            switch (alt64) {
            	                case 1 :
            	                    dbg.enterAlt(1);

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:426:69: ws
            	                    {
            	                    dbg.location(426,69);
            	                    pushFollow(FOLLOW_ws_in_page1029);
            	                    ws();

            	                    state._fsp--;
            	                    if (state.failed) return ;

            	                    }
            	                    break;

            	            }
            	            } finally {dbg.exitSubRule(64);}


            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(65);}


            	    }
            	    break;

            	default :
            	    break loop66;
                }
            } while (true);
            } finally {dbg.exitSubRule(66);}

            dbg.location(427,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page1044); if (state.failed) return ;

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:1: counterStyle : COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void counterStyle() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "counterStyle");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(430, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:5: ( COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:7: COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(431,7);
            match(input,COUNTER_STYLE_SYM,FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1065); if (state.failed) return ;
            dbg.location(431,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:25: ( ws )?
            int alt67=2;
            try { dbg.enterSubRule(67);
            try { dbg.enterDecision(67, decisionCanBacktrack[67]);

            int LA67_0 = input.LA(1);

            if ( (LA67_0==WS||(LA67_0>=NL && LA67_0<=COMMENT)) ) {
                alt67=1;
            }
            } finally {dbg.exitDecision(67);}

            switch (alt67) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:25: ws
                    {
                    dbg.location(431,25);
                    pushFollow(FOLLOW_ws_in_counterStyle1067);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(67);}

            dbg.location(431,29);
            match(input,IDENT,FOLLOW_IDENT_in_counterStyle1070); if (state.failed) return ;
            dbg.location(431,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:35: ( ws )?
            int alt68=2;
            try { dbg.enterSubRule(68);
            try { dbg.enterDecision(68, decisionCanBacktrack[68]);

            int LA68_0 = input.LA(1);

            if ( (LA68_0==WS||(LA68_0>=NL && LA68_0<=COMMENT)) ) {
                alt68=1;
            }
            } finally {dbg.exitDecision(68);}

            switch (alt68) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:35: ws
                    {
                    dbg.location(431,35);
                    pushFollow(FOLLOW_ws_in_counterStyle1072);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(68);}

            dbg.location(432,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_counterStyle1083); if (state.failed) return ;
            dbg.location(432,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:16: ( ws )?
            int alt69=2;
            try { dbg.enterSubRule(69);
            try { dbg.enterDecision(69, decisionCanBacktrack[69]);

            int LA69_0 = input.LA(1);

            if ( (LA69_0==WS||(LA69_0>=NL && LA69_0<=COMMENT)) ) {
                alt69=1;
            }
            } finally {dbg.exitDecision(69);}

            switch (alt69) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:16: ws
                    {
                    dbg.location(432,16);
                    pushFollow(FOLLOW_ws_in_counterStyle1085);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(69);}

            dbg.location(432,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_counterStyle1088);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(433,3);
            pushFollow(FOLLOW_declarations_in_counterStyle1092);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(434,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_counterStyle1102); if (state.failed) return ;

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:437:1: fontFace : FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void fontFace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fontFace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(437, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:5: ( FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:7: FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(438,7);
            match(input,FONT_FACE_SYM,FOLLOW_FONT_FACE_SYM_in_fontFace1123); if (state.failed) return ;
            dbg.location(438,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:21: ( ws )?
            int alt70=2;
            try { dbg.enterSubRule(70);
            try { dbg.enterDecision(70, decisionCanBacktrack[70]);

            int LA70_0 = input.LA(1);

            if ( (LA70_0==WS||(LA70_0>=NL && LA70_0<=COMMENT)) ) {
                alt70=1;
            }
            } finally {dbg.exitDecision(70);}

            switch (alt70) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:21: ws
                    {
                    dbg.location(438,21);
                    pushFollow(FOLLOW_ws_in_fontFace1125);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(70);}

            dbg.location(439,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_fontFace1136); if (state.failed) return ;
            dbg.location(439,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:16: ( ws )?
            int alt71=2;
            try { dbg.enterSubRule(71);
            try { dbg.enterDecision(71, decisionCanBacktrack[71]);

            int LA71_0 = input.LA(1);

            if ( (LA71_0==WS||(LA71_0>=NL && LA71_0<=COMMENT)) ) {
                alt71=1;
            }
            } finally {dbg.exitDecision(71);}

            switch (alt71) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:16: ws
                    {
                    dbg.location(439,16);
                    pushFollow(FOLLOW_ws_in_fontFace1138);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(71);}

            dbg.location(439,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_fontFace1141);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(440,3);
            pushFollow(FOLLOW_declarations_in_fontFace1145);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(441,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_fontFace1155); if (state.failed) return ;

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:1: margin : margin_sym ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void margin() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(444, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:2: ( margin_sym ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:4: margin_sym ( ws )? LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(445,4);
            pushFollow(FOLLOW_margin_sym_in_margin1170);
            margin_sym();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(445,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:15: ( ws )?
            int alt72=2;
            try { dbg.enterSubRule(72);
            try { dbg.enterDecision(72, decisionCanBacktrack[72]);

            int LA72_0 = input.LA(1);

            if ( (LA72_0==WS||(LA72_0>=NL && LA72_0<=COMMENT)) ) {
                alt72=1;
            }
            } finally {dbg.exitDecision(72);}

            switch (alt72) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:15: ws
                    {
                    dbg.location(445,15);
                    pushFollow(FOLLOW_ws_in_margin1172);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(72);}

            dbg.location(445,19);
            match(input,LBRACE,FOLLOW_LBRACE_in_margin1175); if (state.failed) return ;
            dbg.location(445,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:26: ( ws )?
            int alt73=2;
            try { dbg.enterSubRule(73);
            try { dbg.enterDecision(73, decisionCanBacktrack[73]);

            int LA73_0 = input.LA(1);

            if ( (LA73_0==WS||(LA73_0>=NL && LA73_0<=COMMENT)) ) {
                alt73=1;
            }
            } finally {dbg.exitDecision(73);}

            switch (alt73) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:26: ws
                    {
                    dbg.location(445,26);
                    pushFollow(FOLLOW_ws_in_margin1177);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(73);}

            dbg.location(445,30);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_margin1180);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(445,50);
            pushFollow(FOLLOW_declarations_in_margin1182);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(445,63);
            match(input,RBRACE,FOLLOW_RBRACE_in_margin1184); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(446, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:448:1: margin_sym : ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM );
    public final void margin_sym() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin_sym");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(448, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:449:2: ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(449,2);
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
        dbg.location(466, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(468, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:469:7: COLON IDENT
            {
            dbg.location(469,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage1413); if (state.failed) return ;
            dbg.location(469,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage1415); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(470, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:472:1: operator : ( SOLIDUS ( ws )? | COMMA ( ws )? | );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(472, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:5: ( SOLIDUS ( ws )? | COMMA ( ws )? | )
            int alt76=3;
            try { dbg.enterDecision(76, decisionCanBacktrack[76]);

            switch ( input.LA(1) ) {
            case SOLIDUS:
                {
                alt76=1;
                }
                break;
            case COMMA:
                {
                alt76=2;
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
            case REM:
            case EXS:
            case ANGLE:
            case TIME:
            case FREQ:
            case RESOLUTION:
            case DIMENSION:
                {
                alt76=3;
                }
                break;
            default:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:7: SOLIDUS ( ws )?
                    {
                    dbg.location(473,7);
                    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_operator1436); if (state.failed) return ;
                    dbg.location(473,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:15: ( ws )?
                    int alt74=2;
                    try { dbg.enterSubRule(74);
                    try { dbg.enterDecision(74, decisionCanBacktrack[74]);

                    int LA74_0 = input.LA(1);

                    if ( (LA74_0==WS||(LA74_0>=NL && LA74_0<=COMMENT)) ) {
                        alt74=1;
                    }
                    } finally {dbg.exitDecision(74);}

                    switch (alt74) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:15: ws
                            {
                            dbg.location(473,15);
                            pushFollow(FOLLOW_ws_in_operator1438);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(74);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:7: COMMA ( ws )?
                    {
                    dbg.location(474,7);
                    match(input,COMMA,FOLLOW_COMMA_in_operator1447); if (state.failed) return ;
                    dbg.location(474,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:13: ( ws )?
                    int alt75=2;
                    try { dbg.enterSubRule(75);
                    try { dbg.enterDecision(75, decisionCanBacktrack[75]);

                    int LA75_0 = input.LA(1);

                    if ( (LA75_0==WS||(LA75_0>=NL && LA75_0<=COMMENT)) ) {
                        alt75=1;
                    }
                    } finally {dbg.exitDecision(75);}

                    switch (alt75) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:13: ws
                            {
                            dbg.location(474,13);
                            pushFollow(FOLLOW_ws_in_operator1449);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(75);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:476:5: 
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
        dbg.location(476, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:478:1: combinator : ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(478, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:5: ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | )
            int alt80=4;
            try { dbg.enterDecision(80, decisionCanBacktrack[80]);

            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt80=1;
                }
                break;
            case GREATER:
                {
                alt80=2;
                }
                break;
            case TILDE:
                {
                alt80=3;
                }
                break;
            case IDENT:
            case GEN:
            case COLON:
            case HASH:
            case DOT:
            case LBRACKET:
            case DCOLON:
            case STAR:
            case PIPE:
            case 116:
                {
                alt80=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 80, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(80);}

            switch (alt80) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:7: PLUS ( ws )?
                    {
                    dbg.location(479,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator1477); if (state.failed) return ;
                    dbg.location(479,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:12: ( ws )?
                    int alt77=2;
                    try { dbg.enterSubRule(77);
                    try { dbg.enterDecision(77, decisionCanBacktrack[77]);

                    int LA77_0 = input.LA(1);

                    if ( (LA77_0==WS||(LA77_0>=NL && LA77_0<=COMMENT)) ) {
                        alt77=1;
                    }
                    } finally {dbg.exitDecision(77);}

                    switch (alt77) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:479:12: ws
                            {
                            dbg.location(479,12);
                            pushFollow(FOLLOW_ws_in_combinator1479);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(77);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:7: GREATER ( ws )?
                    {
                    dbg.location(480,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator1488); if (state.failed) return ;
                    dbg.location(480,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:15: ( ws )?
                    int alt78=2;
                    try { dbg.enterSubRule(78);
                    try { dbg.enterDecision(78, decisionCanBacktrack[78]);

                    int LA78_0 = input.LA(1);

                    if ( (LA78_0==WS||(LA78_0>=NL && LA78_0<=COMMENT)) ) {
                        alt78=1;
                    }
                    } finally {dbg.exitDecision(78);}

                    switch (alt78) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:480:15: ws
                            {
                            dbg.location(480,15);
                            pushFollow(FOLLOW_ws_in_combinator1490);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(78);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:7: TILDE ( ws )?
                    {
                    dbg.location(481,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator1499); if (state.failed) return ;
                    dbg.location(481,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:13: ( ws )?
                    int alt79=2;
                    try { dbg.enterSubRule(79);
                    try { dbg.enterDecision(79, decisionCanBacktrack[79]);

                    int LA79_0 = input.LA(1);

                    if ( (LA79_0==WS||(LA79_0>=NL && LA79_0<=COMMENT)) ) {
                        alt79=1;
                    }
                    } finally {dbg.exitDecision(79);}

                    switch (alt79) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:481:13: ws
                            {
                            dbg.location(481,13);
                            pushFollow(FOLLOW_ws_in_combinator1501);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(79);}


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:483:5: 
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
        dbg.location(483, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:485:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(485, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:486:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(486,5);
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
        dbg.location(488, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:490:1: property : ( IDENT | GEN ) ( ws )? ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(490, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:5: ( ( IDENT | GEN ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:7: ( IDENT | GEN ) ( ws )?
            {
            dbg.location(491,7);
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

            dbg.location(491,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:21: ( ws )?
            int alt81=2;
            try { dbg.enterSubRule(81);
            try { dbg.enterDecision(81, decisionCanBacktrack[81]);

            int LA81_0 = input.LA(1);

            if ( (LA81_0==WS||(LA81_0>=NL && LA81_0<=COMMENT)) ) {
                alt81=1;
            }
            } finally {dbg.exitDecision(81);}

            switch (alt81) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:491:21: ws
                    {
                    dbg.location(491,21);
                    pushFollow(FOLLOW_ws_in_property1569);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(81);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(492, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "property");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "property"


    // $ANTLR start "rule"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:494:1: rule : selectorsGroup LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE ;
    public final void rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(494, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:495:5: ( selectorsGroup LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:495:9: selectorsGroup LBRACE ( ws )? syncTo_IDENT_RBRACE declarations RBRACE
            {
            dbg.location(495,9);
            pushFollow(FOLLOW_selectorsGroup_in_rule1594);
            selectorsGroup();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(496,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_rule1604); if (state.failed) return ;
            dbg.location(496,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:496:16: ( ws )?
            int alt82=2;
            try { dbg.enterSubRule(82);
            try { dbg.enterDecision(82, decisionCanBacktrack[82]);

            int LA82_0 = input.LA(1);

            if ( (LA82_0==WS||(LA82_0>=NL && LA82_0<=COMMENT)) ) {
                alt82=1;
            }
            } finally {dbg.exitDecision(82);}

            switch (alt82) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:496:16: ws
                    {
                    dbg.location(496,16);
                    pushFollow(FOLLOW_ws_in_rule1606);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(82);}

            dbg.location(496,20);
            pushFollow(FOLLOW_syncTo_IDENT_RBRACE_in_rule1609);
            syncTo_IDENT_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(497,13);
            pushFollow(FOLLOW_declarations_in_rule1623);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(498,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_rule1633); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RBRACE));
                    input.consume(); //consume the RBRACE as well
                    
        }
        finally {
        }
        dbg.location(499, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "rule");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "rule"


    // $ANTLR start "declarations"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:506:1: declarations : ( declaration )? ( SEMI ( ws )? ( declaration )? )* ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(506, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:507:5: ( ( declaration )? ( SEMI ( ws )? ( declaration )? )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:9: ( declaration )? ( SEMI ( ws )? ( declaration )? )*
            {
            dbg.location(509,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:9: ( declaration )?
            int alt83=2;
            try { dbg.enterSubRule(83);
            try { dbg.enterDecision(83, decisionCanBacktrack[83]);

            int LA83_0 = input.LA(1);

            if ( (LA83_0==IDENT||LA83_0==GEN) ) {
                alt83=1;
            }
            } finally {dbg.exitDecision(83);}

            switch (alt83) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:9: declaration
                    {
                    dbg.location(509,9);
                    pushFollow(FOLLOW_declaration_in_declarations1681);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(83);}

            dbg.location(509,22);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:22: ( SEMI ( ws )? ( declaration )? )*
            try { dbg.enterSubRule(86);

            loop86:
            do {
                int alt86=2;
                try { dbg.enterDecision(86, decisionCanBacktrack[86]);

                int LA86_0 = input.LA(1);

                if ( (LA86_0==SEMI) ) {
                    alt86=1;
                }


                } finally {dbg.exitDecision(86);}

                switch (alt86) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:23: SEMI ( ws )? ( declaration )?
            	    {
            	    dbg.location(509,23);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations1685); if (state.failed) return ;
            	    dbg.location(509,28);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:28: ( ws )?
            	    int alt84=2;
            	    try { dbg.enterSubRule(84);
            	    try { dbg.enterDecision(84, decisionCanBacktrack[84]);

            	    int LA84_0 = input.LA(1);

            	    if ( (LA84_0==WS||(LA84_0>=NL && LA84_0<=COMMENT)) ) {
            	        alt84=1;
            	    }
            	    } finally {dbg.exitDecision(84);}

            	    switch (alt84) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:28: ws
            	            {
            	            dbg.location(509,28);
            	            pushFollow(FOLLOW_ws_in_declarations1687);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(84);}

            	    dbg.location(509,32);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:32: ( declaration )?
            	    int alt85=2;
            	    try { dbg.enterSubRule(85);
            	    try { dbg.enterDecision(85, decisionCanBacktrack[85]);

            	    int LA85_0 = input.LA(1);

            	    if ( (LA85_0==IDENT||LA85_0==GEN) ) {
            	        alt85=1;
            	    }
            	    } finally {dbg.exitDecision(85);}

            	    switch (alt85) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:32: declaration
            	            {
            	            dbg.location(509,32);
            	            pushFollow(FOLLOW_declaration_in_declarations1690);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(85);}


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
        dbg.location(510, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:512:1: selectorsGroup : selector ( COMMA ( ws )? selector )* ;
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(512, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:5: ( selector ( COMMA ( ws )? selector )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:7: selector ( COMMA ( ws )? selector )*
            {
            dbg.location(513,7);
            pushFollow(FOLLOW_selector_in_selectorsGroup1714);
            selector();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(513,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:16: ( COMMA ( ws )? selector )*
            try { dbg.enterSubRule(88);

            loop88:
            do {
                int alt88=2;
                try { dbg.enterDecision(88, decisionCanBacktrack[88]);

                int LA88_0 = input.LA(1);

                if ( (LA88_0==COMMA) ) {
                    alt88=1;
                }


                } finally {dbg.exitDecision(88);}

                switch (alt88) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:17: COMMA ( ws )? selector
            	    {
            	    dbg.location(513,17);
            	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup1717); if (state.failed) return ;
            	    dbg.location(513,23);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:23: ( ws )?
            	    int alt87=2;
            	    try { dbg.enterSubRule(87);
            	    try { dbg.enterDecision(87, decisionCanBacktrack[87]);

            	    int LA87_0 = input.LA(1);

            	    if ( (LA87_0==WS||(LA87_0>=NL && LA87_0<=COMMENT)) ) {
            	        alt87=1;
            	    }
            	    } finally {dbg.exitDecision(87);}

            	    switch (alt87) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:513:23: ws
            	            {
            	            dbg.location(513,23);
            	            pushFollow(FOLLOW_ws_in_selectorsGroup1719);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(87);}

            	    dbg.location(513,27);
            	    pushFollow(FOLLOW_selector_in_selectorsGroup1722);
            	    selector();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop88;
                }
            } while (true);
            } finally {dbg.exitSubRule(88);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(514, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:516:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(516, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:517:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:517:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(517,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector1745);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(517,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:517:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(89);

            loop89:
            do {
                int alt89=2;
                try { dbg.enterDecision(89, decisionCanBacktrack[89]);

                int LA89_0 = input.LA(1);

                if ( (LA89_0==IDENT||LA89_0==GEN||LA89_0==COLON||(LA89_0>=PLUS && LA89_0<=TILDE)||(LA89_0>=HASH && LA89_0<=PIPE)||LA89_0==116) ) {
                    alt89=1;
                }


                } finally {dbg.exitDecision(89);}

                switch (alt89) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:517:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(517,31);
            	    pushFollow(FOLLOW_combinator_in_selector1748);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(517,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector1750);
            	    simpleSelectorSequence();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop89;
                }
            } while (true);
            } finally {dbg.exitSubRule(89);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(518, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:521:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(521, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:522:2: ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) )
            int alt92=2;
            try { dbg.enterDecision(92, decisionCanBacktrack[92]);

            int LA92_0 = input.LA(1);

            if ( (LA92_0==IDENT||LA92_0==GEN||(LA92_0>=STAR && LA92_0<=PIPE)) ) {
                alt92=1;
            }
            else if ( (LA92_0==COLON||(LA92_0>=HASH && LA92_0<=DCOLON)||LA92_0==116) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    {
                    dbg.location(524,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:5: typeSelector ( ( esPred )=> elementSubsequent )*
                    {
                    dbg.location(524,5);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence1784);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(524,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:18: ( ( esPred )=> elementSubsequent )*
                    try { dbg.enterSubRule(90);

                    loop90:
                    do {
                        int alt90=2;
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:19: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(524,29);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence1791);
                    	    elementSubsequent();

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
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:2: ( ( ( esPred )=> elementSubsequent )+ )
                    {
                    dbg.location(526,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:2: ( ( ( esPred )=> elementSubsequent )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:4: ( ( esPred )=> elementSubsequent )+
                    {
                    dbg.location(526,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:4: ( ( esPred )=> elementSubsequent )+
                    int cnt91=0;
                    try { dbg.enterSubRule(91);

                    loop91:
                    do {
                        int alt91=2;
                        try { dbg.enterDecision(91, decisionCanBacktrack[91]);

                        switch ( input.LA(1) ) {
                        case HASH:
                            {
                            int LA91_2 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt91=1;
                            }


                            }
                            break;
                        case 116:
                            {
                            int LA91_3 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt91=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA91_4 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt91=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA91_5 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt91=1;
                            }


                            }
                            break;
                        case COLON:
                        case DCOLON:
                            {
                            int LA91_6 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt91=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(91);}

                        switch (alt91) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:5: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(526,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence1809);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt91 >= 1 ) break loop91;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(91, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt91++;
                    } while (true);
                    } finally {dbg.exitSubRule(91);}


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
        dbg.location(527, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "simpleSelectorSequence");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "simpleSelectorSequence"


    // $ANTLR start "esPred"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:534:1: esPred : ( '#' | HASH | DOT | LBRACKET | COLON | DCOLON );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(534, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:535:5: ( '#' | HASH | DOT | LBRACKET | COLON | DCOLON )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(535,5);
            if ( input.LA(1)==COLON||(input.LA(1)>=HASH && input.LA(1)<=DCOLON)||input.LA(1)==116 ) {
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
        dbg.location(536, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "esPred");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "esPred"


    // $ANTLR start "typeSelector"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:538:1: typeSelector options {k=2; } : ( ( nsPred )=> namespacePrefix )? ( elementName ( ws )? ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(538, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:3: ( ( ( nsPred )=> namespacePrefix )? ( elementName ( ws )? ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:6: ( ( nsPred )=> namespacePrefix )? ( elementName ( ws )? )
            {
            dbg.location(540,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:6: ( ( nsPred )=> namespacePrefix )?
            int alt93=2;
            try { dbg.enterSubRule(93);
            try { dbg.enterDecision(93, decisionCanBacktrack[93]);

            int LA93_0 = input.LA(1);

            if ( (LA93_0==IDENT) ) {
                int LA93_1 = input.LA(2);

                if ( (synpred3_Css3()) ) {
                    alt93=1;
                }
            }
            else if ( (LA93_0==STAR) ) {
                int LA93_2 = input.LA(2);

                if ( (synpred3_Css3()) ) {
                    alt93=1;
                }
            }
            else if ( (LA93_0==PIPE) && (synpred3_Css3())) {
                alt93=1;
            }
            } finally {dbg.exitDecision(93);}

            switch (alt93) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:7: ( nsPred )=> namespacePrefix
                    {
                    dbg.location(540,17);
                    pushFollow(FOLLOW_namespacePrefix_in_typeSelector1911);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(93);}

            dbg.location(540,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:35: ( elementName ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:37: elementName ( ws )?
            {
            dbg.location(540,37);
            pushFollow(FOLLOW_elementName_in_typeSelector1917);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(540,49);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:49: ( ws )?
            int alt94=2;
            try { dbg.enterSubRule(94);
            try { dbg.enterDecision(94, decisionCanBacktrack[94]);

            int LA94_0 = input.LA(1);

            if ( (LA94_0==WS||(LA94_0>=NL && LA94_0<=COMMENT)) ) {
                alt94=1;
            }
            } finally {dbg.exitDecision(94);}

            switch (alt94) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:49: ws
                    {
                    dbg.location(540,49);
                    pushFollow(FOLLOW_ws_in_typeSelector1919);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(94);}


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(541, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:544:1: nsPred : ( IDENT | STAR )? PIPE ;
    public final void nsPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "nsPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(544, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:545:3: ( ( IDENT | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:546:3: ( IDENT | STAR )? PIPE
            {
            dbg.location(546,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:546:3: ( IDENT | STAR )?
            int alt95=2;
            try { dbg.enterSubRule(95);
            try { dbg.enterDecision(95, decisionCanBacktrack[95]);

            int LA95_0 = input.LA(1);

            if ( (LA95_0==IDENT||LA95_0==STAR) ) {
                alt95=1;
            }
            } finally {dbg.exitDecision(95);}

            switch (alt95) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    {
                    dbg.location(546,3);
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
            } finally {dbg.exitSubRule(95);}

            dbg.location(546,19);
            match(input,PIPE,FOLLOW_PIPE_in_nsPred1948); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(547, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:549:1: namespacePrefix : ( namespacePrefixName | STAR )? PIPE ;
    public final void namespacePrefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(549, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:550:3: ( ( namespacePrefixName | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:550:5: ( namespacePrefixName | STAR )? PIPE
            {
            dbg.location(550,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:550:5: ( namespacePrefixName | STAR )?
            int alt96=3;
            try { dbg.enterSubRule(96);
            try { dbg.enterDecision(96, decisionCanBacktrack[96]);

            int LA96_0 = input.LA(1);

            if ( (LA96_0==IDENT) ) {
                alt96=1;
            }
            else if ( (LA96_0==STAR) ) {
                alt96=2;
            }
            } finally {dbg.exitDecision(96);}

            switch (alt96) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:550:7: namespacePrefixName
                    {
                    dbg.location(550,7);
                    pushFollow(FOLLOW_namespacePrefixName_in_namespacePrefix1963);
                    namespacePrefixName();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:550:29: STAR
                    {
                    dbg.location(550,29);
                    match(input,STAR,FOLLOW_STAR_in_namespacePrefix1967); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(96);}

            dbg.location(550,36);
            match(input,PIPE,FOLLOW_PIPE_in_namespacePrefix1971); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(551, 3);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "namespacePrefix");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "namespacePrefix"


    // $ANTLR start "elementSubsequent"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:554:1: elementSubsequent : ( cssId | cssClass | slAttribute | pseudo ) ( ws )? ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(554, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:555:5: ( ( cssId | cssClass | slAttribute | pseudo ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:5: ( cssId | cssClass | slAttribute | pseudo ) ( ws )?
            {
            dbg.location(556,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:5: ( cssId | cssClass | slAttribute | pseudo )
            int alt97=4;
            try { dbg.enterSubRule(97);
            try { dbg.enterDecision(97, decisionCanBacktrack[97]);

            switch ( input.LA(1) ) {
            case HASH:
            case 116:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:557:6: cssId
                    {
                    dbg.location(557,6);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent2005);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:558:8: cssClass
                    {
                    dbg.location(558,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent2014);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:559:11: slAttribute
                    {
                    dbg.location(559,11);
                    pushFollow(FOLLOW_slAttribute_in_elementSubsequent2026);
                    slAttribute();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:560:11: pseudo
                    {
                    dbg.location(560,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent2038);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(97);}

            dbg.location(562,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:5: ( ws )?
            int alt98=2;
            try { dbg.enterSubRule(98);
            try { dbg.enterDecision(98, decisionCanBacktrack[98]);

            int LA98_0 = input.LA(1);

            if ( (LA98_0==WS||(LA98_0>=NL && LA98_0<=COMMENT)) ) {
                alt98=1;
            }
            } finally {dbg.exitDecision(98);}

            switch (alt98) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:562:5: ws
                    {
                    dbg.location(562,5);
                    pushFollow(FOLLOW_ws_in_elementSubsequent2050);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(98);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(563, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:566:1: cssId : ( HASH | ( '#' NAME ) );
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(566, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:5: ( HASH | ( '#' NAME ) )
            int alt99=2;
            try { dbg.enterDecision(99, decisionCanBacktrack[99]);

            int LA99_0 = input.LA(1);

            if ( (LA99_0==HASH) ) {
                alt99=1;
            }
            else if ( (LA99_0==116) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:7: HASH
                    {
                    dbg.location(567,7);
                    match(input,HASH,FOLLOW_HASH_in_cssId2073); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:14: ( '#' NAME )
                    {
                    dbg.location(567,14);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:14: ( '#' NAME )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:567:16: '#' NAME
                    {
                    dbg.location(567,16);
                    match(input,116,FOLLOW_116_in_cssId2079); if (state.failed) return ;
                    dbg.location(567,20);
                    match(input,NAME,FOLLOW_NAME_in_cssId2081); if (state.failed) return ;

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
        dbg.location(568, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:574:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(574, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:575:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:575:7: DOT ( IDENT | GEN )
            {
            dbg.location(575,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass2109); if (state.failed) return ;
            dbg.location(575,11);
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
        dbg.location(576, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:583:1: elementName : ( ( IDENT | GEN ) | '*' );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(583, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:5: ( ( IDENT | GEN ) | '*' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(584,5);
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
        dbg.location(585, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "elementName");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "elementName"


    // $ANTLR start "slAttribute"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:1: slAttribute : LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET ;
    public final void slAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(587, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:5: ( LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:7: LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET
            {
            dbg.location(588,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_slAttribute2180); if (state.failed) return ;
            dbg.location(589,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:6: ( namespacePrefix )?
            int alt100=2;
            try { dbg.enterSubRule(100);
            try { dbg.enterDecision(100, decisionCanBacktrack[100]);

            int LA100_0 = input.LA(1);

            if ( (LA100_0==IDENT) ) {
                int LA100_1 = input.LA(2);

                if ( (LA100_1==PIPE) ) {
                    alt100=1;
                }
            }
            else if ( ((LA100_0>=STAR && LA100_0<=PIPE)) ) {
                alt100=1;
            }
            } finally {dbg.exitDecision(100);}

            switch (alt100) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:6: namespacePrefix
                    {
                    dbg.location(589,6);
                    pushFollow(FOLLOW_namespacePrefix_in_slAttribute2187);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(100);}

            dbg.location(589,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:23: ( ws )?
            int alt101=2;
            try { dbg.enterSubRule(101);
            try { dbg.enterDecision(101, decisionCanBacktrack[101]);

            int LA101_0 = input.LA(1);

            if ( (LA101_0==WS||(LA101_0>=NL && LA101_0<=COMMENT)) ) {
                alt101=1;
            }
            } finally {dbg.exitDecision(101);}

            switch (alt101) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:23: ws
                    {
                    dbg.location(589,23);
                    pushFollow(FOLLOW_ws_in_slAttribute2190);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(101);}

            dbg.location(590,9);
            pushFollow(FOLLOW_slAttributeName_in_slAttribute2201);
            slAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(590,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:25: ( ws )?
            int alt102=2;
            try { dbg.enterSubRule(102);
            try { dbg.enterDecision(102, decisionCanBacktrack[102]);

            int LA102_0 = input.LA(1);

            if ( (LA102_0==WS||(LA102_0>=NL && LA102_0<=COMMENT)) ) {
                alt102=1;
            }
            } finally {dbg.exitDecision(102);}

            switch (alt102) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:25: ws
                    {
                    dbg.location(590,25);
                    pushFollow(FOLLOW_ws_in_slAttribute2203);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(102);}

            dbg.location(592,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:13: ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:593:17: ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )?
                    {
                    dbg.location(593,17);
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

                    dbg.location(601,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:601:17: ( ws )?
                    int alt103=2;
                    try { dbg.enterSubRule(103);
                    try { dbg.enterDecision(103, decisionCanBacktrack[103]);

                    int LA103_0 = input.LA(1);

                    if ( (LA103_0==WS||(LA103_0>=NL && LA103_0<=COMMENT)) ) {
                        alt103=1;
                    }
                    } finally {dbg.exitDecision(103);}

                    switch (alt103) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:601:17: ws
                            {
                            dbg.location(601,17);
                            pushFollow(FOLLOW_ws_in_slAttribute2425);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(103);}

                    dbg.location(602,17);
                    pushFollow(FOLLOW_slAttributeValue_in_slAttribute2444);
                    slAttributeValue();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(603,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:17: ( ws )?
                    int alt104=2;
                    try { dbg.enterSubRule(104);
                    try { dbg.enterDecision(104, decisionCanBacktrack[104]);

                    int LA104_0 = input.LA(1);

                    if ( (LA104_0==WS||(LA104_0>=NL && LA104_0<=COMMENT)) ) {
                        alt104=1;
                    }
                    } finally {dbg.exitDecision(104);}

                    switch (alt104) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:603:17: ws
                            {
                            dbg.location(603,17);
                            pushFollow(FOLLOW_ws_in_slAttribute2462);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(104);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(105);}

            dbg.location(606,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_slAttribute2491); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(607, 1);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "slAttribute");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "slAttribute"


    // $ANTLR start "slAttributeName"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:614:1: slAttributeName : IDENT ;
    public final void slAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(614, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:615:4: IDENT
            {
            dbg.location(615,4);
            match(input,IDENT,FOLLOW_IDENT_in_slAttributeName2507); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(616, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "slAttributeName");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "slAttributeName"


    // $ANTLR start "slAttributeValue"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:1: slAttributeValue : ( IDENT | STRING ) ;
    public final void slAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(618, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:619:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:2: ( IDENT | STRING )
            {
            dbg.location(620,2);
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
        dbg.location(624, 9);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "slAttributeValue");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "slAttributeValue"


    // $ANTLR start "pseudo"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:626:1: pseudo : ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(626, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:627:5: ( ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:627:7: ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            {
            dbg.location(627,7);
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

            dbg.location(628,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:628:14: ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? )
                    {
                    dbg.location(629,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:629:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:630:21: ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )?
                    {
                    dbg.location(630,21);
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

                    dbg.location(631,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:631:21: ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:25: ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN
                            {
                            dbg.location(632,25);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:25: ( ws )?
                            int alt106=2;
                            try { dbg.enterSubRule(106);
                            try { dbg.enterDecision(106, decisionCanBacktrack[106]);

                            int LA106_0 = input.LA(1);

                            if ( (LA106_0==WS||(LA106_0>=NL && LA106_0<=COMMENT)) ) {
                                alt106=1;
                            }
                            } finally {dbg.exitDecision(106);}

                            switch (alt106) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:25: ws
                                    {
                                    dbg.location(632,25);
                                    pushFollow(FOLLOW_ws_in_pseudo2702);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(106);}

                            dbg.location(632,29);
                            match(input,LPAREN,FOLLOW_LPAREN_in_pseudo2705); if (state.failed) return ;
                            dbg.location(632,36);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:36: ( ws )?
                            int alt107=2;
                            try { dbg.enterSubRule(107);
                            try { dbg.enterDecision(107, decisionCanBacktrack[107]);

                            int LA107_0 = input.LA(1);

                            if ( (LA107_0==WS||(LA107_0>=NL && LA107_0<=COMMENT)) ) {
                                alt107=1;
                            }
                            } finally {dbg.exitDecision(107);}

                            switch (alt107) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:36: ws
                                    {
                                    dbg.location(632,36);
                                    pushFollow(FOLLOW_ws_in_pseudo2707);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(107);}

                            dbg.location(632,40);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:40: ( expression | '*' )?
                            int alt108=3;
                            try { dbg.enterSubRule(108);
                            try { dbg.enterDecision(108, decisionCanBacktrack[108]);

                            int LA108_0 = input.LA(1);

                            if ( ((LA108_0>=IDENT && LA108_0<=URI)||LA108_0==GEN||LA108_0==PLUS||(LA108_0>=MINUS && LA108_0<=HASH)||(LA108_0>=NUMBER && LA108_0<=DIMENSION)) ) {
                                alt108=1;
                            }
                            else if ( (LA108_0==STAR) ) {
                                alt108=2;
                            }
                            } finally {dbg.exitDecision(108);}

                            switch (alt108) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:42: expression
                                    {
                                    dbg.location(632,42);
                                    pushFollow(FOLLOW_expression_in_pseudo2712);
                                    expression();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;
                                case 2 :
                                    dbg.enterAlt(2);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:632:55: '*'
                                    {
                                    dbg.location(632,55);
                                    match(input,STAR,FOLLOW_STAR_in_pseudo2716); if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(108);}

                            dbg.location(632,62);
                            match(input,RPAREN,FOLLOW_RPAREN_in_pseudo2721); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(109);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    {
                    dbg.location(636,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:19: NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN
                    {
                    dbg.location(636,19);
                    match(input,NOT,FOLLOW_NOT_in_pseudo2800); if (state.failed) return ;
                    dbg.location(636,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:23: ( ws )?
                    int alt110=2;
                    try { dbg.enterSubRule(110);
                    try { dbg.enterDecision(110, decisionCanBacktrack[110]);

                    int LA110_0 = input.LA(1);

                    if ( (LA110_0==WS||(LA110_0>=NL && LA110_0<=COMMENT)) ) {
                        alt110=1;
                    }
                    } finally {dbg.exitDecision(110);}

                    switch (alt110) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:23: ws
                            {
                            dbg.location(636,23);
                            pushFollow(FOLLOW_ws_in_pseudo2802);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(110);}

                    dbg.location(636,27);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo2805); if (state.failed) return ;
                    dbg.location(636,34);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:34: ( ws )?
                    int alt111=2;
                    try { dbg.enterSubRule(111);
                    try { dbg.enterDecision(111, decisionCanBacktrack[111]);

                    int LA111_0 = input.LA(1);

                    if ( (LA111_0==WS||(LA111_0>=NL && LA111_0<=COMMENT)) ) {
                        alt111=1;
                    }
                    } finally {dbg.exitDecision(111);}

                    switch (alt111) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:34: ws
                            {
                            dbg.location(636,34);
                            pushFollow(FOLLOW_ws_in_pseudo2807);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(111);}

                    dbg.location(636,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:38: ( simpleSelectorSequence )?
                    int alt112=2;
                    try { dbg.enterSubRule(112);
                    try { dbg.enterDecision(112, decisionCanBacktrack[112]);

                    int LA112_0 = input.LA(1);

                    if ( (LA112_0==IDENT||LA112_0==GEN||LA112_0==COLON||(LA112_0>=HASH && LA112_0<=PIPE)||LA112_0==116) ) {
                        alt112=1;
                    }
                    } finally {dbg.exitDecision(112);}

                    switch (alt112) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:636:38: simpleSelectorSequence
                            {
                            dbg.location(636,38);
                            pushFollow(FOLLOW_simpleSelectorSequence_in_pseudo2810);
                            simpleSelectorSequence();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(112);}

                    dbg.location(636,62);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo2813); if (state.failed) return ;

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
        dbg.location(638, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:640:1: declaration : property COLON ( ws )? propertyValue ( prio )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(640, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:641:5: ( property COLON ( ws )? propertyValue ( prio )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:5: property COLON ( ws )? propertyValue ( prio )?
            {
            dbg.location(643,5);
            pushFollow(FOLLOW_property_in_declaration2857);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(643,14);
            match(input,COLON,FOLLOW_COLON_in_declaration2859); if (state.failed) return ;
            dbg.location(643,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:20: ( ws )?
            int alt114=2;
            try { dbg.enterSubRule(114);
            try { dbg.enterDecision(114, decisionCanBacktrack[114]);

            int LA114_0 = input.LA(1);

            if ( (LA114_0==WS||(LA114_0>=NL && LA114_0<=COMMENT)) ) {
                alt114=1;
            }
            } finally {dbg.exitDecision(114);}

            switch (alt114) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:20: ws
                    {
                    dbg.location(643,20);
                    pushFollow(FOLLOW_ws_in_declaration2861);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(114);}

            dbg.location(643,24);
            pushFollow(FOLLOW_propertyValue_in_declaration2864);
            propertyValue();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(643,38);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:38: ( prio )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:643:38: prio
                    {
                    dbg.location(643,38);
                    pushFollow(FOLLOW_prio_in_declaration2866);
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
        dbg.location(644, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "declaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "declaration"


    // $ANTLR start "propertyValue"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:652:1: propertyValue : expression ;
    public final void propertyValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "propertyValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(652, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:653:2: ( expression )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:653:4: expression
            {
            dbg.location(653,4);
            pushFollow(FOLLOW_expression_in_propertyValue2890);
            expression();

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
        dbg.location(654, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "propertyValue");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "propertyValue"


    // $ANTLR start "syncTo_IDENT_RBRACE"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:658:1: syncTo_IDENT_RBRACE : ;
    public final void syncTo_IDENT_RBRACE() throws RecognitionException {

                syncToSet(BitSet.of(IDENT, RBRACE));
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_IDENT_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(658, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:662:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:663:6: 
            {
            }

        }
        finally {
        }
        dbg.location(663, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:665:1: syncTo_RBRACE : ;
    public final void syncTo_RBRACE() throws RecognitionException {

                syncToRBRACE(1); //initial nest == 1
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(665, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:669:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:6: 
            {
            }

        }
        finally {
        }
        dbg.location(670, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:673:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(673, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:677:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:678:6: 
            {
            }

        }
        finally {
        }
        dbg.location(678, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:680:1: prio : IMPORTANT_SYM ( ws )? ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(680, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:681:5: ( IMPORTANT_SYM ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:681:7: IMPORTANT_SYM ( ws )?
            {
            dbg.location(681,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio3002); if (state.failed) return ;
            dbg.location(681,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:681:21: ( ws )?
            int alt116=2;
            try { dbg.enterSubRule(116);
            try { dbg.enterDecision(116, decisionCanBacktrack[116]);

            int LA116_0 = input.LA(1);

            if ( (LA116_0==WS||(LA116_0>=NL && LA116_0<=COMMENT)) ) {
                alt116=1;
            }
            } finally {dbg.exitDecision(116);}

            switch (alt116) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:681:21: ws
                    {
                    dbg.location(681,21);
                    pushFollow(FOLLOW_ws_in_prio3004);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(116);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(682, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "prio");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "prio"


    // $ANTLR start "expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:684:1: expression : term ( operator term )* ;
    public final void expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(684, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:685:5: ( term ( operator term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:685:7: term ( operator term )*
            {
            dbg.location(685,7);
            pushFollow(FOLLOW_term_in_expression3026);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(685,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:685:12: ( operator term )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:685:13: operator term
            	    {
            	    dbg.location(685,13);
            	    pushFollow(FOLLOW_operator_in_expression3029);
            	    operator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(685,22);
            	    pushFollow(FOLLOW_term_in_expression3031);
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
        dbg.location(686, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "expression");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "expression"


    // $ANTLR start "term"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:688:1: term : ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( ws )? ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(688, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:689:5: ( ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:689:7: ( unaryOperator )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function ) ( ws )?
            {
            dbg.location(689,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:689:7: ( unaryOperator )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:689:7: unaryOperator
                    {
                    dbg.location(689,7);
                    pushFollow(FOLLOW_unaryOperator_in_term3054);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(118);}

            dbg.location(690,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:690:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:691:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(691,9);
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:704:7: STRING
                    {
                    dbg.location(704,7);
                    match(input,STRING,FOLLOW_STRING_in_term3274); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:705:7: IDENT
                    {
                    dbg.location(705,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term3282); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:706:7: GEN
                    {
                    dbg.location(706,7);
                    match(input,GEN,FOLLOW_GEN_in_term3290); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:707:7: URI
                    {
                    dbg.location(707,7);
                    match(input,URI,FOLLOW_URI_in_term3298); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:7: hexColor
                    {
                    dbg.location(708,7);
                    pushFollow(FOLLOW_hexColor_in_term3306);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:709:7: function
                    {
                    dbg.location(709,7);
                    pushFollow(FOLLOW_function_in_term3314);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(119);}

            dbg.location(711,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:711:5: ( ws )?
            int alt120=2;
            try { dbg.enterSubRule(120);
            try { dbg.enterDecision(120, decisionCanBacktrack[120]);

            int LA120_0 = input.LA(1);

            if ( (LA120_0==WS||(LA120_0>=NL && LA120_0<=COMMENT)) ) {
                alt120=1;
            }
            } finally {dbg.exitDecision(120);}

            switch (alt120) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:711:5: ws
                    {
                    dbg.location(711,5);
                    pushFollow(FOLLOW_ws_in_term3326);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(120);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(712, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:1: function : functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(714, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:715:2: ( functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:715:5: functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN
            {
            dbg.location(715,5);
            pushFollow(FOLLOW_functionName_in_function3342);
            functionName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(715,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:715:18: ( ws )?
            int alt121=2;
            try { dbg.enterSubRule(121);
            try { dbg.enterDecision(121, decisionCanBacktrack[121]);

            int LA121_0 = input.LA(1);

            if ( (LA121_0==WS||(LA121_0>=NL && LA121_0<=COMMENT)) ) {
                alt121=1;
            }
            } finally {dbg.exitDecision(121);}

            switch (alt121) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:715:18: ws
                    {
                    dbg.location(715,18);
                    pushFollow(FOLLOW_ws_in_function3344);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(121);}

            dbg.location(716,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function3349); if (state.failed) return ;
            dbg.location(716,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:716:10: ( ws )?
            int alt122=2;
            try { dbg.enterSubRule(122);
            try { dbg.enterDecision(122, decisionCanBacktrack[122]);

            int LA122_0 = input.LA(1);

            if ( (LA122_0==WS||(LA122_0>=NL && LA122_0<=COMMENT)) ) {
                alt122=1;
            }
            } finally {dbg.exitDecision(122);}

            switch (alt122) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:716:10: ws
                    {
                    dbg.location(716,10);
                    pushFollow(FOLLOW_ws_in_function3351);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(122);}

            dbg.location(717,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:717:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:4: expression
                    {
                    dbg.location(718,4);
                    pushFollow(FOLLOW_expression_in_function3362);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:720:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    {
                    dbg.location(720,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:720:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:721:5: fnAttribute ( COMMA ( ws )? fnAttribute )*
                    {
                    dbg.location(721,5);
                    pushFollow(FOLLOW_fnAttribute_in_function3380);
                    fnAttribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(721,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:721:17: ( COMMA ( ws )? fnAttribute )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:721:18: COMMA ( ws )? fnAttribute
                    	    {
                    	    dbg.location(721,18);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function3383); if (state.failed) return ;
                    	    dbg.location(721,24);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:721:24: ( ws )?
                    	    int alt123=2;
                    	    try { dbg.enterSubRule(123);
                    	    try { dbg.enterDecision(123, decisionCanBacktrack[123]);

                    	    int LA123_0 = input.LA(1);

                    	    if ( (LA123_0==WS||(LA123_0>=NL && LA123_0<=COMMENT)) ) {
                    	        alt123=1;
                    	    }
                    	    } finally {dbg.exitDecision(123);}

                    	    switch (alt123) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:721:24: ws
                    	            {
                    	            dbg.location(721,24);
                    	            pushFollow(FOLLOW_ws_in_function3385);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(123);}

                    	    dbg.location(721,28);
                    	    pushFollow(FOLLOW_fnAttribute_in_function3388);
                    	    fnAttribute();

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

            dbg.location(724,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function3409); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 

        }
        finally {
        }
        dbg.location(725, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "function");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "function"


    // $ANTLR start "functionName"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:731:1: functionName : ( IDENT COLON )? IDENT ( DOT IDENT )* ;
    public final void functionName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "functionName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(731, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:2: ( ( IDENT COLON )? IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:4: ( IDENT COLON )? IDENT ( DOT IDENT )*
            {
            dbg.location(735,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:4: ( IDENT COLON )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:5: IDENT COLON
                    {
                    dbg.location(735,5);
                    match(input,IDENT,FOLLOW_IDENT_in_functionName3457); if (state.failed) return ;
                    dbg.location(735,11);
                    match(input,COLON,FOLLOW_COLON_in_functionName3459); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(126);}

            dbg.location(735,19);
            match(input,IDENT,FOLLOW_IDENT_in_functionName3463); if (state.failed) return ;
            dbg.location(735,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:25: ( DOT IDENT )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:26: DOT IDENT
            	    {
            	    dbg.location(735,26);
            	    match(input,DOT,FOLLOW_DOT_in_functionName3466); if (state.failed) return ;
            	    dbg.location(735,30);
            	    match(input,IDENT,FOLLOW_IDENT_in_functionName3468); if (state.failed) return ;

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
        dbg.location(736, 6);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "functionName");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "functionName"


    // $ANTLR start "fnAttribute"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:738:1: fnAttribute : fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue ;
    public final void fnAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(738, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:2: ( fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:4: fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue
            {
            dbg.location(739,4);
            pushFollow(FOLLOW_fnAttributeName_in_fnAttribute3490);
            fnAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(739,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:20: ( ws )?
            int alt128=2;
            try { dbg.enterSubRule(128);
            try { dbg.enterDecision(128, decisionCanBacktrack[128]);

            int LA128_0 = input.LA(1);

            if ( (LA128_0==WS||(LA128_0>=NL && LA128_0<=COMMENT)) ) {
                alt128=1;
            }
            } finally {dbg.exitDecision(128);}

            switch (alt128) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:20: ws
                    {
                    dbg.location(739,20);
                    pushFollow(FOLLOW_ws_in_fnAttribute3492);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(128);}

            dbg.location(739,24);
            match(input,OPEQ,FOLLOW_OPEQ_in_fnAttribute3495); if (state.failed) return ;
            dbg.location(739,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:29: ( ws )?
            int alt129=2;
            try { dbg.enterSubRule(129);
            try { dbg.enterDecision(129, decisionCanBacktrack[129]);

            int LA129_0 = input.LA(1);

            if ( (LA129_0==WS||(LA129_0>=NL && LA129_0<=COMMENT)) ) {
                alt129=1;
            }
            } finally {dbg.exitDecision(129);}

            switch (alt129) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:29: ws
                    {
                    dbg.location(739,29);
                    pushFollow(FOLLOW_ws_in_fnAttribute3497);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(129);}

            dbg.location(739,33);
            pushFollow(FOLLOW_fnAttributeValue_in_fnAttribute3500);
            fnAttributeValue();

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
        dbg.location(740, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "fnAttribute");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "fnAttribute"


    // $ANTLR start "fnAttributeName"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:742:1: fnAttributeName : IDENT ( DOT IDENT )* ;
    public final void fnAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(742, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:2: ( IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:4: IDENT ( DOT IDENT )*
            {
            dbg.location(743,4);
            match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName3515); if (state.failed) return ;
            dbg.location(743,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:10: ( DOT IDENT )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:743:11: DOT IDENT
            	    {
            	    dbg.location(743,11);
            	    match(input,DOT,FOLLOW_DOT_in_fnAttributeName3518); if (state.failed) return ;
            	    dbg.location(743,15);
            	    match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName3520); if (state.failed) return ;

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
        dbg.location(744, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "fnAttributeName");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "fnAttributeName"


    // $ANTLR start "fnAttributeValue"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:746:1: fnAttributeValue : expression ;
    public final void fnAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(746, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:747:2: ( expression )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:747:4: expression
            {
            dbg.location(747,4);
            pushFollow(FOLLOW_expression_in_fnAttributeValue3534);
            expression();

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
        dbg.location(748, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "fnAttributeValue");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "fnAttributeValue"


    // $ANTLR start "hexColor"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:750:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(750, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:7: HASH
            {
            dbg.location(751,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor3552); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(752, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "hexColor");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "hexColor"


    // $ANTLR start "ws"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:754:1: ws : ( WS | NL | COMMENT )+ ;
    public final void ws() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ws");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(754, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:755:5: ( ( WS | NL | COMMENT )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:755:7: ( WS | NL | COMMENT )+
            {
            dbg.location(755,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:755:7: ( WS | NL | COMMENT )+
            int cnt131=0;
            try { dbg.enterSubRule(131);

            loop131:
            do {
                int alt131=2;
                try { dbg.enterDecision(131, decisionCanBacktrack[131]);

                int LA131_0 = input.LA(1);

                if ( (LA131_0==WS||(LA131_0>=NL && LA131_0<=COMMENT)) ) {
                    alt131=1;
                }


                } finally {dbg.exitDecision(131);}

                switch (alt131) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            	    {
            	    dbg.location(755,7);
            	    if ( input.LA(1)==WS||(input.LA(1)>=NL && input.LA(1)<=COMMENT) ) {
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

            	default :
            	    if ( cnt131 >= 1 ) break loop131;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(131, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt131++;
            } while (true);
            } finally {dbg.exitSubRule(131);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(756, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "ws");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "ws"

    // $ANTLR start synpred1_Css3
    public final void synpred1_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:19: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:20: esPred
        {
        dbg.location(524,20);
        pushFollow(FOLLOW_esPred_in_synpred1_Css31788);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:526:6: esPred
        {
        dbg.location(526,6);
        pushFollow(FOLLOW_esPred_in_synpred2_Css31806);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:7: ( nsPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:540:8: nsPred
        {
        dbg.location(540,8);
        pushFollow(FOLLOW_nsPred_in_synpred3_Css31908);
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


    protected DFA90 dfa90 = new DFA90(this);
    protected DFA109 dfa109 = new DFA109(this);
    protected DFA117 dfa117 = new DFA117(this);
    protected DFA119 dfa119 = new DFA119(this);
    protected DFA125 dfa125 = new DFA125(this);
    static final String DFA90_eotS =
        "\21\uffff";
    static final String DFA90_eofS =
        "\21\uffff";
    static final String DFA90_minS =
        "\1\5\7\uffff\5\0\4\uffff";
    static final String DFA90_maxS =
        "\1\164\7\uffff\5\0\4\uffff";
    static final String DFA90_acceptS =
        "\1\uffff\1\2\16\uffff\1\1";
    static final String DFA90_specialS =
        "\10\uffff\1\0\1\1\1\2\1\3\1\4\4\uffff}>";
    static final String[] DFA90_transitionS = {
            "\1\1\6\uffff\1\1\1\uffff\1\1\3\uffff\1\1\31\uffff\1\14\1\uffff"+
            "\3\1\1\uffff\1\10\1\12\1\13\1\14\2\1\11\uffff\1\1\62\uffff\1"+
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
            return "()* loopback of 524:18: ( ( esPred )=> elementSubsequent )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA90_8 = input.LA(1);

                         
                        int index90_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index90_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA90_9 = input.LA(1);

                         
                        int index90_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index90_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA90_10 = input.LA(1);

                         
                        int index90_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index90_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA90_11 = input.LA(1);

                         
                        int index90_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index90_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA90_12 = input.LA(1);

                         
                        int index90_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index90_12);
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
    static final String DFA109_eotS =
        "\4\uffff";
    static final String DFA109_eofS =
        "\4\uffff";
    static final String DFA109_minS =
        "\2\5\2\uffff";
    static final String DFA109_maxS =
        "\2\164\2\uffff";
    static final String DFA109_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA109_specialS =
        "\4\uffff}>";
    static final String[] DFA109_transitionS = {
            "\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff\1\1\27\uffff"+
            "\1\3\1\uffff\3\3\1\uffff\6\3\10\uffff\1\2\1\3\14\uffff\2\1\44"+
            "\uffff\1\3",
            "\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff\1\1\27\uffff"+
            "\1\3\1\uffff\3\3\1\uffff\6\3\10\uffff\1\2\1\3\14\uffff\2\1\44"+
            "\uffff\1\3",
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
            return "631:21: ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )?";
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
        "\1\5\1\uffff\1\5\1\uffff\4\5\2\24";
    static final String DFA117_maxS =
        "\1\115\1\uffff\1\117\1\uffff\2\117\1\5\3\117";
    static final String DFA117_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\6\uffff";
    static final String DFA117_specialS =
        "\12\uffff}>";
    static final String[] DFA117_transitionS = {
            "\3\3\1\uffff\1\1\3\uffff\1\1\1\2\3\uffff\1\3\32\uffff\2\3\2"+
            "\uffff\2\3\16\uffff\2\1\13\3",
            "",
            "\1\5\2\3\12\uffff\1\3\1\uffff\1\4\31\uffff\1\3\2\uffff\2\3"+
            "\20\uffff\13\3\2\4",
            "",
            "\1\5\2\3\12\uffff\1\3\1\uffff\1\4\31\uffff\1\3\2\uffff\2\3"+
            "\20\uffff\13\3\2\4",
            "\3\3\1\uffff\1\3\3\uffff\2\3\3\uffff\1\3\1\uffff\1\7\27\uffff"+
            "\3\3\2\uffff\2\3\1\6\5\uffff\1\1\6\uffff\16\3\2\7",
            "\1\10",
            "\3\3\1\uffff\1\3\3\uffff\2\3\3\uffff\1\3\1\uffff\1\7\30\uffff"+
            "\2\3\2\uffff\2\3\6\uffff\1\1\6\uffff\16\3\2\7",
            "\1\11\36\uffff\1\6\5\uffff\1\1\6\uffff\1\3\15\uffff\2\11",
            "\1\11\44\uffff\1\1\6\uffff\1\3\15\uffff\2\11"
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
            return "()* loopback of 685:12: ( operator term )*";
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
        "\1\5\2\uffff\1\5\4\uffff\1\5\1\uffff";
    static final String DFA119_maxS =
        "\1\115\2\uffff\1\117\4\uffff\1\117\1\uffff";
    static final String DFA119_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\7\1\uffff\1\3";
    static final String DFA119_specialS =
        "\12\uffff}>";
    static final String[] DFA119_transitionS = {
            "\1\3\1\2\1\5\12\uffff\1\4\37\uffff\1\6\20\uffff\13\1",
            "",
            "",
            "\3\11\1\uffff\1\11\3\uffff\2\11\3\uffff\1\11\1\uffff\1\10\27"+
            "\uffff\1\7\2\11\2\uffff\2\11\1\7\14\uffff\1\7\15\11\2\10",
            "",
            "",
            "",
            "",
            "\3\11\1\uffff\1\11\3\uffff\2\11\3\uffff\1\11\1\uffff\1\10\30"+
            "\uffff\2\11\2\uffff\2\11\15\uffff\1\7\15\11\2\10",
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
            return "690:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function )";
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
        "\1\5\1\uffff\3\5\1\uffff\2\24";
    static final String DFA125_maxS =
        "\1\115\1\uffff\2\117\1\5\1\uffff\2\117";
    static final String DFA125_acceptS =
        "\1\uffff\1\1\3\uffff\1\2\2\uffff";
    static final String DFA125_specialS =
        "\10\uffff}>";
    static final String[] DFA125_transitionS = {
            "\1\2\2\1\12\uffff\1\1\33\uffff\1\1\2\uffff\2\1\20\uffff\13\1",
            "",
            "\3\1\6\uffff\1\1\3\uffff\1\1\1\uffff\1\3\27\uffff\3\1\2\uffff"+
            "\2\1\1\4\5\uffff\1\5\6\uffff\2\1\1\uffff\13\1\2\3",
            "\3\1\6\uffff\1\1\3\uffff\1\1\1\uffff\1\3\30\uffff\2\1\2\uffff"+
            "\2\1\6\uffff\1\5\6\uffff\2\1\1\uffff\13\1\2\3",
            "\1\6",
            "",
            "\1\7\36\uffff\1\4\5\uffff\1\5\6\uffff\1\1\15\uffff\2\7",
            "\1\7\44\uffff\1\5\6\uffff\1\1\15\uffff\2\7"
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
            return "717:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_ws_in_styleSheet125 = new BitSet(new long[]{0x00FC10000E2C0D30L,0x0010000000000000L});
    public static final BitSet FOLLOW_charSet_in_styleSheet135 = new BitSet(new long[]{0x00FC10000E3C0C30L,0x001000000000C000L});
    public static final BitSet FOLLOW_ws_in_styleSheet137 = new BitSet(new long[]{0x00FC10000E2C0C30L,0x0010000000000000L});
    public static final BitSet FOLLOW_imports_in_styleSheet151 = new BitSet(new long[]{0x00FC10000E2C0830L,0x0010000000000000L});
    public static final BitSet FOLLOW_namespaces_in_styleSheet162 = new BitSet(new long[]{0x00FC10000E2C0820L,0x0010000000000000L});
    public static final BitSet FOLLOW_body_in_styleSheet174 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_styleSheet182 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespace_in_namespaces199 = new BitSet(new long[]{0x0000000000100012L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_namespaces201 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_NAMESPACE_SYM_in_namespace217 = new BitSet(new long[]{0x00000000001000E0L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_namespace219 = new BitSet(new long[]{0x00000000001000E0L,0x000000000000C000L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespace223 = new BitSet(new long[]{0x00000000001000E0L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_namespace225 = new BitSet(new long[]{0x00000000001000E0L,0x000000000000C000L});
    public static final BitSet FOLLOW_resourceIdentifier_in_namespace230 = new BitSet(new long[]{0x0000000000100200L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_namespace232 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_namespace235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_namespacePrefixName248 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_resourceIdentifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHARSET_SYM_in_charSet286 = new BitSet(new long[]{0x0000000000100040L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_charSet288 = new BitSet(new long[]{0x0000000000100040L,0x000000000000C000L});
    public static final BitSet FOLLOW_charSetValue_in_charSet291 = new BitSet(new long[]{0x0000000000100200L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_charSet293 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_charSet296 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_charSetValue310 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_importItem_in_imports324 = new BitSet(new long[]{0x0000000000100402L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_imports326 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_IMPORT_SYM_in_importItem347 = new BitSet(new long[]{0x00000000001000E0L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_importItem349 = new BitSet(new long[]{0x00000000001000E0L,0x000000000000C000L});
    public static final BitSet FOLLOW_resourceIdentifier_in_importItem352 = new BitSet(new long[]{0x0000000000170220L,0x000000000000C001L});
    public static final BitSet FOLLOW_ws_in_importItem354 = new BitSet(new long[]{0x0000000000070220L,0x0000000000000001L});
    public static final BitSet FOLLOW_mediaQueryList_in_importItem357 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_importItem359 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEDIA_SYM_in_media375 = new BitSet(new long[]{0x0000000000171020L,0x000000000000C001L});
    public static final BitSet FOLLOW_ws_in_media377 = new BitSet(new long[]{0x0000000000071020L,0x0000000000000001L});
    public static final BitSet FOLLOW_mediaQueryList_in_media380 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_media390 = new BitSet(new long[]{0x00FC10000A142020L,0x001000000000C000L});
    public static final BitSet FOLLOW_ws_in_media392 = new BitSet(new long[]{0x00FC10000A042020L,0x0010000000000000L});
    public static final BitSet FOLLOW_rule_in_media411 = new BitSet(new long[]{0x00FC10000A142020L,0x001000000000C000L});
    public static final BitSet FOLLOW_page_in_media415 = new BitSet(new long[]{0x00FC10000A142020L,0x001000000000C000L});
    public static final BitSet FOLLOW_fontFace_in_media419 = new BitSet(new long[]{0x00FC10000A142020L,0x001000000000C000L});
    public static final BitSet FOLLOW_ws_in_media423 = new BitSet(new long[]{0x00FC10000A042020L,0x0010000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_media437 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList453 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_COMMA_in_mediaQueryList457 = new BitSet(new long[]{0x0000000000170020L,0x000000000000C001L});
    public static final BitSet FOLLOW_ws_in_mediaQueryList459 = new BitSet(new long[]{0x0000000000070020L,0x0000000000000001L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList462 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_mediaQueryOperator_in_mediaQuery481 = new BitSet(new long[]{0x0000000000170020L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery483 = new BitSet(new long[]{0x0000000000070020L});
    public static final BitSet FOLLOW_mediaType_in_mediaQuery490 = new BitSet(new long[]{0x0000000000108002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery492 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery497 = new BitSet(new long[]{0x0000000000170020L,0x000000000000C001L});
    public static final BitSet FOLLOW_ws_in_mediaQuery499 = new BitSet(new long[]{0x0000000000070020L,0x0000000000000001L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery502 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery510 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery514 = new BitSet(new long[]{0x0000000000170020L,0x000000000000C001L});
    public static final BitSet FOLLOW_ws_in_mediaQuery516 = new BitSet(new long[]{0x0000000000070020L,0x0000000000000001L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery519 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_set_in_mediaQueryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_mediaType0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_mediaExpression571 = new BitSet(new long[]{0x0000000000100020L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression573 = new BitSet(new long[]{0x0000000000100020L,0x000000000000C000L});
    public static final BitSet FOLLOW_mediaFeature_in_mediaExpression576 = new BitSet(new long[]{0x0000100000100000L,0x000000000000C002L});
    public static final BitSet FOLLOW_ws_in_mediaExpression578 = new BitSet(new long[]{0x0000100000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_mediaExpression583 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_ws_in_mediaExpression585 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_expression_in_mediaExpression588 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RPAREN_in_mediaExpression593 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression595 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_mediaFeature608 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bodyItem_in_body624 = new BitSet(new long[]{0x00FC10000E3C0822L,0x001000000000C000L});
    public static final BitSet FOLLOW_ws_in_body626 = new BitSet(new long[]{0x00FC10000E2C0822L,0x0010000000000000L});
    public static final BitSet FOLLOW_rule_in_bodyItem651 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_media_in_bodyItem663 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_page_in_bodyItem675 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_counterStyle_in_bodyItem687 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fontFace_in_bodyItem699 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_moz_document_in_bodyItem711 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generic_at_rule_in_bodyItem723 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GENERIC_AT_RULE_in_generic_at_rule755 = new BitSet(new long[]{0x0000000000101060L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule757 = new BitSet(new long[]{0x0000000000101060L});
    public static final BitSet FOLLOW_set_in_generic_at_rule762 = new BitSet(new long[]{0x0000000000101000L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule772 = new BitSet(new long[]{0x0000000000101000L});
    public static final BitSet FOLLOW_LBRACE_in_generic_at_rule787 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_syncTo_RBRACE_in_generic_at_rule799 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_generic_at_rule809 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document825 = new BitSet(new long[]{0x0000000001D00080L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_moz_document827 = new BitSet(new long[]{0x0000000001D00080L,0x000000000000C000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document832 = new BitSet(new long[]{0x0000000000105000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_moz_document834 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_COMMA_in_moz_document840 = new BitSet(new long[]{0x0000000001D00080L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_moz_document842 = new BitSet(new long[]{0x0000000001D00080L,0x000000000000C000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document845 = new BitSet(new long[]{0x0000000000105000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_moz_document847 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_LBRACE_in_moz_document854 = new BitSet(new long[]{0x00FC100002142020L,0x001000000000C000L});
    public static final BitSet FOLLOW_ws_in_moz_document856 = new BitSet(new long[]{0x00FC100002042020L,0x0010000000000000L});
    public static final BitSet FOLLOW_rule_in_moz_document865 = new BitSet(new long[]{0x00FC100002142020L,0x001000000000C000L});
    public static final BitSet FOLLOW_page_in_moz_document869 = new BitSet(new long[]{0x00FC100002142020L,0x001000000000C000L});
    public static final BitSet FOLLOW_ws_in_moz_document873 = new BitSet(new long[]{0x00FC100002042020L,0x0010000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_moz_document879 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_moz_document_function0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page921 = new BitSet(new long[]{0x0000100000101020L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_page923 = new BitSet(new long[]{0x0000100000001020L});
    public static final BitSet FOLLOW_IDENT_in_page928 = new BitSet(new long[]{0x0000100000101000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_page930 = new BitSet(new long[]{0x0000100000001000L});
    public static final BitSet FOLLOW_pseudoPage_in_page937 = new BitSet(new long[]{0x0000000000101000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_page939 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_page952 = new BitSet(new long[]{0x00000FFFF0142220L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_page954 = new BitSet(new long[]{0x00000FFFF0042220L});
    public static final BitSet FOLLOW_declaration_in_page1009 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_margin_in_page1011 = new BitSet(new long[]{0x0000000000102200L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_page1013 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_SEMI_in_page1019 = new BitSet(new long[]{0x00000FFFF0142220L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_page1021 = new BitSet(new long[]{0x00000FFFF0042220L});
    public static final BitSet FOLLOW_declaration_in_page1025 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_margin_in_page1027 = new BitSet(new long[]{0x0000000000102200L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_page1029 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_RBRACE_in_page1044 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1065 = new BitSet(new long[]{0x0000000000100020L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1067 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_counterStyle1070 = new BitSet(new long[]{0x0000000000101000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1072 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_counterStyle1083 = new BitSet(new long[]{0x0000000000142220L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1085 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_counterStyle1088 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_declarations_in_counterStyle1092 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_counterStyle1102 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FONT_FACE_SYM_in_fontFace1123 = new BitSet(new long[]{0x0000000000101000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_fontFace1125 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_fontFace1136 = new BitSet(new long[]{0x0000000000142220L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_fontFace1138 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_fontFace1141 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_declarations_in_fontFace1145 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_fontFace1155 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_margin_sym_in_margin1170 = new BitSet(new long[]{0x0000000000101000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_margin1172 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_margin1175 = new BitSet(new long[]{0x0000000000142220L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_margin1177 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_margin1180 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_declarations_in_margin1182 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_margin1184 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_margin_sym0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage1413 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage1415 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_operator1436 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_operator1438 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMMA_in_operator1447 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_operator1449 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_combinator1477 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_combinator1479 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_combinator1488 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_combinator1490 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_combinator1499 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_combinator1501 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_property1561 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_property1569 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectorsGroup_in_rule1594 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_rule1604 = new BitSet(new long[]{0x0000000000142220L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_rule1606 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_syncTo_IDENT_RBRACE_in_rule1609 = new BitSet(new long[]{0x0000000000042220L});
    public static final BitSet FOLLOW_declarations_in_rule1623 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_rule1633 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_declarations1681 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_SEMI_in_declarations1685 = new BitSet(new long[]{0x0000000000140222L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_declarations1687 = new BitSet(new long[]{0x0000000000040222L});
    public static final BitSet FOLLOW_declaration_in_declarations1690 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup1714 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup1717 = new BitSet(new long[]{0x00FC100000140020L,0x001000000000C000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup1719 = new BitSet(new long[]{0x00FC100000040020L,0x0010000000000000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup1722 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector1745 = new BitSet(new long[]{0x00FDD00000040022L,0x0010000000000000L});
    public static final BitSet FOLLOW_combinator_in_selector1748 = new BitSet(new long[]{0x00FC100000040020L,0x0010000000000000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector1750 = new BitSet(new long[]{0x00FDD00000040022L,0x0010000000000000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence1784 = new BitSet(new long[]{0x00FC100000040022L,0x0010000000000000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence1791 = new BitSet(new long[]{0x00FC100000040022L,0x0010000000000000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence1809 = new BitSet(new long[]{0x00FC100000040022L,0x0010000000000000L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefix_in_typeSelector1911 = new BitSet(new long[]{0x00C0000000040020L});
    public static final BitSet FOLLOW_elementName_in_typeSelector1917 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_typeSelector1919 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_nsPred1939 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_PIPE_in_nsPred1948 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespacePrefix1963 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_STAR_in_namespacePrefix1967 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_PIPE_in_namespacePrefix1971 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent2005 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent2014 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_slAttribute_in_elementSubsequent2026 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent2038 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_elementSubsequent2050 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_cssId2073 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_116_in_cssId2079 = new BitSet(new long[]{0x0100000000000000L});
    public static final BitSet FOLLOW_NAME_in_cssId2081 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass2109 = new BitSet(new long[]{0x0000000000040020L});
    public static final BitSet FOLLOW_set_in_cssClass2111 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_slAttribute2180 = new BitSet(new long[]{0x00C0000000100020L,0x000000000000C000L});
    public static final BitSet FOLLOW_namespacePrefix_in_slAttribute2187 = new BitSet(new long[]{0x00C0000000100020L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2190 = new BitSet(new long[]{0x00C0000000100020L,0x000000000000C000L});
    public static final BitSet FOLLOW_slAttributeName_in_slAttribute2201 = new BitSet(new long[]{0xFE00000000100000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2203 = new BitSet(new long[]{0xFE00000000000000L});
    public static final BitSet FOLLOW_set_in_slAttribute2245 = new BitSet(new long[]{0x0000000000100060L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2425 = new BitSet(new long[]{0x0000000000100060L,0x000000000000C000L});
    public static final BitSet FOLLOW_slAttributeValue_in_slAttribute2444 = new BitSet(new long[]{0x8000000000100000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2462 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_RBRACKET_in_slAttribute2491 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_slAttributeName2507 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_slAttributeValue2521 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pseudo2581 = new BitSet(new long[]{0x0000000000060020L});
    public static final BitSet FOLLOW_set_in_pseudo2645 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C001L});
    public static final BitSet FOLLOW_ws_in_pseudo2702 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo2705 = new BitSet(new long[]{0x00464000001400E0L,0x000000000000FFFAL});
    public static final BitSet FOLLOW_ws_in_pseudo2707 = new BitSet(new long[]{0x00464000001400E0L,0x000000000000FFFAL});
    public static final BitSet FOLLOW_expression_in_pseudo2712 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_pseudo2716 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo2721 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_pseudo2800 = new BitSet(new long[]{0x0000000000100000L,0x000000000000C001L});
    public static final BitSet FOLLOW_ws_in_pseudo2802 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo2805 = new BitSet(new long[]{0x00FC100000140020L,0x001000000000C002L});
    public static final BitSet FOLLOW_ws_in_pseudo2807 = new BitSet(new long[]{0x00FC100000040020L,0x0010000000000002L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_pseudo2810 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo2813 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_property_in_declaration2857 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_COLON_in_declaration2859 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_ws_in_declaration2861 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_propertyValue_in_declaration2864 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_prio_in_declaration2866 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_propertyValue2890 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio3002 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_prio3004 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expression3026 = new BitSet(new long[]{0x00066000001440E2L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_operator_in_expression3029 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_term_in_expression3031 = new BitSet(new long[]{0x00066000001440E2L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_unaryOperator_in_term3054 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_set_in_term3075 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_STRING_in_term3274 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_IDENT_in_term3282 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_GEN_in_term3290 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_URI_in_term3298 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_hexColor_in_term3306 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_function_in_term3314 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_term3326 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_function3342 = new BitSet(new long[]{0x0000000000100000L,0x000000000000C001L});
    public static final BitSet FOLLOW_ws_in_function3344 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_LPAREN_in_function3349 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_ws_in_function3351 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_expression_in_function3362 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_fnAttribute_in_function3380 = new BitSet(new long[]{0x0000000000004000L,0x0000000000000002L});
    public static final BitSet FOLLOW_COMMA_in_function3383 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_ws_in_function3385 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_fnAttribute_in_function3388 = new BitSet(new long[]{0x0000000000004000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RPAREN_in_function3409 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_functionName3457 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_COLON_in_functionName3459 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_functionName3463 = new BitSet(new long[]{0x0008000000000002L});
    public static final BitSet FOLLOW_DOT_in_functionName3466 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_functionName3468 = new BitSet(new long[]{0x0008000000000002L});
    public static final BitSet FOLLOW_fnAttributeName_in_fnAttribute3490 = new BitSet(new long[]{0x0200000000100000L,0x000000000000C000L});
    public static final BitSet FOLLOW_ws_in_fnAttribute3492 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_OPEQ_in_fnAttribute3495 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_ws_in_fnAttribute3497 = new BitSet(new long[]{0x00064000001400E0L,0x000000000000FFF8L});
    public static final BitSet FOLLOW_fnAttributeValue_in_fnAttribute3500 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName3515 = new BitSet(new long[]{0x0008000000000002L});
    public static final BitSet FOLLOW_DOT_in_fnAttributeName3518 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName3520 = new BitSet(new long[]{0x0008000000000002L});
    public static final BitSet FOLLOW_expression_in_fnAttributeValue3534 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor3552 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ws3573 = new BitSet(new long[]{0x0000000000100002L,0x000000000000C000L});
    public static final BitSet FOLLOW_esPred_in_synpred1_Css31788 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred2_Css31806 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nsPred_in_synpred3_Css31908 = new BitSet(new long[]{0x0000000000000002L});

}