// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g 2013-01-16 11:41:31

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "NAMESPACE_SYM", "IDENT", "STRING", "URI", "CHARSET_SYM", "SEMI", "IMPORT_SYM", "MEDIA_SYM", "LBRACE", "RBRACE", "COMMA", "AND", "ONLY", "NOT", "GEN", "AT_IDENT", "WS", "MOZ_DOCUMENT_SYM", "MOZ_URL_PREFIX", "MOZ_DOMAIN", "MOZ_REGEXP", "WEBKIT_KEYFRAMES_SYM", "PERCENTAGE", "PAGE_SYM", "COUNTER_STYLE_SYM", "FONT_FACE_SYM", "TOPLEFTCORNER_SYM", "TOPLEFT_SYM", "TOPCENTER_SYM", "TOPRIGHT_SYM", "TOPRIGHTCORNER_SYM", "BOTTOMLEFTCORNER_SYM", "BOTTOMLEFT_SYM", "BOTTOMCENTER_SYM", "BOTTOMRIGHT_SYM", "BOTTOMRIGHTCORNER_SYM", "LEFTTOP_SYM", "LEFTMIDDLE_SYM", "LEFTBOTTOM_SYM", "RIGHTTOP_SYM", "RIGHTMIDDLE_SYM", "RIGHTBOTTOM_SYM", "COLON", "SOLIDUS", "PLUS", "GREATER", "TILDE", "MINUS", "HASH", "DOT", "LBRACKET", "DCOLON", "STAR", "PIPE", "NAME", "OPEQ", "INCLUDES", "DASHMATCH", "BEGINS", "ENDS", "CONTAINS", "RBRACKET", "LPAREN", "RPAREN", "IMPORTANT_SYM", "NUMBER", "LENGTH", "EMS", "REM", "EXS", "ANGLE", "TIME", "FREQ", "RESOLUTION", "DIMENSION", "NL", "COMMENT", "HEXCHAR", "NONASCII", "UNICODE", "ESCAPE", "NMSTART", "NMCHAR", "URL", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "CDO", "CDC", "INVALID", "'#'"
    };
    public static final int EOF=-1;
    public static final int T__117=117;
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
    public static final int AT_IDENT=19;
    public static final int WS=20;
    public static final int MOZ_DOCUMENT_SYM=21;
    public static final int MOZ_URL_PREFIX=22;
    public static final int MOZ_DOMAIN=23;
    public static final int MOZ_REGEXP=24;
    public static final int WEBKIT_KEYFRAMES_SYM=25;
    public static final int PERCENTAGE=26;
    public static final int PAGE_SYM=27;
    public static final int COUNTER_STYLE_SYM=28;
    public static final int FONT_FACE_SYM=29;
    public static final int TOPLEFTCORNER_SYM=30;
    public static final int TOPLEFT_SYM=31;
    public static final int TOPCENTER_SYM=32;
    public static final int TOPRIGHT_SYM=33;
    public static final int TOPRIGHTCORNER_SYM=34;
    public static final int BOTTOMLEFTCORNER_SYM=35;
    public static final int BOTTOMLEFT_SYM=36;
    public static final int BOTTOMCENTER_SYM=37;
    public static final int BOTTOMRIGHT_SYM=38;
    public static final int BOTTOMRIGHTCORNER_SYM=39;
    public static final int LEFTTOP_SYM=40;
    public static final int LEFTMIDDLE_SYM=41;
    public static final int LEFTBOTTOM_SYM=42;
    public static final int RIGHTTOP_SYM=43;
    public static final int RIGHTMIDDLE_SYM=44;
    public static final int RIGHTBOTTOM_SYM=45;
    public static final int COLON=46;
    public static final int SOLIDUS=47;
    public static final int PLUS=48;
    public static final int GREATER=49;
    public static final int TILDE=50;
    public static final int MINUS=51;
    public static final int HASH=52;
    public static final int DOT=53;
    public static final int LBRACKET=54;
    public static final int DCOLON=55;
    public static final int STAR=56;
    public static final int PIPE=57;
    public static final int NAME=58;
    public static final int OPEQ=59;
    public static final int INCLUDES=60;
    public static final int DASHMATCH=61;
    public static final int BEGINS=62;
    public static final int ENDS=63;
    public static final int CONTAINS=64;
    public static final int RBRACKET=65;
    public static final int LPAREN=66;
    public static final int RPAREN=67;
    public static final int IMPORTANT_SYM=68;
    public static final int NUMBER=69;
    public static final int LENGTH=70;
    public static final int EMS=71;
    public static final int REM=72;
    public static final int EXS=73;
    public static final int ANGLE=74;
    public static final int TIME=75;
    public static final int FREQ=76;
    public static final int RESOLUTION=77;
    public static final int DIMENSION=78;
    public static final int NL=79;
    public static final int COMMENT=80;
    public static final int HEXCHAR=81;
    public static final int NONASCII=82;
    public static final int UNICODE=83;
    public static final int ESCAPE=84;
    public static final int NMSTART=85;
    public static final int NMCHAR=86;
    public static final int URL=87;
    public static final int A=88;
    public static final int B=89;
    public static final int C=90;
    public static final int D=91;
    public static final int E=92;
    public static final int F=93;
    public static final int G=94;
    public static final int H=95;
    public static final int I=96;
    public static final int J=97;
    public static final int K=98;
    public static final int L=99;
    public static final int M=100;
    public static final int N=101;
    public static final int O=102;
    public static final int P=103;
    public static final int Q=104;
    public static final int R=105;
    public static final int S=106;
    public static final int T=107;
    public static final int U=108;
    public static final int V=109;
    public static final int W=110;
    public static final int X=111;
    public static final int Y=112;
    public static final int Z=113;
    public static final int CDO=114;
    public static final int CDC=115;
    public static final int INVALID=116;

    // delegates
    // delegators

    public static final String[] ruleNames = new String[] {
        "invalidRule", "prio", "mediaQuery", "synpred1_Css3", "syncTo_RBRACE", 
        "synpred3_Css3", "webkitKeyframes", "operator", "declarations", 
        "less_variable_declaration", "propertyValue", "fnAttribute", "less_variable", 
        "unaryOperator", "atRuleId", "slAttributeName", "mediaExpression", 
        "fnAttributeValue", "declaration", "pseudo", "importItem", "term", 
        "body", "elementSubsequent", "functionName", "synpred2_Css3", "counterStyle", 
        "slAttribute", "ws", "mediaQueryOperator", "typeSelector", "nsPred", 
        "syncToDeclarationsRule", "namespaces", "slAttributeValue", "mediaQueryList", 
        "property", "namespace", "webkitKeyframeSelectors", "page", "resourceIdentifier", 
        "selector", "simpleSelectorSequence", "namespacePrefix", "namespacePrefixName", 
        "media", "webkitKeyframesBlock", "charSetValue", "pseudoPage", "moz_document", 
        "fontFace", "hexColor", "selectorsGroup", "styleSheet", "margin", 
        "rule", "cssId", "expression", "fnAttributeName", "esPred", "vendorAtRule", 
        "mediaFeature", "combinator", "charSet", "bodyItem", "moz_document_function", 
        "function", "mediaType", "cssClass", "margin_sym", "imports", "generic_at_rule", 
        "syncToFollow", "elementName"
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
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, true, true, 
            false, true, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false, false, 
            false, false, false, false, false, false, false, false
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

            if ( (LA6_0==IDENT||LA6_0==MEDIA_SYM||(LA6_0>=GEN && LA6_0<=AT_IDENT)||LA6_0==MOZ_DOCUMENT_SYM||LA6_0==WEBKIT_KEYFRAMES_SYM||(LA6_0>=PAGE_SYM && LA6_0<=FONT_FACE_SYM)||LA6_0==COLON||(LA6_0>=HASH && LA6_0<=PIPE)||LA6_0==117) ) {
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:347:1: media : MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page | fontFace | vendorAtRule ) ( ws )? )* RBRACE ;
    public final void media() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "media");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(347, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:5: ( MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page | fontFace | vendorAtRule ) ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:348:7: MEDIA_SYM ( ws )? mediaQueryList LBRACE ( ws )? ( ( rule | page | fontFace | vendorAtRule ) ( ws )? )* RBRACE
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:13: ( ( rule | page | fontFace | vendorAtRule ) ( ws )? )*
            try { dbg.enterSubRule(23);

            loop23:
            do {
                int alt23=2;
                try { dbg.enterDecision(23, decisionCanBacktrack[23]);

                int LA23_0 = input.LA(1);

                if ( (LA23_0==IDENT||(LA23_0>=GEN && LA23_0<=AT_IDENT)||LA23_0==MOZ_DOCUMENT_SYM||LA23_0==WEBKIT_KEYFRAMES_SYM||LA23_0==PAGE_SYM||LA23_0==FONT_FACE_SYM||LA23_0==COLON||(LA23_0>=HASH && LA23_0<=PIPE)||LA23_0==117) ) {
                    alt23=1;
                }


                } finally {dbg.exitDecision(23);}

                switch (alt23) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:15: ( rule | page | fontFace | vendorAtRule ) ( ws )?
            	    {
            	    dbg.location(350,15);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:15: ( rule | page | fontFace | vendorAtRule )
            	    int alt21=4;
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
            	    case 117:
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
            	    case AT_IDENT:
            	    case MOZ_DOCUMENT_SYM:
            	    case WEBKIT_KEYFRAMES_SYM:
            	        {
            	        alt21=4;
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
            	        case 4 :
            	            dbg.enterAlt(4);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:42: vendorAtRule
            	            {
            	            dbg.location(350,42);
            	            pushFollow(FOLLOW_vendorAtRule_in_media423);
            	            vendorAtRule();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(21);}

            	    dbg.location(350,57);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:57: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:350:57: ws
            	            {
            	            dbg.location(350,57);
            	            pushFollow(FOLLOW_ws_in_media427);
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
            match(input,RBRACE,FOLLOW_RBRACE_in_media441); if (state.failed) return ;

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
                    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList457);
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
                    	    match(input,COMMA,FOLLOW_COMMA_in_mediaQueryList461); if (state.failed) return ;
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
                    	            pushFollow(FOLLOW_ws_in_mediaQueryList463);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(24);}

                    	    dbg.location(355,29);
                    	    pushFollow(FOLLOW_mediaQuery_in_mediaQueryList466);
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
                            pushFollow(FOLLOW_mediaQueryOperator_in_mediaQuery485);
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
                                    pushFollow(FOLLOW_ws_in_mediaQuery487);
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
                    pushFollow(FOLLOW_mediaType_in_mediaQuery494);
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
                            pushFollow(FOLLOW_ws_in_mediaQuery496);
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
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery501); if (state.failed) return ;
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
                    	            pushFollow(FOLLOW_ws_in_mediaQuery503);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(30);}

                    	    dbg.location(359,56);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery506);
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
                    pushFollow(FOLLOW_mediaExpression_in_mediaQuery514);
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
                    	    match(input,AND,FOLLOW_AND_in_mediaQuery518); if (state.failed) return ;
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
                    	            pushFollow(FOLLOW_ws_in_mediaQuery520);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(32);}

                    	    dbg.location(360,30);
                    	    pushFollow(FOLLOW_mediaExpression_in_mediaQuery523);
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
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:5: ( '(' ( ws )? mediaFeature ( ws )? ( ':' ( ws )? expression )? ')' ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:7: '(' ( ws )? mediaFeature ( ws )? ( ':' ( ws )? expression )? ')' ( ws )?
            {
            dbg.location(372,7);
            match(input,LPAREN,FOLLOW_LPAREN_in_mediaExpression578); if (state.failed) return ;
            dbg.location(372,11);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:11: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:11: ws
                    {
                    dbg.location(372,11);
                    pushFollow(FOLLOW_ws_in_mediaExpression580);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(35);}

            dbg.location(372,15);
            pushFollow(FOLLOW_mediaFeature_in_mediaExpression583);
            mediaFeature();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(372,28);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:28: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:28: ws
                    {
                    dbg.location(372,28);
                    pushFollow(FOLLOW_ws_in_mediaExpression585);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(36);}

            dbg.location(372,32);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:32: ( ':' ( ws )? expression )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:34: ':' ( ws )? expression
                    {
                    dbg.location(372,34);
                    match(input,COLON,FOLLOW_COLON_in_mediaExpression590); if (state.failed) return ;
                    dbg.location(372,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:38: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:38: ws
                            {
                            dbg.location(372,38);
                            pushFollow(FOLLOW_ws_in_mediaExpression592);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(37);}

                    dbg.location(372,42);
                    pushFollow(FOLLOW_expression_in_mediaExpression595);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(38);}

            dbg.location(372,56);
            match(input,RPAREN,FOLLOW_RPAREN_in_mediaExpression600); if (state.failed) return ;
            dbg.location(372,60);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:60: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:372:60: ws
                    {
                    dbg.location(372,60);
                    pushFollow(FOLLOW_ws_in_mediaExpression602);
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
        dbg.location(373, 5);

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
            match(input,IDENT,FOLLOW_IDENT_in_mediaFeature618); if (state.failed) return ;

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

                if ( (LA41_0==IDENT||LA41_0==MEDIA_SYM||(LA41_0>=GEN && LA41_0<=AT_IDENT)||LA41_0==MOZ_DOCUMENT_SYM||LA41_0==WEBKIT_KEYFRAMES_SYM||(LA41_0>=PAGE_SYM && LA41_0<=FONT_FACE_SYM)||LA41_0==COLON||(LA41_0>=HASH && LA41_0<=PIPE)||LA41_0==117) ) {
                    alt41=1;
                }


                } finally {dbg.exitDecision(41);}

                switch (alt41) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:380:4: bodyItem ( ws )?
            	    {
            	    dbg.location(380,4);
            	    pushFollow(FOLLOW_bodyItem_in_body634);
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
            	            pushFollow(FOLLOW_ws_in_body636);
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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:383:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule | less_variable_declaration );
    public final void bodyItem() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "bodyItem");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(383, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:384:5: ( rule | media | page | counterStyle | fontFace | vendorAtRule | less_variable_declaration )
            int alt42=7;
            try { dbg.enterDecision(42, decisionCanBacktrack[42]);

            try {
                isCyclicDecision = true;
                alt42 = dfa42.predict(input);
            }
            catch (NoViableAltException nvae) {
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
                    pushFollow(FOLLOW_rule_in_bodyItem661);
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
                    pushFollow(FOLLOW_media_in_bodyItem673);
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
                    pushFollow(FOLLOW_page_in_bodyItem685);
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
                    pushFollow(FOLLOW_counterStyle_in_bodyItem697);
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
                    pushFollow(FOLLOW_fontFace_in_bodyItem709);
                    fontFace();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:390:11: vendorAtRule
                    {
                    dbg.location(390,11);
                    pushFollow(FOLLOW_vendorAtRule_in_bodyItem721);
                    vendorAtRule();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:391:11: less_variable_declaration
                    {
                    dbg.location(391,11);
                    pushFollow(FOLLOW_less_variable_declaration_in_bodyItem733);
                    less_variable_declaration();

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


    // $ANTLR start "vendorAtRule"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:400:1: vendorAtRule : ( moz_document | webkitKeyframes | generic_at_rule );
    public final void vendorAtRule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "vendorAtRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(400, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:1: ( moz_document | webkitKeyframes | generic_at_rule )
            int alt43=3;
            try { dbg.enterDecision(43, decisionCanBacktrack[43]);

            switch ( input.LA(1) ) {
            case MOZ_DOCUMENT_SYM:
                {
                alt43=1;
                }
                break;
            case WEBKIT_KEYFRAMES_SYM:
                {
                alt43=2;
                }
                break;
            case AT_IDENT:
                {
                alt43=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(43);}

            switch (alt43) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:3: moz_document
                    {
                    dbg.location(401,3);
                    pushFollow(FOLLOW_moz_document_in_vendorAtRule756);
                    moz_document();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:18: webkitKeyframes
                    {
                    dbg.location(401,18);
                    pushFollow(FOLLOW_webkitKeyframes_in_vendorAtRule760);
                    webkitKeyframes();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:401:36: generic_at_rule
                    {
                    dbg.location(401,36);
                    pushFollow(FOLLOW_generic_at_rule_in_vendorAtRule764);
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
        dbg.location(401, 51);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "vendorAtRule");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "vendorAtRule"


    // $ANTLR start "atRuleId"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:403:1: atRuleId : ( IDENT | STRING );
    public final void atRuleId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "atRuleId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(403, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:404:2: ( IDENT | STRING )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(404,2);
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
        dbg.location(406, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "atRuleId");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "atRuleId"


    // $ANTLR start "generic_at_rule"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:408:1: generic_at_rule : AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE ;
    public final void generic_at_rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "generic_at_rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(408, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:5: ( AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:7: AT_IDENT ( WS )* ( atRuleId ( WS )* )? LBRACE syncTo_RBRACE RBRACE
            {
            dbg.location(409,7);
            match(input,AT_IDENT,FOLLOW_AT_IDENT_in_generic_at_rule800); if (state.failed) return ;
            dbg.location(409,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:16: ( WS )*
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

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:16: WS
            	    {
            	    dbg.location(409,16);
            	    match(input,WS,FOLLOW_WS_in_generic_at_rule802); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop44;
                }
            } while (true);
            } finally {dbg.exitSubRule(44);}

            dbg.location(409,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:20: ( atRuleId ( WS )* )?
            int alt46=2;
            try { dbg.enterSubRule(46);
            try { dbg.enterDecision(46, decisionCanBacktrack[46]);

            int LA46_0 = input.LA(1);

            if ( ((LA46_0>=IDENT && LA46_0<=STRING)) ) {
                alt46=1;
            }
            } finally {dbg.exitDecision(46);}

            switch (alt46) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:22: atRuleId ( WS )*
                    {
                    dbg.location(409,22);
                    pushFollow(FOLLOW_atRuleId_in_generic_at_rule807);
                    atRuleId();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(409,31);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:31: ( WS )*
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

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:409:31: WS
                    	    {
                    	    dbg.location(409,31);
                    	    match(input,WS,FOLLOW_WS_in_generic_at_rule809); if (state.failed) return ;

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

            dbg.location(410,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_generic_at_rule824); if (state.failed) return ;
            dbg.location(411,10);
            pushFollow(FOLLOW_syncTo_RBRACE_in_generic_at_rule836);
            syncTo_RBRACE();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(412,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_generic_at_rule846); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "generic_at_rule");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "generic_at_rule"


    // $ANTLR start "moz_document"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:414:1: moz_document : MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE ;
    public final void moz_document() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(414, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:415:2: ( MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:2: MOZ_DOCUMENT_SYM ( ws )? ( moz_document_function ( ws )? ) ( COMMA ( ws )? moz_document_function ( ws )? )* LBRACE ( ws )? ( body )? RBRACE
            {
            dbg.location(416,2);
            match(input,MOZ_DOCUMENT_SYM,FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document862); if (state.failed) return ;
            dbg.location(416,19);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:19: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:19: ws
                    {
                    dbg.location(416,19);
                    pushFollow(FOLLOW_ws_in_moz_document864);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(47);}

            dbg.location(416,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:23: ( moz_document_function ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:25: moz_document_function ( ws )?
            {
            dbg.location(416,25);
            pushFollow(FOLLOW_moz_document_function_in_moz_document869);
            moz_document_function();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(416,47);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:47: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:47: ws
                    {
                    dbg.location(416,47);
                    pushFollow(FOLLOW_ws_in_moz_document871);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(48);}


            }

            dbg.location(416,52);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:52: ( COMMA ( ws )? moz_document_function ( ws )? )*
            try { dbg.enterSubRule(51);

            loop51:
            do {
                int alt51=2;
                try { dbg.enterDecision(51, decisionCanBacktrack[51]);

                int LA51_0 = input.LA(1);

                if ( (LA51_0==COMMA) ) {
                    alt51=1;
                }


                } finally {dbg.exitDecision(51);}

                switch (alt51) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:54: COMMA ( ws )? moz_document_function ( ws )?
            	    {
            	    dbg.location(416,54);
            	    match(input,COMMA,FOLLOW_COMMA_in_moz_document877); if (state.failed) return ;
            	    dbg.location(416,60);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:60: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:60: ws
            	            {
            	            dbg.location(416,60);
            	            pushFollow(FOLLOW_ws_in_moz_document879);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(49);}

            	    dbg.location(416,64);
            	    pushFollow(FOLLOW_moz_document_function_in_moz_document882);
            	    moz_document_function();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(416,86);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:86: ( ws )?
            	    int alt50=2;
            	    try { dbg.enterSubRule(50);
            	    try { dbg.enterDecision(50, decisionCanBacktrack[50]);

            	    int LA50_0 = input.LA(1);

            	    if ( (LA50_0==WS||(LA50_0>=NL && LA50_0<=COMMENT)) ) {
            	        alt50=1;
            	    }
            	    } finally {dbg.exitDecision(50);}

            	    switch (alt50) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:416:86: ws
            	            {
            	            dbg.location(416,86);
            	            pushFollow(FOLLOW_ws_in_moz_document884);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(50);}


            	    }
            	    break;

            	default :
            	    break loop51;
                }
            } while (true);
            } finally {dbg.exitSubRule(51);}

            dbg.location(417,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_moz_document891); if (state.failed) return ;
            dbg.location(417,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:9: ( ws )?
            int alt52=2;
            try { dbg.enterSubRule(52);
            try { dbg.enterDecision(52, decisionCanBacktrack[52]);

            int LA52_0 = input.LA(1);

            if ( (LA52_0==WS||(LA52_0>=NL && LA52_0<=COMMENT)) ) {
                alt52=1;
            }
            } finally {dbg.exitDecision(52);}

            switch (alt52) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:417:9: ws
                    {
                    dbg.location(417,9);
                    pushFollow(FOLLOW_ws_in_moz_document893);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(52);}

            dbg.location(418,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:3: ( body )?
            int alt53=2;
            try { dbg.enterSubRule(53);
            try { dbg.enterDecision(53, decisionCanBacktrack[53]);

            int LA53_0 = input.LA(1);

            if ( (LA53_0==IDENT||LA53_0==MEDIA_SYM||(LA53_0>=GEN && LA53_0<=AT_IDENT)||LA53_0==MOZ_DOCUMENT_SYM||LA53_0==WEBKIT_KEYFRAMES_SYM||(LA53_0>=PAGE_SYM && LA53_0<=FONT_FACE_SYM)||LA53_0==COLON||(LA53_0>=HASH && LA53_0<=PIPE)||LA53_0==117) ) {
                alt53=1;
            }
            } finally {dbg.exitDecision(53);}

            switch (alt53) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:418:3: body
                    {
                    dbg.location(418,3);
                    pushFollow(FOLLOW_body_in_moz_document898);
                    body();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(53);}

            dbg.location(419,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_moz_document903); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "moz_document");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "moz_document"


    // $ANTLR start "moz_document_function"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:422:1: moz_document_function : ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP );
    public final void moz_document_function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "moz_document_function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(422, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:423:2: ( URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(423,2);
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
        dbg.location(425, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "moz_document_function");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "moz_document_function"


    // $ANTLR start "webkitKeyframes"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:428:1: webkitKeyframes : WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE ;
    public final void webkitKeyframes() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframes");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(428, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:429:2: ( WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:2: WEBKIT_KEYFRAMES_SYM ( ws )? atRuleId ( ws )? LBRACE ( ws )? ( webkitKeyframesBlock ( ws )? )* RBRACE
            {
            dbg.location(430,2);
            match(input,WEBKIT_KEYFRAMES_SYM,FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes944); if (state.failed) return ;
            dbg.location(430,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:23: ( ws )?
            int alt54=2;
            try { dbg.enterSubRule(54);
            try { dbg.enterDecision(54, decisionCanBacktrack[54]);

            int LA54_0 = input.LA(1);

            if ( (LA54_0==WS||(LA54_0>=NL && LA54_0<=COMMENT)) ) {
                alt54=1;
            }
            } finally {dbg.exitDecision(54);}

            switch (alt54) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:23: ws
                    {
                    dbg.location(430,23);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes946);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(54);}

            dbg.location(430,27);
            pushFollow(FOLLOW_atRuleId_in_webkitKeyframes949);
            atRuleId();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(430,36);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:36: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:430:36: ws
                    {
                    dbg.location(430,36);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes951);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(55);}

            dbg.location(431,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframes956); if (state.failed) return ;
            dbg.location(431,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:9: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:431:9: ws
                    {
                    dbg.location(431,9);
                    pushFollow(FOLLOW_ws_in_webkitKeyframes958);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(56);}

            dbg.location(432,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:3: ( webkitKeyframesBlock ( ws )? )*
            try { dbg.enterSubRule(58);

            loop58:
            do {
                int alt58=2;
                try { dbg.enterDecision(58, decisionCanBacktrack[58]);

                int LA58_0 = input.LA(1);

                if ( (LA58_0==IDENT||LA58_0==PERCENTAGE) ) {
                    alt58=1;
                }


                } finally {dbg.exitDecision(58);}

                switch (alt58) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:5: webkitKeyframesBlock ( ws )?
            	    {
            	    dbg.location(432,5);
            	    pushFollow(FOLLOW_webkitKeyframesBlock_in_webkitKeyframes965);
            	    webkitKeyframesBlock();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(432,26);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:26: ( ws )?
            	    int alt57=2;
            	    try { dbg.enterSubRule(57);
            	    try { dbg.enterDecision(57, decisionCanBacktrack[57]);

            	    int LA57_0 = input.LA(1);

            	    if ( (LA57_0==WS||(LA57_0>=NL && LA57_0<=COMMENT)) ) {
            	        alt57=1;
            	    }
            	    } finally {dbg.exitDecision(57);}

            	    switch (alt57) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:432:26: ws
            	            {
            	            dbg.location(432,26);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframes967);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(57);}


            	    }
            	    break;

            	default :
            	    break loop58;
                }
            } while (true);
            } finally {dbg.exitSubRule(58);}

            dbg.location(433,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframes974); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "webkitKeyframes");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "webkitKeyframes"


    // $ANTLR start "webkitKeyframesBlock"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:436:1: webkitKeyframesBlock : webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void webkitKeyframesBlock() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframesBlock");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(436, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:437:2: ( webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:2: webkitKeyframeSelectors ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(438,2);
            pushFollow(FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock987);
            webkitKeyframeSelectors();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(438,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:26: ( ws )?
            int alt59=2;
            try { dbg.enterSubRule(59);
            try { dbg.enterDecision(59, decisionCanBacktrack[59]);

            int LA59_0 = input.LA(1);

            if ( (LA59_0==WS||(LA59_0>=NL && LA59_0<=COMMENT)) ) {
                alt59=1;
            }
            } finally {dbg.exitDecision(59);}

            switch (alt59) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:438:26: ws
                    {
                    dbg.location(438,26);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock989);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(59);}

            dbg.location(439,2);
            match(input,LBRACE,FOLLOW_LBRACE_in_webkitKeyframesBlock993); if (state.failed) return ;
            dbg.location(439,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:10: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:439:10: ws
                    {
                    dbg.location(439,10);
                    pushFollow(FOLLOW_ws_in_webkitKeyframesBlock996);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(60);}

            dbg.location(439,14);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_webkitKeyframesBlock999);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(440,3);
            pushFollow(FOLLOW_declarations_in_webkitKeyframesBlock1003);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(441,2);
            match(input,RBRACE,FOLLOW_RBRACE_in_webkitKeyframesBlock1006); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(442, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "webkitKeyframesBlock");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "webkitKeyframesBlock"


    // $ANTLR start "webkitKeyframeSelectors"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:444:1: webkitKeyframeSelectors : ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* ;
    public final void webkitKeyframeSelectors() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "webkitKeyframeSelectors");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(444, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:445:2: ( ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:2: ( IDENT | PERCENTAGE ) ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
            {
            dbg.location(446,2);
            if ( input.LA(1)==IDENT||input.LA(1)==PERCENTAGE ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                dbg.recognitionException(mse);
                throw mse;
            }

            dbg.location(446,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*
            try { dbg.enterSubRule(63);

            loop63:
            do {
                int alt63=2;
                try { dbg.enterDecision(63, decisionCanBacktrack[63]);

                try {
                    isCyclicDecision = true;
                    alt63 = dfa63.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(63);}

                switch (alt63) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:27: ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE )
            	    {
            	    dbg.location(446,27);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:27: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:27: ws
            	            {
            	            dbg.location(446,27);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1033);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(61);}

            	    dbg.location(446,31);
            	    match(input,COMMA,FOLLOW_COMMA_in_webkitKeyframeSelectors1036); if (state.failed) return ;
            	    dbg.location(446,37);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:37: ( ws )?
            	    int alt62=2;
            	    try { dbg.enterSubRule(62);
            	    try { dbg.enterDecision(62, decisionCanBacktrack[62]);

            	    int LA62_0 = input.LA(1);

            	    if ( (LA62_0==WS||(LA62_0>=NL && LA62_0<=COMMENT)) ) {
            	        alt62=1;
            	    }
            	    } finally {dbg.exitDecision(62);}

            	    switch (alt62) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:446:37: ws
            	            {
            	            dbg.location(446,37);
            	            pushFollow(FOLLOW_ws_in_webkitKeyframeSelectors1038);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(62);}

            	    dbg.location(446,41);
            	    if ( input.LA(1)==IDENT||input.LA(1)==PERCENTAGE ) {
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
            	    break loop63;
                }
            } while (true);
            } finally {dbg.exitSubRule(63);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(447, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "webkitKeyframeSelectors");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "webkitKeyframeSelectors"


    // $ANTLR start "page"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:449:1: page : PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE ;
    public final void page() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "page");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(449, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:5: ( PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:7: PAGE_SYM ( ws )? ( IDENT ( ws )? )? ( pseudoPage ( ws )? )? LBRACE ( ws )? ( declaration | margin ( ws )? )? ( SEMI ( ws )? ( declaration | margin ( ws )? )? )* RBRACE
            {
            dbg.location(450,7);
            match(input,PAGE_SYM,FOLLOW_PAGE_SYM_in_page1070); if (state.failed) return ;
            dbg.location(450,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:16: ws
                    {
                    dbg.location(450,16);
                    pushFollow(FOLLOW_ws_in_page1072);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(64);}

            dbg.location(450,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:20: ( IDENT ( ws )? )?
            int alt66=2;
            try { dbg.enterSubRule(66);
            try { dbg.enterDecision(66, decisionCanBacktrack[66]);

            int LA66_0 = input.LA(1);

            if ( (LA66_0==IDENT) ) {
                alt66=1;
            }
            } finally {dbg.exitDecision(66);}

            switch (alt66) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:22: IDENT ( ws )?
                    {
                    dbg.location(450,22);
                    match(input,IDENT,FOLLOW_IDENT_in_page1077); if (state.failed) return ;
                    dbg.location(450,28);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:28: ( ws )?
                    int alt65=2;
                    try { dbg.enterSubRule(65);
                    try { dbg.enterDecision(65, decisionCanBacktrack[65]);

                    int LA65_0 = input.LA(1);

                    if ( (LA65_0==WS||(LA65_0>=NL && LA65_0<=COMMENT)) ) {
                        alt65=1;
                    }
                    } finally {dbg.exitDecision(65);}

                    switch (alt65) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:28: ws
                            {
                            dbg.location(450,28);
                            pushFollow(FOLLOW_ws_in_page1079);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(65);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(66);}

            dbg.location(450,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:35: ( pseudoPage ( ws )? )?
            int alt68=2;
            try { dbg.enterSubRule(68);
            try { dbg.enterDecision(68, decisionCanBacktrack[68]);

            int LA68_0 = input.LA(1);

            if ( (LA68_0==COLON) ) {
                alt68=1;
            }
            } finally {dbg.exitDecision(68);}

            switch (alt68) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:36: pseudoPage ( ws )?
                    {
                    dbg.location(450,36);
                    pushFollow(FOLLOW_pseudoPage_in_page1086);
                    pseudoPage();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(450,47);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:47: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:450:47: ws
                            {
                            dbg.location(450,47);
                            pushFollow(FOLLOW_ws_in_page1088);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(67);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(68);}

            dbg.location(451,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_page1101); if (state.failed) return ;
            dbg.location(451,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:451:16: ws
                    {
                    dbg.location(451,16);
                    pushFollow(FOLLOW_ws_in_page1103);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(69);}

            dbg.location(455,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:13: ( declaration | margin ( ws )? )?
            int alt71=3;
            try { dbg.enterSubRule(71);
            try { dbg.enterDecision(71, decisionCanBacktrack[71]);

            int LA71_0 = input.LA(1);

            if ( (LA71_0==IDENT||LA71_0==GEN||LA71_0==STAR) ) {
                alt71=1;
            }
            else if ( ((LA71_0>=TOPLEFTCORNER_SYM && LA71_0<=RIGHTBOTTOM_SYM)) ) {
                alt71=2;
            }
            } finally {dbg.exitDecision(71);}

            switch (alt71) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:14: declaration
                    {
                    dbg.location(455,14);
                    pushFollow(FOLLOW_declaration_in_page1158);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:26: margin ( ws )?
                    {
                    dbg.location(455,26);
                    pushFollow(FOLLOW_margin_in_page1160);
                    margin();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(455,33);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:33: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:33: ws
                            {
                            dbg.location(455,33);
                            pushFollow(FOLLOW_ws_in_page1162);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(70);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(71);}

            dbg.location(455,39);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:39: ( SEMI ( ws )? ( declaration | margin ( ws )? )? )*
            try { dbg.enterSubRule(75);

            loop75:
            do {
                int alt75=2;
                try { dbg.enterDecision(75, decisionCanBacktrack[75]);

                int LA75_0 = input.LA(1);

                if ( (LA75_0==SEMI) ) {
                    alt75=1;
                }


                } finally {dbg.exitDecision(75);}

                switch (alt75) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:40: SEMI ( ws )? ( declaration | margin ( ws )? )?
            	    {
            	    dbg.location(455,40);
            	    match(input,SEMI,FOLLOW_SEMI_in_page1168); if (state.failed) return ;
            	    dbg.location(455,45);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:45: ( ws )?
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

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:45: ws
            	            {
            	            dbg.location(455,45);
            	            pushFollow(FOLLOW_ws_in_page1170);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(72);}

            	    dbg.location(455,49);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:49: ( declaration | margin ( ws )? )?
            	    int alt74=3;
            	    try { dbg.enterSubRule(74);
            	    try { dbg.enterDecision(74, decisionCanBacktrack[74]);

            	    int LA74_0 = input.LA(1);

            	    if ( (LA74_0==IDENT||LA74_0==GEN||LA74_0==STAR) ) {
            	        alt74=1;
            	    }
            	    else if ( ((LA74_0>=TOPLEFTCORNER_SYM && LA74_0<=RIGHTBOTTOM_SYM)) ) {
            	        alt74=2;
            	    }
            	    } finally {dbg.exitDecision(74);}

            	    switch (alt74) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:50: declaration
            	            {
            	            dbg.location(455,50);
            	            pushFollow(FOLLOW_declaration_in_page1174);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;
            	        case 2 :
            	            dbg.enterAlt(2);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:62: margin ( ws )?
            	            {
            	            dbg.location(455,62);
            	            pushFollow(FOLLOW_margin_in_page1176);
            	            margin();

            	            state._fsp--;
            	            if (state.failed) return ;
            	            dbg.location(455,69);
            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:69: ( ws )?
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

            	                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:455:69: ws
            	                    {
            	                    dbg.location(455,69);
            	                    pushFollow(FOLLOW_ws_in_page1178);
            	                    ws();

            	                    state._fsp--;
            	                    if (state.failed) return ;

            	                    }
            	                    break;

            	            }
            	            } finally {dbg.exitSubRule(73);}


            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(74);}


            	    }
            	    break;

            	default :
            	    break loop75;
                }
            } while (true);
            } finally {dbg.exitSubRule(75);}

            dbg.location(456,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_page1193); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "page");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "page"


    // $ANTLR start "counterStyle"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:459:1: counterStyle : COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void counterStyle() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "counterStyle");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(459, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:5: ( COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:7: COUNTER_STYLE_SYM ( ws )? IDENT ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(460,7);
            match(input,COUNTER_STYLE_SYM,FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1214); if (state.failed) return ;
            dbg.location(460,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:25: ( ws )?
            int alt76=2;
            try { dbg.enterSubRule(76);
            try { dbg.enterDecision(76, decisionCanBacktrack[76]);

            int LA76_0 = input.LA(1);

            if ( (LA76_0==WS||(LA76_0>=NL && LA76_0<=COMMENT)) ) {
                alt76=1;
            }
            } finally {dbg.exitDecision(76);}

            switch (alt76) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:25: ws
                    {
                    dbg.location(460,25);
                    pushFollow(FOLLOW_ws_in_counterStyle1216);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(76);}

            dbg.location(460,29);
            match(input,IDENT,FOLLOW_IDENT_in_counterStyle1219); if (state.failed) return ;
            dbg.location(460,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:35: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:460:35: ws
                    {
                    dbg.location(460,35);
                    pushFollow(FOLLOW_ws_in_counterStyle1221);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(77);}

            dbg.location(461,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_counterStyle1232); if (state.failed) return ;
            dbg.location(461,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:16: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:461:16: ws
                    {
                    dbg.location(461,16);
                    pushFollow(FOLLOW_ws_in_counterStyle1234);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(78);}

            dbg.location(461,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_counterStyle1237);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(462,3);
            pushFollow(FOLLOW_declarations_in_counterStyle1241);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(463,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_counterStyle1251); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(464, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:466:1: fontFace : FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void fontFace() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fontFace");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(466, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:467:5: ( FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:467:7: FONT_FACE_SYM ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(467,7);
            match(input,FONT_FACE_SYM,FOLLOW_FONT_FACE_SYM_in_fontFace1272); if (state.failed) return ;
            dbg.location(467,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:467:21: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:467:21: ws
                    {
                    dbg.location(467,21);
                    pushFollow(FOLLOW_ws_in_fontFace1274);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(79);}

            dbg.location(468,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_fontFace1285); if (state.failed) return ;
            dbg.location(468,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:16: ( ws )?
            int alt80=2;
            try { dbg.enterSubRule(80);
            try { dbg.enterDecision(80, decisionCanBacktrack[80]);

            int LA80_0 = input.LA(1);

            if ( (LA80_0==WS||(LA80_0>=NL && LA80_0<=COMMENT)) ) {
                alt80=1;
            }
            } finally {dbg.exitDecision(80);}

            switch (alt80) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:468:16: ws
                    {
                    dbg.location(468,16);
                    pushFollow(FOLLOW_ws_in_fontFace1287);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(80);}

            dbg.location(468,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_fontFace1290);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(469,3);
            pushFollow(FOLLOW_declarations_in_fontFace1294);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(470,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_fontFace1304); if (state.failed) return ;

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
            dbg.exitRule(getGrammarFileName(), "fontFace");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "fontFace"


    // $ANTLR start "margin"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:473:1: margin : margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void margin() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(473, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:2: ( margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:4: margin_sym ( ws )? LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(474,4);
            pushFollow(FOLLOW_margin_sym_in_margin1319);
            margin_sym();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(474,15);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:15: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:15: ws
                    {
                    dbg.location(474,15);
                    pushFollow(FOLLOW_ws_in_margin1321);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(81);}

            dbg.location(474,19);
            match(input,LBRACE,FOLLOW_LBRACE_in_margin1324); if (state.failed) return ;
            dbg.location(474,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:26: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:474:26: ws
                    {
                    dbg.location(474,26);
                    pushFollow(FOLLOW_ws_in_margin1326);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(82);}

            dbg.location(474,30);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_margin1329);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(474,53);
            pushFollow(FOLLOW_declarations_in_margin1331);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(474,66);
            match(input,RBRACE,FOLLOW_RBRACE_in_margin1333); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(475, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:477:1: margin_sym : ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM );
    public final void margin_sym() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "margin_sym");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(477, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:478:2: ( TOPLEFTCORNER_SYM | TOPLEFT_SYM | TOPCENTER_SYM | TOPRIGHT_SYM | TOPRIGHTCORNER_SYM | BOTTOMLEFTCORNER_SYM | BOTTOMLEFT_SYM | BOTTOMCENTER_SYM | BOTTOMRIGHT_SYM | BOTTOMRIGHTCORNER_SYM | LEFTTOP_SYM | LEFTMIDDLE_SYM | LEFTBOTTOM_SYM | RIGHTTOP_SYM | RIGHTMIDDLE_SYM | RIGHTBOTTOM_SYM )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(478,2);
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
        dbg.location(495, 8);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:497:1: pseudoPage : COLON IDENT ;
    public final void pseudoPage() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudoPage");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(497, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:498:5: ( COLON IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:498:7: COLON IDENT
            {
            dbg.location(498,7);
            match(input,COLON,FOLLOW_COLON_in_pseudoPage1562); if (state.failed) return ;
            dbg.location(498,13);
            match(input,IDENT,FOLLOW_IDENT_in_pseudoPage1564); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(499, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:501:1: operator : ( SOLIDUS ( ws )? | COMMA ( ws )? | );
    public final void operator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "operator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(501, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:5: ( SOLIDUS ( ws )? | COMMA ( ws )? | )
            int alt85=3;
            try { dbg.enterDecision(85, decisionCanBacktrack[85]);

            switch ( input.LA(1) ) {
            case SOLIDUS:
                {
                alt85=1;
                }
                break;
            case COMMA:
                {
                alt85=2;
                }
                break;
            case IDENT:
            case STRING:
            case URI:
            case GEN:
            case AT_IDENT:
            case PERCENTAGE:
            case PLUS:
            case MINUS:
            case HASH:
            case NUMBER:
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
                alt85=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 85, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(85);}

            switch (alt85) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:7: SOLIDUS ( ws )?
                    {
                    dbg.location(502,7);
                    match(input,SOLIDUS,FOLLOW_SOLIDUS_in_operator1585); if (state.failed) return ;
                    dbg.location(502,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:15: ( ws )?
                    int alt83=2;
                    try { dbg.enterSubRule(83);
                    try { dbg.enterDecision(83, decisionCanBacktrack[83]);

                    int LA83_0 = input.LA(1);

                    if ( (LA83_0==WS||(LA83_0>=NL && LA83_0<=COMMENT)) ) {
                        alt83=1;
                    }
                    } finally {dbg.exitDecision(83);}

                    switch (alt83) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:502:15: ws
                            {
                            dbg.location(502,15);
                            pushFollow(FOLLOW_ws_in_operator1587);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(83);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:7: COMMA ( ws )?
                    {
                    dbg.location(503,7);
                    match(input,COMMA,FOLLOW_COMMA_in_operator1596); if (state.failed) return ;
                    dbg.location(503,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:13: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:503:13: ws
                            {
                            dbg.location(503,13);
                            pushFollow(FOLLOW_ws_in_operator1598);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(84);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:505:5: 
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
        dbg.location(505, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:507:1: combinator : ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | );
    public final void combinator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "combinator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(507, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:508:5: ( PLUS ( ws )? | GREATER ( ws )? | TILDE ( ws )? | )
            int alt89=4;
            try { dbg.enterDecision(89, decisionCanBacktrack[89]);

            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt89=1;
                }
                break;
            case GREATER:
                {
                alt89=2;
                }
                break;
            case TILDE:
                {
                alt89=3;
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
            case 117:
                {
                alt89=4;
                }
                break;
            default:
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:508:7: PLUS ( ws )?
                    {
                    dbg.location(508,7);
                    match(input,PLUS,FOLLOW_PLUS_in_combinator1626); if (state.failed) return ;
                    dbg.location(508,12);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:508:12: ( ws )?
                    int alt86=2;
                    try { dbg.enterSubRule(86);
                    try { dbg.enterDecision(86, decisionCanBacktrack[86]);

                    int LA86_0 = input.LA(1);

                    if ( (LA86_0==WS||(LA86_0>=NL && LA86_0<=COMMENT)) ) {
                        alt86=1;
                    }
                    } finally {dbg.exitDecision(86);}

                    switch (alt86) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:508:12: ws
                            {
                            dbg.location(508,12);
                            pushFollow(FOLLOW_ws_in_combinator1628);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(86);}


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:7: GREATER ( ws )?
                    {
                    dbg.location(509,7);
                    match(input,GREATER,FOLLOW_GREATER_in_combinator1637); if (state.failed) return ;
                    dbg.location(509,15);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:15: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:509:15: ws
                            {
                            dbg.location(509,15);
                            pushFollow(FOLLOW_ws_in_combinator1639);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(87);}


                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:7: TILDE ( ws )?
                    {
                    dbg.location(510,7);
                    match(input,TILDE,FOLLOW_TILDE_in_combinator1648); if (state.failed) return ;
                    dbg.location(510,13);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:13: ( ws )?
                    int alt88=2;
                    try { dbg.enterSubRule(88);
                    try { dbg.enterDecision(88, decisionCanBacktrack[88]);

                    int LA88_0 = input.LA(1);

                    if ( (LA88_0==WS||(LA88_0>=NL && LA88_0<=COMMENT)) ) {
                        alt88=1;
                    }
                    } finally {dbg.exitDecision(88);}

                    switch (alt88) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:510:13: ws
                            {
                            dbg.location(510,13);
                            pushFollow(FOLLOW_ws_in_combinator1650);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(88);}


                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:512:5: 
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
        dbg.location(512, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:514:1: unaryOperator : ( MINUS | PLUS );
    public final void unaryOperator() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "unaryOperator");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(514, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:515:5: ( MINUS | PLUS )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(515,5);
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
        dbg.location(517, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:519:1: property : ( IDENT | GEN ) ( ws )? ;
    public final void property() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "property");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(519, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:5: ( ( IDENT | GEN ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:7: ( IDENT | GEN ) ( ws )?
            {
            dbg.location(520,7);
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

            dbg.location(520,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:21: ( ws )?
            int alt90=2;
            try { dbg.enterSubRule(90);
            try { dbg.enterDecision(90, decisionCanBacktrack[90]);

            int LA90_0 = input.LA(1);

            if ( (LA90_0==WS||(LA90_0>=NL && LA90_0<=COMMENT)) ) {
                alt90=1;
            }
            } finally {dbg.exitDecision(90);}

            switch (alt90) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:520:21: ws
                    {
                    dbg.location(520,21);
                    pushFollow(FOLLOW_ws_in_property1718);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(90);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(521, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:523:1: rule : selectorsGroup LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE ;
    public final void rule() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "rule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(523, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:5: ( selectorsGroup LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:524:9: selectorsGroup LBRACE ( ws )? syncToDeclarationsRule declarations RBRACE
            {
            dbg.location(524,9);
            pushFollow(FOLLOW_selectorsGroup_in_rule1743);
            selectorsGroup();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(525,9);
            match(input,LBRACE,FOLLOW_LBRACE_in_rule1753); if (state.failed) return ;
            dbg.location(525,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:16: ( ws )?
            int alt91=2;
            try { dbg.enterSubRule(91);
            try { dbg.enterDecision(91, decisionCanBacktrack[91]);

            int LA91_0 = input.LA(1);

            if ( (LA91_0==WS||(LA91_0>=NL && LA91_0<=COMMENT)) ) {
                alt91=1;
            }
            } finally {dbg.exitDecision(91);}

            switch (alt91) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:525:16: ws
                    {
                    dbg.location(525,16);
                    pushFollow(FOLLOW_ws_in_rule1755);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(91);}

            dbg.location(525,20);
            pushFollow(FOLLOW_syncToDeclarationsRule_in_rule1758);
            syncToDeclarationsRule();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(526,13);
            pushFollow(FOLLOW_declarations_in_rule1772);
            declarations();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(527,9);
            match(input,RBRACE,FOLLOW_RBRACE_in_rule1782); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RBRACE));
                    input.consume(); //consume the RBRACE as well
                    
        }
        finally {
        }
        dbg.location(528, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:535:1: declarations : ( declaration )? ( SEMI ( ws )? ( declaration )? )* ;
    public final void declarations() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declarations");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(535, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:536:5: ( ( declaration )? ( SEMI ( ws )? ( declaration )? )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:9: ( declaration )? ( SEMI ( ws )? ( declaration )? )*
            {
            dbg.location(539,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:9: ( declaration )?
            int alt92=2;
            try { dbg.enterSubRule(92);
            try { dbg.enterDecision(92, decisionCanBacktrack[92]);

            int LA92_0 = input.LA(1);

            if ( (LA92_0==IDENT||LA92_0==GEN||LA92_0==STAR) ) {
                alt92=1;
            }
            } finally {dbg.exitDecision(92);}

            switch (alt92) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:10: declaration
                    {
                    dbg.location(539,10);
                    pushFollow(FOLLOW_declaration_in_declarations1840);
                    declaration();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(92);}

            dbg.location(539,24);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:24: ( SEMI ( ws )? ( declaration )? )*
            try { dbg.enterSubRule(95);

            loop95:
            do {
                int alt95=2;
                try { dbg.enterDecision(95, decisionCanBacktrack[95]);

                int LA95_0 = input.LA(1);

                if ( (LA95_0==SEMI) ) {
                    alt95=1;
                }


                } finally {dbg.exitDecision(95);}

                switch (alt95) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:25: SEMI ( ws )? ( declaration )?
            	    {
            	    dbg.location(539,25);
            	    match(input,SEMI,FOLLOW_SEMI_in_declarations1845); if (state.failed) return ;
            	    dbg.location(539,30);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:30: ( ws )?
            	    int alt93=2;
            	    try { dbg.enterSubRule(93);
            	    try { dbg.enterDecision(93, decisionCanBacktrack[93]);

            	    int LA93_0 = input.LA(1);

            	    if ( (LA93_0==WS||(LA93_0>=NL && LA93_0<=COMMENT)) ) {
            	        alt93=1;
            	    }
            	    } finally {dbg.exitDecision(93);}

            	    switch (alt93) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:30: ws
            	            {
            	            dbg.location(539,30);
            	            pushFollow(FOLLOW_ws_in_declarations1847);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(93);}

            	    dbg.location(539,34);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:34: ( declaration )?
            	    int alt94=2;
            	    try { dbg.enterSubRule(94);
            	    try { dbg.enterDecision(94, decisionCanBacktrack[94]);

            	    int LA94_0 = input.LA(1);

            	    if ( (LA94_0==IDENT||LA94_0==GEN||LA94_0==STAR) ) {
            	        alt94=1;
            	    }
            	    } finally {dbg.exitDecision(94);}

            	    switch (alt94) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:539:35: declaration
            	            {
            	            dbg.location(539,35);
            	            pushFollow(FOLLOW_declaration_in_declarations1851);
            	            declaration();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(94);}


            	    }
            	    break;

            	default :
            	    break loop95;
                }
            } while (true);
            } finally {dbg.exitSubRule(95);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(540, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:542:1: selectorsGroup : selector ( COMMA ( ws )? selector )* ;
    public final void selectorsGroup() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selectorsGroup");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(542, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:5: ( selector ( COMMA ( ws )? selector )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:7: selector ( COMMA ( ws )? selector )*
            {
            dbg.location(543,7);
            pushFollow(FOLLOW_selector_in_selectorsGroup1876);
            selector();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(543,16);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:16: ( COMMA ( ws )? selector )*
            try { dbg.enterSubRule(97);

            loop97:
            do {
                int alt97=2;
                try { dbg.enterDecision(97, decisionCanBacktrack[97]);

                int LA97_0 = input.LA(1);

                if ( (LA97_0==COMMA) ) {
                    alt97=1;
                }


                } finally {dbg.exitDecision(97);}

                switch (alt97) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:17: COMMA ( ws )? selector
            	    {
            	    dbg.location(543,17);
            	    match(input,COMMA,FOLLOW_COMMA_in_selectorsGroup1879); if (state.failed) return ;
            	    dbg.location(543,23);
            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:23: ( ws )?
            	    int alt96=2;
            	    try { dbg.enterSubRule(96);
            	    try { dbg.enterDecision(96, decisionCanBacktrack[96]);

            	    int LA96_0 = input.LA(1);

            	    if ( (LA96_0==WS||(LA96_0>=NL && LA96_0<=COMMENT)) ) {
            	        alt96=1;
            	    }
            	    } finally {dbg.exitDecision(96);}

            	    switch (alt96) {
            	        case 1 :
            	            dbg.enterAlt(1);

            	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:543:23: ws
            	            {
            	            dbg.location(543,23);
            	            pushFollow(FOLLOW_ws_in_selectorsGroup1881);
            	            ws();

            	            state._fsp--;
            	            if (state.failed) return ;

            	            }
            	            break;

            	    }
            	    } finally {dbg.exitSubRule(96);}

            	    dbg.location(543,27);
            	    pushFollow(FOLLOW_selector_in_selectorsGroup1884);
            	    selector();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop97;
                }
            } while (true);
            } finally {dbg.exitSubRule(97);}


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
            dbg.exitRule(getGrammarFileName(), "selectorsGroup");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "selectorsGroup"


    // $ANTLR start "selector"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:546:1: selector : simpleSelectorSequence ( combinator simpleSelectorSequence )* ;
    public final void selector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "selector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(546, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:5: ( simpleSelectorSequence ( combinator simpleSelectorSequence )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:7: simpleSelectorSequence ( combinator simpleSelectorSequence )*
            {
            dbg.location(547,7);
            pushFollow(FOLLOW_simpleSelectorSequence_in_selector1907);
            simpleSelectorSequence();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(547,30);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:30: ( combinator simpleSelectorSequence )*
            try { dbg.enterSubRule(98);

            loop98:
            do {
                int alt98=2;
                try { dbg.enterDecision(98, decisionCanBacktrack[98]);

                int LA98_0 = input.LA(1);

                if ( (LA98_0==IDENT||LA98_0==GEN||LA98_0==COLON||(LA98_0>=PLUS && LA98_0<=TILDE)||(LA98_0>=HASH && LA98_0<=PIPE)||LA98_0==117) ) {
                    alt98=1;
                }


                } finally {dbg.exitDecision(98);}

                switch (alt98) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:547:31: combinator simpleSelectorSequence
            	    {
            	    dbg.location(547,31);
            	    pushFollow(FOLLOW_combinator_in_selector1910);
            	    combinator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(547,42);
            	    pushFollow(FOLLOW_simpleSelectorSequence_in_selector1912);
            	    simpleSelectorSequence();

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
        dbg.location(548, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:551:1: simpleSelectorSequence : ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) );
    public final void simpleSelectorSequence() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "simpleSelectorSequence");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(551, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:552:2: ( ( typeSelector ( ( esPred )=> elementSubsequent )* ) | ( ( ( esPred )=> elementSubsequent )+ ) )
            int alt101=2;
            try { dbg.enterDecision(101, decisionCanBacktrack[101]);

            int LA101_0 = input.LA(1);

            if ( (LA101_0==IDENT||LA101_0==GEN||(LA101_0>=STAR && LA101_0<=PIPE)) ) {
                alt101=1;
            }
            else if ( (LA101_0==COLON||(LA101_0>=HASH && LA101_0<=DCOLON)||LA101_0==117) ) {
                alt101=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 101, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(101);}

            switch (alt101) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:554:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    {
                    dbg.location(554,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:554:2: ( typeSelector ( ( esPred )=> elementSubsequent )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:554:5: typeSelector ( ( esPred )=> elementSubsequent )*
                    {
                    dbg.location(554,5);
                    pushFollow(FOLLOW_typeSelector_in_simpleSelectorSequence1946);
                    typeSelector();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(554,18);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:554:18: ( ( esPred )=> elementSubsequent )*
                    try { dbg.enterSubRule(99);

                    loop99:
                    do {
                        int alt99=2;
                        try { dbg.enterDecision(99, decisionCanBacktrack[99]);

                        try {
                            isCyclicDecision = true;
                            alt99 = dfa99.predict(input);
                        }
                        catch (NoViableAltException nvae) {
                            dbg.recognitionException(nvae);
                            throw nvae;
                        }
                        } finally {dbg.exitDecision(99);}

                        switch (alt99) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:554:19: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(554,29);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence1953);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop99;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(99);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:2: ( ( ( esPred )=> elementSubsequent )+ )
                    {
                    dbg.location(556,2);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:2: ( ( ( esPred )=> elementSubsequent )+ )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:4: ( ( esPred )=> elementSubsequent )+
                    {
                    dbg.location(556,4);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:4: ( ( esPred )=> elementSubsequent )+
                    int cnt100=0;
                    try { dbg.enterSubRule(100);

                    loop100:
                    do {
                        int alt100=2;
                        try { dbg.enterDecision(100, decisionCanBacktrack[100]);

                        switch ( input.LA(1) ) {
                        case HASH:
                            {
                            int LA100_2 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt100=1;
                            }


                            }
                            break;
                        case 117:
                            {
                            int LA100_3 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt100=1;
                            }


                            }
                            break;
                        case DOT:
                            {
                            int LA100_4 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt100=1;
                            }


                            }
                            break;
                        case LBRACKET:
                            {
                            int LA100_5 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt100=1;
                            }


                            }
                            break;
                        case COLON:
                        case DCOLON:
                            {
                            int LA100_6 = input.LA(2);

                            if ( (synpred2_Css3()) ) {
                                alt100=1;
                            }


                            }
                            break;

                        }

                        } finally {dbg.exitDecision(100);}

                        switch (alt100) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:5: ( esPred )=> elementSubsequent
                    	    {
                    	    dbg.location(556,15);
                    	    pushFollow(FOLLOW_elementSubsequent_in_simpleSelectorSequence1971);
                    	    elementSubsequent();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt100 >= 1 ) break loop100;
                    	    if (state.backtracking>0) {state.failed=true; return ;}
                                EarlyExitException eee =
                                    new EarlyExitException(100, input);
                                dbg.recognitionException(eee);

                                throw eee;
                        }
                        cnt100++;
                    } while (true);
                    } finally {dbg.exitSubRule(100);}


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
        dbg.location(557, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:564:1: esPred : ( '#' | HASH | DOT | LBRACKET | COLON | DCOLON );
    public final void esPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "esPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(564, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:565:5: ( '#' | HASH | DOT | LBRACKET | COLON | DCOLON )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(565,5);
            if ( input.LA(1)==COLON||(input.LA(1)>=HASH && input.LA(1)<=DCOLON)||input.LA(1)==117 ) {
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
        dbg.location(566, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:568:1: typeSelector options {k=2; } : ( ( nsPred )=> namespacePrefix )? ( elementName ( ws )? ) ;
    public final void typeSelector() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "typeSelector");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(568, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:570:3: ( ( ( nsPred )=> namespacePrefix )? ( elementName ( ws )? ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:570:6: ( ( nsPred )=> namespacePrefix )? ( elementName ( ws )? )
            {
            dbg.location(570,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:570:6: ( ( nsPred )=> namespacePrefix )?
            int alt102=2;
            try { dbg.enterSubRule(102);
            try { dbg.enterDecision(102, decisionCanBacktrack[102]);

            int LA102_0 = input.LA(1);

            if ( (LA102_0==IDENT) ) {
                int LA102_1 = input.LA(2);

                if ( (synpred3_Css3()) ) {
                    alt102=1;
                }
            }
            else if ( (LA102_0==STAR) ) {
                int LA102_2 = input.LA(2);

                if ( (synpred3_Css3()) ) {
                    alt102=1;
                }
            }
            else if ( (LA102_0==PIPE) && (synpred3_Css3())) {
                alt102=1;
            }
            } finally {dbg.exitDecision(102);}

            switch (alt102) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:570:7: ( nsPred )=> namespacePrefix
                    {
                    dbg.location(570,17);
                    pushFollow(FOLLOW_namespacePrefix_in_typeSelector2073);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(102);}

            dbg.location(570,35);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:570:35: ( elementName ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:570:37: elementName ( ws )?
            {
            dbg.location(570,37);
            pushFollow(FOLLOW_elementName_in_typeSelector2079);
            elementName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(570,49);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:570:49: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:570:49: ws
                    {
                    dbg.location(570,49);
                    pushFollow(FOLLOW_ws_in_typeSelector2081);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(103);}


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(571, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:574:1: nsPred : ( IDENT | STAR )? PIPE ;
    public final void nsPred() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "nsPred");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(574, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:575:3: ( ( IDENT | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:576:3: ( IDENT | STAR )? PIPE
            {
            dbg.location(576,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:576:3: ( IDENT | STAR )?
            int alt104=2;
            try { dbg.enterSubRule(104);
            try { dbg.enterDecision(104, decisionCanBacktrack[104]);

            int LA104_0 = input.LA(1);

            if ( (LA104_0==IDENT||LA104_0==STAR) ) {
                alt104=1;
            }
            } finally {dbg.exitDecision(104);}

            switch (alt104) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
                    {
                    dbg.location(576,3);
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
            } finally {dbg.exitSubRule(104);}

            dbg.location(576,19);
            match(input,PIPE,FOLLOW_PIPE_in_nsPred2110); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(577, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:579:1: namespacePrefix : ( namespacePrefixName | STAR )? PIPE ;
    public final void namespacePrefix() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "namespacePrefix");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(579, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:580:3: ( ( namespacePrefixName | STAR )? PIPE )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:580:5: ( namespacePrefixName | STAR )? PIPE
            {
            dbg.location(580,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:580:5: ( namespacePrefixName | STAR )?
            int alt105=3;
            try { dbg.enterSubRule(105);
            try { dbg.enterDecision(105, decisionCanBacktrack[105]);

            int LA105_0 = input.LA(1);

            if ( (LA105_0==IDENT) ) {
                alt105=1;
            }
            else if ( (LA105_0==STAR) ) {
                alt105=2;
            }
            } finally {dbg.exitDecision(105);}

            switch (alt105) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:580:7: namespacePrefixName
                    {
                    dbg.location(580,7);
                    pushFollow(FOLLOW_namespacePrefixName_in_namespacePrefix2125);
                    namespacePrefixName();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:580:29: STAR
                    {
                    dbg.location(580,29);
                    match(input,STAR,FOLLOW_STAR_in_namespacePrefix2129); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(105);}

            dbg.location(580,36);
            match(input,PIPE,FOLLOW_PIPE_in_namespacePrefix2133); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(581, 3);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:584:1: elementSubsequent : ( cssId | cssClass | slAttribute | pseudo ) ( ws )? ;
    public final void elementSubsequent() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementSubsequent");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(584, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:585:5: ( ( cssId | cssClass | slAttribute | pseudo ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:5: ( cssId | cssClass | slAttribute | pseudo ) ( ws )?
            {
            dbg.location(586,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:586:5: ( cssId | cssClass | slAttribute | pseudo )
            int alt106=4;
            try { dbg.enterSubRule(106);
            try { dbg.enterDecision(106, decisionCanBacktrack[106]);

            switch ( input.LA(1) ) {
            case HASH:
            case 117:
                {
                alt106=1;
                }
                break;
            case DOT:
                {
                alt106=2;
                }
                break;
            case LBRACKET:
                {
                alt106=3;
                }
                break;
            case COLON:
            case DCOLON:
                {
                alt106=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 106, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }

            } finally {dbg.exitDecision(106);}

            switch (alt106) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:587:6: cssId
                    {
                    dbg.location(587,6);
                    pushFollow(FOLLOW_cssId_in_elementSubsequent2167);
                    cssId();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:588:8: cssClass
                    {
                    dbg.location(588,8);
                    pushFollow(FOLLOW_cssClass_in_elementSubsequent2176);
                    cssClass();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:589:11: slAttribute
                    {
                    dbg.location(589,11);
                    pushFollow(FOLLOW_slAttribute_in_elementSubsequent2188);
                    slAttribute();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:590:11: pseudo
                    {
                    dbg.location(590,11);
                    pushFollow(FOLLOW_pseudo_in_elementSubsequent2200);
                    pseudo();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(106);}

            dbg.location(592,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:5: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:592:5: ws
                    {
                    dbg.location(592,5);
                    pushFollow(FOLLOW_ws_in_elementSubsequent2212);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(107);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(593, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:596:1: cssId : ( HASH | ( '#' NAME ) );
    public final void cssId() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssId");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(596, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:5: ( HASH | ( '#' NAME ) )
            int alt108=2;
            try { dbg.enterDecision(108, decisionCanBacktrack[108]);

            int LA108_0 = input.LA(1);

            if ( (LA108_0==HASH) ) {
                alt108=1;
            }
            else if ( (LA108_0==117) ) {
                alt108=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 108, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(108);}

            switch (alt108) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:7: HASH
                    {
                    dbg.location(597,7);
                    match(input,HASH,FOLLOW_HASH_in_cssId2235); if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:14: ( '#' NAME )
                    {
                    dbg.location(597,14);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:14: ( '#' NAME )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:597:16: '#' NAME
                    {
                    dbg.location(597,16);
                    match(input,117,FOLLOW_117_in_cssId2241); if (state.failed) return ;
                    dbg.location(597,20);
                    match(input,NAME,FOLLOW_NAME_in_cssId2243); if (state.failed) return ;

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
        dbg.location(598, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:604:1: cssClass : DOT ( IDENT | GEN ) ;
    public final void cssClass() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "cssClass");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(604, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:5: ( DOT ( IDENT | GEN ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:605:7: DOT ( IDENT | GEN )
            {
            dbg.location(605,7);
            match(input,DOT,FOLLOW_DOT_in_cssClass2271); if (state.failed) return ;
            dbg.location(605,11);
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
        dbg.location(606, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:613:1: elementName : ( ( IDENT | GEN ) | '*' );
    public final void elementName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "elementName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(613, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:614:5: ( ( IDENT | GEN ) | '*' )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            {
            dbg.location(614,5);
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
        dbg.location(615, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:617:1: slAttribute : LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET ;
    public final void slAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(617, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:5: ( LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:618:7: LBRACKET ( namespacePrefix )? ( ws )? slAttributeName ( ws )? ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )? RBRACKET
            {
            dbg.location(618,7);
            match(input,LBRACKET,FOLLOW_LBRACKET_in_slAttribute2342); if (state.failed) return ;
            dbg.location(619,6);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:619:6: ( namespacePrefix )?
            int alt109=2;
            try { dbg.enterSubRule(109);
            try { dbg.enterDecision(109, decisionCanBacktrack[109]);

            int LA109_0 = input.LA(1);

            if ( (LA109_0==IDENT) ) {
                int LA109_1 = input.LA(2);

                if ( (LA109_1==PIPE) ) {
                    alt109=1;
                }
            }
            else if ( ((LA109_0>=STAR && LA109_0<=PIPE)) ) {
                alt109=1;
            }
            } finally {dbg.exitDecision(109);}

            switch (alt109) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:619:6: namespacePrefix
                    {
                    dbg.location(619,6);
                    pushFollow(FOLLOW_namespacePrefix_in_slAttribute2349);
                    namespacePrefix();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(109);}

            dbg.location(619,23);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:619:23: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:619:23: ws
                    {
                    dbg.location(619,23);
                    pushFollow(FOLLOW_ws_in_slAttribute2352);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(110);}

            dbg.location(620,9);
            pushFollow(FOLLOW_slAttributeName_in_slAttribute2363);
            slAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(620,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:25: ( ws )?
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:620:25: ws
                    {
                    dbg.location(620,25);
                    pushFollow(FOLLOW_ws_in_slAttribute2365);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(111);}

            dbg.location(622,13);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:622:13: ( ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )? )?
            int alt114=2;
            try { dbg.enterSubRule(114);
            try { dbg.enterDecision(114, decisionCanBacktrack[114]);

            int LA114_0 = input.LA(1);

            if ( ((LA114_0>=OPEQ && LA114_0<=CONTAINS)) ) {
                alt114=1;
            }
            } finally {dbg.exitDecision(114);}

            switch (alt114) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:623:17: ( OPEQ | INCLUDES | DASHMATCH | BEGINS | ENDS | CONTAINS ) ( ws )? slAttributeValue ( ws )?
                    {
                    dbg.location(623,17);
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

                    dbg.location(631,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:631:17: ( ws )?
                    int alt112=2;
                    try { dbg.enterSubRule(112);
                    try { dbg.enterDecision(112, decisionCanBacktrack[112]);

                    int LA112_0 = input.LA(1);

                    if ( (LA112_0==WS||(LA112_0>=NL && LA112_0<=COMMENT)) ) {
                        alt112=1;
                    }
                    } finally {dbg.exitDecision(112);}

                    switch (alt112) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:631:17: ws
                            {
                            dbg.location(631,17);
                            pushFollow(FOLLOW_ws_in_slAttribute2587);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(112);}

                    dbg.location(632,17);
                    pushFollow(FOLLOW_slAttributeValue_in_slAttribute2606);
                    slAttributeValue();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(633,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:17: ( ws )?
                    int alt113=2;
                    try { dbg.enterSubRule(113);
                    try { dbg.enterDecision(113, decisionCanBacktrack[113]);

                    int LA113_0 = input.LA(1);

                    if ( (LA113_0==WS||(LA113_0>=NL && LA113_0<=COMMENT)) ) {
                        alt113=1;
                    }
                    } finally {dbg.exitDecision(113);}

                    switch (alt113) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:633:17: ws
                            {
                            dbg.location(633,17);
                            pushFollow(FOLLOW_ws_in_slAttribute2624);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(113);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(114);}

            dbg.location(636,7);
            match(input,RBRACKET,FOLLOW_RBRACKET_in_slAttribute2653); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
                
        }
        finally {
        }
        dbg.location(637, 1);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:644:1: slAttributeName : IDENT ;
    public final void slAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(644, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:645:2: ( IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:645:4: IDENT
            {
            dbg.location(645,4);
            match(input,IDENT,FOLLOW_IDENT_in_slAttributeName2669); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(646, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:648:1: slAttributeValue : ( IDENT | STRING ) ;
    public final void slAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "slAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(648, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:649:2: ( ( IDENT | STRING ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:650:2: ( IDENT | STRING )
            {
            dbg.location(650,2);
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
        dbg.location(654, 9);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:656:1: pseudo : ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) ;
    public final void pseudo() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "pseudo");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(656, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:657:5: ( ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) ) )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:657:7: ( COLON | DCOLON ) ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            {
            dbg.location(657,7);
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

            dbg.location(658,14);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:658:14: ( ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? ) | ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN ) )
            int alt122=2;
            try { dbg.enterSubRule(122);
            try { dbg.enterDecision(122, decisionCanBacktrack[122]);

            int LA122_0 = input.LA(1);

            if ( (LA122_0==IDENT||LA122_0==GEN) ) {
                alt122=1;
            }
            else if ( (LA122_0==NOT) ) {
                alt122=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 122, 0, input);

                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(122);}

            switch (alt122) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:659:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? )
                    {
                    dbg.location(659,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:659:17: ( ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )? )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:660:21: ( IDENT | GEN ) ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )?
                    {
                    dbg.location(660,21);
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

                    dbg.location(661,21);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:661:21: ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )?
                    int alt118=2;
                    try { dbg.enterSubRule(118);
                    try { dbg.enterDecision(118, decisionCanBacktrack[118]);

                    try {
                        isCyclicDecision = true;
                        alt118 = dfa118.predict(input);
                    }
                    catch (NoViableAltException nvae) {
                        dbg.recognitionException(nvae);
                        throw nvae;
                    }
                    } finally {dbg.exitDecision(118);}

                    switch (alt118) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:662:25: ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN
                            {
                            dbg.location(662,25);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:662:25: ( ws )?
                            int alt115=2;
                            try { dbg.enterSubRule(115);
                            try { dbg.enterDecision(115, decisionCanBacktrack[115]);

                            int LA115_0 = input.LA(1);

                            if ( (LA115_0==WS||(LA115_0>=NL && LA115_0<=COMMENT)) ) {
                                alt115=1;
                            }
                            } finally {dbg.exitDecision(115);}

                            switch (alt115) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:662:25: ws
                                    {
                                    dbg.location(662,25);
                                    pushFollow(FOLLOW_ws_in_pseudo2864);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(115);}

                            dbg.location(662,29);
                            match(input,LPAREN,FOLLOW_LPAREN_in_pseudo2867); if (state.failed) return ;
                            dbg.location(662,36);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:662:36: ( ws )?
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

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:662:36: ws
                                    {
                                    dbg.location(662,36);
                                    pushFollow(FOLLOW_ws_in_pseudo2869);
                                    ws();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(116);}

                            dbg.location(662,40);
                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:662:40: ( expression | '*' )?
                            int alt117=3;
                            try { dbg.enterSubRule(117);
                            try { dbg.enterDecision(117, decisionCanBacktrack[117]);

                            int LA117_0 = input.LA(1);

                            if ( ((LA117_0>=IDENT && LA117_0<=URI)||(LA117_0>=GEN && LA117_0<=AT_IDENT)||LA117_0==PERCENTAGE||LA117_0==PLUS||(LA117_0>=MINUS && LA117_0<=HASH)||(LA117_0>=NUMBER && LA117_0<=DIMENSION)) ) {
                                alt117=1;
                            }
                            else if ( (LA117_0==STAR) ) {
                                alt117=2;
                            }
                            } finally {dbg.exitDecision(117);}

                            switch (alt117) {
                                case 1 :
                                    dbg.enterAlt(1);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:662:42: expression
                                    {
                                    dbg.location(662,42);
                                    pushFollow(FOLLOW_expression_in_pseudo2874);
                                    expression();

                                    state._fsp--;
                                    if (state.failed) return ;

                                    }
                                    break;
                                case 2 :
                                    dbg.enterAlt(2);

                                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:662:55: '*'
                                    {
                                    dbg.location(662,55);
                                    match(input,STAR,FOLLOW_STAR_in_pseudo2878); if (state.failed) return ;

                                    }
                                    break;

                            }
                            } finally {dbg.exitSubRule(117);}

                            dbg.location(662,62);
                            match(input,RPAREN,FOLLOW_RPAREN_in_pseudo2883); if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(118);}


                    }


                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:666:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    {
                    dbg.location(666,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:666:17: ( NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:666:19: NOT ( ws )? LPAREN ( ws )? ( simpleSelectorSequence )? RPAREN
                    {
                    dbg.location(666,19);
                    match(input,NOT,FOLLOW_NOT_in_pseudo2962); if (state.failed) return ;
                    dbg.location(666,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:666:23: ( ws )?
                    int alt119=2;
                    try { dbg.enterSubRule(119);
                    try { dbg.enterDecision(119, decisionCanBacktrack[119]);

                    int LA119_0 = input.LA(1);

                    if ( (LA119_0==WS||(LA119_0>=NL && LA119_0<=COMMENT)) ) {
                        alt119=1;
                    }
                    } finally {dbg.exitDecision(119);}

                    switch (alt119) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:666:23: ws
                            {
                            dbg.location(666,23);
                            pushFollow(FOLLOW_ws_in_pseudo2964);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(119);}

                    dbg.location(666,27);
                    match(input,LPAREN,FOLLOW_LPAREN_in_pseudo2967); if (state.failed) return ;
                    dbg.location(666,34);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:666:34: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:666:34: ws
                            {
                            dbg.location(666,34);
                            pushFollow(FOLLOW_ws_in_pseudo2969);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(120);}

                    dbg.location(666,38);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:666:38: ( simpleSelectorSequence )?
                    int alt121=2;
                    try { dbg.enterSubRule(121);
                    try { dbg.enterDecision(121, decisionCanBacktrack[121]);

                    int LA121_0 = input.LA(1);

                    if ( (LA121_0==IDENT||LA121_0==GEN||LA121_0==COLON||(LA121_0>=HASH && LA121_0<=PIPE)||LA121_0==117) ) {
                        alt121=1;
                    }
                    } finally {dbg.exitDecision(121);}

                    switch (alt121) {
                        case 1 :
                            dbg.enterAlt(1);

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:666:38: simpleSelectorSequence
                            {
                            dbg.location(666,38);
                            pushFollow(FOLLOW_simpleSelectorSequence_in_pseudo2972);
                            simpleSelectorSequence();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(121);}

                    dbg.location(666,62);
                    match(input,RPAREN,FOLLOW_RPAREN_in_pseudo2975); if (state.failed) return ;

                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(122);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(668, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:670:1: declaration : ( STAR )? property COLON ( ws )? propertyValue ( prio )? ;
    public final void declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(670, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:671:5: ( ( STAR )? property COLON ( ws )? propertyValue ( prio )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:673:5: ( STAR )? property COLON ( ws )? propertyValue ( prio )?
            {
            dbg.location(673,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:673:5: ( STAR )?
            int alt123=2;
            try { dbg.enterSubRule(123);
            try { dbg.enterDecision(123, decisionCanBacktrack[123]);

            int LA123_0 = input.LA(1);

            if ( (LA123_0==STAR) ) {
                alt123=1;
            }
            } finally {dbg.exitDecision(123);}

            switch (alt123) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:673:5: STAR
                    {
                    dbg.location(673,5);
                    match(input,STAR,FOLLOW_STAR_in_declaration3019); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(123);}

            dbg.location(673,11);
            pushFollow(FOLLOW_property_in_declaration3022);
            property();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(673,20);
            match(input,COLON,FOLLOW_COLON_in_declaration3024); if (state.failed) return ;
            dbg.location(673,26);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:673:26: ( ws )?
            int alt124=2;
            try { dbg.enterSubRule(124);
            try { dbg.enterDecision(124, decisionCanBacktrack[124]);

            int LA124_0 = input.LA(1);

            if ( (LA124_0==WS||(LA124_0>=NL && LA124_0<=COMMENT)) ) {
                alt124=1;
            }
            } finally {dbg.exitDecision(124);}

            switch (alt124) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:673:26: ws
                    {
                    dbg.location(673,26);
                    pushFollow(FOLLOW_ws_in_declaration3026);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(124);}

            dbg.location(673,30);
            pushFollow(FOLLOW_propertyValue_in_declaration3029);
            propertyValue();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(673,44);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:673:44: ( prio )?
            int alt125=2;
            try { dbg.enterSubRule(125);
            try { dbg.enterDecision(125, decisionCanBacktrack[125]);

            int LA125_0 = input.LA(1);

            if ( (LA125_0==IMPORTANT_SYM) ) {
                alt125=1;
            }
            } finally {dbg.exitDecision(125);}

            switch (alt125) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:673:44: prio
                    {
                    dbg.location(673,44);
                    pushFollow(FOLLOW_prio_in_declaration3031);
                    prio();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(125);}


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
        dbg.location(674, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:682:1: propertyValue : expression ;
    public final void propertyValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "propertyValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(682, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:683:2: ( expression )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:683:4: expression
            {
            dbg.location(683,4);
            pushFollow(FOLLOW_expression_in_propertyValue3055);
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
        dbg.location(684, 2);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "propertyValue");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "propertyValue"


    // $ANTLR start "syncToDeclarationsRule"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:688:1: syncToDeclarationsRule : ;
    public final void syncToDeclarationsRule() throws RecognitionException {

                syncToSet(BitSet.of(IDENT, RBRACE, STAR));
            
        try { dbg.enterRule(getGrammarFileName(), "syncToDeclarationsRule");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(688, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:692:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:693:6: 
            {
            }

        }
        finally {
        }
        dbg.location(693, 6);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "syncToDeclarationsRule");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "syncToDeclarationsRule"


    // $ANTLR start "syncTo_RBRACE"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:695:1: syncTo_RBRACE : ;
    public final void syncTo_RBRACE() throws RecognitionException {

                syncToRBRACE(1); //initial nest == 1
            
        try { dbg.enterRule(getGrammarFileName(), "syncTo_RBRACE");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(695, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:699:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:700:6: 
            {
            }

        }
        finally {
        }
        dbg.location(700, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:703:1: syncToFollow : ;
    public final void syncToFollow() throws RecognitionException {

                syncToSet();
            
        try { dbg.enterRule(getGrammarFileName(), "syncToFollow");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(703, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:707:6: ()
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:708:6: 
            {
            }

        }
        finally {
        }
        dbg.location(708, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:710:1: prio : IMPORTANT_SYM ( ws )? ;
    public final void prio() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "prio");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(710, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:711:5: ( IMPORTANT_SYM ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:711:7: IMPORTANT_SYM ( ws )?
            {
            dbg.location(711,7);
            match(input,IMPORTANT_SYM,FOLLOW_IMPORTANT_SYM_in_prio3167); if (state.failed) return ;
            dbg.location(711,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:711:21: ( ws )?
            int alt126=2;
            try { dbg.enterSubRule(126);
            try { dbg.enterDecision(126, decisionCanBacktrack[126]);

            int LA126_0 = input.LA(1);

            if ( (LA126_0==WS||(LA126_0>=NL && LA126_0<=COMMENT)) ) {
                alt126=1;
            }
            } finally {dbg.exitDecision(126);}

            switch (alt126) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:711:21: ws
                    {
                    dbg.location(711,21);
                    pushFollow(FOLLOW_ws_in_prio3169);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(126);}


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
            dbg.exitRule(getGrammarFileName(), "prio");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "prio"


    // $ANTLR start "expression"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:714:1: expression : term ( operator term )* ;
    public final void expression() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "expression");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(714, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:715:5: ( term ( operator term )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:715:7: term ( operator term )*
            {
            dbg.location(715,7);
            pushFollow(FOLLOW_term_in_expression3191);
            term();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(715,12);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:715:12: ( operator term )*
            try { dbg.enterSubRule(127);

            loop127:
            do {
                int alt127=2;
                try { dbg.enterDecision(127, decisionCanBacktrack[127]);

                try {
                    isCyclicDecision = true;
                    alt127 = dfa127.predict(input);
                }
                catch (NoViableAltException nvae) {
                    dbg.recognitionException(nvae);
                    throw nvae;
                }
                } finally {dbg.exitDecision(127);}

                switch (alt127) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:715:13: operator term
            	    {
            	    dbg.location(715,13);
            	    pushFollow(FOLLOW_operator_in_expression3194);
            	    operator();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    dbg.location(715,22);
            	    pushFollow(FOLLOW_term_in_expression3196);
            	    term();

            	    state._fsp--;
            	    if (state.failed) return ;

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
        dbg.location(716, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:718:1: term : ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | less_variable ) ( ws )? ;
    public final void term() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "term");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(718, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:5: ( ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | less_variable ) ( ws )? )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:7: ( unaryOperator ( ws )? )? ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | less_variable ) ( ws )?
            {
            dbg.location(719,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:7: ( unaryOperator ( ws )? )?
            int alt129=2;
            try { dbg.enterSubRule(129);
            try { dbg.enterDecision(129, decisionCanBacktrack[129]);

            int LA129_0 = input.LA(1);

            if ( (LA129_0==PLUS||LA129_0==MINUS) ) {
                alt129=1;
            }
            } finally {dbg.exitDecision(129);}

            switch (alt129) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:9: unaryOperator ( ws )?
                    {
                    dbg.location(719,9);
                    pushFollow(FOLLOW_unaryOperator_in_term3221);
                    unaryOperator();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(719,23);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:23: ( ws )?
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

                            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:719:23: ws
                            {
                            dbg.location(719,23);
                            pushFollow(FOLLOW_ws_in_term3223);
                            ws();

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }
                    } finally {dbg.exitSubRule(128);}


                    }
                    break;

            }
            } finally {dbg.exitSubRule(129);}

            dbg.location(720,9);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:720:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | less_variable )
            int alt130=8;
            try { dbg.enterSubRule(130);
            try { dbg.enterDecision(130, decisionCanBacktrack[130]);

            try {
                isCyclicDecision = true;
                alt130 = dfa130.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(130);}

            switch (alt130) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:721:9: ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION )
                    {
                    dbg.location(721,9);
                    if ( input.LA(1)==PERCENTAGE||(input.LA(1)>=NUMBER && input.LA(1)<=DIMENSION) ) {
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

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:734:7: STRING
                    {
                    dbg.location(734,7);
                    match(input,STRING,FOLLOW_STRING_in_term3446); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    dbg.enterAlt(3);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:735:7: IDENT
                    {
                    dbg.location(735,7);
                    match(input,IDENT,FOLLOW_IDENT_in_term3454); if (state.failed) return ;

                    }
                    break;
                case 4 :
                    dbg.enterAlt(4);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:736:7: GEN
                    {
                    dbg.location(736,7);
                    match(input,GEN,FOLLOW_GEN_in_term3462); if (state.failed) return ;

                    }
                    break;
                case 5 :
                    dbg.enterAlt(5);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:737:7: URI
                    {
                    dbg.location(737,7);
                    match(input,URI,FOLLOW_URI_in_term3470); if (state.failed) return ;

                    }
                    break;
                case 6 :
                    dbg.enterAlt(6);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:738:7: hexColor
                    {
                    dbg.location(738,7);
                    pushFollow(FOLLOW_hexColor_in_term3478);
                    hexColor();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 7 :
                    dbg.enterAlt(7);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:739:7: function
                    {
                    dbg.location(739,7);
                    pushFollow(FOLLOW_function_in_term3486);
                    function();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 8 :
                    dbg.enterAlt(8);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:740:7: less_variable
                    {
                    dbg.location(740,7);
                    pushFollow(FOLLOW_less_variable_in_term3494);
                    less_variable();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(130);}

            dbg.location(742,5);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:742:5: ( ws )?
            int alt131=2;
            try { dbg.enterSubRule(131);
            try { dbg.enterDecision(131, decisionCanBacktrack[131]);

            int LA131_0 = input.LA(1);

            if ( (LA131_0==WS||(LA131_0>=NL && LA131_0<=COMMENT)) ) {
                alt131=1;
            }
            } finally {dbg.exitDecision(131);}

            switch (alt131) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:742:5: ws
                    {
                    dbg.location(742,5);
                    pushFollow(FOLLOW_ws_in_term3506);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(131);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(743, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:745:1: function : functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN ;
    public final void function() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "function");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(745, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:746:2: ( functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:746:5: functionName ( ws )? LPAREN ( ws )? ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) ) RPAREN
            {
            dbg.location(746,5);
            pushFollow(FOLLOW_functionName_in_function3522);
            functionName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(746,18);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:746:18: ( ws )?
            int alt132=2;
            try { dbg.enterSubRule(132);
            try { dbg.enterDecision(132, decisionCanBacktrack[132]);

            int LA132_0 = input.LA(1);

            if ( (LA132_0==WS||(LA132_0>=NL && LA132_0<=COMMENT)) ) {
                alt132=1;
            }
            } finally {dbg.exitDecision(132);}

            switch (alt132) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:746:18: ws
                    {
                    dbg.location(746,18);
                    pushFollow(FOLLOW_ws_in_function3524);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(132);}

            dbg.location(747,3);
            match(input,LPAREN,FOLLOW_LPAREN_in_function3529); if (state.failed) return ;
            dbg.location(747,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:747:10: ( ws )?
            int alt133=2;
            try { dbg.enterSubRule(133);
            try { dbg.enterDecision(133, decisionCanBacktrack[133]);

            int LA133_0 = input.LA(1);

            if ( (LA133_0==WS||(LA133_0>=NL && LA133_0<=COMMENT)) ) {
                alt133=1;
            }
            } finally {dbg.exitDecision(133);}

            switch (alt133) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:747:10: ws
                    {
                    dbg.location(747,10);
                    pushFollow(FOLLOW_ws_in_function3531);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(133);}

            dbg.location(748,3);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:748:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )
            int alt136=2;
            try { dbg.enterSubRule(136);
            try { dbg.enterDecision(136, decisionCanBacktrack[136]);

            try {
                isCyclicDecision = true;
                alt136 = dfa136.predict(input);
            }
            catch (NoViableAltException nvae) {
                dbg.recognitionException(nvae);
                throw nvae;
            }
            } finally {dbg.exitDecision(136);}

            switch (alt136) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:749:4: expression
                    {
                    dbg.location(749,4);
                    pushFollow(FOLLOW_expression_in_function3542);
                    expression();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    dbg.enterAlt(2);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    {
                    dbg.location(751,6);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:751:6: ( fnAttribute ( COMMA ( ws )? fnAttribute )* )
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:5: fnAttribute ( COMMA ( ws )? fnAttribute )*
                    {
                    dbg.location(752,5);
                    pushFollow(FOLLOW_fnAttribute_in_function3560);
                    fnAttribute();

                    state._fsp--;
                    if (state.failed) return ;
                    dbg.location(752,17);
                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:17: ( COMMA ( ws )? fnAttribute )*
                    try { dbg.enterSubRule(135);

                    loop135:
                    do {
                        int alt135=2;
                        try { dbg.enterDecision(135, decisionCanBacktrack[135]);

                        int LA135_0 = input.LA(1);

                        if ( (LA135_0==COMMA) ) {
                            alt135=1;
                        }


                        } finally {dbg.exitDecision(135);}

                        switch (alt135) {
                    	case 1 :
                    	    dbg.enterAlt(1);

                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:18: COMMA ( ws )? fnAttribute
                    	    {
                    	    dbg.location(752,18);
                    	    match(input,COMMA,FOLLOW_COMMA_in_function3563); if (state.failed) return ;
                    	    dbg.location(752,24);
                    	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:24: ( ws )?
                    	    int alt134=2;
                    	    try { dbg.enterSubRule(134);
                    	    try { dbg.enterDecision(134, decisionCanBacktrack[134]);

                    	    int LA134_0 = input.LA(1);

                    	    if ( (LA134_0==WS||(LA134_0>=NL && LA134_0<=COMMENT)) ) {
                    	        alt134=1;
                    	    }
                    	    } finally {dbg.exitDecision(134);}

                    	    switch (alt134) {
                    	        case 1 :
                    	            dbg.enterAlt(1);

                    	            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:752:24: ws
                    	            {
                    	            dbg.location(752,24);
                    	            pushFollow(FOLLOW_ws_in_function3565);
                    	            ws();

                    	            state._fsp--;
                    	            if (state.failed) return ;

                    	            }
                    	            break;

                    	    }
                    	    } finally {dbg.exitSubRule(134);}

                    	    dbg.location(752,28);
                    	    pushFollow(FOLLOW_fnAttribute_in_function3568);
                    	    fnAttribute();

                    	    state._fsp--;
                    	    if (state.failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop135;
                        }
                    } while (true);
                    } finally {dbg.exitSubRule(135);}


                    }


                    }
                    break;

            }
            } finally {dbg.exitSubRule(136);}

            dbg.location(755,3);
            match(input,RPAREN,FOLLOW_RPAREN_in_function3589); if (state.failed) return ;

            }

        }
        catch ( RecognitionException rce) {

                    reportError(rce);
                    consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 

        }
        finally {
        }
        dbg.location(756, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:762:1: functionName : ( IDENT COLON )? IDENT ( DOT IDENT )* ;
    public final void functionName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "functionName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(762, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:2: ( ( IDENT COLON )? IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:4: ( IDENT COLON )? IDENT ( DOT IDENT )*
            {
            dbg.location(766,4);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:4: ( IDENT COLON )?
            int alt137=2;
            try { dbg.enterSubRule(137);
            try { dbg.enterDecision(137, decisionCanBacktrack[137]);

            int LA137_0 = input.LA(1);

            if ( (LA137_0==IDENT) ) {
                int LA137_1 = input.LA(2);

                if ( (LA137_1==COLON) ) {
                    alt137=1;
                }
            }
            } finally {dbg.exitDecision(137);}

            switch (alt137) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:5: IDENT COLON
                    {
                    dbg.location(766,5);
                    match(input,IDENT,FOLLOW_IDENT_in_functionName3637); if (state.failed) return ;
                    dbg.location(766,11);
                    match(input,COLON,FOLLOW_COLON_in_functionName3639); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(137);}

            dbg.location(766,19);
            match(input,IDENT,FOLLOW_IDENT_in_functionName3643); if (state.failed) return ;
            dbg.location(766,25);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:25: ( DOT IDENT )*
            try { dbg.enterSubRule(138);

            loop138:
            do {
                int alt138=2;
                try { dbg.enterDecision(138, decisionCanBacktrack[138]);

                int LA138_0 = input.LA(1);

                if ( (LA138_0==DOT) ) {
                    alt138=1;
                }


                } finally {dbg.exitDecision(138);}

                switch (alt138) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:766:26: DOT IDENT
            	    {
            	    dbg.location(766,26);
            	    match(input,DOT,FOLLOW_DOT_in_functionName3646); if (state.failed) return ;
            	    dbg.location(766,30);
            	    match(input,IDENT,FOLLOW_IDENT_in_functionName3648); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop138;
                }
            } while (true);
            } finally {dbg.exitSubRule(138);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(767, 6);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:769:1: fnAttribute : fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue ;
    public final void fnAttribute() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttribute");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(769, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:770:2: ( fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:770:4: fnAttributeName ( ws )? OPEQ ( ws )? fnAttributeValue
            {
            dbg.location(770,4);
            pushFollow(FOLLOW_fnAttributeName_in_fnAttribute3670);
            fnAttributeName();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(770,20);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:770:20: ( ws )?
            int alt139=2;
            try { dbg.enterSubRule(139);
            try { dbg.enterDecision(139, decisionCanBacktrack[139]);

            int LA139_0 = input.LA(1);

            if ( (LA139_0==WS||(LA139_0>=NL && LA139_0<=COMMENT)) ) {
                alt139=1;
            }
            } finally {dbg.exitDecision(139);}

            switch (alt139) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:770:20: ws
                    {
                    dbg.location(770,20);
                    pushFollow(FOLLOW_ws_in_fnAttribute3672);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(139);}

            dbg.location(770,24);
            match(input,OPEQ,FOLLOW_OPEQ_in_fnAttribute3675); if (state.failed) return ;
            dbg.location(770,29);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:770:29: ( ws )?
            int alt140=2;
            try { dbg.enterSubRule(140);
            try { dbg.enterDecision(140, decisionCanBacktrack[140]);

            int LA140_0 = input.LA(1);

            if ( (LA140_0==WS||(LA140_0>=NL && LA140_0<=COMMENT)) ) {
                alt140=1;
            }
            } finally {dbg.exitDecision(140);}

            switch (alt140) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:770:29: ws
                    {
                    dbg.location(770,29);
                    pushFollow(FOLLOW_ws_in_fnAttribute3677);
                    ws();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(140);}

            dbg.location(770,33);
            pushFollow(FOLLOW_fnAttributeValue_in_fnAttribute3680);
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
        dbg.location(771, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:773:1: fnAttributeName : IDENT ( DOT IDENT )* ;
    public final void fnAttributeName() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeName");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(773, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:774:2: ( IDENT ( DOT IDENT )* )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:774:4: IDENT ( DOT IDENT )*
            {
            dbg.location(774,4);
            match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName3695); if (state.failed) return ;
            dbg.location(774,10);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:774:10: ( DOT IDENT )*
            try { dbg.enterSubRule(141);

            loop141:
            do {
                int alt141=2;
                try { dbg.enterDecision(141, decisionCanBacktrack[141]);

                int LA141_0 = input.LA(1);

                if ( (LA141_0==DOT) ) {
                    alt141=1;
                }


                } finally {dbg.exitDecision(141);}

                switch (alt141) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:774:11: DOT IDENT
            	    {
            	    dbg.location(774,11);
            	    match(input,DOT,FOLLOW_DOT_in_fnAttributeName3698); if (state.failed) return ;
            	    dbg.location(774,15);
            	    match(input,IDENT,FOLLOW_IDENT_in_fnAttributeName3700); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop141;
                }
            } while (true);
            } finally {dbg.exitSubRule(141);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(775, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:777:1: fnAttributeValue : expression ;
    public final void fnAttributeValue() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "fnAttributeValue");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(777, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:778:2: ( expression )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:778:4: expression
            {
            dbg.location(778,4);
            pushFollow(FOLLOW_expression_in_fnAttributeValue3714);
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
        dbg.location(779, 2);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:781:1: hexColor : HASH ;
    public final void hexColor() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "hexColor");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(781, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:782:5: ( HASH )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:782:7: HASH
            {
            dbg.location(782,7);
            match(input,HASH,FOLLOW_HASH_in_hexColor3732); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(783, 5);

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
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:785:1: ws : ( WS | NL | COMMENT )+ ;
    public final void ws() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "ws");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(785, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:786:5: ( ( WS | NL | COMMENT )+ )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:786:7: ( WS | NL | COMMENT )+
            {
            dbg.location(786,7);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:786:7: ( WS | NL | COMMENT )+
            int cnt142=0;
            try { dbg.enterSubRule(142);

            loop142:
            do {
                int alt142=2;
                try { dbg.enterDecision(142, decisionCanBacktrack[142]);

                int LA142_0 = input.LA(1);

                if ( (LA142_0==WS||(LA142_0>=NL && LA142_0<=COMMENT)) ) {
                    alt142=1;
                }


                } finally {dbg.exitDecision(142);}

                switch (alt142) {
            	case 1 :
            	    dbg.enterAlt(1);

            	    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:
            	    {
            	    dbg.location(786,7);
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
            	    if ( cnt142 >= 1 ) break loop142;
            	    if (state.backtracking>0) {state.failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(142, input);
                        dbg.recognitionException(eee);

                        throw eee;
                }
                cnt142++;
            } while (true);
            } finally {dbg.exitSubRule(142);}


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(787, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "ws");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "ws"


    // $ANTLR start "less_variable_declaration"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:790:1: less_variable_declaration : less_variable ( WS )? COLON ( WS )? expression SEMI ;
    public final void less_variable_declaration() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_variable_declaration");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(790, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:791:5: ( less_variable ( WS )? COLON ( WS )? expression SEMI )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:791:7: less_variable ( WS )? COLON ( WS )? expression SEMI
            {
            dbg.location(791,7);
            pushFollow(FOLLOW_less_variable_in_less_variable_declaration3788);
            less_variable();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(791,21);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:791:21: ( WS )?
            int alt143=2;
            try { dbg.enterSubRule(143);
            try { dbg.enterDecision(143, decisionCanBacktrack[143]);

            int LA143_0 = input.LA(1);

            if ( (LA143_0==WS) ) {
                alt143=1;
            }
            } finally {dbg.exitDecision(143);}

            switch (alt143) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:791:21: WS
                    {
                    dbg.location(791,21);
                    match(input,WS,FOLLOW_WS_in_less_variable_declaration3790); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(143);}

            dbg.location(791,25);
            match(input,COLON,FOLLOW_COLON_in_less_variable_declaration3793); if (state.failed) return ;
            dbg.location(791,31);
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:791:31: ( WS )?
            int alt144=2;
            try { dbg.enterSubRule(144);
            try { dbg.enterDecision(144, decisionCanBacktrack[144]);

            int LA144_0 = input.LA(1);

            if ( (LA144_0==WS) ) {
                alt144=1;
            }
            } finally {dbg.exitDecision(144);}

            switch (alt144) {
                case 1 :
                    dbg.enterAlt(1);

                    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:791:31: WS
                    {
                    dbg.location(791,31);
                    match(input,WS,FOLLOW_WS_in_less_variable_declaration3795); if (state.failed) return ;

                    }
                    break;

            }
            } finally {dbg.exitSubRule(144);}

            dbg.location(791,35);
            pushFollow(FOLLOW_expression_in_less_variable_declaration3798);
            expression();

            state._fsp--;
            if (state.failed) return ;
            dbg.location(791,46);
            match(input,SEMI,FOLLOW_SEMI_in_less_variable_declaration3800); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(792, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_variable_declaration");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_variable_declaration"


    // $ANTLR start "less_variable"
    // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:794:1: less_variable : AT_IDENT ;
    public final void less_variable() throws RecognitionException {
        try { dbg.enterRule(getGrammarFileName(), "less_variable");
        if ( getRuleLevel()==0 ) {dbg.commence();}
        incRuleLevel();
        dbg.location(794, 1);

        try {
            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:5: ( AT_IDENT )
            dbg.enterAlt(1);

            // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:795:7: AT_IDENT
            {
            dbg.location(795,7);
            match(input,AT_IDENT,FOLLOW_AT_IDENT_in_less_variable3821); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        dbg.location(796, 5);

        }
        finally {
            dbg.exitRule(getGrammarFileName(), "less_variable");
            decRuleLevel();
            if ( getRuleLevel()==0 ) {dbg.terminate();}
        }

        return ;
    }
    // $ANTLR end "less_variable"

    // $ANTLR start synpred1_Css3
    public final void synpred1_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:554:19: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:554:20: esPred
        {
        dbg.location(554,20);
        pushFollow(FOLLOW_esPred_in_synpred1_Css31950);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred1_Css3

    // $ANTLR start synpred2_Css3
    public final void synpred2_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:5: ( esPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:556:6: esPred
        {
        dbg.location(556,6);
        pushFollow(FOLLOW_esPred_in_synpred2_Css31968);
        esPred();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred2_Css3

    // $ANTLR start synpred3_Css3
    public final void synpred3_Css3_fragment() throws RecognitionException {   
        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:570:7: ( nsPred )
        dbg.enterAlt(1);

        // /Volumes/Mercurial/web-main/css.lib/src/org/netbeans/modules/css/lib/Css3.g:570:8: nsPred
        {
        dbg.location(570,8);
        pushFollow(FOLLOW_nsPred_in_synpred3_Css32070);
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


    protected DFA42 dfa42 = new DFA42(this);
    protected DFA63 dfa63 = new DFA63(this);
    protected DFA99 dfa99 = new DFA99(this);
    protected DFA118 dfa118 = new DFA118(this);
    protected DFA127 dfa127 = new DFA127(this);
    protected DFA130 dfa130 = new DFA130(this);
    protected DFA136 dfa136 = new DFA136(this);
    static final String DFA42_eotS =
        "\12\uffff";
    static final String DFA42_eofS =
        "\12\uffff";
    static final String DFA42_minS =
        "\1\5\6\uffff\2\5\1\uffff";
    static final String DFA42_maxS =
        "\1\165\6\uffff\2\56\1\uffff";
    static final String DFA42_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\2\uffff\1\7";
    static final String DFA42_specialS =
        "\12\uffff}>";
    static final String[] DFA42_transitionS = {
            "\1\1\5\uffff\1\2\6\uffff\1\1\1\7\1\uffff\1\6\3\uffff\1\6\1\uffff"+
            "\1\3\1\4\1\5\20\uffff\1\1\5\uffff\6\1\73\uffff\1\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "\2\6\5\uffff\1\6\7\uffff\1\10\31\uffff\1\11",
            "\2\6\5\uffff\1\6\7\uffff\1\6\31\uffff\1\11",
            ""
    };

    static final short[] DFA42_eot = DFA.unpackEncodedString(DFA42_eotS);
    static final short[] DFA42_eof = DFA.unpackEncodedString(DFA42_eofS);
    static final char[] DFA42_min = DFA.unpackEncodedStringToUnsignedChars(DFA42_minS);
    static final char[] DFA42_max = DFA.unpackEncodedStringToUnsignedChars(DFA42_maxS);
    static final short[] DFA42_accept = DFA.unpackEncodedString(DFA42_acceptS);
    static final short[] DFA42_special = DFA.unpackEncodedString(DFA42_specialS);
    static final short[][] DFA42_transition;

    static {
        int numStates = DFA42_transitionS.length;
        DFA42_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA42_transition[i] = DFA.unpackEncodedString(DFA42_transitionS[i]);
        }
    }

    class DFA42 extends DFA {

        public DFA42(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 42;
            this.eot = DFA42_eot;
            this.eof = DFA42_eof;
            this.min = DFA42_min;
            this.max = DFA42_max;
            this.accept = DFA42_accept;
            this.special = DFA42_special;
            this.transition = DFA42_transition;
        }
        public String getDescription() {
            return "383:1: bodyItem : ( rule | media | page | counterStyle | fontFace | vendorAtRule | less_variable_declaration );";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA63_eotS =
        "\4\uffff";
    static final String DFA63_eofS =
        "\4\uffff";
    static final String DFA63_minS =
        "\2\14\2\uffff";
    static final String DFA63_maxS =
        "\2\120\2\uffff";
    static final String DFA63_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA63_specialS =
        "\4\uffff}>";
    static final String[] DFA63_transitionS = {
            "\1\2\1\uffff\1\3\5\uffff\1\1\72\uffff\2\1",
            "\1\2\1\uffff\1\3\5\uffff\1\1\72\uffff\2\1",
            "",
            ""
    };

    static final short[] DFA63_eot = DFA.unpackEncodedString(DFA63_eotS);
    static final short[] DFA63_eof = DFA.unpackEncodedString(DFA63_eofS);
    static final char[] DFA63_min = DFA.unpackEncodedStringToUnsignedChars(DFA63_minS);
    static final char[] DFA63_max = DFA.unpackEncodedStringToUnsignedChars(DFA63_maxS);
    static final short[] DFA63_accept = DFA.unpackEncodedString(DFA63_acceptS);
    static final short[] DFA63_special = DFA.unpackEncodedString(DFA63_specialS);
    static final short[][] DFA63_transition;

    static {
        int numStates = DFA63_transitionS.length;
        DFA63_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA63_transition[i] = DFA.unpackEncodedString(DFA63_transitionS[i]);
        }
    }

    class DFA63 extends DFA {

        public DFA63(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 63;
            this.eot = DFA63_eot;
            this.eof = DFA63_eof;
            this.min = DFA63_min;
            this.max = DFA63_max;
            this.accept = DFA63_accept;
            this.special = DFA63_special;
            this.transition = DFA63_transition;
        }
        public String getDescription() {
            return "()* loopback of 446:25: ( ( ws )? COMMA ( ws )? ( IDENT | PERCENTAGE ) )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA99_eotS =
        "\21\uffff";
    static final String DFA99_eofS =
        "\21\uffff";
    static final String DFA99_minS =
        "\1\5\7\uffff\5\0\4\uffff";
    static final String DFA99_maxS =
        "\1\165\7\uffff\5\0\4\uffff";
    static final String DFA99_acceptS =
        "\1\uffff\1\2\16\uffff\1\1";
    static final String DFA99_specialS =
        "\10\uffff\1\0\1\1\1\2\1\3\1\4\4\uffff}>";
    static final String[] DFA99_transitionS = {
            "\1\1\6\uffff\1\1\1\uffff\1\1\3\uffff\1\1\33\uffff\1\14\1\uffff"+
            "\3\1\1\uffff\1\10\1\12\1\13\1\14\2\1\11\uffff\1\1\61\uffff\1"+
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

    static final short[] DFA99_eot = DFA.unpackEncodedString(DFA99_eotS);
    static final short[] DFA99_eof = DFA.unpackEncodedString(DFA99_eofS);
    static final char[] DFA99_min = DFA.unpackEncodedStringToUnsignedChars(DFA99_minS);
    static final char[] DFA99_max = DFA.unpackEncodedStringToUnsignedChars(DFA99_maxS);
    static final short[] DFA99_accept = DFA.unpackEncodedString(DFA99_acceptS);
    static final short[] DFA99_special = DFA.unpackEncodedString(DFA99_specialS);
    static final short[][] DFA99_transition;

    static {
        int numStates = DFA99_transitionS.length;
        DFA99_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA99_transition[i] = DFA.unpackEncodedString(DFA99_transitionS[i]);
        }
    }

    class DFA99 extends DFA {

        public DFA99(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 99;
            this.eot = DFA99_eot;
            this.eof = DFA99_eof;
            this.min = DFA99_min;
            this.max = DFA99_max;
            this.accept = DFA99_accept;
            this.special = DFA99_special;
            this.transition = DFA99_transition;
        }
        public String getDescription() {
            return "()* loopback of 554:18: ( ( esPred )=> elementSubsequent )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA99_8 = input.LA(1);

                         
                        int index99_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index99_8);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA99_9 = input.LA(1);

                         
                        int index99_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index99_9);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA99_10 = input.LA(1);

                         
                        int index99_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index99_10);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA99_11 = input.LA(1);

                         
                        int index99_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index99_11);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA99_12 = input.LA(1);

                         
                        int index99_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred1_Css3()) ) {s = 16;}

                        else if ( (true) ) {s = 1;}

                         
                        input.seek(index99_12);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 99, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA118_eotS =
        "\4\uffff";
    static final String DFA118_eofS =
        "\4\uffff";
    static final String DFA118_minS =
        "\2\5\2\uffff";
    static final String DFA118_maxS =
        "\2\165\2\uffff";
    static final String DFA118_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA118_specialS =
        "\4\uffff}>";
    static final String[] DFA118_transitionS = {
            "\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff\1\1\31\uffff"+
            "\1\3\1\uffff\3\3\1\uffff\6\3\10\uffff\1\2\1\3\13\uffff\2\1\44"+
            "\uffff\1\3",
            "\1\3\6\uffff\1\3\1\uffff\1\3\3\uffff\1\3\1\uffff\1\1\31\uffff"+
            "\1\3\1\uffff\3\3\1\uffff\6\3\10\uffff\1\2\1\3\13\uffff\2\1\44"+
            "\uffff\1\3",
            "",
            ""
    };

    static final short[] DFA118_eot = DFA.unpackEncodedString(DFA118_eotS);
    static final short[] DFA118_eof = DFA.unpackEncodedString(DFA118_eofS);
    static final char[] DFA118_min = DFA.unpackEncodedStringToUnsignedChars(DFA118_minS);
    static final char[] DFA118_max = DFA.unpackEncodedStringToUnsignedChars(DFA118_maxS);
    static final short[] DFA118_accept = DFA.unpackEncodedString(DFA118_acceptS);
    static final short[] DFA118_special = DFA.unpackEncodedString(DFA118_specialS);
    static final short[][] DFA118_transition;

    static {
        int numStates = DFA118_transitionS.length;
        DFA118_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA118_transition[i] = DFA.unpackEncodedString(DFA118_transitionS[i]);
        }
    }

    class DFA118 extends DFA {

        public DFA118(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 118;
            this.eot = DFA118_eot;
            this.eof = DFA118_eof;
            this.min = DFA118_min;
            this.max = DFA118_max;
            this.accept = DFA118_accept;
            this.special = DFA118_special;
            this.transition = DFA118_transition;
        }
        public String getDescription() {
            return "661:21: ( ( ws )? LPAREN ( ws )? ( expression | '*' )? RPAREN )?";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA127_eotS =
        "\12\uffff";
    static final String DFA127_eofS =
        "\12\uffff";
    static final String DFA127_minS =
        "\1\5\1\uffff\1\5\1\uffff\4\5\2\24";
    static final String DFA127_maxS =
        "\1\116\1\uffff\1\120\1\uffff\2\120\1\5\3\120";
    static final String DFA127_acceptS =
        "\1\uffff\1\2\1\uffff\1\1\6\uffff";
    static final String DFA127_specialS =
        "\12\uffff}>";
    static final String[] DFA127_transitionS = {
            "\3\3\1\uffff\1\1\3\uffff\1\1\1\2\3\uffff\2\3\6\uffff\1\3\24"+
            "\uffff\2\3\2\uffff\2\3\16\uffff\2\1\12\3",
            "",
            "\1\5\2\3\12\uffff\2\3\1\4\5\uffff\1\3\25\uffff\1\3\2\uffff"+
            "\2\3\20\uffff\12\3\2\4",
            "",
            "\1\5\2\3\12\uffff\2\3\1\4\5\uffff\1\3\25\uffff\1\3\2\uffff"+
            "\2\3\20\uffff\12\3\2\4",
            "\3\3\1\uffff\1\3\3\uffff\2\3\3\uffff\2\3\1\7\5\uffff\1\3\23"+
            "\uffff\3\3\2\uffff\2\3\1\6\5\uffff\1\1\6\uffff\15\3\2\7",
            "\1\10",
            "\3\3\1\uffff\1\3\3\uffff\2\3\3\uffff\2\3\1\7\5\uffff\1\3\24"+
            "\uffff\2\3\2\uffff\2\3\6\uffff\1\1\6\uffff\15\3\2\7",
            "\1\11\40\uffff\1\6\5\uffff\1\1\6\uffff\1\3\14\uffff\2\11",
            "\1\11\46\uffff\1\1\6\uffff\1\3\14\uffff\2\11"
    };

    static final short[] DFA127_eot = DFA.unpackEncodedString(DFA127_eotS);
    static final short[] DFA127_eof = DFA.unpackEncodedString(DFA127_eofS);
    static final char[] DFA127_min = DFA.unpackEncodedStringToUnsignedChars(DFA127_minS);
    static final char[] DFA127_max = DFA.unpackEncodedStringToUnsignedChars(DFA127_maxS);
    static final short[] DFA127_accept = DFA.unpackEncodedString(DFA127_acceptS);
    static final short[] DFA127_special = DFA.unpackEncodedString(DFA127_specialS);
    static final short[][] DFA127_transition;

    static {
        int numStates = DFA127_transitionS.length;
        DFA127_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA127_transition[i] = DFA.unpackEncodedString(DFA127_transitionS[i]);
        }
    }

    class DFA127 extends DFA {

        public DFA127(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 127;
            this.eot = DFA127_eot;
            this.eof = DFA127_eof;
            this.min = DFA127_min;
            this.max = DFA127_max;
            this.accept = DFA127_accept;
            this.special = DFA127_special;
            this.transition = DFA127_transition;
        }
        public String getDescription() {
            return "()* loopback of 715:12: ( operator term )*";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA130_eotS =
        "\13\uffff";
    static final String DFA130_eofS =
        "\13\uffff";
    static final String DFA130_minS =
        "\1\5\2\uffff\1\5\5\uffff\1\5\1\uffff";
    static final String DFA130_maxS =
        "\1\116\2\uffff\1\120\5\uffff\1\120\1\uffff";
    static final String DFA130_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\4\1\5\1\6\1\10\1\7\1\uffff\1\3";
    static final String DFA130_specialS =
        "\13\uffff}>";
    static final String[] DFA130_transitionS = {
            "\1\3\1\2\1\5\12\uffff\1\4\1\7\6\uffff\1\1\31\uffff\1\6\20\uffff"+
            "\12\1",
            "",
            "",
            "\3\12\1\uffff\1\12\3\uffff\2\12\3\uffff\2\12\1\11\5\uffff\1"+
            "\12\23\uffff\1\10\2\12\2\uffff\2\12\1\10\14\uffff\1\10\14\12"+
            "\2\11",
            "",
            "",
            "",
            "",
            "",
            "\3\12\1\uffff\1\12\3\uffff\2\12\3\uffff\2\12\1\11\5\uffff\1"+
            "\12\24\uffff\2\12\2\uffff\2\12\15\uffff\1\10\14\12\2\11",
            ""
    };

    static final short[] DFA130_eot = DFA.unpackEncodedString(DFA130_eotS);
    static final short[] DFA130_eof = DFA.unpackEncodedString(DFA130_eofS);
    static final char[] DFA130_min = DFA.unpackEncodedStringToUnsignedChars(DFA130_minS);
    static final char[] DFA130_max = DFA.unpackEncodedStringToUnsignedChars(DFA130_maxS);
    static final short[] DFA130_accept = DFA.unpackEncodedString(DFA130_acceptS);
    static final short[] DFA130_special = DFA.unpackEncodedString(DFA130_specialS);
    static final short[][] DFA130_transition;

    static {
        int numStates = DFA130_transitionS.length;
        DFA130_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA130_transition[i] = DFA.unpackEncodedString(DFA130_transitionS[i]);
        }
    }

    class DFA130 extends DFA {

        public DFA130(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 130;
            this.eot = DFA130_eot;
            this.eof = DFA130_eof;
            this.min = DFA130_min;
            this.max = DFA130_max;
            this.accept = DFA130_accept;
            this.special = DFA130_special;
            this.transition = DFA130_transition;
        }
        public String getDescription() {
            return "720:9: ( ( NUMBER | PERCENTAGE | LENGTH | EMS | REM | EXS | ANGLE | TIME | FREQ | RESOLUTION | DIMENSION ) | STRING | IDENT | GEN | URI | hexColor | function | less_variable )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
    static final String DFA136_eotS =
        "\10\uffff";
    static final String DFA136_eofS =
        "\10\uffff";
    static final String DFA136_minS =
        "\1\5\1\uffff\3\5\1\uffff\2\24";
    static final String DFA136_maxS =
        "\1\116\1\uffff\2\120\1\5\1\uffff\2\120";
    static final String DFA136_acceptS =
        "\1\uffff\1\1\3\uffff\1\2\2\uffff";
    static final String DFA136_specialS =
        "\10\uffff}>";
    static final String[] DFA136_transitionS = {
            "\1\2\2\1\12\uffff\2\1\6\uffff\1\1\25\uffff\1\1\2\uffff\2\1\20"+
            "\uffff\12\1",
            "",
            "\3\1\6\uffff\1\1\3\uffff\2\1\1\3\5\uffff\1\1\23\uffff\3\1\2"+
            "\uffff\2\1\1\4\5\uffff\1\5\6\uffff\2\1\1\uffff\12\1\2\3",
            "\3\1\6\uffff\1\1\3\uffff\2\1\1\3\5\uffff\1\1\24\uffff\2\1\2"+
            "\uffff\2\1\6\uffff\1\5\6\uffff\2\1\1\uffff\12\1\2\3",
            "\1\6",
            "",
            "\1\7\40\uffff\1\4\5\uffff\1\5\6\uffff\1\1\14\uffff\2\7",
            "\1\7\46\uffff\1\5\6\uffff\1\1\14\uffff\2\7"
    };

    static final short[] DFA136_eot = DFA.unpackEncodedString(DFA136_eotS);
    static final short[] DFA136_eof = DFA.unpackEncodedString(DFA136_eofS);
    static final char[] DFA136_min = DFA.unpackEncodedStringToUnsignedChars(DFA136_minS);
    static final char[] DFA136_max = DFA.unpackEncodedStringToUnsignedChars(DFA136_maxS);
    static final short[] DFA136_accept = DFA.unpackEncodedString(DFA136_acceptS);
    static final short[] DFA136_special = DFA.unpackEncodedString(DFA136_specialS);
    static final short[][] DFA136_transition;

    static {
        int numStates = DFA136_transitionS.length;
        DFA136_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA136_transition[i] = DFA.unpackEncodedString(DFA136_transitionS[i]);
        }
    }

    class DFA136 extends DFA {

        public DFA136(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 136;
            this.eot = DFA136_eot;
            this.eof = DFA136_eof;
            this.min = DFA136_min;
            this.max = DFA136_max;
            this.accept = DFA136_accept;
            this.special = DFA136_special;
            this.transition = DFA136_transition;
        }
        public String getDescription() {
            return "748:3: ( expression | ( fnAttribute ( COMMA ( ws )? fnAttribute )* ) )";
        }
        public void error(NoViableAltException nvae) {
            dbg.recognitionException(nvae);
        }
    }
 

    public static final BitSet FOLLOW_ws_in_styleSheet125 = new BitSet(new long[]{0x03F040003A2C0D30L,0x0020000000000000L});
    public static final BitSet FOLLOW_charSet_in_styleSheet135 = new BitSet(new long[]{0x03F040003A3C0C30L,0x0020000000018000L});
    public static final BitSet FOLLOW_ws_in_styleSheet137 = new BitSet(new long[]{0x03F040003A2C0C30L,0x0020000000000000L});
    public static final BitSet FOLLOW_imports_in_styleSheet151 = new BitSet(new long[]{0x03F040003A2C0830L,0x0020000000000000L});
    public static final BitSet FOLLOW_namespaces_in_styleSheet162 = new BitSet(new long[]{0x03F040003A2C0820L,0x0020000000000000L});
    public static final BitSet FOLLOW_body_in_styleSheet174 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_styleSheet182 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespace_in_namespaces199 = new BitSet(new long[]{0x0000000000100012L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_namespaces201 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_NAMESPACE_SYM_in_namespace217 = new BitSet(new long[]{0x00000000001000E0L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_namespace219 = new BitSet(new long[]{0x00000000001000E0L,0x0000000000018000L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespace223 = new BitSet(new long[]{0x00000000001000E0L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_namespace225 = new BitSet(new long[]{0x00000000001000E0L,0x0000000000018000L});
    public static final BitSet FOLLOW_resourceIdentifier_in_namespace230 = new BitSet(new long[]{0x0000000000100200L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_namespace232 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_namespace235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_namespacePrefixName248 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_resourceIdentifier0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CHARSET_SYM_in_charSet286 = new BitSet(new long[]{0x0000000000100040L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_charSet288 = new BitSet(new long[]{0x0000000000100040L,0x0000000000018000L});
    public static final BitSet FOLLOW_charSetValue_in_charSet291 = new BitSet(new long[]{0x0000000000100200L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_charSet293 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_charSet296 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_charSetValue310 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_importItem_in_imports324 = new BitSet(new long[]{0x0000000000100402L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_imports326 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_IMPORT_SYM_in_importItem347 = new BitSet(new long[]{0x00000000001000E0L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_importItem349 = new BitSet(new long[]{0x00000000001000E0L,0x0000000000018000L});
    public static final BitSet FOLLOW_resourceIdentifier_in_importItem352 = new BitSet(new long[]{0x0000000000170220L,0x0000000000018004L});
    public static final BitSet FOLLOW_ws_in_importItem354 = new BitSet(new long[]{0x0000000000070220L,0x0000000000000004L});
    public static final BitSet FOLLOW_mediaQueryList_in_importItem357 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_importItem359 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MEDIA_SYM_in_media375 = new BitSet(new long[]{0x0000000000171020L,0x0000000000018004L});
    public static final BitSet FOLLOW_ws_in_media377 = new BitSet(new long[]{0x0000000000071020L,0x0000000000000004L});
    public static final BitSet FOLLOW_mediaQueryList_in_media380 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_media390 = new BitSet(new long[]{0x03F040002A3C2020L,0x0020000000018000L});
    public static final BitSet FOLLOW_ws_in_media392 = new BitSet(new long[]{0x03F040002A2C2020L,0x0020000000000000L});
    public static final BitSet FOLLOW_rule_in_media411 = new BitSet(new long[]{0x03F040002A3C2020L,0x0020000000018000L});
    public static final BitSet FOLLOW_page_in_media415 = new BitSet(new long[]{0x03F040002A3C2020L,0x0020000000018000L});
    public static final BitSet FOLLOW_fontFace_in_media419 = new BitSet(new long[]{0x03F040002A3C2020L,0x0020000000018000L});
    public static final BitSet FOLLOW_vendorAtRule_in_media423 = new BitSet(new long[]{0x03F040002A3C2020L,0x0020000000018000L});
    public static final BitSet FOLLOW_ws_in_media427 = new BitSet(new long[]{0x03F040002A2C2020L,0x0020000000000000L});
    public static final BitSet FOLLOW_RBRACE_in_media441 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList457 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_COMMA_in_mediaQueryList461 = new BitSet(new long[]{0x0000000000170020L,0x0000000000018004L});
    public static final BitSet FOLLOW_ws_in_mediaQueryList463 = new BitSet(new long[]{0x0000000000070020L,0x0000000000000004L});
    public static final BitSet FOLLOW_mediaQuery_in_mediaQueryList466 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_mediaQueryOperator_in_mediaQuery485 = new BitSet(new long[]{0x0000000000170020L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery487 = new BitSet(new long[]{0x0000000000070020L});
    public static final BitSet FOLLOW_mediaType_in_mediaQuery494 = new BitSet(new long[]{0x0000000000108002L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_mediaQuery496 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery501 = new BitSet(new long[]{0x0000000000170020L,0x0000000000018004L});
    public static final BitSet FOLLOW_ws_in_mediaQuery503 = new BitSet(new long[]{0x0000000000070020L,0x0000000000000004L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery506 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery514 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_AND_in_mediaQuery518 = new BitSet(new long[]{0x0000000000170020L,0x0000000000018004L});
    public static final BitSet FOLLOW_ws_in_mediaQuery520 = new BitSet(new long[]{0x0000000000070020L,0x0000000000000004L});
    public static final BitSet FOLLOW_mediaExpression_in_mediaQuery523 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_set_in_mediaQueryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_mediaType0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_mediaExpression578 = new BitSet(new long[]{0x0000000000100020L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression580 = new BitSet(new long[]{0x0000000000100020L,0x0000000000018000L});
    public static final BitSet FOLLOW_mediaFeature_in_mediaExpression583 = new BitSet(new long[]{0x0000400000100000L,0x0000000000018008L});
    public static final BitSet FOLLOW_ws_in_mediaExpression585 = new BitSet(new long[]{0x0000400000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_COLON_in_mediaExpression590 = new BitSet(new long[]{0x03F940003E3C08E0L,0x002000000001FFE0L});
    public static final BitSet FOLLOW_ws_in_mediaExpression592 = new BitSet(new long[]{0x03F940003E3C08E0L,0x002000000001FFE0L});
    public static final BitSet FOLLOW_expression_in_mediaExpression595 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_mediaExpression600 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_mediaExpression602 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_mediaFeature618 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bodyItem_in_body634 = new BitSet(new long[]{0x03F040003A3C0822L,0x0020000000018000L});
    public static final BitSet FOLLOW_ws_in_body636 = new BitSet(new long[]{0x03F040003A2C0822L,0x0020000000000000L});
    public static final BitSet FOLLOW_rule_in_bodyItem661 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_media_in_bodyItem673 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_page_in_bodyItem685 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_counterStyle_in_bodyItem697 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_fontFace_in_bodyItem709 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_vendorAtRule_in_bodyItem721 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_less_variable_declaration_in_bodyItem733 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_moz_document_in_vendorAtRule756 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframes_in_vendorAtRule760 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generic_at_rule_in_vendorAtRule764 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_atRuleId0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_IDENT_in_generic_at_rule800 = new BitSet(new long[]{0x0000000000101060L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule802 = new BitSet(new long[]{0x0000000000101060L});
    public static final BitSet FOLLOW_atRuleId_in_generic_at_rule807 = new BitSet(new long[]{0x0000000000101000L});
    public static final BitSet FOLLOW_WS_in_generic_at_rule809 = new BitSet(new long[]{0x0000000000101000L});
    public static final BitSet FOLLOW_LBRACE_in_generic_at_rule824 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_syncTo_RBRACE_in_generic_at_rule836 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_generic_at_rule846 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MOZ_DOCUMENT_SYM_in_moz_document862 = new BitSet(new long[]{0x0000000001D00080L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_moz_document864 = new BitSet(new long[]{0x0000000001D00080L,0x0000000000018000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document869 = new BitSet(new long[]{0x0000000000105000L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_moz_document871 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_COMMA_in_moz_document877 = new BitSet(new long[]{0x0000000001D00080L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_moz_document879 = new BitSet(new long[]{0x0000000001D00080L,0x0000000000018000L});
    public static final BitSet FOLLOW_moz_document_function_in_moz_document882 = new BitSet(new long[]{0x0000000000105000L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_moz_document884 = new BitSet(new long[]{0x0000000000005000L});
    public static final BitSet FOLLOW_LBRACE_in_moz_document891 = new BitSet(new long[]{0x03F040003A3C2820L,0x0020000000018000L});
    public static final BitSet FOLLOW_ws_in_moz_document893 = new BitSet(new long[]{0x03F040003A2C2820L,0x0020000000000000L});
    public static final BitSet FOLLOW_body_in_moz_document898 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_moz_document903 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_moz_document_function0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WEBKIT_KEYFRAMES_SYM_in_webkitKeyframes944 = new BitSet(new long[]{0x0000000000100060L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes946 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_atRuleId_in_webkitKeyframes949 = new BitSet(new long[]{0x0000000000101000L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes951 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframes956 = new BitSet(new long[]{0x0000000004102020L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes958 = new BitSet(new long[]{0x0000000004002020L});
    public static final BitSet FOLLOW_webkitKeyframesBlock_in_webkitKeyframes965 = new BitSet(new long[]{0x0000000004102020L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframes967 = new BitSet(new long[]{0x0000000004002020L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframes974 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_webkitKeyframeSelectors_in_webkitKeyframesBlock987 = new BitSet(new long[]{0x0000000000101000L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock989 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_webkitKeyframesBlock993 = new BitSet(new long[]{0x0100000000142220L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframesBlock996 = new BitSet(new long[]{0x0100000000042220L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_webkitKeyframesBlock999 = new BitSet(new long[]{0x0100000000042220L});
    public static final BitSet FOLLOW_declarations_in_webkitKeyframesBlock1003 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_webkitKeyframesBlock1006 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1021 = new BitSet(new long[]{0x0000000000104002L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1033 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_COMMA_in_webkitKeyframeSelectors1036 = new BitSet(new long[]{0x0000000004100020L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_webkitKeyframeSelectors1038 = new BitSet(new long[]{0x0000000004000020L});
    public static final BitSet FOLLOW_set_in_webkitKeyframeSelectors1041 = new BitSet(new long[]{0x0000000000104002L,0x0000000000018000L});
    public static final BitSet FOLLOW_PAGE_SYM_in_page1070 = new BitSet(new long[]{0x0000400000101020L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_page1072 = new BitSet(new long[]{0x0000400000001020L});
    public static final BitSet FOLLOW_IDENT_in_page1077 = new BitSet(new long[]{0x0000400000101000L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_page1079 = new BitSet(new long[]{0x0000400000001000L});
    public static final BitSet FOLLOW_pseudoPage_in_page1086 = new BitSet(new long[]{0x0000000000101000L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_page1088 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_page1101 = new BitSet(new long[]{0x01003FFFC0142220L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_page1103 = new BitSet(new long[]{0x01003FFFC0042220L});
    public static final BitSet FOLLOW_declaration_in_page1158 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_margin_in_page1160 = new BitSet(new long[]{0x0000000000102200L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_page1162 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_SEMI_in_page1168 = new BitSet(new long[]{0x01003FFFC0142220L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_page1170 = new BitSet(new long[]{0x01003FFFC0042220L});
    public static final BitSet FOLLOW_declaration_in_page1174 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_margin_in_page1176 = new BitSet(new long[]{0x0000000000102200L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_page1178 = new BitSet(new long[]{0x0000000000002200L});
    public static final BitSet FOLLOW_RBRACE_in_page1193 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNTER_STYLE_SYM_in_counterStyle1214 = new BitSet(new long[]{0x0000000000100020L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1216 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_counterStyle1219 = new BitSet(new long[]{0x0000000000101000L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1221 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_counterStyle1232 = new BitSet(new long[]{0x0100000000142220L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_counterStyle1234 = new BitSet(new long[]{0x0100000000042220L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_counterStyle1237 = new BitSet(new long[]{0x0100000000042220L});
    public static final BitSet FOLLOW_declarations_in_counterStyle1241 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_counterStyle1251 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FONT_FACE_SYM_in_fontFace1272 = new BitSet(new long[]{0x0000000000101000L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_fontFace1274 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_fontFace1285 = new BitSet(new long[]{0x0100000000142220L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_fontFace1287 = new BitSet(new long[]{0x0100000000042220L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_fontFace1290 = new BitSet(new long[]{0x0100000000042220L});
    public static final BitSet FOLLOW_declarations_in_fontFace1294 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_fontFace1304 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_margin_sym_in_margin1319 = new BitSet(new long[]{0x0000000000101000L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_margin1321 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_margin1324 = new BitSet(new long[]{0x0100000000142220L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_margin1326 = new BitSet(new long[]{0x0100000000042220L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_margin1329 = new BitSet(new long[]{0x0100000000042220L});
    public static final BitSet FOLLOW_declarations_in_margin1331 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_margin1333 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_margin_sym0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLON_in_pseudoPage1562 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_pseudoPage1564 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOLIDUS_in_operator1585 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_operator1587 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COMMA_in_operator1596 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_operator1598 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_combinator1626 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_combinator1628 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATER_in_combinator1637 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_combinator1639 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TILDE_in_combinator1648 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_combinator1650 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_unaryOperator0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_property1710 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_property1718 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectorsGroup_in_rule1743 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_LBRACE_in_rule1753 = new BitSet(new long[]{0x0100000000142220L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_rule1755 = new BitSet(new long[]{0x0100000000042220L});
    public static final BitSet FOLLOW_syncToDeclarationsRule_in_rule1758 = new BitSet(new long[]{0x0100000000042220L});
    public static final BitSet FOLLOW_declarations_in_rule1772 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_RBRACE_in_rule1782 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_declaration_in_declarations1840 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_SEMI_in_declarations1845 = new BitSet(new long[]{0x0100000000140222L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_declarations1847 = new BitSet(new long[]{0x0100000000040222L});
    public static final BitSet FOLLOW_declaration_in_declarations1851 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup1876 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_COMMA_in_selectorsGroup1879 = new BitSet(new long[]{0x03F0400000140020L,0x0020000000018000L});
    public static final BitSet FOLLOW_ws_in_selectorsGroup1881 = new BitSet(new long[]{0x03F0400000040020L,0x0020000000000000L});
    public static final BitSet FOLLOW_selector_in_selectorsGroup1884 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector1907 = new BitSet(new long[]{0x03F7400000040022L,0x0020000000000000L});
    public static final BitSet FOLLOW_combinator_in_selector1910 = new BitSet(new long[]{0x03F0400000040020L,0x0020000000000000L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_selector1912 = new BitSet(new long[]{0x03F7400000040022L,0x0020000000000000L});
    public static final BitSet FOLLOW_typeSelector_in_simpleSelectorSequence1946 = new BitSet(new long[]{0x03F0400000040022L,0x0020000000000000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence1953 = new BitSet(new long[]{0x03F0400000040022L,0x0020000000000000L});
    public static final BitSet FOLLOW_elementSubsequent_in_simpleSelectorSequence1971 = new BitSet(new long[]{0x03F0400000040022L,0x0020000000000000L});
    public static final BitSet FOLLOW_set_in_esPred0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefix_in_typeSelector2073 = new BitSet(new long[]{0x0300000000040020L});
    public static final BitSet FOLLOW_elementName_in_typeSelector2079 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_typeSelector2081 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_nsPred2101 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_PIPE_in_nsPred2110 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespacePrefixName_in_namespacePrefix2125 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_STAR_in_namespacePrefix2129 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_PIPE_in_namespacePrefix2133 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cssId_in_elementSubsequent2167 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_cssClass_in_elementSubsequent2176 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_slAttribute_in_elementSubsequent2188 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_pseudo_in_elementSubsequent2200 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_elementSubsequent2212 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_cssId2235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_117_in_cssId2241 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_NAME_in_cssId2243 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOT_in_cssClass2271 = new BitSet(new long[]{0x0000000000040020L});
    public static final BitSet FOLLOW_set_in_cssClass2273 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_elementName0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_slAttribute2342 = new BitSet(new long[]{0x0300000000100020L,0x0000000000018000L});
    public static final BitSet FOLLOW_namespacePrefix_in_slAttribute2349 = new BitSet(new long[]{0x0300000000100020L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2352 = new BitSet(new long[]{0x0300000000100020L,0x0000000000018000L});
    public static final BitSet FOLLOW_slAttributeName_in_slAttribute2363 = new BitSet(new long[]{0xF800000000100000L,0x0000000000018003L});
    public static final BitSet FOLLOW_ws_in_slAttribute2365 = new BitSet(new long[]{0xF800000000000000L,0x0000000000000003L});
    public static final BitSet FOLLOW_set_in_slAttribute2407 = new BitSet(new long[]{0x0000000000100060L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_slAttribute2587 = new BitSet(new long[]{0x0000000000100060L,0x0000000000018000L});
    public static final BitSet FOLLOW_slAttributeValue_in_slAttribute2606 = new BitSet(new long[]{0x0000000000100000L,0x0000000000018002L});
    public static final BitSet FOLLOW_ws_in_slAttribute2624 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_RBRACKET_in_slAttribute2653 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_slAttributeName2669 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_slAttributeValue2683 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_pseudo2743 = new BitSet(new long[]{0x0000000000060020L});
    public static final BitSet FOLLOW_set_in_pseudo2807 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018004L});
    public static final BitSet FOLLOW_ws_in_pseudo2864 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo2867 = new BitSet(new long[]{0x03F940003E3C08E0L,0x002000000001FFE8L});
    public static final BitSet FOLLOW_ws_in_pseudo2869 = new BitSet(new long[]{0x03F940003E3C08E0L,0x002000000001FFE8L});
    public static final BitSet FOLLOW_expression_in_pseudo2874 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_STAR_in_pseudo2878 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo2883 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_pseudo2962 = new BitSet(new long[]{0x0000000000100000L,0x0000000000018004L});
    public static final BitSet FOLLOW_ws_in_pseudo2964 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_LPAREN_in_pseudo2967 = new BitSet(new long[]{0x03F0400000140020L,0x0020000000018008L});
    public static final BitSet FOLLOW_ws_in_pseudo2969 = new BitSet(new long[]{0x03F0400000040020L,0x0020000000000008L});
    public static final BitSet FOLLOW_simpleSelectorSequence_in_pseudo2972 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_pseudo2975 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_declaration3019 = new BitSet(new long[]{0x0100000000040020L});
    public static final BitSet FOLLOW_property_in_declaration3022 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_COLON_in_declaration3024 = new BitSet(new long[]{0x03F940003E3C08E0L,0x002000000001FFE0L});
    public static final BitSet FOLLOW_ws_in_declaration3026 = new BitSet(new long[]{0x03F940003E3C08E0L,0x002000000001FFE0L});
    public static final BitSet FOLLOW_propertyValue_in_declaration3029 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
    public static final BitSet FOLLOW_prio_in_declaration3031 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_propertyValue3055 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IMPORTANT_SYM_in_prio3167 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_prio3169 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_expression3191 = new BitSet(new long[]{0x03F9C0003E3C48E2L,0x002000000001FFE0L});
    public static final BitSet FOLLOW_operator_in_expression3194 = new BitSet(new long[]{0x03F940003E3C08E0L,0x002000000001FFE0L});
    public static final BitSet FOLLOW_term_in_expression3196 = new BitSet(new long[]{0x03F9C0003E3C48E2L,0x002000000001FFE0L});
    public static final BitSet FOLLOW_unaryOperator_in_term3221 = new BitSet(new long[]{0x03F040003E3C08E0L,0x002000000001FFE0L});
    public static final BitSet FOLLOW_ws_in_term3223 = new BitSet(new long[]{0x03F040003E2C08E0L,0x0020000000007FE0L});
    public static final BitSet FOLLOW_set_in_term3247 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_STRING_in_term3446 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_IDENT_in_term3454 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_GEN_in_term3462 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_URI_in_term3470 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_hexColor_in_term3478 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_function_in_term3486 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_less_variable_in_term3494 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_term3506 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_function3522 = new BitSet(new long[]{0x0000000000100000L,0x0000000000018004L});
    public static final BitSet FOLLOW_ws_in_function3524 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_LPAREN_in_function3529 = new BitSet(new long[]{0x03F940003E3C08E0L,0x002000000001FFE0L});
    public static final BitSet FOLLOW_ws_in_function3531 = new BitSet(new long[]{0x03F940003E3C08E0L,0x002000000001FFE0L});
    public static final BitSet FOLLOW_expression_in_function3542 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_fnAttribute_in_function3560 = new BitSet(new long[]{0x0000000000004000L,0x0000000000000008L});
    public static final BitSet FOLLOW_COMMA_in_function3563 = new BitSet(new long[]{0x03F940003E3C08E0L,0x002000000001FFE0L});
    public static final BitSet FOLLOW_ws_in_function3565 = new BitSet(new long[]{0x03F940003E3C08E0L,0x002000000001FFE0L});
    public static final BitSet FOLLOW_fnAttribute_in_function3568 = new BitSet(new long[]{0x0000000000004000L,0x0000000000000008L});
    public static final BitSet FOLLOW_RPAREN_in_function3589 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_functionName3637 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_COLON_in_functionName3639 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_functionName3643 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_DOT_in_functionName3646 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_functionName3648 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_fnAttributeName_in_fnAttribute3670 = new BitSet(new long[]{0x0800000000100000L,0x0000000000018000L});
    public static final BitSet FOLLOW_ws_in_fnAttribute3672 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_OPEQ_in_fnAttribute3675 = new BitSet(new long[]{0x03F940003E3C08E0L,0x002000000001FFE0L});
    public static final BitSet FOLLOW_ws_in_fnAttribute3677 = new BitSet(new long[]{0x03F940003E3C08E0L,0x002000000001FFE0L});
    public static final BitSet FOLLOW_fnAttributeValue_in_fnAttribute3680 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName3695 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_DOT_in_fnAttributeName3698 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_IDENT_in_fnAttributeName3700 = new BitSet(new long[]{0x0020000000000002L});
    public static final BitSet FOLLOW_expression_in_fnAttributeValue3714 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HASH_in_hexColor3732 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_ws3753 = new BitSet(new long[]{0x0000000000100002L,0x0000000000018000L});
    public static final BitSet FOLLOW_less_variable_in_less_variable_declaration3788 = new BitSet(new long[]{0x0000400000100000L});
    public static final BitSet FOLLOW_WS_in_less_variable_declaration3790 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_COLON_in_less_variable_declaration3793 = new BitSet(new long[]{0x03F940003E3C08E0L,0x002000000001FFE0L});
    public static final BitSet FOLLOW_WS_in_less_variable_declaration3795 = new BitSet(new long[]{0x03F940003E3C08E0L,0x002000000001FFE0L});
    public static final BitSet FOLLOW_expression_in_less_variable_declaration3798 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_less_variable_declaration3800 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AT_IDENT_in_less_variable3821 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred1_Css31950 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_esPred_in_synpred2_Css31968 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_nsPred_in_synpred3_Css32070 = new BitSet(new long[]{0x0000000000000002L});

}